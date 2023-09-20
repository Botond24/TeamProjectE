package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.LecternTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class LecternBlock extends ContainerBlock {
   public static final DirectionProperty FACING = HorizontalBlock.FACING;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final BooleanProperty HAS_BOOK = BlockStateProperties.HAS_BOOK;
   public static final VoxelShape SHAPE_BASE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
   public static final VoxelShape SHAPE_POST = Block.box(4.0D, 2.0D, 4.0D, 12.0D, 14.0D, 12.0D);
   public static final VoxelShape SHAPE_COMMON = VoxelShapes.or(SHAPE_BASE, SHAPE_POST);
   public static final VoxelShape SHAPE_TOP_PLATE = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 15.0D, 16.0D);
   public static final VoxelShape SHAPE_COLLISION = VoxelShapes.or(SHAPE_COMMON, SHAPE_TOP_PLATE);
   public static final VoxelShape SHAPE_WEST = VoxelShapes.or(Block.box(1.0D, 10.0D, 0.0D, 5.333333D, 14.0D, 16.0D), Block.box(5.333333D, 12.0D, 0.0D, 9.666667D, 16.0D, 16.0D), Block.box(9.666667D, 14.0D, 0.0D, 14.0D, 18.0D, 16.0D), SHAPE_COMMON);
   public static final VoxelShape SHAPE_NORTH = VoxelShapes.or(Block.box(0.0D, 10.0D, 1.0D, 16.0D, 14.0D, 5.333333D), Block.box(0.0D, 12.0D, 5.333333D, 16.0D, 16.0D, 9.666667D), Block.box(0.0D, 14.0D, 9.666667D, 16.0D, 18.0D, 14.0D), SHAPE_COMMON);
   public static final VoxelShape SHAPE_EAST = VoxelShapes.or(Block.box(15.0D, 10.0D, 0.0D, 10.666667D, 14.0D, 16.0D), Block.box(10.666667D, 12.0D, 0.0D, 6.333333D, 16.0D, 16.0D), Block.box(6.333333D, 14.0D, 0.0D, 2.0D, 18.0D, 16.0D), SHAPE_COMMON);
   public static final VoxelShape SHAPE_SOUTH = VoxelShapes.or(Block.box(0.0D, 10.0D, 15.0D, 16.0D, 14.0D, 10.666667D), Block.box(0.0D, 12.0D, 10.666667D, 16.0D, 16.0D, 6.333333D), Block.box(0.0D, 14.0D, 6.333333D, 16.0D, 18.0D, 2.0D), SHAPE_COMMON);

   public LecternBlock(AbstractBlock.Properties p_i49979_1_) {
      super(p_i49979_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.valueOf(false)).setValue(HAS_BOOK, Boolean.valueOf(false)));
   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link IBlockState#getRenderType()} whenever possible. Implementing/overriding is fine.
    */
   public BlockRenderType getRenderShape(BlockState pState) {
      return BlockRenderType.MODEL;
   }

   public VoxelShape getOcclusionShape(BlockState pState, IBlockReader pLevel, BlockPos pPos) {
      return SHAPE_COMMON;
   }

   public boolean useShapeForLightOcclusion(BlockState pState) {
      return true;
   }

   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      World world = pContext.getLevel();
      ItemStack itemstack = pContext.getItemInHand();
      CompoundNBT compoundnbt = itemstack.getTag();
      PlayerEntity playerentity = pContext.getPlayer();
      boolean flag = false;
      if (!world.isClientSide && playerentity != null && compoundnbt != null && playerentity.canUseGameMasterBlocks() && compoundnbt.contains("BlockEntityTag")) {
         CompoundNBT compoundnbt1 = compoundnbt.getCompound("BlockEntityTag");
         if (compoundnbt1.contains("Book")) {
            flag = true;
         }
      }

      return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite()).setValue(HAS_BOOK, Boolean.valueOf(flag));
   }

   public VoxelShape getCollisionShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return SHAPE_COLLISION;
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      switch((Direction)pState.getValue(FACING)) {
      case NORTH:
         return SHAPE_NORTH;
      case SOUTH:
         return SHAPE_SOUTH;
      case EAST:
         return SHAPE_EAST;
      case WEST:
         return SHAPE_WEST;
      default:
         return SHAPE_COMMON;
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
      pBuilder.add(FACING, POWERED, HAS_BOOK);
   }

   @Nullable
   public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
      return new LecternTileEntity();
   }

   public static boolean tryPlaceBook(World p_220151_0_, BlockPos p_220151_1_, BlockState p_220151_2_, ItemStack p_220151_3_) {
      if (!p_220151_2_.getValue(HAS_BOOK)) {
         if (!p_220151_0_.isClientSide) {
            placeBook(p_220151_0_, p_220151_1_, p_220151_2_, p_220151_3_);
         }

         return true;
      } else {
         return false;
      }
   }

   private static void placeBook(World p_220148_0_, BlockPos p_220148_1_, BlockState p_220148_2_, ItemStack p_220148_3_) {
      TileEntity tileentity = p_220148_0_.getBlockEntity(p_220148_1_);
      if (tileentity instanceof LecternTileEntity) {
         LecternTileEntity lecterntileentity = (LecternTileEntity)tileentity;
         lecterntileentity.setBook(p_220148_3_.split(1));
         resetBookState(p_220148_0_, p_220148_1_, p_220148_2_, true);
         p_220148_0_.playSound((PlayerEntity)null, p_220148_1_, SoundEvents.BOOK_PUT, SoundCategory.BLOCKS, 1.0F, 1.0F);
      }

   }

   public static void resetBookState(World pLevel, BlockPos pPos, BlockState pState, boolean pHasBook) {
      pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(false)).setValue(HAS_BOOK, Boolean.valueOf(pHasBook)), 3);
      updateBelow(pLevel, pPos, pState);
   }

   public static void signalPageChange(World pLevel, BlockPos pPos, BlockState pState) {
      changePowered(pLevel, pPos, pState, true);
      pLevel.getBlockTicks().scheduleTick(pPos, pState.getBlock(), 2);
      pLevel.levelEvent(1043, pPos, 0);
   }

   private static void changePowered(World pLevel, BlockPos pPos, BlockState pState, boolean pPowered) {
      pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(pPowered)), 3);
      updateBelow(pLevel, pPos, pState);
   }

   private static void updateBelow(World pLevel, BlockPos pPos, BlockState pState) {
      pLevel.updateNeighborsAt(pPos.below(), pState.getBlock());
   }

   public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
      changePowered(pLevel, pPos, pState, false);
   }

   public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pState.is(pNewState.getBlock())) {
         if (pState.getValue(HAS_BOOK)) {
            this.popBook(pState, pLevel, pPos);
         }

         if (pState.getValue(POWERED)) {
            pLevel.updateNeighborsAt(pPos.below(), this);
         }

         super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
      }
   }

   private void popBook(BlockState pState, World pLevel, BlockPos pPos) {
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      if (tileentity instanceof LecternTileEntity) {
         LecternTileEntity lecterntileentity = (LecternTileEntity)tileentity;
         Direction direction = pState.getValue(FACING);
         ItemStack itemstack = lecterntileentity.getBook().copy();
         float f = 0.25F * (float)direction.getStepX();
         float f1 = 0.25F * (float)direction.getStepZ();
         ItemEntity itementity = new ItemEntity(pLevel, (double)pPos.getX() + 0.5D + (double)f, (double)(pPos.getY() + 1), (double)pPos.getZ() + 0.5D + (double)f1, itemstack);
         itementity.setDefaultPickUpDelay();
         pLevel.addFreshEntity(itementity);
         lecterntileentity.clearContent();
      }

   }

   /**
    * Can this block provide power. Only wire currently seems to have this change based on its state.
    * @deprecated call via {@link IBlockState#canProvidePower()} whenever possible. Implementing/overriding is fine.
    */
   public boolean isSignalSource(BlockState pState) {
      return true;
   }

   /**
    * @deprecated call via {@link IBlockState#getWeakPower(IBlockAccess,BlockPos,EnumFacing)} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getSignal(BlockState pBlockState, IBlockReader pBlockAccess, BlockPos pPos, Direction pSide) {
      return pBlockState.getValue(POWERED) ? 15 : 0;
   }

   /**
    * @deprecated call via {@link IBlockState#getStrongPower(IBlockAccess,BlockPos,EnumFacing)} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getDirectSignal(BlockState pBlockState, IBlockReader pBlockAccess, BlockPos pPos, Direction pSide) {
      return pSide == Direction.UP && pBlockState.getValue(POWERED) ? 15 : 0;
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
      if (pBlockState.getValue(HAS_BOOK)) {
         TileEntity tileentity = pLevel.getBlockEntity(pPos);
         if (tileentity instanceof LecternTileEntity) {
            return ((LecternTileEntity)tileentity).getRedstoneSignal();
         }
      }

      return 0;
   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      if (pState.getValue(HAS_BOOK)) {
         if (!pLevel.isClientSide) {
            this.openScreen(pLevel, pPos, pPlayer);
         }

         return ActionResultType.sidedSuccess(pLevel.isClientSide);
      } else {
         ItemStack itemstack = pPlayer.getItemInHand(pHand);
         return !itemstack.isEmpty() && !itemstack.getItem().is(ItemTags.LECTERN_BOOKS) ? ActionResultType.CONSUME : ActionResultType.PASS;
      }
   }

   @Nullable
   public INamedContainerProvider getMenuProvider(BlockState pState, World pLevel, BlockPos pPos) {
      return !pState.getValue(HAS_BOOK) ? null : super.getMenuProvider(pState, pLevel, pPos);
   }

   private void openScreen(World pLevel, BlockPos pPos, PlayerEntity pPlayer) {
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      if (tileentity instanceof LecternTileEntity) {
         pPlayer.openMenu((LecternTileEntity)tileentity);
         pPlayer.awardStat(Stats.INTERACT_WITH_LECTERN);
      }

   }

   public boolean isPathfindable(BlockState pState, IBlockReader pLevel, BlockPos pPos, PathType pType) {
      return false;
   }
}