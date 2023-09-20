package net.minecraft.network.status;

import net.minecraft.network.INetHandler;
import net.minecraft.network.status.client.CPingPacket;
import net.minecraft.network.status.client.CServerQueryPacket;

/**
 * PacketListener for the server side of the STATUS protocol.
 */
public interface IServerStatusNetHandler extends INetHandler {
   void handlePingRequest(CPingPacket pPacket);

   void handleStatusRequest(CServerQueryPacket pPacket);
}