package net.minecraft.block;

import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PoweredRailBlock extends AbstractRailBlock {
   public static final EnumProperty<RailShape> SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   private final boolean isActivator;  // TRUE for an Activator Rail, FALSE for Powered Rail

   public PoweredRailBlock(AbstractBlock.Properties p_i48349_1_) {
      this(p_i48349_1_, false);
   }

   protected PoweredRailBlock(AbstractBlock.Properties builder, boolean isPoweredRail) {
      super(true, builder);
      this.registerDefaultState(this.stateDefinition.any().setValue(SHAPE, RailShape.NORTH_SOUTH).setValue(POWERED, Boolean.valueOf(false)));
      this.isActivator = !isPoweredRail;
   }

   protected boolean findPoweredRailSignal(World pLevel, BlockPos pPos, BlockState pState, boolean pSearchForward, int pRecursionCount) {
      if (pRecursionCount >= 8) {
         return false;
      } else {
         int i = pPos.getX();
         int j = pPos.getY();
         int k = pPos.getZ();
         boolean flag = true;
         RailShape railshape = pState.getValue(SHAPE);
         switch(railshape) {
         case NORTH_SOUTH:
            if (pSearchForward) {
               ++k;
            } else {
               --k;
            }
            break;
         case EAST_WEST:
            if (pSearchForward) {
               --i;
            } else {
               ++i;
            }
            break;
         case ASCENDING_EAST:
            if (pSearchForward) {
               --i;
            } else {
               ++i;
               ++j;
               flag = false;
            }

            railshape = RailShape.EAST_WEST;
            break;
         case ASCENDING_WEST:
            if (pSearchForward) {
               --i;
               ++j;
               flag = false;
            } else {
               ++i;
            }

            railshape = RailShape.EAST_WEST;
            break;
         case ASCENDING_NORTH:
            if (pSearchForward) {
               ++k;
            } else {
               --k;
               ++j;
               flag = false;
            }

            railshape = RailShape.NORTH_SOUTH;
            break;
         case ASCENDING_SOUTH:
            if (pSearchForward) {
               ++k;
               ++j;
               flag = false;
            } else {
               --k;
            }

            railshape = RailShape.NORTH_SOUTH;
         }

         if (this.isSameRailWithPower(pLevel, new BlockPos(i, j, k), pSearchForward, pRecursionCount, railshape)) {
            return true;
         } else {
            return flag && this.isSameRailWithPower(pLevel, new BlockPos(i, j - 1, k), pSearchForward, pRecursionCount, railshape);
         }
      }
   }

   protected boolean isSameRailWithPower(World pLevel, BlockPos pState, boolean pSearchForward, int pRecursionCount, RailShape pShape) {
      BlockState blockstate = pLevel.getBlockState(pState);
      if (!(blockstate.getBlock() instanceof PoweredRailBlock)) {
         return false;
      } else {
         RailShape railshape = getRailDirection(blockstate, pLevel, pState, null);
         if (pShape != RailShape.EAST_WEST || railshape != RailShape.NORTH_SOUTH && railshape != RailShape.ASCENDING_NORTH && railshape != RailShape.ASCENDING_SOUTH) {
            if (pShape != RailShape.NORTH_SOUTH || railshape != RailShape.EAST_WEST && railshape != RailShape.ASCENDING_EAST && railshape != RailShape.ASCENDING_WEST) {
               if (isActivator == (((PoweredRailBlock) blockstate.getBlock()).isActivator)) {
                  return pLevel.hasNeighborSignal(pState) ? true : this.findPoweredRailSignal(pLevel, pState, blockstate, pSearchForward, pRecursionCount + 1);
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   protected void updateState(BlockState pState, World pLevel, BlockPos pPos, Block pBlock) {
      boolean flag = pState.getValue(POWERED);
      boolean flag1 = pLevel.hasNeighborSignal(pPos) || this.findPoweredRailSignal(pLevel, pPos, pState, true, 0) || this.findPoweredRailSignal(pLevel, pPos, pState, false, 0);
      if (flag1 != flag) {
         pLevel.setBlock(pPos, pState.setValue(POWERED, Boolean.valueOf(flag1)), 3);
         pLevel.updateNeighborsAt(pPos.below(), this);
         if (pState.getValue(SHAPE).isAscending()) {
            pLevel.updateNeighborsAt(pPos.above(), this);
         }
      }

   }

   public Property<RailShape> getShapeProperty() {
      return SHAPE;
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
         case NORTH_SOUTH: //Forge fix: MC-196102
         case EAST_WEST:
            return pState;
         }
      case COUNTERCLOCKWISE_90:
         switch((RailShape)pState.getValue(SHAPE)) {
         case NORTH_SOUTH:
            return pState.setValue(SHAPE, RailShape.EAST_WEST);
         case EAST_WEST:
            return pState.setValue(SHAPE, RailShape.NORTH_SOUTH);
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
         }
      case CLOCKWISE_90:
         switch((RailShape)pState.getValue(SHAPE)) {
         case NORTH_SOUTH:
            return pState.setValue(SHAPE, RailShape.EAST_WEST);
         case EAST_WEST:
            return pState.setValue(SHAPE, RailShape.NORTH_SOUTH);
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

   public boolean isActivatorRail() {
      return isActivator;
   }
}
