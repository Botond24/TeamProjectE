package net.minecraft.world.lighting;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.IChunkLightProvider;
import net.minecraft.world.chunk.NibbleArray;

public class BlockLightStorage extends SectionLightStorage<BlockLightStorage.StorageMap> {
   protected BlockLightStorage(IChunkLightProvider p_i51300_1_) {
      super(LightType.BLOCK, p_i51300_1_, new BlockLightStorage.StorageMap(new Long2ObjectOpenHashMap<>()));
   }

   protected int getLightValue(long pLevelPos) {
      long i = SectionPos.blockToSection(pLevelPos);
      NibbleArray nibblearray = this.getDataLayer(i, false);
      return nibblearray == null ? 0 : nibblearray.get(SectionPos.sectionRelative(BlockPos.getX(pLevelPos)), SectionPos.sectionRelative(BlockPos.getY(pLevelPos)), SectionPos.sectionRelative(BlockPos.getZ(pLevelPos)));
   }

   public static final class StorageMap extends LightDataMap<BlockLightStorage.StorageMap> {
      public StorageMap(Long2ObjectOpenHashMap<NibbleArray> p_i50064_1_) {
         super(p_i50064_1_);
      }

      public BlockLightStorage.StorageMap copy() {
         return new BlockLightStorage.StorageMap(this.map.clone());
      }
   }
}