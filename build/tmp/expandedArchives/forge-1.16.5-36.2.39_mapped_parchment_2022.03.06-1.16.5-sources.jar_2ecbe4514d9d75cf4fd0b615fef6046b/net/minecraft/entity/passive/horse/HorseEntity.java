package net.minecraft.entity.passive.horse;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.SoundType;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.HorseArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class HorseEntity extends AbstractHorseEntity {
   private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("556E1665-8B10-40C8-8F9D-CF9B1667F295");
   private static final DataParameter<Integer> DATA_ID_TYPE_VARIANT = EntityDataManager.defineId(HorseEntity.class, DataSerializers.INT);

   public HorseEntity(EntityType<? extends HorseEntity> p_i50238_1_, World p_i50238_2_) {
      super(p_i50238_1_, p_i50238_2_);
   }

   protected void randomizeAttributes() {
      this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double)this.generateRandomMaxHealth());
      this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.generateRandomSpeed());
      this.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(this.generateRandomJumpStrength());
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("Variant", this.getTypeVariant());
      if (!this.inventory.getItem(1).isEmpty()) {
         pCompound.put("ArmorItem", this.inventory.getItem(1).save(new CompoundNBT()));
      }

   }

   public ItemStack getArmor() {
      return this.getItemBySlot(EquipmentSlotType.CHEST);
   }

   private void setArmor(ItemStack pStack) {
      this.setItemSlot(EquipmentSlotType.CHEST, pStack);
      this.setDropChance(EquipmentSlotType.CHEST, 0.0F);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setTypeVariant(pCompound.getInt("Variant"));
      if (pCompound.contains("ArmorItem", 10)) {
         ItemStack itemstack = ItemStack.of(pCompound.getCompound("ArmorItem"));
         if (!itemstack.isEmpty() && this.isArmor(itemstack)) {
            this.inventory.setItem(1, itemstack);
         }
      }

      this.updateContainerEquipment();
   }

   private void setTypeVariant(int pTypeVariant) {
      this.entityData.set(DATA_ID_TYPE_VARIANT, pTypeVariant);
   }

   private int getTypeVariant() {
      return this.entityData.get(DATA_ID_TYPE_VARIANT);
   }

   private void setVariantAndMarkings(CoatColors pVariant, CoatTypes pMarking) {
      this.setTypeVariant(pVariant.getId() & 255 | pMarking.getId() << 8 & '\uff00');
   }

   public CoatColors getVariant() {
      return CoatColors.byId(this.getTypeVariant() & 255);
   }

   public CoatTypes getMarkings() {
      return CoatTypes.byId((this.getTypeVariant() & '\uff00') >> 8);
   }

   protected void updateContainerEquipment() {
      if (!this.level.isClientSide) {
         super.updateContainerEquipment();
         this.setArmorEquipment(this.inventory.getItem(1));
         this.setDropChance(EquipmentSlotType.CHEST, 0.0F);
      }
   }

   private void setArmorEquipment(ItemStack pStack) {
      this.setArmor(pStack);
      if (!this.level.isClientSide) {
         this.getAttribute(Attributes.ARMOR).removeModifier(ARMOR_MODIFIER_UUID);
         if (this.isArmor(pStack)) {
            int i = ((HorseArmorItem)pStack.getItem()).getProtection();
            if (i != 0) {
               this.getAttribute(Attributes.ARMOR).addTransientModifier(new AttributeModifier(ARMOR_MODIFIER_UUID, "Horse armor bonus", (double)i, AttributeModifier.Operation.ADDITION));
            }
         }
      }

   }

   /**
    * Called by InventoryBasic.onInventoryChanged() on a array that is never filled.
    */
   public void containerChanged(IInventory pInvBasic) {
      ItemStack itemstack = this.getArmor();
      super.containerChanged(pInvBasic);
      ItemStack itemstack1 = this.getArmor();
      if (this.tickCount > 20 && this.isArmor(itemstack1) && itemstack != itemstack1) {
         this.playSound(SoundEvents.HORSE_ARMOR, 0.5F, 1.0F);
      }

   }

   protected void playGallopSound(SoundType pSoundType) {
      super.playGallopSound(pSoundType);
      if (this.random.nextInt(10) == 0) {
         this.playSound(SoundEvents.HORSE_BREATHE, pSoundType.getVolume() * 0.6F, pSoundType.getPitch());
      }

      ItemStack stack = this.inventory.getItem(1);
      if (isArmor(stack)) stack.onHorseArmorTick(level, this);
   }

   protected SoundEvent getAmbientSound() {
      super.getAmbientSound();
      return SoundEvents.HORSE_AMBIENT;
   }

   protected SoundEvent getDeathSound() {
      super.getDeathSound();
      return SoundEvents.HORSE_DEATH;
   }

   @Nullable
   protected SoundEvent getEatingSound() {
      return SoundEvents.HORSE_EAT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      super.getHurtSound(pDamageSource);
      return SoundEvents.HORSE_HURT;
   }

   protected SoundEvent getAngrySound() {
      super.getAngrySound();
      return SoundEvents.HORSE_ANGRY;
   }

   public ActionResultType mobInteract(PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (!this.isBaby()) {
         if (this.isTamed() && pPlayer.isSecondaryUseActive()) {
            this.openInventory(pPlayer);
            return ActionResultType.sidedSuccess(this.level.isClientSide);
         }

         if (this.isVehicle()) {
            return super.mobInteract(pPlayer, pHand);
         }
      }

      if (!itemstack.isEmpty()) {
         if (this.isFood(itemstack)) {
            return this.fedFood(pPlayer, itemstack);
         }

         ActionResultType actionresulttype = itemstack.interactLivingEntity(pPlayer, this, pHand);
         if (actionresulttype.consumesAction()) {
            return actionresulttype;
         }

         if (!this.isTamed()) {
            this.makeMad();
            return ActionResultType.sidedSuccess(this.level.isClientSide);
         }

         boolean flag = !this.isBaby() && !this.isSaddled() && itemstack.getItem() == Items.SADDLE;
         if (this.isArmor(itemstack) || flag) {
            this.openInventory(pPlayer);
            return ActionResultType.sidedSuccess(this.level.isClientSide);
         }
      }

      if (this.isBaby()) {
         return super.mobInteract(pPlayer, pHand);
      } else {
         this.doPlayerRide(pPlayer);
         return ActionResultType.sidedSuccess(this.level.isClientSide);
      }
   }

   /**
    * Returns true if the mob is currently able to mate with the specified mob.
    */
   public boolean canMate(AnimalEntity pOtherAnimal) {
      if (pOtherAnimal == this) {
         return false;
      } else if (!(pOtherAnimal instanceof DonkeyEntity) && !(pOtherAnimal instanceof HorseEntity)) {
         return false;
      } else {
         return this.canParent() && ((AbstractHorseEntity)pOtherAnimal).canParent();
      }
   }

   public AgeableEntity getBreedOffspring(ServerWorld pServerLevel, AgeableEntity pMate) {
      AbstractHorseEntity abstracthorseentity;
      if (pMate instanceof DonkeyEntity) {
         abstracthorseentity = EntityType.MULE.create(pServerLevel);
      } else {
         HorseEntity horseentity = (HorseEntity)pMate;
         abstracthorseentity = EntityType.HORSE.create(pServerLevel);
         int i = this.random.nextInt(9);
         CoatColors coatcolors;
         if (i < 4) {
            coatcolors = this.getVariant();
         } else if (i < 8) {
            coatcolors = horseentity.getVariant();
         } else {
            coatcolors = Util.getRandom(CoatColors.values(), this.random);
         }

         int j = this.random.nextInt(5);
         CoatTypes coattypes;
         if (j < 2) {
            coattypes = this.getMarkings();
         } else if (j < 4) {
            coattypes = horseentity.getMarkings();
         } else {
            coattypes = Util.getRandom(CoatTypes.values(), this.random);
         }

         ((HorseEntity)abstracthorseentity).setVariantAndMarkings(coatcolors, coattypes);
      }

      this.setOffspringAttributes(pMate, abstracthorseentity);
      return abstracthorseentity;
   }

   public boolean canWearArmor() {
      return true;
   }

   public boolean isArmor(ItemStack pStack) {
      return pStack.getItem() instanceof HorseArmorItem;
   }

   @Nullable
   public ILivingEntityData finalizeSpawn(IServerWorld pLevel, DifficultyInstance pDifficulty, SpawnReason pReason, @Nullable ILivingEntityData pSpawnData, @Nullable CompoundNBT pDataTag) {
      CoatColors coatcolors;
      if (pSpawnData instanceof HorseEntity.HorseData) {
         coatcolors = ((HorseEntity.HorseData)pSpawnData).variant;
      } else {
         coatcolors = Util.getRandom(CoatColors.values(), this.random);
         pSpawnData = new HorseEntity.HorseData(coatcolors);
      }

      this.setVariantAndMarkings(coatcolors, Util.getRandom(CoatTypes.values(), this.random));
      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   public static class HorseData extends AgeableEntity.AgeableData {
      public final CoatColors variant;

      public HorseData(CoatColors pVariant) {
         super(true);
         this.variant = pVariant;
      }
   }
}
