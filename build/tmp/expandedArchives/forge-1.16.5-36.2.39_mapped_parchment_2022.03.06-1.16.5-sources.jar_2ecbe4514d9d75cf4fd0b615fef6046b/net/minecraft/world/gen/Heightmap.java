package net.minecraft.world.gen;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.BitArray;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Heightmap {
   private static final Predicate<BlockState> NOT_AIR = (p_222688_0_) -> {
      return !p_222688_0_.isAir();
   };
   private static final Predicate<BlockState> MATERIAL_MOTION_BLOCKING = (p_222689_0_) -> {
      return p_222689_0_.getMaterial().blocksMotion();
   };
   private final BitArray data = new BitArray(9, 256);
   private final Predicate<BlockState> isOpaque;
   private final IChunk chunk;

   public Heightmap(IChunk pChunk, Heightmap.Type pType) {
      this.isOpaque = pType.isOpaque();
      this.chunk = pChunk;
   }

   public static void primeHeightmaps(IChunk pChunk, Set<Heightmap.Type> pTypes) {
      int i = pTypes.size();
      ObjectList<Heightmap> objectlist = new ObjectArrayList<>(i);
      ObjectListIterator<Heightmap> objectlistiterator = objectlist.iterator();
      int j = pChunk.getHighestSectionPosition() + 16;
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

      for(int k = 0; k < 16; ++k) {
         for(int l = 0; l < 16; ++l) {
            for(Heightmap.Type heightmap$type : pTypes) {
               objectlist.add(pChunk.getOrCreateHeightmapUnprimed(heightmap$type));
            }

            for(int i1 = j - 1; i1 >= 0; --i1) {
               blockpos$mutable.set(k, i1, l);
               BlockState blockstate = pChunk.getBlockState(blockpos$mutable);
               if (!blockstate.is(Blocks.AIR)) {
                  while(objectlistiterator.hasNext()) {
                     Heightmap heightmap = objectlistiterator.next();
                     if (heightmap.isOpaque.test(blockstate)) {
                        heightmap.setHeight(k, l, i1 + 1);
                        objectlistiterator.remove();
                     }
                  }

                  if (objectlist.isEmpty()) {
                     break;
                  }

                  objectlistiterator.back(i);
               }
            }
         }
      }

   }

   public boolean update(int pX, int pY, int pZ, BlockState pState) {
      int i = this.getFirstAvailable(pX, pZ);
      if (pY <= i - 2) {
         return false;
      } else {
         if (this.isOpaque.test(pState)) {
            if (pY >= i) {
               this.setHeight(pX, pZ, pY + 1);
               return true;
            }
         } else if (i - 1 == pY) {
            BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

            for(int j = pY - 1; j >= 0; --j) {
               blockpos$mutable.set(pX, j, pZ);
               if (this.isOpaque.test(this.chunk.getBlockState(blockpos$mutable))) {
                  this.setHeight(pX, pZ, j + 1);
                  return true;
               }
            }

            this.setHeight(pX, pZ, 0);
            return true;
         }

         return false;
      }
   }

   public int getFirstAvailable(int pX, int pZ) {
      return this.getFirstAvailable(getIndex(pX, pZ));
   }

   private int getFirstAvailable(int pDataArrayIndex) {
      return this.data.get(pDataArrayIndex);
   }

   private void setHeight(int pX, int pZ, int pValue) {
      this.data.set(getIndex(pX, pZ), pValue);
   }

   public void setRawData(long[] p_202268_1_) {
      System.arraycopy(p_202268_1_, 0, this.data.getRaw(), 0, p_202268_1_.length);
   }

   public long[] getRawData() {
      return this.data.getRaw();
   }

   private static int getIndex(int pX, int pZ) {
      return pX + pZ * 16;
   }

   public static enum Type implements IStringSerializable {
      WORLD_SURFACE_WG("WORLD_SURFACE_WG", Heightmap.Usage.WORLDGEN, Heightmap.NOT_AIR),
      WORLD_SURFACE("WORLD_SURFACE", Heightmap.Usage.CLIENT, Heightmap.NOT_AIR),
      OCEAN_FLOOR_WG("OCEAN_FLOOR_WG", Heightmap.Usage.WORLDGEN, Heightmap.MATERIAL_MOTION_BLOCKING),
      OCEAN_FLOOR("OCEAN_FLOOR", Heightmap.Usage.LIVE_WORLD, Heightmap.MATERIAL_MOTION_BLOCKING),
      MOTION_BLOCKING("MOTION_BLOCKING", Heightmap.Usage.CLIENT, (p_222680_0_) -> {
         return p_222680_0_.getMaterial().blocksMotion() || !p_222680_0_.getFluidState().isEmpty();
      }),
      MOTION_BLOCKING_NO_LEAVES("MOTION_BLOCKING_NO_LEAVES", Heightmap.Usage.LIVE_WORLD, (p_222682_0_) -> {
         return (p_222682_0_.getMaterial().blocksMotion() || !p_222682_0_.getFluidState().isEmpty()) && !(p_222682_0_.getBlock() instanceof LeavesBlock);
      });

      public static final Codec<Heightmap.Type> CODEC = IStringSerializable.fromEnum(Heightmap.Type::values, Heightmap.Type::getFromKey);
      private final String serializationKey;
      private final Heightmap.Usage usage;
      private final Predicate<BlockState> isOpaque;
      private static final Map<String, Heightmap.Type> REVERSE_LOOKUP = Util.make(Maps.newHashMap(), (p_222679_0_) -> {
         for(Heightmap.Type heightmap$type : values()) {
            p_222679_0_.put(heightmap$type.serializationKey, heightmap$type);
         }

      });

      private Type(String pSerializationKey, Heightmap.Usage pUsage, Predicate<BlockState> pIsOpaque) {
         this.serializationKey = pSerializationKey;
         this.usage = pUsage;
         this.isOpaque = pIsOpaque;
      }

      public String getSerializationKey() {
         return this.serializationKey;
      }

      public boolean sendToClient() {
         return this.usage == Heightmap.Usage.CLIENT;
      }

      @OnlyIn(Dist.CLIENT)
      public boolean keepAfterWorldgen() {
         return this.usage != Heightmap.Usage.WORLDGEN;
      }

      @Nullable
      public static Heightmap.Type getFromKey(String p_203501_0_) {
         return REVERSE_LOOKUP.get(p_203501_0_);
      }

      public Predicate<BlockState> isOpaque() {
         return this.isOpaque;
      }

      public String getSerializedName() {
         return this.serializationKey;
      }
   }

   public static enum Usage {
      WORLDGEN,
      LIVE_WORLD,
      CLIENT;
   }
}