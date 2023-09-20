package net.minecraft.block;

import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.HugeFungusConfig;
import net.minecraft.world.server.ServerWorld;

public class FungusBlock extends BushBlock implements IGrowable {
   protected static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 9.0D, 12.0D);
   private final Supplier<ConfiguredFeature<HugeFungusConfig, ?>> feature;

   public FungusBlock(AbstractBlock.Properties p_i241177_1_, Supplier<ConfiguredFeature<HugeFungusConfig, ?>> p_i241177_2_) {
      super(p_i241177_1_);
      this.feature = p_i241177_2_;
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return SHAPE;
   }

   protected boolean mayPlaceOn(BlockState pState, IBlockReader pLevel, BlockPos pPos) {
      return pState.is(BlockTags.NYLIUM) || pState.is(Blocks.MYCELIUM) || pState.is(Blocks.SOUL_SOIL) || super.mayPlaceOn(pState, pLevel, pPos);
   }

   /**
    * Whether this IGrowable can grow
    */
   public boolean isValidBonemealTarget(IBlockReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
      Block block = ((HugeFungusConfig)(this.feature.get()).config).validBaseState.getBlock();
      Block block1 = pLevel.getBlockState(pPos.below()).getBlock();
      return block1 == block;
   }

   public boolean isBonemealSuccess(World pLevel, Random pRand, BlockPos pPos, BlockState pState) {
      return (double)pRand.nextFloat() < 0.4D;
   }

   public void performBonemeal(ServerWorld pLevel, Random pRand, BlockPos pPos, BlockState pState) {
      this.feature.get().place(pLevel, pLevel.getChunkSource().getGenerator(), pRand, pPos);
   }
}