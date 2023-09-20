package net.minecraft.inventory.container;

import com.mojang.datafixers.util.Pair;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeBookCategory;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PlayerContainer extends RecipeBookContainer<CraftingInventory> {
   public static final ResourceLocation BLOCK_ATLAS = new ResourceLocation("textures/atlas/blocks.png");
   public static final ResourceLocation EMPTY_ARMOR_SLOT_HELMET = new ResourceLocation("item/empty_armor_slot_helmet");
   public static final ResourceLocation EMPTY_ARMOR_SLOT_CHESTPLATE = new ResourceLocation("item/empty_armor_slot_chestplate");
   public static final ResourceLocation EMPTY_ARMOR_SLOT_LEGGINGS = new ResourceLocation("item/empty_armor_slot_leggings");
   public static final ResourceLocation EMPTY_ARMOR_SLOT_BOOTS = new ResourceLocation("item/empty_armor_slot_boots");
   public static final ResourceLocation EMPTY_ARMOR_SLOT_SHIELD = new ResourceLocation("item/empty_armor_slot_shield");
   private static final ResourceLocation[] TEXTURE_EMPTY_SLOTS = new ResourceLocation[]{EMPTY_ARMOR_SLOT_BOOTS, EMPTY_ARMOR_SLOT_LEGGINGS, EMPTY_ARMOR_SLOT_CHESTPLATE, EMPTY_ARMOR_SLOT_HELMET};
   private static final EquipmentSlotType[] SLOT_IDS = new EquipmentSlotType[]{EquipmentSlotType.HEAD, EquipmentSlotType.CHEST, EquipmentSlotType.LEGS, EquipmentSlotType.FEET};
   private final CraftingInventory craftSlots = new CraftingInventory(this, 2, 2);
   private final CraftResultInventory resultSlots = new CraftResultInventory();
   public final boolean active;
   private final PlayerEntity owner;

   public PlayerContainer(PlayerInventory pPlayerInventory, boolean pActive, PlayerEntity pOwner) {
      super((ContainerType<?>)null, 0);
      this.active = pActive;
      this.owner = pOwner;
      this.addSlot(new CraftingResultSlot(pPlayerInventory.player, this.craftSlots, this.resultSlots, 0, 154, 28));

      for(int i = 0; i < 2; ++i) {
         for(int j = 0; j < 2; ++j) {
            this.addSlot(new Slot(this.craftSlots, j + i * 2, 98 + j * 18, 18 + i * 18));
         }
      }

      for(int k = 0; k < 4; ++k) {
         final EquipmentSlotType equipmentslottype = SLOT_IDS[k];
         this.addSlot(new Slot(pPlayerInventory, 39 - k, 8, 8 + k * 18) {
            /**
             * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in
             * the case of armor slots)
             */
            public int getMaxStackSize() {
               return 1;
            }

            /**
             * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
             */
            public boolean mayPlace(ItemStack pStack) {
               return pStack.canEquip(equipmentslottype, owner);
            }

            /**
             * Return whether this slot's stack can be taken from this slot.
             */
            public boolean mayPickup(PlayerEntity pPlayer) {
               ItemStack itemstack = this.getItem();
               return !itemstack.isEmpty() && !pPlayer.isCreative() && EnchantmentHelper.hasBindingCurse(itemstack) ? false : super.mayPickup(pPlayer);
            }

            @OnlyIn(Dist.CLIENT)
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
               return Pair.of(PlayerContainer.BLOCK_ATLAS, PlayerContainer.TEXTURE_EMPTY_SLOTS[equipmentslottype.getIndex()]);
            }
         });
      }

      for(int l = 0; l < 3; ++l) {
         for(int j1 = 0; j1 < 9; ++j1) {
            this.addSlot(new Slot(pPlayerInventory, j1 + (l + 1) * 9, 8 + j1 * 18, 84 + l * 18));
         }
      }

      for(int i1 = 0; i1 < 9; ++i1) {
         this.addSlot(new Slot(pPlayerInventory, i1, 8 + i1 * 18, 142));
      }

      this.addSlot(new Slot(pPlayerInventory, 40, 77, 62) {
         @OnlyIn(Dist.CLIENT)
         public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
            return Pair.of(PlayerContainer.BLOCK_ATLAS, PlayerContainer.EMPTY_ARMOR_SLOT_SHIELD);
         }
      });
   }

   public void fillCraftSlotsStackedContents(RecipeItemHelper pItemHelper) {
      this.craftSlots.fillStackedContents(pItemHelper);
   }

   public void clearCraftingContent() {
      this.resultSlots.clearContent();
      this.craftSlots.clearContent();
   }

   public boolean recipeMatches(IRecipe<? super CraftingInventory> pRecipe) {
      return pRecipe.matches(this.craftSlots, this.owner.level);
   }

   /**
    * Callback for when the crafting matrix is changed.
    */
   public void slotsChanged(IInventory pInventory) {
      WorkbenchContainer.slotChangedCraftingGrid(this.containerId, this.owner.level, this.owner, this.craftSlots, this.resultSlots);
   }

   /**
    * Called when the container is closed.
    */
   public void removed(PlayerEntity pPlayer) {
      super.removed(pPlayer);
      this.resultSlots.clearContent();
      if (!pPlayer.level.isClientSide) {
         this.clearContainer(pPlayer, pPlayer.level, this.craftSlots);
      }
   }

   /**
    * Determines whether supplied player can use this container
    */
   public boolean stillValid(PlayerEntity pPlayer) {
      return true;
   }

   /**
    * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
    * inventory and the other inventory(s).
    */
   public ItemStack quickMoveStack(PlayerEntity pPlayer, int pIndex) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(pIndex);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         EquipmentSlotType equipmentslottype = MobEntity.getEquipmentSlotForItem(itemstack);
         if (pIndex == 0) {
            if (!this.moveItemStackTo(itemstack1, 9, 45, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
         } else if (pIndex >= 1 && pIndex < 5) {
            if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
               return ItemStack.EMPTY;
            }
         } else if (pIndex >= 5 && pIndex < 9) {
            if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
               return ItemStack.EMPTY;
            }
         } else if (equipmentslottype.getType() == EquipmentSlotType.Group.ARMOR && !this.slots.get(8 - equipmentslottype.getIndex()).hasItem()) {
            int i = 8 - equipmentslottype.getIndex();
            if (!this.moveItemStackTo(itemstack1, i, i + 1, false)) {
               return ItemStack.EMPTY;
            }
         } else if (equipmentslottype == EquipmentSlotType.OFFHAND && !this.slots.get(45).hasItem()) {
            if (!this.moveItemStackTo(itemstack1, 45, 46, false)) {
               return ItemStack.EMPTY;
            }
         } else if (pIndex >= 9 && pIndex < 36) {
            if (!this.moveItemStackTo(itemstack1, 36, 45, false)) {
               return ItemStack.EMPTY;
            }
         } else if (pIndex >= 36 && pIndex < 45) {
            if (!this.moveItemStackTo(itemstack1, 9, 36, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 9, 45, false)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (itemstack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }

         ItemStack itemstack2 = slot.onTake(pPlayer, itemstack1);
         if (pIndex == 0) {
            pPlayer.drop(itemstack2, false);
         }
      }

      return itemstack;
   }

   /**
    * Called to determine if the current slot is valid for the stack merging (double-click) code. The stack passed in is
    * null for the initial slot that was double-clicked.
    */
   public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
      return pSlot.container != this.resultSlots && super.canTakeItemForPickAll(pStack, pSlot);
   }

   public int getResultSlotIndex() {
      return 0;
   }

   public int getGridWidth() {
      return this.craftSlots.getWidth();
   }

   public int getGridHeight() {
      return this.craftSlots.getHeight();
   }

   @OnlyIn(Dist.CLIENT)
   public int getSize() {
      return 5;
   }

   public CraftingInventory getCraftSlots() {
      return this.craftSlots;
   }

   @OnlyIn(Dist.CLIENT)
   public RecipeBookCategory getRecipeBookType() {
      return RecipeBookCategory.CRAFTING;
   }
}
