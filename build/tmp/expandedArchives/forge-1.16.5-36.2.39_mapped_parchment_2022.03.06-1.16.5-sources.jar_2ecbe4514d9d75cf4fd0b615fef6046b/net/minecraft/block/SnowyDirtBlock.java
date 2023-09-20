package net.minecraft.block;

import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class SnowyDirtBlock extends Block {
   public static final BooleanProperty SNOWY = BlockStateProperties.SNOWY;

   public SnowyDirtBlock(AbstractBlock.Properties p_i48327_1_) {
      super(p_i48327_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(SNOWY, Boolean.valueOf(false)));
   }

   /**
    * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific face passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      return pFacing != Direction.UP ? super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos) : pState.setValue(SNOWY, Boolean.valueOf(pFacingState.is(Blocks.SNOW_BLOCK) || pFacingState.is(Blocks.SNOW)));
   }

   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      BlockState blockstate = pContext.getLevel().getBlockState(pContext.getClickedPos().above());
      return this.defaultBlockState().setValue(SNOWY, Boolean.valueOf(blockstate.is(Blocks.SNOW_BLOCK) || blockstate.is(Blocks.SNOW)));
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(SNOWY);
   }
}