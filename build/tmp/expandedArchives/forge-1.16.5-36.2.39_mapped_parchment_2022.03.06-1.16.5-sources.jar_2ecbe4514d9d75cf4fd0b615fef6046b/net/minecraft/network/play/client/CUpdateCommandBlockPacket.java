package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CUpdateCommandBlockPacket implements IPacket<IServerPlayNetHandler> {
   private BlockPos pos;
   private String command;
   private boolean trackOutput;
   private boolean conditional;
   private boolean automatic;
   private CommandBlockTileEntity.Mode mode;

   public CUpdateCommandBlockPacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CUpdateCommandBlockPacket(BlockPos pPos, String pCommand, CommandBlockTileEntity.Mode pMode, boolean pTrackOutput, boolean pConditional, boolean pAutomatic) {
      this.pos = pPos;
      this.command = pCommand;
      this.trackOutput = pTrackOutput;
      this.conditional = pConditional;
      this.automatic = pAutomatic;
      this.mode = pMode;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.pos = p_148837_1_.readBlockPos();
      this.command = p_148837_1_.readUtf(32767);
      this.mode = p_148837_1_.readEnum(CommandBlockTileEntity.Mode.class);
      int i = p_148837_1_.readByte();
      this.trackOutput = (i & 1) != 0;
      this.conditional = (i & 2) != 0;
      this.automatic = (i & 4) != 0;
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeBlockPos(this.pos);
      pBuffer.writeUtf(this.command);
      pBuffer.writeEnum(this.mode);
      int i = 0;
      if (this.trackOutput) {
         i |= 1;
      }

      if (this.conditional) {
         i |= 2;
      }

      if (this.automatic) {
         i |= 4;
      }

      pBuffer.writeByte(i);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleSetCommandBlock(this);
   }

   public BlockPos getPos() {
      return this.pos;
   }

   public String getCommand() {
      return this.command;
   }

   public boolean isTrackOutput() {
      return this.trackOutput;
   }

   public boolean isConditional() {
      return this.conditional;
   }

   public boolean isAutomatic() {
      return this.automatic;
   }

   public CommandBlockTileEntity.Mode getMode() {
      return this.mode;
   }
}