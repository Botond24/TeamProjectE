package net.minecraft.network.login.server;

import java.io.IOException;
import java.security.PublicKey;
import net.minecraft.client.network.login.IClientLoginNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.CryptException;
import net.minecraft.util.CryptManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SEncryptionRequestPacket implements IPacket<IClientLoginNetHandler> {
   private String serverId;
   private byte[] publicKey;
   private byte[] nonce;

   public SEncryptionRequestPacket() {
   }

   public SEncryptionRequestPacket(String pServerId, byte[] pPublicKey, byte[] pNonce) {
      this.serverId = pServerId;
      this.publicKey = pPublicKey;
      this.nonce = pNonce;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.serverId = p_148837_1_.readUtf(20);
      this.publicKey = p_148837_1_.readByteArray();
      this.nonce = p_148837_1_.readByteArray();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeUtf(this.serverId);
      pBuffer.writeByteArray(this.publicKey);
      pBuffer.writeByteArray(this.nonce);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientLoginNetHandler pHandler) {
      pHandler.handleHello(this);
   }

   @OnlyIn(Dist.CLIENT)
   public String getServerId() {
      return this.serverId;
   }

   @OnlyIn(Dist.CLIENT)
   public PublicKey getPublicKey() throws CryptException {
      return CryptManager.byteToPublicKey(this.publicKey);
   }

   @OnlyIn(Dist.CLIENT)
   public byte[] getNonce() {
      return this.nonce;
   }
}