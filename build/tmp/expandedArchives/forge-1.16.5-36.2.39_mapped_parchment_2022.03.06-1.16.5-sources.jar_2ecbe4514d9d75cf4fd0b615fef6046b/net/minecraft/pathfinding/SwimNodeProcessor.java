package net.minecraft.pathfinding;

import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.entity.MobEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;

public class SwimNodeProcessor extends NodeProcessor {
   private final boolean allowBreaching;

   public SwimNodeProcessor(boolean p_i48927_1_) {
      this.allowBreaching = p_i48927_1_;
   }

   public PathPoint getStart() {
      return super.getNode(MathHelper.floor(this.mob.getBoundingBox().minX), MathHelper.floor(this.mob.getBoundingBox().minY + 0.5D), MathHelper.floor(this.mob.getBoundingBox().minZ));
   }

   public FlaggedPathPoint getGoal(double p_224768_1_, double p_224768_3_, double p_224768_5_) {
      return new FlaggedPathPoint(super.getNode(MathHelper.floor(p_224768_1_ - (double)(this.mob.getBbWidth() / 2.0F)), MathHelper.floor(p_224768_3_ + 0.5D), MathHelper.floor(p_224768_5_ - (double)(this.mob.getBbWidth() / 2.0F))));
   }

   public int getNeighbors(PathPoint[] p_222859_1_, PathPoint p_222859_2_) {
      int i = 0;

      for(Direction direction : Direction.values()) {
         PathPoint pathpoint = this.getWaterNode(p_222859_2_.x + direction.getStepX(), p_222859_2_.y + direction.getStepY(), p_222859_2_.z + direction.getStepZ());
         if (pathpoint != null && !pathpoint.closed) {
            p_222859_1_[i++] = pathpoint;
         }
      }

      return i;
   }

   /**
    * Returns the significant (e.g LAVA if the entity were half in lava) node type at the location taking the
    * surroundings and the entity size in account
    */
   public PathNodeType getBlockPathType(IBlockReader pBlockaccess, int pX, int pY, int pZ, MobEntity pEntityliving, int pXSize, int pYSize, int pZSize, boolean pCanBreakDoors, boolean pCanEnterDoors) {
      return this.getBlockPathType(pBlockaccess, pX, pY, pZ);
   }

   /**
    * Returns the node type at the specified postion taking the block below into account
    */
   public PathNodeType getBlockPathType(IBlockReader pLevel, int pX, int pY, int pZ) {
      BlockPos blockpos = new BlockPos(pX, pY, pZ);
      FluidState fluidstate = pLevel.getFluidState(blockpos);
      BlockState blockstate = pLevel.getBlockState(blockpos);
      if (fluidstate.isEmpty() && blockstate.isPathfindable(pLevel, blockpos.below(), PathType.WATER) && blockstate.isAir()) {
         return PathNodeType.BREACH;
      } else {
         return fluidstate.is(FluidTags.WATER) && blockstate.isPathfindable(pLevel, blockpos, PathType.WATER) ? PathNodeType.WATER : PathNodeType.BLOCKED;
      }
   }

   @Nullable
   private PathPoint getWaterNode(int p_186328_1_, int p_186328_2_, int p_186328_3_) {
      PathNodeType pathnodetype = this.isFree(p_186328_1_, p_186328_2_, p_186328_3_);
      return (!this.allowBreaching || pathnodetype != PathNodeType.BREACH) && pathnodetype != PathNodeType.WATER ? null : this.getNode(p_186328_1_, p_186328_2_, p_186328_3_);
   }

   /**
    * Returns a mapped point or creates and adds one
    */
   @Nullable
   protected PathPoint getNode(int pX, int pY, int pZ) {
      PathPoint pathpoint = null;
      PathNodeType pathnodetype = this.getBlockPathType(this.mob.level, pX, pY, pZ);
      float f = this.mob.getPathfindingMalus(pathnodetype);
      if (f >= 0.0F) {
         pathpoint = super.getNode(pX, pY, pZ);
         pathpoint.type = pathnodetype;
         pathpoint.costMalus = Math.max(pathpoint.costMalus, f);
         if (this.level.getFluidState(new BlockPos(pX, pY, pZ)).isEmpty()) {
            pathpoint.costMalus += 8.0F;
         }
      }

      return pathnodetype == PathNodeType.OPEN ? pathpoint : pathpoint;
   }

   private PathNodeType isFree(int p_186327_1_, int p_186327_2_, int p_186327_3_) {
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

      for(int i = p_186327_1_; i < p_186327_1_ + this.entityWidth; ++i) {
         for(int j = p_186327_2_; j < p_186327_2_ + this.entityHeight; ++j) {
            for(int k = p_186327_3_; k < p_186327_3_ + this.entityDepth; ++k) {
               FluidState fluidstate = this.level.getFluidState(blockpos$mutable.set(i, j, k));
               BlockState blockstate = this.level.getBlockState(blockpos$mutable.set(i, j, k));
               if (fluidstate.isEmpty() && blockstate.isPathfindable(this.level, blockpos$mutable.below(), PathType.WATER) && blockstate.isAir()) {
                  return PathNodeType.BREACH;
               }

               if (!fluidstate.is(FluidTags.WATER)) {
                  return PathNodeType.BLOCKED;
               }
            }
         }
      }

      BlockState blockstate1 = this.level.getBlockState(blockpos$mutable);
      return blockstate1.isPathfindable(this.level, blockpos$mutable, PathType.WATER) ? PathNodeType.WATER : PathNodeType.BLOCKED;
   }
}