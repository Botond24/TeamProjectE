package net.minecraft.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.lang.reflect.Type;
import javax.annotation.Nullable;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ResourceLocation implements Comparable<ResourceLocation> {
   public static final Codec<ResourceLocation> CODEC = Codec.STRING.comapFlatMap(ResourceLocation::read, ResourceLocation::toString).stable();
   private static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType(new TranslationTextComponent("argument.id.invalid"));
   protected final String namespace;
   protected final String path;

   protected ResourceLocation(String[] p_i47923_1_) {
      this.namespace = org.apache.commons.lang3.StringUtils.isEmpty(p_i47923_1_[0]) ? "minecraft" : p_i47923_1_[0];
      this.path = p_i47923_1_[1];
      if (!isValidNamespace(this.namespace)) {
         throw new ResourceLocationException("Non [a-z0-9_.-] character in namespace of location: " + this.namespace + ':' + this.path);
      } else if (!isValidPath(this.path)) {
         throw new ResourceLocationException("Non [a-z0-9/._-] character in path of location: " + this.namespace + ':' + this.path);
      }
   }

   public ResourceLocation(String p_i1293_1_) {
      this(decompose(p_i1293_1_, ':'));
   }

   public ResourceLocation(String p_i1292_1_, String p_i1292_2_) {
      this(new String[]{p_i1292_1_, p_i1292_2_});
   }

   /**
    * Constructs a ResourceLocation by splitting a String representation of a valid location on a specified character.
    */
   public static ResourceLocation of(String pResourceName, char pSplitOn) {
      return new ResourceLocation(decompose(pResourceName, pSplitOn));
   }

   @Nullable
   public static ResourceLocation tryParse(String pString) {
      try {
         return new ResourceLocation(pString);
      } catch (ResourceLocationException resourcelocationexception) {
         return null;
      }
   }

   protected static String[] decompose(String pResourceName, char pSplitOn) {
      String[] astring = new String[]{"minecraft", pResourceName};
      int i = pResourceName.indexOf(pSplitOn);
      if (i >= 0) {
         astring[1] = pResourceName.substring(i + 1, pResourceName.length());
         if (i >= 1) {
            astring[0] = pResourceName.substring(0, i);
         }
      }

      return astring;
   }

   private static DataResult<ResourceLocation> read(String p_240911_0_) {
      try {
         return DataResult.success(new ResourceLocation(p_240911_0_));
      } catch (ResourceLocationException resourcelocationexception) {
         return DataResult.error("Not a valid resource location: " + p_240911_0_ + " " + resourcelocationexception.getMessage());
      }
   }

   public String getPath() {
      return this.path;
   }

   public String getNamespace() {
      return this.namespace;
   }

   public String toString() {
      return this.namespace + ':' + this.path;
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (!(p_equals_1_ instanceof ResourceLocation)) {
         return false;
      } else {
         ResourceLocation resourcelocation = (ResourceLocation)p_equals_1_;
         return this.namespace.equals(resourcelocation.namespace) && this.path.equals(resourcelocation.path);
      }
   }

   public int hashCode() {
      return 31 * this.namespace.hashCode() + this.path.hashCode();
   }

   public int compareTo(ResourceLocation p_compareTo_1_) {
      int i = this.path.compareTo(p_compareTo_1_.path);
      if (i == 0) {
         i = this.namespace.compareTo(p_compareTo_1_.namespace);
      }

      return i;
   }

   // Normal compare sorts by path first, this compares namespace first.
   public int compareNamespaced(ResourceLocation o) {
      int ret = this.namespace.compareTo(o.namespace);
      return ret != 0 ? ret : this.path.compareTo(o.path);
   }

   public static ResourceLocation read(StringReader pReader) throws CommandSyntaxException {
      int i = pReader.getCursor();

      while(pReader.canRead() && isAllowedInResourceLocation(pReader.peek())) {
         pReader.skip();
      }

      String s = pReader.getString().substring(i, pReader.getCursor());

      try {
         return new ResourceLocation(s);
      } catch (ResourceLocationException resourcelocationexception) {
         pReader.setCursor(i);
         throw ERROR_INVALID.createWithContext(pReader);
      }
   }

   public static boolean isAllowedInResourceLocation(char pCharValue) {
      return pCharValue >= '0' && pCharValue <= '9' || pCharValue >= 'a' && pCharValue <= 'z' || pCharValue == '_' || pCharValue == ':' || pCharValue == '/' || pCharValue == '.' || pCharValue == '-';
   }

   /**
    * Checks if the path contains invalid characters.
    */
   private static boolean isValidPath(String pPath) {
      for(int i = 0; i < pPath.length(); ++i) {
         if (!validPathChar(pPath.charAt(i))) {
            return false;
         }
      }

      return true;
   }

   /**
    * Returns true if given namespace only consists of allowed characters.
    */
   private static boolean isValidNamespace(String pNamespace) {
      for(int i = 0; i < pNamespace.length(); ++i) {
         if (!validNamespaceChar(pNamespace.charAt(i))) {
            return false;
         }
      }

      return true;
   }

   public static boolean validPathChar(char pCharValue) {
      return pCharValue == '_' || pCharValue == '-' || pCharValue >= 'a' && pCharValue <= 'z' || pCharValue >= '0' && pCharValue <= '9' || pCharValue == '/' || pCharValue == '.';
   }

   private static boolean validNamespaceChar(char pCharValue) {
      return pCharValue == '_' || pCharValue == '-' || pCharValue >= 'a' && pCharValue <= 'z' || pCharValue >= '0' && pCharValue <= '9' || pCharValue == '.';
   }

   /**
    * Checks if the specified resource name (namespace and path) contains invalid characters.
    */
   @OnlyIn(Dist.CLIENT)
   public static boolean isValidResourceLocation(String pResourceName) {
      String[] astring = decompose(pResourceName, ':');
      return isValidNamespace(org.apache.commons.lang3.StringUtils.isEmpty(astring[0]) ? "minecraft" : astring[0]) && isValidPath(astring[1]);
   }

   public static class Serializer implements JsonDeserializer<ResourceLocation>, JsonSerializer<ResourceLocation> {
      public ResourceLocation deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {
         return new ResourceLocation(JSONUtils.convertToString(p_deserialize_1_, "location"));
      }

      public JsonElement serialize(ResourceLocation p_serialize_1_, Type p_serialize_2_, JsonSerializationContext p_serialize_3_) {
         return new JsonPrimitive(p_serialize_1_.toString());
      }
   }
}
