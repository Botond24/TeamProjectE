package net.minecraft.loot;

import net.minecraft.loot.conditions.ILootCondition;

/**
 * A composite loot pool entry container that expands all its children in order.
 * This container always succeeds.
 */
public class GroupLootEntry extends ParentedLootEntry {
   GroupLootEntry(LootEntry[] p_i51257_1_, ILootCondition[] p_i51257_2_) {
      super(p_i51257_1_, p_i51257_2_);
   }

   public LootPoolEntryType getType() {
      return LootEntryManager.GROUP;
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
         ILootEntry ilootentry = pEntries[0];
         ILootEntry ilootentry1 = pEntries[1];
         return (p_216151_2_, p_216151_3_) -> {
            ilootentry.expand(p_216151_2_, p_216151_3_);
            ilootentry1.expand(p_216151_2_, p_216151_3_);
            return true;
         };
      default:
         return (p_216152_1_, p_216152_2_) -> {
            for(ILootEntry ilootentry2 : pEntries) {
               ilootentry2.expand(p_216152_1_, p_216152_2_);
            }

            return true;
         };
      }
   }
}