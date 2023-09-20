package net.minecraft.pathfinding;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class GroundPathNavigator extends PathNavigator {
   private boolean avoidSun;

   public GroundPathNavigator(MobEntity p_i45875_1_, World p_i45875_2_) {
      super(p_i45875_1_, p_i45875_2_);
   }

   protected PathFinder createPathFinder(int pMaxVisitedNodes) {
      this.nodeEvaluator = new WalkNodeProcessor();
      this.nodeEvaluator.setCanPassDoors(true);
      return new PathFinder(this.nodeEvaluator, pMaxVisitedNodes);
   }

   /**
    * If on ground or swimming and can swim
    */
   protected boolean canUpdatePath() {
      return this.mob.isOnGround() || this.isInLiquid() || this.mob.isPassenger();
   }

   protected Vector3d getTempMobPos() {
      return new Vector3d(this.mob.getX(), (double)this.getSurfaceY(), this.mob.getZ());
   }

   /**
    * Returns path to given BlockPos
    */
   public Path createPath(BlockPos pPos, int pAccuracy) {
      if (this.level.getBlockState(pPos).isAir()) {
         BlockPos blockpos;
         for(blockpos = pPos.below(); blockpos.getY() > 0 && this.level.getBlockState(blockpos).isAir(); blockpos = blockpos.below()) {
         }

         if (blockpos.getY() > 0) {
            return super.createPath(blockpos.above(), pAccuracy);
         }

         while(blockpos.getY() < this.level.getMaxBuildHeight() && this.level.getBlockState(blockpos).isAir()) {
            blockpos = blockpos.above();
         }

         pPos = blockpos;
      }

      if (!this.level.getBlockState(pPos).getMaterial().isSolid()) {
         return super.createPath(pPos, pAccuracy);
      } else {
         BlockPos blockpos1;
         for(blockpos1 = pPos.above(); blockpos1.getY() < this.level.getMaxBuildHeight() && this.level.getBlockState(blockpos1).getMaterial().isSolid(); blockpos1 = blockpos1.above()) {
         }

         return super.createPath(blockpos1, pAccuracy);
      }
   }

   /**
    * Returns a path to the given entity or null
    */
   public Path createPath(Entity pEntity, int p_75494_2_) {
      return this.createPath(pEntity.blockPosition(), p_75494_2_);
   }

   /**
    * Gets the safe pathing Y position for the entity depending on if it can path swim or not
    */
   private int getSurfaceY() {
      if (this.mob.isInWater() && this.canFloat()) {
         int i = MathHelper.floor(this.mob.getY());
         Block block = this.level.getBlockState(new BlockPos(this.mob.getX(), (double)i, this.mob.getZ())).getBlock();
         int j = 0;

         while(block == Blocks.WATER) {
            ++i;
            block = this.level.getBlockState(new BlockPos(this.mob.getX(), (double)i, this.mob.getZ())).getBlock();
            ++j;
            if (j > 16) {
               return MathHelper.floor(this.mob.getY());
            }
         }

         return i;
      } else {
         return MathHelper.floor(this.mob.getY() + 0.5D);
      }
   }

   /**
    * Trims path data from the end to the first sun covered block
    */
   protected void trimPath() {
      super.trimPath();
      if (this.avoidSun) {
         if (this.level.canSeeSky(new BlockPos(this.mob.getX(), this.mob.getY() + 0.5D, this.mob.getZ()))) {
            return;
         }

         for(int i = 0; i < this.path.getNodeCount(); ++i) {
            PathPoint pathpoint = this.path.getNode(i);
            if (this.level.canSeeSky(new BlockPos(pathpoint.x, pathpoint.y, pathpoint.z))) {
               this.path.truncateNodes(i);
               return;
            }
         }
      }

   }

   /**
    * Checks if the specified entity can safely walk to the specified location.
    */
   protected boolean canMoveDirectly(Vector3d pPosVec31, Vector3d pPosVec32, int pSizeX, int pSizeY, int pSizeZ) {
      int i = MathHelper.floor(pPosVec31.x);
      int j = MathHelper.floor(pPosVec31.z);
      double d0 = pPosVec32.x - pPosVec31.x;
      double d1 = pPosVec32.z - pPosVec31.z;
      double d2 = d0 * d0 + d1 * d1;
      if (d2 < 1.0E-8D) {
         return false;
      } else {
         double d3 = 1.0D / Math.sqrt(d2);
         d0 = d0 * d3;
         d1 = d1 * d3;
         pSizeX = pSizeX + 2;
         pSizeZ = pSizeZ + 2;
         if (!this.canWalkOn(i, MathHelper.floor(pPosVec31.y), j, pSizeX, pSizeY, pSizeZ, pPosVec31, d0, d1)) {
            return false;
         } else {
            pSizeX = pSizeX - 2;
            pSizeZ = pSizeZ - 2;
            double d4 = 1.0D / Math.abs(d0);
            double d5 = 1.0D / Math.abs(d1);
            double d6 = (double)i - pPosVec31.x;
            double d7 = (double)j - pPosVec31.z;
            if (d0 >= 0.0D) {
               ++d6;
            }

            if (d1 >= 0.0D) {
               ++d7;
            }

            d6 = d6 / d0;
            d7 = d7 / d1;
            int k = d0 < 0.0D ? -1 : 1;
            int l = d1 < 0.0D ? -1 : 1;
            int i1 = MathHelper.floor(pPosVec32.x);
            int j1 = MathHelper.floor(pPosVec32.z);
            int k1 = i1 - i;
            int l1 = j1 - j;

            while(k1 * k > 0 || l1 * l > 0) {
               if (d6 < d7) {
                  d6 += d4;
                  i += k;
                  k1 = i1 - i;
               } else {
                  d7 += d5;
                  j += l;
                  l1 = j1 - j;
               }

               if (!this.canWalkOn(i, MathHelper.floor(pPosVec31.y), j, pSizeX, pSizeY, pSizeZ, pPosVec31, d0, d1)) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   /**
    * Returns true when an entity could stand at a position, including solid blocks under the entire entity.
    */
   private boolean canWalkOn(int pX, int pY, int pZ, int pSizeX, int pSizeY, int pSizeZ, Vector3d pVec31, double p_179683_8_, double p_179683_10_) {
      int i = pX - pSizeX / 2;
      int j = pZ - pSizeZ / 2;
      if (!this.canWalkAbove(i, pY, j, pSizeX, pSizeY, pSizeZ, pVec31, p_179683_8_, p_179683_10_)) {
         return false;
      } else {
         for(int k = i; k < i + pSizeX; ++k) {
            for(int l = j; l < j + pSizeZ; ++l) {
               double d0 = (double)k + 0.5D - pVec31.x;
               double d1 = (double)l + 0.5D - pVec31.z;
               if (!(d0 * p_179683_8_ + d1 * p_179683_10_ < 0.0D)) {
                  PathNodeType pathnodetype = this.nodeEvaluator.getBlockPathType(this.level, k, pY - 1, l, this.mob, pSizeX, pSizeY, pSizeZ, true, true);
                  if (!this.hasValidPathType(pathnodetype)) {
                     return false;
                  }

                  pathnodetype = this.nodeEvaluator.getBlockPathType(this.level, k, pY, l, this.mob, pSizeX, pSizeY, pSizeZ, true, true);
                  float f = this.mob.getPathfindingMalus(pathnodetype);
                  if (f < 0.0F || f >= 8.0F) {
                     return false;
                  }

                  if (pathnodetype == PathNodeType.DAMAGE_FIRE || pathnodetype == PathNodeType.DANGER_FIRE || pathnodetype == PathNodeType.DAMAGE_OTHER) {
                     return false;
                  }
               }
            }
         }

         return true;
      }
   }

   protected boolean hasValidPathType(PathNodeType pPathType) {
      if (pPathType == PathNodeType.WATER) {
         return false;
      } else if (pPathType == PathNodeType.LAVA) {
         return false;
      } else {
         return pPathType != PathNodeType.OPEN;
      }
   }

   /**
    * Returns true if an entity does not collide with any solid blocks at the position.
    */
   private boolean canWalkAbove(int pX, int pY, int pZ, int pSizeX, int pSizeY, int pSizeZ, Vector3d p_179692_7_, double p_179692_8_, double p_179692_10_) {
      for(BlockPos blockpos : BlockPos.betweenClosed(new BlockPos(pX, pY, pZ), new BlockPos(pX + pSizeX - 1, pY + pSizeY - 1, pZ + pSizeZ - 1))) {
         double d0 = (double)blockpos.getX() + 0.5D - p_179692_7_.x;
         double d1 = (double)blockpos.getZ() + 0.5D - p_179692_7_.z;
         if (!(d0 * p_179692_8_ + d1 * p_179692_10_ < 0.0D) && !this.level.getBlockState(blockpos).isPathfindable(this.level, blockpos, PathType.LAND)) {
            return false;
         }
      }

      return true;
   }

   public void setCanOpenDoors(boolean pCanBreakDoors) {
      this.nodeEvaluator.setCanOpenDoors(pCanBreakDoors);
   }

   public boolean canOpenDoors() {
      return this.nodeEvaluator.canPassDoors();
   }

   public void setAvoidSun(boolean pAvoidSun) {
      this.avoidSun = pAvoidSun;
   }
}