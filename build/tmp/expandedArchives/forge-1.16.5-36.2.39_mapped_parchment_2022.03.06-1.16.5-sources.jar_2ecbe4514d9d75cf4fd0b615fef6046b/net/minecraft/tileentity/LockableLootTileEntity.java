package net.minecraft.tileentity;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.server.ServerWorld;

public abstract class LockableLootTileEntity extends LockableTileEntity {
   @Nullable
   protected ResourceLocation lootTable;
   protected long lootTableSeed;

   protected LockableLootTileEntity(TileEntityType<?> p_i48284_1_) {
      super(p_i48284_1_);
   }

   public static void setLootTable(IBlockReader pLevel, Random pRandom, BlockPos pPos, ResourceLocation pLootTable) {
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      if (tileentity instanceof LockableLootTileEntity) {
         ((LockableLootTileEntity)tileentity).setLootTable(pLootTable, pRandom.nextLong());
      }

   }

   protected boolean tryLoadLootTable(CompoundNBT pTag) {
      if (pTag.contains("LootTable", 8)) {
         this.lootTable = new ResourceLocation(pTag.getString("LootTable"));
         this.lootTableSeed = pTag.getLong("LootTableSeed");
         return true;
      } else {
         return false;
      }
   }

   protected boolean trySaveLootTable(CompoundNBT pTag) {
      if (this.lootTable == null) {
         return false;
      } else {
         pTag.putString("LootTable", this.lootTable.toString());
         if (this.lootTableSeed != 0L) {
            pTag.putLong("LootTableSeed", this.lootTableSeed);
         }

         return true;
      }
   }

   public void unpackLootTable(@Nullable PlayerEntity pPlayer) {
      if (this.lootTable != null && this.level.getServer() != null) {
         LootTable loottable = this.level.getServer().getLootTables().get(this.lootTable);
         if (pPlayer instanceof ServerPlayerEntity) {
            CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayerEntity)pPlayer, this.lootTable);
         }

         this.lootTable = null;
         LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerWorld)this.level)).withParameter(LootParameters.ORIGIN, Vector3d.atCenterOf(this.worldPosition)).withOptionalRandomSeed(this.lootTableSeed);
         if (pPlayer != null) {
            lootcontext$builder.withLuck(pPlayer.getLuck()).withParameter(LootParameters.THIS_ENTITY, pPlayer);
         }

         loottable.fill(this, lootcontext$builder.create(LootParameterSets.CHEST));
      }

   }

   public void setLootTable(ResourceLocation pLootTable, long pLootTableSeed) {
      this.lootTable = pLootTable;
      this.lootTableSeed = pLootTableSeed;
   }

   public boolean isEmpty() {
      this.unpackLootTable((PlayerEntity)null);
      return this.getItems().stream().allMatch(ItemStack::isEmpty);
   }

   /**
    * Returns the stack in the given slot.
    */
   public ItemStack getItem(int pIndex) {
      this.unpackLootTable((PlayerEntity)null);
      return this.getItems().get(pIndex);
   }

   /**
    * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
    */
   public ItemStack removeItem(int pIndex, int pCount) {
      this.unpackLootTable((PlayerEntity)null);
      ItemStack itemstack = ItemStackHelper.removeItem(this.getItems(), pIndex, pCount);
      if (!itemstack.isEmpty()) {
         this.setChanged();
      }

      return itemstack;
   }

   /**
    * Removes a stack from the given slot and returns it.
    */
   public ItemStack removeItemNoUpdate(int pIndex) {
      this.unpackLootTable((PlayerEntity)null);
      return ItemStackHelper.takeItem(this.getItems(), pIndex);
   }

   /**
    * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
    */
   public void setItem(int pIndex, ItemStack pStack) {
      this.unpackLootTable((PlayerEntity)null);
      this.getItems().set(pIndex, pStack);
      if (pStack.getCount() > this.getMaxStackSize()) {
         pStack.setCount(this.getMaxStackSize());
      }

      this.setChanged();
   }

   /**
    * Don't rename this method to canInteractWith due to conflicts with Container
    */
   public boolean stillValid(PlayerEntity pPlayer) {
      if (this.level.getBlockEntity(this.worldPosition) != this) {
         return false;
      } else {
         return !(pPlayer.distanceToSqr((double)this.worldPosition.getX() + 0.5D, (double)this.worldPosition.getY() + 0.5D, (double)this.worldPosition.getZ() + 0.5D) > 64.0D);
      }
   }

   public void clearContent() {
      this.getItems().clear();
   }

   protected abstract NonNullList<ItemStack> getItems();

   protected abstract void setItems(NonNullList<ItemStack> pItems);

   public boolean canOpen(PlayerEntity pPlayer) {
      return super.canOpen(pPlayer) && (this.lootTable == null || !pPlayer.isSpectator());
   }

   @Nullable
   public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {
      if (this.canOpen(p_createMenu_3_)) {
         this.unpackLootTable(p_createMenu_2_.player);
         return this.createMenu(p_createMenu_1_, p_createMenu_2_);
      } else {
         return null;
      }
   }
}