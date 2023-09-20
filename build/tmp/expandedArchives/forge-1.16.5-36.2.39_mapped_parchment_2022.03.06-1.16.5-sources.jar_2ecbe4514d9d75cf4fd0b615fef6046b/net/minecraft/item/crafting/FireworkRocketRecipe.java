package net.minecraft.item.crafting;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class FireworkRocketRecipe extends SpecialRecipe {
   private static final Ingredient PAPER_INGREDIENT = Ingredient.of(Items.PAPER);
   private static final Ingredient GUNPOWDER_INGREDIENT = Ingredient.of(Items.GUNPOWDER);
   private static final Ingredient STAR_INGREDIENT = Ingredient.of(Items.FIREWORK_STAR);

   public FireworkRocketRecipe(ResourceLocation p_i48168_1_) {
      super(p_i48168_1_);
   }

   /**
    * Used to check if a recipe matches current crafting inventory
    */
   public boolean matches(CraftingInventory pInv, World pLevel) {
      boolean flag = false;
      int i = 0;

      for(int j = 0; j < pInv.getContainerSize(); ++j) {
         ItemStack itemstack = pInv.getItem(j);
         if (!itemstack.isEmpty()) {
            if (PAPER_INGREDIENT.test(itemstack)) {
               if (flag) {
                  return false;
               }

               flag = true;
            } else if (GUNPOWDER_INGREDIENT.test(itemstack)) {
               ++i;
               if (i > 3) {
                  return false;
               }
            } else if (!STAR_INGREDIENT.test(itemstack)) {
               return false;
            }
         }
      }

      return flag && i >= 1;
   }

   /**
    * Returns an Item that is the result of this recipe
    */
   public ItemStack assemble(CraftingInventory pInv) {
      ItemStack itemstack = new ItemStack(Items.FIREWORK_ROCKET, 3);
      CompoundNBT compoundnbt = itemstack.getOrCreateTagElement("Fireworks");
      ListNBT listnbt = new ListNBT();
      int i = 0;

      for(int j = 0; j < pInv.getContainerSize(); ++j) {
         ItemStack itemstack1 = pInv.getItem(j);
         if (!itemstack1.isEmpty()) {
            if (GUNPOWDER_INGREDIENT.test(itemstack1)) {
               ++i;
            } else if (STAR_INGREDIENT.test(itemstack1)) {
               CompoundNBT compoundnbt1 = itemstack1.getTagElement("Explosion");
               if (compoundnbt1 != null) {
                  listnbt.add(compoundnbt1);
               }
            }
         }
      }

      compoundnbt.putByte("Flight", (byte)i);
      if (!listnbt.isEmpty()) {
         compoundnbt.put("Explosions", listnbt);
      }

      return itemstack;
   }

   /**
    * Used to determine if this recipe can fit in a grid of the given width/height
    */
   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return pWidth * pHeight >= 2;
   }

   /**
    * Get the result of this recipe, usually for display purposes (e.g. recipe book). If your recipe has more than one
    * possible result (e.g. it's dynamic and depends on its inputs), then return an empty stack.
    */
   public ItemStack getResultItem() {
      return new ItemStack(Items.FIREWORK_ROCKET);
   }

   public IRecipeSerializer<?> getSerializer() {
      return IRecipeSerializer.FIREWORK_ROCKET;
   }
}