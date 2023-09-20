package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CrossedArmsItemLayer<T extends LivingEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> {
   public CrossedArmsItemLayer(IEntityRenderer<T, M> p_i226037_1_) {
      super(p_i226037_1_);
   }

   public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
      pMatrixStack.pushPose();
      pMatrixStack.translate(0.0D, (double)0.4F, (double)-0.4F);
      pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
      ItemStack itemstack = pLivingEntity.getItemBySlot(EquipmentSlotType.MAINHAND);
      Minecraft.getInstance().getItemInHandRenderer().renderItem(pLivingEntity, itemstack, ItemCameraTransforms.TransformType.GROUND, false, pMatrixStack, pBuffer, pPackedLight);
      pMatrixStack.popPose();
   }
}