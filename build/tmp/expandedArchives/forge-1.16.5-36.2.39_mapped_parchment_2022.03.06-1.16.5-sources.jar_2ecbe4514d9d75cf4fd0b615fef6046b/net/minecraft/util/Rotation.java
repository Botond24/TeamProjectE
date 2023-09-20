package net.minecraft.util;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.util.math.vector.Orientation;

public enum Rotation {
   NONE(Orientation.IDENTITY),
   CLOCKWISE_90(Orientation.ROT_90_Y_NEG),
   CLOCKWISE_180(Orientation.ROT_180_FACE_XZ),
   COUNTERCLOCKWISE_90(Orientation.ROT_90_Y_POS);

   private final Orientation rotation;

   private Rotation(Orientation p_i231796_3_) {
      this.rotation = p_i231796_3_;
   }

   public Rotation getRotated(Rotation pRotation) {
      switch(pRotation) {
      case CLOCKWISE_180:
         switch(this) {
         case NONE:
            return CLOCKWISE_180;
         case CLOCKWISE_90:
            return COUNTERCLOCKWISE_90;
         case CLOCKWISE_180:
            return NONE;
         case COUNTERCLOCKWISE_90:
            return CLOCKWISE_90;
         }
      case COUNTERCLOCKWISE_90:
         switch(this) {
         case NONE:
            return COUNTERCLOCKWISE_90;
         case CLOCKWISE_90:
            return NONE;
         case CLOCKWISE_180:
            return CLOCKWISE_90;
         case COUNTERCLOCKWISE_90:
            return CLOCKWISE_180;
         }
      case CLOCKWISE_90:
         switch(this) {
         case NONE:
            return CLOCKWISE_90;
         case CLOCKWISE_90:
            return CLOCKWISE_180;
         case CLOCKWISE_180:
            return COUNTERCLOCKWISE_90;
         case COUNTERCLOCKWISE_90:
            return NONE;
         }
      default:
         return this;
      }
   }

   public Orientation rotation() {
      return this.rotation;
   }

   public Direction rotate(Direction pFacing) {
      if (pFacing.getAxis() == Direction.Axis.Y) {
         return pFacing;
      } else {
         switch(this) {
         case CLOCKWISE_90:
            return pFacing.getClockWise();
         case CLOCKWISE_180:
            return pFacing.getOpposite();
         case COUNTERCLOCKWISE_90:
            return pFacing.getCounterClockWise();
         default:
            return pFacing;
         }
      }
   }

   public int rotate(int pRotation, int pPositionCount) {
      switch(this) {
      case CLOCKWISE_90:
         return (pRotation + pPositionCount / 4) % pPositionCount;
      case CLOCKWISE_180:
         return (pRotation + pPositionCount / 2) % pPositionCount;
      case COUNTERCLOCKWISE_90:
         return (pRotation + pPositionCount * 3 / 4) % pPositionCount;
      default:
         return pRotation;
      }
   }

   /**
    * Chooses a random rotation.
    */
   public static Rotation getRandom(Random pRandom) {
      return Util.getRandom(values(), pRandom);
   }

   /**
    * Get a list of all rotations in random order.
    */
   public static List<Rotation> getShuffled(Random pRandom) {
      List<Rotation> list = Lists.newArrayList(values());
      Collections.shuffle(list, pRandom);
      return list;
   }
}