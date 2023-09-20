package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class CatLieOnBedGoal extends MoveToBlockGoal {
   private final CatEntity cat;

   public CatLieOnBedGoal(CatEntity p_i50331_1_, double p_i50331_2_, int p_i50331_4_) {
      super(p_i50331_1_, p_i50331_2_, p_i50331_4_, 6);
      this.cat = p_i50331_1_;
      this.verticalSearchStart = -2;
      this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      return this.cat.isTame() && !this.cat.isOrderedToSit() && !this.cat.isLying() && super.canUse();
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      super.start();
      this.cat.setInSittingPose(false);
   }

   protected int nextStartTick(CreatureEntity pCreature) {
      return 40;
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      super.stop();
      this.cat.setLying(false);
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      super.tick();
      this.cat.setInSittingPose(false);
      if (!this.isReachedTarget()) {
         this.cat.setLying(false);
      } else if (!this.cat.isLying()) {
         this.cat.setLying(true);
      }

   }

   /**
    * Return true to set given position as destination
    */
   protected boolean isValidTarget(IWorldReader pLevel, BlockPos pPos) {
      return pLevel.isEmptyBlock(pPos.above()) && pLevel.getBlockState(pPos).getBlock().is(BlockTags.BEDS);
   }
}