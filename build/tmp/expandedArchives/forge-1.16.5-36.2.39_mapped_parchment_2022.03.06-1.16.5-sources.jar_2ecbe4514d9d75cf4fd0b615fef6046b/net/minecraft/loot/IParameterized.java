package net.minecraft.loot;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

/**
 * An object that will use some parameters from a LootContext. Used for validation purposes to validate that the correct
 * parameters are present.
 */
public interface IParameterized {
   /**
    * Get the parameters used by this object.
    */
   default Set<LootParameter<?>> getReferencedContextParams() {
      return ImmutableSet.of();
   }

   /**
    * Validate that this object is used correctly according to the given ValidationContext.
    */
   default void validate(ValidationTracker pContext) {
      pContext.validateUser(this);
   }
}