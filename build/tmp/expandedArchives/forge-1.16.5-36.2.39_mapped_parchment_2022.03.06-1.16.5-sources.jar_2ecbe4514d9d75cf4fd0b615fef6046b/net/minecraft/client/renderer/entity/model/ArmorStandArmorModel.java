package net.minecraft.client.renderer.entity.model;

import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ArmorStandArmorModel extends BipedModel<ArmorStandEntity> {
   public ArmorStandArmorModel(float p_i46307_1_) {
      this(p_i46307_1_, 64, 32);
   }

   protected ArmorStandArmorModel(float p_i46308_1_, int p_i46308_2_, int p_i46308_3_) {
      super(p_i46308_1_, 0.0F, p_i46308_2_, p_i46308_3_);
   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(ArmorStandEntity pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      this.head.xRot = ((float)Math.PI / 180F) * pEntity.getHeadPose().getX();
      this.head.yRot = ((float)Math.PI / 180F) * pEntity.getHeadPose().getY();
      this.head.zRot = ((float)Math.PI / 180F) * pEntity.getHeadPose().getZ();
      this.head.setPos(0.0F, 1.0F, 0.0F);
      this.body.xRot = ((float)Math.PI / 180F) * pEntity.getBodyPose().getX();
      this.body.yRot = ((float)Math.PI / 180F) * pEntity.getBodyPose().getY();
      this.body.zRot = ((float)Math.PI / 180F) * pEntity.getBodyPose().getZ();
      this.leftArm.xRot = ((float)Math.PI / 180F) * pEntity.getLeftArmPose().getX();
      this.leftArm.yRot = ((float)Math.PI / 180F) * pEntity.getLeftArmPose().getY();
      this.leftArm.zRot = ((float)Math.PI / 180F) * pEntity.getLeftArmPose().getZ();
      this.rightArm.xRot = ((float)Math.PI / 180F) * pEntity.getRightArmPose().getX();
      this.rightArm.yRot = ((float)Math.PI / 180F) * pEntity.getRightArmPose().getY();
      this.rightArm.zRot = ((float)Math.PI / 180F) * pEntity.getRightArmPose().getZ();
      this.leftLeg.xRot = ((float)Math.PI / 180F) * pEntity.getLeftLegPose().getX();
      this.leftLeg.yRot = ((float)Math.PI / 180F) * pEntity.getLeftLegPose().getY();
      this.leftLeg.zRot = ((float)Math.PI / 180F) * pEntity.getLeftLegPose().getZ();
      this.leftLeg.setPos(1.9F, 11.0F, 0.0F);
      this.rightLeg.xRot = ((float)Math.PI / 180F) * pEntity.getRightLegPose().getX();
      this.rightLeg.yRot = ((float)Math.PI / 180F) * pEntity.getRightLegPose().getY();
      this.rightLeg.zRot = ((float)Math.PI / 180F) * pEntity.getRightLegPose().getZ();
      this.rightLeg.setPos(-1.9F, 11.0F, 0.0F);
      this.hat.copyFrom(this.head);
   }
}