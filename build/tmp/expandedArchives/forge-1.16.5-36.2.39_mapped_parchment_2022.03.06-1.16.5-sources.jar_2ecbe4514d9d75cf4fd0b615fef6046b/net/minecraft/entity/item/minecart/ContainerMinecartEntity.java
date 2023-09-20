package net.minecraft.entity.item.minecart;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.piglin.PiglinTasks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public abstract class ContainerMinecartEntity extends AbstractMinecartEntity implements IInventory, INamedContainerProvider {
   private NonNullList<ItemStack> itemStacks = NonNullList.withSize(36, ItemStack.EMPTY);
   private boolean dropEquipment = true;
   @Nullable
   private ResourceLocation lootTable;
   private long lootTableSeed;

   protected ContainerMinecartEntity(EntityType<?> p_i48536_1_, World p_i48536_2_) {
      super(p_i48536_1_, p_i48536_2_);
   }

   protected ContainerMinecartEntity(EntityType<?> pEntityType, double pX, double pY, double pZ, World pLevel) {
      super(pEntityType, pLevel, pX, pY, pZ);
   }

   public void destroy(DamageSource pSource) {
      super.destroy(pSource);
      if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
         InventoryHelper.dropContents(this.level, this, this);
         if (!this.level.isClientSide) {
            Entity entity = pSource.getDirectEntity();
            if (entity != null && entity.getType() == EntityType.PLAYER) {
               PiglinTasks.angerNearbyPiglins((PlayerEntity)entity, true);
            }
         }
      }

   }

   public boolean isEmpty() {
      for(ItemStack itemstack : this.itemStacks) {
         if (!itemstack.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   /**
    * Returns the stack in the given slot.
    */
   public ItemStack getItem(int pIndex) {
      this.unpackLootTable((PlayerEntity)null);
      return this.itemStacks.get(pIndex);
   }

   /**
    * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
    */
   public ItemStack removeItem(int pIndex, int pCount) {
      this.unpackLootTable((PlayerEntity)null);
      return ItemStackHelper.removeItem(this.itemStacks, pIndex, pCount);
   }

   /**
    * Removes a stack from the given slot and returns it.
    */
   public ItemStack removeItemNoUpdate(int pIndex) {
      this.unpackLootTable((PlayerEntity)null);
      ItemStack itemstack = this.itemStacks.get(pIndex);
      if (itemstack.isEmpty()) {
         return ItemStack.EMPTY;
      } else {
         this.itemStacks.set(pIndex, ItemStack.EMPTY);
         return itemstack;
      }
   }

   /**
    * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
    */
   public void setItem(int pIndex, ItemStack pStack) {
      this.unpackLootTable((PlayerEntity)null);
      this.itemStacks.set(pIndex, pStack);
      if (!pStack.isEmpty() && pStack.getCount() > this.getMaxStackSize()) {
         pStack.setCount(this.getMaxStackSize());
      }

   }

   public boolean setSlot(int pSlotIndex, ItemStack pStack) {
      if (pSlotIndex >= 0 && pSlotIndex < this.getContainerSize()) {
         this.setItem(pSlotIndex, pStack);
         return true;
      } else {
         return false;
      }
   }

   /**
    * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
    * hasn't changed and skip it.
    */
   public void setChanged() {
   }

   /**
    * Don't rename this method to canInteractWith due to conflicts with Container
    */
   public boolean stillValid(PlayerEntity pPlayer) {
      if (this.removed) {
         return false;
      } else {
         return !(pPlayer.distanceToSqr(this) > 64.0D);
      }
   }

   @Nullable
   public Entity changeDimension(ServerWorld pServer, net.minecraftforge.common.util.ITeleporter teleporter) {
      this.dropEquipment = false;
      return super.changeDimension(pServer, teleporter);
   }

   @Override
   public void remove(boolean keepData) {
      if (!this.level.isClientSide && this.dropEquipment) {
         InventoryHelper.dropContents(this.level, this, this);
      }

      super.remove(keepData);
   }

   protected void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      if (this.lootTable != null) {
         pCompound.putString("LootTable", this.lootTable.toString());
         if (this.lootTableSeed != 0L) {
            pCompound.putLong("LootTableSeed", this.lootTableSeed);
         }
      } else {
         ItemStackHelper.saveAllItems(pCompound, this.itemStacks);
      }

   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   protected void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      if (pCompound.contains("LootTable", 8)) {
         this.lootTable = new ResourceLocation(pCompound.getString("LootTable"));
         this.lootTableSeed = pCompound.getLong("LootTableSeed");
      } else {
         ItemStackHelper.loadAllItems(pCompound, this.itemStacks);
      }

   }

   public ActionResultType interact(PlayerEntity pPlayer, Hand pHand) {
      ActionResultType ret = super.interact(pPlayer, pHand);
      if (ret.consumesAction()) return ret;
      pPlayer.openMenu(this);
      if (!pPlayer.level.isClientSide) {
         PiglinTasks.angerNearbyPiglins(pPlayer, true);
         return ActionResultType.CONSUME;
      } else {
         return ActionResultType.SUCCESS;
      }
   }

   protected void applyNaturalSlowdown() {
      float f = 0.98F;
      if (this.lootTable == null) {
         int i = 15 - Container.getRedstoneSignalFromContainer(this);
         f += (float)i * 0.001F;
      }

      this.setDeltaMovement(this.getDeltaMovement().multiply((double)f, 0.0D, (double)f));
   }

   /**
    * Adds loot to the minecart's contents.
    */
   public void unpackLootTable(@Nullable PlayerEntity pPlayer) {
      if (this.lootTable != null && this.level.getServer() != null) {
         LootTable loottable = this.level.getServer().getLootTables().get(this.lootTable);
         if (pPlayer instanceof ServerPlayerEntity) {
            CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayerEntity)pPlayer, this.lootTable);
         }

         this.lootTable = null;
         LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerWorld)this.level)).withParameter(LootParameters.ORIGIN, this.position()).withOptionalRandomSeed(this.lootTableSeed);
         // Forge: add this entity to loot context, however, currently Vanilla uses 'this' for the player creating the chests. So we take over 'killer_entity' for this.
         lootcontext$builder.withParameter(LootParameters.KILLER_ENTITY, this);
         if (pPlayer != null) {
            lootcontext$builder.withLuck(pPlayer.getLuck()).withParameter(LootParameters.THIS_ENTITY, pPlayer);
         }

         loottable.fill(this, lootcontext$builder.create(LootParameterSets.CHEST));
      }

   }

   public void clearContent() {
      this.unpackLootTable((PlayerEntity)null);
      this.itemStacks.clear();
   }

   public void setLootTable(ResourceLocation pLootTable, long pLootTableSeed) {
      this.lootTable = pLootTable;
      this.lootTableSeed = pLootTableSeed;
   }

   @Nullable
   public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {
      if (this.lootTable != null && p_createMenu_3_.isSpectator()) {
         return null;
      } else {
         this.unpackLootTable(p_createMenu_2_.player);
         return this.createMenu(p_createMenu_1_, p_createMenu_2_);
      }
   }

   protected abstract Container createMenu(int pContainerId, PlayerInventory pPlayerInventory);

   private net.minecraftforge.common.util.LazyOptional<?> itemHandler = net.minecraftforge.common.util.LazyOptional.of(() -> new net.minecraftforge.items.wrapper.InvWrapper(this));

   @Override
   public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable net.minecraft.util.Direction facing) {
      if (this.isAlive() && capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
         return itemHandler.cast();
      return super.getCapability(capability, facing);
   }

   @Override
   protected void invalidateCaps() {
      super.invalidateCaps();
      itemHandler.invalidate();
   }

   public void dropContentsWhenDead(boolean value) {
      this.dropEquipment = value;
   }
}
