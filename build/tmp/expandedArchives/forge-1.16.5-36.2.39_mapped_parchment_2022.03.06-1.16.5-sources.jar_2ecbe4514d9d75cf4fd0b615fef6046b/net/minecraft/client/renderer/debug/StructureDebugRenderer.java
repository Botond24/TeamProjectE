package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StructureDebugRenderer implements DebugRenderer.IDebugRenderer {
   private final Minecraft minecraft;
   private final Map<DimensionType, Map<String, MutableBoundingBox>> postMainBoxes = Maps.newIdentityHashMap();
   private final Map<DimensionType, Map<String, MutableBoundingBox>> postPiecesBoxes = Maps.newIdentityHashMap();
   private final Map<DimensionType, Map<String, Boolean>> startPiecesMap = Maps.newIdentityHashMap();

   public StructureDebugRenderer(Minecraft p_i48764_1_) {
      this.minecraft = p_i48764_1_;
   }

   public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, double pCamX, double pCamY, double pCamZ) {
      ActiveRenderInfo activerenderinfo = this.minecraft.gameRenderer.getMainCamera();
      IWorld iworld = this.minecraft.level;
      DimensionType dimensiontype = iworld.dimensionType();
      BlockPos blockpos = new BlockPos(activerenderinfo.getPosition().x, 0.0D, activerenderinfo.getPosition().z);
      IVertexBuilder ivertexbuilder = pBuffer.getBuffer(RenderType.lines());
      if (this.postMainBoxes.containsKey(dimensiontype)) {
         for(MutableBoundingBox mutableboundingbox : this.postMainBoxes.get(dimensiontype).values()) {
            if (blockpos.closerThan(mutableboundingbox.getCenter(), 500.0D)) {
               WorldRenderer.renderLineBox(pMatrixStack, ivertexbuilder, (double)mutableboundingbox.x0 - pCamX, (double)mutableboundingbox.y0 - pCamY, (double)mutableboundingbox.z0 - pCamZ, (double)(mutableboundingbox.x1 + 1) - pCamX, (double)(mutableboundingbox.y1 + 1) - pCamY, (double)(mutableboundingbox.z1 + 1) - pCamZ, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F, 1.0F);
            }
         }
      }

      if (this.postPiecesBoxes.containsKey(dimensiontype)) {
         for(Entry<String, MutableBoundingBox> entry : this.postPiecesBoxes.get(dimensiontype).entrySet()) {
            String s = entry.getKey();
            MutableBoundingBox mutableboundingbox1 = entry.getValue();
            Boolean obool = this.startPiecesMap.get(dimensiontype).get(s);
            if (blockpos.closerThan(mutableboundingbox1.getCenter(), 500.0D)) {
               if (obool) {
                  WorldRenderer.renderLineBox(pMatrixStack, ivertexbuilder, (double)mutableboundingbox1.x0 - pCamX, (double)mutableboundingbox1.y0 - pCamY, (double)mutableboundingbox1.z0 - pCamZ, (double)(mutableboundingbox1.x1 + 1) - pCamX, (double)(mutableboundingbox1.y1 + 1) - pCamY, (double)(mutableboundingbox1.z1 + 1) - pCamZ, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F, 1.0F, 0.0F);
               } else {
                  WorldRenderer.renderLineBox(pMatrixStack, ivertexbuilder, (double)mutableboundingbox1.x0 - pCamX, (double)mutableboundingbox1.y0 - pCamY, (double)mutableboundingbox1.z0 - pCamZ, (double)(mutableboundingbox1.x1 + 1) - pCamX, (double)(mutableboundingbox1.y1 + 1) - pCamY, (double)(mutableboundingbox1.z1 + 1) - pCamZ, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, 0.0F, 1.0F);
               }
            }
         }
      }

   }

   public void addBoundingBox(MutableBoundingBox p_223454_1_, List<MutableBoundingBox> p_223454_2_, List<Boolean> p_223454_3_, DimensionType p_223454_4_) {
      if (!this.postMainBoxes.containsKey(p_223454_4_)) {
         this.postMainBoxes.put(p_223454_4_, Maps.newHashMap());
      }

      if (!this.postPiecesBoxes.containsKey(p_223454_4_)) {
         this.postPiecesBoxes.put(p_223454_4_, Maps.newHashMap());
         this.startPiecesMap.put(p_223454_4_, Maps.newHashMap());
      }

      this.postMainBoxes.get(p_223454_4_).put(p_223454_1_.toString(), p_223454_1_);

      for(int i = 0; i < p_223454_2_.size(); ++i) {
         MutableBoundingBox mutableboundingbox = p_223454_2_.get(i);
         Boolean obool = p_223454_3_.get(i);
         this.postPiecesBoxes.get(p_223454_4_).put(mutableboundingbox.toString(), mutableboundingbox);
         this.startPiecesMap.get(p_223454_4_).put(mutableboundingbox.toString(), obool);
      }

   }

   public void clear() {
      this.postMainBoxes.clear();
      this.postPiecesBoxes.clear();
      this.startPiecesMap.clear();
   }
}