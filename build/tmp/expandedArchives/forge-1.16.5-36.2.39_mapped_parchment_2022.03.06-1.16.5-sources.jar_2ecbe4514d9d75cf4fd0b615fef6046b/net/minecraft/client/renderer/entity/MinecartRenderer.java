package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.MinecartModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MinecartRenderer<T extends AbstractMinecartEntity> extends EntityRenderer<T> {
   private static final ResourceLocation MINECART_LOCATION = new ResourceLocation("textures/entity/minecart.png");
   protected final EntityModel<T> model = new MinecartModel<>();

   public MinecartRenderer(EntityRendererManager p_i46155_1_) {
      super(p_i46155_1_);
      this.shadowRadius = 0.7F;
   }

   public void render(T pEntity, float pEntityYaw, float pPartialTicks, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight) {
      super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
      pMatrixStack.pushPose();
      long i = (long)pEntity.getId() * 493286711L;
      i = i * i * 4392167121L + i * 98761L;
      float f = (((float)(i >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
      float f1 = (((float)(i >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
      float f2 = (((float)(i >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
      pMatrixStack.translate((double)f, (double)f1, (double)f2);
      double d0 = MathHelper.lerp((double)pPartialTicks, pEntity.xOld, pEntity.getX());
      double d1 = MathHelper.lerp((double)pPartialTicks, pEntity.yOld, pEntity.getY());
      double d2 = MathHelper.lerp((double)pPartialTicks, pEntity.zOld, pEntity.getZ());
      double d3 = (double)0.3F;
      Vector3d vector3d = pEntity.getPos(d0, d1, d2);
      float f3 = MathHelper.lerp(pPartialTicks, pEntity.xRotO, pEntity.xRot);
      if (vector3d != null) {
         Vector3d vector3d1 = pEntity.getPosOffs(d0, d1, d2, (double)0.3F);
         Vector3d vector3d2 = pEntity.getPosOffs(d0, d1, d2, (double)-0.3F);
         if (vector3d1 == null) {
            vector3d1 = vector3d;
         }

         if (vector3d2 == null) {
            vector3d2 = vector3d;
         }

         pMatrixStack.translate(vector3d.x - d0, (vector3d1.y + vector3d2.y) / 2.0D - d1, vector3d.z - d2);
         Vector3d vector3d3 = vector3d2.add(-vector3d1.x, -vector3d1.y, -vector3d1.z);
         if (vector3d3.length() != 0.0D) {
            vector3d3 = vector3d3.normalize();
            pEntityYaw = (float)(Math.atan2(vector3d3.z, vector3d3.x) * 180.0D / Math.PI);
            f3 = (float)(Math.atan(vector3d3.y) * 73.0D);
         }
      }

      pMatrixStack.translate(0.0D, 0.375D, 0.0D);
      pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - pEntityYaw));
      pMatrixStack.mulPose(Vector3f.ZP.rotationDegrees(-f3));
      float f5 = (float)pEntity.getHurtTime() - pPartialTicks;
      float f6 = pEntity.getDamage() - pPartialTicks;
      if (f6 < 0.0F) {
         f6 = 0.0F;
      }

      if (f5 > 0.0F) {
         pMatrixStack.mulPose(Vector3f.XP.rotationDegrees(MathHelper.sin(f5) * f5 * f6 / 10.0F * (float)pEntity.getHurtDir()));
      }

      int j = pEntity.getDisplayOffset();
      BlockState blockstate = pEntity.getDisplayBlockState();
      if (blockstate.getRenderShape() != BlockRenderType.INVISIBLE) {
         pMatrixStack.pushPose();
         float f4 = 0.75F;
         pMatrixStack.scale(0.75F, 0.75F, 0.75F);
         pMatrixStack.translate(-0.5D, (double)((float)(j - 8) / 16.0F), 0.5D);
         pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
         this.renderMinecartContents(pEntity, pPartialTicks, blockstate, pMatrixStack, pBuffer, pPackedLight);
         pMatrixStack.popPose();
      }

      pMatrixStack.scale(-1.0F, -1.0F, 1.0F);
      this.model.setupAnim(pEntity, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F);
      IVertexBuilder ivertexbuilder = pBuffer.getBuffer(this.model.renderType(this.getTextureLocation(pEntity)));
      this.model.renderToBuffer(pMatrixStack, ivertexbuilder, pPackedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
      pMatrixStack.popPose();
   }

   /**
    * Returns the location of an entity's texture.
    */
   public ResourceLocation getTextureLocation(T pEntity) {
      return MINECART_LOCATION;
   }

   protected void renderMinecartContents(T pEntity, float pPartialTicks, BlockState pState, MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, int pPackedLight) {
      Minecraft.getInstance().getBlockRenderer().renderSingleBlock(pState, pMatrixStack, pBuffer, pPackedLight, OverlayTexture.NO_OVERLAY);
   }
}