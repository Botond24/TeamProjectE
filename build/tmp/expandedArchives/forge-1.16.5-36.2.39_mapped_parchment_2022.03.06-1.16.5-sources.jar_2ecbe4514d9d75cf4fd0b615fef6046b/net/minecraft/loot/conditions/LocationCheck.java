package net.minecraft.loot.conditions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

/**
 * LootItemCondition that checks the {@link LootContextParams.ORIGIN} position against a {@link LocationPredicate} after
 * applying an offset to the origin position.
 */
public class LocationCheck implements ILootCondition {
   private final LocationPredicate predicate;
   private final BlockPos offset;

   private LocationCheck(LocationPredicate pLocationPredicate, BlockPos pOffset) {
      this.predicate = pLocationPredicate;
      this.offset = pOffset;
   }

   public LootConditionType getType() {
      return LootConditionManager.LOCATION_CHECK;
   }

   public boolean test(LootContext p_test_1_) {
      Vector3d vector3d = p_test_1_.getParamOrNull(LootParameters.ORIGIN);
      return vector3d != null && this.predicate.matches(p_test_1_.getLevel(), vector3d.x() + (double)this.offset.getX(), vector3d.y() + (double)this.offset.getY(), vector3d.z() + (double)this.offset.getZ());
   }

   public static ILootCondition.IBuilder checkLocation(LocationPredicate.Builder pLocationPredicateBuilder) {
      return () -> {
         return new LocationCheck(pLocationPredicateBuilder.build(), BlockPos.ZERO);
      };
   }

   public static ILootCondition.IBuilder checkLocation(LocationPredicate.Builder pLocationPredicateBuilder, BlockPos pOffset) {
      return () -> {
         return new LocationCheck(pLocationPredicateBuilder.build(), pOffset);
      };
   }

   public static class Serializer implements ILootSerializer<LocationCheck> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, LocationCheck pValue, JsonSerializationContext pSerializationContext) {
         pJson.add("predicate", pValue.predicate.serializeToJson());
         if (pValue.offset.getX() != 0) {
            pJson.addProperty("offsetX", pValue.offset.getX());
         }

         if (pValue.offset.getY() != 0) {
            pJson.addProperty("offsetY", pValue.offset.getY());
         }

         if (pValue.offset.getZ() != 0) {
            pJson.addProperty("offsetZ", pValue.offset.getZ());
         }

      }

      /**
       * Deserialize a value by reading it from the JsonObject.
       */
      public LocationCheck deserialize(JsonObject pJson, JsonDeserializationContext pSerializationContext) {
         LocationPredicate locationpredicate = LocationPredicate.fromJson(pJson.get("predicate"));
         int i = JSONUtils.getAsInt(pJson, "offsetX", 0);
         int j = JSONUtils.getAsInt(pJson, "offsetY", 0);
         int k = JSONUtils.getAsInt(pJson, "offsetZ", 0);
         return new LocationCheck(locationpredicate, new BlockPos(i, j, k));
      }
   }
}