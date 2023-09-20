package net.minecraft.client.renderer.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.List;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BeaconTileEntityRenderer extends TileEntityRenderer<BeaconTileEntity> {
   public static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/beacon_beam.png");

   public BeaconTileEntityRenderer(TileEntityRendererDispatcher p_i226003_1_) {
      super(p_i226003_1_);
   }

   public void render(BeaconTileEntity pBlockEntity, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, int pCombinedOverlay) {
      long i = pBlockEntity.getLevel().getGameTime();
      List<BeaconTileEntity.BeamSegment> list = pBlockEntity.getBeamSections();
      int j = 0;

      for(int k = 0; k < list.size(); ++k) {
         BeaconTileEntity.BeamSegment beacontileentity$beamsegment = list.get(k);
         renderBeaconBeam(pMatrixStack, pBuffer, pPartialTicks, i, j, k == list.size() - 1 ? 1024 : beacontileentity$beamsegment.getHeight(), beacontileentity$beamsegment.getColor());
         j += beacontileentity$beamsegment.getHeight();
      }

   }

   private static void renderBeaconBeam(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, float pPartialTicks, long pTotalLevelTime, int pYOffset, int pHeight, float[] pColors) {
      renderBeaconBeam(pMatrixStack, pBuffer, BEAM_LOCATION, pPartialTicks, 1.0F, pTotalLevelTime, pYOffset, pHeight, pColors, 0.2F, 0.25F);
   }

   public static void renderBeaconBeam(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, ResourceLocation pTextureLocation, float pPartialTicks, float pTextureScale, long pTotalLevelTime, int pYOffset, int pHeight, float[] pColors, float pBeamRadius, float pGlowRadius) {
      int i = pYOffset + pHeight;
      pMatrixStack.pushPose();
      pMatrixStack.translate(0.5D, 0.0D, 0.5D);
      float f = (float)Math.floorMod(pTotalLevelTime, 40L) + pPartialTicks;
      float f1 = pHeight < 0 ? f : -f;
      float f2 = MathHelper.frac(f1 * 0.2F - (float)MathHelper.floor(f1 * 0.1F));
      float f3 = pColors[0];
      float f4 = pColors[1];
      float f5 = pColors[2];
      pMatrixStack.pushPose();
      pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f * 2.25F - 45.0F));
      float f6 = 0.0F;
      float f8 = 0.0F;
      float f9 = -pBeamRadius;
      float f10 = 0.0F;
      float f11 = 0.0F;
      float f12 = -pBeamRadius;
      float f13 = 0.0F;
      float f14 = 1.0F;
      float f15 = -1.0F + f2;
      float f16 = (float)pHeight * pTextureScale * (0.5F / pBeamRadius) + f15;
      renderPart(pMatrixStack, pBuffer.getBuffer(RenderType.beaconBeam(pTextureLocation, false)), f3, f4, f5, 1.0F, pYOffset, i, 0.0F, pBeamRadius, pBeamRadius, 0.0F, f9, 0.0F, 0.0F, f12, 0.0F, 1.0F, f16, f15);
      pMatrixStack.popPose();
      f6 = -pGlowRadius;
      float f7 = -pGlowRadius;
      f8 = -pGlowRadius;
      f9 = -pGlowRadius;
      f13 = 0.0F;
      f14 = 1.0F;
      f15 = -1.0F + f2;
      f16 = (float)pHeight * pTextureScale + f15;
      renderPart(pMatrixStack, pBuffer.getBuffer(RenderType.beaconBeam(pTextureLocation, true)), f3, f4, f5, 0.125F, pYOffset, i, f6, f7, pGlowRadius, f8, f9, pGlowRadius, pGlowRadius, pGlowRadius, 0.0F, 1.0F, f16, f15);
      pMatrixStack.popPose();
   }

   private static void renderPart(MatrixStack pMatrixStack, IVertexBuilder pBuffer, float pRed, float pGreen, float pBlue, float pAlpha, int pYMin, int pYMax, float p_228840_8_, float p_228840_9_, float p_228840_10_, float p_228840_11_, float p_228840_12_, float p_228840_13_, float p_228840_14_, float p_228840_15_, float pU1, float pU2, float pV1, float pV2) {
      MatrixStack.Entry matrixstack$entry = pMatrixStack.last();
      Matrix4f matrix4f = matrixstack$entry.pose();
      Matrix3f matrix3f = matrixstack$entry.normal();
      renderQuad(matrix4f, matrix3f, pBuffer, pRed, pGreen, pBlue, pAlpha, pYMin, pYMax, p_228840_8_, p_228840_9_, p_228840_10_, p_228840_11_, pU1, pU2, pV1, pV2);
      renderQuad(matrix4f, matrix3f, pBuffer, pRed, pGreen, pBlue, pAlpha, pYMin, pYMax, p_228840_14_, p_228840_15_, p_228840_12_, p_228840_13_, pU1, pU2, pV1, pV2);
      renderQuad(matrix4f, matrix3f, pBuffer, pRed, pGreen, pBlue, pAlpha, pYMin, pYMax, p_228840_10_, p_228840_11_, p_228840_14_, p_228840_15_, pU1, pU2, pV1, pV2);
      renderQuad(matrix4f, matrix3f, pBuffer, pRed, pGreen, pBlue, pAlpha, pYMin, pYMax, p_228840_12_, p_228840_13_, p_228840_8_, p_228840_9_, pU1, pU2, pV1, pV2);
   }

   private static void renderQuad(Matrix4f pMatrixPos, Matrix3f pMatrixNormal, IVertexBuilder pBuffer, float pRed, float pGreen, float pBlue, float pAlpha, int pYMin, int pYMax, float pX1, float pZ1, float pX2, float pZ2, float pU1, float pU2, float pV1, float pV2) {
      addVertex(pMatrixPos, pMatrixNormal, pBuffer, pRed, pGreen, pBlue, pAlpha, pYMax, pX1, pZ1, pU2, pV1);
      addVertex(pMatrixPos, pMatrixNormal, pBuffer, pRed, pGreen, pBlue, pAlpha, pYMin, pX1, pZ1, pU2, pV2);
      addVertex(pMatrixPos, pMatrixNormal, pBuffer, pRed, pGreen, pBlue, pAlpha, pYMin, pX2, pZ2, pU1, pV2);
      addVertex(pMatrixPos, pMatrixNormal, pBuffer, pRed, pGreen, pBlue, pAlpha, pYMax, pX2, pZ2, pU1, pV1);
   }

   private static void addVertex(Matrix4f pMatrixPos, Matrix3f pMatrixNormal, IVertexBuilder pBuffer, float pRed, float pGreen, float pBlue, float pAlpha, int pY, float pX, float pZ, float pTexU, float pTexV) {
      pBuffer.vertex(pMatrixPos, pX, (float)pY, pZ).color(pRed, pGreen, pBlue, pAlpha).uv(pTexU, pTexV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(pMatrixNormal, 0.0F, 1.0F, 0.0F).endVertex();
   }

   public boolean shouldRenderOffScreen(BeaconTileEntity pTe) {
      return true;
   }
}