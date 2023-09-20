package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class JumpOnBedTask extends Task<MobEntity> {
   private final float speedModifier;
   @Nullable
   private BlockPos targetBed;
   private int remainingTimeToReachBed;
   private int remainingJumps;
   private int remainingCooldownUntilNextJump;

   public JumpOnBedTask(float p_i50362_1_) {
      super(ImmutableMap.of(MemoryModuleType.NEAREST_BED, MemoryModuleStatus.VALUE_PRESENT, MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT));
      this.speedModifier = p_i50362_1_;
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, MobEntity pOwner) {
      return pOwner.isBaby() && this.nearBed(pLevel, pOwner);
   }

   protected void start(ServerWorld pLevel, MobEntity pEntity, long pGameTime) {
      super.start(pLevel, pEntity, pGameTime);
      this.getNearestBed(pEntity).ifPresent((p_220461_3_) -> {
         this.targetBed = p_220461_3_;
         this.remainingTimeToReachBed = 100;
         this.remainingJumps = 3 + pLevel.random.nextInt(4);
         this.remainingCooldownUntilNextJump = 0;
         this.startWalkingTowardsBed(pEntity, p_220461_3_);
      });
   }

   protected void stop(ServerWorld pLevel, MobEntity pEntity, long pGameTime) {
      super.stop(pLevel, pEntity, pGameTime);
      this.targetBed = null;
      this.remainingTimeToReachBed = 0;
      this.remainingJumps = 0;
      this.remainingCooldownUntilNextJump = 0;
   }

   protected boolean canStillUse(ServerWorld pLevel, MobEntity pEntity, long pGameTime) {
      return pEntity.isBaby() && this.targetBed != null && this.isBed(pLevel, this.targetBed) && !this.tiredOfWalking(pLevel, pEntity) && !this.tiredOfJumping(pLevel, pEntity);
   }

   protected boolean timedOut(long pGameTime) {
      return false;
   }

   protected void tick(ServerWorld pLevel, MobEntity pOwner, long pGameTime) {
      if (!this.onOrOverBed(pLevel, pOwner)) {
         --this.remainingTimeToReachBed;
      } else if (this.remainingCooldownUntilNextJump > 0) {
         --this.remainingCooldownUntilNextJump;
      } else {
         if (this.onBedSurface(pLevel, pOwner)) {
            pOwner.getJumpControl().jump();
            --this.remainingJumps;
            this.remainingCooldownUntilNextJump = 5;
         }

      }
   }

   private void startWalkingTowardsBed(MobEntity pMob, BlockPos pPos) {
      pMob.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(pPos, this.speedModifier, 0));
   }

   private boolean nearBed(ServerWorld p_220469_1_, MobEntity p_220469_2_) {
      return this.onOrOverBed(p_220469_1_, p_220469_2_) || this.getNearestBed(p_220469_2_).isPresent();
   }

   private boolean onOrOverBed(ServerWorld p_220468_1_, MobEntity p_220468_2_) {
      BlockPos blockpos = p_220468_2_.blockPosition();
      BlockPos blockpos1 = blockpos.below();
      return this.isBed(p_220468_1_, blockpos) || this.isBed(p_220468_1_, blockpos1);
   }

   private boolean onBedSurface(ServerWorld p_220465_1_, MobEntity p_220465_2_) {
      return this.isBed(p_220465_1_, p_220465_2_.blockPosition());
   }

   private boolean isBed(ServerWorld pLevel, BlockPos pPos) {
      return pLevel.getBlockState(pPos).is(BlockTags.BEDS);
   }

   private Optional<BlockPos> getNearestBed(MobEntity p_220463_1_) {
      return p_220463_1_.getBrain().getMemory(MemoryModuleType.NEAREST_BED);
   }

   private boolean tiredOfWalking(ServerWorld p_220464_1_, MobEntity p_220464_2_) {
      return !this.onOrOverBed(p_220464_1_, p_220464_2_) && this.remainingTimeToReachBed <= 0;
   }

   private boolean tiredOfJumping(ServerWorld p_220462_1_, MobEntity p_220462_2_) {
      return this.onOrOverBed(p_220462_1_, p_220462_2_) && this.remainingJumps <= 0;
   }
}