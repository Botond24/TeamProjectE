package net.minecraft.network.play.server;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SEntityEquipmentPacket implements IPacket<IClientPlayNetHandler> {
   private int entity;
   private final List<Pair<EquipmentSlotType, ItemStack>> slots;

   public SEntityEquipmentPacket() {
      this.slots = Lists.newArrayList();
   }

   public SEntityEquipmentPacket(int pEntity, List<Pair<EquipmentSlotType, ItemStack>> pSlots) {
      this.entity = pEntity;
      this.slots = pSlots;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.entity = p_148837_1_.readVarInt();
      EquipmentSlotType[] aequipmentslottype = EquipmentSlotType.values();

      int i;
      do {
         i = p_148837_1_.readByte();
         EquipmentSlotType equipmentslottype = aequipmentslottype[i & 127];
         ItemStack itemstack = p_148837_1_.readItem();
         this.slots.add(Pair.of(equipmentslottype, itemstack));
      } while((i & -128) != 0);

   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeVarInt(this.entity);
      int i = this.slots.size();

      for(int j = 0; j < i; ++j) {
         Pair<EquipmentSlotType, ItemStack> pair = this.slots.get(j);
         EquipmentSlotType equipmentslottype = pair.getFirst();
         boolean flag = j != i - 1;
         int k = equipmentslottype.ordinal();
         pBuffer.writeByte(flag ? k | -128 : k);
         pBuffer.writeItem(pair.getSecond());
      }

   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleSetEquipment(this);
   }

   @OnlyIn(Dist.CLIENT)
   public int getEntity() {
      return this.entity;
   }

   @OnlyIn(Dist.CLIENT)
   public List<Pair<EquipmentSlotType, ItemStack>> getSlots() {
      return this.slots;
   }
}