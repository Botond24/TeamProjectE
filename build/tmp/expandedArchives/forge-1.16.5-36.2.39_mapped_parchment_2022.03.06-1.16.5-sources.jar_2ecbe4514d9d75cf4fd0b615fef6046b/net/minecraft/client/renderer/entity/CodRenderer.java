package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.model.CodModel;
import net.minecraft.entity.passive.fish.CodEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CodRenderer extends MobRenderer<CodEntity, CodModel<CodEntity>> {
   private static final ResourceLocation COD_LOCATION = new ResourceLocation("textures/entity/fish/cod.png");

   public CodRenderer(EntityRendererManager p_i48864_1_) {
      super(p_i48864_1_, new CodModel<>(), 0.3F);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(CodEntity pEntity) {
      return COD_LOCATION;
   }

   protected void setupRotations(CodEntity pEntityLiving, MatrixStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks) {
      super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
      float f = 4.3F * MathHelper.sin(0.6F * pAgeInTicks);
      pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f));
      if (!pEntityLiving.isInWater()) {
         pMatrixStack.translate((double)0.1F, (double)0.1F, (double)-0.1F);
         pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
      }

   }
}