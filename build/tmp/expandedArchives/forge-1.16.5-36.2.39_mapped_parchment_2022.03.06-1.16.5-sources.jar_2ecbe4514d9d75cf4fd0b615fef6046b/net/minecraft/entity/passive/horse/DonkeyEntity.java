package net.minecraft.entity.passive.horse;

import javax.annotation.Nullable;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class DonkeyEntity extends AbstractChestedHorseEntity {
   public DonkeyEntity(EntityType<? extends DonkeyEntity> p_i50239_1_, World p_i50239_2_) {
      super(p_i50239_1_, p_i50239_2_);
   }

   protected SoundEvent getAmbientSound() {
      super.getAmbientSound();
      return SoundEvents.DONKEY_AMBIENT;
   }

   protected SoundEvent getAngrySound() {
      super.getAngrySound();
      return SoundEvents.DONKEY_ANGRY;
   }

   protected SoundEvent getDeathSound() {
      super.getDeathSound();
      return SoundEvents.DONKEY_DEATH;
   }

   @Nullable
   protected SoundEvent getEatingSound() {
      return SoundEvents.DONKEY_EAT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      super.getHurtSound(pDamageSource);
      return SoundEvents.DONKEY_HURT;
   }

   /**
    * Returns true if the mob is currently able to mate with the specified mob.
    */
   public boolean canMate(AnimalEntity pOtherAnimal) {
      if (pOtherAnimal == this) {
         return false;
      } else if (!(pOtherAnimal instanceof DonkeyEntity) && !(pOtherAnimal instanceof HorseEntity)) {
         return false;
      } else {
         return this.canParent() && ((AbstractHorseEntity)pOtherAnimal).canParent();
      }
   }

   public AgeableEntity getBreedOffspring(ServerWorld pServerLevel, AgeableEntity pMate) {
      EntityType<? extends AbstractHorseEntity> entitytype = pMate instanceof HorseEntity ? EntityType.MULE : EntityType.DONKEY;
      AbstractHorseEntity abstracthorseentity = entitytype.create(pServerLevel);
      this.setOffspringAttributes(pMate, abstracthorseentity);
      return abstracthorseentity;
   }
}