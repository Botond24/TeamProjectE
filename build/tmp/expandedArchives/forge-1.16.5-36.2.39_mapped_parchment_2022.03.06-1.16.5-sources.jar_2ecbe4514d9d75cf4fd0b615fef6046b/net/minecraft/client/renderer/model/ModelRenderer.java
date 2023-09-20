package net.minecraft.client.renderer.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.Random;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelRenderer {
   private float xTexSize = 64.0F;
   private float yTexSize = 32.0F;
   private int xTexOffs;
   private int yTexOffs;
   public float x;
   public float y;
   public float z;
   public float xRot;
   public float yRot;
   public float zRot;
   public boolean mirror;
   public boolean visible = true;
   private final ObjectList<ModelRenderer.ModelBox> cubes = new ObjectArrayList<>();
   private final ObjectList<ModelRenderer> children = new ObjectArrayList<>();

   public ModelRenderer(Model pModel) {
      pModel.accept(this);
      this.setTexSize(pModel.texWidth, pModel.texHeight);
   }

   public ModelRenderer(Model pModel, int p_i46358_2_, int p_i46358_3_) {
      this(pModel.texWidth, pModel.texHeight, p_i46358_2_, p_i46358_3_);
      pModel.accept(this);
   }

   public ModelRenderer(int p_i225949_1_, int p_i225949_2_, int p_i225949_3_, int p_i225949_4_) {
      this.setTexSize(p_i225949_1_, p_i225949_2_);
      this.texOffs(p_i225949_3_, p_i225949_4_);
   }

   private ModelRenderer() {
   }

   public ModelRenderer createShallowCopy() {
      ModelRenderer modelrenderer = new ModelRenderer();
      modelrenderer.copyFrom(this);
      return modelrenderer;
   }

   public void copyFrom(ModelRenderer pModelPart) {
      this.xRot = pModelPart.xRot;
      this.yRot = pModelPart.yRot;
      this.zRot = pModelPart.zRot;
      this.x = pModelPart.x;
      this.y = pModelPart.y;
      this.z = pModelPart.z;
   }

   public void addChild(ModelRenderer pPart) {
      this.children.add(pPart);
   }

   public ModelRenderer texOffs(int p_78784_1_, int p_78784_2_) {
      this.xTexOffs = p_78784_1_;
      this.yTexOffs = p_78784_2_;
      return this;
   }

   public ModelRenderer addBox(String p_217178_1_, float p_217178_2_, float p_217178_3_, float p_217178_4_, int p_217178_5_, int p_217178_6_, int p_217178_7_, float p_217178_8_, int p_217178_9_, int p_217178_10_) {
      this.texOffs(p_217178_9_, p_217178_10_);
      this.addBox(this.xTexOffs, this.yTexOffs, p_217178_2_, p_217178_3_, p_217178_4_, (float)p_217178_5_, (float)p_217178_6_, (float)p_217178_7_, p_217178_8_, p_217178_8_, p_217178_8_, this.mirror, false);
      return this;
   }

   public ModelRenderer addBox(float p_228300_1_, float p_228300_2_, float p_228300_3_, float p_228300_4_, float p_228300_5_, float p_228300_6_) {
      this.addBox(this.xTexOffs, this.yTexOffs, p_228300_1_, p_228300_2_, p_228300_3_, p_228300_4_, p_228300_5_, p_228300_6_, 0.0F, 0.0F, 0.0F, this.mirror, false);
      return this;
   }

   public ModelRenderer addBox(float p_228304_1_, float p_228304_2_, float p_228304_3_, float p_228304_4_, float p_228304_5_, float p_228304_6_, boolean p_228304_7_) {
      this.addBox(this.xTexOffs, this.yTexOffs, p_228304_1_, p_228304_2_, p_228304_3_, p_228304_4_, p_228304_5_, p_228304_6_, 0.0F, 0.0F, 0.0F, p_228304_7_, false);
      return this;
   }

   public void addBox(float p_228301_1_, float p_228301_2_, float p_228301_3_, float p_228301_4_, float p_228301_5_, float p_228301_6_, float p_228301_7_) {
      this.addBox(this.xTexOffs, this.yTexOffs, p_228301_1_, p_228301_2_, p_228301_3_, p_228301_4_, p_228301_5_, p_228301_6_, p_228301_7_, p_228301_7_, p_228301_7_, this.mirror, false);
   }

   public void addBox(float p_228302_1_, float p_228302_2_, float p_228302_3_, float p_228302_4_, float p_228302_5_, float p_228302_6_, float p_228302_7_, float p_228302_8_, float p_228302_9_) {
      this.addBox(this.xTexOffs, this.yTexOffs, p_228302_1_, p_228302_2_, p_228302_3_, p_228302_4_, p_228302_5_, p_228302_6_, p_228302_7_, p_228302_8_, p_228302_9_, this.mirror, false);
   }

   public void addBox(float p_228303_1_, float p_228303_2_, float p_228303_3_, float p_228303_4_, float p_228303_5_, float p_228303_6_, float p_228303_7_, boolean p_228303_8_) {
      this.addBox(this.xTexOffs, this.yTexOffs, p_228303_1_, p_228303_2_, p_228303_3_, p_228303_4_, p_228303_5_, p_228303_6_, p_228303_7_, p_228303_7_, p_228303_7_, p_228303_8_, false);
   }

   private void addBox(int p_228305_1_, int p_228305_2_, float p_228305_3_, float p_228305_4_, float p_228305_5_, float p_228305_6_, float p_228305_7_, float p_228305_8_, float p_228305_9_, float p_228305_10_, float p_228305_11_, boolean p_228305_12_, boolean p_228305_13_) {
      this.cubes.add(new ModelRenderer.ModelBox(p_228305_1_, p_228305_2_, p_228305_3_, p_228305_4_, p_228305_5_, p_228305_6_, p_228305_7_, p_228305_8_, p_228305_9_, p_228305_10_, p_228305_11_, p_228305_12_, this.xTexSize, this.yTexSize));
   }

   public void setPos(float pX, float pY, float pZ) {
      this.x = pX;
      this.y = pY;
      this.z = pZ;
   }

   public void render(MatrixStack pPoseStack, IVertexBuilder pVertexConsumer, int pPackedLight, int pPackedOverlay) {
      this.render(pPoseStack, pVertexConsumer, pPackedLight, pPackedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
   }

   public void render(MatrixStack pPoseStack, IVertexBuilder pVertexConsumer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
      if (this.visible) {
         if (!this.cubes.isEmpty() || !this.children.isEmpty()) {
            pPoseStack.pushPose();
            this.translateAndRotate(pPoseStack);
            this.compile(pPoseStack.last(), pVertexConsumer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);

            for(ModelRenderer modelrenderer : this.children) {
               modelrenderer.render(pPoseStack, pVertexConsumer, pPackedLight, pPackedOverlay, pRed, pGreen, pBlue, pAlpha);
            }

            pPoseStack.popPose();
         }
      }
   }

   public void translateAndRotate(MatrixStack pPoseStack) {
      pPoseStack.translate((double)(this.x / 16.0F), (double)(this.y / 16.0F), (double)(this.z / 16.0F));
      if (this.zRot != 0.0F) {
         pPoseStack.mulPose(Vector3f.ZP.rotation(this.zRot));
      }

      if (this.yRot != 0.0F) {
         pPoseStack.mulPose(Vector3f.YP.rotation(this.yRot));
      }

      if (this.xRot != 0.0F) {
         pPoseStack.mulPose(Vector3f.XP.rotation(this.xRot));
      }

   }

   private void compile(MatrixStack.Entry pPose, IVertexBuilder pVertexConsumer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
      Matrix4f matrix4f = pPose.pose();
      Matrix3f matrix3f = pPose.normal();

      for(ModelRenderer.ModelBox modelrenderer$modelbox : this.cubes) {
         for(ModelRenderer.TexturedQuad modelrenderer$texturedquad : modelrenderer$modelbox.polygons) {
            Vector3f vector3f = modelrenderer$texturedquad.normal.copy();
            vector3f.transform(matrix3f);
            float f = vector3f.x();
            float f1 = vector3f.y();
            float f2 = vector3f.z();

            for(int i = 0; i < 4; ++i) {
               ModelRenderer.PositionTextureVertex modelrenderer$positiontexturevertex = modelrenderer$texturedquad.vertices[i];
               float f3 = modelrenderer$positiontexturevertex.pos.x() / 16.0F;
               float f4 = modelrenderer$positiontexturevertex.pos.y() / 16.0F;
               float f5 = modelrenderer$positiontexturevertex.pos.z() / 16.0F;
               Vector4f vector4f = new Vector4f(f3, f4, f5, 1.0F);
               vector4f.transform(matrix4f);
               pVertexConsumer.vertex(vector4f.x(), vector4f.y(), vector4f.z(), pRed, pGreen, pBlue, pAlpha, modelrenderer$positiontexturevertex.u, modelrenderer$positiontexturevertex.v, pPackedOverlay, pPackedLight, f, f1, f2);
            }
         }
      }

   }

   public ModelRenderer setTexSize(int p_78787_1_, int p_78787_2_) {
      this.xTexSize = (float)p_78787_1_;
      this.yTexSize = (float)p_78787_2_;
      return this;
   }

   public ModelRenderer.ModelBox getRandomCube(Random pRandom) {
      return this.cubes.get(pRandom.nextInt(this.cubes.size()));
   }

   @OnlyIn(Dist.CLIENT)
   public static class ModelBox {
      private final ModelRenderer.TexturedQuad[] polygons;
      public final float minX;
      public final float minY;
      public final float minZ;
      public final float maxX;
      public final float maxY;
      public final float maxZ;

      public ModelBox(int pTexCoordU, int pTexCoordV, float pOriginX, float pOriginY, float pOriginZ, float pDimensionX, float pDimensionY, float pDimensionZ, float pGrowX, float pGrowY, float pGrowZ, boolean pMirror, float pTexWidthScaled, float pTexHeightScaled) {
         this.minX = pOriginX;
         this.minY = pOriginY;
         this.minZ = pOriginZ;
         this.maxX = pOriginX + pDimensionX;
         this.maxY = pOriginY + pDimensionY;
         this.maxZ = pOriginZ + pDimensionZ;
         this.polygons = new ModelRenderer.TexturedQuad[6];
         float f = pOriginX + pDimensionX;
         float f1 = pOriginY + pDimensionY;
         float f2 = pOriginZ + pDimensionZ;
         pOriginX = pOriginX - pGrowX;
         pOriginY = pOriginY - pGrowY;
         pOriginZ = pOriginZ - pGrowZ;
         f = f + pGrowX;
         f1 = f1 + pGrowY;
         f2 = f2 + pGrowZ;
         if (pMirror) {
            float f3 = f;
            f = pOriginX;
            pOriginX = f3;
         }

         ModelRenderer.PositionTextureVertex modelrenderer$positiontexturevertex7 = new ModelRenderer.PositionTextureVertex(pOriginX, pOriginY, pOriginZ, 0.0F, 0.0F);
         ModelRenderer.PositionTextureVertex modelrenderer$positiontexturevertex = new ModelRenderer.PositionTextureVertex(f, pOriginY, pOriginZ, 0.0F, 8.0F);
         ModelRenderer.PositionTextureVertex modelrenderer$positiontexturevertex1 = new ModelRenderer.PositionTextureVertex(f, f1, pOriginZ, 8.0F, 8.0F);
         ModelRenderer.PositionTextureVertex modelrenderer$positiontexturevertex2 = new ModelRenderer.PositionTextureVertex(pOriginX, f1, pOriginZ, 8.0F, 0.0F);
         ModelRenderer.PositionTextureVertex modelrenderer$positiontexturevertex3 = new ModelRenderer.PositionTextureVertex(pOriginX, pOriginY, f2, 0.0F, 0.0F);
         ModelRenderer.PositionTextureVertex modelrenderer$positiontexturevertex4 = new ModelRenderer.PositionTextureVertex(f, pOriginY, f2, 0.0F, 8.0F);
         ModelRenderer.PositionTextureVertex modelrenderer$positiontexturevertex5 = new ModelRenderer.PositionTextureVertex(f, f1, f2, 8.0F, 8.0F);
         ModelRenderer.PositionTextureVertex modelrenderer$positiontexturevertex6 = new ModelRenderer.PositionTextureVertex(pOriginX, f1, f2, 8.0F, 0.0F);
         float f4 = (float)pTexCoordU;
         float f5 = (float)pTexCoordU + pDimensionZ;
         float f6 = (float)pTexCoordU + pDimensionZ + pDimensionX;
         float f7 = (float)pTexCoordU + pDimensionZ + pDimensionX + pDimensionX;
         float f8 = (float)pTexCoordU + pDimensionZ + pDimensionX + pDimensionZ;
         float f9 = (float)pTexCoordU + pDimensionZ + pDimensionX + pDimensionZ + pDimensionX;
         float f10 = (float)pTexCoordV;
         float f11 = (float)pTexCoordV + pDimensionZ;
         float f12 = (float)pTexCoordV + pDimensionZ + pDimensionY;
         this.polygons[2] = new ModelRenderer.TexturedQuad(new ModelRenderer.PositionTextureVertex[]{modelrenderer$positiontexturevertex4, modelrenderer$positiontexturevertex3, modelrenderer$positiontexturevertex7, modelrenderer$positiontexturevertex}, f5, f10, f6, f11, pTexWidthScaled, pTexHeightScaled, pMirror, Direction.DOWN);
         this.polygons[3] = new ModelRenderer.TexturedQuad(new ModelRenderer.PositionTextureVertex[]{modelrenderer$positiontexturevertex1, modelrenderer$positiontexturevertex2, modelrenderer$positiontexturevertex6, modelrenderer$positiontexturevertex5}, f6, f11, f7, f10, pTexWidthScaled, pTexHeightScaled, pMirror, Direction.UP);
         this.polygons[1] = new ModelRenderer.TexturedQuad(new ModelRenderer.PositionTextureVertex[]{modelrenderer$positiontexturevertex7, modelrenderer$positiontexturevertex3, modelrenderer$positiontexturevertex6, modelrenderer$positiontexturevertex2}, f4, f11, f5, f12, pTexWidthScaled, pTexHeightScaled, pMirror, Direction.WEST);
         this.polygons[4] = new ModelRenderer.TexturedQuad(new ModelRenderer.PositionTextureVertex[]{modelrenderer$positiontexturevertex, modelrenderer$positiontexturevertex7, modelrenderer$positiontexturevertex2, modelrenderer$positiontexturevertex1}, f5, f11, f6, f12, pTexWidthScaled, pTexHeightScaled, pMirror, Direction.NORTH);
         this.polygons[0] = new ModelRenderer.TexturedQuad(new ModelRenderer.PositionTextureVertex[]{modelrenderer$positiontexturevertex4, modelrenderer$positiontexturevertex, modelrenderer$positiontexturevertex1, modelrenderer$positiontexturevertex5}, f6, f11, f8, f12, pTexWidthScaled, pTexHeightScaled, pMirror, Direction.EAST);
         this.polygons[5] = new ModelRenderer.TexturedQuad(new ModelRenderer.PositionTextureVertex[]{modelrenderer$positiontexturevertex3, modelrenderer$positiontexturevertex4, modelrenderer$positiontexturevertex5, modelrenderer$positiontexturevertex6}, f8, f11, f9, f12, pTexWidthScaled, pTexHeightScaled, pMirror, Direction.SOUTH);
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class PositionTextureVertex {
      public final Vector3f pos;
      public final float u;
      public final float v;

      public PositionTextureVertex(float pX, float pY, float pZ, float pU, float pV) {
         this(new Vector3f(pX, pY, pZ), pU, pV);
      }

      public ModelRenderer.PositionTextureVertex remap(float pU, float pV) {
         return new ModelRenderer.PositionTextureVertex(this.pos, pU, pV);
      }

      public PositionTextureVertex(Vector3f pPos, float pU, float pV) {
         this.pos = pPos;
         this.u = pU;
         this.v = pV;
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class TexturedQuad {
      public final ModelRenderer.PositionTextureVertex[] vertices;
      public final Vector3f normal;

      public TexturedQuad(ModelRenderer.PositionTextureVertex[] p_i225951_1_, float p_i225951_2_, float p_i225951_3_, float p_i225951_4_, float p_i225951_5_, float p_i225951_6_, float p_i225951_7_, boolean p_i225951_8_, Direction p_i225951_9_) {
         this.vertices = p_i225951_1_;
         float f = 0.0F / p_i225951_6_;
         float f1 = 0.0F / p_i225951_7_;
         p_i225951_1_[0] = p_i225951_1_[0].remap(p_i225951_4_ / p_i225951_6_ - f, p_i225951_3_ / p_i225951_7_ + f1);
         p_i225951_1_[1] = p_i225951_1_[1].remap(p_i225951_2_ / p_i225951_6_ + f, p_i225951_3_ / p_i225951_7_ + f1);
         p_i225951_1_[2] = p_i225951_1_[2].remap(p_i225951_2_ / p_i225951_6_ + f, p_i225951_5_ / p_i225951_7_ - f1);
         p_i225951_1_[3] = p_i225951_1_[3].remap(p_i225951_4_ / p_i225951_6_ - f, p_i225951_5_ / p_i225951_7_ - f1);
         if (p_i225951_8_) {
            int i = p_i225951_1_.length;

            for(int j = 0; j < i / 2; ++j) {
               ModelRenderer.PositionTextureVertex modelrenderer$positiontexturevertex = p_i225951_1_[j];
               p_i225951_1_[j] = p_i225951_1_[i - 1 - j];
               p_i225951_1_[i - 1 - j] = modelrenderer$positiontexturevertex;
            }
         }

         this.normal = p_i225951_9_.step();
         if (p_i225951_8_) {
            this.normal.mul(-1.0F, 1.0F, 1.0F);
         }

      }
   }
}