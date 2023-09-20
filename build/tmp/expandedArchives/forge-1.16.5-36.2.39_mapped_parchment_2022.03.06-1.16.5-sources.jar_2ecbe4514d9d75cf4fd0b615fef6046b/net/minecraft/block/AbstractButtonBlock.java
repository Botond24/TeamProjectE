package net.minecraft.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public abstract class AbstractButtonBlock extends HorizontalFaceBlock {
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   protected static final VoxelShape CEILING_AABB_X = Block.box(6.0D, 14.0D, 5.0D, 10.0D, 16.0D, 11.0D);
   protected static final VoxelShape CEILING_AABB_Z = Block.box(5.0D, 14.0D, 6.0D, 11.0D, 16.0D, 10.0D);
   protected static final VoxelShape FLOOR_AABB_X = Block.box(6.0D, 0.0D, 5.0D, 10.0D, 2.0D, 11.0D);
   protected static final VoxelShape FLOOR_AABB_Z = Block.box(5.0D, 0.0D, 6.0D, 11.0D, 2.0D, 10.0D);
   protected static final VoxelShape NORTH_AABB = Block.box(5.0D, 6.0D, 14.0D, 11.0D, 10.0D, 16.0D);
   protected static final VoxelShape SOUTH_AABB = Block.box(5.0D, 6.0D, 0.0D, 11.0D, 10.0D, 2.0D);
   protected static final VoxelShape WEST_AABB = Block.box(14.0D, 6.0D, 5.0D, 16.0D, 10.0D, 11.0D);
   protected static final VoxelShape EAST_AABB = Block.box(0.0D, 6.0D, 5.0D, 2.0D, 10.0D, 11.0D);
   protected static final VoxelShape PRESSED_CEILING_AABB_X = Block.box(6.0D, 15.0D, 5.0D, 10.0D, 16.0D, 11.0D);
   protected static final VoxelShape PRESSED_CEILING_AABB_Z = Block.box(5.0D, 15.0D, 6.0D, 11.0D, 16.0D, 10.0D);
   protected static final VoxelShape PRESSED_FLOOR_AABB_X = Block.box(6.0D, 0.0D, 5.0D, 10.0D, 1.0D, 11.0D);
   protected static final VoxelShape PRESSED_FLOOR_AABB_Z = Block.box(5.0D, 0.0D, 6.0D, 11.0D, 1.0D, 10.0D);
   protected static final VoxelShape PRESSED_NORTH_AABB = Block.box(5.0D, 6.0D, 15.0D, 11.0D, 10.0D, 16.0D);
   protected static final VoxelShape PRESSED_SOUTH_AABB = Block.box(5.0D, 6.0D, 0.0D, 11.0D, 10.0D, 1.0D);
   protected static final VoxelShape PRESSED_WEST_AABB = Block.box(15.0D, 6.0D, 5.0D, 16.0D, 10.0D, 11.0D);
   protected static final VoxelShape PRESSED_EAST_AABB = Block.box(0.0D, 6.0D, 5.0D, 1.0D, 10.0D, 11.0D);
   private final boolean sensitive;

   protected AbstractButtonBlock(boolean pSensitive, AbstractBlock.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.valueOf(false)).setValue(FACE, AttachFace.WALL));
      this.sensitive = pSensitive;
   }

   private int getPressDuration() {
      return this.sensitive ? 30 : 20;
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      Direction direction = pState.getValue(FACING);
      boolean flag = pState.getValue(POWERED);
      switch((AttachFace)pState.getValue(FACE)) {
      case FLOOR:
         if (direction.getAxis() == Direction.Axis.X) {
            return flag ? PRESSED_FLOOR_AABB_X : FLOOR_AABB_X;
         }

         return flag ? PRESSED_FLOOR_AABB_Z : FLOOR_AABB_Z;
      case WALL:
         switch(direction) {
         case EAST:
            return flag ? PRESSED_EAST_AABB : EAST_AABB;
         case WEST:
            return flag ? PRESSED_WEST_AABB : WEST_AABB;
         case SOUTH:
            return flag ? PRESSED_SOUTH_AABB : SOUTH_AABB;
         case NORTH:
         default:
            return flag ? PRESSED_NORTH_AABB : NORTH_AABB;
         }
      case CEILING:
      default:
         if (direction.getAxis() == Direction.Axis.X) {
            return flag ? PRESSED_CEILING_AABB_X : CEILING_AABB_X;
         } else {
            return flag ? PRESSED_CEILING_AABB_Z : CEILING_AABB_Z;
         }
      }
   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      if (pState.getValue(POWERED)) {
         return ActionResultType.CONSUME;
      } else {
         this.press(pState, pLevel, pPos);
         this.playSound(pPlayer, pLevel, pPos, true);
         return ActionResultType.sidedSuccess(pLevel.isClientSide);
      }
   }

   public void press(BlockState pState, World pLevel, BlockPos pPos) {
      pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(true)), 3);
      this.updateNeighbours(pState, pLevel, pPos);
      pLevel.getBlockTicks().scheduleTick(pPos, this, this.getPressDuration());
   }

   protected void playSound(@Nullable PlayerEntity pPlayer, IWorld pLevel, BlockPos pPos, boolean pHitByArrow) {
      pLevel.playSound(pHitByArrow ? pPlayer : null, pPos, this.getSound(pHitByArrow), SoundCategory.BLOCKS, 0.3F, pHitByArrow ? 0.6F : 0.5F);
   }

   protected abstract SoundEvent getSound(boolean pIsOn);

   public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pIsMoving && !pState.is(pNewState.getBlock())) {
         if (pState.getValue(POWERED)) {
            this.updateNeighbours(pState, pLevel, pPos);
         }

         super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
      }
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
      return pBlockState.getValue(POWERED) && getConnectedDirection(pBlockState) == pSide ? 15 : 0;
   }

   /**
    * Can this block provide power. Only wire currently seems to have this change based on its state.
    * @deprecated call via {@link IBlockState#canProvidePower()} whenever possible. Implementing/overriding is fine.
    */
   public boolean isSignalSource(BlockState pState) {
      return true;
   }

   public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
      if (pState.getValue(POWERED)) {
         if (this.sensitive) {
            this.checkPressed(pState, pLevel, pPos);
         } else {
            pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(false)), 3);
            this.updateNeighbours(pState, pLevel, pPos);
            this.playSound((PlayerEntity)null, pLevel, pPos, false);
         }

      }
   }

   public void entityInside(BlockState pState, World pLevel, BlockPos pPos, Entity pEntity) {
      if (!pLevel.isClientSide && this.sensitive && !pState.getValue(POWERED)) {
         this.checkPressed(pState, pLevel, pPos);
      }
   }

   private void checkPressed(BlockState pState, World pLevel, BlockPos pPos) {
      List<? extends Entity> list = pLevel.getEntitiesOfClass(AbstractArrowEntity.class, pState.getShape(pLevel, pPos).bounds().move(pPos));
      boolean flag = !list.isEmpty();
      boolean flag1 = pState.getValue(POWERED);
      if (flag != flag1) {
         pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(flag)), 3);
         this.updateNeighbours(pState, pLevel, pPos);
         this.playSound((PlayerEntity)null, pLevel, pPos, flag);
      }

      if (flag) {
         pLevel.getBlockTicks().scheduleTick(new BlockPos(pPos), this, this.getPressDuration());
      }

   }

   private void updateNeighbours(BlockState pState, World pLevel, BlockPos pPos) {
      pLevel.updateNeighborsAt(pPos, this);
      pLevel.updateNeighborsAt(pPos.relative(getConnectedDirection(pState).getOpposite()), this);
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING, POWERED, FACE);
   }
}