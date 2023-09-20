package net.minecraft.client.renderer.entity.model;

import net.minecraft.entity.passive.CatEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CatModel<T extends CatEntity> extends OcelotModel<T> {
   private float lieDownAmount;
   private float lieDownAmountTail;
   private float relaxStateOneAmount;

   public CatModel(float p_i51069_1_) {
      super(p_i51069_1_);
   }

   public void prepareMobModel(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick) {
      this.lieDownAmount = pEntity.getLieDownAmount(pPartialTick);
      this.lieDownAmountTail = pEntity.getLieDownAmountTail(pPartialTick);
      this.relaxStateOneAmount = pEntity.getRelaxStateOneAmount(pPartialTick);
      if (this.lieDownAmount <= 0.0F) {
         this.head.xRot = 0.0F;
         this.head.zRot = 0.0F;
         this.frontLegL.xRot = 0.0F;
         this.frontLegL.zRot = 0.0F;
         this.frontLegR.xRot = 0.0F;
         this.frontLegR.zRot = 0.0F;
         this.frontLegR.x = -1.2F;
         this.backLegL.xRot = 0.0F;
         this.backLegR.xRot = 0.0F;
         this.backLegR.zRot = 0.0F;
         this.backLegR.x = -1.1F;
         this.backLegR.y = 18.0F;
      }

      super.prepareMobModel(pEntity, pLimbSwing, pLimbSwingAmount, pPartialTick);
      if (pEntity.isInSittingPose()) {
         this.body.xRot = ((float)Math.PI / 4F);
         this.body.y += -4.0F;
         this.body.z += 5.0F;
         this.head.y += -3.3F;
         ++this.head.z;
         this.tail1.y += 8.0F;
         this.tail1.z += -2.0F;
         this.tail2.y += 2.0F;
         this.tail2.z += -0.8F;
         this.tail1.xRot = 1.7278761F;
         this.tail2.xRot = 2.670354F;
         this.frontLegL.xRot = -0.15707964F;
         this.frontLegL.y = 16.1F;
         this.frontLegL.z = -7.0F;
         this.frontLegR.xRot = -0.15707964F;
         this.frontLegR.y = 16.1F;
         this.frontLegR.z = -7.0F;
         this.backLegL.xRot = (-(float)Math.PI / 2F);
         this.backLegL.y = 21.0F;
         this.backLegL.z = 1.0F;
         this.backLegR.xRot = (-(float)Math.PI / 2F);
         this.backLegR.y = 21.0F;
         this.backLegR.z = 1.0F;
         this.state = 3;
      }

   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
      if (this.lieDownAmount > 0.0F) {
         this.head.zRot = ModelUtils.rotlerpRad(this.head.zRot, -1.2707963F, this.lieDownAmount);
         this.head.yRot = ModelUtils.rotlerpRad(this.head.yRot, 1.2707963F, this.lieDownAmount);
         this.frontLegL.xRot = -1.2707963F;
         this.frontLegR.xRot = -0.47079635F;
         this.frontLegR.zRot = -0.2F;
         this.frontLegR.x = -0.2F;
         this.backLegL.xRot = -0.4F;
         this.backLegR.xRot = 0.5F;
         this.backLegR.zRot = -0.5F;
         this.backLegR.x = -0.3F;
         this.backLegR.y = 20.0F;
         this.tail1.xRot = ModelUtils.rotlerpRad(this.tail1.xRot, 0.8F, this.lieDownAmountTail);
         this.tail2.xRot = ModelUtils.rotlerpRad(this.tail2.xRot, -0.4F, this.lieDownAmountTail);
      }

      if (this.relaxStateOneAmount > 0.0F) {
         this.head.xRot = ModelUtils.rotlerpRad(this.head.xRot, -0.58177644F, this.relaxStateOneAmount);
      }

   }
}