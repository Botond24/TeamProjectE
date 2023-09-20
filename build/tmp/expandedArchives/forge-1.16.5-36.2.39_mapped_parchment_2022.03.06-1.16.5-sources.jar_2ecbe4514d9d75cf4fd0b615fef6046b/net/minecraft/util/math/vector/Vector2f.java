package net.minecraft.util.math.vector;

public class Vector2f {
   public static final Vector2f ZERO = new Vector2f(0.0F, 0.0F);
   public static final Vector2f ONE = new Vector2f(1.0F, 1.0F);
   public static final Vector2f UNIT_X = new Vector2f(1.0F, 0.0F);
   public static final Vector2f NEG_UNIT_X = new Vector2f(-1.0F, 0.0F);
   public static final Vector2f UNIT_Y = new Vector2f(0.0F, 1.0F);
   public static final Vector2f NEG_UNIT_Y = new Vector2f(0.0F, -1.0F);
   public static final Vector2f MAX = new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
   public static final Vector2f MIN = new Vector2f(Float.MIN_VALUE, Float.MIN_VALUE);
   public final float x;
   public final float y;

   public Vector2f(float pX, float pY) {
      this.x = pX;
      this.y = pY;
   }

   public boolean equals(Vector2f pOther) {
      return this.x == pOther.x && this.y == pOther.y;
   }
}