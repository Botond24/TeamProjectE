package net.minecraft.network.play.server;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.io.IOException;
import java.util.Map;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SStatisticsPacket implements IPacket<IClientPlayNetHandler> {
   private Object2IntMap<Stat<?>> stats;

   public SStatisticsPacket() {
   }

   public SStatisticsPacket(Object2IntMap<Stat<?>> pStats) {
      this.stats = pStats;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleAwardStats(this);
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      int i = p_148837_1_.readVarInt();
      this.stats = new Object2IntOpenHashMap<>(i);

      for(int j = 0; j < i; ++j) {
         this.readStat(Registry.STAT_TYPE.byId(p_148837_1_.readVarInt()), p_148837_1_);
      }

   }

   private <T> void readStat(StatType<T> p_197684_1_, PacketBuffer p_197684_2_) {
      int i = p_197684_2_.readVarInt();
      int j = p_197684_2_.readVarInt();
      this.stats.put(p_197684_1_.get(p_197684_1_.getRegistry().byId(i)), j);
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.stats.size());

      for(Entry<Stat<?>> entry : this.stats.object2IntEntrySet()) {
         Stat<?> stat = entry.getKey();
         pBuffer.writeVarInt(Registry.STAT_TYPE.getId(stat.getType()));
         pBuffer.writeVarInt(this.getId(stat));
         pBuffer.writeVarInt(entry.getIntValue());
      }

   }

   private <T> int getId(Stat<T> p_197683_1_) {
      return p_197683_1_.getType().getRegistry().getId(p_197683_1_.getValue());
   }

   @OnlyIn(Dist.CLIENT)
   public Map<Stat<?>, Integer> getStats() {
      return this.stats;
   }
}