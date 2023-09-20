package net.minecraft.client.renderer.model;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.util.Direction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockPart {
   public final Vector3f from;
   public final Vector3f to;
   public final Map<Direction, BlockPartFace> faces;
   public final BlockPartRotation rotation;
   public final boolean shade;

   public BlockPart(Vector3f p_i47624_1_, Vector3f p_i47624_2_, Map<Direction, BlockPartFace> p_i47624_3_, @Nullable BlockPartRotation p_i47624_4_, boolean p_i47624_5_) {
      this.from = p_i47624_1_;
      this.to = p_i47624_2_;
      this.faces = p_i47624_3_;
      this.rotation = p_i47624_4_;
      this.shade = p_i47624_5_;
      this.fillUvs();
   }

   private void fillUvs() {
      for(Entry<Direction, BlockPartFace> entry : this.faces.entrySet()) {
         float[] afloat = this.uvsByFace(entry.getKey());
         (entry.getValue()).uv.setMissingUv(afloat);
      }

   }

   public float[] uvsByFace(Direction pFacing) {
      switch(pFacing) {
      case DOWN:
         return new float[]{this.from.x(), 16.0F - this.to.z(), this.to.x(), 16.0F - this.from.z()};
      case UP:
         return new float[]{this.from.x(), this.from.z(), this.to.x(), this.to.z()};
      case NORTH:
      default:
         return new float[]{16.0F - this.to.x(), 16.0F - this.to.y(), 16.0F - this.from.x(), 16.0F - this.from.y()};
      case SOUTH:
         return new float[]{this.from.x(), 16.0F - this.to.y(), this.to.x(), 16.0F - this.from.y()};
      case WEST:
         return new float[]{this.from.z(), 16.0F - this.to.y(), this.to.z(), 16.0F - this.from.y()};
      case EAST:
         return new float[]{16.0F - this.to.z(), 16.0F - this.to.y(), 16.0F - this.from.z(), 16.0F - this.from.y()};
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class Deserializer implements JsonDeserializer<BlockPart> {
      public BlockPart deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {
         JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
         Vector3f vector3f = this.getFrom(jsonobject);
         Vector3f vector3f1 = this.getTo(jsonobject);
         BlockPartRotation blockpartrotation = this.getRotation(jsonobject);
         Map<Direction, BlockPartFace> map = this.getFaces(p_deserialize_3_, jsonobject);
         if (jsonobject.has("shade") && !JSONUtils.isBooleanValue(jsonobject, "shade")) {
            throw new JsonParseException("Expected shade to be a Boolean");
         } else {
            boolean flag = JSONUtils.getAsBoolean(jsonobject, "shade", true);
            return new BlockPart(vector3f, vector3f1, map, blockpartrotation, flag);
         }
      }

      @Nullable
      private BlockPartRotation getRotation(JsonObject pObject) {
         BlockPartRotation blockpartrotation = null;
         if (pObject.has("rotation")) {
            JsonObject jsonobject = JSONUtils.getAsJsonObject(pObject, "rotation");
            Vector3f vector3f = this.getVector3f(jsonobject, "origin");
            vector3f.mul(0.0625F);
            Direction.Axis direction$axis = this.getAxis(jsonobject);
            float f = this.getAngle(jsonobject);
            boolean flag = JSONUtils.getAsBoolean(jsonobject, "rescale", false);
            blockpartrotation = new BlockPartRotation(vector3f, direction$axis, f, flag);
         }

         return blockpartrotation;
      }

      private float getAngle(JsonObject pObject) {
         float f = JSONUtils.getAsFloat(pObject, "angle");
         if (f != 0.0F && MathHelper.abs(f) != 22.5F && MathHelper.abs(f) != 45.0F) {
            throw new JsonParseException("Invalid rotation " + f + " found, only -45/-22.5/0/22.5/45 allowed");
         } else {
            return f;
         }
      }

      private Direction.Axis getAxis(JsonObject pObject) {
         String s = JSONUtils.getAsString(pObject, "axis");
         Direction.Axis direction$axis = Direction.Axis.byName(s.toLowerCase(Locale.ROOT));
         if (direction$axis == null) {
            throw new JsonParseException("Invalid rotation axis: " + s);
         } else {
            return direction$axis;
         }
      }

      private Map<Direction, BlockPartFace> getFaces(JsonDeserializationContext pDeserializationContext, JsonObject pObject) {
         Map<Direction, BlockPartFace> map = this.filterNullFromFaces(pDeserializationContext, pObject);
         if (map.isEmpty()) {
            throw new JsonParseException("Expected between 1 and 6 unique faces, got 0");
         } else {
            return map;
         }
      }

      private Map<Direction, BlockPartFace> filterNullFromFaces(JsonDeserializationContext pDeserializationContext, JsonObject pObject) {
         Map<Direction, BlockPartFace> map = Maps.newEnumMap(Direction.class);
         JsonObject jsonobject = JSONUtils.getAsJsonObject(pObject, "faces");

         for(Entry<String, JsonElement> entry : jsonobject.entrySet()) {
            Direction direction = this.getFacing(entry.getKey());
            map.put(direction, pDeserializationContext.deserialize(entry.getValue(), BlockPartFace.class));
         }

         return map;
      }

      private Direction getFacing(String pName) {
         Direction direction = Direction.byName(pName);
         if (direction == null) {
            throw new JsonParseException("Unknown facing: " + pName);
         } else {
            return direction;
         }
      }

      private Vector3f getTo(JsonObject pJson) {
         Vector3f vector3f = this.getVector3f(pJson, "to");
         if (!(vector3f.x() < -16.0F) && !(vector3f.y() < -16.0F) && !(vector3f.z() < -16.0F) && !(vector3f.x() > 32.0F) && !(vector3f.y() > 32.0F) && !(vector3f.z() > 32.0F)) {
            return vector3f;
         } else {
            throw new JsonParseException("'to' specifier exceeds the allowed boundaries: " + vector3f);
         }
      }

      private Vector3f getFrom(JsonObject pJson) {
         Vector3f vector3f = this.getVector3f(pJson, "from");
         if (!(vector3f.x() < -16.0F) && !(vector3f.y() < -16.0F) && !(vector3f.z() < -16.0F) && !(vector3f.x() > 32.0F) && !(vector3f.y() > 32.0F) && !(vector3f.z() > 32.0F)) {
            return vector3f;
         } else {
            throw new JsonParseException("'from' specifier exceeds the allowed boundaries: " + vector3f);
         }
      }

      private Vector3f getVector3f(JsonObject pJson, String pName) {
         JsonArray jsonarray = JSONUtils.getAsJsonArray(pJson, pName);
         if (jsonarray.size() != 3) {
            throw new JsonParseException("Expected 3 " + pName + " values, found: " + jsonarray.size());
         } else {
            float[] afloat = new float[3];

            for(int i = 0; i < afloat.length; ++i) {
               afloat[i] = JSONUtils.convertToFloat(jsonarray.get(i), pName + "[" + i + "]");
            }

            return new Vector3f(afloat[0], afloat[1], afloat[2]);
         }
      }
   }
}