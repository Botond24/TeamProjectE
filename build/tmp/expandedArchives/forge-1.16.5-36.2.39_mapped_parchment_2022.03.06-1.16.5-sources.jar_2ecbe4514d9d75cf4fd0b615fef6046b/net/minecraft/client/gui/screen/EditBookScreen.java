package net.minecraft.client.gui.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import javax.annotation.Nullable;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.fonts.TextInputUtil;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.ChangePageButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.play.client.CEditBookPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.text.CharacterManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

@OnlyIn(Dist.CLIENT)
public class EditBookScreen extends Screen {
   private static final ITextComponent EDIT_TITLE_LABEL = new TranslationTextComponent("book.editTitle");
   private static final ITextComponent FINALIZE_WARNING_LABEL = new TranslationTextComponent("book.finalizeWarning");
   private static final IReorderingProcessor BLACK_CURSOR = IReorderingProcessor.forward("_", Style.EMPTY.withColor(TextFormatting.BLACK));
   private static final IReorderingProcessor GRAY_CURSOR = IReorderingProcessor.forward("_", Style.EMPTY.withColor(TextFormatting.GRAY));
   private final PlayerEntity owner;
   private final ItemStack book;
   /** Whether the book's title or contents has been modified since being opened */
   private boolean isModified;
   /** Determines if the signing screen is open */
   private boolean isSigning;
   /** Update ticks since the gui was opened */
   private int frameTick;
   private int currentPage;
   private final List<String> pages = Lists.newArrayList();
   private String title = "";
   private final TextInputUtil pageEdit = new TextInputUtil(this::getCurrentPageText, this::setCurrentPageText, this::getClipboard, this::setClipboard, (p_238774_1_) -> {
      return p_238774_1_.length() < 1024 && this.font.wordWrapHeight(p_238774_1_, 114) <= 128;
   });
   private final TextInputUtil titleEdit = new TextInputUtil(() -> {
      return this.title;
   }, (p_238772_1_) -> {
      this.title = p_238772_1_;
   }, this::getClipboard, this::setClipboard, (p_238771_0_) -> {
      return p_238771_0_.length() < 16;
   });
   /** In milliseconds */
   private long lastClickTime;
   private int lastIndex = -1;
   private ChangePageButton forwardButton;
   private ChangePageButton backButton;
   private Button doneButton;
   private Button signButton;
   private Button finalizeButton;
   private Button cancelButton;
   private final Hand hand;
   @Nullable
   private EditBookScreen.BookPage displayCache = EditBookScreen.BookPage.EMPTY;
   private ITextComponent pageMsg = StringTextComponent.EMPTY;
   private final ITextComponent ownerText;

   public EditBookScreen(PlayerEntity pPlayer, ItemStack pBook, Hand pHand) {
      super(NarratorChatListener.NO_TITLE);
      this.owner = pPlayer;
      this.book = pBook;
      this.hand = pHand;
      CompoundNBT compoundnbt = pBook.getTag();
      if (compoundnbt != null) {
         ListNBT listnbt = compoundnbt.getList("pages", 8).copy();

         for(int i = 0; i < listnbt.size(); ++i) {
            this.pages.add(listnbt.getString(i));
         }
      }

      if (this.pages.isEmpty()) {
         this.pages.add("");
      }

      this.ownerText = (new TranslationTextComponent("book.byAuthor", pPlayer.getName())).withStyle(TextFormatting.DARK_GRAY);
   }

   private void setClipboard(String p_238760_1_) {
      if (this.minecraft != null) {
         TextInputUtil.setClipboardContents(this.minecraft, p_238760_1_);
      }

   }

   private String getClipboard() {
      return this.minecraft != null ? TextInputUtil.getClipboardContents(this.minecraft) : "";
   }

   /**
    * Returns the number of pages in the book
    */
   private int getNumPages() {
      return this.pages.size();
   }

   public void tick() {
      super.tick();
      ++this.frameTick;
   }

   protected void init() {
      this.clearDisplayCache();
      this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
      this.signButton = this.addButton(new Button(this.width / 2 - 100, 196, 98, 20, new TranslationTextComponent("book.signButton"), (p_214201_1_) -> {
         this.isSigning = true;
         this.updateButtonVisibility();
      }));
      this.doneButton = this.addButton(new Button(this.width / 2 + 2, 196, 98, 20, DialogTexts.GUI_DONE, (p_214204_1_) -> {
         this.minecraft.setScreen((Screen)null);
         this.saveChanges(false);
      }));
      this.finalizeButton = this.addButton(new Button(this.width / 2 - 100, 196, 98, 20, new TranslationTextComponent("book.finalizeButton"), (p_214195_1_) -> {
         if (this.isSigning) {
            this.saveChanges(true);
            this.minecraft.setScreen((Screen)null);
         }

      }));
      this.cancelButton = this.addButton(new Button(this.width / 2 + 2, 196, 98, 20, DialogTexts.GUI_CANCEL, (p_214212_1_) -> {
         if (this.isSigning) {
            this.isSigning = false;
         }

         this.updateButtonVisibility();
      }));
      int i = (this.width - 192) / 2;
      int j = 2;
      this.forwardButton = this.addButton(new ChangePageButton(i + 116, 159, true, (p_214208_1_) -> {
         this.pageForward();
      }, true));
      this.backButton = this.addButton(new ChangePageButton(i + 43, 159, false, (p_214205_1_) -> {
         this.pageBack();
      }, true));
      this.updateButtonVisibility();
   }

   /**
    * Displays the previous page
    */
   private void pageBack() {
      if (this.currentPage > 0) {
         --this.currentPage;
      }

      this.updateButtonVisibility();
      this.clearDisplayCacheAfterPageChange();
   }

   /**
    * Displays the next page (creating it if necessary)
    */
   private void pageForward() {
      if (this.currentPage < this.getNumPages() - 1) {
         ++this.currentPage;
      } else {
         this.appendPageToBook();
         if (this.currentPage < this.getNumPages() - 1) {
            ++this.currentPage;
         }
      }

      this.updateButtonVisibility();
      this.clearDisplayCacheAfterPageChange();
   }

   public void removed() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
   }

   /**
    * Sets visibility for book buttons
    */
   private void updateButtonVisibility() {
      this.backButton.visible = !this.isSigning && this.currentPage > 0;
      this.forwardButton.visible = !this.isSigning;
      this.doneButton.visible = !this.isSigning;
      this.signButton.visible = !this.isSigning;
      this.cancelButton.visible = this.isSigning;
      this.finalizeButton.visible = this.isSigning;
      this.finalizeButton.active = !this.title.trim().isEmpty();
   }

   private void eraseEmptyTrailingPages() {
      ListIterator<String> listiterator = this.pages.listIterator(this.pages.size());

      while(listiterator.hasPrevious() && listiterator.previous().isEmpty()) {
         listiterator.remove();
      }

   }

   private void saveChanges(boolean pPublish) {
      if (this.isModified) {
         this.eraseEmptyTrailingPages();
         ListNBT listnbt = new ListNBT();
         this.pages.stream().map(StringNBT::valueOf).forEach(listnbt::add);
         if (!this.pages.isEmpty()) {
            this.book.addTagElement("pages", listnbt);
         }

         if (pPublish) {
            this.book.addTagElement("author", StringNBT.valueOf(this.owner.getGameProfile().getName()));
            this.book.addTagElement("title", StringNBT.valueOf(this.title.trim()));
         }

         int i = this.hand == Hand.MAIN_HAND ? this.owner.inventory.selected : 40;
         this.minecraft.getConnection().send(new CEditBookPacket(this.book, pPublish, i));
      }
   }

   /**
    * Adds a new page to the book (capped at 100 pages)
    */
   private void appendPageToBook() {
      if (this.getNumPages() < 100) {
         this.pages.add("");
         this.isModified = true;
      }
   }

   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (super.keyPressed(pKeyCode, pScanCode, pModifiers)) {
         return true;
      } else if (this.isSigning) {
         return this.titleKeyPressed(pKeyCode, pScanCode, pModifiers);
      } else {
         boolean flag = this.bookKeyPressed(pKeyCode, pScanCode, pModifiers);
         if (flag) {
            this.clearDisplayCache();
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean charTyped(char pCodePoint, int pModifiers) {
      if (super.charTyped(pCodePoint, pModifiers)) {
         return true;
      } else if (this.isSigning) {
         boolean flag = this.titleEdit.charTyped(pCodePoint);
         if (flag) {
            this.updateButtonVisibility();
            this.isModified = true;
            return true;
         } else {
            return false;
         }
      } else if (SharedConstants.isAllowedChatCharacter(pCodePoint)) {
         this.pageEdit.insertText(Character.toString(pCodePoint));
         this.clearDisplayCache();
         return true;
      } else {
         return false;
      }
   }

   /**
    * Handles keypresses, clipboard functions, and page turning
    */
   private boolean bookKeyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (Screen.isSelectAll(pKeyCode)) {
         this.pageEdit.selectAll();
         return true;
      } else if (Screen.isCopy(pKeyCode)) {
         this.pageEdit.copy();
         return true;
      } else if (Screen.isPaste(pKeyCode)) {
         this.pageEdit.paste();
         return true;
      } else if (Screen.isCut(pKeyCode)) {
         this.pageEdit.cut();
         return true;
      } else {
         switch(pKeyCode) {
         case 257:
         case 335:
            this.pageEdit.insertText("\n");
            return true;
         case 259:
            this.pageEdit.removeCharsFromCursor(-1);
            return true;
         case 261:
            this.pageEdit.removeCharsFromCursor(1);
            return true;
         case 262:
            this.pageEdit.moveByChars(1, Screen.hasShiftDown());
            return true;
         case 263:
            this.pageEdit.moveByChars(-1, Screen.hasShiftDown());
            return true;
         case 264:
            this.keyDown();
            return true;
         case 265:
            this.keyUp();
            return true;
         case 266:
            this.backButton.onPress();
            return true;
         case 267:
            this.forwardButton.onPress();
            return true;
         case 268:
            this.keyHome();
            return true;
         case 269:
            this.keyEnd();
            return true;
         default:
            return false;
         }
      }
   }

   private void keyUp() {
      this.changeLine(-1);
   }

   private void keyDown() {
      this.changeLine(1);
   }

   private void changeLine(int pYChange) {
      int i = this.pageEdit.getCursorPos();
      int j = this.getDisplayCache().changeLine(i, pYChange);
      this.pageEdit.setCursorPos(j, Screen.hasShiftDown());
   }

   private void keyHome() {
      int i = this.pageEdit.getCursorPos();
      int j = this.getDisplayCache().findLineStart(i);
      this.pageEdit.setCursorPos(j, Screen.hasShiftDown());
   }

   private void keyEnd() {
      EditBookScreen.BookPage editbookscreen$bookpage = this.getDisplayCache();
      int i = this.pageEdit.getCursorPos();
      int j = editbookscreen$bookpage.findLineEnd(i);
      this.pageEdit.setCursorPos(j, Screen.hasShiftDown());
   }

   /**
    * Handles special keys pressed while editing the book's title
    */
   private boolean titleKeyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      switch(pKeyCode) {
      case 257:
      case 335:
         if (!this.title.isEmpty()) {
            this.saveChanges(true);
            this.minecraft.setScreen((Screen)null);
         }

         return true;
      case 259:
         this.titleEdit.removeCharsFromCursor(-1);
         this.updateButtonVisibility();
         this.isModified = true;
         return true;
      default:
         return false;
      }
   }

   /**
    * Returns the contents of the current page as a string (or an empty string if the currPage isn't a valid page index)
    */
   private String getCurrentPageText() {
      return this.currentPage >= 0 && this.currentPage < this.pages.size() ? this.pages.get(this.currentPage) : "";
   }

   private void setCurrentPageText(String p_214217_1_) {
      if (this.currentPage >= 0 && this.currentPage < this.pages.size()) {
         this.pages.set(this.currentPage, p_214217_1_);
         this.isModified = true;
         this.clearDisplayCache();
      }

   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.renderBackground(pMatrixStack);
      this.setFocused((IGuiEventListener)null);
      RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
      this.minecraft.getTextureManager().bind(ReadBookScreen.BOOK_LOCATION);
      int i = (this.width - 192) / 2;
      int j = 2;
      this.blit(pMatrixStack, i, 2, 0, 0, 192, 192);
      if (this.isSigning) {
         boolean flag = this.frameTick / 6 % 2 == 0;
         IReorderingProcessor ireorderingprocessor = IReorderingProcessor.composite(IReorderingProcessor.forward(this.title, Style.EMPTY), flag ? BLACK_CURSOR : GRAY_CURSOR);
         int k = this.font.width(EDIT_TITLE_LABEL);
         this.font.draw(pMatrixStack, EDIT_TITLE_LABEL, (float)(i + 36 + (114 - k) / 2), 34.0F, 0);
         int l = this.font.width(ireorderingprocessor);
         this.font.draw(pMatrixStack, ireorderingprocessor, (float)(i + 36 + (114 - l) / 2), 50.0F, 0);
         int i1 = this.font.width(this.ownerText);
         this.font.draw(pMatrixStack, this.ownerText, (float)(i + 36 + (114 - i1) / 2), 60.0F, 0);
         this.font.drawWordWrap(FINALIZE_WARNING_LABEL, i + 36, 82, 114, 0);
      } else {
         int j1 = this.font.width(this.pageMsg);
         this.font.draw(pMatrixStack, this.pageMsg, (float)(i - j1 + 192 - 44), 18.0F, 0);
         EditBookScreen.BookPage editbookscreen$bookpage = this.getDisplayCache();

         for(EditBookScreen.BookLine editbookscreen$bookline : editbookscreen$bookpage.lines) {
            this.font.draw(pMatrixStack, editbookscreen$bookline.asComponent, (float)editbookscreen$bookline.x, (float)editbookscreen$bookline.y, -16777216);
         }

         this.renderHighlight(editbookscreen$bookpage.selection);
         this.renderCursor(pMatrixStack, editbookscreen$bookpage.cursor, editbookscreen$bookpage.cursorAtEnd);
      }

      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
   }

   private void renderCursor(MatrixStack pPoseStack, EditBookScreen.Point pCursorPos, boolean pIsEndOfText) {
      if (this.frameTick / 6 % 2 == 0) {
         pCursorPos = this.convertLocalToScreen(pCursorPos);
         if (!pIsEndOfText) {
            AbstractGui.fill(pPoseStack, pCursorPos.x, pCursorPos.y - 1, pCursorPos.x + 1, pCursorPos.y + 9, -16777216);
         } else {
            this.font.draw(pPoseStack, "_", (float)pCursorPos.x, (float)pCursorPos.y, 0);
         }
      }

   }

   private void renderHighlight(Rectangle2d[] pSelected) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuilder();
      RenderSystem.color4f(0.0F, 0.0F, 255.0F, 255.0F);
      RenderSystem.disableTexture();
      RenderSystem.enableColorLogicOp();
      RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION);

      for(Rectangle2d rectangle2d : pSelected) {
         int i = rectangle2d.getX();
         int j = rectangle2d.getY();
         int k = i + rectangle2d.getWidth();
         int l = j + rectangle2d.getHeight();
         bufferbuilder.vertex((double)i, (double)l, 0.0D).endVertex();
         bufferbuilder.vertex((double)k, (double)l, 0.0D).endVertex();
         bufferbuilder.vertex((double)k, (double)j, 0.0D).endVertex();
         bufferbuilder.vertex((double)i, (double)j, 0.0D).endVertex();
      }

      tessellator.end();
      RenderSystem.disableColorLogicOp();
      RenderSystem.enableTexture();
   }

   private EditBookScreen.Point convertScreenToLocal(EditBookScreen.Point pScreenPos) {
      return new EditBookScreen.Point(pScreenPos.x - (this.width - 192) / 2 - 36, pScreenPos.y - 32);
   }

   private EditBookScreen.Point convertLocalToScreen(EditBookScreen.Point pLocalScreenPos) {
      return new EditBookScreen.Point(pLocalScreenPos.x + (this.width - 192) / 2 + 36, pLocalScreenPos.y + 32);
   }

   public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
      if (super.mouseClicked(pMouseX, pMouseY, pButton)) {
         return true;
      } else {
         if (pButton == 0) {
            long i = Util.getMillis();
            EditBookScreen.BookPage editbookscreen$bookpage = this.getDisplayCache();
            int j = editbookscreen$bookpage.getIndexAtPosition(this.font, this.convertScreenToLocal(new EditBookScreen.Point((int)pMouseX, (int)pMouseY)));
            if (j >= 0) {
               if (j == this.lastIndex && i - this.lastClickTime < 250L) {
                  if (!this.pageEdit.isSelecting()) {
                     this.selectWord(j);
                  } else {
                     this.pageEdit.selectAll();
                  }
               } else {
                  this.pageEdit.setCursorPos(j, Screen.hasShiftDown());
               }

               this.clearDisplayCache();
            }

            this.lastIndex = j;
            this.lastClickTime = i;
         }

         return true;
      }
   }

   private void selectWord(int pIndex) {
      String s = this.getCurrentPageText();
      this.pageEdit.setSelectionRange(CharacterManager.getWordPosition(s, -1, pIndex, false), CharacterManager.getWordPosition(s, 1, pIndex, false));
   }

   public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
      if (super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY)) {
         return true;
      } else {
         if (pButton == 0) {
            EditBookScreen.BookPage editbookscreen$bookpage = this.getDisplayCache();
            int i = editbookscreen$bookpage.getIndexAtPosition(this.font, this.convertScreenToLocal(new EditBookScreen.Point((int)pMouseX, (int)pMouseY)));
            this.pageEdit.setCursorPos(i, true);
            this.clearDisplayCache();
         }

         return true;
      }
   }

   private EditBookScreen.BookPage getDisplayCache() {
      if (this.displayCache == null) {
         this.displayCache = this.rebuildDisplayCache();
         this.pageMsg = new TranslationTextComponent("book.pageIndicator", this.currentPage + 1, this.getNumPages());
      }

      return this.displayCache;
   }

   private void clearDisplayCache() {
      this.displayCache = null;
   }

   private void clearDisplayCacheAfterPageChange() {
      this.pageEdit.setCursorToEnd();
      this.clearDisplayCache();
   }

   private EditBookScreen.BookPage rebuildDisplayCache() {
      String s = this.getCurrentPageText();
      if (s.isEmpty()) {
         return EditBookScreen.BookPage.EMPTY;
      } else {
         int i = this.pageEdit.getCursorPos();
         int j = this.pageEdit.getSelectionPos();
         IntList intlist = new IntArrayList();
         List<EditBookScreen.BookLine> list = Lists.newArrayList();
         MutableInt mutableint = new MutableInt();
         MutableBoolean mutableboolean = new MutableBoolean();
         CharacterManager charactermanager = this.font.getSplitter();
         charactermanager.splitLines(s, 114, Style.EMPTY, true, (p_238762_6_, p_238762_7_, p_238762_8_) -> {
            int k3 = mutableint.getAndIncrement();
            String s2 = s.substring(p_238762_7_, p_238762_8_);
            mutableboolean.setValue(s2.endsWith("\n"));
            String s3 = StringUtils.stripEnd(s2, " \n");
            int l3 = k3 * 9;
            EditBookScreen.Point editbookscreen$point1 = this.convertLocalToScreen(new EditBookScreen.Point(0, l3));
            intlist.add(p_238762_7_);
            list.add(new EditBookScreen.BookLine(p_238762_6_, s3, editbookscreen$point1.x, editbookscreen$point1.y));
         });
         int[] aint = intlist.toIntArray();
         boolean flag = i == s.length();
         EditBookScreen.Point editbookscreen$point;
         if (flag && mutableboolean.isTrue()) {
            editbookscreen$point = new EditBookScreen.Point(0, list.size() * 9);
         } else {
            int k = findLineFromPos(aint, i);
            int l = this.font.width(s.substring(aint[k], i));
            editbookscreen$point = new EditBookScreen.Point(l, k * 9);
         }

         List<Rectangle2d> list1 = Lists.newArrayList();
         if (i != j) {
            int l2 = Math.min(i, j);
            int i1 = Math.max(i, j);
            int j1 = findLineFromPos(aint, l2);
            int k1 = findLineFromPos(aint, i1);
            if (j1 == k1) {
               int l1 = j1 * 9;
               int i2 = aint[j1];
               list1.add(this.createPartialLineSelection(s, charactermanager, l2, i1, l1, i2));
            } else {
               int i3 = j1 + 1 > aint.length ? s.length() : aint[j1 + 1];
               list1.add(this.createPartialLineSelection(s, charactermanager, l2, i3, j1 * 9, aint[j1]));

               for(int j3 = j1 + 1; j3 < k1; ++j3) {
                  int j2 = j3 * 9;
                  String s1 = s.substring(aint[j3], aint[j3 + 1]);
                  int k2 = (int)charactermanager.stringWidth(s1);
                  list1.add(this.createSelection(new EditBookScreen.Point(0, j2), new EditBookScreen.Point(k2, j2 + 9)));
               }

               list1.add(this.createPartialLineSelection(s, charactermanager, aint[k1], i1, k1 * 9, aint[k1]));
            }
         }

         return new EditBookScreen.BookPage(s, editbookscreen$point, flag, aint, list.toArray(new EditBookScreen.BookLine[0]), list1.toArray(new Rectangle2d[0]));
      }
   }

   private static int findLineFromPos(int[] p_238768_0_, int p_238768_1_) {
      int i = Arrays.binarySearch(p_238768_0_, p_238768_1_);
      return i < 0 ? -(i + 2) : i;
   }

   private Rectangle2d createPartialLineSelection(String p_238761_1_, CharacterManager p_238761_2_, int p_238761_3_, int p_238761_4_, int p_238761_5_, int p_238761_6_) {
      String s = p_238761_1_.substring(p_238761_6_, p_238761_3_);
      String s1 = p_238761_1_.substring(p_238761_6_, p_238761_4_);
      EditBookScreen.Point editbookscreen$point = new EditBookScreen.Point((int)p_238761_2_.stringWidth(s), p_238761_5_);
      EditBookScreen.Point editbookscreen$point1 = new EditBookScreen.Point((int)p_238761_2_.stringWidth(s1), p_238761_5_ + 9);
      return this.createSelection(editbookscreen$point, editbookscreen$point1);
   }

   private Rectangle2d createSelection(EditBookScreen.Point p_238759_1_, EditBookScreen.Point p_238759_2_) {
      EditBookScreen.Point editbookscreen$point = this.convertLocalToScreen(p_238759_1_);
      EditBookScreen.Point editbookscreen$point1 = this.convertLocalToScreen(p_238759_2_);
      int i = Math.min(editbookscreen$point.x, editbookscreen$point1.x);
      int j = Math.max(editbookscreen$point.x, editbookscreen$point1.x);
      int k = Math.min(editbookscreen$point.y, editbookscreen$point1.y);
      int l = Math.max(editbookscreen$point.y, editbookscreen$point1.y);
      return new Rectangle2d(i, k, j - i, l - k);
   }

   @OnlyIn(Dist.CLIENT)
   static class BookLine {
      private final Style style;
      private final String contents;
      private final ITextComponent asComponent;
      private final int x;
      private final int y;

      public BookLine(Style pStyle, String pContents, int pX, int pY) {
         this.style = pStyle;
         this.contents = pContents;
         this.x = pX;
         this.y = pY;
         this.asComponent = (new StringTextComponent(pContents)).setStyle(pStyle);
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class BookPage {
      private static final EditBookScreen.BookPage EMPTY = new EditBookScreen.BookPage("", new EditBookScreen.Point(0, 0), true, new int[]{0}, new EditBookScreen.BookLine[]{new EditBookScreen.BookLine(Style.EMPTY, "", 0, 0)}, new Rectangle2d[0]);
      private final String fullText;
      private final EditBookScreen.Point cursor;
      private final boolean cursorAtEnd;
      private final int[] lineStarts;
      private final EditBookScreen.BookLine[] lines;
      private final Rectangle2d[] selection;

      public BookPage(String pFullText, EditBookScreen.Point pCursor, boolean pIsCursorAtEnd, int[] pLineStarts, EditBookScreen.BookLine[] pLines, Rectangle2d[] pSelection) {
         this.fullText = pFullText;
         this.cursor = pCursor;
         this.cursorAtEnd = pIsCursorAtEnd;
         this.lineStarts = pLineStarts;
         this.lines = pLines;
         this.selection = pSelection;
      }

      public int getIndexAtPosition(FontRenderer pFont, EditBookScreen.Point pCursorPosition) {
         int i = pCursorPosition.y / 9;
         if (i < 0) {
            return 0;
         } else if (i >= this.lines.length) {
            return this.fullText.length();
         } else {
            EditBookScreen.BookLine editbookscreen$bookline = this.lines[i];
            return this.lineStarts[i] + pFont.getSplitter().plainIndexAtWidth(editbookscreen$bookline.contents, pCursorPosition.x, editbookscreen$bookline.style);
         }
      }

      public int changeLine(int p_238788_1_, int p_238788_2_) {
         int i = EditBookScreen.findLineFromPos(this.lineStarts, p_238788_1_);
         int j = i + p_238788_2_;
         int k;
         if (0 <= j && j < this.lineStarts.length) {
            int l = p_238788_1_ - this.lineStarts[i];
            int i1 = this.lines[j].contents.length();
            k = this.lineStarts[j] + Math.min(l, i1);
         } else {
            k = p_238788_1_;
         }

         return k;
      }

      public int findLineStart(int p_238787_1_) {
         int i = EditBookScreen.findLineFromPos(this.lineStarts, p_238787_1_);
         return this.lineStarts[i];
      }

      public int findLineEnd(int p_238791_1_) {
         int i = EditBookScreen.findLineFromPos(this.lineStarts, p_238791_1_);
         return this.lineStarts[i] + this.lines[i].contents.length();
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class Point {
      public final int x;
      public final int y;

      Point(int pX, int pY) {
         this.x = pX;
         this.y = pY;
      }
   }
}