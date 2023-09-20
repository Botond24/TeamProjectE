package net.minecraft.entity.monster;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.raid.Raid;

public abstract class PatrollerEntity extends MonsterEntity {
   private BlockPos patrolTarget;
   private boolean patrolLeader;
   private boolean patrolling;

   protected PatrollerEntity(EntityType<? extends PatrollerEntity> p_i50201_1_, World p_i50201_2_) {
      super(p_i50201_1_, p_i50201_2_);
   }

   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(4, new PatrollerEntity.PatrolGoal<>(this, 0.7D, 0.595D));
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      if (this.patrolTarget != null) {
         pCompound.put("PatrolTarget", NBTUtil.writeBlockPos(this.patrolTarget));
      }

      pCompound.putBoolean("PatrolLeader", this.patrolLeader);
      pCompound.putBoolean("Patrolling", this.patrolling);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.contains("PatrolTarget")) {
         this.patrolTarget = NBTUtil.readBlockPos(pCompound.getCompound("PatrolTarget"));
      }

      this.patrolLeader = pCompound.getBoolean("PatrolLeader");
      this.patrolling = pCompound.getBoolean("Patrolling");
   }

   /**
    * Returns the Y Offset of this entity.
    */
   public double getMyRidingOffset() {
      return -0.45D;
   }

   public boolean canBeLeader() {
      return true;
   }

   @Nullable
   public ILivingEntityData finalizeSpawn(IServerWorld pLevel, DifficultyInstance pDifficulty, SpawnReason pReason, @Nullable ILivingEntityData pSpawnData, @Nullable CompoundNBT pDataTag) {
      if (pReason != SpawnReason.PATROL && pReason != SpawnReason.EVENT && pReason != SpawnReason.STRUCTURE && this.random.nextFloat() < 0.06F && this.canBeLeader()) {
         this.patrolLeader = true;
      }

      if (this.isPatrolLeader()) {
         this.setItemSlot(EquipmentSlotType.HEAD, Raid.getLeaderBannerInstance());
         this.setDropChance(EquipmentSlotType.HEAD, 2.0F);
      }

      if (pReason == SpawnReason.PATROL) {
         this.patrolling = true;
      }

      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   public static boolean checkPatrollingMonsterSpawnRules(EntityType<? extends PatrollerEntity> pPatrollingMonster, IWorld pLevel, SpawnReason pSpawnType, BlockPos pPos, Random pRandom) {
      return pLevel.getBrightness(LightType.BLOCK, pPos) > 8 ? false : checkAnyLightMonsterSpawnRules(pPatrollingMonster, pLevel, pSpawnType, pPos, pRandom);
   }

   public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
      return !this.patrolling || pDistanceToClosestPlayer > 16384.0D;
   }

   public void setPatrolTarget(BlockPos pPatrolTarget) {
      this.patrolTarget = pPatrolTarget;
      this.patrolling = true;
   }

   public BlockPos getPatrolTarget() {
      return this.patrolTarget;
   }

   public boolean hasPatrolTarget() {
      return this.patrolTarget != null;
   }

   public void setPatrolLeader(boolean pPatrolLeader) {
      this.patrolLeader = pPatrolLeader;
      this.patrolling = true;
   }

   public boolean isPatrolLeader() {
      return this.patrolLeader;
   }

   public boolean canJoinPatrol() {
      return true;
   }

   public void findPatrolTarget() {
      this.patrolTarget = this.blockPosition().offset(-500 + this.random.nextInt(1000), 0, -500 + this.random.nextInt(1000));
      this.patrolling = true;
   }

   protected boolean isPatrolling() {
      return this.patrolling;
   }

   protected void setPatrolling(boolean pPatrolling) {
      this.patrolling = pPatrolling;
   }

   public static class PatrolGoal<T extends PatrollerEntity> extends Goal {
      private final T mob;
      private final double speedModifier;
      private final double leaderSpeedModifier;
      private long cooldownUntil;

      public PatrolGoal(T pMob, double pSpeedModifier, double pLeaderSpeedModifier) {
         this.mob = pMob;
         this.speedModifier = pSpeedModifier;
         this.leaderSpeedModifier = pLeaderSpeedModifier;
         this.cooldownUntil = -1L;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         boolean flag = this.mob.level.getGameTime() < this.cooldownUntil;
         return this.mob.isPatrolling() && this.mob.getTarget() == null && !this.mob.isVehicle() && this.mob.hasPatrolTarget() && !flag;
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         boolean flag = this.mob.isPatrolLeader();
         PathNavigator pathnavigator = this.mob.getNavigation();
         if (pathnavigator.isDone()) {
            List<PatrollerEntity> list = this.findPatrolCompanions();
            if (this.mob.isPatrolling() && list.isEmpty()) {
               this.mob.setPatrolling(false);
            } else if (flag && this.mob.getPatrolTarget().closerThan(this.mob.position(), 10.0D)) {
               this.mob.findPatrolTarget();
            } else {
               Vector3d vector3d = Vector3d.atBottomCenterOf(this.mob.getPatrolTarget());
               Vector3d vector3d1 = this.mob.position();
               Vector3d vector3d2 = vector3d1.subtract(vector3d);
               vector3d = vector3d2.yRot(90.0F).scale(0.4D).add(vector3d);
               Vector3d vector3d3 = vector3d.subtract(vector3d1).normalize().scale(10.0D).add(vector3d1);
               BlockPos blockpos = new BlockPos(vector3d3);
               blockpos = this.mob.level.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, blockpos);
               if (!pathnavigator.moveTo((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), flag ? this.leaderSpeedModifier : this.speedModifier)) {
                  this.moveRandomly();
                  this.cooldownUntil = this.mob.level.getGameTime() + 200L;
               } else if (flag) {
                  for(PatrollerEntity patrollerentity : list) {
                     patrollerentity.setPatrolTarget(blockpos);
                  }
               }
            }
         }

      }

      private List<PatrollerEntity> findPatrolCompanions() {
         return this.mob.level.getEntitiesOfClass(PatrollerEntity.class, this.mob.getBoundingBox().inflate(16.0D), (p_226543_1_) -> {
            return p_226543_1_.canJoinPatrol() && !p_226543_1_.is(this.mob);
         });
      }

      private boolean moveRandomly() {
         Random random = this.mob.getRandom();
         BlockPos blockpos = this.mob.level.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, this.mob.blockPosition().offset(-8 + random.nextInt(16), 0, -8 + random.nextInt(16)));
         return this.mob.getNavigation().moveTo((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), this.speedModifier);
      }
   }
}