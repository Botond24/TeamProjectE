package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.item.minecart.TNTMinecartEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TNTMinecartRenderer extends MinecartRenderer<TNTMinecartEntity> {
   public TNTMinecartRenderer(EntityRendererManager p_i46135_1_) {
      super(p_i46135_1_);
   }

   protected void renderMinecartContents(TNTMinecartEntity pEntity, float pPartialTicks, BlockState pState, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight) {
      int i = pEntity.getFuse();
      if (i > -1 && (float)i - pPartialTicks + 1.0F < 10.0F) {
         float f = 1.0F - ((float)i - pPartialTicks + 1.0F) / 10.0F;
         f = MathHelper.clamp(f, 0.0F, 1.0F);
         f = f * f;
         f = f * f;
         float f1 = 1.0F + f * 0.3F;
         pMatrixStack.scale(f1, f1, f1);
      }

      renderWhiteSolidBlock(pState, pMatrixStack, pBuffer, pPackedLight, i > -1 && i / 5 % 2 == 0);
   }

   public static void renderWhiteSolidBlock(BlockState pBlockState, MatrixStack pMatrixStack, IRenderTypeBuffer pRenderTypeBuffer, int pCombinedLight, boolean pDoFullBright) {
      int i;
      if (pDoFullBright) {
         i = OverlayTexture.pack(OverlayTexture.u(1.0F), 10);
      } else {
         i = OverlayTexture.NO_OVERLAY;
      }

      Minecraft.getInstance().getBlockRenderer().renderSingleBlock(pBlockState, pMatrixStack, pRenderTypeBuffer, pCombinedLight, i);
   }
}