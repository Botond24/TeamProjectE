package net.minecraft.block;

import java.util.Random;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LeavesBlock extends Block implements net.minecraftforge.common.IForgeShearable {
   public static final IntegerProperty DISTANCE = BlockStateProperties.DISTANCE;
   public static final BooleanProperty PERSISTENT = BlockStateProperties.PERSISTENT;

   public LeavesBlock(AbstractBlock.Properties p_i48370_1_) {
      super(p_i48370_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(DISTANCE, Integer.valueOf(7)).setValue(PERSISTENT, Boolean.valueOf(false)));
   }

   public VoxelShape getBlockSupportShape(BlockState pState, IBlockReader pReader, BlockPos pPos) {
      return VoxelShapes.empty();
   }

   /**
    * Returns whether or not this block is of a type that needs random ticking. Called for ref-counting purposes by
    * ExtendedBlockStorage in order to broadly cull a chunk from the random chunk update list for efficiency's sake.
    */
   public boolean isRandomlyTicking(BlockState pState) {
      return pState.getValue(DISTANCE) == 7 && !pState.getValue(PERSISTENT);
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
      if (!pState.getValue(PERSISTENT) && pState.getValue(DISTANCE) == 7) {
         dropResources(pState, pLevel, pPos);
         pLevel.removeBlock(pPos, false);
      }

   }

   public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
      pLevel.setBlock(pPos, updateDistance(pState, pLevel, pPos), 3);
   }

   public int getLightBlock(BlockState pState, IBlockReader pLevel, BlockPos pPos) {
      return 1;
   }

   /**
    * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific face passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      int i = getDistanceAt(pFacingState) + 1;
      if (i != 1 || pState.getValue(DISTANCE) != i) {
         pLevel.getBlockTicks().scheduleTick(pCurrentPos, this, 1);
      }

      return pState;
   }

   private static BlockState updateDistance(BlockState pState, IWorld pLevel, BlockPos pPos) {
      int i = 7;
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

      for(Direction direction : Direction.values()) {
         blockpos$mutable.setWithOffset(pPos, direction);
         i = Math.min(i, getDistanceAt(pLevel.getBlockState(blockpos$mutable)) + 1);
         if (i == 1) {
            break;
         }
      }

      return pState.setValue(DISTANCE, Integer.valueOf(i));
   }

   private static int getDistanceAt(BlockState pNeighbor) {
      if (BlockTags.LOGS.contains(pNeighbor.getBlock())) {
         return 0;
      } else {
         return pNeighbor.getBlock() instanceof LeavesBlock ? pNeighbor.getValue(DISTANCE) : 7;
      }
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
    * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
    * of whether the block can receive random update ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
      if (pLevel.isRainingAt(pPos.above())) {
         if (pRand.nextInt(15) == 1) {
            BlockPos blockpos = pPos.below();
            BlockState blockstate = pLevel.getBlockState(blockpos);
            if (!blockstate.canOcclude() || !blockstate.isFaceSturdy(pLevel, blockpos, Direction.UP)) {
               double d0 = (double)pPos.getX() + pRand.nextDouble();
               double d1 = (double)pPos.getY() - 0.05D;
               double d2 = (double)pPos.getZ() + pRand.nextDouble();
               pLevel.addParticle(ParticleTypes.DRIPPING_WATER, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            }
         }
      }
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(DISTANCE, PERSISTENT);
   }

   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      return updateDistance(this.defaultBlockState().setValue(PERSISTENT, Boolean.valueOf(true)), pContext.getLevel(), pContext.getClickedPos());
   }
}
