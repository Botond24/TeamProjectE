package net.minecraft.client.gui.fonts.providers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryUtil;

@OnlyIn(Dist.CLIENT)
public class TrueTypeGlyphProviderFactory implements IGlyphProviderFactory {
   private static final Logger LOGGER = LogManager.getLogger();
   private final ResourceLocation location;
   private final float size;
   private final float oversample;
   private final float shiftX;
   private final float shiftY;
   private final String skip;

   public TrueTypeGlyphProviderFactory(ResourceLocation pLocation, float pSize, float pOversample, float pShiftX, float pShiftY, String pSkip) {
      this.location = pLocation;
      this.size = pSize;
      this.oversample = pOversample;
      this.shiftX = pShiftX;
      this.shiftY = pShiftY;
      this.skip = pSkip;
   }

   public static IGlyphProviderFactory fromJson(JsonObject pJson) {
      float f = 0.0F;
      float f1 = 0.0F;
      if (pJson.has("shift")) {
         JsonArray jsonarray = pJson.getAsJsonArray("shift");
         if (jsonarray.size() != 2) {
            throw new JsonParseException("Expected 2 elements in 'shift', found " + jsonarray.size());
         }

         f = JSONUtils.convertToFloat(jsonarray.get(0), "shift[0]");
         f1 = JSONUtils.convertToFloat(jsonarray.get(1), "shift[1]");
      }

      StringBuilder stringbuilder = new StringBuilder();
      if (pJson.has("skip")) {
         JsonElement jsonelement = pJson.get("skip");
         if (jsonelement.isJsonArray()) {
            JsonArray jsonarray1 = JSONUtils.convertToJsonArray(jsonelement, "skip");

            for(int i = 0; i < jsonarray1.size(); ++i) {
               stringbuilder.append(JSONUtils.convertToString(jsonarray1.get(i), "skip[" + i + "]"));
            }
         } else {
            stringbuilder.append(JSONUtils.convertToString(jsonelement, "skip"));
         }
      }

      return new TrueTypeGlyphProviderFactory(new ResourceLocation(JSONUtils.getAsString(pJson, "file")), JSONUtils.getAsFloat(pJson, "size", 11.0F), JSONUtils.getAsFloat(pJson, "oversample", 1.0F), f, f1, stringbuilder.toString());
   }

   @Nullable
   public IGlyphProvider create(IResourceManager pResourceManager) {
      STBTTFontinfo stbttfontinfo = null;
      ByteBuffer bytebuffer = null;

      try (IResource iresource = pResourceManager.getResource(new ResourceLocation(this.location.getNamespace(), "font/" + this.location.getPath()))) {
         LOGGER.debug("Loading font {}", (Object)this.location);
         stbttfontinfo = STBTTFontinfo.malloc();
         bytebuffer = TextureUtil.readResource(iresource.getInputStream());
         ((Buffer)bytebuffer).flip();
         LOGGER.debug("Reading font {}", (Object)this.location);
         if (!STBTruetype.stbtt_InitFont(stbttfontinfo, bytebuffer)) {
            throw new IOException("Invalid ttf");
         } else {
            return new TrueTypeGlyphProvider(bytebuffer, stbttfontinfo, this.size, this.oversample, this.shiftX, this.shiftY, this.skip);
         }
      } catch (Exception exception) {
         LOGGER.error("Couldn't load truetype font {}", this.location, exception);
         if (stbttfontinfo != null) {
            stbttfontinfo.free();
         }

         MemoryUtil.memFree(bytebuffer);
         return null;
      }
   }
}