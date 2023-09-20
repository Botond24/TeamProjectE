package net.minecraft.client.renderer.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MobSpawnerTileEntityRenderer extends TileEntityRenderer<MobSpawnerTileEntity> {
   public MobSpawnerTileEntityRenderer(TileEntityRendererDispatcher p_i226016_1_) {
      super(p_i226016_1_);
   }

   public void render(MobSpawnerTileEntity pBlockEntity, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, int pCombinedOverlay) {
      pMatrixStack.pushPose();
      pMatrixStack.translate(0.5D, 0.0D, 0.5D);
      AbstractSpawner abstractspawner = pBlockEntity.getSpawner();
      Entity entity = abstractspawner.getOrCreateDisplayEntity();
      if (entity != null) {
         float f = 0.53125F;
         float f1 = Math.max(entity.getBbWidth(), entity.getBbHeight());
         if ((double)f1 > 1.0D) {
            f /= f1;
         }

         pMatrixStack.translate(0.0D, (double)0.4F, 0.0D);
         pMatrixStack.mulPose(Vector3f.YP.rotationDegrees((float)MathHelper.lerp((double)pPartialTicks, abstractspawner.getoSpin(), abstractspawner.getSpin()) * 10.0F));
         pMatrixStack.translate(0.0D, (double)-0.2F, 0.0D);
         pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(-30.0F));
         pMatrixStack.scale(f, f, f);
         Minecraft.getInstance().getEntityRenderDispatcher().render(entity, 0.0D, 0.0D, 0.0D, 0.0F, pPartialTicks, pMatrixStack, pBuffer, pCombinedLight);
      }

      pMatrixStack.popPose();
   }
}