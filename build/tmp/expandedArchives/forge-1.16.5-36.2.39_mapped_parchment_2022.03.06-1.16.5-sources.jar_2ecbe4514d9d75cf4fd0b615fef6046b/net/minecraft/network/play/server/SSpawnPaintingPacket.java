package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.UUID;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.entity.item.PaintingType;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SSpawnPaintingPacket implements IPacket<IClientPlayNetHandler> {
   private int id;
   private UUID uuid;
   private BlockPos pos;
   private Direction direction;
   private int motive;

   public SSpawnPaintingPacket() {
   }

   public SSpawnPaintingPacket(PaintingEntity pPainting) {
      this.id = pPainting.getId();
      this.uuid = pPainting.getUUID();
      this.pos = pPainting.getPos();
      this.direction = pPainting.getDirection();
      this.motive = Registry.MOTIVE.getId(pPainting.motive);
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.id = p_148837_1_.readVarInt();
      this.uuid = p_148837_1_.readUUID();
      this.motive = p_148837_1_.readVarInt();
      this.pos = p_148837_1_.readBlockPos();
      this.direction = Direction.from2DDataValue(p_148837_1_.readUnsignedByte());
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.id);
      pBuffer.writeUUID(this.uuid);
      pBuffer.writeVarInt(this.motive);
      pBuffer.writeBlockPos(this.pos);
      pBuffer.writeByte(this.direction.get2DDataValue());
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleAddPainting(this);
   }

   @OnlyIn(Dist.CLIENT)
   public int getId() {
      return this.id;
   }

   @OnlyIn(Dist.CLIENT)
   public UUID getUUID() {
      return this.uuid;
   }

   @OnlyIn(Dist.CLIENT)
   public BlockPos getPos() {
      return this.pos;
   }

   @OnlyIn(Dist.CLIENT)
   public Direction getDirection() {
      return this.direction;
   }

   @OnlyIn(Dist.CLIENT)
   public PaintingType getMotive() {
      return Registry.MOTIVE.byId(this.motive);
   }
}