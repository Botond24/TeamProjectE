package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.pathfinding.PathType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;

public class BreatheAirGoal extends Goal {
   private final CreatureEntity mob;

   public BreatheAirGoal(CreatureEntity p_i48940_1_) {
      this.mob = p_i48940_1_;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      return this.mob.getAirSupply() < 140;
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      return this.canUse();
   }

   public boolean isInterruptable() {
      return false;
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.findAirPosition();
   }

   private void findAirPosition() {
      Iterable<BlockPos> iterable = BlockPos.betweenClosed(MathHelper.floor(this.mob.getX() - 1.0D), MathHelper.floor(this.mob.getY()), MathHelper.floor(this.mob.getZ() - 1.0D), MathHelper.floor(this.mob.getX() + 1.0D), MathHelper.floor(this.mob.getY() + 8.0D), MathHelper.floor(this.mob.getZ() + 1.0D));
      BlockPos blockpos = null;

      for(BlockPos blockpos1 : iterable) {
         if (this.givesAir(this.mob.level, blockpos1)) {
            blockpos = blockpos1;
            break;
         }
      }

      if (blockpos == null) {
         blockpos = new BlockPos(this.mob.getX(), this.mob.getY() + 8.0D, this.mob.getZ());
      }

      this.mob.getNavigation().moveTo((double)blockpos.getX(), (double)(blockpos.getY() + 1), (double)blockpos.getZ(), 1.0D);
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      this.findAirPosition();
      this.mob.moveRelative(0.02F, new Vector3d((double)this.mob.xxa, (double)this.mob.yya, (double)this.mob.zza));
      this.mob.move(MoverType.SELF, this.mob.getDeltaMovement());
   }

   private boolean givesAir(IWorldReader pLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      return (pLevel.getFluidState(pPos).isEmpty() || blockstate.is(Blocks.BUBBLE_COLUMN)) && blockstate.isPathfindable(pLevel, pPos, PathType.LAND);
   }
}