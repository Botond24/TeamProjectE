package net.minecraft.client.resources.data;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextureMetadataSection {
   public static final TextureMetadataSectionSerializer SERIALIZER = new TextureMetadataSectionSerializer();
   private final boolean blur;
   private final boolean clamp;

   public TextureMetadataSection(boolean pBlur, boolean pClamp) {
      this.blur = pBlur;
      this.clamp = pClamp;
   }

   public boolean isBlur() {
      return this.blur;
   }

   public boolean isClamp() {
      return this.clamp;
   }
}