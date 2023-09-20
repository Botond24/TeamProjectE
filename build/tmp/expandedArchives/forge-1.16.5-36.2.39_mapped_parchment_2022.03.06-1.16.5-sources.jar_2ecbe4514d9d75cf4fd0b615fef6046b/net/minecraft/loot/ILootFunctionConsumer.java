package net.minecraft.loot;

import net.minecraft.loot.functions.ILootFunction;

/**
 * Base interface for builders that accept loot functions.
 * 
 * @see LootItemFunction
 */
public interface ILootFunctionConsumer<T> {
   T apply(ILootFunction.IBuilder pFunctionBuilder);

   T unwrap();
}