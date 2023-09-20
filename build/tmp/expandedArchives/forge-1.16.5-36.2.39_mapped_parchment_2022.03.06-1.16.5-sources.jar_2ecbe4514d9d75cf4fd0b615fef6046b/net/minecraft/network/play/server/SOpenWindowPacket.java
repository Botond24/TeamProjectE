package net.minecraft.network.play.server;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SOpenWindowPacket implements IPacket<IClientPlayNetHandler> {
   private int containerId;
   private int type;
   private ITextComponent title;

   public SOpenWindowPacket() {
   }

   public SOpenWindowPacket(int pContainerId, ContainerType<?> pMenuType, ITextComponent pTitle) {
      this.containerId = pContainerId;
      this.type = Registry.MENU.getId(pMenuType);
      this.title = pTitle;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.containerId = p_148837_1_.readVarInt();
      this.type = p_148837_1_.readVarInt();
      this.title = p_148837_1_.readComponent();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.containerId);
      pBuffer.writeVarInt(this.type);
      pBuffer.writeComponent(this.title);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleOpenScreen(this);
   }

   @OnlyIn(Dist.CLIENT)
   public int getContainerId() {
      return this.containerId;
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public ContainerType<?> getType() {
      return Registry.MENU.byId(this.type);
   }

   @OnlyIn(Dist.CLIENT)
   public ITextComponent getTitle() {
      return this.title;
   }
}