package net.minecraft.util;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.network.play.server.SCooldownPacket;

public class ServerCooldownTracker extends CooldownTracker {
   private final ServerPlayerEntity player;

   public ServerCooldownTracker(ServerPlayerEntity pPlayer) {
      this.player = pPlayer;
   }

   protected void onCooldownStarted(Item pItem, int pTicks) {
      super.onCooldownStarted(pItem, pTicks);
      this.player.connection.send(new SCooldownPacket(pItem, pTicks));
   }

   protected void onCooldownEnded(Item pItem) {
      super.onCooldownEnded(pItem);
      this.player.connection.send(new SCooldownPacket(pItem, 0));
   }
}