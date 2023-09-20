package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Deadmau5HeadLayer extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {
   public Deadmau5HeadLayer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> p_i50945_1_) {
      super(p_i50945_1_);
   }

   public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight, AbstractClientPlayerEntity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      if ("deadmau5".equals(pLivingEntity.getName().getString()) && pLivingEntity.isSkinLoaded() && !pLivingEntity.isInvisible()) {
         IVertexBuilder ivertexbuilder = pBuffer.getBuffer(RenderType.entitySolid(pLivingEntity.getSkinTextureLocation()));
         int i = LivingRenderer.getOverlayCoords(pLivingEntity, 0.0F);

         for(int j = 0; j < 2; ++j) {
            float f = MathHelper.lerp(pPartialTicks, pLivingEntity.yRotO, pLivingEntity.yRot) - MathHelper.lerp(pPartialTicks, pLivingEntity.yBodyRotO, pLivingEntity.yBodyRot);
            float f1 = MathHelper.lerp(pPartialTicks, pLivingEntity.xRotO, pLivingEntity.xRot);
            pMatrixStack.pushPose();
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f));
            pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(f1));
            pMatrixStack.translate((double)(0.375F * (float)(j * 2 - 1)), 0.0D, 0.0D);
            pMatrixStack.translate(0.0D, -0.375D, 0.0D);
            pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(-f1));
            pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(-f));
            float f2 = 1.3333334F;
            pMatrixStack.scale(1.3333334F, 1.3333334F, 1.3333334F);
            this.getParentModel().renderEars(pMatrixStack, ivertexbuilder, pPackedLight, i);
            pMatrixStack.popPose();
         }

      }
   }
}