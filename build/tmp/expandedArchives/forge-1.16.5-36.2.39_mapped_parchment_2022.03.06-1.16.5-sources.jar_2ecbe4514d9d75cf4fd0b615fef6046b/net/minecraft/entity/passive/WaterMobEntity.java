package net.minecraft.entity.passive;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public abstract class WaterMobEntity extends CreatureEntity {
   protected WaterMobEntity(EntityType<? extends WaterMobEntity> p_i48565_1_, World p_i48565_2_) {
      super(p_i48565_1_, p_i48565_2_);
      this.setPathfindingMalus(PathNodeType.WATER, 0.0F);
   }

   public boolean canBreatheUnderwater() {
      return true;
   }

   public CreatureAttribute getMobType() {
      return CreatureAttribute.WATER;
   }

   public boolean checkSpawnObstruction(IWorldReader pLevel) {
      return pLevel.isUnobstructed(this);
   }

   /**
    * Get number of ticks, at least during which the living entity will be silent.
    */
   public int getAmbientSoundInterval() {
      return 120;
   }

   /**
    * Get the experience points the entity currently has.
    */
   protected int getExperienceReward(PlayerEntity pPlayer) {
      return 1 + this.level.random.nextInt(3);
   }

   protected void handleAirSupply(int pAirSupply) {
      if (this.isAlive() && !this.isInWaterOrBubble()) {
         this.setAirSupply(pAirSupply - 1);
         if (this.getAirSupply() == -20) {
            this.setAirSupply(0);
            this.hurt(DamageSource.DROWN, 2.0F);
         }
      } else {
         this.setAirSupply(300);
      }

   }

   /**
    * Gets called every tick from main Entity class
    */
   public void baseTick() {
      int i = this.getAirSupply();
      super.baseTick();
      this.handleAirSupply(i);
   }

   public boolean isPushedByFluid() {
      return false;
   }

   public boolean canBeLeashed(PlayerEntity pPlayer) {
      return false;
   }
}