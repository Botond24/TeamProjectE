package net.minecraft.entity.projectile;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractArrowEntity extends ProjectileEntity {
   private static final DataParameter<Byte> ID_FLAGS = EntityDataManager.defineId(AbstractArrowEntity.class, DataSerializers.BYTE);
   private static final DataParameter<Byte> PIERCE_LEVEL = EntityDataManager.defineId(AbstractArrowEntity.class, DataSerializers.BYTE);
   @Nullable
   private BlockState lastState;
   protected boolean inGround;
   protected int inGroundTime;
   public AbstractArrowEntity.PickupStatus pickup = AbstractArrowEntity.PickupStatus.DISALLOWED;
   public int shakeTime;
   private int life;
   private double baseDamage = 2.0D;
   private int knockback;
   private SoundEvent soundEvent = this.getDefaultHitGroundSoundEvent();
   private IntOpenHashSet piercingIgnoreEntityIds;
   private List<Entity> piercedAndKilledEntities;

   protected AbstractArrowEntity(EntityType<? extends AbstractArrowEntity> p_i48546_1_, World p_i48546_2_) {
      super(p_i48546_1_, p_i48546_2_);
   }

   protected AbstractArrowEntity(EntityType<? extends AbstractArrowEntity> pEntityType, double pX, double pY, double pZ, World pLevel) {
      this(pEntityType, pLevel);
      this.setPos(pX, pY, pZ);
   }

   protected AbstractArrowEntity(EntityType<? extends AbstractArrowEntity> pEntityType, LivingEntity pShooter, World pLevel) {
      this(pEntityType, pShooter.getX(), pShooter.getEyeY() - (double)0.1F, pShooter.getZ(), pLevel);
      this.setOwner(pShooter);
      if (pShooter instanceof PlayerEntity) {
         this.pickup = AbstractArrowEntity.PickupStatus.ALLOWED;
      }

   }

   public void setSoundEvent(SoundEvent pSoundEvent) {
      this.soundEvent = pSoundEvent;
   }

   /**
    * Checks if the entity is in range to render.
    */
   @OnlyIn(Dist.CLIENT)
   public boolean shouldRenderAtSqrDistance(double pDistance) {
      double d0 = this.getBoundingBox().getSize() * 10.0D;
      if (Double.isNaN(d0)) {
         d0 = 1.0D;
      }

      d0 = d0 * 64.0D * getViewScale();
      return pDistance < d0 * d0;
   }

   protected void defineSynchedData() {
      this.entityData.define(ID_FLAGS, (byte)0);
      this.entityData.define(PIERCE_LEVEL, (byte)0);
   }

   /**
    * Similar to setArrowHeading, it's point the throwable entity to a x, y, z direction.
    */
   public void shoot(double pX, double pY, double pZ, float pVelocity, float pInaccuracy) {
      super.shoot(pX, pY, pZ, pVelocity, pInaccuracy);
      this.life = 0;
   }

   /**
    * Sets a target for the client to interpolate towards over the next few ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void lerpTo(double pX, double pY, double pZ, float pYRot, float pXRot, int pLerpSteps, boolean pTeleport) {
      this.setPos(pX, pY, pZ);
      this.setRot(pYRot, pXRot);
   }

   /**
    * Updates the entity motion clientside, called by packets from the server
    */
   @OnlyIn(Dist.CLIENT)
   public void lerpMotion(double pX, double pY, double pZ) {
      super.lerpMotion(pX, pY, pZ);
      this.life = 0;
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      boolean flag = this.isNoPhysics();
      Vector3d vector3d = this.getDeltaMovement();
      if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
         float f = MathHelper.sqrt(getHorizontalDistanceSqr(vector3d));
         this.yRot = (float)(MathHelper.atan2(vector3d.x, vector3d.z) * (double)(180F / (float)Math.PI));
         this.xRot = (float)(MathHelper.atan2(vector3d.y, (double)f) * (double)(180F / (float)Math.PI));
         this.yRotO = this.yRot;
         this.xRotO = this.xRot;
      }

      BlockPos blockpos = this.blockPosition();
      BlockState blockstate = this.level.getBlockState(blockpos);
      if (!blockstate.isAir(this.level, blockpos) && !flag) {
         VoxelShape voxelshape = blockstate.getCollisionShape(this.level, blockpos);
         if (!voxelshape.isEmpty()) {
            Vector3d vector3d1 = this.position();

            for(AxisAlignedBB axisalignedbb : voxelshape.toAabbs()) {
               if (axisalignedbb.move(blockpos).contains(vector3d1)) {
                  this.inGround = true;
                  break;
               }
            }
         }
      }

      if (this.shakeTime > 0) {
         --this.shakeTime;
      }

      if (this.isInWaterOrRain()) {
         this.clearFire();
      }

      if (this.inGround && !flag) {
         if (this.lastState != blockstate && this.shouldFall()) {
            this.startFalling();
         } else if (!this.level.isClientSide) {
            this.tickDespawn();
         }

         ++this.inGroundTime;
      } else {
         this.inGroundTime = 0;
         Vector3d vector3d2 = this.position();
         Vector3d vector3d3 = vector3d2.add(vector3d);
         RayTraceResult raytraceresult = this.level.clip(new RayTraceContext(vector3d2, vector3d3, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
         if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
            vector3d3 = raytraceresult.getLocation();
         }

         while(!this.removed) {
            EntityRayTraceResult entityraytraceresult = this.findHitEntity(vector3d2, vector3d3);
            if (entityraytraceresult != null) {
               raytraceresult = entityraytraceresult;
            }

            if (raytraceresult != null && raytraceresult.getType() == RayTraceResult.Type.ENTITY) {
               Entity entity = ((EntityRayTraceResult)raytraceresult).getEntity();
               Entity entity1 = this.getOwner();
               if (entity instanceof PlayerEntity && entity1 instanceof PlayerEntity && !((PlayerEntity)entity1).canHarmPlayer((PlayerEntity)entity)) {
                  raytraceresult = null;
                  entityraytraceresult = null;
               }
            }

            if (raytraceresult != null && raytraceresult.getType() != RayTraceResult.Type.MISS && !flag && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
               this.onHit(raytraceresult);
               this.hasImpulse = true;
            }

            if (entityraytraceresult == null || this.getPierceLevel() <= 0) {
               break;
            }

            raytraceresult = null;
         }

         vector3d = this.getDeltaMovement();
         double d3 = vector3d.x;
         double d4 = vector3d.y;
         double d0 = vector3d.z;
         if (this.isCritArrow()) {
            for(int i = 0; i < 4; ++i) {
               this.level.addParticle(ParticleTypes.CRIT, this.getX() + d3 * (double)i / 4.0D, this.getY() + d4 * (double)i / 4.0D, this.getZ() + d0 * (double)i / 4.0D, -d3, -d4 + 0.2D, -d0);
            }
         }

         double d5 = this.getX() + d3;
         double d1 = this.getY() + d4;
         double d2 = this.getZ() + d0;
         float f1 = MathHelper.sqrt(getHorizontalDistanceSqr(vector3d));
         if (flag) {
            this.yRot = (float)(MathHelper.atan2(-d3, -d0) * (double)(180F / (float)Math.PI));
         } else {
            this.yRot = (float)(MathHelper.atan2(d3, d0) * (double)(180F / (float)Math.PI));
         }

         this.xRot = (float)(MathHelper.atan2(d4, (double)f1) * (double)(180F / (float)Math.PI));
         this.xRot = lerpRotation(this.xRotO, this.xRot);
         this.yRot = lerpRotation(this.yRotO, this.yRot);
         float f2 = 0.99F;
         float f3 = 0.05F;
         if (this.isInWater()) {
            for(int j = 0; j < 4; ++j) {
               float f4 = 0.25F;
               this.level.addParticle(ParticleTypes.BUBBLE, d5 - d3 * 0.25D, d1 - d4 * 0.25D, d2 - d0 * 0.25D, d3, d4, d0);
            }

            f2 = this.getWaterInertia();
         }

         this.setDeltaMovement(vector3d.scale((double)f2));
         if (!this.isNoGravity() && !flag) {
            Vector3d vector3d4 = this.getDeltaMovement();
            this.setDeltaMovement(vector3d4.x, vector3d4.y - (double)0.05F, vector3d4.z);
         }

         this.setPos(d5, d1, d2);
         this.checkInsideBlocks();
      }
   }

   private boolean shouldFall() {
      return this.inGround && this.level.noCollision((new AxisAlignedBB(this.position(), this.position())).inflate(0.06D));
   }

   private void startFalling() {
      this.inGround = false;
      Vector3d vector3d = this.getDeltaMovement();
      this.setDeltaMovement(vector3d.multiply((double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F), (double)(this.random.nextFloat() * 0.2F)));
      this.life = 0;
   }

   public void move(MoverType pType, Vector3d pPos) {
      super.move(pType, pPos);
      if (pType != MoverType.SELF && this.shouldFall()) {
         this.startFalling();
      }

   }

   protected void tickDespawn() {
      ++this.life;
      if (this.life >= 1200) {
         this.remove();
      }

   }

   private void resetPiercedEntities() {
      if (this.piercedAndKilledEntities != null) {
         this.piercedAndKilledEntities.clear();
      }

      if (this.piercingIgnoreEntityIds != null) {
         this.piercingIgnoreEntityIds.clear();
      }

   }

   /**
    * Called when the arrow hits an entity
    */
   protected void onHitEntity(EntityRayTraceResult pResult) {
      super.onHitEntity(pResult);
      Entity entity = pResult.getEntity();
      float f = (float)this.getDeltaMovement().length();
      int i = MathHelper.ceil(MathHelper.clamp((double)f * this.baseDamage, 0.0D, 2.147483647E9D));
      if (this.getPierceLevel() > 0) {
         if (this.piercingIgnoreEntityIds == null) {
            this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
         }

         if (this.piercedAndKilledEntities == null) {
            this.piercedAndKilledEntities = Lists.newArrayListWithCapacity(5);
         }

         if (this.piercingIgnoreEntityIds.size() >= this.getPierceLevel() + 1) {
            this.remove();
            return;
         }

         this.piercingIgnoreEntityIds.add(entity.getId());
      }

      if (this.isCritArrow()) {
         long j = (long)this.random.nextInt(i / 2 + 2);
         i = (int)Math.min(j + (long)i, 2147483647L);
      }

      Entity entity1 = this.getOwner();
      DamageSource damagesource;
      if (entity1 == null) {
         damagesource = DamageSource.arrow(this, this);
      } else {
         damagesource = DamageSource.arrow(this, entity1);
         if (entity1 instanceof LivingEntity) {
            ((LivingEntity)entity1).setLastHurtMob(entity);
         }
      }

      boolean flag = entity.getType() == EntityType.ENDERMAN;
      int k = entity.getRemainingFireTicks();
      if (this.isOnFire() && !flag) {
         entity.setSecondsOnFire(5);
      }

      if (entity.hurt(damagesource, (float)i)) {
         if (flag) {
            return;
         }

         if (entity instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity)entity;
            if (!this.level.isClientSide && this.getPierceLevel() <= 0) {
               livingentity.setArrowCount(livingentity.getArrowCount() + 1);
            }

            if (this.knockback > 0) {
               Vector3d vector3d = this.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize().scale((double)this.knockback * 0.6D);
               if (vector3d.lengthSqr() > 0.0D) {
                  livingentity.push(vector3d.x, 0.1D, vector3d.z);
               }
            }

            if (!this.level.isClientSide && entity1 instanceof LivingEntity) {
               EnchantmentHelper.doPostHurtEffects(livingentity, entity1);
               EnchantmentHelper.doPostDamageEffects((LivingEntity)entity1, livingentity);
            }

            this.doPostHurtEffects(livingentity);
            if (entity1 != null && livingentity != entity1 && livingentity instanceof PlayerEntity && entity1 instanceof ServerPlayerEntity && !this.isSilent()) {
               ((ServerPlayerEntity)entity1).connection.send(new SChangeGameStatePacket(SChangeGameStatePacket.ARROW_HIT_PLAYER, 0.0F));
            }

            if (!entity.isAlive() && this.piercedAndKilledEntities != null) {
               this.piercedAndKilledEntities.add(livingentity);
            }

            if (!this.level.isClientSide && entity1 instanceof ServerPlayerEntity) {
               ServerPlayerEntity serverplayerentity = (ServerPlayerEntity)entity1;
               if (this.piercedAndKilledEntities != null && this.shotFromCrossbow()) {
                  CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayerentity, this.piercedAndKilledEntities);
               } else if (!entity.isAlive() && this.shotFromCrossbow()) {
                  CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayerentity, Arrays.asList(entity));
               }
            }
         }

         this.playSound(this.soundEvent, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
         if (this.getPierceLevel() <= 0) {
            this.remove();
         }
      } else {
         entity.setRemainingFireTicks(k);
         this.setDeltaMovement(this.getDeltaMovement().scale(-0.1D));
         this.yRot += 180.0F;
         this.yRotO += 180.0F;
         if (!this.level.isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7D) {
            if (this.pickup == AbstractArrowEntity.PickupStatus.ALLOWED) {
               this.spawnAtLocation(this.getPickupItem(), 0.1F);
            }

            this.remove();
         }
      }

   }

   protected void onHitBlock(BlockRayTraceResult pResult) {
      this.lastState = this.level.getBlockState(pResult.getBlockPos());
      super.onHitBlock(pResult);
      Vector3d vector3d = pResult.getLocation().subtract(this.getX(), this.getY(), this.getZ());
      this.setDeltaMovement(vector3d);
      Vector3d vector3d1 = vector3d.normalize().scale((double)0.05F);
      this.setPosRaw(this.getX() - vector3d1.x, this.getY() - vector3d1.y, this.getZ() - vector3d1.z);
      this.playSound(this.getHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
      this.inGround = true;
      this.shakeTime = 7;
      this.setCritArrow(false);
      this.setPierceLevel((byte)0);
      this.setSoundEvent(SoundEvents.ARROW_HIT);
      this.setShotFromCrossbow(false);
      this.resetPiercedEntities();
   }

   /**
    * The sound made when an entity is hit by this projectile
    */
   protected SoundEvent getDefaultHitGroundSoundEvent() {
      return SoundEvents.ARROW_HIT;
   }

   protected final SoundEvent getHitGroundSoundEvent() {
      return this.soundEvent;
   }

   protected void doPostHurtEffects(LivingEntity pLiving) {
   }

   /**
    * Gets the EntityHitResult representing the entity hit
    */
   @Nullable
   protected EntityRayTraceResult findHitEntity(Vector3d pStartVec, Vector3d pEndVec) {
      return ProjectileHelper.getEntityHitResult(this.level, this, pStartVec, pEndVec, this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), this::canHitEntity);
   }

   protected boolean canHitEntity(Entity pTarget) {
      return super.canHitEntity(pTarget) && (this.piercingIgnoreEntityIds == null || !this.piercingIgnoreEntityIds.contains(pTarget.getId()));
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putShort("life", (short)this.life);
      if (this.lastState != null) {
         pCompound.put("inBlockState", NBTUtil.writeBlockState(this.lastState));
      }

      pCompound.putByte("shake", (byte)this.shakeTime);
      pCompound.putBoolean("inGround", this.inGround);
      pCompound.putByte("pickup", (byte)this.pickup.ordinal());
      pCompound.putDouble("damage", this.baseDamage);
      pCompound.putBoolean("crit", this.isCritArrow());
      pCompound.putByte("PierceLevel", this.getPierceLevel());
      pCompound.putString("SoundEvent", Registry.SOUND_EVENT.getKey(this.soundEvent).toString());
      pCompound.putBoolean("ShotFromCrossbow", this.shotFromCrossbow());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.life = pCompound.getShort("life");
      if (pCompound.contains("inBlockState", 10)) {
         this.lastState = NBTUtil.readBlockState(pCompound.getCompound("inBlockState"));
      }

      this.shakeTime = pCompound.getByte("shake") & 255;
      this.inGround = pCompound.getBoolean("inGround");
      if (pCompound.contains("damage", 99)) {
         this.baseDamage = pCompound.getDouble("damage");
      }

      if (pCompound.contains("pickup", 99)) {
         this.pickup = AbstractArrowEntity.PickupStatus.byOrdinal(pCompound.getByte("pickup"));
      } else if (pCompound.contains("player", 99)) {
         this.pickup = pCompound.getBoolean("player") ? AbstractArrowEntity.PickupStatus.ALLOWED : AbstractArrowEntity.PickupStatus.DISALLOWED;
      }

      this.setCritArrow(pCompound.getBoolean("crit"));
      this.setPierceLevel(pCompound.getByte("PierceLevel"));
      if (pCompound.contains("SoundEvent", 8)) {
         this.soundEvent = Registry.SOUND_EVENT.getOptional(new ResourceLocation(pCompound.getString("SoundEvent"))).orElse(this.getDefaultHitGroundSoundEvent());
      }

      this.setShotFromCrossbow(pCompound.getBoolean("ShotFromCrossbow"));
   }

   public void setOwner(@Nullable Entity pEntity) {
      super.setOwner(pEntity);
      if (pEntity instanceof PlayerEntity) {
         this.pickup = ((PlayerEntity)pEntity).abilities.instabuild ? AbstractArrowEntity.PickupStatus.CREATIVE_ONLY : AbstractArrowEntity.PickupStatus.ALLOWED;
      }

   }

   /**
    * Called by a player entity when they collide with an entity
    */
   public void playerTouch(PlayerEntity pEntity) {
      if (!this.level.isClientSide && (this.inGround || this.isNoPhysics()) && this.shakeTime <= 0) {
         boolean flag = this.pickup == AbstractArrowEntity.PickupStatus.ALLOWED || this.pickup == AbstractArrowEntity.PickupStatus.CREATIVE_ONLY && pEntity.abilities.instabuild || this.isNoPhysics() && this.getOwner().getUUID() == pEntity.getUUID();
         if (this.pickup == AbstractArrowEntity.PickupStatus.ALLOWED && !pEntity.inventory.add(this.getPickupItem())) {
            flag = false;
         }

         if (flag) {
            pEntity.take(this, 1);
            this.remove();
         }

      }
   }

   protected abstract ItemStack getPickupItem();

   protected boolean isMovementNoisy() {
      return false;
   }

   public void setBaseDamage(double pBaseDamage) {
      this.baseDamage = pBaseDamage;
   }

   public double getBaseDamage() {
      return this.baseDamage;
   }

   /**
    * Sets the amount of knockback the arrow applies when it hits a mob.
    */
   public void setKnockback(int pKnockback) {
      this.knockback = pKnockback;
   }

   /**
    * Returns true if it's possible to attack this entity with an item.
    */
   public boolean isAttackable() {
      return false;
   }

   protected float getEyeHeight(Pose pPose, EntitySize pSize) {
      return 0.13F;
   }

   /**
    * Whether the arrow has a stream of critical hit particles flying behind it.
    */
   public void setCritArrow(boolean pCritArrow) {
      this.setFlag(1, pCritArrow);
   }

   public void setPierceLevel(byte pPierceLevel) {
      this.entityData.set(PIERCE_LEVEL, pPierceLevel);
   }

   private void setFlag(int pId, boolean pValue) {
      byte b0 = this.entityData.get(ID_FLAGS);
      if (pValue) {
         this.entityData.set(ID_FLAGS, (byte)(b0 | pId));
      } else {
         this.entityData.set(ID_FLAGS, (byte)(b0 & ~pId));
      }

   }

   /**
    * Whether the arrow has a stream of critical hit particles flying behind it.
    */
   public boolean isCritArrow() {
      byte b0 = this.entityData.get(ID_FLAGS);
      return (b0 & 1) != 0;
   }

   /**
    * Whether the arrow was shot from a crossbow.
    */
   public boolean shotFromCrossbow() {
      byte b0 = this.entityData.get(ID_FLAGS);
      return (b0 & 4) != 0;
   }

   public byte getPierceLevel() {
      return this.entityData.get(PIERCE_LEVEL);
   }

   public void setEnchantmentEffectsFromEntity(LivingEntity pShooter, float pVelocity) {
      int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER_ARROWS, pShooter);
      int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH_ARROWS, pShooter);
      this.setBaseDamage((double)(pVelocity * 2.0F) + this.random.nextGaussian() * 0.25D + (double)((float)this.level.getDifficulty().getId() * 0.11F));
      if (i > 0) {
         this.setBaseDamage(this.getBaseDamage() + (double)i * 0.5D + 0.5D);
      }

      if (j > 0) {
         this.setKnockback(j);
      }

      if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAMING_ARROWS, pShooter) > 0) {
         this.setSecondsOnFire(100);
      }

   }

   protected float getWaterInertia() {
      return 0.6F;
   }

   /**
    * Sets if this arrow can noClip
    */
   public void setNoPhysics(boolean pNoPhysics) {
      this.noPhysics = pNoPhysics;
      this.setFlag(2, pNoPhysics);
   }

   /**
    * Whether the arrow can noClip
    */
   public boolean isNoPhysics() {
      if (!this.level.isClientSide) {
         return this.noPhysics;
      } else {
         return (this.entityData.get(ID_FLAGS) & 2) != 0;
      }
   }

   /**
    * Sets data about if this arrow entity was shot from a crossbow
    */
   public void setShotFromCrossbow(boolean pShotFromCrossbow) {
      this.setFlag(4, pShotFromCrossbow);
   }

   public IPacket<?> getAddEntityPacket() {
      Entity entity = this.getOwner();
      return new SSpawnObjectPacket(this, entity == null ? 0 : entity.getId());
   }

   public static enum PickupStatus {
      DISALLOWED,
      ALLOWED,
      CREATIVE_ONLY;

      public static AbstractArrowEntity.PickupStatus byOrdinal(int pOrdinal) {
         if (pOrdinal < 0 || pOrdinal > values().length) {
            pOrdinal = 0;
         }

         return values()[pOrdinal];
      }
   }
}