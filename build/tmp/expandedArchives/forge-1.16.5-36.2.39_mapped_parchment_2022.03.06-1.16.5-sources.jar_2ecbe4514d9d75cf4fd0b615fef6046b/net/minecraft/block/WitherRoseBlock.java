package net.minecraft.block;

import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WitherRoseBlock extends FlowerBlock {
   public WitherRoseBlock(Effect pSuspiciousStewEffect, AbstractBlock.Properties pProperties) {
      super(pSuspiciousStewEffect, 8, pProperties);
   }

   protected boolean mayPlaceOn(BlockState pState, IBlockReader pLevel, BlockPos pPos) {
      return super.mayPlaceOn(pState, pLevel, pPos) || pState.is(Blocks.NETHERRACK) || pState.is(Blocks.SOUL_SAND) || pState.is(Blocks.SOUL_SOIL);
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
    * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
    * of whether the block can receive random update ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
      VoxelShape voxelshape = this.getShape(pState, pLevel, pPos, ISelectionContext.empty());
      Vector3d vector3d = voxelshape.bounds().getCenter();
      double d0 = (double)pPos.getX() + vector3d.x;
      double d1 = (double)pPos.getZ() + vector3d.z;

      for(int i = 0; i < 3; ++i) {
         if (pRand.nextBoolean()) {
            pLevel.addParticle(ParticleTypes.SMOKE, d0 + pRand.nextDouble() / 5.0D, (double)pPos.getY() + (0.5D - pRand.nextDouble()), d1 + pRand.nextDouble() / 5.0D, 0.0D, 0.0D, 0.0D);
         }
      }

   }

   public void entityInside(BlockState pState, World pLevel, BlockPos pPos, Entity pEntity) {
      if (!pLevel.isClientSide && pLevel.getDifficulty() != Difficulty.PEACEFUL) {
         if (pEntity instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity)pEntity;
            if (!livingentity.isInvulnerableTo(DamageSource.WITHER)) {
               livingentity.addEffect(new EffectInstance(Effects.WITHER, 40));
            }
         }

      }
   }
}