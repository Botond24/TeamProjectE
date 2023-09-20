package net.minecraft.inventory.container;

import java.util.Optional;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeBookCategory;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WorkbenchContainer extends RecipeBookContainer<CraftingInventory> {
   private final CraftingInventory craftSlots = new CraftingInventory(this, 3, 3);
   private final CraftResultInventory resultSlots = new CraftResultInventory();
   private final IWorldPosCallable access;
   private final PlayerEntity player;

   public WorkbenchContainer(int pContainerId, PlayerInventory pPlayerInventory) {
      this(pContainerId, pPlayerInventory, IWorldPosCallable.NULL);
   }

   public WorkbenchContainer(int pContainerId, PlayerInventory pPlayerInventory, IWorldPosCallable pAccess) {
      super(ContainerType.CRAFTING, pContainerId);
      this.access = pAccess;
      this.player = pPlayerInventory.player;
      this.addSlot(new CraftingResultSlot(pPlayerInventory.player, this.craftSlots, this.resultSlots, 0, 124, 35));

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 3; ++j) {
            this.addSlot(new Slot(this.craftSlots, j + i * 3, 30 + j * 18, 17 + i * 18));
         }
      }

      for(int k = 0; k < 3; ++k) {
         for(int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(pPlayerInventory, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
         }
      }

      for(int l = 0; l < 9; ++l) {
         this.addSlot(new Slot(pPlayerInventory, l, 8 + l * 18, 142));
      }

   }

   protected static void slotChangedCraftingGrid(int pContainerId, World pLevel, PlayerEntity pPlayer, CraftingInventory pContainer, CraftResultInventory pResultContainer) {
      if (!pLevel.isClientSide) {
         ServerPlayerEntity serverplayerentity = (ServerPlayerEntity)pPlayer;
         ItemStack itemstack = ItemStack.EMPTY;
         Optional<ICraftingRecipe> optional = pLevel.getServer().getRecipeManager().getRecipeFor(IRecipeType.CRAFTING, pContainer, pLevel);
         if (optional.isPresent()) {
            ICraftingRecipe icraftingrecipe = optional.get();
            if (pResultContainer.setRecipeUsed(pLevel, serverplayerentity, icraftingrecipe)) {
               itemstack = icraftingrecipe.assemble(pContainer);
            }
         }

         pResultContainer.setItem(0, itemstack);
         serverplayerentity.connection.send(new SSetSlotPacket(pContainerId, 0, itemstack));
      }
   }

   /**
    * Callback for when the crafting matrix is changed.
    */
   public void slotsChanged(IInventory pInventory) {
      this.access.execute((p_217069_1_, p_217069_2_) -> {
         slotChangedCraftingGrid(this.containerId, p_217069_1_, this.player, this.craftSlots, this.resultSlots);
      });
   }

   public void fillCraftSlotsStackedContents(RecipeItemHelper pItemHelper) {
      this.craftSlots.fillStackedContents(pItemHelper);
   }

   public void clearCraftingContent() {
      this.craftSlots.clearContent();
      this.resultSlots.clearContent();
   }

   public boolean recipeMatches(IRecipe<? super CraftingInventory> pRecipe) {
      return pRecipe.matches(this.craftSlots, this.player.level);
   }

   /**
    * Called when the container is closed.
    */
   public void removed(PlayerEntity pPlayer) {
      super.removed(pPlayer);
      this.access.execute((p_217068_2_, p_217068_3_) -> {
         this.clearContainer(pPlayer, p_217068_2_, this.craftSlots);
      });
   }

   /**
    * Determines whether supplied player can use this container
    */
   public boolean stillValid(PlayerEntity pPlayer) {
      return stillValid(this.access, pPlayer, Blocks.CRAFTING_TABLE);
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
         if (pIndex == 0) {
            this.access.execute((p_217067_2_, p_217067_3_) -> {
               itemstack1.getItem().onCraftedBy(itemstack1, p_217067_2_, pPlayer);
            });
            if (!this.moveItemStackTo(itemstack1, 10, 46, true)) {
               return ItemStack.EMPTY;
            }

            slot.onQuickCraft(itemstack1, itemstack);
         } else if (pIndex >= 10 && pIndex < 46) {
            if (!this.moveItemStackTo(itemstack1, 1, 10, false)) {
               if (pIndex < 37) {
                  if (!this.moveItemStackTo(itemstack1, 37, 46, false)) {
                     return ItemStack.EMPTY;
                  }
               } else if (!this.moveItemStackTo(itemstack1, 10, 37, false)) {
                  return ItemStack.EMPTY;
               }
            }
         } else if (!this.moveItemStackTo(itemstack1, 10, 46, false)) {
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
      return 10;
   }

   @OnlyIn(Dist.CLIENT)
   public RecipeBookCategory getRecipeBookType() {
      return RecipeBookCategory.CRAFTING;
   }
}