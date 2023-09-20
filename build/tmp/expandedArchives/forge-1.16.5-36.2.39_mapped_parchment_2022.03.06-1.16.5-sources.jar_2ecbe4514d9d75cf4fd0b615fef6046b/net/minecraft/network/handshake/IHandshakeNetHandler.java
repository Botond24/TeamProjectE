package net.minecraft.network.handshake;

import net.minecraft.network.INetHandler;
import net.minecraft.network.handshake.client.CHandshakePacket;

/**
 * PacketListener for the server side of the HANDSHAKING protocol.
 */
public interface IHandshakeNetHandler extends INetHandler {
   /**
    * There are two recognized intentions for initiating a handshake: logging in and acquiring server status. The
    * NetworkManager's protocol will be reconfigured according to the specified intention, although a login-intention
    * must pass a versioncheck or receive a disconnect otherwise
    */
   void handleIntention(CHandshakePacket pPacket);
}