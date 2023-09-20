package net.minecraft.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreateBuffetWorldScreen extends Screen {
   private static final ITextComponent BIOME_SELECT_INFO = new TranslationTextComponent("createWorld.customize.buffet.biome");
   private final Screen parent;
   private final Consumer<Biome> applySettings;
   private final MutableRegistry<Biome> biomes;
   private CreateBuffetWorldScreen.BiomeList list;
   private Biome biome;
   private Button doneButton;

   public CreateBuffetWorldScreen(Screen p_i242054_1_, DynamicRegistries p_i242054_2_, Consumer<Biome> p_i242054_3_, Biome p_i242054_4_) {
      super(new TranslationTextComponent("createWorld.customize.buffet.title"));
      this.parent = p_i242054_1_;
      this.applySettings = p_i242054_3_;
      this.biome = p_i242054_4_;
      this.biomes = p_i242054_2_.registryOrThrow(Registry.BIOME_REGISTRY);
   }

   public void onClose() {
      this.minecraft.setScreen(this.parent);
   }

   protected void init() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
      this.list = new CreateBuffetWorldScreen.BiomeList();
      this.children.add(this.list);
      this.doneButton = this.addButton(new Button(this.width / 2 - 155, this.height - 28, 150, 20, DialogTexts.GUI_DONE, (p_241579_1_) -> {
         this.applySettings.accept(this.biome);
         this.minecraft.setScreen(this.parent);
      }));
      this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, DialogTexts.GUI_CANCEL, (p_213015_1_) -> {
         this.minecraft.setScreen(this.parent);
      }));
      this.list.setSelected(this.list.children().stream().filter((p_241578_1_) -> {
         return Objects.equals(p_241578_1_.biome, this.biome);
      }).findFirst().orElse((CreateBuffetWorldScreen.BiomeList.BiomeEntry)null));
   }

   private void updateButtonValidity() {
      this.doneButton.active = this.list.getSelected() != null;
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.renderDirtBackground(0);
      this.list.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      drawCenteredString(pMatrixStack, this.font, this.title, this.width / 2, 8, 16777215);
      drawCenteredString(pMatrixStack, this.font, BIOME_SELECT_INFO, this.width / 2, 28, 10526880);
      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
   }

   @OnlyIn(Dist.CLIENT)
   class BiomeList extends ExtendedList<CreateBuffetWorldScreen.BiomeList.BiomeEntry> {
      private BiomeList() {
         super(CreateBuffetWorldScreen.this.minecraft, CreateBuffetWorldScreen.this.width, CreateBuffetWorldScreen.this.height, 40, CreateBuffetWorldScreen.this.height - 37, 16);
         CreateBuffetWorldScreen.this.biomes.entrySet().stream().sorted(Comparator.comparing((p_238598_0_) -> {
            return p_238598_0_.getKey().location().toString();
         })).forEach((p_238597_1_) -> {
            this.addEntry(new CreateBuffetWorldScreen.BiomeList.BiomeEntry(p_238597_1_.getValue()));
         });
      }

      protected boolean isFocused() {
         return CreateBuffetWorldScreen.this.getFocused() == this;
      }

      public void setSelected(@Nullable CreateBuffetWorldScreen.BiomeList.BiomeEntry pEntry) {
         super.setSelected(pEntry);
         if (pEntry != null) {
            CreateBuffetWorldScreen.this.biome = pEntry.biome;
            NarratorChatListener.INSTANCE.sayNow((new TranslationTextComponent("narrator.select", CreateBuffetWorldScreen.this.biomes.getKey(pEntry.biome))).getString());
         }

         CreateBuffetWorldScreen.this.updateButtonValidity();
      }

      @OnlyIn(Dist.CLIENT)
      class BiomeEntry extends ExtendedList.AbstractListEntry<CreateBuffetWorldScreen.BiomeList.BiomeEntry> {
         private final Biome biome;
         private final ITextComponent name;

         public BiomeEntry(Biome p_i232272_2_) {
            this.biome = p_i232272_2_;
            ResourceLocation resourcelocation = CreateBuffetWorldScreen.this.biomes.getKey(p_i232272_2_);
            String s = "biome." + resourcelocation.getNamespace() + "." + resourcelocation.getPath();
            if (LanguageMap.getInstance().has(s)) {
               this.name = new TranslationTextComponent(s);
            } else {
               this.name = new StringTextComponent(resourcelocation.toString());
            }

         }

         public void render(MatrixStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
            AbstractGui.drawString(pMatrixStack, CreateBuffetWorldScreen.this.font, this.name, pLeft + 5, pTop + 2, 16777215);
         }

         public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
            if (pButton == 0) {
               BiomeList.this.setSelected(this);
               return true;
            } else {
               return false;
            }
         }
      }
   }
}