package net.minecraft.inventory.container;

import com.mojang.datafixers.util.Pair;
import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Slot {
   private final int slot;
   public final IInventory container;
   public int index;
   public final int x;
   public final int y;

   public Slot(IInventory pContainer, int pIndex, int pX, int pY) {
      this.container = pContainer;
      this.slot = pIndex;
      this.x = pX;
      this.y = pY;
   }

   /**
    * if par2 has more items than par1, onCrafting(item,countIncrease) is called
    */
   public void onQuickCraft(ItemStack pOldStack, ItemStack pNewStack) {
      int i = pNewStack.getCount() - pOldStack.getCount();
      if (i > 0) {
         this.onQuickCraft(pNewStack, i);
      }

   }

   /**
    * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood. Typically increases an
    * internal count then calls onCrafting(item).
    */
   protected void onQuickCraft(ItemStack pStack, int pAmount) {
   }

   protected void onSwapCraft(int pNumItemsCrafted) {
   }

   /**
    * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood.
    */
   protected void checkTakeAchievements(ItemStack pStack) {
   }

   public ItemStack onTake(PlayerEntity pPlayer, ItemStack pStack) {
      this.setChanged();
      return pStack;
   }

   /**
    * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
    */
   public boolean mayPlace(ItemStack pStack) {
      return true;
   }

   /**
    * Helper fnct to get the stack in the slot.
    */
   public ItemStack getItem() {
      return this.container.getItem(this.slot);
   }

   /**
    * Returns if this slot contains a stack.
    */
   public boolean hasItem() {
      return !this.getItem().isEmpty();
   }

   /**
    * Helper method to put a stack in the slot.
    */
   public void set(ItemStack pStack) {
      this.container.setItem(this.slot, pStack);
      this.setChanged();
   }

   /**
    * Called when the stack in a Slot changes
    */
   public void setChanged() {
      this.container.setChanged();
   }

   /**
    * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in the case
    * of armor slots)
    */
   public int getMaxStackSize() {
      return this.container.getMaxStackSize();
   }

   public int getMaxStackSize(ItemStack pStack) {
      return this.getMaxStackSize();
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
      return backgroundPair;
   }

   /**
    * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new stack.
    */
   public ItemStack remove(int pAmount) {
      return this.container.removeItem(this.slot, pAmount);
   }

   /**
    * Return whether this slot's stack can be taken from this slot.
    */
   public boolean mayPickup(PlayerEntity pPlayer) {
      return true;
   }

   /**
    * Actualy only call when we want to render the white square effect over the slots. Return always True, except for
    * the armor slot of the Donkey/Mule (we can't interact with the Undead and Skeleton horses)
    */
   @OnlyIn(Dist.CLIENT)
   public boolean isActive() {
      return true;
   }

   /**
    * Retrieves the index in the inventory for this slot, this value should typically not
    * be used, but can be useful for some occasions.
    *
    * @return Index in associated inventory for this slot.
    */
   public int getSlotIndex() {
      return slot;
   }

   /**
    * Checks if the other slot is in the same inventory, by comparing the inventory reference.
    * @param other
    * @return true if the other slot is in the same inventory
    */
   public boolean isSameInventory(Slot other) {
      return this.container == other.container;
   }

   private Pair<ResourceLocation, ResourceLocation> backgroundPair;
   /**
    * Sets the background atlas and sprite location.
    *
    * @param atlas The atlas name
    * @param sprite The sprite located on that atlas.
    * @return this, to allow chaining.
    */
   public Slot setBackground(ResourceLocation atlas, ResourceLocation sprite) {
       this.backgroundPair = Pair.of(atlas, sprite);
       return this;
   }
}
