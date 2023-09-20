package net.minecraft.pathfinding;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ClimberPathNavigator extends GroundPathNavigator {
   /** Current path navigation target */
   private BlockPos pathToPosition;

   public ClimberPathNavigator(MobEntity p_i45874_1_, World p_i45874_2_) {
      super(p_i45874_1_, p_i45874_2_);
   }

   /**
    * Returns path to given BlockPos
    */
   public Path createPath(BlockPos pPos, int pAccuracy) {
      this.pathToPosition = pPos;
      return super.createPath(pPos, pAccuracy);
   }

   /**
    * Returns a path to the given entity or null
    */
   public Path createPath(Entity pEntity, int p_75494_2_) {
      this.pathToPosition = pEntity.blockPosition();
      return super.createPath(pEntity, p_75494_2_);
   }

   /**
    * Try to find and set a path to EntityLiving. Returns true if successful. Args : entity, speed
    */
   public boolean moveTo(Entity pEntity, double pSpeed) {
      Path path = this.createPath(pEntity, 0);
      if (path != null) {
         return this.moveTo(path, pSpeed);
      } else {
         this.pathToPosition = pEntity.blockPosition();
         this.speedModifier = pSpeed;
         return true;
      }
   }

   public void tick() {
      if (!this.isDone()) {
         super.tick();
      } else {
         if (this.pathToPosition != null) {
            // FORGE: Fix MC-94054
            if (!this.pathToPosition.closerThan(this.mob.position(), Math.max((double)this.mob.getBbWidth(), 1.0D)) && (!(this.mob.getY() > (double)this.pathToPosition.getY()) || !(new BlockPos((double)this.pathToPosition.getX(), this.mob.getY(), (double)this.pathToPosition.getZ())).closerThan(this.mob.position(), Math.max((double)this.mob.getBbWidth(), 1.0D)))) {
               this.mob.getMoveControl().setWantedPosition((double)this.pathToPosition.getX(), (double)this.pathToPosition.getY(), (double)this.pathToPosition.getZ(), this.speedModifier);
            } else {
               this.pathToPosition = null;
            }
         }

      }
   }
}
