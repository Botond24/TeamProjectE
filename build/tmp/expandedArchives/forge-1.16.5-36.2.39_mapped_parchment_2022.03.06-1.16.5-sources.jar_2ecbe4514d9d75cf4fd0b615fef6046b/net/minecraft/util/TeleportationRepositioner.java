package net.minecraft.util;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;
import java.util.function.Predicate;
import net.minecraft.util.math.BlockPos;

public class TeleportationRepositioner {
   /**
    * Finds the rectangle with the largest area containing centerPos within the blocks specified by the predicate
    */
   public static TeleportationRepositioner.Result getLargestRectangleAround(BlockPos pCenterPos, Direction.Axis pAxis1, int pMax1, Direction.Axis pAxis2, int pMax2, Predicate<BlockPos> pPosPredicate) {
      BlockPos.Mutable blockpos$mutable = pCenterPos.mutable();
      Direction direction = Direction.get(Direction.AxisDirection.NEGATIVE, pAxis1);
      Direction direction1 = direction.getOpposite();
      Direction direction2 = Direction.get(Direction.AxisDirection.NEGATIVE, pAxis2);
      Direction direction3 = direction2.getOpposite();
      int i = getLimit(pPosPredicate, blockpos$mutable.set(pCenterPos), direction, pMax1);
      int j = getLimit(pPosPredicate, blockpos$mutable.set(pCenterPos), direction1, pMax1);
      int k = i;
      TeleportationRepositioner.IntBounds[] ateleportationrepositioner$intbounds = new TeleportationRepositioner.IntBounds[i + 1 + j];
      ateleportationrepositioner$intbounds[i] = new TeleportationRepositioner.IntBounds(getLimit(pPosPredicate, blockpos$mutable.set(pCenterPos), direction2, pMax2), getLimit(pPosPredicate, blockpos$mutable.set(pCenterPos), direction3, pMax2));
      int l = ateleportationrepositioner$intbounds[i].min;

      for(int i1 = 1; i1 <= i; ++i1) {
         TeleportationRepositioner.IntBounds teleportationrepositioner$intbounds = ateleportationrepositioner$intbounds[k - (i1 - 1)];
         ateleportationrepositioner$intbounds[k - i1] = new TeleportationRepositioner.IntBounds(getLimit(pPosPredicate, blockpos$mutable.set(pCenterPos).move(direction, i1), direction2, teleportationrepositioner$intbounds.min), getLimit(pPosPredicate, blockpos$mutable.set(pCenterPos).move(direction, i1), direction3, teleportationrepositioner$intbounds.max));
      }

      for(int l2 = 1; l2 <= j; ++l2) {
         TeleportationRepositioner.IntBounds teleportationrepositioner$intbounds2 = ateleportationrepositioner$intbounds[k + l2 - 1];
         ateleportationrepositioner$intbounds[k + l2] = new TeleportationRepositioner.IntBounds(getLimit(pPosPredicate, blockpos$mutable.set(pCenterPos).move(direction1, l2), direction2, teleportationrepositioner$intbounds2.min), getLimit(pPosPredicate, blockpos$mutable.set(pCenterPos).move(direction1, l2), direction3, teleportationrepositioner$intbounds2.max));
      }

      int i3 = 0;
      int j3 = 0;
      int j1 = 0;
      int k1 = 0;
      int[] aint = new int[ateleportationrepositioner$intbounds.length];

      for(int l1 = l; l1 >= 0; --l1) {
         for(int i2 = 0; i2 < ateleportationrepositioner$intbounds.length; ++i2) {
            TeleportationRepositioner.IntBounds teleportationrepositioner$intbounds1 = ateleportationrepositioner$intbounds[i2];
            int j2 = l - teleportationrepositioner$intbounds1.min;
            int k2 = l + teleportationrepositioner$intbounds1.max;
            aint[i2] = l1 >= j2 && l1 <= k2 ? k2 + 1 - l1 : 0;
         }

         Pair<TeleportationRepositioner.IntBounds, Integer> pair = getMaxRectangleLocation(aint);
         TeleportationRepositioner.IntBounds teleportationrepositioner$intbounds3 = pair.getFirst();
         int k3 = 1 + teleportationrepositioner$intbounds3.max - teleportationrepositioner$intbounds3.min;
         int l3 = pair.getSecond();
         if (k3 * l3 > j1 * k1) {
            i3 = teleportationrepositioner$intbounds3.min;
            j3 = l1;
            j1 = k3;
            k1 = l3;
         }
      }

      return new TeleportationRepositioner.Result(pCenterPos.relative(pAxis1, i3 - k).relative(pAxis2, j3 - l), j1, k1);
   }

   /**
    * Finds the distance we can travel in the given direction while the predicate returns true
    */
   private static int getLimit(Predicate<BlockPos> pPosPredicate, BlockPos.Mutable pCenterPos, Direction pDirection, int pMax) {
      int i;
      for(i = 0; i < pMax && pPosPredicate.test(pCenterPos.move(pDirection)); ++i) {
      }

      return i;
   }

   /**
    * Finds the largest rectangle within the array of heights
    */
   @VisibleForTesting
   static Pair<TeleportationRepositioner.IntBounds, Integer> getMaxRectangleLocation(int[] pHeights) {
      int i = 0;
      int j = 0;
      int k = 0;
      IntStack intstack = new IntArrayList();
      intstack.push(0);

      for(int l = 1; l <= pHeights.length; ++l) {
         int i1 = l == pHeights.length ? 0 : pHeights[l];

         while(!intstack.isEmpty()) {
            int j1 = pHeights[intstack.topInt()];
            if (i1 >= j1) {
               intstack.push(l);
               break;
            }

            intstack.popInt();
            int k1 = intstack.isEmpty() ? 0 : intstack.topInt() + 1;
            if (j1 * (l - k1) > k * (j - i)) {
               j = l;
               i = k1;
               k = j1;
            }
         }

         if (intstack.isEmpty()) {
            intstack.push(l);
         }
      }

      return new Pair<>(new TeleportationRepositioner.IntBounds(i, j - 1), k);
   }

   public static class IntBounds {
      /** The minimum bound */
      public final int min;
      /** The maximum bound */
      public final int max;

      public IntBounds(int pMin, int pMax) {
         this.min = pMin;
         this.max = pMax;
      }

      public String toString() {
         return "IntBounds{min=" + this.min + ", max=" + this.max + '}';
      }
   }

   public static class Result {
      /** Starting position of the rectangle represented by this result */
      public final BlockPos minCorner;
      /** Distance between minimum and maximum values on the first axis argument */
      public final int axis1Size;
      /** Distance between minimum and maximum values on the second axis argument */
      public final int axis2Size;

      public Result(BlockPos pMinCorner, int pAxis1Size, int pAxis2Size) {
         this.minCorner = pMinCorner;
         this.axis1Size = pAxis1Size;
         this.axis2Size = pAxis2Size;
      }
   }
}