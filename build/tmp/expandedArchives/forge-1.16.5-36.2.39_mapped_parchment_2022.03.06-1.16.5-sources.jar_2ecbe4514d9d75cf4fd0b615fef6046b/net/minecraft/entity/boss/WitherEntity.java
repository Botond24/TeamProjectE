package net.minecraft.entity.boss;

import com.google.common.collect.ImmutableList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IChargeableMob;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.RangedAttackGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(
   value = Dist.CLIENT,
   _interface = IChargeableMob.class
)
public class WitherEntity extends MonsterEntity implements IChargeableMob, IRangedAttackMob {
   private static final DataParameter<Integer> DATA_TARGET_A = EntityDataManager.defineId(WitherEntity.class, DataSerializers.INT);
   private static final DataParameter<Integer> DATA_TARGET_B = EntityDataManager.defineId(WitherEntity.class, DataSerializers.INT);
   private static final DataParameter<Integer> DATA_TARGET_C = EntityDataManager.defineId(WitherEntity.class, DataSerializers.INT);
   private static final List<DataParameter<Integer>> DATA_TARGETS = ImmutableList.of(DATA_TARGET_A, DATA_TARGET_B, DATA_TARGET_C);
   private static final DataParameter<Integer> DATA_ID_INV = EntityDataManager.defineId(WitherEntity.class, DataSerializers.INT);
   private final float[] xRotHeads = new float[2];
   private final float[] yRotHeads = new float[2];
   private final float[] xRotOHeads = new float[2];
   private final float[] yRotOHeads = new float[2];
   private final int[] nextHeadUpdate = new int[2];
   private final int[] idleHeadUpdates = new int[2];
   private int destroyBlocksTick;
   private final ServerBossInfo bossEvent = (ServerBossInfo)(new ServerBossInfo(this.getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS)).setDarkenScreen(true);
   private static final Predicate<LivingEntity> LIVING_ENTITY_SELECTOR = (p_213797_0_) -> {
      return p_213797_0_.getMobType() != CreatureAttribute.UNDEAD && p_213797_0_.attackable();
   };
   private static final EntityPredicate TARGETING_CONDITIONS = (new EntityPredicate()).range(20.0D).selector(LIVING_ENTITY_SELECTOR);

   public WitherEntity(EntityType<? extends WitherEntity> p_i50226_1_, World p_i50226_2_) {
      super(p_i50226_1_, p_i50226_2_);
      this.setHealth(this.getMaxHealth());
      this.getNavigation().setCanFloat(true);
      this.xpReward = 50;
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new WitherEntity.DoNothingGoal());
      this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0D, 40, 20.0F));
      this.goalSelector.addGoal(5, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
      this.goalSelector.addGoal(6, new LookAtGoal(this, PlayerEntity.class, 8.0F));
      this.goalSelector.addGoal(7, new LookRandomlyGoal(this));
      this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, MobEntity.class, 0, false, false, LIVING_ENTITY_SELECTOR));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_TARGET_A, 0);
      this.entityData.define(DATA_TARGET_B, 0);
      this.entityData.define(DATA_TARGET_C, 0);
      this.entityData.define(DATA_ID_INV, 0);
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("Invul", this.getInvulnerableTicks());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setInvulnerableTicks(pCompound.getInt("Invul"));
      if (this.hasCustomName()) {
         this.bossEvent.setName(this.getDisplayName());
      }

   }

   public void setCustomName(@Nullable ITextComponent pName) {
      super.setCustomName(pName);
      this.bossEvent.setName(this.getDisplayName());
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.WITHER_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.WITHER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.WITHER_DEATH;
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      Vector3d vector3d = this.getDeltaMovement().multiply(1.0D, 0.6D, 1.0D);
      if (!this.level.isClientSide && this.getAlternativeTarget(0) > 0) {
         Entity entity = this.level.getEntity(this.getAlternativeTarget(0));
         if (entity != null) {
            double d0 = vector3d.y;
            if (this.getY() < entity.getY() || !this.isPowered() && this.getY() < entity.getY() + 5.0D) {
               d0 = Math.max(0.0D, d0);
               d0 = d0 + (0.3D - d0 * (double)0.6F);
            }

            vector3d = new Vector3d(vector3d.x, d0, vector3d.z);
            Vector3d vector3d1 = new Vector3d(entity.getX() - this.getX(), 0.0D, entity.getZ() - this.getZ());
            if (getHorizontalDistanceSqr(vector3d1) > 9.0D) {
               Vector3d vector3d2 = vector3d1.normalize();
               vector3d = vector3d.add(vector3d2.x * 0.3D - vector3d.x * 0.6D, 0.0D, vector3d2.z * 0.3D - vector3d.z * 0.6D);
            }
         }
      }

      this.setDeltaMovement(vector3d);
      if (getHorizontalDistanceSqr(vector3d) > 0.05D) {
         this.yRot = (float)MathHelper.atan2(vector3d.z, vector3d.x) * (180F / (float)Math.PI) - 90.0F;
      }

      super.aiStep();

      for(int i = 0; i < 2; ++i) {
         this.yRotOHeads[i] = this.yRotHeads[i];
         this.xRotOHeads[i] = this.xRotHeads[i];
      }

      for(int j = 0; j < 2; ++j) {
         int k = this.getAlternativeTarget(j + 1);
         Entity entity1 = null;
         if (k > 0) {
            entity1 = this.level.getEntity(k);
         }

         if (entity1 != null) {
            double d9 = this.getHeadX(j + 1);
            double d1 = this.getHeadY(j + 1);
            double d3 = this.getHeadZ(j + 1);
            double d4 = entity1.getX() - d9;
            double d5 = entity1.getEyeY() - d1;
            double d6 = entity1.getZ() - d3;
            double d7 = (double)MathHelper.sqrt(d4 * d4 + d6 * d6);
            float f = (float)(MathHelper.atan2(d6, d4) * (double)(180F / (float)Math.PI)) - 90.0F;
            float f1 = (float)(-(MathHelper.atan2(d5, d7) * (double)(180F / (float)Math.PI)));
            this.xRotHeads[j] = this.rotlerp(this.xRotHeads[j], f1, 40.0F);
            this.yRotHeads[j] = this.rotlerp(this.yRotHeads[j], f, 10.0F);
         } else {
            this.yRotHeads[j] = this.rotlerp(this.yRotHeads[j], this.yBodyRot, 10.0F);
         }
      }

      boolean flag = this.isPowered();

      for(int l = 0; l < 3; ++l) {
         double d8 = this.getHeadX(l);
         double d10 = this.getHeadY(l);
         double d2 = this.getHeadZ(l);
         this.level.addParticle(ParticleTypes.SMOKE, d8 + this.random.nextGaussian() * (double)0.3F, d10 + this.random.nextGaussian() * (double)0.3F, d2 + this.random.nextGaussian() * (double)0.3F, 0.0D, 0.0D, 0.0D);
         if (flag && this.level.random.nextInt(4) == 0) {
            this.level.addParticle(ParticleTypes.ENTITY_EFFECT, d8 + this.random.nextGaussian() * (double)0.3F, d10 + this.random.nextGaussian() * (double)0.3F, d2 + this.random.nextGaussian() * (double)0.3F, (double)0.7F, (double)0.7F, 0.5D);
         }
      }

      if (this.getInvulnerableTicks() > 0) {
         for(int i1 = 0; i1 < 3; ++i1) {
            this.level.addParticle(ParticleTypes.ENTITY_EFFECT, this.getX() + this.random.nextGaussian(), this.getY() + (double)(this.random.nextFloat() * 3.3F), this.getZ() + this.random.nextGaussian(), (double)0.7F, (double)0.7F, (double)0.9F);
         }
      }

   }

   protected void customServerAiStep() {
      if (this.getInvulnerableTicks() > 0) {
         int j1 = this.getInvulnerableTicks() - 1;
         if (j1 <= 0) {
            Explosion.Mode explosion$mode = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level, this) ? Explosion.Mode.DESTROY : Explosion.Mode.NONE;
            this.level.explode(this, this.getX(), this.getEyeY(), this.getZ(), 7.0F, false, explosion$mode);
            if (!this.isSilent()) {
               this.level.globalLevelEvent(1023, this.blockPosition(), 0);
            }
         }

         this.setInvulnerableTicks(j1);
         if (this.tickCount % 10 == 0) {
            this.heal(10.0F);
         }

      } else {
         super.customServerAiStep();

         for(int i = 1; i < 3; ++i) {
            if (this.tickCount >= this.nextHeadUpdate[i - 1]) {
               this.nextHeadUpdate[i - 1] = this.tickCount + 10 + this.random.nextInt(10);
               if (this.level.getDifficulty() == Difficulty.NORMAL || this.level.getDifficulty() == Difficulty.HARD) {
                  int j3 = i - 1;
                  int k3 = this.idleHeadUpdates[i - 1];
                  this.idleHeadUpdates[j3] = this.idleHeadUpdates[i - 1] + 1;
                  if (k3 > 15) {
                     float f = 10.0F;
                     float f1 = 5.0F;
                     double d0 = MathHelper.nextDouble(this.random, this.getX() - 10.0D, this.getX() + 10.0D);
                     double d1 = MathHelper.nextDouble(this.random, this.getY() - 5.0D, this.getY() + 5.0D);
                     double d2 = MathHelper.nextDouble(this.random, this.getZ() - 10.0D, this.getZ() + 10.0D);
                     this.performRangedAttack(i + 1, d0, d1, d2, true);
                     this.idleHeadUpdates[i - 1] = 0;
                  }
               }

               int k1 = this.getAlternativeTarget(i);
               if (k1 > 0) {
                  Entity entity = this.level.getEntity(k1);
                  if (entity != null && entity.isAlive() && !(this.distanceToSqr(entity) > 900.0D) && this.canSee(entity)) {
                     if (entity instanceof PlayerEntity && ((PlayerEntity)entity).abilities.invulnerable) {
                        this.setAlternativeTarget(i, 0);
                     } else {
                        this.performRangedAttack(i + 1, (LivingEntity)entity);
                        this.nextHeadUpdate[i - 1] = this.tickCount + 40 + this.random.nextInt(20);
                        this.idleHeadUpdates[i - 1] = 0;
                     }
                  } else {
                     this.setAlternativeTarget(i, 0);
                  }
               } else {
                  List<LivingEntity> list = this.level.getNearbyEntities(LivingEntity.class, TARGETING_CONDITIONS, this, this.getBoundingBox().inflate(20.0D, 8.0D, 20.0D));

                  for(int j2 = 0; j2 < 10 && !list.isEmpty(); ++j2) {
                     LivingEntity livingentity = list.get(this.random.nextInt(list.size()));
                     if (livingentity != this && livingentity.isAlive() && this.canSee(livingentity)) {
                        if (livingentity instanceof PlayerEntity) {
                           if (!((PlayerEntity)livingentity).abilities.invulnerable) {
                              this.setAlternativeTarget(i, livingentity.getId());
                           }
                        } else {
                           this.setAlternativeTarget(i, livingentity.getId());
                        }
                        break;
                     }

                     list.remove(livingentity);
                  }
               }
            }
         }

         if (this.getTarget() != null) {
            this.setAlternativeTarget(0, this.getTarget().getId());
         } else {
            this.setAlternativeTarget(0, 0);
         }

         if (this.destroyBlocksTick > 0) {
            --this.destroyBlocksTick;
            if (this.destroyBlocksTick == 0 && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level, this)) {
               int i1 = MathHelper.floor(this.getY());
               int l1 = MathHelper.floor(this.getX());
               int i2 = MathHelper.floor(this.getZ());
               boolean flag = false;

               for(int k2 = -1; k2 <= 1; ++k2) {
                  for(int l2 = -1; l2 <= 1; ++l2) {
                     for(int j = 0; j <= 3; ++j) {
                        int i3 = l1 + k2;
                        int k = i1 + j;
                        int l = i2 + l2;
                        BlockPos blockpos = new BlockPos(i3, k, l);
                        BlockState blockstate = this.level.getBlockState(blockpos);
                        if (blockstate.canEntityDestroy(this.level, blockpos, this) && net.minecraftforge.event.ForgeEventFactory.onEntityDestroyBlock(this, blockpos, blockstate)) {
                           flag = this.level.destroyBlock(blockpos, true, this) || flag;
                        }
                     }
                  }
               }

               if (flag) {
                  this.level.levelEvent((PlayerEntity)null, 1022, this.blockPosition(), 0);
               }
            }
         }

         if (this.tickCount % 20 == 0) {
            this.heal(1.0F);
         }

         this.bossEvent.setPercent(this.getHealth() / this.getMaxHealth());
      }
   }

   @Deprecated //Forge: DO NOT USE use BlockState.canEntityDestroy
   public static boolean canDestroy(BlockState pState) {
      return !pState.isAir() && !BlockTags.WITHER_IMMUNE.contains(pState.getBlock());
   }

   /**
    * Initializes this Wither's explosion sequence and makes it invulnerable. Called immediately after spawning.
    */
   public void makeInvulnerable() {
      this.setInvulnerableTicks(220);
      this.setHealth(this.getMaxHealth() / 3.0F);
   }

   public void makeStuckInBlock(BlockState pState, Vector3d pMotionMultiplier) {
   }

   /**
    * Add the given player to the list of players tracking this entity. For instance, a player may track a boss in order
    * to view its associated boss bar.
    */
   public void startSeenByPlayer(ServerPlayerEntity pServerPlayer) {
      super.startSeenByPlayer(pServerPlayer);
      this.bossEvent.addPlayer(pServerPlayer);
   }

   /**
    * Removes the given player from the list of players tracking this entity. See {@link Entity#addTrackingPlayer} for
    * more information on tracking.
    */
   public void stopSeenByPlayer(ServerPlayerEntity pServerPlayer) {
      super.stopSeenByPlayer(pServerPlayer);
      this.bossEvent.removePlayer(pServerPlayer);
   }

   private double getHeadX(int pHead) {
      if (pHead <= 0) {
         return this.getX();
      } else {
         float f = (this.yBodyRot + (float)(180 * (pHead - 1))) * ((float)Math.PI / 180F);
         float f1 = MathHelper.cos(f);
         return this.getX() + (double)f1 * 1.3D;
      }
   }

   private double getHeadY(int pHead) {
      return pHead <= 0 ? this.getY() + 3.0D : this.getY() + 2.2D;
   }

   private double getHeadZ(int pHead) {
      if (pHead <= 0) {
         return this.getZ();
      } else {
         float f = (this.yBodyRot + (float)(180 * (pHead - 1))) * ((float)Math.PI / 180F);
         float f1 = MathHelper.sin(f);
         return this.getZ() + (double)f1 * 1.3D;
      }
   }

   private float rotlerp(float pAngle, float p_82204_2_, float p_82204_3_) {
      float f = MathHelper.wrapDegrees(p_82204_2_ - pAngle);
      if (f > p_82204_3_) {
         f = p_82204_3_;
      }

      if (f < -p_82204_3_) {
         f = -p_82204_3_;
      }

      return pAngle + f;
   }

   private void performRangedAttack(int pHead, LivingEntity pTarget) {
      this.performRangedAttack(pHead, pTarget.getX(), pTarget.getY() + (double)pTarget.getEyeHeight() * 0.5D, pTarget.getZ(), pHead == 0 && this.random.nextFloat() < 0.001F);
   }

   /**
    * Launches a Wither skull toward (par2, par4, par6)
    */
   private void performRangedAttack(int pHead, double pX, double pY, double pZ, boolean pIsDangerous) {
      if (!this.isSilent()) {
         this.level.levelEvent((PlayerEntity)null, 1024, this.blockPosition(), 0);
      }

      double d0 = this.getHeadX(pHead);
      double d1 = this.getHeadY(pHead);
      double d2 = this.getHeadZ(pHead);
      double d3 = pX - d0;
      double d4 = pY - d1;
      double d5 = pZ - d2;
      WitherSkullEntity witherskullentity = new WitherSkullEntity(this.level, this, d3, d4, d5);
      witherskullentity.setOwner(this);
      if (pIsDangerous) {
         witherskullentity.setDangerous(true);
      }

      witherskullentity.setPosRaw(d0, d1, d2);
      this.level.addFreshEntity(witherskullentity);
   }

   /**
    * Attack the specified entity using a ranged attack.
    */
   public void performRangedAttack(LivingEntity pTarget, float pVelocity) {
      this.performRangedAttack(0, pTarget);
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (this.isInvulnerableTo(pSource)) {
         return false;
      } else if (pSource != DamageSource.DROWN && !(pSource.getEntity() instanceof WitherEntity)) {
         if (this.getInvulnerableTicks() > 0 && pSource != DamageSource.OUT_OF_WORLD) {
            return false;
         } else {
            if (this.isPowered()) {
               Entity entity = pSource.getDirectEntity();
               if (entity instanceof AbstractArrowEntity) {
                  return false;
               }
            }

            Entity entity1 = pSource.getEntity();
            if (entity1 != null && !(entity1 instanceof PlayerEntity) && entity1 instanceof LivingEntity && ((LivingEntity)entity1).getMobType() == this.getMobType()) {
               return false;
            } else {
               if (this.destroyBlocksTick <= 0) {
                  this.destroyBlocksTick = 20;
               }

               for(int i = 0; i < this.idleHeadUpdates.length; ++i) {
                  this.idleHeadUpdates[i] += 3;
               }

               return super.hurt(pSource, pAmount);
            }
         }
      } else {
         return false;
      }
   }

   protected void dropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit) {
      super.dropCustomDeathLoot(pSource, pLooting, pRecentlyHit);
      ItemEntity itementity = this.spawnAtLocation(Items.NETHER_STAR);
      if (itementity != null) {
         itementity.setExtendedLifetime();
      }

   }

   /**
    * Makes the entity despawn if requirements are reached
    */
   public void checkDespawn() {
      if (this.level.getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
         this.remove();
      } else {
         this.noActionTime = 0;
      }
   }

   public boolean causeFallDamage(float pFallDistance, float pDamageMultiplier) {
      return false;
   }

   public boolean addEffect(EffectInstance pEffectInstance) {
      return false;
   }

   public static AttributeModifierMap.MutableAttribute createAttributes() {
      return MonsterEntity.createMonsterAttributes().add(Attributes.MAX_HEALTH, 300.0D).add(Attributes.MOVEMENT_SPEED, (double)0.6F).add(Attributes.FOLLOW_RANGE, 40.0D).add(Attributes.ARMOR, 4.0D);
   }

   @OnlyIn(Dist.CLIENT)
   public float getHeadYRot(int pHead) {
      return this.yRotHeads[pHead];
   }

   @OnlyIn(Dist.CLIENT)
   public float getHeadXRot(int pHead) {
      return this.xRotHeads[pHead];
   }

   public int getInvulnerableTicks() {
      return this.entityData.get(DATA_ID_INV);
   }

   public void setInvulnerableTicks(int pInvulnerableTicks) {
      this.entityData.set(DATA_ID_INV, pInvulnerableTicks);
   }

   /**
    * Returns the target entity ID if present, or -1 if not
    * @param pHead The target offset, should be from 0-2
    */
   public int getAlternativeTarget(int pHead) {
      return this.entityData.get(DATA_TARGETS.get(pHead));
   }

   /**
    * Updates the target entity ID
    */
   public void setAlternativeTarget(int pTargetOffset, int pNewId) {
      this.entityData.set(DATA_TARGETS.get(pTargetOffset), pNewId);
   }

   public boolean isPowered() {
      return this.getHealth() <= this.getMaxHealth() / 2.0F;
   }

   public CreatureAttribute getMobType() {
      return CreatureAttribute.UNDEAD;
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

   public boolean canBeAffected(EffectInstance pEffectInstance) {
      return pEffectInstance.getEffect() == Effects.WITHER ? false : super.canBeAffected(pEffectInstance);
   }

   class DoNothingGoal extends Goal {
      public DoNothingGoal() {
         this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return WitherEntity.this.getInvulnerableTicks() > 0;
      }
   }
}
