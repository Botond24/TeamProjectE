package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.MobEntity;
import net.minecraft.tags.FluidTags;

public class SwimGoal extends Goal {
   private final MobEntity mob;

   public SwimGoal(MobEntity p_i1624_1_) {
      this.mob = p_i1624_1_;
      this.setFlags(EnumSet.of(Goal.Flag.JUMP));
      p_i1624_1_.getNavigation().setCanFloat(true);
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      return this.mob.isInWater() && this.mob.getFluidHeight(FluidTags.WATER) > this.mob.getFluidJumpThreshold() || this.mob.isInLava();
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      if (this.mob.getRandom().nextFloat() < 0.8F) {
         this.mob.getJumpControl().jump();
      }

   }
}