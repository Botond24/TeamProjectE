package net.minecraft.data;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public class ModelTextures {
   private final Map<StockTextureAliases, ResourceLocation> slots = Maps.newHashMap();
   private final Set<StockTextureAliases> forcedSlots = Sets.newHashSet();

   public ModelTextures put(StockTextureAliases pTextureSlot, ResourceLocation pTextureLocation) {
      this.slots.put(pTextureSlot, pTextureLocation);
      return this;
   }

   public Stream<StockTextureAliases> getForced() {
      return this.forcedSlots.stream();
   }

   public ModelTextures copyForced(StockTextureAliases pSourceSlot, StockTextureAliases pTargetSlot) {
      this.slots.put(pTargetSlot, this.slots.get(pSourceSlot));
      this.forcedSlots.add(pTargetSlot);
      return this;
   }

   public ResourceLocation get(StockTextureAliases pTextureSlot) {
      for(StockTextureAliases stocktexturealiases = pTextureSlot; stocktexturealiases != null; stocktexturealiases = stocktexturealiases.getParent()) {
         ResourceLocation resourcelocation = this.slots.get(stocktexturealiases);
         if (resourcelocation != null) {
            return resourcelocation;
         }
      }

      throw new IllegalStateException("Can't find texture for slot " + pTextureSlot);
   }

   public ModelTextures copyAndUpdate(StockTextureAliases pTextureSlot, ResourceLocation pTextureLocation) {
      ModelTextures modeltextures = new ModelTextures();
      modeltextures.slots.putAll(this.slots);
      modeltextures.forcedSlots.addAll(this.forcedSlots);
      modeltextures.put(pTextureSlot, pTextureLocation);
      return modeltextures;
   }

   public static ModelTextures cube(Block pBlock) {
      ResourceLocation resourcelocation = getBlockTexture(pBlock);
      return cube(resourcelocation);
   }

   public static ModelTextures defaultTexture(Block pBlock) {
      ResourceLocation resourcelocation = getBlockTexture(pBlock);
      return defaultTexture(resourcelocation);
   }

   public static ModelTextures defaultTexture(ResourceLocation pTextureLocation) {
      return (new ModelTextures()).put(StockTextureAliases.TEXTURE, pTextureLocation);
   }

   public static ModelTextures cube(ResourceLocation pAllTextureLocation) {
      return (new ModelTextures()).put(StockTextureAliases.ALL, pAllTextureLocation);
   }

   public static ModelTextures cross(Block pBlock) {
      return singleSlot(StockTextureAliases.CROSS, getBlockTexture(pBlock));
   }

   public static ModelTextures cross(ResourceLocation pCrossTextureLocation) {
      return singleSlot(StockTextureAliases.CROSS, pCrossTextureLocation);
   }

   public static ModelTextures plant(Block pPlantBlock) {
      return singleSlot(StockTextureAliases.PLANT, getBlockTexture(pPlantBlock));
   }

   public static ModelTextures plant(ResourceLocation pPlantTextureLocation) {
      return singleSlot(StockTextureAliases.PLANT, pPlantTextureLocation);
   }

   public static ModelTextures rail(Block pRailBlock) {
      return singleSlot(StockTextureAliases.RAIL, getBlockTexture(pRailBlock));
   }

   public static ModelTextures rail(ResourceLocation pRailTextureLocation) {
      return singleSlot(StockTextureAliases.RAIL, pRailTextureLocation);
   }

   public static ModelTextures wool(Block pWoolBlock) {
      return singleSlot(StockTextureAliases.WOOL, getBlockTexture(pWoolBlock));
   }

   public static ModelTextures stem(Block pStemBlock) {
      return singleSlot(StockTextureAliases.STEM, getBlockTexture(pStemBlock));
   }

   public static ModelTextures attachedStem(Block pUnattachedStemBlock, Block pAttachedStemBlock) {
      return (new ModelTextures()).put(StockTextureAliases.STEM, getBlockTexture(pUnattachedStemBlock)).put(StockTextureAliases.UPPER_STEM, getBlockTexture(pAttachedStemBlock));
   }

   public static ModelTextures pattern(Block pPatternBlock) {
      return singleSlot(StockTextureAliases.PATTERN, getBlockTexture(pPatternBlock));
   }

   public static ModelTextures fan(Block pFanBlock) {
      return singleSlot(StockTextureAliases.FAN, getBlockTexture(pFanBlock));
   }

   public static ModelTextures crop(ResourceLocation pCropTextureLocation) {
      return singleSlot(StockTextureAliases.CROP, pCropTextureLocation);
   }

   public static ModelTextures pane(Block pGlassBlock, Block pPaneBlock) {
      return (new ModelTextures()).put(StockTextureAliases.PANE, getBlockTexture(pGlassBlock)).put(StockTextureAliases.EDGE, getBlockTexture(pPaneBlock, "_top"));
   }

   public static ModelTextures singleSlot(StockTextureAliases pTextureSlot, ResourceLocation pTextureLocation) {
      return (new ModelTextures()).put(pTextureSlot, pTextureLocation);
   }

   public static ModelTextures column(Block pColumnBlock) {
      return (new ModelTextures()).put(StockTextureAliases.SIDE, getBlockTexture(pColumnBlock, "_side")).put(StockTextureAliases.END, getBlockTexture(pColumnBlock, "_top"));
   }

   public static ModelTextures cubeTop(Block pBlock) {
      return (new ModelTextures()).put(StockTextureAliases.SIDE, getBlockTexture(pBlock, "_side")).put(StockTextureAliases.TOP, getBlockTexture(pBlock, "_top"));
   }

   public static ModelTextures logColumn(Block pLogBlock) {
      return (new ModelTextures()).put(StockTextureAliases.SIDE, getBlockTexture(pLogBlock)).put(StockTextureAliases.END, getBlockTexture(pLogBlock, "_top"));
   }

   public static ModelTextures column(ResourceLocation pSideTextureLocation, ResourceLocation pEndTextureLocation) {
      return (new ModelTextures()).put(StockTextureAliases.SIDE, pSideTextureLocation).put(StockTextureAliases.END, pEndTextureLocation);
   }

   public static ModelTextures cubeBottomTop(Block pBlock) {
      return (new ModelTextures()).put(StockTextureAliases.SIDE, getBlockTexture(pBlock, "_side")).put(StockTextureAliases.TOP, getBlockTexture(pBlock, "_top")).put(StockTextureAliases.BOTTOM, getBlockTexture(pBlock, "_bottom"));
   }

   public static ModelTextures cubeBottomTopWithWall(Block pBlock) {
      ResourceLocation resourcelocation = getBlockTexture(pBlock);
      return (new ModelTextures()).put(StockTextureAliases.WALL, resourcelocation).put(StockTextureAliases.SIDE, resourcelocation).put(StockTextureAliases.TOP, getBlockTexture(pBlock, "_top")).put(StockTextureAliases.BOTTOM, getBlockTexture(pBlock, "_bottom"));
   }

   public static ModelTextures columnWithWall(Block pColumnBlock) {
      ResourceLocation resourcelocation = getBlockTexture(pColumnBlock);
      return (new ModelTextures()).put(StockTextureAliases.WALL, resourcelocation).put(StockTextureAliases.SIDE, resourcelocation).put(StockTextureAliases.END, getBlockTexture(pColumnBlock, "_top"));
   }

   public static ModelTextures door(Block pDoorBlock) {
      return (new ModelTextures()).put(StockTextureAliases.TOP, getBlockTexture(pDoorBlock, "_top")).put(StockTextureAliases.BOTTOM, getBlockTexture(pDoorBlock, "_bottom"));
   }

   public static ModelTextures particle(Block pParticleBlock) {
      return (new ModelTextures()).put(StockTextureAliases.PARTICLE, getBlockTexture(pParticleBlock));
   }

   public static ModelTextures particle(ResourceLocation pTextureLocation) {
      return (new ModelTextures()).put(StockTextureAliases.PARTICLE, pTextureLocation);
   }

   public static ModelTextures fire0(Block pFireBlock) {
      return (new ModelTextures()).put(StockTextureAliases.FIRE, getBlockTexture(pFireBlock, "_0"));
   }

   public static ModelTextures fire1(Block pFireBlock) {
      return (new ModelTextures()).put(StockTextureAliases.FIRE, getBlockTexture(pFireBlock, "_1"));
   }

   public static ModelTextures lantern(Block pLanternBlock) {
      return (new ModelTextures()).put(StockTextureAliases.LANTERN, getBlockTexture(pLanternBlock));
   }

   public static ModelTextures torch(Block pTorchBlock) {
      return (new ModelTextures()).put(StockTextureAliases.TORCH, getBlockTexture(pTorchBlock));
   }

   public static ModelTextures torch(ResourceLocation pTorchTextureLocation) {
      return (new ModelTextures()).put(StockTextureAliases.TORCH, pTorchTextureLocation);
   }

   public static ModelTextures particleFromItem(Item pParticleItem) {
      return (new ModelTextures()).put(StockTextureAliases.PARTICLE, getItemTexture(pParticleItem));
   }

   public static ModelTextures commandBlock(Block pCommandBlock) {
      return (new ModelTextures()).put(StockTextureAliases.SIDE, getBlockTexture(pCommandBlock, "_side")).put(StockTextureAliases.FRONT, getBlockTexture(pCommandBlock, "_front")).put(StockTextureAliases.BACK, getBlockTexture(pCommandBlock, "_back"));
   }

   public static ModelTextures orientableCube(Block pBlock) {
      return (new ModelTextures()).put(StockTextureAliases.SIDE, getBlockTexture(pBlock, "_side")).put(StockTextureAliases.FRONT, getBlockTexture(pBlock, "_front")).put(StockTextureAliases.TOP, getBlockTexture(pBlock, "_top")).put(StockTextureAliases.BOTTOM, getBlockTexture(pBlock, "_bottom"));
   }

   public static ModelTextures orientableCubeOnlyTop(Block pBlock) {
      return (new ModelTextures()).put(StockTextureAliases.SIDE, getBlockTexture(pBlock, "_side")).put(StockTextureAliases.FRONT, getBlockTexture(pBlock, "_front")).put(StockTextureAliases.TOP, getBlockTexture(pBlock, "_top"));
   }

   public static ModelTextures orientableCubeSameEnds(Block pBlock) {
      return (new ModelTextures()).put(StockTextureAliases.SIDE, getBlockTexture(pBlock, "_side")).put(StockTextureAliases.FRONT, getBlockTexture(pBlock, "_front")).put(StockTextureAliases.END, getBlockTexture(pBlock, "_end"));
   }

   public static ModelTextures top(Block pBlock) {
      return (new ModelTextures()).put(StockTextureAliases.TOP, getBlockTexture(pBlock, "_top"));
   }

   public static ModelTextures craftingTable(Block pCraftingTableBlock, Block pCraftingTableMaterialBlock) {
      return (new ModelTextures()).put(StockTextureAliases.PARTICLE, getBlockTexture(pCraftingTableBlock, "_front")).put(StockTextureAliases.DOWN, getBlockTexture(pCraftingTableMaterialBlock)).put(StockTextureAliases.UP, getBlockTexture(pCraftingTableBlock, "_top")).put(StockTextureAliases.NORTH, getBlockTexture(pCraftingTableBlock, "_front")).put(StockTextureAliases.EAST, getBlockTexture(pCraftingTableBlock, "_side")).put(StockTextureAliases.SOUTH, getBlockTexture(pCraftingTableBlock, "_side")).put(StockTextureAliases.WEST, getBlockTexture(pCraftingTableBlock, "_front"));
   }

   public static ModelTextures fletchingTable(Block pFletchingTableBlock, Block pFletchingTableMaterialBlock) {
      return (new ModelTextures()).put(StockTextureAliases.PARTICLE, getBlockTexture(pFletchingTableBlock, "_front")).put(StockTextureAliases.DOWN, getBlockTexture(pFletchingTableMaterialBlock)).put(StockTextureAliases.UP, getBlockTexture(pFletchingTableBlock, "_top")).put(StockTextureAliases.NORTH, getBlockTexture(pFletchingTableBlock, "_front")).put(StockTextureAliases.SOUTH, getBlockTexture(pFletchingTableBlock, "_front")).put(StockTextureAliases.EAST, getBlockTexture(pFletchingTableBlock, "_side")).put(StockTextureAliases.WEST, getBlockTexture(pFletchingTableBlock, "_side"));
   }

   public static ModelTextures campfire(Block pCampfireBlock) {
      return (new ModelTextures()).put(StockTextureAliases.LIT_LOG, getBlockTexture(pCampfireBlock, "_log_lit")).put(StockTextureAliases.FIRE, getBlockTexture(pCampfireBlock, "_fire"));
   }

   public static ModelTextures layer0(Item pLayerZeroItem) {
      return (new ModelTextures()).put(StockTextureAliases.LAYER0, getItemTexture(pLayerZeroItem));
   }

   public static ModelTextures layer0(Block pLayerZeroBlock) {
      return (new ModelTextures()).put(StockTextureAliases.LAYER0, getBlockTexture(pLayerZeroBlock));
   }

   public static ModelTextures layer0(ResourceLocation pLayerZeroTextureLocation) {
      return (new ModelTextures()).put(StockTextureAliases.LAYER0, pLayerZeroTextureLocation);
   }

   public static ResourceLocation getBlockTexture(Block pBlock) {
      ResourceLocation resourcelocation = Registry.BLOCK.getKey(pBlock);
      return new ResourceLocation(resourcelocation.getNamespace(), "block/" + resourcelocation.getPath());
   }

   public static ResourceLocation getBlockTexture(Block pBlock, String pTextureSuffix) {
      ResourceLocation resourcelocation = Registry.BLOCK.getKey(pBlock);
      return new ResourceLocation(resourcelocation.getNamespace(), "block/" + resourcelocation.getPath() + pTextureSuffix);
   }

   public static ResourceLocation getItemTexture(Item pItem) {
      ResourceLocation resourcelocation = Registry.ITEM.getKey(pItem);
      return new ResourceLocation(resourcelocation.getNamespace(), "item/" + resourcelocation.getPath());
   }

   public static ResourceLocation getItemTexture(Item pItem, String pTextureSuffix) {
      ResourceLocation resourcelocation = Registry.ITEM.getKey(pItem);
      return new ResourceLocation(resourcelocation.getNamespace(), "item/" + resourcelocation.getPath() + pTextureSuffix);
   }
}