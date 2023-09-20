package net.minecraft.entity.passive;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.MovementController;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.ai.goal.BreedGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.GameRules;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PandaEntity extends AnimalEntity {
   private static final DataParameter<Integer> UNHAPPY_COUNTER = EntityDataManager.defineId(PandaEntity.class, DataSerializers.INT);
   private static final DataParameter<Integer> SNEEZE_COUNTER = EntityDataManager.defineId(PandaEntity.class, DataSerializers.INT);
   private static final DataParameter<Integer> EAT_COUNTER = EntityDataManager.defineId(PandaEntity.class, DataSerializers.INT);
   private static final DataParameter<Byte> MAIN_GENE_ID = EntityDataManager.defineId(PandaEntity.class, DataSerializers.BYTE);
   private static final DataParameter<Byte> HIDDEN_GENE_ID = EntityDataManager.defineId(PandaEntity.class, DataSerializers.BYTE);
   private static final DataParameter<Byte> DATA_ID_FLAGS = EntityDataManager.defineId(PandaEntity.class, DataSerializers.BYTE);
   private static final EntityPredicate BREED_TARGETING = (new EntityPredicate()).range(8.0D).allowSameTeam().allowInvulnerable();
   private boolean gotBamboo;
   private boolean didBite;
   public int rollCounter;
   private Vector3d rollDelta;
   private float sitAmount;
   private float sitAmountO;
   private float onBackAmount;
   private float onBackAmountO;
   private float rollAmount;
   private float rollAmountO;
   private PandaEntity.WatchGoal lookAtPlayerGoal;
   private static final Predicate<ItemEntity> PANDA_ITEMS = (p_213575_0_) -> {
      Item item = p_213575_0_.getItem().getItem();
      return (item == Blocks.BAMBOO.asItem() || item == Blocks.CAKE.asItem()) && p_213575_0_.isAlive() && !p_213575_0_.hasPickUpDelay();
   };

   public PandaEntity(EntityType<? extends PandaEntity> p_i50252_1_, World p_i50252_2_) {
      super(p_i50252_1_, p_i50252_2_);
      this.moveControl = new PandaEntity.MoveHelperController(this);
      if (!this.isBaby()) {
         this.setCanPickUpLoot(true);
      }

   }

   public boolean canTakeItem(ItemStack pItemstack) {
      EquipmentSlotType equipmentslottype = MobEntity.getEquipmentSlotForItem(pItemstack);
      if (!this.getItemBySlot(equipmentslottype).isEmpty()) {
         return false;
      } else {
         return equipmentslottype == EquipmentSlotType.MAINHAND && super.canTakeItem(pItemstack);
      }
   }

   public int getUnhappyCounter() {
      return this.entityData.get(UNHAPPY_COUNTER);
   }

   public void setUnhappyCounter(int pUnhappyCounter) {
      this.entityData.set(UNHAPPY_COUNTER, pUnhappyCounter);
   }

   public boolean isSneezing() {
      return this.getFlag(2);
   }

   public boolean isSitting() {
      return this.getFlag(8);
   }

   public void sit(boolean pSitting) {
      this.setFlag(8, pSitting);
   }

   public boolean isOnBack() {
      return this.getFlag(16);
   }

   public void setOnBack(boolean pOnBack) {
      this.setFlag(16, pOnBack);
   }

   public boolean isEating() {
      return this.entityData.get(EAT_COUNTER) > 0;
   }

   public void eat(boolean pEating) {
      this.entityData.set(EAT_COUNTER, pEating ? 1 : 0);
   }

   private int getEatCounter() {
      return this.entityData.get(EAT_COUNTER);
   }

   private void setEatCounter(int pEatCounter) {
      this.entityData.set(EAT_COUNTER, pEatCounter);
   }

   public void sneeze(boolean pSneezing) {
      this.setFlag(2, pSneezing);
      if (!pSneezing) {
         this.setSneezeCounter(0);
      }

   }

   public int getSneezeCounter() {
      return this.entityData.get(SNEEZE_COUNTER);
   }

   public void setSneezeCounter(int pSneezeCounter) {
      this.entityData.set(SNEEZE_COUNTER, pSneezeCounter);
   }

   public PandaEntity.Gene getMainGene() {
      return PandaEntity.Gene.byId(this.entityData.get(MAIN_GENE_ID));
   }

   public void setMainGene(PandaEntity.Gene pPandaType) {
      if (pPandaType.getId() > 6) {
         pPandaType = PandaEntity.Gene.getRandom(this.random);
      }

      this.entityData.set(MAIN_GENE_ID, (byte)pPandaType.getId());
   }

   public PandaEntity.Gene getHiddenGene() {
      return PandaEntity.Gene.byId(this.entityData.get(HIDDEN_GENE_ID));
   }

   public void setHiddenGene(PandaEntity.Gene pPandaType) {
      if (pPandaType.getId() > 6) {
         pPandaType = PandaEntity.Gene.getRandom(this.random);
      }

      this.entityData.set(HIDDEN_GENE_ID, (byte)pPandaType.getId());
   }

   public boolean isRolling() {
      return this.getFlag(4);
   }

   public void roll(boolean pRolling) {
      this.setFlag(4, pRolling);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(UNHAPPY_COUNTER, 0);
      this.entityData.define(SNEEZE_COUNTER, 0);
      this.entityData.define(MAIN_GENE_ID, (byte)0);
      this.entityData.define(HIDDEN_GENE_ID, (byte)0);
      this.entityData.define(DATA_ID_FLAGS, (byte)0);
      this.entityData.define(EAT_COUNTER, 0);
   }

   private boolean getFlag(int pFlagId) {
      return (this.entityData.get(DATA_ID_FLAGS) & pFlagId) != 0;
   }

   private void setFlag(int pFlagId, boolean pValue) {
      byte b0 = this.entityData.get(DATA_ID_FLAGS);
      if (pValue) {
         this.entityData.set(DATA_ID_FLAGS, (byte)(b0 | pFlagId));
      } else {
         this.entityData.set(DATA_ID_FLAGS, (byte)(b0 & ~pFlagId));
      }

   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putString("MainGene", this.getMainGene().getName());
      pCompound.putString("HiddenGene", this.getHiddenGene().getName());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setMainGene(PandaEntity.Gene.byName(pCompound.getString("MainGene")));
      this.setHiddenGene(PandaEntity.Gene.byName(pCompound.getString("HiddenGene")));
   }

   @Nullable
   public AgeableEntity getBreedOffspring(ServerWorld pServerLevel, AgeableEntity pMate) {
      PandaEntity pandaentity = EntityType.PANDA.create(pServerLevel);
      if (pMate instanceof PandaEntity) {
         pandaentity.setGeneFromParents(this, (PandaEntity)pMate);
      }

      pandaentity.setAttributes();
      return pandaentity;
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new SwimGoal(this));
      this.goalSelector.addGoal(2, new PandaEntity.PanicGoal(this, 2.0D));
      this.goalSelector.addGoal(2, new PandaEntity.MateGoal(this, 1.0D));
      this.goalSelector.addGoal(3, new PandaEntity.AttackGoal(this, (double)1.2F, true));
      this.goalSelector.addGoal(4, new TemptGoal(this, 1.0D, Ingredient.of(Blocks.BAMBOO.asItem()), false));
      this.goalSelector.addGoal(6, new PandaEntity.AvoidGoal<>(this, PlayerEntity.class, 8.0F, 2.0D, 2.0D));
      this.goalSelector.addGoal(6, new PandaEntity.AvoidGoal<>(this, MonsterEntity.class, 4.0F, 2.0D, 2.0D));
      this.goalSelector.addGoal(7, new PandaEntity.SitGoal());
      this.goalSelector.addGoal(8, new PandaEntity.LieBackGoal(this));
      this.goalSelector.addGoal(8, new PandaEntity.ChildPlayGoal(this));
      this.lookAtPlayerGoal = new PandaEntity.WatchGoal(this, PlayerEntity.class, 6.0F);
      this.goalSelector.addGoal(9, this.lookAtPlayerGoal);
      this.goalSelector.addGoal(10, new LookRandomlyGoal(this));
      this.goalSelector.addGoal(12, new PandaEntity.RollGoal(this));
      this.goalSelector.addGoal(13, new FollowParentGoal(this, 1.25D));
      this.goalSelector.addGoal(14, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
      this.targetSelector.addGoal(1, (new PandaEntity.RevengeGoal(this)).setAlertOthers(new Class[0]));
   }

   public static AttributeModifierMap.MutableAttribute createAttributes() {
      return MobEntity.createMobAttributes().add(Attributes.MOVEMENT_SPEED, (double)0.15F).add(Attributes.ATTACK_DAMAGE, 6.0D);
   }

   public PandaEntity.Gene getVariant() {
      return PandaEntity.Gene.getVariantFromGenes(this.getMainGene(), this.getHiddenGene());
   }

   public boolean isLazy() {
      return this.getVariant() == PandaEntity.Gene.LAZY;
   }

   public boolean isWorried() {
      return this.getVariant() == PandaEntity.Gene.WORRIED;
   }

   public boolean isPlayful() {
      return this.getVariant() == PandaEntity.Gene.PLAYFUL;
   }

   public boolean isWeak() {
      return this.getVariant() == PandaEntity.Gene.WEAK;
   }

   public boolean isAggressive() {
      return this.getVariant() == PandaEntity.Gene.AGGRESSIVE;
   }

   public boolean canBeLeashed(PlayerEntity pPlayer) {
      return false;
   }

   public boolean doHurtTarget(Entity pEntity) {
      this.playSound(SoundEvents.PANDA_BITE, 1.0F, 1.0F);
      if (!this.isAggressive()) {
         this.didBite = true;
      }

      return super.doHurtTarget(pEntity);
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      if (this.isWorried()) {
         if (this.level.isThundering() && !this.isInWater()) {
            this.sit(true);
            this.eat(false);
         } else if (!this.isEating()) {
            this.sit(false);
         }
      }

      if (this.getTarget() == null) {
         this.gotBamboo = false;
         this.didBite = false;
      }

      if (this.getUnhappyCounter() > 0) {
         if (this.getTarget() != null) {
            this.lookAt(this.getTarget(), 90.0F, 90.0F);
         }

         if (this.getUnhappyCounter() == 29 || this.getUnhappyCounter() == 14) {
            this.playSound(SoundEvents.PANDA_CANT_BREED, 1.0F, 1.0F);
         }

         this.setUnhappyCounter(this.getUnhappyCounter() - 1);
      }

      if (this.isSneezing()) {
         this.setSneezeCounter(this.getSneezeCounter() + 1);
         if (this.getSneezeCounter() > 20) {
            this.sneeze(false);
            this.afterSneeze();
         } else if (this.getSneezeCounter() == 1) {
            this.playSound(SoundEvents.PANDA_PRE_SNEEZE, 1.0F, 1.0F);
         }
      }

      if (this.isRolling()) {
         this.handleRoll();
      } else {
         this.rollCounter = 0;
      }

      if (this.isSitting()) {
         this.xRot = 0.0F;
      }

      this.updateSitAmount();
      this.handleEating();
      this.updateOnBackAnimation();
      this.updateRollAmount();
   }

   public boolean isScared() {
      return this.isWorried() && this.level.isThundering();
   }

   private void handleEating() {
      if (!this.isEating() && this.isSitting() && !this.isScared() && !this.getItemBySlot(EquipmentSlotType.MAINHAND).isEmpty() && this.random.nextInt(80) == 1) {
         this.eat(true);
      } else if (this.getItemBySlot(EquipmentSlotType.MAINHAND).isEmpty() || !this.isSitting()) {
         this.eat(false);
      }

      if (this.isEating()) {
         this.addEatingParticles();
         if (!this.level.isClientSide && this.getEatCounter() > 80 && this.random.nextInt(20) == 1) {
            if (this.getEatCounter() > 100 && this.isFoodOrCake(this.getItemBySlot(EquipmentSlotType.MAINHAND))) {
               if (!this.level.isClientSide) {
                  this.setItemSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
               }

               this.sit(false);
            }

            this.eat(false);
            return;
         }

         this.setEatCounter(this.getEatCounter() + 1);
      }

   }

   private void addEatingParticles() {
      if (this.getEatCounter() % 5 == 0) {
         this.playSound(SoundEvents.PANDA_EAT, 0.5F + 0.5F * (float)this.random.nextInt(2), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);

         for(int i = 0; i < 6; ++i) {
            Vector3d vector3d = new Vector3d(((double)this.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, ((double)this.random.nextFloat() - 0.5D) * 0.1D);
            vector3d = vector3d.xRot(-this.xRot * ((float)Math.PI / 180F));
            vector3d = vector3d.yRot(-this.yRot * ((float)Math.PI / 180F));
            double d0 = (double)(-this.random.nextFloat()) * 0.6D - 0.3D;
            Vector3d vector3d1 = new Vector3d(((double)this.random.nextFloat() - 0.5D) * 0.8D, d0, 1.0D + ((double)this.random.nextFloat() - 0.5D) * 0.4D);
            vector3d1 = vector3d1.yRot(-this.yBodyRot * ((float)Math.PI / 180F));
            vector3d1 = vector3d1.add(this.getX(), this.getEyeY() + 1.0D, this.getZ());
            this.level.addParticle(new ItemParticleData(ParticleTypes.ITEM, this.getItemBySlot(EquipmentSlotType.MAINHAND)), vector3d1.x, vector3d1.y, vector3d1.z, vector3d.x, vector3d.y + 0.05D, vector3d.z);
         }
      }

   }

   private void updateSitAmount() {
      this.sitAmountO = this.sitAmount;
      if (this.isSitting()) {
         this.sitAmount = Math.min(1.0F, this.sitAmount + 0.15F);
      } else {
         this.sitAmount = Math.max(0.0F, this.sitAmount - 0.19F);
      }

   }

   private void updateOnBackAnimation() {
      this.onBackAmountO = this.onBackAmount;
      if (this.isOnBack()) {
         this.onBackAmount = Math.min(1.0F, this.onBackAmount + 0.15F);
      } else {
         this.onBackAmount = Math.max(0.0F, this.onBackAmount - 0.19F);
      }

   }

   private void updateRollAmount() {
      this.rollAmountO = this.rollAmount;
      if (this.isRolling()) {
         this.rollAmount = Math.min(1.0F, this.rollAmount + 0.15F);
      } else {
         this.rollAmount = Math.max(0.0F, this.rollAmount - 0.19F);
      }

   }

   @OnlyIn(Dist.CLIENT)
   public float getSitAmount(float pPartialTick) {
      return MathHelper.lerp(pPartialTick, this.sitAmountO, this.sitAmount);
   }

   @OnlyIn(Dist.CLIENT)
   public float getLieOnBackAmount(float pPartialTick) {
      return MathHelper.lerp(pPartialTick, this.onBackAmountO, this.onBackAmount);
   }

   @OnlyIn(Dist.CLIENT)
   public float getRollAmount(float pPartialTick) {
      return MathHelper.lerp(pPartialTick, this.rollAmountO, this.rollAmount);
   }

   private void handleRoll() {
      ++this.rollCounter;
      if (this.rollCounter > 32) {
         this.roll(false);
      } else {
         if (!this.level.isClientSide) {
            Vector3d vector3d = this.getDeltaMovement();
            if (this.rollCounter == 1) {
               float f = this.yRot * ((float)Math.PI / 180F);
               float f1 = this.isBaby() ? 0.1F : 0.2F;
               this.rollDelta = new Vector3d(vector3d.x + (double)(-MathHelper.sin(f) * f1), 0.0D, vector3d.z + (double)(MathHelper.cos(f) * f1));
               this.setDeltaMovement(this.rollDelta.add(0.0D, 0.27D, 0.0D));
            } else if ((float)this.rollCounter != 7.0F && (float)this.rollCounter != 15.0F && (float)this.rollCounter != 23.0F) {
               this.setDeltaMovement(this.rollDelta.x, vector3d.y, this.rollDelta.z);
            } else {
               this.setDeltaMovement(0.0D, this.onGround ? 0.27D : vector3d.y, 0.0D);
            }
         }

      }
   }

   private void afterSneeze() {
      Vector3d vector3d = this.getDeltaMovement();
      this.level.addParticle(ParticleTypes.SNEEZE, this.getX() - (double)(this.getBbWidth() + 1.0F) * 0.5D * (double)MathHelper.sin(this.yBodyRot * ((float)Math.PI / 180F)), this.getEyeY() - (double)0.1F, this.getZ() + (double)(this.getBbWidth() + 1.0F) * 0.5D * (double)MathHelper.cos(this.yBodyRot * ((float)Math.PI / 180F)), vector3d.x, 0.0D, vector3d.z);
      this.playSound(SoundEvents.PANDA_SNEEZE, 1.0F, 1.0F);

      for(PandaEntity pandaentity : this.level.getEntitiesOfClass(PandaEntity.class, this.getBoundingBox().inflate(10.0D))) {
         if (!pandaentity.isBaby() && pandaentity.onGround && !pandaentity.isInWater() && pandaentity.canPerformAction()) {
            pandaentity.jumpFromGround();
         }
      }

      if (!this.level.isClientSide() && this.random.nextInt(700) == 0 && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
         this.spawnAtLocation(Items.SLIME_BALL);
      }

   }

   /**
    * Tests if this entity should pickup a weapon or an armor. Entity drops current weapon or armor if the new one is
    * better.
    */
   protected void pickUpItem(ItemEntity pItemEntity) {
      if (this.getItemBySlot(EquipmentSlotType.MAINHAND).isEmpty() && PANDA_ITEMS.test(pItemEntity)) {
         this.onItemPickup(pItemEntity);
         ItemStack itemstack = pItemEntity.getItem();
         this.setItemSlot(EquipmentSlotType.MAINHAND, itemstack);
         this.handDropChances[EquipmentSlotType.MAINHAND.getIndex()] = 2.0F;
         this.take(pItemEntity, itemstack.getCount());
         pItemEntity.remove();
      }

   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      this.sit(false);
      return super.hurt(pSource, pAmount);
   }

   @Nullable
   public ILivingEntityData finalizeSpawn(IServerWorld pLevel, DifficultyInstance pDifficulty, SpawnReason pReason, @Nullable ILivingEntityData pSpawnData, @Nullable CompoundNBT pDataTag) {
      this.setMainGene(PandaEntity.Gene.getRandom(this.random));
      this.setHiddenGene(PandaEntity.Gene.getRandom(this.random));
      this.setAttributes();
      if (pSpawnData == null) {
         pSpawnData = new AgeableEntity.AgeableData(0.2F);
      }

      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   public void setGeneFromParents(PandaEntity pFather, @Nullable PandaEntity pMother) {
      if (pMother == null) {
         if (this.random.nextBoolean()) {
            this.setMainGene(pFather.getOneOfGenesRandomly());
            this.setHiddenGene(PandaEntity.Gene.getRandom(this.random));
         } else {
            this.setMainGene(PandaEntity.Gene.getRandom(this.random));
            this.setHiddenGene(pFather.getOneOfGenesRandomly());
         }
      } else if (this.random.nextBoolean()) {
         this.setMainGene(pFather.getOneOfGenesRandomly());
         this.setHiddenGene(pMother.getOneOfGenesRandomly());
      } else {
         this.setMainGene(pMother.getOneOfGenesRandomly());
         this.setHiddenGene(pFather.getOneOfGenesRandomly());
      }

      if (this.random.nextInt(32) == 0) {
         this.setMainGene(PandaEntity.Gene.getRandom(this.random));
      }

      if (this.random.nextInt(32) == 0) {
         this.setHiddenGene(PandaEntity.Gene.getRandom(this.random));
      }

   }

   private PandaEntity.Gene getOneOfGenesRandomly() {
      return this.random.nextBoolean() ? this.getMainGene() : this.getHiddenGene();
   }

   public void setAttributes() {
      if (this.isWeak()) {
         this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10.0D);
      }

      if (this.isLazy()) {
         this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double)0.07F);
      }

   }

   private void tryToSit() {
      if (!this.isInWater()) {
         this.setZza(0.0F);
         this.getNavigation().stop();
         this.sit(true);
      }

   }

   public ActionResultType mobInteract(PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (this.isScared()) {
         return ActionResultType.PASS;
      } else if (this.isOnBack()) {
         this.setOnBack(false);
         return ActionResultType.sidedSuccess(this.level.isClientSide);
      } else if (this.isFood(itemstack)) {
         if (this.getTarget() != null) {
            this.gotBamboo = true;
         }

         if (this.isBaby()) {
            this.usePlayerItem(pPlayer, itemstack);
            this.ageUp((int)((float)(-this.getAge() / 20) * 0.1F), true);
         } else if (!this.level.isClientSide && this.getAge() == 0 && this.canFallInLove()) {
            this.usePlayerItem(pPlayer, itemstack);
            this.setInLove(pPlayer);
         } else {
            if (this.level.isClientSide || this.isSitting() || this.isInWater()) {
               return ActionResultType.PASS;
            }

            this.tryToSit();
            this.eat(true);
            ItemStack itemstack1 = this.getItemBySlot(EquipmentSlotType.MAINHAND);
            if (!itemstack1.isEmpty() && !pPlayer.abilities.instabuild) {
               this.spawnAtLocation(itemstack1);
            }

            this.setItemSlot(EquipmentSlotType.MAINHAND, new ItemStack(itemstack.getItem(), 1));
            this.usePlayerItem(pPlayer, itemstack);
         }

         return ActionResultType.SUCCESS;
      } else {
         return ActionResultType.PASS;
      }
   }

   @Nullable
   protected SoundEvent getAmbientSound() {
      if (this.isAggressive()) {
         return SoundEvents.PANDA_AGGRESSIVE_AMBIENT;
      } else {
         return this.isWorried() ? SoundEvents.PANDA_WORRIED_AMBIENT : SoundEvents.PANDA_AMBIENT;
      }
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      this.playSound(SoundEvents.PANDA_STEP, 0.15F, 1.0F);
   }

   /**
    * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
    * the animal type)
    */
   public boolean isFood(ItemStack pStack) {
      return pStack.getItem() == Blocks.BAMBOO.asItem();
   }

   private boolean isFoodOrCake(ItemStack pStack) {
      return this.isFood(pStack) || pStack.getItem() == Blocks.CAKE.asItem();
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return SoundEvents.PANDA_DEATH;
   }

   @Nullable
   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.PANDA_HURT;
   }

   public boolean canPerformAction() {
      return !this.isOnBack() && !this.isScared() && !this.isEating() && !this.isRolling() && !this.isSitting();
   }

   static class AttackGoal extends MeleeAttackGoal {
      private final PandaEntity panda;

      public AttackGoal(PandaEntity pPanda, double pSpeedModifier, boolean pFollowingTargetEvenIfNotSeen) {
         super(pPanda, pSpeedModifier, pFollowingTargetEvenIfNotSeen);
         this.panda = pPanda;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return this.panda.canPerformAction() && super.canUse();
      }
   }

   static class AvoidGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
      private final PandaEntity panda;

      public AvoidGoal(PandaEntity pPanda, Class<T> pEntityClassToAvoid, float pMaxDist, double pWalkSpeedModifier, double pSprintSpeedModifier) {
         super(pPanda, pEntityClassToAvoid, pMaxDist, pWalkSpeedModifier, pSprintSpeedModifier, EntityPredicates.NO_SPECTATORS::test);
         this.panda = pPanda;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return this.panda.isWorried() && this.panda.canPerformAction() && super.canUse();
      }
   }

   static class ChildPlayGoal extends Goal {
      private final PandaEntity panda;

      public ChildPlayGoal(PandaEntity pPanda) {
         this.panda = pPanda;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if (this.panda.isBaby() && this.panda.canPerformAction()) {
            if (this.panda.isWeak() && this.panda.random.nextInt(500) == 1) {
               return true;
            } else {
               return this.panda.random.nextInt(6000) == 1;
            }
         } else {
            return false;
         }
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return false;
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.panda.sneeze(true);
      }
   }

   public static enum Gene {
      NORMAL(0, "normal", false),
      LAZY(1, "lazy", false),
      WORRIED(2, "worried", false),
      PLAYFUL(3, "playful", false),
      BROWN(4, "brown", true),
      WEAK(5, "weak", true),
      AGGRESSIVE(6, "aggressive", false);

      private static final PandaEntity.Gene[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(PandaEntity.Gene::getId)).toArray((p_221102_0_) -> {
         return new PandaEntity.Gene[p_221102_0_];
      });
      private final int id;
      private final String name;
      private final boolean isRecessive;

      private Gene(int pId, String pName, boolean pIsRecessive) {
         this.id = pId;
         this.name = pName;
         this.isRecessive = pIsRecessive;
      }

      public int getId() {
         return this.id;
      }

      public String getName() {
         return this.name;
      }

      public boolean isRecessive() {
         return this.isRecessive;
      }

      private static PandaEntity.Gene getVariantFromGenes(PandaEntity.Gene pMainGene, PandaEntity.Gene pHiddenGene) {
         if (pMainGene.isRecessive()) {
            return pMainGene == pHiddenGene ? pMainGene : NORMAL;
         } else {
            return pMainGene;
         }
      }

      public static PandaEntity.Gene byId(int pIndex) {
         if (pIndex < 0 || pIndex >= BY_ID.length) {
            pIndex = 0;
         }

         return BY_ID[pIndex];
      }

      public static PandaEntity.Gene byName(String pName) {
         for(PandaEntity.Gene pandaentity$gene : values()) {
            if (pandaentity$gene.name.equals(pName)) {
               return pandaentity$gene;
            }
         }

         return NORMAL;
      }

      public static PandaEntity.Gene getRandom(Random pRand) {
         int i = pRand.nextInt(16);
         if (i == 0) {
            return LAZY;
         } else if (i == 1) {
            return WORRIED;
         } else if (i == 2) {
            return PLAYFUL;
         } else if (i == 4) {
            return AGGRESSIVE;
         } else if (i < 9) {
            return WEAK;
         } else {
            return i < 11 ? BROWN : NORMAL;
         }
      }
   }

   static class LieBackGoal extends Goal {
      private final PandaEntity panda;
      private int cooldown;

      public LieBackGoal(PandaEntity pPanda) {
         this.panda = pPanda;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return this.cooldown < this.panda.tickCount && this.panda.isLazy() && this.panda.canPerformAction() && this.panda.random.nextInt(400) == 1;
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         if (!this.panda.isInWater() && (this.panda.isLazy() || this.panda.random.nextInt(600) != 1)) {
            return this.panda.random.nextInt(2000) != 1;
         } else {
            return false;
         }
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.panda.setOnBack(true);
         this.cooldown = 0;
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         this.panda.setOnBack(false);
         this.cooldown = this.panda.tickCount + 200;
      }
   }

   class MateGoal extends BreedGoal {
      private final PandaEntity panda;
      private int unhappyCooldown;

      public MateGoal(PandaEntity pPanda, double pSpeedModifier) {
         super(pPanda, pSpeedModifier);
         this.panda = pPanda;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if (super.canUse() && this.panda.getUnhappyCounter() == 0) {
            if (!this.canFindBamboo()) {
               if (this.unhappyCooldown <= this.panda.tickCount) {
                  this.panda.setUnhappyCounter(32);
                  this.unhappyCooldown = this.panda.tickCount + 600;
                  if (this.panda.isEffectiveAi()) {
                     PlayerEntity playerentity = this.level.getNearestPlayer(PandaEntity.BREED_TARGETING, this.panda);
                     this.panda.lookAtPlayerGoal.setTarget(playerentity);
                  }
               }

               return false;
            } else {
               return true;
            }
         } else {
            return false;
         }
      }

      private boolean canFindBamboo() {
         BlockPos blockpos = this.panda.blockPosition();
         BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

         for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 8; ++j) {
               for(int k = 0; k <= j; k = k > 0 ? -k : 1 - k) {
                  for(int l = k < j && k > -j ? j : 0; l <= j; l = l > 0 ? -l : 1 - l) {
                     blockpos$mutable.setWithOffset(blockpos, k, i, l);
                     if (this.level.getBlockState(blockpos$mutable).is(Blocks.BAMBOO)) {
                        return true;
                     }
                  }
               }
            }
         }

         return false;
      }
   }

   static class MoveHelperController extends MovementController {
      private final PandaEntity panda;

      public MoveHelperController(PandaEntity pPanda) {
         super(pPanda);
         this.panda = pPanda;
      }

      public void tick() {
         if (this.panda.canPerformAction()) {
            super.tick();
         }
      }
   }

   static class PanicGoal extends net.minecraft.entity.ai.goal.PanicGoal {
      private final PandaEntity panda;

      public PanicGoal(PandaEntity pPanda, double pSpeedModifier) {
         super(pPanda, pSpeedModifier);
         this.panda = pPanda;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if (!this.panda.isOnFire()) {
            return false;
         } else {
            BlockPos blockpos = this.lookForWater(this.mob.level, this.mob, 5, 4);
            if (blockpos != null) {
               this.posX = (double)blockpos.getX();
               this.posY = (double)blockpos.getY();
               this.posZ = (double)blockpos.getZ();
               return true;
            } else {
               return this.findRandomPosition();
            }
         }
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         if (this.panda.isSitting()) {
            this.panda.getNavigation().stop();
            return false;
         } else {
            return super.canContinueToUse();
         }
      }
   }

   static class RevengeGoal extends HurtByTargetGoal {
      private final PandaEntity panda;

      public RevengeGoal(PandaEntity pPanda, Class<?>... pEntityClassToIgnoreDamage) {
         super(pPanda, pEntityClassToIgnoreDamage);
         this.panda = pPanda;
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         if (!this.panda.gotBamboo && !this.panda.didBite) {
            return super.canContinueToUse();
         } else {
            this.panda.setTarget((LivingEntity)null);
            return false;
         }
      }

      protected void alertOther(MobEntity pMob, LivingEntity pTarget) {
         if (pMob instanceof PandaEntity && ((PandaEntity)pMob).isAggressive()) {
            pMob.setTarget(pTarget);
         }

      }
   }

   static class RollGoal extends Goal {
      private final PandaEntity panda;

      public RollGoal(PandaEntity pPanda) {
         this.panda = pPanda;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if ((this.panda.isBaby() || this.panda.isPlayful()) && this.panda.onGround) {
            if (!this.panda.canPerformAction()) {
               return false;
            } else {
               float f = this.panda.yRot * ((float)Math.PI / 180F);
               int i = 0;
               int j = 0;
               float f1 = -MathHelper.sin(f);
               float f2 = MathHelper.cos(f);
               if ((double)Math.abs(f1) > 0.5D) {
                  i = (int)((float)i + f1 / Math.abs(f1));
               }

               if ((double)Math.abs(f2) > 0.5D) {
                  j = (int)((float)j + f2 / Math.abs(f2));
               }

               if (this.panda.level.getBlockState(this.panda.blockPosition().offset(i, -1, j)).isAir()) {
                  return true;
               } else if (this.panda.isPlayful() && this.panda.random.nextInt(60) == 1) {
                  return true;
               } else {
                  return this.panda.random.nextInt(500) == 1;
               }
            }
         } else {
            return false;
         }
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return false;
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.panda.roll(true);
      }

      public boolean isInterruptable() {
         return false;
      }
   }

   class SitGoal extends Goal {
      private int cooldown;

      public SitGoal() {
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if (this.cooldown <= PandaEntity.this.tickCount && !PandaEntity.this.isBaby() && !PandaEntity.this.isInWater() && PandaEntity.this.canPerformAction() && PandaEntity.this.getUnhappyCounter() <= 0) {
            List<ItemEntity> list = PandaEntity.this.level.getEntitiesOfClass(ItemEntity.class, PandaEntity.this.getBoundingBox().inflate(6.0D, 6.0D, 6.0D), PandaEntity.PANDA_ITEMS);
            return !list.isEmpty() || !PandaEntity.this.getItemBySlot(EquipmentSlotType.MAINHAND).isEmpty();
         } else {
            return false;
         }
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         if (!PandaEntity.this.isInWater() && (PandaEntity.this.isLazy() || PandaEntity.this.random.nextInt(600) != 1)) {
            return PandaEntity.this.random.nextInt(2000) != 1;
         } else {
            return false;
         }
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         if (!PandaEntity.this.isSitting() && !PandaEntity.this.getItemBySlot(EquipmentSlotType.MAINHAND).isEmpty()) {
            PandaEntity.this.tryToSit();
         }

      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         List<ItemEntity> list = PandaEntity.this.level.getEntitiesOfClass(ItemEntity.class, PandaEntity.this.getBoundingBox().inflate(8.0D, 8.0D, 8.0D), PandaEntity.PANDA_ITEMS);
         if (!list.isEmpty() && PandaEntity.this.getItemBySlot(EquipmentSlotType.MAINHAND).isEmpty()) {
            PandaEntity.this.getNavigation().moveTo(list.get(0), (double)1.2F);
         } else if (!PandaEntity.this.getItemBySlot(EquipmentSlotType.MAINHAND).isEmpty()) {
            PandaEntity.this.tryToSit();
         }

         this.cooldown = 0;
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         ItemStack itemstack = PandaEntity.this.getItemBySlot(EquipmentSlotType.MAINHAND);
         if (!itemstack.isEmpty()) {
            PandaEntity.this.spawnAtLocation(itemstack);
            PandaEntity.this.setItemSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
            int i = PandaEntity.this.isLazy() ? PandaEntity.this.random.nextInt(50) + 10 : PandaEntity.this.random.nextInt(150) + 10;
            this.cooldown = PandaEntity.this.tickCount + i * 20;
         }

         PandaEntity.this.sit(false);
      }
   }

   static class WatchGoal extends LookAtGoal {
      private final PandaEntity panda;

      public WatchGoal(PandaEntity pPanda, Class<? extends LivingEntity> pLookAtType, float pLookDistance) {
         super(pPanda, pLookAtType, pLookDistance);
         this.panda = pPanda;
      }

      public void setTarget(LivingEntity pLookAt) {
         this.lookAt = pLookAt;
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return this.lookAt != null && super.canContinueToUse();
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if (this.mob.getRandom().nextFloat() >= this.probability) {
            return false;
         } else {
            if (this.lookAt == null) {
               if (this.lookAtType == PlayerEntity.class) {
                  this.lookAt = this.mob.level.getNearestPlayer(this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
               } else {
                  this.lookAt = this.mob.level.getNearestLoadedEntity(this.lookAtType, this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ(), this.mob.getBoundingBox().inflate((double)this.lookDistance, 3.0D, (double)this.lookDistance));
               }
            }

            return this.panda.canPerformAction() && this.lookAt != null;
         }
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         if (this.lookAt != null) {
            super.tick();
         }

      }
   }
}