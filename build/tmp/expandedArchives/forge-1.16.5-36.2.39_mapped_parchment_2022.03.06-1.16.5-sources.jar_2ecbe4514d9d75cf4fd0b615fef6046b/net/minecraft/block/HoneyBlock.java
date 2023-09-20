package net.minecraft.block;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class HoneyBlock extends BreakableBlock {
   protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);

   public HoneyBlock(AbstractBlock.Properties p_i225762_1_) {
      super(p_i225762_1_);
   }

   private static boolean doesEntityDoHoneyBlockSlideEffects(Entity pEntity) {
      return pEntity instanceof LivingEntity || pEntity instanceof AbstractMinecartEntity || pEntity instanceof TNTEntity || pEntity instanceof BoatEntity;
   }

   public VoxelShape getCollisionShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return SHAPE;
   }

   public void fallOn(World p_180658_1_, BlockPos p_180658_2_, Entity p_180658_3_, float p_180658_4_) {
      p_180658_3_.playSound(SoundEvents.HONEY_BLOCK_SLIDE, 1.0F, 1.0F);
      if (!p_180658_1_.isClientSide) {
         p_180658_1_.broadcastEntityEvent(p_180658_3_, (byte)54);
      }

      if (p_180658_3_.causeFallDamage(p_180658_4_, 0.2F)) {
         p_180658_3_.playSound(this.soundType.getFallSound(), this.soundType.getVolume() * 0.5F, this.soundType.getPitch() * 0.75F);
      }

   }

   public void entityInside(BlockState pState, World pLevel, BlockPos pPos, Entity pEntity) {
      if (this.isSlidingDown(pPos, pEntity)) {
         this.maybeDoSlideAchievement(pEntity, pPos);
         this.doSlideMovement(pEntity);
         this.maybeDoSlideEffects(pLevel, pEntity);
      }

      super.entityInside(pState, pLevel, pPos, pEntity);
   }

   private boolean isSlidingDown(BlockPos pPos, Entity pEntity) {
      if (pEntity.isOnGround()) {
         return false;
      } else if (pEntity.getY() > (double)pPos.getY() + 0.9375D - 1.0E-7D) {
         return false;
      } else if (pEntity.getDeltaMovement().y >= -0.08D) {
         return false;
      } else {
         double d0 = Math.abs((double)pPos.getX() + 0.5D - pEntity.getX());
         double d1 = Math.abs((double)pPos.getZ() + 0.5D - pEntity.getZ());
         double d2 = 0.4375D + (double)(pEntity.getBbWidth() / 2.0F);
         return d0 + 1.0E-7D > d2 || d1 + 1.0E-7D > d2;
      }
   }

   private void maybeDoSlideAchievement(Entity pEntity, BlockPos pPos) {
      if (pEntity instanceof ServerPlayerEntity && pEntity.level.getGameTime() % 20L == 0L) {
         CriteriaTriggers.HONEY_BLOCK_SLIDE.trigger((ServerPlayerEntity)pEntity, pEntity.level.getBlockState(pPos));
      }

   }

   private void doSlideMovement(Entity pEntity) {
      Vector3d vector3d = pEntity.getDeltaMovement();
      if (vector3d.y < -0.13D) {
         double d0 = -0.05D / vector3d.y;
         pEntity.setDeltaMovement(new Vector3d(vector3d.x * d0, -0.05D, vector3d.z * d0));
      } else {
         pEntity.setDeltaMovement(new Vector3d(vector3d.x, -0.05D, vector3d.z));
      }

      pEntity.fallDistance = 0.0F;
   }

   private void maybeDoSlideEffects(World pLevel, Entity pEntity) {
      if (doesEntityDoHoneyBlockSlideEffects(pEntity)) {
         if (pLevel.random.nextInt(5) == 0) {
            pEntity.playSound(SoundEvents.HONEY_BLOCK_SLIDE, 1.0F, 1.0F);
         }

         if (!pLevel.isClientSide && pLevel.random.nextInt(5) == 0) {
            pLevel.broadcastEntityEvent(pEntity, (byte)53);
         }
      }

   }

   @OnlyIn(Dist.CLIENT)
   public static void showSlideParticles(Entity pEntity) {
      showParticles(pEntity, 5);
   }

   @OnlyIn(Dist.CLIENT)
   public static void showJumpParticles(Entity pEntity) {
      showParticles(pEntity, 10);
   }

   @OnlyIn(Dist.CLIENT)
   private static void showParticles(Entity pEntity, int pParticleCount) {
      if (pEntity.level.isClientSide) {
         BlockState blockstate = Blocks.HONEY_BLOCK.defaultBlockState();

         for(int i = 0; i < pParticleCount; ++i) {
            pEntity.level.addParticle(new BlockParticleData(ParticleTypes.BLOCK, blockstate), pEntity.getX(), pEntity.getY(), pEntity.getZ(), 0.0D, 0.0D, 0.0D);
         }

      }
   }
}