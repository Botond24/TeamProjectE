package net.minecraft.util.math;

import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.ListNBT;

public class Rotations {
   /** Rotation on the X axis */
   protected final float x;
   /** Rotation on the Y axis */
   protected final float y;
   /** Rotation on the Z axis */
   protected final float z;

   public Rotations(float pX, float pY, float pZ) {
      this.x = !Float.isInfinite(pX) && !Float.isNaN(pX) ? pX % 360.0F : 0.0F;
      this.y = !Float.isInfinite(pY) && !Float.isNaN(pY) ? pY % 360.0F : 0.0F;
      this.z = !Float.isInfinite(pZ) && !Float.isNaN(pZ) ? pZ % 360.0F : 0.0F;
   }

   public Rotations(ListNBT p_i46010_1_) {
      this(p_i46010_1_.getFloat(0), p_i46010_1_.getFloat(1), p_i46010_1_.getFloat(2));
   }

   public ListNBT save() {
      ListNBT listnbt = new ListNBT();
      listnbt.add(FloatNBT.valueOf(this.x));
      listnbt.add(FloatNBT.valueOf(this.y));
      listnbt.add(FloatNBT.valueOf(this.z));
      return listnbt;
   }

   public boolean equals(Object p_equals_1_) {
      if (!(p_equals_1_ instanceof Rotations)) {
         return false;
      } else {
         Rotations rotations = (Rotations)p_equals_1_;
         return this.x == rotations.x && this.y == rotations.y && this.z == rotations.z;
      }
   }

   /**
    * @return the X axis rotation
    */
   public float getX() {
      return this.x;
   }

   /**
    * @return the Y axis rotation
    */
   public float getY() {
      return this.y;
   }

   /**
    * @return the Z axis rotation
    */
   public float getZ() {
      return this.z;
   }
}