package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.layers.CatCollarLayer;
import net.minecraft.client.renderer.entity.model.CatModel;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CatRenderer extends MobRenderer<CatEntity, CatModel<CatEntity>> {
   public CatRenderer(EntityRendererManager p_i50973_1_) {
      super(p_i50973_1_, new CatModel<>(0.0F), 0.4F);
      this.addLayer(new CatCollarLayer(this));
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(CatEntity pEntity) {
      return pEntity.getResourceLocation();
   }

   protected void scale(CatEntity pLivingEntity, MatrixStack pMatrixStack, float pPartialTickTime) {
      super.scale(pLivingEntity, pMatrixStack, pPartialTickTime);
      pMatrixStack.scale(0.8F, 0.8F, 0.8F);
   }

   protected void setupRotations(CatEntity pEntityLiving, MatrixStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
      super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
      float f = pEntityLiving.getLieDownAmount(pPartialTicks);
      if (f > 0.0F) {
         pMatrixStack.translate((double)(0.4F * f), (double)(0.15F * f), (double)(0.1F * f));
         pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(MathHelper.rotLerp(f, 0.0F, 90.0F)));
         BlockPos blockpos = pEntityLiving.blockPosition();

         for(PlayerEntity playerentity : pEntityLiving.level.getEntitiesOfClass(PlayerEntity.class, (new AxisAlignedBB(blockpos)).inflate(2.0D, 2.0D, 2.0D))) {
            if (playerentity.isSleeping()) {
               pMatrixStack.translate((double)(0.15F * f), 0.0D, 0.0D);
               break;
            }
         }
      }

   }
}