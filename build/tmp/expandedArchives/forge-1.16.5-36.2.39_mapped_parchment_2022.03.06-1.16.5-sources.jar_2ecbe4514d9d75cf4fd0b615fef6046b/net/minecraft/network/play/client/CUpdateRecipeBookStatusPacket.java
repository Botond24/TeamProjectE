package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.item.crafting.RecipeBookCategory;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.IServerPlayNetHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CUpdateRecipeBookStatusPacket implements IPacket<IServerPlayNetHandler> {
   private RecipeBookCategory bookType;
   private boolean isOpen;
   private boolean isFiltering;

   public CUpdateRecipeBookStatusPacket() {
   }

   @OnlyIn(Dist.CLIENT)
   public CUpdateRecipeBookStatusPacket(RecipeBookCategory pBookType, boolean pIsOpen, boolean pIsFiltering) {
      this.bookType = pBookType;
      this.isOpen = pIsOpen;
      this.isFiltering = pIsFiltering;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.bookType = p_148837_1_.readEnum(RecipeBookCategory.class);
      this.isOpen = p_148837_1_.readBoolean();
      this.isFiltering = p_148837_1_.readBoolean();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeEnum(this.bookType);
      pBuffer.writeBoolean(this.isOpen);
      pBuffer.writeBoolean(this.isFiltering);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IServerPlayNetHandler pHandler) {
      pHandler.handleRecipeBookChangeSettingsPacket(this);
   }

   public RecipeBookCategory getBookType() {
      return this.bookType;
   }

   public boolean isOpen() {
      return this.isOpen;
   }

   public boolean isFiltering() {
      return this.isFiltering;
   }
}