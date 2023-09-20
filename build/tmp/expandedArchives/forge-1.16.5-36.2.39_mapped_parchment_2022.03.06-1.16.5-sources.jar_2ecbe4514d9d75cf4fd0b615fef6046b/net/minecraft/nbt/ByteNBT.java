package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ByteNBT extends NumberNBT {
   public static final INBTType<ByteNBT> TYPE = new INBTType<ByteNBT>() {
      public ByteNBT load(DataInput pInput, int pDepth, NBTSizeTracker pAccounter) throws IOException {
         pAccounter.accountBits(72L);
         return ByteNBT.valueOf(pInput.readByte());
      }

      public String getName() {
         return "BYTE";
      }

      public String getPrettyName() {
         return "TAG_Byte";
      }

      public boolean isValue() {
         return true;
      }
   };
   public static final ByteNBT ZERO = valueOf((byte)0);
   public static final ByteNBT ONE = valueOf((byte)1);
   private final byte data;

   private ByteNBT(byte pData) {
      this.data = pData;
   }

   public static ByteNBT valueOf(byte pData) {
      return ByteNBT.Cache.cache[128 + pData];
   }

   public static ByteNBT valueOf(boolean pData) {
      return pData ? ONE : ZERO;
   }

   /**
    * Write the actual data contents of the tag, implemented in NBT extension classes
    */
   public void write(DataOutput pOutput) throws IOException {
      pOutput.writeByte(this.data);
   }

   /**
    * Gets the type byte for the tag.
    */
   public byte getId() {
      return 1;
   }

   public INBTType<ByteNBT> getType() {
      return TYPE;
   }

   public String toString() {
      return this.data + "b";
   }

   /**
    * Creates a clone of the tag.
    */
   public ByteNBT copy() {
      return this;
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else {
         return p_equals_1_ instanceof ByteNBT && this.data == ((ByteNBT)p_equals_1_).data;
      }
   }

   public int hashCode() {
      return this.data;
   }

   public ITextComponent getPrettyDisplay(String p_199850_1_, int p_199850_2_) {
      ITextComponent itextcomponent = (new StringTextComponent("b")).withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
      return (new StringTextComponent(String.valueOf((int)this.data))).append(itextcomponent).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
   }

   public long getAsLong() {
      return (long)this.data;
   }

   public int getAsInt() {
      return this.data;
   }

   public short getAsShort() {
      return (short)this.data;
   }

   public byte getAsByte() {
      return this.data;
   }

   public double getAsDouble() {
      return (double)this.data;
   }

   public float getAsFloat() {
      return (float)this.data;
   }

   public Number getAsNumber() {
      return this.data;
   }

   static class Cache {
      private static final ByteNBT[] cache = new ByteNBT[256];

      static {
         for(int i = 0; i < cache.length; ++i) {
            cache[i] = new ByteNBT((byte)(i - 128));
         }

      }
   }
}