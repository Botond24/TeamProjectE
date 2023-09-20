package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FireworkRocketRenderer extends EntityRenderer<FireworkRocketEntity> {
   private final net.minecraft.client.renderer.ItemRenderer itemRenderer;

   public FireworkRocketRenderer(EntityRendererManager p_i50970_1_, net.minecraft.client.renderer.ItemRenderer p_i50970_2_) {
      super(p_i50970_1_);
      this.itemRenderer = p_i50970_2_;
   }

   public void render(FireworkRocketEntity pEntity, float pEntityYaw, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight) {
      pMatrixStack.pushPose();
      pMatrixStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
      pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
      if (pEntity.isShotAtAngle()) {
         pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
         pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
         pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
      }

      this.itemRenderer.renderStatic(pEntity.getItem(), ItemCameraTransforms.TransformType.GROUND, pPackedLight, OverlayTexture.NO_OVERLAY, pMatrixStack, pBuffer);
      pMatrixStack.popPose();
      super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(FireworkRocketEntity pEntity) {
      return AtlasTexture.LOCATION_BLOCKS;
   }
}