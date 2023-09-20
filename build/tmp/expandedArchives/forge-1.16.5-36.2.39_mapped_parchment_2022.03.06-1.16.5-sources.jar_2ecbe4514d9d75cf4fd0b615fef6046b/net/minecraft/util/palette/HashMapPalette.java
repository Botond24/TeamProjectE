package net.minecraft.util.palette;

import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class HashMapPalette<T> implements IPalette<T> {
   private final ObjectIntIdentityMap<T> registry;
   private final IntIdentityHashBiMap<T> values;
   private final IResizeCallback<T> resizeHandler;
   private final Function<CompoundNBT, T> reader;
   private final Function<T, CompoundNBT> writer;
   private final int bits;

   public HashMapPalette(ObjectIntIdentityMap<T> pRegistry, int pSize, IResizeCallback<T> pResizeHandler, Function<CompoundNBT, T> pReader, Function<T, CompoundNBT> pWriter) {
      this.registry = pRegistry;
      this.bits = pSize;
      this.resizeHandler = pResizeHandler;
      this.reader = pReader;
      this.writer = pWriter;
      this.values = new IntIdentityHashBiMap<>(1 << pSize);
   }

   public int idFor(T pState) {
      int i = this.values.getId(pState);
      if (i == -1) {
         i = this.values.add(pState);
         if (i >= 1 << this.bits) {
            i = this.resizeHandler.onResize(this.bits + 1, pState);
         }
      }

      return i;
   }

   public boolean maybeHas(Predicate<T> pFilter) {
      for(int i = 0; i < this.getSize(); ++i) {
         if (pFilter.test(this.values.byId(i))) {
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
      return this.values.byId(pIndexKey);
   }

   @OnlyIn(Dist.CLIENT)
   public void read(PacketBuffer pBuf) {
      this.values.clear();
      int i = pBuf.readVarInt();

      for(int j = 0; j < i; ++j) {
         this.values.add(this.registry.byId(pBuf.readVarInt()));
      }

   }

   public void write(PacketBuffer pBuf) {
      int i = this.getSize();
      pBuf.writeVarInt(i);

      for(int j = 0; j < i; ++j) {
         pBuf.writeVarInt(this.registry.getId(this.values.byId(j)));
      }

   }

   public int getSerializedSize() {
      int i = PacketBuffer.getVarIntSize(this.getSize());

      for(int j = 0; j < this.getSize(); ++j) {
         i += PacketBuffer.getVarIntSize(this.registry.getId(this.values.byId(j)));
      }

      return i;
   }

   public int getSize() {
      return this.values.size();
   }

   public void read(ListNBT pNbt) {
      this.values.clear();

      for(int i = 0; i < pNbt.size(); ++i) {
         this.values.add(this.reader.apply(pNbt.getCompound(i)));
      }

   }

   public void write(ListNBT pPaletteList) {
      for(int i = 0; i < this.getSize(); ++i) {
         pPaletteList.add(this.writer.apply(this.values.byId(i)));
      }

   }
}