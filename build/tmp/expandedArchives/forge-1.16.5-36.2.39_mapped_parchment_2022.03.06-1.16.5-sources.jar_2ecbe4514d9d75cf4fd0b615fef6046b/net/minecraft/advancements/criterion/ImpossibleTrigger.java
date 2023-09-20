package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;

public class ImpossibleTrigger implements ICriterionTrigger<ImpossibleTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("impossible");

   public ResourceLocation getId() {
      return ID;
   }

   public void addPlayerListener(PlayerAdvancements pPlayerAdvancements, ICriterionTrigger.Listener<ImpossibleTrigger.Instance> pListener) {
   }

   public void removePlayerListener(PlayerAdvancements pPlayerAdvancements, ICriterionTrigger.Listener<ImpossibleTrigger.Instance> pListener) {
   }

   public void removePlayerListeners(PlayerAdvancements pPlayerAdvancements) {
   }

   public ImpossibleTrigger.Instance createInstance(JsonObject pObject, ConditionArrayParser pConditions) {
      return new ImpossibleTrigger.Instance();
   }

   public static class Instance implements ICriterionInstance {
      public ResourceLocation getCriterion() {
         return ImpossibleTrigger.ID;
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         return new JsonObject();
      }
   }
}