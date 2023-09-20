package net.minecraft.world.storage;

import net.minecraft.util.math.BlockPos;

public interface ISpawnWorldInfo extends IWorldInfo {
   /**
    * Set the x spawn position to the passed in value
    */
   void setXSpawn(int pX);

   /**
    * Sets the y spawn position
    */
   void setYSpawn(int pY);

   /**
    * Set the z spawn position to the passed in value
    */
   void setZSpawn(int pZ);

   void setSpawnAngle(float pAngle);

   default void setSpawn(BlockPos pSpawnPoint, float pAngle) {
      this.setXSpawn(pSpawnPoint.getX());
      this.setYSpawn(pSpawnPoint.getY());
      this.setZSpawn(pSpawnPoint.getZ());
      this.setSpawnAngle(pAngle);
   }
}