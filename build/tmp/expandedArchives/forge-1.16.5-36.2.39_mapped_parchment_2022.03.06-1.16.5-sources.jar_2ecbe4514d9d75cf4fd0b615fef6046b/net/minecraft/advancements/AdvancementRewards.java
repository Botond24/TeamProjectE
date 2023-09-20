package net.minecraft.advancements;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.FunctionObject;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;

public class AdvancementRewards {
   public static final AdvancementRewards EMPTY = new AdvancementRewards(0, new ResourceLocation[0], new ResourceLocation[0], FunctionObject.CacheableFunction.NONE);
   private final int experience;
   private final ResourceLocation[] loot;
   private final ResourceLocation[] recipes;
   private final FunctionObject.CacheableFunction function;

   public AdvancementRewards(int pExperience, ResourceLocation[] pLoot, ResourceLocation[] pRecipes, FunctionObject.CacheableFunction pFunction) {
      this.experience = pExperience;
      this.loot = pLoot;
      this.recipes = pRecipes;
      this.function = pFunction;
   }

   public void grant(ServerPlayerEntity pPlayer) {
      pPlayer.giveExperiencePoints(this.experience);
      LootContext lootcontext = (new LootContext.Builder(pPlayer.getLevel())).withParameter(LootParameters.THIS_ENTITY, pPlayer).withParameter(LootParameters.ORIGIN, pPlayer.position()).withRandom(pPlayer.getRandom()).withLuck(pPlayer.getLuck()).create(LootParameterSets.ADVANCEMENT_REWARD); // FORGE: luck to LootContext
      boolean flag = false;

      for(ResourceLocation resourcelocation : this.loot) {
         for(ItemStack itemstack : pPlayer.server.getLootTables().get(resourcelocation).getRandomItems(lootcontext)) {
            if (pPlayer.addItem(itemstack)) {
               pPlayer.level.playSound((PlayerEntity)null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((pPlayer.getRandom().nextFloat() - pPlayer.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
               flag = true;
            } else {
               ItemEntity itementity = pPlayer.drop(itemstack, false);
               if (itementity != null) {
                  itementity.setNoPickUpDelay();
                  itementity.setOwner(pPlayer.getUUID());
               }
            }
         }
      }

      if (flag) {
         pPlayer.inventoryMenu.broadcastChanges();
      }

      if (this.recipes.length > 0) {
         pPlayer.awardRecipesByKey(this.recipes);
      }

      MinecraftServer minecraftserver = pPlayer.server;
      this.function.get(minecraftserver.getFunctions()).ifPresent((p_215098_2_) -> {
         minecraftserver.getFunctions().execute(p_215098_2_, pPlayer.createCommandSourceStack().withSuppressedOutput().withPermission(2));
      });
   }

   public String toString() {
      return "AdvancementRewards{experience=" + this.experience + ", loot=" + Arrays.toString((Object[])this.loot) + ", recipes=" + Arrays.toString((Object[])this.recipes) + ", function=" + this.function + '}';
   }

   public JsonElement serializeToJson() {
      if (this == EMPTY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         if (this.experience != 0) {
            jsonobject.addProperty("experience", this.experience);
         }

         if (this.loot.length > 0) {
            JsonArray jsonarray = new JsonArray();

            for(ResourceLocation resourcelocation : this.loot) {
               jsonarray.add(resourcelocation.toString());
            }

            jsonobject.add("loot", jsonarray);
         }

         if (this.recipes.length > 0) {
            JsonArray jsonarray1 = new JsonArray();

            for(ResourceLocation resourcelocation1 : this.recipes) {
               jsonarray1.add(resourcelocation1.toString());
            }

            jsonobject.add("recipes", jsonarray1);
         }

         if (this.function.getId() != null) {
            jsonobject.addProperty("function", this.function.getId().toString());
         }

         return jsonobject;
      }
   }

   public static AdvancementRewards deserialize(JsonObject pJson) throws JsonParseException {
      int i = JSONUtils.getAsInt(pJson, "experience", 0);
      JsonArray jsonarray = JSONUtils.getAsJsonArray(pJson, "loot", new JsonArray());
      ResourceLocation[] aresourcelocation = new ResourceLocation[jsonarray.size()];

      for(int j = 0; j < aresourcelocation.length; ++j) {
         aresourcelocation[j] = new ResourceLocation(JSONUtils.convertToString(jsonarray.get(j), "loot[" + j + "]"));
      }

      JsonArray jsonarray1 = JSONUtils.getAsJsonArray(pJson, "recipes", new JsonArray());
      ResourceLocation[] aresourcelocation1 = new ResourceLocation[jsonarray1.size()];

      for(int k = 0; k < aresourcelocation1.length; ++k) {
         aresourcelocation1[k] = new ResourceLocation(JSONUtils.convertToString(jsonarray1.get(k), "recipes[" + k + "]"));
      }

      FunctionObject.CacheableFunction functionobject$cacheablefunction;
      if (pJson.has("function")) {
         functionobject$cacheablefunction = new FunctionObject.CacheableFunction(new ResourceLocation(JSONUtils.getAsString(pJson, "function")));
      } else {
         functionobject$cacheablefunction = FunctionObject.CacheableFunction.NONE;
      }

      return new AdvancementRewards(i, aresourcelocation, aresourcelocation1, functionobject$cacheablefunction);
   }

   public static class Builder {
      private int experience;
      private final List<ResourceLocation> loot = Lists.newArrayList();
      private final List<ResourceLocation> recipes = Lists.newArrayList();
      @Nullable
      private ResourceLocation function;

      /**
       * Creates a new builder with the given amount of experience as a reward
       */
      public static AdvancementRewards.Builder experience(int pExperience) {
         return (new AdvancementRewards.Builder()).addExperience(pExperience);
      }

      /**
       * Adds the given amount of experience. (Not a direct setter)
       */
      public AdvancementRewards.Builder addExperience(int pExperience) {
         this.experience += pExperience;
         return this;
      }

      /**
       * Creates a new builder with the given recipe as a reward.
       */
      public static AdvancementRewards.Builder recipe(ResourceLocation pRecipe) {
         return (new AdvancementRewards.Builder()).addRecipe(pRecipe);
      }

      /**
       * Adds the given recipe to the rewards.
       */
      public AdvancementRewards.Builder addRecipe(ResourceLocation pRecipe) {
         this.recipes.add(pRecipe);
         return this;
      }

      public AdvancementRewards build() {
         return new AdvancementRewards(this.experience, this.loot.toArray(new ResourceLocation[0]), this.recipes.toArray(new ResourceLocation[0]), this.function == null ? FunctionObject.CacheableFunction.NONE : new FunctionObject.CacheableFunction(this.function));
      }
   }
}
