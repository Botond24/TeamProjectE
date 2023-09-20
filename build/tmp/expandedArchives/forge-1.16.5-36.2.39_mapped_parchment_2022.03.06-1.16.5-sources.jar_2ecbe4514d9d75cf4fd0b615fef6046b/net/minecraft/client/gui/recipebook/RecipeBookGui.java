package net.minecraft.client.gui.recipebook;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ToggleWidget;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.util.ClientRecipeBook;
import net.minecraft.client.util.RecipeBookCategories;
import net.minecraft.client.util.SearchTreeManager;
import net.minecraft.inventory.container.RecipeBookContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipePlacer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeBookCategory;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.network.play.client.CUpdateRecipeBookStatusPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RecipeBookGui extends AbstractGui implements IRenderable, IGuiEventListener, IRecipeUpdateListener, IRecipePlacer<Ingredient> {
   protected static final ResourceLocation RECIPE_BOOK_LOCATION = new ResourceLocation("textures/gui/recipe_book.png");
   private static final ITextComponent SEARCH_HINT = (new TranslationTextComponent("gui.recipebook.search_hint")).withStyle(TextFormatting.ITALIC).withStyle(TextFormatting.GRAY);
   private static final ITextComponent ONLY_CRAFTABLES_TOOLTIP = new TranslationTextComponent("gui.recipebook.toggleRecipes.craftable");
   private static final ITextComponent ALL_RECIPES_TOOLTIP = new TranslationTextComponent("gui.recipebook.toggleRecipes.all");
   private int xOffset;
   private int width;
   private int height;
   protected final GhostRecipe ghostRecipe = new GhostRecipe();
   private final List<RecipeTabToggleWidget> tabButtons = Lists.newArrayList();
   private RecipeTabToggleWidget selectedTab;
   protected ToggleWidget filterButton;
   protected RecipeBookContainer<?> menu;
   protected Minecraft minecraft;
   private TextFieldWidget searchBox;
   private String lastSearch = "";
   private ClientRecipeBook book;
   private final RecipeBookPage recipeBookPage = new RecipeBookPage();
   private final RecipeItemHelper stackedContents = new RecipeItemHelper();
   private int timesInventoryChanged;
   private boolean ignoreTextInput;

   public void init(int pWidth, int pHeight, Minecraft pMinecraft, boolean pWidthTooNarrow, RecipeBookContainer<?> pContainer) {
      this.minecraft = pMinecraft;
      this.width = pWidth;
      this.height = pHeight;
      this.menu = pContainer;
      pMinecraft.player.containerMenu = pContainer;
      this.book = pMinecraft.player.getRecipeBook();
      this.timesInventoryChanged = pMinecraft.player.inventory.getTimesChanged();
      if (this.isVisible()) {
         this.initVisuals(pWidthTooNarrow);
      }

      pMinecraft.keyboardHandler.setSendRepeatsToGui(true);
   }

   public void initVisuals(boolean p_201518_1_) {
      this.xOffset = p_201518_1_ ? 0 : 86;
      int i = (this.width - 147) / 2 - this.xOffset;
      int j = (this.height - 166) / 2;
      this.stackedContents.clear();
      this.minecraft.player.inventory.fillStackedContents(this.stackedContents);
      this.menu.fillCraftSlotsStackedContents(this.stackedContents);
      String s = this.searchBox != null ? this.searchBox.getValue() : "";
      this.searchBox = new TextFieldWidget(this.minecraft.font, i + 25, j + 14, 80, 9 + 5, new TranslationTextComponent("itemGroup.search"));
      this.searchBox.setMaxLength(50);
      this.searchBox.setBordered(false);
      this.searchBox.setVisible(true);
      this.searchBox.setTextColor(16777215);
      this.searchBox.setValue(s);
      this.recipeBookPage.init(this.minecraft, i, j);
      this.recipeBookPage.addListener(this);
      this.filterButton = new ToggleWidget(i + 110, j + 12, 26, 16, this.book.isFiltering(this.menu));
      this.initFilterButtonTextures();
      this.tabButtons.clear();

      for(RecipeBookCategories recipebookcategories : this.menu.getRecipeBookCategories()) {
         this.tabButtons.add(new RecipeTabToggleWidget(recipebookcategories));
      }

      if (this.selectedTab != null) {
         this.selectedTab = this.tabButtons.stream().filter((p_209505_1_) -> {
            return p_209505_1_.getCategory().equals(this.selectedTab.getCategory());
         }).findFirst().orElse((RecipeTabToggleWidget)null);
      }

      if (this.selectedTab == null) {
         this.selectedTab = this.tabButtons.get(0);
      }

      this.selectedTab.setStateTriggered(true);
      this.updateCollections(false);
      this.updateTabs();
   }

   public boolean changeFocus(boolean pFocus) {
      return false;
   }

   protected void initFilterButtonTextures() {
      this.filterButton.initTextureValues(152, 41, 28, 18, RECIPE_BOOK_LOCATION);
   }

   public void removed() {
      this.searchBox = null;
      this.selectedTab = null;
      this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
   }

   public int updateScreenPosition(boolean p_193011_1_, int p_193011_2_, int p_193011_3_) {
      int i;
      if (this.isVisible() && !p_193011_1_) {
         i = 177 + (p_193011_2_ - p_193011_3_ - 200) / 2;
      } else {
         i = (p_193011_2_ - p_193011_3_) / 2;
      }

      return i;
   }

   public void toggleVisibility() {
      this.setVisible(!this.isVisible());
   }

   public boolean isVisible() {
      return this.book.isOpen(this.menu.getRecipeBookType());
   }

   protected void setVisible(boolean p_193006_1_) {
      this.book.setOpen(this.menu.getRecipeBookType(), p_193006_1_);
      if (!p_193006_1_) {
         this.recipeBookPage.setInvisible();
      }

      this.sendUpdateSettings();
   }

   public void slotClicked(@Nullable Slot pSlot) {
      if (pSlot != null && pSlot.index < this.menu.getSize()) {
         this.ghostRecipe.clear();
         if (this.isVisible()) {
            this.updateStackedContents();
         }
      }

   }

   private void updateCollections(boolean p_193003_1_) {
      List<RecipeList> list = this.book.getCollection(this.selectedTab.getCategory());
      list.forEach((p_193944_1_) -> {
         p_193944_1_.canCraft(this.stackedContents, this.menu.getGridWidth(), this.menu.getGridHeight(), this.book);
      });
      List<RecipeList> list1 = Lists.newArrayList(list);
      list1.removeIf((p_193952_0_) -> {
         return !p_193952_0_.hasKnownRecipes();
      });
      list1.removeIf((p_193953_0_) -> {
         return !p_193953_0_.hasFitting();
      });
      String s = this.searchBox.getValue();
      if (!s.isEmpty()) {
         ObjectSet<RecipeList> objectset = new ObjectLinkedOpenHashSet<>(this.minecraft.getSearchTree(SearchTreeManager.RECIPE_COLLECTIONS).search(s.toLowerCase(Locale.ROOT)));
         list1.removeIf((p_193947_1_) -> {
            return !objectset.contains(p_193947_1_);
         });
      }

      if (this.book.isFiltering(this.menu)) {
         list1.removeIf((p_193958_0_) -> {
            return !p_193958_0_.hasCraftable();
         });
      }

      this.recipeBookPage.updateCollections(list1, p_193003_1_);
   }

   private void updateTabs() {
      int i = (this.width - 147) / 2 - this.xOffset - 30;
      int j = (this.height - 166) / 2 + 3;
      int k = 27;
      int l = 0;

      for(RecipeTabToggleWidget recipetabtogglewidget : this.tabButtons) {
         RecipeBookCategories recipebookcategories = recipetabtogglewidget.getCategory();
         if (recipebookcategories != RecipeBookCategories.CRAFTING_SEARCH && recipebookcategories != RecipeBookCategories.FURNACE_SEARCH) {
            if (recipetabtogglewidget.updateVisibility(this.book)) {
               recipetabtogglewidget.setPosition(i, j + 27 * l++);
               recipetabtogglewidget.startAnimation(this.minecraft);
            }
         } else {
            recipetabtogglewidget.visible = true;
            recipetabtogglewidget.setPosition(i, j + 27 * l++);
         }
      }

   }

   public void tick() {
      if (this.isVisible()) {
         if (this.timesInventoryChanged != this.minecraft.player.inventory.getTimesChanged()) {
            this.updateStackedContents();
            this.timesInventoryChanged = this.minecraft.player.inventory.getTimesChanged();
         }

         this.searchBox.tick();
      }
   }

   private void updateStackedContents() {
      this.stackedContents.clear();
      this.minecraft.player.inventory.fillStackedContents(this.stackedContents);
      this.menu.fillCraftSlotsStackedContents(this.stackedContents);
      this.updateCollections(false);
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      if (this.isVisible()) {
         RenderSystem.pushMatrix();
         RenderSystem.translatef(0.0F, 0.0F, 100.0F);
         this.minecraft.getTextureManager().bind(RECIPE_BOOK_LOCATION);
         RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
         int i = (this.width - 147) / 2 - this.xOffset;
         int j = (this.height - 166) / 2;
         this.blit(pMatrixStack, i, j, 1, 1, 147, 166);
         if (!this.searchBox.isFocused() && this.searchBox.getValue().isEmpty()) {
            drawString(pMatrixStack, this.minecraft.font, SEARCH_HINT, i + 25, j + 14, -1);
         } else {
            this.searchBox.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
         }

         for(RecipeTabToggleWidget recipetabtogglewidget : this.tabButtons) {
            recipetabtogglewidget.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
         }

         this.filterButton.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
         this.recipeBookPage.render(pMatrixStack, i, j, pMouseX, pMouseY, pPartialTicks);
         RenderSystem.popMatrix();
      }
   }

   public void renderTooltip(MatrixStack p_238924_1_, int p_238924_2_, int p_238924_3_, int p_238924_4_, int p_238924_5_) {
      if (this.isVisible()) {
         this.recipeBookPage.renderTooltip(p_238924_1_, p_238924_4_, p_238924_5_);
         if (this.filterButton.isHovered()) {
            ITextComponent itextcomponent = this.getFilterButtonTooltip();
            if (this.minecraft.screen != null) {
               this.minecraft.screen.renderTooltip(p_238924_1_, itextcomponent, p_238924_4_, p_238924_5_);
            }
         }

         this.renderGhostRecipeTooltip(p_238924_1_, p_238924_2_, p_238924_3_, p_238924_4_, p_238924_5_);
      }
   }

   private ITextComponent getFilterButtonTooltip() {
      return this.filterButton.isStateTriggered() ? this.getRecipeFilterName() : ALL_RECIPES_TOOLTIP;
   }

   protected ITextComponent getRecipeFilterName() {
      return ONLY_CRAFTABLES_TOOLTIP;
   }

   private void renderGhostRecipeTooltip(MatrixStack p_238925_1_, int p_238925_2_, int p_238925_3_, int p_238925_4_, int p_238925_5_) {
      ItemStack itemstack = null;

      for(int i = 0; i < this.ghostRecipe.size(); ++i) {
         GhostRecipe.GhostIngredient ghostrecipe$ghostingredient = this.ghostRecipe.get(i);
         int j = ghostrecipe$ghostingredient.getX() + p_238925_2_;
         int k = ghostrecipe$ghostingredient.getY() + p_238925_3_;
         if (p_238925_4_ >= j && p_238925_5_ >= k && p_238925_4_ < j + 16 && p_238925_5_ < k + 16) {
            itemstack = ghostrecipe$ghostingredient.getItem();
         }
      }

      if (itemstack != null && this.minecraft.screen != null) {
         this.minecraft.screen.renderComponentTooltip(p_238925_1_, this.minecraft.screen.getTooltipFromItem(itemstack), p_238925_4_, p_238925_5_);
      }

   }

   public void renderGhostRecipe(MatrixStack p_230477_1_, int p_230477_2_, int p_230477_3_, boolean p_230477_4_, float p_230477_5_) {
      this.ghostRecipe.render(p_230477_1_, this.minecraft, p_230477_2_, p_230477_3_, p_230477_4_, p_230477_5_);
   }

   public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
      if (this.isVisible() && !this.minecraft.player.isSpectator()) {
         if (this.recipeBookPage.mouseClicked(pMouseX, pMouseY, pButton, (this.width - 147) / 2 - this.xOffset, (this.height - 166) / 2, 147, 166)) {
            IRecipe<?> irecipe = this.recipeBookPage.getLastClickedRecipe();
            RecipeList recipelist = this.recipeBookPage.getLastClickedRecipeCollection();
            if (irecipe != null && recipelist != null) {
               if (!recipelist.isCraftable(irecipe) && this.ghostRecipe.getRecipe() == irecipe) {
                  return false;
               }

               this.ghostRecipe.clear();
               this.minecraft.gameMode.handlePlaceRecipe(this.minecraft.player.containerMenu.containerId, irecipe, Screen.hasShiftDown());
               if (!this.isOffsetNextToMainGUI()) {
                  this.setVisible(false);
               }
            }

            return true;
         } else if (this.searchBox.mouseClicked(pMouseX, pMouseY, pButton)) {
            return true;
         } else if (this.filterButton.mouseClicked(pMouseX, pMouseY, pButton)) {
            boolean flag = this.toggleFiltering();
            this.filterButton.setStateTriggered(flag);
            this.sendUpdateSettings();
            this.updateCollections(false);
            return true;
         } else {
            for(RecipeTabToggleWidget recipetabtogglewidget : this.tabButtons) {
               if (recipetabtogglewidget.mouseClicked(pMouseX, pMouseY, pButton)) {
                  if (this.selectedTab != recipetabtogglewidget) {
                     this.selectedTab.setStateTriggered(false);
                     this.selectedTab = recipetabtogglewidget;
                     this.selectedTab.setStateTriggered(true);
                     this.updateCollections(true);
                  }

                  return true;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   private boolean toggleFiltering() {
      RecipeBookCategory recipebookcategory = this.menu.getRecipeBookType();
      boolean flag = !this.book.isFiltering(recipebookcategory);
      this.book.setFiltering(recipebookcategory, flag);
      return flag;
   }

   public boolean hasClickedOutside(double p_195604_1_, double p_195604_3_, int p_195604_5_, int p_195604_6_, int p_195604_7_, int p_195604_8_, int p_195604_9_) {
      if (!this.isVisible()) {
         return true;
      } else {
         boolean flag = p_195604_1_ < (double)p_195604_5_ || p_195604_3_ < (double)p_195604_6_ || p_195604_1_ >= (double)(p_195604_5_ + p_195604_7_) || p_195604_3_ >= (double)(p_195604_6_ + p_195604_8_);
         boolean flag1 = (double)(p_195604_5_ - 147) < p_195604_1_ && p_195604_1_ < (double)p_195604_5_ && (double)p_195604_6_ < p_195604_3_ && p_195604_3_ < (double)(p_195604_6_ + p_195604_8_);
         return flag && !flag1 && !this.selectedTab.isHovered();
      }
   }

   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      this.ignoreTextInput = false;
      if (this.isVisible() && !this.minecraft.player.isSpectator()) {
         if (pKeyCode == 256 && !this.isOffsetNextToMainGUI()) {
            this.setVisible(false);
            return true;
         } else if (this.searchBox.keyPressed(pKeyCode, pScanCode, pModifiers)) {
            this.checkSearchStringUpdate();
            return true;
         } else if (this.searchBox.isFocused() && this.searchBox.isVisible() && pKeyCode != 256) {
            return true;
         } else if (this.minecraft.options.keyChat.matches(pKeyCode, pScanCode) && !this.searchBox.isFocused()) {
            this.ignoreTextInput = true;
            this.searchBox.setFocus(true);
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
      this.ignoreTextInput = false;
      return IGuiEventListener.super.keyReleased(pKeyCode, pScanCode, pModifiers);
   }

   public boolean charTyped(char pCodePoint, int pModifiers) {
      if (this.ignoreTextInput) {
         return false;
      } else if (this.isVisible() && !this.minecraft.player.isSpectator()) {
         if (this.searchBox.charTyped(pCodePoint, pModifiers)) {
            this.checkSearchStringUpdate();
            return true;
         } else {
            return IGuiEventListener.super.charTyped(pCodePoint, pModifiers);
         }
      } else {
         return false;
      }
   }

   public boolean isMouseOver(double pMouseX, double pMouseY) {
      return false;
   }

   private void checkSearchStringUpdate() {
      String s = this.searchBox.getValue().toLowerCase(Locale.ROOT);
      this.pirateSpeechForThePeople(s);
      if (!s.equals(this.lastSearch)) {
         this.updateCollections(false);
         this.lastSearch = s;
      }

   }

   /**
    * Check if we should activate the pirate speak easter egg"
    */
   private void pirateSpeechForThePeople(String pText) {
      if ("excitedze".equals(pText)) {
         LanguageManager languagemanager = this.minecraft.getLanguageManager();
         Language language = languagemanager.getLanguage("en_pt");
         if (languagemanager.getSelected().compareTo(language) == 0) {
            return;
         }

         languagemanager.setSelected(language);
         this.minecraft.options.languageCode = language.getCode();
         net.minecraftforge.client.ForgeHooksClient.refreshResources(this.minecraft, net.minecraftforge.resource.VanillaResourceType.LANGUAGES);
         this.minecraft.options.save();
      }

   }

   private boolean isOffsetNextToMainGUI() {
      return this.xOffset == 86;
   }

   public void recipesUpdated() {
      this.updateTabs();
      if (this.isVisible()) {
         this.updateCollections(false);
      }

   }

   public void recipesShown(List<IRecipe<?>> pRecipes) {
      for(IRecipe<?> irecipe : pRecipes) {
         this.minecraft.player.removeRecipeHighlight(irecipe);
      }

   }

   public void setupGhostRecipe(IRecipe<?> p_193951_1_, List<Slot> p_193951_2_) {
      ItemStack itemstack = p_193951_1_.getResultItem();
      this.ghostRecipe.setRecipe(p_193951_1_);
      this.ghostRecipe.addIngredient(Ingredient.of(itemstack), (p_193951_2_.get(0)).x, (p_193951_2_.get(0)).y);
      this.placeRecipe(this.menu.getGridWidth(), this.menu.getGridHeight(), this.menu.getResultSlotIndex(), p_193951_1_, p_193951_1_.getIngredients().iterator(), 0);
   }

   public void addItemToSlot(Iterator<Ingredient> pIngredients, int pSlot, int pMaxAmount, int pY, int pX) {
      Ingredient ingredient = pIngredients.next();
      if (!ingredient.isEmpty()) {
         Slot slot = this.menu.slots.get(pSlot);
         this.ghostRecipe.addIngredient(ingredient, slot.x, slot.y);
      }

   }

   protected void sendUpdateSettings() {
      if (this.minecraft.getConnection() != null) {
         RecipeBookCategory recipebookcategory = this.menu.getRecipeBookType();
         boolean flag = this.book.getBookSettings().isOpen(recipebookcategory);
         boolean flag1 = this.book.getBookSettings().isFiltering(recipebookcategory);
         this.minecraft.getConnection().send(new CUpdateRecipeBookStatusPacket(recipebookcategory, flag, flag1));
      }

   }
}
