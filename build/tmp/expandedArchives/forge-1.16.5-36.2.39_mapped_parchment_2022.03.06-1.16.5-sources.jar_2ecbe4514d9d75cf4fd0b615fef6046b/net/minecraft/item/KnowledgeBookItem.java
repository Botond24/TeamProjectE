package net.minecraft.item;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KnowledgeBookItem extends Item {
   private static final Logger LOGGER = LogManager.getLogger();

   public KnowledgeBookItem(Item.Properties p_i48485_1_) {
      super(p_i48485_1_);
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      CompoundNBT compoundnbt = itemstack.getTag();
      if (!pPlayer.abilities.instabuild) {
         pPlayer.setItemInHand(pHand, ItemStack.EMPTY);
      }

      if (compoundnbt != null && compoundnbt.contains("Recipes", 9)) {
         if (!pLevel.isClientSide) {
            ListNBT listnbt = compoundnbt.getList("Recipes", 8);
            List<IRecipe<?>> list = Lists.newArrayList();
            RecipeManager recipemanager = pLevel.getServer().getRecipeManager();

            for(int i = 0; i < listnbt.size(); ++i) {
               String s = listnbt.getString(i);
               Optional<? extends IRecipe<?>> optional = recipemanager.byKey(new ResourceLocation(s));
               if (!optional.isPresent()) {
                  LOGGER.error("Invalid recipe: {}", (Object)s);
                  return ActionResult.fail(itemstack);
               }

               list.add(optional.get());
            }

            pPlayer.awardRecipes(list);
            pPlayer.awardStat(Stats.ITEM_USED.get(this));
         }

         return ActionResult.sidedSuccess(itemstack, pLevel.isClientSide());
      } else {
         LOGGER.error("Tag not valid: {}", (Object)compoundnbt);
         return ActionResult.fail(itemstack);
      }
   }
}