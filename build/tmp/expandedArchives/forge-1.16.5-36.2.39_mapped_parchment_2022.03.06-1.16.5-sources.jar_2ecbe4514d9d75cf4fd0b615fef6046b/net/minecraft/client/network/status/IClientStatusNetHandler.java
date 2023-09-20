package net.minecraft.client.network.status;

import net.minecraft.network.INetHandler;
import net.minecraft.network.status.server.SPongPacket;
import net.minecraft.network.status.server.SServerInfoPacket;

/**
 * PacketListener for the client side of the STATUS protocol.
 */
public interface IClientStatusNetHandler extends INetHandler {
   void handleStatusResponse(SServerInfoPacket pPacket);

   void handlePongResponse(SPongPacket pPacket);
}