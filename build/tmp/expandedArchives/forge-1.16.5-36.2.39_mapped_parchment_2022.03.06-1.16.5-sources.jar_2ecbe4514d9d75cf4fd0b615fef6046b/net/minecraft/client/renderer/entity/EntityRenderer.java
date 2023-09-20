package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.LightType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class EntityRenderer<T extends Entity> {
   protected final EntityRendererManager entityRenderDispatcher;
   protected float shadowRadius;
   protected float shadowStrength = 1.0F;

   protected EntityRenderer(EntityRendererManager p_i46179_1_) {
      this.entityRenderDispatcher = p_i46179_1_;
   }

   public final int getPackedLightCoords(T pEntity, float pPartialTicks) {
      BlockPos blockpos = new BlockPos(pEntity.getLightProbePosition(pPartialTicks));
      return LightTexture.pack(this.getBlockLightLevel(pEntity, blockpos), this.getSkyLightLevel(pEntity, blockpos));
   }

   protected int getSkyLightLevel(T pEntity, BlockPos pPos) {
      return pEntity.level.getBrightness(LightType.SKY, pPos);
   }

   protected int getBlockLightLevel(T pEntity, BlockPos pPos) {
      return pEntity.isOnFire() ? 15 : pEntity.level.getBrightness(LightType.BLOCK, pPos);
   }

   public boolean shouldRender(T pLivingEntity, ClippingHelper pCamera, double pCamX, double pCamY, double pCamZ) {
      if (!pLivingEntity.shouldRender(pCamX, pCamY, pCamZ)) {
         return false;
      } else if (pLivingEntity.noCulling) {
         return true;
      } else {
         AxisAlignedBB axisalignedbb = pLivingEntity.getBoundingBoxForCulling().inflate(0.5D);
         if (axisalignedbb.hasNaN() || axisalignedbb.getSize() == 0.0D) {
            axisalignedbb = new AxisAlignedBB(pLivingEntity.getX() - 2.0D, pLivingEntity.getY() - 2.0D, pLivingEntity.getZ() - 2.0D, pLivingEntity.getX() + 2.0D, pLivingEntity.getY() + 2.0D, pLivingEntity.getZ() + 2.0D);
         }

         return pCamera.isVisible(axisalignedbb);
      }
   }

   public Vector3d getRenderOffset(T pEntity, float pPartialTicks) {
      return Vector3d.ZERO;
   }

   public void render(T pEntity, float pEntityYaw, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight) {
      net.minecraftforge.client.event.RenderNameplateEvent renderNameplateEvent = new net.minecraftforge.client.event.RenderNameplateEvent(pEntity, pEntity.getDisplayName(), this, pMatrixStack, pBuffer, pPackedLight, pPartialTicks);
      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(renderNameplateEvent);
      if (renderNameplateEvent.getResult() != net.minecraftforge.eventbus.api.Event.Result.DENY && (renderNameplateEvent.getResult() == net.minecraftforge.eventbus.api.Event.Result.ALLOW || this.shouldShowName(pEntity))) {
         this.renderNameTag(pEntity, renderNameplateEvent.getContent(), pMatrixStack, pBuffer, pPackedLight);
      }
   }

   protected boolean shouldShowName(T pEntity) {
      return pEntity.shouldShowName() && pEntity.hasCustomName();
   }

   /**
    * Returns the location of an entity's texture.
    */
   public abstract ResourceLocation getTextureLocation(T pEntity);

   /**
    * Returns the font renderer from the set render manager
    */
   public FontRenderer getFont() {
      return this.entityRenderDispatcher.getFont();
   }

   protected void renderNameTag(T pEntity, ITextComponent pDisplayName, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight) {
      double d0 = this.entityRenderDispatcher.distanceToSqr(pEntity);
      if (net.minecraftforge.client.ForgeHooksClient.isNameplateInRenderDistance(pEntity, d0)) {
         boolean flag = !pEntity.isDiscrete();
         float f = pEntity.getBbHeight() + 0.5F;
         int i = "deadmau5".equals(pDisplayName.getString()) ? -10 : 0;
         pMatrixStack.pushPose();
         pMatrixStack.translate(0.0D, (double)f, 0.0D);
         pMatrixStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
         pMatrixStack.scale(-0.025F, -0.025F, 0.025F);
         Matrix4f matrix4f = pMatrixStack.last().pose();
         float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
         int j = (int)(f1 * 255.0F) << 24;
         FontRenderer fontrenderer = this.getFont();
         float f2 = (float)(-fontrenderer.width(pDisplayName) / 2);
         fontrenderer.drawInBatch(pDisplayName, f2, (float)i, 553648127, false, matrix4f, pBuffer, flag, j, pPackedLight);
         if (flag) {
            fontrenderer.drawInBatch(pDisplayName, f2, (float)i, -1, false, matrix4f, pBuffer, false, 0, pPackedLight);
         }

         pMatrixStack.popPose();
      }
   }

   public EntityRendererManager getDispatcher() {
      return this.entityRenderDispatcher;
   }
}
