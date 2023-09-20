package net.minecraft.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.ComparatorMode;
import net.minecraft.tileentity.ComparatorTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class ComparatorBlock extends RedstoneDiodeBlock implements ITileEntityProvider {
   public static final EnumProperty<ComparatorMode> MODE = BlockStateProperties.MODE_COMPARATOR;

   public ComparatorBlock(AbstractBlock.Properties p_i48424_1_) {
      super(p_i48424_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.valueOf(false)).setValue(MODE, ComparatorMode.COMPARE));
   }

   protected int getDelay(BlockState pState) {
      return 2;
   }

   protected int getOutputSignal(IBlockReader pLevel, BlockPos pPos, BlockState pState) {
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      return tileentity instanceof ComparatorTileEntity ? ((ComparatorTileEntity)tileentity).getOutputSignal() : 0;
   }

   private int calculateOutputSignal(World pLevel, BlockPos pPos, BlockState pState) {
      return pState.getValue(MODE) == ComparatorMode.SUBTRACT ? Math.max(this.getInputSignal(pLevel, pPos, pState) - this.getAlternateSignal(pLevel, pPos, pState), 0) : this.getInputSignal(pLevel, pPos, pState);
   }

   protected boolean shouldTurnOn(World pLevel, BlockPos pPos, BlockState pState) {
      int i = this.getInputSignal(pLevel, pPos, pState);
      if (i == 0) {
         return false;
      } else {
         int j = this.getAlternateSignal(pLevel, pPos, pState);
         if (i > j) {
            return true;
         } else {
            return i == j && pState.getValue(MODE) == ComparatorMode.COMPARE;
         }
      }
   }

   protected int getInputSignal(World pLevel, BlockPos pPos, BlockState pState) {
      int i = super.getInputSignal(pLevel, pPos, pState);
      Direction direction = pState.getValue(FACING);
      BlockPos blockpos = pPos.relative(direction);
      BlockState blockstate = pLevel.getBlockState(blockpos);
      if (blockstate.hasAnalogOutputSignal()) {
         i = blockstate.getAnalogOutputSignal(pLevel, blockpos);
      } else if (i < 15 && blockstate.isRedstoneConductor(pLevel, blockpos)) {
         blockpos = blockpos.relative(direction);
         blockstate = pLevel.getBlockState(blockpos);
         ItemFrameEntity itemframeentity = this.getItemFrame(pLevel, direction, blockpos);
         int j = Math.max(itemframeentity == null ? Integer.MIN_VALUE : itemframeentity.getAnalogOutput(), blockstate.hasAnalogOutputSignal() ? blockstate.getAnalogOutputSignal(pLevel, blockpos) : Integer.MIN_VALUE);
         if (j != Integer.MIN_VALUE) {
            i = j;
         }
      }

      return i;
   }

   @Nullable
   private ItemFrameEntity getItemFrame(World pLevel, Direction pFacing, BlockPos pPos) {
      List<ItemFrameEntity> list = pLevel.getEntitiesOfClass(ItemFrameEntity.class, new AxisAlignedBB((double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ(), (double)(pPos.getX() + 1), (double)(pPos.getY() + 1), (double)(pPos.getZ() + 1)), (p_210304_1_) -> {
         return p_210304_1_ != null && p_210304_1_.getDirection() == pFacing;
      });
      return list.size() == 1 ? list.get(0) : null;
   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      if (!pPlayer.abilities.mayBuild) {
         return ActionResultType.PASS;
      } else {
         pState = pState.cycle(MODE);
         float f = pState.getValue(MODE) == ComparatorMode.SUBTRACT ? 0.55F : 0.5F;
         pLevel.playSound(pPlayer, pPos, SoundEvents.COMPARATOR_CLICK, SoundCategory.BLOCKS, 0.3F, f);
         pLevel.setBlock(pPos, pState, 2);
         this.refreshOutputState(pLevel, pPos, pState);
         return ActionResultType.sidedSuccess(pLevel.isClientSide);
      }
   }

   protected void checkTickOnNeighbor(World pLevel, BlockPos pPos, BlockState pState) {
      if (!pLevel.getBlockTicks().willTickThisTick(pPos, this)) {
         int i = this.calculateOutputSignal(pLevel, pPos, pState);
         TileEntity tileentity = pLevel.getBlockEntity(pPos);
         int j = tileentity instanceof ComparatorTileEntity ? ((ComparatorTileEntity)tileentity).getOutputSignal() : 0;
         if (i != j || pState.getValue(POWERED) != this.shouldTurnOn(pLevel, pPos, pState)) {
            TickPriority tickpriority = this.shouldPrioritize(pLevel, pPos, pState) ? TickPriority.HIGH : TickPriority.NORMAL;
            pLevel.getBlockTicks().scheduleTick(pPos, this, 2, tickpriority);
         }

      }
   }

   private void refreshOutputState(World pLevel, BlockPos pPos, BlockState pState) {
      int i = this.calculateOutputSignal(pLevel, pPos, pState);
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      int j = 0;
      if (tileentity instanceof ComparatorTileEntity) {
         ComparatorTileEntity comparatortileentity = (ComparatorTileEntity)tileentity;
         j = comparatortileentity.getOutputSignal();
         comparatortileentity.setOutputSignal(i);
      }

      if (j != i || pState.getValue(MODE) == ComparatorMode.COMPARE) {
         boolean flag1 = this.shouldTurnOn(pLevel, pPos, pState);
         boolean flag = pState.getValue(POWERED);
         if (flag && !flag1) {
            pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(false)), 2);
         } else if (!flag && flag1) {
            pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(true)), 2);
         }

         this.updateNeighborsInFront(pLevel, pPos, pState);
      }

   }

   public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
      this.refreshOutputState(pLevel, pPos, pState);
   }

   /**
    * Called on server when World#addBlockEvent is called. If server returns true, then also called on the client. On
    * the Server, this may perform additional changes to the world, like pistons replacing the block with an extended
    * base. On the client, the update may involve replacing tile entities or effects such as sounds or particles
    * @deprecated call via {@link IBlockState#onBlockEventReceived(World,BlockPos,int,int)} whenever possible.
    * Implementing/overriding is fine.
    */
   public boolean triggerEvent(BlockState pState, World pLevel, BlockPos pPos, int pId, int pParam) {
      super.triggerEvent(pState, pLevel, pPos, pId, pParam);
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      return tileentity != null && tileentity.triggerEvent(pId, pParam);
   }

   public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
      return new ComparatorTileEntity();
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING, MODE, POWERED);
   }

   @Override
   public boolean getWeakChanges(BlockState state, net.minecraft.world.IWorldReader world, BlockPos pos) {
      return state.is(Blocks.COMPARATOR);
   }

   @Override
   public void onNeighborChange(BlockState state, net.minecraft.world.IWorldReader world, BlockPos pos, BlockPos neighbor) {
      if (pos.getY() == neighbor.getY() && world instanceof World && !((World)world).isClientSide()) {
         state.neighborChanged((World)world, pos, world.getBlockState(neighbor).getBlock(), neighbor, false);
      }
   }
}
