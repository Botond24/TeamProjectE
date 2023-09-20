package net.minecraft.world.gen.area;

import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.layer.traits.IPixelTransformer;

public final class LazyArea implements IArea {
   private final IPixelTransformer transformer;
   private final Long2IntLinkedOpenHashMap cache;
   private final int maxCache;

   public LazyArea(Long2IntLinkedOpenHashMap p_i51286_1_, int p_i51286_2_, IPixelTransformer p_i51286_3_) {
      this.cache = p_i51286_1_;
      this.maxCache = p_i51286_2_;
      this.transformer = p_i51286_3_;
   }

   public int get(int pX, int pZ) {
      long i = ChunkPos.asLong(pX, pZ);
      synchronized(this.cache) {
         int j = this.cache.get(i);
         if (j != Integer.MIN_VALUE) {
            return j;
         } else {
            int k = this.transformer.apply(pX, pZ);
            this.cache.put(i, k);
            if (this.cache.size() > this.maxCache) {
               for(int l = 0; l < this.maxCache / 16; ++l) {
                  this.cache.removeFirstInt();
               }
            }

            return k;
         }
      }
   }

   public int getMaxCache() {
      return this.maxCache;
   }
}