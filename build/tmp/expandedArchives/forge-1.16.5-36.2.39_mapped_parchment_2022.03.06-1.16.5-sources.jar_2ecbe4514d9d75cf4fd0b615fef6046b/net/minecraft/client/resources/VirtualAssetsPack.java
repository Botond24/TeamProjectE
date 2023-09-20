package net.minecraft.client.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.VanillaPack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VirtualAssetsPack extends VanillaPack {
   private final ResourceIndex assetIndex;

   public VirtualAssetsPack(ResourceIndex pAssetIndex) {
      super("minecraft", "realms");
      this.assetIndex = pAssetIndex;
   }

   @Nullable
   protected InputStream getResourceAsStream(ResourcePackType pType, ResourceLocation pLocation) {
      if (pType == ResourcePackType.CLIENT_RESOURCES) {
         File file1 = this.assetIndex.getFile(pLocation);
         if (file1 != null && file1.exists()) {
            try {
               return new FileInputStream(file1);
            } catch (FileNotFoundException filenotfoundexception) {
            }
         }
      }

      return super.getResourceAsStream(pType, pLocation);
   }

   public boolean hasResource(ResourcePackType pType, ResourceLocation pLocation) {
      if (pType == ResourcePackType.CLIENT_RESOURCES) {
         File file1 = this.assetIndex.getFile(pLocation);
         if (file1 != null && file1.exists()) {
            return true;
         }
      }

      return super.hasResource(pType, pLocation);
   }

   @Nullable
   protected InputStream getResourceAsStream(String pPath) {
      File file1 = this.assetIndex.getRootFile(pPath);
      if (file1 != null && file1.exists()) {
         try {
            return new FileInputStream(file1);
         } catch (FileNotFoundException filenotfoundexception) {
         }
      }

      return super.getResourceAsStream(pPath);
   }

   public Collection<ResourceLocation> getResources(ResourcePackType pType, String pNamespace, String pPath, int pMaxDepth, Predicate<String> pFilter) {
      Collection<ResourceLocation> collection = super.getResources(pType, pNamespace, pPath, pMaxDepth, pFilter);
      collection.addAll(this.assetIndex.getFiles(pPath, pNamespace, pMaxDepth, pFilter));
      return collection;
   }
}