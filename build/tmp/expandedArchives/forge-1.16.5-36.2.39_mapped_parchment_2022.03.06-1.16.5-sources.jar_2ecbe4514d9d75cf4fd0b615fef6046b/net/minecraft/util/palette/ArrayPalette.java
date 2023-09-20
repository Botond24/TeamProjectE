package net.minecraft.util.palette;

import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ArrayPalette<T> implements IPalette<T> {
   private final ObjectIntIdentityMap<T> registry;
   private final T[] values;
   private final IResizeCallback<T> resizeHandler;
   private final Function<CompoundNBT, T> reader;
   private final int bits;
   private int size;

   public ArrayPalette(ObjectIntIdentityMap<T> pRegistry, int pBits, IResizeCallback<T> pResizeHandler, Function<CompoundNBT, T> pReader) {
      this.registry = pRegistry;
      this.values = (T[])(new Object[1 << pBits]);
      this.bits = pBits;
      this.resizeHandler = pResizeHandler;
      this.reader = pReader;
   }

   public int idFor(T pState) {
      for(int i = 0; i < this.size; ++i) {
         if (this.values[i] == pState) {
            return i;
         }
      }

      int j = this.size;
      if (j < this.values.length) {
         this.values[j] = pState;
         ++this.size;
         return j;
      } else {
         return this.resizeHandler.onResize(this.bits + 1, pState);
      }
   }

   public boolean maybeHas(Predicate<T> pFilter) {
      for(int i = 0; i < this.size; ++i) {
         if (pFilter.test(this.values[i])) {
            return true;
         }
      }

      return false;
   }

   /**
    * Gets the block state by the palette id.
    */
   @Nullable
   public T valueFor(int pIndexKey) {
      return (T)(pIndexKey >= 0 && pIndexKey < this.size ? this.values[pIndexKey] : null);
   }

   @OnlyIn(Dist.CLIENT)
   public void read(PacketBuffer pBuf) {
      this.size = pBuf.readVarInt();

      for(int i = 0; i < this.size; ++i) {
         this.values[i] = this.registry.byId(pBuf.readVarInt());
      }

   }

   public void write(PacketBuffer pBuf) {
      pBuf.writeVarInt(this.size);

      for(int i = 0; i < this.size; ++i) {
         pBuf.writeVarInt(this.registry.getId(this.values[i]));
      }

   }

   public int getSerializedSize() {
      int i = PacketBuffer.getVarIntSize(this.getSize());

      for(int j = 0; j < this.getSize(); ++j) {
         i += PacketBuffer.getVarIntSize(this.registry.getId(this.values[j]));
      }

      return i;
   }

   public int getSize() {
      return this.size;
   }

   public void read(ListNBT pNbt) {
      for(int i = 0; i < pNbt.size(); ++i) {
         this.values[i] = this.reader.apply(pNbt.getCompound(i));
      }

      this.size = pNbt.size();
   }
}