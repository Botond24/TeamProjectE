package net.minecraft.util;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ICollisionReader;

public class TransportationHelper {
   public static int[][] offsetsForDirection(Direction pDirection) {
      Direction direction = pDirection.getClockWise();
      Direction direction1 = direction.getOpposite();
      Direction direction2 = pDirection.getOpposite();
      return new int[][]{{direction.getStepX(), direction.getStepZ()}, {direction1.getStepX(), direction1.getStepZ()}, {direction2.getStepX() + direction.getStepX(), direction2.getStepZ() + direction.getStepZ()}, {direction2.getStepX() + direction1.getStepX(), direction2.getStepZ() + direction1.getStepZ()}, {pDirection.getStepX() + direction.getStepX(), pDirection.getStepZ() + direction.getStepZ()}, {pDirection.getStepX() + direction1.getStepX(), pDirection.getStepZ() + direction1.getStepZ()}, {direction2.getStepX(), direction2.getStepZ()}, {pDirection.getStepX(), pDirection.getStepZ()}};
   }

   public static boolean isBlockFloorValid(double pDistance) {
      return !Double.isInfinite(pDistance) && pDistance < 1.0D;
   }

   public static boolean canDismountTo(ICollisionReader pLevel, LivingEntity pPassenger, AxisAlignedBB pBoundingBox) {
      return pLevel.getBlockCollisions(pPassenger, pBoundingBox).allMatch(VoxelShape::isEmpty);
   }

   @Nullable
   public static Vector3d findDismountLocation(ICollisionReader pLevel, double pX, double pY, double pZ, LivingEntity pPassenger, Pose pPos) {
      if (isBlockFloorValid(pY)) {
         Vector3d vector3d = new Vector3d(pX, pY, pZ);
         if (canDismountTo(pLevel, pPassenger, pPassenger.getLocalBoundsForPose(pPos).move(vector3d))) {
            return vector3d;
         }
      }

      return null;
   }

   public static VoxelShape nonClimbableShape(IBlockReader pLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      return !blockstate.is(BlockTags.CLIMBABLE) && (!(blockstate.getBlock() instanceof TrapDoorBlock) || !blockstate.getValue(TrapDoorBlock.OPEN)) ? blockstate.getCollisionShape(pLevel, pPos) : VoxelShapes.empty();
   }

   public static double findCeilingFrom(BlockPos pPos, int pCeiling, Function<BlockPos, VoxelShape> pShapeForPos) {
      BlockPos.Mutable blockpos$mutable = pPos.mutable();
      int i = 0;

      while(i < pCeiling) {
         VoxelShape voxelshape = pShapeForPos.apply(blockpos$mutable);
         if (!voxelshape.isEmpty()) {
            return (double)(pPos.getY() + i) + voxelshape.min(Direction.Axis.Y);
         }

         ++i;
         blockpos$mutable.move(Direction.UP);
      }

      return Double.POSITIVE_INFINITY;
   }

   @Nullable
   public static Vector3d findSafeDismountLocation(EntityType<?> pEntityType, ICollisionReader pLevel, BlockPos pPos, boolean pOnlySafePositions) {
      if (pOnlySafePositions && pEntityType.isBlockDangerous(pLevel.getBlockState(pPos))) {
         return null;
      } else {
         double d0 = pLevel.getBlockFloorHeight(nonClimbableShape(pLevel, pPos), () -> {
            return nonClimbableShape(pLevel, pPos.below());
         });
         if (!isBlockFloorValid(d0)) {
            return null;
         } else if (pOnlySafePositions && d0 <= 0.0D && pEntityType.isBlockDangerous(pLevel.getBlockState(pPos.below()))) {
            return null;
         } else {
            Vector3d vector3d = Vector3d.upFromBottomCenterOf(pPos, d0);
            return pLevel.getBlockCollisions((Entity)null, pEntityType.getDimensions().makeBoundingBox(vector3d)).allMatch(VoxelShape::isEmpty) ? vector3d : null;
         }
      }
   }
}