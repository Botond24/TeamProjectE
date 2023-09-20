package net.minecraft.client.resources.data;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.texture.TextureAtlasSpriteStitcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VillagerMetadataSection {
   public static final TextureAtlasSpriteStitcher SERIALIZER = new TextureAtlasSpriteStitcher();
   private final VillagerMetadataSection.HatType hat;

   public VillagerMetadataSection(VillagerMetadataSection.HatType pHat) {
      this.hat = pHat;
   }

   public VillagerMetadataSection.HatType getHat() {
      return this.hat;
   }

   @OnlyIn(Dist.CLIENT)
   public static enum HatType {
      NONE("none"),
      PARTIAL("partial"),
      FULL("full");

      private static final Map<String, VillagerMetadataSection.HatType> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap(VillagerMetadataSection.HatType::getName, (p_217822_0_) -> {
         return p_217822_0_;
      }));
      private final String name;

      private HatType(String pName) {
         this.name = pName;
      }

      public String getName() {
         return this.name;
      }

      public static VillagerMetadataSection.HatType getByName(String pName) {
         return BY_NAME.getOrDefault(pName, NONE);
      }
   }
}