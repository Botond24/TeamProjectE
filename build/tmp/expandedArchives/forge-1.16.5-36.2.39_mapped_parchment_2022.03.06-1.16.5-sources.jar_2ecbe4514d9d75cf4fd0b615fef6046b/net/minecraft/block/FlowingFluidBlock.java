package net.minecraft.block;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FlowingFluidBlock extends Block implements IBucketPickupHandler {
   public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;
   private final FlowingFluid fluid;
   private final List<FluidState> stateCache;
   public static final VoxelShape STABLE_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);

   @Deprecated  // Forge: Use the constructor that takes a supplier
   public FlowingFluidBlock(FlowingFluid pFluid, AbstractBlock.Properties pProperties) {
      super(pProperties);
      this.fluid = pFluid;
      this.stateCache = Lists.newArrayList();
      this.stateCache.add(pFluid.getSource(false));

      for(int i = 1; i < 8; ++i) {
         this.stateCache.add(pFluid.getFlowing(8 - i, false));
      }

      this.stateCache.add(pFluid.getFlowing(8, true));
      this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(0)));
      fluidStateCacheInitialized = true;
      supplier = pFluid.delegate;
   }

   /**
    * @param supplier A fluid supplier such as {@link net.minecraftforge.fml.RegistryObject<Fluid>}
    */
   public FlowingFluidBlock(java.util.function.Supplier<? extends FlowingFluid> supplier, AbstractBlock.Properties p_i48368_1_) {
      super(p_i48368_1_);
      this.fluid = null;
      this.stateCache = Lists.newArrayList();
      this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(0)));
      this.supplier = supplier;
   }

   public VoxelShape getCollisionShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return pContext.isAbove(STABLE_SHAPE, pPos, true) && pState.getValue(LEVEL) == 0 && pContext.canStandOnFluid(pLevel.getFluidState(pPos.above()), this.fluid) ? STABLE_SHAPE : VoxelShapes.empty();
   }

   /**
    * Returns whether or not this block is of a type that needs random ticking. Called for ref-counting purposes by
    * ExtendedBlockStorage in order to broadly cull a chunk from the random chunk update list for efficiency's sake.
    */
   public boolean isRandomlyTicking(BlockState pState) {
      return pState.getFluidState().isRandomlyTicking();
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
      pState.getFluidState().randomTick(pLevel, pPos, pRandom);
   }

   public boolean propagatesSkylightDown(BlockState pState, IBlockReader pReader, BlockPos pPos) {
      return false;
   }

   public boolean isPathfindable(BlockState pState, IBlockReader pLevel, BlockPos pPos, PathType pType) {
      return !this.fluid.is(FluidTags.LAVA);
   }

   public FluidState getFluidState(BlockState pState) {
      int i = pState.getValue(LEVEL);
      if (!fluidStateCacheInitialized) initFluidStateCache();
      return this.stateCache.get(Math.min(i, 8));
   }

   @OnlyIn(Dist.CLIENT)
   public boolean skipRendering(BlockState pState, BlockState pAdjacentBlockState, Direction pSide) {
      return pAdjacentBlockState.getFluidState().getType().isSame(this.fluid);
   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link IBlockState#getRenderType()} whenever possible. Implementing/overriding is fine.
    */
   public BlockRenderType getRenderShape(BlockState pState) {
      return BlockRenderType.INVISIBLE;
   }

   public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
      return Collections.emptyList();
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return VoxelShapes.empty();
   }

   public void onPlace(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (this.shouldSpreadLiquid(pLevel, pPos, pState)) {
         pLevel.getLiquidTicks().scheduleTick(pPos, pState.getFluidState().getType(), this.fluid.getTickDelay(pLevel));
      }

   }

   /**
    * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific face passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pState.getFluidState().isSource() || pFacingState.getFluidState().isSource()) {
         pLevel.getLiquidTicks().scheduleTick(pCurrentPos, pState.getFluidState().getType(), this.fluid.getTickDelay(pLevel));
      }

      return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public void neighborChanged(BlockState pState, World pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      if (this.shouldSpreadLiquid(pLevel, pPos, pState)) {
         pLevel.getLiquidTicks().scheduleTick(pPos, pState.getFluidState().getType(), this.fluid.getTickDelay(pLevel));
      }

   }

   private boolean shouldSpreadLiquid(World pLevel, BlockPos pPos, BlockState pState) {
      if (this.fluid.is(FluidTags.LAVA)) {
         boolean flag = pLevel.getBlockState(pPos.below()).is(Blocks.SOUL_SOIL);

         for(Direction direction : Direction.values()) {
            if (direction != Direction.DOWN) {
               BlockPos blockpos = pPos.relative(direction);
               if (pLevel.getFluidState(blockpos).is(FluidTags.WATER)) {
                  Block block = pLevel.getFluidState(pPos).isSource() ? Blocks.OBSIDIAN : Blocks.COBBLESTONE;
                  pLevel.setBlockAndUpdate(pPos, net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(pLevel, pPos, pPos, block.defaultBlockState()));
                  this.fizz(pLevel, pPos);
                  return false;
               }

               if (flag && pLevel.getBlockState(blockpos).is(Blocks.BLUE_ICE)) {
                  pLevel.setBlockAndUpdate(pPos, net.minecraftforge.event.ForgeEventFactory.fireFluidPlaceBlockEvent(pLevel, pPos, pPos, Blocks.BASALT.defaultBlockState()));
                  this.fizz(pLevel, pPos);
                  return false;
               }
            }
         }
      }

      return true;
   }

   private void fizz(IWorld pLevel, BlockPos pPos) {
      pLevel.levelEvent(1501, pPos, 0);
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(LEVEL);
   }

   public Fluid takeLiquid(IWorld p_204508_1_, BlockPos p_204508_2_, BlockState p_204508_3_) {
      if (p_204508_3_.getValue(LEVEL) == 0) {
         p_204508_1_.setBlock(p_204508_2_, Blocks.AIR.defaultBlockState(), 11);
         return this.fluid;
      } else {
         return Fluids.EMPTY;
      }
   }

   // Forge start
   private final java.util.function.Supplier<? extends Fluid> supplier;
   public FlowingFluid getFluid() {
      return (FlowingFluid)supplier.get();
   }

   private boolean fluidStateCacheInitialized = false;
   protected synchronized void initFluidStateCache() {
      if (fluidStateCacheInitialized == false) {
         this.stateCache.add(getFluid().getSource(false));

         for (int i = 1; i < 8; ++i)
            this.stateCache.add(getFluid().getFlowing(8 - i, false));

         this.stateCache.add(getFluid().getFlowing(8, true));
         fluidStateCacheInitialized = true;
      }
   }
}
