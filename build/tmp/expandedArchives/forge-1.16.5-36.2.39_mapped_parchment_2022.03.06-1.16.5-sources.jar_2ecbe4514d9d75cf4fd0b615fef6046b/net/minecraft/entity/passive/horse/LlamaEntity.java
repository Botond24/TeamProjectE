package net.minecraft.entity.passive.horse;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarpetBlock;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.BreedGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LlamaFollowCaravanGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.PanicGoal;
import net.minecraft.entity.ai.goal.RangedAttackGoal;
import net.minecraft.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.LlamaSpitEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LlamaEntity extends AbstractChestedHorseEntity implements IRangedAttackMob {
   private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.WHEAT, Blocks.HAY_BLOCK.asItem());
   private static final DataParameter<Integer> DATA_STRENGTH_ID = EntityDataManager.defineId(LlamaEntity.class, DataSerializers.INT);
   private static final DataParameter<Integer> DATA_SWAG_ID = EntityDataManager.defineId(LlamaEntity.class, DataSerializers.INT);
   private static final DataParameter<Integer> DATA_VARIANT_ID = EntityDataManager.defineId(LlamaEntity.class, DataSerializers.INT);
   private boolean didSpit;
   @Nullable
   private LlamaEntity caravanHead;
   @Nullable
   private LlamaEntity caravanTail;

   public LlamaEntity(EntityType<? extends LlamaEntity> p_i50237_1_, World p_i50237_2_) {
      super(p_i50237_1_, p_i50237_2_);
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isTraderLlama() {
      return false;
   }

   private void setStrength(int pStrength) {
      this.entityData.set(DATA_STRENGTH_ID, Math.max(1, Math.min(5, pStrength)));
   }

   private void setRandomStrength() {
      int i = this.random.nextFloat() < 0.04F ? 5 : 3;
      this.setStrength(1 + this.random.nextInt(i));
   }

   public int getStrength() {
      return this.entityData.get(DATA_STRENGTH_ID);
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("Variant", this.getVariant());
      pCompound.putInt("Strength", this.getStrength());
      if (!this.inventory.getItem(1).isEmpty()) {
         pCompound.put("DecorItem", this.inventory.getItem(1).save(new CompoundNBT()));
      }

   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      this.setStrength(pCompound.getInt("Strength"));
      super.readAdditionalSaveData(pCompound);
      this.setVariant(pCompound.getInt("Variant"));
      if (pCompound.contains("DecorItem", 10)) {
         this.inventory.setItem(1, ItemStack.of(pCompound.getCompound("DecorItem")));
      }

      this.updateContainerEquipment();
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new SwimGoal(this));
      this.goalSelector.addGoal(1, new RunAroundLikeCrazyGoal(this, 1.2D));
      this.goalSelector.addGoal(2, new LlamaFollowCaravanGoal(this, (double)2.1F));
      this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.25D, 40, 20.0F));
      this.goalSelector.addGoal(3, new PanicGoal(this, 1.2D));
      this.goalSelector.addGoal(4, new BreedGoal(this, 1.0D));
      this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.0D));
      this.goalSelector.addGoal(6, new WaterAvoidingRandomWalkingGoal(this, 0.7D));
      this.goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 6.0F));
      this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
      this.targetSelector.addGoal(1, new LlamaEntity.HurtByTargetGoal(this));
      this.targetSelector.addGoal(2, new LlamaEntity.DefendTargetGoal(this));
   }

   public static AttributeModifierMap.MutableAttribute createAttributes() {
      return createBaseChestedHorseAttributes().add(Attributes.FOLLOW_RANGE, 40.0D);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_STRENGTH_ID, 0);
      this.entityData.define(DATA_SWAG_ID, -1);
      this.entityData.define(DATA_VARIANT_ID, 0);
   }

   public int getVariant() {
      return MathHelper.clamp(this.entityData.get(DATA_VARIANT_ID), 0, 3);
   }

   public void setVariant(int pVariant) {
      this.entityData.set(DATA_VARIANT_ID, pVariant);
   }

   protected int getInventorySize() {
      return this.hasChest() ? 2 + 3 * this.getInventoryColumns() : super.getInventorySize();
   }

   public void positionRider(Entity pPassenger) {
      if (this.hasPassenger(pPassenger)) {
         float f = MathHelper.cos(this.yBodyRot * ((float)Math.PI / 180F));
         float f1 = MathHelper.sin(this.yBodyRot * ((float)Math.PI / 180F));
         float f2 = 0.3F;
         pPassenger.setPos(this.getX() + (double)(0.3F * f1), this.getY() + this.getPassengersRidingOffset() + pPassenger.getMyRidingOffset(), this.getZ() - (double)(0.3F * f));
      }
   }

   /**
    * Returns the Y offset from the entity's position for any entity riding this one.
    */
   public double getPassengersRidingOffset() {
      return (double)this.getBbHeight() * 0.67D;
   }

   /**
    * @return true if all the conditions for steering the entity are met. For pigs, this is true if it is being ridden
    * by a player and the player is holding a carrot-on-a-stick
    */
   public boolean canBeControlledByRider() {
      return false;
   }

   /**
    * Checks if the parameter is an item which this animal can be fed to breed it (wheat, carrots or seeds depending on
    * the animal type)
    */
   public boolean isFood(ItemStack pStack) {
      return FOOD_ITEMS.test(pStack);
   }

   protected boolean handleEating(PlayerEntity pPlayer, ItemStack pStack) {
      int i = 0;
      int j = 0;
      float f = 0.0F;
      boolean flag = false;
      Item item = pStack.getItem();
      if (item == Items.WHEAT) {
         i = 10;
         j = 3;
         f = 2.0F;
      } else if (item == Blocks.HAY_BLOCK.asItem()) {
         i = 90;
         j = 6;
         f = 10.0F;
         if (this.isTamed() && this.getAge() == 0 && this.canFallInLove()) {
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

      if (flag && !this.isSilent()) {
         SoundEvent soundevent = this.getEatingSound();
         if (soundevent != null) {
            this.level.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), this.getEatingSound(), this.getSoundSource(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
         }
      }

      return flag;
   }

   /**
    * Dead and sleeping entities cannot move
    */
   protected boolean isImmobile() {
      return this.isDeadOrDying() || this.isEating();
   }

   @Nullable
   public ILivingEntityData finalizeSpawn(IServerWorld pLevel, DifficultyInstance pDifficulty, SpawnReason pReason, @Nullable ILivingEntityData pSpawnData, @Nullable CompoundNBT pDataTag) {
      this.setRandomStrength();
      int i;
      if (pSpawnData instanceof LlamaEntity.LlamaData) {
         i = ((LlamaEntity.LlamaData)pSpawnData).variant;
      } else {
         i = this.random.nextInt(4);
         pSpawnData = new LlamaEntity.LlamaData(i);
      }

      this.setVariant(i);
      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   protected SoundEvent getAngrySound() {
      return SoundEvents.LLAMA_ANGRY;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.LLAMA_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.LLAMA_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.LLAMA_DEATH;
   }

   @Nullable
   protected SoundEvent getEatingSound() {
      return SoundEvents.LLAMA_EAT;
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      this.playSound(SoundEvents.LLAMA_STEP, 0.15F, 1.0F);
   }

   protected void playChestEquipsSound() {
      this.playSound(SoundEvents.LLAMA_CHEST, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
   }

   public void makeMad() {
      SoundEvent soundevent = this.getAngrySound();
      if (soundevent != null) {
         this.playSound(soundevent, this.getSoundVolume(), this.getVoicePitch());
      }

   }

   public int getInventoryColumns() {
      return this.getStrength();
   }

   public boolean canWearArmor() {
      return true;
   }

   public boolean isWearingArmor() {
      return !this.inventory.getItem(1).isEmpty();
   }

   public boolean isArmor(ItemStack pStack) {
      Item item = pStack.getItem();
      return ItemTags.CARPETS.contains(item);
   }

   public boolean isSaddleable() {
      return false;
   }

   /**
    * Called by InventoryBasic.onInventoryChanged() on a array that is never filled.
    */
   public void containerChanged(IInventory pInvBasic) {
      DyeColor dyecolor = this.getSwag();
      super.containerChanged(pInvBasic);
      DyeColor dyecolor1 = this.getSwag();
      if (this.tickCount > 20 && dyecolor1 != null && dyecolor1 != dyecolor) {
         this.playSound(SoundEvents.LLAMA_SWAG, 0.5F, 1.0F);
      }

   }

   protected void updateContainerEquipment() {
      if (!this.level.isClientSide) {
         super.updateContainerEquipment();
         this.setSwag(getDyeColor(this.inventory.getItem(1)));
      }
   }

   private void setSwag(@Nullable DyeColor pColor) {
      this.entityData.set(DATA_SWAG_ID, pColor == null ? -1 : pColor.getId());
   }

   @Nullable
   private static DyeColor getDyeColor(ItemStack pStack) {
      Block block = Block.byItem(pStack.getItem());
      return block instanceof CarpetBlock ? ((CarpetBlock)block).getColor() : null;
   }

   @Nullable
   public DyeColor getSwag() {
      int i = this.entityData.get(DATA_SWAG_ID);
      return i == -1 ? null : DyeColor.byId(i);
   }

   public int getMaxTemper() {
      return 30;
   }

   /**
    * Returns true if the mob is currently able to mate with the specified mob.
    */
   public boolean canMate(AnimalEntity pOtherAnimal) {
      return pOtherAnimal != this && pOtherAnimal instanceof LlamaEntity && this.canParent() && ((LlamaEntity)pOtherAnimal).canParent();
   }

   public LlamaEntity getBreedOffspring(ServerWorld pServerLevel, AgeableEntity pMate) {
      LlamaEntity llamaentity = this.makeBabyLlama();
      this.setOffspringAttributes(pMate, llamaentity);
      LlamaEntity llamaentity1 = (LlamaEntity)pMate;
      int i = this.random.nextInt(Math.max(this.getStrength(), llamaentity1.getStrength())) + 1;
      if (this.random.nextFloat() < 0.03F) {
         ++i;
      }

      llamaentity.setStrength(i);
      llamaentity.setVariant(this.random.nextBoolean() ? this.getVariant() : llamaentity1.getVariant());
      return llamaentity;
   }

   protected LlamaEntity makeBabyLlama() {
      return EntityType.LLAMA.create(this.level);
   }

   private void spit(LivingEntity pTarget) {
      LlamaSpitEntity llamaspitentity = new LlamaSpitEntity(this.level, this);
      double d0 = pTarget.getX() - this.getX();
      double d1 = pTarget.getY(0.3333333333333333D) - llamaspitentity.getY();
      double d2 = pTarget.getZ() - this.getZ();
      float f = MathHelper.sqrt(d0 * d0 + d2 * d2) * 0.2F;
      llamaspitentity.shoot(d0, d1 + (double)f, d2, 1.5F, 10.0F);
      if (!this.isSilent()) {
         this.level.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.LLAMA_SPIT, this.getSoundSource(), 1.0F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F);
      }

      this.level.addFreshEntity(llamaspitentity);
      this.didSpit = true;
   }

   private void setDidSpit(boolean pDidSpit) {
      this.didSpit = pDidSpit;
   }

   public boolean causeFallDamage(float pFallDistance, float pDamageMultiplier) {
      int i = this.calculateFallDamage(pFallDistance, pDamageMultiplier);
      if (i <= 0) {
         return false;
      } else {
         if (pFallDistance >= 6.0F) {
            this.hurt(DamageSource.FALL, (float)i);
            if (this.isVehicle()) {
               for(Entity entity : this.getIndirectPassengers()) {
                  entity.hurt(DamageSource.FALL, (float)i);
               }
            }
         }

         this.playBlockFallSound();
         return true;
      }
   }

   public void leaveCaravan() {
      if (this.caravanHead != null) {
         this.caravanHead.caravanTail = null;
      }

      this.caravanHead = null;
   }

   public void joinCaravan(LlamaEntity pCaravanHead) {
      this.caravanHead = pCaravanHead;
      this.caravanHead.caravanTail = this;
   }

   public boolean hasCaravanTail() {
      return this.caravanTail != null;
   }

   public boolean inCaravan() {
      return this.caravanHead != null;
   }

   @Nullable
   public LlamaEntity getCaravanHead() {
      return this.caravanHead;
   }

   protected double followLeashSpeed() {
      return 2.0D;
   }

   protected void followMommy() {
      if (!this.inCaravan() && this.isBaby()) {
         super.followMommy();
      }

   }

   public boolean canEatGrass() {
      return false;
   }

   /**
    * Attack the specified entity using a ranged attack.
    */
   public void performRangedAttack(LivingEntity pTarget, float pVelocity) {
      this.spit(pTarget);
   }

   @OnlyIn(Dist.CLIENT)
   public Vector3d getLeashOffset() {
      return new Vector3d(0.0D, 0.75D * (double)this.getEyeHeight(), (double)this.getBbWidth() * 0.5D);
   }

   static class DefendTargetGoal extends NearestAttackableTargetGoal<WolfEntity> {
      public DefendTargetGoal(LlamaEntity pLlama) {
         super(pLlama, WolfEntity.class, 16, false, true, (p_220789_0_) -> {
            return !((WolfEntity)p_220789_0_).isTame();
         });
      }

      protected double getFollowDistance() {
         return super.getFollowDistance() * 0.25D;
      }
   }

   static class HurtByTargetGoal extends net.minecraft.entity.ai.goal.HurtByTargetGoal {
      public HurtByTargetGoal(LlamaEntity pLlama) {
         super(pLlama);
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         if (this.mob instanceof LlamaEntity) {
            LlamaEntity llamaentity = (LlamaEntity)this.mob;
            if (llamaentity.didSpit) {
               llamaentity.setDidSpit(false);
               return false;
            }
         }

         return super.canContinueToUse();
      }
   }

   static class LlamaData extends AgeableEntity.AgeableData {
      public final int variant;

      private LlamaData(int pVariant) {
         super(true);
         this.variant = pVariant;
      }
   }
}