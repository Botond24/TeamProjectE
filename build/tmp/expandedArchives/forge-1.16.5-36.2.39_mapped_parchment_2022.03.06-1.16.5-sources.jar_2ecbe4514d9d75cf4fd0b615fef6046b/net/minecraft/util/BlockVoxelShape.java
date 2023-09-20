package net.minecraft.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public enum BlockVoxelShape {
   FULL {
      public boolean isSupporting(BlockState pState, IBlockReader pLevel, BlockPos pPos, Direction pFace) {
         return Block.isFaceFull(pState.getBlockSupportShape(pLevel, pPos), pFace);
      }
   },
   CENTER {
      private final int CENTER_SUPPORT_WIDTH = 1;
      private final VoxelShape CENTER_SUPPORT_SHAPE = Block.box(7.0D, 0.0D, 7.0D, 9.0D, 10.0D, 9.0D);

      public boolean isSupporting(BlockState pState, IBlockReader pLevel, BlockPos pPos, Direction pFace) {
         return !VoxelShapes.joinIsNotEmpty(pState.getBlockSupportShape(pLevel, pPos).getFaceShape(pFace), this.CENTER_SUPPORT_SHAPE, IBooleanFunction.ONLY_SECOND);
      }
   },
   RIGID {
      private final int RIGID_SUPPORT_WIDTH = 2;
      private final VoxelShape RIGID_SUPPORT_SHAPE = VoxelShapes.join(VoxelShapes.block(), Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D), IBooleanFunction.ONLY_FIRST);

      public boolean isSupporting(BlockState pState, IBlockReader pLevel, BlockPos pPos, Direction pFace) {
         return !VoxelShapes.joinIsNotEmpty(pState.getBlockSupportShape(pLevel, pPos).getFaceShape(pFace), this.RIGID_SUPPORT_SHAPE, IBooleanFunction.ONLY_SECOND);
      }
   };

   private BlockVoxelShape() {
   }

   public abstract boolean isSupporting(BlockState pState, IBlockReader pLevel, BlockPos pPos, Direction pFace);
}