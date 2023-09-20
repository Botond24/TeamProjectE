package net.minecraft.client.renderer.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class TileEntityRenderer<T extends TileEntity> {
   protected final TileEntityRendererDispatcher renderer;

   public TileEntityRenderer(TileEntityRendererDispatcher p_i226006_1_) {
      this.renderer = p_i226006_1_;
   }

   public abstract void render(T pBlockEntity, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, int pCombinedOverlay);

   public boolean shouldRenderOffScreen(T pTe) {
      return false;
   }
}