package net.minecraft.client.renderer.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.List;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StandingSignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.WoodType;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SignTileEntityRenderer extends TileEntityRenderer<SignTileEntity> {
   private final SignTileEntityRenderer.SignModel signModel = new SignTileEntityRenderer.SignModel();

   public SignTileEntityRenderer(TileEntityRendererDispatcher p_i226014_1_) {
      super(p_i226014_1_);
   }

   public void render(SignTileEntity pBlockEntity, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, int pCombinedOverlay) {
      BlockState blockstate = pBlockEntity.getBlockState();
      pMatrixStack.pushPose();
      float f = 0.6666667F;
      if (blockstate.getBlock() instanceof StandingSignBlock) {
         pMatrixStack.translate(0.5D, 0.5D, 0.5D);
         float f1 = -((float)(blockstate.getValue(StandingSignBlock.ROTATION) * 360) / 16.0F);
         pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f1));
         this.signModel.stick.visible = true;
      } else {
         pMatrixStack.translate(0.5D, 0.5D, 0.5D);
         float f4 = -blockstate.getValue(WallSignBlock.FACING).toYRot();
         pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f4));
         pMatrixStack.translate(0.0D, -0.3125D, -0.4375D);
         this.signModel.stick.visible = false;
      }

      pMatrixStack.pushPose();
      pMatrixStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
      RenderMaterial rendermaterial = getMaterial(blockstate.getBlock());
      IVertexBuilder ivertexbuilder = rendermaterial.buffer(pBuffer, this.signModel::renderType);
      this.signModel.sign.render(pMatrixStack, ivertexbuilder, pCombinedLight, pCombinedOverlay);
      this.signModel.stick.render(pMatrixStack, ivertexbuilder, pCombinedLight, pCombinedOverlay);
      pMatrixStack.popPose();
      FontRenderer fontrenderer = this.renderer.getFont();
      float f2 = 0.010416667F;
      pMatrixStack.translate(0.0D, (double)0.33333334F, (double)0.046666667F);
      pMatrixStack.scale(0.010416667F, -0.010416667F, 0.010416667F);
      int i = pBlockEntity.getColor().getTextColor();
      double d0 = 0.4D;
      int j = (int)((double)NativeImage.getR(i) * 0.4D);
      int k = (int)((double)NativeImage.getG(i) * 0.4D);
      int l = (int)((double)NativeImage.getB(i) * 0.4D);
      int i1 = NativeImage.combine(0, l, k, j);
      int j1 = 20;

      for(int k1 = 0; k1 < 4; ++k1) {
         IReorderingProcessor ireorderingprocessor = pBlockEntity.getRenderMessage(k1, (p_243502_1_) -> {
            List<IReorderingProcessor> list = fontrenderer.split(p_243502_1_, 90);
            return list.isEmpty() ? IReorderingProcessor.EMPTY : list.get(0);
         });
         if (ireorderingprocessor != null) {
            float f3 = (float)(-fontrenderer.width(ireorderingprocessor) / 2);
            fontrenderer.drawInBatch(ireorderingprocessor, f3, (float)(k1 * 10 - 20), i1, false, pMatrixStack.last().pose(), pBuffer, false, 0, pCombinedLight);
         }
      }

      pMatrixStack.popPose();
   }

   public static RenderMaterial getMaterial(Block p_228877_0_) {
      WoodType woodtype;
      if (p_228877_0_ instanceof AbstractSignBlock) {
         woodtype = ((AbstractSignBlock)p_228877_0_).type();
      } else {
         woodtype = WoodType.OAK;
      }

      return Atlases.SIGN_MATERIALS.get(woodtype);
   }

   @OnlyIn(Dist.CLIENT)
   public static final class SignModel extends Model {
      public final ModelRenderer sign = new ModelRenderer(64, 32, 0, 0);
      public final ModelRenderer stick;

      public SignModel() {
         super(RenderType::entityCutoutNoCull);
         this.sign.addBox(-12.0F, -14.0F, -1.0F, 24.0F, 12.0F, 2.0F, 0.0F);
         this.stick = new ModelRenderer(64, 32, 0, 14);
         this.stick.addBox(-1.0F, -2.0F, -1.0F, 2.0F, 14.0F, 2.0F, 0.0F);
      }

      public void renderToBuffer(MatrixStack pMatrixStack, IVertexBuilder pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
         this.sign.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
         this.stick.render(pMatrixStack, pBuffer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
      }
   }
}
