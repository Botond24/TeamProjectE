package net.minecraft.item;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockItem extends Item {
   @Deprecated
   private final Block block;

   public BlockItem(Block pBlock, Item.Properties pProperties) {
      super(pProperties);
      this.block = pBlock;
   }

   /**
    * Called when this item is used when targetting a Block
    */
   public ActionResultType useOn(ItemUseContext pContext) {
      ActionResultType actionresulttype = this.place(new BlockItemUseContext(pContext));
      return !actionresulttype.consumesAction() && this.isEdible() ? this.use(pContext.getLevel(), pContext.getPlayer(), pContext.getHand()).getResult() : actionresulttype;
   }

   public ActionResultType place(BlockItemUseContext pContext) {
      if (!pContext.canPlace()) {
         return ActionResultType.FAIL;
      } else {
         BlockItemUseContext blockitemusecontext = this.updatePlacementContext(pContext);
         if (blockitemusecontext == null) {
            return ActionResultType.FAIL;
         } else {
            BlockState blockstate = this.getPlacementState(blockitemusecontext);
            if (blockstate == null) {
               return ActionResultType.FAIL;
            } else if (!this.placeBlock(blockitemusecontext, blockstate)) {
               return ActionResultType.FAIL;
            } else {
               BlockPos blockpos = blockitemusecontext.getClickedPos();
               World world = blockitemusecontext.getLevel();
               PlayerEntity playerentity = blockitemusecontext.getPlayer();
               ItemStack itemstack = blockitemusecontext.getItemInHand();
               BlockState blockstate1 = world.getBlockState(blockpos);
               Block block = blockstate1.getBlock();
               if (block == blockstate.getBlock()) {
                  blockstate1 = this.updateBlockStateFromTag(blockpos, world, itemstack, blockstate1);
                  this.updateCustomBlockEntityTag(blockpos, world, playerentity, itemstack, blockstate1);
                  block.setPlacedBy(world, blockpos, blockstate1, playerentity, itemstack);
                  if (playerentity instanceof ServerPlayerEntity) {
                     CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity)playerentity, blockpos, itemstack);
                  }
               }

               SoundType soundtype = blockstate1.getSoundType(world, blockpos, pContext.getPlayer());
               world.playSound(playerentity, blockpos, this.getPlaceSound(blockstate1, world, blockpos, pContext.getPlayer()), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
               if (playerentity == null || !playerentity.abilities.instabuild) {
                  itemstack.shrink(1);
               }

               return ActionResultType.sidedSuccess(world.isClientSide);
            }
         }
      }
   }

   @Deprecated //Forge: Use more sensitive version {@link BlockItem#getPlaceSound(BlockState, IBlockReader, BlockPos, Entity) }
   protected SoundEvent getPlaceSound(BlockState pState) {
      return pState.getSoundType().getPlaceSound();
   }

   //Forge: Sensitive version of BlockItem#getPlaceSound
   protected SoundEvent getPlaceSound(BlockState state, World world, BlockPos pos, PlayerEntity entity) {
      return state.getSoundType(world, pos, entity).getPlaceSound();
   }

   @Nullable
   public BlockItemUseContext updatePlacementContext(BlockItemUseContext pContext) {
      return pContext;
   }

   protected boolean updateCustomBlockEntityTag(BlockPos pPos, World pLevel, @Nullable PlayerEntity pPlayer, ItemStack pStack, BlockState pState) {
      return updateCustomBlockEntityTag(pLevel, pPlayer, pPos, pStack);
   }

   @Nullable
   protected BlockState getPlacementState(BlockItemUseContext pContext) {
      BlockState blockstate = this.getBlock().getStateForPlacement(pContext);
      return blockstate != null && this.canPlace(pContext, blockstate) ? blockstate : null;
   }

   private BlockState updateBlockStateFromTag(BlockPos pPos, World pLevel, ItemStack pStack, BlockState pState) {
      BlockState blockstate = pState;
      CompoundNBT compoundnbt = pStack.getTag();
      if (compoundnbt != null) {
         CompoundNBT compoundnbt1 = compoundnbt.getCompound("BlockStateTag");
         StateContainer<Block, BlockState> statecontainer = pState.getBlock().getStateDefinition();

         for(String s : compoundnbt1.getAllKeys()) {
            Property<?> property = statecontainer.getProperty(s);
            if (property != null) {
               String s1 = compoundnbt1.get(s).getAsString();
               blockstate = updateState(blockstate, property, s1);
            }
         }
      }

      if (blockstate != pState) {
         pLevel.setBlock(pPos, blockstate, 2);
      }

      return blockstate;
   }

   private static <T extends Comparable<T>> BlockState updateState(BlockState pState, Property<T> pProperty, String pValueIdentifier) {
      return pProperty.getValue(pValueIdentifier).map((p_219986_2_) -> {
         return pState.setValue(pProperty, p_219986_2_);
      }).orElse(pState);
   }

   protected boolean canPlace(BlockItemUseContext pContext, BlockState pState) {
      PlayerEntity playerentity = pContext.getPlayer();
      ISelectionContext iselectioncontext = playerentity == null ? ISelectionContext.empty() : ISelectionContext.of(playerentity);
      return (!this.mustSurvive() || pState.canSurvive(pContext.getLevel(), pContext.getClickedPos())) && pContext.getLevel().isUnobstructed(pState, pContext.getClickedPos(), iselectioncontext);
   }

   protected boolean mustSurvive() {
      return true;
   }

   protected boolean placeBlock(BlockItemUseContext pContext, BlockState pState) {
      return pContext.getLevel().setBlock(pContext.getClickedPos(), pState, 11);
   }

   public static boolean updateCustomBlockEntityTag(World pLevel, @Nullable PlayerEntity pPlayer, BlockPos pPos, ItemStack pStack) {
      MinecraftServer minecraftserver = pLevel.getServer();
      if (minecraftserver == null) {
         return false;
      } else {
         CompoundNBT compoundnbt = pStack.getTagElement("BlockEntityTag");
         if (compoundnbt != null) {
            TileEntity tileentity = pLevel.getBlockEntity(pPos);
            if (tileentity != null) {
               if (!pLevel.isClientSide && tileentity.onlyOpCanSetNbt() && (pPlayer == null || !pPlayer.canUseGameMasterBlocks())) {
                  return false;
               }

               CompoundNBT compoundnbt1 = tileentity.save(new CompoundNBT());
               CompoundNBT compoundnbt2 = compoundnbt1.copy();
               compoundnbt1.merge(compoundnbt);
               compoundnbt1.putInt("x", pPos.getX());
               compoundnbt1.putInt("y", pPos.getY());
               compoundnbt1.putInt("z", pPos.getZ());
               if (!compoundnbt1.equals(compoundnbt2)) {
                  tileentity.load(pLevel.getBlockState(pPos), compoundnbt1);
                  tileentity.setChanged();
                  return true;
               }
            }
         }

         return false;
      }
   }

   /**
    * Returns the unlocalized name of this item.
    */
   public String getDescriptionId() {
      return this.getBlock().getDescriptionId();
   }

   /**
    * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
    */
   public void fillItemCategory(ItemGroup pGroup, NonNullList<ItemStack> pItems) {
      if (this.allowdedIn(pGroup)) {
         this.getBlock().fillItemCategory(pGroup, pItems);
      }

   }

   /**
    * allows items to add custom lines of information to the mouseover description
    */
   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(ItemStack pStack, @Nullable World pLevel, List<ITextComponent> pTooltip, ITooltipFlag pFlag) {
      super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
      this.getBlock().appendHoverText(pStack, pLevel, pTooltip, pFlag);
   }

   public Block getBlock() {
      return this.getBlockRaw() == null ? null : this.getBlockRaw().delegate.get();
   }

   private Block getBlockRaw() {
      return this.block;
   }

   public void registerBlocks(Map<Block, Item> pBlockToItemMap, Item pItem) {
      pBlockToItemMap.put(this.getBlock(), pItem);
   }

   public void removeFromBlockToItemMap(Map<Block, Item> blockToItemMap, Item itemIn) {
      blockToItemMap.remove(this.getBlock());
   }
}
