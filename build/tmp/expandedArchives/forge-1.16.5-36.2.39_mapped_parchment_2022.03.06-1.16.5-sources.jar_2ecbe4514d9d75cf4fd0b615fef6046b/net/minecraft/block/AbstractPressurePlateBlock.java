package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public abstract class AbstractPressurePlateBlock extends Block {
   protected static final VoxelShape PRESSED_AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 0.5D, 15.0D);
   protected static final VoxelShape AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 1.0D, 15.0D);
   protected static final AxisAlignedBB TOUCH_AABB = new AxisAlignedBB(0.125D, 0.0D, 0.125D, 0.875D, 0.25D, 0.875D);

   protected AbstractPressurePlateBlock(AbstractBlock.Properties p_i48445_1_) {
      super(p_i48445_1_);
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return this.getSignalForState(pState) > 0 ? PRESSED_AABB : AABB;
   }

   protected int getPressedTime() {
      return 20;
   }

   /**
    * Return true if an entity can be spawned inside the block (used to get the player's bed spawn location)
    */
   public boolean isPossibleToRespawnInThis() {
      return true;
   }

   /**
    * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific face passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      return pFacing == Direction.DOWN && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public boolean canSurvive(BlockState pState, IWorldReader pLevel, BlockPos pPos) {
      BlockPos blockpos = pPos.below();
      return canSupportRigidBlock(pLevel, blockpos) || canSupportCenter(pLevel, blockpos, Direction.UP);
   }

   public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
      int i = this.getSignalForState(pState);
      if (i > 0) {
         this.checkPressed(pLevel, pPos, pState, i);
      }

   }

   public void entityInside(BlockState pState, World pLevel, BlockPos pPos, Entity pEntity) {
      if (!pLevel.isClientSide) {
         int i = this.getSignalForState(pState);
         if (i == 0) {
            this.checkPressed(pLevel, pPos, pState, i);
         }

      }
   }

   protected void checkPressed(World p_180666_1_, BlockPos p_180666_2_, BlockState p_180666_3_, int p_180666_4_) {
      int i = this.getSignalStrength(p_180666_1_, p_180666_2_);
      boolean flag = p_180666_4_ > 0;
      boolean flag1 = i > 0;
      if (p_180666_4_ != i) {
         BlockState blockstate = this.setSignalForState(p_180666_3_, i);
         p_180666_1_.setBlock(p_180666_2_, blockstate, 2);
         this.updateNeighbours(p_180666_1_, p_180666_2_);
         p_180666_1_.setBlocksDirty(p_180666_2_, p_180666_3_, blockstate);
      }

      if (!flag1 && flag) {
         this.playOffSound(p_180666_1_, p_180666_2_);
      } else if (flag1 && !flag) {
         this.playOnSound(p_180666_1_, p_180666_2_);
      }

      if (flag1) {
         p_180666_1_.getBlockTicks().scheduleTick(new BlockPos(p_180666_2_), this, this.getPressedTime());
      }

   }

   protected abstract void playOnSound(IWorld pLevel, BlockPos pPos);

   protected abstract void playOffSound(IWorld pLevel, BlockPos pPos);

   public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pIsMoving && !pState.is(pNewState.getBlock())) {
         if (this.getSignalForState(pState) > 0) {
            this.updateNeighbours(pLevel, pPos);
         }

         super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
      }
   }

   /**
    * Notify block and block below of changes
    */
   protected void updateNeighbours(World pLevel, BlockPos pPos) {
      pLevel.updateNeighborsAt(pPos, this);
      pLevel.updateNeighborsAt(pPos.below(), this);
   }

   /**
    * @deprecated call via {@link IBlockState#getWeakPower(IBlockAccess,BlockPos,EnumFacing)} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getSignal(BlockState pBlockState, IBlockReader pBlockAccess, BlockPos pPos, Direction pSide) {
      return this.getSignalForState(pBlockState);
   }

   /**
    * @deprecated call via {@link IBlockState#getStrongPower(IBlockAccess,BlockPos,EnumFacing)} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getDirectSignal(BlockState pBlockState, IBlockReader pBlockAccess, BlockPos pPos, Direction pSide) {
      return pSide == Direction.UP ? this.getSignalForState(pBlockState) : 0;
   }

   /**
    * Can this block provide power. Only wire currently seems to have this change based on its state.
    * @deprecated call via {@link IBlockState#canProvidePower()} whenever possible. Implementing/overriding is fine.
    */
   public boolean isSignalSource(BlockState pState) {
      return true;
   }

   /**
    * @deprecated call via {@link IBlockState#getMobilityFlag()} whenever possible. Implementing/overriding is fine.
    */
   public PushReaction getPistonPushReaction(BlockState pState) {
      return PushReaction.DESTROY;
   }

   protected abstract int getSignalStrength(World pLevel, BlockPos pPos);

   protected abstract int getSignalForState(BlockState pState);

   protected abstract BlockState setSignalForState(BlockState pState, int pStrength);
}