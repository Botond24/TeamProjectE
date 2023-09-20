package net.minecraft.client.renderer.entity.model;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PandaModel<T extends PandaEntity> extends QuadrupedModel<T> {
   private float sitAmount;
   private float lieOnBackAmount;
   private float rollAmount;

   public PandaModel(int p_i51063_1_, float p_i51063_2_) {
      super(p_i51063_1_, p_i51063_2_, true, 23.0F, 4.8F, 2.7F, 3.0F, 49);
      this.texWidth = 64;
      this.texHeight = 64;
      this.head = new ModelRenderer(this, 0, 6);
      this.head.addBox(-6.5F, -5.0F, -4.0F, 13.0F, 10.0F, 9.0F);
      this.head.setPos(0.0F, 11.5F, -17.0F);
      this.head.texOffs(45, 16).addBox(-3.5F, 0.0F, -6.0F, 7.0F, 5.0F, 2.0F);
      this.head.texOffs(52, 25).addBox(-8.5F, -8.0F, -1.0F, 5.0F, 4.0F, 1.0F);
      this.head.texOffs(52, 25).addBox(3.5F, -8.0F, -1.0F, 5.0F, 4.0F, 1.0F);
      this.body = new ModelRenderer(this, 0, 25);
      this.body.addBox(-9.5F, -13.0F, -6.5F, 19.0F, 26.0F, 13.0F);
      this.body.setPos(0.0F, 10.0F, 0.0F);
      int i = 9;
      int j = 6;
      this.leg0 = new ModelRenderer(this, 40, 0);
      this.leg0.addBox(-3.0F, 0.0F, -3.0F, 6.0F, 9.0F, 6.0F);
      this.leg0.setPos(-5.5F, 15.0F, 9.0F);
      this.leg1 = new ModelRenderer(this, 40, 0);
      this.leg1.addBox(-3.0F, 0.0F, -3.0F, 6.0F, 9.0F, 6.0F);
      this.leg1.setPos(5.5F, 15.0F, 9.0F);
      this.leg2 = new ModelRenderer(this, 40, 0);
      this.leg2.addBox(-3.0F, 0.0F, -3.0F, 6.0F, 9.0F, 6.0F);
      this.leg2.setPos(-5.5F, 15.0F, -9.0F);
      this.leg3 = new ModelRenderer(this, 40, 0);
      this.leg3.addBox(-3.0F, 0.0F, -3.0F, 6.0F, 9.0F, 6.0F);
      this.leg3.setPos(5.5F, 15.0F, -9.0F);
   }

   public void prepareMobModel(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick) {
      super.prepareMobModel(pEntity, pLimbSwing, pLimbSwingAmount, pPartialTick);
      this.sitAmount = pEntity.getSitAmount(pPartialTick);
      this.lieOnBackAmount = pEntity.getLieOnBackAmount(pPartialTick);
      this.rollAmount = pEntity.isBaby() ? 0.0F : pEntity.getRollAmount(pPartialTick);
   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
      boolean flag = pEntity.getUnhappyCounter() > 0;
      boolean flag1 = pEntity.isSneezing();
      int i = pEntity.getSneezeCounter();
      boolean flag2 = pEntity.isEating();
      boolean flag3 = pEntity.isScared();
      if (flag) {
         this.head.yRot = 0.35F * MathHelper.sin(0.6F * pAgeInTicks);
         this.head.zRot = 0.35F * MathHelper.sin(0.6F * pAgeInTicks);
         this.leg2.xRot = -0.75F * MathHelper.sin(0.3F * pAgeInTicks);
         this.leg3.xRot = 0.75F * MathHelper.sin(0.3F * pAgeInTicks);
      } else {
         this.head.zRot = 0.0F;
      }

      if (flag1) {
         if (i < 15) {
            this.head.xRot = (-(float)Math.PI / 4F) * (float)i / 14.0F;
         } else if (i < 20) {
            float f = (float)((i - 15) / 5);
            this.head.xRot = (-(float)Math.PI / 4F) + ((float)Math.PI / 4F) * f;
         }
      }

      if (this.sitAmount > 0.0F) {
         this.body.xRot = ModelUtils.rotlerpRad(this.body.xRot, 1.7407963F, this.sitAmount);
         this.head.xRot = ModelUtils.rotlerpRad(this.head.xRot, ((float)Math.PI / 2F), this.sitAmount);
         this.leg2.zRot = -0.27079642F;
         this.leg3.zRot = 0.27079642F;
         this.leg0.zRot = 0.5707964F;
         this.leg1.zRot = -0.5707964F;
         if (flag2) {
            this.head.xRot = ((float)Math.PI / 2F) + 0.2F * MathHelper.sin(pAgeInTicks * 0.6F);
            this.leg2.xRot = -0.4F - 0.2F * MathHelper.sin(pAgeInTicks * 0.6F);
            this.leg3.xRot = -0.4F - 0.2F * MathHelper.sin(pAgeInTicks * 0.6F);
         }

         if (flag3) {
            this.head.xRot = 2.1707964F;
            this.leg2.xRot = -0.9F;
            this.leg3.xRot = -0.9F;
         }
      } else {
         this.leg0.zRot = 0.0F;
         this.leg1.zRot = 0.0F;
         this.leg2.zRot = 0.0F;
         this.leg3.zRot = 0.0F;
      }

      if (this.lieOnBackAmount > 0.0F) {
         this.leg0.xRot = -0.6F * MathHelper.sin(pAgeInTicks * 0.15F);
         this.leg1.xRot = 0.6F * MathHelper.sin(pAgeInTicks * 0.15F);
         this.leg2.xRot = 0.3F * MathHelper.sin(pAgeInTicks * 0.25F);
         this.leg3.xRot = -0.3F * MathHelper.sin(pAgeInTicks * 0.25F);
         this.head.xRot = ModelUtils.rotlerpRad(this.head.xRot, ((float)Math.PI / 2F), this.lieOnBackAmount);
      }

      if (this.rollAmount > 0.0F) {
         this.head.xRot = ModelUtils.rotlerpRad(this.head.xRot, 2.0561945F, this.rollAmount);
         this.leg0.xRot = -0.5F * MathHelper.sin(pAgeInTicks * 0.5F);
         this.leg1.xRot = 0.5F * MathHelper.sin(pAgeInTicks * 0.5F);
         this.leg2.xRot = 0.5F * MathHelper.sin(pAgeInTicks * 0.5F);
         this.leg3.xRot = -0.5F * MathHelper.sin(pAgeInTicks * 0.5F);
      }

   }
}