package net.minecraft.entity.item;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class TNTEntity extends Entity {
   private static final DataParameter<Integer> DATA_FUSE_ID = EntityDataManager.defineId(TNTEntity.class, DataSerializers.INT);
   @Nullable
   private LivingEntity owner;
   private int life = 80;

   public TNTEntity(EntityType<? extends TNTEntity> p_i50216_1_, World p_i50216_2_) {
      super(p_i50216_1_, p_i50216_2_);
      this.blocksBuilding = true;
   }

   public TNTEntity(World pLevel, double pX, double pY, double pZ, @Nullable LivingEntity pOwner) {
      this(EntityType.TNT, pLevel);
      this.setPos(pX, pY, pZ);
      double d0 = pLevel.random.nextDouble() * (double)((float)Math.PI * 2F);
      this.setDeltaMovement(-Math.sin(d0) * 0.02D, (double)0.2F, -Math.cos(d0) * 0.02D);
      this.setFuse(80);
      this.xo = pX;
      this.yo = pY;
      this.zo = pZ;
      this.owner = pOwner;
   }

   protected void defineSynchedData() {
      this.entityData.define(DATA_FUSE_ID, 80);
   }

   protected boolean isMovementNoisy() {
      return false;
   }

   /**
    * Returns true if other Entities should be prevented from moving through this Entity.
    */
   public boolean isPickable() {
      return !this.removed;
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      if (!this.isNoGravity()) {
         this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
      }

      this.move(MoverType.SELF, this.getDeltaMovement());
      this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
      if (this.onGround) {
         this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));
      }

      --this.life;
      if (this.life <= 0) {
         this.remove();
         if (!this.level.isClientSide) {
            this.explode();
         }
      } else {
         this.updateInWaterStateAndDoFluidPushing();
         if (this.level.isClientSide) {
            this.level.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5D, this.getZ(), 0.0D, 0.0D, 0.0D);
         }
      }

   }

   protected void explode() {
      float f = 4.0F;
      this.level.explode(this, this.getX(), this.getY(0.0625D), this.getZ(), 4.0F, Explosion.Mode.BREAK);
   }

   protected void addAdditionalSaveData(CompoundNBT pCompound) {
      pCompound.putShort("Fuse", (short)this.getLife());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   protected void readAdditionalSaveData(CompoundNBT pCompound) {
      this.setFuse(pCompound.getShort("Fuse"));
   }

   /**
    * Returns null or the entityliving it was ignited by
    */
   @Nullable
   public LivingEntity getOwner() {
      return this.owner;
   }

   protected float getEyeHeight(Pose pPose, EntitySize pSize) {
      return 0.15F;
   }

   public void setFuse(int pLife) {
      this.entityData.set(DATA_FUSE_ID, pLife);
      this.life = pLife;
   }

   public void onSyncedDataUpdated(DataParameter<?> pKey) {
      if (DATA_FUSE_ID.equals(pKey)) {
         this.life = this.getFuse();
      }

   }

   /**
    * Gets the fuse from the data manager
    */
   public int getFuse() {
      return this.entityData.get(DATA_FUSE_ID);
   }

   public int getLife() {
      return this.life;
   }

   public IPacket<?> getAddEntityPacket() {
      return new SSpawnObjectPacket(this);
   }
}