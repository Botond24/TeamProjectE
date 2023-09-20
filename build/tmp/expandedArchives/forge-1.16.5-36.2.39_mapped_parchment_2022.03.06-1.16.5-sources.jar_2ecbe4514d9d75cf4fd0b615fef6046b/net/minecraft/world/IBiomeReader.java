package net.minecraft.world;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.IWorldGenerationReader;

public interface IBiomeReader extends IEntityReader, IWorldReader, IWorldGenerationReader {
   default Stream<VoxelShape> getEntityCollisions(@Nullable Entity pEntity, AxisAlignedBB pArea, Predicate<Entity> pFilter) {
      return IEntityReader.super.getEntityCollisions(pEntity, pArea, pFilter);
   }

   default boolean isUnobstructed(@Nullable Entity pEntity, VoxelShape pShape) {
      return IEntityReader.super.isUnobstructed(pEntity, pShape);
   }

   default BlockPos getHeightmapPos(Heightmap.Type pHeightmapType, BlockPos pPos) {
      return IWorldReader.super.getHeightmapPos(pHeightmapType, pPos);
   }

   DynamicRegistries registryAccess();

   default Optional<RegistryKey<Biome>> getBiomeName(BlockPos pPos) {
      return this.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getResourceKey(this.getBiome(pPos));
   }
}