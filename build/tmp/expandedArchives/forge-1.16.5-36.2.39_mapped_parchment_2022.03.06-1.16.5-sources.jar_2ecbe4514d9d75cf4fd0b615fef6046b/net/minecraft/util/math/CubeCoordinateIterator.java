package net.minecraft.util.math;

public class CubeCoordinateIterator {
   private int originX;
   private int originY;
   private int originZ;
   private int width;
   private int height;
   private int depth;
   private int end;
   private int index;
   private int x;
   private int y;
   private int z;

   public CubeCoordinateIterator(int pOriginX, int pOriginY, int pOriginZ, int p_i50798_4_, int p_i50798_5_, int p_i50798_6_) {
      this.originX = pOriginX;
      this.originY = pOriginY;
      this.originZ = pOriginZ;
      this.width = p_i50798_4_ - pOriginX + 1;
      this.height = p_i50798_5_ - pOriginY + 1;
      this.depth = p_i50798_6_ - pOriginZ + 1;
      this.end = this.width * this.height * this.depth;
   }

   public boolean advance() {
      if (this.index == this.end) {
         return false;
      } else {
         this.x = this.index % this.width;
         int i = this.index / this.width;
         this.y = i % this.height;
         this.z = i / this.height;
         ++this.index;
         return true;
      }
   }

   public int nextX() {
      return this.originX + this.x;
   }

   public int nextY() {
      return this.originY + this.y;
   }

   public int nextZ() {
      return this.originZ + this.z;
   }

   public int getNextType() {
      int i = 0;
      if (this.x == 0 || this.x == this.width - 1) {
         ++i;
      }

      if (this.y == 0 || this.y == this.height - 1) {
         ++i;
      }

      if (this.z == 0 || this.z == this.depth - 1) {
         ++i;
      }

      return i;
   }
}