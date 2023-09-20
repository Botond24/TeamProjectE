package net.minecraft.world.gen.feature.structure;

import com.mojang.datafixers.DataFixUtils;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.IStructureReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;

public class StructureManager {
   private final IWorld level;
   private final DimensionGeneratorSettings worldGenSettings;

   public StructureManager(IWorld pLevel, DimensionGeneratorSettings pWorldGenSettings) {
      this.level = pLevel;
      this.worldGenSettings = pWorldGenSettings;
   }

   public StructureManager forWorldGenRegion(WorldGenRegion pRegion) {
      if (pRegion.getLevel() != this.level) {
         throw new IllegalStateException("Using invalid feature manager (source level: " + pRegion.getLevel() + ", region: " + pRegion);
      } else {
         return new StructureManager(pRegion, this.worldGenSettings);
      }
   }

   public Stream<? extends StructureStart<?>> startsForFeature(SectionPos pPos, Structure<?> pStructure) {
      return this.level.getChunk(pPos.x(), pPos.z(), ChunkStatus.STRUCTURE_REFERENCES).getReferencesForFeature(pStructure).stream().map((p_235015_0_) -> {
         return SectionPos.of(new ChunkPos(p_235015_0_), 0);
      }).map((p_235006_2_) -> {
         return this.getStartForFeature(p_235006_2_, pStructure, this.level.getChunk(p_235006_2_.x(), p_235006_2_.z(), ChunkStatus.STRUCTURE_STARTS));
      }).filter((p_235007_0_) -> {
         return p_235007_0_ != null && p_235007_0_.isValid();
      });
   }

   @Nullable
   public StructureStart<?> getStartForFeature(SectionPos pSectionPos, Structure<?> pStructure, IStructureReader pReader) {
      return pReader.getStartForFeature(pStructure);
   }

   public void setStartForFeature(SectionPos pSectionPos, Structure<?> pStructure, StructureStart<?> pStart, IStructureReader pReader) {
      pReader.setStartForFeature(pStructure, pStart);
   }

   public void addReferenceForFeature(SectionPos pSectionPos, Structure<?> pStructure, long pChunkValue, IStructureReader pReader) {
      pReader.addReferenceForFeature(pStructure, pChunkValue);
   }

   public boolean shouldGenerateFeatures() {
      return this.worldGenSettings.generateFeatures();
   }

   public StructureStart<?> getStructureAt(BlockPos pPos, boolean p_235010_2_, Structure<?> pStructure) {
      return DataFixUtils.orElse(this.startsForFeature(SectionPos.of(pPos), pStructure).filter((p_235009_1_) -> {
         return p_235009_1_.getBoundingBox().isInside(pPos);
      }).filter((p_235016_2_) -> {
         return !p_235010_2_ || p_235016_2_.getPieces().stream().anyMatch((p_235008_1_) -> {
            return p_235008_1_.getBoundingBox().isInside(pPos);
         });
      }).findFirst(), StructureStart.INVALID_START);
   }
}