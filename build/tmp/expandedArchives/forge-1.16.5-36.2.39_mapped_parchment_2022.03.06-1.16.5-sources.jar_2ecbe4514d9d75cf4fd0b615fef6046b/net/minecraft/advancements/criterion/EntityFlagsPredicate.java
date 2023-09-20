package net.minecraft.advancements.criterion;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.JSONUtils;

public class EntityFlagsPredicate {
   public static final EntityFlagsPredicate ANY = (new EntityFlagsPredicate.Builder()).build();
   @Nullable
   private final Boolean isOnFire;
   @Nullable
   private final Boolean isCrouching;
   @Nullable
   private final Boolean isSprinting;
   @Nullable
   private final Boolean isSwimming;
   @Nullable
   private final Boolean isBaby;

   public EntityFlagsPredicate(@Nullable Boolean p_i50808_1_, @Nullable Boolean p_i50808_2_, @Nullable Boolean p_i50808_3_, @Nullable Boolean p_i50808_4_, @Nullable Boolean p_i50808_5_) {
      this.isOnFire = p_i50808_1_;
      this.isCrouching = p_i50808_2_;
      this.isSprinting = p_i50808_3_;
      this.isSwimming = p_i50808_4_;
      this.isBaby = p_i50808_5_;
   }

   public boolean matches(Entity pEntity) {
      if (this.isOnFire != null && pEntity.isOnFire() != this.isOnFire) {
         return false;
      } else if (this.isCrouching != null && pEntity.isCrouching() != this.isCrouching) {
         return false;
      } else if (this.isSprinting != null && pEntity.isSprinting() != this.isSprinting) {
         return false;
      } else if (this.isSwimming != null && pEntity.isSwimming() != this.isSwimming) {
         return false;
      } else {
         return this.isBaby == null || !(pEntity instanceof LivingEntity) || ((LivingEntity)pEntity).isBaby() == this.isBaby;
      }
   }

   @Nullable
   private static Boolean getOptionalBoolean(JsonObject pJsonObject, String pName) {
      return pJsonObject.has(pName) ? JSONUtils.getAsBoolean(pJsonObject, pName) : null;
   }

   public static EntityFlagsPredicate fromJson(@Nullable JsonElement pElement) {
      if (pElement != null && !pElement.isJsonNull()) {
         JsonObject jsonobject = JSONUtils.convertToJsonObject(pElement, "entity flags");
         Boolean obool = getOptionalBoolean(jsonobject, "is_on_fire");
         Boolean obool1 = getOptionalBoolean(jsonobject, "is_sneaking");
         Boolean obool2 = getOptionalBoolean(jsonobject, "is_sprinting");
         Boolean obool3 = getOptionalBoolean(jsonobject, "is_swimming");
         Boolean obool4 = getOptionalBoolean(jsonobject, "is_baby");
         return new EntityFlagsPredicate(obool, obool1, obool2, obool3, obool4);
      } else {
         return ANY;
      }
   }

   private void addOptionalBoolean(JsonObject pJsonObject, String pName, @Nullable Boolean pBool) {
      if (pBool != null) {
         pJsonObject.addProperty(pName, pBool);
      }

   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         this.addOptionalBoolean(jsonobject, "is_on_fire", this.isOnFire);
         this.addOptionalBoolean(jsonobject, "is_sneaking", this.isCrouching);
         this.addOptionalBoolean(jsonobject, "is_sprinting", this.isSprinting);
         this.addOptionalBoolean(jsonobject, "is_swimming", this.isSwimming);
         this.addOptionalBoolean(jsonobject, "is_baby", this.isBaby);
         return jsonobject;
      }
   }

   public static class Builder {
      @Nullable
      private Boolean isOnFire;
      @Nullable
      private Boolean isCrouching;
      @Nullable
      private Boolean isSprinting;
      @Nullable
      private Boolean isSwimming;
      @Nullable
      private Boolean isBaby;

      public static EntityFlagsPredicate.Builder flags() {
         return new EntityFlagsPredicate.Builder();
      }

      public EntityFlagsPredicate.Builder setOnFire(@Nullable Boolean pOnFire) {
         this.isOnFire = pOnFire;
         return this;
      }

      public EntityFlagsPredicate.Builder setIsBaby(@Nullable Boolean pBaby) {
         this.isBaby = pBaby;
         return this;
      }

      public EntityFlagsPredicate build() {
         return new EntityFlagsPredicate(this.isOnFire, this.isCrouching, this.isSprinting, this.isSwimming, this.isBaby);
      }
   }
}