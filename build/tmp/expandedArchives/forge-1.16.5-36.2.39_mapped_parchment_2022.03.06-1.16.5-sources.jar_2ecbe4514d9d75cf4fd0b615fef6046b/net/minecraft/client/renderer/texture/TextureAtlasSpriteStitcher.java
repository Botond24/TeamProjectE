package net.minecraft.client.renderer.texture;

import com.google.gson.JsonObject;
import net.minecraft.client.resources.data.VillagerMetadataSection;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TextureAtlasSpriteStitcher implements IMetadataSectionSerializer<VillagerMetadataSection> {
   public VillagerMetadataSection fromJson(JsonObject pJson) {
      return new VillagerMetadataSection(VillagerMetadataSection.HatType.getByName(JSONUtils.getAsString(pJson, "hat", "none")));
   }

   /**
    * The name of this section type as it appears in JSON.
    */
   public String getMetadataSectionName() {
      return "villager";
   }
}