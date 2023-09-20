package net.minecraft.client.audio;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AudioStreamManager {
   private final IResourceManager resourceManager;
   private final Map<ResourceLocation, CompletableFuture<AudioStreamBuffer>> cache = Maps.newHashMap();

   public AudioStreamManager(IResourceManager pResourceManager) {
      this.resourceManager = pResourceManager;
   }

   public CompletableFuture<AudioStreamBuffer> getCompleteBuffer(ResourceLocation pSoundID) {
      return this.cache.computeIfAbsent(pSoundID, (p_217913_1_) -> {
         return CompletableFuture.supplyAsync(() -> {
            try (
               IResource iresource = this.resourceManager.getResource(p_217913_1_);
               InputStream inputstream = iresource.getInputStream();
               OggAudioStream oggaudiostream = new OggAudioStream(inputstream);
            ) {
               ByteBuffer bytebuffer = oggaudiostream.readAll();
               return new AudioStreamBuffer(bytebuffer, oggaudiostream.getFormat());
            } catch (IOException ioexception) {
               throw new CompletionException(ioexception);
            }
         }, Util.backgroundExecutor());
      });
   }

   public CompletableFuture<IAudioStream> getStream(ResourceLocation pResourceLocation, boolean pIsWrapper) {
      return CompletableFuture.supplyAsync(() -> {
         try {
            IResource iresource = this.resourceManager.getResource(pResourceLocation);
            InputStream inputstream = iresource.getInputStream();
            return (IAudioStream)(pIsWrapper ? new OggAudioStreamWrapper(OggAudioStream::new, inputstream) : new OggAudioStream(inputstream));
         } catch (IOException ioexception) {
            throw new CompletionException(ioexception);
         }
      }, Util.backgroundExecutor());
   }

   public void clear() {
      this.cache.values().forEach((p_217910_0_) -> {
         p_217910_0_.thenAccept(AudioStreamBuffer::discardAlBuffer);
      });
      this.cache.clear();
   }

   public CompletableFuture<?> preload(Collection<Sound> pSounds) {
      return CompletableFuture.allOf(pSounds.stream().map((p_217911_1_) -> {
         return this.getCompleteBuffer(p_217911_1_.getPath());
      }).toArray((p_217916_0_) -> {
         return new CompletableFuture[p_217916_0_];
      }));
   }
}