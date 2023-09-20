package net.minecraft.world;

public enum LightType {
   SKY(15),
   BLOCK(0);

   public final int surrounding;

   private LightType(int pSurrounding) {
      this.surrounding = pSurrounding;
   }
}