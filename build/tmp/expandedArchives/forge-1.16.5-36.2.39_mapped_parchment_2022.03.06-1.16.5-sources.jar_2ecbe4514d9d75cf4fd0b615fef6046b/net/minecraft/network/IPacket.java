package net.minecraft.network;

import java.io.IOException;

public interface IPacket<T extends INetHandler> {
   void read(PacketBuffer p_148837_1_) throws IOException;

   /**
    * Writes the raw packet data to the data stream.
    */
   void write(PacketBuffer pBuffer) throws IOException;

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   void handle(T pHandler);

   /**
    * Whether decoding errors will be ignored for this packet.
    */
   default boolean isSkippable() {
      return false;
   }
}