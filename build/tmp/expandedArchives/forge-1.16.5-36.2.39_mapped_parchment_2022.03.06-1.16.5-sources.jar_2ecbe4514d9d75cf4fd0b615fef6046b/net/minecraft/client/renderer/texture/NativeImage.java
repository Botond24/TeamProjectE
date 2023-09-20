package net.minecraft.client.renderer.texture;

import com.google.common.base.Charsets;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.util.LWJGLMemoryUntracker;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.stb.STBIWriteCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@OnlyIn(Dist.CLIENT)
public final class NativeImage implements AutoCloseable {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Set<StandardOpenOption> OPEN_OPTIONS = EnumSet.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
   private final NativeImage.PixelFormat format;
   private final int width;
   private final int height;
   private final boolean useStbFree;
   private long pixels;
   private final long size;

   public NativeImage(int pWidth, int pHeight, boolean p_i48122_3_) {
      this(NativeImage.PixelFormat.RGBA, pWidth, pHeight, p_i48122_3_);
   }

   public NativeImage(NativeImage.PixelFormat pFormat, int pWidth, int pHeight, boolean p_i49763_4_) {
      this.format = pFormat;
      this.width = pWidth;
      this.height = pHeight;
      this.size = (long)pWidth * (long)pHeight * (long)pFormat.components();
      this.useStbFree = false;
      if (p_i49763_4_) {
         this.pixels = MemoryUtil.nmemCalloc(1L, this.size);
      } else {
         this.pixels = MemoryUtil.nmemAlloc(this.size);
      }

   }

   private NativeImage(NativeImage.PixelFormat pFormat, int pWidth, int pHeight, boolean pUseStbFree, long pPixels) {
      this.format = pFormat;
      this.width = pWidth;
      this.height = pHeight;
      this.useStbFree = pUseStbFree;
      this.pixels = pPixels;
      this.size = (long)(pWidth * pHeight * pFormat.components());
   }

   public String toString() {
      return "NativeImage[" + this.format + " " + this.width + "x" + this.height + "@" + this.pixels + (this.useStbFree ? "S" : "N") + "]";
   }

   public static NativeImage read(InputStream pTextureStream) throws IOException {
      return read(NativeImage.PixelFormat.RGBA, pTextureStream);
   }

   public static NativeImage read(@Nullable NativeImage.PixelFormat pFormat, InputStream pTextureStream) throws IOException {
      ByteBuffer bytebuffer = null;

      NativeImage nativeimage;
      try {
         bytebuffer = TextureUtil.readResource(pTextureStream);
         ((Buffer)bytebuffer).rewind();
         nativeimage = read(pFormat, bytebuffer);
      } finally {
         MemoryUtil.memFree(bytebuffer);
         IOUtils.closeQuietly(pTextureStream);
      }

      return nativeimage;
   }

   public static NativeImage read(ByteBuffer pTextureData) throws IOException {
      return read(NativeImage.PixelFormat.RGBA, pTextureData);
   }

   public static NativeImage read(@Nullable NativeImage.PixelFormat pFormat, ByteBuffer pTextureData) throws IOException {
      if (pFormat != null && !pFormat.supportedByStb()) {
         throw new UnsupportedOperationException("Don't know how to read format " + pFormat);
      } else if (MemoryUtil.memAddress(pTextureData) == 0L) {
         throw new IllegalArgumentException("Invalid buffer");
      } else {
         NativeImage nativeimage;
         try (MemoryStack memorystack = MemoryStack.stackPush()) {
            IntBuffer intbuffer = memorystack.mallocInt(1);
            IntBuffer intbuffer1 = memorystack.mallocInt(1);
            IntBuffer intbuffer2 = memorystack.mallocInt(1);
            ByteBuffer bytebuffer = STBImage.stbi_load_from_memory(pTextureData, intbuffer, intbuffer1, intbuffer2, pFormat == null ? 0 : pFormat.components);
            if (bytebuffer == null) {
               throw new IOException("Could not load image: " + STBImage.stbi_failure_reason());
            }

            nativeimage = new NativeImage(pFormat == null ? NativeImage.PixelFormat.getStbFormat(intbuffer2.get(0)) : pFormat, intbuffer.get(0), intbuffer1.get(0), true, MemoryUtil.memAddress(bytebuffer));
         }

         return nativeimage;
      }
   }

   private static void setClamp(boolean p_195707_0_) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      if (p_195707_0_) {
         GlStateManager._texParameter(3553, 10242, 10496);
         GlStateManager._texParameter(3553, 10243, 10496);
      } else {
         GlStateManager._texParameter(3553, 10242, 10497);
         GlStateManager._texParameter(3553, 10243, 10497);
      }

   }

   private static void setFilter(boolean pLinear, boolean pMipmap) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      if (pLinear) {
         GlStateManager._texParameter(3553, 10241, pMipmap ? 9987 : 9729);
         GlStateManager._texParameter(3553, 10240, 9729);
      } else {
         GlStateManager._texParameter(3553, 10241, pMipmap ? 9986 : 9728);
         GlStateManager._texParameter(3553, 10240, 9728);
      }

   }

   private void checkAllocated() {
      if (this.pixels == 0L) {
         throw new IllegalStateException("Image is not allocated.");
      }
   }

   public void close() {
      if (this.pixels != 0L) {
         if (this.useStbFree) {
            STBImage.nstbi_image_free(this.pixels);
         } else {
            MemoryUtil.nmemFree(this.pixels);
         }
      }

      this.pixels = 0L;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public NativeImage.PixelFormat format() {
      return this.format;
   }

   public int getPixelRGBA(int pX, int pY) {
      if (this.format != NativeImage.PixelFormat.RGBA) {
         throw new IllegalArgumentException(String.format("getPixelRGBA only works on RGBA images; have %s", this.format));
      } else if (pX >= 0 && pY >= 0 && pX < this.width && pY < this.height) { //Fix MC-162953 bounds checks in `NativeImage`
         this.checkAllocated();
         long i = (long)((pX + pY * this.width) * 4);
         return MemoryUtil.memGetInt(this.pixels + i);
      } else {
         throw new IllegalArgumentException(String.format("(%s, %s) outside of image bounds (%s, %s)", pX, pY, this.width, this.height));
      }
   }

   public void setPixelRGBA(int pX, int pY, int pAbgrColor) {
      if (this.format != NativeImage.PixelFormat.RGBA) {
         throw new IllegalArgumentException(String.format("getPixelRGBA only works on RGBA images; have %s", this.format));
      } else if (pX >= 0 && pY >= 0 && pX < this.width && pY < this.height) { //Fix MC-162953 bounds checks in `NativeImage`
         this.checkAllocated();
         long i = (long)((pX + pY * this.width) * 4);
         MemoryUtil.memPutInt(this.pixels + i, pAbgrColor);
      } else {
         throw new IllegalArgumentException(String.format("(%s, %s) outside of image bounds (%s, %s)", pX, pY, this.width, this.height));
      }
   }

   public byte getLuminanceOrAlpha(int pX, int pY) {
      if (!this.format.hasLuminanceOrAlpha()) {
         throw new IllegalArgumentException(String.format("no luminance or alpha in %s", this.format));
      } else if (pX >= 0 && pY >= 0 && pX < this.width && pY < this.height) { //Fix MC-162953 bounds checks in `NativeImage`
         int i = (pX + pY * this.width) * this.format.components() + this.format.luminanceOrAlphaOffset() / 8;
         return MemoryUtil.memGetByte(this.pixels + (long)i);
      } else {
         throw new IllegalArgumentException(String.format("(%s, %s) outside of image bounds (%s, %s)", pX, pY, this.width, this.height));
      }
   }

   @Deprecated
   public int[] makePixelArray() {
      if (this.format != NativeImage.PixelFormat.RGBA) {
         throw new UnsupportedOperationException("can only call makePixelArray for RGBA images.");
      } else {
         this.checkAllocated();
         int[] aint = new int[this.getWidth() * this.getHeight()];

         for(int i = 0; i < this.getHeight(); ++i) {
            for(int j = 0; j < this.getWidth(); ++j) {
               int k = this.getPixelRGBA(j, i);
               int l = getA(k);
               int i1 = getB(k);
               int j1 = getG(k);
               int k1 = getR(k);
               int l1 = l << 24 | k1 << 16 | j1 << 8 | i1;
               aint[j + i * this.getWidth()] = l1;
            }
         }

         return aint;
      }
   }

   public void upload(int pLevel, int pXOffset, int pYOffset, boolean pMipmap) {
      this.upload(pLevel, pXOffset, pYOffset, 0, 0, this.width, this.height, false, pMipmap);
   }

   public void upload(int pLevel, int pXOffset, int pYOffset, int pUnpackSkipPixels, int pUnpackSkipRows, int pWidth, int pHeight, boolean pMipmap, boolean pAutoClose) {
      this.upload(pLevel, pXOffset, pYOffset, pUnpackSkipPixels, pUnpackSkipRows, pWidth, pHeight, false, false, pMipmap, pAutoClose);
   }

   public void upload(int pLevel, int pXOffset, int pYOffset, int pUnpackSkipPixels, int pUnpackSkipRows, int pWidth, int pHeight, boolean pBlur, boolean pClamp, boolean pMipmap, boolean pAutoClose) {
      if (!RenderSystem.isOnRenderThreadOrInit()) {
         RenderSystem.recordRenderCall(() -> {
            this._upload(pLevel, pXOffset, pYOffset, pUnpackSkipPixels, pUnpackSkipRows, pWidth, pHeight, pBlur, pClamp, pMipmap, pAutoClose);
         });
      } else {
         this._upload(pLevel, pXOffset, pYOffset, pUnpackSkipPixels, pUnpackSkipRows, pWidth, pHeight, pBlur, pClamp, pMipmap, pAutoClose);
      }

   }

   private void _upload(int pLevel, int pXOffset, int pYOffset, int pUnpackSkipPixels, int pUnpackSkipRows, int pWidth, int pHeight, boolean pBlur, boolean pClamp, boolean pMipmap, boolean pAutoClose) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
      this.checkAllocated();
      setFilter(pBlur, pMipmap);
      setClamp(pClamp);
      if (pWidth == this.getWidth()) {
         GlStateManager._pixelStore(3314, 0);
      } else {
         GlStateManager._pixelStore(3314, this.getWidth());
      }

      GlStateManager._pixelStore(3316, pUnpackSkipPixels);
      GlStateManager._pixelStore(3315, pUnpackSkipRows);
      this.format.setUnpackPixelStoreState();
      GlStateManager._texSubImage2D(3553, pLevel, pXOffset, pYOffset, pWidth, pHeight, this.format.glFormat(), 5121, this.pixels);
      if (pAutoClose) {
         this.close();
      }

   }

   public void downloadTexture(int pLevel, boolean pOpaque) {
      RenderSystem.assertThread(RenderSystem::isOnRenderThread);
      this.checkAllocated();
      this.format.setPackPixelStoreState();
      GlStateManager._getTexImage(3553, pLevel, this.format.glFormat(), 5121, this.pixels);
      if (pOpaque && this.format.hasAlpha()) {
         for(int i = 0; i < this.getHeight(); ++i) {
            for(int j = 0; j < this.getWidth(); ++j) {
               this.setPixelRGBA(j, i, this.getPixelRGBA(j, i) | 255 << this.format.alphaOffset());
            }
         }
      }

   }

   public void writeToFile(File pFile) throws IOException {
      this.writeToFile(pFile.toPath());
   }

   /**
    * Renders given glyph into this image
    */
   public void copyFromFont(STBTTFontinfo pInfo, int pGlyphIndex, int pWidth, int pHeight, float pScaleX, float pScaleY, float pShiftX, float pShiftY, int pX, int pY) {
      if (pX >= 0 && pX + pWidth <= this.getWidth() && pY >= 0 && pY + pHeight <= this.getHeight()) {
         if (this.format.components() != 1) {
            throw new IllegalArgumentException("Can only write fonts into 1-component images.");
         } else {
            STBTruetype.nstbtt_MakeGlyphBitmapSubpixel(pInfo.address(), this.pixels + (long)pX + (long)(pY * this.getWidth()), pWidth, pHeight, this.getWidth(), pScaleX, pScaleY, pShiftX, pShiftY, pGlyphIndex);
         }
      } else {
         throw new IllegalArgumentException(String.format("Out of bounds: start: (%s, %s) (size: %sx%s); size: %sx%s", pX, pY, pWidth, pHeight, this.getWidth(), this.getHeight()));
      }
   }

   public void writeToFile(Path pPath) throws IOException {
      if (!this.format.supportedByStb()) {
         throw new UnsupportedOperationException("Don't know how to write format " + this.format);
      } else {
         this.checkAllocated();

         try (WritableByteChannel writablebytechannel = Files.newByteChannel(pPath, OPEN_OPTIONS)) {
            if (!this.writeToChannel(writablebytechannel)) {
               throw new IOException("Could not write image to the PNG file \"" + pPath.toAbsolutePath() + "\": " + STBImage.stbi_failure_reason());
            }
         }

      }
   }

   public byte[] asByteArray() throws IOException {
      byte[] abyte;
      try (
         ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
         WritableByteChannel writablebytechannel = Channels.newChannel(bytearrayoutputstream);
      ) {
         if (!this.writeToChannel(writablebytechannel)) {
            throw new IOException("Could not write image to byte array: " + STBImage.stbi_failure_reason());
         }

         abyte = bytearrayoutputstream.toByteArray();
      }

      return abyte;
   }

   private boolean writeToChannel(WritableByteChannel pChannel) throws IOException {
      NativeImage.WriteCallback nativeimage$writecallback = new NativeImage.WriteCallback(pChannel);

      boolean flag;
      try {
         int i = Math.min(this.getHeight(), Integer.MAX_VALUE / this.getWidth() / this.format.components());
         if (i < this.getHeight()) {
            LOGGER.warn("Dropping image height from {} to {} to fit the size into 32-bit signed int", this.getHeight(), i);
         }

         if (STBImageWrite.nstbi_write_png_to_func(nativeimage$writecallback.address(), 0L, this.getWidth(), i, this.format.components(), this.pixels, 0) != 0) {
            nativeimage$writecallback.throwIfException();
            return true;
         }

         flag = false;
      } finally {
         nativeimage$writecallback.free();
      }

      return flag;
   }

   public void copyFrom(NativeImage pOther) {
      if (pOther.format() != this.format) {
         throw new UnsupportedOperationException("Image formats don't match.");
      } else {
         int i = this.format.components();
         this.checkAllocated();
         pOther.checkAllocated();
         if (this.width == pOther.width) {
            MemoryUtil.memCopy(pOther.pixels, this.pixels, Math.min(this.size, pOther.size));
         } else {
            int j = Math.min(this.getWidth(), pOther.getWidth());
            int k = Math.min(this.getHeight(), pOther.getHeight());

            for(int l = 0; l < k; ++l) {
               int i1 = l * pOther.getWidth() * i;
               int j1 = l * this.getWidth() * i;
               MemoryUtil.memCopy(pOther.pixels + (long)i1, this.pixels + (long)j1, (long)j);
            }
         }

      }
   }

   public void fillRect(int pX, int pY, int pWidth, int pHeight, int pValue) {
      for(int i = pY; i < pY + pHeight; ++i) {
         for(int j = pX; j < pX + pWidth; ++j) {
            this.setPixelRGBA(j, i, pValue);
         }
      }

   }

   public void copyRect(int pXFrom, int pYFrom, int pXToDelta, int pYToDelta, int pWidth, int pHeight, boolean pMirrorX, boolean pMirrorY) {
      for(int i = 0; i < pHeight; ++i) {
         for(int j = 0; j < pWidth; ++j) {
            int k = pMirrorX ? pWidth - 1 - j : j;
            int l = pMirrorY ? pHeight - 1 - i : i;
            int i1 = this.getPixelRGBA(pXFrom + j, pYFrom + i);
            this.setPixelRGBA(pXFrom + pXToDelta + k, pYFrom + pYToDelta + l, i1);
         }
      }

   }

   public void flipY() {
      this.checkAllocated();

      try (MemoryStack memorystack = MemoryStack.stackPush()) {
         int i = this.format.components();
         int j = this.getWidth() * i;
         long k = memorystack.nmalloc(j);

         for(int l = 0; l < this.getHeight() / 2; ++l) {
            int i1 = l * this.getWidth() * i;
            int j1 = (this.getHeight() - 1 - l) * this.getWidth() * i;
            MemoryUtil.memCopy(this.pixels + (long)i1, k, (long)j);
            MemoryUtil.memCopy(this.pixels + (long)j1, this.pixels + (long)i1, (long)j);
            MemoryUtil.memCopy(k, this.pixels + (long)j1, (long)j);
         }
      }

   }

   public void resizeSubRectTo(int pX, int pY, int pWidth, int pHeight, NativeImage pImage) {
      this.checkAllocated();
      if (pImage.format() != this.format) {
         throw new UnsupportedOperationException("resizeSubRectTo only works for images of the same format.");
      } else {
         int i = this.format.components();
         STBImageResize.nstbir_resize_uint8(this.pixels + (long)((pX + pY * this.getWidth()) * i), pWidth, pHeight, this.getWidth() * i, pImage.pixels, pImage.getWidth(), pImage.getHeight(), 0, i);
      }
   }

   public void untrack() {
      LWJGLMemoryUntracker.untrack(this.pixels);
   }

   public static NativeImage fromBase64(String pString) throws IOException {
      byte[] abyte = Base64.getDecoder().decode(pString.replaceAll("\n", "").getBytes(Charsets.UTF_8));

      NativeImage nativeimage;
      try (MemoryStack memorystack = MemoryStack.stackPush()) {
         ByteBuffer bytebuffer = memorystack.malloc(abyte.length);
         bytebuffer.put(abyte);
         ((Buffer)bytebuffer).rewind();
         nativeimage = read(bytebuffer);
      }

      return nativeimage;
   }

   public static int getA(int pAbgrColor) {
      return pAbgrColor >> 24 & 255;
   }

   public static int getR(int pAbgrColor) {
      return pAbgrColor >> 0 & 255;
   }

   public static int getG(int pAbgrColor) {
      return pAbgrColor >> 8 & 255;
   }

   public static int getB(int pAbgrColor) {
      return pAbgrColor >> 16 & 255;
   }

   public static int combine(int pAlpha, int pBlue, int pGreen, int pRed) {
      return (pAlpha & 255) << 24 | (pBlue & 255) << 16 | (pGreen & 255) << 8 | (pRed & 255) << 0;
   }

   @OnlyIn(Dist.CLIENT)
   public static enum PixelFormat {
      RGBA(4, 6408, true, true, true, false, true, 0, 8, 16, 255, 24, true),
      RGB(3, 6407, true, true, true, false, false, 0, 8, 16, 255, 255, true),
      LUMINANCE_ALPHA(2, 6410, false, false, false, true, true, 255, 255, 255, 0, 8, true),
      LUMINANCE(1, 6409, false, false, false, true, false, 0, 0, 0, 0, 255, true);

      private final int components;
      private final int glFormat;
      private final boolean hasRed;
      private final boolean hasGreen;
      private final boolean hasBlue;
      private final boolean hasLuminance;
      private final boolean hasAlpha;
      private final int redOffset;
      private final int greenOffset;
      private final int blueOffset;
      private final int luminanceOffset;
      private final int alphaOffset;
      private final boolean supportedByStb;

      private PixelFormat(int pComponents, int pGlFormat, boolean pHasRed, boolean pHasGreen, boolean pHasBlue, boolean pHasLuminance, boolean pHasAlpha, int pRedOffset, int pGreenOffset, int pBlueOffset, int pLuminanceOffset, int pAlphaOffset, boolean pSupportedByStb) {
         this.components = pComponents;
         this.glFormat = pGlFormat;
         this.hasRed = pHasRed;
         this.hasGreen = pHasGreen;
         this.hasBlue = pHasBlue;
         this.hasLuminance = pHasLuminance;
         this.hasAlpha = pHasAlpha;
         this.redOffset = pRedOffset;
         this.greenOffset = pGreenOffset;
         this.blueOffset = pBlueOffset;
         this.luminanceOffset = pLuminanceOffset;
         this.alphaOffset = pAlphaOffset;
         this.supportedByStb = pSupportedByStb;
      }

      public int components() {
         return this.components;
      }

      public void setPackPixelStoreState() {
         RenderSystem.assertThread(RenderSystem::isOnRenderThread);
         GlStateManager._pixelStore(3333, this.components());
      }

      public void setUnpackPixelStoreState() {
         RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
         GlStateManager._pixelStore(3317, this.components());
      }

      public int glFormat() {
         return this.glFormat;
      }

      public boolean hasAlpha() {
         return this.hasAlpha;
      }

      public int alphaOffset() {
         return this.alphaOffset;
      }

      public boolean hasLuminanceOrAlpha() {
         return this.hasLuminance || this.hasAlpha;
      }

      public int luminanceOrAlphaOffset() {
         return this.hasLuminance ? this.luminanceOffset : this.alphaOffset;
      }

      public boolean supportedByStb() {
         return this.supportedByStb;
      }

      private static NativeImage.PixelFormat getStbFormat(int pChannels) {
         switch(pChannels) {
         case 1:
            return LUMINANCE;
         case 2:
            return LUMINANCE_ALPHA;
         case 3:
            return RGB;
         case 4:
         default:
            return RGBA;
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static enum PixelFormatGLCode {
      RGBA(6408),
      RGB(6407),
      LUMINANCE_ALPHA(6410),
      LUMINANCE(6409),
      INTENSITY(32841);

      private final int glFormat;

      private PixelFormatGLCode(int pGlFormat) {
         this.glFormat = pGlFormat;
      }

      int glFormat() {
         return this.glFormat;
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class WriteCallback extends STBIWriteCallback {
      private final WritableByteChannel output;
      @Nullable
      private IOException exception;

      private WriteCallback(WritableByteChannel pOutput) {
         this.output = pOutput;
      }

      public void invoke(long p_invoke_1_, long p_invoke_3_, int p_invoke_5_) {
         ByteBuffer bytebuffer = getData(p_invoke_3_, p_invoke_5_);

         try {
            this.output.write(bytebuffer);
         } catch (IOException ioexception) {
            this.exception = ioexception;
         }

      }

      public void throwIfException() throws IOException {
         if (this.exception != null) {
            throw this.exception;
         }
      }
   }
}
