package net.minecraft.world.chunk.listener;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import javax.annotation.Nullable;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TrackingChunkStatusListener implements IChunkStatusListener {
   private final LoggingChunkStatusListener delegate;
   private final Long2ObjectOpenHashMap<ChunkStatus> statuses;
   private ChunkPos spawnPos = new ChunkPos(0, 0);
   private final int fullDiameter;
   private final int radius;
   private final int diameter;
   private boolean started;

   public TrackingChunkStatusListener(int p_i50695_1_) {
      this.delegate = new LoggingChunkStatusListener(p_i50695_1_);
      this.fullDiameter = p_i50695_1_ * 2 + 1;
      this.radius = p_i50695_1_ + ChunkStatus.maxDistance();
      this.diameter = this.radius * 2 + 1;
      this.statuses = new Long2ObjectOpenHashMap<>();
   }

   public void updateSpawnPos(ChunkPos pCenter) {
      if (this.started) {
         this.delegate.updateSpawnPos(pCenter);
         this.spawnPos = pCenter;
      }
   }

   public void onStatusChange(ChunkPos pChunkPosition, @Nullable ChunkStatus pNewStatus) {
      if (this.started) {
         this.delegate.onStatusChange(pChunkPosition, pNewStatus);
         if (pNewStatus == null) {
            this.statuses.remove(pChunkPosition.toLong());
         } else {
            this.statuses.put(pChunkPosition.toLong(), pNewStatus);
         }

      }
   }

   public void start() {
      this.started = true;
      this.statuses.clear();
   }

   public void stop() {
      this.started = false;
      this.delegate.stop();
   }

   public int getFullDiameter() {
      return this.fullDiameter;
   }

   public int getDiameter() {
      return this.diameter;
   }

   public int getProgress() {
      return this.delegate.getProgress();
   }

   @Nullable
   public ChunkStatus getStatus(int pX, int pZ) {
      return this.statuses.get(ChunkPos.asLong(pX + this.spawnPos.x - this.radius, pZ + this.spawnPos.z - this.radius));
   }
}