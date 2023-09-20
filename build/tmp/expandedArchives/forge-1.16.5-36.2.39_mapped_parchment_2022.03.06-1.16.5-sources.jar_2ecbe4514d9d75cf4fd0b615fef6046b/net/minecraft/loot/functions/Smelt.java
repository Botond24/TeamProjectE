package net.minecraft.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.conditions.ILootCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * LootItemFunction that tries to smelt any items using {@link RecipeType.SMELTING}.
 */
public class Smelt extends LootFunction {
   private static final Logger LOGGER = LogManager.getLogger();

   private Smelt(ILootCondition[] p_i46619_1_) {
      super(p_i46619_1_);
   }

   public LootFunctionType getType() {
      return LootFunctionManager.FURNACE_SMELT;
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      if (pStack.isEmpty()) {
         return pStack;
      } else {
         Optional<FurnaceRecipe> optional = pContext.getLevel().getRecipeManager().getRecipeFor(IRecipeType.SMELTING, new Inventory(pStack), pContext.getLevel());
         if (optional.isPresent()) {
            ItemStack itemstack = optional.get().getResultItem();
            if (!itemstack.isEmpty()) {
               ItemStack itemstack1 = itemstack.copy();
               itemstack1.setCount(pStack.getCount() * itemstack.getCount()); //Forge: Support smelting returning multiple
               return itemstack1;
            }
         }

         LOGGER.warn("Couldn't smelt {} because there is no smelting recipe", (Object)pStack);
         return pStack;
      }
   }

   public static LootFunction.Builder<?> smelted() {
      return simpleBuilder(Smelt::new);
   }

   public static class Serializer extends LootFunction.Serializer<Smelt> {
      public Smelt deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, ILootCondition[] pConditions) {
         return new Smelt(pConditions);
      }
   }
}
