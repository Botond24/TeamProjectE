package net.minecraft.world.gen.feature;

import java.util.BitSet;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;

public class WorldDecoratingHelper {
   private final ISeedReader level;
   private final ChunkGenerator generator;

   public WorldDecoratingHelper(ISeedReader pLevel, ChunkGenerator pGenerator) {
      this.level = pLevel;
      this.generator = pGenerator;
   }

   public int getHeight(Heightmap.Type pHeightmap, int pX, int pZ) {
      return this.level.getHeight(pHeightmap, pX, pZ);
   }

   public int getGenDepth() {
      return this.generator.getGenDepth();
   }

   public int getSeaLevel() {
      return this.generator.getSeaLevel();
   }

   public BitSet getCarvingMask(ChunkPos pChunkPos, GenerationStage.Carving pCarving) {
      return ((ChunkPrimer)this.level.getChunk(pChunkPos.x, pChunkPos.z)).getOrCreateCarvingMask(pCarving);
   }

   public BlockState getBlockState(BlockPos pPos) {
      return this.level.getBlockState(pPos);
   }
}