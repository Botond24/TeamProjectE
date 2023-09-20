package net.minecraft.entity.ai.goal;

import com.google.common.collect.Sets;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.monster.AbstractRaiderEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.raid.RaidManager;
import net.minecraft.world.server.ServerWorld;

public class MoveTowardsRaidGoal<T extends AbstractRaiderEntity> extends Goal {
   private final T mob;

   public MoveTowardsRaidGoal(T p_i50323_1_) {
      this.mob = p_i50323_1_;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      return this.mob.getTarget() == null && !this.mob.isVehicle() && this.mob.hasActiveRaid() && !this.mob.getCurrentRaid().isOver() && !((ServerWorld)this.mob.level).isVillage(this.mob.blockPosition());
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      return this.mob.hasActiveRaid() && !this.mob.getCurrentRaid().isOver() && this.mob.level instanceof ServerWorld && !((ServerWorld)this.mob.level).isVillage(this.mob.blockPosition());
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      if (this.mob.hasActiveRaid()) {
         Raid raid = this.mob.getCurrentRaid();
         if (this.mob.tickCount % 20 == 0) {
            this.recruitNearby(raid);
         }

         if (!this.mob.isPathFinding()) {
            Vector3d vector3d = RandomPositionGenerator.getPosTowards(this.mob, 15, 4, Vector3d.atBottomCenterOf(raid.getCenter()));
            if (vector3d != null) {
               this.mob.getNavigation().moveTo(vector3d.x, vector3d.y, vector3d.z, 1.0D);
            }
         }
      }

   }

   private void recruitNearby(Raid pRaid) {
      if (pRaid.isActive()) {
         Set<AbstractRaiderEntity> set = Sets.newHashSet();
         List<AbstractRaiderEntity> list = this.mob.level.getEntitiesOfClass(AbstractRaiderEntity.class, this.mob.getBoundingBox().inflate(16.0D), (p_220742_1_) -> {
            return !p_220742_1_.hasActiveRaid() && RaidManager.canJoinRaid(p_220742_1_, pRaid);
         });
         set.addAll(list);

         for(AbstractRaiderEntity abstractraiderentity : set) {
            pRaid.joinRaid(pRaid.getGroupsSpawned(), abstractraiderentity, (BlockPos)null, true);
         }
      }

   }
}