package net.minecraft.network.login.server;

import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.util.UUID;
import net.minecraft.client.network.login.IClientLoginNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.UUIDCodec;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SLoginSuccessPacket implements IPacket<IClientLoginNetHandler> {
   private GameProfile gameProfile;

   public SLoginSuccessPacket() {
   }

   public SLoginSuccessPacket(GameProfile pGameProfile) {
      this.gameProfile = pGameProfile;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      int[] aint = new int[4];

      for(int i = 0; i < aint.length; ++i) {
         aint[i] = p_148837_1_.readInt();
      }

      UUID uuid = UUIDCodec.uuidFromIntArray(aint);
      String s = p_148837_1_.readUtf(16);
      this.gameProfile = new GameProfile(uuid, s);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      for(int i : UUIDCodec.uuidToIntArray(this.gameProfile.getId())) {
         pBuffer.writeInt(i);
      }

      pBuffer.writeUtf(this.gameProfile.getName());
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientLoginNetHandler pHandler) {
      pHandler.handleGameProfile(this);
   }

   @OnlyIn(Dist.CLIENT)
   public GameProfile getGameProfile() {
      return this.gameProfile;
   }
}