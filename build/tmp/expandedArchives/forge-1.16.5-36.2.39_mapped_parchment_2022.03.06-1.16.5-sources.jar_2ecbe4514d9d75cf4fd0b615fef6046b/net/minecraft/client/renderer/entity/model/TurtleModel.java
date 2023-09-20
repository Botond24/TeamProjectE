package net.minecraft.client.renderer.entity.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TurtleModel<T extends TurtleEntity> extends QuadrupedModel<T> {
   private final ModelRenderer eggBelly;

   public TurtleModel(float p_i48834_1_) {
      super(12, p_i48834_1_, true, 120.0F, 0.0F, 9.0F, 6.0F, 120);
      this.texWidth = 128;
      this.texHeight = 64;
      this.head = new ModelRenderer(this, 3, 0);
      this.head.addBox(-3.0F, -1.0F, -3.0F, 6.0F, 5.0F, 6.0F, 0.0F);
      this.head.setPos(0.0F, 19.0F, -10.0F);
      this.body = new ModelRenderer(this);
      this.body.texOffs(7, 37).addBox(-9.5F, 3.0F, -10.0F, 19.0F, 20.0F, 6.0F, 0.0F);
      this.body.texOffs(31, 1).addBox(-5.5F, 3.0F, -13.0F, 11.0F, 18.0F, 3.0F, 0.0F);
      this.body.setPos(0.0F, 11.0F, -10.0F);
      this.eggBelly = new ModelRenderer(this);
      this.eggBelly.texOffs(70, 33).addBox(-4.5F, 3.0F, -14.0F, 9.0F, 18.0F, 1.0F, 0.0F);
      this.eggBelly.setPos(0.0F, 11.0F, -10.0F);
      int i = 1;
      this.leg0 = new ModelRenderer(this, 1, 23);
      this.leg0.addBox(-2.0F, 0.0F, 0.0F, 4.0F, 1.0F, 10.0F, 0.0F);
      this.leg0.setPos(-3.5F, 22.0F, 11.0F);
      this.leg1 = new ModelRenderer(this, 1, 12);
      this.leg1.addBox(-2.0F, 0.0F, 0.0F, 4.0F, 1.0F, 10.0F, 0.0F);
      this.leg1.setPos(3.5F, 22.0F, 11.0F);
      this.leg2 = new ModelRenderer(this, 27, 30);
      this.leg2.addBox(-13.0F, 0.0F, -2.0F, 13.0F, 1.0F, 5.0F, 0.0F);
      this.leg2.setPos(-5.0F, 21.0F, -4.0F);
      this.leg3 = new ModelRenderer(this, 27, 24);
      this.leg3.addBox(0.0F, 0.0F, -2.0F, 13.0F, 1.0F, 5.0F, 0.0F);
      this.leg3.setPos(5.0F, 21.0F, -4.0F);
   }

   protected Iterable<ModelRenderer> bodyParts() {
      return Iterables.concat(super.bodyParts(), ImmutableList.of(this.eggBelly));
   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
      this.leg0.xRot = MathHelper.cos(pLimbSwing * 0.6662F * 0.6F) * 0.5F * pLimbSwingAmount;
      this.leg1.xRot = MathHelper.cos(pLimbSwing * 0.6662F * 0.6F + (float)Math.PI) * 0.5F * pLimbSwingAmount;
      this.leg2.zRot = MathHelper.cos(pLimbSwing * 0.6662F * 0.6F + (float)Math.PI) * 0.5F * pLimbSwingAmount;
      this.leg3.zRot = MathHelper.cos(pLimbSwing * 0.6662F * 0.6F) * 0.5F * pLimbSwingAmount;
      this.leg2.xRot = 0.0F;
      this.leg3.xRot = 0.0F;
      this.leg2.yRot = 0.0F;
      this.leg3.yRot = 0.0F;
      this.leg0.yRot = 0.0F;
      this.leg1.yRot = 0.0F;
      this.eggBelly.xRot = ((float)Math.PI / 2F);
      if (!pEntity.isInWater() && pEntity.isOnGround()) {
         float f = pEntity.isLayingEgg() ? 4.0F : 1.0F;
         float f1 = pEntity.isLayingEgg() ? 2.0F : 1.0F;
         float f2 = 5.0F;
         this.leg2.yRot = MathHelper.cos(f * pLimbSwing * 5.0F + (float)Math.PI) * 8.0F * pLimbSwingAmount * f1;
         this.leg2.zRot = 0.0F;
         this.leg3.yRot = MathHelper.cos(f * pLimbSwing * 5.0F) * 8.0F * pLimbSwingAmount * f1;
         this.leg3.zRot = 0.0F;
         this.leg0.yRot = MathHelper.cos(pLimbSwing * 5.0F + (float)Math.PI) * 3.0F * pLimbSwingAmount;
         this.leg0.xRot = 0.0F;
         this.leg1.yRot = MathHelper.cos(pLimbSwing * 5.0F) * 3.0F * pLimbSwingAmount;
         this.leg1.xRot = 0.0F;
      }

      this.eggBelly.visible = !this.young && pEntity.hasEgg();
   }

   public void renderToBuffer(MatrixStack pMatrixStack, IVertexBuilder pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
      boolean flag = this.eggBelly.visible;
      if (flag) {
         pMatrixStack.pushPose();
         pMatrixStack.translate(0.0D, (double)-0.08F, 0.0D);
      }

      super.renderToBuffer(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
      if (flag) {
         pMatrixStack.popPose();
      }

   }
}