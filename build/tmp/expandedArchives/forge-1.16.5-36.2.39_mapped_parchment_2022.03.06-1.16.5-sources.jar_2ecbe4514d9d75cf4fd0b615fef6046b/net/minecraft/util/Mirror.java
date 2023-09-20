package net.minecraft.util;

import net.minecraft.util.math.vector.Orientation;

public enum Mirror {
   NONE(Orientation.IDENTITY),
   LEFT_RIGHT(Orientation.INVERT_Z),
   FRONT_BACK(Orientation.INVERT_X);

   private final Orientation rotation;

   private Mirror(Orientation p_i241181_3_) {
      this.rotation = p_i241181_3_;
   }

   /**
    * Mirrors the given rotation like specified by this mirror. Rotations start at 0 and go up to rotationCount-1. 0 is
    * front, rotationCount/2 is back.
    */
   public int mirror(int pRotation, int pRotationCount) {
      int i = pRotationCount / 2;
      int j = pRotation > i ? pRotation - pRotationCount : pRotation;
      switch(this) {
      case FRONT_BACK:
         return (pRotationCount - j) % pRotationCount;
      case LEFT_RIGHT:
         return (i - j + pRotationCount) % pRotationCount;
      default:
         return pRotation;
      }
   }

   /**
    * Determines the rotation that is equivalent to this mirror if the rotating object faces in the given direction
    */
   public Rotation getRotation(Direction pFacing) {
      Direction.Axis direction$axis = pFacing.getAxis();
      return (this != LEFT_RIGHT || direction$axis != Direction.Axis.Z) && (this != FRONT_BACK || direction$axis != Direction.Axis.X) ? Rotation.NONE : Rotation.CLOCKWISE_180;
   }

   /**
    * Mirror the given facing according to this mirror
    */
   public Direction mirror(Direction pFacing) {
      if (this == FRONT_BACK && pFacing.getAxis() == Direction.Axis.X) {
         return pFacing.getOpposite();
      } else {
         return this == LEFT_RIGHT && pFacing.getAxis() == Direction.Axis.Z ? pFacing.getOpposite() : pFacing;
      }
   }

   public Orientation rotation() {
      return this.rotation;
   }
}