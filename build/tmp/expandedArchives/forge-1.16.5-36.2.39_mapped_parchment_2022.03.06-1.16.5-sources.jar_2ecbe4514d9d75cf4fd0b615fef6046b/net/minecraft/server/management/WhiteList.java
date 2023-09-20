package net.minecraft.server.management;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;

public class WhiteList extends UserList<GameProfile, WhitelistEntry> {
   public WhiteList(File p_i1132_1_) {
      super(p_i1132_1_);
   }

   protected UserListEntry<GameProfile> createEntry(JsonObject pEntryData) {
      return new WhitelistEntry(pEntryData);
   }

   /**
    * Returns true if the profile is in the whitelist.
    */
   public boolean isWhiteListed(GameProfile pProfile) {
      return this.contains(pProfile);
   }

   public String[] getUserList() {
      String[] astring = new String[this.getEntries().size()];
      int i = 0;

      for(UserListEntry<GameProfile> userlistentry : this.getEntries()) {
         astring[i++] = userlistentry.getUser().getName();
      }

      return astring;
   }

   /**
    * Gets the key value for the given object
    */
   protected String getKeyForUser(GameProfile pObj) {
      return pObj.getId().toString();
   }
}