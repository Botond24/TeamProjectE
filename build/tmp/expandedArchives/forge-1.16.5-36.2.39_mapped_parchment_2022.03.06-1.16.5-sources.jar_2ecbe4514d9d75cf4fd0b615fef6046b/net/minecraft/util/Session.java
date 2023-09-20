package net.minecraft.util;

import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Session {
   private final String name;
   private final String uuid;
   private final String accessToken;
   private final Session.Type type;
   /** Forge: Cache of the local session's GameProfile properties. */
   private com.mojang.authlib.properties.PropertyMap properties;

   public Session(String pName, String pUuid, String pAccessToken, String pTypeName) {
      if (pName == null || pName.isEmpty()) {
         pName = "MissingName";
         pUuid = pAccessToken = "NotValid";
         org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(getClass().getName());
         logger.log(org.apache.logging.log4j.Level.WARN, "=========================================================");
         logger.log(org.apache.logging.log4j.Level.WARN, "WARNING!! the username was not set for this session, typically");
         logger.log(org.apache.logging.log4j.Level.WARN, "this means you installed Forge incorrectly. We have set your");
         logger.log(org.apache.logging.log4j.Level.WARN, "name to \"MissingName\" and your session to nothing. Please");
         logger.log(org.apache.logging.log4j.Level.WARN, "check your installation and post a console log from the launcher");
         logger.log(org.apache.logging.log4j.Level.WARN, "when asking for help!");
         logger.log(org.apache.logging.log4j.Level.WARN, "=========================================================");
      }
      this.name = pName;
      this.uuid = pUuid;
      this.accessToken = pAccessToken;
      this.type = Session.Type.byName(pTypeName);
   }

   public String getSessionId() {
      return "token:" + this.accessToken + ":" + this.uuid;
   }

   public String getUuid() {
      return this.uuid;
   }

   public String getName() {
      return this.name;
   }

   public String getAccessToken() {
      return this.accessToken;
   }

   public GameProfile getGameProfile() {
      try {
         UUID uuid = UUIDTypeAdapter.fromString(this.getUuid());
         GameProfile ret = new GameProfile(uuid, this.getName());    //Forge: Adds cached GameProfile properties to returned GameProfile.
         if (properties != null) ret.getProperties().putAll(properties); // Helps to cut down on calls to the session service,
         return ret;                                                     // which helps to fix MC-52974.
      } catch (IllegalArgumentException illegalargumentexception) {
         return new GameProfile((UUID)null, this.getName());
      }
   }

   //For internal use only. Modders should never need to use this.
   public void setProperties(com.mojang.authlib.properties.PropertyMap properties) {
       if (this.properties == null)
           this.properties = properties;
   }

   public boolean hasCachedProperties() {
       return properties != null;
   }

   @OnlyIn(Dist.CLIENT)
   public static enum Type {
      LEGACY("legacy"),
      MOJANG("mojang");

      private static final Map<String, Session.Type> BY_NAME = Arrays.stream(values()).collect(Collectors.toMap((p_199876_0_) -> {
         return p_199876_0_.name;
      }, Function.identity()));
      private final String name;

      private Type(String pName) {
         this.name = pName;
      }

      @Nullable
      public static Session.Type byName(String pTypeName) {
         return BY_NAME.get(pTypeName.toLowerCase(Locale.ROOT));
      }
   }
}
