package net.minecraft.entity.projectile;

import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(
   value = Dist.CLIENT,
   _interface = IRendersAsItem.class
)
public class PotionEntity extends ProjectileItemEntity implements IRendersAsItem {
   public static final Predicate<LivingEntity> WATER_SENSITIVE = LivingEntity::isSensitiveToWater;

   public PotionEntity(EntityType<? extends PotionEntity> p_i50149_1_, World p_i50149_2_) {
      super(p_i50149_1_, p_i50149_2_);
   }

   public PotionEntity(World pLevel, LivingEntity pShooter) {
      super(EntityType.POTION, pShooter, pLevel);
   }

   public PotionEntity(World pLevel, double pX, double pY, double pZ) {
      super(EntityType.POTION, pX, pY, pZ, pLevel);
   }

   protected Item getDefaultItem() {
      return Items.SPLASH_POTION;
   }

   /**
    * Gets the amount of gravity to apply to the thrown entity with each tick.
    */
   protected float getGravity() {
      return 0.05F;
   }

   protected void onHitBlock(BlockRayTraceResult pResult) {
      super.onHitBlock(pResult);
      if (!this.level.isClientSide) {
         ItemStack itemstack = this.getItem();
         Potion potion = PotionUtils.getPotion(itemstack);
         List<EffectInstance> list = PotionUtils.getMobEffects(itemstack);
         boolean flag = potion == Potions.WATER && list.isEmpty();
         Direction direction = pResult.getDirection();
         BlockPos blockpos = pResult.getBlockPos();
         BlockPos blockpos1 = blockpos.relative(direction);
         if (flag) {
            this.dowseFire(blockpos1, direction);
            this.dowseFire(blockpos1.relative(direction.getOpposite()), direction);

            for(Direction direction1 : Direction.Plane.HORIZONTAL) {
               this.dowseFire(blockpos1.relative(direction1), direction1);
            }
         }

      }
   }

   /**
    * Called when this EntityFireball hits a block or entity.
    */
   protected void onHit(RayTraceResult pResult) {
      super.onHit(pResult);
      if (!this.level.isClientSide) {
         ItemStack itemstack = this.getItem();
         Potion potion = PotionUtils.getPotion(itemstack);
         List<EffectInstance> list = PotionUtils.getMobEffects(itemstack);
         boolean flag = potion == Potions.WATER && list.isEmpty();
         if (flag) {
            this.applyWater();
         } else if (!list.isEmpty()) {
            if (this.isLingering()) {
               this.makeAreaOfEffectCloud(itemstack, potion);
            } else {
               this.applySplash(list, pResult.getType() == RayTraceResult.Type.ENTITY ? ((EntityRayTraceResult)pResult).getEntity() : null);
            }
         }

         int i = potion.hasInstantEffects() ? 2007 : 2002;
         this.level.levelEvent(i, this.blockPosition(), PotionUtils.getColor(itemstack));
         this.remove();
      }
   }

   private void applyWater() {
      AxisAlignedBB axisalignedbb = this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
      List<LivingEntity> list = this.level.getEntitiesOfClass(LivingEntity.class, axisalignedbb, WATER_SENSITIVE);
      if (!list.isEmpty()) {
         for(LivingEntity livingentity : list) {
            double d0 = this.distanceToSqr(livingentity);
            if (d0 < 16.0D && livingentity.isSensitiveToWater()) {
               livingentity.hurt(DamageSource.indirectMagic(livingentity, this.getOwner()), 1.0F);
            }
         }
      }

   }

   private void applySplash(List<EffectInstance> pEffectInstances, @Nullable Entity pTarget) {
      AxisAlignedBB axisalignedbb = this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
      List<LivingEntity> list = this.level.getEntitiesOfClass(LivingEntity.class, axisalignedbb);
      if (!list.isEmpty()) {
         for(LivingEntity livingentity : list) {
            if (livingentity.isAffectedByPotions()) {
               double d0 = this.distanceToSqr(livingentity);
               if (d0 < 16.0D) {
                  double d1 = 1.0D - Math.sqrt(d0) / 4.0D;
                  if (livingentity == pTarget) {
                     d1 = 1.0D;
                  }

                  for(EffectInstance effectinstance : pEffectInstances) {
                     Effect effect = effectinstance.getEffect();
                     if (effect.isInstantenous()) {
                        effect.applyInstantenousEffect(this, this.getOwner(), livingentity, effectinstance.getAmplifier(), d1);
                     } else {
                        int i = (int)(d1 * (double)effectinstance.getDuration() + 0.5D);
                        if (i > 20) {
                           livingentity.addEffect(new EffectInstance(effect, i, effectinstance.getAmplifier(), effectinstance.isAmbient(), effectinstance.isVisible()));
                        }
                     }
                  }
               }
            }
         }
      }

   }

   private void makeAreaOfEffectCloud(ItemStack pStack, Potion pPotion) {
      AreaEffectCloudEntity areaeffectcloudentity = new AreaEffectCloudEntity(this.level, this.getX(), this.getY(), this.getZ());
      Entity entity = this.getOwner();
      if (entity instanceof LivingEntity) {
         areaeffectcloudentity.setOwner((LivingEntity)entity);
      }

      areaeffectcloudentity.setRadius(3.0F);
      areaeffectcloudentity.setRadiusOnUse(-0.5F);
      areaeffectcloudentity.setWaitTime(10);
      areaeffectcloudentity.setRadiusPerTick(-areaeffectcloudentity.getRadius() / (float)areaeffectcloudentity.getDuration());
      areaeffectcloudentity.setPotion(pPotion);

      for(EffectInstance effectinstance : PotionUtils.getCustomEffects(pStack)) {
         areaeffectcloudentity.addEffect(new EffectInstance(effectinstance));
      }

      CompoundNBT compoundnbt = pStack.getTag();
      if (compoundnbt != null && compoundnbt.contains("CustomPotionColor", 99)) {
         areaeffectcloudentity.setFixedColor(compoundnbt.getInt("CustomPotionColor"));
      }

      this.level.addFreshEntity(areaeffectcloudentity);
   }

   private boolean isLingering() {
      return this.getItem().getItem() == Items.LINGERING_POTION;
   }

   private void dowseFire(BlockPos pPos, Direction pDirection) {
      BlockState blockstate = this.level.getBlockState(pPos);
      if (blockstate.is(BlockTags.FIRE)) {
         this.level.removeBlock(pPos, false);
      } else if (CampfireBlock.isLitCampfire(blockstate)) {
         this.level.levelEvent((PlayerEntity)null, 1009, pPos, 0);
         CampfireBlock.dowse(this.level, pPos, blockstate);
         this.level.setBlockAndUpdate(pPos, blockstate.setValue(CampfireBlock.LIT, Boolean.valueOf(false)));
      }

   }
}