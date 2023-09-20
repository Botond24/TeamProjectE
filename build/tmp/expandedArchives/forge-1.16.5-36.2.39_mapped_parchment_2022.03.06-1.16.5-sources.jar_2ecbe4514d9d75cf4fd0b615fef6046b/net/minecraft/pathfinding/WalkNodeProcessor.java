package net.minecraft.pathfinding;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.MobEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.Region;

public class WalkNodeProcessor extends NodeProcessor {
   protected float oldWaterCost;
   private final Long2ObjectMap<PathNodeType> pathTypesByPosCache = new Long2ObjectOpenHashMap<>();
   private final Object2BooleanMap<AxisAlignedBB> collisionCache = new Object2BooleanOpenHashMap<>();

   public void prepare(Region p_225578_1_, MobEntity p_225578_2_) {
      super.prepare(p_225578_1_, p_225578_2_);
      this.oldWaterCost = p_225578_2_.getPathfindingMalus(PathNodeType.WATER);
   }

   /**
    * This method is called when all nodes have been processed and PathEntity is created.
    * {@link net.minecraft.world.pathfinder.WalkNodeProcessor WalkNodeProcessor} uses this to change its field {@link
    * net.minecraft.world.pathfinder.WalkNodeProcessor#avoidsWater avoidsWater}
    */
   public void done() {
      this.mob.setPathfindingMalus(PathNodeType.WATER, this.oldWaterCost);
      this.pathTypesByPosCache.clear();
      this.collisionCache.clear();
      super.done();
   }

   public PathPoint getStart() {
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
      int i = MathHelper.floor(this.mob.getY());
      BlockState blockstate = this.level.getBlockState(blockpos$mutable.set(this.mob.getX(), (double)i, this.mob.getZ()));
      if (!this.mob.canStandOnFluid(blockstate.getFluidState().getType())) {
         if (this.canFloat() && this.mob.isInWater()) {
            while(true) {
               if (blockstate.getBlock() != Blocks.WATER && blockstate.getFluidState() != Fluids.WATER.getSource(false)) {
                  --i;
                  break;
               }

               ++i;
               blockstate = this.level.getBlockState(blockpos$mutable.set(this.mob.getX(), (double)i, this.mob.getZ()));
            }
         } else if (this.mob.isOnGround()) {
            i = MathHelper.floor(this.mob.getY() + 0.5D);
         } else {
            BlockPos blockpos;
            for(blockpos = this.mob.blockPosition(); (this.level.getBlockState(blockpos).isAir() || this.level.getBlockState(blockpos).isPathfindable(this.level, blockpos, PathType.LAND)) && blockpos.getY() > 0; blockpos = blockpos.below()) {
            }

            i = blockpos.above().getY();
         }
      } else {
         while(this.mob.canStandOnFluid(blockstate.getFluidState().getType())) {
            ++i;
            blockstate = this.level.getBlockState(blockpos$mutable.set(this.mob.getX(), (double)i, this.mob.getZ()));
         }

         --i;
      }

      BlockPos blockpos1 = this.mob.blockPosition();
      PathNodeType pathnodetype = this.getCachedBlockType(this.mob, blockpos1.getX(), i, blockpos1.getZ());
      if (this.mob.getPathfindingMalus(pathnodetype) < 0.0F) {
         AxisAlignedBB axisalignedbb = this.mob.getBoundingBox();
         if (this.hasPositiveMalus(blockpos$mutable.set(axisalignedbb.minX, (double)i, axisalignedbb.minZ)) || this.hasPositiveMalus(blockpos$mutable.set(axisalignedbb.minX, (double)i, axisalignedbb.maxZ)) || this.hasPositiveMalus(blockpos$mutable.set(axisalignedbb.maxX, (double)i, axisalignedbb.minZ)) || this.hasPositiveMalus(blockpos$mutable.set(axisalignedbb.maxX, (double)i, axisalignedbb.maxZ))) {
            PathPoint pathpoint = this.getNode(blockpos$mutable);
            pathpoint.type = this.getBlockPathType(this.mob, pathpoint.asBlockPos());
            pathpoint.costMalus = this.mob.getPathfindingMalus(pathpoint.type);
            return pathpoint;
         }
      }

      PathPoint pathpoint1 = this.getNode(blockpos1.getX(), i, blockpos1.getZ());
      pathpoint1.type = this.getBlockPathType(this.mob, pathpoint1.asBlockPos());
      pathpoint1.costMalus = this.mob.getPathfindingMalus(pathpoint1.type);
      return pathpoint1;
   }

   private boolean hasPositiveMalus(BlockPos p_237239_1_) {
      PathNodeType pathnodetype = this.getBlockPathType(this.mob, p_237239_1_);
      return this.mob.getPathfindingMalus(pathnodetype) >= 0.0F;
   }

   public FlaggedPathPoint getGoal(double p_224768_1_, double p_224768_3_, double p_224768_5_) {
      return new FlaggedPathPoint(this.getNode(MathHelper.floor(p_224768_1_), MathHelper.floor(p_224768_3_), MathHelper.floor(p_224768_5_)));
   }

   public int getNeighbors(PathPoint[] p_222859_1_, PathPoint p_222859_2_) {
      int i = 0;
      int j = 0;
      PathNodeType pathnodetype = this.getCachedBlockType(this.mob, p_222859_2_.x, p_222859_2_.y + 1, p_222859_2_.z);
      PathNodeType pathnodetype1 = this.getCachedBlockType(this.mob, p_222859_2_.x, p_222859_2_.y, p_222859_2_.z);
      if (this.mob.getPathfindingMalus(pathnodetype) >= 0.0F && pathnodetype1 != PathNodeType.STICKY_HONEY) {
         j = MathHelper.floor(Math.max(1.0F, this.mob.maxUpStep));
      }

      double d0 = getFloorLevel(this.level, new BlockPos(p_222859_2_.x, p_222859_2_.y, p_222859_2_.z));
      PathPoint pathpoint = this.getLandNode(p_222859_2_.x, p_222859_2_.y, p_222859_2_.z + 1, j, d0, Direction.SOUTH, pathnodetype1);
      if (this.isNeighborValid(pathpoint, p_222859_2_)) {
         p_222859_1_[i++] = pathpoint;
      }

      PathPoint pathpoint1 = this.getLandNode(p_222859_2_.x - 1, p_222859_2_.y, p_222859_2_.z, j, d0, Direction.WEST, pathnodetype1);
      if (this.isNeighborValid(pathpoint1, p_222859_2_)) {
         p_222859_1_[i++] = pathpoint1;
      }

      PathPoint pathpoint2 = this.getLandNode(p_222859_2_.x + 1, p_222859_2_.y, p_222859_2_.z, j, d0, Direction.EAST, pathnodetype1);
      if (this.isNeighborValid(pathpoint2, p_222859_2_)) {
         p_222859_1_[i++] = pathpoint2;
      }

      PathPoint pathpoint3 = this.getLandNode(p_222859_2_.x, p_222859_2_.y, p_222859_2_.z - 1, j, d0, Direction.NORTH, pathnodetype1);
      if (this.isNeighborValid(pathpoint3, p_222859_2_)) {
         p_222859_1_[i++] = pathpoint3;
      }

      PathPoint pathpoint4 = this.getLandNode(p_222859_2_.x - 1, p_222859_2_.y, p_222859_2_.z - 1, j, d0, Direction.NORTH, pathnodetype1);
      if (this.isDiagonalValid(p_222859_2_, pathpoint1, pathpoint3, pathpoint4)) {
         p_222859_1_[i++] = pathpoint4;
      }

      PathPoint pathpoint5 = this.getLandNode(p_222859_2_.x + 1, p_222859_2_.y, p_222859_2_.z - 1, j, d0, Direction.NORTH, pathnodetype1);
      if (this.isDiagonalValid(p_222859_2_, pathpoint2, pathpoint3, pathpoint5)) {
         p_222859_1_[i++] = pathpoint5;
      }

      PathPoint pathpoint6 = this.getLandNode(p_222859_2_.x - 1, p_222859_2_.y, p_222859_2_.z + 1, j, d0, Direction.SOUTH, pathnodetype1);
      if (this.isDiagonalValid(p_222859_2_, pathpoint1, pathpoint, pathpoint6)) {
         p_222859_1_[i++] = pathpoint6;
      }

      PathPoint pathpoint7 = this.getLandNode(p_222859_2_.x + 1, p_222859_2_.y, p_222859_2_.z + 1, j, d0, Direction.SOUTH, pathnodetype1);
      if (this.isDiagonalValid(p_222859_2_, pathpoint2, pathpoint, pathpoint7)) {
         p_222859_1_[i++] = pathpoint7;
      }

      return i;
   }

   private boolean isNeighborValid(PathPoint p_237235_1_, PathPoint p_237235_2_) {
      return p_237235_1_ != null && !p_237235_1_.closed && (p_237235_1_.costMalus >= 0.0F || p_237235_2_.costMalus < 0.0F);
   }

   private boolean isDiagonalValid(PathPoint p_222860_1_, @Nullable PathPoint p_222860_2_, @Nullable PathPoint p_222860_3_, @Nullable PathPoint p_222860_4_) {
      if (p_222860_4_ != null && p_222860_3_ != null && p_222860_2_ != null) {
         if (p_222860_4_.closed) {
            return false;
         } else if (p_222860_3_.y <= p_222860_1_.y && p_222860_2_.y <= p_222860_1_.y) {
            if (p_222860_2_.type != PathNodeType.WALKABLE_DOOR && p_222860_3_.type != PathNodeType.WALKABLE_DOOR && p_222860_4_.type != PathNodeType.WALKABLE_DOOR) {
               boolean flag = p_222860_3_.type == PathNodeType.FENCE && p_222860_2_.type == PathNodeType.FENCE && (double)this.mob.getBbWidth() < 0.5D;
               return p_222860_4_.costMalus >= 0.0F && (p_222860_3_.y < p_222860_1_.y || p_222860_3_.costMalus >= 0.0F || flag) && (p_222860_2_.y < p_222860_1_.y || p_222860_2_.costMalus >= 0.0F || flag);
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean canReachWithoutCollision(PathPoint p_237234_1_) {
      Vector3d vector3d = new Vector3d((double)p_237234_1_.x - this.mob.getX(), (double)p_237234_1_.y - this.mob.getY(), (double)p_237234_1_.z - this.mob.getZ());
      AxisAlignedBB axisalignedbb = this.mob.getBoundingBox();
      int i = MathHelper.ceil(vector3d.length() / axisalignedbb.getSize());
      vector3d = vector3d.scale((double)(1.0F / (float)i));

      for(int j = 1; j <= i; ++j) {
         axisalignedbb = axisalignedbb.move(vector3d);
         if (this.hasCollisions(axisalignedbb)) {
            return false;
         }
      }

      return true;
   }

   public static double getFloorLevel(IBlockReader pLevel, BlockPos pPos) {
      BlockPos blockpos = pPos.below();
      VoxelShape voxelshape = pLevel.getBlockState(blockpos).getCollisionShape(pLevel, blockpos);
      return (double)blockpos.getY() + (voxelshape.isEmpty() ? 0.0D : voxelshape.max(Direction.Axis.Y));
   }

   @Nullable
   private PathPoint getLandNode(int p_186332_1_, int p_186332_2_, int p_186332_3_, int p_186332_4_, double p_186332_5_, Direction p_186332_7_, PathNodeType p_186332_8_) {
      PathPoint pathpoint = null;
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
      double d0 = getFloorLevel(this.level, blockpos$mutable.set(p_186332_1_, p_186332_2_, p_186332_3_));
      if (d0 - p_186332_5_ > 1.125D) {
         return null;
      } else {
         PathNodeType pathnodetype = this.getCachedBlockType(this.mob, p_186332_1_, p_186332_2_, p_186332_3_);
         float f = this.mob.getPathfindingMalus(pathnodetype);
         double d1 = (double)this.mob.getBbWidth() / 2.0D;
         if (f >= 0.0F) {
            pathpoint = this.getNode(p_186332_1_, p_186332_2_, p_186332_3_);
            pathpoint.type = pathnodetype;
            pathpoint.costMalus = Math.max(pathpoint.costMalus, f);
         }

         if (p_186332_8_ == PathNodeType.FENCE && pathpoint != null && pathpoint.costMalus >= 0.0F && !this.canReachWithoutCollision(pathpoint)) {
            pathpoint = null;
         }

         if (pathnodetype == PathNodeType.WALKABLE) {
            return pathpoint;
         } else {
            if ((pathpoint == null || pathpoint.costMalus < 0.0F) && p_186332_4_ > 0 && pathnodetype != PathNodeType.FENCE && pathnodetype != PathNodeType.UNPASSABLE_RAIL && pathnodetype != PathNodeType.TRAPDOOR) {
               pathpoint = this.getLandNode(p_186332_1_, p_186332_2_ + 1, p_186332_3_, p_186332_4_ - 1, p_186332_5_, p_186332_7_, p_186332_8_);
               if (pathpoint != null && (pathpoint.type == PathNodeType.OPEN || pathpoint.type == PathNodeType.WALKABLE) && this.mob.getBbWidth() < 1.0F) {
                  double d2 = (double)(p_186332_1_ - p_186332_7_.getStepX()) + 0.5D;
                  double d3 = (double)(p_186332_3_ - p_186332_7_.getStepZ()) + 0.5D;
                  AxisAlignedBB axisalignedbb = new AxisAlignedBB(d2 - d1, getFloorLevel(this.level, blockpos$mutable.set(d2, (double)(p_186332_2_ + 1), d3)) + 0.001D, d3 - d1, d2 + d1, (double)this.mob.getBbHeight() + getFloorLevel(this.level, blockpos$mutable.set((double)pathpoint.x, (double)pathpoint.y, (double)pathpoint.z)) - 0.002D, d3 + d1);
                  if (this.hasCollisions(axisalignedbb)) {
                     pathpoint = null;
                  }
               }
            }

            if (pathnodetype == PathNodeType.WATER && !this.canFloat()) {
               if (this.getCachedBlockType(this.mob, p_186332_1_, p_186332_2_ - 1, p_186332_3_) != PathNodeType.WATER) {
                  return pathpoint;
               }

               while(p_186332_2_ > 0) {
                  --p_186332_2_;
                  pathnodetype = this.getCachedBlockType(this.mob, p_186332_1_, p_186332_2_, p_186332_3_);
                  if (pathnodetype != PathNodeType.WATER) {
                     return pathpoint;
                  }

                  pathpoint = this.getNode(p_186332_1_, p_186332_2_, p_186332_3_);
                  pathpoint.type = pathnodetype;
                  pathpoint.costMalus = Math.max(pathpoint.costMalus, this.mob.getPathfindingMalus(pathnodetype));
               }
            }

            if (pathnodetype == PathNodeType.OPEN) {
               int j = 0;
               int i = p_186332_2_;

               while(pathnodetype == PathNodeType.OPEN) {
                  --p_186332_2_;
                  if (p_186332_2_ < 0) {
                     PathPoint pathpoint3 = this.getNode(p_186332_1_, i, p_186332_3_);
                     pathpoint3.type = PathNodeType.BLOCKED;
                     pathpoint3.costMalus = -1.0F;
                     return pathpoint3;
                  }

                  if (j++ >= this.mob.getMaxFallDistance()) {
                     PathPoint pathpoint2 = this.getNode(p_186332_1_, p_186332_2_, p_186332_3_);
                     pathpoint2.type = PathNodeType.BLOCKED;
                     pathpoint2.costMalus = -1.0F;
                     return pathpoint2;
                  }

                  pathnodetype = this.getCachedBlockType(this.mob, p_186332_1_, p_186332_2_, p_186332_3_);
                  f = this.mob.getPathfindingMalus(pathnodetype);
                  if (pathnodetype != PathNodeType.OPEN && f >= 0.0F) {
                     pathpoint = this.getNode(p_186332_1_, p_186332_2_, p_186332_3_);
                     pathpoint.type = pathnodetype;
                     pathpoint.costMalus = Math.max(pathpoint.costMalus, f);
                     break;
                  }

                  if (f < 0.0F) {
                     PathPoint pathpoint1 = this.getNode(p_186332_1_, p_186332_2_, p_186332_3_);
                     pathpoint1.type = PathNodeType.BLOCKED;
                     pathpoint1.costMalus = -1.0F;
                     return pathpoint1;
                  }
               }
            }

            if (pathnodetype == PathNodeType.FENCE) {
               pathpoint = this.getNode(p_186332_1_, p_186332_2_, p_186332_3_);
               pathpoint.closed = true;
               pathpoint.type = pathnodetype;
               pathpoint.costMalus = pathnodetype.getMalus();
            }

            return pathpoint;
         }
      }
   }

   private boolean hasCollisions(AxisAlignedBB p_237236_1_) {
      return this.collisionCache.computeIfAbsent(p_237236_1_, (p_237237_2_) -> {
         return !this.level.noCollision(this.mob, p_237236_1_);
      });
   }

   /**
    * Returns the significant (e.g LAVA if the entity were half in lava) node type at the location taking the
    * surroundings and the entity size in account
    */
   public PathNodeType getBlockPathType(IBlockReader pBlockaccess, int pX, int pY, int pZ, MobEntity pEntityliving, int pXSize, int pYSize, int pZSize, boolean pCanBreakDoors, boolean pCanEnterDoors) {
      EnumSet<PathNodeType> enumset = EnumSet.noneOf(PathNodeType.class);
      PathNodeType pathnodetype = PathNodeType.BLOCKED;
      BlockPos blockpos = pEntityliving.blockPosition();
      pathnodetype = this.getBlockPathTypes(pBlockaccess, pX, pY, pZ, pXSize, pYSize, pZSize, pCanBreakDoors, pCanEnterDoors, enumset, pathnodetype, blockpos);
      if (enumset.contains(PathNodeType.FENCE)) {
         return PathNodeType.FENCE;
      } else if (enumset.contains(PathNodeType.UNPASSABLE_RAIL)) {
         return PathNodeType.UNPASSABLE_RAIL;
      } else {
         PathNodeType pathnodetype1 = PathNodeType.BLOCKED;

         for(PathNodeType pathnodetype2 : enumset) {
            if (pEntityliving.getPathfindingMalus(pathnodetype2) < 0.0F) {
               return pathnodetype2;
            }

            if (pEntityliving.getPathfindingMalus(pathnodetype2) >= pEntityliving.getPathfindingMalus(pathnodetype1)) {
               pathnodetype1 = pathnodetype2;
            }
         }

         return pathnodetype == PathNodeType.OPEN && pEntityliving.getPathfindingMalus(pathnodetype1) == 0.0F && pXSize <= 1 ? PathNodeType.OPEN : pathnodetype1;
      }
   }

   /**
    * Populates the nodeTypeEnum with all the surrounding node types and returns the center one
    */
   public PathNodeType getBlockPathTypes(IBlockReader pLevel, int pX, int pY, int pZ, int pXSize, int pYSize, int pZSize, boolean pCanOpenDoors, boolean pCanEnterDoors, EnumSet<PathNodeType> pNodeTypeEnum, PathNodeType pNodeType, BlockPos pPos) {
      for(int i = 0; i < pXSize; ++i) {
         for(int j = 0; j < pYSize; ++j) {
            for(int k = 0; k < pZSize; ++k) {
               int l = i + pX;
               int i1 = j + pY;
               int j1 = k + pZ;
               PathNodeType pathnodetype = this.getBlockPathType(pLevel, l, i1, j1);
               pathnodetype = this.evaluateBlockPathType(pLevel, pCanOpenDoors, pCanEnterDoors, pPos, pathnodetype);
               if (i == 0 && j == 0 && k == 0) {
                  pNodeType = pathnodetype;
               }

               pNodeTypeEnum.add(pathnodetype);
            }
         }
      }

      return pNodeType;
   }

   /**
    * Returns the exact path node type according to abilities and settings of the entity
    */
   protected PathNodeType evaluateBlockPathType(IBlockReader pLevel, boolean pCanOpenDoors, boolean pCanEnterDoors, BlockPos pPos, PathNodeType pNodeType) {
      if (pNodeType == PathNodeType.DOOR_WOOD_CLOSED && pCanOpenDoors && pCanEnterDoors) {
         pNodeType = PathNodeType.WALKABLE_DOOR;
      }

      if (pNodeType == PathNodeType.DOOR_OPEN && !pCanEnterDoors) {
         pNodeType = PathNodeType.BLOCKED;
      }

      if (pNodeType == PathNodeType.RAIL && !(pLevel.getBlockState(pPos).getBlock() instanceof AbstractRailBlock) && !(pLevel.getBlockState(pPos.below()).getBlock() instanceof AbstractRailBlock)) {
         pNodeType = PathNodeType.UNPASSABLE_RAIL;
      }

      if (pNodeType == PathNodeType.LEAVES) {
         pNodeType = PathNodeType.BLOCKED;
      }

      return pNodeType;
   }

   /**
    * Returns a significant cached path node type for specified position or calculates it
    */
   private PathNodeType getBlockPathType(MobEntity pEntityliving, BlockPos pPos) {
      return this.getCachedBlockType(pEntityliving, pPos.getX(), pPos.getY(), pPos.getZ());
   }

   /**
    * Returns a cached path node type for specified position or calculates it
    */
   private PathNodeType getCachedBlockType(MobEntity pEntity, int pX, int pY, int pZ) {
      return this.pathTypesByPosCache.computeIfAbsent(BlockPos.asLong(pX, pY, pZ), (p_237229_5_) -> {
         return this.getBlockPathType(this.level, pX, pY, pZ, pEntity, this.entityWidth, this.entityHeight, this.entityDepth, this.canOpenDoors(), this.canPassDoors());
      });
   }

   /**
    * Returns the node type at the specified postion taking the block below into account
    */
   public PathNodeType getBlockPathType(IBlockReader pLevel, int pX, int pY, int pZ) {
      return getBlockPathTypeStatic(pLevel, new BlockPos.Mutable(pX, pY, pZ));
   }

   /**
    * Returns the node type at the specified postion taking the block below into account
    */
   public static PathNodeType getBlockPathTypeStatic(IBlockReader pLevel, BlockPos.Mutable pPos) {
      int i = pPos.getX();
      int j = pPos.getY();
      int k = pPos.getZ();
      PathNodeType pathnodetype = getBlockPathTypeRaw(pLevel, pPos);
      if (pathnodetype == PathNodeType.OPEN && j >= 1) {
         PathNodeType pathnodetype1 = getBlockPathTypeRaw(pLevel, pPos.set(i, j - 1, k));
         pathnodetype = pathnodetype1 != PathNodeType.WALKABLE && pathnodetype1 != PathNodeType.OPEN && pathnodetype1 != PathNodeType.WATER && pathnodetype1 != PathNodeType.LAVA ? PathNodeType.WALKABLE : PathNodeType.OPEN;
         if (pathnodetype1 == PathNodeType.DAMAGE_FIRE) {
            pathnodetype = PathNodeType.DAMAGE_FIRE;
         }

         if (pathnodetype1 == PathNodeType.DAMAGE_CACTUS) {
            pathnodetype = PathNodeType.DAMAGE_CACTUS;
         }

         if (pathnodetype1 == PathNodeType.DAMAGE_OTHER) {
            pathnodetype = PathNodeType.DAMAGE_OTHER;
         }

         if (pathnodetype1 == PathNodeType.STICKY_HONEY) {
            pathnodetype = PathNodeType.STICKY_HONEY;
         }
      }

      if (pathnodetype == PathNodeType.WALKABLE) {
         pathnodetype = checkNeighbourBlocks(pLevel, pPos.set(i, j, k), pathnodetype);
      }

      return pathnodetype;
   }

   /**
    * Returns possible dangers in a 3x3 cube, otherwise nodeType
    */
   public static PathNodeType checkNeighbourBlocks(IBlockReader pLevel, BlockPos.Mutable pCenterPos, PathNodeType pNodeType) {
      int i = pCenterPos.getX();
      int j = pCenterPos.getY();
      int k = pCenterPos.getZ();

      for(int l = -1; l <= 1; ++l) {
         for(int i1 = -1; i1 <= 1; ++i1) {
            for(int j1 = -1; j1 <= 1; ++j1) {
               if (l != 0 || j1 != 0) {
                  pCenterPos.set(i + l, j + i1, k + j1);
                  BlockState blockstate = pLevel.getBlockState(pCenterPos);
                  if (blockstate.is(Blocks.CACTUS)) {
                     return PathNodeType.DANGER_CACTUS;
                  }

                  if (blockstate.is(Blocks.SWEET_BERRY_BUSH)) {
                     return PathNodeType.DANGER_OTHER;
                  }

                  if (isBurningBlock(blockstate)) {
                     return PathNodeType.DANGER_FIRE;
                  }

                  if (pLevel.getFluidState(pCenterPos).is(FluidTags.WATER)) {
                     return PathNodeType.WATER_BORDER;
                  }
               }
            }
         }
      }

      return pNodeType;
   }

   protected static PathNodeType getBlockPathTypeRaw(IBlockReader p_237238_0_, BlockPos p_237238_1_) {
      BlockState blockstate = p_237238_0_.getBlockState(p_237238_1_);
      PathNodeType type = blockstate.getAiPathNodeType(p_237238_0_, p_237238_1_);
      if (type != null) return type;
      Block block = blockstate.getBlock();
      Material material = blockstate.getMaterial();
      if (blockstate.isAir(p_237238_0_, p_237238_1_)) {
         return PathNodeType.OPEN;
      } else if (!blockstate.is(BlockTags.TRAPDOORS) && !blockstate.is(Blocks.LILY_PAD)) {
         if (blockstate.is(Blocks.CACTUS)) {
            return PathNodeType.DAMAGE_CACTUS;
         } else if (blockstate.is(Blocks.SWEET_BERRY_BUSH)) {
            return PathNodeType.DAMAGE_OTHER;
         } else if (blockstate.is(Blocks.HONEY_BLOCK)) {
            return PathNodeType.STICKY_HONEY;
         } else if (blockstate.is(Blocks.COCOA)) {
            return PathNodeType.COCOA;
         } else {
            FluidState fluidstate = p_237238_0_.getFluidState(p_237238_1_);
            if (fluidstate.is(FluidTags.WATER)) {
               return PathNodeType.WATER;
            } else if (fluidstate.is(FluidTags.LAVA)) {
               return PathNodeType.LAVA;
            } else if (isBurningBlock(blockstate)) {
               return PathNodeType.DAMAGE_FIRE;
            } else if (DoorBlock.isWoodenDoor(blockstate) && !blockstate.getValue(DoorBlock.OPEN)) {
               return PathNodeType.DOOR_WOOD_CLOSED;
            } else if (block instanceof DoorBlock && material == Material.METAL && !blockstate.getValue(DoorBlock.OPEN)) {
               return PathNodeType.DOOR_IRON_CLOSED;
            } else if (block instanceof DoorBlock && blockstate.getValue(DoorBlock.OPEN)) {
               return PathNodeType.DOOR_OPEN;
            } else if (block instanceof AbstractRailBlock) {
               return PathNodeType.RAIL;
            } else if (block instanceof LeavesBlock) {
               return PathNodeType.LEAVES;
            } else if (!block.is(BlockTags.FENCES) && !block.is(BlockTags.WALLS) && (!(block instanceof FenceGateBlock) || blockstate.getValue(FenceGateBlock.OPEN))) {
               return !blockstate.isPathfindable(p_237238_0_, p_237238_1_, PathType.LAND) ? PathNodeType.BLOCKED : PathNodeType.OPEN;
            } else {
               return PathNodeType.FENCE;
            }
         }
      } else {
         return PathNodeType.TRAPDOOR;
      }
   }

   /**
    * Checks whether the specified block state can cause burn damage
    */
   private static boolean isBurningBlock(BlockState pState) {
      return pState.is(BlockTags.FIRE) || pState.is(Blocks.LAVA) || pState.is(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(pState);
   }
}
