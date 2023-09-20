package net.minecraft.block;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RedstoneTorchBlock extends TorchBlock {
   public static final BooleanProperty LIT = BlockStateProperties.LIT;
   private static final Map<IBlockReader, List<RedstoneTorchBlock.Toggle>> RECENT_TOGGLES = new WeakHashMap<>();

   public RedstoneTorchBlock(AbstractBlock.Properties p_i48342_1_) {
      super(p_i48342_1_, RedstoneParticleData.REDSTONE);
      this.registerDefaultState(this.stateDefinition.any().setValue(LIT, Boolean.valueOf(true)));
   }

   public void onPlace(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      for(Direction direction : Direction.values()) {
         pLevel.updateNeighborsAt(pPos.relative(direction), this);
      }

   }

   public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pIsMoving) {
         for(Direction direction : Direction.values()) {
            pLevel.updateNeighborsAt(pPos.relative(direction), this);
         }

      }
   }

   /**
    * @deprecated call via {@link IBlockState#getWeakPower(IBlockAccess,BlockPos,EnumFacing)} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getSignal(BlockState pBlockState, IBlockReader pBlockAccess, BlockPos pPos, Direction pSide) {
      return pBlockState.getValue(LIT) && Direction.UP != pSide ? 15 : 0;
   }

   protected boolean hasNeighborSignal(World pLevel, BlockPos pPos, BlockState pState) {
      return pLevel.hasSignal(pPos.below(), Direction.DOWN);
   }

   public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
      boolean flag = this.hasNeighborSignal(pLevel, pPos, pState);
      List<RedstoneTorchBlock.Toggle> list = RECENT_TOGGLES.get(pLevel);

      while(list != null && !list.isEmpty() && pLevel.getGameTime() - (list.get(0)).when > 60L) {
         list.remove(0);
      }

      if (pState.getValue(LIT)) {
         if (flag) {
            pLevel.setBlock(pPos, pState.setValue(LIT, Boolean.valueOf(false)), 3);
            if (isToggledTooFrequently(pLevel, pPos, true)) {
               pLevel.levelEvent(1502, pPos, 0);
               pLevel.getBlockTicks().scheduleTick(pPos, pLevel.getBlockState(pPos).getBlock(), 160);
            }
         }
      } else if (!flag && !isToggledTooFrequently(pLevel, pPos, false)) {
         pLevel.setBlock(pPos, pState.setValue(LIT, Boolean.valueOf(true)), 3);
      }

   }

   public void neighborChanged(BlockState pState, World pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      if (pState.getValue(LIT) == this.hasNeighborSignal(pLevel, pPos, pState) && !pLevel.getBlockTicks().willTickThisTick(pPos, this)) {
         pLevel.getBlockTicks().scheduleTick(pPos, this, 2);
      }

   }

   /**
    * @deprecated call via {@link IBlockState#getStrongPower(IBlockAccess,BlockPos,EnumFacing)} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getDirectSignal(BlockState pBlockState, IBlockReader pBlockAccess, BlockPos pPos, Direction pSide) {
      return pSide == Direction.DOWN ? pBlockState.getSignal(pBlockAccess, pPos, pSide) : 0;
   }

   /**
    * Can this block provide power. Only wire currently seems to have this change based on its state.
    * @deprecated call via {@link IBlockState#canProvidePower()} whenever possible. Implementing/overriding is fine.
    */
   public boolean isSignalSource(BlockState pState) {
      return true;
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
    * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
    * of whether the block can receive random update ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
      if (pState.getValue(LIT)) {
         double d0 = (double)pPos.getX() + 0.5D + (pRand.nextDouble() - 0.5D) * 0.2D;
         double d1 = (double)pPos.getY() + 0.7D + (pRand.nextDouble() - 0.5D) * 0.2D;
         double d2 = (double)pPos.getZ() + 0.5D + (pRand.nextDouble() - 0.5D) * 0.2D;
         pLevel.addParticle(this.flameParticle, d0, d1, d2, 0.0D, 0.0D, 0.0D);
      }
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(LIT);
   }

   private static boolean isToggledTooFrequently(World pLevel, BlockPos pLevelConflicting, boolean pPos) {
      List<RedstoneTorchBlock.Toggle> list = RECENT_TOGGLES.computeIfAbsent(pLevel, (p_220288_0_) -> {
         return Lists.newArrayList();
      });
      if (pPos) {
         list.add(new RedstoneTorchBlock.Toggle(pLevelConflicting.immutable(), pLevel.getGameTime()));
      }

      int i = 0;

      for(int j = 0; j < list.size(); ++j) {
         RedstoneTorchBlock.Toggle redstonetorchblock$toggle = list.get(j);
         if (redstonetorchblock$toggle.pos.equals(pLevelConflicting)) {
            ++i;
            if (i >= 8) {
               return true;
            }
         }
      }

      return false;
   }

   public static class Toggle {
      private final BlockPos pos;
      private final long when;

      public Toggle(BlockPos pPos, long pWhen) {
         this.pos = pPos;
         this.when = pWhen;
      }
   }
}