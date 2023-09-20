package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.util.DamageSource;

public abstract class SittingPhase extends Phase {
   public SittingPhase(EnderDragonEntity p_i46794_1_) {
      super(p_i46794_1_);
   }

   public boolean isSitting() {
      return true;
   }

   public float onHurt(DamageSource pDamageSource, float pAmount) {
      if (pDamageSource.getDirectEntity() instanceof AbstractArrowEntity) {
         pDamageSource.getDirectEntity().setSecondsOnFire(1);
         return 0.0F;
      } else {
         return super.onHurt(pDamageSource, pAmount);
      }
   }
}