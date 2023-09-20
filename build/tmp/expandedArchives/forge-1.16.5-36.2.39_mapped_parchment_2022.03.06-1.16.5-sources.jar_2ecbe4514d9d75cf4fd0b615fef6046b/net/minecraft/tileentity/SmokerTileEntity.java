package net.minecraft.tileentity;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.SmokerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class SmokerTileEntity extends AbstractFurnaceTileEntity {
   public SmokerTileEntity() {
      super(TileEntityType.SMOKER, IRecipeType.SMOKING);
   }

   protected ITextComponent getDefaultName() {
      return new TranslationTextComponent("container.smoker");
   }

   protected int getBurnDuration(ItemStack pFuel) {
      return super.getBurnDuration(pFuel) / 2;
   }

   protected Container createMenu(int pId, PlayerInventory pPlayer) {
      return new SmokerContainer(pId, pPlayer, this, this.dataAccess);
   }
}