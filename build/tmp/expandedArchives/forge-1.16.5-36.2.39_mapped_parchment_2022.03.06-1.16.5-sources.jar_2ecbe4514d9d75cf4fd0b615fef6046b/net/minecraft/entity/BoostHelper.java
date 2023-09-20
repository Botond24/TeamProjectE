package net.minecraft.entity;

import java.util.Random;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;

public class BoostHelper {
   private final EntityDataManager entityData;
   private final DataParameter<Integer> boostTimeAccessor;
   private final DataParameter<Boolean> hasSaddleAccessor;
   public boolean boosting;
   public int boostTime;
   public int boostTimeTotal;

   public BoostHelper(EntityDataManager pEntityData, DataParameter<Integer> pBoostTimeAccessor, DataParameter<Boolean> pHasSaddleAccessor) {
      this.entityData = pEntityData;
      this.boostTimeAccessor = pBoostTimeAccessor;
      this.hasSaddleAccessor = pHasSaddleAccessor;
   }

   public void onSynced() {
      this.boosting = true;
      this.boostTime = 0;
      this.boostTimeTotal = this.entityData.get(this.boostTimeAccessor);
   }

   public boolean boost(Random pRandom) {
      if (this.boosting) {
         return false;
      } else {
         this.boosting = true;
         this.boostTime = 0;
         this.boostTimeTotal = pRandom.nextInt(841) + 140;
         this.entityData.set(this.boostTimeAccessor, this.boostTimeTotal);
         return true;
      }
   }

   public void addAdditionalSaveData(CompoundNBT pNbt) {
      pNbt.putBoolean("Saddle", this.hasSaddle());
   }

   public void readAdditionalSaveData(CompoundNBT pNbt) {
      this.setSaddle(pNbt.getBoolean("Saddle"));
   }

   public void setSaddle(boolean pSaddled) {
      this.entityData.set(this.hasSaddleAccessor, pSaddled);
   }

   public boolean hasSaddle() {
      return this.entityData.get(this.hasSaddleAccessor);
   }
}