package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CPlayerTryUseItemOnBlockPacket implements IPacket<IServerPlayNetHandler> {
   private BlockRayTraceResult blockHit;
   private Hand hand;

   public CPlayerTryUseItemOnBlockPacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CPlayerTryUseItemOnBlockPacket(Hand pHand, BlockRayTraceResult pBlockHit) {
      this.hand = pHand;
      this.blockHit = pBlockHit;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.hand = p_148837_1_.readEnum(Hand.class);
      this.blockHit = p_148837_1_.readBlockHitResult();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeEnum(this.hand);
      pBuffer.writeBlockHitResult(this.blockHit);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleUseItemOn(this);
   }

   public Hand getHand() {
      return this.hand;
   }

   public BlockRayTraceResult getHitResult() {
      return this.blockHit;
   }
}