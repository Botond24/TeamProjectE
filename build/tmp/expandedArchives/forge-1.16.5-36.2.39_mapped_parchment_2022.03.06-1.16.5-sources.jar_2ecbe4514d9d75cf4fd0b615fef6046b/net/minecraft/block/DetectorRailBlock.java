package net.minecraft.block;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.item.minecart.CommandBlockMinecartEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class DetectorRailBlock extends AbstractRailBlock {
   public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

   public DetectorRailBlock(AbstractBlock.Properties p_i48417_1_) {
      super(true, p_i48417_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, Boolean.valueOf(false)).setValue(SHAPE, RailShape.NORTH_SOUTH));
   }

   /**
    * Can this block provide power. Only wire currently seems to have this change based on its state.
    * @deprecated call via {@link IBlockState#canProvidePower()} whenever possible. Implementing/overriding is fine.
    */
   public boolean isSignalSource(BlockState pState) {
      return true;
   }

   public void entityInside(BlockState pState, World pLevel, BlockPos pPos, Entity pEntity) {
      if (!pLevel.isClientSide) {
         if (!pState.getValue(POWERED)) {
            this.checkPressed(pLevel, pPos, pState);
         }
      }
   }

   public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
      if (pState.getValue(POWERED)) {
         this.checkPressed(pLevel, pPos, pState);
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
      if (!pBlockState.getValue(POWERED)) {
         return 0;
      } else {
         return pSide == Direction.UP ? 15 : 0;
      }
   }

   private void checkPressed(World pLevel, BlockPos pPos, BlockState pState) {
      if (this.canSurvive(pState, pLevel, pPos)) {
         boolean flag = pState.getValue(POWERED);
         boolean flag1 = false;
         List<AbstractMinecartEntity> list = this.getInteractingMinecartOfType(pLevel, pPos, AbstractMinecartEntity.class, (Predicate<Entity>)null);
         if (!list.isEmpty()) {
            flag1 = true;
         }

         if (flag1 && !flag) {
            BlockState blockstate = pState.setValue(POWERED, Boolean.valueOf(true));
            pLevel.setBlock(pPos, blockstate, 3);
            this.updatePowerToConnected(pLevel, pPos, blockstate, true);
            pLevel.updateNeighborsAt(pPos, this);
            pLevel.updateNeighborsAt(pPos.below(), this);
            pLevel.setBlocksDirty(pPos, pState, blockstate);
         }

         if (!flag1 && flag) {
            BlockState blockstate1 = pState.setValue(POWERED, Boolean.valueOf(false));
            pLevel.setBlock(pPos, blockstate1, 3);
            this.updatePowerToConnected(pLevel, pPos, blockstate1, false);
            pLevel.updateNeighborsAt(pPos, this);
            pLevel.updateNeighborsAt(pPos.below(), this);
            pLevel.setBlocksDirty(pPos, pState, blockstate1);
         }

         if (flag1) {
            pLevel.getBlockTicks().scheduleTick(pPos, this, 20);
         }

         pLevel.updateNeighbourForOutputSignal(pPos, this);
      }
   }

   protected void updatePowerToConnected(World pLevel, BlockPos pPos, BlockState pState, boolean pPowered) {
      RailState railstate = new RailState(pLevel, pPos, pState);

      for(BlockPos blockpos : railstate.getConnections()) {
         BlockState blockstate = pLevel.getBlockState(blockpos);
         blockstate.neighborChanged(pLevel, blockpos, blockstate.getBlock(), pPos, false);
      }

   }

   public void onPlace(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (!pOldState.is(pState.getBlock())) {
         this.checkPressed(pLevel, pPos, this.updateState(pState, pLevel, pPos, pIsMoving));
      }
   }

   public Property<RailShape> getShapeProperty() {
      return SHAPE;
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
      if (pBlockState.getValue(POWERED)) {
         List<CommandBlockMinecartEntity> list = this.getInteractingMinecartOfType(pLevel, pPos, CommandBlockMinecartEntity.class, (Predicate<Entity>)null);
         if (!list.isEmpty()) {
            return list.get(0).getCommandBlock().getSuccessCount();
         }

         List<AbstractMinecartEntity> list1 = this.getInteractingMinecartOfType(pLevel, pPos, AbstractMinecartEntity.class, EntityPredicates.CONTAINER_ENTITY_SELECTOR);
         List<AbstractMinecartEntity> carts = this.getInteractingMinecartOfType(pLevel, pPos, AbstractMinecartEntity.class, null);
         if (!carts.isEmpty() && carts.get(0).getComparatorLevel() > -1) return carts.get(0).getComparatorLevel();
         if (!list1.isEmpty()) {
            return Container.getRedstoneSignalFromContainer((IInventory)list1.get(0));
         }
      }

      return 0;
   }

   protected <T extends AbstractMinecartEntity> List<T> getInteractingMinecartOfType(World pLevel, BlockPos pPos, Class<T> pCartType, @Nullable Predicate<Entity> pFilter) {
      return pLevel.getEntitiesOfClass(pCartType, this.getSearchBB(pPos), pFilter);
   }

   private AxisAlignedBB getSearchBB(BlockPos pPos) {
      double d0 = 0.2D;
      return new AxisAlignedBB((double)pPos.getX() + 0.2D, (double)pPos.getY(), (double)pPos.getZ() + 0.2D, (double)(pPos.getX() + 1) - 0.2D, (double)(pPos.getY() + 1) - 0.2D, (double)(pPos.getZ() + 1) - 0.2D);
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link IBlockState#withRotation(Rotation)} whenever possible. Implementing/overriding is
    * fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRotation) {
      switch(pRotation) {
      case CLOCKWISE_180:
         switch((RailShape)pState.getValue(SHAPE)) {
         case ASCENDING_EAST:
            return pState.setValue(SHAPE, RailShape.ASCENDING_WEST);
         case ASCENDING_WEST:
            return pState.setValue(SHAPE, RailShape.ASCENDING_EAST);
         case ASCENDING_NORTH:
            return pState.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
         case ASCENDING_SOUTH:
            return pState.setValue(SHAPE, RailShape.ASCENDING_NORTH);
         case SOUTH_EAST:
            return pState.setValue(SHAPE, RailShape.NORTH_WEST);
         case SOUTH_WEST:
            return pState.setValue(SHAPE, RailShape.NORTH_EAST);
         case NORTH_WEST:
            return pState.setValue(SHAPE, RailShape.SOUTH_EAST);
         case NORTH_EAST:
            return pState.setValue(SHAPE, RailShape.SOUTH_WEST);
         }
      case COUNTERCLOCKWISE_90:
         switch((RailShape)pState.getValue(SHAPE)) {
         case ASCENDING_EAST:
            return pState.setValue(SHAPE, RailShape.ASCENDING_NORTH);
         case ASCENDING_WEST:
            return pState.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
         case ASCENDING_NORTH:
            return pState.setValue(SHAPE, RailShape.ASCENDING_WEST);
         case ASCENDING_SOUTH:
            return pState.setValue(SHAPE, RailShape.ASCENDING_EAST);
         case SOUTH_EAST:
            return pState.setValue(SHAPE, RailShape.NORTH_EAST);
         case SOUTH_WEST:
            return pState.setValue(SHAPE, RailShape.SOUTH_EAST);
         case NORTH_WEST:
            return pState.setValue(SHAPE, RailShape.SOUTH_WEST);
         case NORTH_EAST:
            return pState.setValue(SHAPE, RailShape.NORTH_WEST);
         case NORTH_SOUTH:
            return pState.setValue(SHAPE, RailShape.EAST_WEST);
         case EAST_WEST:
            return pState.setValue(SHAPE, RailShape.NORTH_SOUTH);
         }
      case CLOCKWISE_90:
         switch((RailShape)pState.getValue(SHAPE)) {
         case ASCENDING_EAST:
            return pState.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
         case ASCENDING_WEST:
            return pState.setValue(SHAPE, RailShape.ASCENDING_NORTH);
         case ASCENDING_NORTH:
            return pState.setValue(SHAPE, RailShape.ASCENDING_EAST);
         case ASCENDING_SOUTH:
            return pState.setValue(SHAPE, RailShape.ASCENDING_WEST);
         case SOUTH_EAST:
            return pState.setValue(SHAPE, RailShape.SOUTH_WEST);
         case SOUTH_WEST:
            return pState.setValue(SHAPE, RailShape.NORTH_WEST);
         case NORTH_WEST:
            return pState.setValue(SHAPE, RailShape.NORTH_EAST);
         case NORTH_EAST:
            return pState.setValue(SHAPE, RailShape.SOUTH_EAST);
         case NORTH_SOUTH:
            return pState.setValue(SHAPE, RailShape.EAST_WEST);
         case EAST_WEST:
            return pState.setValue(SHAPE, RailShape.NORTH_SOUTH);
         }
      default:
         return pState;
      }
   }

   /**
    * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link IBlockState#withMirror(Mirror)} whenever possible. Implementing/overriding is fine.
    */
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      RailShape railshape = pState.getValue(SHAPE);
      switch(pMirror) {
      case LEFT_RIGHT:
         switch(railshape) {
         case ASCENDING_NORTH:
            return pState.setValue(SHAPE, RailShape.ASCENDING_SOUTH);
         case ASCENDING_SOUTH:
            return pState.setValue(SHAPE, RailShape.ASCENDING_NORTH);
         case SOUTH_EAST:
            return pState.setValue(SHAPE, RailShape.NORTH_EAST);
         case SOUTH_WEST:
            return pState.setValue(SHAPE, RailShape.NORTH_WEST);
         case NORTH_WEST:
            return pState.setValue(SHAPE, RailShape.SOUTH_WEST);
         case NORTH_EAST:
            return pState.setValue(SHAPE, RailShape.SOUTH_EAST);
         default:
            return super.mirror(pState, pMirror);
         }
      case FRONT_BACK:
         switch(railshape) {
         case ASCENDING_EAST:
            return pState.setValue(SHAPE, RailShape.ASCENDING_WEST);
         case ASCENDING_WEST:
            return pState.setValue(SHAPE, RailShape.ASCENDING_EAST);
         case ASCENDING_NORTH:
         case ASCENDING_SOUTH:
         default:
            break;
         case SOUTH_EAST:
            return pState.setValue(SHAPE, RailShape.SOUTH_WEST);
         case SOUTH_WEST:
            return pState.setValue(SHAPE, RailShape.SOUTH_EAST);
         case NORTH_WEST:
            return pState.setValue(SHAPE, RailShape.NORTH_EAST);
         case NORTH_EAST:
            return pState.setValue(SHAPE, RailShape.NORTH_WEST);
         }
      }

      return super.mirror(pState, pMirror);
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(SHAPE, POWERED);
   }
}
