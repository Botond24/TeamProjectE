package net.minecraft.item.crafting;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.play.server.SRecipeBookPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerRecipeBook extends RecipeBook {
   private static final Logger LOGGER = LogManager.getLogger();

   public int addRecipes(Collection<IRecipe<?>> pRecipes, ServerPlayerEntity pPlayer) {
      List<ResourceLocation> list = Lists.newArrayList();
      int i = 0;

      for(IRecipe<?> irecipe : pRecipes) {
         ResourceLocation resourcelocation = irecipe.getId();
         if (!this.known.contains(resourcelocation) && !irecipe.isSpecial()) {
            this.add(resourcelocation);
            this.addHighlight(resourcelocation);
            list.add(resourcelocation);
            CriteriaTriggers.RECIPE_UNLOCKED.trigger(pPlayer, irecipe);
            ++i;
         }
      }

      this.sendRecipes(SRecipeBookPacket.State.ADD, pPlayer, list);
      return i;
   }

   public int removeRecipes(Collection<IRecipe<?>> pRecipes, ServerPlayerEntity pPlayer) {
      List<ResourceLocation> list = Lists.newArrayList();
      int i = 0;

      for(IRecipe<?> irecipe : pRecipes) {
         ResourceLocation resourcelocation = irecipe.getId();
         if (this.known.contains(resourcelocation)) {
            this.remove(resourcelocation);
            list.add(resourcelocation);
            ++i;
         }
      }

      this.sendRecipes(SRecipeBookPacket.State.REMOVE, pPlayer, list);
      return i;
   }

   private void sendRecipes(SRecipeBookPacket.State pState, ServerPlayerEntity pPlayer, List<ResourceLocation> pRecipes) {
      pPlayer.connection.send(new SRecipeBookPacket(pState, pRecipes, Collections.emptyList(), this.getBookSettings()));
   }

   public CompoundNBT toNbt() {
      CompoundNBT compoundnbt = new CompoundNBT();
      this.getBookSettings().write(compoundnbt);
      ListNBT listnbt = new ListNBT();

      for(ResourceLocation resourcelocation : this.known) {
         listnbt.add(StringNBT.valueOf(resourcelocation.toString()));
      }

      compoundnbt.put("recipes", listnbt);
      ListNBT listnbt1 = new ListNBT();

      for(ResourceLocation resourcelocation1 : this.highlight) {
         listnbt1.add(StringNBT.valueOf(resourcelocation1.toString()));
      }

      compoundnbt.put("toBeDisplayed", listnbt1);
      return compoundnbt;
   }

   public void fromNbt(CompoundNBT pTag, RecipeManager pRecipeManager) {
      this.setBookSettings(RecipeBookStatus.read(pTag));
      ListNBT listnbt = pTag.getList("recipes", 8);
      this.loadRecipes(listnbt, this::add, pRecipeManager);
      ListNBT listnbt1 = pTag.getList("toBeDisplayed", 8);
      this.loadRecipes(listnbt1, this::addHighlight, pRecipeManager);
   }

   private void loadRecipes(ListNBT pNbtList, Consumer<IRecipe<?>> pRecipeConsumer, RecipeManager pRecipeManager) {
      for(int i = 0; i < pNbtList.size(); ++i) {
         String s = pNbtList.getString(i);

         try {
            ResourceLocation resourcelocation = new ResourceLocation(s);
            Optional<? extends IRecipe<?>> optional = pRecipeManager.byKey(resourcelocation);
            if (!optional.isPresent()) {
               LOGGER.error("Tried to load unrecognized recipe: {} removed now.", (Object)resourcelocation);
            } else {
               pRecipeConsumer.accept(optional.get());
            }
         } catch (ResourceLocationException resourcelocationexception) {
            LOGGER.error("Tried to load improperly formatted recipe: {} removed now.", (Object)s);
         }
      }

   }

   public void sendInitialRecipeBook(ServerPlayerEntity pPlayer) {
      pPlayer.connection.send(new SRecipeBookPacket(SRecipeBookPacket.State.INIT, this.known, this.highlight, this.getBookSettings()));
   }
}