package net.minecraft.client.gui.screen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ChangePageButton;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ReadBookScreen extends Screen {
   public static final ReadBookScreen.IBookInfo EMPTY_ACCESS = new ReadBookScreen.IBookInfo() {
      /**
       * Returns the size of the book
       */
      public int getPageCount() {
         return 0;
      }

      public ITextProperties getPageRaw(int p_230456_1_) {
         return ITextProperties.EMPTY;
      }
   };
   public static final ResourceLocation BOOK_LOCATION = new ResourceLocation("textures/gui/book.png");
   private ReadBookScreen.IBookInfo bookAccess;
   private int currentPage;
   /** Holds a copy of the page text, split into page width lines */
   private List<IReorderingProcessor> cachedPageComponents = Collections.emptyList();
   private int cachedPage = -1;
   private ITextComponent pageMsg = StringTextComponent.EMPTY;
   private ChangePageButton forwardButton;
   private ChangePageButton backButton;
   /** Determines if a sound is played when the page is turned */
   private final boolean playTurnSound;

   public ReadBookScreen(ReadBookScreen.IBookInfo p_i51098_1_) {
      this(p_i51098_1_, true);
   }

   public ReadBookScreen() {
      this(EMPTY_ACCESS, false);
   }

   private ReadBookScreen(ReadBookScreen.IBookInfo p_i51099_1_, boolean p_i51099_2_) {
      super(NarratorChatListener.NO_TITLE);
      this.bookAccess = p_i51099_1_;
      this.playTurnSound = p_i51099_2_;
   }

   public void setBookAccess(ReadBookScreen.IBookInfo p_214155_1_) {
      this.bookAccess = p_214155_1_;
      this.currentPage = MathHelper.clamp(this.currentPage, 0, p_214155_1_.getPageCount());
      this.updateButtonVisibility();
      this.cachedPage = -1;
   }

   /**
    * Moves the book to the specified page and returns true if it exists, false otherwise
    */
   public boolean setPage(int pPageNum) {
      int i = MathHelper.clamp(pPageNum, 0, this.bookAccess.getPageCount() - 1);
      if (i != this.currentPage) {
         this.currentPage = i;
         this.updateButtonVisibility();
         this.cachedPage = -1;
         return true;
      } else {
         return false;
      }
   }

   /**
    * I'm not sure why this exists. The function it calls is public and does all of the work
    */
   protected boolean forcePage(int pPageNum) {
      return this.setPage(pPageNum);
   }

   protected void init() {
      this.createMenuControls();
      this.createPageControlButtons();
   }

   protected void createMenuControls() {
      this.addButton(new Button(this.width / 2 - 100, 196, 200, 20, DialogTexts.GUI_DONE, (p_214161_1_) -> {
         this.minecraft.setScreen((Screen)null);
      }));
   }

   protected void createPageControlButtons() {
      int i = (this.width - 192) / 2;
      int j = 2;
      this.forwardButton = this.addButton(new ChangePageButton(i + 116, 159, true, (p_214159_1_) -> {
         this.pageForward();
      }, this.playTurnSound));
      this.backButton = this.addButton(new ChangePageButton(i + 43, 159, false, (p_214158_1_) -> {
         this.pageBack();
      }, this.playTurnSound));
      this.updateButtonVisibility();
   }

   private int getNumPages() {
      return this.bookAccess.getPageCount();
   }

   /**
    * Moves the display back one page
    */
   protected void pageBack() {
      if (this.currentPage > 0) {
         --this.currentPage;
      }

      this.updateButtonVisibility();
   }

   /**
    * Moves the display forward one page
    */
   protected void pageForward() {
      if (this.currentPage < this.getNumPages() - 1) {
         ++this.currentPage;
      }

      this.updateButtonVisibility();
   }

   private void updateButtonVisibility() {
      this.forwardButton.visible = this.currentPage < this.getNumPages() - 1;
      this.backButton.visible = this.currentPage > 0;
   }

   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (super.keyPressed(pKeyCode, pScanCode, pModifiers)) {
         return true;
      } else {
         switch(pKeyCode) {
         case 266:
            this.backButton.onPress();
            return true;
         case 267:
            this.forwardButton.onPress();
            return true;
         default:
            return false;
         }
      }
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.renderBackground(pMatrixStack);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.minecraft.getTextureManager().bind(BOOK_LOCATION);
      int i = (this.width - 192) / 2;
      int j = 2;
      this.blit(pMatrixStack, i, 2, 0, 0, 192, 192);
      if (this.cachedPage != this.currentPage) {
         ITextProperties itextproperties = this.bookAccess.getPage(this.currentPage);
         this.cachedPageComponents = this.font.split(itextproperties, 114);
         this.pageMsg = new TranslationTextComponent("book.pageIndicator", this.currentPage + 1, Math.max(this.getNumPages(), 1));
      }

      this.cachedPage = this.currentPage;
      int i1 = this.font.width(this.pageMsg);
      this.font.draw(pMatrixStack, this.pageMsg, (float)(i - i1 + 192 - 44), 18.0F, 0);
      int k = Math.min(128 / 9, this.cachedPageComponents.size());

      for(int l = 0; l < k; ++l) {
         IReorderingProcessor ireorderingprocessor = this.cachedPageComponents.get(l);
         this.font.draw(pMatrixStack, ireorderingprocessor, (float)(i + 36), (float)(32 + l * 9), 0);
      }

      Style style = this.getClickedComponentStyleAt((double)pMouseX, (double)pMouseY);
      if (style != null) {
         this.renderComponentHoverEffect(pMatrixStack, style, pMouseX, pMouseY);
      }

      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
   }

   public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
      if (pButton == 0) {
         Style style = this.getClickedComponentStyleAt(pMouseX, pMouseY);
         if (style != null && this.handleComponentClicked(style)) {
            return true;
         }
      }

      return super.mouseClicked(pMouseX, pMouseY, pButton);
   }

   public boolean handleComponentClicked(Style pStyle) {
      ClickEvent clickevent = pStyle.getClickEvent();
      if (clickevent == null) {
         return false;
      } else if (clickevent.getAction() == ClickEvent.Action.CHANGE_PAGE) {
         String s = clickevent.getValue();

         try {
            int i = Integer.parseInt(s) - 1;
            return this.forcePage(i);
         } catch (Exception exception) {
            return false;
         }
      } else {
         boolean flag = super.handleComponentClicked(pStyle);
         if (flag && clickevent.getAction() == ClickEvent.Action.RUN_COMMAND) {
            this.minecraft.setScreen((Screen)null);
         }

         return flag;
      }
   }

   @Nullable
   public Style getClickedComponentStyleAt(double p_238805_1_, double p_238805_3_) {
      if (this.cachedPageComponents.isEmpty()) {
         return null;
      } else {
         int i = MathHelper.floor(p_238805_1_ - (double)((this.width - 192) / 2) - 36.0D);
         int j = MathHelper.floor(p_238805_3_ - 2.0D - 30.0D);
         if (i >= 0 && j >= 0) {
            int k = Math.min(128 / 9, this.cachedPageComponents.size());
            if (i <= 114 && j < 9 * k + k) {
               int l = j / 9;
               if (l >= 0 && l < this.cachedPageComponents.size()) {
                  IReorderingProcessor ireorderingprocessor = this.cachedPageComponents.get(l);
                  return this.minecraft.font.getSplitter().componentStyleAtWidth(ireorderingprocessor, i);
               } else {
                  return null;
               }
            } else {
               return null;
            }
         } else {
            return null;
         }
      }
   }

   public static List<String> convertPages(CompoundNBT p_214157_0_) {
      ListNBT listnbt = p_214157_0_.getList("pages", 8).copy();
      Builder<String> builder = ImmutableList.builder();

      for(int i = 0; i < listnbt.size(); ++i) {
         builder.add(listnbt.getString(i));
      }

      return builder.build();
   }

   @OnlyIn(Dist.CLIENT)
   public interface IBookInfo {
      /**
       * Returns the size of the book
       */
      int getPageCount();

      ITextProperties getPageRaw(int p_230456_1_);

      default ITextProperties getPage(int p_238806_1_) {
         return p_238806_1_ >= 0 && p_238806_1_ < this.getPageCount() ? this.getPageRaw(p_238806_1_) : ITextProperties.EMPTY;
      }

      static ReadBookScreen.IBookInfo fromItem(ItemStack p_216917_0_) {
         Item item = p_216917_0_.getItem();
         if (item == Items.WRITTEN_BOOK) {
            return new ReadBookScreen.WrittenBookInfo(p_216917_0_);
         } else {
            return (ReadBookScreen.IBookInfo)(item == Items.WRITABLE_BOOK ? new ReadBookScreen.UnwrittenBookInfo(p_216917_0_) : ReadBookScreen.EMPTY_ACCESS);
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class UnwrittenBookInfo implements ReadBookScreen.IBookInfo {
      private final List<String> pages;

      public UnwrittenBookInfo(ItemStack p_i50617_1_) {
         this.pages = readPages(p_i50617_1_);
      }

      private static List<String> readPages(ItemStack p_216919_0_) {
         CompoundNBT compoundnbt = p_216919_0_.getTag();
         return (List<String>)(compoundnbt != null ? ReadBookScreen.convertPages(compoundnbt) : ImmutableList.of());
      }

      /**
       * Returns the size of the book
       */
      public int getPageCount() {
         return this.pages.size();
      }

      public ITextProperties getPageRaw(int p_230456_1_) {
         return ITextProperties.of(this.pages.get(p_230456_1_));
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class WrittenBookInfo implements ReadBookScreen.IBookInfo {
      private final List<String> pages;

      public WrittenBookInfo(ItemStack p_i50616_1_) {
         this.pages = readPages(p_i50616_1_);
      }

      private static List<String> readPages(ItemStack p_216921_0_) {
         CompoundNBT compoundnbt = p_216921_0_.getTag();
         return (List<String>)(compoundnbt != null && WrittenBookItem.makeSureTagIsValid(compoundnbt) ? ReadBookScreen.convertPages(compoundnbt) : ImmutableList.of(ITextComponent.Serializer.toJson((new TranslationTextComponent("book.invalid.tag")).withStyle(TextFormatting.DARK_RED))));
      }

      /**
       * Returns the size of the book
       */
      public int getPageCount() {
         return this.pages.size();
      }

      public ITextProperties getPageRaw(int p_230456_1_) {
         String s = this.pages.get(p_230456_1_);

         try {
            ITextProperties itextproperties = ITextComponent.Serializer.fromJson(s);
            if (itextproperties != null) {
               return itextproperties;
            }
         } catch (Exception exception) {
         }

         return ITextProperties.of(s);
      }
   }
}