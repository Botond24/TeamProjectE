package net.minecraft.client.resources.data;

import com.google.gson.JsonObject;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextureMetadataSectionSerializer implements IMetadataSectionSerializer<TextureMetadataSection> {
   public TextureMetadataSection fromJson(JsonObject pJson) {
      boolean flag = JSONUtils.getAsBoolean(pJson, "blur", false);
      boolean flag1 = JSONUtils.getAsBoolean(pJson, "clamp", false);
      return new TextureMetadataSection(flag, flag1);
   }

   /**
    * The name of this section type as it appears in JSON.
    */
   public String getMetadataSectionName() {
      return "texture";
   }
}