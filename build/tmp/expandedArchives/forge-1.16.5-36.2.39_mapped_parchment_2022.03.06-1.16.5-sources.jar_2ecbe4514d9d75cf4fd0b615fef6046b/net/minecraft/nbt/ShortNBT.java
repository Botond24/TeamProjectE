package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ShortNBT extends NumberNBT {
   public static final INBTType<ShortNBT> TYPE = new INBTType<ShortNBT>() {
      public ShortNBT load(DataInput pInput, int pDepth, NBTSizeTracker pAccounter) throws IOException {
         pAccounter.accountBits(80L);
         return ShortNBT.valueOf(pInput.readShort());
      }

      public String getName() {
         return "SHORT";
      }

      public String getPrettyName() {
         return "TAG_Short";
      }

      public boolean isValue() {
         return true;
      }
   };
   private final short data;

   private ShortNBT(short pData) {
      this.data = pData;
   }

   public static ShortNBT valueOf(short pData) {
      return pData >= -128 && pData <= 1024 ? ShortNBT.Cache.cache[pData + 128] : new ShortNBT(pData);
   }

   /**
    * Write the actual data contents of the tag, implemented in NBT extension classes
    */
   public void write(DataOutput pOutput) throws IOException {
      pOutput.writeShort(this.data);
   }

   /**
    * Gets the type byte for the tag.
    */
   public byte getId() {
      return 2;
   }

   public INBTType<ShortNBT> getType() {
      return TYPE;
   }

   public String toString() {
      return this.data + "s";
   }

   /**
    * Creates a clone of the tag.
    */
   public ShortNBT copy() {
      return this;
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else {
         return p_equals_1_ instanceof ShortNBT && this.data == ((ShortNBT)p_equals_1_).data;
      }
   }

   public int hashCode() {
      return this.data;
   }

   public ITextComponent getPrettyDisplay(String p_199850_1_, int p_199850_2_) {
      ITextComponent itextcomponent = (new StringTextComponent("s")).withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
      return (new StringTextComponent(String.valueOf((int)this.data))).append(itextcomponent).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
   }

   public long getAsLong() {
      return (long)this.data;
   }

   public int getAsInt() {
      return this.data;
   }

   public short getAsShort() {
      return this.data;
   }

   public byte getAsByte() {
      return (byte)(this.data & 255);
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
      static final ShortNBT[] cache = new ShortNBT[1153];

      static {
         for(int i = 0; i < cache.length; ++i) {
            cache[i] = new ShortNBT((short)(-128 + i));
         }

      }
   }
}