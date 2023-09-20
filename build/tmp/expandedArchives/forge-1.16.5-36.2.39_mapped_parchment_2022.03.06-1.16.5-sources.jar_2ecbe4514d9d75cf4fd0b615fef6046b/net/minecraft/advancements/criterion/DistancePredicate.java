package net.minecraft.advancements.criterion;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.MathHelper;

public class DistancePredicate {
   public static final DistancePredicate ANY = new DistancePredicate(MinMaxBounds.FloatBound.ANY, MinMaxBounds.FloatBound.ANY, MinMaxBounds.FloatBound.ANY, MinMaxBounds.FloatBound.ANY, MinMaxBounds.FloatBound.ANY);
   private final MinMaxBounds.FloatBound x;
   private final MinMaxBounds.FloatBound y;
   private final MinMaxBounds.FloatBound z;
   private final MinMaxBounds.FloatBound horizontal;
   private final MinMaxBounds.FloatBound absolute;

   public DistancePredicate(MinMaxBounds.FloatBound pX, MinMaxBounds.FloatBound pY, MinMaxBounds.FloatBound pZ, MinMaxBounds.FloatBound pHorizontal, MinMaxBounds.FloatBound pAbsolute) {
      this.x = pX;
      this.y = pY;
      this.z = pZ;
      this.horizontal = pHorizontal;
      this.absolute = pAbsolute;
   }

   public static DistancePredicate horizontal(MinMaxBounds.FloatBound pHorizontal) {
      return new DistancePredicate(MinMaxBounds.FloatBound.ANY, MinMaxBounds.FloatBound.ANY, MinMaxBounds.FloatBound.ANY, pHorizontal, MinMaxBounds.FloatBound.ANY);
   }

   public static DistancePredicate vertical(MinMaxBounds.FloatBound pVertical) {
      return new DistancePredicate(MinMaxBounds.FloatBound.ANY, pVertical, MinMaxBounds.FloatBound.ANY, MinMaxBounds.FloatBound.ANY, MinMaxBounds.FloatBound.ANY);
   }

   public boolean matches(double pX1, double pY1, double pZ1, double pX2, double pY2, double pZ2) {
      float f = (float)(pX1 - pX2);
      float f1 = (float)(pY1 - pY2);
      float f2 = (float)(pZ1 - pZ2);
      if (this.x.matches(MathHelper.abs(f)) && this.y.matches(MathHelper.abs(f1)) && this.z.matches(MathHelper.abs(f2))) {
         if (!this.horizontal.matchesSqr((double)(f * f + f2 * f2))) {
            return false;
         } else {
            return this.absolute.matchesSqr((double)(f * f + f1 * f1 + f2 * f2));
         }
      } else {
         return false;
      }
   }

   public static DistancePredicate fromJson(@Nullable JsonElement pElement) {
      if (pElement != null && !pElement.isJsonNull()) {
         JsonObject jsonobject = JSONUtils.convertToJsonObject(pElement, "distance");
         MinMaxBounds.FloatBound minmaxbounds$floatbound = MinMaxBounds.FloatBound.fromJson(jsonobject.get("x"));
         MinMaxBounds.FloatBound minmaxbounds$floatbound1 = MinMaxBounds.FloatBound.fromJson(jsonobject.get("y"));
         MinMaxBounds.FloatBound minmaxbounds$floatbound2 = MinMaxBounds.FloatBound.fromJson(jsonobject.get("z"));
         MinMaxBounds.FloatBound minmaxbounds$floatbound3 = MinMaxBounds.FloatBound.fromJson(jsonobject.get("horizontal"));
         MinMaxBounds.FloatBound minmaxbounds$floatbound4 = MinMaxBounds.FloatBound.fromJson(jsonobject.get("absolute"));
         return new DistancePredicate(minmaxbounds$floatbound, minmaxbounds$floatbound1, minmaxbounds$floatbound2, minmaxbounds$floatbound3, minmaxbounds$floatbound4);
      } else {
         return ANY;
      }
   }

   public JsonElement serializeToJson() {
      if (this == ANY) {
         return JsonNull.INSTANCE;
      } else {
         JsonObject jsonobject = new JsonObject();
         jsonobject.add("x", this.x.serializeToJson());
         jsonobject.add("y", this.y.serializeToJson());
         jsonobject.add("z", this.z.serializeToJson());
         jsonobject.add("horizontal", this.horizontal.serializeToJson());
         jsonobject.add("absolute", this.absolute.serializeToJson());
         return jsonobject;
      }
   }
}