package net.minecraft.client.network.login;

import net.minecraft.network.INetHandler;
import net.minecraft.network.login.server.SCustomPayloadLoginPacket;
import net.minecraft.network.login.server.SDisconnectLoginPacket;
import net.minecraft.network.login.server.SEnableCompressionPacket;
import net.minecraft.network.login.server.SEncryptionRequestPacket;
import net.minecraft.network.login.server.SLoginSuccessPacket;

/**
 * PacketListener for the client side of the LOGIN protocol.
 */
public interface IClientLoginNetHandler extends INetHandler {
   void handleHello(SEncryptionRequestPacket pPacket);

   void handleGameProfile(SLoginSuccessPacket pPacket);

   void handleDisconnect(SDisconnectLoginPacket pPacket);

   void handleCompression(SEnableCompressionPacket pPacket);

   void handleCustomQuery(SCustomPayloadLoginPacket pPacket);
}