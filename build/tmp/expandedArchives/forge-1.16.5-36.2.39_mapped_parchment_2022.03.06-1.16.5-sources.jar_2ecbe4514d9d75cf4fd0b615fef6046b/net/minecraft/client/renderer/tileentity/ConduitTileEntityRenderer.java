package net.minecraft.client.renderer.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.tileentity.ConduitTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ConduitTileEntityRenderer extends TileEntityRenderer<ConduitTileEntity> {
   public static final RenderMaterial SHELL_TEXTURE = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, new ResourceLocation("entity/conduit/base"));
   public static final RenderMaterial ACTIVE_SHELL_TEXTURE = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, new ResourceLocation("entity/conduit/cage"));
   public static final RenderMaterial WIND_TEXTURE = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, new ResourceLocation("entity/conduit/wind"));
   public static final RenderMaterial VERTICAL_WIND_TEXTURE = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, new ResourceLocation("entity/conduit/wind_vertical"));
   public static final RenderMaterial OPEN_EYE_TEXTURE = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, new ResourceLocation("entity/conduit/open_eye"));
   public static final RenderMaterial CLOSED_EYE_TEXTURE = new RenderMaterial(AtlasTexture.LOCATION_BLOCKS, new ResourceLocation("entity/conduit/closed_eye"));
   private final ModelRenderer eye = new ModelRenderer(16, 16, 0, 0);
   private final ModelRenderer wind;
   private final ModelRenderer shell;
   private final ModelRenderer cage;

   public ConduitTileEntityRenderer(TileEntityRendererDispatcher p_i226009_1_) {
      super(p_i226009_1_);
      this.eye.addBox(-4.0F, -4.0F, 0.0F, 8.0F, 8.0F, 0.0F, 0.01F);
      this.wind = new ModelRenderer(64, 32, 0, 0);
      this.wind.addBox(-8.0F, -8.0F, -8.0F, 16.0F, 16.0F, 16.0F);
      this.shell = new ModelRenderer(32, 16, 0, 0);
      this.shell.addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F);
      this.cage = new ModelRenderer(32, 16, 0, 0);
      this.cage.addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);
   }

   public void render(ConduitTileEntity pBlockEntity, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pCombinedLight, int pCombinedOverlay) {
      float f = (float)pBlockEntity.tickCount + pPartialTicks;
      if (!pBlockEntity.isActive()) {
         float f5 = pBlockEntity.getActiveRotation(0.0F);
         IVertexBuilder ivertexbuilder1 = SHELL_TEXTURE.buffer(pBuffer, RenderType::entitySolid);
         pMatrixStack.pushPose();
         pMatrixStack.translate(0.5D, 0.5D, 0.5D);
         pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f5));
         this.shell.render(pMatrixStack, ivertexbuilder1, pCombinedLight, pCombinedOverlay);
         pMatrixStack.popPose();
      } else {
         float f1 = pBlockEntity.getActiveRotation(pPartialTicks) * (180F / (float)Math.PI);
         float f2 = MathHelper.sin(f * 0.1F) / 2.0F + 0.5F;
         f2 = f2 * f2 + f2;
         pMatrixStack.pushPose();
         pMatrixStack.translate(0.5D, (double)(0.3F + f2 * 0.2F), 0.5D);
         Vector3f vector3f = new Vector3f(0.5F, 1.0F, 0.5F);
         vector3f.normalize();
         pMatrixStack.mulPose(new Quaternion(vector3f, f1, true));
         this.cage.render(pMatrixStack, ACTIVE_SHELL_TEXTURE.buffer(pBuffer, RenderType::entityCutoutNoCull), pCombinedLight, pCombinedOverlay);
         pMatrixStack.popPose();
         int i = pBlockEntity.tickCount / 66 % 3;
         pMatrixStack.pushPose();
         pMatrixStack.translate(0.5D, 0.5D, 0.5D);
         if (i == 1) {
            pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
         } else if (i == 2) {
            pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(90.0F));
         }

         IVertexBuilder ivertexbuilder = (i == 1 ? VERTICAL_WIND_TEXTURE : WIND_TEXTURE).buffer(pBuffer, RenderType::entityCutoutNoCull);
         this.wind.render(pMatrixStack, ivertexbuilder, pCombinedLight, pCombinedOverlay);
         pMatrixStack.popPose();
         pMatrixStack.pushPose();
         pMatrixStack.translate(0.5D, 0.5D, 0.5D);
         pMatrixStack.scale(0.875F, 0.875F, 0.875F);
         pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
         pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
         this.wind.render(pMatrixStack, ivertexbuilder, pCombinedLight, pCombinedOverlay);
         pMatrixStack.popPose();
         ActiveRenderInfo activerenderinfo = this.renderer.camera;
         pMatrixStack.pushPose();
         pMatrixStack.translate(0.5D, (double)(0.3F + f2 * 0.2F), 0.5D);
         pMatrixStack.scale(0.5F, 0.5F, 0.5F);
         float f3 = -activerenderinfo.getYRot();
         pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(f3));
         pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(activerenderinfo.getXRot()));
         pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
         float f4 = 1.3333334F;
         pMatrixStack.scale(1.3333334F, 1.3333334F, 1.3333334F);
         this.eye.render(pMatrixStack, (pBlockEntity.isHunting() ? OPEN_EYE_TEXTURE : CLOSED_EYE_TEXTURE).buffer(pBuffer, RenderType::entityCutoutNoCull), pCombinedLight, pCombinedOverlay);
         pMatrixStack.popPose();
      }
   }
}