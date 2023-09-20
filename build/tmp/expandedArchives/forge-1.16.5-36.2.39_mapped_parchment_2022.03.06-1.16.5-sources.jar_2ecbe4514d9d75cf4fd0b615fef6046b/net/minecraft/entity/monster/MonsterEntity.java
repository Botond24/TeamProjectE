package net.minecraft.entity.monster;

import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShootableItem;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public abstract class MonsterEntity extends CreatureEntity implements IMob {
   protected MonsterEntity(EntityType<? extends MonsterEntity> p_i48553_1_, World p_i48553_2_) {
      super(p_i48553_1_, p_i48553_2_);
      this.xpReward = 5;
   }

   public SoundCategory getSoundSource() {
      return SoundCategory.HOSTILE;
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      this.updateSwingTime();
      this.updateNoActionTime();
      super.aiStep();
   }

   protected void updateNoActionTime() {
      float f = this.getBrightness();
      if (f > 0.5F) {
         this.noActionTime += 2;
      }

   }

   protected boolean shouldDespawnInPeaceful() {
      return true;
   }

   protected SoundEvent getSwimSound() {
      return SoundEvents.HOSTILE_SWIM;
   }

   protected SoundEvent getSwimSplashSound() {
      return SoundEvents.HOSTILE_SPLASH;
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      return this.isInvulnerableTo(pSource) ? false : super.hurt(pSource, pAmount);
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.HOSTILE_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.HOSTILE_DEATH;
   }

   protected SoundEvent getFallDamageSound(int pHeight) {
      return pHeight > 4 ? SoundEvents.HOSTILE_BIG_FALL : SoundEvents.HOSTILE_SMALL_FALL;
   }

   public float getWalkTargetValue(BlockPos pPos, IWorldReader pLevel) {
      return 0.5F - pLevel.getBrightness(pPos);
   }

   /**
    * Static predicate for determining if the current light level and environmental conditions allow for a monster to
    * spawn.
    */
   public static boolean isDarkEnoughToSpawn(IServerWorld pLevel, BlockPos pPos, Random pRandom) {
      if (pLevel.getBrightness(LightType.SKY, pPos) > pRandom.nextInt(32)) {
         return false;
      } else {
         int i = pLevel.getLevel().isThundering() ? pLevel.getMaxLocalRawBrightness(pPos, 10) : pLevel.getMaxLocalRawBrightness(pPos);
         return i <= pRandom.nextInt(8);
      }
   }

   /**
    * Static predicate for determining whether or not a monster can spawn at the provided location, incorporating a
    * check of the current light level at the location.
    */
   public static boolean checkMonsterSpawnRules(EntityType<? extends MonsterEntity> pType, IServerWorld pLevel, SpawnReason pSpawnType, BlockPos pPos, Random pRandom) {
      return pLevel.getDifficulty() != Difficulty.PEACEFUL && isDarkEnoughToSpawn(pLevel, pPos, pRandom) && checkMobSpawnRules(pType, pLevel, pSpawnType, pPos, pRandom);
   }

   /**
    * Static predicate for determining whether or not a monster can spawn at the provided location.
    */
   public static boolean checkAnyLightMonsterSpawnRules(EntityType<? extends MonsterEntity> pType, IWorld pLevel, SpawnReason pSpawnType, BlockPos pPos, Random pRandom) {
      return pLevel.getDifficulty() != Difficulty.PEACEFUL && checkMobSpawnRules(pType, pLevel, pSpawnType, pPos, pRandom);
   }

   public static AttributeModifierMap.MutableAttribute createMonsterAttributes() {
      return MobEntity.createMobAttributes().add(Attributes.ATTACK_DAMAGE);
   }

   /**
    * Entity won't drop items or experience points if this returns false
    */
   protected boolean shouldDropExperience() {
      return true;
   }

   /**
    * Entity won't drop items if this returns false
    */
   protected boolean shouldDropLoot() {
      return true;
   }

   public boolean isPreventingPlayerRest(PlayerEntity pPlayer) {
      return true;
   }

   public ItemStack getProjectile(ItemStack pShootable) {
      if (pShootable.getItem() instanceof ShootableItem) {
         Predicate<ItemStack> predicate = ((ShootableItem)pShootable.getItem()).getSupportedHeldProjectiles();
         ItemStack itemstack = ShootableItem.getHeldProjectile(this, predicate);
         return itemstack.isEmpty() ? new ItemStack(Items.ARROW) : itemstack;
      } else {
         return ItemStack.EMPTY;
      }
   }
}