package net.minecraft.client.gui.social;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Collection;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.screen.ConfirmOpenLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SocialInteractionsScreen extends Screen {
   protected static final ResourceLocation SOCIAL_INTERACTIONS_LOCATION = new ResourceLocation("textures/gui/social_interactions.png");
   private static final ITextComponent TAB_ALL = new TranslationTextComponent("gui.socialInteractions.tab_all");
   private static final ITextComponent TAB_HIDDEN = new TranslationTextComponent("gui.socialInteractions.tab_hidden");
   private static final ITextComponent TAB_BLOCKED = new TranslationTextComponent("gui.socialInteractions.tab_blocked");
   private static final ITextComponent TAB_ALL_SELECTED = TAB_ALL.plainCopy().withStyle(TextFormatting.UNDERLINE);
   private static final ITextComponent TAB_HIDDEN_SELECTED = TAB_HIDDEN.plainCopy().withStyle(TextFormatting.UNDERLINE);
   private static final ITextComponent TAB_BLOCKED_SELECTED = TAB_BLOCKED.plainCopy().withStyle(TextFormatting.UNDERLINE);
   private static final ITextComponent SEARCH_HINT = (new TranslationTextComponent("gui.socialInteractions.search_hint")).withStyle(TextFormatting.ITALIC).withStyle(TextFormatting.GRAY);
   private static final ITextComponent EMPTY_SEARCH = (new TranslationTextComponent("gui.socialInteractions.search_empty")).withStyle(TextFormatting.GRAY);
   private static final ITextComponent EMPTY_HIDDEN = (new TranslationTextComponent("gui.socialInteractions.empty_hidden")).withStyle(TextFormatting.GRAY);
   private static final ITextComponent EMPTY_BLOCKED = (new TranslationTextComponent("gui.socialInteractions.empty_blocked")).withStyle(TextFormatting.GRAY);
   private static final ITextComponent BLOCKING_HINT = new TranslationTextComponent("gui.socialInteractions.blocking_hint");
   private FilterList socialInteractionsPlayerList;
   private TextFieldWidget searchBox;
   private String lastSearch = "";
   private SocialInteractionsScreen.Mode page = SocialInteractionsScreen.Mode.ALL;
   private Button allButton;
   private Button hiddenButton;
   private Button blockedButton;
   private Button blockingHintButton;
   @Nullable
   private ITextComponent serverLabel;
   private int playerCount;
   private boolean initialized;
   @Nullable
   private Runnable postRenderRunnable;

   public SocialInteractionsScreen() {
      super(new TranslationTextComponent("gui.socialInteractions.title"));
      this.updateServerLabel(Minecraft.getInstance());
   }

   private int windowHeight() {
      return Math.max(52, this.height - 128 - 16);
   }

   private int backgroundUnits() {
      return this.windowHeight() / 16;
   }

   private int listEnd() {
      return 80 + this.backgroundUnits() * 16 - 8;
   }

   private int marginX() {
      return (this.width - 238) / 2;
   }

   public String getNarrationMessage() {
      return super.getNarrationMessage() + ". " + this.serverLabel.getString();
   }

   public void tick() {
      super.tick();
      this.searchBox.tick();
   }

   protected void init() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
      if (this.initialized) {
         this.socialInteractionsPlayerList.updateSize(this.width, this.height, 88, this.listEnd());
      } else {
         this.socialInteractionsPlayerList = new FilterList(this, this.minecraft, this.width, this.height, 88, this.listEnd(), 36);
      }

      int i = this.socialInteractionsPlayerList.getRowWidth() / 3;
      int j = this.socialInteractionsPlayerList.getRowLeft();
      int k = this.socialInteractionsPlayerList.getRowRight();
      int l = this.font.width(BLOCKING_HINT) + 40;
      int i1 = 64 + 16 * this.backgroundUnits();
      int j1 = (this.width - l) / 2;
      this.allButton = this.addButton(new Button(j, 45, i, 20, TAB_ALL, (p_244686_1_) -> {
         this.showPage(SocialInteractionsScreen.Mode.ALL);
      }));
      this.hiddenButton = this.addButton(new Button((j + k - i) / 2 + 1, 45, i, 20, TAB_HIDDEN, (p_244681_1_) -> {
         this.showPage(SocialInteractionsScreen.Mode.HIDDEN);
      }));
      this.blockedButton = this.addButton(new Button(k - i + 1, 45, i, 20, TAB_BLOCKED, (p_244769_1_) -> {
         this.showPage(SocialInteractionsScreen.Mode.BLOCKED);
      }));
      this.blockingHintButton = this.addButton(new Button(j1, i1, l, 20, BLOCKING_HINT, (p_244767_1_) -> {
         this.minecraft.setScreen(new ConfirmOpenLinkScreen((p_244771_1_) -> {
            if (p_244771_1_) {
               Util.getPlatform().openUri("https://aka.ms/javablocking");
            }

            this.minecraft.setScreen(this);
         }, "https://aka.ms/javablocking", true));
      }));
      String s = this.searchBox != null ? this.searchBox.getValue() : "";
      this.searchBox = new TextFieldWidget(this.font, this.marginX() + 28, 78, 196, 16, SEARCH_HINT) {
         protected IFormattableTextComponent createNarrationMessage() {
            return !SocialInteractionsScreen.this.searchBox.getValue().isEmpty() && SocialInteractionsScreen.this.socialInteractionsPlayerList.isEmpty() ? super.createNarrationMessage().append(", ").append(SocialInteractionsScreen.EMPTY_SEARCH) : super.createNarrationMessage();
         }
      };
      this.searchBox.setMaxLength(16);
      this.searchBox.setBordered(false);
      this.searchBox.setVisible(true);
      this.searchBox.setTextColor(16777215);
      this.searchBox.setValue(s);
      this.searchBox.setResponder(this::checkSearchStringUpdate);
      this.children.add(this.searchBox);
      this.children.add(this.socialInteractionsPlayerList);
      this.initialized = true;
      this.showPage(this.page);
   }

   private void showPage(SocialInteractionsScreen.Mode p_244682_1_) {
      this.page = p_244682_1_;
      this.allButton.setMessage(TAB_ALL);
      this.hiddenButton.setMessage(TAB_HIDDEN);
      this.blockedButton.setMessage(TAB_BLOCKED);
      Collection<UUID> collection;
      switch(p_244682_1_) {
      case ALL:
         this.allButton.setMessage(TAB_ALL_SELECTED);
         collection = this.minecraft.player.connection.getOnlinePlayerIds();
         break;
      case HIDDEN:
         this.hiddenButton.setMessage(TAB_HIDDEN_SELECTED);
         collection = this.minecraft.getPlayerSocialManager().getHiddenPlayers();
         break;
      case BLOCKED:
         this.blockedButton.setMessage(TAB_BLOCKED_SELECTED);
         FilterManager filtermanager = this.minecraft.getPlayerSocialManager();
         collection = this.minecraft.player.connection.getOnlinePlayerIds().stream().filter(filtermanager::isBlocked).collect(Collectors.toSet());
         break;
      default:
         collection = ImmutableList.of();
      }

      this.page = p_244682_1_;
      this.socialInteractionsPlayerList.updatePlayerList(collection, this.socialInteractionsPlayerList.getScrollAmount());
      if (!this.searchBox.getValue().isEmpty() && this.socialInteractionsPlayerList.isEmpty() && !this.searchBox.isFocused()) {
         NarratorChatListener.INSTANCE.sayNow(EMPTY_SEARCH.getString());
      } else if (collection.isEmpty()) {
         if (p_244682_1_ == SocialInteractionsScreen.Mode.HIDDEN) {
            NarratorChatListener.INSTANCE.sayNow(EMPTY_HIDDEN.getString());
         } else if (p_244682_1_ == SocialInteractionsScreen.Mode.BLOCKED) {
            NarratorChatListener.INSTANCE.sayNow(EMPTY_BLOCKED.getString());
         }
      }

   }

   public void removed() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
   }

   public void renderBackground(MatrixStack pMatrixStack) {
      int i = this.marginX() + 3;
      super.renderBackground(pMatrixStack);
      this.minecraft.getTextureManager().bind(SOCIAL_INTERACTIONS_LOCATION);
      this.blit(pMatrixStack, i, 64, 1, 1, 236, 8);
      int j = this.backgroundUnits();

      for(int k = 0; k < j; ++k) {
         this.blit(pMatrixStack, i, 72 + 16 * k, 1, 10, 236, 16);
      }

      this.blit(pMatrixStack, i, 72 + 16 * j, 1, 27, 236, 8);
      this.blit(pMatrixStack, i + 10, 76, 243, 1, 12, 12);
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.updateServerLabel(this.minecraft);
      this.renderBackground(pMatrixStack);
      if (this.serverLabel != null) {
         drawString(pMatrixStack, this.minecraft.font, this.serverLabel, this.marginX() + 8, 35, -1);
      }

      if (!this.socialInteractionsPlayerList.isEmpty()) {
         this.socialInteractionsPlayerList.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      } else if (!this.searchBox.getValue().isEmpty()) {
         drawCenteredString(pMatrixStack, this.minecraft.font, EMPTY_SEARCH, this.width / 2, (78 + this.listEnd()) / 2, -1);
      } else {
         switch(this.page) {
         case HIDDEN:
            drawCenteredString(pMatrixStack, this.minecraft.font, EMPTY_HIDDEN, this.width / 2, (78 + this.listEnd()) / 2, -1);
            break;
         case BLOCKED:
            drawCenteredString(pMatrixStack, this.minecraft.font, EMPTY_BLOCKED, this.width / 2, (78 + this.listEnd()) / 2, -1);
         }
      }

      if (!this.searchBox.isFocused() && this.searchBox.getValue().isEmpty()) {
         drawString(pMatrixStack, this.minecraft.font, SEARCH_HINT, this.searchBox.x, this.searchBox.y, -1);
      } else {
         this.searchBox.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      }

      this.blockingHintButton.visible = this.page == SocialInteractionsScreen.Mode.BLOCKED;
      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      if (this.postRenderRunnable != null) {
         this.postRenderRunnable.run();
      }

   }

   public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
      if (this.searchBox.isFocused()) {
         this.searchBox.mouseClicked(pMouseX, pMouseY, pButton);
      }

      return super.mouseClicked(pMouseX, pMouseY, pButton) || this.socialInteractionsPlayerList.mouseClicked(pMouseX, pMouseY, pButton);
   }

   public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
      if (!this.searchBox.isFocused() && this.minecraft.options.keySocialInteractions.matches(pKeyCode, pScanCode)) {
         this.minecraft.setScreen((Screen)null);
         return true;
      } else {
         return super.keyPressed(pKeyCode, pScanCode, pModifiers);
      }
   }

   public boolean isPauseScreen() {
      return false;
   }

   private void checkSearchStringUpdate(String p_244687_1_) {
      p_244687_1_ = p_244687_1_.toLowerCase(Locale.ROOT);
      if (!p_244687_1_.equals(this.lastSearch)) {
         this.socialInteractionsPlayerList.setFilter(p_244687_1_);
         this.lastSearch = p_244687_1_;
         this.showPage(this.page);
      }

   }

   private void updateServerLabel(Minecraft p_244680_1_) {
      int i = p_244680_1_.getConnection().getOnlinePlayers().size();
      if (this.playerCount != i) {
         String s = "";
         ServerData serverdata = p_244680_1_.getCurrentServer();
         if (p_244680_1_.isLocalServer()) {
            s = p_244680_1_.getSingleplayerServer().getMotd();
         } else if (serverdata != null) {
            s = serverdata.name;
         }

         if (i > 1) {
            this.serverLabel = new TranslationTextComponent("gui.socialInteractions.server_label.multiple", s, i);
         } else {
            this.serverLabel = new TranslationTextComponent("gui.socialInteractions.server_label.single", s, i);
         }

         this.playerCount = i;
      }

   }

   public void onAddPlayer(NetworkPlayerInfo p_244683_1_) {
      this.socialInteractionsPlayerList.addPlayer(p_244683_1_, this.page);
   }

   public void onRemovePlayer(UUID p_244685_1_) {
      this.socialInteractionsPlayerList.removePlayer(p_244685_1_);
   }

   public void setPostRenderRunnable(@Nullable Runnable p_244684_1_) {
      this.postRenderRunnable = p_244684_1_;
   }

   @OnlyIn(Dist.CLIENT)
   public static enum Mode {
      ALL,
      HIDDEN,
      BLOCKED;
   }
}