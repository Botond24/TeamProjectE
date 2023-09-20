package net.minecraft.advancements.criterion;

import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.loot.LootContext;
import net.minecraft.util.ResourceLocation;

public class ChanneledLightningTrigger extends AbstractCriterionTrigger<ChanneledLightningTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("channeled_lightning");

   public ResourceLocation getId() {
      return ID;
   }

   public ChanneledLightningTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      EntityPredicate.AndPredicate[] aentitypredicate$andpredicate = EntityPredicate.AndPredicate.fromJsonArray(pJson, "victims", pConditionsParser);
      return new ChanneledLightningTrigger.Instance(pEntityPredicate, aentitypredicate$andpredicate);
   }

   public void trigger(ServerPlayerEntity pPlayer, Collection<? extends Entity> pEntityTriggered) {
      List<LootContext> list = pEntityTriggered.stream().map((p_233674_1_) -> {
         return EntityPredicate.createContext(pPlayer, p_233674_1_);
      }).collect(Collectors.toList());
      this.trigger(pPlayer, (p_233673_1_) -> {
         return p_233673_1_.matches(list);
      });
   }

   public static class Instance extends CriterionInstance {
      private final EntityPredicate.AndPredicate[] victims;

      public Instance(EntityPredicate.AndPredicate p_i231493_1_, EntityPredicate.AndPredicate[] p_i231493_2_) {
         super(ChanneledLightningTrigger.ID, p_i231493_1_);
         this.victims = p_i231493_2_;
      }

      public static ChanneledLightningTrigger.Instance channeledLightning(EntityPredicate... pVictims) {
         return new ChanneledLightningTrigger.Instance(EntityPredicate.AndPredicate.ANY, Stream.of(pVictims).map(EntityPredicate.AndPredicate::wrap).toArray((p_233675_0_) -> {
            return new EntityPredicate.AndPredicate[p_233675_0_];
         }));
      }

      public boolean matches(Collection<? extends LootContext> pVictims) {
         for(EntityPredicate.AndPredicate entitypredicate$andpredicate : this.victims) {
            boolean flag = false;

            for(LootContext lootcontext : pVictims) {
               if (entitypredicate$andpredicate.matches(lootcontext)) {
                  flag = true;
                  break;
               }
            }

            if (!flag) {
               return false;
            }
         }

         return true;
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("victims", EntityPredicate.AndPredicate.toJson(this.victims, pConditions));
         return jsonobject;
      }
   }
}