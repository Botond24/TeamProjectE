package net.minecraft.loot;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;

/**
 * A LootContextParamSet defines a set of required and optional {@link LootContextParam}s.
 * This is used to validate that conditions, functions and other {@link LootContextUser}s only use those parameters that
 * are present for the given loot table.
 * 
 * @see LootContextParamSets
 * @see ValidationContext
 */
public class LootParameterSet {
   private final Set<LootParameter<?>> required;
   private final Set<LootParameter<?>> all;

   private LootParameterSet(Set<LootParameter<?>> pRequired, Set<LootParameter<?>> pOptional) {
      this.required = ImmutableSet.copyOf(pRequired);
      this.all = ImmutableSet.copyOf(Sets.union(pRequired, pOptional));
   }

   /**
    * Gets only the required parameters
    */
   public Set<LootParameter<?>> getRequired() {
      return this.required;
   }

   /**
    * Gets the required and optional parameters
    */
   public Set<LootParameter<?>> getAllowed() {
      return this.all;
   }

   public String toString() {
      return "[" + Joiner.on(", ").join(this.all.stream().map((p_216275_1_) -> {
         return (this.required.contains(p_216275_1_) ? "!" : "") + p_216275_1_.getName();
      }).iterator()) + "]";
   }

   /**
    * Validate that all parameters referenced by the given LootContextUser are present in this set.
    */
   public void validateUser(ValidationTracker pValidationContext, IParameterized pLootContextUser) {
      Set<LootParameter<?>> set = pLootContextUser.getReferencedContextParams();
      Set<LootParameter<?>> set1 = Sets.difference(set, this.all);
      if (!set1.isEmpty()) {
         pValidationContext.reportProblem("Parameters " + set1 + " are not provided in this context");
      }

   }

   public static class Builder {
      private final Set<LootParameter<?>> required = Sets.newIdentityHashSet();
      private final Set<LootParameter<?>> optional = Sets.newIdentityHashSet();

      public LootParameterSet.Builder required(LootParameter<?> pParameter) {
         if (this.optional.contains(pParameter)) {
            throw new IllegalArgumentException("Parameter " + pParameter.getName() + " is already optional");
         } else {
            this.required.add(pParameter);
            return this;
         }
      }

      public LootParameterSet.Builder optional(LootParameter<?> pParameter) {
         if (this.required.contains(pParameter)) {
            throw new IllegalArgumentException("Parameter " + pParameter.getName() + " is already required");
         } else {
            this.optional.add(pParameter);
            return this;
         }
      }

      public LootParameterSet build() {
         return new LootParameterSet(this.required, this.optional);
      }
   }
}