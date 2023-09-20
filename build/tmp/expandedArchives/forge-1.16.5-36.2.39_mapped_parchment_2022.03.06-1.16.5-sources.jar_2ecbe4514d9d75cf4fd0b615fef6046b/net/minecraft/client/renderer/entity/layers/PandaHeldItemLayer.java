package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.PandaModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PandaHeldItemLayer extends LayerRenderer<PandaEntity, PandaModel<PandaEntity>> {
   public PandaHeldItemLayer(IEntityRenderer<PandaEntity, PandaModel<PandaEntity>> p_i50930_1_) {
      super(p_i50930_1_);
   }

   public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight, PandaEntity pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      ItemStack itemstack = pLivingEntity.getItemBySlot(EquipmentSlotType.MAINHAND);
      if (pLivingEntity.isSitting() && !pLivingEntity.isScared()) {
         float f = -0.6F;
         float f1 = 1.4F;
         if (pLivingEntity.isEating()) {
            f -= 0.2F * MathHelper.sin(pAgeInTicks * 0.6F) + 0.2F;
            f1 -= 0.09F * MathHelper.sin(pAgeInTicks * 0.6F);
         }

         pMatrixStack.pushPose();
         pMatrixStack.translate((double)0.1F, (double)f1, (double)f);
         Minecraft.getInstance().getItemInHandRenderer().renderItem(pLivingEntity, itemstack, ItemCameraTransforms.TransformType.GROUND, false, pMatrixStack, pBuffer, pPackedLight);
         pMatrixStack.popPose();
      }
   }
}