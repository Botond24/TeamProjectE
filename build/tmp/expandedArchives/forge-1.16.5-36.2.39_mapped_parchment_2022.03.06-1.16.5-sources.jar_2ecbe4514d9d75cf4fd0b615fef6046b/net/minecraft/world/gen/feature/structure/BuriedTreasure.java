package net.minecraft.world.gen.feature.structure;

import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class BuriedTreasure {
   public static class Piece extends StructurePiece {
      public Piece(BlockPos pPos) {
         super(IStructurePieceType.BURIED_TREASURE_PIECE, 0);
         this.boundingBox = new MutableBoundingBox(pPos.getX(), pPos.getY(), pPos.getZ(), pPos.getX(), pPos.getY(), pPos.getZ());
      }

      public Piece(TemplateManager p_i50677_1_, CompoundNBT p_i50677_2_) {
         super(IStructurePieceType.BURIED_TREASURE_PIECE, p_i50677_2_);
      }

      protected void addAdditionalSaveData(CompoundNBT p_143011_1_) {
      }

      public boolean postProcess(ISeedReader pLevel, StructureManager pStructureManager, ChunkGenerator pChunkGenerator, Random pRandom, MutableBoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         int i = pLevel.getHeight(Heightmap.Type.OCEAN_FLOOR_WG, this.boundingBox.x0, this.boundingBox.z0);
         BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable(this.boundingBox.x0, i, this.boundingBox.z0);

         while(blockpos$mutable.getY() > 0) {
            BlockState blockstate = pLevel.getBlockState(blockpos$mutable);
            BlockState blockstate1 = pLevel.getBlockState(blockpos$mutable.below());
            if (blockstate1 == Blocks.SANDSTONE.defaultBlockState() || blockstate1 == Blocks.STONE.defaultBlockState() || blockstate1 == Blocks.ANDESITE.defaultBlockState() || blockstate1 == Blocks.GRANITE.defaultBlockState() || blockstate1 == Blocks.DIORITE.defaultBlockState()) {
               BlockState blockstate2 = !blockstate.isAir() && !this.isLiquid(blockstate) ? blockstate : Blocks.SAND.defaultBlockState();

               for(Direction direction : Direction.values()) {
                  BlockPos blockpos = blockpos$mutable.relative(direction);
                  BlockState blockstate3 = pLevel.getBlockState(blockpos);
                  if (blockstate3.isAir() || this.isLiquid(blockstate3)) {
                     BlockPos blockpos1 = blockpos.below();
                     BlockState blockstate4 = pLevel.getBlockState(blockpos1);
                     if ((blockstate4.isAir() || this.isLiquid(blockstate4)) && direction != Direction.UP) {
                        pLevel.setBlock(blockpos, blockstate1, 3);
                     } else {
                        pLevel.setBlock(blockpos, blockstate2, 3);
                     }
                  }
               }

               this.boundingBox = new MutableBoundingBox(blockpos$mutable.getX(), blockpos$mutable.getY(), blockpos$mutable.getZ(), blockpos$mutable.getX(), blockpos$mutable.getY(), blockpos$mutable.getZ());
               return this.createChest(pLevel, pBox, pRandom, blockpos$mutable, LootTables.BURIED_TREASURE, (BlockState)null);
            }

            blockpos$mutable.move(0, -1, 0);
         }

         return false;
      }

      private boolean isLiquid(BlockState pState) {
         return pState == Blocks.WATER.defaultBlockState() || pState == Blocks.LAVA.defaultBlockState();
      }
   }
}