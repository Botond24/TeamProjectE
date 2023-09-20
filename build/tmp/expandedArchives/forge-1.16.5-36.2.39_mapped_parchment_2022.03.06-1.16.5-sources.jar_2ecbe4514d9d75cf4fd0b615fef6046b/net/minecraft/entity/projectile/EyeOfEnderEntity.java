package net.minecraft.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(
   value = Dist.CLIENT,
   _interface = IRendersAsItem.class
)
public class EyeOfEnderEntity extends Entity implements IRendersAsItem {
   private static final DataParameter<ItemStack> DATA_ITEM_STACK = EntityDataManager.defineId(EyeOfEnderEntity.class, DataSerializers.ITEM_STACK);
   private double tx;
   private double ty;
   private double tz;
   private int life;
   private boolean surviveAfterDeath;

   public EyeOfEnderEntity(EntityType<? extends EyeOfEnderEntity> p_i50169_1_, World p_i50169_2_) {
      super(p_i50169_1_, p_i50169_2_);
   }

   public EyeOfEnderEntity(World pLevel, double pX, double pY, double pZ) {
      this(EntityType.EYE_OF_ENDER, pLevel);
      this.life = 0;
      this.setPos(pX, pY, pZ);
   }

   public void setItem(ItemStack pStack) {
      if (pStack.getItem() != Items.ENDER_EYE || pStack.hasTag()) {
         this.getEntityData().set(DATA_ITEM_STACK, Util.make(pStack.copy(), (p_213862_0_) -> {
            p_213862_0_.setCount(1);
         }));
      }

   }

   private ItemStack getItemRaw() {
      return this.getEntityData().get(DATA_ITEM_STACK);
   }

   public ItemStack getItem() {
      ItemStack itemstack = this.getItemRaw();
      return itemstack.isEmpty() ? new ItemStack(Items.ENDER_EYE) : itemstack;
   }

   protected void defineSynchedData() {
      this.getEntityData().define(DATA_ITEM_STACK, ItemStack.EMPTY);
   }

   /**
    * Checks if the entity is in range to render.
    */
   @OnlyIn(Dist.CLIENT)
   public boolean shouldRenderAtSqrDistance(double pDistance) {
      double d0 = this.getBoundingBox().getSize() * 4.0D;
      if (Double.isNaN(d0)) {
         d0 = 4.0D;
      }

      d0 = d0 * 64.0D;
      return pDistance < d0 * d0;
   }

   public void signalTo(BlockPos pPos) {
      double d0 = (double)pPos.getX();
      int i = pPos.getY();
      double d1 = (double)pPos.getZ();
      double d2 = d0 - this.getX();
      double d3 = d1 - this.getZ();
      float f = MathHelper.sqrt(d2 * d2 + d3 * d3);
      if (f > 12.0F) {
         this.tx = this.getX() + d2 / (double)f * 12.0D;
         this.tz = this.getZ() + d3 / (double)f * 12.0D;
         this.ty = this.getY() + 8.0D;
      } else {
         this.tx = d0;
         this.ty = (double)i;
         this.tz = d1;
      }

      this.life = 0;
      this.surviveAfterDeath = this.random.nextInt(5) > 0;
   }

   /**
    * Updates the entity motion clientside, called by packets from the server
    */
   @OnlyIn(Dist.CLIENT)
   public void lerpMotion(double pX, double pY, double pZ) {
      this.setDeltaMovement(pX, pY, pZ);
      if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
         float f = MathHelper.sqrt(pX * pX + pZ * pZ);
         this.yRot = (float)(MathHelper.atan2(pX, pZ) * (double)(180F / (float)Math.PI));
         this.xRot = (float)(MathHelper.atan2(pY, (double)f) * (double)(180F / (float)Math.PI));
         this.yRotO = this.yRot;
         this.xRotO = this.xRot;
      }

   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      Vector3d vector3d = this.getDeltaMovement();
      double d0 = this.getX() + vector3d.x;
      double d1 = this.getY() + vector3d.y;
      double d2 = this.getZ() + vector3d.z;
      float f = MathHelper.sqrt(getHorizontalDistanceSqr(vector3d));
      this.xRot = ProjectileEntity.lerpRotation(this.xRotO, (float)(MathHelper.atan2(vector3d.y, (double)f) * (double)(180F / (float)Math.PI)));
      this.yRot = ProjectileEntity.lerpRotation(this.yRotO, (float)(MathHelper.atan2(vector3d.x, vector3d.z) * (double)(180F / (float)Math.PI)));
      if (!this.level.isClientSide) {
         double d3 = this.tx - d0;
         double d4 = this.tz - d2;
         float f1 = (float)Math.sqrt(d3 * d3 + d4 * d4);
         float f2 = (float)MathHelper.atan2(d4, d3);
         double d5 = MathHelper.lerp(0.0025D, (double)f, (double)f1);
         double d6 = vector3d.y;
         if (f1 < 1.0F) {
            d5 *= 0.8D;
            d6 *= 0.8D;
         }

         int j = this.getY() < this.ty ? 1 : -1;
         vector3d = new Vector3d(Math.cos((double)f2) * d5, d6 + ((double)j - d6) * (double)0.015F, Math.sin((double)f2) * d5);
         this.setDeltaMovement(vector3d);
      }

      float f3 = 0.25F;
      if (this.isInWater()) {
         for(int i = 0; i < 4; ++i) {
            this.level.addParticle(ParticleTypes.BUBBLE, d0 - vector3d.x * 0.25D, d1 - vector3d.y * 0.25D, d2 - vector3d.z * 0.25D, vector3d.x, vector3d.y, vector3d.z);
         }
      } else {
         this.level.addParticle(ParticleTypes.PORTAL, d0 - vector3d.x * 0.25D + this.random.nextDouble() * 0.6D - 0.3D, d1 - vector3d.y * 0.25D - 0.5D, d2 - vector3d.z * 0.25D + this.random.nextDouble() * 0.6D - 0.3D, vector3d.x, vector3d.y, vector3d.z);
      }

      if (!this.level.isClientSide) {
         this.setPos(d0, d1, d2);
         ++this.life;
         if (this.life > 80 && !this.level.isClientSide) {
            this.playSound(SoundEvents.ENDER_EYE_DEATH, 1.0F, 1.0F);
            this.remove();
            if (this.surviveAfterDeath) {
               this.level.addFreshEntity(new ItemEntity(this.level, this.getX(), this.getY(), this.getZ(), this.getItem()));
            } else {
               this.level.levelEvent(2003, this.blockPosition(), 0);
            }
         }
      } else {
         this.setPosRaw(d0, d1, d2);
      }

   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      ItemStack itemstack = this.getItemRaw();
      if (!itemstack.isEmpty()) {
         pCompound.put("Item", itemstack.save(new CompoundNBT()));
      }

   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      ItemStack itemstack = ItemStack.of(pCompound.getCompound("Item"));
      this.setItem(itemstack);
   }

   /**
    * Gets how bright this entity is.
    */
   public float getBrightness() {
      return 1.0F;
   }

   /**
    * Returns true if it's possible to attack this entity with an item.
    */
   public boolean isAttackable() {
      return false;
   }

   public IPacket<?> getAddEntityPacket() {
      return new SSpawnObjectPacket(this);
   }
}