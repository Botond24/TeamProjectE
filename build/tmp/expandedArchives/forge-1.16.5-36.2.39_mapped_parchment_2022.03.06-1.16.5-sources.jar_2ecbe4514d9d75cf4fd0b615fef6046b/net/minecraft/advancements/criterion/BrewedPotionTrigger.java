package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.potion.Potion;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public class BrewedPotionTrigger extends AbstractCriterionTrigger<BrewedPotionTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("brewed_potion");

   public ResourceLocation getId() {
      return ID;
   }

   public BrewedPotionTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      Potion potion = null;
      if (pJson.has("potion")) {
         ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getAsString(pJson, "potion"));
         potion = Registry.POTION.getOptional(resourcelocation).orElseThrow(() -> {
            return new JsonSyntaxException("Unknown potion '" + resourcelocation + "'");
         });
      }

      return new BrewedPotionTrigger.Instance(pEntityPredicate, potion);
   }

   public void trigger(ServerPlayerEntity pPlayer, Potion pPotion) {
      this.trigger(pPlayer, (p_226301_1_) -> {
         return p_226301_1_.matches(pPotion);
      });
   }

   public static class Instance extends CriterionInstance {
      private final Potion potion;

      public Instance(EntityPredicate.AndPredicate p_i231487_1_, @Nullable Potion p_i231487_2_) {
         super(BrewedPotionTrigger.ID, p_i231487_1_);
         this.potion = p_i231487_2_;
      }

      public static BrewedPotionTrigger.Instance brewedPotion() {
         return new BrewedPotionTrigger.Instance(EntityPredicate.AndPredicate.ANY, (Potion)null);
      }

      public boolean matches(Potion pPotion) {
         return this.potion == null || this.potion == pPotion;
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         if (this.potion != null) {
            jsonobject.addProperty("potion", Registry.POTION.getKey(this.potion).toString());
         }

         return jsonobject;
      }
   }
}