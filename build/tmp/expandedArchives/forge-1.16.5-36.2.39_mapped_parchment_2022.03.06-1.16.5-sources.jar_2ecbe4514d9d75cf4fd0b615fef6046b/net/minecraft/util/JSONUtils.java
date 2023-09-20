package net.minecraft.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import javax.annotation.Nullable;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class JSONUtils {
   private static final Gson GSON = (new GsonBuilder()).create();

   /**
    * Does the given JsonObject contain a string field with the given name?
    */
   public static boolean isStringValue(JsonObject pJson, String pMemberName) {
      return !isValidPrimitive(pJson, pMemberName) ? false : pJson.getAsJsonPrimitive(pMemberName).isString();
   }

   /**
    * Is the given JsonElement a string?
    */
   @OnlyIn(Dist.CLIENT)
   public static boolean isStringValue(JsonElement pJson) {
      return !pJson.isJsonPrimitive() ? false : pJson.getAsJsonPrimitive().isString();
   }

   public static boolean isNumberValue(JsonElement pJson) {
      return !pJson.isJsonPrimitive() ? false : pJson.getAsJsonPrimitive().isNumber();
   }

   @OnlyIn(Dist.CLIENT)
   public static boolean isBooleanValue(JsonObject pJson, String pMemberName) {
      return !isValidPrimitive(pJson, pMemberName) ? false : pJson.getAsJsonPrimitive(pMemberName).isBoolean();
   }

   /**
    * Does the given JsonObject contain an array field with the given name?
    */
   public static boolean isArrayNode(JsonObject pJson, String pMemberName) {
      return !isValidNode(pJson, pMemberName) ? false : pJson.get(pMemberName).isJsonArray();
   }

   /**
    * Does the given JsonObject contain a field with the given name whose type is primitive (String, Java primitive, or
    * Java primitive wrapper)?
    */
   public static boolean isValidPrimitive(JsonObject pJson, String pMemberName) {
      return !isValidNode(pJson, pMemberName) ? false : pJson.get(pMemberName).isJsonPrimitive();
   }

   /**
    * Does the given JsonObject contain a field with the given name?
    */
   public static boolean isValidNode(JsonObject pJson, String pMemberName) {
      if (pJson == null) {
         return false;
      } else {
         return pJson.get(pMemberName) != null;
      }
   }

   /**
    * Gets the string value of the given JsonElement.  Expects the second parameter to be the name of the element's
    * field if an error message needs to be thrown.
    */
   public static String convertToString(JsonElement pJson, String pMemberName) {
      if (pJson.isJsonPrimitive()) {
         return pJson.getAsString();
      } else {
         throw new JsonSyntaxException("Expected " + pMemberName + " to be a string, was " + getType(pJson));
      }
   }

   /**
    * Gets the string value of the field on the JsonObject with the given name.
    */
   public static String getAsString(JsonObject pJson, String pMemberName) {
      if (pJson.has(pMemberName)) {
         return convertToString(pJson.get(pMemberName), pMemberName);
      } else {
         throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a string");
      }
   }

   /**
    * Gets the string value of the field on the JsonObject with the given name, or the given default value if the field
    * is missing.
    */
   public static String getAsString(JsonObject pJson, String pMemberName, String pFallback) {
      return pJson.has(pMemberName) ? convertToString(pJson.get(pMemberName), pMemberName) : pFallback;
   }

   public static Item convertToItem(JsonElement pJson, String pMemberName) {
      if (pJson.isJsonPrimitive()) {
         String s = pJson.getAsString();
         return Registry.ITEM.getOptional(new ResourceLocation(s)).orElseThrow(() -> {
            return new JsonSyntaxException("Expected " + pMemberName + " to be an item, was unknown string '" + s + "'");
         });
      } else {
         throw new JsonSyntaxException("Expected " + pMemberName + " to be an item, was " + getType(pJson));
      }
   }

   public static Item getAsItem(JsonObject pJson, String pMemberName) {
      if (pJson.has(pMemberName)) {
         return convertToItem(pJson.get(pMemberName), pMemberName);
      } else {
         throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find an item");
      }
   }

   /**
    * Gets the boolean value of the given JsonElement.  Expects the second parameter to be the name of the element's
    * field if an error message needs to be thrown.
    */
   public static boolean convertToBoolean(JsonElement pJson, String pMemberName) {
      if (pJson.isJsonPrimitive()) {
         return pJson.getAsBoolean();
      } else {
         throw new JsonSyntaxException("Expected " + pMemberName + " to be a Boolean, was " + getType(pJson));
      }
   }

   /**
    * Gets the boolean value of the field on the JsonObject with the given name.
    */
   public static boolean getAsBoolean(JsonObject pJson, String pMemberName) {
      if (pJson.has(pMemberName)) {
         return convertToBoolean(pJson.get(pMemberName), pMemberName);
      } else {
         throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a Boolean");
      }
   }

   /**
    * Gets the boolean value of the field on the JsonObject with the given name, or the given default value if the field
    * is missing.
    */
   public static boolean getAsBoolean(JsonObject pJson, String pMemberName, boolean pFallback) {
      return pJson.has(pMemberName) ? convertToBoolean(pJson.get(pMemberName), pMemberName) : pFallback;
   }

   /**
    * Gets the float value of the given JsonElement.  Expects the second parameter to be the name of the element's field
    * if an error message needs to be thrown.
    */
   public static float convertToFloat(JsonElement pJson, String pMemberName) {
      if (pJson.isJsonPrimitive() && pJson.getAsJsonPrimitive().isNumber()) {
         return pJson.getAsFloat();
      } else {
         throw new JsonSyntaxException("Expected " + pMemberName + " to be a Float, was " + getType(pJson));
      }
   }

   /**
    * Gets the float value of the field on the JsonObject with the given name.
    */
   public static float getAsFloat(JsonObject pJson, String pMemberName) {
      if (pJson.has(pMemberName)) {
         return convertToFloat(pJson.get(pMemberName), pMemberName);
      } else {
         throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a Float");
      }
   }

   /**
    * Gets the float value of the field on the JsonObject with the given name, or the given default value if the field
    * is missing.
    */
   public static float getAsFloat(JsonObject pJson, String pMemberName, float pFallback) {
      return pJson.has(pMemberName) ? convertToFloat(pJson.get(pMemberName), pMemberName) : pFallback;
   }

   /**
    * Gets a long from a JSON element and validates that the value is actually a number.
    */
   public static long convertToLong(JsonElement pJson, String pMemberName) {
      if (pJson.isJsonPrimitive() && pJson.getAsJsonPrimitive().isNumber()) {
         return pJson.getAsLong();
      } else {
         throw new JsonSyntaxException("Expected " + pMemberName + " to be a Long, was " + getType(pJson));
      }
   }

   /**
    * Gets a long from a JSON element, throws an error if the member does not exist.
    */
   public static long getAsLong(JsonObject pJson, String pMemberName) {
      if (pJson.has(pMemberName)) {
         return convertToLong(pJson.get(pMemberName), pMemberName);
      } else {
         throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a Long");
      }
   }

   public static long getAsLong(JsonObject pJson, String pMemberName, long pFallback) {
      return pJson.has(pMemberName) ? convertToLong(pJson.get(pMemberName), pMemberName) : pFallback;
   }

   /**
    * Gets the integer value of the given JsonElement.  Expects the second parameter to be the name of the element's
    * field if an error message needs to be thrown.
    */
   public static int convertToInt(JsonElement pJson, String pMemberName) {
      if (pJson.isJsonPrimitive() && pJson.getAsJsonPrimitive().isNumber()) {
         return pJson.getAsInt();
      } else {
         throw new JsonSyntaxException("Expected " + pMemberName + " to be a Int, was " + getType(pJson));
      }
   }

   /**
    * Gets the integer value of the field on the JsonObject with the given name.
    */
   public static int getAsInt(JsonObject pJson, String pMemberName) {
      if (pJson.has(pMemberName)) {
         return convertToInt(pJson.get(pMemberName), pMemberName);
      } else {
         throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a Int");
      }
   }

   /**
    * Gets the integer value of the field on the JsonObject with the given name, or the given default value if the field
    * is missing.
    */
   public static int getAsInt(JsonObject pJson, String pMemberName, int pFallback) {
      return pJson.has(pMemberName) ? convertToInt(pJson.get(pMemberName), pMemberName) : pFallback;
   }

   public static byte convertToByte(JsonElement pJson, String pMemberName) {
      if (pJson.isJsonPrimitive() && pJson.getAsJsonPrimitive().isNumber()) {
         return pJson.getAsByte();
      } else {
         throw new JsonSyntaxException("Expected " + pMemberName + " to be a Byte, was " + getType(pJson));
      }
   }

   public static byte getAsByte(JsonObject pJson, String pMemberName, byte pFallback) {
      return pJson.has(pMemberName) ? convertToByte(pJson.get(pMemberName), pMemberName) : pFallback;
   }

   /**
    * Gets the given JsonElement as a JsonObject.  Expects the second parameter to be the name of the element's field if
    * an error message needs to be thrown.
    */
   public static JsonObject convertToJsonObject(JsonElement pJson, String pMemberName) {
      if (pJson.isJsonObject()) {
         return pJson.getAsJsonObject();
      } else {
         throw new JsonSyntaxException("Expected " + pMemberName + " to be a JsonObject, was " + getType(pJson));
      }
   }

   public static JsonObject getAsJsonObject(JsonObject pJson, String pMemberName) {
      if (pJson.has(pMemberName)) {
         return convertToJsonObject(pJson.get(pMemberName), pMemberName);
      } else {
         throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a JsonObject");
      }
   }

   /**
    * Gets the JsonObject field on the JsonObject with the given name, or the given default value if the field is
    * missing.
    */
   public static JsonObject getAsJsonObject(JsonObject pJson, String pMemberName, JsonObject pFallback) {
      return pJson.has(pMemberName) ? convertToJsonObject(pJson.get(pMemberName), pMemberName) : pFallback;
   }

   /**
    * Gets the given JsonElement as a JsonArray.  Expects the second parameter to be the name of the element's field if
    * an error message needs to be thrown.
    */
   public static JsonArray convertToJsonArray(JsonElement pJson, String pMemberName) {
      if (pJson.isJsonArray()) {
         return pJson.getAsJsonArray();
      } else {
         throw new JsonSyntaxException("Expected " + pMemberName + " to be a JsonArray, was " + getType(pJson));
      }
   }

   /**
    * Gets the JsonArray field on the JsonObject with the given name.
    */
   public static JsonArray getAsJsonArray(JsonObject pJson, String pMemberName) {
      if (pJson.has(pMemberName)) {
         return convertToJsonArray(pJson.get(pMemberName), pMemberName);
      } else {
         throw new JsonSyntaxException("Missing " + pMemberName + ", expected to find a JsonArray");
      }
   }

   /**
    * Gets the JsonArray field on the JsonObject with the given name, or the given default value if the field is
    * missing.
    */
   @Nullable
   public static JsonArray getAsJsonArray(JsonObject pJson, String pMemberName, @Nullable JsonArray pFallback) {
      return pJson.has(pMemberName) ? convertToJsonArray(pJson.get(pMemberName), pMemberName) : pFallback;
   }

   public static <T> T convertToObject(@Nullable JsonElement pJson, String pMemberName, JsonDeserializationContext pContext, Class<? extends T> pAdapter) {
      if (pJson != null) {
         return pContext.deserialize(pJson, pAdapter);
      } else {
         throw new JsonSyntaxException("Missing " + pMemberName);
      }
   }

   public static <T> T getAsObject(JsonObject pJson, String pMemberName, JsonDeserializationContext pContext, Class<? extends T> pAdapter) {
      if (pJson.has(pMemberName)) {
         return convertToObject(pJson.get(pMemberName), pMemberName, pContext, pAdapter);
      } else {
         throw new JsonSyntaxException("Missing " + pMemberName);
      }
   }

   public static <T> T getAsObject(JsonObject pJson, String pMemberName, T pFallback, JsonDeserializationContext pContext, Class<? extends T> pAdapter) {
      return (T)(pJson.has(pMemberName) ? convertToObject(pJson.get(pMemberName), pMemberName, pContext, pAdapter) : pFallback);
   }

   /**
    * Gets a human-readable description of the given JsonElement's type.  For example: "a number (4)"
    */
   public static String getType(JsonElement pJson) {
      String s = org.apache.commons.lang3.StringUtils.abbreviateMiddle(String.valueOf((Object)pJson), "...", 10);
      if (pJson == null) {
         return "null (missing)";
      } else if (pJson.isJsonNull()) {
         return "null (json)";
      } else if (pJson.isJsonArray()) {
         return "an array (" + s + ")";
      } else if (pJson.isJsonObject()) {
         return "an object (" + s + ")";
      } else {
         if (pJson.isJsonPrimitive()) {
            JsonPrimitive jsonprimitive = pJson.getAsJsonPrimitive();
            if (jsonprimitive.isNumber()) {
               return "a number (" + s + ")";
            }

            if (jsonprimitive.isBoolean()) {
               return "a boolean (" + s + ")";
            }
         }

         return s;
      }
   }

   @Nullable
   public static <T> T fromJson(Gson pGson, Reader pReader, Class<T> pAdapter, boolean pLenient) {
      try {
         JsonReader jsonreader = new JsonReader(pReader);
         jsonreader.setLenient(pLenient);
         return pGson.getAdapter(pAdapter).read(jsonreader);
      } catch (IOException ioexception) {
         throw new JsonParseException(ioexception);
      }
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public static <T> T fromJson(Gson pGson, Reader pReader, TypeToken<T> pType, boolean pLenient) {
      try {
         JsonReader jsonreader = new JsonReader(pReader);
         jsonreader.setLenient(pLenient);
         return pGson.getAdapter(pType).read(jsonreader);
      } catch (IOException ioexception) {
         throw new JsonParseException(ioexception);
      }
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public static <T> T fromJson(Gson pGson, String pString, TypeToken<T> pType, boolean pLenient) {
      return fromJson(pGson, new StringReader(pString), pType, pLenient);
   }

   @Nullable
   public static <T> T fromJson(Gson pGson, String pJson, Class<T> pAdapter, boolean pLenient) {
      return fromJson(pGson, new StringReader(pJson), pAdapter, pLenient);
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public static <T> T fromJson(Gson pGson, Reader pReader, TypeToken<T> pType) {
      return fromJson(pGson, pReader, pType, false);
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public static <T> T fromJson(Gson pGson, String pString, TypeToken<T> pType) {
      return fromJson(pGson, pString, pType, false);
   }

   @Nullable
   public static <T> T fromJson(Gson pGson, Reader pReader, Class<T> pJsonClass) {
      return fromJson(pGson, pReader, pJsonClass, false);
   }

   @Nullable
   public static <T> T fromJson(Gson pGson, String pJson, Class<T> pAdapter) {
      return fromJson(pGson, pJson, pAdapter, false);
   }

   public static JsonObject parse(String pJson, boolean pLenient) {
      return parse(new StringReader(pJson), pLenient);
   }

   public static JsonObject parse(Reader pReader, boolean pLenient) {
      return fromJson(GSON, pReader, JsonObject.class, pLenient);
   }

   public static JsonObject parse(String pJson) {
      return parse(pJson, false);
   }

   public static JsonObject parse(Reader pReader) {
      return parse(pReader, false);
   }
}