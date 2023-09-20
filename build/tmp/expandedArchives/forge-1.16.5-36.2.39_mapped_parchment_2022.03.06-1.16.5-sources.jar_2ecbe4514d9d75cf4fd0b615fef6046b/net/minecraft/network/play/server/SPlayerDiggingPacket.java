package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SPlayerDiggingPacket implements IPacket<IClientPlayNetHandler> {
   /** Unused (probably related to the unused parameter in the constructor) */
   private static final Logger LOGGER = LogManager.getLogger();
   private BlockPos pos;
   private BlockState state;
   CPlayerDiggingPacket.Action action;
   private boolean allGood;

   public SPlayerDiggingPacket() {
   }

   public SPlayerDiggingPacket(BlockPos pPos, BlockState pState, CPlayerDiggingPacket.Action pAction, boolean pAllGood, String pReason) {
      this.pos = pPos.immutable();
      this.state = pState;
      this.action = pAction;
      this.allGood = pAllGood;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.pos = p_148837_1_.readBlockPos();
      this.state = Block.BLOCK_STATE_REGISTRY.byId(p_148837_1_.readVarInt());
      this.action = p_148837_1_.readEnum(CPlayerDiggingPacket.Action.class);
      this.allGood = p_148837_1_.readBoolean();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeBlockPos(this.pos);
      pBuffer.writeVarInt(Block.getId(this.state));
      pBuffer.writeEnum(this.action);
      pBuffer.writeBoolean(this.allGood);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleBlockBreakAck(this);
   }

   @OnlyIn(Dist.CLIENT)
   public BlockState getState() {
      return this.state;
   }

   @OnlyIn(Dist.CLIENT)
   public BlockPos getPos() {
      return this.pos;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean allGood() {
      return this.allGood;
   }

   @OnlyIn(Dist.CLIENT)
   public CPlayerDiggingPacket.Action action() {
      return this.action;
   }
}