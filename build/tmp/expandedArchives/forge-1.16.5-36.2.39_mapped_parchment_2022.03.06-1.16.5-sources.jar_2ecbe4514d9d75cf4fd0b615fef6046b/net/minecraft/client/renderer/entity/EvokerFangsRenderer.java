package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.EvokerFangsModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.projectile.EvokerFangsEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EvokerFangsRenderer extends EntityRenderer<EvokerFangsEntity> {
   private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/illager/evoker_fangs.png");
   private final EvokerFangsModel<EvokerFangsEntity> model = new EvokerFangsModel<>();

   public EvokerFangsRenderer(EntityRendererManager p_i47208_1_) {
      super(p_i47208_1_);
   }

   public void render(EvokerFangsEntity pEntity, float pEntityYaw, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight) {
      float f = pEntity.getAnimationProgress(pPartialTicks);
      if (f != 0.0F) {
         float f1 = 2.0F;
         if (f > 0.9F) {
            f1 = (float)((double)f1 * ((1.0D - (double)f) / (double)0.1F));
         }

         pMatrixStack.pushPose();
         pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(90.0F - pEntity.yRot));
         pMatrixStack.scale(-f1, -f1, f1);
         float f2 = 0.03125F;
         pMatrixStack.translate(0.0D, (double)-0.626F, 0.0D);
         pMatrixStack.scale(0.5F, 0.5F, 0.5F);
         this.model.setupAnim(pEntity, f, 0.0F, 0.0F, pEntity.yRot, pEntity.xRot);
         IVertexBuilder ivertexbuilder = pBuffer.getBuffer(this.model.renderType(TEXTURE_LOCATION));
         this.model.renderToBuffer(pMatrixStack, ivertexbuilder, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
         pMatrixStack.popPose();
         super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
      }
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(EvokerFangsEntity pEntity) {
      return TEXTURE_LOCATION;
   }
}