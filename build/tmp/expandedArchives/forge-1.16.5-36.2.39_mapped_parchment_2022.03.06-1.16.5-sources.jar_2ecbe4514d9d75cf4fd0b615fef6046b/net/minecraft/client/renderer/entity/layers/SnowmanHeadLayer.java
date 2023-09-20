package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.SnowManModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SnowmanHeadLayer extends LayerRenderer<SnowGolemEntity, SnowManModel<SnowGolemEntity>> {
   public SnowmanHeadLayer(IEntityRenderer<SnowGolemEntity, SnowManModel<SnowGolemEntity>> p_i50922_1_) {
      super(p_i50922_1_);
   }

   public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight, SnowGolemEntity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      if (!pLivingEntity.isInvisible() && pLivingEntity.hasPumpkin()) {
         pMatrixStack.pushPose();
         this.getParentModel().getHead().translateAndRotate(pMatrixStack);
         float f = 0.625F;
         pMatrixStack.translate(0.0D, -0.34375D, 0.0D);
         pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
         pMatrixStack.scale(0.625F, -0.625F, -0.625F);
         ItemStack itemstack = new ItemStack(Blocks.CARVED_PUMPKIN);
         Minecraft.getInstance().getItemRenderer().renderStatic(pLivingEntity, itemstack, ItemCameraTransforms.TransformType.HEAD, false, pMatrixStack, pBuffer, pLivingEntity.level, pPackedLight, LivingRenderer.getOverlayCoords(pLivingEntity, 0.0F));
         pMatrixStack.popPose();
      }
   }
}