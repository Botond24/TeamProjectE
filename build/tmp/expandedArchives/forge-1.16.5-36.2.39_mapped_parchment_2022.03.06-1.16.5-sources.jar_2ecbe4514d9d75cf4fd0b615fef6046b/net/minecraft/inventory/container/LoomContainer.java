package net.minecraft.inventory.container;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BannerItem;
import net.minecraft.item.BannerPatternItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LoomContainer extends Container {
   private final IWorldPosCallable access;
   private final IntReferenceHolder selectedBannerPatternIndex = IntReferenceHolder.standalone();
   private Runnable slotUpdateListener = () -> {
   };
   private final Slot bannerSlot;
   private final Slot dyeSlot;
   private final Slot patternSlot;
   private final Slot resultSlot;
   private long lastSoundTime;
   private final IInventory inputContainer = new Inventory(3) {
      /**
       * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think
       * it hasn't changed and skip it.
       */
      public void setChanged() {
         super.setChanged();
         LoomContainer.this.slotsChanged(this);
         LoomContainer.this.slotUpdateListener.run();
      }
   };
   private final IInventory outputContainer = new Inventory(1) {
      /**
       * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think
       * it hasn't changed and skip it.
       */
      public void setChanged() {
         super.setChanged();
         LoomContainer.this.slotUpdateListener.run();
      }
   };

   public LoomContainer(int pContainerId, PlayerInventory pPlayerInventory) {
      this(pContainerId, pPlayerInventory, IWorldPosCallable.NULL);
   }

   public LoomContainer(int pContainerId, PlayerInventory pPlayerInventory, final IWorldPosCallable pAccess) {
      super(ContainerType.LOOM, pContainerId);
      this.access = pAccess;
      this.bannerSlot = this.addSlot(new Slot(this.inputContainer, 0, 13, 26) {
         /**
          * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
          */
         public boolean mayPlace(ItemStack pStack) {
            return pStack.getItem() instanceof BannerItem;
         }
      });
      this.dyeSlot = this.addSlot(new Slot(this.inputContainer, 1, 33, 26) {
         /**
          * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
          */
         public boolean mayPlace(ItemStack pStack) {
            return pStack.getItem() instanceof DyeItem;
         }
      });
      this.patternSlot = this.addSlot(new Slot(this.inputContainer, 2, 23, 45) {
         /**
          * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
          */
         public boolean mayPlace(ItemStack pStack) {
            return pStack.getItem() instanceof BannerPatternItem;
         }
      });
      this.resultSlot = this.addSlot(new Slot(this.outputContainer, 0, 143, 58) {
         /**
          * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
          */
         public boolean mayPlace(ItemStack pStack) {
            return false;
         }

         public ItemStack onTake(PlayerEntity pPlayer, ItemStack pStack) {
            LoomContainer.this.bannerSlot.remove(1);
            LoomContainer.this.dyeSlot.remove(1);
            if (!LoomContainer.this.bannerSlot.hasItem() || !LoomContainer.this.dyeSlot.hasItem()) {
               LoomContainer.this.selectedBannerPatternIndex.set(0);
            }

            pAccess.execute((p_216951_1_, p_216951_2_) -> {
               long l = p_216951_1_.getGameTime();
               if (LoomContainer.this.lastSoundTime != l) {
                  p_216951_1_.playSound((PlayerEntity)null, p_216951_2_, SoundEvents.UI_LOOM_TAKE_RESULT, SoundCategory.BLOCKS, 1.0F, 1.0F);
                  LoomContainer.this.lastSoundTime = l;
               }

            });
            return super.onTake(pPlayer, pStack);
         }
      });

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(pPlayerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for(int k = 0; k < 9; ++k) {
         this.addSlot(new Slot(pPlayerInventory, k, 8 + k * 18, 142));
      }

      this.addDataSlot(this.selectedBannerPatternIndex);
   }

   @OnlyIn(Dist.CLIENT)
   public int getSelectedBannerPatternIndex() {
      return this.selectedBannerPatternIndex.get();
   }

   /**
    * Determines whether supplied player can use this container
    */
   public boolean stillValid(PlayerEntity pPlayer) {
      return stillValid(this.access, pPlayer, Blocks.LOOM);
   }

   /**
    * Handles the given Button-click on the server, currently only used by enchanting. Name is for legacy.
    */
   public boolean clickMenuButton(PlayerEntity pPlayer, int pId) {
      if (pId > 0 && pId <= BannerPattern.AVAILABLE_PATTERNS) {
         this.selectedBannerPatternIndex.set(pId);
         this.setupResultSlot();
         return true;
      } else {
         return false;
      }
   }

   /**
    * Callback for when the crafting matrix is changed.
    */
   public void slotsChanged(IInventory pInventory) {
      ItemStack itemstack = this.bannerSlot.getItem();
      ItemStack itemstack1 = this.dyeSlot.getItem();
      ItemStack itemstack2 = this.patternSlot.getItem();
      ItemStack itemstack3 = this.resultSlot.getItem();
      if (itemstack3.isEmpty() || !itemstack.isEmpty() && !itemstack1.isEmpty() && this.selectedBannerPatternIndex.get() > 0 && (this.selectedBannerPatternIndex.get() < BannerPattern.COUNT - BannerPattern.PATTERN_ITEM_COUNT || !itemstack2.isEmpty())) {
         if (!itemstack2.isEmpty() && itemstack2.getItem() instanceof BannerPatternItem) {
            CompoundNBT compoundnbt = itemstack.getOrCreateTagElement("BlockEntityTag");
            boolean flag = compoundnbt.contains("Patterns", 9) && !itemstack.isEmpty() && compoundnbt.getList("Patterns", 10).size() >= 6;
            if (flag) {
               this.selectedBannerPatternIndex.set(0);
            } else {
               this.selectedBannerPatternIndex.set(((BannerPatternItem)itemstack2.getItem()).getBannerPattern().ordinal());
            }
         }
      } else {
         this.resultSlot.set(ItemStack.EMPTY);
         this.selectedBannerPatternIndex.set(0);
      }

      this.setupResultSlot();
      this.broadcastChanges();
   }

   @OnlyIn(Dist.CLIENT)
   public void registerUpdateListener(Runnable pListener) {
      this.slotUpdateListener = pListener;
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
         if (pIndex == this.resultSlot.index) {
            if (!this.moveItemStackTo(itemstack1, 4, 40, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
         } else if (pIndex != this.dyeSlot.index && pIndex != this.bannerSlot.index && pIndex != this.patternSlot.index) {
            if (itemstack1.getItem() instanceof BannerItem) {
               if (!this.moveItemStackTo(itemstack1, this.bannerSlot.index, this.bannerSlot.index + 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (itemstack1.getItem() instanceof DyeItem) {
               if (!this.moveItemStackTo(itemstack1, this.dyeSlot.index, this.dyeSlot.index + 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (itemstack1.getItem() instanceof BannerPatternItem) {
               if (!this.moveItemStackTo(itemstack1, this.patternSlot.index, this.patternSlot.index + 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (pIndex >= 4 && pIndex < 31) {
               if (!this.moveItemStackTo(itemstack1, 31, 40, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (pIndex >= 31 && pIndex < 40 && !this.moveItemStackTo(itemstack1, 4, 31, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 4, 40, false)) {
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

         slot.onTake(pPlayer, itemstack1);
      }

      return itemstack;
   }

   /**
    * Called when the container is closed.
    */
   public void removed(PlayerEntity pPlayer) {
      super.removed(pPlayer);
      this.access.execute((p_217028_2_, p_217028_3_) -> {
         this.clearContainer(pPlayer, pPlayer.level, this.inputContainer);
      });
   }

   /**
    * Creates an output banner ItemStack based on the patterns, dyes, etc. in the loom.
    */
   private void setupResultSlot() {
      if (this.selectedBannerPatternIndex.get() > 0) {
         ItemStack itemstack = this.bannerSlot.getItem();
         ItemStack itemstack1 = this.dyeSlot.getItem();
         ItemStack itemstack2 = ItemStack.EMPTY;
         if (!itemstack.isEmpty() && !itemstack1.isEmpty()) {
            itemstack2 = itemstack.copy();
            itemstack2.setCount(1);
            BannerPattern bannerpattern = BannerPattern.values()[this.selectedBannerPatternIndex.get()];
            DyeColor dyecolor = ((DyeItem)itemstack1.getItem()).getDyeColor();
            CompoundNBT compoundnbt = itemstack2.getOrCreateTagElement("BlockEntityTag");
            ListNBT listnbt;
            if (compoundnbt.contains("Patterns", 9)) {
               listnbt = compoundnbt.getList("Patterns", 10);
            } else {
               listnbt = new ListNBT();
               compoundnbt.put("Patterns", listnbt);
            }

            CompoundNBT compoundnbt1 = new CompoundNBT();
            compoundnbt1.putString("Pattern", bannerpattern.getHashname());
            compoundnbt1.putInt("Color", dyecolor.getId());
            listnbt.add(compoundnbt1);
         }

         if (!ItemStack.matches(itemstack2, this.resultSlot.getItem())) {
            this.resultSlot.set(itemstack2);
         }
      }

   }

   @OnlyIn(Dist.CLIENT)
   public Slot getBannerSlot() {
      return this.bannerSlot;
   }

   @OnlyIn(Dist.CLIENT)
   public Slot getDyeSlot() {
      return this.dyeSlot;
   }

   @OnlyIn(Dist.CLIENT)
   public Slot getPatternSlot() {
      return this.patternSlot;
   }

   @OnlyIn(Dist.CLIENT)
   public Slot getResultSlot() {
      return this.resultSlot;
   }
}