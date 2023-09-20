package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

public class PlayerGeneratesContainerLootTrigger extends AbstractCriterionTrigger<PlayerGeneratesContainerLootTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("player_generates_container_loot");

   public ResourceLocation getId() {
      return ID;
   }

   protected PlayerGeneratesContainerLootTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getAsString(pJson, "loot_table"));
      return new PlayerGeneratesContainerLootTrigger.Instance(pEntityPredicate, resourcelocation);
   }

   public void trigger(ServerPlayerEntity pPlayer, ResourceLocation pGeneratedLoot) {
      this.trigger(pPlayer, (p_235477_1_) -> {
         return p_235477_1_.matches(pGeneratedLoot);
      });
   }

   public static class Instance extends CriterionInstance {
      private final ResourceLocation lootTable;

      public Instance(EntityPredicate.AndPredicate p_i231684_1_, ResourceLocation p_i231684_2_) {
         super(PlayerGeneratesContainerLootTrigger.ID, p_i231684_1_);
         this.lootTable = p_i231684_2_;
      }

      public static PlayerGeneratesContainerLootTrigger.Instance lootTableUsed(ResourceLocation pGeneratedLoot) {
         return new PlayerGeneratesContainerLootTrigger.Instance(EntityPredicate.AndPredicate.ANY, pGeneratedLoot);
      }

      public boolean matches(ResourceLocation pGeneratedLoot) {
         return this.lootTable.equals(pGeneratedLoot);
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.addProperty("loot_table", this.lootTable.toString());
         return jsonobject;
      }
   }
}