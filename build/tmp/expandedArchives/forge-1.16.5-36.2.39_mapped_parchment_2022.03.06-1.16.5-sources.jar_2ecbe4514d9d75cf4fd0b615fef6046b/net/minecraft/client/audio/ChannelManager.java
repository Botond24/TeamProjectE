package net.minecraft.client.audio;

import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChannelManager {
   private final Set<ChannelManager.Entry> channels = Sets.newIdentityHashSet();
   private final SoundSystem library;
   private final Executor executor;

   public ChannelManager(SoundSystem pLibrary, Executor pExecutor) {
      this.library = pLibrary;
      this.executor = pExecutor;
   }

   public CompletableFuture<ChannelManager.Entry> createHandle(SoundSystem.Mode pSystemMode) {
      CompletableFuture<ChannelManager.Entry> completablefuture = new CompletableFuture<>();
      this.executor.execute(() -> {
         SoundSource soundsource = this.library.acquireChannel(pSystemMode);
         if (soundsource != null) {
            ChannelManager.Entry channelmanager$entry = new ChannelManager.Entry(soundsource);
            this.channels.add(channelmanager$entry);
            completablefuture.complete(channelmanager$entry);
         } else {
            completablefuture.complete((ChannelManager.Entry)null);
         }

      });
      return completablefuture;
   }

   public void executeOnChannels(Consumer<Stream<SoundSource>> pSourceStreamConsumer) {
      this.executor.execute(() -> {
         pSourceStreamConsumer.accept(this.channels.stream().map((p_217896_0_) -> {
            return p_217896_0_.channel;
         }).filter(Objects::nonNull));
      });
   }

   public void scheduleTick() {
      this.executor.execute(() -> {
         Iterator<ChannelManager.Entry> iterator = this.channels.iterator();

         while(iterator.hasNext()) {
            ChannelManager.Entry channelmanager$entry = iterator.next();
            channelmanager$entry.channel.updateStream();
            if (channelmanager$entry.channel.stopped()) {
               channelmanager$entry.release();
               iterator.remove();
            }
         }

      });
   }

   public void clear() {
      this.channels.forEach(ChannelManager.Entry::release);
      this.channels.clear();
   }

   @OnlyIn(Dist.CLIENT)
   public class Entry {
      @Nullable
      private SoundSource channel;
      private boolean stopped;

      public boolean isStopped() {
         return this.stopped;
      }

      public Entry(SoundSource pChannel) {
         this.channel = pChannel;
      }

      public void execute(Consumer<SoundSource> pSoundConsumer) {
         ChannelManager.this.executor.execute(() -> {
            if (this.channel != null) {
               pSoundConsumer.accept(this.channel);
            }

         });
      }

      public void release() {
         this.stopped = true;
         ChannelManager.this.library.releaseChannel(this.channel);
         this.channel = null;
      }
   }
}