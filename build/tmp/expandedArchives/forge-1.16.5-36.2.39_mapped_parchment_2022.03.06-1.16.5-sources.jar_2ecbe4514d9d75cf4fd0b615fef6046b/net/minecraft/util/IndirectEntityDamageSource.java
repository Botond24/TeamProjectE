package net.minecraft.util;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class IndirectEntityDamageSource extends EntityDamageSource {
   private final Entity owner;

   public IndirectEntityDamageSource(String pDamageTypeId, Entity pSource, @Nullable Entity pIndirectEntity) {
      super(pDamageTypeId, pSource);
      this.owner = pIndirectEntity;
   }

   /**
    * Retrieves the immediate causer of the damage, e.g. the arrow entity, not its shooter
    */
   @Nullable
   public Entity getDirectEntity() {
      return this.entity;
   }

   /**
    * Retrieves the true causer of the damage, e.g. the player who fired an arrow, the shulker who fired the bullet,
    * etc.
    */
   @Nullable
   public Entity getEntity() {
      return this.owner;
   }

   /**
    * Gets the death message that is displayed when the player dies
    */
   public ITextComponent getLocalizedDeathMessage(LivingEntity pLivingEntity) {
      ITextComponent itextcomponent = this.owner == null ? this.entity.getDisplayName() : this.owner.getDisplayName();
      ItemStack itemstack = this.owner instanceof LivingEntity ? ((LivingEntity)this.owner).getMainHandItem() : ItemStack.EMPTY;
      String s = "death.attack." + this.msgId;
      String s1 = s + ".item";
      return !itemstack.isEmpty() && itemstack.hasCustomHoverName() ? new TranslationTextComponent(s1, pLivingEntity.getDisplayName(), itextcomponent, itemstack.getDisplayName()) : new TranslationTextComponent(s, pLivingEntity.getDisplayName(), itextcomponent);
   }
}