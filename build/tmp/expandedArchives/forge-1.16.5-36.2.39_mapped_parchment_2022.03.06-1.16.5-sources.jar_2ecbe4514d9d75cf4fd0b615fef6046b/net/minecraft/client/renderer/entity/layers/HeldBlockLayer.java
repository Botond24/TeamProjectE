package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.EndermanModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HeldBlockLayer extends LayerRenderer<EndermanEntity, EndermanModel<EndermanEntity>> {
   public HeldBlockLayer(IEntityRenderer<EndermanEntity, EndermanModel<EndermanEntity>> p_i50949_1_) {
      super(p_i50949_1_);
   }

   public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight, EndermanEntity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      BlockState blockstate = pLivingEntity.getCarriedBlock();
      if (blockstate != null) {
         pMatrixStack.pushPose();
         pMatrixStack.translate(0.0D, 0.6875D, -0.75D);
         pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(20.0F));
         pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(45.0F));
         pMatrixStack.translate(0.25D, 0.1875D, 0.25D);
         float f = 0.5F;
         pMatrixStack.scale(-0.5F, -0.5F, 0.5F);
         pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
         Minecraft.getInstance().getBlockRenderer().renderSingleBlock(blockstate, pMatrixStack, pBuffer, pPackedLight, OverlayTexture.NO_OVERLAY);
         pMatrixStack.popPose();
      }
   }
}