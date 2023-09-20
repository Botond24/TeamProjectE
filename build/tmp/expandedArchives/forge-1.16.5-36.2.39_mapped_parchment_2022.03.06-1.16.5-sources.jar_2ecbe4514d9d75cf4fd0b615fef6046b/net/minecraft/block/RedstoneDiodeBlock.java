package net.minecraft.block;

import java.util.Random;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public abstract class RedstoneDiodeBlock extends HorizontalBlock {
   protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

   protected RedstoneDiodeBlock(AbstractBlock.Properties p_i48416_1_) {
      super(p_i48416_1_);
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return SHAPE;
   }

   public boolean canSurvive(BlockState pState, IWorldReader pLevel, BlockPos pPos) {
      return canSupportRigidBlock(pLevel, pPos.below());
   }

   public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
      if (!this.isLocked(pLevel, pPos, pState)) {
         boolean flag = pState.getValue(POWERED);
         boolean flag1 = this.shouldTurnOn(pLevel, pPos, pState);
         if (flag && !flag1) {
            pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(false)), 2);
         } else if (!flag) {
            pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(true)), 2);
            if (!flag1) {
               pLevel.getBlockTicks().scheduleTick(pPos, this, this.getDelay(pState), TickPriority.VERY_HIGH);
            }
         }

      }
   }

   /**
    * @deprecated call via {@link IBlockState#getStrongPower(IBlockAccess,BlockPos,EnumFacing)} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getDirectSignal(BlockState pBlockState, IBlockReader pBlockAccess, BlockPos pPos, Direction pSide) {
      return pBlockState.getSignal(pBlockAccess, pPos, pSide);
   }

   /**
    * @deprecated call via {@link IBlockState#getWeakPower(IBlockAccess,BlockPos,EnumFacing)} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getSignal(BlockState pBlockState, IBlockReader pBlockAccess, BlockPos pPos, Direction pSide) {
      if (!pBlockState.getValue(POWERED)) {
         return 0;
      } else {
         return pBlockState.getValue(FACING) == pSide ? this.getOutputSignal(pBlockAccess, pPos, pBlockState) : 0;
      }
   }

   public void neighborChanged(BlockState pState, World pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      if (pState.canSurvive(pLevel, pPos)) {
         this.checkTickOnNeighbor(pLevel, pPos, pState);
      } else {
         TileEntity tileentity = pState.hasTileEntity() ? pLevel.getBlockEntity(pPos) : null;
         dropResources(pState, pLevel, pPos, tileentity);
         pLevel.removeBlock(pPos, false);

         for(Direction direction : Direction.values()) {
            pLevel.updateNeighborsAt(pPos.relative(direction), this);
         }

      }
   }

   protected void checkTickOnNeighbor(World pLevel, BlockPos pPos, BlockState pState) {
      if (!this.isLocked(pLevel, pPos, pState)) {
         boolean flag = pState.getValue(POWERED);
         boolean flag1 = this.shouldTurnOn(pLevel, pPos, pState);
         if (flag != flag1 && !pLevel.getBlockTicks().willTickThisTick(pPos, this)) {
            TickPriority tickpriority = TickPriority.HIGH;
            if (this.shouldPrioritize(pLevel, pPos, pState)) {
               tickpriority = TickPriority.EXTREMELY_HIGH;
            } else if (flag) {
               tickpriority = TickPriority.VERY_HIGH;
            }

            pLevel.getBlockTicks().scheduleTick(pPos, this, this.getDelay(pState), tickpriority);
         }

      }
   }

   public boolean isLocked(IWorldReader pLevel, BlockPos pPos, BlockState pState) {
      return false;
   }

   protected boolean shouldTurnOn(World pLevel, BlockPos pPos, BlockState pState) {
      return this.getInputSignal(pLevel, pPos, pState) > 0;
   }

   protected int getInputSignal(World pLevel, BlockPos pPos, BlockState pState) {
      Direction direction = pState.getValue(FACING);
      BlockPos blockpos = pPos.relative(direction);
      int i = pLevel.getSignal(blockpos, direction);
      if (i >= 15) {
         return i;
      } else {
         BlockState blockstate = pLevel.getBlockState(blockpos);
         return Math.max(i, blockstate.is(Blocks.REDSTONE_WIRE) ? blockstate.getValue(RedstoneWireBlock.POWER) : 0);
      }
   }

   protected int getAlternateSignal(IWorldReader pLevel, BlockPos pPos, BlockState pState) {
      Direction direction = pState.getValue(FACING);
      Direction direction1 = direction.getClockWise();
      Direction direction2 = direction.getCounterClockWise();
      return Math.max(this.getAlternateSignalAt(pLevel, pPos.relative(direction1), direction1), this.getAlternateSignalAt(pLevel, pPos.relative(direction2), direction2));
   }

   protected int getAlternateSignalAt(IWorldReader pLevel, BlockPos pPos, Direction pSide) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      if (this.isAlternateInput(blockstate)) {
         if (blockstate.is(Blocks.REDSTONE_BLOCK)) {
            return 15;
         } else {
            return blockstate.is(Blocks.REDSTONE_WIRE) ? blockstate.getValue(RedstoneWireBlock.POWER) : pLevel.getDirectSignal(pPos, pSide);
         }
      } else {
         return 0;
      }
   }

   /**
    * Can this block provide power. Only wire currently seems to have this change based on its state.
    * @deprecated call via {@link IBlockState#canProvidePower()} whenever possible. Implementing/overriding is fine.
    */
   public boolean isSignalSource(BlockState pState) {
      return true;
   }

   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
   }

   /**
    * Called by ItemBlocks after a block is set in the world, to allow post-place logic
    */
   public void setPlacedBy(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
      if (this.shouldTurnOn(pLevel, pPos, pState)) {
         pLevel.getBlockTicks().scheduleTick(pPos, this, 1);
      }

   }

   public void onPlace(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      this.updateNeighborsInFront(pLevel, pPos, pState);
   }

   public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pIsMoving && !pState.is(pNewState.getBlock())) {
         super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
         this.updateNeighborsInFront(pLevel, pPos, pState);
      }
   }

   protected void updateNeighborsInFront(World pLevel, BlockPos pPos, BlockState pState) {
      Direction direction = pState.getValue(FACING);
      BlockPos blockpos = pPos.relative(direction.getOpposite());
      if (net.minecraftforge.event.ForgeEventFactory.onNeighborNotify(pLevel, pPos, pLevel.getBlockState(pPos), java.util.EnumSet.of(direction.getOpposite()), false).isCanceled())
         return;
      pLevel.neighborChanged(blockpos, this, pPos);
      pLevel.updateNeighborsAtExceptFromFacing(blockpos, this, direction);
   }

   protected boolean isAlternateInput(BlockState pState) {
      return pState.isSignalSource();
   }

   protected int getOutputSignal(IBlockReader pLevel, BlockPos pPos, BlockState pState) {
      return 15;
   }

   public static boolean isDiode(BlockState pState) {
      return pState.getBlock() instanceof RedstoneDiodeBlock;
   }

   public boolean shouldPrioritize(IBlockReader pLevel, BlockPos pPos, BlockState pState) {
      Direction direction = pState.getValue(FACING).getOpposite();
      BlockState blockstate = pLevel.getBlockState(pPos.relative(direction));
      return isDiode(blockstate) && blockstate.getValue(FACING) != direction;
   }

   protected abstract int getDelay(BlockState pState);
}
