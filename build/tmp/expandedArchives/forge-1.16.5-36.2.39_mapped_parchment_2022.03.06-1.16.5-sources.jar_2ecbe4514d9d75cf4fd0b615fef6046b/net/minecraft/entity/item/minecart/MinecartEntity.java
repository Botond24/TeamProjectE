package net.minecraft.entity.item.minecart;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class MinecartEntity extends AbstractMinecartEntity {
   public MinecartEntity(EntityType<?> p_i50126_1_, World p_i50126_2_) {
      super(p_i50126_1_, p_i50126_2_);
   }

   public MinecartEntity(World pLevel, double pX, double pY, double pZ) {
      super(EntityType.MINECART, pLevel, pX, pY, pZ);
   }

   public ActionResultType interact(PlayerEntity pPlayer, Hand pHand) {
      ActionResultType ret = super.interact(pPlayer, pHand);
      if (ret.consumesAction()) return ret;
      if (pPlayer.isSecondaryUseActive()) {
         return ActionResultType.PASS;
      } else if (this.isVehicle()) {
         return ActionResultType.PASS;
      } else if (!this.level.isClientSide) {
         return pPlayer.startRiding(this) ? ActionResultType.CONSUME : ActionResultType.PASS;
      } else {
         return ActionResultType.SUCCESS;
      }
   }

   /**
    * Called every tick the minecart is on an activator rail.
    */
   public void activateMinecart(int pX, int pY, int pZ, boolean pReceivingPower) {
      if (pReceivingPower) {
         if (this.isVehicle()) {
            this.ejectPassengers();
         }

         if (this.getHurtTime() == 0) {
            this.setHurtDir(-this.getHurtDir());
            this.setHurtTime(10);
            this.setDamage(50.0F);
            this.markHurt();
         }
      }

   }

   public AbstractMinecartEntity.Type getMinecartType() {
      return AbstractMinecartEntity.Type.RIDEABLE;
   }
}
