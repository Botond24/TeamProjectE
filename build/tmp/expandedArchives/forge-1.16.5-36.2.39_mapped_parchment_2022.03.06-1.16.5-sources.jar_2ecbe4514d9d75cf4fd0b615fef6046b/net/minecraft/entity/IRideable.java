package net.minecraft.entity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public interface IRideable {
   boolean boost();

   void travelWithInput(Vector3d pTravelVec);

   float getSteeringSpeed();

   default boolean travel(MobEntity pVehicle, BoostHelper pHelper, Vector3d pTravelVec) {
      if (!pVehicle.isAlive()) {
         return false;
      } else {
         Entity entity = pVehicle.getPassengers().isEmpty() ? null : pVehicle.getPassengers().get(0);
         if (pVehicle.isVehicle() && pVehicle.canBeControlledByRider() && entity instanceof PlayerEntity) {
            pVehicle.yRot = entity.yRot;
            pVehicle.yRotO = pVehicle.yRot;
            pVehicle.xRot = entity.xRot * 0.5F;
            pVehicle.setRot(pVehicle.yRot, pVehicle.xRot);
            pVehicle.yBodyRot = pVehicle.yRot;
            pVehicle.yHeadRot = pVehicle.yRot;
            pVehicle.maxUpStep = 1.0F;
            pVehicle.flyingSpeed = pVehicle.getSpeed() * 0.1F;
            if (pHelper.boosting && pHelper.boostTime++ > pHelper.boostTimeTotal) {
               pHelper.boosting = false;
            }

            if (pVehicle.isControlledByLocalInstance()) {
               float f = this.getSteeringSpeed();
               if (pHelper.boosting) {
                  f += f * 1.15F * MathHelper.sin((float)pHelper.boostTime / (float)pHelper.boostTimeTotal * (float)Math.PI);
               }

               pVehicle.setSpeed(f);
               this.travelWithInput(new Vector3d(0.0D, 0.0D, 1.0D));
               pVehicle.lerpSteps = 0;
            } else {
               pVehicle.calculateEntityAnimation(pVehicle, false);
               pVehicle.setDeltaMovement(Vector3d.ZERO);
            }

            return true;
         } else {
            pVehicle.maxUpStep = 0.5F;
            pVehicle.flyingSpeed = 0.02F;
            this.travelWithInput(pTravelVec);
            return false;
         }
      }
   }
}