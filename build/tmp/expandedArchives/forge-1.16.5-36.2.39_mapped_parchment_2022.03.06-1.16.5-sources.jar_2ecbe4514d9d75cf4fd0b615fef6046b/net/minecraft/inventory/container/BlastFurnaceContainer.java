package net.minecraft.inventory.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeBookCategory;
import net.minecraft.util.IIntArray;

public class BlastFurnaceContainer extends AbstractFurnaceContainer {
   public BlastFurnaceContainer(int pContainerId, PlayerInventory pPlayerInventory) {
      super(ContainerType.BLAST_FURNACE, IRecipeType.BLASTING, RecipeBookCategory.BLAST_FURNACE, pContainerId, pPlayerInventory);
   }

   public BlastFurnaceContainer(int pContainerId, PlayerInventory pPlayerInventory, IInventory pBlastFurnaceContainer, IIntArray pBlastFurnaceData) {
      super(ContainerType.BLAST_FURNACE, IRecipeType.BLASTING, RecipeBookCategory.BLAST_FURNACE, pContainerId, pPlayerInventory, pBlastFurnaceContainer, pBlastFurnaceData);
   }
}