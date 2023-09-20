package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.loot.LootContext;
import net.minecraft.util.ResourceLocation;

public class CuredZombieVillagerTrigger extends AbstractCriterionTrigger<CuredZombieVillagerTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("cured_zombie_villager");

   public ResourceLocation getId() {
      return ID;
   }

   public CuredZombieVillagerTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      EntityPredicate.AndPredicate entitypredicate$andpredicate = EntityPredicate.AndPredicate.fromJson(pJson, "zombie", pConditionsParser);
      EntityPredicate.AndPredicate entitypredicate$andpredicate1 = EntityPredicate.AndPredicate.fromJson(pJson, "villager", pConditionsParser);
      return new CuredZombieVillagerTrigger.Instance(pEntityPredicate, entitypredicate$andpredicate, entitypredicate$andpredicate1);
   }

   public void trigger(ServerPlayerEntity pPlayer, ZombieEntity pZombie, VillagerEntity pVillager) {
      LootContext lootcontext = EntityPredicate.createContext(pPlayer, pZombie);
      LootContext lootcontext1 = EntityPredicate.createContext(pPlayer, pVillager);
      this.trigger(pPlayer, (p_233969_2_) -> {
         return p_233969_2_.matches(lootcontext, lootcontext1);
      });
   }

   public static class Instance extends CriterionInstance {
      private final EntityPredicate.AndPredicate zombie;
      private final EntityPredicate.AndPredicate villager;

      public Instance(EntityPredicate.AndPredicate p_i231535_1_, EntityPredicate.AndPredicate p_i231535_2_, EntityPredicate.AndPredicate p_i231535_3_) {
         super(CuredZombieVillagerTrigger.ID, p_i231535_1_);
         this.zombie = p_i231535_2_;
         this.villager = p_i231535_3_;
      }

      public static CuredZombieVillagerTrigger.Instance curedZombieVillager() {
         return new CuredZombieVillagerTrigger.Instance(EntityPredicate.AndPredicate.ANY, EntityPredicate.AndPredicate.ANY, EntityPredicate.AndPredicate.ANY);
      }

      public boolean matches(LootContext pZombie, LootContext pVillager) {
         if (!this.zombie.matches(pZombie)) {
            return false;
         } else {
            return this.villager.matches(pVillager);
         }
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("zombie", this.zombie.toJson(pConditions));
         jsonobject.add("villager", this.villager.toJson(pConditions));
         return jsonobject;
      }
   }
}