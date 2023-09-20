package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NeighborsUpdateDebugRenderer implements DebugRenderer.IDebugRenderer {
   private final Minecraft minecraft;
   private final Map<Long, Map<BlockPos, Integer>> lastUpdate = Maps.newTreeMap(Ordering.natural().reverse());

   NeighborsUpdateDebugRenderer(Minecraft p_i47365_1_) {
      this.minecraft = p_i47365_1_;
   }

   public void addUpdate(long pLevelTime, BlockPos pPos) {
      Map<BlockPos, Integer> map = this.lastUpdate.computeIfAbsent(pLevelTime, (p_241730_0_) -> {
         return Maps.newHashMap();
      });
      int i = map.getOrDefault(pPos, 0);
      map.put(pPos, i + 1);
   }

   public void render(MatrixStack pMatrixStack, IRenderTypeBuffer pBuffer, double pCamX, double pCamY, double pCamZ) {
      long i = this.minecraft.level.getGameTime();
      int j = 200;
      double d0 = 0.0025D;
      Set<BlockPos> set = Sets.newHashSet();
      Map<BlockPos, Integer> map = Maps.newHashMap();
      IVertexBuilder ivertexbuilder = pBuffer.getBuffer(RenderType.lines());
      Iterator<Entry<Long, Map<BlockPos, Integer>>> iterator = this.lastUpdate.entrySet().iterator();

      while(iterator.hasNext()) {
         Entry<Long, Map<BlockPos, Integer>> entry = iterator.next();
         Long olong = entry.getKey();
         Map<BlockPos, Integer> map1 = entry.getValue();
         long k = i - olong;
         if (k > 200L) {
            iterator.remove();
         } else {
            for(Entry<BlockPos, Integer> entry1 : map1.entrySet()) {
               BlockPos blockpos = entry1.getKey();
               Integer integer = entry1.getValue();
               if (set.add(blockpos)) {
                  AxisAlignedBB axisalignedbb = (new AxisAlignedBB(BlockPos.ZERO)).inflate(0.002D).deflate(0.0025D * (double)k).move((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ()).move(-pCamX, -pCamY, -pCamZ);
                  WorldRenderer.renderLineBox(pMatrixStack, ivertexbuilder, axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ, 1.0F, 1.0F, 1.0F, 1.0F);
                  map.put(blockpos, integer);
               }
            }
         }
      }

      for(Entry<BlockPos, Integer> entry2 : map.entrySet()) {
         BlockPos blockpos1 = entry2.getKey();
         Integer integer1 = entry2.getValue();
         DebugRenderer.renderFloatingText(String.valueOf((Object)integer1), blockpos1.getX(), blockpos1.getY(), blockpos1.getZ(), -1);
      }

   }
}