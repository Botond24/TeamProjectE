package net.minecraft.inventory.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeBookCategory;
import net.minecraft.util.IIntArray;

public class SmokerContainer extends AbstractFurnaceContainer {
   public SmokerContainer(int pContainerId, PlayerInventory pPlayerInventory) {
      super(ContainerType.SMOKER, IRecipeType.SMOKING, RecipeBookCategory.SMOKER, pContainerId, pPlayerInventory);
   }

   public SmokerContainer(int pContainerId, PlayerInventory pPlayerInventory, IInventory pSmokerContainer, IIntArray pSmokerData) {
      super(ContainerType.SMOKER, IRecipeType.SMOKING, RecipeBookCategory.SMOKER, pContainerId, pPlayerInventory, pSmokerContainer, pSmokerData);
   }
}