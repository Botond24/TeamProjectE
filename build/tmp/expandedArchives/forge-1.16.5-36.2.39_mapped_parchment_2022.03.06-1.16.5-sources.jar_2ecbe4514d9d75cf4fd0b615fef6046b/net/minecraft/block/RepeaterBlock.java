package net.minecraft.block;

import java.util.Random;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RepeaterBlock extends RedstoneDiodeBlock {
   public static final BooleanProperty LOCKED = BlockStateProperties.LOCKED;
   public static final IntegerProperty DELAY = BlockStateProperties.DELAY;

   public RepeaterBlock(AbstractBlock.Properties p_i48340_1_) {
      super(p_i48340_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(DELAY, Integer.valueOf(1)).setValue(LOCKED, Boolean.valueOf(false)).setValue(POWERED, Boolean.valueOf(false)));
   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      if (!pPlayer.abilities.mayBuild) {
         return ActionResultType.PASS;
      } else {
         pLevel.setBlock(pPos, pState.cycle(DELAY), 3);
         return ActionResultType.sidedSuccess(pLevel.isClientSide);
      }
   }

   protected int getDelay(BlockState pState) {
      return pState.getValue(DELAY) * 2;
   }

   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      BlockState blockstate = super.getStateForPlacement(pContext);
      return blockstate.setValue(LOCKED, Boolean.valueOf(this.isLocked(pContext.getLevel(), pContext.getClickedPos(), blockstate)));
   }

   /**
    * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific face passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      return !pLevel.isClientSide() && pFacing.getAxis() != pState.getValue(FACING).getAxis() ? pState.setValue(LOCKED, Boolean.valueOf(this.isLocked(pLevel, pCurrentPos, pState))) : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public boolean isLocked(IWorldReader pLevel, BlockPos pPos, BlockState pState) {
      return this.getAlternateSignal(pLevel, pPos, pState) > 0;
   }

   protected boolean isAlternateInput(BlockState pState) {
      return isDiode(pState);
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
    * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
    * of whether the block can receive random update ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
      if (pState.getValue(POWERED)) {
         Direction direction = pState.getValue(FACING);
         double d0 = (double)pPos.getX() + 0.5D + (pRand.nextDouble() - 0.5D) * 0.2D;
         double d1 = (double)pPos.getY() + 0.4D + (pRand.nextDouble() - 0.5D) * 0.2D;
         double d2 = (double)pPos.getZ() + 0.5D + (pRand.nextDouble() - 0.5D) * 0.2D;
         float f = -5.0F;
         if (pRand.nextBoolean()) {
            f = (float)(pState.getValue(DELAY) * 2 - 1);
         }

         f = f / 16.0F;
         double d3 = (double)(f * (float)direction.getStepX());
         double d4 = (double)(f * (float)direction.getStepZ());
         pLevel.addParticle(RedstoneParticleData.REDSTONE, d0 + d3, d1, d2 + d4, 0.0D, 0.0D, 0.0D);
      }
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING, DELAY, LOCKED, POWERED);
   }
}