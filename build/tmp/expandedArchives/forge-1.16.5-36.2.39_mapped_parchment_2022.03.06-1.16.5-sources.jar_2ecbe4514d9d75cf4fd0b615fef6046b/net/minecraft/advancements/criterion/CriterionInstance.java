package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;

public abstract class CriterionInstance implements ICriterionInstance {
   private final ResourceLocation criterion;
   private final EntityPredicate.AndPredicate player;

   public CriterionInstance(ResourceLocation pCriterion, EntityPredicate.AndPredicate pPlayer) {
      this.criterion = pCriterion;
      this.player = pPlayer;
   }

   public ResourceLocation getCriterion() {
      return this.criterion;
   }

   protected EntityPredicate.AndPredicate getPlayerPredicate() {
      return this.player;
   }

   public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
      JsonObject jsonobject = new JsonObject();
      jsonobject.add("player", this.player.toJson(pConditions));
      return jsonobject;
   }

   public String toString() {
      return "AbstractCriterionInstance{criterion=" + this.criterion + '}';
   }
}