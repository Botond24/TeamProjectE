package net.minecraft.item.crafting;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RecipeBook {
   protected final Set<ResourceLocation> known = Sets.newHashSet();
   protected final Set<ResourceLocation> highlight = Sets.newHashSet();
   private final RecipeBookStatus bookSettings = new RecipeBookStatus();

   public void copyOverData(RecipeBook pThat) {
      this.known.clear();
      this.highlight.clear();
      this.bookSettings.replaceFrom(pThat.bookSettings);
      this.known.addAll(pThat.known);
      this.highlight.addAll(pThat.highlight);
   }

   public void add(IRecipe<?> pRecipe) {
      if (!pRecipe.isSpecial()) {
         this.add(pRecipe.getId());
      }

   }

   protected void add(ResourceLocation pResourceLocation) {
      this.known.add(pResourceLocation);
   }

   public boolean contains(@Nullable IRecipe<?> pRecipe) {
      return pRecipe == null ? false : this.known.contains(pRecipe.getId());
   }

   public boolean contains(ResourceLocation pId) {
      return this.known.contains(pId);
   }

   @OnlyIn(Dist.CLIENT)
   public void remove(IRecipe<?> pRecipe) {
      this.remove(pRecipe.getId());
   }

   protected void remove(ResourceLocation pResourceLocation) {
      this.known.remove(pResourceLocation);
      this.highlight.remove(pResourceLocation);
   }

   @OnlyIn(Dist.CLIENT)
   public boolean willHighlight(IRecipe<?> pRecipe) {
      return this.highlight.contains(pRecipe.getId());
   }

   public void removeHighlight(IRecipe<?> pRecipe) {
      this.highlight.remove(pRecipe.getId());
   }

   public void addHighlight(IRecipe<?> pRecipe) {
      this.addHighlight(pRecipe.getId());
   }

   protected void addHighlight(ResourceLocation pResourceLocation) {
      this.highlight.add(pResourceLocation);
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isOpen(RecipeBookCategory pType) {
      return this.bookSettings.isOpen(pType);
   }

   @OnlyIn(Dist.CLIENT)
   public void setOpen(RecipeBookCategory pType, boolean pIsVisible) {
      this.bookSettings.setOpen(pType, pIsVisible);
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isFiltering(RecipeBookContainer<?> pMenu) {
      return this.isFiltering(pMenu.getRecipeBookType());
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isFiltering(RecipeBookCategory pType) {
      return this.bookSettings.isFiltering(pType);
   }

   @OnlyIn(Dist.CLIENT)
   public void setFiltering(RecipeBookCategory pType, boolean pShouldFilterCraftable) {
      this.bookSettings.setFiltering(pType, pShouldFilterCraftable);
   }

   public void setBookSettings(RecipeBookStatus pBookSettings) {
      this.bookSettings.replaceFrom(pBookSettings);
   }

   public RecipeBookStatus getBookSettings() {
      return this.bookSettings.copy();
   }

   public void setBookSetting(RecipeBookCategory pType, boolean pIsVisible, boolean pShouldFilterCraftable) {
      this.bookSettings.setOpen(pType, pIsVisible);
      this.bookSettings.setFiltering(pType, pShouldFilterCraftable);
   }
}