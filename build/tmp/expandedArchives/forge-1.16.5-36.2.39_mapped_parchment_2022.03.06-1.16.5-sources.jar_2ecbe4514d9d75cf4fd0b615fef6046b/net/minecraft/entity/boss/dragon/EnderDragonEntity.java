package net.minecraft.entity.boss.dragon;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.boss.dragon.phase.IPhase;
import net.minecraft.entity.boss.dragon.phase.PhaseManager;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathHeap;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.potion.EffectInstance;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.end.DragonFightManager;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.EndPodiumFeature;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EnderDragonEntity extends MobEntity implements IMob {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final DataParameter<Integer> DATA_PHASE = EntityDataManager.defineId(EnderDragonEntity.class, DataSerializers.INT);
   private static final EntityPredicate CRYSTAL_DESTROY_TARGETING = (new EntityPredicate()).range(64.0D);
   public final double[][] positions = new double[64][3];
   public int posPointer = -1;
   private final EnderDragonPartEntity[] subEntities;
   public final EnderDragonPartEntity head;
   private final EnderDragonPartEntity neck;
   private final EnderDragonPartEntity body;
   private final EnderDragonPartEntity tail1;
   private final EnderDragonPartEntity tail2;
   private final EnderDragonPartEntity tail3;
   private final EnderDragonPartEntity wing1;
   private final EnderDragonPartEntity wing2;
   public float oFlapTime;
   public float flapTime;
   public boolean inWall;
   public int dragonDeathTime;
   public float yRotA;
   @Nullable
   public EnderCrystalEntity nearestCrystal;
   @Nullable
   private final DragonFightManager dragonFight;
   private final PhaseManager phaseManager;
   private int growlTime = 100;
   private int sittingDamageReceived;
   private final PathPoint[] nodes = new PathPoint[24];
   private final int[] nodeAdjacency = new int[24];
   private final PathHeap openSet = new PathHeap();

   public EnderDragonEntity(EntityType<? extends EnderDragonEntity> p_i50230_1_, World p_i50230_2_) {
      super(EntityType.ENDER_DRAGON, p_i50230_2_);
      this.head = new EnderDragonPartEntity(this, "head", 1.0F, 1.0F);
      this.neck = new EnderDragonPartEntity(this, "neck", 3.0F, 3.0F);
      this.body = new EnderDragonPartEntity(this, "body", 5.0F, 3.0F);
      this.tail1 = new EnderDragonPartEntity(this, "tail", 2.0F, 2.0F);
      this.tail2 = new EnderDragonPartEntity(this, "tail", 2.0F, 2.0F);
      this.tail3 = new EnderDragonPartEntity(this, "tail", 2.0F, 2.0F);
      this.wing1 = new EnderDragonPartEntity(this, "wing", 4.0F, 2.0F);
      this.wing2 = new EnderDragonPartEntity(this, "wing", 4.0F, 2.0F);
      this.subEntities = new EnderDragonPartEntity[]{this.head, this.neck, this.body, this.tail1, this.tail2, this.tail3, this.wing1, this.wing2};
      this.setHealth(this.getMaxHealth());
      this.noPhysics = true;
      this.noCulling = true;
      if (p_i50230_2_ instanceof ServerWorld) {
         this.dragonFight = ((ServerWorld)p_i50230_2_).dragonFight();
      } else {
         this.dragonFight = null;
      }

      this.phaseManager = new PhaseManager(this);
      this.setId(ENTITY_COUNTER.getAndAdd(this.subEntities.length + 1) + 1); // Forge: Fix MC-158205: Make sure part ids are successors of parent mob id
   }

   @Override
   public void setId(int pId) {
      super.setId(pId);
      for(int i = 0; i < this.subEntities.length; ++i) // Forge: Fix MC-158205: Set part ids to successors of parent mob id
         this.subEntities[i].setId(pId + i + 1);
   }

   public static AttributeModifierMap.MutableAttribute createAttributes() {
      return MobEntity.createMobAttributes().add(Attributes.MAX_HEALTH, 200.0D);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.getEntityData().define(DATA_PHASE, PhaseType.HOVERING.getId());
   }

   /**
    * Returns a double[3] array with movement offsets, used to calculate trailing tail/neck positions. [0] = yaw offset,
    * [1] = y offset, [2] = unused, always 0. Parameters: buffer index offset, partial ticks.
    */
   public double[] getLatencyPos(int pBufferIndexOffset, float pPartialTicks) {
      if (this.isDeadOrDying()) {
         pPartialTicks = 0.0F;
      }

      pPartialTicks = 1.0F - pPartialTicks;
      int i = this.posPointer - pBufferIndexOffset & 63;
      int j = this.posPointer - pBufferIndexOffset - 1 & 63;
      double[] adouble = new double[3];
      double d0 = this.positions[i][0];
      double d1 = MathHelper.wrapDegrees(this.positions[j][0] - d0);
      adouble[0] = d0 + d1 * (double)pPartialTicks;
      d0 = this.positions[i][1];
      d1 = this.positions[j][1] - d0;
      adouble[1] = d0 + d1 * (double)pPartialTicks;
      adouble[2] = MathHelper.lerp((double)pPartialTicks, this.positions[i][2], this.positions[j][2]);
      return adouble;
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      if (this.level.isClientSide) {
         this.setHealth(this.getHealth());
         if (!this.isSilent()) {
            float f = MathHelper.cos(this.flapTime * ((float)Math.PI * 2F));
            float f1 = MathHelper.cos(this.oFlapTime * ((float)Math.PI * 2F));
            if (f1 <= -0.3F && f >= -0.3F) {
               this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENDER_DRAGON_FLAP, this.getSoundSource(), 5.0F, 0.8F + this.random.nextFloat() * 0.3F, false);
            }

            if (!this.phaseManager.getCurrentPhase().isSitting() && --this.growlTime < 0) {
               this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENDER_DRAGON_GROWL, this.getSoundSource(), 2.5F, 0.8F + this.random.nextFloat() * 0.3F, false);
               this.growlTime = 200 + this.random.nextInt(200);
            }
         }
      }

      this.oFlapTime = this.flapTime;
      if (this.isDeadOrDying()) {
         float f11 = (this.random.nextFloat() - 0.5F) * 8.0F;
         float f13 = (this.random.nextFloat() - 0.5F) * 4.0F;
         float f14 = (this.random.nextFloat() - 0.5F) * 8.0F;
         this.level.addParticle(ParticleTypes.EXPLOSION, this.getX() + (double)f11, this.getY() + 2.0D + (double)f13, this.getZ() + (double)f14, 0.0D, 0.0D, 0.0D);
      } else {
         this.checkCrystals();
         Vector3d vector3d4 = this.getDeltaMovement();
         float f12 = 0.2F / (MathHelper.sqrt(getHorizontalDistanceSqr(vector3d4)) * 10.0F + 1.0F);
         f12 = f12 * (float)Math.pow(2.0D, vector3d4.y);
         if (this.phaseManager.getCurrentPhase().isSitting()) {
            this.flapTime += 0.1F;
         } else if (this.inWall) {
            this.flapTime += f12 * 0.5F;
         } else {
            this.flapTime += f12;
         }

         this.yRot = MathHelper.wrapDegrees(this.yRot);
         if (this.isNoAi()) {
            this.flapTime = 0.5F;
         } else {
            if (this.posPointer < 0) {
               for(int i = 0; i < this.positions.length; ++i) {
                  this.positions[i][0] = (double)this.yRot;
                  this.positions[i][1] = this.getY();
               }
            }

            if (++this.posPointer == this.positions.length) {
               this.posPointer = 0;
            }

            this.positions[this.posPointer][0] = (double)this.yRot;
            this.positions[this.posPointer][1] = this.getY();
            if (this.level.isClientSide) {
               if (this.lerpSteps > 0) {
                  double d7 = this.getX() + (this.lerpX - this.getX()) / (double)this.lerpSteps;
                  double d0 = this.getY() + (this.lerpY - this.getY()) / (double)this.lerpSteps;
                  double d1 = this.getZ() + (this.lerpZ - this.getZ()) / (double)this.lerpSteps;
                  double d2 = MathHelper.wrapDegrees(this.lerpYRot - (double)this.yRot);
                  this.yRot = (float)((double)this.yRot + d2 / (double)this.lerpSteps);
                  this.xRot = (float)((double)this.xRot + (this.lerpXRot - (double)this.xRot) / (double)this.lerpSteps);
                  --this.lerpSteps;
                  this.setPos(d7, d0, d1);
                  this.setRot(this.yRot, this.xRot);
               }

               this.phaseManager.getCurrentPhase().doClientTick();
            } else {
               IPhase iphase = this.phaseManager.getCurrentPhase();
               iphase.doServerTick();
               if (this.phaseManager.getCurrentPhase() != iphase) {
                  iphase = this.phaseManager.getCurrentPhase();
                  iphase.doServerTick();
               }

               Vector3d vector3d = iphase.getFlyTargetLocation();
               if (vector3d != null) {
                  double d8 = vector3d.x - this.getX();
                  double d9 = vector3d.y - this.getY();
                  double d10 = vector3d.z - this.getZ();
                  double d3 = d8 * d8 + d9 * d9 + d10 * d10;
                  float f6 = iphase.getFlySpeed();
                  double d4 = (double)MathHelper.sqrt(d8 * d8 + d10 * d10);
                  if (d4 > 0.0D) {
                     d9 = MathHelper.clamp(d9 / d4, (double)(-f6), (double)f6);
                  }

                  this.setDeltaMovement(this.getDeltaMovement().add(0.0D, d9 * 0.01D, 0.0D));
                  this.yRot = MathHelper.wrapDegrees(this.yRot);
                  double d5 = MathHelper.clamp(MathHelper.wrapDegrees(180.0D - MathHelper.atan2(d8, d10) * (double)(180F / (float)Math.PI) - (double)this.yRot), -50.0D, 50.0D);
                  Vector3d vector3d1 = vector3d.subtract(this.getX(), this.getY(), this.getZ()).normalize();
                  Vector3d vector3d2 = (new Vector3d((double)MathHelper.sin(this.yRot * ((float)Math.PI / 180F)), this.getDeltaMovement().y, (double)(-MathHelper.cos(this.yRot * ((float)Math.PI / 180F))))).normalize();
                  float f8 = Math.max(((float)vector3d2.dot(vector3d1) + 0.5F) / 1.5F, 0.0F);
                  this.yRotA *= 0.8F;
                  this.yRotA = (float)((double)this.yRotA + d5 * (double)iphase.getTurnSpeed());
                  this.yRot += this.yRotA * 0.1F;
                  float f9 = (float)(2.0D / (d3 + 1.0D));
                  float f10 = 0.06F;
                  this.moveRelative(0.06F * (f8 * f9 + (1.0F - f9)), new Vector3d(0.0D, 0.0D, -1.0D));
                  if (this.inWall) {
                     this.move(MoverType.SELF, this.getDeltaMovement().scale((double)0.8F));
                  } else {
                     this.move(MoverType.SELF, this.getDeltaMovement());
                  }

                  Vector3d vector3d3 = this.getDeltaMovement().normalize();
                  double d6 = 0.8D + 0.15D * (vector3d3.dot(vector3d2) + 1.0D) / 2.0D;
                  this.setDeltaMovement(this.getDeltaMovement().multiply(d6, (double)0.91F, d6));
               }
            }

            this.yBodyRot = this.yRot;
            Vector3d[] avector3d = new Vector3d[this.subEntities.length];

            for(int j = 0; j < this.subEntities.length; ++j) {
               avector3d[j] = new Vector3d(this.subEntities[j].getX(), this.subEntities[j].getY(), this.subEntities[j].getZ());
            }

            float f15 = (float)(this.getLatencyPos(5, 1.0F)[1] - this.getLatencyPos(10, 1.0F)[1]) * 10.0F * ((float)Math.PI / 180F);
            float f16 = MathHelper.cos(f15);
            float f2 = MathHelper.sin(f15);
            float f17 = this.yRot * ((float)Math.PI / 180F);
            float f3 = MathHelper.sin(f17);
            float f18 = MathHelper.cos(f17);
            this.tickPart(this.body, (double)(f3 * 0.5F), 0.0D, (double)(-f18 * 0.5F));
            this.tickPart(this.wing1, (double)(f18 * 4.5F), 2.0D, (double)(f3 * 4.5F));
            this.tickPart(this.wing2, (double)(f18 * -4.5F), 2.0D, (double)(f3 * -4.5F));
            if (!this.level.isClientSide && this.hurtTime == 0) {
               this.knockBack(this.level.getEntities(this, this.wing1.getBoundingBox().inflate(4.0D, 2.0D, 4.0D).move(0.0D, -2.0D, 0.0D), EntityPredicates.NO_CREATIVE_OR_SPECTATOR));
               this.knockBack(this.level.getEntities(this, this.wing2.getBoundingBox().inflate(4.0D, 2.0D, 4.0D).move(0.0D, -2.0D, 0.0D), EntityPredicates.NO_CREATIVE_OR_SPECTATOR));
               this.hurt(this.level.getEntities(this, this.head.getBoundingBox().inflate(1.0D), EntityPredicates.NO_CREATIVE_OR_SPECTATOR));
               this.hurt(this.level.getEntities(this, this.neck.getBoundingBox().inflate(1.0D), EntityPredicates.NO_CREATIVE_OR_SPECTATOR));
            }

            float f4 = MathHelper.sin(this.yRot * ((float)Math.PI / 180F) - this.yRotA * 0.01F);
            float f19 = MathHelper.cos(this.yRot * ((float)Math.PI / 180F) - this.yRotA * 0.01F);
            float f5 = this.getHeadYOffset();
            this.tickPart(this.head, (double)(f4 * 6.5F * f16), (double)(f5 + f2 * 6.5F), (double)(-f19 * 6.5F * f16));
            this.tickPart(this.neck, (double)(f4 * 5.5F * f16), (double)(f5 + f2 * 5.5F), (double)(-f19 * 5.5F * f16));
            double[] adouble = this.getLatencyPos(5, 1.0F);

            for(int k = 0; k < 3; ++k) {
               EnderDragonPartEntity enderdragonpartentity = null;
               if (k == 0) {
                  enderdragonpartentity = this.tail1;
               }

               if (k == 1) {
                  enderdragonpartentity = this.tail2;
               }

               if (k == 2) {
                  enderdragonpartentity = this.tail3;
               }

               double[] adouble1 = this.getLatencyPos(12 + k * 2, 1.0F);
               float f7 = this.yRot * ((float)Math.PI / 180F) + this.rotWrap(adouble1[0] - adouble[0]) * ((float)Math.PI / 180F);
               float f20 = MathHelper.sin(f7);
               float f21 = MathHelper.cos(f7);
               float f22 = 1.5F;
               float f23 = (float)(k + 1) * 2.0F;
               this.tickPart(enderdragonpartentity, (double)(-(f3 * 1.5F + f20 * f23) * f16), adouble1[1] - adouble[1] - (double)((f23 + 1.5F) * f2) + 1.5D, (double)((f18 * 1.5F + f21 * f23) * f16));
            }

            if (!this.level.isClientSide) {
               this.inWall = this.checkWalls(this.head.getBoundingBox()) | this.checkWalls(this.neck.getBoundingBox()) | this.checkWalls(this.body.getBoundingBox());
               if (this.dragonFight != null) {
                  this.dragonFight.updateDragon(this);
               }
            }

            for(int l = 0; l < this.subEntities.length; ++l) {
               this.subEntities[l].xo = avector3d[l].x;
               this.subEntities[l].yo = avector3d[l].y;
               this.subEntities[l].zo = avector3d[l].z;
               this.subEntities[l].xOld = avector3d[l].x;
               this.subEntities[l].yOld = avector3d[l].y;
               this.subEntities[l].zOld = avector3d[l].z;
            }

         }
      }
   }

   private void tickPart(EnderDragonPartEntity pPart, double pOffsetX, double pOffsetY, double pOffsetZ) {
      pPart.setPos(this.getX() + pOffsetX, this.getY() + pOffsetY, this.getZ() + pOffsetZ);
   }

   private float getHeadYOffset() {
      if (this.phaseManager.getCurrentPhase().isSitting()) {
         return -1.0F;
      } else {
         double[] adouble = this.getLatencyPos(5, 1.0F);
         double[] adouble1 = this.getLatencyPos(0, 1.0F);
         return (float)(adouble[1] - adouble1[1]);
      }
   }

   /**
    * Updates the state of the enderdragon's current endercrystal.
    */
   private void checkCrystals() {
      if (this.nearestCrystal != null) {
         if (this.nearestCrystal.removed) {
            this.nearestCrystal = null;
         } else if (this.tickCount % 10 == 0 && this.getHealth() < this.getMaxHealth()) {
            this.setHealth(this.getHealth() + 1.0F);
         }
      }

      if (this.random.nextInt(10) == 0) {
         List<EnderCrystalEntity> list = this.level.getEntitiesOfClass(EnderCrystalEntity.class, this.getBoundingBox().inflate(32.0D));
         EnderCrystalEntity endercrystalentity = null;
         double d0 = Double.MAX_VALUE;

         for(EnderCrystalEntity endercrystalentity1 : list) {
            double d1 = endercrystalentity1.distanceToSqr(this);
            if (d1 < d0) {
               d0 = d1;
               endercrystalentity = endercrystalentity1;
            }
         }

         this.nearestCrystal = endercrystalentity;
      }

   }

   /**
    * Pushes all entities inside the list away from the enderdragon.
    */
   private void knockBack(List<Entity> pEntities) {
      double d0 = (this.body.getBoundingBox().minX + this.body.getBoundingBox().maxX) / 2.0D;
      double d1 = (this.body.getBoundingBox().minZ + this.body.getBoundingBox().maxZ) / 2.0D;

      for(Entity entity : pEntities) {
         if (entity instanceof LivingEntity) {
            double d2 = entity.getX() - d0;
            double d3 = entity.getZ() - d1;
            double d4 = Math.max(d2 * d2 + d3 * d3, 0.1D);
            entity.push(d2 / d4 * 4.0D, (double)0.2F, d3 / d4 * 4.0D);
            if (!this.phaseManager.getCurrentPhase().isSitting() && ((LivingEntity)entity).getLastHurtByMobTimestamp() < entity.tickCount - 2) {
               entity.hurt(DamageSource.mobAttack(this), 5.0F);
               this.doEnchantDamageEffects(this, entity);
            }
         }
      }

   }

   /**
    * Attacks all entities inside this list, dealing 5 hearts of damage.
    */
   private void hurt(List<Entity> pEntities) {
      for(Entity entity : pEntities) {
         if (entity instanceof LivingEntity) {
            entity.hurt(DamageSource.mobAttack(this), 10.0F);
            this.doEnchantDamageEffects(this, entity);
         }
      }

   }

   /**
    * Simplifies the value of a number by adding/subtracting 180 to the point that the number is between -180 and 180.
    */
   private float rotWrap(double pAngle) {
      return (float)MathHelper.wrapDegrees(pAngle);
   }

   /**
    * Destroys all blocks that aren't associated with 'The End' inside the given bounding box.
    */
   private boolean checkWalls(AxisAlignedBB pArea) {
      int i = MathHelper.floor(pArea.minX);
      int j = MathHelper.floor(pArea.minY);
      int k = MathHelper.floor(pArea.minZ);
      int l = MathHelper.floor(pArea.maxX);
      int i1 = MathHelper.floor(pArea.maxY);
      int j1 = MathHelper.floor(pArea.maxZ);
      boolean flag = false;
      boolean flag1 = false;

      for(int k1 = i; k1 <= l; ++k1) {
         for(int l1 = j; l1 <= i1; ++l1) {
            for(int i2 = k; i2 <= j1; ++i2) {
               BlockPos blockpos = new BlockPos(k1, l1, i2);
               BlockState blockstate = this.level.getBlockState(blockpos);
               Block block = blockstate.getBlock();
               if (!blockstate.isAir(this.level, blockpos) && blockstate.getMaterial() != Material.FIRE) {
                  if (net.minecraftforge.common.ForgeHooks.canEntityDestroy(this.level, blockpos, this) && !BlockTags.DRAGON_IMMUNE.contains(block)) {
                     flag1 = this.level.removeBlock(blockpos, false) || flag1;
                  } else {
                     flag = true;
                  }
               }
            }
         }
      }

      if (flag1) {
         BlockPos blockpos1 = new BlockPos(i + this.random.nextInt(l - i + 1), j + this.random.nextInt(i1 - j + 1), k + this.random.nextInt(j1 - k + 1));
         this.level.levelEvent(2008, blockpos1, 0);
      }

      return flag;
   }

   public boolean hurt(EnderDragonPartEntity pPart, DamageSource pSource, float pDamage) {
      if (this.phaseManager.getCurrentPhase().getPhase() == PhaseType.DYING) {
         return false;
      } else {
         pDamage = this.phaseManager.getCurrentPhase().onHurt(pSource, pDamage);
         if (pPart != this.head) {
            pDamage = pDamage / 4.0F + Math.min(pDamage, 1.0F);
         }

         if (pDamage < 0.01F) {
            return false;
         } else {
            if (pSource.getEntity() instanceof PlayerEntity || pSource.isExplosion()) {
               float f = this.getHealth();
               this.reallyHurt(pSource, pDamage);
               if (this.isDeadOrDying() && !this.phaseManager.getCurrentPhase().isSitting()) {
                  this.setHealth(1.0F);
                  this.phaseManager.setPhase(PhaseType.DYING);
               }

               if (this.phaseManager.getCurrentPhase().isSitting()) {
                  this.sittingDamageReceived = (int)((float)this.sittingDamageReceived + (f - this.getHealth()));
                  if ((float)this.sittingDamageReceived > 0.25F * this.getMaxHealth()) {
                     this.sittingDamageReceived = 0;
                     this.phaseManager.setPhase(PhaseType.TAKEOFF);
                  }
               }
            }

            return true;
         }
      }
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (pSource instanceof EntityDamageSource && ((EntityDamageSource)pSource).isThorns()) {
         this.hurt(this.body, pSource, pAmount);
      }

      return false;
   }

   /**
    * Provides a way to cause damage to an ender dragon.
    */
   protected boolean reallyHurt(DamageSource pDamageSource, float pAmount) {
      return super.hurt(pDamageSource, pAmount);
   }

   /**
    * Called by the /kill command.
    */
   public void kill() {
      this.remove();
      if (this.dragonFight != null) {
         this.dragonFight.updateDragon(this);
         this.dragonFight.setDragonKilled(this);
      }

   }

   /**
    * handles entity death timer, experience orb and particle creation
    */
   protected void tickDeath() {
      if (this.dragonFight != null) {
         this.dragonFight.updateDragon(this);
      }

      ++this.dragonDeathTime;
      if (this.dragonDeathTime >= 180 && this.dragonDeathTime <= 200) {
         float f = (this.random.nextFloat() - 0.5F) * 8.0F;
         float f1 = (this.random.nextFloat() - 0.5F) * 4.0F;
         float f2 = (this.random.nextFloat() - 0.5F) * 8.0F;
         this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.getX() + (double)f, this.getY() + 2.0D + (double)f1, this.getZ() + (double)f2, 0.0D, 0.0D, 0.0D);
      }

      boolean flag = this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT);
      int i = 500;
      if (this.dragonFight != null && !this.dragonFight.hasPreviouslyKilledDragon()) {
         i = 12000;
      }

      if (!this.level.isClientSide) {
         if (this.dragonDeathTime > 150 && this.dragonDeathTime % 5 == 0 && flag) {
            this.dropExperience(MathHelper.floor((float)i * 0.08F));
         }

         if (this.dragonDeathTime == 1 && !this.isSilent()) {
            this.level.globalLevelEvent(1028, this.blockPosition(), 0);
         }
      }

      this.move(MoverType.SELF, new Vector3d(0.0D, (double)0.1F, 0.0D));
      this.yRot += 20.0F;
      this.yBodyRot = this.yRot;
      if (this.dragonDeathTime == 200 && !this.level.isClientSide) {
         if (flag) {
            this.dropExperience(MathHelper.floor((float)i * 0.2F));
         }

         if (this.dragonFight != null) {
            this.dragonFight.setDragonKilled(this);
         }

         this.remove();
      }

   }

   private void dropExperience(int pAmount) {
      while(pAmount > 0) {
         int i = ExperienceOrbEntity.getExperienceValue(pAmount);
         pAmount -= i;
         this.level.addFreshEntity(new ExperienceOrbEntity(this.level, this.getX(), this.getY(), this.getZ(), i));
      }

   }

   /**
    * Generates values for the fields pathPoints, and neighbors, and then returns the nearest pathPoint to the specified
    * position.
    */
   public int findClosestNode() {
      if (this.nodes[0] == null) {
         for(int i = 0; i < 24; ++i) {
            int j = 5;
            int l;
            int i1;
            if (i < 12) {
               l = MathHelper.floor(60.0F * MathHelper.cos(2.0F * (-(float)Math.PI + 0.2617994F * (float)i)));
               i1 = MathHelper.floor(60.0F * MathHelper.sin(2.0F * (-(float)Math.PI + 0.2617994F * (float)i)));
            } else if (i < 20) {
               int lvt_3_1_ = i - 12;
               l = MathHelper.floor(40.0F * MathHelper.cos(2.0F * (-(float)Math.PI + ((float)Math.PI / 8F) * (float)lvt_3_1_)));
               i1 = MathHelper.floor(40.0F * MathHelper.sin(2.0F * (-(float)Math.PI + ((float)Math.PI / 8F) * (float)lvt_3_1_)));
               j += 10;
            } else {
               int k1 = i - 20;
               l = MathHelper.floor(20.0F * MathHelper.cos(2.0F * (-(float)Math.PI + ((float)Math.PI / 4F) * (float)k1)));
               i1 = MathHelper.floor(20.0F * MathHelper.sin(2.0F * (-(float)Math.PI + ((float)Math.PI / 4F) * (float)k1)));
            }

            int j1 = Math.max(this.level.getSeaLevel() + 10, this.level.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, new BlockPos(l, 0, i1)).getY() + j);
            this.nodes[i] = new PathPoint(l, j1, i1);
         }

         this.nodeAdjacency[0] = 6146;
         this.nodeAdjacency[1] = 8197;
         this.nodeAdjacency[2] = 8202;
         this.nodeAdjacency[3] = 16404;
         this.nodeAdjacency[4] = 32808;
         this.nodeAdjacency[5] = 32848;
         this.nodeAdjacency[6] = 65696;
         this.nodeAdjacency[7] = 131392;
         this.nodeAdjacency[8] = 131712;
         this.nodeAdjacency[9] = 263424;
         this.nodeAdjacency[10] = 526848;
         this.nodeAdjacency[11] = 525313;
         this.nodeAdjacency[12] = 1581057;
         this.nodeAdjacency[13] = 3166214;
         this.nodeAdjacency[14] = 2138120;
         this.nodeAdjacency[15] = 6373424;
         this.nodeAdjacency[16] = 4358208;
         this.nodeAdjacency[17] = 12910976;
         this.nodeAdjacency[18] = 9044480;
         this.nodeAdjacency[19] = 9706496;
         this.nodeAdjacency[20] = 15216640;
         this.nodeAdjacency[21] = 13688832;
         this.nodeAdjacency[22] = 11763712;
         this.nodeAdjacency[23] = 8257536;
      }

      return this.findClosestNode(this.getX(), this.getY(), this.getZ());
   }

   /**
    * Returns the index into pathPoints of the nearest PathPoint.
    */
   public int findClosestNode(double pX, double pY, double pZ) {
      float f = 10000.0F;
      int i = 0;
      PathPoint pathpoint = new PathPoint(MathHelper.floor(pX), MathHelper.floor(pY), MathHelper.floor(pZ));
      int j = 0;
      if (this.dragonFight == null || this.dragonFight.getCrystalsAlive() == 0) {
         j = 12;
      }

      for(int k = j; k < 24; ++k) {
         if (this.nodes[k] != null) {
            float f1 = this.nodes[k].distanceToSqr(pathpoint);
            if (f1 < f) {
               f = f1;
               i = k;
            }
         }
      }

      return i;
   }

   /**
    * Find and return a path among the circles described by pathPoints, or null if the shortest path would just be
    * directly between the start and finish with no intermediate points.
    * 
    * Starting with pathPoint[startIdx], it searches the neighboring points (and their neighboring points, and so on)
    * until it reaches pathPoint[finishIdx], at which point it calls makePath to seal the deal.
    */
   @Nullable
   public Path findPath(int pStartIndex, int pFinishIndex, @Nullable PathPoint pAndThen) {
      for(int i = 0; i < 24; ++i) {
         PathPoint pathpoint = this.nodes[i];
         pathpoint.closed = false;
         pathpoint.f = 0.0F;
         pathpoint.g = 0.0F;
         pathpoint.h = 0.0F;
         pathpoint.cameFrom = null;
         pathpoint.heapIdx = -1;
      }

      PathPoint pathpoint4 = this.nodes[pStartIndex];
      PathPoint pathpoint5 = this.nodes[pFinishIndex];
      pathpoint4.g = 0.0F;
      pathpoint4.h = pathpoint4.distanceTo(pathpoint5);
      pathpoint4.f = pathpoint4.h;
      this.openSet.clear();
      this.openSet.insert(pathpoint4);
      PathPoint pathpoint1 = pathpoint4;
      int j = 0;
      if (this.dragonFight == null || this.dragonFight.getCrystalsAlive() == 0) {
         j = 12;
      }

      while(!this.openSet.isEmpty()) {
         PathPoint pathpoint2 = this.openSet.pop();
         if (pathpoint2.equals(pathpoint5)) {
            if (pAndThen != null) {
               pAndThen.cameFrom = pathpoint5;
               pathpoint5 = pAndThen;
            }

            return this.reconstructPath(pathpoint4, pathpoint5);
         }

         if (pathpoint2.distanceTo(pathpoint5) < pathpoint1.distanceTo(pathpoint5)) {
            pathpoint1 = pathpoint2;
         }

         pathpoint2.closed = true;
         int k = 0;

         for(int l = 0; l < 24; ++l) {
            if (this.nodes[l] == pathpoint2) {
               k = l;
               break;
            }
         }

         for(int i1 = j; i1 < 24; ++i1) {
            if ((this.nodeAdjacency[k] & 1 << i1) > 0) {
               PathPoint pathpoint3 = this.nodes[i1];
               if (!pathpoint3.closed) {
                  float f = pathpoint2.g + pathpoint2.distanceTo(pathpoint3);
                  if (!pathpoint3.inOpenSet() || f < pathpoint3.g) {
                     pathpoint3.cameFrom = pathpoint2;
                     pathpoint3.g = f;
                     pathpoint3.h = pathpoint3.distanceTo(pathpoint5);
                     if (pathpoint3.inOpenSet()) {
                        this.openSet.changeCost(pathpoint3, pathpoint3.g + pathpoint3.h);
                     } else {
                        pathpoint3.f = pathpoint3.g + pathpoint3.h;
                        this.openSet.insert(pathpoint3);
                     }
                  }
               }
            }
         }
      }

      if (pathpoint1 == pathpoint4) {
         return null;
      } else {
         LOGGER.debug("Failed to find path from {} to {}", pStartIndex, pFinishIndex);
         if (pAndThen != null) {
            pAndThen.cameFrom = pathpoint1;
            pathpoint1 = pAndThen;
         }

         return this.reconstructPath(pathpoint4, pathpoint1);
      }
   }

   /**
    * Create and return a new PathEntity defining a path from the start to the finish, using the connections already
    * made by the caller, findPath.
    */
   private Path reconstructPath(PathPoint pStart, PathPoint pFinish) {
      List<PathPoint> list = Lists.newArrayList();
      PathPoint pathpoint = pFinish;
      list.add(0, pFinish);

      while(pathpoint.cameFrom != null) {
         pathpoint = pathpoint.cameFrom;
         list.add(0, pathpoint);
      }

      return new Path(list, new BlockPos(pFinish.x, pFinish.y, pFinish.z), true);
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("DragonPhase", this.phaseManager.getCurrentPhase().getPhase().getId());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.contains("DragonPhase")) {
         this.phaseManager.setPhase(PhaseType.getById(pCompound.getInt("DragonPhase")));
      }

   }

   /**
    * Makes the entity despawn if requirements are reached
    */
   public void checkDespawn() {
   }

   public EnderDragonPartEntity[] getSubEntities() {
      return this.subEntities;
   }

   /**
    * Returns true if other Entities should be prevented from moving through this Entity.
    */
   public boolean isPickable() {
      return false;
   }

   public SoundCategory getSoundSource() {
      return SoundCategory.HOSTILE;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENDER_DRAGON_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.ENDER_DRAGON_HURT;
   }

   /**
    * Returns the volume for the sounds this mob makes.
    */
   protected float getSoundVolume() {
      return 5.0F;
   }

   @OnlyIn(Dist.CLIENT)
   public float getHeadPartYOffset(int pPartIndex, double[] pSpineEndOffsets, double[] pHeadPartOffsets) {
      IPhase iphase = this.phaseManager.getCurrentPhase();
      PhaseType<? extends IPhase> phasetype = iphase.getPhase();
      double d0;
      if (phasetype != PhaseType.LANDING && phasetype != PhaseType.TAKEOFF) {
         if (iphase.isSitting()) {
            d0 = (double)pPartIndex;
         } else if (pPartIndex == 6) {
            d0 = 0.0D;
         } else {
            d0 = pHeadPartOffsets[1] - pSpineEndOffsets[1];
         }
      } else {
         BlockPos blockpos = this.level.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
         float f = Math.max(MathHelper.sqrt(blockpos.distSqr(this.position(), true)) / 4.0F, 1.0F);
         d0 = (double)((float)pPartIndex / f);
      }

      return (float)d0;
   }

   public Vector3d getHeadLookVector(float pPartialTicks) {
      IPhase iphase = this.phaseManager.getCurrentPhase();
      PhaseType<? extends IPhase> phasetype = iphase.getPhase();
      Vector3d vector3d;
      if (phasetype != PhaseType.LANDING && phasetype != PhaseType.TAKEOFF) {
         if (iphase.isSitting()) {
            float f4 = this.xRot;
            float f5 = 1.5F;
            this.xRot = -45.0F;
            vector3d = this.getViewVector(pPartialTicks);
            this.xRot = f4;
         } else {
            vector3d = this.getViewVector(pPartialTicks);
         }
      } else {
         BlockPos blockpos = this.level.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
         float f = Math.max(MathHelper.sqrt(blockpos.distSqr(this.position(), true)) / 4.0F, 1.0F);
         float f1 = 6.0F / f;
         float f2 = this.xRot;
         float f3 = 1.5F;
         this.xRot = -f1 * 1.5F * 5.0F;
         vector3d = this.getViewVector(pPartialTicks);
         this.xRot = f2;
      }

      return vector3d;
   }

   public void onCrystalDestroyed(EnderCrystalEntity pCrystal, BlockPos pPos, DamageSource pDamageSource) {
      PlayerEntity playerentity;
      if (pDamageSource.getEntity() instanceof PlayerEntity) {
         playerentity = (PlayerEntity)pDamageSource.getEntity();
      } else {
         playerentity = this.level.getNearestPlayer(CRYSTAL_DESTROY_TARGETING, (double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ());
      }

      if (pCrystal == this.nearestCrystal) {
         this.hurt(this.head, DamageSource.explosion(playerentity), 10.0F);
      }

      this.phaseManager.getCurrentPhase().onCrystalDestroyed(pCrystal, pPos, pDamageSource, playerentity);
   }

   public void onSyncedDataUpdated(DataParameter<?> pKey) {
      if (DATA_PHASE.equals(pKey) && this.level.isClientSide) {
         this.phaseManager.setPhase(PhaseType.getById(this.getEntityData().get(DATA_PHASE)));
      }

      super.onSyncedDataUpdated(pKey);
   }

   public PhaseManager getPhaseManager() {
      return this.phaseManager;
   }

   @Nullable
   public DragonFightManager getDragonFight() {
      return this.dragonFight;
   }

   public boolean addEffect(EffectInstance pEffectInstance) {
      return false;
   }

   protected boolean canRide(Entity pEntity) {
      return false;
   }

   /**
    * Returns false if this Entity can't move between dimensions. True if it can.
    */
   public boolean canChangeDimensions() {
      return false;
   }

   @Override
   public boolean isMultipartEntity() {
      return true;
   }

   @Override
   public net.minecraftforge.entity.PartEntity<?>[] getParts() {
      return this.subEntities;
   }
}
