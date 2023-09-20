package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.UUID;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SChatPacket implements IPacket<IClientPlayNetHandler> {
   private ITextComponent message;
   private ChatType type;
   private UUID sender;

   public SChatPacket() {
   }

   public SChatPacket(ITextComponent pMessage, ChatType pType, UUID pSender) {
      this.message = pMessage;
      this.type = pType;
      this.sender = pSender;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.message = p_148837_1_.readComponent();
      this.type = ChatType.getForIndex(p_148837_1_.readByte());
      this.sender = p_148837_1_.readUUID();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeComponent(this.message);
      pBuffer.writeByte(this.type.getIndex());
      pBuffer.writeUUID(this.sender);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleChat(this);
   }

   @OnlyIn(Dist.CLIENT)
   public ITextComponent getMessage() {
      return this.message;
   }

   public boolean isSystem() {
      return this.type == ChatType.SYSTEM || this.type == ChatType.GAME_INFO;
   }

   public ChatType getType() {
      return this.type;
   }

   @OnlyIn(Dist.CLIENT)
   public UUID getSender() {
      return this.sender;
   }

   /**
    * Whether decoding errors will be ignored for this packet.
    */
   public boolean isSkippable() {
      return true;
   }
}