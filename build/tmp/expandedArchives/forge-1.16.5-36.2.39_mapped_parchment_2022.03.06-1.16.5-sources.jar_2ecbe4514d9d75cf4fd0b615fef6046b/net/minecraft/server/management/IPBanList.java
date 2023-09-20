package net.minecraft.server.management;

import com.google.gson.JsonObject;
import java.io.File;
import java.net.SocketAddress;

public class IPBanList extends UserList<String, IPBanEntry> {
   public IPBanList(File p_i1490_1_) {
      super(p_i1490_1_);
   }

   protected UserListEntry<String> createEntry(JsonObject pEntryData) {
      return new IPBanEntry(pEntryData);
   }

   public boolean isBanned(SocketAddress pAddress) {
      String s = this.getIpFromAddress(pAddress);
      return this.contains(s);
   }

   public boolean isBanned(String p_199044_1_) {
      return this.contains(p_199044_1_);
   }

   public IPBanEntry get(SocketAddress pAddress) {
      String s = this.getIpFromAddress(pAddress);
      return this.get(s);
   }

   private String getIpFromAddress(SocketAddress pAddress) {
      String s = pAddress.toString();
      if (s.contains("/")) {
         s = s.substring(s.indexOf(47) + 1);
      }

      if (s.contains(":")) {
         s = s.substring(0, s.indexOf(58));
      }

      return s;
   }
}