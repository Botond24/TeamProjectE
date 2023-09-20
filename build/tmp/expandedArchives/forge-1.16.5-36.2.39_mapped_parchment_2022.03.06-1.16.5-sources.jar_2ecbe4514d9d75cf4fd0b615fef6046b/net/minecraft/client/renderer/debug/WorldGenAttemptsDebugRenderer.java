package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WorldGenAttemptsDebugRenderer implements DebugRenderer.IDebugRenderer {
   private final List<BlockPos> toRender = Lists.newArrayList();
   private final List<Float> scales = Lists.newArrayList();
   private final List<Float> alphas = Lists.newArrayList();
   private final List<Float> reds = Lists.newArrayList();
   private final List<Float> greens = Lists.newArrayList();
   private final List<Float> blues = Lists.newArrayList();

   public void addPos(BlockPos pPos, float pSize, float pRed, float pGreen, float pBlue, float pAlpha) {
      this.toRender.add(pPos);
      this.scales.add(pSize);
      this.alphas.add(pAlpha);
      this.reds.add(pRed);
      this.greens.add(pGreen);
      this.blues.add(pBlue);
   }

   public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, double pCamX, double pCamY, double pCamZ) {
      RenderSystem.pushMatrix();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableTexture();
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuilder();
      bufferbuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);

      for(int i = 0; i < this.toRender.size(); ++i) {
         BlockPos blockpos = this.toRender.get(i);
         Float f = this.scales.get(i);
         float f1 = f / 2.0F;
         WorldRenderer.addChainedFilledBoxVertices(bufferbuilder, (double)((float)blockpos.getX() + 0.5F - f1) - pCamX, (double)((float)blockpos.getY() + 0.5F - f1) - pCamY, (double)((float)blockpos.getZ() + 0.5F - f1) - pCamZ, (double)((float)blockpos.getX() + 0.5F + f1) - pCamX, (double)((float)blockpos.getY() + 0.5F + f1) - pCamY, (double)((float)blockpos.getZ() + 0.5F + f1) - pCamZ, this.reds.get(i), this.greens.get(i), this.blues.get(i), this.alphas.get(i));
      }

      tessellator.end();
      RenderSystem.enableTexture();
      RenderSystem.popMatrix();
   }
}