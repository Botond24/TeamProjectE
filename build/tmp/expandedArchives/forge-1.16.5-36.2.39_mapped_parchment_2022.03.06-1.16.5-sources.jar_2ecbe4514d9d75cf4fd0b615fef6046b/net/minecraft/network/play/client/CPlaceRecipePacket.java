package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CPlaceRecipePacket implements IPacket<IServerPlayNetHandler> {
   private int containerId;
   private ResourceLocation recipe;
   private boolean shiftDown;

   public CPlaceRecipePacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CPlaceRecipePacket(int pContainerId, IRecipe<?> pRecipe, boolean pShiftDown) {
      this.containerId = pContainerId;
      this.recipe = pRecipe.getId();
      this.shiftDown = pShiftDown;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.containerId = p_148837_1_.readByte();
      this.recipe = p_148837_1_.readResourceLocation();
      this.shiftDown = p_148837_1_.readBoolean();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeByte(this.containerId);
      pBuffer.writeResourceLocation(this.recipe);
      pBuffer.writeBoolean(this.shiftDown);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handlePlaceRecipe(this);
   }

   public int getContainerId() {
      return this.containerId;
   }

   public ResourceLocation getRecipe() {
      return this.recipe;
   }

   public boolean isShiftDown() {
      return this.shiftDown;
   }
}