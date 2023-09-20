package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.Collection;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SMapDataPacket implements IPacket<IClientPlayNetHandler> {
   private int mapId;
   private byte scale;
   private boolean trackingPosition;
   private boolean locked;
   private MapDecoration[] decorations;
   private int startX;
   private int startY;
   private int width;
   private int height;
   private byte[] mapColors;

   public SMapDataPacket() {
   }

   public SMapDataPacket(int p_i50772_1_, byte p_i50772_2_, boolean p_i50772_3_, boolean p_i50772_4_, Collection<MapDecoration> p_i50772_5_, byte[] p_i50772_6_, int p_i50772_7_, int p_i50772_8_, int p_i50772_9_, int p_i50772_10_) {
      this.mapId = p_i50772_1_;
      this.scale = p_i50772_2_;
      this.trackingPosition = p_i50772_3_;
      this.locked = p_i50772_4_;
      this.decorations = p_i50772_5_.toArray(new MapDecoration[p_i50772_5_.size()]);
      this.startX = p_i50772_7_;
      this.startY = p_i50772_8_;
      this.width = p_i50772_9_;
      this.height = p_i50772_10_;
      this.mapColors = new byte[p_i50772_9_ * p_i50772_10_];

      for(int i = 0; i < p_i50772_9_; ++i) {
         for(int j = 0; j < p_i50772_10_; ++j) {
            this.mapColors[i + j * p_i50772_9_] = p_i50772_6_[p_i50772_7_ + i + (p_i50772_8_ + j) * 128];
         }
      }

   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.mapId = p_148837_1_.readVarInt();
      this.scale = p_148837_1_.readByte();
      this.trackingPosition = p_148837_1_.readBoolean();
      this.locked = p_148837_1_.readBoolean();
      this.decorations = new MapDecoration[p_148837_1_.readVarInt()];

      for(int i = 0; i < this.decorations.length; ++i) {
         MapDecoration.Type mapdecoration$type = p_148837_1_.readEnum(MapDecoration.Type.class);
         this.decorations[i] = new MapDecoration(mapdecoration$type, p_148837_1_.readByte(), p_148837_1_.readByte(), (byte)(p_148837_1_.readByte() & 15), p_148837_1_.readBoolean() ? p_148837_1_.readComponent() : null);
      }

      this.width = p_148837_1_.readUnsignedByte();
      if (this.width > 0) {
         this.height = p_148837_1_.readUnsignedByte();
         this.startX = p_148837_1_.readUnsignedByte();
         this.startY = p_148837_1_.readUnsignedByte();
         this.mapColors = p_148837_1_.readByteArray();
      }

   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.mapId);
      pBuffer.writeByte(this.scale);
      pBuffer.writeBoolean(this.trackingPosition);
      pBuffer.writeBoolean(this.locked);
      pBuffer.writeVarInt(this.decorations.length);

      for(MapDecoration mapdecoration : this.decorations) {
         pBuffer.writeEnum(mapdecoration.getType());
         pBuffer.writeByte(mapdecoration.getX());
         pBuffer.writeByte(mapdecoration.getY());
         pBuffer.writeByte(mapdecoration.getRot() & 15);
         if (mapdecoration.getName() != null) {
            pBuffer.writeBoolean(true);
            pBuffer.writeComponent(mapdecoration.getName());
         } else {
            pBuffer.writeBoolean(false);
         }
      }

      pBuffer.writeByte(this.width);
      if (this.width > 0) {
         pBuffer.writeByte(this.height);
         pBuffer.writeByte(this.startX);
         pBuffer.writeByte(this.startY);
         pBuffer.writeByteArray(this.mapColors);
      }

   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleMapItemData(this);
   }

   @OnlyIn(Dist.CLIENT)
   public int getMapId() {
      return this.mapId;
   }

   /**
    * Sets new MapData from the packet to given MapData param
    */
   @OnlyIn(Dist.CLIENT)
   public void applyToMap(MapData pMapdata) {
      pMapdata.scale = this.scale;
      pMapdata.trackingPosition = this.trackingPosition;
      pMapdata.locked = this.locked;
      pMapdata.decorations.clear();

      for(int i = 0; i < this.decorations.length; ++i) {
         MapDecoration mapdecoration = this.decorations[i];
         pMapdata.decorations.put("icon-" + i, mapdecoration);
      }

      for(int j = 0; j < this.width; ++j) {
         for(int k = 0; k < this.height; ++k) {
            pMapdata.colors[this.startX + j + (this.startY + k) * 128] = this.mapColors[j + k * this.width];
         }
      }

   }
}