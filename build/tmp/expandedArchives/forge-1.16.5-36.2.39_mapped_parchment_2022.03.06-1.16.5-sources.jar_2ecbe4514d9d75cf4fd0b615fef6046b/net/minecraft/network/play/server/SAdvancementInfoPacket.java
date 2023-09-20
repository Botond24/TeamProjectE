package net.minecraft.network.play.server;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SAdvancementInfoPacket implements IPacket<IClientPlayNetHandler> {
   private boolean reset;
   private Map<ResourceLocation, Advancement.Builder> added;
   private Set<ResourceLocation> removed;
   private Map<ResourceLocation, AdvancementProgress> progress;

   public SAdvancementInfoPacket() {
   }

   public SAdvancementInfoPacket(boolean pReset, Collection<Advancement> pAdded, Set<ResourceLocation> pRemoved, Map<ResourceLocation, AdvancementProgress> pProgress) {
      this.reset = pReset;
      this.added = Maps.newHashMap();

      for(Advancement advancement : pAdded) {
         this.added.put(advancement.getId(), advancement.deconstruct());
      }

      this.removed = pRemoved;
      this.progress = Maps.newHashMap(pProgress);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleUpdateAdvancementsPacket(this);
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.reset = p_148837_1_.readBoolean();
      this.added = Maps.newHashMap();
      this.removed = Sets.newLinkedHashSet();
      this.progress = Maps.newHashMap();
      int i = p_148837_1_.readVarInt();

      for(int j = 0; j < i; ++j) {
         ResourceLocation resourcelocation = p_148837_1_.readResourceLocation();
         Advancement.Builder advancement$builder = Advancement.Builder.fromNetwork(p_148837_1_);
         this.added.put(resourcelocation, advancement$builder);
      }

      i = p_148837_1_.readVarInt();

      for(int k = 0; k < i; ++k) {
         ResourceLocation resourcelocation1 = p_148837_1_.readResourceLocation();
         this.removed.add(resourcelocation1);
      }

      i = p_148837_1_.readVarInt();

      for(int l = 0; l < i; ++l) {
         ResourceLocation resourcelocation2 = p_148837_1_.readResourceLocation();
         this.progress.put(resourcelocation2, AdvancementProgress.fromNetwork(p_148837_1_));
      }

   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeBoolean(this.reset);
      pBuffer.writeVarInt(this.added.size());

      for(Entry<ResourceLocation, Advancement.Builder> entry : this.added.entrySet()) {
         ResourceLocation resourcelocation = entry.getKey();
         Advancement.Builder advancement$builder = entry.getValue();
         pBuffer.writeResourceLocation(resourcelocation);
         advancement$builder.serializeToNetwork(pBuffer);
      }

      pBuffer.writeVarInt(this.removed.size());

      for(ResourceLocation resourcelocation1 : this.removed) {
         pBuffer.writeResourceLocation(resourcelocation1);
      }

      pBuffer.writeVarInt(this.progress.size());

      for(Entry<ResourceLocation, AdvancementProgress> entry1 : this.progress.entrySet()) {
         pBuffer.writeResourceLocation(entry1.getKey());
         entry1.getValue().serializeToNetwork(pBuffer);
      }

   }

   @OnlyIn(Dist.CLIENT)
   public Map<ResourceLocation, Advancement.Builder> getAdded() {
      return this.added;
   }

   @OnlyIn(Dist.CLIENT)
   public Set<ResourceLocation> getRemoved() {
      return this.removed;
   }

   @OnlyIn(Dist.CLIENT)
   public Map<ResourceLocation, AdvancementProgress> getProgress() {
      return this.progress;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean shouldReset() {
      return this.reset;
   }
}