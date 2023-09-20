package net.minecraft.inventory;

import java.util.Collections;
import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public interface IRecipeHolder {
   void setRecipeUsed(@Nullable IRecipe<?> pRecipe);

   @Nullable
   IRecipe<?> getRecipeUsed();

   default void awardUsedRecipes(PlayerEntity pPlayer) {
      IRecipe<?> irecipe = this.getRecipeUsed();
      if (irecipe != null && !irecipe.isSpecial()) {
         pPlayer.awardRecipes(Collections.singleton(irecipe));
         this.setRecipeUsed((IRecipe<?>)null);
      }

   }

   default boolean setRecipeUsed(World pLevel, ServerPlayerEntity pPlayer, IRecipe<?> pRecipe) {
      if (!pRecipe.isSpecial() && pLevel.getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING) && !pPlayer.getRecipeBook().contains(pRecipe)) {
         return false;
      } else {
         this.setRecipeUsed(pRecipe);
         return true;
      }
   }
}