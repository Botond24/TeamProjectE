package net.minecraft.tileentity;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.BlastFurnaceContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class BlastFurnaceTileEntity extends AbstractFurnaceTileEntity {
   public BlastFurnaceTileEntity() {
      super(TileEntityType.BLAST_FURNACE, IRecipeType.BLASTING);
   }

   protected ITextComponent getDefaultName() {
      return new TranslationTextComponent("container.blast_furnace");
   }

   protected int getBurnDuration(ItemStack pFuel) {
      return super.getBurnDuration(pFuel) / 2;
   }

   protected Container createMenu(int pId, PlayerInventory pPlayer) {
      return new BlastFurnaceContainer(pId, pPlayer, this, this.dataAccess);
   }
}