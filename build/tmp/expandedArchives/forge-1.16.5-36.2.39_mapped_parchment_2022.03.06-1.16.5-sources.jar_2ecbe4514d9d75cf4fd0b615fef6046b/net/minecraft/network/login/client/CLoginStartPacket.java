package net.minecraft.network.login.client;

import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.util.UUID;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.login.IServerLoginNetHandler;

public class CLoginStartPacket implements IPacket<IServerLoginNetHandler> {
   private GameProfile gameProfile;

   public CLoginStartPacket() {
   }

   public CLoginStartPacket(GameProfile pGameProfile) {
      this.gameProfile = pGameProfile;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.gameProfile = new GameProfile((UUID)null, p_148837_1_.readUtf(16));
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeUtf(this.gameProfile.getName());
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerLoginNetHandler pHandler) {
      pHandler.handleHello(this);
   }

   public GameProfile getGameProfile() {
      return this.gameProfile;
   }
}