package net.minecraft.network.play.client;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.minecart.CommandBlockMinecartEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraft.tileentity.CommandBlockLogic;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CUpdateMinecartCommandBlockPacket implements IPacket<IServerPlayNetHandler> {
   private int entity;
   private String command;
   private boolean trackOutput;

   public CUpdateMinecartCommandBlockPacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CUpdateMinecartCommandBlockPacket(int pEntity, String pCommand, boolean pTrackOutput) {
      this.entity = pEntity;
      this.command = pCommand;
      this.trackOutput = pTrackOutput;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.entity = p_148837_1_.readVarInt();
      this.command = p_148837_1_.readUtf(32767);
      this.trackOutput = p_148837_1_.readBoolean();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.entity);
      pBuffer.writeUtf(this.command);
      pBuffer.writeBoolean(this.trackOutput);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleSetCommandMinecart(this);
   }

   @Nullable
   public CommandBlockLogic getCommandBlock(World pLevel) {
      Entity entity = pLevel.getEntity(this.entity);
      return entity instanceof CommandBlockMinecartEntity ? ((CommandBlockMinecartEntity)entity).getCommandBlock() : null;
   }

   public String getCommand() {
      return this.command;
   }

   public boolean isTrackOutput() {
      return this.trackOutput;
   }
}