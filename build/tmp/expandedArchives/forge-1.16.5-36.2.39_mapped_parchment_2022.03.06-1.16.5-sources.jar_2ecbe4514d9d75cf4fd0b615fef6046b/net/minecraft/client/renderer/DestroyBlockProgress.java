package net.minecraft.client.renderer;

import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DestroyBlockProgress implements Comparable<DestroyBlockProgress> {
   private final int id;
   private final BlockPos pos;
   private int progress;
   private int updatedRenderTick;

   public DestroyBlockProgress(int p_i45925_1_, BlockPos p_i45925_2_) {
      this.id = p_i45925_1_;
      this.pos = p_i45925_2_;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   /**
    * inserts damage value into this partially destroyed Block. -1 causes client renderer to delete it, otherwise ranges
    * from 1 to 10
    */
   public void setProgress(int pDamage) {
      if (pDamage > 10) {
         pDamage = 10;
      }

      this.progress = pDamage;
   }

   public int getProgress() {
      return this.progress;
   }

   /**
    * saves the current Cloud update tick into the PartiallyDestroyedBlock
    */
   public void updateTick(int pCreatedAtCloudUpdateTick) {
      this.updatedRenderTick = pCreatedAtCloudUpdateTick;
   }

   /**
    * retrieves the 'date' at which the PartiallyDestroyedBlock was created
    */
   public int getUpdatedRenderTick() {
      return this.updatedRenderTick;
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
         DestroyBlockProgress destroyblockprogress = (DestroyBlockProgress)p_equals_1_;
         return this.id == destroyblockprogress.id;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Integer.hashCode(this.id);
   }

   public int compareTo(DestroyBlockProgress p_compareTo_1_) {
      return this.progress != p_compareTo_1_.progress ? Integer.compare(this.progress, p_compareTo_1_.progress) : Integer.compare(this.id, p_compareTo_1_.id);
   }
}