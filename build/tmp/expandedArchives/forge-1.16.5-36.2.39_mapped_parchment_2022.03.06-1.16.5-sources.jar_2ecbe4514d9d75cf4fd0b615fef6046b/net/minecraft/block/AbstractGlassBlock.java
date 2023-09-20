package net.minecraft.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractGlassBlock extends BreakableBlock {
   protected AbstractGlassBlock(AbstractBlock.Properties p_i49999_1_) {
      super(p_i49999_1_);
   }

   public VoxelShape getVisualShape(BlockState pState, IBlockReader pReader, BlockPos pPos, ISelectionContext pContext) {
      return VoxelShapes.empty();
   }

   @OnlyIn(Dist.CLIENT)
   public float getShadeBrightness(BlockState pState, IBlockReader pLevel, BlockPos pPos) {
      return 1.0F;
   }

   public boolean propagatesSkylightDown(BlockState pState, IBlockReader pReader, BlockPos pPos) {
      return true;
   }
}