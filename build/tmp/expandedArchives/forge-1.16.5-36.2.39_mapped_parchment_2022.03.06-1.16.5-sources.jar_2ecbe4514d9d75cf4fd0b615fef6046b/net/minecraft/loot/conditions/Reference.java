package net.minecraft.loot.conditions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.ValidationTracker;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A LootItemCondition that refers to another LootItemCondition by its ID.
 */
public class Reference implements ILootCondition {
   private static final Logger LOGGER = LogManager.getLogger();
   private final ResourceLocation name;

   private Reference(ResourceLocation pName) {
      this.name = pName;
   }

   public LootConditionType getType() {
      return LootConditionManager.REFERENCE;
   }

   /**
    * Validate that this object is used correctly according to the given ValidationContext.
    */
   public void validate(ValidationTracker pContext) {
      if (pContext.hasVisitedCondition(this.name)) {
         pContext.reportProblem("Condition " + this.name + " is recursively called");
      } else {
         ILootCondition.super.validate(pContext);
         ILootCondition ilootcondition = pContext.resolveCondition(this.name);
         if (ilootcondition == null) {
            pContext.reportProblem("Unknown condition table called " + this.name);
         } else {
            ilootcondition.validate(pContext.enterTable(".{" + this.name + "}", this.name));
         }

      }
   }

   public boolean test(LootContext p_test_1_) {
      ILootCondition ilootcondition = p_test_1_.getCondition(this.name);
      if (p_test_1_.addVisitedCondition(ilootcondition)) {
         boolean flag;
         try {
            flag = ilootcondition.test(p_test_1_);
         } finally {
            p_test_1_.removeVisitedCondition(ilootcondition);
         }

         return flag;
      } else {
         LOGGER.warn("Detected infinite loop in loot tables");
         return false;
      }
   }

   public static class Serializer implements ILootSerializer<Reference> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, Reference pValue, JsonSerializationContext pSerializationContext) {
         pJson.addProperty("name", pValue.name.toString());
      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public Reference deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext) {
         ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getAsString(pJson, "name"));
         return new Reference(resourcelocation);
      }
   }
}