package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public abstract class MoveToBlockGoal extends Goal {
   protected final CreatureEntity mob;
   public final double speedModifier;
   /** Controls task execution delay */
   protected int nextStartTick;
   protected int tryTicks;
   private int maxStayTicks;
   /** Block to move to */
   protected BlockPos blockPos = BlockPos.ZERO;
   private boolean reachedTarget;
   private final int searchRange;
   private final int verticalSearchRange;
   protected int verticalSearchStart;

   public MoveToBlockGoal(CreatureEntity p_i45888_1_, double p_i45888_2_, int p_i45888_4_) {
      this(p_i45888_1_, p_i45888_2_, p_i45888_4_, 1);
   }

   public MoveToBlockGoal(CreatureEntity p_i48796_1_, double p_i48796_2_, int p_i48796_4_, int p_i48796_5_) {
      this.mob = p_i48796_1_;
      this.speedModifier = p_i48796_2_;
      this.searchRange = p_i48796_4_;
      this.verticalSearchStart = 0;
      this.verticalSearchRange = p_i48796_5_;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      if (this.nextStartTick > 0) {
         --this.nextStartTick;
         return false;
      } else {
         this.nextStartTick = this.nextStartTick(this.mob);
         return this.findNearestBlock();
      }
   }

   protected int nextStartTick(CreatureEntity pCreature) {
      return 200 + pCreature.getRandom().nextInt(200);
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      return this.tryTicks >= -this.maxStayTicks && this.tryTicks <= 1200 && this.isValidTarget(this.mob.level, this.blockPos);
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.moveMobToBlock();
      this.tryTicks = 0;
      this.maxStayTicks = this.mob.getRandom().nextInt(this.mob.getRandom().nextInt(1200) + 1200) + 1200;
   }

   protected void moveMobToBlock() {
      this.mob.getNavigation().moveTo((double)((float)this.blockPos.getX()) + 0.5D, (double)(this.blockPos.getY() + 1), (double)((float)this.blockPos.getZ()) + 0.5D, this.speedModifier);
   }

   public double acceptedDistance() {
      return 1.0D;
   }

   protected BlockPos getMoveToTarget() {
      return this.blockPos.above();
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      BlockPos blockpos = this.getMoveToTarget();
      if (!blockpos.closerThan(this.mob.position(), this.acceptedDistance())) {
         this.reachedTarget = false;
         ++this.tryTicks;
         if (this.shouldRecalculatePath()) {
            this.mob.getNavigation().moveTo((double)((float)blockpos.getX()) + 0.5D, (double)blockpos.getY(), (double)((float)blockpos.getZ()) + 0.5D, this.speedModifier);
         }
      } else {
         this.reachedTarget = true;
         --this.tryTicks;
      }

   }

   public boolean shouldRecalculatePath() {
      return this.tryTicks % 40 == 0;
   }

   protected boolean isReachedTarget() {
      return this.reachedTarget;
   }

   /**
    * Searches and sets new destination block and returns true if a suitable block (specified in {@link
    * net.minecraft.entity.ai.EntityAIMoveToBlock#shouldMoveTo(World, BlockPos) EntityAIMoveToBlock#shouldMoveTo(World,
    * BlockPos)}) can be found.
    */
   protected boolean findNearestBlock() {
      int i = this.searchRange;
      int j = this.verticalSearchRange;
      BlockPos blockpos = this.mob.blockPosition();
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

      for(int k = this.verticalSearchStart; k <= j; k = k > 0 ? -k : 1 - k) {
         for(int l = 0; l < i; ++l) {
            for(int i1 = 0; i1 <= l; i1 = i1 > 0 ? -i1 : 1 - i1) {
               for(int j1 = i1 < l && i1 > -l ? l : 0; j1 <= l; j1 = j1 > 0 ? -j1 : 1 - j1) {
                  blockpos$mutable.setWithOffset(blockpos, i1, k - 1, j1);
                  if (this.mob.isWithinRestriction(blockpos$mutable) && this.isValidTarget(this.mob.level, blockpos$mutable)) {
                     this.blockPos = blockpos$mutable;
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   /**
    * Return true to set given position as destination
    */
   protected abstract boolean isValidTarget(IWorldReader pLevel, BlockPos pPos);
}