package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.DolphinModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DolphinCarriedItemLayer extends LayerRenderer<DolphinEntity, DolphinModel<DolphinEntity>> {
   public DolphinCarriedItemLayer(IEntityRenderer<DolphinEntity, DolphinModel<DolphinEntity>> p_i50944_1_) {
      super(p_i50944_1_);
   }

   public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight, DolphinEntity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      boolean flag = pLivingEntity.getMainArm() == HandSide.RIGHT;
      pMatrixStack.pushPose();
      float f = 1.0F;
      float f1 = -1.0F;
      float f2 = MathHelper.abs(pLivingEntity.xRot) / 60.0F;
      if (pLivingEntity.xRot < 0.0F) {
         pMatrixStack.translate(0.0D, (double)(1.0F - f2 * 0.5F), (double)(-1.0F + f2 * 0.5F));
      } else {
         pMatrixStack.translate(0.0D, (double)(1.0F + f2 * 0.8F), (double)(-1.0F + f2 * 0.2F));
      }

      ItemStack itemstack = flag ? pLivingEntity.getMainHandItem() : pLivingEntity.getOffhandItem();
      Minecraft.getInstance().getItemInHandRenderer().renderItem(pLivingEntity, itemstack, ItemCameraTransforms.TransformType.GROUND, false, pMatrixStack, pBuffer, pPackedLight);
      pMatrixStack.popPose();
   }
}