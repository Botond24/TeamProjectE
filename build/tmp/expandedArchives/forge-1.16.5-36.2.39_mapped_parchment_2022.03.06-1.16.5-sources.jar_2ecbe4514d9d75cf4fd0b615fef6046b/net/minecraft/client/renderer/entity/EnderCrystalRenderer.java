package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EnderCrystalRenderer extends EntityRenderer<EnderCrystalEntity> {
   private static final ResourceLocation END_CRYSTAL_LOCATION = new ResourceLocation("textures/entity/end_crystal/end_crystal.png");
   private static final RenderType RENDER_TYPE = RenderType.entityCutoutNoCull(END_CRYSTAL_LOCATION);
   private static final float SIN_45 = (float)Math.sin((Math.PI / 4D));
   private final ModelRenderer cube;
   private final ModelRenderer glass;
   private final ModelRenderer base;

   public EnderCrystalRenderer(EntityRendererManager p_i46184_1_) {
      super(p_i46184_1_);
      this.shadowRadius = 0.5F;
      this.glass = new ModelRenderer(64, 32, 0, 0);
      this.glass.addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);
      this.cube = new ModelRenderer(64, 32, 32, 0);
      this.cube.addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);
      this.base = new ModelRenderer(64, 32, 0, 16);
      this.base.addBox(-6.0F, 0.0F, -6.0F, 12.0F, 4.0F, 12.0F);
   }

   public void render(EnderCrystalEntity pEntity, float pEntityYaw, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight) {
      pMatrixStack.pushPose();
      float f = getY(pEntity, pPartialTicks);
      float f1 = ((float)pEntity.time + pPartialTicks) * 3.0F;
      IVertexBuilder ivertexbuilder = pBuffer.getBuffer(RENDER_TYPE);
      pMatrixStack.pushPose();
      pMatrixStack.scale(2.0F, 2.0F, 2.0F);
      pMatrixStack.translate(0.0D, -0.5D, 0.0D);
      int i = OverlayTexture.NO_OVERLAY;
      if (pEntity.showsBottom()) {
         this.base.render(pMatrixStack, ivertexbuilder, pPackedLight, i);
      }

      pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f1));
      pMatrixStack.translate(0.0D, (double)(1.5F + f / 2.0F), 0.0D);
      pMatrixStack.mulPose(new Quaternion(new Vector3f(SIN_45, 0.0F, SIN_45), 60.0F, true));
      this.glass.render(pMatrixStack, ivertexbuilder, pPackedLight, i);
      float f2 = 0.875F;
      pMatrixStack.scale(0.875F, 0.875F, 0.875F);
      pMatrixStack.mulPose(new Quaternion(new Vector3f(SIN_45, 0.0F, SIN_45), 60.0F, true));
      pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f1));
      this.glass.render(pMatrixStack, ivertexbuilder, pPackedLight, i);
      pMatrixStack.scale(0.875F, 0.875F, 0.875F);
      pMatrixStack.mulPose(new Quaternion(new Vector3f(SIN_45, 0.0F, SIN_45), 60.0F, true));
      pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f1));
      this.cube.render(pMatrixStack, ivertexbuilder, pPackedLight, i);
      pMatrixStack.popPose();
      pMatrixStack.popPose();
      BlockPos blockpos = pEntity.getBeamTarget();
      if (blockpos != null) {
         float f3 = (float)blockpos.getX() + 0.5F;
         float f4 = (float)blockpos.getY() + 0.5F;
         float f5 = (float)blockpos.getZ() + 0.5F;
         float f6 = (float)((double)f3 - pEntity.getX());
         float f7 = (float)((double)f4 - pEntity.getY());
         float f8 = (float)((double)f5 - pEntity.getZ());
         pMatrixStack.translate((double)f6, (double)f7, (double)f8);
         EnderDragonRenderer.renderCrystalBeams(-f6, -f7 + f, -f8, pPartialTicks, pEntity.time, pMatrixStack, pBuffer, pPackedLight);
      }

      super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
   }

   public static float getY(EnderCrystalEntity p_229051_0_, float p_229051_1_) {
      float f = (float)p_229051_0_.time + p_229051_1_;
      float f1 = MathHelper.sin(f * 0.2F) / 2.0F + 0.5F;
      f1 = (f1 * f1 + f1) * 0.4F;
      return f1 - 1.4F;
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(EnderCrystalEntity pEntity) {
      return END_CRYSTAL_LOCATION;
   }

   public boolean shouldRender(EnderCrystalEntity pLivingEntity, ClippingHelper pCamera, double pCamX, double pCamY, double pCamZ) {
      return super.shouldRender(pLivingEntity, pCamera, pCamX, pCamY, pCamZ) || pLivingEntity.getBeamTarget() != null;
   }
}