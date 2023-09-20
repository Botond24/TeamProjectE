package net.minecraft.world;

import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.level.ColorResolver;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IWorldReader extends IBlockDisplayReader, ICollisionReader, BiomeManager.IBiomeReader {
   @Nullable
   IChunk getChunk(int pX, int pZ, ChunkStatus pRequiredStatus, boolean pNonnull);

   @Deprecated
   boolean hasChunk(int pChunkX, int pChunkZ);

   int getHeight(Heightmap.Type pHeightmapType, int pX, int pZ);

   int getSkyDarken();

   BiomeManager getBiomeManager();

   default Biome getBiome(BlockPos pPos) {
      return this.getBiomeManager().getBiome(pPos);
   }

   default Stream<BlockState> getBlockStatesIfLoaded(AxisAlignedBB pAabb) {
      int i = MathHelper.floor(pAabb.minX);
      int j = MathHelper.floor(pAabb.maxX);
      int k = MathHelper.floor(pAabb.minY);
      int l = MathHelper.floor(pAabb.maxY);
      int i1 = MathHelper.floor(pAabb.minZ);
      int j1 = MathHelper.floor(pAabb.maxZ);
      return this.hasChunksAt(i, k, i1, j, l, j1) ? this.getBlockStates(pAabb) : Stream.empty();
   }

   @OnlyIn(Dist.CLIENT)
   default int getBlockTint(BlockPos pBlockPos, ColorResolver pColorResolver) {
      return pColorResolver.getColor(this.getBiome(pBlockPos), (double)pBlockPos.getX(), (double)pBlockPos.getZ());
   }

   default Biome getNoiseBiome(int pX, int pY, int pZ) {
      IChunk ichunk = this.getChunk(pX >> 2, pZ >> 2, ChunkStatus.BIOMES, false);
      return ichunk != null && ichunk.getBiomes() != null ? ichunk.getBiomes().getNoiseBiome(pX, pY, pZ) : this.getUncachedNoiseBiome(pX, pY, pZ);
   }

   Biome getUncachedNoiseBiome(int pX, int pY, int pZ);

   boolean isClientSide();

   @Deprecated
   int getSeaLevel();

   DimensionType dimensionType();

   default BlockPos getHeightmapPos(Heightmap.Type pHeightmapType, BlockPos pPos) {
      return new BlockPos(pPos.getX(), this.getHeight(pHeightmapType, pPos.getX(), pPos.getZ()), pPos.getZ());
   }

   /**
    * Checks to see if an air block exists at the provided location. Note that this only checks to see if the blocks
    * material is set to air, meaning it is possible for non-vanilla blocks to still pass this check.
    */
   default boolean isEmptyBlock(BlockPos pPos) {
      return this.getBlockState(pPos).isAir(this, pPos);
   }

   default boolean canSeeSkyFromBelowWater(BlockPos pPos) {
      if (pPos.getY() >= this.getSeaLevel()) {
         return this.canSeeSky(pPos);
      } else {
         BlockPos blockpos = new BlockPos(pPos.getX(), this.getSeaLevel(), pPos.getZ());
         if (!this.canSeeSky(blockpos)) {
            return false;
         } else {
            for(BlockPos blockpos1 = blockpos.below(); blockpos1.getY() > pPos.getY(); blockpos1 = blockpos1.below()) {
               BlockState blockstate = this.getBlockState(blockpos1);
               if (blockstate.getLightBlock(this, blockpos1) > 0 && !blockstate.getMaterial().isLiquid()) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   @Deprecated
   default float getBrightness(BlockPos pPos) {
      return this.dimensionType().brightness(this.getMaxLocalRawBrightness(pPos));
   }

   default int getDirectSignal(BlockPos pPos, Direction pDirection) {
      return this.getBlockState(pPos).getDirectSignal(this, pPos, pDirection);
   }

   default IChunk getChunk(BlockPos pPos) {
      return this.getChunk(pPos.getX() >> 4, pPos.getZ() >> 4);
   }

   default IChunk getChunk(int pChunkX, int pChunkZ) {
      return this.getChunk(pChunkX, pChunkZ, ChunkStatus.FULL, true);
   }

   default IChunk getChunk(int pChunkX, int pChunkZ, ChunkStatus pRequiredStatus) {
      return this.getChunk(pChunkX, pChunkZ, pRequiredStatus, true);
   }

   @Nullable
   default IBlockReader getChunkForCollisions(int pChunkX, int pChunkZ) {
      return this.getChunk(pChunkX, pChunkZ, ChunkStatus.EMPTY, false);
   }

   default boolean isWaterAt(BlockPos pPos) {
      return this.getFluidState(pPos).is(FluidTags.WATER);
   }

   /**
    * Checks if any of the blocks within the aabb are liquids.
    */
   default boolean containsAnyLiquid(AxisAlignedBB pBb) {
      int i = MathHelper.floor(pBb.minX);
      int j = MathHelper.ceil(pBb.maxX);
      int k = MathHelper.floor(pBb.minY);
      int l = MathHelper.ceil(pBb.maxY);
      int i1 = MathHelper.floor(pBb.minZ);
      int j1 = MathHelper.ceil(pBb.maxZ);
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

      for(int k1 = i; k1 < j; ++k1) {
         for(int l1 = k; l1 < l; ++l1) {
            for(int i2 = i1; i2 < j1; ++i2) {
               BlockState blockstate = this.getBlockState(blockpos$mutable.set(k1, l1, i2));
               if (!blockstate.getFluidState().isEmpty()) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   default int getMaxLocalRawBrightness(BlockPos pPos) {
      return this.getMaxLocalRawBrightness(pPos, this.getSkyDarken());
   }

   default int getMaxLocalRawBrightness(BlockPos pPos, int pAmount) {
      return pPos.getX() >= -30000000 && pPos.getZ() >= -30000000 && pPos.getX() < 30000000 && pPos.getZ() < 30000000 ? this.getRawBrightness(pPos, pAmount) : 15;
   }

   @Deprecated
   default boolean hasChunkAt(BlockPos pPos) {
      return this.hasChunk(pPos.getX() >> 4, pPos.getZ() >> 4);
   }

   default boolean isAreaLoaded(BlockPos center, int range) {
      return this.hasChunksAt(center.offset(-range, -range, -range), center.offset(range, range, range));
   }

   @Deprecated
   default boolean hasChunksAt(BlockPos pFrom, BlockPos pTo) {
      return this.hasChunksAt(pFrom.getX(), pFrom.getY(), pFrom.getZ(), pTo.getX(), pTo.getY(), pTo.getZ());
   }

   @Deprecated
   default boolean hasChunksAt(int pFromX, int pFromY, int pFromZ, int pToX, int pToY, int pToZ) {
      if (pToY >= 0 && pFromY < 256) {
         pFromX = pFromX >> 4;
         pFromZ = pFromZ >> 4;
         pToX = pToX >> 4;
         pToZ = pToZ >> 4;

         for(int i = pFromX; i <= pToX; ++i) {
            for(int j = pFromZ; j <= pToZ; ++j) {
               if (!this.hasChunk(i, j)) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }
}
