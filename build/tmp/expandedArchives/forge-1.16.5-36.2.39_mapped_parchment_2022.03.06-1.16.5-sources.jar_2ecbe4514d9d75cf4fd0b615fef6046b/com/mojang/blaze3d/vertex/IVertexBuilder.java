package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.matrix.MatrixStack;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.system.MemoryStack;

@OnlyIn(Dist.CLIENT)
public interface IVertexBuilder extends net.minecraftforge.client.extensions.IForgeVertexBuilder {
   Logger LOGGER = LogManager.getLogger();

   IVertexBuilder vertex(double pX, double pY, double pZ);

   IVertexBuilder color(int pRed, int pGreen, int pBlue, int pAlpha);

   IVertexBuilder uv(float pU, float pV);

   IVertexBuilder overlayCoords(int pU, int pV);

   IVertexBuilder uv2(int pU, int pV);

   IVertexBuilder normal(float pX, float pY, float pZ);

   void endVertex();

   default void vertex(float pX, float pY, float pZ, float pRed, float pGreen, float pBlue, float pAlpha, float pTexU, float pTexV, int pOverlayUV, int pLightmapUV, float pNormalX, float pNormalY, float pNormalZ) {
      this.vertex((double)pX, (double)pY, (double)pZ);
      this.color(pRed, pGreen, pBlue, pAlpha);
      this.uv(pTexU, pTexV);
      this.overlayCoords(pOverlayUV);
      this.uv2(pLightmapUV);
      this.normal(pNormalX, pNormalY, pNormalZ);
      this.endVertex();
   }

   default IVertexBuilder color(float pRed, float pGreen, float pBlue, float pAlpha) {
      return this.color((int)(pRed * 255.0F), (int)(pGreen * 255.0F), (int)(pBlue * 255.0F), (int)(pAlpha * 255.0F));
   }

   default IVertexBuilder uv2(int pLightmapUV) {
      return this.uv2(pLightmapUV & '\uffff', pLightmapUV >> 16 & '\uffff');
   }

   default IVertexBuilder overlayCoords(int pOverlayUV) {
      return this.overlayCoords(pOverlayUV & '\uffff', pOverlayUV >> 16 & '\uffff');
   }

   default void putBulkData(MatrixStack.Entry pPoseEntry, BakedQuad pQuad, float pRed, float pGreen, float pBlue, int pCombinedLight, int pCombinedOverlay) {
      this.putBulkData(pPoseEntry, pQuad, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, pRed, pGreen, pBlue, new int[]{pCombinedLight, pCombinedLight, pCombinedLight, pCombinedLight}, pCombinedOverlay, false);
   }

   default void putBulkData(MatrixStack.Entry pPoseEntry, BakedQuad pQuad, float[] pColorMuls, float pRed, float pGreen, float pBlue, int[] pCombinedLights, int pCombinedOverlay, boolean pMulColor) {
      int[] aint = pQuad.getVertices();
      Vector3i vector3i = pQuad.getDirection().getNormal();
      Vector3f vector3f = new Vector3f((float)vector3i.getX(), (float)vector3i.getY(), (float)vector3i.getZ());
      Matrix4f matrix4f = pPoseEntry.pose();
      vector3f.transform(pPoseEntry.normal());
      int i = 8;
      int j = aint.length / 8;

      try (MemoryStack memorystack = MemoryStack.stackPush()) {
         ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormats.BLOCK.getVertexSize());
         IntBuffer intbuffer = bytebuffer.asIntBuffer();

         for(int k = 0; k < j; ++k) {
            ((Buffer)intbuffer).clear();
            intbuffer.put(aint, k * 8, 8);
            float f = bytebuffer.getFloat(0);
            float f1 = bytebuffer.getFloat(4);
            float f2 = bytebuffer.getFloat(8);
            float f3;
            float f4;
            float f5;
            if (pMulColor) {
               float f6 = (float)(bytebuffer.get(12) & 255) / 255.0F;
               float f7 = (float)(bytebuffer.get(13) & 255) / 255.0F;
               float f8 = (float)(bytebuffer.get(14) & 255) / 255.0F;
               f3 = f6 * pColorMuls[k] * pRed;
               f4 = f7 * pColorMuls[k] * pGreen;
               f5 = f8 * pColorMuls[k] * pBlue;
            } else {
               f3 = pColorMuls[k] * pRed;
               f4 = pColorMuls[k] * pGreen;
               f5 = pColorMuls[k] * pBlue;
            }

            int l = applyBakedLighting(pCombinedLights[k], bytebuffer);
            float f9 = bytebuffer.getFloat(16);
            float f10 = bytebuffer.getFloat(20);
            Vector4f vector4f = new Vector4f(f, f1, f2, 1.0F);
            vector4f.transform(matrix4f);
            applyBakedNormals(vector3f, bytebuffer, pPoseEntry.normal());
            this.vertex(vector4f.x(), vector4f.y(), vector4f.z(), f3, f4, f5, 1.0F, f9, f10, pCombinedOverlay, l, vector3f.x(), vector3f.y(), vector3f.z());
         }
      }

   }

   default IVertexBuilder vertex(Matrix4f pMatrix, float pX, float pY, float pZ) {
      Vector4f vector4f = new Vector4f(pX, pY, pZ, 1.0F);
      vector4f.transform(pMatrix);
      return this.vertex((double)vector4f.x(), (double)vector4f.y(), (double)vector4f.z());
   }

   default IVertexBuilder normal(Matrix3f pMatrix, float pX, float pY, float pZ) {
      Vector3f vector3f = new Vector3f(pX, pY, pZ);
      vector3f.transform(pMatrix);
      return this.normal(vector3f.x(), vector3f.y(), vector3f.z());
   }
}
