package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;

public class PanicGoal extends Goal {
   protected final CreatureEntity mob;
   protected final double speedModifier;
   protected double posX;
   protected double posY;
   protected double posZ;
   protected boolean isRunning;

   public PanicGoal(CreatureEntity p_i1645_1_, double p_i1645_2_) {
      this.mob = p_i1645_1_;
      this.speedModifier = p_i1645_2_;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      if (this.mob.getLastHurtByMob() == null && !this.mob.isOnFire()) {
         return false;
      } else {
         if (this.mob.isOnFire()) {
            BlockPos blockpos = this.lookForWater(this.mob.level, this.mob, 5, 4);
            if (blockpos != null) {
               this.posX = (double)blockpos.getX();
               this.posY = (double)blockpos.getY();
               this.posZ = (double)blockpos.getZ();
               return true;
            }
         }

         return this.findRandomPosition();
      }
   }

   protected boolean findRandomPosition() {
      Vector3d vector3d = RandomPositionGenerator.getPos(this.mob, 5, 4);
      if (vector3d == null) {
         return false;
      } else {
         this.posX = vector3d.x;
         this.posY = vector3d.y;
         this.posZ = vector3d.z;
         return true;
      }
   }

   public boolean isRunning() {
      return this.isRunning;
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.mob.getNavigation().moveTo(this.posX, this.posY, this.posZ, this.speedModifier);
      this.isRunning = true;
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      this.isRunning = false;
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      return !this.mob.getNavigation().isDone();
   }

   @Nullable
   protected BlockPos lookForWater(IBlockReader pLevel, Entity pEntity, int pHorizontalRange, int pVerticalRange) {
      BlockPos blockpos = pEntity.blockPosition();
      int i = blockpos.getX();
      int j = blockpos.getY();
      int k = blockpos.getZ();
      float f = (float)(pHorizontalRange * pHorizontalRange * pVerticalRange * 2);
      BlockPos blockpos1 = null;
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

      for(int l = i - pHorizontalRange; l <= i + pHorizontalRange; ++l) {
         for(int i1 = j - pVerticalRange; i1 <= j + pVerticalRange; ++i1) {
            for(int j1 = k - pHorizontalRange; j1 <= k + pHorizontalRange; ++j1) {
               blockpos$mutable.set(l, i1, j1);
               if (pLevel.getFluidState(blockpos$mutable).is(FluidTags.WATER)) {
                  float f1 = (float)((l - i) * (l - i) + (i1 - j) * (i1 - j) + (j1 - k) * (j1 - k));
                  if (f1 < f) {
                     f = f1;
                     blockpos1 = new BlockPos(blockpos$mutable);
                  }
               }
            }
         }
      }

      return blockpos1;
   }
}