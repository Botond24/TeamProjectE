package net.minecraft.util.palette;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IPalette<T> {
   int idFor(T pState);

   boolean maybeHas(Predicate<T> pFilter);

   /**
    * Gets the block state by the palette id.
    */
   @Nullable
   T valueFor(int pIndexKey);

   @OnlyIn(Dist.CLIENT)
   void read(PacketBuffer pBuf);

   void write(PacketBuffer pBuf);

   int getSerializedSize();

   void read(ListNBT pNbt);
}