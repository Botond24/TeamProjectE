package net.minecraft.server.management;

import com.google.gson.JsonObject;
import java.util.Date;
import javax.annotation.Nullable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class IPBanEntry extends BanEntry<String> {
   public IPBanEntry(String p_i46330_1_) {
      this(p_i46330_1_, (Date)null, (String)null, (Date)null, (String)null);
   }

   public IPBanEntry(String p_i1159_1_, @Nullable Date p_i1159_2_, @Nullable String p_i1159_3_, @Nullable Date p_i1159_4_, @Nullable String p_i1159_5_) {
      super(p_i1159_1_, p_i1159_2_, p_i1159_3_, p_i1159_4_, p_i1159_5_);
   }

   public ITextComponent getDisplayName() {
      return new StringTextComponent(this.getUser());
   }

   public IPBanEntry(JsonObject p_i46331_1_) {
      super(createIpInfo(p_i46331_1_), p_i46331_1_);
   }

   private static String createIpInfo(JsonObject pJson) {
      return pJson.has("ip") ? pJson.get("ip").getAsString() : null;
   }

   protected void serialize(JsonObject pData) {
      if (this.getUser() != null) {
         pData.addProperty("ip", this.getUser());
         super.serialize(pData);
      }
   }
}