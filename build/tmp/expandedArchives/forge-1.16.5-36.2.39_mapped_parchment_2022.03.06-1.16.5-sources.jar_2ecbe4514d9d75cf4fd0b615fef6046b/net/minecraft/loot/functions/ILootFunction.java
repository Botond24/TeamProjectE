package net.minecraft.loot.functions;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.IParameterized;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunctionType;

/**
 * A LootItemFunction modifies an ItemStack based on the current LootContext.
 * 
 * @see LootItemFunctions
 */
public interface ILootFunction extends IParameterized, BiFunction<ItemStack, LootContext, ItemStack> {
   LootFunctionType getType();

   /**
    * Create a decorated Consumer. The resulting consumer will first apply {@code stackModification} to all stacks
    * before passing them on to {@code originalConsumer}.
    */
   static Consumer<ItemStack> decorate(BiFunction<ItemStack, LootContext, ItemStack> pStackModification, Consumer<ItemStack> pOriginalConsumer, LootContext pLootContext) {
      return (p_215857_3_) -> {
         pOriginalConsumer.accept(pStackModification.apply(p_215857_3_, pLootContext));
      };
   }

   public interface IBuilder {
      ILootFunction build();
   }
}