package net.minecraft.block;

import net.minecraft.block.material.PushReaction;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.Property;
import net.minecraft.state.properties.RailShape;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public abstract class AbstractRailBlock extends Block implements net.minecraftforge.common.extensions.IAbstractRailBlock {
   protected static final VoxelShape FLAT_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
   protected static final VoxelShape HALF_BLOCK_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
   private final boolean isStraight;

   public static boolean isRail(World pLevel, BlockPos pPos) {
      return isRail(pLevel.getBlockState(pPos));
   }

   public static boolean isRail(BlockState pState) {
      return pState.is(BlockTags.RAILS) && pState.getBlock() instanceof AbstractRailBlock;
   }

   protected AbstractRailBlock(boolean pIsStraight, AbstractBlock.Properties pProperties) {
      super(pProperties);
      this.isStraight = pIsStraight;
   }

   public boolean isStraight() {
      return this.isStraight;
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      RailShape railshape = pState.is(this) ? pState.getValue(this.getShapeProperty()) : null;
      RailShape railShape2 = pState.is(this) ? getRailDirection(pState, pLevel, pPos, null) : null;
      return railshape != null && railshape.isAscending() ? HALF_BLOCK_AABB : FLAT_AABB;
   }

   public boolean canSurvive(BlockState pState, IWorldReader pLevel, BlockPos pPos) {
      return canSupportRigidBlock(pLevel, pPos.below());
   }

   public void onPlace(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (!pOldState.is(pState.getBlock())) {
         this.updateState(pState, pLevel, pPos, pIsMoving);
      }
   }

   protected BlockState updateState(BlockState pState, World pLevel, BlockPos pPos, boolean pIsMoving) {
      pState = this.updateDir(pLevel, pPos, pState, true);
      if (this.isStraight) {
         pState.neighborChanged(pLevel, pPos, this, pPos, pIsMoving);
      }

      return pState;
   }

   public void neighborChanged(BlockState pState, World pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      if (!pLevel.isClientSide && pLevel.getBlockState(pPos).is(this)) {
         RailShape railshape = getRailDirection(pState, pLevel, pPos, null);
         if (shouldBeRemoved(pPos, pLevel, railshape)) {
            dropResources(pState, pLevel, pPos);
            pLevel.removeBlock(pPos, pIsMoving);
         } else {
            this.updateState(pState, pLevel, pPos, pBlock);
         }

      }
   }

   private static boolean shouldBeRemoved(BlockPos pPos, World pLevel, RailShape pRailShape) {
      if (!canSupportRigidBlock(pLevel, pPos.below())) {
         return true;
      } else {
         switch(pRailShape) {
         case ASCENDING_EAST:
            return !canSupportRigidBlock(pLevel, pPos.east());
         case ASCENDING_WEST:
            return !canSupportRigidBlock(pLevel, pPos.west());
         case ASCENDING_NORTH:
            return !canSupportRigidBlock(pLevel, pPos.north());
         case ASCENDING_SOUTH:
            return !canSupportRigidBlock(pLevel, pPos.south());
         default:
            return false;
         }
      }
   }

   protected void updateState(BlockState pState, World pLevel, BlockPos pPos, Block pBlock) {
   }

   protected BlockState updateDir(World pLevel, BlockPos pPos, BlockState pState, boolean pPlacing) {
      if (pLevel.isClientSide) {
         return pState;
      } else {
         RailShape railshape = pState.getValue(this.getShapeProperty());
         return (new RailState(pLevel, pPos, pState)).place(pLevel.hasNeighborSignal(pPos), pPlacing, railshape).getState();
      }
   }

   /**
    * @deprecated call via {@link IBlockState#getMobilityFlag()} whenever possible. Implementing/overriding is fine.
    */
   public PushReaction getPistonPushReaction(BlockState pState) {
      return PushReaction.NORMAL;
   }

   public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pIsMoving) {
         super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
         if (getRailDirection(pState, pLevel, pPos, null).isAscending()) {
            pLevel.updateNeighborsAt(pPos.above(), this);
         }

         if (this.isStraight) {
            pLevel.updateNeighborsAt(pPos, this);
            pLevel.updateNeighborsAt(pPos.below(), this);
         }

      }
   }

   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      BlockState blockstate = super.defaultBlockState();
      Direction direction = pContext.getHorizontalDirection();
      boolean flag = direction == Direction.EAST || direction == Direction.WEST;
      return blockstate.setValue(this.getShapeProperty(), flag ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH);
   }

   @Deprecated //Forge: Use getRailDirection(IBlockAccess, BlockPos, IBlockState, EntityMinecart) for enhanced ability
   public abstract Property<RailShape> getShapeProperty();

   /* ======================================== FORGE START =====================================*/

   @Override
   public boolean isFlexibleRail(BlockState state, IBlockReader world, BlockPos pos)
   {
      return  !this.isStraight;
   }

   @Override
   public RailShape getRailDirection(BlockState state, IBlockReader world, BlockPos pos, @javax.annotation.Nullable net.minecraft.entity.item.minecart.AbstractMinecartEntity cart) {
      return state.getValue(getShapeProperty());
   }
   /* ========================================= FORGE END ======================================*/
}
