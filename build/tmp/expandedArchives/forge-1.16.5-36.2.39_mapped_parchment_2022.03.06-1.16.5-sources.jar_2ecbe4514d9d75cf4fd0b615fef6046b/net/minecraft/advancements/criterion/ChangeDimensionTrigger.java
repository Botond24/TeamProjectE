package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class ChangeDimensionTrigger extends AbstractCriterionTrigger<ChangeDimensionTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("changed_dimension");

   public ResourceLocation getId() {
      return ID;
   }

   public ChangeDimensionTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      RegistryKey<World> registrykey = pJson.has("from") ? RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(JSONUtils.getAsString(pJson, "from"))) : null;
      RegistryKey<World> registrykey1 = pJson.has("to") ? RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(JSONUtils.getAsString(pJson, "to"))) : null;
      return new ChangeDimensionTrigger.Instance(pEntityPredicate, registrykey, registrykey1);
   }

   public void trigger(ServerPlayerEntity pPlayer, RegistryKey<World> pFromLevel, RegistryKey<World> pToLevel) {
      this.trigger(pPlayer, (p_233550_2_) -> {
         return p_233550_2_.matches(pFromLevel, pToLevel);
      });
   }

   public static class Instance extends CriterionInstance {
      @Nullable
      private final RegistryKey<World> from;
      @Nullable
      private final RegistryKey<World> to;

      public Instance(EntityPredicate.AndPredicate p_i231488_1_, @Nullable RegistryKey<World> p_i231488_2_, @Nullable RegistryKey<World> p_i231488_3_) {
         super(ChangeDimensionTrigger.ID, p_i231488_1_);
         this.from = p_i231488_2_;
         this.to = p_i231488_3_;
      }

      public static ChangeDimensionTrigger.Instance changedDimensionTo(RegistryKey<World> pToLevel) {
         return new ChangeDimensionTrigger.Instance(EntityPredicate.AndPredicate.ANY, (RegistryKey<World>)null, pToLevel);
      }

      public boolean matches(RegistryKey<World> pFromLevel, RegistryKey<World> pToLevel) {
         if (this.from != null && this.from != pFromLevel) {
            return false;
         } else {
            return this.to == null || this.to == pToLevel;
         }
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         if (this.from != null) {
            jsonobject.addProperty("from", this.from.location().toString());
         }

         if (this.to != null) {
            jsonobject.addProperty("to", this.to.location().toString());
         }

         return jsonobject;
      }
   }
}