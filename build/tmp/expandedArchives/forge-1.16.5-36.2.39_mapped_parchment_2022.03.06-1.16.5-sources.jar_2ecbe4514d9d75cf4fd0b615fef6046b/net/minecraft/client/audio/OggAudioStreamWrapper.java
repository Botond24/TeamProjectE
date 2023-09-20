package net.minecraft.client.audio;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import javax.sound.sampled.AudioFormat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class OggAudioStreamWrapper implements IAudioStream {
   private final OggAudioStreamWrapper.IFactory provider;
   private IAudioStream stream;
   private final BufferedInputStream bufferedInputStream;

   public OggAudioStreamWrapper(OggAudioStreamWrapper.IFactory pProvider, InputStream pInput) throws IOException {
      this.provider = pProvider;
      this.bufferedInputStream = new BufferedInputStream(pInput);
      this.bufferedInputStream.mark(Integer.MAX_VALUE);
      this.stream = pProvider.create(new OggAudioStreamWrapper.Stream(this.bufferedInputStream));
   }

   public AudioFormat getFormat() {
      return this.stream.getFormat();
   }

   public ByteBuffer read(int pSize) throws IOException {
      ByteBuffer bytebuffer = this.stream.read(pSize);
      if (!bytebuffer.hasRemaining()) {
         this.stream.close();
         this.bufferedInputStream.reset();
         this.stream = this.provider.create(new OggAudioStreamWrapper.Stream(this.bufferedInputStream));
         bytebuffer = this.stream.read(pSize);
      }

      return bytebuffer;
   }

   public void close() throws IOException {
      this.stream.close();
      this.bufferedInputStream.close();
   }

   @FunctionalInterface
   @OnlyIn(Dist.CLIENT)
   public interface IFactory {
      IAudioStream create(InputStream p_create_1_) throws IOException;
   }

   @OnlyIn(Dist.CLIENT)
   static class Stream extends FilterInputStream {
      private Stream(InputStream pInput) {
         super(pInput);
      }

      public void close() {
      }
   }
}