package net.minecraft.entity.item;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemEntity extends Entity {
   private static final DataParameter<ItemStack> DATA_ITEM = EntityDataManager.defineId(ItemEntity.class, DataSerializers.ITEM_STACK);
   private int age;
   private int pickupDelay;
   private int health = 5;
   private UUID thrower;
   private UUID owner;
   public final float bobOffs;
   /**
    * The maximum age of this EntityItem.  The item is expired once this is reached.
    */
   public int lifespan = 6000;

   public ItemEntity(EntityType<? extends ItemEntity> p_i50217_1_, World p_i50217_2_) {
      super(p_i50217_1_, p_i50217_2_);
      this.bobOffs = (float)(Math.random() * Math.PI * 2.0D);
   }

   public ItemEntity(World pLevel, double pX, double pY, double pZ) {
      this(EntityType.ITEM, pLevel);
      this.setPos(pX, pY, pZ);
      this.yRot = this.random.nextFloat() * 360.0F;
      this.setDeltaMovement(this.random.nextDouble() * 0.2D - 0.1D, 0.2D, this.random.nextDouble() * 0.2D - 0.1D);
   }

   public ItemEntity(World pLevel, double pX, double pY, double pZ, ItemStack pStack) {
      this(pLevel, pX, pY, pZ);
      this.setItem(pStack);
      this.lifespan = (pStack.getItem() == null ? 6000 : pStack.getEntityLifespan(pLevel));
   }

   @OnlyIn(Dist.CLIENT)
   private ItemEntity(ItemEntity pItemEntity) {
      super(pItemEntity.getType(), pItemEntity.level);
      this.setItem(pItemEntity.getItem().copy());
      this.copyPosition(pItemEntity);
      this.age = pItemEntity.age;
      this.bobOffs = pItemEntity.bobOffs;
   }

   protected boolean isMovementNoisy() {
      return false;
   }

   protected void defineSynchedData() {
      this.getEntityData().define(DATA_ITEM, ItemStack.EMPTY);
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      if (getItem().onEntityItemUpdate(this)) return;
      if (this.getItem().isEmpty()) {
         this.remove();
      } else {
         super.tick();
         if (this.pickupDelay > 0 && this.pickupDelay != 32767) {
            --this.pickupDelay;
         }

         this.xo = this.getX();
         this.yo = this.getY();
         this.zo = this.getZ();
         Vector3d vector3d = this.getDeltaMovement();
         float f = this.getEyeHeight() - 0.11111111F;
         if (this.isInWater() && this.getFluidHeight(FluidTags.WATER) > (double)f) {
            this.setUnderwaterMovement();
         } else if (this.isInLava() && this.getFluidHeight(FluidTags.LAVA) > (double)f) {
            this.setUnderLavaMovement();
         } else if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
         }

         if (this.level.isClientSide) {
            this.noPhysics = false;
         } else {
            this.noPhysics = !this.level.noCollision(this);
            if (this.noPhysics) {
               this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D, this.getZ());
            }
         }

         if (!this.onGround || getHorizontalDistanceSqr(this.getDeltaMovement()) > (double)1.0E-5F || (this.tickCount + this.getId()) % 4 == 0) {
            this.move(MoverType.SELF, this.getDeltaMovement());
            float f1 = 0.98F;
            if (this.onGround) {
               f1 = this.level.getBlockState(new BlockPos(this.getX(), this.getY() - 1.0D, this.getZ())).getSlipperiness(level, new BlockPos(this.getX(), this.getY() - 1.0D, this.getZ()), this) * 0.98F;
            }

            this.setDeltaMovement(this.getDeltaMovement().multiply((double)f1, 0.98D, (double)f1));
            if (this.onGround) {
               Vector3d vector3d1 = this.getDeltaMovement();
               if (vector3d1.y < 0.0D) {
                  this.setDeltaMovement(vector3d1.multiply(1.0D, -0.5D, 1.0D));
               }
            }
         }

         boolean flag = MathHelper.floor(this.xo) != MathHelper.floor(this.getX()) || MathHelper.floor(this.yo) != MathHelper.floor(this.getY()) || MathHelper.floor(this.zo) != MathHelper.floor(this.getZ());
         int i = flag ? 2 : 40;
         if (this.tickCount % i == 0) {
            if (this.level.getFluidState(this.blockPosition()).is(FluidTags.LAVA) && !this.fireImmune()) {
               this.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + this.random.nextFloat() * 0.4F);
            }

            if (!this.level.isClientSide && this.isMergable()) {
               this.mergeWithNeighbours();
            }
         }

         if (this.age != -32768) {
            ++this.age;
         }

         this.hasImpulse |= this.updateInWaterStateAndDoFluidPushing();
         if (!this.level.isClientSide) {
            double d0 = this.getDeltaMovement().subtract(vector3d).lengthSqr();
            if (d0 > 0.01D) {
               this.hasImpulse = true;
            }
         }

         ItemStack item = this.getItem();
         if (!this.level.isClientSide && this.age >= lifespan) {
             int hook = net.minecraftforge.event.ForgeEventFactory.onItemExpire(this, item);
             if (hook < 0) this.remove();
             else          this.lifespan += hook;
         }

         if (item.isEmpty()) {
            this.remove();
         }

      }
   }

   private void setUnderwaterMovement() {
      Vector3d vector3d = this.getDeltaMovement();
      this.setDeltaMovement(vector3d.x * (double)0.99F, vector3d.y + (double)(vector3d.y < (double)0.06F ? 5.0E-4F : 0.0F), vector3d.z * (double)0.99F);
   }

   private void setUnderLavaMovement() {
      Vector3d vector3d = this.getDeltaMovement();
      this.setDeltaMovement(vector3d.x * (double)0.95F, vector3d.y + (double)(vector3d.y < (double)0.06F ? 5.0E-4F : 0.0F), vector3d.z * (double)0.95F);
   }

   /**
    * Looks for other itemstacks nearby and tries to stack them together
    */
   private void mergeWithNeighbours() {
      if (this.isMergable()) {
         for(ItemEntity itementity : this.level.getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(0.5D, 0.0D, 0.5D), (p_213859_1_) -> {
            return p_213859_1_ != this && p_213859_1_.isMergable();
         })) {
            if (itementity.isMergable()) {
               this.tryToMerge(itementity);
               if (this.removed) {
                  break;
               }
            }
         }

      }
   }

   private boolean isMergable() {
      ItemStack itemstack = this.getItem();
      return this.isAlive() && this.pickupDelay != 32767 && this.age != -32768 && this.age < 6000 && itemstack.getCount() < itemstack.getMaxStackSize();
   }

   private void tryToMerge(ItemEntity pItemEntity) {
      ItemStack itemstack = this.getItem();
      ItemStack itemstack1 = pItemEntity.getItem();
      if (Objects.equals(this.getOwner(), pItemEntity.getOwner()) && areMergable(itemstack, itemstack1)) {
         if (itemstack1.getCount() < itemstack.getCount()) {
            merge(this, itemstack, pItemEntity, itemstack1);
         } else {
            merge(pItemEntity, itemstack1, this, itemstack);
         }

      }
   }

   public static boolean areMergable(ItemStack pDestinationStack, ItemStack pOriginStack) {
      if (pOriginStack.getItem() != pDestinationStack.getItem()) {
         return false;
      } else if (pOriginStack.getCount() + pDestinationStack.getCount() > pOriginStack.getMaxStackSize()) {
         return false;
      } else if (pOriginStack.hasTag() ^ pDestinationStack.hasTag()) {
         return false;
      } else if (!pDestinationStack.areCapsCompatible(pOriginStack)) {
         return false;
      } else {
         return !pOriginStack.hasTag() || pOriginStack.getTag().equals(pDestinationStack.getTag());
      }
   }

   public static ItemStack merge(ItemStack pDestinationStack, ItemStack pOriginStack, int pAmount) {
      int i = Math.min(Math.min(pDestinationStack.getMaxStackSize(), pAmount) - pDestinationStack.getCount(), pOriginStack.getCount());
      ItemStack itemstack = pDestinationStack.copy();
      itemstack.grow(i);
      pOriginStack.shrink(i);
      return itemstack;
   }

   private static void merge(ItemEntity pDestinationEntity, ItemStack pDestinationStack, ItemStack pOriginStack) {
      ItemStack itemstack = merge(pDestinationStack, pOriginStack, 64);
      pDestinationEntity.setItem(itemstack);
   }

   private static void merge(ItemEntity pDestinationEntity, ItemStack pDestinationStack, ItemEntity pOriginEntity, ItemStack pOriginStack) {
      merge(pDestinationEntity, pDestinationStack, pOriginStack);
      pDestinationEntity.pickupDelay = Math.max(pDestinationEntity.pickupDelay, pOriginEntity.pickupDelay);
      pDestinationEntity.age = Math.min(pDestinationEntity.age, pOriginEntity.age);
      if (pOriginStack.isEmpty()) {
         pOriginEntity.remove();
      }

   }

   public boolean fireImmune() {
      return this.getItem().getItem().isFireResistant() || super.fireImmune();
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (this.level.isClientSide || this.removed) return false; //Forge: Fixes MC-53850
      if (this.isInvulnerableTo(pSource)) {
         return false;
      } else if (!this.getItem().isEmpty() && this.getItem().getItem() == Items.NETHER_STAR && pSource.isExplosion()) {
         return false;
      } else if (!this.getItem().getItem().canBeHurtBy(pSource)) {
         return false;
      } else {
         this.markHurt();
         this.health = (int)((float)this.health - pAmount);
         if (this.health <= 0) {
            this.remove();
         }

         return false;
      }
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      pCompound.putShort("Health", (short)this.health);
      pCompound.putShort("Age", (short)this.age);
      pCompound.putShort("PickupDelay", (short)this.pickupDelay);
      pCompound.putInt("Lifespan", lifespan);
      if (this.getThrower() != null) {
         pCompound.putUUID("Thrower", this.getThrower());
      }

      if (this.getOwner() != null) {
         pCompound.putUUID("Owner", this.getOwner());
      }

      if (!this.getItem().isEmpty()) {
         pCompound.put("Item", this.getItem().save(new CompoundNBT()));
      }

   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      this.health = pCompound.getShort("Health");
      this.age = pCompound.getShort("Age");
      if (pCompound.contains("PickupDelay")) {
         this.pickupDelay = pCompound.getShort("PickupDelay");
      }
      if (pCompound.contains("Lifespan")) lifespan = pCompound.getInt("Lifespan");

      if (pCompound.hasUUID("Owner")) {
         this.owner = pCompound.getUUID("Owner");
      }

      if (pCompound.hasUUID("Thrower")) {
         this.thrower = pCompound.getUUID("Thrower");
      }

      CompoundNBT compoundnbt = pCompound.getCompound("Item");
      this.setItem(ItemStack.of(compoundnbt));
      if (this.getItem().isEmpty()) {
         this.remove();
      }

   }

   /**
    * Called by a player entity when they collide with an entity
    */
   public void playerTouch(PlayerEntity pEntity) {
      if (!this.level.isClientSide) {
         if (this.pickupDelay > 0) return;
         ItemStack itemstack = this.getItem();
         Item item = itemstack.getItem();
         int i = itemstack.getCount();

         int hook = net.minecraftforge.event.ForgeEventFactory.onItemPickup(this, pEntity);
         if (hook < 0) return;

         ItemStack copy = itemstack.copy();
         if (this.pickupDelay == 0 && (this.owner == null || lifespan - this.age <= 200 || this.owner.equals(pEntity.getUUID())) && (hook == 1 || i <= 0 || pEntity.inventory.add(itemstack))) {
            copy.setCount(copy.getCount() - getItem().getCount());
            net.minecraftforge.fml.hooks.BasicEventHooks.firePlayerItemPickupEvent(pEntity, this, copy);
            pEntity.take(this, i);
            if (itemstack.isEmpty()) {
               this.remove();
               itemstack.setCount(i);
            }

            pEntity.awardStat(Stats.ITEM_PICKED_UP.get(item), i);
            pEntity.onItemPickup(this);
         }

      }
   }

   public ITextComponent getName() {
      ITextComponent itextcomponent = this.getCustomName();
      return (ITextComponent)(itextcomponent != null ? itextcomponent : new TranslationTextComponent(this.getItem().getDescriptionId()));
   }

   /**
    * Returns true if it's possible to attack this entity with an item.
    */
   public boolean isAttackable() {
      return false;
   }

   @Nullable
   public Entity changeDimension(ServerWorld pServer, net.minecraftforge.common.util.ITeleporter teleporter) {
      Entity entity = super.changeDimension(pServer, teleporter);
      if (!this.level.isClientSide && entity instanceof ItemEntity) {
         ((ItemEntity)entity).mergeWithNeighbours();
      }

      return entity;
   }

   /**
    * Gets the item that this entity represents.
    */
   public ItemStack getItem() {
      return this.getEntityData().get(DATA_ITEM);
   }

   /**
    * Sets the item that this entity represents.
    */
   public void setItem(ItemStack pStack) {
      this.getEntityData().set(DATA_ITEM, pStack);
   }

   public void onSyncedDataUpdated(DataParameter<?> pKey) {
      super.onSyncedDataUpdated(pKey);
      if (DATA_ITEM.equals(pKey)) {
         this.getItem().setEntityRepresentation(this);
      }

   }

   @Nullable
   public UUID getOwner() {
      return this.owner;
   }

   public void setOwner(@Nullable UUID pOwner) {
      this.owner = pOwner;
   }

   @Nullable
   public UUID getThrower() {
      return this.thrower;
   }

   public void setThrower(@Nullable UUID pThrower) {
      this.thrower = pThrower;
   }

   @OnlyIn(Dist.CLIENT)
   public int getAge() {
      return this.age;
   }

   public void setDefaultPickUpDelay() {
      this.pickupDelay = 10;
   }

   public void setNoPickUpDelay() {
      this.pickupDelay = 0;
   }

   public void setNeverPickUp() {
      this.pickupDelay = 32767;
   }

   public void setPickUpDelay(int pPickupDelay) {
      this.pickupDelay = pPickupDelay;
   }

   public boolean hasPickUpDelay() {
      return this.pickupDelay > 0;
   }

   public void setExtendedLifetime() {
      this.age = -6000;
   }

   public void makeFakeItem() {
      this.setNeverPickUp();
      this.age = getItem().getEntityLifespan(level) - 1;
   }

   @OnlyIn(Dist.CLIENT)
   public float getSpin(float pPartialTicks) {
      return ((float)this.getAge() + pPartialTicks) / 20.0F + this.bobOffs;
   }

   public IPacket<?> getAddEntityPacket() {
      return new SSpawnObjectPacket(this);
   }

   @OnlyIn(Dist.CLIENT)
   public ItemEntity copy() {
      return new ItemEntity(this);
   }
}
