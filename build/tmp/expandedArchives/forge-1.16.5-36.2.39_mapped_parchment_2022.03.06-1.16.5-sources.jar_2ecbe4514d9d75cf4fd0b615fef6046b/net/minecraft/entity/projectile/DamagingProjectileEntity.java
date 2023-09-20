package net.minecraft.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class DamagingProjectileEntity extends ProjectileEntity {
   public double xPower;
   public double yPower;
   public double zPower;

   protected DamagingProjectileEntity(EntityType<? extends DamagingProjectileEntity> p_i50173_1_, World p_i50173_2_) {
      super(p_i50173_1_, p_i50173_2_);
   }

   public DamagingProjectileEntity(EntityType<? extends DamagingProjectileEntity> pEntityType, double pX, double pY, double pZ, double pOffsetX, double pOffsetY, double pOffsetZ, World pLevel) {
      this(pEntityType, pLevel);
      this.moveTo(pX, pY, pZ, this.yRot, this.xRot);
      this.reapplyPosition();
      double d0 = (double)MathHelper.sqrt(pOffsetX * pOffsetX + pOffsetY * pOffsetY + pOffsetZ * pOffsetZ);
      if (d0 != 0.0D) {
         this.xPower = pOffsetX / d0 * 0.1D;
         this.yPower = pOffsetY / d0 * 0.1D;
         this.zPower = pOffsetZ / d0 * 0.1D;
      }

   }

   public DamagingProjectileEntity(EntityType<? extends DamagingProjectileEntity> pEntityType, LivingEntity pShooter, double pOffsetX, double pOffsetY, double pOffsetZ, World pLevel) {
      this(pEntityType, pShooter.getX(), pShooter.getY(), pShooter.getZ(), pOffsetX, pOffsetY, pOffsetZ, pLevel);
      this.setOwner(pShooter);
      this.setRot(pShooter.yRot, pShooter.xRot);
   }

   protected void defineSynchedData() {
   }

   /**
    * Checks if the entity is in range to render.
    */
   @OnlyIn(Dist.CLIENT)
   public boolean shouldRenderAtSqrDistance(double pDistance) {
      double d0 = this.getBoundingBox().getSize() * 4.0D;
      if (Double.isNaN(d0)) {
         d0 = 4.0D;
      }

      d0 = d0 * 64.0D;
      return pDistance < d0 * d0;
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      Entity entity = this.getOwner();
      if (this.level.isClientSide || (entity == null || !entity.removed) && this.level.hasChunkAt(this.blockPosition())) {
         super.tick();
         if (this.shouldBurn()) {
            this.setSecondsOnFire(1);
         }

         RayTraceResult raytraceresult = ProjectileHelper.getHitResult(this, this::canHitEntity);
         if (raytraceresult.getType() != RayTraceResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
            this.onHit(raytraceresult);
         }

         this.checkInsideBlocks();
         Vector3d vector3d = this.getDeltaMovement();
         double d0 = this.getX() + vector3d.x;
         double d1 = this.getY() + vector3d.y;
         double d2 = this.getZ() + vector3d.z;
         ProjectileHelper.rotateTowardsMovement(this, 0.2F);
         float f = this.getInertia();
         if (this.isInWater()) {
            for(int i = 0; i < 4; ++i) {
               float f1 = 0.25F;
               this.level.addParticle(ParticleTypes.BUBBLE, d0 - vector3d.x * 0.25D, d1 - vector3d.y * 0.25D, d2 - vector3d.z * 0.25D, vector3d.x, vector3d.y, vector3d.z);
            }

            f = 0.8F;
         }

         this.setDeltaMovement(vector3d.add(this.xPower, this.yPower, this.zPower).scale((double)f));
         this.level.addParticle(this.getTrailParticle(), d0, d1 + 0.5D, d2, 0.0D, 0.0D, 0.0D);
         this.setPos(d0, d1, d2);
      } else {
         this.remove();
      }
   }

   protected boolean canHitEntity(Entity pTarget) {
      return super.canHitEntity(pTarget) && !pTarget.noPhysics;
   }

   protected boolean shouldBurn() {
      return true;
   }

   protected IParticleData getTrailParticle() {
      return ParticleTypes.SMOKE;
   }

   /**
    * Return the motion factor for this projectile. The factor is multiplied by the original motion.
    */
   protected float getInertia() {
      return 0.95F;
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.put("power", this.newDoubleList(new double[]{this.xPower, this.yPower, this.zPower}));
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.contains("power", 9)) {
         ListNBT listnbt = pCompound.getList("power", 6);
         if (listnbt.size() == 3) {
            this.xPower = listnbt.getDouble(0);
            this.yPower = listnbt.getDouble(1);
            this.zPower = listnbt.getDouble(2);
         }
      }

   }

   /**
    * Returns true if other Entities should be prevented from moving through this Entity.
    */
   public boolean isPickable() {
      return true;
   }

   public float getPickRadius() {
      return 1.0F;
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (this.isInvulnerableTo(pSource)) {
         return false;
      } else {
         this.markHurt();
         Entity entity = pSource.getEntity();
         if (entity != null) {
            Vector3d vector3d = entity.getLookAngle();
            this.setDeltaMovement(vector3d);
            this.xPower = vector3d.x * 0.1D;
            this.yPower = vector3d.y * 0.1D;
            this.zPower = vector3d.z * 0.1D;
            this.setOwner(entity);
            return true;
         } else {
            return false;
         }
      }
   }

   /**
    * Gets how bright this entity is.
    */
   public float getBrightness() {
      return 1.0F;
   }

   public IPacket<?> getAddEntityPacket() {
      Entity entity = this.getOwner();
      int i = entity == null ? 0 : entity.getId();
      return new SSpawnObjectPacket(this.getId(), this.getUUID(), this.getX(), this.getY(), this.getZ(), this.xRot, this.yRot, this.getType(), i, new Vector3d(this.xPower, this.yPower, this.zPower));
   }
}
