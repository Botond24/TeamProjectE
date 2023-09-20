package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import java.util.Map;
import net.minecraft.client.renderer.entity.layers.PandaHeldItemLayer;
import net.minecraft.client.renderer.entity.model.PandaModel;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PandaRenderer extends MobRenderer<PandaEntity, PandaModel<PandaEntity>> {
   private static final Map<PandaEntity.Gene, ResourceLocation> TEXTURES = Util.make(Maps.newEnumMap(PandaEntity.Gene.class), (p_217776_0_) -> {
      p_217776_0_.put(PandaEntity.Gene.NORMAL, new ResourceLocation("textures/entity/panda/panda.png"));
      p_217776_0_.put(PandaEntity.Gene.LAZY, new ResourceLocation("textures/entity/panda/lazy_panda.png"));
      p_217776_0_.put(PandaEntity.Gene.WORRIED, new ResourceLocation("textures/entity/panda/worried_panda.png"));
      p_217776_0_.put(PandaEntity.Gene.PLAYFUL, new ResourceLocation("textures/entity/panda/playful_panda.png"));
      p_217776_0_.put(PandaEntity.Gene.BROWN, new ResourceLocation("textures/entity/panda/brown_panda.png"));
      p_217776_0_.put(PandaEntity.Gene.WEAK, new ResourceLocation("textures/entity/panda/weak_panda.png"));
      p_217776_0_.put(PandaEntity.Gene.AGGRESSIVE, new ResourceLocation("textures/entity/panda/aggressive_panda.png"));
   });

   public PandaRenderer(EntityRendererManager p_i50960_1_) {
      super(p_i50960_1_, new PandaModel<>(9, 0.0F), 0.9F);
      this.addLayer(new PandaHeldItemLayer(this));
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(PandaEntity pEntity) {
      return TEXTURES.getOrDefault(pEntity.getVariant(), TEXTURES.get(PandaEntity.Gene.NORMAL));
   }

   protected void setupRotations(PandaEntity pEntityLiving, MatrixStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
      super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
      if (pEntityLiving.rollCounter > 0) {
         int i = pEntityLiving.rollCounter;
         int j = i + 1;
         float f = 7.0F;
         float f1 = pEntityLiving.isBaby() ? 0.3F : 0.8F;
         if (i < 8) {
            float f3 = (float)(90 * i) / 7.0F;
            float f4 = (float)(90 * j) / 7.0F;
            float f2 = this.getAngle(f3, f4, j, pPartialTicks, 8.0F);
            pMatrixStack.translate(0.0D, (double)((f1 + 0.2F) * (f2 / 90.0F)), 0.0D);
            pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(-f2));
         } else if (i < 16) {
            float f13 = ((float)i - 8.0F) / 7.0F;
            float f16 = 90.0F + 90.0F * f13;
            float f5 = 90.0F + 90.0F * ((float)j - 8.0F) / 7.0F;
            float f10 = this.getAngle(f16, f5, j, pPartialTicks, 16.0F);
            pMatrixStack.translate(0.0D, (double)(f1 + 0.2F + (f1 - 0.2F) * (f10 - 90.0F) / 90.0F), 0.0D);
            pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(-f10));
         } else if ((float)i < 24.0F) {
            float f14 = ((float)i - 16.0F) / 7.0F;
            float f17 = 180.0F + 90.0F * f14;
            float f19 = 180.0F + 90.0F * ((float)j - 16.0F) / 7.0F;
            float f11 = this.getAngle(f17, f19, j, pPartialTicks, 24.0F);
            pMatrixStack.translate(0.0D, (double)(f1 + f1 * (270.0F - f11) / 90.0F), 0.0D);
            pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(-f11));
         } else if (i < 32) {
            float f15 = ((float)i - 24.0F) / 7.0F;
            float f18 = 270.0F + 90.0F * f15;
            float f20 = 270.0F + 90.0F * ((float)j - 24.0F) / 7.0F;
            float f12 = this.getAngle(f18, f20, j, pPartialTicks, 32.0F);
            pMatrixStack.translate(0.0D, (double)(f1 * ((360.0F - f12) / 90.0F)), 0.0D);
            pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(-f12));
         }
      }

      float f6 = pEntityLiving.getSitAmount(pPartialTicks);
      if (f6 > 0.0F) {
         pMatrixStack.translate(0.0D, (double)(0.8F * f6), 0.0D);
         pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(MathHelper.lerp(f6, pEntityLiving.xRot, pEntityLiving.xRot + 90.0F)));
         pMatrixStack.translate(0.0D, (double)(-1.0F * f6), 0.0D);
         if (pEntityLiving.isScared()) {
            float f7 = (float)(Math.cos((double)pEntityLiving.tickCount * 1.25D) * Math.PI * (double)0.05F);
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f7));
            if (pEntityLiving.isBaby()) {
               pMatrixStack.translate(0.0D, (double)0.8F, (double)0.55F);
            }
         }
      }

      float f8 = pEntityLiving.getLieOnBackAmount(pPartialTicks);
      if (f8 > 0.0F) {
         float f9 = pEntityLiving.isBaby() ? 0.5F : 1.3F;
         pMatrixStack.translate(0.0D, (double)(f9 * f8), 0.0D);
         pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(MathHelper.lerp(f8, pEntityLiving.xRot, pEntityLiving.xRot + 180.0F)));
      }

   }

   private float getAngle(float p_217775_1_, float p_217775_2_, int p_217775_3_, float p_217775_4_, float p_217775_5_) {
      return (float)p_217775_3_ < p_217775_5_ ? MathHelper.lerp(p_217775_4_, p_217775_1_, p_217775_2_) : p_217775_1_;
   }
}