package net.minecraft.network.login;

import net.minecraft.network.INetHandler;
import net.minecraft.network.login.client.CCustomPayloadLoginPacket;
import net.minecraft.network.login.client.CEncryptionResponsePacket;
import net.minecraft.network.login.client.CLoginStartPacket;

/**
 * PacketListener for the server side of the LOGIN protocol.
 */
public interface IServerLoginNetHandler extends INetHandler {
   void handleHello(CLoginStartPacket pPacket);

   void handleKey(CEncryptionResponsePacket pPacket);

   void handleCustomQueryPacket(CCustomPayloadLoginPacket pPacket);
}