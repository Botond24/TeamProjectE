package net.minecraft.entity.monster;

import java.util.EnumSet;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SilverfishBlock;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.RandomWalkingGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class SilverfishEntity extends MonsterEntity {
   private SilverfishEntity.SummonSilverfishGoal friendsGoal;

   public SilverfishEntity(EntityType<? extends SilverfishEntity> p_i50195_1_, World p_i50195_2_) {
      super(p_i50195_1_, p_i50195_2_);
   }

   protected void registerGoals() {
      this.friendsGoal = new SilverfishEntity.SummonSilverfishGoal(this);
      this.goalSelector.addGoal(1, new SwimGoal(this));
      this.goalSelector.addGoal(3, this.friendsGoal);
      this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, false));
      this.goalSelector.addGoal(5, new SilverfishEntity.HideInStoneGoal(this));
      this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers());
      this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
   }

   /**
    * Returns the Y Offset of this entity.
    */
   public double getMyRidingOffset() {
      return 0.1D;
   }

   protected float getStandingEyeHeight(Pose pPose, EntitySize pSize) {
      return 0.13F;
   }

   public static AttributeModifierMap.MutableAttribute createAttributes() {
      return MonsterEntity.createMonsterAttributes().add(Attributes.MAX_HEALTH, 8.0D).add(Attributes.MOVEMENT_SPEED, 0.25D).add(Attributes.ATTACK_DAMAGE, 1.0D);
   }

   protected boolean isMovementNoisy() {
      return false;
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.SILVERFISH_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.SILVERFISH_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.SILVERFISH_DEATH;
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      this.playSound(SoundEvents.SILVERFISH_STEP, 0.15F, 1.0F);
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (this.isInvulnerableTo(pSource)) {
         return false;
      } else {
         if ((pSource instanceof EntityDamageSource || pSource == DamageSource.MAGIC) && this.friendsGoal != null) {
            this.friendsGoal.notifyHurt();
         }

         return super.hurt(pSource, pAmount);
      }
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      this.yBodyRot = this.yRot;
      super.tick();
   }

   /**
    * Set the body Y rotation of the entity.
    */
   public void setYBodyRot(float pYBodyRot) {
      this.yRot = pYBodyRot;
      super.setYBodyRot(pYBodyRot);
   }

   public float getWalkTargetValue(BlockPos pPos, IWorldReader pLevel) {
      return SilverfishBlock.isCompatibleHostBlock(pLevel.getBlockState(pPos.below())) ? 10.0F : super.getWalkTargetValue(pPos, pLevel);
   }

   public static boolean checkSliverfishSpawnRules(EntityType<SilverfishEntity> pSilverfish, IWorld pLevel, SpawnReason pSpawnType, BlockPos pPos, Random pRandom) {
      if (checkAnyLightMonsterSpawnRules(pSilverfish, pLevel, pSpawnType, pPos, pRandom)) {
         PlayerEntity playerentity = pLevel.getNearestPlayer((double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, 5.0D, true);
         return playerentity == null;
      } else {
         return false;
      }
   }

   public CreatureAttribute getMobType() {
      return CreatureAttribute.ARTHROPOD;
   }

   static class HideInStoneGoal extends RandomWalkingGoal {
      private Direction selectedDirection;
      private boolean doMerge;

      public HideInStoneGoal(SilverfishEntity pSilverfish) {
         super(pSilverfish, 1.0D, 10);
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if (this.mob.getTarget() != null) {
            return false;
         } else if (!this.mob.getNavigation().isDone()) {
            return false;
         } else {
            Random random = this.mob.getRandom();
            if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.mob.level, this.mob) && random.nextInt(10) == 0) {
               this.selectedDirection = Direction.getRandom(random);
               BlockPos blockpos = (new BlockPos(this.mob.getX(), this.mob.getY() + 0.5D, this.mob.getZ())).relative(this.selectedDirection);
               BlockState blockstate = this.mob.level.getBlockState(blockpos);
               if (SilverfishBlock.isCompatibleHostBlock(blockstate)) {
                  this.doMerge = true;
                  return true;
               }
            }

            this.doMerge = false;
            return super.canUse();
         }
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return this.doMerge ? false : super.canContinueToUse();
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         if (!this.doMerge) {
            super.start();
         } else {
            IWorld iworld = this.mob.level;
            BlockPos blockpos = (new BlockPos(this.mob.getX(), this.mob.getY() + 0.5D, this.mob.getZ())).relative(this.selectedDirection);
            BlockState blockstate = iworld.getBlockState(blockpos);
            if (SilverfishBlock.isCompatibleHostBlock(blockstate)) {
               iworld.setBlock(blockpos, SilverfishBlock.stateByHostBlock(blockstate.getBlock()), 3);
               this.mob.spawnAnim();
               this.mob.remove();
            }

         }
      }
   }

   static class SummonSilverfishGoal extends Goal {
      private final SilverfishEntity silverfish;
      private int lookForFriends;

      public SummonSilverfishGoal(SilverfishEntity pSilverfish) {
         this.silverfish = pSilverfish;
      }

      public void notifyHurt() {
         if (this.lookForFriends == 0) {
            this.lookForFriends = 20;
         }

      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return this.lookForFriends > 0;
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         --this.lookForFriends;
         if (this.lookForFriends <= 0) {
            World world = this.silverfish.level;
            Random random = this.silverfish.getRandom();
            BlockPos blockpos = this.silverfish.blockPosition();

            for(int i = 0; i <= 5 && i >= -5; i = (i <= 0 ? 1 : 0) - i) {
               for(int j = 0; j <= 10 && j >= -10; j = (j <= 0 ? 1 : 0) - j) {
                  for(int k = 0; k <= 10 && k >= -10; k = (k <= 0 ? 1 : 0) - k) {
                     BlockPos blockpos1 = blockpos.offset(j, i, k);
                     BlockState blockstate = world.getBlockState(blockpos1);
                     Block block = blockstate.getBlock();
                     if (block instanceof SilverfishBlock) {
                        if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world, this.silverfish)) {
                           world.destroyBlock(blockpos1, true, this.silverfish);
                        } else {
                           world.setBlock(blockpos1, ((SilverfishBlock)block).getHostBlock().defaultBlockState(), 3);
                        }

                        if (random.nextBoolean()) {
                           return;
                        }
                     }
                  }
               }
            }
         }

      }
   }
}
