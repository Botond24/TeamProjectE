package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class EndNBT implements INBT {
   public static final INBTType<EndNBT> TYPE = new INBTType<EndNBT>() {
      public EndNBT load(DataInput pInput, int pDepth, NBTSizeTracker pAccounter) {
         pAccounter.accountBits(64L);
         return EndNBT.INSTANCE;
      }

      public String getName() {
         return "END";
      }

      public String getPrettyName() {
         return "TAG_End";
      }

      public boolean isValue() {
         return true;
      }
   };
   public static final EndNBT INSTANCE = new EndNBT();

   private EndNBT() {
   }

   /**
    * Write the actual data contents of the tag, implemented in NBT extension classes
    */
   public void write(DataOutput pOutput) throws IOException {
   }

   /**
    * Gets the type byte for the tag.
    */
   public byte getId() {
      return 0;
   }

   public INBTType<EndNBT> getType() {
      return TYPE;
   }

   public String toString() {
      return "END";
   }

   /**
    * Creates a clone of the tag.
    */
   public EndNBT copy() {
      return this;
   }

   public ITextComponent getPrettyDisplay(String p_199850_1_, int p_199850_2_) {
      return StringTextComponent.EMPTY;
   }
}