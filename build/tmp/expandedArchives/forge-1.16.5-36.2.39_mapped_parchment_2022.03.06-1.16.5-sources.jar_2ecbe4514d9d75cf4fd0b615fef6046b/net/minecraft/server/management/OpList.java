package net.minecraft.server.management;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;

public class OpList extends UserList<GameProfile, OpEntry> {
   public OpList(File p_i1152_1_) {
      super(p_i1152_1_);
   }

   protected UserListEntry<GameProfile> createEntry(JsonObject pEntryData) {
      return new OpEntry(pEntryData);
   }

   public String[] getUserList() {
      String[] astring = new String[this.getEntries().size()];
      int i = 0;

      for(UserListEntry<GameProfile> userlistentry : this.getEntries()) {
         astring[i++] = userlistentry.getUser().getName();
      }

      return astring;
   }

   public boolean canBypassPlayerLimit(GameProfile pProfile) {
      OpEntry opentry = this.get(pProfile);
      return opentry != null ? opentry.getBypassesPlayerLimit() : false;
   }

   /**
    * Gets the key value for the given object
    */
   protected String getKeyForUser(GameProfile pObj) {
      return pObj.getId().toString();
   }
}