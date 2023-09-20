package net.minecraft.advancements.criterion;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.loot.LootContext;
import net.minecraft.util.ResourceLocation;

public class KilledByCrossbowTrigger extends AbstractCriterionTrigger<KilledByCrossbowTrigger.Instance> {
   private static final ResourceLocation ID = new ResourceLocation("killed_by_crossbow");

   public ResourceLocation getId() {
      return ID;
   }

   public KilledByCrossbowTrigger.Instance createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser) {
      EntityPredicate.AndPredicate[] aentitypredicate$andpredicate = EntityPredicate.AndPredicate.fromJsonArray(pJson, "victims", pConditionsParser);
      MinMaxBounds.IntBound minmaxbounds$intbound = MinMaxBounds.IntBound.fromJson(pJson.get("unique_entity_types"));
      return new KilledByCrossbowTrigger.Instance(pEntityPredicate, aentitypredicate$andpredicate, minmaxbounds$intbound);
   }

   public void trigger(ServerPlayerEntity pPlayer, Collection<Entity> pEntities) {
      List<LootContext> list = Lists.newArrayList();
      Set<EntityType<?>> set = Sets.newHashSet();

      for(Entity entity : pEntities) {
         set.add(entity.getType());
         list.add(EntityPredicate.createContext(pPlayer, entity));
      }

      this.trigger(pPlayer, (p_234940_2_) -> {
         return p_234940_2_.matches(list, set.size());
      });
   }

   public static class Instance extends CriterionInstance {
      private final EntityPredicate.AndPredicate[] victims;
      private final MinMaxBounds.IntBound uniqueEntityTypes;

      public Instance(EntityPredicate.AndPredicate p_i231619_1_, EntityPredicate.AndPredicate[] p_i231619_2_, MinMaxBounds.IntBound p_i231619_3_) {
         super(KilledByCrossbowTrigger.ID, p_i231619_1_);
         this.victims = p_i231619_2_;
         this.uniqueEntityTypes = p_i231619_3_;
      }

      public static KilledByCrossbowTrigger.Instance crossbowKilled(EntityPredicate.Builder... pBuilders) {
         EntityPredicate.AndPredicate[] aentitypredicate$andpredicate = new EntityPredicate.AndPredicate[pBuilders.length];

         for(int i = 0; i < pBuilders.length; ++i) {
            EntityPredicate.Builder entitypredicate$builder = pBuilders[i];
            aentitypredicate$andpredicate[i] = EntityPredicate.AndPredicate.wrap(entitypredicate$builder.build());
         }

         return new KilledByCrossbowTrigger.Instance(EntityPredicate.AndPredicate.ANY, aentitypredicate$andpredicate, MinMaxBounds.IntBound.ANY);
      }

      public static KilledByCrossbowTrigger.Instance crossbowKilled(MinMaxBounds.IntBound pBounds) {
         EntityPredicate.AndPredicate[] aentitypredicate$andpredicate = new EntityPredicate.AndPredicate[0];
         return new KilledByCrossbowTrigger.Instance(EntityPredicate.AndPredicate.ANY, aentitypredicate$andpredicate, pBounds);
      }

      public boolean matches(Collection<LootContext> pContexts, int pBounds) {
         if (this.victims.length > 0) {
            List<LootContext> list = Lists.newArrayList(pContexts);

            for(EntityPredicate.AndPredicate entitypredicate$andpredicate : this.victims) {
               boolean flag = false;
               Iterator<LootContext> iterator = list.iterator();

               while(iterator.hasNext()) {
                  LootContext lootcontext = iterator.next();
                  if (entitypredicate$andpredicate.matches(lootcontext)) {
                     iterator.remove();
                     flag = true;
                     break;
                  }
               }

               if (!flag) {
                  return false;
               }
            }
         }

         return this.uniqueEntityTypes.matches(pBounds);
      }

      public JsonObject serializeToJson(ConditionArraySerializer pConditions) {
         JsonObject jsonobject = super.serializeToJson(pConditions);
         jsonobject.add("victims", EntityPredicate.AndPredicate.toJson(this.victims, pConditions));
         jsonobject.add("unique_entity_types", this.uniqueEntityTypes.serializeToJson());
         return jsonobject;
      }
   }
}