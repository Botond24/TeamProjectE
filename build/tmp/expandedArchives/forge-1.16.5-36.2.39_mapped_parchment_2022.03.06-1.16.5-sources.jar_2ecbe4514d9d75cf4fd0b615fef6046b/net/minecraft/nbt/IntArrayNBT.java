package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.ArrayUtils;

public class IntArrayNBT extends CollectionNBT<IntNBT> {
   public static final INBTType<IntArrayNBT> TYPE = new INBTType<IntArrayNBT>() {
      public IntArrayNBT load(DataInput pInput, int pDepth, NBTSizeTracker pAccounter) throws IOException {
         pAccounter.accountBits(192L);
         int i = pInput.readInt();
         pAccounter.accountBits(32L * (long)i);
         int[] aint = new int[i];

         for(int j = 0; j < i; ++j) {
            aint[j] = pInput.readInt();
         }

         return new IntArrayNBT(aint);
      }

      public String getName() {
         return "INT[]";
      }

      public String getPrettyName() {
         return "TAG_Int_Array";
      }
   };
   private int[] data;

   public IntArrayNBT(int[] pData) {
      this.data = pData;
   }

   public IntArrayNBT(List<Integer> pDataList) {
      this(toArray(pDataList));
   }

   private static int[] toArray(List<Integer> pDataList) {
      int[] aint = new int[pDataList.size()];

      for(int i = 0; i < pDataList.size(); ++i) {
         Integer integer = pDataList.get(i);
         aint[i] = integer == null ? 0 : integer;
      }

      return aint;
   }

   /**
    * Write the actual data contents of the tag, implemented in NBT extension classes
    */
   public void write(DataOutput pOutput) throws IOException {
      pOutput.writeInt(this.data.length);

      for(int i : this.data) {
         pOutput.writeInt(i);
      }

   }

   /**
    * Gets the type byte for the tag.
    */
   public byte getId() {
      return 11;
   }

   public INBTType<IntArrayNBT> getType() {
      return TYPE;
   }

   public String toString() {
      StringBuilder stringbuilder = new StringBuilder("[I;");

      for(int i = 0; i < this.data.length; ++i) {
         if (i != 0) {
            stringbuilder.append(',');
         }

         stringbuilder.append(this.data[i]);
      }

      return stringbuilder.append(']').toString();
   }

   /**
    * Creates a clone of the tag.
    */
   public IntArrayNBT copy() {
      int[] aint = new int[this.data.length];
      System.arraycopy(this.data, 0, aint, 0, this.data.length);
      return new IntArrayNBT(aint);
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else {
         return p_equals_1_ instanceof IntArrayNBT && Arrays.equals(this.data, ((IntArrayNBT)p_equals_1_).data);
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.data);
   }

   public int[] getAsIntArray() {
      return this.data;
   }

   public ITextComponent getPrettyDisplay(String p_199850_1_, int p_199850_2_) {
      ITextComponent itextcomponent = (new StringTextComponent("I")).withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
      IFormattableTextComponent iformattabletextcomponent = (new StringTextComponent("[")).append(itextcomponent).append(";");

      for(int i = 0; i < this.data.length; ++i) {
         iformattabletextcomponent.append(" ").append((new StringTextComponent(String.valueOf(this.data[i]))).withStyle(SYNTAX_HIGHLIGHTING_NUMBER));
         if (i != this.data.length - 1) {
            iformattabletextcomponent.append(",");
         }
      }

      iformattabletextcomponent.append("]");
      return iformattabletextcomponent;
   }

   public int size() {
      return this.data.length;
   }

   public IntNBT get(int p_get_1_) {
      return IntNBT.valueOf(this.data[p_get_1_]);
   }

   public IntNBT set(int p_set_1_, IntNBT p_set_2_) {
      int i = this.data[p_set_1_];
      this.data[p_set_1_] = p_set_2_.getAsInt();
      return IntNBT.valueOf(i);
   }

   public void add(int p_add_1_, IntNBT p_add_2_) {
      this.data = ArrayUtils.add(this.data, p_add_1_, p_add_2_.getAsInt());
   }

   public boolean setTag(int pIndex, INBT pTag) {
      if (pTag instanceof NumberNBT) {
         this.data[pIndex] = ((NumberNBT)pTag).getAsInt();
         return true;
      } else {
         return false;
      }
   }

   public boolean addTag(int pIndex, INBT pTag) {
      if (pTag instanceof NumberNBT) {
         this.data = ArrayUtils.add(this.data, pIndex, ((NumberNBT)pTag).getAsInt());
         return true;
      } else {
         return false;
      }
   }

   public IntNBT remove(int p_remove_1_) {
      int i = this.data[p_remove_1_];
      this.data = ArrayUtils.remove(this.data, p_remove_1_);
      return IntNBT.valueOf(i);
   }

   public byte getElementType() {
      return 3;
   }

   public void clear() {
      this.data = new int[0];
   }
}