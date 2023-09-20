package net.minecraft.client.gui.screen;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.FlatLayerInfo;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class FlatPresetsScreen extends Screen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final List<FlatPresetsScreen.LayerItem> PRESETS = Lists.newArrayList();
   /** The parent GUI */
   private final CreateFlatWorldScreen parent;
   private ITextComponent shareText;
   private ITextComponent listText;
   private FlatPresetsScreen.SlotList list;
   private Button selectButton;
   private TextFieldWidget export;
   private FlatGenerationSettings settings;

   public FlatPresetsScreen(CreateFlatWorldScreen pParent) {
      super(new TranslationTextComponent("createWorld.customize.presets.title"));
      this.parent = pParent;
   }

   @Nullable
   private static FlatLayerInfo getLayerInfoFromString(String p_238638_0_, int p_238638_1_) {
      String[] astring = p_238638_0_.split("\\*", 2);
      int i;
      if (astring.length == 2) {
         try {
            i = Math.max(Integer.parseInt(astring[0]), 0);
         } catch (NumberFormatException numberformatexception) {
            LOGGER.error("Error while parsing flat world string => {}", (Object)numberformatexception.getMessage());
            return null;
         }
      } else {
         i = 1;
      }

      int j = Math.min(p_238638_1_ + i, 256);
      int k = j - p_238638_1_;
      String s = astring[astring.length - 1];

      Block block;
      try {
         block = Registry.BLOCK.getOptional(new ResourceLocation(s)).orElse((Block)null);
      } catch (Exception exception) {
         LOGGER.error("Error while parsing flat world string => {}", (Object)exception.getMessage());
         return null;
      }

      if (block == null) {
         LOGGER.error("Error while parsing flat world string => Unknown block, {}", (Object)s);
         return null;
      } else {
         FlatLayerInfo flatlayerinfo = new FlatLayerInfo(k, block);
         flatlayerinfo.setStart(p_238638_1_);
         return flatlayerinfo;
      }
   }

   private static List<FlatLayerInfo> getLayersInfoFromString(String p_238637_0_) {
      List<FlatLayerInfo> list = Lists.newArrayList();
      String[] astring = p_238637_0_.split(",");
      int i = 0;

      for(String s : astring) {
         FlatLayerInfo flatlayerinfo = getLayerInfoFromString(s, i);
         if (flatlayerinfo == null) {
            return Collections.emptyList();
         }

         list.add(flatlayerinfo);
         i += flatlayerinfo.getHeight();
      }

      return list;
   }

   public static FlatGenerationSettings fromString(Registry<Biome> pBiomeRegistry, String p_243299_1_, FlatGenerationSettings p_243299_2_) {
      Iterator<String> iterator = Splitter.on(';').split(p_243299_1_).iterator();
      if (!iterator.hasNext()) {
         return FlatGenerationSettings.getDefault(pBiomeRegistry);
      } else {
         List<FlatLayerInfo> list = getLayersInfoFromString(iterator.next());
         if (list.isEmpty()) {
            return FlatGenerationSettings.getDefault(pBiomeRegistry);
         } else {
            FlatGenerationSettings flatgenerationsettings = p_243299_2_.withLayers(list, p_243299_2_.structureSettings());
            RegistryKey<Biome> registrykey = Biomes.PLAINS;
            if (iterator.hasNext()) {
               try {
                  ResourceLocation resourcelocation = new ResourceLocation(iterator.next());
                  registrykey = RegistryKey.create(Registry.BIOME_REGISTRY, resourcelocation);
                  pBiomeRegistry.getOptional(registrykey).orElseThrow(() -> {
                     return new IllegalArgumentException("Invalid Biome: " + resourcelocation);
                  });
               } catch (Exception exception) {
                  LOGGER.error("Error while parsing flat world string => {}", (Object)exception.getMessage());
               }
            }

            RegistryKey<Biome> registrykey1 = registrykey;
            flatgenerationsettings.setBiome(() -> {
               return pBiomeRegistry.getOrThrow(registrykey1);
            });
            return flatgenerationsettings;
         }
      }
   }

   private static String save(Registry<Biome> pBiomeRegistry, FlatGenerationSettings p_243303_1_) {
      StringBuilder stringbuilder = new StringBuilder();

      for(int i = 0; i < p_243303_1_.getLayersInfo().size(); ++i) {
         if (i > 0) {
            stringbuilder.append(",");
         }

         stringbuilder.append(p_243303_1_.getLayersInfo().get(i));
      }

      stringbuilder.append(";");
      stringbuilder.append((Object)pBiomeRegistry.getKey(p_243303_1_.getBiome()));
      return stringbuilder.toString();
   }

   protected void init() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
      this.shareText = new TranslationTextComponent("createWorld.customize.presets.share");
      this.listText = new TranslationTextComponent("createWorld.customize.presets.list");
      this.export = new TextFieldWidget(this.font, 50, 40, this.width - 100, 20, this.shareText);
      this.export.setMaxLength(1230);
      Registry<Biome> registry = this.parent.parent.worldGenSettingsComponent.registryHolder().registryOrThrow(Registry.BIOME_REGISTRY);
      this.export.setValue(save(registry, this.parent.settings()));
      this.settings = this.parent.settings();
      this.children.add(this.export);
      this.list = new FlatPresetsScreen.SlotList();
      this.children.add(this.list);
      this.selectButton = this.addButton(new Button(this.width / 2 - 155, this.height - 28, 150, 20, new TranslationTextComponent("createWorld.customize.presets.select"), (p_243298_2_) -> {
         FlatGenerationSettings flatgenerationsettings = fromString(registry, this.export.getValue(), this.settings);
         this.parent.setConfig(flatgenerationsettings);
         this.minecraft.setScreen(this.parent);
      }));
      this.addButton(new Button(this.width / 2 + 5, this.height - 28, 150, 20, DialogTexts.GUI_CANCEL, (p_243294_1_) -> {
         this.minecraft.setScreen(this.parent);
      }));
      this.updateButtonValidity(this.list.getSelected() != null);
   }

   public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
      return this.list.mouseScrolled(pMouseX, pMouseY, pDelta);
   }

   public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
      String s = this.export.getValue();
      this.init(pMinecraft, pWidth, pHeight);
      this.export.setValue(s);
   }

   public void onClose() {
      this.minecraft.setScreen(this.parent);
   }

   public void removed() {
      this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
   }

   public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks) {
      this.renderBackground(pMatrixStack);
      this.list.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      RenderSystem.pushMatrix();
      RenderSystem.translatef(0.0F, 0.0F, 400.0F);
      drawCenteredString(pMatrixStack, this.font, this.title, this.width / 2, 8, 16777215);
      drawString(pMatrixStack, this.font, this.shareText, 50, 30, 10526880);
      drawString(pMatrixStack, this.font, this.listText, 50, 70, 10526880);
      RenderSystem.popMatrix();
      this.export.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
      super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
   }

   public void tick() {
      this.export.tick();
      super.tick();
   }

   public void updateButtonValidity(boolean p_213074_1_) {
      this.selectButton.active = p_213074_1_ || this.export.getValue().length() > 1;
   }

   private static void preset(ITextComponent p_238640_0_, IItemProvider p_238640_1_, RegistryKey<Biome> p_238640_2_, List<Structure<?>> p_238640_3_, boolean p_238640_4_, boolean p_238640_5_, boolean p_238640_6_, FlatLayerInfo... p_238640_7_) {
      PRESETS.add(new FlatPresetsScreen.LayerItem(p_238640_1_.asItem(), p_238640_0_, (p_243301_6_) -> {
         Map<Structure<?>, StructureSeparationSettings> map = Maps.newHashMap();

         for(Structure<?> structure : p_238640_3_) {
            map.put(structure, DimensionStructuresSettings.DEFAULTS.get(structure));
         }

         DimensionStructuresSettings dimensionstructuressettings = new DimensionStructuresSettings(p_238640_4_ ? Optional.of(DimensionStructuresSettings.DEFAULT_STRONGHOLD) : Optional.empty(), map);
         FlatGenerationSettings flatgenerationsettings = new FlatGenerationSettings(dimensionstructuressettings, p_243301_6_);
         if (p_238640_5_) {
            flatgenerationsettings.setDecoration();
         }

         if (p_238640_6_) {
            flatgenerationsettings.setAddLakes();
         }

         for(int i = p_238640_7_.length - 1; i >= 0; --i) {
            flatgenerationsettings.getLayersInfo().add(p_238640_7_[i]);
         }

         flatgenerationsettings.setBiome(() -> {
            return p_243301_6_.getOrThrow(p_238640_2_);
         });
         flatgenerationsettings.updateLayers();
         return flatgenerationsettings.withStructureSettings(dimensionstructuressettings);
      }));
   }

   static {
      preset(new TranslationTextComponent("createWorld.customize.preset.classic_flat"), Blocks.GRASS_BLOCK, Biomes.PLAINS, Arrays.asList(Structure.VILLAGE), false, false, false, new FlatLayerInfo(1, Blocks.GRASS_BLOCK), new FlatLayerInfo(2, Blocks.DIRT), new FlatLayerInfo(1, Blocks.BEDROCK));
      preset(new TranslationTextComponent("createWorld.customize.preset.tunnelers_dream"), Blocks.STONE, Biomes.MOUNTAINS, Arrays.asList(Structure.MINESHAFT), true, true, false, new FlatLayerInfo(1, Blocks.GRASS_BLOCK), new FlatLayerInfo(5, Blocks.DIRT), new FlatLayerInfo(230, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
      preset(new TranslationTextComponent("createWorld.customize.preset.water_world"), Items.WATER_BUCKET, Biomes.DEEP_OCEAN, Arrays.asList(Structure.OCEAN_RUIN, Structure.SHIPWRECK, Structure.OCEAN_MONUMENT), false, false, false, new FlatLayerInfo(90, Blocks.WATER), new FlatLayerInfo(5, Blocks.SAND), new FlatLayerInfo(5, Blocks.DIRT), new FlatLayerInfo(5, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
      preset(new TranslationTextComponent("createWorld.customize.preset.overworld"), Blocks.GRASS, Biomes.PLAINS, Arrays.asList(Structure.VILLAGE, Structure.MINESHAFT, Structure.PILLAGER_OUTPOST, Structure.RUINED_PORTAL), true, true, true, new FlatLayerInfo(1, Blocks.GRASS_BLOCK), new FlatLayerInfo(3, Blocks.DIRT), new FlatLayerInfo(59, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
      preset(new TranslationTextComponent("createWorld.customize.preset.snowy_kingdom"), Blocks.SNOW, Biomes.SNOWY_TUNDRA, Arrays.asList(Structure.VILLAGE, Structure.IGLOO), false, false, false, new FlatLayerInfo(1, Blocks.SNOW), new FlatLayerInfo(1, Blocks.GRASS_BLOCK), new FlatLayerInfo(3, Blocks.DIRT), new FlatLayerInfo(59, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
      preset(new TranslationTextComponent("createWorld.customize.preset.bottomless_pit"), Items.FEATHER, Biomes.PLAINS, Arrays.asList(Structure.VILLAGE), false, false, false, new FlatLayerInfo(1, Blocks.GRASS_BLOCK), new FlatLayerInfo(3, Blocks.DIRT), new FlatLayerInfo(2, Blocks.COBBLESTONE));
      preset(new TranslationTextComponent("createWorld.customize.preset.desert"), Blocks.SAND, Biomes.DESERT, Arrays.asList(Structure.VILLAGE, Structure.DESERT_PYRAMID, Structure.MINESHAFT), true, true, false, new FlatLayerInfo(8, Blocks.SAND), new FlatLayerInfo(52, Blocks.SANDSTONE), new FlatLayerInfo(3, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
      preset(new TranslationTextComponent("createWorld.customize.preset.redstone_ready"), Items.REDSTONE, Biomes.DESERT, Collections.emptyList(), false, false, false, new FlatLayerInfo(52, Blocks.SANDSTONE), new FlatLayerInfo(3, Blocks.STONE), new FlatLayerInfo(1, Blocks.BEDROCK));
      preset(new TranslationTextComponent("createWorld.customize.preset.the_void"), Blocks.BARRIER, Biomes.THE_VOID, Collections.emptyList(), false, true, false, new FlatLayerInfo(1, Blocks.AIR));
   }

   @OnlyIn(Dist.CLIENT)
   static class LayerItem {
      public final Item icon;
      public final ITextComponent name;
      public final Function<Registry<Biome>, FlatGenerationSettings> settings;

      public LayerItem(Item pIcon, ITextComponent pName, Function<Registry<Biome>, FlatGenerationSettings> pSettings) {
         this.icon = pIcon;
         this.name = pName;
         this.settings = pSettings;
      }

      public ITextComponent getName() {
         return this.name;
      }
   }

   @OnlyIn(Dist.CLIENT)
   class SlotList extends ExtendedList<FlatPresetsScreen.SlotList.PresetEntry> {
      public SlotList() {
         super(FlatPresetsScreen.this.minecraft, FlatPresetsScreen.this.width, FlatPresetsScreen.this.height, 80, FlatPresetsScreen.this.height - 37, 24);

         for(int i = 0; i < FlatPresetsScreen.PRESETS.size(); ++i) {
            this.addEntry(new FlatPresetsScreen.SlotList.PresetEntry());
         }

      }

      public void setSelected(@Nullable FlatPresetsScreen.SlotList.PresetEntry pEntry) {
         super.setSelected(pEntry);
         if (pEntry != null) {
            NarratorChatListener.INSTANCE.sayNow((new TranslationTextComponent("narrator.select", FlatPresetsScreen.PRESETS.get(this.children().indexOf(pEntry)).getName())).getString());
         }

         FlatPresetsScreen.this.updateButtonValidity(pEntry != null);
      }

      protected boolean isFocused() {
         return FlatPresetsScreen.this.getFocused() == this;
      }

      public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
         if (super.keyPressed(pKeyCode, pScanCode, pModifiers)) {
            return true;
         } else {
            if ((pKeyCode == 257 || pKeyCode == 335) && this.getSelected() != null) {
               this.getSelected().select();
            }

            return false;
         }
      }

      @OnlyIn(Dist.CLIENT)
      public class PresetEntry extends ExtendedList.AbstractListEntry<FlatPresetsScreen.SlotList.PresetEntry> {
         public void render(MatrixStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
            FlatPresetsScreen.LayerItem flatpresetsscreen$layeritem = FlatPresetsScreen.PRESETS.get(pIndex);
            this.blitSlot(pMatrixStack, pLeft, pTop, flatpresetsscreen$layeritem.icon);
            FlatPresetsScreen.this.font.draw(pMatrixStack, flatpresetsscreen$layeritem.name, (float)(pLeft + 18 + 5), (float)(pTop + 6), 16777215);
         }

         public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
            if (pButton == 0) {
               this.select();
            }

            return false;
         }

         private void select() {
            SlotList.this.setSelected(this);
            FlatPresetsScreen.LayerItem flatpresetsscreen$layeritem = FlatPresetsScreen.PRESETS.get(SlotList.this.children().indexOf(this));
            Registry<Biome> registry = FlatPresetsScreen.this.parent.parent.worldGenSettingsComponent.registryHolder().registryOrThrow(Registry.BIOME_REGISTRY);
            FlatPresetsScreen.this.settings = flatpresetsscreen$layeritem.settings.apply(registry);
            FlatPresetsScreen.this.export.setValue(FlatPresetsScreen.save(registry, FlatPresetsScreen.this.settings));
            FlatPresetsScreen.this.export.moveCursorToStart();
         }

         private void blitSlot(MatrixStack pPoseStack, int p_238647_2_, int p_238647_3_, Item pItem) {
            this.blitSlotBg(pPoseStack, p_238647_2_ + 1, p_238647_3_ + 1);
            RenderSystem.enableRescaleNormal();
            FlatPresetsScreen.this.itemRenderer.renderGuiItem(new ItemStack(pItem), p_238647_2_ + 2, p_238647_3_ + 2);
            RenderSystem.disableRescaleNormal();
         }

         private void blitSlotBg(MatrixStack pPoseStack, int p_238646_2_, int p_238646_3_) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            SlotList.this.minecraft.getTextureManager().bind(AbstractGui.STATS_ICON_LOCATION);
            AbstractGui.blit(pPoseStack, p_238646_2_, p_238646_3_, FlatPresetsScreen.this.getBlitOffset(), 0.0F, 0.0F, 18, 18, 128, 128);
         }
      }
   }
}