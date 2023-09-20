package net.minecraft.advancements.criterion;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public class MobEffectsPredicate {
   public static final MobEffectsPredicate ANY = new MobEffectsPredicate(Collections.emptyMap());
   private final Map<Effect, MobEffectsPredicate.InstancePredicate> effects;

   public MobEffectsPredicate(Map<Effect, MobEffectsPredicate.InstancePredicate> p_i47538_1_) {
      this.effects = p_i47538_1_;
   }

   public static MobEffectsPredicate effects() {
      return new MobEffectsPredicate(Maps.newLinkedHashMap());
   }

   public MobEffectsPredicate and(Effect pEffect) {
      this.effects.put(pEffect, new MobEffectsPredicate.InstancePredicate());
      return this;
   }

   public boolean matches(Entity pEntity) {
      if (this == ANY) {
         return true;
      } else {
         return pEntity instanceof LivingEntity ? this.matches(((LivingEntity)pEntity).getActiveEffectsMap()) : false;
      }
   }

   public boolean matches(LivingEntity pEntity) {
      return this == ANY ? true : this.matches(pEntity.getActiveEffectsMap());
   }

   public boolean matches(Map<Effect, EffectInstance> pPotions) {
      if (this == ANY) {
         return true;
      } else {
         for(Entry<Effect, MobEffectsPredicate.InstancePredicate> entry : this.effects.entrySet()) {
            EffectInstance effectinstance = pPotions.get(entry.getKey());
            if (!entry.getValue().matches(effectinstance)) {
               return false;
            }
         }

         return true;
      }
   }

   public static MobEffectsPredicate fromJson(@Nullable JsonElement pElement) {
      if (pElement != null && !pElement.isJsonNull()) {
         JsonObject jsonobject = JSONUtils.convertToJsonObject(pElement, "effects");
         Map<Effect, MobEffectsPredicate.InstancePredicate> map = Maps.newLinkedHashMap();

         for(Entry<String, JsonElement> entry : jsonobject.entrySet()) {
            ResourceLocation resourcelocation = new ResourceLocation(entry.getKey());
            Effect effect = Registry.MOB_EFFECT.getOptional(resourcelocation).orElseThrow(() -> {
               return new JsonSyntaxException("Unknown effect '" + resourcelocation + "'");
            });
            MobEffectsPredicate.InstancePredicate mobeffectspredicate$instancepredicate = MobEffectsPredicate.InstancePredicate.fromJson(JSONUtils.convertToJsonObject(entry.getValue(), entry.getKey()));
            map.put(effect, mobeffectspredicate$instancepredicate);
         }

         return new MobEffectsPredicate(map);
      } else {
         return ANY;
      }
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();

         for(Entry<Effect, MobEffectsPredicate.InstancePredicate> entry : this.effects.entrySet()) {
            jsonobject.add(Registry.MOB_EFFECT.getKey(entry.getKey()).toString(), entry.getValue().serializeToJson());
         }

         return jsonobject;
      }
   }

   public static class InstancePredicate {
      private final MinMaxBounds.IntBound amplifier;
      private final MinMaxBounds.IntBound duration;
      @Nullable
      private final Boolean ambient;
      @Nullable
      private final Boolean visible;

      public InstancePredicate(MinMaxBounds.IntBound p_i49709_1_, MinMaxBounds.IntBound p_i49709_2_, @Nullable Boolean p_i49709_3_, @Nullable Boolean p_i49709_4_) {
         this.amplifier = p_i49709_1_;
         this.duration = p_i49709_2_;
         this.ambient = p_i49709_3_;
         this.visible = p_i49709_4_;
      }

      public InstancePredicate() {
         this(MinMaxBounds.IntBound.ANY, MinMaxBounds.IntBound.ANY, (Boolean)null, (Boolean)null);
      }

      public boolean matches(@Nullable EffectInstance pEffect) {
         if (pEffect == null) {
            return false;
         } else if (!this.amplifier.matches(pEffect.getAmplifier())) {
            return false;
         } else if (!this.duration.matches(pEffect.getDuration())) {
            return false;
         } else if (this.ambient != null && this.ambient != pEffect.isAmbient()) {
            return false;
         } else {
            return this.visible == null || this.visible == pEffect.isVisible();
         }
      }

      public JsonElement serializeToJson() {
         JsonObject jsonobject = new JsonObject();
         jsonobject.add("amplifier", this.amplifier.serializeToJson());
         jsonobject.add("duration", this.duration.serializeToJson());
         jsonobject.addProperty("ambient", this.ambient);
         jsonobject.addProperty("visible", this.visible);
         return jsonobject;
      }

      public static MobEffectsPredicate.InstancePredicate fromJson(JsonObject pObject) {
         MinMaxBounds.IntBound minmaxbounds$intbound = MinMaxBounds.IntBound.fromJson(pObject.get("amplifier"));
         MinMaxBounds.IntBound minmaxbounds$intbound1 = MinMaxBounds.IntBound.fromJson(pObject.get("duration"));
         Boolean obool = pObject.has("ambient") ? JSONUtils.getAsBoolean(pObject, "ambient") : null;
         Boolean obool1 = pObject.has("visible") ? JSONUtils.getAsBoolean(pObject, "visible") : null;
         return new MobEffectsPredicate.InstancePredicate(minmaxbounds$intbound, minmaxbounds$intbound1, obool, obool1);
      }
   }
}