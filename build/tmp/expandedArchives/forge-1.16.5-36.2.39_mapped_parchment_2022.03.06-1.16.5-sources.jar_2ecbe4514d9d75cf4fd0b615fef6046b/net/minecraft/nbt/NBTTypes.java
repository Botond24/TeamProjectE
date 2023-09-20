package net.minecraft.nbt;

public class NBTTypes {
   private static final INBTType<?>[] TYPES = new INBTType[]{EndNBT.TYPE, ByteNBT.TYPE, ShortNBT.TYPE, IntNBT.TYPE, LongNBT.TYPE, FloatNBT.TYPE, DoubleNBT.TYPE, ByteArrayNBT.TYPE, StringNBT.TYPE, ListNBT.TYPE, CompoundNBT.TYPE, IntArrayNBT.TYPE, LongArrayNBT.TYPE};

   public static INBTType<?> getType(int pId) {
      return pId >= 0 && pId < TYPES.length ? TYPES[pId] : INBTType.createInvalid(pId);
   }
}