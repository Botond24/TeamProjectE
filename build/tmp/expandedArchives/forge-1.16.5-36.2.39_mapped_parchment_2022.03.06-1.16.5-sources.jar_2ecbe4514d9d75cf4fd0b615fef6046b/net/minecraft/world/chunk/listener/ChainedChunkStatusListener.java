package net.minecraft.world.chunk.listener;

import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.minecraft.util.concurrent.DelegatedTaskExecutor;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChainedChunkStatusListener implements IChunkStatusListener {
   private final IChunkStatusListener delegate;
   private final DelegatedTaskExecutor<Runnable> mailbox;

   public ChainedChunkStatusListener(IChunkStatusListener p_i50696_1_, Executor p_i50696_2_) {
      this.delegate = p_i50696_1_;
      this.mailbox = DelegatedTaskExecutor.create(p_i50696_2_, "progressListener");
   }

   public void updateSpawnPos(ChunkPos pCenter) {
      this.mailbox.tell(() -> {
         this.delegate.updateSpawnPos(pCenter);
      });
   }

   public void onStatusChange(ChunkPos pChunkPosition, @Nullable ChunkStatus pNewStatus) {
      this.mailbox.tell(() -> {
         this.delegate.onStatusChange(pChunkPosition, pNewStatus);
      });
   }

   public void stop() {
      this.mailbox.tell(this.delegate::stop);
   }
}