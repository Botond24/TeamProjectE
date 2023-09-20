package net.minecraft.world;

import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;

public class ExplosionContext {
   public Optional<Float> getBlockExplosionResistance(Explosion pExplosion, IBlockReader pReader, BlockPos pPos, BlockState pState, FluidState pFluid) {
      return pState.isAir(pReader, pPos) && pFluid.isEmpty() ? Optional.empty() : Optional.of(Math.max(pState.getExplosionResistance(pReader, pPos, pExplosion), pFluid.getExplosionResistance(pReader, pPos, pExplosion)));
   }

   public boolean shouldBlockExplode(Explosion pExplosion, IBlockReader pReader, BlockPos pPos, BlockState pState, float pPower) {
      return true;
   }
}
