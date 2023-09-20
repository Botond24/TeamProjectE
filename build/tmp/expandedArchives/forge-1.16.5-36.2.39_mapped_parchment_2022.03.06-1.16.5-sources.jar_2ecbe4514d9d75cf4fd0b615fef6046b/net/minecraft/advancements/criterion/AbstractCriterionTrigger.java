package net.minecraft.advancements.criterion;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.advancements.ICriterionTrigger;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.LootContext;

public abstract class AbstractCriterionTrigger<T extends CriterionInstance> implements ICriterionTrigger<T> {
   private final Map<PlayerAdvancements, Set<ICriterionTrigger.Listener<T>>> players = Maps.newIdentityHashMap();

   public final void addPlayerListener(PlayerAdvancements pPlayerAdvancements, ICriterionTrigger.Listener<T> pListener) {
      this.players.computeIfAbsent(pPlayerAdvancements, (p_227072_0_) -> {
         return Sets.newHashSet();
      }).add(pListener);
   }

   public final void removePlayerListener(PlayerAdvancements pPlayerAdvancements, ICriterionTrigger.Listener<T> pListener) {
      Set<ICriterionTrigger.Listener<T>> set = this.players.get(pPlayerAdvancements);
      if (set != null) {
         set.remove(pListener);
         if (set.isEmpty()) {
            this.players.remove(pPlayerAdvancements);
         }
      }

   }

   public final void removePlayerListeners(PlayerAdvancements pPlayerAdvancements) {
      this.players.remove(pPlayerAdvancements);
   }

   protected abstract T createInstance(JsonObject pJson, EntityPredicate.AndPredicate pEntityPredicate, ConditionArrayParser pConditionsParser);

   public final T createInstance(JsonObject pObject, ConditionArrayParser pConditions) {
      EntityPredicate.AndPredicate entitypredicate$andpredicate = EntityPredicate.AndPredicate.fromJson(pObject, "player", pConditions);
      return this.createInstance(pObject, entitypredicate$andpredicate, pConditions);
   }

   protected void trigger(ServerPlayerEntity pServerPlayer, Predicate<T> pTestTrigger) {
      PlayerAdvancements playeradvancements = pServerPlayer.getAdvancements();
      Set<ICriterionTrigger.Listener<T>> set = this.players.get(playeradvancements);
      if (set != null && !set.isEmpty()) {
         LootContext lootcontext = EntityPredicate.createContext(pServerPlayer, pServerPlayer);
         List<ICriterionTrigger.Listener<T>> list = null;

         for(ICriterionTrigger.Listener<T> listener : set) {
            T t = listener.getTriggerInstance();
            if (t.getPlayerPredicate().matches(lootcontext) && pTestTrigger.test(t)) {
               if (list == null) {
                  list = Lists.newArrayList();
               }

               list.add(listener);
            }
         }

         if (list != null) {
            for(ICriterionTrigger.Listener<T> listener1 : list) {
               listener1.run(playeradvancements);
            }
         }

      }
   }
}