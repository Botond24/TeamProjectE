package net.minecraft.util;

import javax.annotation.Nullable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.text.ITextComponent;

public class CombatEntry {
   private final DamageSource source;
   private final int time;
   private final float damage;
   private final float health;
   private final String location;
   private final float fallDistance;

   public CombatEntry(DamageSource pSource, int pTime, float pHealth, float pDamage, String pLocation, float pFallDistance) {
      this.source = pSource;
      this.time = pTime;
      this.damage = pDamage;
      this.health = pHealth;
      this.location = pLocation;
      this.fallDistance = pFallDistance;
   }

   /**
    * Get the DamageSource of the CombatEntry instance.
    */
   public DamageSource getSource() {
      return this.source;
   }

   public float getDamage() {
      return this.damage;
   }

   /**
    * Returns true if {@link net.minecraft.util.DamageSource#getEntity() damage source} is a living entity
    */
   public boolean isCombatRelated() {
      return this.source.getEntity() instanceof LivingEntity;
   }

   @Nullable
   public String getLocation() {
      return this.location;
   }

   @Nullable
   public ITextComponent getAttackerName() {
      return this.getSource().getEntity() == null ? null : this.getSource().getEntity().getDisplayName();
   }

   public float getFallDistance() {
      return this.source == DamageSource.OUT_OF_WORLD ? Float.MAX_VALUE : this.fallDistance;
   }
}