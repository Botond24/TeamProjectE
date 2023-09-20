package net.minecraft.util.math;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;

public abstract class RayTraceResult {
   protected final Vector3d location;
   /** Used to determine what sub-segment is hit */
   public int subHit = -1;

   /** Used to add extra hit info */
   public Object hitInfo = null;

   protected RayTraceResult(Vector3d pLocation) {
      this.location = pLocation;
   }

   public double distanceTo(Entity pEntity) {
      double d0 = this.location.x - pEntity.getX();
      double d1 = this.location.y - pEntity.getY();
      double d2 = this.location.z - pEntity.getZ();
      return d0 * d0 + d1 * d1 + d2 * d2;
   }

   public abstract RayTraceResult.Type getType();

   /**
    * Returns the hit position of the raycast, in absolute world coordinates
    */
   public Vector3d getLocation() {
      return this.location;
   }

   public static enum Type {
      MISS,
      BLOCK,
      ENTITY;
   }
}
