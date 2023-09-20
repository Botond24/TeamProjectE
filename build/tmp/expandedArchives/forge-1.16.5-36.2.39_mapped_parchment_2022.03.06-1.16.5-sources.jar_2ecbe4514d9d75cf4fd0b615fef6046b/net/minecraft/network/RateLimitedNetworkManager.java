package net.minecraft.network;

import net.minecraft.network.play.server.SDisconnectPacket;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Variant of {@link Connection} that monitors the amount of received packets and disables receiving if the set limit is
 * exceeded.
 */
public class RateLimitedNetworkManager extends NetworkManager {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final ITextComponent EXCEED_REASON = new TranslationTextComponent("disconnect.exceeded_packet_rate");
   private final int rateLimitPacketsPerSecond;

   public RateLimitedNetworkManager(int pRateLimitPacketsPerSecond) {
      super(PacketDirection.SERVERBOUND);
      this.rateLimitPacketsPerSecond = pRateLimitPacketsPerSecond;
   }

   protected void tickSecond() {
      super.tickSecond();
      float f = this.getAverageReceivedPackets();
      if (f > (float)this.rateLimitPacketsPerSecond) {
         LOGGER.warn("Player exceeded rate-limit (sent {} packets per second)", (float)f);
         this.send(new SDisconnectPacket(EXCEED_REASON), (p_244277_1_) -> {
            this.disconnect(EXCEED_REASON);
         });
         this.setReadOnly();
      }

   }
}