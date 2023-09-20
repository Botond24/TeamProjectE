package net.minecraft.entity.passive;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

public abstract class ShoulderRidingEntity extends TameableEntity {
   private int rideCooldownCounter;

   protected ShoulderRidingEntity(EntityType<? extends ShoulderRidingEntity> p_i48566_1_, World p_i48566_2_) {
      super(p_i48566_1_, p_i48566_2_);
   }

   public boolean setEntityOnShoulder(ServerPlayerEntity pPlayer) {
      CompoundNBT compoundnbt = new CompoundNBT();
      compoundnbt.putString("id", this.getEncodeId());
      this.saveWithoutId(compoundnbt);
      if (pPlayer.setEntityOnShoulder(compoundnbt)) {
         this.remove();
         return true;
      } else {
         return false;
      }
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      ++this.rideCooldownCounter;
      super.tick();
   }

   public boolean canSitOnShoulder() {
      return this.rideCooldownCounter > 100;
   }
}