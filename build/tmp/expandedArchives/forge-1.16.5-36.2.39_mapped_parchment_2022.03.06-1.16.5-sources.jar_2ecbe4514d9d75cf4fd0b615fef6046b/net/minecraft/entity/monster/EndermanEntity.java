package net.minecraft.entity.monster;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.ResetAngerGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.RangedInteger;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.TickRangeConverter;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class EndermanEntity extends MonsterEntity implements IAngerable {
   private static final UUID SPEED_MODIFIER_ATTACKING_UUID = UUID.fromString("020E0DFB-87AE-4653-9556-831010E291A0");
   private static final AttributeModifier SPEED_MODIFIER_ATTACKING = new AttributeModifier(SPEED_MODIFIER_ATTACKING_UUID, "Attacking speed boost", (double)0.15F, AttributeModifier.Operation.ADDITION);
   private static final DataParameter<Optional<BlockState>> DATA_CARRY_STATE = EntityDataManager.defineId(EndermanEntity.class, DataSerializers.BLOCK_STATE);
   private static final DataParameter<Boolean> DATA_CREEPY = EntityDataManager.defineId(EndermanEntity.class, DataSerializers.BOOLEAN);
   private static final DataParameter<Boolean> DATA_STARED_AT = EntityDataManager.defineId(EndermanEntity.class, DataSerializers.BOOLEAN);
   private static final Predicate<LivingEntity> ENDERMITE_SELECTOR = (p_213626_0_) -> {
      return p_213626_0_ instanceof EndermiteEntity && ((EndermiteEntity)p_213626_0_).isPlayerSpawned();
   };
   private int lastStareSound = Integer.MIN_VALUE;
   private int targetChangeTime;
   private static final RangedInteger PERSISTENT_ANGER_TIME = TickRangeConverter.rangeOfSeconds(20, 39);
   private int remainingPersistentAngerTime;
   private UUID persistentAngerTarget;

   public EndermanEntity(EntityType<? extends EndermanEntity> p_i50210_1_, World p_i50210_2_) {
      super(p_i50210_1_, p_i50210_2_);
      this.maxUpStep = 1.0F;
      this.setPathfindingMalus(PathNodeType.WATER, -1.0F);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new SwimGoal(this));
      this.goalSelector.addGoal(1, new EndermanEntity.StareGoal(this));
      this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
      this.goalSelector.addGoal(7, new WaterAvoidingRandomWalkingGoal(this, 1.0D, 0.0F));
      this.goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 8.0F));
      this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
      this.goalSelector.addGoal(10, new EndermanEntity.PlaceBlockGoal(this));
      this.goalSelector.addGoal(11, new EndermanEntity.TakeBlockGoal(this));
      this.targetSelector.addGoal(1, new EndermanEntity.FindPlayerGoal(this, this::isAngryAt));
      this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
      this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, EndermiteEntity.class, 10, true, false, ENDERMITE_SELECTOR));
      this.targetSelector.addGoal(4, new ResetAngerGoal<>(this, false));
   }

   public static AttributeModifierMap.MutableAttribute createAttributes() {
      return MonsterEntity.createMonsterAttributes().add(Attributes.MAX_HEALTH, 40.0D).add(Attributes.MOVEMENT_SPEED, (double)0.3F).add(Attributes.ATTACK_DAMAGE, 7.0D).add(Attributes.FOLLOW_RANGE, 64.0D);
   }

   /**
    * Sets the active target the Task system uses for tracking
    */
   public void setTarget(@Nullable LivingEntity pLivingEntity) {
      ModifiableAttributeInstance modifiableattributeinstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
      if (pLivingEntity == null) {
         this.targetChangeTime = 0;
         this.entityData.set(DATA_CREEPY, false);
         this.entityData.set(DATA_STARED_AT, false);
         modifiableattributeinstance.removeModifier(SPEED_MODIFIER_ATTACKING);
      } else {
         this.targetChangeTime = this.tickCount;
         this.entityData.set(DATA_CREEPY, true);
         if (!modifiableattributeinstance.hasModifier(SPEED_MODIFIER_ATTACKING)) {
            modifiableattributeinstance.addTransientModifier(SPEED_MODIFIER_ATTACKING);
         }
      }

      super.setTarget(pLivingEntity); //Forge: Moved down to allow event handlers to write data manager values.
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_CARRY_STATE, Optional.empty());
      this.entityData.define(DATA_CREEPY, false);
      this.entityData.define(DATA_STARED_AT, false);
   }

   public void startPersistentAngerTimer() {
      this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.randomValue(this.random));
   }

   public void setRemainingPersistentAngerTime(int pRemainingPersistentAngerTime) {
      this.remainingPersistentAngerTime = pRemainingPersistentAngerTime;
   }

   public int getRemainingPersistentAngerTime() {
      return this.remainingPersistentAngerTime;
   }

   public void setPersistentAngerTarget(@Nullable UUID pPersistentAngerTarget) {
      this.persistentAngerTarget = pPersistentAngerTarget;
   }

   public UUID getPersistentAngerTarget() {
      return this.persistentAngerTarget;
   }

   public void playStareSound() {
      if (this.tickCount >= this.lastStareSound + 400) {
         this.lastStareSound = this.tickCount;
         if (!this.isSilent()) {
            this.level.playLocalSound(this.getX(), this.getEyeY(), this.getZ(), SoundEvents.ENDERMAN_STARE, this.getSoundSource(), 2.5F, 1.0F, false);
         }
      }

   }

   public void onSyncedDataUpdated(DataParameter<?> pKey) {
      if (DATA_CREEPY.equals(pKey) && this.hasBeenStaredAt() && this.level.isClientSide) {
         this.playStareSound();
      }

      super.onSyncedDataUpdated(pKey);
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      BlockState blockstate = this.getCarriedBlock();
      if (blockstate != null) {
         pCompound.put("carriedBlockState", NBTUtil.writeBlockState(blockstate));
      }

      this.addPersistentAngerSaveData(pCompound);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      BlockState blockstate = null;
      if (pCompound.contains("carriedBlockState", 10)) {
         blockstate = NBTUtil.readBlockState(pCompound.getCompound("carriedBlockState"));
         if (blockstate.isAir()) {
            blockstate = null;
         }
      }

      this.setCarriedBlock(blockstate);
      if(!level.isClientSide) //FORGE: allow this entity to be read from nbt on client. (Fixes MC-189565)
      this.readPersistentAngerSaveData((ServerWorld)this.level, pCompound);
   }

   /**
    * Checks to see if this enderman should be attacking this player
    */
   private boolean isLookingAtMe(PlayerEntity pPlayer) {
      ItemStack itemstack = pPlayer.inventory.armor.get(3);
      if (itemstack.isEnderMask(pPlayer, this)) {
         return false;
      } else {
         Vector3d vector3d = pPlayer.getViewVector(1.0F).normalize();
         Vector3d vector3d1 = new Vector3d(this.getX() - pPlayer.getX(), this.getEyeY() - pPlayer.getEyeY(), this.getZ() - pPlayer.getZ());
         double d0 = vector3d1.length();
         vector3d1 = vector3d1.normalize();
         double d1 = vector3d.dot(vector3d1);
         return d1 > 1.0D - 0.025D / d0 ? pPlayer.canSee(this) : false;
      }
   }

   protected float getStandingEyeHeight(Pose pPose, EntitySize pSize) {
      return 2.55F;
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      if (this.level.isClientSide) {
         for(int i = 0; i < 2; ++i) {
            this.level.addParticle(ParticleTypes.PORTAL, this.getRandomX(0.5D), this.getRandomY() - 0.25D, this.getRandomZ(0.5D), (this.random.nextDouble() - 0.5D) * 2.0D, -this.random.nextDouble(), (this.random.nextDouble() - 0.5D) * 2.0D);
         }
      }

      this.jumping = false;
      if (!this.level.isClientSide) {
         this.updatePersistentAnger((ServerWorld)this.level, true);
      }

      super.aiStep();
   }

   public boolean isSensitiveToWater() {
      return true;
   }

   protected void customServerAiStep() {
      if (this.level.isDay() && this.tickCount >= this.targetChangeTime + 600) {
         float f = this.getBrightness();
         if (f > 0.5F && this.level.canSeeSky(this.blockPosition()) && this.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F) {
            this.setTarget((LivingEntity)null);
            this.teleport();
         }
      }

      super.customServerAiStep();
   }

   /**
    * Teleport the enderman to a random nearby position
    */
   protected boolean teleport() {
      if (!this.level.isClientSide() && this.isAlive()) {
         double d0 = this.getX() + (this.random.nextDouble() - 0.5D) * 64.0D;
         double d1 = this.getY() + (double)(this.random.nextInt(64) - 32);
         double d2 = this.getZ() + (this.random.nextDouble() - 0.5D) * 64.0D;
         return this.teleport(d0, d1, d2);
      } else {
         return false;
      }
   }

   /**
    * Teleport the enderman to another entity
    */
   private boolean teleportTowards(Entity pTarget) {
      Vector3d vector3d = new Vector3d(this.getX() - pTarget.getX(), this.getY(0.5D) - pTarget.getEyeY(), this.getZ() - pTarget.getZ());
      vector3d = vector3d.normalize();
      double d0 = 16.0D;
      double d1 = this.getX() + (this.random.nextDouble() - 0.5D) * 8.0D - vector3d.x * 16.0D;
      double d2 = this.getY() + (double)(this.random.nextInt(16) - 8) - vector3d.y * 16.0D;
      double d3 = this.getZ() + (this.random.nextDouble() - 0.5D) * 8.0D - vector3d.z * 16.0D;
      return this.teleport(d1, d2, d3);
   }

   /**
    * Teleport the enderman
    */
   private boolean teleport(double pX, double pY, double pZ) {
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable(pX, pY, pZ);

      while(blockpos$mutable.getY() > 0 && !this.level.getBlockState(blockpos$mutable).getMaterial().blocksMotion()) {
         blockpos$mutable.move(Direction.DOWN);
      }

      BlockState blockstate = this.level.getBlockState(blockpos$mutable);
      boolean flag = blockstate.getMaterial().blocksMotion();
      boolean flag1 = blockstate.getFluidState().is(FluidTags.WATER);
      if (flag && !flag1) {
         net.minecraftforge.event.entity.living.EntityTeleportEvent.EnderEntity event = net.minecraftforge.event.ForgeEventFactory.onEnderTeleport(this, pX, pY, pZ);
         if (event.isCanceled()) return false;
         boolean flag2 = this.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true);
         if (flag2 && !this.isSilent()) {
            this.level.playSound((PlayerEntity)null, this.xo, this.yo, this.zo, SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 1.0F, 1.0F);
            this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
         }

         return flag2;
      } else {
         return false;
      }
   }

   protected SoundEvent getAmbientSound() {
      return this.isCreepy() ? SoundEvents.ENDERMAN_SCREAM : SoundEvents.ENDERMAN_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.ENDERMAN_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENDERMAN_DEATH;
   }

   protected void dropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit) {
      super.dropCustomDeathLoot(pSource, pLooting, pRecentlyHit);
      BlockState blockstate = this.getCarriedBlock();
      if (blockstate != null) {
         this.spawnAtLocation(blockstate.getBlock());
      }

   }

   public void setCarriedBlock(@Nullable BlockState pState) {
      this.entityData.set(DATA_CARRY_STATE, Optional.ofNullable(pState));
   }

   @Nullable
   public BlockState getCarriedBlock() {
      return this.entityData.get(DATA_CARRY_STATE).orElse((BlockState)null);
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (this.isInvulnerableTo(pSource)) {
         return false;
      } else if (pSource instanceof IndirectEntityDamageSource) {
         for(int i = 0; i < 64; ++i) {
            if (this.teleport()) {
               return true;
            }
         }

         return false;
      } else {
         boolean flag = super.hurt(pSource, pAmount);
         if (!this.level.isClientSide() && !(pSource.getEntity() instanceof LivingEntity) && this.random.nextInt(10) != 0) {
            this.teleport();
         }

         return flag;
      }
   }

   public boolean isCreepy() {
      return this.entityData.get(DATA_CREEPY);
   }

   public boolean hasBeenStaredAt() {
      return this.entityData.get(DATA_STARED_AT);
   }

   public void setBeingStaredAt() {
      this.entityData.set(DATA_STARED_AT, true);
   }

   public boolean requiresCustomPersistence() {
      return super.requiresCustomPersistence() || this.getCarriedBlock() != null;
   }

   static class FindPlayerGoal extends NearestAttackableTargetGoal<PlayerEntity> {
      private final EndermanEntity enderman;
      /** The player */
      private PlayerEntity pendingTarget;
      private int aggroTime;
      private int teleportTime;
      private final EntityPredicate startAggroTargetConditions;
      private final EntityPredicate continueAggroTargetConditions = (new EntityPredicate()).allowUnseeable();

      public FindPlayerGoal(EndermanEntity pEnderman, @Nullable Predicate<LivingEntity> pSelectionPredicate) {
         super(pEnderman, PlayerEntity.class, 10, false, false, pSelectionPredicate);
         this.enderman = pEnderman;
         this.startAggroTargetConditions = (new EntityPredicate()).range(this.getFollowDistance()).selector((p_220790_1_) -> {
            return pEnderman.isLookingAtMe((PlayerEntity)p_220790_1_);
         });
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         this.pendingTarget = this.enderman.level.getNearestPlayer(this.startAggroTargetConditions, this.enderman);
         return this.pendingTarget != null;
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.aggroTime = 5;
         this.teleportTime = 0;
         this.enderman.setBeingStaredAt();
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         this.pendingTarget = null;
         super.stop();
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         if (this.pendingTarget != null) {
            if (!this.enderman.isLookingAtMe(this.pendingTarget)) {
               return false;
            } else {
               this.enderman.lookAt(this.pendingTarget, 10.0F, 10.0F);
               return true;
            }
         } else {
            return this.target != null && this.continueAggroTargetConditions.test(this.enderman, this.target) ? true : super.canContinueToUse();
         }
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         if (this.enderman.getTarget() == null) {
            super.setTarget((LivingEntity)null);
         }

         if (this.pendingTarget != null) {
            if (--this.aggroTime <= 0) {
               this.target = this.pendingTarget;
               this.pendingTarget = null;
               super.start();
            }
         } else {
            if (this.target != null && !this.enderman.isPassenger()) {
               if (this.enderman.isLookingAtMe((PlayerEntity)this.target)) {
                  if (this.target.distanceToSqr(this.enderman) < 16.0D) {
                     this.enderman.teleport();
                  }

                  this.teleportTime = 0;
               } else if (this.target.distanceToSqr(this.enderman) > 256.0D && this.teleportTime++ >= 30 && this.enderman.teleportTowards(this.target)) {
                  this.teleportTime = 0;
               }
            }

            super.tick();
         }

      }
   }

   static class PlaceBlockGoal extends Goal {
      private final EndermanEntity enderman;

      public PlaceBlockGoal(EndermanEntity pEnderman) {
         this.enderman = pEnderman;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if (this.enderman.getCarriedBlock() == null) {
            return false;
         } else if (!net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.enderman.level, this.enderman)) {
            return false;
         } else {
            return this.enderman.getRandom().nextInt(2000) == 0;
         }
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         Random random = this.enderman.getRandom();
         World world = this.enderman.level;
         int i = MathHelper.floor(this.enderman.getX() - 1.0D + random.nextDouble() * 2.0D);
         int j = MathHelper.floor(this.enderman.getY() + random.nextDouble() * 2.0D);
         int k = MathHelper.floor(this.enderman.getZ() - 1.0D + random.nextDouble() * 2.0D);
         BlockPos blockpos = new BlockPos(i, j, k);
         BlockState blockstate = world.getBlockState(blockpos);
         BlockPos blockpos1 = blockpos.below();
         BlockState blockstate1 = world.getBlockState(blockpos1);
         BlockState blockstate2 = this.enderman.getCarriedBlock();
         if (blockstate2 != null) {
            blockstate2 = Block.updateFromNeighbourShapes(blockstate2, this.enderman.level, blockpos);
            if (this.canPlaceBlock(world, blockpos, blockstate2, blockstate, blockstate1, blockpos1) && !net.minecraftforge.event.ForgeEventFactory.onBlockPlace(enderman, net.minecraftforge.common.util.BlockSnapshot.create(world.dimension(), world, blockpos1), net.minecraft.util.Direction.UP)) {
               world.setBlock(blockpos, blockstate2, 3);
               this.enderman.setCarriedBlock((BlockState)null);
            }

         }
      }

      private boolean canPlaceBlock(World pLevel, BlockPos pDestinationPos, BlockState pCarriedState, BlockState pDestinationState, BlockState pBelowDestinationState, BlockPos pBelowDestinationPos) {
         return pDestinationState.isAir(pLevel, pDestinationPos) && !pBelowDestinationState.isAir(pLevel, pBelowDestinationPos) && !pBelowDestinationState.is(Blocks.BEDROCK) && !pBelowDestinationState.is(net.minecraftforge.common.Tags.Blocks.ENDERMAN_PLACE_ON_BLACKLIST) && pBelowDestinationState.isCollisionShapeFullBlock(pLevel, pBelowDestinationPos) && pCarriedState.canSurvive(pLevel, pDestinationPos) && pLevel.getEntities(this.enderman, AxisAlignedBB.unitCubeFromLowerCorner(Vector3d.atLowerCornerOf(pDestinationPos))).isEmpty();
      }
   }

   static class StareGoal extends Goal {
      private final EndermanEntity enderman;
      private LivingEntity target;

      public StareGoal(EndermanEntity pEnderman) {
         this.enderman = pEnderman;
         this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         this.target = this.enderman.getTarget();
         if (!(this.target instanceof PlayerEntity)) {
            return false;
         } else {
            double d0 = this.target.distanceToSqr(this.enderman);
            return d0 > 256.0D ? false : this.enderman.isLookingAtMe((PlayerEntity)this.target);
         }
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.enderman.getNavigation().stop();
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         this.enderman.getLookControl().setLookAt(this.target.getX(), this.target.getEyeY(), this.target.getZ());
      }
   }

   static class TakeBlockGoal extends Goal {
      private final EndermanEntity enderman;

      public TakeBlockGoal(EndermanEntity pEnderman) {
         this.enderman = pEnderman;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if (this.enderman.getCarriedBlock() != null) {
            return false;
         } else if (!net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.enderman.level, this.enderman)) {
            return false;
         } else {
            return this.enderman.getRandom().nextInt(20) == 0;
         }
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         Random random = this.enderman.getRandom();
         World world = this.enderman.level;
         int i = MathHelper.floor(this.enderman.getX() - 2.0D + random.nextDouble() * 4.0D);
         int j = MathHelper.floor(this.enderman.getY() + random.nextDouble() * 3.0D);
         int k = MathHelper.floor(this.enderman.getZ() - 2.0D + random.nextDouble() * 4.0D);
         BlockPos blockpos = new BlockPos(i, j, k);
         BlockState blockstate = world.getBlockState(blockpos);
         Block block = blockstate.getBlock();
         Vector3d vector3d = new Vector3d((double)MathHelper.floor(this.enderman.getX()) + 0.5D, (double)j + 0.5D, (double)MathHelper.floor(this.enderman.getZ()) + 0.5D);
         Vector3d vector3d1 = new Vector3d((double)i + 0.5D, (double)j + 0.5D, (double)k + 0.5D);
         BlockRayTraceResult blockraytraceresult = world.clip(new RayTraceContext(vector3d, vector3d1, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, this.enderman));
         boolean flag = blockraytraceresult.getBlockPos().equals(blockpos);
         if (block.is(BlockTags.ENDERMAN_HOLDABLE) && flag) {
            world.removeBlock(blockpos, false);
            this.enderman.setCarriedBlock(blockstate.getBlock().defaultBlockState());
         }

      }
   }
}
