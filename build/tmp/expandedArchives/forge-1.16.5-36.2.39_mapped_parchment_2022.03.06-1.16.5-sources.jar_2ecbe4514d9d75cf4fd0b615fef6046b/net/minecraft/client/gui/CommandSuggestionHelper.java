package net.minecraft.client.gui;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.SuggestionContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CommandSuggestionHelper {
   private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");
   private static final Style UNPARSED_STYLE = Style.EMPTY.withColor(TextFormatting.RED);
   private static final Style LITERAL_STYLE = Style.EMPTY.withColor(TextFormatting.GRAY);
   private static final List<Style> ARGUMENT_STYLES = Stream.of(TextFormatting.AQUA, TextFormatting.YELLOW, TextFormatting.GREEN, TextFormatting.LIGHT_PURPLE, TextFormatting.GOLD).map(Style.EMPTY::withColor).collect(ImmutableList.toImmutableList());
   private final Minecraft minecraft;
   private final Screen screen;
   private final TextFieldWidget input;
   private final FontRenderer font;
   private final boolean commandsOnly;
   private final boolean onlyShowIfCursorPastError;
   private final int lineStartOffset;
   private final int suggestionLineLimit;
   private final boolean anchorToBottom;
   private final int fillColor;
   private final List<IReorderingProcessor> commandUsage = Lists.newArrayList();
   private int commandUsagePosition;
   private int commandUsageWidth;
   private ParseResults<ISuggestionProvider> currentParse;
   private CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> pendingSuggestions;
   private CommandSuggestionHelper.Suggestions suggestions;
   private boolean allowSuggestions;
   private boolean keepSuggestions;

   public CommandSuggestionHelper(Minecraft pMinecraft, Screen pScreen, TextFieldWidget pInput, FontRenderer pFont, boolean pCommandsOnly, boolean pOnlyShowIfCursorPastError, int pLineStartOffset, int pSuggestionLineLimit, boolean pAnchorToBottom, int pFillColor) {
      this.minecraft = pMinecraft;
      this.screen = pScreen;
      this.input = pInput;
      this.font = pFont;
      this.commandsOnly = pCommandsOnly;
      this.onlyShowIfCursorPastError = pOnlyShowIfCursorPastError;
      this.lineStartOffset = pLineStartOffset;
      this.suggestionLineLimit = pSuggestionLineLimit;
      this.anchorToBottom = pAnchorToBottom;
      this.fillColor = pFillColor;
      pInput.setFormatter(this::formatChat);
   }

   public void setAllowSuggestions(boolean pAutoSuggest) {
      this.allowSuggestions = pAutoSuggest;
      if (!pAutoSuggest) {
         this.suggestions = null;
      }

   }

   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (this.suggestions != null && this.suggestions.keyPressed(pKeyCode, pScanCode, pModifiers)) {
         return true;
      } else if (this.screen.getFocused() == this.input && pKeyCode == 258) {
         this.showSuggestions(true);
         return true;
      } else {
         return false;
      }
   }

   public boolean mouseScrolled(double pDelta) {
      return this.suggestions != null && this.suggestions.mouseScrolled(MathHelper.clamp(pDelta, -1.0D, 1.0D));
   }

   public boolean mouseClicked(double pMouseX, double pMouseY, int pMouseButton) {
      return this.suggestions != null && this.suggestions.mouseClicked((int)pMouseX, (int)pMouseY, pMouseButton);
   }

   public void showSuggestions(boolean pNarrateFirstSuggestion) {
      if (this.pendingSuggestions != null && this.pendingSuggestions.isDone()) {
         com.mojang.brigadier.suggestion.Suggestions suggestions = this.pendingSuggestions.join();
         if (!suggestions.isEmpty()) {
            int i = 0;

            for(Suggestion suggestion : suggestions.getList()) {
               i = Math.max(i, this.font.width(suggestion.getText()));
            }

            int j = MathHelper.clamp(this.input.getScreenX(suggestions.getRange().getStart()), 0, this.input.getScreenX(0) + this.input.getInnerWidth() - i);
            int k = this.anchorToBottom ? this.screen.height - 12 : 72;
            this.suggestions = new CommandSuggestionHelper.Suggestions(j, k, i, this.sortSuggestions(suggestions), pNarrateFirstSuggestion);
         }
      }

   }

   private List<Suggestion> sortSuggestions(com.mojang.brigadier.suggestion.Suggestions pSuggestions) {
      String s = this.input.getValue().substring(0, this.input.getCursorPosition());
      int i = getLastWordIndex(s);
      String s1 = s.substring(i).toLowerCase(Locale.ROOT);
      List<Suggestion> list = Lists.newArrayList();
      List<Suggestion> list1 = Lists.newArrayList();

      for(Suggestion suggestion : pSuggestions.getList()) {
         if (!suggestion.getText().startsWith(s1) && !suggestion.getText().startsWith("minecraft:" + s1)) {
            list1.add(suggestion);
         } else {
            list.add(suggestion);
         }
      }

      list.addAll(list1);
      return list;
   }

   public void updateCommandInfo() {
      String s = this.input.getValue();
      if (this.currentParse != null && !this.currentParse.getReader().getString().equals(s)) {
         this.currentParse = null;
      }

      if (!this.keepSuggestions) {
         this.input.setSuggestion((String)null);
         this.suggestions = null;
      }

      this.commandUsage.clear();
      StringReader stringreader = new StringReader(s);
      boolean flag = stringreader.canRead() && stringreader.peek() == '/';
      if (flag) {
         stringreader.skip();
      }

      boolean flag1 = this.commandsOnly || flag;
      int i = this.input.getCursorPosition();
      if (flag1) {
         CommandDispatcher<ISuggestionProvider> commanddispatcher = this.minecraft.player.connection.getCommands();
         if (this.currentParse == null) {
            this.currentParse = commanddispatcher.parse(stringreader, this.minecraft.player.connection.getSuggestionsProvider());
         }

         int j = this.onlyShowIfCursorPastError ? stringreader.getCursor() : 1;
         if (i >= j && (this.suggestions == null || !this.keepSuggestions)) {
            this.pendingSuggestions = commanddispatcher.getCompletionSuggestions(this.currentParse, i);
            this.pendingSuggestions.thenRun(() -> {
               if (this.pendingSuggestions.isDone()) {
                  this.updateUsageInfo();
               }
            });
         }
      } else {
         String s1 = s.substring(0, i);
         int k = getLastWordIndex(s1);
         Collection<String> collection = this.minecraft.player.connection.getSuggestionsProvider().getOnlinePlayerNames();
         this.pendingSuggestions = ISuggestionProvider.suggest(collection, new SuggestionsBuilder(s1, k));
      }

   }

   private static int getLastWordIndex(String pText) {
      if (Strings.isNullOrEmpty(pText)) {
         return 0;
      } else {
         int i = 0;

         for(Matcher matcher = WHITESPACE_PATTERN.matcher(pText); matcher.find(); i = matcher.end()) {
         }

         return i;
      }
   }

   private static IReorderingProcessor getExceptionMessage(CommandSyntaxException pException) {
      ITextComponent itextcomponent = TextComponentUtils.fromMessage(pException.getRawMessage());
      String s = pException.getContext();
      return s == null ? itextcomponent.getVisualOrderText() : (new TranslationTextComponent("command.context.parse_error", itextcomponent, pException.getCursor(), s)).getVisualOrderText();
   }

   private void updateUsageInfo() {
      if (this.input.getCursorPosition() == this.input.getValue().length()) {
         if (this.pendingSuggestions.join().isEmpty() && !this.currentParse.getExceptions().isEmpty()) {
            int i = 0;

            for(Entry<CommandNode<ISuggestionProvider>, CommandSyntaxException> entry : this.currentParse.getExceptions().entrySet()) {
               CommandSyntaxException commandsyntaxexception = entry.getValue();
               if (commandsyntaxexception.getType() == CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect()) {
                  ++i;
               } else {
                  this.commandUsage.add(getExceptionMessage(commandsyntaxexception));
               }
            }

            if (i > 0) {
               this.commandUsage.add(getExceptionMessage(CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().create()));
            }
         } else if (this.currentParse.getReader().canRead()) {
            this.commandUsage.add(getExceptionMessage(Commands.getParseException(this.currentParse)));
         }
      }

      this.commandUsagePosition = 0;
      this.commandUsageWidth = this.screen.width;
      if (this.commandUsage.isEmpty()) {
         this.fillNodeUsage(TextFormatting.GRAY);
      }

      this.suggestions = null;
      if (this.allowSuggestions && this.minecraft.options.autoSuggestions) {
         this.showSuggestions(false);
      }

   }

   private void fillNodeUsage(TextFormatting pFormatting) {
      CommandContextBuilder<ISuggestionProvider> commandcontextbuilder = this.currentParse.getContext();
      SuggestionContext<ISuggestionProvider> suggestioncontext = commandcontextbuilder.findSuggestionContext(this.input.getCursorPosition());
      Map<CommandNode<ISuggestionProvider>, String> map = this.minecraft.player.connection.getCommands().getSmartUsage(suggestioncontext.parent, this.minecraft.player.connection.getSuggestionsProvider());
      List<IReorderingProcessor> list = Lists.newArrayList();
      int i = 0;
      Style style = Style.EMPTY.withColor(pFormatting);

      for(Entry<CommandNode<ISuggestionProvider>, String> entry : map.entrySet()) {
         if (!(entry.getKey() instanceof LiteralCommandNode)) {
            list.add(IReorderingProcessor.forward(entry.getValue(), style));
            i = Math.max(i, this.font.width(entry.getValue()));
         }
      }

      if (!list.isEmpty()) {
         this.commandUsage.addAll(list);
         this.commandUsagePosition = MathHelper.clamp(this.input.getScreenX(suggestioncontext.startPos), 0, this.input.getScreenX(0) + this.input.getInnerWidth() - i);
         this.commandUsageWidth = i;
      }

   }

   private IReorderingProcessor formatChat(String p_228122_1_, int p_228122_2_) {
      return this.currentParse != null ? formatText(this.currentParse, p_228122_1_, p_228122_2_) : IReorderingProcessor.forward(p_228122_1_, Style.EMPTY);
   }

   @Nullable
   private static String calculateSuggestionSuffix(String pInputText, String pSuggestionText) {
      return pSuggestionText.startsWith(pInputText) ? pSuggestionText.substring(pInputText.length()) : null;
   }

   private static IReorderingProcessor formatText(ParseResults<ISuggestionProvider> pProvider, String pCommand, int pMaxLength) {
      List<IReorderingProcessor> list = Lists.newArrayList();
      int i = 0;
      int j = -1;
      CommandContextBuilder<ISuggestionProvider> commandcontextbuilder = pProvider.getContext().getLastChild();

      for(ParsedArgument<ISuggestionProvider, ?> parsedargument : commandcontextbuilder.getArguments().values()) {
         ++j;
         if (j >= ARGUMENT_STYLES.size()) {
            j = 0;
         }

         int k = Math.max(parsedargument.getRange().getStart() - pMaxLength, 0);
         if (k >= pCommand.length()) {
            break;
         }

         int l = Math.min(parsedargument.getRange().getEnd() - pMaxLength, pCommand.length());
         if (l > 0) {
            list.add(IReorderingProcessor.forward(pCommand.substring(i, k), LITERAL_STYLE));
            list.add(IReorderingProcessor.forward(pCommand.substring(k, l), ARGUMENT_STYLES.get(j)));
            i = l;
         }
      }

      if (pProvider.getReader().canRead()) {
         int i1 = Math.max(pProvider.getReader().getCursor() - pMaxLength, 0);
         if (i1 < pCommand.length()) {
            int j1 = Math.min(i1 + pProvider.getReader().getRemainingLength(), pCommand.length());
            list.add(IReorderingProcessor.forward(pCommand.substring(i, i1), LITERAL_STYLE));
            list.add(IReorderingProcessor.forward(pCommand.substring(i1, j1), UNPARSED_STYLE));
            i = j1;
         }
      }

      list.add(IReorderingProcessor.forward(pCommand.substring(i), LITERAL_STYLE));
      return IReorderingProcessor.composite(list);
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY) {
      if (this.suggestions != null) {
         this.suggestions.render(pMatrixStack, pMouseX, pMouseY);
      } else {
         int i = 0;

         for(IReorderingProcessor ireorderingprocessor : this.commandUsage) {
            int j = this.anchorToBottom ? this.screen.height - 14 - 13 - 12 * i : 72 + 12 * i;
            AbstractGui.fill(pMatrixStack, this.commandUsagePosition - 1, j, this.commandUsagePosition + this.commandUsageWidth + 1, j + 12, this.fillColor);
            this.font.drawShadow(pMatrixStack, ireorderingprocessor, (float)this.commandUsagePosition, (float)(j + 2), -1);
            ++i;
         }
      }

   }

   public String getNarrationMessage() {
      return this.suggestions != null ? "\n" + this.suggestions.getNarrationMessage() : "";
   }

   @OnlyIn(Dist.CLIENT)
   public class Suggestions {
      private final Rectangle2d rect;
      private final String originalContents;
      private final List<Suggestion> suggestionList;
      private int offset;
      private int current;
      private Vector2f lastMouse = Vector2f.ZERO;
      private boolean tabCycles;
      private int lastNarratedEntry;

      private Suggestions(int p_i241247_2_, int p_i241247_3_, int p_i241247_4_, List<Suggestion> p_i241247_5_, boolean p_i241247_6_) {
         int i = p_i241247_2_ - 1;
         int j = CommandSuggestionHelper.this.anchorToBottom ? p_i241247_3_ - 3 - Math.min(p_i241247_5_.size(), CommandSuggestionHelper.this.suggestionLineLimit) * 12 : p_i241247_3_;
         this.rect = new Rectangle2d(i, j, p_i241247_4_ + 1, Math.min(p_i241247_5_.size(), CommandSuggestionHelper.this.suggestionLineLimit) * 12);
         this.originalContents = CommandSuggestionHelper.this.input.getValue();
         this.lastNarratedEntry = p_i241247_6_ ? -1 : 0;
         this.suggestionList = p_i241247_5_;
         this.select(0);
      }

      public void render(MatrixStack pPoseStack, int pMouseX, int pMouseY) {
         int i = Math.min(this.suggestionList.size(), CommandSuggestionHelper.this.suggestionLineLimit);
         int j = -5592406;
         boolean flag = this.offset > 0;
         boolean flag1 = this.suggestionList.size() > this.offset + i;
         boolean flag2 = flag || flag1;
         boolean flag3 = this.lastMouse.x != (float)pMouseX || this.lastMouse.y != (float)pMouseY;
         if (flag3) {
            this.lastMouse = new Vector2f((float)pMouseX, (float)pMouseY);
         }

         if (flag2) {
            AbstractGui.fill(pPoseStack, this.rect.getX(), this.rect.getY() - 1, this.rect.getX() + this.rect.getWidth(), this.rect.getY(), CommandSuggestionHelper.this.fillColor);
            AbstractGui.fill(pPoseStack, this.rect.getX(), this.rect.getY() + this.rect.getHeight(), this.rect.getX() + this.rect.getWidth(), this.rect.getY() + this.rect.getHeight() + 1, CommandSuggestionHelper.this.fillColor);
            if (flag) {
               for(int k = 0; k < this.rect.getWidth(); ++k) {
                  if (k % 2 == 0) {
                     AbstractGui.fill(pPoseStack, this.rect.getX() + k, this.rect.getY() - 1, this.rect.getX() + k + 1, this.rect.getY(), -1);
                  }
               }
            }

            if (flag1) {
               for(int i1 = 0; i1 < this.rect.getWidth(); ++i1) {
                  if (i1 % 2 == 0) {
                     AbstractGui.fill(pPoseStack, this.rect.getX() + i1, this.rect.getY() + this.rect.getHeight(), this.rect.getX() + i1 + 1, this.rect.getY() + this.rect.getHeight() + 1, -1);
                  }
               }
            }
         }

         boolean flag4 = false;

         for(int l = 0; l < i; ++l) {
            Suggestion suggestion = this.suggestionList.get(l + this.offset);
            AbstractGui.fill(pPoseStack, this.rect.getX(), this.rect.getY() + 12 * l, this.rect.getX() + this.rect.getWidth(), this.rect.getY() + 12 * l + 12, CommandSuggestionHelper.this.fillColor);
            if (pMouseX > this.rect.getX() && pMouseX < this.rect.getX() + this.rect.getWidth() && pMouseY > this.rect.getY() + 12 * l && pMouseY < this.rect.getY() + 12 * l + 12) {
               if (flag3) {
                  this.select(l + this.offset);
               }

               flag4 = true;
            }

            CommandSuggestionHelper.this.font.drawShadow(pPoseStack, suggestion.getText(), (float)(this.rect.getX() + 1), (float)(this.rect.getY() + 2 + 12 * l), l + this.offset == this.current ? -256 : -5592406);
         }

         if (flag4) {
            Message message = this.suggestionList.get(this.current).getTooltip();
            if (message != null) {
               CommandSuggestionHelper.this.screen.renderTooltip(pPoseStack, TextComponentUtils.fromMessage(message), pMouseX, pMouseY);
            }
         }

      }

      public boolean mouseClicked(int pMouseX, int pMouseY, int pMouseButton) {
         if (!this.rect.contains(pMouseX, pMouseY)) {
            return false;
         } else {
            int i = (pMouseY - this.rect.getY()) / 12 + this.offset;
            if (i >= 0 && i < this.suggestionList.size()) {
               this.select(i);
               this.useSuggestion();
            }

            return true;
         }
      }

      public boolean mouseScrolled(double pDelta) {
         int i = (int)(CommandSuggestionHelper.this.minecraft.mouseHandler.xpos() * (double)CommandSuggestionHelper.this.minecraft.getWindow().getGuiScaledWidth() / (double)CommandSuggestionHelper.this.minecraft.getWindow().getScreenWidth());
         int j = (int)(CommandSuggestionHelper.this.minecraft.mouseHandler.ypos() * (double)CommandSuggestionHelper.this.minecraft.getWindow().getGuiScaledHeight() / (double)CommandSuggestionHelper.this.minecraft.getWindow().getScreenHeight());
         if (this.rect.contains(i, j)) {
            this.offset = MathHelper.clamp((int)((double)this.offset - pDelta), 0, Math.max(this.suggestionList.size() - CommandSuggestionHelper.this.suggestionLineLimit, 0));
            return true;
         } else {
            return false;
         }
      }

      public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
         if (pKeyCode == 265) {
            this.cycle(-1);
            this.tabCycles = false;
            return true;
         } else if (pKeyCode == 264) {
            this.cycle(1);
            this.tabCycles = false;
            return true;
         } else if (pKeyCode == 258) {
            if (this.tabCycles) {
               this.cycle(Screen.hasShiftDown() ? -1 : 1);
            }

            this.useSuggestion();
            return true;
         } else if (pKeyCode == 256) {
            this.hide();
            return true;
         } else {
            return false;
         }
      }

      public void cycle(int pChange) {
         this.select(this.current + pChange);
         int i = this.offset;
         int j = this.offset + CommandSuggestionHelper.this.suggestionLineLimit - 1;
         if (this.current < i) {
            this.offset = MathHelper.clamp(this.current, 0, Math.max(this.suggestionList.size() - CommandSuggestionHelper.this.suggestionLineLimit, 0));
         } else if (this.current > j) {
            this.offset = MathHelper.clamp(this.current + CommandSuggestionHelper.this.lineStartOffset - CommandSuggestionHelper.this.suggestionLineLimit, 0, Math.max(this.suggestionList.size() - CommandSuggestionHelper.this.suggestionLineLimit, 0));
         }

      }

      public void select(int pIndex) {
         this.current = pIndex;
         if (this.current < 0) {
            this.current += this.suggestionList.size();
         }

         if (this.current >= this.suggestionList.size()) {
            this.current -= this.suggestionList.size();
         }

         Suggestion suggestion = this.suggestionList.get(this.current);
         CommandSuggestionHelper.this.input.setSuggestion(CommandSuggestionHelper.calculateSuggestionSuffix(CommandSuggestionHelper.this.input.getValue(), suggestion.apply(this.originalContents)));
         if (NarratorChatListener.INSTANCE.isActive() && this.lastNarratedEntry != this.current) {
            NarratorChatListener.INSTANCE.sayNow(this.getNarrationMessage());
         }

      }

      public void useSuggestion() {
         Suggestion suggestion = this.suggestionList.get(this.current);
         CommandSuggestionHelper.this.keepSuggestions = true;
         CommandSuggestionHelper.this.input.setValue(suggestion.apply(this.originalContents));
         int i = suggestion.getRange().getStart() + suggestion.getText().length();
         CommandSuggestionHelper.this.input.setCursorPosition(i);
         CommandSuggestionHelper.this.input.setHighlightPos(i);
         this.select(this.current);
         CommandSuggestionHelper.this.keepSuggestions = false;
         this.tabCycles = true;
      }

      private String getNarrationMessage() {
         this.lastNarratedEntry = this.current;
         Suggestion suggestion = this.suggestionList.get(this.current);
         Message message = suggestion.getTooltip();
         return message != null ? I18n.get("narration.suggestion.tooltip", this.current + 1, this.suggestionList.size(), suggestion.getText(), message.getString()) : I18n.get("narration.suggestion", this.current + 1, this.suggestionList.size(), suggestion.getText());
      }

      public void hide() {
         CommandSuggestionHelper.this.suggestions = null;
      }
   }
}