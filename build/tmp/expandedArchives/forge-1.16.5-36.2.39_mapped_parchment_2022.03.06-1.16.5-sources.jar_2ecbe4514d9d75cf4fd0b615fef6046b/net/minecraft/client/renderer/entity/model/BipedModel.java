package net.minecraft.client.renderer.entity.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelHelper;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BipedModel<T extends LivingEntity> extends AgeableModel<T> implements IHasArm, IHasHead {
   public ModelRenderer head;
   /** The Biped's Headwear. Used for the outer layer of player skins. */
   public ModelRenderer hat;
   public ModelRenderer body;
   /** The Biped's Right Arm */
   public ModelRenderer rightArm;
   /** The Biped's Left Arm */
   public ModelRenderer leftArm;
   /** The Biped's Right Leg */
   public ModelRenderer rightLeg;
   /** The Biped's Left Leg */
   public ModelRenderer leftLeg;
   public BipedModel.ArmPose leftArmPose = BipedModel.ArmPose.EMPTY;
   public BipedModel.ArmPose rightArmPose = BipedModel.ArmPose.EMPTY;
   public boolean crouching;
   public float swimAmount;

   public BipedModel(float p_i1148_1_) {
      this(RenderType::entityCutoutNoCull, p_i1148_1_, 0.0F, 64, 32);
   }

   protected BipedModel(float p_i1149_1_, float p_i1149_2_, int p_i1149_3_, int p_i1149_4_) {
      this(RenderType::entityCutoutNoCull, p_i1149_1_, p_i1149_2_, p_i1149_3_, p_i1149_4_);
   }

   public BipedModel(Function<ResourceLocation, RenderType> p_i225946_1_, float p_i225946_2_, float p_i225946_3_, int p_i225946_4_, int p_i225946_5_) {
      super(p_i225946_1_, true, 16.0F, 0.0F, 2.0F, 2.0F, 24.0F);
      this.texWidth = p_i225946_4_;
      this.texHeight = p_i225946_5_;
      this.head = new ModelRenderer(this, 0, 0);
      this.head.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, p_i225946_2_);
      this.head.setPos(0.0F, 0.0F + p_i225946_3_, 0.0F);
      this.hat = new ModelRenderer(this, 32, 0);
      this.hat.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, p_i225946_2_ + 0.5F);
      this.hat.setPos(0.0F, 0.0F + p_i225946_3_, 0.0F);
      this.body = new ModelRenderer(this, 16, 16);
      this.body.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, p_i225946_2_);
      this.body.setPos(0.0F, 0.0F + p_i225946_3_, 0.0F);
      this.rightArm = new ModelRenderer(this, 40, 16);
      this.rightArm.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_i225946_2_);
      this.rightArm.setPos(-5.0F, 2.0F + p_i225946_3_, 0.0F);
      this.leftArm = new ModelRenderer(this, 40, 16);
      this.leftArm.mirror = true;
      this.leftArm.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_i225946_2_);
      this.leftArm.setPos(5.0F, 2.0F + p_i225946_3_, 0.0F);
      this.rightLeg = new ModelRenderer(this, 0, 16);
      this.rightLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_i225946_2_);
      this.rightLeg.setPos(-1.9F, 12.0F + p_i225946_3_, 0.0F);
      this.leftLeg = new ModelRenderer(this, 0, 16);
      this.leftLeg.mirror = true;
      this.leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_i225946_2_);
      this.leftLeg.setPos(1.9F, 12.0F + p_i225946_3_, 0.0F);
   }

   protected Iterable<ModelRenderer> headParts() {
      return ImmutableList.of(this.head);
   }

   protected Iterable<ModelRenderer> bodyParts() {
      return ImmutableList.of(this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg, this.hat);
   }

   public void prepareMobModel(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTick) {
      this.swimAmount = pEntity.getSwimAmount(pPartialTick);
      super.prepareMobModel(pEntity, pLimbSwing, pLimbSwingAmount, pPartialTick);
   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      boolean flag = pEntity.getFallFlyingTicks() > 4;
      boolean flag1 = pEntity.isVisuallySwimming();
      this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
      if (flag) {
         this.head.xRot = (-(float)Math.PI / 4F);
      } else if (this.swimAmount > 0.0F) {
         if (flag1) {
            this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, (-(float)Math.PI / 4F));
         } else {
            this.head.xRot = this.rotlerpRad(this.swimAmount, this.head.xRot, pHeadPitch * ((float)Math.PI / 180F));
         }
      } else {
         this.head.xRot = pHeadPitch * ((float)Math.PI / 180F);
      }

      this.body.yRot = 0.0F;
      this.rightArm.z = 0.0F;
      this.rightArm.x = -5.0F;
      this.leftArm.z = 0.0F;
      this.leftArm.x = 5.0F;
      float f = 1.0F;
      if (flag) {
         f = (float)pEntity.getDeltaMovement().lengthSqr();
         f = f / 0.2F;
         f = f * f * f;
      }

      if (f < 1.0F) {
         f = 1.0F;
      }

      this.rightArm.xRot = MathHelper.cos(pLimbSwing * 0.6662F + (float)Math.PI) * 2.0F * pLimbSwingAmount * 0.5F / f;
      this.leftArm.xRot = MathHelper.cos(pLimbSwing * 0.6662F) * 2.0F * pLimbSwingAmount * 0.5F / f;
      this.rightArm.zRot = 0.0F;
      this.leftArm.zRot = 0.0F;
      this.rightLeg.xRot = MathHelper.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount / f;
      this.leftLeg.xRot = MathHelper.cos(pLimbSwing * 0.6662F + (float)Math.PI) * 1.4F * pLimbSwingAmount / f;
      this.rightLeg.yRot = 0.0F;
      this.leftLeg.yRot = 0.0F;
      this.rightLeg.zRot = 0.0F;
      this.leftLeg.zRot = 0.0F;
      if (this.riding) {
         this.rightArm.xRot += (-(float)Math.PI / 5F);
         this.leftArm.xRot += (-(float)Math.PI / 5F);
         this.rightLeg.xRot = -1.4137167F;
         this.rightLeg.yRot = ((float)Math.PI / 10F);
         this.rightLeg.zRot = 0.07853982F;
         this.leftLeg.xRot = -1.4137167F;
         this.leftLeg.yRot = (-(float)Math.PI / 10F);
         this.leftLeg.zRot = -0.07853982F;
      }

      this.rightArm.yRot = 0.0F;
      this.leftArm.yRot = 0.0F;
      boolean flag2 = pEntity.getMainArm() == HandSide.RIGHT;
      boolean flag3 = flag2 ? this.leftArmPose.isTwoHanded() : this.rightArmPose.isTwoHanded();
      if (flag2 != flag3) {
         this.poseLeftArm(pEntity);
         this.poseRightArm(pEntity);
      } else {
         this.poseRightArm(pEntity);
         this.poseLeftArm(pEntity);
      }

      this.setupAttackAnimation(pEntity, pAgeInTicks);
      if (this.crouching) {
         this.body.xRot = 0.5F;
         this.rightArm.xRot += 0.4F;
         this.leftArm.xRot += 0.4F;
         this.rightLeg.z = 4.0F;
         this.leftLeg.z = 4.0F;
         this.rightLeg.y = 12.2F;
         this.leftLeg.y = 12.2F;
         this.head.y = 4.2F;
         this.body.y = 3.2F;
         this.leftArm.y = 5.2F;
         this.rightArm.y = 5.2F;
      } else {
         this.body.xRot = 0.0F;
         this.rightLeg.z = 0.1F;
         this.leftLeg.z = 0.1F;
         this.rightLeg.y = 12.0F;
         this.leftLeg.y = 12.0F;
         this.head.y = 0.0F;
         this.body.y = 0.0F;
         this.leftArm.y = 2.0F;
         this.rightArm.y = 2.0F;
      }

      ModelHelper.bobArms(this.rightArm, this.leftArm, pAgeInTicks);
      if (this.swimAmount > 0.0F) {
         float f1 = pLimbSwing % 26.0F;
         HandSide handside = this.getAttackArm(pEntity);
         float f2 = handside == HandSide.RIGHT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
         float f3 = handside == HandSide.LEFT && this.attackTime > 0.0F ? 0.0F : this.swimAmount;
         if (f1 < 14.0F) {
            this.leftArm.xRot = this.rotlerpRad(f3, this.leftArm.xRot, 0.0F);
            this.rightArm.xRot = MathHelper.lerp(f2, this.rightArm.xRot, 0.0F);
            this.leftArm.yRot = this.rotlerpRad(f3, this.leftArm.yRot, (float)Math.PI);
            this.rightArm.yRot = MathHelper.lerp(f2, this.rightArm.yRot, (float)Math.PI);
            this.leftArm.zRot = this.rotlerpRad(f3, this.leftArm.zRot, (float)Math.PI + 1.8707964F * this.quadraticArmUpdate(f1) / this.quadraticArmUpdate(14.0F));
            this.rightArm.zRot = MathHelper.lerp(f2, this.rightArm.zRot, (float)Math.PI - 1.8707964F * this.quadraticArmUpdate(f1) / this.quadraticArmUpdate(14.0F));
         } else if (f1 >= 14.0F && f1 < 22.0F) {
            float f6 = (f1 - 14.0F) / 8.0F;
            this.leftArm.xRot = this.rotlerpRad(f3, this.leftArm.xRot, ((float)Math.PI / 2F) * f6);
            this.rightArm.xRot = MathHelper.lerp(f2, this.rightArm.xRot, ((float)Math.PI / 2F) * f6);
            this.leftArm.yRot = this.rotlerpRad(f3, this.leftArm.yRot, (float)Math.PI);
            this.rightArm.yRot = MathHelper.lerp(f2, this.rightArm.yRot, (float)Math.PI);
            this.leftArm.zRot = this.rotlerpRad(f3, this.leftArm.zRot, 5.012389F - 1.8707964F * f6);
            this.rightArm.zRot = MathHelper.lerp(f2, this.rightArm.zRot, 1.2707963F + 1.8707964F * f6);
         } else if (f1 >= 22.0F && f1 < 26.0F) {
            float f4 = (f1 - 22.0F) / 4.0F;
            this.leftArm.xRot = this.rotlerpRad(f3, this.leftArm.xRot, ((float)Math.PI / 2F) - ((float)Math.PI / 2F) * f4);
            this.rightArm.xRot = MathHelper.lerp(f2, this.rightArm.xRot, ((float)Math.PI / 2F) - ((float)Math.PI / 2F) * f4);
            this.leftArm.yRot = this.rotlerpRad(f3, this.leftArm.yRot, (float)Math.PI);
            this.rightArm.yRot = MathHelper.lerp(f2, this.rightArm.yRot, (float)Math.PI);
            this.leftArm.zRot = this.rotlerpRad(f3, this.leftArm.zRot, (float)Math.PI);
            this.rightArm.zRot = MathHelper.lerp(f2, this.rightArm.zRot, (float)Math.PI);
         }

         float f7 = 0.3F;
         float f5 = 0.33333334F;
         this.leftLeg.xRot = MathHelper.lerp(this.swimAmount, this.leftLeg.xRot, 0.3F * MathHelper.cos(pLimbSwing * 0.33333334F + (float)Math.PI));
         this.rightLeg.xRot = MathHelper.lerp(this.swimAmount, this.rightLeg.xRot, 0.3F * MathHelper.cos(pLimbSwing * 0.33333334F));
      }

      this.hat.copyFrom(this.head);
   }

   private void poseRightArm(T p_241654_1_) {
      switch(this.rightArmPose) {
      case EMPTY:
         this.rightArm.yRot = 0.0F;
         break;
      case BLOCK:
         this.rightArm.xRot = this.rightArm.xRot * 0.5F - 0.9424779F;
         this.rightArm.yRot = (-(float)Math.PI / 6F);
         break;
      case ITEM:
         this.rightArm.xRot = this.rightArm.xRot * 0.5F - ((float)Math.PI / 10F);
         this.rightArm.yRot = 0.0F;
         break;
      case THROW_SPEAR:
         this.rightArm.xRot = this.rightArm.xRot * 0.5F - (float)Math.PI;
         this.rightArm.yRot = 0.0F;
         break;
      case BOW_AND_ARROW:
         this.rightArm.yRot = -0.1F + this.head.yRot;
         this.leftArm.yRot = 0.1F + this.head.yRot + 0.4F;
         this.rightArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot;
         this.leftArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot;
         break;
      case CROSSBOW_CHARGE:
         ModelHelper.animateCrossbowCharge(this.rightArm, this.leftArm, p_241654_1_, true);
         break;
      case CROSSBOW_HOLD:
         ModelHelper.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
      }

   }

   private void poseLeftArm(T p_241655_1_) {
      switch(this.leftArmPose) {
      case EMPTY:
         this.leftArm.yRot = 0.0F;
         break;
      case BLOCK:
         this.leftArm.xRot = this.leftArm.xRot * 0.5F - 0.9424779F;
         this.leftArm.yRot = ((float)Math.PI / 6F);
         break;
      case ITEM:
         this.leftArm.xRot = this.leftArm.xRot * 0.5F - ((float)Math.PI / 10F);
         this.leftArm.yRot = 0.0F;
         break;
      case THROW_SPEAR:
         this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float)Math.PI;
         this.leftArm.yRot = 0.0F;
         break;
      case BOW_AND_ARROW:
         this.rightArm.yRot = -0.1F + this.head.yRot - 0.4F;
         this.leftArm.yRot = 0.1F + this.head.yRot;
         this.rightArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot;
         this.leftArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot;
         break;
      case CROSSBOW_CHARGE:
         ModelHelper.animateCrossbowCharge(this.rightArm, this.leftArm, p_241655_1_, false);
         break;
      case CROSSBOW_HOLD:
         ModelHelper.animateCrossbowHold(this.rightArm, this.leftArm, this.head, false);
      }

   }

   protected void setupAttackAnimation(T p_230486_1_, float p_230486_2_) {
      if (!(this.attackTime <= 0.0F)) {
         HandSide handside = this.getAttackArm(p_230486_1_);
         ModelRenderer modelrenderer = this.getArm(handside);
         float f = this.attackTime;
         this.body.yRot = MathHelper.sin(MathHelper.sqrt(f) * ((float)Math.PI * 2F)) * 0.2F;
         if (handside == HandSide.LEFT) {
            this.body.yRot *= -1.0F;
         }

         this.rightArm.z = MathHelper.sin(this.body.yRot) * 5.0F;
         this.rightArm.x = -MathHelper.cos(this.body.yRot) * 5.0F;
         this.leftArm.z = -MathHelper.sin(this.body.yRot) * 5.0F;
         this.leftArm.x = MathHelper.cos(this.body.yRot) * 5.0F;
         this.rightArm.yRot += this.body.yRot;
         this.leftArm.yRot += this.body.yRot;
         this.leftArm.xRot += this.body.yRot;
         f = 1.0F - this.attackTime;
         f = f * f;
         f = f * f;
         f = 1.0F - f;
         float f1 = MathHelper.sin(f * (float)Math.PI);
         float f2 = MathHelper.sin(this.attackTime * (float)Math.PI) * -(this.head.xRot - 0.7F) * 0.75F;
         modelrenderer.xRot = (float)((double)modelrenderer.xRot - ((double)f1 * 1.2D + (double)f2));
         modelrenderer.yRot += this.body.yRot * 2.0F;
         modelrenderer.zRot += MathHelper.sin(this.attackTime * (float)Math.PI) * -0.4F;
      }
   }

   protected float rotlerpRad(float pAngle, float pMaxAngle, float pMul) {
      float f = (pMul - pMaxAngle) % ((float)Math.PI * 2F);
      if (f < -(float)Math.PI) {
         f += ((float)Math.PI * 2F);
      }

      if (f >= (float)Math.PI) {
         f -= ((float)Math.PI * 2F);
      }

      return pMaxAngle + pAngle * f;
   }

   private float quadraticArmUpdate(float pLimbSwing) {
      return -65.0F * pLimbSwing + pLimbSwing * pLimbSwing;
   }

   public void copyPropertiesTo(BipedModel<T> pModel) {
      super.copyPropertiesTo(pModel);
      pModel.leftArmPose = this.leftArmPose;
      pModel.rightArmPose = this.rightArmPose;
      pModel.crouching = this.crouching;
      pModel.head.copyFrom(this.head);
      pModel.hat.copyFrom(this.hat);
      pModel.body.copyFrom(this.body);
      pModel.rightArm.copyFrom(this.rightArm);
      pModel.leftArm.copyFrom(this.leftArm);
      pModel.rightLeg.copyFrom(this.rightLeg);
      pModel.leftLeg.copyFrom(this.leftLeg);
   }

   public void setAllVisible(boolean pVisible) {
      this.head.visible = pVisible;
      this.hat.visible = pVisible;
      this.body.visible = pVisible;
      this.rightArm.visible = pVisible;
      this.leftArm.visible = pVisible;
      this.rightLeg.visible = pVisible;
      this.leftLeg.visible = pVisible;
   }

   public void translateToHand(HandSide pSide, MatrixStack pMatrixStack) {
      this.getArm(pSide).translateAndRotate(pMatrixStack);
   }

   protected ModelRenderer getArm(HandSide pSide) {
      return pSide == HandSide.LEFT ? this.leftArm : this.rightArm;
   }

   public ModelRenderer getHead() {
      return this.head;
   }

   protected HandSide getAttackArm(T pEntity) {
      HandSide handside = pEntity.getMainArm();
      return pEntity.swingingArm == Hand.MAIN_HAND ? handside : handside.getOpposite();
   }

   @OnlyIn(Dist.CLIENT)
   public static enum ArmPose {
      EMPTY(false),
      ITEM(false),
      BLOCK(false),
      BOW_AND_ARROW(true),
      THROW_SPEAR(false),
      CROSSBOW_CHARGE(true),
      CROSSBOW_HOLD(true);

      private final boolean twoHanded;

      private ArmPose(boolean p_i241257_3_) {
         this.twoHanded = p_i241257_3_;
      }

      public boolean isTwoHanded() {
         return this.twoHanded;
      }
   }
}