package net.minecraft.util.palette;

import java.util.function.Predicate;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class IdentityPalette<T> implements IPalette<T> {
   private final ObjectIntIdentityMap<T> registry;
   private final T defaultValue;

   public IdentityPalette(ObjectIntIdentityMap<T> pRegistry, T pDefaultValue) {
      this.registry = pRegistry;
      this.defaultValue = pDefaultValue;
   }

   public int idFor(T pState) {
      int i = this.registry.getId(pState);
      return i == -1 ? 0 : i;
   }

   public boolean maybeHas(Predicate<T> pFilter) {
      return true;
   }

   /**
    * Gets the block state by the palette id.
    */
   public T valueFor(int pIndexKey) {
      T t = this.registry.byId(pIndexKey);
      return (T)(t == null ? this.defaultValue : t);
   }

   @OnlyIn(Dist.CLIENT)
   public void read(PacketBuffer pBuf) {
   }

   public void write(PacketBuffer pBuf) {
   }

   public int getSerializedSize() {
      return PacketBuffer.getVarIntSize(0);
   }

   public void read(ListNBT pNbt) {
   }
}