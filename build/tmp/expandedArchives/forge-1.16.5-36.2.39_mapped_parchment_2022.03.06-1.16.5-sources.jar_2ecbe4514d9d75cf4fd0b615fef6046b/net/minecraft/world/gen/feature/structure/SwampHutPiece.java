package net.minecraft.world.gen.feature.structure;

import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.WitchEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class SwampHutPiece extends ScatteredStructurePiece {
   private boolean spawnedWitch;
   private boolean spawnedCat;

   public SwampHutPiece(Random pRandom, int pX, int pZ) {
      super(IStructurePieceType.SWAMPLAND_HUT, pRandom, pX, 64, pZ, 7, 7, 9);
   }

   public SwampHutPiece(TemplateManager p_i51340_1_, CompoundNBT p_i51340_2_) {
      super(IStructurePieceType.SWAMPLAND_HUT, p_i51340_2_);
      this.spawnedWitch = p_i51340_2_.getBoolean("Witch");
      this.spawnedCat = p_i51340_2_.getBoolean("Cat");
   }

   protected void addAdditionalSaveData(CompoundNBT p_143011_1_) {
      super.addAdditionalSaveData(p_143011_1_);
      p_143011_1_.putBoolean("Witch", this.spawnedWitch);
      p_143011_1_.putBoolean("Cat", this.spawnedCat);
   }

   public boolean postProcess(ISeedReader pLevel, StructureManager pStructureManager, ChunkGenerator pChunkGenerator, Random pRandom, MutableBoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
      if (!this.updateAverageGroundHeight(pLevel, pBox, 0)) {
         return false;
      } else {
         this.generateBox(pLevel, pBox, 1, 1, 1, 5, 1, 7, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 4, 2, 5, 4, 7, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 2, 1, 0, 4, 1, 0, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 2, 2, 2, 3, 3, 2, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 2, 3, 1, 3, 6, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 5, 2, 3, 5, 3, 6, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 2, 2, 7, 4, 3, 7, Blocks.SPRUCE_PLANKS.defaultBlockState(), Blocks.SPRUCE_PLANKS.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 0, 2, 1, 3, 2, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 5, 0, 2, 5, 3, 2, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 0, 7, 1, 3, 7, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 5, 0, 7, 5, 3, 7, Blocks.OAK_LOG.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), false);
         this.placeBlock(pLevel, Blocks.OAK_FENCE.defaultBlockState(), 2, 3, 2, pBox);
         this.placeBlock(pLevel, Blocks.OAK_FENCE.defaultBlockState(), 3, 3, 7, pBox);
         this.placeBlock(pLevel, Blocks.AIR.defaultBlockState(), 1, 3, 4, pBox);
         this.placeBlock(pLevel, Blocks.AIR.defaultBlockState(), 5, 3, 4, pBox);
         this.placeBlock(pLevel, Blocks.AIR.defaultBlockState(), 5, 3, 5, pBox);
         this.placeBlock(pLevel, Blocks.POTTED_RED_MUSHROOM.defaultBlockState(), 1, 3, 5, pBox);
         this.placeBlock(pLevel, Blocks.CRAFTING_TABLE.defaultBlockState(), 3, 2, 6, pBox);
         this.placeBlock(pLevel, Blocks.CAULDRON.defaultBlockState(), 4, 2, 6, pBox);
         this.placeBlock(pLevel, Blocks.OAK_FENCE.defaultBlockState(), 1, 2, 1, pBox);
         this.placeBlock(pLevel, Blocks.OAK_FENCE.defaultBlockState(), 5, 2, 1, pBox);
         BlockState blockstate = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairsBlock.FACING, Direction.NORTH);
         BlockState blockstate1 = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairsBlock.FACING, Direction.EAST);
         BlockState blockstate2 = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairsBlock.FACING, Direction.WEST);
         BlockState blockstate3 = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairsBlock.FACING, Direction.SOUTH);
         this.generateBox(pLevel, pBox, 0, 4, 1, 6, 4, 1, blockstate, blockstate, false);
         this.generateBox(pLevel, pBox, 0, 4, 2, 0, 4, 7, blockstate1, blockstate1, false);
         this.generateBox(pLevel, pBox, 6, 4, 2, 6, 4, 7, blockstate2, blockstate2, false);
         this.generateBox(pLevel, pBox, 0, 4, 8, 6, 4, 8, blockstate3, blockstate3, false);
         this.placeBlock(pLevel, blockstate.setValue(StairsBlock.SHAPE, StairsShape.OUTER_RIGHT), 0, 4, 1, pBox);
         this.placeBlock(pLevel, blockstate.setValue(StairsBlock.SHAPE, StairsShape.OUTER_LEFT), 6, 4, 1, pBox);
         this.placeBlock(pLevel, blockstate3.setValue(StairsBlock.SHAPE, StairsShape.OUTER_LEFT), 0, 4, 8, pBox);
         this.placeBlock(pLevel, blockstate3.setValue(StairsBlock.SHAPE, StairsShape.OUTER_RIGHT), 6, 4, 8, pBox);

         for(int i = 2; i <= 7; i += 5) {
            for(int j = 1; j <= 5; j += 4) {
               this.fillColumnDown(pLevel, Blocks.OAK_LOG.defaultBlockState(), j, -1, i, pBox);
            }
         }

         if (!this.spawnedWitch) {
            int l = this.getWorldX(2, 5);
            int i1 = this.getWorldY(2);
            int k = this.getWorldZ(2, 5);
            if (pBox.isInside(new BlockPos(l, i1, k))) {
               this.spawnedWitch = true;
               WitchEntity witchentity = EntityType.WITCH.create(pLevel.getLevel());
               witchentity.setPersistenceRequired();
               witchentity.moveTo((double)l + 0.5D, (double)i1, (double)k + 0.5D, 0.0F, 0.0F);
               witchentity.finalizeSpawn(pLevel, pLevel.getCurrentDifficultyAt(new BlockPos(l, i1, k)), SpawnReason.STRUCTURE, (ILivingEntityData)null, (CompoundNBT)null);
               pLevel.addFreshEntityWithPassengers(witchentity);
            }
         }

         this.spawnCat(pLevel, pBox);
         return true;
      }
   }

   private void spawnCat(IServerWorld pLevel, MutableBoundingBox pBox) {
      if (!this.spawnedCat) {
         int i = this.getWorldX(2, 5);
         int j = this.getWorldY(2);
         int k = this.getWorldZ(2, 5);
         if (pBox.isInside(new BlockPos(i, j, k))) {
            this.spawnedCat = true;
            CatEntity catentity = EntityType.CAT.create(pLevel.getLevel());
            catentity.setPersistenceRequired();
            catentity.moveTo((double)i + 0.5D, (double)j, (double)k + 0.5D, 0.0F, 0.0F);
            catentity.finalizeSpawn(pLevel, pLevel.getCurrentDifficultyAt(new BlockPos(i, j, k)), SpawnReason.STRUCTURE, (ILivingEntityData)null, (CompoundNBT)null);
            pLevel.addFreshEntityWithPassengers(catentity);
         }
      }

   }
}