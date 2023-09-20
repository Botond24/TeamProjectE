package net.minecraft.inventory.container;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.StonecuttingRecipe;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class StonecutterContainer extends Container {
   private final IWorldPosCallable access;
   /** The index of the selected recipe in the GUI. */
   private final IntReferenceHolder selectedRecipeIndex = IntReferenceHolder.standalone();
   private final World level;
   private List<StonecuttingRecipe> recipes = Lists.newArrayList();
   /** The {@plainlink ItemStack} set in the input slot by the player. */
   private ItemStack input = ItemStack.EMPTY;
   /**
    * Stores the game time of the last time the player took items from the the crafting result slot. This is used to
    * prevent the sound from being played multiple times on the same tick.
    */
   private long lastSoundTime;
   final Slot inputSlot;
   /** The inventory slot that stores the output of the crafting recipe. */
   final Slot resultSlot;
   private Runnable slotUpdateListener = () -> {
   };
   public final IInventory container = new Inventory(1) {
      /**
       * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think
       * it hasn't changed and skip it.
       */
      public void setChanged() {
         super.setChanged();
         StonecutterContainer.this.slotsChanged(this);
         StonecutterContainer.this.slotUpdateListener.run();
      }
   };
   /** The inventory that stores the output of the crafting recipe. */
   private final CraftResultInventory resultContainer = new CraftResultInventory();

   public StonecutterContainer(int pContainerId, PlayerInventory pPlayerInventory) {
      this(pContainerId, pPlayerInventory, IWorldPosCallable.NULL);
   }

   public StonecutterContainer(int pContainerId, PlayerInventory pPlayerInventory, final IWorldPosCallable pAccess) {
      super(ContainerType.STONECUTTER, pContainerId);
      this.access = pAccess;
      this.level = pPlayerInventory.player.level;
      this.inputSlot = this.addSlot(new Slot(this.container, 0, 20, 33));
      this.resultSlot = this.addSlot(new Slot(this.resultContainer, 1, 143, 33) {
         /**
          * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
          */
         public boolean mayPlace(ItemStack pStack) {
            return false;
         }

         public ItemStack onTake(PlayerEntity pPlayer, ItemStack pStack) {
            pStack.onCraftedBy(pPlayer.level, pPlayer, pStack.getCount());
            StonecutterContainer.this.resultContainer.awardUsedRecipes(pPlayer);
            ItemStack itemstack = StonecutterContainer.this.inputSlot.remove(1);
            if (!itemstack.isEmpty()) {
               StonecutterContainer.this.setupResultSlot();
            }

            pAccess.execute((p_216954_1_, p_216954_2_) -> {
               long l = p_216954_1_.getGameTime();
               if (StonecutterContainer.this.lastSoundTime != l) {
                  p_216954_1_.playSound((PlayerEntity)null, p_216954_2_, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundCategory.BLOCKS, 1.0F, 1.0F);
                  StonecutterContainer.this.lastSoundTime = l;
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

      this.addDataSlot(this.selectedRecipeIndex);
   }

   /**
    * Returns the index of the selected recipe.
    */
   @OnlyIn(Dist.CLIENT)
   public int getSelectedRecipeIndex() {
      return this.selectedRecipeIndex.get();
   }

   @OnlyIn(Dist.CLIENT)
   public List<StonecuttingRecipe> getRecipes() {
      return this.recipes;
   }

   @OnlyIn(Dist.CLIENT)
   public int getNumRecipes() {
      return this.recipes.size();
   }

   @OnlyIn(Dist.CLIENT)
   public boolean hasInputItem() {
      return this.inputSlot.hasItem() && !this.recipes.isEmpty();
   }

   /**
    * Determines whether supplied player can use this container
    */
   public boolean stillValid(PlayerEntity pPlayer) {
      return stillValid(this.access, pPlayer, Blocks.STONECUTTER);
   }

   /**
    * Handles the given Button-click on the server, currently only used by enchanting. Name is for legacy.
    */
   public boolean clickMenuButton(PlayerEntity pPlayer, int pId) {
      if (this.isValidRecipeIndex(pId)) {
         this.selectedRecipeIndex.set(pId);
         this.setupResultSlot();
      }

      return true;
   }

   private boolean isValidRecipeIndex(int pRecipeIndex) {
      return pRecipeIndex >= 0 && pRecipeIndex < this.recipes.size();
   }

   /**
    * Callback for when the crafting matrix is changed.
    */
   public void slotsChanged(IInventory pInventory) {
      ItemStack itemstack = this.inputSlot.getItem();
      if (itemstack.getItem() != this.input.getItem()) {
         this.input = itemstack.copy();
         this.setupRecipeList(pInventory, itemstack);
      }

   }

   private void setupRecipeList(IInventory pInventory, ItemStack pStack) {
      this.recipes.clear();
      this.selectedRecipeIndex.set(-1);
      this.resultSlot.set(ItemStack.EMPTY);
      if (!pStack.isEmpty()) {
         this.recipes = this.level.getRecipeManager().getRecipesFor(IRecipeType.STONECUTTING, pInventory, this.level);
      }

   }

   private void setupResultSlot() {
      if (!this.recipes.isEmpty() && this.isValidRecipeIndex(this.selectedRecipeIndex.get())) {
         StonecuttingRecipe stonecuttingrecipe = this.recipes.get(this.selectedRecipeIndex.get());
         this.resultContainer.setRecipeUsed(stonecuttingrecipe);
         this.resultSlot.set(stonecuttingrecipe.assemble(this.container));
      } else {
         this.resultSlot.set(ItemStack.EMPTY);
      }

      this.broadcastChanges();
   }

   public ContainerType<?> getType() {
      return ContainerType.STONECUTTER;
   }

   @OnlyIn(Dist.CLIENT)
   public void registerUpdateListener(Runnable pListener) {
      this.slotUpdateListener = pListener;
   }

   /**
    * Called to determine if the current slot is valid for the stack merging (double-click) code. The stack passed in is
    * null for the initial slot that was double-clicked.
    */
   public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
      return pSlot.container != this.resultContainer && super.canTakeItemForPickAll(pStack, pSlot);
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
         Item item = itemstack1.getItem();
         itemstack = itemstack1.copy();
         if (pIndex == 1) {
            item.onCraftedBy(itemstack1, pPlayer.level, pPlayer);
            if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
         } else if (pIndex == 0) {
            if (!this.moveItemStackTo(itemstack1, 2, 38, false)) {
               return ItemStack.EMPTY;
            }
         } else if (this.level.getRecipeManager().getRecipeFor(IRecipeType.STONECUTTING, new Inventory(itemstack1), this.level).isPresent()) {
            if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
               return ItemStack.EMPTY;
            }
         } else if (pIndex >= 2 && pIndex < 29) {
            if (!this.moveItemStackTo(itemstack1, 29, 38, false)) {
               return ItemStack.EMPTY;
            }
         } else if (pIndex >= 29 && pIndex < 38 && !this.moveItemStackTo(itemstack1, 2, 29, false)) {
            return ItemStack.EMPTY;
         }

         if (itemstack1.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         }

         slot.setChanged();
         if (itemstack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(pPlayer, itemstack1);
         this.broadcastChanges();
      }

      return itemstack;
   }

   /**
    * Called when the container is closed.
    */
   public void removed(PlayerEntity pPlayer) {
      super.removed(pPlayer);
      this.resultContainer.removeItemNoUpdate(1);
      this.access.execute((p_217079_2_, p_217079_3_) -> {
         this.clearContainer(pPlayer, pPlayer.level, this.container);
      });
   }
}