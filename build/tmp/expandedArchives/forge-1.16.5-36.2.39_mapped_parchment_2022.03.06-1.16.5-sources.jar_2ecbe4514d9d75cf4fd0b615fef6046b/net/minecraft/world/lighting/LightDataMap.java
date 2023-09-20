package net.minecraft.world.lighting;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.world.chunk.NibbleArray;

public abstract class LightDataMap<M extends LightDataMap<M>> {
   private final long[] lastSectionKeys = new long[2];
   private final NibbleArray[] lastSections = new NibbleArray[2];
   private boolean cacheEnabled;
   protected final Long2ObjectOpenHashMap<NibbleArray> map;

   protected LightDataMap(Long2ObjectOpenHashMap<NibbleArray> p_i51299_1_) {
      this.map = p_i51299_1_;
      this.clearCache();
      this.cacheEnabled = true;
   }

   public abstract M copy();

   public void copyDataLayer(long pSectionPos) {
      this.map.put(pSectionPos, this.map.get(pSectionPos).copy());
      this.clearCache();
   }

   public boolean hasLayer(long pSectionPos) {
      return this.map.containsKey(pSectionPos);
   }

   @Nullable
   public NibbleArray getLayer(long pSectionPos) {
      if (this.cacheEnabled) {
         for(int i = 0; i < 2; ++i) {
            if (pSectionPos == this.lastSectionKeys[i]) {
               return this.lastSections[i];
            }
         }
      }

      NibbleArray nibblearray = this.map.get(pSectionPos);
      if (nibblearray == null) {
         return null;
      } else {
         if (this.cacheEnabled) {
            for(int j = 1; j > 0; --j) {
               this.lastSectionKeys[j] = this.lastSectionKeys[j - 1];
               this.lastSections[j] = this.lastSections[j - 1];
            }

            this.lastSectionKeys[0] = pSectionPos;
            this.lastSections[0] = nibblearray;
         }

         return nibblearray;
      }
   }

   @Nullable
   public NibbleArray removeLayer(long pSectionPos) {
      return this.map.remove(pSectionPos);
   }

   public void setLayer(long pSectionPos, NibbleArray pArray) {
      this.map.put(pSectionPos, pArray);
   }

   public void clearCache() {
      for(int i = 0; i < 2; ++i) {
         this.lastSectionKeys[i] = Long.MAX_VALUE;
         this.lastSections[i] = null;
      }

   }

   public void disableCache() {
      this.cacheEnabled = false;
   }
}