package net.minecraft.loot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.conditions.LootConditionManager;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * DataPack reload listener that reads loot conditions from the ResourceManager and stores them.
 * 
 * @see LootItemCondition
 */
public class LootPredicateManager extends JsonReloadListener {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON = LootSerializers.createConditionSerializer().create();
   private Map<ResourceLocation, ILootCondition> conditions = ImmutableMap.of();

   public LootPredicateManager() {
      super(GSON, "predicates");
   }

   /**
    * Get the LootItemCondition with the given ID. Returns null if no such condition exists.
    */
   @Nullable
   public ILootCondition get(ResourceLocation pId) {
      return this.conditions.get(pId);
   }

   protected void apply(Map<ResourceLocation, JsonElement> pObject, IResourceManager pResourceManager, IProfiler pProfiler) {
      Builder<ResourceLocation, ILootCondition> builder = ImmutableMap.builder();
      pObject.forEach((p_237404_1_, p_237404_2_) -> {
         try {
            if (p_237404_2_.isJsonArray()) {
               ILootCondition[] ailootcondition = GSON.fromJson(p_237404_2_, ILootCondition[].class);
               builder.put(p_237404_1_, new LootPredicateManager.AndCombiner(ailootcondition));
            } else {
               ILootCondition ilootcondition = GSON.fromJson(p_237404_2_, ILootCondition.class);
               builder.put(p_237404_1_, ilootcondition);
            }
         } catch (Exception exception) {
            LOGGER.error("Couldn't parse loot table {}", p_237404_1_, exception);
         }

      });
      Map<ResourceLocation, ILootCondition> map = builder.build();
      ValidationTracker validationtracker = new ValidationTracker(LootParameterSets.ALL_PARAMS, map::get, (p_227518_0_) -> {
         return null;
      });
      map.forEach((p_227515_1_, p_227515_2_) -> {
         p_227515_2_.validate(validationtracker.enterCondition("{" + p_227515_1_ + "}", p_227515_1_));
      });
      validationtracker.getProblems().forEach((p_227516_0_, p_227516_1_) -> {
         LOGGER.warn("Found validation problem in " + p_227516_0_ + ": " + p_227516_1_);
      });
      this.conditions = map;
   }

   /**
    * Get all known condition IDs.
    */
   public Set<ResourceLocation> getKeys() {
      return Collections.unmodifiableSet(this.conditions.keySet());
   }

   static class AndCombiner implements ILootCondition {
      private final ILootCondition[] terms;
      private final Predicate<LootContext> composedPredicate;

      private AndCombiner(ILootCondition[] pTerms) {
         this.terms = pTerms;
         this.composedPredicate = LootConditionManager.andConditions(pTerms);
      }

      public final boolean test(LootContext p_test_1_) {
         return this.composedPredicate.test(p_test_1_);
      }

      /**
       * Validate that this object is used correctly according to the given ValidationContext.
       */
      public void validate(ValidationTracker pContext) {
         ILootCondition.super.validate(pContext);

         for(int i = 0; i < this.terms.length; ++i) {
            this.terms[i].validate(pContext.forChild(".term[" + i + "]"));
         }

      }

      public LootConditionType getType() {
         throw new UnsupportedOperationException();
      }
   }
}