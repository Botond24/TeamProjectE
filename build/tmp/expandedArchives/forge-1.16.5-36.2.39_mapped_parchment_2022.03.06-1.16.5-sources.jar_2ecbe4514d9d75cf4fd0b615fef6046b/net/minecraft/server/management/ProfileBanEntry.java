package net.minecraft.server.management;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ProfileBanEntry extends BanEntry<GameProfile> {
   public ProfileBanEntry(GameProfile p_i1134_1_) {
      this(p_i1134_1_, (Date)null, (String)null, (Date)null, (String)null);
   }

   public ProfileBanEntry(GameProfile p_i1135_1_, @Nullable Date p_i1135_2_, @Nullable String p_i1135_3_, @Nullable Date p_i1135_4_, @Nullable String p_i1135_5_) {
      super(p_i1135_1_, p_i1135_2_, p_i1135_3_, p_i1135_4_, p_i1135_5_);
   }

   public ProfileBanEntry(JsonObject p_i1136_1_) {
      super(createGameProfile(p_i1136_1_), p_i1136_1_);
   }

   protected void serialize(JsonObject pData) {
      if (this.getUser() != null) {
         pData.addProperty("uuid", this.getUser().getId() == null ? "" : this.getUser().getId().toString());
         pData.addProperty("name", this.getUser().getName());
         super.serialize(pData);
      }
   }

   public ITextComponent getDisplayName() {
      GameProfile gameprofile = this.getUser();
      return new StringTextComponent(gameprofile.getName() != null ? gameprofile.getName() : Objects.toString(gameprofile.getId(), "(Unknown)"));
   }

   /**
    * Convert a {@linkplain com.google.gson.JsonObject JsonObject} into a {@linkplain com.mojang.authlib.GameProfile}.
    * The json object must have {@code uuid} and {@code name} attributes or {@code null} will be returned.
    */
   private static GameProfile createGameProfile(JsonObject pJson) {
      if (pJson.has("uuid") && pJson.has("name")) {
         String s = pJson.get("uuid").getAsString();

         UUID uuid;
         try {
            uuid = UUID.fromString(s);
         } catch (Throwable throwable) {
            return null;
         }

         return new GameProfile(uuid, pJson.get("name").getAsString());
      } else {
         return null;
      }
   }
}