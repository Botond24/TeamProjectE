package net.minecraft.util;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class EntityDamageSource extends DamageSource {
   @Nullable
   protected final Entity entity;
   /** Whether this EntityDamageSource is from an entity wearing Thorns-enchanted armor. */
   private boolean isThorns;

   public EntityDamageSource(String pDamageTypeId, @Nullable Entity pEntity) {
      super(pDamageTypeId);
      this.entity = pEntity;
   }

   /**
    * Sets this EntityDamageSource as originating from Thorns armor
    */
   public EntityDamageSource setThorns() {
      this.isThorns = true;
      return this;
   }

   public boolean isThorns() {
      return this.isThorns;
   }

   /**
    * Retrieves the true causer of the damage, e.g. the player who fired an arrow, the shulker who fired the bullet,
    * etc.
    */
   @Nullable
   public Entity getEntity() {
      return this.entity;
   }

   /**
    * Gets the death message that is displayed when the player dies
    */
   public ITextComponent getLocalizedDeathMessage(LivingEntity pLivingEntity) {
      ItemStack itemstack = this.entity instanceof LivingEntity ? ((LivingEntity)this.entity).getMainHandItem() : ItemStack.EMPTY;
      String s = "death.attack." + this.msgId;
      return !itemstack.isEmpty() && itemstack.hasCustomHoverName() ? new TranslationTextComponent(s + ".item", pLivingEntity.getDisplayName(), this.entity.getDisplayName(), itemstack.getDisplayName()) : new TranslationTextComponent(s, pLivingEntity.getDisplayName(), this.entity.getDisplayName());
   }

   /**
    * Return whether this damage source will have its damage amount scaled based on the current difficulty.
    */
   public boolean scalesWithDifficulty() {
      return this.entity != null && this.entity instanceof LivingEntity && !(this.entity instanceof PlayerEntity);
   }

   /**
    * Gets the location from which the damage originates.
    */
   @Nullable
   public Vector3d getSourcePosition() {
      return this.entity != null ? this.entity.position() : null;
   }

   public String toString() {
      return "EntityDamageSource (" + this.entity + ")";
   }
}