package net.minecraft.loot;

import net.minecraft.loot.functions.ILootFunction;

/**
 * The SerializerType for {@link LootItemFunction}.
 */
public class LootFunctionType extends LootType<ILootFunction> {
   public LootFunctionType(ILootSerializer<? extends ILootFunction> p_i232171_1_) {
      super(p_i232171_1_);
   }
}