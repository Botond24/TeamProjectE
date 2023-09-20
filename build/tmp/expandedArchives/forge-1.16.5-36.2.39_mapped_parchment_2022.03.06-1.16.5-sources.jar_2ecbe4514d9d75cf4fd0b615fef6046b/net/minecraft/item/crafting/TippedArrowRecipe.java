package net.minecraft.item.crafting;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class TippedArrowRecipe extends SpecialRecipe {
   public TippedArrowRecipe(ResourceLocation p_i48184_1_) {
      super(p_i48184_1_);
   }

   /**
    * Used to check if a recipe matches current crafting inventory
    */
   public boolean matches(CraftingInventory pInv, World pLevel) {
      if (pInv.getWidth() == 3 && pInv.getHeight() == 3) {
         for(int i = 0; i < pInv.getWidth(); ++i) {
            for(int j = 0; j < pInv.getHeight(); ++j) {
               ItemStack itemstack = pInv.getItem(i + j * pInv.getWidth());
               if (itemstack.isEmpty()) {
                  return false;
               }

               Item item = itemstack.getItem();
               if (i == 1 && j == 1) {
                  if (item != Items.LINGERING_POTION) {
                     return false;
                  }
               } else if (item != Items.ARROW) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   /**
    * Returns an Item that is the result of this recipe
    */
   public ItemStack assemble(CraftingInventory pInv) {
      ItemStack itemstack = pInv.getItem(1 + pInv.getWidth());
      if (itemstack.getItem() != Items.LINGERING_POTION) {
         return ItemStack.EMPTY;
      } else {
         ItemStack itemstack1 = new ItemStack(Items.TIPPED_ARROW, 8);
         PotionUtils.setPotion(itemstack1, PotionUtils.getPotion(itemstack));
         PotionUtils.setCustomEffects(itemstack1, PotionUtils.getCustomEffects(itemstack));
         return itemstack1;
      }
   }

   /**
    * Used to determine if this recipe can fit in a grid of the given width/height
    */
   public boolean canCraftInDimensions(int pWidth, int pHeight) {
      return pWidth >= 2 && pHeight >= 2;
   }

   public IRecipeSerializer<?> getSerializer() {
      return IRecipeSerializer.TIPPED_ARROW;
   }
}