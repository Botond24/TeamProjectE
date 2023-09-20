package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Util;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CollisionBoxDebugRenderer implements DebugRenderer.IDebugRenderer {
   private final Minecraft minecraft;
   private double lastUpdateTime = Double.MIN_VALUE;
   private List<VoxelShape> shapes = Collections.emptyList();

   public CollisionBoxDebugRenderer(Minecraft p_i47215_1_) {
      this.minecraft = p_i47215_1_;
   }

   public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, double pCamX, double pCamY, double pCamZ) {
      double d0 = (double)Util.getNanos();
      if (d0 - this.lastUpdateTime > 1.0E8D) {
         this.lastUpdateTime = d0;
         Entity entity = this.minecraft.gameRenderer.getMainCamera().getEntity();
         this.shapes = entity.level.getCollisions(entity, entity.getBoundingBox().inflate(6.0D), (p_239370_0_) -> {
            return true;
         }).collect(Collectors.toList());
      }

      IVertexBuilder ivertexbuilder = pBuffer.getBuffer(RenderType.lines());

      for(VoxelShape voxelshape : this.shapes) {
         WorldRenderer.renderVoxelShape(pMatrixStack, ivertexbuilder, voxelshape, -pCamX, -pCamY, -pCamZ, 1.0F, 1.0F, 1.0F, 1.0F);
      }

   }
}