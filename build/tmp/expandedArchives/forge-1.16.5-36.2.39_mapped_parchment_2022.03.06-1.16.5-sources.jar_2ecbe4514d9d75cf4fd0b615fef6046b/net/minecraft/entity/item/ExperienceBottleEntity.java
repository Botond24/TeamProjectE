package net.minecraft.entity.item;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class ExperienceBottleEntity extends ProjectileItemEntity {
   public ExperienceBottleEntity(EntityType<? extends ExperienceBottleEntity> p_i50152_1_, World p_i50152_2_) {
      super(p_i50152_1_, p_i50152_2_);
   }

   public ExperienceBottleEntity(World pLevel, LivingEntity pShooter) {
      super(EntityType.EXPERIENCE_BOTTLE, pShooter, pLevel);
   }

   public ExperienceBottleEntity(World pLevel, double pX, double pY, double pZ) {
      super(EntityType.EXPERIENCE_BOTTLE, pX, pY, pZ, pLevel);
   }

   protected Item getDefaultItem() {
      return Items.EXPERIENCE_BOTTLE;
   }

   /**
    * Gets the amount of gravity to apply to the thrown entity with each tick.
    */
   protected float getGravity() {
      return 0.07F;
   }

   /**
    * Called when this EntityFireball hits a block or entity.
    */
   protected void onHit(RayTraceResult pResult) {
      super.onHit(pResult);
      if (!this.level.isClientSide) {
         this.level.levelEvent(2002, this.blockPosition(), PotionUtils.getColor(Potions.WATER));
         int i = 3 + this.level.random.nextInt(5) + this.level.random.nextInt(5);

         while(i > 0) {
            int j = ExperienceOrbEntity.getExperienceValue(i);
            i -= j;
            this.level.addFreshEntity(new ExperienceOrbEntity(this.level, this.getX(), this.getY(), this.getZ(), j));
         }

         this.remove();
      }

   }
}