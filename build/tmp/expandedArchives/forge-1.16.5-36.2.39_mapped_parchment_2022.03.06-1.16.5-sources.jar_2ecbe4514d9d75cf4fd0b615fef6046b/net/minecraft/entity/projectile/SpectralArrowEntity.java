package net.minecraft.entity.projectile;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

public class SpectralArrowEntity extends AbstractArrowEntity {
   private int duration = 200;

   public SpectralArrowEntity(EntityType<? extends SpectralArrowEntity> p_i50158_1_, World p_i50158_2_) {
      super(p_i50158_1_, p_i50158_2_);
   }

   public SpectralArrowEntity(World pLevel, LivingEntity pShooter) {
      super(EntityType.SPECTRAL_ARROW, pShooter, pLevel);
   }

   public SpectralArrowEntity(World pLevel, double pX, double pY, double pZ) {
      super(EntityType.SPECTRAL_ARROW, pX, pY, pZ, pLevel);
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      if (this.level.isClientSide && !this.inGround) {
         this.level.addParticle(ParticleTypes.INSTANT_EFFECT, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
      }

   }

   protected ItemStack getPickupItem() {
      return new ItemStack(Items.SPECTRAL_ARROW);
   }

   protected void doPostHurtEffects(LivingEntity pLiving) {
      super.doPostHurtEffects(pLiving);
      EffectInstance effectinstance = new EffectInstance(Effects.GLOWING, this.duration, 0);
      pLiving.addEffect(effectinstance);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.contains("Duration")) {
         this.duration = pCompound.getInt("Duration");
      }

   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("Duration", this.duration);
   }
}