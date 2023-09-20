package net.minecraft.client.gui.recipebook;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.List;
import java.util.Set;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeBook;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RecipeList {
   private final List<IRecipe<?>> recipes;
   private final boolean singleResultItem;
   private final Set<IRecipe<?>> craftable = Sets.newHashSet();
   private final Set<IRecipe<?>> fitsDimensions = Sets.newHashSet();
   private final Set<IRecipe<?>> known = Sets.newHashSet();

   public RecipeList(List<IRecipe<?>> p_i242062_1_) {
      this.recipes = ImmutableList.copyOf(p_i242062_1_);
      if (p_i242062_1_.size() <= 1) {
         this.singleResultItem = true;
      } else {
         this.singleResultItem = allRecipesHaveSameResult(p_i242062_1_);
      }

   }

   private static boolean allRecipesHaveSameResult(List<IRecipe<?>> p_243413_0_) {
      int i = p_243413_0_.size();
      ItemStack itemstack = p_243413_0_.get(0).getResultItem();

      for(int j = 1; j < i; ++j) {
         ItemStack itemstack1 = p_243413_0_.get(j).getResultItem();
         if (!ItemStack.isSame(itemstack, itemstack1) || !ItemStack.tagMatches(itemstack, itemstack1)) {
            return false;
         }
      }

      return true;
   }

   /**
    * Checks if recipebook is not empty
    */
   public boolean hasKnownRecipes() {
      return !this.known.isEmpty();
   }

   public void updateKnownRecipes(RecipeBook pBook) {
      for(IRecipe<?> irecipe : this.recipes) {
         if (pBook.contains(irecipe)) {
            this.known.add(irecipe);
         }
      }

   }

   public void canCraft(RecipeItemHelper pHandler, int pWidth, int pHeight, RecipeBook pBook) {
      for(IRecipe<?> irecipe : this.recipes) {
         boolean flag = irecipe.canCraftInDimensions(pWidth, pHeight) && pBook.contains(irecipe);
         if (flag) {
            this.fitsDimensions.add(irecipe);
         } else {
            this.fitsDimensions.remove(irecipe);
         }

         if (flag && pHandler.canCraft(irecipe, (IntList)null)) {
            this.craftable.add(irecipe);
         } else {
            this.craftable.remove(irecipe);
         }
      }

   }

   public boolean isCraftable(IRecipe<?> pRecipe) {
      return this.craftable.contains(pRecipe);
   }

   public boolean hasCraftable() {
      return !this.craftable.isEmpty();
   }

   public boolean hasFitting() {
      return !this.fitsDimensions.isEmpty();
   }

   public List<IRecipe<?>> getRecipes() {
      return this.recipes;
   }

   public List<IRecipe<?>> getRecipes(boolean pOnlyCraftable) {
      List<IRecipe<?>> list = Lists.newArrayList();
      Set<IRecipe<?>> set = pOnlyCraftable ? this.craftable : this.fitsDimensions;

      for(IRecipe<?> irecipe : this.recipes) {
         if (set.contains(irecipe)) {
            list.add(irecipe);
         }
      }

      return list;
   }

   public List<IRecipe<?>> getDisplayRecipes(boolean pOnlyCraftable) {
      List<IRecipe<?>> list = Lists.newArrayList();

      for(IRecipe<?> irecipe : this.recipes) {
         if (this.fitsDimensions.contains(irecipe) && this.craftable.contains(irecipe) == pOnlyCraftable) {
            list.add(irecipe);
         }
      }

      return list;
   }

   public boolean hasSingleResultItem() {
      return this.singleResultItem;
   }
}