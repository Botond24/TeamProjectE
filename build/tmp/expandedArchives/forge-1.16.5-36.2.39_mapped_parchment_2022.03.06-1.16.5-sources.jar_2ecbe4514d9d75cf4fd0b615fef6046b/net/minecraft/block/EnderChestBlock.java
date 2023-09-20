package net.minecraft.block;

import java.util.Random;
import net.minecraft.entity.monster.piglin.PiglinTasks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMerger;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EnderChestBlock extends AbstractChestBlock<EnderChestTileEntity> implements IWaterLoggable {
   public static final DirectionProperty FACING = HorizontalBlock.FACING;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
   private static final ITextComponent CONTAINER_TITLE = new TranslationTextComponent("container.enderchest");

   public EnderChestBlock(AbstractBlock.Properties p_i48403_1_) {
      super(p_i48403_1_, () -> {
         return TileEntityType.ENDER_CHEST;
      });
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, Boolean.valueOf(false)));
   }

   @OnlyIn(Dist.CLIENT)
   public TileEntityMerger.ICallbackWrapper<? extends ChestTileEntity> combine(BlockState pState, World pLevel, BlockPos pPos, boolean pOverride) {
      return TileEntityMerger.ICallback::acceptNone;
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return SHAPE;
   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link IBlockState#getRenderType()} whenever possible. Implementing/overriding is fine.
    */
   public BlockRenderType getRenderShape(BlockState pState) {
      return BlockRenderType.ENTITYBLOCK_ANIMATED;
   }

   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
      return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      EnderChestInventory enderchestinventory = pPlayer.getEnderChestInventory();
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      if (enderchestinventory != null && tileentity instanceof EnderChestTileEntity) {
         BlockPos blockpos = pPos.above();
         if (pLevel.getBlockState(blockpos).isRedstoneConductor(pLevel, blockpos)) {
            return ActionResultType.sidedSuccess(pLevel.isClientSide);
         } else if (pLevel.isClientSide) {
            return ActionResultType.SUCCESS;
         } else {
            EnderChestTileEntity enderchesttileentity = (EnderChestTileEntity)tileentity;
            enderchestinventory.setActiveChest(enderchesttileentity);
            pPlayer.openMenu(new SimpleNamedContainerProvider((p_226928_1_, p_226928_2_, p_226928_3_) -> {
               return ChestContainer.threeRows(p_226928_1_, p_226928_2_, enderchestinventory);
            }, CONTAINER_TITLE));
            pPlayer.awardStat(Stats.OPEN_ENDERCHEST);
            PiglinTasks.angerNearbyPiglins(pPlayer, true);
            return ActionResultType.CONSUME;
         }
      } else {
         return ActionResultType.sidedSuccess(pLevel.isClientSide);
      }
   }

   public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
      return new EnderChestTileEntity();
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
    * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
    * of whether the block can receive random update ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
      for(int i = 0; i < 3; ++i) {
         int j = pRand.nextInt(2) * 2 - 1;
         int k = pRand.nextInt(2) * 2 - 1;
         double d0 = (double)pPos.getX() + 0.5D + 0.25D * (double)j;
         double d1 = (double)((float)pPos.getY() + pRand.nextFloat());
         double d2 = (double)pPos.getZ() + 0.5D + 0.25D * (double)k;
         double d3 = (double)(pRand.nextFloat() * (float)j);
         double d4 = ((double)pRand.nextFloat() - 0.5D) * 0.125D;
         double d5 = (double)(pRand.nextFloat() * (float)k);
         pLevel.addParticle(ParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
      }

   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link IBlockState#withRotation(Rotation)} whenever possible. Implementing/overriding is
    * fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRotation) {
      return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
   }

   /**
    * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link IBlockState#withMirror(Mirror)} whenever possible. Implementing/overriding is fine.
    */
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING, WATERLOGGED);
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   /**
    * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific face passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pState.getValue(WATERLOGGED)) {
         pLevel.getLiquidTicks().scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
      }

      return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public boolean isPathfindable(BlockState pState, IBlockReader pLevel, BlockPos pPos, PathType pType) {
      return false;
   }
}