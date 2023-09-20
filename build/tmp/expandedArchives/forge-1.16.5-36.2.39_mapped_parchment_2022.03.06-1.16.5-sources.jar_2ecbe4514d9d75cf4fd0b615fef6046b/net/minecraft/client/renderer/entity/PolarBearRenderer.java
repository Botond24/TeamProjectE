package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.model.PolarBearModel;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PolarBearRenderer extends MobRenderer<PolarBearEntity, PolarBearModel<PolarBearEntity>> {
   private static final ResourceLocation BEAR_LOCATION = new ResourceLocation("textures/entity/bear/polarbear.png");

   public PolarBearRenderer(EntityRendererManager p_i47197_1_) {
      super(p_i47197_1_, new PolarBearModel<>(), 0.9F);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(PolarBearEntity pEntity) {
      return BEAR_LOCATION;
   }

   protected void scale(PolarBearEntity pLivingEntity, MatrixStack pMatrixStack, float pPartialTickTime) {
      pMatrixStack.scale(1.2F, 1.2F, 1.2F);
      super.scale(pLivingEntity, pMatrixStack, pPartialTickTime);
   }
}