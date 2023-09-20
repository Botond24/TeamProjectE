package net.minecraft.block;

import java.util.Random;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public abstract class AbstractTopPlantBlock extends AbstractPlantBlock implements IGrowable {
   public static final IntegerProperty AGE = BlockStateProperties.AGE_25;
   private final double growPerTickProbability;

   protected AbstractTopPlantBlock(AbstractBlock.Properties pProperties, Direction pGrowthDirection, VoxelShape pShape, boolean pScheduleFluidTicks, double pGrowPerTickProbability) {
      super(pProperties, pGrowthDirection, pShape, pScheduleFluidTicks);
      this.growPerTickProbability = pGrowPerTickProbability;
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
   }

   public BlockState getStateForPlacement(IWorld pLevel) {
      return this.defaultBlockState().setValue(AGE, Integer.valueOf(pLevel.getRandom().nextInt(25)));
   }

   /**
    * Returns whether or not this block is of a type that needs random ticking. Called for ref-counting purposes by
    * ExtendedBlockStorage in order to broadly cull a chunk from the random chunk update list for efficiency's sake.
    */
   public boolean isRandomlyTicking(BlockState pState) {
      return pState.getValue(AGE) < 25;
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
      if (pState.getValue(AGE) < 25 && net.minecraftforge.common.ForgeHooks.onCropsGrowPre(pLevel, pPos.relative(this.growthDirection), pLevel.getBlockState(pPos.relative(this.growthDirection)),pRandom.nextDouble() < this.growPerTickProbability)) {
         BlockPos blockpos = pPos.relative(this.growthDirection);
         if (this.canGrowInto(pLevel.getBlockState(blockpos))) {
            pLevel.setBlockAndUpdate(blockpos, pState.cycle(AGE));
            net.minecraftforge.common.ForgeHooks.onCropsGrowPost(pLevel, blockpos, pLevel.getBlockState(blockpos));
         }
      }

   }

   /**
    * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific face passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pFacing == this.growthDirection.getOpposite() && !pState.canSurvive(pLevel, pCurrentPos)) {
         pLevel.getBlockTicks().scheduleTick(pCurrentPos, this, 1);
      }

      if (pFacing != this.growthDirection || !pFacingState.is(this) && !pFacingState.is(this.getBodyBlock())) {
         if (this.scheduleFluidTicks) {
            pLevel.getLiquidTicks().scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
         }

         return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
      } else {
         return this.getBodyBlock().defaultBlockState();
      }
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(AGE);
   }

   /**
    * Whether this IGrowable can grow
    */
   public boolean isValidBonemealTarget(IBlockReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
      return this.canGrowInto(pLevel.getBlockState(pPos.relative(this.growthDirection)));
   }

   public boolean isBonemealSuccess(World pLevel, Random pRand, BlockPos pPos, BlockState pState) {
      return true;
   }

   public void performBonemeal(ServerWorld pLevel, Random pRand, BlockPos pPos, BlockState pState) {
      BlockPos blockpos = pPos.relative(this.growthDirection);
      int i = Math.min(pState.getValue(AGE) + 1, 25);
      int j = this.getBlocksToGrowWhenBonemealed(pRand);

      for(int k = 0; k < j && this.canGrowInto(pLevel.getBlockState(blockpos)); ++k) {
         pLevel.setBlockAndUpdate(blockpos, pState.setValue(AGE, Integer.valueOf(i)));
         blockpos = blockpos.relative(this.growthDirection);
         i = Math.min(i + 1, 25);
      }

   }

   /**
    * Used to determine how much to grow the plant when using bonemeal. Kelp always returns 1, where as the nether vines
    * return a random value at least 1.
    */
   protected abstract int getBlocksToGrowWhenBonemealed(Random pRandom);

   protected abstract boolean canGrowInto(BlockState pState);

   protected AbstractTopPlantBlock getHeadBlock() {
      return this;
   }
}