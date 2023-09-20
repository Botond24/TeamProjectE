package net.minecraft.pathfinding;

public class PathHeap {
   private PathPoint[] heap = new PathPoint[128];
   private int size;

   /**
    * Adds a point to the path
    */
   public PathPoint insert(PathPoint pPoint) {
      if (pPoint.heapIdx >= 0) {
         throw new IllegalStateException("OW KNOWS!");
      } else {
         if (this.size == this.heap.length) {
            PathPoint[] apathpoint = new PathPoint[this.size << 1];
            System.arraycopy(this.heap, 0, apathpoint, 0, this.size);
            this.heap = apathpoint;
         }

         this.heap[this.size] = pPoint;
         pPoint.heapIdx = this.size;
         this.upHeap(this.size++);
         return pPoint;
      }
   }

   /**
    * Clears the path
    */
   public void clear() {
      this.size = 0;
   }

   /**
    * Returns and removes the first point in the path
    */
   public PathPoint pop() {
      PathPoint pathpoint = this.heap[0];
      this.heap[0] = this.heap[--this.size];
      this.heap[this.size] = null;
      if (this.size > 0) {
         this.downHeap(0);
      }

      pathpoint.heapIdx = -1;
      return pathpoint;
   }

   /**
    * Changes the provided point's total cost if costIn is smaller
    */
   public void changeCost(PathPoint pPoint, float pCost) {
      float f = pPoint.f;
      pPoint.f = pCost;
      if (pCost < f) {
         this.upHeap(pPoint.heapIdx);
      } else {
         this.downHeap(pPoint.heapIdx);
      }

   }

   /**
    * Sorts a point to the left
    */
   private void upHeap(int pIndex) {
      PathPoint pathpoint = this.heap[pIndex];

      int i;
      for(float f = pathpoint.f; pIndex > 0; pIndex = i) {
         i = pIndex - 1 >> 1;
         PathPoint pathpoint1 = this.heap[i];
         if (!(f < pathpoint1.f)) {
            break;
         }

         this.heap[pIndex] = pathpoint1;
         pathpoint1.heapIdx = pIndex;
      }

      this.heap[pIndex] = pathpoint;
      pathpoint.heapIdx = pIndex;
   }

   /**
    * Sorts a point to the right
    */
   private void downHeap(int pIndex) {
      PathPoint pathpoint = this.heap[pIndex];
      float f = pathpoint.f;

      while(true) {
         int i = 1 + (pIndex << 1);
         int j = i + 1;
         if (i >= this.size) {
            break;
         }

         PathPoint pathpoint1 = this.heap[i];
         float f1 = pathpoint1.f;
         PathPoint pathpoint2;
         float f2;
         if (j >= this.size) {
            pathpoint2 = null;
            f2 = Float.POSITIVE_INFINITY;
         } else {
            pathpoint2 = this.heap[j];
            f2 = pathpoint2.f;
         }

         if (f1 < f2) {
            if (!(f1 < f)) {
               break;
            }

            this.heap[pIndex] = pathpoint1;
            pathpoint1.heapIdx = pIndex;
            pIndex = i;
         } else {
            if (!(f2 < f)) {
               break;
            }

            this.heap[pIndex] = pathpoint2;
            pathpoint2.heapIdx = pIndex;
            pIndex = j;
         }
      }

      this.heap[pIndex] = pathpoint;
      pathpoint.heapIdx = pIndex;
   }

   /**
    * Returns true if this path contains no points
    */
   public boolean isEmpty() {
      return this.size == 0;
   }
}