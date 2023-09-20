package net.minecraft.advancements;

import com.google.gson.JsonObject;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.util.ResourceLocation;

public interface ICriterionTrigger<T extends ICriterionInstance> {
   ResourceLocation getId();

   void addPlayerListener(PlayerAdvancements pPlayerAdvancements, ICriterionTrigger.Listener<T> pListener);

   void removePlayerListener(PlayerAdvancements pPlayerAdvancements, ICriterionTrigger.Listener<T> pListener);

   void removePlayerListeners(PlayerAdvancements pPlayerAdvancements);

   T createInstance(JsonObject pObject, ConditionArrayParser pConditions);

   public static class Listener<T extends ICriterionInstance> {
      private final T trigger;
      private final Advancement advancement;
      private final String criterion;

      public Listener(T pTrigger, Advancement pAdvancement, String pCriterion) {
         this.trigger = pTrigger;
         this.advancement = pAdvancement;
         this.criterion = pCriterion;
      }

      public T getTriggerInstance() {
         return this.trigger;
      }

      public void run(PlayerAdvancements pPlayerAdvancements) {
         pPlayerAdvancements.award(this.advancement, this.criterion);
      }

      public boolean equals(Object p_equals_1_) {
         if (this == p_equals_1_) {
            return true;
         } else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
            ICriterionTrigger.Listener<?> listener = (ICriterionTrigger.Listener)p_equals_1_;
            if (!this.trigger.equals(listener.trigger)) {
               return false;
            } else {
               return !this.advancement.equals(listener.advancement) ? false : this.criterion.equals(listener.criterion);
            }
         } else {
            return false;
         }
      }

      public int hashCode() {
         int i = this.trigger.hashCode();
         i = 31 * i + this.advancement.hashCode();
         return 31 * i + this.criterion.hashCode();
      }
   }
}