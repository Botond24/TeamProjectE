package net.minecraft.entity;

public interface IRangedAttackMob {
   /**
    * Attack the specified entity using a ranged attack.
    */
   void performRangedAttack(LivingEntity pTarget, float pVelocity);
}