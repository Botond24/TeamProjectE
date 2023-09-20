package net.minecraft.pathfinding;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class FlyingPathNavigator extends PathNavigator {
   public FlyingPathNavigator(MobEntity p_i47412_1_, World p_i47412_2_) {
      super(p_i47412_1_, p_i47412_2_);
   }

   protected PathFinder createPathFinder(int pMaxVisitedNodes) {
      this.nodeEvaluator = new FlyingNodeProcessor();
      this.nodeEvaluator.setCanPassDoors(true);
      return new PathFinder(this.nodeEvaluator, pMaxVisitedNodes);
   }

   /**
    * If on ground or swimming and can swim
    */
   protected boolean canUpdatePath() {
      return this.canFloat() && this.isInLiquid() || !this.mob.isPassenger();
   }

   protected Vector3d getTempMobPos() {
      return this.mob.position();
   }

   /**
    * Returns a path to the given entity or null
    */
   public Path createPath(Entity pEntity, int p_75494_2_) {
      return this.createPath(pEntity.blockPosition(), p_75494_2_);
   }

   public void tick() {
      ++this.tick;
      if (this.hasDelayedRecomputation) {
         this.recomputePath();
      }

      if (!this.isDone()) {
         if (this.canUpdatePath()) {
            this.followThePath();
         } else if (this.path != null && !this.path.isDone()) {
            Vector3d vector3d = this.path.getNextEntityPos(this.mob);
            if (MathHelper.floor(this.mob.getX()) == MathHelper.floor(vector3d.x) && MathHelper.floor(this.mob.getY()) == MathHelper.floor(vector3d.y) && MathHelper.floor(this.mob.getZ()) == MathHelper.floor(vector3d.z)) {
               this.path.advance();
            }
         }

         DebugPacketSender.sendPathFindingPacket(this.level, this.mob, this.path, this.maxDistanceToWaypoint);
         if (!this.isDone()) {
            Vector3d vector3d1 = this.path.getNextEntityPos(this.mob);
            this.mob.getMoveControl().setWantedPosition(vector3d1.x, vector3d1.y, vector3d1.z, this.speedModifier);
         }
      }
   }

   /**
    * Checks if the specified entity can safely walk to the specified location.
    */
   protected boolean canMoveDirectly(Vector3d pPosVec31, Vector3d pPosVec32, int pSizeX, int pSizeY, int pSizeZ) {
      int i = MathHelper.floor(pPosVec31.x);
      int j = MathHelper.floor(pPosVec31.y);
      int k = MathHelper.floor(pPosVec31.z);
      double d0 = pPosVec32.x - pPosVec31.x;
      double d1 = pPosVec32.y - pPosVec31.y;
      double d2 = pPosVec32.z - pPosVec31.z;
      double d3 = d0 * d0 + d1 * d1 + d2 * d2;
      if (d3 < 1.0E-8D) {
         return false;
      } else {
         double d4 = 1.0D / Math.sqrt(d3);
         d0 = d0 * d4;
         d1 = d1 * d4;
         d2 = d2 * d4;
         double d5 = 1.0D / Math.abs(d0);
         double d6 = 1.0D / Math.abs(d1);
         double d7 = 1.0D / Math.abs(d2);
         double d8 = (double)i - pPosVec31.x;
         double d9 = (double)j - pPosVec31.y;
         double d10 = (double)k - pPosVec31.z;
         if (d0 >= 0.0D) {
            ++d8;
         }

         if (d1 >= 0.0D) {
            ++d9;
         }

         if (d2 >= 0.0D) {
            ++d10;
         }

         d8 = d8 / d0;
         d9 = d9 / d1;
         d10 = d10 / d2;
         int l = d0 < 0.0D ? -1 : 1;
         int i1 = d1 < 0.0D ? -1 : 1;
         int j1 = d2 < 0.0D ? -1 : 1;
         int k1 = MathHelper.floor(pPosVec32.x);
         int l1 = MathHelper.floor(pPosVec32.y);
         int i2 = MathHelper.floor(pPosVec32.z);
         int j2 = k1 - i;
         int k2 = l1 - j;
         int l2 = i2 - k;

         while(j2 * l > 0 || k2 * i1 > 0 || l2 * j1 > 0) {
            if (d8 < d10 && d8 <= d9) {
               d8 += d5;
               i += l;
               j2 = k1 - i;
            } else if (d9 < d8 && d9 <= d10) {
               d9 += d6;
               j += i1;
               k2 = l1 - j;
            } else {
               d10 += d7;
               k += j1;
               l2 = i2 - k;
            }
         }

         return true;
      }
   }

   public void setCanOpenDoors(boolean pCanOpenDoors) {
      this.nodeEvaluator.setCanOpenDoors(pCanOpenDoors);
   }

   public void setCanPassDoors(boolean pCanEnterDoors) {
      this.nodeEvaluator.setCanPassDoors(pCanEnterDoors);
   }

   public boolean isStableDestination(BlockPos pPos) {
      return this.level.getBlockState(pPos).entityCanStandOn(this.level, pPos, this.mob);
   }
}