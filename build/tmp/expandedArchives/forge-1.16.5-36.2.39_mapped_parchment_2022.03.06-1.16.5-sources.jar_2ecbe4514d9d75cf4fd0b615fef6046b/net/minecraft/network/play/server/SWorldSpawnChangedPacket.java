package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SWorldSpawnChangedPacket implements IPacket<IClientPlayNetHandler> {
   private BlockPos pos;
   private float angle;

   public SWorldSpawnChangedPacket() {
   }

   public SWorldSpawnChangedPacket(BlockPos pPos, float pAngle) {
      this.pos = pPos;
      this.angle = pAngle;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.pos = p_148837_1_.readBlockPos();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeBlockPos(this.pos);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleSetSpawn(this);
   }

   @OnlyIn(Dist.CLIENT)
   public BlockPos getPos() {
      return this.pos;
   }

   @OnlyIn(Dist.CLIENT)
   public float getAngle() {
      return this.angle;
   }
}