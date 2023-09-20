package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SPlaceGhostRecipePacket implements IPacket<IClientPlayNetHandler> {
   private int containerId;
   private ResourceLocation recipe;

   public SPlaceGhostRecipePacket() {
   }

   public SPlaceGhostRecipePacket(int pContainerId, IRecipe<?> pRecipe) {
      this.containerId = pContainerId;
      this.recipe = pRecipe.getId();
   }

   @OnlyIn(Dist.CLIENT)
   public ResourceLocation getRecipe() {
      return this.recipe;
   }

   @OnlyIn(Dist.CLIENT)
   public int getContainerId() {
      return this.containerId;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.containerId = p_148837_1_.readByte();
      this.recipe = p_148837_1_.readResourceLocation();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeByte(this.containerId);
      pBuffer.writeResourceLocation(this.recipe);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handlePlaceRecipe(this);
   }
}