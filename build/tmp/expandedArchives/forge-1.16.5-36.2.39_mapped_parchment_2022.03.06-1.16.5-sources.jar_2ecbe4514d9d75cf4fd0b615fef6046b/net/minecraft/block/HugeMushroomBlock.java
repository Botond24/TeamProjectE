package net.minecraft.block;

import java.util.Map;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

public class HugeMushroomBlock extends Block {
   public static final BooleanProperty NORTH = SixWayBlock.NORTH;
   public static final BooleanProperty EAST = SixWayBlock.EAST;
   public static final BooleanProperty SOUTH = SixWayBlock.SOUTH;
   public static final BooleanProperty WEST = SixWayBlock.WEST;
   public static final BooleanProperty UP = SixWayBlock.UP;
   public static final BooleanProperty DOWN = SixWayBlock.DOWN;
   private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = SixWayBlock.PROPERTY_BY_DIRECTION;

   public HugeMushroomBlock(AbstractBlock.Properties p_i49982_1_) {
      super(p_i49982_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, Boolean.valueOf(true)).setValue(EAST, Boolean.valueOf(true)).setValue(SOUTH, Boolean.valueOf(true)).setValue(WEST, Boolean.valueOf(true)).setValue(UP, Boolean.valueOf(true)).setValue(DOWN, Boolean.valueOf(true)));
   }

   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      IBlockReader iblockreader = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      return this.defaultBlockState().setValue(DOWN, Boolean.valueOf(this != iblockreader.getBlockState(blockpos.below()).getBlock())).setValue(UP, Boolean.valueOf(this != iblockreader.getBlockState(blockpos.above()).getBlock())).setValue(NORTH, Boolean.valueOf(this != iblockreader.getBlockState(blockpos.north()).getBlock())).setValue(EAST, Boolean.valueOf(this != iblockreader.getBlockState(blockpos.east()).getBlock())).setValue(SOUTH, Boolean.valueOf(this != iblockreader.getBlockState(blockpos.south()).getBlock())).setValue(WEST, Boolean.valueOf(this != iblockreader.getBlockState(blockpos.west()).getBlock()));
   }

   /**
    * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific face passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      return pFacingState.is(this) ? pState.setValue(PROPERTY_BY_DIRECTION.get(pFacing), Boolean.valueOf(false)) : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link IBlockState#withRotation(Rotation)} whenever possible. Implementing/overriding is
    * fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRotation) {
      return pState.setValue(PROPERTY_BY_DIRECTION.get(pRotation.rotate(Direction.NORTH)), pState.getValue(NORTH)).setValue(PROPERTY_BY_DIRECTION.get(pRotation.rotate(Direction.SOUTH)), pState.getValue(SOUTH)).setValue(PROPERTY_BY_DIRECTION.get(pRotation.rotate(Direction.EAST)), pState.getValue(EAST)).setValue(PROPERTY_BY_DIRECTION.get(pRotation.rotate(Direction.WEST)), pState.getValue(WEST)).setValue(PROPERTY_BY_DIRECTION.get(pRotation.rotate(Direction.UP)), pState.getValue(UP)).setValue(PROPERTY_BY_DIRECTION.get(pRotation.rotate(Direction.DOWN)), pState.getValue(DOWN));
   }

   /**
    * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link IBlockState#withMirror(Mirror)} whenever possible. Implementing/overriding is fine.
    */
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      return pState.setValue(PROPERTY_BY_DIRECTION.get(pMirror.mirror(Direction.NORTH)), pState.getValue(NORTH)).setValue(PROPERTY_BY_DIRECTION.get(pMirror.mirror(Direction.SOUTH)), pState.getValue(SOUTH)).setValue(PROPERTY_BY_DIRECTION.get(pMirror.mirror(Direction.EAST)), pState.getValue(EAST)).setValue(PROPERTY_BY_DIRECTION.get(pMirror.mirror(Direction.WEST)), pState.getValue(WEST)).setValue(PROPERTY_BY_DIRECTION.get(pMirror.mirror(Direction.UP)), pState.getValue(UP)).setValue(PROPERTY_BY_DIRECTION.get(pMirror.mirror(Direction.DOWN)), pState.getValue(DOWN));
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(UP, DOWN, NORTH, EAST, SOUTH, WEST);
   }
}