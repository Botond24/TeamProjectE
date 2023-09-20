package net.minecraft.block;

import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

public class KelpBlock extends AbstractBodyPlantBlock implements ILiquidContainer {
   public KelpBlock(AbstractBlock.Properties p_i48782_1_) {
      super(p_i48782_1_, Direction.UP, VoxelShapes.block(), true);
   }

   protected AbstractTopPlantBlock getHeadBlock() {
      return (AbstractTopPlantBlock)Blocks.KELP;
   }

   public FluidState getFluidState(BlockState pState) {
      return Fluids.WATER.getSource(false);
   }

   public boolean canPlaceLiquid(IBlockReader pLevel, BlockPos pPos, BlockState pState, Fluid pFluid) {
      return false;
   }

   public boolean placeLiquid(IWorld pLevel, BlockPos pPos, BlockState pState, FluidState pFluidState) {
      return false;
   }
}