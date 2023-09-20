package net.minecraft.network.handshake.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.ProtocolType;
import net.minecraft.network.handshake.IHandshakeNetHandler;
import net.minecraft.util.SharedConstants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CHandshakePacket implements IPacket<IHandshakeNetHandler> {
   private int protocolVersion;
   private String hostName;
   private int port;
   private ProtocolType intention;
   private String fmlVersion = net.minecraftforge.fml.network.FMLNetworkConstants.NETVERSION;

   public CHandshakePacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CHandshakePacket(String pHostName, int pPort, ProtocolType pIntention) {
      this.protocolVersion = SharedConstants.getCurrentVersion().getProtocolVersion();
      this.hostName = pHostName;
      this.port = pPort;
      this.intention = pIntention;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.protocolVersion = p_148837_1_.readVarInt();
      this.hostName = p_148837_1_.readUtf(255);
      this.port = p_148837_1_.readUnsignedShort();
      this.intention = ProtocolType.getById(p_148837_1_.readVarInt());
      this.fmlVersion = net.minecraftforge.fml.network.NetworkHooks.getFMLVersion(this.hostName);
      this.hostName = this.hostName.split("\0")[0];
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.protocolVersion);
      pBuffer.writeUtf(this.hostName + "\0"+net.minecraftforge.fml.network.FMLNetworkConstants.NETVERSION+"\0");
      pBuffer.writeShort(this.port);
      pBuffer.writeVarInt(this.intention.getId());
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IHandshakeNetHandler pHandler) {
      pHandler.handleIntention(this);
   }

   public ProtocolType getIntention() {
      return this.intention;
   }

   public int getProtocolVersion() {
      return this.protocolVersion;
   }

   public String getFMLVersion() {
      return this.fmlVersion;
   }
}
