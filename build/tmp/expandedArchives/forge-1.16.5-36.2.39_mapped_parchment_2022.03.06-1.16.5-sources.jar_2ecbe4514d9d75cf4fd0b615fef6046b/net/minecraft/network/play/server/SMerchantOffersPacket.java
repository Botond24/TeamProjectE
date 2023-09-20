package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.item.MerchantOffers;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SMerchantOffersPacket implements IPacket<IClientPlayNetHandler> {
   private int containerId;
   private MerchantOffers offers;
   private int villagerLevel;
   private int villagerXp;
   private boolean showProgress;
   private boolean canRestock;

   public SMerchantOffersPacket() {
   }

   public SMerchantOffersPacket(int pContainerId, MerchantOffers pOffers, int pVillagerLevel, int pVillagerXp, boolean pShowProgress, boolean pCanRestock) {
      this.containerId = pContainerId;
      this.offers = pOffers;
      this.villagerLevel = pVillagerLevel;
      this.villagerXp = pVillagerXp;
      this.showProgress = pShowProgress;
      this.canRestock = pCanRestock;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.containerId = p_148837_1_.readVarInt();
      this.offers = MerchantOffers.createFromStream(p_148837_1_);
      this.villagerLevel = p_148837_1_.readVarInt();
      this.villagerXp = p_148837_1_.readVarInt();
      this.showProgress = p_148837_1_.readBoolean();
      this.canRestock = p_148837_1_.readBoolean();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.containerId);
      this.offers.writeToStream(pBuffer);
      pBuffer.writeVarInt(this.villagerLevel);
      pBuffer.writeVarInt(this.villagerXp);
      pBuffer.writeBoolean(this.showProgress);
      pBuffer.writeBoolean(this.canRestock);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleMerchantOffers(this);
   }

   @OnlyIn(Dist.CLIENT)
   public int getContainerId() {
      return this.containerId;
   }

   @OnlyIn(Dist.CLIENT)
   public MerchantOffers getOffers() {
      return this.offers;
   }

   @OnlyIn(Dist.CLIENT)
   public int getVillagerLevel() {
      return this.villagerLevel;
   }

   @OnlyIn(Dist.CLIENT)
   public int getVillagerXp() {
      return this.villagerXp;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean showProgress() {
      return this.showProgress;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean canRestock() {
      return this.canRestock;
   }
}