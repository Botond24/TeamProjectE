package net.minecraft.dispenser;

public class Position implements IPosition {
   protected final double x;
   protected final double y;
   protected final double z;

   public Position(double pX, double pY, double pZ) {
      this.x = pX;
      this.y = pY;
      this.z = pZ;
   }

   public double x() {
      return this.x;
   }

   public double y() {
      return this.y;
   }

   public double z() {
      return this.z;
   }
}