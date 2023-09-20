package net.minecraft.loot;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.loot.conditions.ILootCondition;
import org.apache.commons.lang3.ArrayUtils;

/**
 * A composite loot pool entry container that expands all its children in order until one of them succeeds.
 * This container succeeds if one of its children succeeds.
 */
public class AlternativesLootEntry extends ParentedLootEntry {
   AlternativesLootEntry(LootEntry[] p_i51263_1_, ILootCondition[] p_i51263_2_) {
      super(p_i51263_1_, p_i51263_2_);
   }

   public LootPoolEntryType getType() {
      return LootEntryManager.ALTERNATIVES;
   }

   /**
    * Compose the given children into one container.
    */
   protected ILootEntry compose(ILootEntry[] pEntries) {
      switch(pEntries.length) {
      case 0:
         return ALWAYS_FALSE;
      case 1:
         return pEntries[0];
      case 2:
         return pEntries[0].or(pEntries[1]);
      default:
         return (p_216150_1_, p_216150_2_) -> {
            for(ILootEntry ilootentry : pEntries) {
               if (ilootentry.expand(p_216150_1_, p_216150_2_)) {
                  return true;
               }
            }

            return false;
         };
      }
   }

   public void validate(ValidationTracker pValidationContext) {
      super.validate(pValidationContext);

      for(int i = 0; i < this.children.length - 1; ++i) {
         if (ArrayUtils.isEmpty((Object[])this.children[i].conditions)) {
            pValidationContext.reportProblem("Unreachable entry!");
         }
      }

   }

   public static AlternativesLootEntry.Builder alternatives(LootEntry.Builder<?>... pChildren) {
      return new AlternativesLootEntry.Builder(pChildren);
   }

   public static class Builder extends LootEntry.Builder<AlternativesLootEntry.Builder> {
      private final List<LootEntry> entries = Lists.newArrayList();

      public Builder(LootEntry.Builder<?>... pChildren) {
         for(LootEntry.Builder<?> builder : pChildren) {
            this.entries.add(builder.build());
         }

      }

      protected AlternativesLootEntry.Builder getThis() {
         return this;
      }

      public AlternativesLootEntry.Builder otherwise(LootEntry.Builder<?> pChildBuilder) {
         this.entries.add(pChildBuilder.build());
         return this;
      }

      public LootEntry build() {
         return new AlternativesLootEntry(this.entries.toArray(new LootEntry[0]), this.getConditions());
      }
   }
}