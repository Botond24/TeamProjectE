package net.minecraft.entity.item;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Rotations;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ArmorStandEntity extends LivingEntity {
   private static final Rotations DEFAULT_HEAD_POSE = new Rotations(0.0F, 0.0F, 0.0F);
   private static final Rotations DEFAULT_BODY_POSE = new Rotations(0.0F, 0.0F, 0.0F);
   private static final Rotations DEFAULT_LEFT_ARM_POSE = new Rotations(-10.0F, 0.0F, -10.0F);
   private static final Rotations DEFAULT_RIGHT_ARM_POSE = new Rotations(-15.0F, 0.0F, 10.0F);
   private static final Rotations DEFAULT_LEFT_LEG_POSE = new Rotations(-1.0F, 0.0F, -1.0F);
   private static final Rotations DEFAULT_RIGHT_LEG_POSE = new Rotations(1.0F, 0.0F, 1.0F);
   private static final EntitySize MARKER_DIMENSIONS = new EntitySize(0.0F, 0.0F, true);
   private static final EntitySize BABY_DIMENSIONS = EntityType.ARMOR_STAND.getDimensions().scale(0.5F);
   public static final DataParameter<Byte> DATA_CLIENT_FLAGS = EntityDataManager.defineId(ArmorStandEntity.class, DataSerializers.BYTE);
   public static final DataParameter<Rotations> DATA_HEAD_POSE = EntityDataManager.defineId(ArmorStandEntity.class, DataSerializers.ROTATIONS);
   public static final DataParameter<Rotations> DATA_BODY_POSE = EntityDataManager.defineId(ArmorStandEntity.class, DataSerializers.ROTATIONS);
   public static final DataParameter<Rotations> DATA_LEFT_ARM_POSE = EntityDataManager.defineId(ArmorStandEntity.class, DataSerializers.ROTATIONS);
   public static final DataParameter<Rotations> DATA_RIGHT_ARM_POSE = EntityDataManager.defineId(ArmorStandEntity.class, DataSerializers.ROTATIONS);
   public static final DataParameter<Rotations> DATA_LEFT_LEG_POSE = EntityDataManager.defineId(ArmorStandEntity.class, DataSerializers.ROTATIONS);
   public static final DataParameter<Rotations> DATA_RIGHT_LEG_POSE = EntityDataManager.defineId(ArmorStandEntity.class, DataSerializers.ROTATIONS);
   private static final Predicate<Entity> RIDABLE_MINECARTS = (p_200617_0_) -> {
      return p_200617_0_ instanceof AbstractMinecartEntity && ((AbstractMinecartEntity)p_200617_0_).canBeRidden();
   };
   private final NonNullList<ItemStack> handItems = NonNullList.withSize(2, ItemStack.EMPTY);
   private final NonNullList<ItemStack> armorItems = NonNullList.withSize(4, ItemStack.EMPTY);
   private boolean invisible;
   /** After punching the stand, the cooldown before you can punch it again without breaking it. */
   public long lastHit;
   private int disabledSlots;
   private Rotations headPose = DEFAULT_HEAD_POSE;
   private Rotations bodyPose = DEFAULT_BODY_POSE;
   private Rotations leftArmPose = DEFAULT_LEFT_ARM_POSE;
   private Rotations rightArmPose = DEFAULT_RIGHT_ARM_POSE;
   private Rotations leftLegPose = DEFAULT_LEFT_LEG_POSE;
   private Rotations rightLegPose = DEFAULT_RIGHT_LEG_POSE;

   public ArmorStandEntity(EntityType<? extends ArmorStandEntity> p_i50225_1_, World p_i50225_2_) {
      super(p_i50225_1_, p_i50225_2_);
      this.maxUpStep = 0.0F;
   }

   public ArmorStandEntity(World pLevel, double pX, double pY, double pZ) {
      this(EntityType.ARMOR_STAND, pLevel);
      this.setPos(pX, pY, pZ);
   }

   public void refreshDimensions() {
      double d0 = this.getX();
      double d1 = this.getY();
      double d2 = this.getZ();
      super.refreshDimensions();
      this.setPos(d0, d1, d2);
   }

   private boolean hasPhysics() {
      return !this.isMarker() && !this.isNoGravity();
   }

   /**
    * Returns whether the entity is in a server world
    */
   public boolean isEffectiveAi() {
      return super.isEffectiveAi() && this.hasPhysics();
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_CLIENT_FLAGS, (byte)0);
      this.entityData.define(DATA_HEAD_POSE, DEFAULT_HEAD_POSE);
      this.entityData.define(DATA_BODY_POSE, DEFAULT_BODY_POSE);
      this.entityData.define(DATA_LEFT_ARM_POSE, DEFAULT_LEFT_ARM_POSE);
      this.entityData.define(DATA_RIGHT_ARM_POSE, DEFAULT_RIGHT_ARM_POSE);
      this.entityData.define(DATA_LEFT_LEG_POSE, DEFAULT_LEFT_LEG_POSE);
      this.entityData.define(DATA_RIGHT_LEG_POSE, DEFAULT_RIGHT_LEG_POSE);
   }

   public Iterable<ItemStack> getHandSlots() {
      return this.handItems;
   }

   public Iterable<ItemStack> getArmorSlots() {
      return this.armorItems;
   }

   public ItemStack getItemBySlot(EquipmentSlotType pSlot) {
      switch(pSlot.getType()) {
      case HAND:
         return this.handItems.get(pSlot.getIndex());
      case ARMOR:
         return this.armorItems.get(pSlot.getIndex());
      default:
         return ItemStack.EMPTY;
      }
   }

   public void setItemSlot(EquipmentSlotType pSlot, ItemStack pStack) {
      switch(pSlot.getType()) {
      case HAND:
         this.playEquipSound(pStack);
         this.handItems.set(pSlot.getIndex(), pStack);
         break;
      case ARMOR:
         this.playEquipSound(pStack);
         this.armorItems.set(pSlot.getIndex(), pStack);
      }

   }

   public boolean setSlot(int pSlotIndex, ItemStack pStack) {
      EquipmentSlotType equipmentslottype;
      if (pSlotIndex == 98) {
         equipmentslottype = EquipmentSlotType.MAINHAND;
      } else if (pSlotIndex == 99) {
         equipmentslottype = EquipmentSlotType.OFFHAND;
      } else if (pSlotIndex == 100 + EquipmentSlotType.HEAD.getIndex()) {
         equipmentslottype = EquipmentSlotType.HEAD;
      } else if (pSlotIndex == 100 + EquipmentSlotType.CHEST.getIndex()) {
         equipmentslottype = EquipmentSlotType.CHEST;
      } else if (pSlotIndex == 100 + EquipmentSlotType.LEGS.getIndex()) {
         equipmentslottype = EquipmentSlotType.LEGS;
      } else {
         if (pSlotIndex != 100 + EquipmentSlotType.FEET.getIndex()) {
            return false;
         }

         equipmentslottype = EquipmentSlotType.FEET;
      }

      if (!pStack.isEmpty() && !MobEntity.isValidSlotForItem(equipmentslottype, pStack) && equipmentslottype != EquipmentSlotType.HEAD) {
         return false;
      } else {
         this.setItemSlot(equipmentslottype, pStack);
         return true;
      }
   }

   public boolean canTakeItem(ItemStack pItemstack) {
      EquipmentSlotType equipmentslottype = MobEntity.getEquipmentSlotForItem(pItemstack);
      return this.getItemBySlot(equipmentslottype).isEmpty() && !this.isDisabled(equipmentslottype);
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      ListNBT listnbt = new ListNBT();

      for(ItemStack itemstack : this.armorItems) {
         CompoundNBT compoundnbt = new CompoundNBT();
         if (!itemstack.isEmpty()) {
            itemstack.save(compoundnbt);
         }

         listnbt.add(compoundnbt);
      }

      pCompound.put("ArmorItems", listnbt);
      ListNBT listnbt1 = new ListNBT();

      for(ItemStack itemstack1 : this.handItems) {
         CompoundNBT compoundnbt1 = new CompoundNBT();
         if (!itemstack1.isEmpty()) {
            itemstack1.save(compoundnbt1);
         }

         listnbt1.add(compoundnbt1);
      }

      pCompound.put("HandItems", listnbt1);
      pCompound.putBoolean("Invisible", this.isInvisible());
      pCompound.putBoolean("Small", this.isSmall());
      pCompound.putBoolean("ShowArms", this.isShowArms());
      pCompound.putInt("DisabledSlots", this.disabledSlots);
      pCompound.putBoolean("NoBasePlate", this.isNoBasePlate());
      if (this.isMarker()) {
         pCompound.putBoolean("Marker", this.isMarker());
      }

      pCompound.put("Pose", this.writePose());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.contains("ArmorItems", 9)) {
         ListNBT listnbt = pCompound.getList("ArmorItems", 10);

         for(int i = 0; i < this.armorItems.size(); ++i) {
            this.armorItems.set(i, ItemStack.of(listnbt.getCompound(i)));
         }
      }

      if (pCompound.contains("HandItems", 9)) {
         ListNBT listnbt1 = pCompound.getList("HandItems", 10);

         for(int j = 0; j < this.handItems.size(); ++j) {
            this.handItems.set(j, ItemStack.of(listnbt1.getCompound(j)));
         }
      }

      this.setInvisible(pCompound.getBoolean("Invisible"));
      this.setSmall(pCompound.getBoolean("Small"));
      this.setShowArms(pCompound.getBoolean("ShowArms"));
      this.disabledSlots = pCompound.getInt("DisabledSlots");
      this.setNoBasePlate(pCompound.getBoolean("NoBasePlate"));
      this.setMarker(pCompound.getBoolean("Marker"));
      this.noPhysics = !this.hasPhysics();
      CompoundNBT compoundnbt = pCompound.getCompound("Pose");
      this.readPose(compoundnbt);
   }

   private void readPose(CompoundNBT pCompound) {
      ListNBT listnbt = pCompound.getList("Head", 5);
      this.setHeadPose(listnbt.isEmpty() ? DEFAULT_HEAD_POSE : new Rotations(listnbt));
      ListNBT listnbt1 = pCompound.getList("Body", 5);
      this.setBodyPose(listnbt1.isEmpty() ? DEFAULT_BODY_POSE : new Rotations(listnbt1));
      ListNBT listnbt2 = pCompound.getList("LeftArm", 5);
      this.setLeftArmPose(listnbt2.isEmpty() ? DEFAULT_LEFT_ARM_POSE : new Rotations(listnbt2));
      ListNBT listnbt3 = pCompound.getList("RightArm", 5);
      this.setRightArmPose(listnbt3.isEmpty() ? DEFAULT_RIGHT_ARM_POSE : new Rotations(listnbt3));
      ListNBT listnbt4 = pCompound.getList("LeftLeg", 5);
      this.setLeftLegPose(listnbt4.isEmpty() ? DEFAULT_LEFT_LEG_POSE : new Rotations(listnbt4));
      ListNBT listnbt5 = pCompound.getList("RightLeg", 5);
      this.setRightLegPose(listnbt5.isEmpty() ? DEFAULT_RIGHT_LEG_POSE : new Rotations(listnbt5));
   }

   private CompoundNBT writePose() {
      CompoundNBT compoundnbt = new CompoundNBT();
      if (!DEFAULT_HEAD_POSE.equals(this.headPose)) {
         compoundnbt.put("Head", this.headPose.save());
      }

      if (!DEFAULT_BODY_POSE.equals(this.bodyPose)) {
         compoundnbt.put("Body", this.bodyPose.save());
      }

      if (!DEFAULT_LEFT_ARM_POSE.equals(this.leftArmPose)) {
         compoundnbt.put("LeftArm", this.leftArmPose.save());
      }

      if (!DEFAULT_RIGHT_ARM_POSE.equals(this.rightArmPose)) {
         compoundnbt.put("RightArm", this.rightArmPose.save());
      }

      if (!DEFAULT_LEFT_LEG_POSE.equals(this.leftLegPose)) {
         compoundnbt.put("LeftLeg", this.leftLegPose.save());
      }

      if (!DEFAULT_RIGHT_LEG_POSE.equals(this.rightLegPose)) {
         compoundnbt.put("RightLeg", this.rightLegPose.save());
      }

      return compoundnbt;
   }

   /**
    * Returns true if this entity should push and be pushed by other entities when colliding.
    */
   public boolean isPushable() {
      return false;
   }

   protected void doPush(Entity pEntity) {
   }

   protected void pushEntities() {
      List<Entity> list = this.level.getEntities(this, this.getBoundingBox(), RIDABLE_MINECARTS);

      for(int i = 0; i < list.size(); ++i) {
         Entity entity = list.get(i);
         if (this.distanceToSqr(entity) <= 0.2D) {
            entity.push(this);
         }
      }

   }

   /**
    * Applies the given player interaction to this Entity.
    */
   public ActionResultType interactAt(PlayerEntity pPlayer, Vector3d pVec, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (!this.isMarker() && itemstack.getItem() != Items.NAME_TAG) {
         if (pPlayer.isSpectator()) {
            return ActionResultType.SUCCESS;
         } else if (pPlayer.level.isClientSide) {
            return ActionResultType.CONSUME;
         } else {
            EquipmentSlotType equipmentslottype = MobEntity.getEquipmentSlotForItem(itemstack);
            if (itemstack.isEmpty()) {
               EquipmentSlotType equipmentslottype1 = this.getClickedSlot(pVec);
               EquipmentSlotType equipmentslottype2 = this.isDisabled(equipmentslottype1) ? equipmentslottype : equipmentslottype1;
               if (this.hasItemInSlot(equipmentslottype2) && this.swapItem(pPlayer, equipmentslottype2, itemstack, pHand)) {
                  return ActionResultType.SUCCESS;
               }
            } else {
               if (this.isDisabled(equipmentslottype)) {
                  return ActionResultType.FAIL;
               }

               if (equipmentslottype.getType() == EquipmentSlotType.Group.HAND && !this.isShowArms()) {
                  return ActionResultType.FAIL;
               }

               if (this.swapItem(pPlayer, equipmentslottype, itemstack, pHand)) {
                  return ActionResultType.SUCCESS;
               }
            }

            return ActionResultType.PASS;
         }
      } else {
         return ActionResultType.PASS;
      }
   }

   private EquipmentSlotType getClickedSlot(Vector3d pVector) {
      EquipmentSlotType equipmentslottype = EquipmentSlotType.MAINHAND;
      boolean flag = this.isSmall();
      double d0 = flag ? pVector.y * 2.0D : pVector.y;
      EquipmentSlotType equipmentslottype1 = EquipmentSlotType.FEET;
      if (d0 >= 0.1D && d0 < 0.1D + (flag ? 0.8D : 0.45D) && this.hasItemInSlot(equipmentslottype1)) {
         equipmentslottype = EquipmentSlotType.FEET;
      } else if (d0 >= 0.9D + (flag ? 0.3D : 0.0D) && d0 < 0.9D + (flag ? 1.0D : 0.7D) && this.hasItemInSlot(EquipmentSlotType.CHEST)) {
         equipmentslottype = EquipmentSlotType.CHEST;
      } else if (d0 >= 0.4D && d0 < 0.4D + (flag ? 1.0D : 0.8D) && this.hasItemInSlot(EquipmentSlotType.LEGS)) {
         equipmentslottype = EquipmentSlotType.LEGS;
      } else if (d0 >= 1.6D && this.hasItemInSlot(EquipmentSlotType.HEAD)) {
         equipmentslottype = EquipmentSlotType.HEAD;
      } else if (!this.hasItemInSlot(EquipmentSlotType.MAINHAND) && this.hasItemInSlot(EquipmentSlotType.OFFHAND)) {
         equipmentslottype = EquipmentSlotType.OFFHAND;
      }

      return equipmentslottype;
   }

   private boolean isDisabled(EquipmentSlotType pSlot) {
      return (this.disabledSlots & 1 << pSlot.getFilterFlag()) != 0 || pSlot.getType() == EquipmentSlotType.Group.HAND && !this.isShowArms();
   }

   private boolean swapItem(PlayerEntity pPlayer, EquipmentSlotType pSlot, ItemStack pStack, Hand pHand) {
      ItemStack itemstack = this.getItemBySlot(pSlot);
      if (!itemstack.isEmpty() && (this.disabledSlots & 1 << pSlot.getFilterFlag() + 8) != 0) {
         return false;
      } else if (itemstack.isEmpty() && (this.disabledSlots & 1 << pSlot.getFilterFlag() + 16) != 0) {
         return false;
      } else if (pPlayer.abilities.instabuild && itemstack.isEmpty() && !pStack.isEmpty()) {
         ItemStack itemstack2 = pStack.copy();
         itemstack2.setCount(1);
         this.setItemSlot(pSlot, itemstack2);
         return true;
      } else if (!pStack.isEmpty() && pStack.getCount() > 1) {
         if (!itemstack.isEmpty()) {
            return false;
         } else {
            ItemStack itemstack1 = pStack.copy();
            itemstack1.setCount(1);
            this.setItemSlot(pSlot, itemstack1);
            pStack.shrink(1);
            return true;
         }
      } else {
         this.setItemSlot(pSlot, pStack);
         pPlayer.setItemInHand(pHand, itemstack);
         return true;
      }
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (!this.level.isClientSide && !this.removed) {
         if (DamageSource.OUT_OF_WORLD.equals(pSource)) {
            this.remove();
            return false;
         } else if (!this.isInvulnerableTo(pSource) && !this.invisible && !this.isMarker()) {
            if (pSource.isExplosion()) {
               this.brokenByAnything(pSource);
               this.remove();
               return false;
            } else if (DamageSource.IN_FIRE.equals(pSource)) {
               if (this.isOnFire()) {
                  this.causeDamage(pSource, 0.15F);
               } else {
                  this.setSecondsOnFire(5);
               }

               return false;
            } else if (DamageSource.ON_FIRE.equals(pSource) && this.getHealth() > 0.5F) {
               this.causeDamage(pSource, 4.0F);
               return false;
            } else {
               boolean flag = pSource.getDirectEntity() instanceof AbstractArrowEntity;
               boolean flag1 = flag && ((AbstractArrowEntity)pSource.getDirectEntity()).getPierceLevel() > 0;
               boolean flag2 = "player".equals(pSource.getMsgId());
               if (!flag2 && !flag) {
                  return false;
               } else if (pSource.getEntity() instanceof PlayerEntity && !((PlayerEntity)pSource.getEntity()).abilities.mayBuild) {
                  return false;
               } else if (pSource.isCreativePlayer()) {
                  this.playBrokenSound();
                  this.showBreakingParticles();
                  this.remove();
                  return flag1;
               } else {
                  long i = this.level.getGameTime();
                  if (i - this.lastHit > 5L && !flag) {
                     this.level.broadcastEntityEvent(this, (byte)32);
                     this.lastHit = i;
                  } else {
                     this.brokenByPlayer(pSource);
                     this.showBreakingParticles();
                     this.remove();
                  }

                  return true;
               }
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   /**
    * Handles an entity event fired from {@link net.minecraft.world.level.Level#broadcastEntityEvent}.
    */
   @OnlyIn(Dist.CLIENT)
   public void handleEntityEvent(byte pId) {
      if (pId == 32) {
         if (this.level.isClientSide) {
            this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ARMOR_STAND_HIT, this.getSoundSource(), 0.3F, 1.0F, false);
            this.lastHit = this.level.getGameTime();
         }
      } else {
         super.handleEntityEvent(pId);
      }

   }

   /**
    * Checks if the entity is in range to render.
    */
   @OnlyIn(Dist.CLIENT)
   public boolean shouldRenderAtSqrDistance(double pDistance) {
      double d0 = this.getBoundingBox().getSize() * 4.0D;
      if (Double.isNaN(d0) || d0 == 0.0D) {
         d0 = 4.0D;
      }

      d0 = d0 * 64.0D;
      return pDistance < d0 * d0;
   }

   private void showBreakingParticles() {
      if (this.level instanceof ServerWorld) {
         ((ServerWorld)this.level).sendParticles(new BlockParticleData(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.defaultBlockState()), this.getX(), this.getY(0.6666666666666666D), this.getZ(), 10, (double)(this.getBbWidth() / 4.0F), (double)(this.getBbHeight() / 4.0F), (double)(this.getBbWidth() / 4.0F), 0.05D);
      }

   }

   private void causeDamage(DamageSource pDamageSource, float pAmount) {
      float f = this.getHealth();
      f = f - pAmount;
      if (f <= 0.5F) {
         this.brokenByAnything(pDamageSource);
         this.remove();
      } else {
         this.setHealth(f);
      }

   }

   private void brokenByPlayer(DamageSource pDamageSource) {
      Block.popResource(this.level, this.blockPosition(), new ItemStack(Items.ARMOR_STAND));
      this.brokenByAnything(pDamageSource);
   }

   private void brokenByAnything(DamageSource pDamageSource) {
      this.playBrokenSound();
      this.dropAllDeathLoot(pDamageSource);

      for(int i = 0; i < this.handItems.size(); ++i) {
         ItemStack itemstack = this.handItems.get(i);
         if (!itemstack.isEmpty()) {
            Block.popResource(this.level, this.blockPosition().above(), itemstack);
            this.handItems.set(i, ItemStack.EMPTY);
         }
      }

      for(int j = 0; j < this.armorItems.size(); ++j) {
         ItemStack itemstack1 = this.armorItems.get(j);
         if (!itemstack1.isEmpty()) {
            Block.popResource(this.level, this.blockPosition().above(), itemstack1);
            this.armorItems.set(j, ItemStack.EMPTY);
         }
      }

   }

   private void playBrokenSound() {
      this.level.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.ARMOR_STAND_BREAK, this.getSoundSource(), 1.0F, 1.0F);
   }

   protected float tickHeadTurn(float pYRot, float pAnimStep) {
      this.yBodyRotO = this.yRotO;
      this.yBodyRot = this.yRot;
      return 0.0F;
   }

   protected float getStandingEyeHeight(Pose pPose, EntitySize pSize) {
      return pSize.height * (this.isBaby() ? 0.5F : 0.9F);
   }

   /**
    * Returns the Y Offset of this entity.
    */
   public double getMyRidingOffset() {
      return this.isMarker() ? 0.0D : (double)0.1F;
   }

   public void travel(Vector3d pTravelVector) {
      if (this.hasPhysics()) {
         super.travel(pTravelVector);
      }
   }

   /**
    * Set the body Y rotation of the entity.
    */
   public void setYBodyRot(float pYBodyRot) {
      this.yBodyRotO = this.yRotO = pYBodyRot;
      this.yHeadRotO = this.yHeadRot = pYBodyRot;
   }

   /**
    * Sets the head's Y rotation of the entity.
    */
   public void setYHeadRot(float pYHeadRot) {
      this.yBodyRotO = this.yRotO = pYHeadRot;
      this.yHeadRotO = this.yHeadRot = pYHeadRot;
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      Rotations rotations = this.entityData.get(DATA_HEAD_POSE);
      if (!this.headPose.equals(rotations)) {
         this.setHeadPose(rotations);
      }

      Rotations rotations1 = this.entityData.get(DATA_BODY_POSE);
      if (!this.bodyPose.equals(rotations1)) {
         this.setBodyPose(rotations1);
      }

      Rotations rotations2 = this.entityData.get(DATA_LEFT_ARM_POSE);
      if (!this.leftArmPose.equals(rotations2)) {
         this.setLeftArmPose(rotations2);
      }

      Rotations rotations3 = this.entityData.get(DATA_RIGHT_ARM_POSE);
      if (!this.rightArmPose.equals(rotations3)) {
         this.setRightArmPose(rotations3);
      }

      Rotations rotations4 = this.entityData.get(DATA_LEFT_LEG_POSE);
      if (!this.leftLegPose.equals(rotations4)) {
         this.setLeftLegPose(rotations4);
      }

      Rotations rotations5 = this.entityData.get(DATA_RIGHT_LEG_POSE);
      if (!this.rightLegPose.equals(rotations5)) {
         this.setRightLegPose(rotations5);
      }

   }

   /**
    * Clears potion metadata values if the entity has no potion effects. Otherwise, updates potion effect color,
    * ambience, and invisibility metadata values
    */
   protected void updateInvisibilityStatus() {
      this.setInvisible(this.invisible);
   }

   public void setInvisible(boolean pInvisible) {
      this.invisible = pInvisible;
      super.setInvisible(pInvisible);
   }

   /**
    * If Animal, checks if the age timer is negative
    */
   public boolean isBaby() {
      return this.isSmall();
   }

   /**
    * Called by the /kill command.
    */
   public void kill() {
      this.remove();
   }

   public boolean ignoreExplosion() {
      return this.isInvisible();
   }

   public PushReaction getPistonPushReaction() {
      return this.isMarker() ? PushReaction.IGNORE : super.getPistonPushReaction();
   }

   private void setSmall(boolean pSmall) {
      this.entityData.set(DATA_CLIENT_FLAGS, this.setBit(this.entityData.get(DATA_CLIENT_FLAGS), 1, pSmall));
   }

   public boolean isSmall() {
      return (this.entityData.get(DATA_CLIENT_FLAGS) & 1) != 0;
   }

   private void setShowArms(boolean pShowArms) {
      this.entityData.set(DATA_CLIENT_FLAGS, this.setBit(this.entityData.get(DATA_CLIENT_FLAGS), 4, pShowArms));
   }

   public boolean isShowArms() {
      return (this.entityData.get(DATA_CLIENT_FLAGS) & 4) != 0;
   }

   private void setNoBasePlate(boolean pNoBasePlate) {
      this.entityData.set(DATA_CLIENT_FLAGS, this.setBit(this.entityData.get(DATA_CLIENT_FLAGS), 8, pNoBasePlate));
   }

   public boolean isNoBasePlate() {
      return (this.entityData.get(DATA_CLIENT_FLAGS) & 8) != 0;
   }

   /**
    * Marker defines where if true, the size is 0 and will not be rendered or intractable.
    */
   private void setMarker(boolean pMarker) {
      this.entityData.set(DATA_CLIENT_FLAGS, this.setBit(this.entityData.get(DATA_CLIENT_FLAGS), 16, pMarker));
   }

   /**
    * Gets whether the armor stand has marker enabled. If true, the armor stand's bounding box is set to zero and cannot
    * be interacted with.
    */
   public boolean isMarker() {
      return (this.entityData.get(DATA_CLIENT_FLAGS) & 16) != 0;
   }

   private byte setBit(byte pOldBit, int pOffset, boolean pValue) {
      if (pValue) {
         pOldBit = (byte)(pOldBit | pOffset);
      } else {
         pOldBit = (byte)(pOldBit & ~pOffset);
      }

      return pOldBit;
   }

   public void setHeadPose(Rotations pHeadPose) {
      this.headPose = pHeadPose;
      this.entityData.set(DATA_HEAD_POSE, pHeadPose);
   }

   public void setBodyPose(Rotations pBodyPose) {
      this.bodyPose = pBodyPose;
      this.entityData.set(DATA_BODY_POSE, pBodyPose);
   }

   public void setLeftArmPose(Rotations pLeftArmPose) {
      this.leftArmPose = pLeftArmPose;
      this.entityData.set(DATA_LEFT_ARM_POSE, pLeftArmPose);
   }

   public void setRightArmPose(Rotations pRightArmPose) {
      this.rightArmPose = pRightArmPose;
      this.entityData.set(DATA_RIGHT_ARM_POSE, pRightArmPose);
   }

   public void setLeftLegPose(Rotations pLeftLegPose) {
      this.leftLegPose = pLeftLegPose;
      this.entityData.set(DATA_LEFT_LEG_POSE, pLeftLegPose);
   }

   public void setRightLegPose(Rotations pRightLegPose) {
      this.rightLegPose = pRightLegPose;
      this.entityData.set(DATA_RIGHT_LEG_POSE, pRightLegPose);
   }

   public Rotations getHeadPose() {
      return this.headPose;
   }

   public Rotations getBodyPose() {
      return this.bodyPose;
   }

   @OnlyIn(Dist.CLIENT)
   public Rotations getLeftArmPose() {
      return this.leftArmPose;
   }

   @OnlyIn(Dist.CLIENT)
   public Rotations getRightArmPose() {
      return this.rightArmPose;
   }

   @OnlyIn(Dist.CLIENT)
   public Rotations getLeftLegPose() {
      return this.leftLegPose;
   }

   @OnlyIn(Dist.CLIENT)
   public Rotations getRightLegPose() {
      return this.rightLegPose;
   }

   /**
    * Returns true if other Entities should be prevented from moving through this Entity.
    */
   public boolean isPickable() {
      return super.isPickable() && !this.isMarker();
   }

   /**
    * Called when a player attacks an entity. If this returns true the attack will not happen.
    */
   public boolean skipAttackInteraction(Entity pEntity) {
      return pEntity instanceof PlayerEntity && !this.level.mayInteract((PlayerEntity)pEntity, this.blockPosition());
   }

   public HandSide getMainArm() {
      return HandSide.RIGHT;
   }

   protected SoundEvent getFallDamageSound(int pHeight) {
      return SoundEvents.ARMOR_STAND_FALL;
   }

   @Nullable
   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.ARMOR_STAND_HIT;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return SoundEvents.ARMOR_STAND_BREAK;
   }

   public void thunderHit(ServerWorld pLevel, LightningBoltEntity pLightning) {
   }

   /**
    * Returns false if the entity is an armor stand. Returns true for all other entity living bases.
    */
   public boolean isAffectedByPotions() {
      return false;
   }

   public void onSyncedDataUpdated(DataParameter<?> pKey) {
      if (DATA_CLIENT_FLAGS.equals(pKey)) {
         this.refreshDimensions();
         this.blocksBuilding = !this.isMarker();
      }

      super.onSyncedDataUpdated(pKey);
   }

   public boolean attackable() {
      return false;
   }

   public EntitySize getDimensions(Pose pPose) {
      return this.getDimensionsMarker(this.isMarker());
   }

   private EntitySize getDimensionsMarker(boolean pIsMarker) {
      if (pIsMarker) {
         return MARKER_DIMENSIONS;
      } else {
         return this.isBaby() ? BABY_DIMENSIONS : this.getType().getDimensions();
      }
   }

   @OnlyIn(Dist.CLIENT)
   public Vector3d getLightProbePosition(float pPartialTicks) {
      if (this.isMarker()) {
         AxisAlignedBB axisalignedbb = this.getDimensionsMarker(false).makeBoundingBox(this.position());
         BlockPos blockpos = this.blockPosition();
         int i = Integer.MIN_VALUE;

         for(BlockPos blockpos1 : BlockPos.betweenClosed(new BlockPos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ), new BlockPos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ))) {
            int j = Math.max(this.level.getBrightness(LightType.BLOCK, blockpos1), this.level.getBrightness(LightType.SKY, blockpos1));
            if (j == 15) {
               return Vector3d.atCenterOf(blockpos1);
            }

            if (j > i) {
               i = j;
               blockpos = blockpos1.immutable();
            }
         }

         return Vector3d.atCenterOf(blockpos);
      } else {
         return super.getLightProbePosition(pPartialTicks);
      }
   }
}
