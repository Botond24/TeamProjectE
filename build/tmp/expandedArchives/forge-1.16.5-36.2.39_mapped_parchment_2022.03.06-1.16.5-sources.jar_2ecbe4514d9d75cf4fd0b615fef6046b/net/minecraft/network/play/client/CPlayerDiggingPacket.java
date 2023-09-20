package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CPlayerDiggingPacket implements IPacket<IServerPlayNetHandler> {
   private BlockPos pos;
   private Direction direction;
   /** Status of the digging (started, ongoing, broken). */
   private CPlayerDiggingPacket.Action action;

   public CPlayerDiggingPacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CPlayerDiggingPacket(CPlayerDiggingPacket.Action pAction, BlockPos pPos, Direction pDirection) {
      this.action = pAction;
      this.pos = pPos.immutable();
      this.direction = pDirection;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.action = p_148837_1_.readEnum(CPlayerDiggingPacket.Action.class);
      this.pos = p_148837_1_.readBlockPos();
      this.direction = Direction.from3DDataValue(p_148837_1_.readUnsignedByte());
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeEnum(this.action);
      pBuffer.writeBlockPos(this.pos);
      pBuffer.writeByte(this.direction.get3DDataValue());
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handlePlayerAction(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public Direction getDirection() {
      return this.direction;
   }

   public CPlayerDiggingPacket.Action getAction() {
      return this.action;
   }

   public static enum Action {
      START_DESTROY_BLOCK,
      ABORT_DESTROY_BLOCK,
      STOP_DESTROY_BLOCK,
      DROP_ALL_ITEMS,
      DROP_ITEM,
      RELEASE_USE_ITEM,
      SWAP_ITEM_WITH_OFFHAND;
   }
}