package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class LilyPadBlock extends BushBlock {
   protected static final VoxelShape AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 1.5D, 15.0D);

   public LilyPadBlock(AbstractBlock.Properties p_i48297_1_) {
      super(p_i48297_1_);
   }

   public void entityInside(BlockState pState, World pLevel, BlockPos pPos, Entity pEntity) {
      super.entityInside(pState, pLevel, pPos, pEntity);
      if (pLevel instanceof ServerWorld && pEntity instanceof BoatEntity) {
         pLevel.destroyBlock(new BlockPos(pPos), true, pEntity);
      }

   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return AABB;
   }

   protected boolean mayPlaceOn(BlockState pState, IBlockReader pLevel, BlockPos pPos) {
      FluidState fluidstate = pLevel.getFluidState(pPos);
      FluidState fluidstate1 = pLevel.getFluidState(pPos.above());
      return (fluidstate.getType() == Fluids.WATER || pState.getMaterial() == Material.ICE) && fluidstate1.getType() == Fluids.EMPTY;
   }
}