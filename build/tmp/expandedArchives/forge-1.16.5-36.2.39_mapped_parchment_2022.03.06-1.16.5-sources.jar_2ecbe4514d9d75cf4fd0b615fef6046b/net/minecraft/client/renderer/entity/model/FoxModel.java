package net.minecraft.client.renderer.entity.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FoxModel<T extends FoxEntity> extends AgeableModel<T> {
   public final ModelRenderer head;
   private final ModelRenderer earL;
   private final ModelRenderer earR;
   private final ModelRenderer nose;
   private final ModelRenderer body;
   private final ModelRenderer leg0;
   private final ModelRenderer leg1;
   private final ModelRenderer leg2;
   private final ModelRenderer leg3;
   private final ModelRenderer tail;
   private float legMotionPos;

   public FoxModel() {
      super(true, 8.0F, 3.35F);
      this.texWidth = 48;
      this.texHeight = 32;
      this.head = new ModelRenderer(this, 1, 5);
      this.head.addBox(-3.0F, -2.0F, -5.0F, 8.0F, 6.0F, 6.0F);
      this.head.setPos(-1.0F, 16.5F, -3.0F);
      this.earL = new ModelRenderer(this, 8, 1);
      this.earL.addBox(-3.0F, -4.0F, -4.0F, 2.0F, 2.0F, 1.0F);
      this.earR = new ModelRenderer(this, 15, 1);
      this.earR.addBox(3.0F, -4.0F, -4.0F, 2.0F, 2.0F, 1.0F);
      this.nose = new ModelRenderer(this, 6, 18);
      this.nose.addBox(-1.0F, 2.01F, -8.0F, 4.0F, 2.0F, 3.0F);
      this.head.addChild(this.earL);
      this.head.addChild(this.earR);
      this.head.addChild(this.nose);
      this.body = new ModelRenderer(this, 24, 15);
      this.body.addBox(-3.0F, 3.999F, -3.5F, 6.0F, 11.0F, 6.0F);
      this.body.setPos(0.0F, 16.0F, -6.0F);
      float f = 0.001F;
      this.leg0 = new ModelRenderer(this, 13, 24);
      this.leg0.addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, 0.001F);
      this.leg0.setPos(-5.0F, 17.5F, 7.0F);
      this.leg1 = new ModelRenderer(this, 4, 24);
      this.leg1.addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, 0.001F);
      this.leg1.setPos(-1.0F, 17.5F, 7.0F);
      this.leg2 = new ModelRenderer(this, 13, 24);
      this.leg2.addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, 0.001F);
      this.leg2.setPos(-5.0F, 17.5F, 0.0F);
      this.leg3 = new ModelRenderer(this, 4, 24);
      this.leg3.addBox(2.0F, 0.5F, -1.0F, 2.0F, 6.0F, 2.0F, 0.001F);
      this.leg3.setPos(-1.0F, 17.5F, 0.0F);
      this.tail = new ModelRenderer(this, 30, 0);
      this.tail.addBox(2.0F, 0.0F, -1.0F, 4.0F, 9.0F, 5.0F);
      this.tail.setPos(-4.0F, 15.0F, -1.0F);
      this.body.addChild(this.tail);
   }

   public void prepareMobModel(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick) {
      this.body.xRot = ((float)Math.PI / 2F);
      this.tail.xRot = -0.05235988F;
      this.leg0.xRot = MathHelper.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount;
      this.leg1.xRot = MathHelper.cos(pLimbSwing * 0.6662F + (float)Math.PI) * 1.4F * pLimbSwingAmount;
      this.leg2.xRot = MathHelper.cos(pLimbSwing * 0.6662F + (float)Math.PI) * 1.4F * pLimbSwingAmount;
      this.leg3.xRot = MathHelper.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount;
      this.head.setPos(-1.0F, 16.5F, -3.0F);
      this.head.yRot = 0.0F;
      this.head.zRot = pEntity.getHeadRollAngle(pPartialTick);
      this.leg0.visible = true;
      this.leg1.visible = true;
      this.leg2.visible = true;
      this.leg3.visible = true;
      this.body.setPos(0.0F, 16.0F, -6.0F);
      this.body.zRot = 0.0F;
      this.leg0.setPos(-5.0F, 17.5F, 7.0F);
      this.leg1.setPos(-1.0F, 17.5F, 7.0F);
      if (pEntity.isCrouching()) {
         this.body.xRot = 1.6755161F;
         float f = pEntity.getCrouchAmount(pPartialTick);
         this.body.setPos(0.0F, 16.0F + pEntity.getCrouchAmount(pPartialTick), -6.0F);
         this.head.setPos(-1.0F, 16.5F + f, -3.0F);
         this.head.yRot = 0.0F;
      } else if (pEntity.isSleeping()) {
         this.body.zRot = (-(float)Math.PI / 2F);
         this.body.setPos(0.0F, 21.0F, -6.0F);
         this.tail.xRot = -2.6179938F;
         if (this.young) {
            this.tail.xRot = -2.1816616F;
            this.body.setPos(0.0F, 21.0F, -2.0F);
         }

         this.head.setPos(1.0F, 19.49F, -3.0F);
         this.head.xRot = 0.0F;
         this.head.yRot = -2.0943952F;
         this.head.zRot = 0.0F;
         this.leg0.visible = false;
         this.leg1.visible = false;
         this.leg2.visible = false;
         this.leg3.visible = false;
      } else if (pEntity.isSitting()) {
         this.body.xRot = ((float)Math.PI / 6F);
         this.body.setPos(0.0F, 9.0F, -3.0F);
         this.tail.xRot = ((float)Math.PI / 4F);
         this.tail.setPos(-4.0F, 15.0F, -2.0F);
         this.head.setPos(-1.0F, 10.0F, -0.25F);
         this.head.xRot = 0.0F;
         this.head.yRot = 0.0F;
         if (this.young) {
            this.head.setPos(-1.0F, 13.0F, -3.75F);
         }

         this.leg0.xRot = -1.3089969F;
         this.leg0.setPos(-5.0F, 21.5F, 6.75F);
         this.leg1.xRot = -1.3089969F;
         this.leg1.setPos(-1.0F, 21.5F, 6.75F);
         this.leg2.xRot = -0.2617994F;
         this.leg3.xRot = -0.2617994F;
      }

   }

   protected Iterable<ModelRenderer> headParts() {
      return ImmutableList.of(this.head);
   }

   protected Iterable<ModelRenderer> bodyParts() {
      return ImmutableList.of(this.body, this.leg0, this.leg1, this.leg2, this.leg3);
   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      if (!pEntity.isSleeping() && !pEntity.isFaceplanted() && !pEntity.isCrouching()) {
         this.head.xRot = pHeadPitch * ((float)Math.PI / 180F);
         this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
      }

      if (pEntity.isSleeping()) {
         this.head.xRot = 0.0F;
         this.head.yRot = -2.0943952F;
         this.head.zRot = MathHelper.cos(pAgeInTicks * 0.027F) / 22.0F;
      }

      if (pEntity.isCrouching()) {
         float f = MathHelper.cos(pAgeInTicks) * 0.01F;
         this.body.yRot = f;
         this.leg0.zRot = f;
         this.leg1.zRot = f;
         this.leg2.zRot = f / 2.0F;
         this.leg3.zRot = f / 2.0F;
      }

      if (pEntity.isFaceplanted()) {
         float f1 = 0.1F;
         this.legMotionPos += 0.67F;
         this.leg0.xRot = MathHelper.cos(this.legMotionPos * 0.4662F) * 0.1F;
         this.leg1.xRot = MathHelper.cos(this.legMotionPos * 0.4662F + (float)Math.PI) * 0.1F;
         this.leg2.xRot = MathHelper.cos(this.legMotionPos * 0.4662F + (float)Math.PI) * 0.1F;
         this.leg3.xRot = MathHelper.cos(this.legMotionPos * 0.4662F) * 0.1F;
      }

   }
}