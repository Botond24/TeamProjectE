package net.minecraft.client.renderer.entity.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.AbstractVillagerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VillagerModel<T extends Entity> extends SegmentedModel<T> implements IHasHead, IHeadToggle {
   protected ModelRenderer head;
   protected ModelRenderer hat;
   protected final ModelRenderer hatRim;
   protected final ModelRenderer body;
   protected final ModelRenderer jacket;
   protected final ModelRenderer arms;
   protected final ModelRenderer leg0;
   protected final ModelRenderer leg1;
   protected final ModelRenderer nose;

   public VillagerModel(float p_i1163_1_) {
      this(p_i1163_1_, 64, 64);
   }

   public VillagerModel(float p_i51059_1_, int p_i51059_2_, int p_i51059_3_) {
      float f = 0.5F;
      this.head = (new ModelRenderer(this)).setTexSize(p_i51059_2_, p_i51059_3_);
      this.head.setPos(0.0F, 0.0F, 0.0F);
      this.head.texOffs(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, p_i51059_1_);
      this.hat = (new ModelRenderer(this)).setTexSize(p_i51059_2_, p_i51059_3_);
      this.hat.setPos(0.0F, 0.0F, 0.0F);
      this.hat.texOffs(32, 0).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 10.0F, 8.0F, p_i51059_1_ + 0.5F);
      this.head.addChild(this.hat);
      this.hatRim = (new ModelRenderer(this)).setTexSize(p_i51059_2_, p_i51059_3_);
      this.hatRim.setPos(0.0F, 0.0F, 0.0F);
      this.hatRim.texOffs(30, 47).addBox(-8.0F, -8.0F, -6.0F, 16.0F, 16.0F, 1.0F, p_i51059_1_);
      this.hatRim.xRot = (-(float)Math.PI / 2F);
      this.hat.addChild(this.hatRim);
      this.nose = (new ModelRenderer(this)).setTexSize(p_i51059_2_, p_i51059_3_);
      this.nose.setPos(0.0F, -2.0F, 0.0F);
      this.nose.texOffs(24, 0).addBox(-1.0F, -1.0F, -6.0F, 2.0F, 4.0F, 2.0F, p_i51059_1_);
      this.head.addChild(this.nose);
      this.body = (new ModelRenderer(this)).setTexSize(p_i51059_2_, p_i51059_3_);
      this.body.setPos(0.0F, 0.0F, 0.0F);
      this.body.texOffs(16, 20).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 12.0F, 6.0F, p_i51059_1_);
      this.jacket = (new ModelRenderer(this)).setTexSize(p_i51059_2_, p_i51059_3_);
      this.jacket.setPos(0.0F, 0.0F, 0.0F);
      this.jacket.texOffs(0, 38).addBox(-4.0F, 0.0F, -3.0F, 8.0F, 18.0F, 6.0F, p_i51059_1_ + 0.5F);
      this.body.addChild(this.jacket);
      this.arms = (new ModelRenderer(this)).setTexSize(p_i51059_2_, p_i51059_3_);
      this.arms.setPos(0.0F, 2.0F, 0.0F);
      this.arms.texOffs(44, 22).addBox(-8.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, p_i51059_1_);
      this.arms.texOffs(44, 22).addBox(4.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, p_i51059_1_, true);
      this.arms.texOffs(40, 38).addBox(-4.0F, 2.0F, -2.0F, 8.0F, 4.0F, 4.0F, p_i51059_1_);
      this.leg0 = (new ModelRenderer(this, 0, 22)).setTexSize(p_i51059_2_, p_i51059_3_);
      this.leg0.setPos(-2.0F, 12.0F, 0.0F);
      this.leg0.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_i51059_1_);
      this.leg1 = (new ModelRenderer(this, 0, 22)).setTexSize(p_i51059_2_, p_i51059_3_);
      this.leg1.mirror = true;
      this.leg1.setPos(2.0F, 12.0F, 0.0F);
      this.leg1.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, p_i51059_1_);
   }

   public Iterable<ModelRenderer> parts() {
      return ImmutableList.of(this.head, this.body, this.leg0, this.leg1, this.arms);
   }

   /**
    * Sets this entity's model rotation angles
    */
   public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      boolean flag = false;
      if (pEntity instanceof AbstractVillagerEntity) {
         flag = ((AbstractVillagerEntity)pEntity).getUnhappyCounter() > 0;
      }

      this.head.yRot = pNetHeadYaw * ((float)Math.PI / 180F);
      this.head.xRot = pHeadPitch * ((float)Math.PI / 180F);
      if (flag) {
         this.head.zRot = 0.3F * MathHelper.sin(0.45F * pAgeInTicks);
         this.head.xRot = 0.4F;
      } else {
         this.head.zRot = 0.0F;
      }

      this.arms.y = 3.0F;
      this.arms.z = -1.0F;
      this.arms.xRot = -0.75F;
      this.leg0.xRot = MathHelper.cos(pLimbSwing * 0.6662F) * 1.4F * pLimbSwingAmount * 0.5F;
      this.leg1.xRot = MathHelper.cos(pLimbSwing * 0.6662F + (float)Math.PI) * 1.4F * pLimbSwingAmount * 0.5F;
      this.leg0.yRot = 0.0F;
      this.leg1.yRot = 0.0F;
   }

   public ModelRenderer getHead() {
      return this.head;
   }

   public void hatVisible(boolean p_217146_1_) {
      this.head.visible = p_217146_1_;
      this.hat.visible = p_217146_1_;
      this.hatRim.visible = p_217146_1_;
   }
}