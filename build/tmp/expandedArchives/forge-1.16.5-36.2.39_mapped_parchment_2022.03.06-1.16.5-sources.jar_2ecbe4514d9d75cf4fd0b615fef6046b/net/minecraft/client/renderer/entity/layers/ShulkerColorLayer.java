package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.ShulkerRenderer;
import net.minecraft.client.renderer.entity.model.ShulkerModel;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShulkerColorLayer extends LayerRenderer<ShulkerEntity, ShulkerModel<ShulkerEntity>> {
   public ShulkerColorLayer(IEntityRenderer<ShulkerEntity, ShulkerModel<ShulkerEntity>> p_i50924_1_) {
      super(p_i50924_1_);
   }

   public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight, ShulkerEntity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      pMatrixStack.pushPose();
      pMatrixStack.translate(0.0D, 1.0D, 0.0D);
      pMatrixStack.scale(-1.0F, -1.0F, 1.0F);
      Quaternion quaternion = pLivingEntity.getAttachFace().getOpposite().getRotation();
      quaternion.conj();
      pMatrixStack.mulPose(quaternion);
      pMatrixStack.scale(-1.0F, -1.0F, 1.0F);
      pMatrixStack.translate(0.0D, -1.0D, 0.0D);
      DyeColor dyecolor = pLivingEntity.getColor();
      ResourceLocation resourcelocation = dyecolor == null ? ShulkerRenderer.DEFAULT_TEXTURE_LOCATION : ShulkerRenderer.TEXTURE_LOCATION[dyecolor.getId()];
      IVertexBuilder ivertexbuilder = pBuffer.getBuffer(RenderType.entitySolid(resourcelocation));
      this.getParentModel().getHead().render(pMatrixStack, ivertexbuilder, pPackedLight, LivingRenderer.getOverlayCoords(pLivingEntity, 0.0F));
      pMatrixStack.popPose();
   }
}