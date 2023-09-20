package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.FoxModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FoxHeldItemLayer extends LayerRenderer<FoxEntity, FoxModel<FoxEntity>> {
   public FoxHeldItemLayer(IEntityRenderer<FoxEntity, FoxModel<FoxEntity>> p_i50938_1_) {
      super(p_i50938_1_);
   }

   public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight, FoxEntity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      boolean flag = pLivingEntity.isSleeping();
      boolean flag1 = pLivingEntity.isBaby();
      pMatrixStack.pushPose();
      if (flag1) {
         float f = 0.75F;
         pMatrixStack.scale(0.75F, 0.75F, 0.75F);
         pMatrixStack.translate(0.0D, 0.5D, (double)0.209375F);
      }

      pMatrixStack.translate((double)((this.getParentModel()).head.x / 16.0F), (double)((this.getParentModel()).head.y / 16.0F), (double)((this.getParentModel()).head.z / 16.0F));
      float f1 = pLivingEntity.getHeadRollAngle(pPartialTicks);
      pMatrixStack.mulPose(Vector3f.ZP.rotation(f1));
      pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(pNetHeadYaw));
      pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(pHeadPitch));
      if (pLivingEntity.isBaby()) {
         if (flag) {
            pMatrixStack.translate((double)0.4F, (double)0.26F, (double)0.15F);
         } else {
            pMatrixStack.translate((double)0.06F, (double)0.26F, -0.5D);
         }
      } else if (flag) {
         pMatrixStack.translate((double)0.46F, (double)0.26F, (double)0.22F);
      } else {
         pMatrixStack.translate((double)0.06F, (double)0.27F, -0.5D);
      }

      pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
      if (flag) {
         pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
      }

      ItemStack itemstack = pLivingEntity.getItemBySlot(EquipmentSlotType.MAINHAND);
      Minecraft.getInstance().getItemInHandRenderer().renderItem(pLivingEntity, itemstack, ItemCameraTransforms.TransformType.GROUND, false, pMatrixStack, pBuffer, pPackedLight);
      pMatrixStack.popPose();
   }
}