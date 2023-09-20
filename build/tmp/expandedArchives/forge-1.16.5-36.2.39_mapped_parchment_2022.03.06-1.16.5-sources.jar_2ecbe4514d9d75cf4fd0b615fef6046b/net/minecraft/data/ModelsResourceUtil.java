package net.minecraft.data;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public class ModelsResourceUtil {
   @Deprecated
   public static ResourceLocation decorateBlockModelLocation(String pBlockModelLocation) {
      return new ResourceLocation("minecraft", "block/" + pBlockModelLocation);
   }

   public static ResourceLocation decorateItemModelLocation(String pItemModelLocation) {
      return new ResourceLocation("minecraft", "item/" + pItemModelLocation);
   }

   public static ResourceLocation getModelLocation(Block pBlock, String pModelLocationSuffix) {
      ResourceLocation resourcelocation = Registry.BLOCK.getKey(pBlock);
      return new ResourceLocation(resourcelocation.getNamespace(), "block/" + resourcelocation.getPath() + pModelLocationSuffix);
   }

   public static ResourceLocation getModelLocation(Block pBlock) {
      ResourceLocation resourcelocation = Registry.BLOCK.getKey(pBlock);
      return new ResourceLocation(resourcelocation.getNamespace(), "block/" + resourcelocation.getPath());
   }

   public static ResourceLocation getModelLocation(Item pItem) {
      ResourceLocation resourcelocation = Registry.ITEM.getKey(pItem);
      return new ResourceLocation(resourcelocation.getNamespace(), "item/" + resourcelocation.getPath());
   }

   public static ResourceLocation getModelLocation(Item pItem, String pModelLocationSuffix) {
      ResourceLocation resourcelocation = Registry.ITEM.getKey(pItem);
      return new ResourceLocation(resourcelocation.getNamespace(), "item/" + resourcelocation.getPath() + pModelLocationSuffix);
   }
}