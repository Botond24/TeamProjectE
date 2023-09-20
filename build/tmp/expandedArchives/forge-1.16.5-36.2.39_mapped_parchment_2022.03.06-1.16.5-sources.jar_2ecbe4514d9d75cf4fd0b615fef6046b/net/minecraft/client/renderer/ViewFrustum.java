package net.minecraft.client.renderer;

import javax.annotation.Nullable;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ViewFrustum {
   protected final WorldRenderer levelRenderer;
   protected final World level;
   protected int chunkGridSizeY;
   protected int chunkGridSizeX;
   protected int chunkGridSizeZ;
   public ChunkRenderDispatcher.ChunkRender[] chunks;

   public ViewFrustum(ChunkRenderDispatcher p_i226000_1_, World pLevel, int p_i226000_3_, WorldRenderer pLevelRenderer) {
      this.levelRenderer = pLevelRenderer;
      this.level = pLevel;
      this.setViewDistance(p_i226000_3_);
      this.createChunks(p_i226000_1_);
   }

   protected void createChunks(ChunkRenderDispatcher pRenderChunkFactory) {
      int i = this.chunkGridSizeX * this.chunkGridSizeY * this.chunkGridSizeZ;
      this.chunks = new ChunkRenderDispatcher.ChunkRender[i];

      for(int j = 0; j < this.chunkGridSizeX; ++j) {
         for(int k = 0; k < this.chunkGridSizeY; ++k) {
            for(int l = 0; l < this.chunkGridSizeZ; ++l) {
               int i1 = this.getChunkIndex(j, k, l);
               this.chunks[i1] = pRenderChunkFactory.new ChunkRender();
               this.chunks[i1].setOrigin(j * 16, k * 16, l * 16);
            }
         }
      }

   }

   public void releaseAllBuffers() {
      for(ChunkRenderDispatcher.ChunkRender chunkrenderdispatcher$chunkrender : this.chunks) {
         chunkrenderdispatcher$chunkrender.releaseBuffers();
      }

   }

   private int getChunkIndex(int pX, int pY, int pZ) {
      return (pZ * this.chunkGridSizeY + pY) * this.chunkGridSizeX + pX;
   }

   protected void setViewDistance(int pRenderDistanceChunks) {
      int i = pRenderDistanceChunks * 2 + 1;
      this.chunkGridSizeX = i;
      this.chunkGridSizeY = 16;
      this.chunkGridSizeZ = i;
   }

   public void repositionCamera(double pViewEntityX, double pViewEntityZ) {
      int i = MathHelper.floor(pViewEntityX);
      int j = MathHelper.floor(pViewEntityZ);

      for(int k = 0; k < this.chunkGridSizeX; ++k) {
         int l = this.chunkGridSizeX * 16;
         int i1 = i - 8 - l / 2;
         int j1 = i1 + Math.floorMod(k * 16 - i1, l);

         for(int k1 = 0; k1 < this.chunkGridSizeZ; ++k1) {
            int l1 = this.chunkGridSizeZ * 16;
            int i2 = j - 8 - l1 / 2;
            int j2 = i2 + Math.floorMod(k1 * 16 - i2, l1);

            for(int k2 = 0; k2 < this.chunkGridSizeY; ++k2) {
               int l2 = k2 * 16;
               ChunkRenderDispatcher.ChunkRender chunkrenderdispatcher$chunkrender = this.chunks[this.getChunkIndex(k, k2, k1)];
               chunkrenderdispatcher$chunkrender.setOrigin(j1, l2, j2);
            }
         }
      }

   }

   public void setDirty(int pSectionX, int pSectionY, int pSectionZ, boolean pRerenderOnMainThread) {
      int i = Math.floorMod(pSectionX, this.chunkGridSizeX);
      int j = Math.floorMod(pSectionY, this.chunkGridSizeY);
      int k = Math.floorMod(pSectionZ, this.chunkGridSizeZ);
      ChunkRenderDispatcher.ChunkRender chunkrenderdispatcher$chunkrender = this.chunks[this.getChunkIndex(i, j, k)];
      chunkrenderdispatcher$chunkrender.setDirty(pRerenderOnMainThread);
   }

   @Nullable
   protected ChunkRenderDispatcher.ChunkRender getRenderChunkAt(BlockPos pPos) {
      int i = MathHelper.intFloorDiv(pPos.getX(), 16);
      int j = MathHelper.intFloorDiv(pPos.getY(), 16);
      int k = MathHelper.intFloorDiv(pPos.getZ(), 16);
      if (j >= 0 && j < this.chunkGridSizeY) {
         i = MathHelper.positiveModulo(i, this.chunkGridSizeX);
         k = MathHelper.positiveModulo(k, this.chunkGridSizeZ);
         return this.chunks[this.getChunkIndex(i, j, k)];
      } else {
         return null;
      }
   }
}