package net.minecraft.world.border;

public interface IBorderListener {
   void onBorderSizeSet(WorldBorder pBorder, double pNewSize);

   void onBorderSizeLerping(WorldBorder pBorder, double pOldSize, double pNewSize, long pTime);

   void onBorderCenterSet(WorldBorder pBorder, double pX, double pZ);

   void onBorderSetWarningTime(WorldBorder pBorder, int pNewTime);

   void onBorderSetWarningBlocks(WorldBorder pBorder, int pNewDistance);

   void onBorderSetDamagePerBlock(WorldBorder pBorder, double pNewAmount);

   void onBorderSetDamageSafeZOne(WorldBorder pBorder, double pNewSize);

   public static class Impl implements IBorderListener {
      private final WorldBorder worldBorder;

      public Impl(WorldBorder pWorldBorder) {
         this.worldBorder = pWorldBorder;
      }

      public void onBorderSizeSet(WorldBorder pBorder, double pNewSize) {
         this.worldBorder.setSize(pNewSize);
      }

      public void onBorderSizeLerping(WorldBorder pBorder, double pOldSize, double pNewSize, long pTime) {
         this.worldBorder.lerpSizeBetween(pOldSize, pNewSize, pTime);
      }

      public void onBorderCenterSet(WorldBorder pBorder, double pX, double pZ) {
         this.worldBorder.setCenter(pX, pZ);
      }

      public void onBorderSetWarningTime(WorldBorder pBorder, int pNewTime) {
         this.worldBorder.setWarningTime(pNewTime);
      }

      public void onBorderSetWarningBlocks(WorldBorder pBorder, int pNewDistance) {
         this.worldBorder.setWarningBlocks(pNewDistance);
      }

      public void onBorderSetDamagePerBlock(WorldBorder pBorder, double pNewAmount) {
         this.worldBorder.setDamagePerBlock(pNewAmount);
      }

      public void onBorderSetDamageSafeZOne(WorldBorder pBorder, double pNewSize) {
         this.worldBorder.setDamageSafeZone(pNewSize);
      }
   }
}