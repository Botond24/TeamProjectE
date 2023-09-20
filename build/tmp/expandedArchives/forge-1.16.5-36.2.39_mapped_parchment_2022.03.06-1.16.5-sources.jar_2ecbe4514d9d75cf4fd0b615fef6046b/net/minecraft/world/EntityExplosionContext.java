package net.minecraft.world;

import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;

public class EntityExplosionContext extends ExplosionContext {
   private final Entity source;

   public EntityExplosionContext(Entity pSouce) {
      this.source = pSouce;
   }

   public Optional<Float> getBlockExplosionResistance(Explosion pExplosion, IBlockReader pReader, BlockPos pPos, BlockState pState, FluidState pFluid) {
      return super.getBlockExplosionResistance(pExplosion, pReader, pPos, pState, pFluid).map((p_234890_6_) -> {
         return this.source.getBlockExplosionResistance(pExplosion, pReader, pPos, pState, pFluid, p_234890_6_);
      });
   }

   public boolean shouldBlockExplode(Explosion pExplosion, IBlockReader pReader, BlockPos pPos, BlockState pState, float pPower) {
      return this.source.shouldBlockExplode(pExplosion, pReader, pPos, pState, pPower);
   }
}