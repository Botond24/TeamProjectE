package net.minecraft.util.math;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Tuple3d {
   /** The X coordinate */
   public double x;
   /** The Y coordinate */
   public double y;
   /** The Z coordinate */
   public double z;

   public Tuple3d(double pX, double pY, double pZ) {
      this.x = pX;
      this.y = pY;
      this.z = pZ;
   }
}