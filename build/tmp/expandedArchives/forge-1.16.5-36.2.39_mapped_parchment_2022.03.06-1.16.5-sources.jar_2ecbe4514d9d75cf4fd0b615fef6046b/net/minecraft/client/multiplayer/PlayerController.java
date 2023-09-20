package net.minecraft.client.multiplayer;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CommandBlockBlock;
import net.minecraft.block.JigsawBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.StructureBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.util.ClientRecipeBook;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CCreativeInventoryActionPacket;
import net.minecraft.network.play.client.CEnchantItemPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPickItemPacket;
import net.minecraft.network.play.client.CPlaceRecipePacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class PlayerController {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Minecraft minecraft;
   private final ClientPlayNetHandler connection;
   private BlockPos destroyBlockPos = new BlockPos(-1, -1, -1);
   private ItemStack destroyingItem = ItemStack.EMPTY;
   private float destroyProgress;
   private float destroyTicks;
   private int destroyDelay;
   private boolean isDestroying;
   private GameType localPlayerMode = GameType.SURVIVAL;
   private GameType previousLocalPlayerMode = GameType.NOT_SET;
   private final Object2ObjectLinkedOpenHashMap<Pair<BlockPos, CPlayerDiggingPacket.Action>, Vector3d> unAckedActions = new Object2ObjectLinkedOpenHashMap<>();
   private int carriedIndex;

   public PlayerController(Minecraft p_i45062_1_, ClientPlayNetHandler p_i45062_2_) {
      this.minecraft = p_i45062_1_;
      this.connection = p_i45062_2_;
   }

   /**
    * Sets player capabilities depending on current gametype. params: player
    */
   public void adjustPlayer(PlayerEntity pPlayer) {
      this.localPlayerMode.updatePlayerAbilities(pPlayer.abilities);
   }

   public void setPreviousLocalMode(GameType p_241675_1_) {
      this.previousLocalPlayerMode = p_241675_1_;
   }

   /**
    * Sets the game type for the player.
    */
   public void setLocalMode(GameType pType) {
      if (pType != this.localPlayerMode) {
         this.previousLocalPlayerMode = this.localPlayerMode;
      }

      this.localPlayerMode = pType;
      this.localPlayerMode.updatePlayerAbilities(this.minecraft.player.abilities);
   }

   public boolean canHurtPlayer() {
      return this.localPlayerMode.isSurvival();
   }

   public boolean destroyBlock(BlockPos pPos) {
      if (minecraft.player.getMainHandItem().onBlockStartBreak(pPos, minecraft.player)) return false;
      if (this.minecraft.player.blockActionRestricted(this.minecraft.level, pPos, this.localPlayerMode)) {
         return false;
      } else {
         World world = this.minecraft.level;
         BlockState blockstate = world.getBlockState(pPos);
         if (!this.minecraft.player.getMainHandItem().getItem().canAttackBlock(blockstate, world, pPos, this.minecraft.player)) {
            return false;
         } else {
            Block block = blockstate.getBlock();
            if ((block instanceof CommandBlockBlock || block instanceof StructureBlock || block instanceof JigsawBlock) && !this.minecraft.player.canUseGameMasterBlocks()) {
               return false;
            } else if (blockstate.isAir(world, pPos)) {
               return false;
            } else {
               FluidState fluidstate = world.getFluidState(pPos);
               boolean flag = blockstate.removedByPlayer(world, pPos, minecraft.player, false, fluidstate);
               if (flag) {
                  block.destroy(world, pPos, blockstate);
               }

               return flag;
            }
         }
      }
   }

   /**
    * Called when the player is hitting a block with an item.
    */
   public boolean startDestroyBlock(BlockPos pLoc, Direction pFace) {
      if (this.minecraft.player.blockActionRestricted(this.minecraft.level, pLoc, this.localPlayerMode)) {
         return false;
      } else if (!this.minecraft.level.getWorldBorder().isWithinBounds(pLoc)) {
         return false;
      } else {
         if (this.localPlayerMode.isCreative()) {
            BlockState blockstate = this.minecraft.level.getBlockState(pLoc);
            this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, pLoc, blockstate, 1.0F);
            this.sendBlockAction(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, pLoc, pFace);
            if (!net.minecraftforge.common.ForgeHooks.onLeftClickBlock(this.minecraft.player, pLoc, pFace).isCanceled())
            this.destroyBlock(pLoc);
            this.destroyDelay = 5;
         } else if (!this.isDestroying || !this.sameDestroyTarget(pLoc)) {
            if (this.isDestroying) {
               this.sendBlockAction(CPlayerDiggingPacket.Action.ABORT_DESTROY_BLOCK, this.destroyBlockPos, pFace);
            }
            net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock event = net.minecraftforge.common.ForgeHooks.onLeftClickBlock(this.minecraft.player, pLoc, pFace);

            BlockState blockstate1 = this.minecraft.level.getBlockState(pLoc);
            this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, pLoc, blockstate1, 0.0F);
            this.sendBlockAction(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, pLoc, pFace);
            boolean flag = !blockstate1.isAir(this.minecraft.level, pLoc);
            if (flag && this.destroyProgress == 0.0F) {
               if (event.getUseBlock() != net.minecraftforge.eventbus.api.Event.Result.DENY)
               blockstate1.attack(this.minecraft.level, pLoc, this.minecraft.player);
            }

            if (event.getUseItem() == net.minecraftforge.eventbus.api.Event.Result.DENY) return true;
            if (flag && blockstate1.getDestroyProgress(this.minecraft.player, this.minecraft.player.level, pLoc) >= 1.0F) {
               this.destroyBlock(pLoc);
            } else {
               this.isDestroying = true;
               this.destroyBlockPos = pLoc;
               this.destroyingItem = this.minecraft.player.getMainHandItem();
               this.destroyProgress = 0.0F;
               this.destroyTicks = 0.0F;
               this.minecraft.level.destroyBlockProgress(this.minecraft.player.getId(), this.destroyBlockPos, (int)(this.destroyProgress * 10.0F) - 1);
            }
         }

         return true;
      }
   }

   /**
    * Resets current block damage
    */
   public void stopDestroyBlock() {
      if (this.isDestroying) {
         BlockState blockstate = this.minecraft.level.getBlockState(this.destroyBlockPos);
         this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, this.destroyBlockPos, blockstate, -1.0F);
         this.sendBlockAction(CPlayerDiggingPacket.Action.ABORT_DESTROY_BLOCK, this.destroyBlockPos, Direction.DOWN);
         this.isDestroying = false;
         this.destroyProgress = 0.0F;
         this.minecraft.level.destroyBlockProgress(this.minecraft.player.getId(), this.destroyBlockPos, -1);
         this.minecraft.player.resetAttackStrengthTicker();
      }

   }

   public boolean continueDestroyBlock(BlockPos pPosBlock, Direction pDirectionFacing) {
      this.ensureHasSentCarriedItem();
      if (this.destroyDelay > 0) {
         --this.destroyDelay;
         return true;
      } else if (this.localPlayerMode.isCreative() && this.minecraft.level.getWorldBorder().isWithinBounds(pPosBlock)) {
         this.destroyDelay = 5;
         BlockState blockstate1 = this.minecraft.level.getBlockState(pPosBlock);
         this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, pPosBlock, blockstate1, 1.0F);
         this.sendBlockAction(CPlayerDiggingPacket.Action.START_DESTROY_BLOCK, pPosBlock, pDirectionFacing);
         if (!net.minecraftforge.common.ForgeHooks.onLeftClickBlock(this.minecraft.player, pPosBlock, pDirectionFacing).isCanceled())
         this.destroyBlock(pPosBlock);
         return true;
      } else if (this.sameDestroyTarget(pPosBlock)) {
         BlockState blockstate = this.minecraft.level.getBlockState(pPosBlock);
         if (blockstate.isAir(this.minecraft.level, pPosBlock)) {
            this.isDestroying = false;
            return false;
         } else {
            this.destroyProgress += blockstate.getDestroyProgress(this.minecraft.player, this.minecraft.player.level, pPosBlock);
            if (this.destroyTicks % 4.0F == 0.0F) {
               SoundType soundtype = blockstate.getSoundType(this.minecraft.level, pPosBlock, this.minecraft.player);
               this.minecraft.getSoundManager().play(new SimpleSound(soundtype.getHitSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 8.0F, soundtype.getPitch() * 0.5F, pPosBlock));
            }

            ++this.destroyTicks;
            this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, pPosBlock, blockstate, MathHelper.clamp(this.destroyProgress, 0.0F, 1.0F));
            if (net.minecraftforge.common.ForgeHooks.onLeftClickBlock(this.minecraft.player, pPosBlock, pDirectionFacing).getUseItem() == net.minecraftforge.eventbus.api.Event.Result.DENY) return true;
            if (this.destroyProgress >= 1.0F) {
               this.isDestroying = false;
               this.sendBlockAction(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, pPosBlock, pDirectionFacing);
               this.destroyBlock(pPosBlock);
               this.destroyProgress = 0.0F;
               this.destroyTicks = 0.0F;
               this.destroyDelay = 5;
            }

            this.minecraft.level.destroyBlockProgress(this.minecraft.player.getId(), this.destroyBlockPos, (int)(this.destroyProgress * 10.0F) - 1);
            return true;
         }
      } else {
         return this.startDestroyBlock(pPosBlock, pDirectionFacing);
      }
   }

   /**
    * player reach distance = 4F
    */
   public float getPickRange() {
      float attrib = (float)minecraft.player.getAttribute(net.minecraftforge.common.ForgeMod.REACH_DISTANCE.get()).getValue();
      return this.localPlayerMode.isCreative() ? attrib : attrib - 0.5F;
   }

   public void tick() {
      this.ensureHasSentCarriedItem();
      if (this.connection.getConnection().isConnected()) {
         this.connection.getConnection().tick();
      } else {
         this.connection.getConnection().handleDisconnection();
      }

   }

   private boolean sameDestroyTarget(BlockPos pPos) {
      ItemStack itemstack = this.minecraft.player.getMainHandItem();
      boolean flag = this.destroyingItem.isEmpty() && itemstack.isEmpty();
      if (!this.destroyingItem.isEmpty() && !itemstack.isEmpty()) {
         flag = !this.destroyingItem.shouldCauseBlockBreakReset(itemstack);
      }

      return pPos.equals(this.destroyBlockPos) && flag;
   }

   /**
    * Syncs the current player item with the server
    */
   private void ensureHasSentCarriedItem() {
      int i = this.minecraft.player.inventory.selected;
      if (i != this.carriedIndex) {
         this.carriedIndex = i;
         this.connection.send(new CHeldItemChangePacket(this.carriedIndex));
      }

   }

   public ActionResultType useItemOn(ClientPlayerEntity p_217292_1_, ClientWorld p_217292_2_, Hand p_217292_3_, BlockRayTraceResult p_217292_4_) {
      this.ensureHasSentCarriedItem();
      BlockPos blockpos = p_217292_4_.getBlockPos();
      if (!this.minecraft.level.getWorldBorder().isWithinBounds(blockpos)) {
         return ActionResultType.FAIL;
      } else {
         ItemStack itemstack = p_217292_1_.getItemInHand(p_217292_3_);
         net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock event = net.minecraftforge.common.ForgeHooks
                 .onRightClickBlock(p_217292_1_, p_217292_3_, blockpos, p_217292_4_);
         if (event.isCanceled()) {
            this.connection.send(new CPlayerTryUseItemOnBlockPacket(p_217292_3_, p_217292_4_));
            return event.getCancellationResult();
         }
         if (this.localPlayerMode == GameType.SPECTATOR) {
            this.connection.send(new CPlayerTryUseItemOnBlockPacket(p_217292_3_, p_217292_4_));
            return ActionResultType.SUCCESS;
         } else {
            ItemUseContext itemusecontext = new ItemUseContext(p_217292_1_, p_217292_3_, p_217292_4_);
            if (event.getUseItem() != net.minecraftforge.eventbus.api.Event.Result.DENY) {
               ActionResultType result = itemstack.onItemUseFirst(itemusecontext);
               if (result != ActionResultType.PASS) {
                  this.connection.send(new CPlayerTryUseItemOnBlockPacket(p_217292_3_, p_217292_4_));
                  return result;
               }
            }
            boolean flag = !p_217292_1_.getMainHandItem().doesSneakBypassUse(p_217292_2_,blockpos,p_217292_1_) || !p_217292_1_.getOffhandItem().doesSneakBypassUse(p_217292_2_,blockpos,p_217292_1_);
            boolean flag1 = p_217292_1_.isSecondaryUseActive() && flag;
            if (event.getUseBlock() == net.minecraftforge.eventbus.api.Event.Result.ALLOW || (event.getUseBlock() != net.minecraftforge.eventbus.api.Event.Result.DENY && !flag1)) {
               ActionResultType actionresulttype = p_217292_2_.getBlockState(blockpos).use(p_217292_2_, p_217292_1_, p_217292_3_, p_217292_4_);
               if (actionresulttype.consumesAction()) {
                  this.connection.send(new CPlayerTryUseItemOnBlockPacket(p_217292_3_, p_217292_4_));
                  return actionresulttype;
               }
            }

            this.connection.send(new CPlayerTryUseItemOnBlockPacket(p_217292_3_, p_217292_4_));
            if (event.getUseItem() == net.minecraftforge.eventbus.api.Event.Result.DENY) return ActionResultType.PASS;
            if (event.getUseItem() == net.minecraftforge.eventbus.api.Event.Result.ALLOW || (!itemstack.isEmpty() && !p_217292_1_.getCooldowns().isOnCooldown(itemstack.getItem()))) {
               ActionResultType actionresulttype1;
               if (this.localPlayerMode.isCreative()) {
                  int i = itemstack.getCount();
                  actionresulttype1 = itemstack.useOn(itemusecontext);
                  itemstack.setCount(i);
               } else {
                  actionresulttype1 = itemstack.useOn(itemusecontext);
               }

               return actionresulttype1;
            } else {
               return ActionResultType.PASS;
            }
         }
      }
   }

   public ActionResultType useItem(PlayerEntity pPlayer, World pLevel, Hand pHand) {
      if (this.localPlayerMode == GameType.SPECTATOR) {
         return ActionResultType.PASS;
      } else {
         this.ensureHasSentCarriedItem();
         this.connection.send(new CPlayerTryUseItemPacket(pHand));
         ItemStack itemstack = pPlayer.getItemInHand(pHand);
         if (pPlayer.getCooldowns().isOnCooldown(itemstack.getItem())) {
            return ActionResultType.PASS;
         } else {
            ActionResultType cancelResult = net.minecraftforge.common.ForgeHooks.onItemRightClick(pPlayer, pHand);
            if (cancelResult != null) return cancelResult;
            int i = itemstack.getCount();
            ActionResult<ItemStack> actionresult = itemstack.use(pLevel, pPlayer, pHand);
            ItemStack itemstack1 = actionresult.getObject();
            if (itemstack1 != itemstack) {
               pPlayer.setItemInHand(pHand, itemstack1);
               if (itemstack1.isEmpty()) net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(pPlayer, itemstack, pHand);
            }

            return actionresult.getResult();
         }
      }
   }

   public ClientPlayerEntity createPlayer(ClientWorld pLevel, StatisticsManager pStatsManager, ClientRecipeBook pRecipes) {
      return this.createPlayer(pLevel, pStatsManager, pRecipes, false, false);
   }

   public ClientPlayerEntity createPlayer(ClientWorld pLevel, StatisticsManager pStatsManager, ClientRecipeBook pRecipes, boolean p_239167_4_, boolean p_239167_5_) {
      return new ClientPlayerEntity(this.minecraft, pLevel, this.connection, pStatsManager, pRecipes, p_239167_4_, p_239167_5_);
   }

   /**
    * Attacks an entity
    */
   public void attack(PlayerEntity pPlayer, Entity pTargetEntity) {
      this.ensureHasSentCarriedItem();
      this.connection.send(new CUseEntityPacket(pTargetEntity, pPlayer.isShiftKeyDown()));
      if (this.localPlayerMode != GameType.SPECTATOR) {
         pPlayer.attack(pTargetEntity);
         pPlayer.resetAttackStrengthTicker();
      }

   }

   /**
    * Handles right clicking an entity, sends a packet to the server.
    */
   public ActionResultType interact(PlayerEntity pPlayer, Entity pTarget, Hand pHand) {
      this.ensureHasSentCarriedItem();
      this.connection.send(new CUseEntityPacket(pTarget, pHand, pPlayer.isShiftKeyDown()));
      if (this.localPlayerMode == GameType.SPECTATOR) return ActionResultType.PASS; // don't fire for spectators to match non-specific EntityInteract
      ActionResultType cancelResult = net.minecraftforge.common.ForgeHooks.onInteractEntity(pPlayer, pTarget, pHand);
      if(cancelResult != null) return cancelResult;
      return this.localPlayerMode == GameType.SPECTATOR ? ActionResultType.PASS : pPlayer.interactOn(pTarget, pHand);
   }

   /**
    * Handles right clicking an entity from the entities side, sends a packet to the server.
    */
   public ActionResultType interactAt(PlayerEntity pPlayer, Entity pTarget, EntityRayTraceResult pRay, Hand pHand) {
      this.ensureHasSentCarriedItem();
      Vector3d vector3d = pRay.getLocation().subtract(pTarget.getX(), pTarget.getY(), pTarget.getZ());
      this.connection.send(new CUseEntityPacket(pTarget, pHand, vector3d, pPlayer.isShiftKeyDown()));
      if (this.localPlayerMode == GameType.SPECTATOR) return ActionResultType.PASS; // don't fire for spectators to match non-specific EntityInteract
      ActionResultType cancelResult = net.minecraftforge.common.ForgeHooks.onInteractEntityAt(pPlayer, pTarget, pRay, pHand);
      if(cancelResult != null) return cancelResult;
      return this.localPlayerMode == GameType.SPECTATOR ? ActionResultType.PASS : pTarget.interactAt(pPlayer, vector3d, pHand);
   }

   public ItemStack handleInventoryMouseClick(int p_187098_1_, int p_187098_2_, int p_187098_3_, ClickType p_187098_4_, PlayerEntity p_187098_5_) {
      short short1 = p_187098_5_.containerMenu.backup(p_187098_5_.inventory);
      ItemStack itemstack = p_187098_5_.containerMenu.clicked(p_187098_2_, p_187098_3_, p_187098_4_, p_187098_5_);
      this.connection.send(new CClickWindowPacket(p_187098_1_, p_187098_2_, p_187098_3_, p_187098_4_, itemstack, short1));
      return itemstack;
   }

   public void handlePlaceRecipe(int p_203413_1_, IRecipe<?> pRecipe, boolean pPlaceAll) {
      this.connection.send(new CPlaceRecipePacket(p_203413_1_, pRecipe, pPlaceAll));
   }

   /**
    * GuiEnchantment uses this during multiplayer to tell PlayerControllerMP to send a packet indicating the enchantment
    * action the player has taken.
    */
   public void handleInventoryButtonClick(int pWindowID, int pButton) {
      this.connection.send(new CEnchantItemPacket(pWindowID, pButton));
   }

   /**
    * Used in PlayerControllerMP to update the server with an ItemStack in a slot.
    */
   public void handleCreativeModeItemAdd(ItemStack pItemStack, int pSlotId) {
      if (this.localPlayerMode.isCreative()) {
         this.connection.send(new CCreativeInventoryActionPacket(pSlotId, pItemStack));
      }

   }

   /**
    * Sends a Packet107 to the server to drop the item on the ground
    */
   public void handleCreativeModeItemDrop(ItemStack pItemStack) {
      if (this.localPlayerMode.isCreative() && !pItemStack.isEmpty()) {
         this.connection.send(new CCreativeInventoryActionPacket(-1, pItemStack));
      }

   }

   public void releaseUsingItem(PlayerEntity pPlayer) {
      this.ensureHasSentCarriedItem();
      this.connection.send(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
      pPlayer.releaseUsingItem();
   }

   public boolean hasExperience() {
      return this.localPlayerMode.isSurvival();
   }

   /**
    * Checks if the player is not creative, used for checking if it should break a block instantly
    */
   public boolean hasMissTime() {
      return !this.localPlayerMode.isCreative();
   }

   /**
    * returns true if player is in creative mode
    */
   public boolean hasInfiniteItems() {
      return this.localPlayerMode.isCreative();
   }

   /**
    * true for hitting entities far away.
    */
   public boolean hasFarPickRange() {
      return this.localPlayerMode.isCreative();
   }

   /**
    * Checks if the player is riding a horse, used to chose the GUI to open
    */
   public boolean isServerControlledInventory() {
      return this.minecraft.player.isPassenger() && this.minecraft.player.getVehicle() instanceof AbstractHorseEntity;
   }

   public boolean isAlwaysFlying() {
      return this.localPlayerMode == GameType.SPECTATOR;
   }

   public GameType getPreviousPlayerMode() {
      return this.previousLocalPlayerMode;
   }

   public GameType getPlayerMode() {
      return this.localPlayerMode;
   }

   /**
    * Return isHittingBlock
    */
   public boolean isDestroying() {
      return this.isDestroying;
   }

   public void handlePickItem(int pIndex) {
      this.connection.send(new CPickItemPacket(pIndex));
   }

   private void sendBlockAction(CPlayerDiggingPacket.Action pAction, BlockPos pPos, Direction pDir) {
      ClientPlayerEntity clientplayerentity = this.minecraft.player;
      this.unAckedActions.put(Pair.of(pPos, pAction), clientplayerentity.position());
      this.connection.send(new CPlayerDiggingPacket(pAction, pPos, pDir));
   }

   public void handleBlockBreakAck(ClientWorld pLevel, BlockPos pPos, BlockState pBlock, CPlayerDiggingPacket.Action pAction, boolean pSuccessful) {
      Vector3d vector3d = this.unAckedActions.remove(Pair.of(pPos, pAction));
      BlockState blockstate = pLevel.getBlockState(pPos);
      if ((vector3d == null || !pSuccessful || pAction != CPlayerDiggingPacket.Action.START_DESTROY_BLOCK && blockstate != pBlock) && blockstate != pBlock) {
         pLevel.setKnownState(pPos, pBlock);
         PlayerEntity playerentity = this.minecraft.player;
         if (vector3d != null && pLevel == playerentity.level && playerentity.isColliding(pPos, pBlock)) {
            playerentity.absMoveTo(vector3d.x, vector3d.y, vector3d.z);
         }
      }

      while(this.unAckedActions.size() >= 50) {
         Pair<BlockPos, CPlayerDiggingPacket.Action> pair = this.unAckedActions.firstKey();
         this.unAckedActions.removeFirst();
         LOGGER.error("Too many unacked block actions, dropping " + pair);
      }

   }
}
