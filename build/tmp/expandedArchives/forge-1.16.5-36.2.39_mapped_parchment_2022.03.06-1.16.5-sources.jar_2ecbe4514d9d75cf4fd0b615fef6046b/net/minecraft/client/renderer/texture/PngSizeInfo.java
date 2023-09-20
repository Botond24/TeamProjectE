package net.minecraft.client.renderer.texture;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.stb.STBIEOFCallback;
import org.lwjgl.stb.STBIIOCallbacks;
import org.lwjgl.stb.STBIReadCallback;
import org.lwjgl.stb.STBISkipCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@OnlyIn(Dist.CLIENT)
public class PngSizeInfo {
   public final int width;
   public final int height;

   public PngSizeInfo(String pFileName, InputStream pTextureStream) throws IOException {
      try (
         MemoryStack memorystack = MemoryStack.stackPush();
         PngSizeInfo.Reader pngsizeinfo$reader = createCallbacks(pTextureStream);
         STBIReadCallback stbireadcallback = STBIReadCallback.create(pngsizeinfo$reader::read);
         STBISkipCallback stbiskipcallback = STBISkipCallback.create(pngsizeinfo$reader::skip);
         STBIEOFCallback stbieofcallback = STBIEOFCallback.create(pngsizeinfo$reader::eof);
      ) {
         STBIIOCallbacks stbiiocallbacks = STBIIOCallbacks.mallocStack(memorystack);
         stbiiocallbacks.read(stbireadcallback);
         stbiiocallbacks.skip(stbiskipcallback);
         stbiiocallbacks.eof(stbieofcallback);
         IntBuffer intbuffer = memorystack.mallocInt(1);
         IntBuffer intbuffer1 = memorystack.mallocInt(1);
         IntBuffer intbuffer2 = memorystack.mallocInt(1);
         if (!STBImage.stbi_info_from_callbacks(stbiiocallbacks, 0L, intbuffer, intbuffer1, intbuffer2)) {
            throw new IOException("Could not read info from the PNG file " + pFileName + " " + STBImage.stbi_failure_reason());
         }

         this.width = intbuffer.get(0);
         this.height = intbuffer1.get(0);
      }

   }

   private static PngSizeInfo.Reader createCallbacks(InputStream pTextureStream) {
      return (PngSizeInfo.Reader)(pTextureStream instanceof FileInputStream ? new PngSizeInfo.ReaderSeekable(((FileInputStream)pTextureStream).getChannel()) : new PngSizeInfo.ReaderBuffer(Channels.newChannel(pTextureStream)));
   }

   @OnlyIn(Dist.CLIENT)
   abstract static class Reader implements AutoCloseable {
      protected boolean closed;

      private Reader() {
      }

      int read(long pFunctionPointer, long pAdress, int pCapacity) {
         try {
            return this.read(pAdress, pCapacity);
         } catch (IOException ioexception) {
            this.closed = true;
            return 0;
         }
      }

      void skip(long pFunctionPointer, int pOffset) {
         try {
            this.skip(pOffset);
         } catch (IOException ioexception) {
            this.closed = true;
         }

      }

      int eof(long pFunctionPointer) {
         return this.closed ? 1 : 0;
      }

      protected abstract int read(long pAddress, int pCapacity) throws IOException;

      protected abstract void skip(int pOffset) throws IOException;

      public abstract void close() throws IOException;
   }

   @OnlyIn(Dist.CLIENT)
   static class ReaderBuffer extends PngSizeInfo.Reader {
      private final ReadableByteChannel channel;
      private long readBufferAddress = MemoryUtil.nmemAlloc(128L);
      private int bufferSize = 128;
      private int read;
      private int consumed;

      private ReaderBuffer(ReadableByteChannel pChannel) {
         this.channel = pChannel;
      }

      private void fillReadBuffer(int pLength) throws IOException {
         ByteBuffer bytebuffer = MemoryUtil.memByteBuffer(this.readBufferAddress, this.bufferSize);
         if (pLength + this.consumed > this.bufferSize) {
            this.bufferSize = pLength + this.consumed;
            bytebuffer = MemoryUtil.memRealloc(bytebuffer, this.bufferSize);
            this.readBufferAddress = MemoryUtil.memAddress(bytebuffer);
         }

         ((Buffer)bytebuffer).position(this.read);

         while(pLength + this.consumed > this.read) {
            try {
               int i = this.channel.read(bytebuffer);
               if (i == -1) {
                  break;
               }
            } finally {
               this.read = bytebuffer.position();
            }
         }

      }

      public int read(long pAddress, int pCapacity) throws IOException {
         this.fillReadBuffer(pCapacity);
         if (pCapacity + this.consumed > this.read) {
            pCapacity = this.read - this.consumed;
         }

         MemoryUtil.memCopy(this.readBufferAddress + (long)this.consumed, pAddress, (long)pCapacity);
         this.consumed += pCapacity;
         return pCapacity;
      }

      public void skip(int pOffset) throws IOException {
         if (pOffset > 0) {
            this.fillReadBuffer(pOffset);
            if (pOffset + this.consumed > this.read) {
               throw new EOFException("Can't skip past the EOF.");
            }
         }

         if (this.consumed + pOffset < 0) {
            throw new IOException("Can't seek before the beginning: " + (this.consumed + pOffset));
         } else {
            this.consumed += pOffset;
         }
      }

      public void close() throws IOException {
         MemoryUtil.nmemFree(this.readBufferAddress);
         this.channel.close();
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class ReaderSeekable extends PngSizeInfo.Reader {
      private final SeekableByteChannel channel;

      private ReaderSeekable(SeekableByteChannel pChannel) {
         this.channel = pChannel;
      }

      public int read(long pAddress, int pCapacity) throws IOException {
         ByteBuffer bytebuffer = MemoryUtil.memByteBuffer(pAddress, pCapacity);
         return this.channel.read(bytebuffer);
      }

      public void skip(int pOffset) throws IOException {
         this.channel.position(this.channel.position() + (long)pOffset);
      }

      public int eof(long pFunctionPointer) {
         return super.eof(pFunctionPointer) != 0 && this.channel.isOpen() ? 1 : 0;
      }

      public void close() throws IOException {
         this.channel.close();
      }
   }
}