package net.minecraft.entity;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public abstract class AgeableEntity extends CreatureEntity {
   private static final DataParameter<Boolean> DATA_BABY_ID = EntityDataManager.defineId(AgeableEntity.class, DataSerializers.BOOLEAN);
   protected int age;
   protected int forcedAge;
   protected int forcedAgeTimer;

   protected AgeableEntity(EntityType<? extends AgeableEntity> p_i48581_1_, World p_i48581_2_) {
      super(p_i48581_1_, p_i48581_2_);
   }

   public ILivingEntityData finalizeSpawn(IServerWorld pLevel, DifficultyInstance pDifficulty, SpawnReason pReason, @Nullable ILivingEntityData pSpawnData, @Nullable CompoundNBT pDataTag) {
      if (pSpawnData == null) {
         pSpawnData = new AgeableEntity.AgeableData(true);
      }

      AgeableEntity.AgeableData ageableentity$ageabledata = (AgeableEntity.AgeableData)pSpawnData;
      if (ageableentity$ageabledata.isShouldSpawnBaby() && ageableentity$ageabledata.getGroupSize() > 0 && this.random.nextFloat() <= ageableentity$ageabledata.getBabySpawnChance()) {
         this.setAge(-24000);
      }

      ageableentity$ageabledata.increaseGroupSizeByOne();
      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   @Nullable
   public abstract AgeableEntity getBreedOffspring(ServerWorld pServerLevel, AgeableEntity pMate);

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_BABY_ID, false);
   }

   public boolean canBreed() {
      return false;
   }

   public int getAge() {
      if (this.level.isClientSide) {
         return this.entityData.get(DATA_BABY_ID) ? -1 : 1;
      } else {
         return this.age;
      }
   }

   public void ageUp(int pGrowthSeconds, boolean pForceAging) {
      int i = this.getAge();
      i = i + pGrowthSeconds * 20;
      if (i > 0) {
         i = 0;
      }

      int j = i - i;
      this.setAge(i);
      if (pForceAging) {
         this.forcedAge += j;
         if (this.forcedAgeTimer == 0) {
            this.forcedAgeTimer = 40;
         }
      }

      if (this.getAge() == 0) {
         this.setAge(this.forcedAge);
      }

   }

   public void ageUp(int pGrowthSeconds) {
      this.ageUp(pGrowthSeconds, false);
   }

   public void setAge(int pAge) {
      int i = this.age;
      this.age = pAge;
      if (i < 0 && pAge >= 0 || i >= 0 && pAge < 0) {
         this.entityData.set(DATA_BABY_ID, pAge < 0);
         this.ageBoundaryReached();
      }

   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("Age", this.getAge());
      pCompound.putInt("ForcedAge", this.forcedAge);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setAge(pCompound.getInt("Age"));
      this.forcedAge = pCompound.getInt("ForcedAge");
   }

   public void onSyncedDataUpdated(DataParameter<?> pKey) {
      if (DATA_BABY_ID.equals(pKey)) {
         this.refreshDimensions();
      }

      super.onSyncedDataUpdated(pKey);
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      super.aiStep();
      if (this.level.isClientSide) {
         if (this.forcedAgeTimer > 0) {
            if (this.forcedAgeTimer % 4 == 0) {
               this.level.addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), 0.0D, 0.0D, 0.0D);
            }

            --this.forcedAgeTimer;
         }
      } else if (this.isAlive()) {
         int i = this.getAge();
         if (i < 0) {
            ++i;
            this.setAge(i);
         } else if (i > 0) {
            --i;
            this.setAge(i);
         }
      }

   }

   protected void ageBoundaryReached() {
   }

   /**
    * If Animal, checks if the age timer is negative
    */
   public boolean isBaby() {
      return this.getAge() < 0;
   }

   /**
    * Set whether this zombie is a child.
    */
   public void setBaby(boolean pChildZombie) {
      this.setAge(pChildZombie ? -24000 : 0);
   }

   public static class AgeableData implements ILivingEntityData {
      private int groupSize;
      private final boolean shouldSpawnBaby;
      private final float babySpawnChance;

      private AgeableData(boolean pShouldSpawnBaby, float pBabySpawnChance) {
         this.shouldSpawnBaby = pShouldSpawnBaby;
         this.babySpawnChance = pBabySpawnChance;
      }

      public AgeableData(boolean pShouldSpawnBaby) {
         this(pShouldSpawnBaby, 0.05F);
      }

      public AgeableData(float pBabySpawnChance) {
         this(true, pBabySpawnChance);
      }

      public int getGroupSize() {
         return this.groupSize;
      }

      public void increaseGroupSizeByOne() {
         ++this.groupSize;
      }

      public boolean isShouldSpawnBaby() {
         return this.shouldSpawnBaby;
      }

      public float getBabySpawnChance() {
         return this.babySpawnChance;
      }
   }
}