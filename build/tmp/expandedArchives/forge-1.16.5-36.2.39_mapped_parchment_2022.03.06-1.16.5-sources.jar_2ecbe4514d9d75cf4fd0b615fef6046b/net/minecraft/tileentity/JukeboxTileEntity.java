package net.minecraft.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.inventory.IClearable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class JukeboxTileEntity extends TileEntity implements IClearable {
   private ItemStack record = ItemStack.EMPTY;

   public JukeboxTileEntity() {
      super(TileEntityType.JUKEBOX);
   }

   public void load(BlockState p_230337_1_, CompoundNBT p_230337_2_) {
      super.load(p_230337_1_, p_230337_2_);
      if (p_230337_2_.contains("RecordItem", 10)) {
         this.setRecord(ItemStack.of(p_230337_2_.getCompound("RecordItem")));
      }

   }

   public CompoundNBT save(CompoundNBT pCompound) {
      super.save(pCompound);
      if (!this.getRecord().isEmpty()) {
         pCompound.put("RecordItem", this.getRecord().save(new CompoundNBT()));
      }

      return pCompound;
   }

   public ItemStack getRecord() {
      return this.record;
   }

   public void setRecord(ItemStack pRecord) {
      this.record = pRecord;
      this.setChanged();
   }

   public void clearContent() {
      this.setRecord(ItemStack.EMPTY);
   }
}