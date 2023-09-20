package net.minecraft.client.renderer.chunk;

import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChunkRenderCache implements IBlockDisplayReader {
   protected final int centerX;
   protected final int centerZ;
   protected final BlockPos start;
   protected final int xLength;
   protected final int yLength;
   protected final int zLength;
   protected final Chunk[][] chunks;
   protected final BlockState[] blockStates;
   protected final FluidState[] fluidStates;
   protected final World level;

   /**
    * generates a RenderChunkCache, but returns null if the chunk is empty (contains only air)
    */
   @Nullable
   public static ChunkRenderCache createIfNotEmpty(World pLevel, BlockPos pFrom, BlockPos pTo, int pPadding) {
      int i = pFrom.getX() - pPadding >> 4;
      int j = pFrom.getZ() - pPadding >> 4;
      int k = pTo.getX() + pPadding >> 4;
      int l = pTo.getZ() + pPadding >> 4;
      Chunk[][] achunk = new Chunk[k - i + 1][l - j + 1];

      for(int i1 = i; i1 <= k; ++i1) {
         for(int j1 = j; j1 <= l; ++j1) {
            achunk[i1 - i][j1 - j] = pLevel.getChunk(i1, j1);
         }
      }

      if (isAllEmpty(pFrom, pTo, i, j, achunk)) {
         return null;
      } else {
         int k1 = 1;
         BlockPos blockpos1 = pFrom.offset(-1, -1, -1);
         BlockPos blockpos = pTo.offset(1, 1, 1);
         return new ChunkRenderCache(pLevel, i, j, achunk, blockpos1, blockpos);
      }
   }

   public static boolean isAllEmpty(BlockPos p_241718_0_, BlockPos p_241718_1_, int p_241718_2_, int p_241718_3_, Chunk[][] p_241718_4_) {
      for(int i = p_241718_0_.getX() >> 4; i <= p_241718_1_.getX() >> 4; ++i) {
         for(int j = p_241718_0_.getZ() >> 4; j <= p_241718_1_.getZ() >> 4; ++j) {
            Chunk chunk = p_241718_4_[i - p_241718_2_][j - p_241718_3_];
            if (!chunk.isYSpaceEmpty(p_241718_0_.getY(), p_241718_1_.getY())) {
               return false;
            }
         }
      }

      return true;
   }

   public ChunkRenderCache(World p_i49840_1_, int p_i49840_2_, int p_i49840_3_, Chunk[][] p_i49840_4_, BlockPos p_i49840_5_, BlockPos p_i49840_6_) {
      this.level = p_i49840_1_;
      this.centerX = p_i49840_2_;
      this.centerZ = p_i49840_3_;
      this.chunks = p_i49840_4_;
      this.start = p_i49840_5_;
      this.xLength = p_i49840_6_.getX() - p_i49840_5_.getX() + 1;
      this.yLength = p_i49840_6_.getY() - p_i49840_5_.getY() + 1;
      this.zLength = p_i49840_6_.getZ() - p_i49840_5_.getZ() + 1;
      this.blockStates = new BlockState[this.xLength * this.yLength * this.zLength];
      this.fluidStates = new FluidState[this.xLength * this.yLength * this.zLength];

      for(BlockPos blockpos : BlockPos.betweenClosed(p_i49840_5_, p_i49840_6_)) {
         int i = (blockpos.getX() >> 4) - p_i49840_2_;
         int j = (blockpos.getZ() >> 4) - p_i49840_3_;
         Chunk chunk = p_i49840_4_[i][j];
         int k = this.index(blockpos);
         this.blockStates[k] = chunk.getBlockState(blockpos);
         this.fluidStates[k] = chunk.getFluidState(blockpos);
      }

   }

   protected final int index(BlockPos pPos) {
      return this.index(pPos.getX(), pPos.getY(), pPos.getZ());
   }

   protected int index(int pX, int pY, int pZ) {
      int i = pX - this.start.getX();
      int j = pY - this.start.getY();
      int k = pZ - this.start.getZ();
      return k * this.xLength * this.yLength + j * this.xLength + i;
   }

   public BlockState getBlockState(BlockPos pPos) {
      return this.blockStates[this.index(pPos)];
   }

   public FluidState getFluidState(BlockPos pPos) {
      return this.fluidStates[this.index(pPos)];
   }

   public float getShade(Direction pDirection, boolean pIsShade) {
      return this.level.getShade(pDirection, pIsShade);
   }

   public WorldLightManager getLightEngine() {
      return this.level.getLightEngine();
   }

   @Nullable
   public TileEntity getBlockEntity(BlockPos pPos) {
      return this.getBlockEntity(pPos, Chunk.CreateEntityType.IMMEDIATE);
   }

   @Nullable
   public TileEntity getBlockEntity(BlockPos pPos, Chunk.CreateEntityType pCreationType) {
      int i = (pPos.getX() >> 4) - this.centerX;
      int j = (pPos.getZ() >> 4) - this.centerZ;
      return this.chunks[i][j].getBlockEntity(pPos, pCreationType);
   }

   public int getBlockTint(BlockPos pBlockPos, ColorResolver pColorResolver) {
      return this.level.getBlockTint(pBlockPos, pColorResolver);
   }
}