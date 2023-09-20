package net.minecraft.world.lighting;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.IChunkLightProvider;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableInt;

public final class SkyLightEngine extends LightEngine<SkyLightStorage.StorageMap, SkyLightStorage> {
   private static final Direction[] DIRECTIONS = Direction.values();
   private static final Direction[] HORIZONTALS = new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

   public SkyLightEngine(IChunkLightProvider p_i51289_1_) {
      super(p_i51289_1_, LightType.SKY, new SkyLightStorage(p_i51289_1_));
   }

   /**
    * Returns level propagated from start position with specified level to the neighboring end position.
    */
   protected int computeLevelFromNeighbor(long pStartPos, long pEndPos, int pStartLevel) {
      if (pEndPos == Long.MAX_VALUE) {
         return 15;
      } else {
         if (pStartPos == Long.MAX_VALUE) {
            if (!this.storage.hasLightSource(pEndPos)) {
               return 15;
            }

            pStartLevel = 0;
         }

         if (pStartLevel >= 15) {
            return pStartLevel;
         } else {
            MutableInt mutableint = new MutableInt();
            BlockState blockstate = this.getStateAndOpacity(pEndPos, mutableint);
            if (mutableint.getValue() >= 15) {
               return 15;
            } else {
               int i = BlockPos.getX(pStartPos);
               int j = BlockPos.getY(pStartPos);
               int k = BlockPos.getZ(pStartPos);
               int l = BlockPos.getX(pEndPos);
               int i1 = BlockPos.getY(pEndPos);
               int j1 = BlockPos.getZ(pEndPos);
               boolean flag = i == l && k == j1;
               int k1 = Integer.signum(l - i);
               int l1 = Integer.signum(i1 - j);
               int i2 = Integer.signum(j1 - k);
               Direction direction;
               if (pStartPos == Long.MAX_VALUE) {
                  direction = Direction.DOWN;
               } else {
                  direction = Direction.fromNormal(k1, l1, i2);
               }

               BlockState blockstate1 = this.getStateAndOpacity(pStartPos, (MutableInt)null);
               if (direction != null) {
                  VoxelShape voxelshape = this.getShape(blockstate1, pStartPos, direction);
                  VoxelShape voxelshape1 = this.getShape(blockstate, pEndPos, direction.getOpposite());
                  if (VoxelShapes.faceShapeOccludes(voxelshape, voxelshape1)) {
                     return 15;
                  }
               } else {
                  VoxelShape voxelshape3 = this.getShape(blockstate1, pStartPos, Direction.DOWN);
                  if (VoxelShapes.faceShapeOccludes(voxelshape3, VoxelShapes.empty())) {
                     return 15;
                  }

                  int j2 = flag ? -1 : 0;
                  Direction direction1 = Direction.fromNormal(k1, j2, i2);
                  if (direction1 == null) {
                     return 15;
                  }

                  VoxelShape voxelshape2 = this.getShape(blockstate, pEndPos, direction1.getOpposite());
                  if (VoxelShapes.faceShapeOccludes(VoxelShapes.empty(), voxelshape2)) {
                     return 15;
                  }
               }

               boolean flag1 = pStartPos == Long.MAX_VALUE || flag && j > i1;
               return flag1 && pStartLevel == 0 && mutableint.getValue() == 0 ? 0 : pStartLevel + Math.max(1, mutableint.getValue());
            }
         }
      }
   }

   protected void checkNeighborsAfterUpdate(long pPos, int pLevel, boolean pIsDecreasing) {
      long i = SectionPos.blockToSection(pPos);
      int j = BlockPos.getY(pPos);
      int k = SectionPos.sectionRelative(j);
      int l = SectionPos.blockToSectionCoord(j);
      int i1;
      if (k != 0) {
         i1 = 0;
      } else {
         int j1;
         for(j1 = 0; !this.storage.storingLightForSection(SectionPos.offset(i, 0, -j1 - 1, 0)) && this.storage.hasSectionsBelow(l - j1 - 1); ++j1) {
         }

         i1 = j1;
      }

      long i3 = BlockPos.offset(pPos, 0, -1 - i1 * 16, 0);
      long k1 = SectionPos.blockToSection(i3);
      if (i == k1 || this.storage.storingLightForSection(k1)) {
         this.checkNeighbor(pPos, i3, pLevel, pIsDecreasing);
      }

      long l1 = BlockPos.offset(pPos, Direction.UP);
      long i2 = SectionPos.blockToSection(l1);
      if (i == i2 || this.storage.storingLightForSection(i2)) {
         this.checkNeighbor(pPos, l1, pLevel, pIsDecreasing);
      }

      for(Direction direction : HORIZONTALS) {
         int j2 = 0;

         while(true) {
            long k2 = BlockPos.offset(pPos, direction.getStepX(), -j2, direction.getStepZ());
            long l2 = SectionPos.blockToSection(k2);
            if (i == l2) {
               this.checkNeighbor(pPos, k2, pLevel, pIsDecreasing);
               break;
            }

            if (this.storage.storingLightForSection(l2)) {
               this.checkNeighbor(pPos, k2, pLevel, pIsDecreasing);
            }

            ++j2;
            if (j2 > i1 * 16) {
               break;
            }
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
         long l = SectionPos.blockToSection(k);
         NibbleArray nibblearray1;
         if (j1 == l) {
            nibblearray1 = nibblearray;
         } else {
            nibblearray1 = this.storage.getDataLayer(l, true);
         }

         if (nibblearray1 != null) {
            if (k != pExcludedSourcePos) {
               int k1 = this.computeLevelFromNeighbor(k, pPos, this.getLevel(nibblearray1, k));
               if (i > k1) {
                  i = k1;
               }

               if (i == 0) {
                  return i;
               }
            }
         } else if (direction != Direction.DOWN) {
            for(k = BlockPos.getFlatIndex(k); !this.storage.storingLightForSection(l) && !this.storage.isAboveData(l); k = BlockPos.offset(k, 0, 16, 0)) {
               l = SectionPos.offset(l, Direction.UP);
            }

            NibbleArray nibblearray2 = this.storage.getDataLayer(l, true);
            if (k != pExcludedSourcePos) {
               int i1;
               if (nibblearray2 != null) {
                  i1 = this.computeLevelFromNeighbor(k, pPos, this.getLevel(nibblearray2, k));
               } else {
                  i1 = this.storage.lightOnInSection(l) ? 0 : 15;
               }

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

   protected void checkNode(long pLevelPos) {
      this.storage.runAllUpdates();
      long i = SectionPos.blockToSection(pLevelPos);
      if (this.storage.storingLightForSection(i)) {
         super.checkNode(pLevelPos);
      } else {
         for(pLevelPos = BlockPos.getFlatIndex(pLevelPos); !this.storage.storingLightForSection(i) && !this.storage.isAboveData(i); pLevelPos = BlockPos.offset(pLevelPos, 0, 16, 0)) {
            i = SectionPos.offset(i, Direction.UP);
         }

         if (this.storage.storingLightForSection(i)) {
            super.checkNode(pLevelPos);
         }
      }

   }

   @OnlyIn(Dist.CLIENT)
   public String getDebugData(long pSectionPos) {
      return super.getDebugData(pSectionPos) + (this.storage.isAboveData(pSectionPos) ? "*" : "");
   }

   @Override
   public int queuedUpdateSize() {
      return 0;
   }
}
