package net.minecraft.loot;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Base interface for loot pool entry containers.
 * A loot pool entry container holds one or more loot pools and will expand into those.
 * Additionally, the container can either succeed or fail, based on its conditions.
 */
@FunctionalInterface
interface ILootEntry {
   /** A container which always fails. */
   ILootEntry ALWAYS_FALSE = (p_216134_0_, p_216134_1_) -> {
      return false;
   };
   /** A container that always succeeds. */
   ILootEntry ALWAYS_TRUE = (p_216136_0_, p_216136_1_) -> {
      return true;
   };

   boolean expand(LootContext p_expand_1_, Consumer<ILootGenerator> p_expand_2_);

   default ILootEntry and(ILootEntry pEntry) {
      Objects.requireNonNull(pEntry);
      return (p_216137_2_, p_216137_3_) -> {
         return this.expand(p_216137_2_, p_216137_3_) && pEntry.expand(p_216137_2_, p_216137_3_);
      };
   }

   default ILootEntry or(ILootEntry pEntry) {
      Objects.requireNonNull(pEntry);
      return (p_216138_2_, p_216138_3_) -> {
         return this.expand(p_216138_2_, p_216138_3_) || pEntry.expand(p_216138_2_, p_216138_3_);
      };
   }
}