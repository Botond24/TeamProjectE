package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.IronGolemModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IronGolenFlowerLayer extends LayerRenderer<IronGolemEntity, IronGolemModel<IronGolemEntity>> {
   public IronGolenFlowerLayer(IEntityRenderer<IronGolemEntity, IronGolemModel<IronGolemEntity>> p_i50935_1_) {
      super(p_i50935_1_);
   }

   public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight, IronGolemEntity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      if (pLivingEntity.getOfferFlowerTick() != 0) {
         pMatrixStack.pushPose();
         ModelRenderer modelrenderer = this.getParentModel().getFlowerHoldingArm();
         modelrenderer.translateAndRotate(pMatrixStack);
         pMatrixStack.translate(-1.1875D, 1.0625D, -0.9375D);
         pMatrixStack.translate(0.5D, 0.5D, 0.5D);
         float f = 0.5F;
         pMatrixStack.scale(0.5F, 0.5F, 0.5F);
         pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(-90.0F));
         pMatrixStack.translate(-0.5D, -0.5D, -0.5D);
         Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.POPPY.defaultBlockState(), pMatrixStack, pBuffer, pPackedLight, OverlayTexture.NO_OVERLAY);
         pMatrixStack.popPose();
      }
   }
}