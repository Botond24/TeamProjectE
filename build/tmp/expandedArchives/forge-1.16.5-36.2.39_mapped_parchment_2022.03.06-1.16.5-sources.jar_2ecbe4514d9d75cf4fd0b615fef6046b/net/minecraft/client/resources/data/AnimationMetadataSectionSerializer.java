package net.minecraft.client.resources.data;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.List;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;

@OnlyIn(Dist.CLIENT)
public class AnimationMetadataSectionSerializer implements IMetadataSectionSerializer<AnimationMetadataSection> {
   public AnimationMetadataSection fromJson(JsonObject pJson) {
      List<AnimationFrame> list = Lists.newArrayList();
      int i = JSONUtils.getAsInt(pJson, "frametime", 1);
      if (i != 1) {
         Validate.inclusiveBetween(1L, 2147483647L, (long)i, "Invalid default frame time");
      }

      if (pJson.has("frames")) {
         try {
            JsonArray jsonarray = JSONUtils.getAsJsonArray(pJson, "frames");

            for(int j = 0; j < jsonarray.size(); ++j) {
               JsonElement jsonelement = jsonarray.get(j);
               AnimationFrame animationframe = this.getFrame(j, jsonelement);
               if (animationframe != null) {
                  list.add(animationframe);
               }
            }
         } catch (ClassCastException classcastexception) {
            throw new JsonParseException("Invalid animation->frames: expected array, was " + pJson.get("frames"), classcastexception);
         }
      }

      int k = JSONUtils.getAsInt(pJson, "width", -1);
      int l = JSONUtils.getAsInt(pJson, "height", -1);
      if (k != -1) {
         Validate.inclusiveBetween(1L, 2147483647L, (long)k, "Invalid width");
      }

      if (l != -1) {
         Validate.inclusiveBetween(1L, 2147483647L, (long)l, "Invalid height");
      }

      boolean flag = JSONUtils.getAsBoolean(pJson, "interpolate", false);
      return new AnimationMetadataSection(list, k, l, i, flag);
   }

   private AnimationFrame getFrame(int pFrame, JsonElement pElement) {
      if (pElement.isJsonPrimitive()) {
         return new AnimationFrame(JSONUtils.convertToInt(pElement, "frames[" + pFrame + "]"));
      } else if (pElement.isJsonObject()) {
         JsonObject jsonobject = JSONUtils.convertToJsonObject(pElement, "frames[" + pFrame + "]");
         int i = JSONUtils.getAsInt(jsonobject, "time", -1);
         if (jsonobject.has("time")) {
            Validate.inclusiveBetween(1L, 2147483647L, (long)i, "Invalid frame time");
         }

         int j = JSONUtils.getAsInt(jsonobject, "index");
         Validate.inclusiveBetween(0L, 2147483647L, (long)j, "Invalid frame index");
         return new AnimationFrame(j, i);
      } else {
         return null;
      }
   }

   /**
    * The name of this section type as it appears in JSON.
    */
   public String getMetadataSectionName() {
      return "animation";
   }
}