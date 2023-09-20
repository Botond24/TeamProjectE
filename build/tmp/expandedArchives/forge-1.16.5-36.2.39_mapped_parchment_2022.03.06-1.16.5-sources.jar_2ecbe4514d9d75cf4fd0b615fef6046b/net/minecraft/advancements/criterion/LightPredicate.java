package net.minecraft.advancements.criterion;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class LightPredicate {
   public static final LightPredicate ANY = new LightPredicate(MinMaxBounds.IntBound.ANY);
   private final MinMaxBounds.IntBound composite;

   private LightPredicate(MinMaxBounds.IntBound p_i225753_1_) {
      this.composite = p_i225753_1_;
   }

   public boolean matches(ServerWorld pLevel, BlockPos pPos) {
      if (this == ANY) {
         return true;
      } else if (!pLevel.isLoaded(pPos)) {
         return false;
      } else {
         return this.composite.matches(pLevel.getMaxLocalRawBrightness(pPos));
      }
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         jsonobject.add("light", this.composite.serializeToJson());
         return jsonobject;
      }
   }

   public static LightPredicate fromJson(@Nullable JsonElement pElement) {
      if (pElement != null && !pElement.isJsonNull()) {
         JsonObject jsonobject = JSONUtils.convertToJsonObject(pElement, "light");
         MinMaxBounds.IntBound minmaxbounds$intbound = MinMaxBounds.IntBound.fromJson(jsonobject.get("light"));
         return new LightPredicate(minmaxbounds$intbound);
      } else {
         return ANY;
      }
   }
}