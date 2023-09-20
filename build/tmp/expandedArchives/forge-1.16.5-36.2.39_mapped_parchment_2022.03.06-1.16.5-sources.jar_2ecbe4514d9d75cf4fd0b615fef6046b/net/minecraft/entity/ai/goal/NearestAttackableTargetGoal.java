package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class NearestAttackableTargetGoal<T extends LivingEntity> extends TargetGoal {
   protected final Class<T> targetType;
   protected final int randomInterval;
   protected LivingEntity target;
   /** This filter is applied to the Entity search. Only matching entities will be targeted. */
   protected EntityPredicate targetConditions;

   public NearestAttackableTargetGoal(MobEntity p_i50313_1_, Class<T> p_i50313_2_, boolean p_i50313_3_) {
      this(p_i50313_1_, p_i50313_2_, p_i50313_3_, false);
   }

   public NearestAttackableTargetGoal(MobEntity p_i50314_1_, Class<T> p_i50314_2_, boolean p_i50314_3_, boolean p_i50314_4_) {
      this(p_i50314_1_, p_i50314_2_, 10, p_i50314_3_, p_i50314_4_, (Predicate<LivingEntity>)null);
   }

   public NearestAttackableTargetGoal(MobEntity p_i50315_1_, Class<T> p_i50315_2_, int p_i50315_3_, boolean p_i50315_4_, boolean p_i50315_5_, @Nullable Predicate<LivingEntity> p_i50315_6_) {
      super(p_i50315_1_, p_i50315_4_, p_i50315_5_);
      this.targetType = p_i50315_2_;
      this.randomInterval = p_i50315_3_;
      this.setFlags(EnumSet.of(Goal.Flag.TARGET));
      this.targetConditions = (new EntityPredicate()).range(this.getFollowDistance()).selector(p_i50315_6_);
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
         return false;
      } else {
         this.findTarget();
         return this.target != null;
      }
   }

   protected AxisAlignedBB getTargetSearchArea(double pTargetDistance) {
      return this.mob.getBoundingBox().inflate(pTargetDistance, 4.0D, pTargetDistance);
   }

   protected void findTarget() {
      if (this.targetType != PlayerEntity.class && this.targetType != ServerPlayerEntity.class) {
         this.target = this.mob.level.getNearestLoadedEntity(this.targetType, this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ(), this.getTargetSearchArea(this.getFollowDistance()));
      } else {
         this.target = this.mob.level.getNearestPlayer(this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
      }

   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.mob.setTarget(this.target);
      super.start();
   }

   public void setTarget(@Nullable LivingEntity pTarget) {
      this.target = pTarget;
   }
}