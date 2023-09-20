package net.minecraft.block;

import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BubbleColumnBlock extends Block implements IBucketPickupHandler {
   public static final BooleanProperty DRAG_DOWN = BlockStateProperties.DRAG;

   public BubbleColumnBlock(AbstractBlock.Properties p_i48783_1_) {
      super(p_i48783_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(DRAG_DOWN, Boolean.valueOf(true)));
   }

   public void entityInside(BlockState pState, World pLevel, BlockPos pPos, Entity pEntity) {
      BlockState blockstate = pLevel.getBlockState(pPos.above());
      if (blockstate.isAir()) {
         pEntity.onAboveBubbleCol(pState.getValue(DRAG_DOWN));
         if (!pLevel.isClientSide) {
            ServerWorld serverworld = (ServerWorld)pLevel;

            for(int i = 0; i < 2; ++i) {
               serverworld.sendParticles(ParticleTypes.SPLASH, (double)pPos.getX() + pLevel.random.nextDouble(), (double)(pPos.getY() + 1), (double)pPos.getZ() + pLevel.random.nextDouble(), 1, 0.0D, 0.0D, 0.0D, 1.0D);
               serverworld.sendParticles(ParticleTypes.BUBBLE, (double)pPos.getX() + pLevel.random.nextDouble(), (double)(pPos.getY() + 1), (double)pPos.getZ() + pLevel.random.nextDouble(), 1, 0.0D, 0.01D, 0.0D, 0.2D);
            }
         }
      } else {
         pEntity.onInsideBubbleColumn(pState.getValue(DRAG_DOWN));
      }

   }

   public void onPlace(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      growColumn(pLevel, pPos.above(), getDrag(pLevel, pPos.below()));
   }

   public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
      growColumn(pLevel, pPos.above(), getDrag(pLevel, pPos));
   }

   public FluidState getFluidState(BlockState pState) {
      return Fluids.WATER.getSource(false);
   }

   public static void growColumn(IWorld p_203159_0_, BlockPos p_203159_1_, boolean p_203159_2_) {
      if (canExistIn(p_203159_0_, p_203159_1_)) {
         p_203159_0_.setBlock(p_203159_1_, Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(DRAG_DOWN, Boolean.valueOf(p_203159_2_)), 2);
      }

   }

   public static boolean canExistIn(IWorld p_208072_0_, BlockPos p_208072_1_) {
      FluidState fluidstate = p_208072_0_.getFluidState(p_208072_1_);
      return p_208072_0_.getBlockState(p_208072_1_).is(Blocks.WATER) && fluidstate.getAmount() >= 8 && fluidstate.isSource();
   }

   private static boolean getDrag(IBlockReader p_203157_0_, BlockPos p_203157_1_) {
      BlockState blockstate = p_203157_0_.getBlockState(p_203157_1_);
      if (blockstate.is(Blocks.BUBBLE_COLUMN)) {
         return blockstate.getValue(DRAG_DOWN);
      } else {
         return !blockstate.is(Blocks.SOUL_SAND);
      }
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
    * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
    * of whether the block can receive random update ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
      double d0 = (double)pPos.getX();
      double d1 = (double)pPos.getY();
      double d2 = (double)pPos.getZ();
      if (pState.getValue(DRAG_DOWN)) {
         pLevel.addAlwaysVisibleParticle(ParticleTypes.CURRENT_DOWN, d0 + 0.5D, d1 + 0.8D, d2, 0.0D, 0.0D, 0.0D);
         if (pRand.nextInt(200) == 0) {
            pLevel.playLocalSound(d0, d1, d2, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, SoundCategory.BLOCKS, 0.2F + pRand.nextFloat() * 0.2F, 0.9F + pRand.nextFloat() * 0.15F, false);
         }
      } else {
         pLevel.addAlwaysVisibleParticle(ParticleTypes.BUBBLE_COLUMN_UP, d0 + 0.5D, d1, d2 + 0.5D, 0.0D, 0.04D, 0.0D);
         pLevel.addAlwaysVisibleParticle(ParticleTypes.BUBBLE_COLUMN_UP, d0 + (double)pRand.nextFloat(), d1 + (double)pRand.nextFloat(), d2 + (double)pRand.nextFloat(), 0.0D, 0.04D, 0.0D);
         if (pRand.nextInt(200) == 0) {
            pLevel.playLocalSound(d0, d1, d2, SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundCategory.BLOCKS, 0.2F + pRand.nextFloat() * 0.2F, 0.9F + pRand.nextFloat() * 0.15F, false);
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
      if (!pState.canSurvive(pLevel, pCurrentPos)) {
         return Blocks.WATER.defaultBlockState();
      } else {
         if (pFacing == Direction.DOWN) {
            pLevel.setBlock(pCurrentPos, Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(DRAG_DOWN, Boolean.valueOf(getDrag(pLevel, pFacingPos))), 2);
         } else if (pFacing == Direction.UP && !pFacingState.is(Blocks.BUBBLE_COLUMN) && canExistIn(pLevel, pFacingPos)) {
            pLevel.getBlockTicks().scheduleTick(pCurrentPos, this, 5);
         }

         pLevel.getLiquidTicks().scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
         return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
      }
   }

   public boolean canSurvive(BlockState pState, IWorldReader pLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos.below());
      return blockstate.is(Blocks.BUBBLE_COLUMN) || blockstate.is(Blocks.MAGMA_BLOCK) || blockstate.is(Blocks.SOUL_SAND);
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return VoxelShapes.empty();
   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link IBlockState#getRenderType()} whenever possible. Implementing/overriding is fine.
    */
   public BlockRenderType getRenderShape(BlockState pState) {
      return BlockRenderType.INVISIBLE;
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(DRAG_DOWN);
   }

   public Fluid takeLiquid(IWorld p_204508_1_, BlockPos p_204508_2_, BlockState p_204508_3_) {
      p_204508_1_.setBlock(p_204508_2_, Blocks.AIR.defaultBlockState(), 11);
      return Fluids.WATER;
   }
}