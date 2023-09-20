package net.minecraft.world;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.storage.IWorldInfo;

public interface IWorld extends IBiomeReader, IDayTimeReader {
   default long dayTime() {
      return this.getLevelData().getDayTime();
   }

   ITickList<Block> getBlockTicks();

   ITickList<Fluid> getLiquidTicks();

   /**
    * Returns the world's WorldInfo object
    */
   IWorldInfo getLevelData();

   DifficultyInstance getCurrentDifficultyAt(BlockPos pPos);

   default Difficulty getDifficulty() {
      return this.getLevelData().getDifficulty();
   }

   /**
    * Gets the world's chunk provider
    */
   AbstractChunkProvider getChunkSource();

   default boolean hasChunk(int pChunkX, int pChunkZ) {
      return this.getChunkSource().hasChunk(pChunkX, pChunkZ);
   }

   Random getRandom();

   default void blockUpdated(BlockPos pPos, Block pBlock) {
   }

   /**
    * Plays a sound. On the server, the sound is broadcast to all nearby <em>except</em> the given player. On the
    * client, the sound only plays if the given player is the client player. Thus, this method is intended to be called
    * from code running on both sides. The client plays it locally and the server plays it for everyone else.
    */
   void playSound(@Nullable PlayerEntity pPlayer, BlockPos pPos, SoundEvent pSound, SoundCategory pCategory, float pVolume, float pPitch);

   void addParticle(IParticleData pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed);

   void levelEvent(@Nullable PlayerEntity pPlayer, int pType, BlockPos pPos, int pData);

   default int getHeight() {
      return this.dimensionType().logicalHeight();
   }

   default void levelEvent(int pType, BlockPos pPos, int pData) {
      this.levelEvent((PlayerEntity)null, pType, pPos, pData);
   }
}