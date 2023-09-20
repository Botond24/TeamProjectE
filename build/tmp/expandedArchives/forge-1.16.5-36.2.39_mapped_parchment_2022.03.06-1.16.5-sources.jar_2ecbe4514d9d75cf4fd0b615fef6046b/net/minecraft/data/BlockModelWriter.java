package net.minecraft.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.function.Supplier;
import net.minecraft.util.ResourceLocation;

public class BlockModelWriter implements Supplier<JsonElement> {
   private final ResourceLocation parent;

   public BlockModelWriter(ResourceLocation pParent) {
      this.parent = pParent;
   }

   public JsonElement get() {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("parent", this.parent.toString());
      return jsonobject;
   }
}