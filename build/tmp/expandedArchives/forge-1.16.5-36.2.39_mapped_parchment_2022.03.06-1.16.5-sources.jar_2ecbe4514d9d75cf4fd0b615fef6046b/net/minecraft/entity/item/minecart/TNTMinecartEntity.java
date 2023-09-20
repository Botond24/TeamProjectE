package net.minecraft.entity.item.minecart;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TNTMinecartEntity extends AbstractMinecartEntity {
   private int fuse = -1;

   public TNTMinecartEntity(EntityType<? extends TNTMinecartEntity> p_i50112_1_, World p_i50112_2_) {
      super(p_i50112_1_, p_i50112_2_);
   }

   public TNTMinecartEntity(World pLevel, double pX, double pY, double pZ) {
      super(EntityType.TNT_MINECART, pLevel, pX, pY, pZ);
   }

   public AbstractMinecartEntity.Type getMinecartType() {
      return AbstractMinecartEntity.Type.TNT;
   }

   public BlockState getDefaultDisplayBlockState() {
      return Blocks.TNT.defaultBlockState();
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      if (this.fuse > 0) {
         --this.fuse;
         this.level.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5D, this.getZ(), 0.0D, 0.0D, 0.0D);
      } else if (this.fuse == 0) {
         this.explode(getHorizontalDistanceSqr(this.getDeltaMovement()));
      }

      if (this.horizontalCollision) {
         double d0 = getHorizontalDistanceSqr(this.getDeltaMovement());
         if (d0 >= (double)0.01F) {
            this.explode(d0);
         }
      }

   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      Entity entity = pSource.getDirectEntity();
      if (entity instanceof AbstractArrowEntity) {
         AbstractArrowEntity abstractarrowentity = (AbstractArrowEntity)entity;
         if (abstractarrowentity.isOnFire()) {
            this.explode(abstractarrowentity.getDeltaMovement().lengthSqr());
         }
      }

      return super.hurt(pSource, pAmount);
   }

   public void destroy(DamageSource pSource) {
      double d0 = getHorizontalDistanceSqr(this.getDeltaMovement());
      if (!pSource.isFire() && !pSource.isExplosion() && !(d0 >= (double)0.01F)) {
         super.destroy(pSource);
         if (!pSource.isExplosion() && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            this.spawnAtLocation(Blocks.TNT);
         }

      } else {
         if (this.fuse < 0) {
            this.primeFuse();
            this.fuse = this.random.nextInt(20) + this.random.nextInt(20);
         }

      }
   }

   /**
    * Makes the minecart explode.
    */
   protected void explode(double pRadiusModifier) {
      if (!this.level.isClientSide) {
         double d0 = Math.sqrt(pRadiusModifier);
         if (d0 > 5.0D) {
            d0 = 5.0D;
         }

         this.level.explode(this, this.getX(), this.getY(), this.getZ(), (float)(4.0D + this.random.nextDouble() * 1.5D * d0), Explosion.Mode.BREAK);
         this.remove();
      }

   }

   public boolean causeFallDamage(float pFallDistance, float pDamageMultiplier) {
      if (pFallDistance >= 3.0F) {
         float f = pFallDistance / 10.0F;
         this.explode((double)(f * f));
      }

      return super.causeFallDamage(pFallDistance, pDamageMultiplier);
   }

   /**
    * Called every tick the minecart is on an activator rail.
    */
   public void activateMinecart(int pX, int pY, int pZ, boolean pReceivingPower) {
      if (pReceivingPower && this.fuse < 0) {
         this.primeFuse();
      }

   }

   /**
    * Handles an entity event fired from {@link net.minecraft.world.level.Level#broadcastEntityEvent}.
    */
   @OnlyIn(Dist.CLIENT)
   public void handleEntityEvent(byte pId) {
      if (pId == 10) {
         this.primeFuse();
      } else {
         super.handleEntityEvent(pId);
      }

   }

   /**
    * Ignites this TNT cart.
    */
   public void primeFuse() {
      this.fuse = 80;
      if (!this.level.isClientSide) {
         this.level.broadcastEntityEvent(this, (byte)10);
         if (!this.isSilent()) {
            this.level.playSound((PlayerEntity)null, this.getX(), this.getY(), this.getZ(), SoundEvents.TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
         }
      }

   }

   /**
    * Gets the remaining fuse time in ticks.
    */
   @OnlyIn(Dist.CLIENT)
   public int getFuse() {
      return this.fuse;
   }

   /**
    * Returns true if the TNT minecart is ignited.
    */
   public boolean isPrimed() {
      return this.fuse > -1;
   }

   /**
    * Explosion resistance of a block relative to this entity
    */
   public float getBlockExplosionResistance(Explosion pExplosion, IBlockReader pLevel, BlockPos pPos, BlockState pBlockState, FluidState pFluidState, float pExplosionPower) {
      return !this.isPrimed() || !pBlockState.is(BlockTags.RAILS) && !pLevel.getBlockState(pPos.above()).is(BlockTags.RAILS) ? super.getBlockExplosionResistance(pExplosion, pLevel, pPos, pBlockState, pFluidState, pExplosionPower) : 0.0F;
   }

   public boolean shouldBlockExplode(Explosion pExplosion, IBlockReader pLevel, BlockPos pPos, BlockState pBlockState, float pExplosionPower) {
      return !this.isPrimed() || !pBlockState.is(BlockTags.RAILS) && !pLevel.getBlockState(pPos.above()).is(BlockTags.RAILS) ? super.shouldBlockExplode(pExplosion, pLevel, pPos, pBlockState, pExplosionPower) : false;
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   protected void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.contains("TNTFuse", 99)) {
         this.fuse = pCompound.getInt("TNTFuse");
      }

   }

   protected void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("TNTFuse", this.fuse);
   }
}