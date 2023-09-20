package net.minecraft.potion;

import com.google.common.collect.ComparisonChain;
import javax.annotation.Nullable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EffectInstance implements Comparable<EffectInstance>, net.minecraftforge.common.extensions.IForgeEffectInstance {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Effect effect;
   private int duration;
   private int amplifier;
   private boolean splash;
   private boolean ambient;
   /** True if potion effect duration is at maximum, false otherwise. */
   @OnlyIn(Dist.CLIENT)
   private boolean noCounter;
   private boolean visible;
   private boolean showIcon;
   /** A hidden effect which is not shown to the player. */
   @Nullable
   private EffectInstance hiddenEffect;

   public EffectInstance(Effect pEffect) {
      this(pEffect, 0, 0);
   }

   public EffectInstance(Effect pEffect, int pDuration) {
      this(pEffect, pDuration, 0);
   }

   public EffectInstance(Effect pEffect, int pDuration, int pAmplifier) {
      this(pEffect, pDuration, pAmplifier, false, true);
   }

   public EffectInstance(Effect pEffect, int pDuration, int pAmplifier, boolean pAmbient, boolean pVisible) {
      this(pEffect, pDuration, pAmplifier, pAmbient, pVisible, pVisible);
   }

   public EffectInstance(Effect pEffect, int pDuration, int pAmplifier, boolean pAmbient, boolean pVisible, boolean pShowIcon) {
      this(pEffect, pDuration, pAmplifier, pAmbient, pVisible, pShowIcon, (EffectInstance)null);
   }

   public EffectInstance(Effect pEffect, int pDuration, int pAmplifier, boolean pAmbient, boolean pVisible, boolean pShowIcon, @Nullable EffectInstance pHiddenEffect) {
      this.effect = pEffect;
      this.duration = pDuration;
      this.amplifier = pAmplifier;
      this.ambient = pAmbient;
      this.visible = pVisible;
      this.showIcon = pShowIcon;
      this.hiddenEffect = pHiddenEffect;
   }

   public EffectInstance(EffectInstance pEffectInstance) {
      this.effect = pEffectInstance.effect;
      this.setDetailsFrom(pEffectInstance);
   }

   void setDetailsFrom(EffectInstance pEffectInstance) {
      this.duration = pEffectInstance.duration;
      this.amplifier = pEffectInstance.amplifier;
      this.ambient = pEffectInstance.ambient;
      this.visible = pEffectInstance.visible;
      this.showIcon = pEffectInstance.showIcon;
      this.curativeItems = pEffectInstance.curativeItems == null ? null : new java.util.ArrayList<net.minecraft.item.ItemStack>(pEffectInstance.curativeItems);
   }

   public boolean update(EffectInstance pOther) {
      if (this.effect != pOther.effect) {
         LOGGER.warn("This method should only be called for matching effects!");
      }

      boolean flag = false;
      if (pOther.amplifier > this.amplifier) {
         if (pOther.duration < this.duration) {
            EffectInstance effectinstance = this.hiddenEffect;
            this.hiddenEffect = new EffectInstance(this);
            this.hiddenEffect.hiddenEffect = effectinstance;
         }

         this.amplifier = pOther.amplifier;
         this.duration = pOther.duration;
         flag = true;
      } else if (pOther.duration > this.duration) {
         if (pOther.amplifier == this.amplifier) {
            this.duration = pOther.duration;
            flag = true;
         } else if (this.hiddenEffect == null) {
            this.hiddenEffect = new EffectInstance(pOther);
         } else {
            this.hiddenEffect.update(pOther);
         }
      }

      if (!pOther.ambient && this.ambient || flag) {
         this.ambient = pOther.ambient;
         flag = true;
      }

      if (pOther.visible != this.visible) {
         this.visible = pOther.visible;
         flag = true;
      }

      if (pOther.showIcon != this.showIcon) {
         this.showIcon = pOther.showIcon;
         flag = true;
      }

      return flag;
   }

   public Effect getEffect() {
      return this.effect == null ? null : this.effect.delegate.get();
   }

   public int getDuration() {
      return this.duration;
   }

   public int getAmplifier() {
      return this.amplifier;
   }

   /**
    * Gets whether this potion effect originated from a beacon
    */
   public boolean isAmbient() {
      return this.ambient;
   }

   /**
    * Gets whether this potion effect will show ambient particles or not.
    */
   public boolean isVisible() {
      return this.visible;
   }

   public boolean showIcon() {
      return this.showIcon;
   }

   public boolean tick(LivingEntity pEntity, Runnable pOnExpirationRunnable) {
      if (this.duration > 0) {
         if (this.effect.isDurationEffectTick(this.duration, this.amplifier)) {
            this.applyEffect(pEntity);
         }

         this.tickDownDuration();
         if (this.duration == 0 && this.hiddenEffect != null) {
            this.setDetailsFrom(this.hiddenEffect);
            this.hiddenEffect = this.hiddenEffect.hiddenEffect;
            pOnExpirationRunnable.run();
         }
      }

      return this.duration > 0;
   }

   private int tickDownDuration() {
      if (this.hiddenEffect != null) {
         this.hiddenEffect.tickDownDuration();
      }

      return --this.duration;
   }

   public void applyEffect(LivingEntity pEntity) {
      if (this.duration > 0) {
         this.effect.applyEffectTick(pEntity, this.amplifier);
      }

   }

   public String getDescriptionId() {
      return this.effect.getDescriptionId();
   }

   public String toString() {
      String s;
      if (this.amplifier > 0) {
         s = this.getDescriptionId() + " x " + (this.amplifier + 1) + ", Duration: " + this.duration;
      } else {
         s = this.getDescriptionId() + ", Duration: " + this.duration;
      }

      if (this.splash) {
         s = s + ", Splash: true";
      }

      if (!this.visible) {
         s = s + ", Particles: false";
      }

      if (!this.showIcon) {
         s = s + ", Show Icon: false";
      }

      return s;
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (!(p_equals_1_ instanceof EffectInstance)) {
         return false;
      } else {
         EffectInstance effectinstance = (EffectInstance)p_equals_1_;
         return this.duration == effectinstance.duration && this.amplifier == effectinstance.amplifier && this.splash == effectinstance.splash && this.ambient == effectinstance.ambient && this.effect.equals(effectinstance.effect);
      }
   }

   public int hashCode() {
      int i = this.effect.hashCode();
      i = 31 * i + this.duration;
      i = 31 * i + this.amplifier;
      i = 31 * i + (this.splash ? 1 : 0);
      return 31 * i + (this.ambient ? 1 : 0);
   }

   /**
    * Write a custom potion effect to a potion item's NBT data.
    */
   public CompoundNBT save(CompoundNBT pNbt) {
      pNbt.putByte("Id", (byte)Effect.getId(this.getEffect()));
      this.writeDetailsTo(pNbt);
      return pNbt;
   }

   private void writeDetailsTo(CompoundNBT pNbt) {
      pNbt.putByte("Amplifier", (byte)this.getAmplifier());
      pNbt.putInt("Duration", this.getDuration());
      pNbt.putBoolean("Ambient", this.isAmbient());
      pNbt.putBoolean("ShowParticles", this.isVisible());
      pNbt.putBoolean("ShowIcon", this.showIcon());
      if (this.hiddenEffect != null) {
         CompoundNBT compoundnbt = new CompoundNBT();
         this.hiddenEffect.save(compoundnbt);
         pNbt.put("HiddenEffect", compoundnbt);
      }
      writeCurativeItems(pNbt);

   }

   /**
    * Read a custom potion effect from a potion item's NBT data.
    */
   public static EffectInstance load(CompoundNBT pNbt) {
      int i = pNbt.getByte("Id") & 0xFF;
      Effect effect = Effect.byId(i);
      return effect == null ? null : loadSpecifiedEffect(effect, pNbt);
   }

   private static EffectInstance loadSpecifiedEffect(Effect pEffect, CompoundNBT pNbt) {
      int i = pNbt.getByte("Amplifier");
      int j = pNbt.getInt("Duration");
      boolean flag = pNbt.getBoolean("Ambient");
      boolean flag1 = true;
      if (pNbt.contains("ShowParticles", 1)) {
         flag1 = pNbt.getBoolean("ShowParticles");
      }

      boolean flag2 = flag1;
      if (pNbt.contains("ShowIcon", 1)) {
         flag2 = pNbt.getBoolean("ShowIcon");
      }

      EffectInstance effectinstance = null;
      if (pNbt.contains("HiddenEffect", 10)) {
         effectinstance = loadSpecifiedEffect(pEffect, pNbt.getCompound("HiddenEffect"));
      }

      return readCurativeItems(new EffectInstance(pEffect, j, i < 0 ? 0 : i, flag, flag1, flag2, effectinstance), pNbt);
   }

   /**
    * Toggle the isPotionDurationMax field.
    */
   @OnlyIn(Dist.CLIENT)
   public void setNoCounter(boolean pMaxDuration) {
      this.noCounter = pMaxDuration;
   }

   /**
    * Get the value of the isPotionDurationMax field.
    */
   @OnlyIn(Dist.CLIENT)
   public boolean isNoCounter() {
      return this.noCounter;
   }

   public int compareTo(EffectInstance p_compareTo_1_) {
      int i = 32147;
      return (this.getDuration() <= 32147 || p_compareTo_1_.getDuration() <= 32147) && (!this.isAmbient() || !p_compareTo_1_.isAmbient()) ? ComparisonChain.start().compare(this.isAmbient(), p_compareTo_1_.isAmbient()).compare(this.getDuration(), p_compareTo_1_.getDuration()).compare(this.getEffect().getGuiSortColor(this), p_compareTo_1_.getEffect().getGuiSortColor(this)).result() : ComparisonChain.start().compare(this.isAmbient(), p_compareTo_1_.isAmbient()).compare(this.getEffect().getGuiSortColor(this), p_compareTo_1_.getEffect().getGuiSortColor(this)).result();
   }

   //======================= FORGE START ===========================
   private java.util.List<net.minecraft.item.ItemStack> curativeItems;

   @Override
   public java.util.List<net.minecraft.item.ItemStack> getCurativeItems() {
      if (this.curativeItems == null) //Lazy load this so that we don't create a circular dep on Items.
         this.curativeItems = getEffect().getCurativeItems();
      return this.curativeItems;
   }
   @Override
   public void setCurativeItems(java.util.List<net.minecraft.item.ItemStack> curativeItems) {
      this.curativeItems = curativeItems;
   }
   private static EffectInstance readCurativeItems(EffectInstance effect, CompoundNBT nbt) {
      if (nbt.contains("CurativeItems", net.minecraftforge.common.util.Constants.NBT.TAG_LIST)) {
         java.util.List<net.minecraft.item.ItemStack> items = new java.util.ArrayList<net.minecraft.item.ItemStack>();
         net.minecraft.nbt.ListNBT list = nbt.getList("CurativeItems", net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND);
         for (int i = 0; i < list.size(); i++) {
            items.add(net.minecraft.item.ItemStack.of(list.getCompound(i)));
         }
         effect.setCurativeItems(items);
      }

      return effect;
   }
}
