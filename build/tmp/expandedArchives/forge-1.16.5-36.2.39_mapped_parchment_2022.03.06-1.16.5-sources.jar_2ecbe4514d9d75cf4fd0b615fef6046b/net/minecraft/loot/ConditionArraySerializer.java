package net.minecraft.loot;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.loot.conditions.ILootCondition;

public class ConditionArraySerializer {
   public static final ConditionArraySerializer INSTANCE = new ConditionArraySerializer();
   private final Gson predicateGson = LootSerializers.createConditionSerializer().create();

   public final JsonElement serializeConditions(ILootCondition[] pConditions) {
      return this.predicateGson.toJsonTree(pConditions);
   }
}