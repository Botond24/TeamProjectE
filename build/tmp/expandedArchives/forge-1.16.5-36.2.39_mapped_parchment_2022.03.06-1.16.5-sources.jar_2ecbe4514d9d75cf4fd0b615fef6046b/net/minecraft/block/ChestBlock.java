package net.minecraft.block;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.piglin.PiglinTasks;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.DoubleSidedInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.ChestType;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.IChestLid;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMerger;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
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

public class ChestBlock extends AbstractChestBlock<ChestTileEntity> implements IWaterLoggable {
   public static final DirectionProperty FACING = HorizontalBlock.FACING;
   public static final EnumProperty<ChestType> TYPE = BlockStateProperties.CHEST_TYPE;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   protected static final VoxelShape NORTH_AABB = Block.box(1.0D, 0.0D, 0.0D, 15.0D, 14.0D, 15.0D);
   protected static final VoxelShape SOUTH_AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 16.0D);
   protected static final VoxelShape WEST_AABB = Block.box(0.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
   protected static final VoxelShape EAST_AABB = Block.box(1.0D, 0.0D, 1.0D, 16.0D, 14.0D, 15.0D);
   protected static final VoxelShape AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
   private static final TileEntityMerger.ICallback<ChestTileEntity, Optional<IInventory>> CHEST_COMBINER = new TileEntityMerger.ICallback<ChestTileEntity, Optional<IInventory>>() {
      public Optional<IInventory> acceptDouble(ChestTileEntity p_225539_1_, ChestTileEntity p_225539_2_) {
         return Optional.of(new DoubleSidedInventory(p_225539_1_, p_225539_2_));
      }

      public Optional<IInventory> acceptSingle(ChestTileEntity p_225538_1_) {
         return Optional.of(p_225538_1_);
      }

      public Optional<IInventory> acceptNone() {
         return Optional.empty();
      }
   };
   private static final TileEntityMerger.ICallback<ChestTileEntity, Optional<INamedContainerProvider>> MENU_PROVIDER_COMBINER = new TileEntityMerger.ICallback<ChestTileEntity, Optional<INamedContainerProvider>>() {
      public Optional<INamedContainerProvider> acceptDouble(final ChestTileEntity p_225539_1_, final ChestTileEntity p_225539_2_) {
         final IInventory iinventory = new DoubleSidedInventory(p_225539_1_, p_225539_2_);
         return Optional.of(new INamedContainerProvider() {
            @Nullable
            public Container createMenu(int p_createMenu_1_, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {
               if (p_225539_1_.canOpen(p_createMenu_3_) && p_225539_2_.canOpen(p_createMenu_3_)) {
                  p_225539_1_.unpackLootTable(p_createMenu_2_.player);
                  p_225539_2_.unpackLootTable(p_createMenu_2_.player);
                  return ChestContainer.sixRows(p_createMenu_1_, p_createMenu_2_, iinventory);
               } else {
                  return null;
               }
            }

            public ITextComponent getDisplayName() {
               if (p_225539_1_.hasCustomName()) {
                  return p_225539_1_.getDisplayName();
               } else {
                  return (ITextComponent)(p_225539_2_.hasCustomName() ? p_225539_2_.getDisplayName() : new TranslationTextComponent("container.chestDouble"));
               }
            }
         });
      }

      public Optional<INamedContainerProvider> acceptSingle(ChestTileEntity p_225538_1_) {
         return Optional.of(p_225538_1_);
      }

      public Optional<INamedContainerProvider> acceptNone() {
         return Optional.empty();
      }
   };

   public ChestBlock(AbstractBlock.Properties p_i225757_1_, Supplier<TileEntityType<? extends ChestTileEntity>> p_i225757_2_) {
      super(p_i225757_1_, p_i225757_2_);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, ChestType.SINGLE).setValue(WATERLOGGED, Boolean.valueOf(false)));
   }

   public static TileEntityMerger.Type getBlockType(BlockState p_226919_0_) {
      ChestType chesttype = p_226919_0_.getValue(TYPE);
      if (chesttype == ChestType.SINGLE) {
         return TileEntityMerger.Type.SINGLE;
      } else {
         return chesttype == ChestType.RIGHT ? TileEntityMerger.Type.FIRST : TileEntityMerger.Type.SECOND;
      }
   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link IBlockState#getRenderType()} whenever possible. Implementing/overriding is fine.
    */
   public BlockRenderType getRenderShape(BlockState pState) {
      return BlockRenderType.ENTITYBLOCK_ANIMATED;
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

      if (pFacingState.is(this) && pFacing.getAxis().isHorizontal()) {
         ChestType chesttype = pFacingState.getValue(TYPE);
         if (pState.getValue(TYPE) == ChestType.SINGLE && chesttype != ChestType.SINGLE && pState.getValue(FACING) == pFacingState.getValue(FACING) && getConnectedDirection(pFacingState) == pFacing.getOpposite()) {
            return pState.setValue(TYPE, chesttype.getOpposite());
         }
      } else if (getConnectedDirection(pState) == pFacing) {
         return pState.setValue(TYPE, ChestType.SINGLE);
      }

      return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      if (pState.getValue(TYPE) == ChestType.SINGLE) {
         return AABB;
      } else {
         switch(getConnectedDirection(pState)) {
         case NORTH:
         default:
            return NORTH_AABB;
         case SOUTH:
            return SOUTH_AABB;
         case WEST:
            return WEST_AABB;
         case EAST:
            return EAST_AABB;
         }
      }
   }

   /**
    * @return the Direction pointing from the given state to its attached double chest
    */
   public static Direction getConnectedDirection(BlockState p_196311_0_) {
      Direction direction = p_196311_0_.getValue(FACING);
      return p_196311_0_.getValue(TYPE) == ChestType.LEFT ? direction.getClockWise() : direction.getCounterClockWise();
   }

   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      ChestType chesttype = ChestType.SINGLE;
      Direction direction = pContext.getHorizontalDirection().getOpposite();
      FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
      boolean flag = pContext.isSecondaryUseActive();
      Direction direction1 = pContext.getClickedFace();
      if (direction1.getAxis().isHorizontal() && flag) {
         Direction direction2 = this.candidatePartnerFacing(pContext, direction1.getOpposite());
         if (direction2 != null && direction2.getAxis() != direction1.getAxis()) {
            direction = direction2;
            chesttype = direction2.getCounterClockWise() == direction1.getOpposite() ? ChestType.RIGHT : ChestType.LEFT;
         }
      }

      if (chesttype == ChestType.SINGLE && !flag) {
         if (direction == this.candidatePartnerFacing(pContext, direction.getClockWise())) {
            chesttype = ChestType.LEFT;
         } else if (direction == this.candidatePartnerFacing(pContext, direction.getCounterClockWise())) {
            chesttype = ChestType.RIGHT;
         }
      }

      return this.defaultBlockState().setValue(FACING, direction).setValue(TYPE, chesttype).setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   /**
    * Returns facing pointing to a chest to form a double chest with, null otherwise
    */
   @Nullable
   private Direction candidatePartnerFacing(BlockItemUseContext pContext, Direction pDirection) {
      BlockState blockstate = pContext.getLevel().getBlockState(pContext.getClickedPos().relative(pDirection));
      return blockstate.is(this) && blockstate.getValue(TYPE) == ChestType.SINGLE ? blockstate.getValue(FACING) : null;
   }

   /**
    * Called by ItemBlocks after a block is set in the world, to allow post-place logic
    */
   public void setPlacedBy(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
      if (pStack.hasCustomHoverName()) {
         TileEntity tileentity = pLevel.getBlockEntity(pPos);
         if (tileentity instanceof ChestTileEntity) {
            ((ChestTileEntity)tileentity).setCustomName(pStack.getHoverName());
         }
      }

   }

   public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pState.is(pNewState.getBlock())) {
         TileEntity tileentity = pLevel.getBlockEntity(pPos);
         if (tileentity instanceof IInventory) {
            InventoryHelper.dropContents(pLevel, pPos, (IInventory)tileentity);
            pLevel.updateNeighbourForOutputSignal(pPos, this);
         }

         super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
      }
   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      if (pLevel.isClientSide) {
         return ActionResultType.SUCCESS;
      } else {
         INamedContainerProvider inamedcontainerprovider = this.getMenuProvider(pState, pLevel, pPos);
         if (inamedcontainerprovider != null) {
            pPlayer.openMenu(inamedcontainerprovider);
            pPlayer.awardStat(this.getOpenChestStat());
            PiglinTasks.angerNearbyPiglins(pPlayer, true);
         }

         return ActionResultType.CONSUME;
      }
   }

   protected Stat<ResourceLocation> getOpenChestStat() {
      return Stats.CUSTOM.get(Stats.OPEN_CHEST);
   }

   @Nullable
   public static IInventory getContainer(ChestBlock pChest, BlockState pState, World pLevel, BlockPos pPos, boolean pOverride) {
      return pChest.combine(pState, pLevel, pPos, pOverride).<Optional<IInventory>>apply(CHEST_COMBINER).orElse((IInventory)null);
   }

   public TileEntityMerger.ICallbackWrapper<? extends ChestTileEntity> combine(BlockState pState, World pLevel, BlockPos pPos, boolean pOverride) {
      BiPredicate<IWorld, BlockPos> bipredicate;
      if (pOverride) {
         bipredicate = (p_226918_0_, p_226918_1_) -> {
            return false;
         };
      } else {
         bipredicate = ChestBlock::isChestBlockedAt;
      }

      return TileEntityMerger.combineWithNeigbour(this.blockEntityType.get(), ChestBlock::getBlockType, ChestBlock::getConnectedDirection, FACING, pState, pLevel, pPos, bipredicate);
   }

   @Nullable
   public INamedContainerProvider getMenuProvider(BlockState pState, World pLevel, BlockPos pPos) {
      return this.combine(pState, pLevel, pPos, false).<Optional<INamedContainerProvider>>apply(MENU_PROVIDER_COMBINER).orElse((INamedContainerProvider)null);
   }

   @OnlyIn(Dist.CLIENT)
   public static TileEntityMerger.ICallback<ChestTileEntity, Float2FloatFunction> opennessCombiner(final IChestLid pLid) {
      return new TileEntityMerger.ICallback<ChestTileEntity, Float2FloatFunction>() {
         public Float2FloatFunction acceptDouble(ChestTileEntity p_225539_1_, ChestTileEntity p_225539_2_) {
            return (p_226921_2_) -> {
               return Math.max(p_225539_1_.getOpenNess(p_226921_2_), p_225539_2_.getOpenNess(p_226921_2_));
            };
         }

         public Float2FloatFunction acceptSingle(ChestTileEntity p_225538_1_) {
            return p_225538_1_::getOpenNess;
         }

         public Float2FloatFunction acceptNone() {
            return pLid::getOpenNess;
         }
      };
   }

   public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
      return new ChestTileEntity();
   }

   public static boolean isChestBlockedAt(IWorld p_220108_0_, BlockPos p_220108_1_) {
      return isBlockedChestByBlock(p_220108_0_, p_220108_1_) || isCatSittingOnChest(p_220108_0_, p_220108_1_);
   }

   private static boolean isBlockedChestByBlock(IBlockReader pLevel, BlockPos pPos) {
      BlockPos blockpos = pPos.above();
      return pLevel.getBlockState(blockpos).isRedstoneConductor(pLevel, blockpos);
   }

   private static boolean isCatSittingOnChest(IWorld pLevel, BlockPos pPos) {
      List<CatEntity> list = pLevel.getEntitiesOfClass(CatEntity.class, new AxisAlignedBB((double)pPos.getX(), (double)(pPos.getY() + 1), (double)pPos.getZ(), (double)(pPos.getX() + 1), (double)(pPos.getY() + 2), (double)(pPos.getZ() + 1)));
      if (!list.isEmpty()) {
         for(CatEntity catentity : list) {
            if (catentity.isInSittingPose()) {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * @deprecated call via {@link IBlockState#hasComparatorInputOverride()} whenever possible. Implementing/overriding
    * is fine.
    */
   public boolean hasAnalogOutputSignal(BlockState pState) {
      return true;
   }

   /**
    * @deprecated call via {@link IBlockState#getComparatorInputOverride(World,BlockPos)} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getAnalogOutputSignal(BlockState pBlockState, World pLevel, BlockPos pPos) {
      return Container.getRedstoneSignalFromContainer(getContainer(this, pBlockState, pLevel, pPos, false));
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
      BlockState rotated = pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
      return pMirror == Mirror.NONE ? rotated : rotated.setValue(TYPE, rotated.getValue(TYPE).getOpposite());  // Forge: Fixed MC-134110 Structure mirroring breaking apart double chests
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING, TYPE, WATERLOGGED);
   }

   public boolean isPathfindable(BlockState pState, IBlockReader pLevel, BlockPos pPos, PathType pType) {
      return false;
   }
}
