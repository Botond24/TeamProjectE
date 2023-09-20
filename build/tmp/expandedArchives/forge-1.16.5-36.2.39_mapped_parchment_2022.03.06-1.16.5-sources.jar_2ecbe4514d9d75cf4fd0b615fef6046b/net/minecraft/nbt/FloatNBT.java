package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class FloatNBT extends NumberNBT {
   public static final FloatNBT ZERO = new FloatNBT(0.0F);
   public static final INBTType<FloatNBT> TYPE = new INBTType<FloatNBT>() {
      public FloatNBT load(DataInput pInput, int pDepth, NBTSizeTracker pAccounter) throws IOException {
         pAccounter.accountBits(96L);
         return FloatNBT.valueOf(pInput.readFloat());
      }

      public String getName() {
         return "FLOAT";
      }

      public String getPrettyName() {
         return "TAG_Float";
      }

      public boolean isValue() {
         return true;
      }
   };
   private final float data;

   private FloatNBT(float pData) {
      this.data = pData;
   }

   public static FloatNBT valueOf(float pData) {
      return pData == 0.0F ? ZERO : new FloatNBT(pData);
   }

   /**
    * Write the actual data contents of the tag, implemented in NBT extension classes
    */
   public void write(DataOutput pOutput) throws IOException {
      pOutput.writeFloat(this.data);
   }

   /**
    * Gets the type byte for the tag.
    */
   public byte getId() {
      return 5;
   }

   public INBTType<FloatNBT> getType() {
      return TYPE;
   }

   public String toString() {
      return this.data + "f";
   }

   /**
    * Creates a clone of the tag.
    */
   public FloatNBT copy() {
      return this;
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else {
         return p_equals_1_ instanceof FloatNBT && this.data == ((FloatNBT)p_equals_1_).data;
      }
   }

   public int hashCode() {
      return Float.floatToIntBits(this.data);
   }

   public ITextComponent getPrettyDisplay(String p_199850_1_, int p_199850_2_) {
      ITextComponent itextcomponent = (new StringTextComponent("f")).withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
      return (new StringTextComponent(String.valueOf(this.data))).append(itextcomponent).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
   }

   public long getAsLong() {
      return (long)this.data;
   }

   public int getAsInt() {
      return MathHelper.floor(this.data);
   }

   public short getAsShort() {
      return (short)(MathHelper.floor(this.data) & '\uffff');
   }

   public byte getAsByte() {
      return (byte)(MathHelper.floor(this.data) & 255);
   }

   public double getAsDouble() {
      return (double)this.data;
   }

   public float getAsFloat() {
      return this.data;
   }

   public Number getAsNumber() {
      return this.data;
   }
}