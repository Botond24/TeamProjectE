package net.minecraft.world.lighting;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.IChunkLightProvider;
import net.minecraft.world.chunk.NibbleArray;
import org.apache.commons.lang3.mutable.MutableInt;

public final class BlockLightEngine extends LightEngine<BlockLightStorage.StorageMap, BlockLightStorage> {
   private static final Direction[] DIRECTIONS = Direction.values();
   private final BlockPos.Mutable pos = new BlockPos.Mutable();

   public BlockLightEngine(IChunkLightProvider p_i51301_1_) {
      super(p_i51301_1_, LightType.BLOCK, new BlockLightStorage(p_i51301_1_));
   }

   private int getLightEmission(long pLevelPos) {
      int i = BlockPos.getX(pLevelPos);
      int j = BlockPos.getY(pLevelPos);
      int k = BlockPos.getZ(pLevelPos);
      IBlockReader iblockreader = this.chunkSource.getChunkForLighting(i >> 4, k >> 4);
      return iblockreader != null ? iblockreader.getLightEmission(this.pos.set(i, j, k)) : 0;
   }

   /**
    * Returns level propagated from start position with specified level to the neighboring end position.
    */
   protected int computeLevelFromNeighbor(long pStartPos, long pEndPos, int pStartLevel) {
      if (pEndPos == Long.MAX_VALUE) {
         return 15;
      } else if (pStartPos == Long.MAX_VALUE) {
         return pStartLevel + 15 - this.getLightEmission(pEndPos);
      } else if (pStartLevel >= 15) {
         return pStartLevel;
      } else {
         int i = Integer.signum(BlockPos.getX(pEndPos) - BlockPos.getX(pStartPos));
         int j = Integer.signum(BlockPos.getY(pEndPos) - BlockPos.getY(pStartPos));
         int k = Integer.signum(BlockPos.getZ(pEndPos) - BlockPos.getZ(pStartPos));
         Direction direction = Direction.fromNormal(i, j, k);
         if (direction == null) {
            return 15;
         } else {
            MutableInt mutableint = new MutableInt();
            BlockState blockstate = this.getStateAndOpacity(pEndPos, mutableint);
            if (mutableint.getValue() >= 15) {
               return 15;
            } else {
               BlockState blockstate1 = this.getStateAndOpacity(pStartPos, (MutableInt)null);
               VoxelShape voxelshape = this.getShape(blockstate1, pStartPos, direction);
               VoxelShape voxelshape1 = this.getShape(blockstate, pEndPos, direction.getOpposite());
               return VoxelShapes.faceShapeOccludes(voxelshape, voxelshape1) ? 15 : pStartLevel + Math.max(1, mutableint.getValue());
            }
         }
      }
   }

   protected void checkNeighborsAfterUpdate(long pPos, int pLevel, boolean pIsDecreasing) {
      long i = SectionPos.blockToSection(pPos);

      for(Direction direction : DIRECTIONS) {
         long j = BlockPos.offset(pPos, direction);
         long k = SectionPos.blockToSection(j);
         if (i == k || this.storage.storingLightForSection(k)) {
            this.checkNeighbor(pPos, j, pLevel, pIsDecreasing);
         }
      }

   }

   /**
    * Computes level propagated from neighbors of specified position with given existing level, excluding the given
    * source position.
    */
   protected int getComputedLevel(long pPos, long pExcludedSourcePos, int pLevel) {
      int i = pLevel;
      if (Long.MAX_VALUE != pExcludedSourcePos) {
         int j = this.computeLevelFromNeighbor(Long.MAX_VALUE, pPos, 0);
         if (pLevel > j) {
            i = j;
         }

         if (i == 0) {
            return i;
         }
      }

      long j1 = SectionPos.blockToSection(pPos);
      NibbleArray nibblearray = this.storage.getDataLayer(j1, true);

      for(Direction direction : DIRECTIONS) {
         long k = BlockPos.offset(pPos, direction);
         if (k != pExcludedSourcePos) {
            long l = SectionPos.blockToSection(k);
            NibbleArray nibblearray1;
            if (j1 == l) {
               nibblearray1 = nibblearray;
            } else {
               nibblearray1 = this.storage.getDataLayer(l, true);
            }

            if (nibblearray1 != null) {
               int i1 = this.computeLevelFromNeighbor(k, pPos, this.getLevel(nibblearray1, k));
               if (i > i1) {
                  i = i1;
               }

               if (i == 0) {
                  return i;
               }
            }
         }
      }

      return i;
   }

   public void onBlockEmissionIncrease(BlockPos p_215623_1_, int p_215623_2_) {
      this.storage.runAllUpdates();
      this.checkEdge(Long.MAX_VALUE, p_215623_1_.asLong(), 15 - p_215623_2_, true);
   }

   @Override
   public int queuedUpdateSize() {
      return storage.queuedUpdateSize();
   }
}
