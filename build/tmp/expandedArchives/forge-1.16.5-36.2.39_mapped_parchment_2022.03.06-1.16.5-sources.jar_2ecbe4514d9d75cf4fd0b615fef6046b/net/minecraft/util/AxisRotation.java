package net.minecraft.util;

public enum AxisRotation {
   NONE {
      public int cycle(int pX, int pY, int pZ, Direction.Axis pAxis) {
         return pAxis.choose(pX, pY, pZ);
      }

      public Direction.Axis cycle(Direction.Axis pAxis) {
         return pAxis;
      }

      public AxisRotation inverse() {
         return this;
      }
   },
   FORWARD {
      public int cycle(int pX, int pY, int pZ, Direction.Axis pAxis) {
         return pAxis.choose(pZ, pX, pY);
      }

      public Direction.Axis cycle(Direction.Axis pAxis) {
         return AXIS_VALUES[Math.floorMod(pAxis.ordinal() + 1, 3)];
      }

      public AxisRotation inverse() {
         return BACKWARD;
      }
   },
   BACKWARD {
      public int cycle(int pX, int pY, int pZ, Direction.Axis pAxis) {
         return pAxis.choose(pY, pZ, pX);
      }

      public Direction.Axis cycle(Direction.Axis pAxis) {
         return AXIS_VALUES[Math.floorMod(pAxis.ordinal() - 1, 3)];
      }

      public AxisRotation inverse() {
         return FORWARD;
      }
   };

   public static final Direction.Axis[] AXIS_VALUES = Direction.Axis.values();
   public static final AxisRotation[] VALUES = values();

   private AxisRotation() {
   }

   public abstract int cycle(int pX, int pY, int pZ, Direction.Axis pAxis);

   public abstract Direction.Axis cycle(Direction.Axis pAxis);

   public abstract AxisRotation inverse();

   public static AxisRotation between(Direction.Axis pTo, Direction.Axis pAxis2) {
      return VALUES[Math.floorMod(pAxis2.ordinal() - pTo.ordinal(), 3)];
   }
}