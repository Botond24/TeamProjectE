package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CInputPacket implements IPacket<IServerPlayNetHandler> {
   /** Positive for left strafe, negative for right */
   private float xxa;
   private float zza;
   private boolean isJumping;
   private boolean isShiftKeyDown;

   public CInputPacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CInputPacket(float pXxa, float pZza, boolean pIsJumping, boolean pIsShiftKeyDown) {
      this.xxa = pXxa;
      this.zza = pZza;
      this.isJumping = pIsJumping;
      this.isShiftKeyDown = pIsShiftKeyDown;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.xxa = p_148837_1_.readFloat();
      this.zza = p_148837_1_.readFloat();
      byte b0 = p_148837_1_.readByte();
      this.isJumping = (b0 & 1) > 0;
      this.isShiftKeyDown = (b0 & 2) > 0;
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeFloat(this.xxa);
      pBuffer.writeFloat(this.zza);
      byte b0 = 0;
      if (this.isJumping) {
         b0 = (byte)(b0 | 1);
      }

      if (this.isShiftKeyDown) {
         b0 = (byte)(b0 | 2);
      }

      pBuffer.writeByte(b0);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handlePlayerInput(this);
   }

   public float getXxa() {
      return this.xxa;
   }

   public float getZza() {
      return this.zza;
   }

   public boolean isJumping() {
      return this.isJumping;
   }

   public boolean isShiftKeyDown() {
      return this.isShiftKeyDown;
   }
}