package net.minecraft.entity.monster;

import java.util.Random;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

public class StrayEntity extends AbstractSkeletonEntity {
   public StrayEntity(EntityType<? extends StrayEntity> p_i50191_1_, World p_i50191_2_) {
      super(p_i50191_1_, p_i50191_2_);
   }

   public static boolean checkStraySpawnRules(EntityType<StrayEntity> pStray, IServerWorld pLevel, SpawnReason pSpawnType, BlockPos pPos, Random pRandom) {
      return checkMonsterSpawnRules(pStray, pLevel, pSpawnType, pPos, pRandom) && (pSpawnType == SpawnReason.SPAWNER || pLevel.canSeeSky(pPos));
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.STRAY_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.STRAY_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.STRAY_DEATH;
   }

   protected SoundEvent getStepSound() {
      return SoundEvents.STRAY_STEP;
   }

   /**
    * Fires an arrow
    */
   protected AbstractArrowEntity getArrow(ItemStack pArrowStack, float pDistanceFactor) {
      AbstractArrowEntity abstractarrowentity = super.getArrow(pArrowStack, pDistanceFactor);
      if (abstractarrowentity instanceof ArrowEntity) {
         ((ArrowEntity)abstractarrowentity).addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 600));
      }

      return abstractarrowentity;
   }
}