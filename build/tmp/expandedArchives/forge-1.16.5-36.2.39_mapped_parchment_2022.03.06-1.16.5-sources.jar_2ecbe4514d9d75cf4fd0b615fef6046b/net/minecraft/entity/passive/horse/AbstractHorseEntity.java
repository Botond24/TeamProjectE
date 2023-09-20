package net.minecraft.entity.passive.horse;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IEquipable;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.BreedGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.PanicGoal;
import net.minecraft.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.HandSide;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.TransportationHelper;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractHorseEntity extends AnimalEntity implements IInventoryChangedListener, IJumpingMount, IEquipable {
   private static final Predicate<LivingEntity> PARENT_HORSE_SELECTOR = (p_213617_0_) -> {
      return p_213617_0_ instanceof AbstractHorseEntity && ((AbstractHorseEntity)p_213617_0_).isBred();
   };
   private static final EntityPredicate MOMMY_TARGETING = (new EntityPredicate()).range(16.0D).allowInvulnerable().allowSameTeam().allowUnseeable().selector(PARENT_HORSE_SELECTOR);
   private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.WHEAT, Items.SUGAR, Blocks.HAY_BLOCK.asItem(), Items.APPLE, Items.GOLDEN_CARROT, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE);
   private static final DataParameter<Byte> DATA_ID_FLAGS = EntityDataManager.defineId(AbstractHorseEntity.class, DataSerializers.BYTE);
   private static final DataParameter<Optional<UUID>> DATA_ID_OWNER_UUID = EntityDataManager.defineId(AbstractHorseEntity.class, DataSerializers.OPTIONAL_UUID);
   private int eatingCounter;
   private int mouthCounter;
   private int standCounter;
   public int tailCounter;
   public int sprintCounter;
   protected boolean isJumping;
   protected Inventory inventory;
   /** The higher this value, the more likely the horse is to be tamed next time a player rides it. */
   protected int temper;
   protected float playerJumpPendingScale;
   private boolean allowStandSliding;
   private float eatAnim;
   private float eatAnimO;
   private float standAnim;
   private float standAnimO;
   private float mouthAnim;
   private float mouthAnimO;
   protected boolean canGallop = true;
   /** Used to determine the sound that the horse should make when it steps */
   protected int gallopSoundCounter;

   protected AbstractHorseEntity(EntityType<? extends AbstractHorseEntity> p_i48563_1_, World p_i48563_2_) {
      super(p_i48563_1_, p_i48563_2_);
      this.maxUpStep = 1.0F;
      this.createInventory();
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(1, new PanicGoal(this, 1.2D));
      this.goalSelector.addGoal(1, new RunAroundLikeCrazyGoal(this, 1.2D));
      this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D, AbstractHorseEntity.class));
      this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.0D));
      this.goalSelector.addGoal(6, new WaterAvoidingRandomWalkingGoal(this, 0.7D));
      this.goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 6.0F));
      this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
      this.addBehaviourGoals();
   }

   protected void addBehaviourGoals() {
      this.goalSelector.addGoal(0, new SwimGoal(this));
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_ID_FLAGS, (byte)0);
      this.entityData.define(DATA_ID_OWNER_UUID, Optional.empty());
   }

   protected boolean getFlag(int pFlagId) {
      return (this.entityData.get(DATA_ID_FLAGS) & pFlagId) != 0;
   }

   protected void setFlag(int pFlagId, boolean pValue) {
      byte b0 = this.entityData.get(DATA_ID_FLAGS);
      if (pValue) {
         this.entityData.set(DATA_ID_FLAGS, (byte)(b0 | pFlagId));
      } else {
         this.entityData.set(DATA_ID_FLAGS, (byte)(b0 & ~pFlagId));
      }

   }

   public boolean isTamed() {
      return this.getFlag(2);
   }

   @Nullable
   public UUID getOwnerUUID() {
      return this.entityData.get(DATA_ID_OWNER_UUID).orElse((UUID)null);
   }

   public void setOwnerUUID(@Nullable UUID pUuid) {
      this.entityData.set(DATA_ID_OWNER_UUID, Optional.ofNullable(pUuid));
   }

   public boolean isJumping() {
      return this.isJumping;
   }

   public void setTamed(boolean pTamed) {
      this.setFlag(2, pTamed);
   }

   public void setIsJumping(boolean pJumping) {
      this.isJumping = pJumping;
   }

   protected void onLeashDistance(float pDistance) {
      if (pDistance > 6.0F && this.isEating()) {
         this.setEating(false);
      }

   }

   public boolean isEating() {
      return this.getFlag(16);
   }

   public boolean isStanding() {
      return this.getFlag(32);
   }

   public boolean isBred() {
      return this.getFlag(8);
   }

   public void setBred(boolean pBreeding) {
      this.setFlag(8, pBreeding);
   }

   public boolean isSaddleable() {
      return this.isAlive() && !this.isBaby() && this.isTamed();
   }

   public void equipSaddle(@Nullable SoundCategory pSource) {
      this.inventory.setItem(0, new ItemStack(Items.SADDLE));
      if (pSource != null) {
         this.level.playSound((PlayerEntity)null, this, SoundEvents.HORSE_SADDLE, pSource, 0.5F, 1.0F);
      }

   }

   public boolean isSaddled() {
      return this.getFlag(4);
   }

   public int getTemper() {
      return this.temper;
   }

   public void setTemper(int pTemper) {
      this.temper = pTemper;
   }

   public int modifyTemper(int pAddedTemper) {
      int i = MathHelper.clamp(this.getTemper() + pAddedTemper, 0, this.getMaxTemper());
      this.setTemper(i);
      return i;
   }

   /**
    * Returns true if this entity should push and be pushed by other entities when colliding.
    */
   public boolean isPushable() {
      return !this.isVehicle();
   }

   private void eating() {
      this.openMouth();
      if (!this.isSilent()) {
         SoundEvent soundevent = this.getEatingSound();
         if (soundevent != null) {
            this.level.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), soundevent, this.getSoundSource(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
         }
      }

   }

   public boolean causeFallDamage(float pFallDistance, float pDamageMultiplier) {
      if (pFallDistance > 1.0F) {
         this.playSound(SoundEvents.HORSE_LAND, 0.4F, 1.0F);
      }

      int i = this.calculateFallDamage(pFallDistance, pDamageMultiplier);
      if (i <= 0) {
         return false;
      } else {
         this.hurt(DamageSource.FALL, (float)i);
         if (this.isVehicle()) {
            for(Entity entity : this.getIndirectPassengers()) {
               entity.hurt(DamageSource.FALL, (float)i);
            }
         }

         this.playBlockFallSound();
         return true;
      }
   }

   protected int calculateFallDamage(float pDistance, float pDamageMultiplier) {
      return MathHelper.ceil((pDistance * 0.5F - 3.0F) * pDamageMultiplier);
   }

   protected int getInventorySize() {
      return 2;
   }

   protected void createInventory() {
      Inventory inventory = this.inventory;
      this.inventory = new Inventory(this.getInventorySize());
      if (inventory != null) {
         inventory.removeListener(this);
         int i = Math.min(inventory.getContainerSize(), this.inventory.getContainerSize());

         for(int j = 0; j < i; ++j) {
            ItemStack itemstack = inventory.getItem(j);
            if (!itemstack.isEmpty()) {
               this.inventory.setItem(j, itemstack.copy());
            }
         }
      }

      this.inventory.addListener(this);
      this.updateContainerEquipment();
      this.itemHandler = net.minecraftforge.common.util.LazyOptional.of(() -> new net.minecraftforge.items.wrapper.InvWrapper(this.inventory));
   }

   protected void updateContainerEquipment() {
      if (!this.level.isClientSide) {
         this.setFlag(4, !this.inventory.getItem(0).isEmpty());
      }
   }

   /**
    * Called by InventoryBasic.onInventoryChanged() on a array that is never filled.
    */
   public void containerChanged(IInventory pInvBasic) {
      boolean flag = this.isSaddled();
      this.updateContainerEquipment();
      if (this.tickCount > 20 && !flag && this.isSaddled()) {
         this.playSound(SoundEvents.HORSE_SADDLE, 0.5F, 1.0F);
      }

   }

   public double getCustomJump() {
      return this.getAttributeValue(Attributes.JUMP_STRENGTH);
   }

   @Nullable
   protected SoundEvent getEatingSound() {
      return null;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return null;
   }

   @Nullable
   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      if (this.random.nextInt(3) == 0) {
         this.stand();
      }

      return null;
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      if (this.random.nextInt(10) == 0 && !this.isImmobile()) {
         this.stand();
      }

      return null;
   }

   @Nullable
   protected SoundEvent getAngrySound() {
      this.stand();
      return null;
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      if (!pBlock.getMaterial().isLiquid()) {
         BlockState blockstate = this.level.getBlockState(pPos.above());
         SoundType soundtype = pBlock.getSoundType(level, pPos, this);
         if (blockstate.is(Blocks.SNOW)) {
            soundtype = blockstate.getSoundType(level, pPos, this);
         }

         if (this.isVehicle() && this.canGallop) {
            ++this.gallopSoundCounter;
            if (this.gallopSoundCounter > 5 && this.gallopSoundCounter % 3 == 0) {
               this.playGallopSound(soundtype);
            } else if (this.gallopSoundCounter <= 5) {
               this.playSound(SoundEvents.HORSE_STEP_WOOD, soundtype.getVolume() * 0.15F, soundtype.getPitch());
            }
         } else if (soundtype == SoundType.WOOD) {
            this.playSound(SoundEvents.HORSE_STEP_WOOD, soundtype.getVolume() * 0.15F, soundtype.getPitch());
         } else {
            this.playSound(SoundEvents.HORSE_STEP, soundtype.getVolume() * 0.15F, soundtype.getPitch());
         }

      }
   }

   protected void playGallopSound(SoundType pSoundType) {
      this.playSound(SoundEvents.HORSE_GALLOP, pSoundType.getVolume() * 0.15F, pSoundType.getPitch());
   }

   public static AttributeModifierMap.MutableAttribute createBaseHorseAttributes() {
      return MobEntity.createMobAttributes().add(Attributes.JUMP_STRENGTH).add(Attributes.MAX_HEALTH, 53.0D).add(Attributes.MOVEMENT_SPEED, (double)0.225F);
   }

   /**
    * Will return how many at most can spawn in a chunk at once.
    */
   public int getMaxSpawnClusterSize() {
      return 6;
   }

   public int getMaxTemper() {
      return 100;
   }

   /**
    * Returns the volume for the sounds this mob makes.
    */
   protected float getSoundVolume() {
      return 0.8F;
   }

   /**
    * Get number of ticks, at least during which the living entity will be silent.
    */
   public int getAmbientSoundInterval() {
      return 400;
   }

   public void openInventory(PlayerEntity pPlayer) {
      if (!this.level.isClientSide && (!this.isVehicle() || this.hasPassenger(pPlayer)) && this.isTamed()) {
         pPlayer.openHorseInventory(this, this.inventory);
      }

   }

   public ActionResultType fedFood(PlayerEntity pPlayer, ItemStack pStack) {
      boolean flag = this.handleEating(pPlayer, pStack);
      if (!pPlayer.abilities.instabuild) {
         pStack.shrink(1);
      }

      if (this.level.isClientSide) {
         return ActionResultType.CONSUME;
      } else {
         return flag ? ActionResultType.SUCCESS : ActionResultType.PASS;
      }
   }

   protected boolean handleEating(PlayerEntity pPlayer, ItemStack pStack) {
      boolean flag = false;
      float f = 0.0F;
      int i = 0;
      int j = 0;
      Item item = pStack.getItem();
      if (item == Items.WHEAT) {
         f = 2.0F;
         i = 20;
         j = 3;
      } else if (item == Items.SUGAR) {
         f = 1.0F;
         i = 30;
         j = 3;
      } else if (item == Blocks.HAY_BLOCK.asItem()) {
         f = 20.0F;
         i = 180;
      } else if (item == Items.APPLE) {
         f = 3.0F;
         i = 60;
         j = 3;
      } else if (item == Items.GOLDEN_CARROT) {
         f = 4.0F;
         i = 60;
         j = 5;
         if (!this.level.isClientSide && this.isTamed() && this.getAge() == 0 && !this.isInLove()) {
            flag = true;
            this.setInLove(pPlayer);
         }
      } else if (item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE) {
         f = 10.0F;
         i = 240;
         j = 10;
         if (!this.level.isClientSide && this.isTamed() && this.getAge() == 0 && !this.isInLove()) {
            flag = true;
            this.setInLove(pPlayer);
         }
      }

      if (this.getHealth() < this.getMaxHealth() && f > 0.0F) {
         this.heal(f);
         flag = true;
      }

      if (this.isBaby() && i > 0) {
         this.level.addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), 0.0D, 0.0D, 0.0D);
         if (!this.level.isClientSide) {
            this.ageUp(i);
         }

         flag = true;
      }

      if (j > 0 && (flag || !this.isTamed()) && this.getTemper() < this.getMaxTemper()) {
         flag = true;
         if (!this.level.isClientSide) {
            this.modifyTemper(j);
         }
      }

      if (flag) {
         this.eating();
      }

      return flag;
   }

   protected void doPlayerRide(PlayerEntity pPlayer) {
      this.setEating(false);
      this.setStanding(false);
      if (!this.level.isClientSide) {
         pPlayer.yRot = this.yRot;
         pPlayer.xRot = this.xRot;
         pPlayer.startRiding(this);
      }

   }

   /**
    * Dead and sleeping entities cannot move
    */
   protected boolean isImmobile() {
      return super.isImmobile() && this.isVehicle() && this.isSaddled() || this.isEating() || this.isStanding();
   }

   /**
    * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
    * the animal type)
    */
   public boolean isFood(ItemStack pStack) {
      return FOOD_ITEMS.test(pStack);
   }

   private void moveTail() {
      this.tailCounter = 1;
   }

   protected void dropEquipment() {
      super.dropEquipment();
      if (this.inventory != null) {
         for(int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemstack = this.inventory.getItem(i);
            if (!itemstack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemstack)) {
               this.spawnAtLocation(itemstack);
            }
         }

      }
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      if (this.random.nextInt(200) == 0) {
         this.moveTail();
      }

      super.aiStep();
      if (!this.level.isClientSide && this.isAlive()) {
         if (this.random.nextInt(900) == 0 && this.deathTime == 0) {
            this.heal(1.0F);
         }

         if (this.canEatGrass()) {
            if (!this.isEating() && !this.isVehicle() && this.random.nextInt(300) == 0 && this.level.getBlockState(this.blockPosition().below()).is(Blocks.GRASS_BLOCK)) {
               this.setEating(true);
            }

            if (this.isEating() && ++this.eatingCounter > 50) {
               this.eatingCounter = 0;
               this.setEating(false);
            }
         }

         this.followMommy();
      }
   }

   protected void followMommy() {
      if (this.isBred() && this.isBaby() && !this.isEating()) {
         LivingEntity livingentity = this.level.getNearestEntity(AbstractHorseEntity.class, MOMMY_TARGETING, this, this.getX(), this.getY(), this.getZ(), this.getBoundingBox().inflate(16.0D));
         if (livingentity != null && this.distanceToSqr(livingentity) > 4.0D) {
            this.navigation.createPath(livingentity, 0);
         }
      }

   }

   public boolean canEatGrass() {
      return true;
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      if (this.mouthCounter > 0 && ++this.mouthCounter > 30) {
         this.mouthCounter = 0;
         this.setFlag(64, false);
      }

      if ((this.isControlledByLocalInstance() || this.isEffectiveAi()) && this.standCounter > 0 && ++this.standCounter > 20) {
         this.standCounter = 0;
         this.setStanding(false);
      }

      if (this.tailCounter > 0 && ++this.tailCounter > 8) {
         this.tailCounter = 0;
      }

      if (this.sprintCounter > 0) {
         ++this.sprintCounter;
         if (this.sprintCounter > 300) {
            this.sprintCounter = 0;
         }
      }

      this.eatAnimO = this.eatAnim;
      if (this.isEating()) {
         this.eatAnim += (1.0F - this.eatAnim) * 0.4F + 0.05F;
         if (this.eatAnim > 1.0F) {
            this.eatAnim = 1.0F;
         }
      } else {
         this.eatAnim += (0.0F - this.eatAnim) * 0.4F - 0.05F;
         if (this.eatAnim < 0.0F) {
            this.eatAnim = 0.0F;
         }
      }

      this.standAnimO = this.standAnim;
      if (this.isStanding()) {
         this.eatAnim = 0.0F;
         this.eatAnimO = this.eatAnim;
         this.standAnim += (1.0F - this.standAnim) * 0.4F + 0.05F;
         if (this.standAnim > 1.0F) {
            this.standAnim = 1.0F;
         }
      } else {
         this.allowStandSliding = false;
         this.standAnim += (0.8F * this.standAnim * this.standAnim * this.standAnim - this.standAnim) * 0.6F - 0.05F;
         if (this.standAnim < 0.0F) {
            this.standAnim = 0.0F;
         }
      }

      this.mouthAnimO = this.mouthAnim;
      if (this.getFlag(64)) {
         this.mouthAnim += (1.0F - this.mouthAnim) * 0.7F + 0.05F;
         if (this.mouthAnim > 1.0F) {
            this.mouthAnim = 1.0F;
         }
      } else {
         this.mouthAnim += (0.0F - this.mouthAnim) * 0.7F - 0.05F;
         if (this.mouthAnim < 0.0F) {
            this.mouthAnim = 0.0F;
         }
      }

   }

   private void openMouth() {
      if (!this.level.isClientSide) {
         this.mouthCounter = 1;
         this.setFlag(64, true);
      }

   }

   public void setEating(boolean pEating) {
      this.setFlag(16, pEating);
   }

   public void setStanding(boolean pStanding) {
      if (pStanding) {
         this.setEating(false);
      }

      this.setFlag(32, pStanding);
   }

   private void stand() {
      if (this.isControlledByLocalInstance() || this.isEffectiveAi()) {
         this.standCounter = 1;
         this.setStanding(true);
      }

   }

   public void makeMad() {
      if (!this.isStanding()) {
         this.stand();
         SoundEvent soundevent = this.getAngrySound();
         if (soundevent != null) {
            this.playSound(soundevent, this.getSoundVolume(), this.getVoicePitch());
         }
      }

   }

   public boolean tameWithName(PlayerEntity pPlayer) {
      this.setOwnerUUID(pPlayer.getUUID());
      this.setTamed(true);
      if (pPlayer instanceof ServerPlayerEntity) {
         CriteriaTriggers.TAME_ANIMAL.trigger((ServerPlayerEntity)pPlayer, this);
      }

      this.level.broadcastEntityEvent(this, (byte)7);
      return true;
   }

   public void travel(Vector3d pTravelVector) {
      if (this.isAlive()) {
         if (this.isVehicle() && this.canBeControlledByRider() && this.isSaddled()) {
            LivingEntity livingentity = (LivingEntity)this.getControllingPassenger();
            this.yRot = livingentity.yRot;
            this.yRotO = this.yRot;
            this.xRot = livingentity.xRot * 0.5F;
            this.setRot(this.yRot, this.xRot);
            this.yBodyRot = this.yRot;
            this.yHeadRot = this.yBodyRot;
            float f = livingentity.xxa * 0.5F;
            float f1 = livingentity.zza;
            if (f1 <= 0.0F) {
               f1 *= 0.25F;
               this.gallopSoundCounter = 0;
            }

            if (this.onGround && this.playerJumpPendingScale == 0.0F && this.isStanding() && !this.allowStandSliding) {
               f = 0.0F;
               f1 = 0.0F;
            }

            if (this.playerJumpPendingScale > 0.0F && !this.isJumping() && this.onGround) {
               double d0 = this.getCustomJump() * (double)this.playerJumpPendingScale * (double)this.getBlockJumpFactor();
               double d1;
               if (this.hasEffect(Effects.JUMP)) {
                  d1 = d0 + (double)((float)(this.getEffect(Effects.JUMP).getAmplifier() + 1) * 0.1F);
               } else {
                  d1 = d0;
               }

               Vector3d vector3d = this.getDeltaMovement();
               this.setDeltaMovement(vector3d.x, d1, vector3d.z);
               this.setIsJumping(true);
               this.hasImpulse = true;
               net.minecraftforge.common.ForgeHooks.onLivingJump(this);
               if (f1 > 0.0F) {
                  float f2 = MathHelper.sin(this.yRot * ((float)Math.PI / 180F));
                  float f3 = MathHelper.cos(this.yRot * ((float)Math.PI / 180F));
                  this.setDeltaMovement(this.getDeltaMovement().add((double)(-0.4F * f2 * this.playerJumpPendingScale), 0.0D, (double)(0.4F * f3 * this.playerJumpPendingScale)));
               }

               this.playerJumpPendingScale = 0.0F;
            }

            this.flyingSpeed = this.getSpeed() * 0.1F;
            if (this.isControlledByLocalInstance()) {
               this.setSpeed((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED));
               super.travel(new Vector3d((double)f, pTravelVector.y, (double)f1));
            } else if (livingentity instanceof PlayerEntity) {
               this.setDeltaMovement(Vector3d.ZERO);
            }

            if (this.onGround) {
               this.playerJumpPendingScale = 0.0F;
               this.setIsJumping(false);
            }

            this.calculateEntityAnimation(this, false);
         } else {
            this.flyingSpeed = 0.02F;
            super.travel(pTravelVector);
         }
      }
   }

   protected void playJumpSound() {
      this.playSound(SoundEvents.HORSE_JUMP, 0.4F, 1.0F);
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putBoolean("EatingHaystack", this.isEating());
      pCompound.putBoolean("Bred", this.isBred());
      pCompound.putInt("Temper", this.getTemper());
      pCompound.putBoolean("Tame", this.isTamed());
      if (this.getOwnerUUID() != null) {
         pCompound.putUUID("Owner", this.getOwnerUUID());
      }

      if (!this.inventory.getItem(0).isEmpty()) {
         pCompound.put("SaddleItem", this.inventory.getItem(0).save(new CompoundNBT()));
      }

   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setEating(pCompound.getBoolean("EatingHaystack"));
      this.setBred(pCompound.getBoolean("Bred"));
      this.setTemper(pCompound.getInt("Temper"));
      this.setTamed(pCompound.getBoolean("Tame"));
      UUID uuid;
      if (pCompound.hasUUID("Owner")) {
         uuid = pCompound.getUUID("Owner");
      } else {
         String s = pCompound.getString("Owner");
         uuid = PreYggdrasilConverter.convertMobOwnerIfNecessary(this.getServer(), s);
      }

      if (uuid != null) {
         this.setOwnerUUID(uuid);
      }

      if (pCompound.contains("SaddleItem", 10)) {
         ItemStack itemstack = ItemStack.of(pCompound.getCompound("SaddleItem"));
         if (itemstack.getItem() == Items.SADDLE) {
            this.inventory.setItem(0, itemstack);
         }
      }

      this.updateContainerEquipment();
   }

   /**
    * Returns true if the mob is currently able to mate with the specified mob.
    */
   public boolean canMate(AnimalEntity pOtherAnimal) {
      return false;
   }

   /**
    * Return true if the horse entity ready to mate. (no rider, not riding, tame, adult, not steril...)
    */
   protected boolean canParent() {
      return !this.isVehicle() && !this.isPassenger() && this.isTamed() && !this.isBaby() && this.getHealth() >= this.getMaxHealth() && this.isInLove();
   }

   @Nullable
   public AgeableEntity getBreedOffspring(ServerWorld pServerLevel, AgeableEntity pMate) {
      return null;
   }

   protected void setOffspringAttributes(AgeableEntity pMate, AbstractHorseEntity pOffspring) {
      double d0 = this.getAttributeBaseValue(Attributes.MAX_HEALTH) + pMate.getAttributeBaseValue(Attributes.MAX_HEALTH) + (double)this.generateRandomMaxHealth();
      pOffspring.getAttribute(Attributes.MAX_HEALTH).setBaseValue(d0 / 3.0D);
      double d1 = this.getAttributeBaseValue(Attributes.JUMP_STRENGTH) + pMate.getAttributeBaseValue(Attributes.JUMP_STRENGTH) + this.generateRandomJumpStrength();
      pOffspring.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(d1 / 3.0D);
      double d2 = this.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) + pMate.getAttributeBaseValue(Attributes.MOVEMENT_SPEED) + this.generateRandomSpeed();
      pOffspring.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(d2 / 3.0D);
   }

   /**
    * @return true if all the conditions for steering the entity are met. For pigs, this is true if it is being ridden
    * by a player and the player is holding a carrot-on-a-stick
    */
   public boolean canBeControlledByRider() {
      return this.getControllingPassenger() instanceof LivingEntity;
   }

   @OnlyIn(Dist.CLIENT)
   public float getEatAnim(float pPartialTick) {
      return MathHelper.lerp(pPartialTick, this.eatAnimO, this.eatAnim);
   }

   @OnlyIn(Dist.CLIENT)
   public float getStandAnim(float pPartialTick) {
      return MathHelper.lerp(pPartialTick, this.standAnimO, this.standAnim);
   }

   @OnlyIn(Dist.CLIENT)
   public float getMouthAnim(float pPartialTick) {
      return MathHelper.lerp(pPartialTick, this.mouthAnimO, this.mouthAnim);
   }

   @OnlyIn(Dist.CLIENT)
   public void onPlayerJump(int pJumpPower) {
      if (this.isSaddled()) {
         if (pJumpPower < 0) {
            pJumpPower = 0;
         } else {
            this.allowStandSliding = true;
            this.stand();
         }

         if (pJumpPower >= 90) {
            this.playerJumpPendingScale = 1.0F;
         } else {
            this.playerJumpPendingScale = 0.4F + 0.4F * (float)pJumpPower / 90.0F;
         }

      }
   }

   public boolean canJump() {
      return this.isSaddled();
   }

   public void handleStartJump(int pJumpPower) {
      this.allowStandSliding = true;
      this.stand();
      this.playJumpSound();
   }

   public void handleStopJump() {
   }

   /**
    * Spawns particles for the horse entity. par1 tells whether to spawn hearts. If it is false, it spawns smoke."
    */
   @OnlyIn(Dist.CLIENT)
   protected void spawnTamingParticles(boolean pTamed) {
      IParticleData iparticledata = pTamed ? ParticleTypes.HEART : ParticleTypes.SMOKE;

      for(int i = 0; i < 7; ++i) {
         double d0 = this.random.nextGaussian() * 0.02D;
         double d1 = this.random.nextGaussian() * 0.02D;
         double d2 = this.random.nextGaussian() * 0.02D;
         this.level.addParticle(iparticledata, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), d0, d1, d2);
      }

   }

   /**
    * Handles an entity event fired from {@link net.minecraft.world.level.Level#broadcastEntityEvent}.
    */
   @OnlyIn(Dist.CLIENT)
   public void handleEntityEvent(byte pId) {
      if (pId == 7) {
         this.spawnTamingParticles(true);
      } else if (pId == 6) {
         this.spawnTamingParticles(false);
      } else {
         super.handleEntityEvent(pId);
      }

   }

   public void positionRider(Entity pPassenger) {
      super.positionRider(pPassenger);
      if (pPassenger instanceof MobEntity) {
         MobEntity mobentity = (MobEntity)pPassenger;
         this.yBodyRot = mobentity.yBodyRot;
      }

      if (this.standAnimO > 0.0F) {
         float f3 = MathHelper.sin(this.yBodyRot * ((float)Math.PI / 180F));
         float f = MathHelper.cos(this.yBodyRot * ((float)Math.PI / 180F));
         float f1 = 0.7F * this.standAnimO;
         float f2 = 0.15F * this.standAnimO;
         pPassenger.setPos(this.getX() + (double)(f1 * f3), this.getY() + this.getPassengersRidingOffset() + pPassenger.getMyRidingOffset() + (double)f2, this.getZ() - (double)(f1 * f));
         if (pPassenger instanceof LivingEntity) {
            ((LivingEntity)pPassenger).yBodyRot = this.yBodyRot;
         }
      }

   }

   /**
    * Returns randomized max health
    */
   protected float generateRandomMaxHealth() {
      return 15.0F + (float)this.random.nextInt(8) + (float)this.random.nextInt(9);
   }

   /**
    * Returns randomized jump strength
    */
   protected double generateRandomJumpStrength() {
      return (double)0.4F + this.random.nextDouble() * 0.2D + this.random.nextDouble() * 0.2D + this.random.nextDouble() * 0.2D;
   }

   /**
    * Returns randomized movement speed
    */
   protected double generateRandomSpeed() {
      return ((double)0.45F + this.random.nextDouble() * 0.3D + this.random.nextDouble() * 0.3D + this.random.nextDouble() * 0.3D) * 0.25D;
   }

   /**
    * Returns true if this entity should move as if it were on a ladder (either because it's actually on a ladder, or
    * for AI reasons)
    */
   public boolean onClimbable() {
      return false;
   }

   protected float getStandingEyeHeight(Pose pPose, EntitySize pSize) {
      return pSize.height * 0.95F;
   }

   public boolean canWearArmor() {
      return false;
   }

   public boolean isWearingArmor() {
      return !this.getItemBySlot(EquipmentSlotType.CHEST).isEmpty();
   }

   public boolean isArmor(ItemStack pStack) {
      return false;
   }

   public boolean setSlot(int pSlotIndex, ItemStack pStack) {
      int i = pSlotIndex - 400;
      if (i >= 0 && i < 2 && i < this.inventory.getContainerSize()) {
         if (i == 0 && pStack.getItem() != Items.SADDLE) {
            return false;
         } else if (i != 1 || this.canWearArmor() && this.isArmor(pStack)) {
            this.inventory.setItem(i, pStack);
            this.updateContainerEquipment();
            return true;
         } else {
            return false;
         }
      } else {
         int j = pSlotIndex - 500 + 2;
         if (j >= 2 && j < this.inventory.getContainerSize()) {
            this.inventory.setItem(j, pStack);
            return true;
         } else {
            return false;
         }
      }
   }

   /**
    * For vehicles, the first passenger is generally considered the controller and "drives" the vehicle. For example,
    * Pigs, Horses, and Boats are generally "steered" by the controlling passenger.
    */
   @Nullable
   public Entity getControllingPassenger() {
      return this.getPassengers().isEmpty() ? null : this.getPassengers().get(0);
   }

   @Nullable
   private Vector3d getDismountLocationInDirection(Vector3d pDirection, LivingEntity pPassenger) {
      double d0 = this.getX() + pDirection.x;
      double d1 = this.getBoundingBox().minY;
      double d2 = this.getZ() + pDirection.z;
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

      for(Pose pose : pPassenger.getDismountPoses()) {
         blockpos$mutable.set(d0, d1, d2);
         double d3 = this.getBoundingBox().maxY + 0.75D;

         while(true) {
            double d4 = this.level.getBlockFloorHeight(blockpos$mutable);
            if ((double)blockpos$mutable.getY() + d4 > d3) {
               break;
            }

            if (TransportationHelper.isBlockFloorValid(d4)) {
               AxisAlignedBB axisalignedbb = pPassenger.getLocalBoundsForPose(pose);
               Vector3d vector3d = new Vector3d(d0, (double)blockpos$mutable.getY() + d4, d2);
               if (TransportationHelper.canDismountTo(this.level, pPassenger, axisalignedbb.move(vector3d))) {
                  pPassenger.setPose(pose);
                  return vector3d;
               }
            }

            blockpos$mutable.move(Direction.UP);
            if (!((double)blockpos$mutable.getY() < d3)) {
               break;
            }
         }
      }

      return null;
   }

   public Vector3d getDismountLocationForPassenger(LivingEntity pLivingEntity) {
      Vector3d vector3d = getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)pLivingEntity.getBbWidth(), this.yRot + (pLivingEntity.getMainArm() == HandSide.RIGHT ? 90.0F : -90.0F));
      Vector3d vector3d1 = this.getDismountLocationInDirection(vector3d, pLivingEntity);
      if (vector3d1 != null) {
         return vector3d1;
      } else {
         Vector3d vector3d2 = getCollisionHorizontalEscapeVector((double)this.getBbWidth(), (double)pLivingEntity.getBbWidth(), this.yRot + (pLivingEntity.getMainArm() == HandSide.LEFT ? 90.0F : -90.0F));
         Vector3d vector3d3 = this.getDismountLocationInDirection(vector3d2, pLivingEntity);
         return vector3d3 != null ? vector3d3 : this.position();
      }
   }

   protected void randomizeAttributes() {
   }

   @Nullable
   public ILivingEntityData finalizeSpawn(IServerWorld pLevel, DifficultyInstance pDifficulty, SpawnReason pReason, @Nullable ILivingEntityData pSpawnData, @Nullable CompoundNBT pDataTag) {
      if (pSpawnData == null) {
         pSpawnData = new AgeableEntity.AgeableData(0.2F);
      }

      this.randomizeAttributes();
      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   private net.minecraftforge.common.util.LazyOptional<?> itemHandler = null;

   @Override
   public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable net.minecraft.util.Direction facing) {
      if (this.isAlive() && capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && itemHandler != null)
         return itemHandler.cast();
      return super.getCapability(capability, facing);
   }

   @Override
   protected void invalidateCaps() {
      super.invalidateCaps();
      if (itemHandler != null) {
         net.minecraftforge.common.util.LazyOptional<?> oldHandler = itemHandler;
         itemHandler = null;
         oldHandler.invalidate();
      }
   }
}
