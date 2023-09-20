package net.minecraft.loot;

import net.minecraft.loot.conditions.ILootCondition;

/**
 * A composite loot pool entry container that expands all its children in order until one of them fails.
 * This container succeeds if all children succeed.
 */
public class SequenceLootEntry extends ParentedLootEntry {
   SequenceLootEntry(LootEntry[] p_i51250_1_, ILootCondition[] p_i51250_2_) {
      super(p_i51250_1_, p_i51250_2_);
   }

   public LootPoolEntryType getType() {
      return LootEntryManager.SEQUENCE;
   }

   /**
    * Compose the given children into one container.
    */
   protected ILootEntry compose(ILootEntry[] pEntries) {
      switch(pEntries.length) {
      case 0:
         return ALWAYS_TRUE;
      case 1:
         return pEntries[0];
      case 2:
         return pEntries[0].and(pEntries[1]);
      default:
         return (p_216153_1_, p_216153_2_) -> {
            for(ILootEntry ilootentry : pEntries) {
               if (!ilootentry.expand(p_216153_1_, p_216153_2_)) {
                  return false;
               }
            }

            return true;
         };
      }
   }
}