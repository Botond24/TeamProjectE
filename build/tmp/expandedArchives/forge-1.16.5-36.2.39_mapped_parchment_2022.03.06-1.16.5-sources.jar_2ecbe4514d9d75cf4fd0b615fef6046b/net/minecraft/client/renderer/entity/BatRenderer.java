package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.model.BatModel;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BatRenderer extends MobRenderer<BatEntity, BatModel> {
   private static final ResourceLocation BAT_LOCATION = new ResourceLocation("textures/entity/bat.png");

   public BatRenderer(EntityRendererManager p_i46192_1_) {
      super(p_i46192_1_, new BatModel(), 0.25F);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(BatEntity pEntity) {
      return BAT_LOCATION;
   }

   protected void scale(BatEntity pLivingEntity, MatrixStack pMatrixStack, float pPartialTickTime) {
      pMatrixStack.scale(0.35F, 0.35F, 0.35F);
   }

   protected void setupRotations(BatEntity pEntityLiving, MatrixStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
      if (pEntityLiving.isResting()) {
         pMatrixStack.translate(0.0D, (double)-0.1F, 0.0D);
      } else {
         pMatrixStack.translate(0.0D, (double)(MathHelper.cos(pAgeInTicks * 0.3F) * 0.1F), 0.0D);
      }

      super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
   }
}