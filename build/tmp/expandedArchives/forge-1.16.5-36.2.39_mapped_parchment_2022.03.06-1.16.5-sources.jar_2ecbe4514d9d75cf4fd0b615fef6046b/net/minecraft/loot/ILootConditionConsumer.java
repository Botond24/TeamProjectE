package net.minecraft.loot;

import net.minecraft.loot.conditions.ILootCondition;

/**
 * Base interface for builders that can accept loot conditions.
 * 
 * @see LootItemCondition
 */
public interface ILootConditionConsumer<T> {
   T when(ILootCondition.IBuilder pConditionBuilder);

   T unwrap();
}