package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.trees.Tree;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class SaplingBlock extends BushBlock implements IGrowable {
   public static final IntegerProperty STAGE = BlockStateProperties.STAGE;
   protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D);
   private final Tree treeGrower;

   public SaplingBlock(Tree pTreeGrower, AbstractBlock.Properties pProperties) {
      super(pProperties);
      this.treeGrower = pTreeGrower;
      this.registerDefaultState(this.stateDefinition.any().setValue(STAGE, Integer.valueOf(0)));
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return SHAPE;
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
      if (pLevel.getMaxLocalRawBrightness(pPos.above()) >= 9 && pRandom.nextInt(7) == 0) {
      if (!pLevel.isAreaLoaded(pPos, 1)) return; // Forge: prevent loading unloaded chunks when checking neighbor's light
         this.advanceTree(pLevel, pPos, pState, pRandom);
      }

   }

   public void advanceTree(ServerWorld pLevel, BlockPos pPos, BlockState pState, Random pRand) {
      if (pState.getValue(STAGE) == 0) {
         pLevel.setBlock(pPos, pState.cycle(STAGE), 4);
      } else {
         if (!net.minecraftforge.event.ForgeEventFactory.saplingGrowTree(pLevel, pRand, pPos)) return;
         this.treeGrower.growTree(pLevel, pLevel.getChunkSource().getGenerator(), pPos, pState, pRand);
      }

   }

   /**
    * Whether this IGrowable can grow
    */
   public boolean isValidBonemealTarget(IBlockReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
      return true;
   }

   public boolean isBonemealSuccess(World pLevel, Random pRand, BlockPos pPos, BlockState pState) {
      return (double)pLevel.random.nextFloat() < 0.45D;
   }

   public void performBonemeal(ServerWorld pLevel, Random pRand, BlockPos pPos, BlockState pState) {
      this.advanceTree(pLevel, pPos, pState, pRand);
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(STAGE);
   }
}
