package net.minecraft.world.gen.feature.structure;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.state.properties.SlabType;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class StrongholdPieces {
   private static final StrongholdPieces.PieceWeight[] STRONGHOLD_PIECE_WEIGHTS = new StrongholdPieces.PieceWeight[]{new StrongholdPieces.PieceWeight(StrongholdPieces.Straight.class, 40, 0), new StrongholdPieces.PieceWeight(StrongholdPieces.Prison.class, 5, 5), new StrongholdPieces.PieceWeight(StrongholdPieces.LeftTurn.class, 20, 0), new StrongholdPieces.PieceWeight(StrongholdPieces.RightTurn.class, 20, 0), new StrongholdPieces.PieceWeight(StrongholdPieces.RoomCrossing.class, 10, 6), new StrongholdPieces.PieceWeight(StrongholdPieces.StairsStraight.class, 5, 5), new StrongholdPieces.PieceWeight(StrongholdPieces.Stairs.class, 5, 5), new StrongholdPieces.PieceWeight(StrongholdPieces.Crossing.class, 5, 4), new StrongholdPieces.PieceWeight(StrongholdPieces.ChestCorridor.class, 5, 4), new StrongholdPieces.PieceWeight(StrongholdPieces.Library.class, 10, 2) {
      public boolean doPlace(int p_75189_1_) {
         return super.doPlace(p_75189_1_) && p_75189_1_ > 4;
      }
   }, new StrongholdPieces.PieceWeight(StrongholdPieces.PortalRoom.class, 20, 1) {
      public boolean doPlace(int p_75189_1_) {
         return super.doPlace(p_75189_1_) && p_75189_1_ > 5;
      }
   }};
   private static List<StrongholdPieces.PieceWeight> currentPieces;
   private static Class<? extends StrongholdPieces.Stronghold> imposedPiece;
   private static int totalWeight;
   private static final StrongholdPieces.Stones SMOOTH_STONE_SELECTOR = new StrongholdPieces.Stones();

   /**
    * sets up Arrays with the Structure pieces and their weights
    */
   public static void resetPieces() {
      currentPieces = Lists.newArrayList();

      for(StrongholdPieces.PieceWeight strongholdpieces$pieceweight : STRONGHOLD_PIECE_WEIGHTS) {
         strongholdpieces$pieceweight.placeCount = 0;
         currentPieces.add(strongholdpieces$pieceweight);
      }

      imposedPiece = null;
   }

   private static boolean updatePieceWeight() {
      boolean flag = false;
      totalWeight = 0;

      for(StrongholdPieces.PieceWeight strongholdpieces$pieceweight : currentPieces) {
         if (strongholdpieces$pieceweight.maxPlaceCount > 0 && strongholdpieces$pieceweight.placeCount < strongholdpieces$pieceweight.maxPlaceCount) {
            flag = true;
         }

         totalWeight += strongholdpieces$pieceweight.weight;
      }

      return flag;
   }

   private static StrongholdPieces.Stronghold findAndCreatePieceFactory(Class<? extends StrongholdPieces.Stronghold> p_175954_0_, List<StructurePiece> p_175954_1_, Random p_175954_2_, int p_175954_3_, int p_175954_4_, int p_175954_5_, @Nullable Direction p_175954_6_, int p_175954_7_) {
      StrongholdPieces.Stronghold strongholdpieces$stronghold = null;
      if (p_175954_0_ == StrongholdPieces.Straight.class) {
         strongholdpieces$stronghold = StrongholdPieces.Straight.createPiece(p_175954_1_, p_175954_2_, p_175954_3_, p_175954_4_, p_175954_5_, p_175954_6_, p_175954_7_);
      } else if (p_175954_0_ == StrongholdPieces.Prison.class) {
         strongholdpieces$stronghold = StrongholdPieces.Prison.createPiece(p_175954_1_, p_175954_2_, p_175954_3_, p_175954_4_, p_175954_5_, p_175954_6_, p_175954_7_);
      } else if (p_175954_0_ == StrongholdPieces.LeftTurn.class) {
         strongholdpieces$stronghold = StrongholdPieces.LeftTurn.createPiece(p_175954_1_, p_175954_2_, p_175954_3_, p_175954_4_, p_175954_5_, p_175954_6_, p_175954_7_);
      } else if (p_175954_0_ == StrongholdPieces.RightTurn.class) {
         strongholdpieces$stronghold = StrongholdPieces.RightTurn.createPiece(p_175954_1_, p_175954_2_, p_175954_3_, p_175954_4_, p_175954_5_, p_175954_6_, p_175954_7_);
      } else if (p_175954_0_ == StrongholdPieces.RoomCrossing.class) {
         strongholdpieces$stronghold = StrongholdPieces.RoomCrossing.createPiece(p_175954_1_, p_175954_2_, p_175954_3_, p_175954_4_, p_175954_5_, p_175954_6_, p_175954_7_);
      } else if (p_175954_0_ == StrongholdPieces.StairsStraight.class) {
         strongholdpieces$stronghold = StrongholdPieces.StairsStraight.createPiece(p_175954_1_, p_175954_2_, p_175954_3_, p_175954_4_, p_175954_5_, p_175954_6_, p_175954_7_);
      } else if (p_175954_0_ == StrongholdPieces.Stairs.class) {
         strongholdpieces$stronghold = StrongholdPieces.Stairs.createPiece(p_175954_1_, p_175954_2_, p_175954_3_, p_175954_4_, p_175954_5_, p_175954_6_, p_175954_7_);
      } else if (p_175954_0_ == StrongholdPieces.Crossing.class) {
         strongholdpieces$stronghold = StrongholdPieces.Crossing.createPiece(p_175954_1_, p_175954_2_, p_175954_3_, p_175954_4_, p_175954_5_, p_175954_6_, p_175954_7_);
      } else if (p_175954_0_ == StrongholdPieces.ChestCorridor.class) {
         strongholdpieces$stronghold = StrongholdPieces.ChestCorridor.createPiece(p_175954_1_, p_175954_2_, p_175954_3_, p_175954_4_, p_175954_5_, p_175954_6_, p_175954_7_);
      } else if (p_175954_0_ == StrongholdPieces.Library.class) {
         strongholdpieces$stronghold = StrongholdPieces.Library.createPiece(p_175954_1_, p_175954_2_, p_175954_3_, p_175954_4_, p_175954_5_, p_175954_6_, p_175954_7_);
      } else if (p_175954_0_ == StrongholdPieces.PortalRoom.class) {
         strongholdpieces$stronghold = StrongholdPieces.PortalRoom.createPiece(p_175954_1_, p_175954_3_, p_175954_4_, p_175954_5_, p_175954_6_, p_175954_7_);
      }

      return strongholdpieces$stronghold;
   }

   private static StrongholdPieces.Stronghold generatePieceFromSmallDoor(StrongholdPieces.Stairs2 p_175955_0_, List<StructurePiece> p_175955_1_, Random p_175955_2_, int p_175955_3_, int p_175955_4_, int p_175955_5_, Direction p_175955_6_, int p_175955_7_) {
      if (!updatePieceWeight()) {
         return null;
      } else {
         if (imposedPiece != null) {
            StrongholdPieces.Stronghold strongholdpieces$stronghold = findAndCreatePieceFactory(imposedPiece, p_175955_1_, p_175955_2_, p_175955_3_, p_175955_4_, p_175955_5_, p_175955_6_, p_175955_7_);
            imposedPiece = null;
            if (strongholdpieces$stronghold != null) {
               return strongholdpieces$stronghold;
            }
         }

         int j = 0;

         while(j < 5) {
            ++j;
            int i = p_175955_2_.nextInt(totalWeight);

            for(StrongholdPieces.PieceWeight strongholdpieces$pieceweight : currentPieces) {
               i -= strongholdpieces$pieceweight.weight;
               if (i < 0) {
                  if (!strongholdpieces$pieceweight.doPlace(p_175955_7_) || strongholdpieces$pieceweight == p_175955_0_.previousPiece) {
                     break;
                  }

                  StrongholdPieces.Stronghold strongholdpieces$stronghold1 = findAndCreatePieceFactory(strongholdpieces$pieceweight.pieceClass, p_175955_1_, p_175955_2_, p_175955_3_, p_175955_4_, p_175955_5_, p_175955_6_, p_175955_7_);
                  if (strongholdpieces$stronghold1 != null) {
                     ++strongholdpieces$pieceweight.placeCount;
                     p_175955_0_.previousPiece = strongholdpieces$pieceweight;
                     if (!strongholdpieces$pieceweight.isValid()) {
                        currentPieces.remove(strongholdpieces$pieceweight);
                     }

                     return strongholdpieces$stronghold1;
                  }
               }
            }
         }

         MutableBoundingBox mutableboundingbox = StrongholdPieces.Corridor.findPieceBox(p_175955_1_, p_175955_2_, p_175955_3_, p_175955_4_, p_175955_5_, p_175955_6_);
         return mutableboundingbox != null && mutableboundingbox.y0 > 1 ? new StrongholdPieces.Corridor(p_175955_7_, mutableboundingbox, p_175955_6_) : null;
      }
   }

   private static StructurePiece generateAndAddPiece(StrongholdPieces.Stairs2 p_175953_0_, List<StructurePiece> p_175953_1_, Random p_175953_2_, int p_175953_3_, int p_175953_4_, int p_175953_5_, @Nullable Direction p_175953_6_, int p_175953_7_) {
      if (p_175953_7_ > 50) {
         return null;
      } else if (Math.abs(p_175953_3_ - p_175953_0_.getBoundingBox().x0) <= 112 && Math.abs(p_175953_5_ - p_175953_0_.getBoundingBox().z0) <= 112) {
         StructurePiece structurepiece = generatePieceFromSmallDoor(p_175953_0_, p_175953_1_, p_175953_2_, p_175953_3_, p_175953_4_, p_175953_5_, p_175953_6_, p_175953_7_ + 1);
         if (structurepiece != null) {
            p_175953_1_.add(structurepiece);
            p_175953_0_.pendingChildren.add(structurepiece);
         }

         return structurepiece;
      } else {
         return null;
      }
   }

   public static class ChestCorridor extends StrongholdPieces.Stronghold {
      private boolean hasPlacedChest;

      public ChestCorridor(int pGenDepth, Random pRandom, MutableBoundingBox pBox, Direction pOrientation) {
         super(IStructurePieceType.STRONGHOLD_CHEST_CORRIDOR, pGenDepth);
         this.setOrientation(pOrientation);
         this.entryDoor = this.randomSmallDoor(pRandom);
         this.boundingBox = pBox;
      }

      public ChestCorridor(TemplateManager p_i50140_1_, CompoundNBT p_i50140_2_) {
         super(IStructurePieceType.STRONGHOLD_CHEST_CORRIDOR, p_i50140_2_);
         this.hasPlacedChest = p_i50140_2_.getBoolean("Chest");
      }

      protected void addAdditionalSaveData(CompoundNBT p_143011_1_) {
         super.addAdditionalSaveData(p_143011_1_);
         p_143011_1_.putBoolean("Chest", this.hasPlacedChest);
      }

      public void addChildren(StructurePiece p_74861_1_, List<StructurePiece> p_74861_2_, Random p_74861_3_) {
         this.generateSmallDoorChildForward((StrongholdPieces.Stairs2)p_74861_1_, p_74861_2_, p_74861_3_, 1, 1);
      }

      public static StrongholdPieces.ChestCorridor createPiece(List<StructurePiece> p_175868_0_, Random p_175868_1_, int p_175868_2_, int p_175868_3_, int p_175868_4_, Direction p_175868_5_, int p_175868_6_) {
         MutableBoundingBox mutableboundingbox = MutableBoundingBox.orientBox(p_175868_2_, p_175868_3_, p_175868_4_, -1, -1, 0, 5, 5, 7, p_175868_5_);
         return isOkBox(mutableboundingbox) && StructurePiece.findCollisionPiece(p_175868_0_, mutableboundingbox) == null ? new StrongholdPieces.ChestCorridor(p_175868_6_, p_175868_1_, mutableboundingbox, p_175868_5_) : null;
      }

      public boolean postProcess(ISeedReader pLevel, StructureManager pStructureManager, ChunkGenerator pChunkGenerator, Random pRandom, MutableBoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         this.generateBox(pLevel, pBox, 0, 0, 0, 4, 4, 6, true, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor(pLevel, pRandom, pBox, this.entryDoor, 1, 1, 0);
         this.generateSmallDoor(pLevel, pRandom, pBox, StrongholdPieces.Stronghold.Door.OPENING, 1, 1, 6);
         this.generateBox(pLevel, pBox, 3, 1, 2, 3, 1, 4, Blocks.STONE_BRICKS.defaultBlockState(), Blocks.STONE_BRICKS.defaultBlockState(), false);
         this.placeBlock(pLevel, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 1, 1, pBox);
         this.placeBlock(pLevel, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 1, 5, pBox);
         this.placeBlock(pLevel, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 2, 2, pBox);
         this.placeBlock(pLevel, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 2, 4, pBox);

         for(int i = 2; i <= 4; ++i) {
            this.placeBlock(pLevel, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 2, 1, i, pBox);
         }

         if (!this.hasPlacedChest && pBox.isInside(new BlockPos(this.getWorldX(3, 3), this.getWorldY(2), this.getWorldZ(3, 3)))) {
            this.hasPlacedChest = true;
            this.createChest(pLevel, pBox, pRandom, 3, 2, 3, LootTables.STRONGHOLD_CORRIDOR);
         }

         return true;
      }
   }

   public static class Corridor extends StrongholdPieces.Stronghold {
      private final int steps;

      public Corridor(int pGenDepth, MutableBoundingBox pBox, Direction pOrientation) {
         super(IStructurePieceType.STRONGHOLD_FILLER_CORRIDOR, pGenDepth);
         this.setOrientation(pOrientation);
         this.boundingBox = pBox;
         this.steps = pOrientation != Direction.NORTH && pOrientation != Direction.SOUTH ? pBox.getXSpan() : pBox.getZSpan();
      }

      public Corridor(TemplateManager p_i50138_1_, CompoundNBT p_i50138_2_) {
         super(IStructurePieceType.STRONGHOLD_FILLER_CORRIDOR, p_i50138_2_);
         this.steps = p_i50138_2_.getInt("Steps");
      }

      protected void addAdditionalSaveData(CompoundNBT p_143011_1_) {
         super.addAdditionalSaveData(p_143011_1_);
         p_143011_1_.putInt("Steps", this.steps);
      }

      public static MutableBoundingBox findPieceBox(List<StructurePiece> p_175869_0_, Random p_175869_1_, int p_175869_2_, int p_175869_3_, int p_175869_4_, Direction p_175869_5_) {
         int i = 3;
         MutableBoundingBox mutableboundingbox = MutableBoundingBox.orientBox(p_175869_2_, p_175869_3_, p_175869_4_, -1, -1, 0, 5, 5, 4, p_175869_5_);
         StructurePiece structurepiece = StructurePiece.findCollisionPiece(p_175869_0_, mutableboundingbox);
         if (structurepiece == null) {
            return null;
         } else {
            if (structurepiece.getBoundingBox().y0 == mutableboundingbox.y0) {
               for(int j = 3; j >= 1; --j) {
                  mutableboundingbox = MutableBoundingBox.orientBox(p_175869_2_, p_175869_3_, p_175869_4_, -1, -1, 0, 5, 5, j - 1, p_175869_5_);
                  if (!structurepiece.getBoundingBox().intersects(mutableboundingbox)) {
                     return MutableBoundingBox.orientBox(p_175869_2_, p_175869_3_, p_175869_4_, -1, -1, 0, 5, 5, j, p_175869_5_);
                  }
               }
            }

            return null;
         }
      }

      public boolean postProcess(ISeedReader pLevel, StructureManager pStructureManager, ChunkGenerator pChunkGenerator, Random pRandom, MutableBoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         for(int i = 0; i < this.steps; ++i) {
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 0, 0, i, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 0, i, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 0, i, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 0, i, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 4, 0, i, pBox);

            for(int j = 1; j <= 3; ++j) {
               this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 0, j, i, pBox);
               this.placeBlock(pLevel, Blocks.CAVE_AIR.defaultBlockState(), 1, j, i, pBox);
               this.placeBlock(pLevel, Blocks.CAVE_AIR.defaultBlockState(), 2, j, i, pBox);
               this.placeBlock(pLevel, Blocks.CAVE_AIR.defaultBlockState(), 3, j, i, pBox);
               this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 4, j, i, pBox);
            }

            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 0, 4, i, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 4, i, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 4, i, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 4, i, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 4, 4, i, pBox);
         }

         return true;
      }
   }

   public static class Crossing extends StrongholdPieces.Stronghold {
      private final boolean leftLow;
      private final boolean leftHigh;
      private final boolean rightLow;
      private final boolean rightHigh;

      public Crossing(int pGenDepth, Random pRandom, MutableBoundingBox pBox, Direction pOrientation) {
         super(IStructurePieceType.STRONGHOLD_FIVE_CROSSING, pGenDepth);
         this.setOrientation(pOrientation);
         this.entryDoor = this.randomSmallDoor(pRandom);
         this.boundingBox = pBox;
         this.leftLow = pRandom.nextBoolean();
         this.leftHigh = pRandom.nextBoolean();
         this.rightLow = pRandom.nextBoolean();
         this.rightHigh = pRandom.nextInt(3) > 0;
      }

      public Crossing(TemplateManager p_i50136_1_, CompoundNBT p_i50136_2_) {
         super(IStructurePieceType.STRONGHOLD_FIVE_CROSSING, p_i50136_2_);
         this.leftLow = p_i50136_2_.getBoolean("leftLow");
         this.leftHigh = p_i50136_2_.getBoolean("leftHigh");
         this.rightLow = p_i50136_2_.getBoolean("rightLow");
         this.rightHigh = p_i50136_2_.getBoolean("rightHigh");
      }

      protected void addAdditionalSaveData(CompoundNBT p_143011_1_) {
         super.addAdditionalSaveData(p_143011_1_);
         p_143011_1_.putBoolean("leftLow", this.leftLow);
         p_143011_1_.putBoolean("leftHigh", this.leftHigh);
         p_143011_1_.putBoolean("rightLow", this.rightLow);
         p_143011_1_.putBoolean("rightHigh", this.rightHigh);
      }

      public void addChildren(StructurePiece p_74861_1_, List<StructurePiece> p_74861_2_, Random p_74861_3_) {
         int i = 3;
         int j = 5;
         Direction direction = this.getOrientation();
         if (direction == Direction.WEST || direction == Direction.NORTH) {
            i = 8 - i;
            j = 8 - j;
         }

         this.generateSmallDoorChildForward((StrongholdPieces.Stairs2)p_74861_1_, p_74861_2_, p_74861_3_, 5, 1);
         if (this.leftLow) {
            this.generateSmallDoorChildLeft((StrongholdPieces.Stairs2)p_74861_1_, p_74861_2_, p_74861_3_, i, 1);
         }

         if (this.leftHigh) {
            this.generateSmallDoorChildLeft((StrongholdPieces.Stairs2)p_74861_1_, p_74861_2_, p_74861_3_, j, 7);
         }

         if (this.rightLow) {
            this.generateSmallDoorChildRight((StrongholdPieces.Stairs2)p_74861_1_, p_74861_2_, p_74861_3_, i, 1);
         }

         if (this.rightHigh) {
            this.generateSmallDoorChildRight((StrongholdPieces.Stairs2)p_74861_1_, p_74861_2_, p_74861_3_, j, 7);
         }

      }

      public static StrongholdPieces.Crossing createPiece(List<StructurePiece> p_175866_0_, Random p_175866_1_, int p_175866_2_, int p_175866_3_, int p_175866_4_, Direction p_175866_5_, int p_175866_6_) {
         MutableBoundingBox mutableboundingbox = MutableBoundingBox.orientBox(p_175866_2_, p_175866_3_, p_175866_4_, -4, -3, 0, 10, 9, 11, p_175866_5_);
         return isOkBox(mutableboundingbox) && StructurePiece.findCollisionPiece(p_175866_0_, mutableboundingbox) == null ? new StrongholdPieces.Crossing(p_175866_6_, p_175866_1_, mutableboundingbox, p_175866_5_) : null;
      }

      public boolean postProcess(ISeedReader pLevel, StructureManager pStructureManager, ChunkGenerator pChunkGenerator, Random pRandom, MutableBoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         this.generateBox(pLevel, pBox, 0, 0, 0, 9, 8, 10, true, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor(pLevel, pRandom, pBox, this.entryDoor, 4, 3, 0);
         if (this.leftLow) {
            this.generateBox(pLevel, pBox, 0, 3, 1, 0, 5, 3, CAVE_AIR, CAVE_AIR, false);
         }

         if (this.rightLow) {
            this.generateBox(pLevel, pBox, 9, 3, 1, 9, 5, 3, CAVE_AIR, CAVE_AIR, false);
         }

         if (this.leftHigh) {
            this.generateBox(pLevel, pBox, 0, 5, 7, 0, 7, 9, CAVE_AIR, CAVE_AIR, false);
         }

         if (this.rightHigh) {
            this.generateBox(pLevel, pBox, 9, 5, 7, 9, 7, 9, CAVE_AIR, CAVE_AIR, false);
         }

         this.generateBox(pLevel, pBox, 5, 1, 10, 7, 3, 10, CAVE_AIR, CAVE_AIR, false);
         this.generateBox(pLevel, pBox, 1, 2, 1, 8, 2, 6, false, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 4, 1, 5, 4, 4, 9, false, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 8, 1, 5, 8, 4, 9, false, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 1, 4, 7, 3, 4, 9, false, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 1, 3, 5, 3, 3, 6, false, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 1, 3, 4, 3, 3, 4, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 1, 4, 6, 3, 4, 6, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 5, 1, 7, 7, 1, 8, false, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 5, 1, 9, 7, 1, 9, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 5, 2, 7, 7, 2, 7, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 4, 5, 7, 4, 5, 9, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 8, 5, 7, 8, 5, 9, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 5, 5, 7, 7, 5, 9, Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE), Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE), false);
         this.placeBlock(pLevel, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH), 6, 5, 6, pBox);
         return true;
      }
   }

   public static class LeftTurn extends StrongholdPieces.Turn {
      public LeftTurn(int pGenDepth, Random pRandom, MutableBoundingBox pBox, Direction pOrientation) {
         super(IStructurePieceType.STRONGHOLD_LEFT_TURN, pGenDepth);
         this.setOrientation(pOrientation);
         this.entryDoor = this.randomSmallDoor(pRandom);
         this.boundingBox = pBox;
      }

      public LeftTurn(TemplateManager p_i50134_1_, CompoundNBT p_i50134_2_) {
         super(IStructurePieceType.STRONGHOLD_LEFT_TURN, p_i50134_2_);
      }

      public void addChildren(StructurePiece p_74861_1_, List<StructurePiece> p_74861_2_, Random p_74861_3_) {
         Direction direction = this.getOrientation();
         if (direction != Direction.NORTH && direction != Direction.EAST) {
            this.generateSmallDoorChildRight((StrongholdPieces.Stairs2)p_74861_1_, p_74861_2_, p_74861_3_, 1, 1);
         } else {
            this.generateSmallDoorChildLeft((StrongholdPieces.Stairs2)p_74861_1_, p_74861_2_, p_74861_3_, 1, 1);
         }

      }

      public static StrongholdPieces.LeftTurn createPiece(List<StructurePiece> p_175867_0_, Random p_175867_1_, int p_175867_2_, int p_175867_3_, int p_175867_4_, Direction p_175867_5_, int p_175867_6_) {
         MutableBoundingBox mutableboundingbox = MutableBoundingBox.orientBox(p_175867_2_, p_175867_3_, p_175867_4_, -1, -1, 0, 5, 5, 5, p_175867_5_);
         return isOkBox(mutableboundingbox) && StructurePiece.findCollisionPiece(p_175867_0_, mutableboundingbox) == null ? new StrongholdPieces.LeftTurn(p_175867_6_, p_175867_1_, mutableboundingbox, p_175867_5_) : null;
      }

      public boolean postProcess(ISeedReader pLevel, StructureManager pStructureManager, ChunkGenerator pChunkGenerator, Random pRandom, MutableBoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         this.generateBox(pLevel, pBox, 0, 0, 0, 4, 4, 4, true, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor(pLevel, pRandom, pBox, this.entryDoor, 1, 1, 0);
         Direction direction = this.getOrientation();
         if (direction != Direction.NORTH && direction != Direction.EAST) {
            this.generateBox(pLevel, pBox, 4, 1, 1, 4, 3, 3, CAVE_AIR, CAVE_AIR, false);
         } else {
            this.generateBox(pLevel, pBox, 0, 1, 1, 0, 3, 3, CAVE_AIR, CAVE_AIR, false);
         }

         return true;
      }
   }

   public static class Library extends StrongholdPieces.Stronghold {
      private final boolean isTall;

      public Library(int pGenDepth, Random pRandom, MutableBoundingBox pBox, Direction pOrientation) {
         super(IStructurePieceType.STRONGHOLD_LIBRARY, pGenDepth);
         this.setOrientation(pOrientation);
         this.entryDoor = this.randomSmallDoor(pRandom);
         this.boundingBox = pBox;
         this.isTall = pBox.getYSpan() > 6;
      }

      public Library(TemplateManager p_i50133_1_, CompoundNBT p_i50133_2_) {
         super(IStructurePieceType.STRONGHOLD_LIBRARY, p_i50133_2_);
         this.isTall = p_i50133_2_.getBoolean("Tall");
      }

      protected void addAdditionalSaveData(CompoundNBT p_143011_1_) {
         super.addAdditionalSaveData(p_143011_1_);
         p_143011_1_.putBoolean("Tall", this.isTall);
      }

      public static StrongholdPieces.Library createPiece(List<StructurePiece> p_175864_0_, Random p_175864_1_, int p_175864_2_, int p_175864_3_, int p_175864_4_, Direction p_175864_5_, int p_175864_6_) {
         MutableBoundingBox mutableboundingbox = MutableBoundingBox.orientBox(p_175864_2_, p_175864_3_, p_175864_4_, -4, -1, 0, 14, 11, 15, p_175864_5_);
         if (!isOkBox(mutableboundingbox) || StructurePiece.findCollisionPiece(p_175864_0_, mutableboundingbox) != null) {
            mutableboundingbox = MutableBoundingBox.orientBox(p_175864_2_, p_175864_3_, p_175864_4_, -4, -1, 0, 14, 6, 15, p_175864_5_);
            if (!isOkBox(mutableboundingbox) || StructurePiece.findCollisionPiece(p_175864_0_, mutableboundingbox) != null) {
               return null;
            }
         }

         return new StrongholdPieces.Library(p_175864_6_, p_175864_1_, mutableboundingbox, p_175864_5_);
      }

      public boolean postProcess(ISeedReader pLevel, StructureManager pStructureManager, ChunkGenerator pChunkGenerator, Random pRandom, MutableBoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         int i = 11;
         if (!this.isTall) {
            i = 6;
         }

         this.generateBox(pLevel, pBox, 0, 0, 0, 13, i - 1, 14, true, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor(pLevel, pRandom, pBox, this.entryDoor, 4, 1, 0);
         this.generateMaybeBox(pLevel, pBox, pRandom, 0.07F, 2, 1, 1, 11, 4, 13, Blocks.COBWEB.defaultBlockState(), Blocks.COBWEB.defaultBlockState(), false, false);
         int j = 1;
         int k = 12;

         for(int l = 1; l <= 13; ++l) {
            if ((l - 1) % 4 == 0) {
               this.generateBox(pLevel, pBox, 1, 1, l, 1, 4, l, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
               this.generateBox(pLevel, pBox, 12, 1, l, 12, 4, l, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
               this.placeBlock(pLevel, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST), 2, 3, l, pBox);
               this.placeBlock(pLevel, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST), 11, 3, l, pBox);
               if (this.isTall) {
                  this.generateBox(pLevel, pBox, 1, 6, l, 1, 9, l, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
                  this.generateBox(pLevel, pBox, 12, 6, l, 12, 9, l, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
               }
            } else {
               this.generateBox(pLevel, pBox, 1, 1, l, 1, 4, l, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
               this.generateBox(pLevel, pBox, 12, 1, l, 12, 4, l, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
               if (this.isTall) {
                  this.generateBox(pLevel, pBox, 1, 6, l, 1, 9, l, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
                  this.generateBox(pLevel, pBox, 12, 6, l, 12, 9, l, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
               }
            }
         }

         for(int l1 = 3; l1 < 12; l1 += 2) {
            this.generateBox(pLevel, pBox, 3, 1, l1, 4, 3, l1, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
            this.generateBox(pLevel, pBox, 6, 1, l1, 7, 3, l1, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
            this.generateBox(pLevel, pBox, 9, 1, l1, 10, 3, l1, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false);
         }

         if (this.isTall) {
            this.generateBox(pLevel, pBox, 1, 5, 1, 3, 5, 13, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
            this.generateBox(pLevel, pBox, 10, 5, 1, 12, 5, 13, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
            this.generateBox(pLevel, pBox, 4, 5, 1, 9, 5, 2, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
            this.generateBox(pLevel, pBox, 4, 5, 12, 9, 5, 13, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
            this.placeBlock(pLevel, Blocks.OAK_PLANKS.defaultBlockState(), 9, 5, 11, pBox);
            this.placeBlock(pLevel, Blocks.OAK_PLANKS.defaultBlockState(), 8, 5, 11, pBox);
            this.placeBlock(pLevel, Blocks.OAK_PLANKS.defaultBlockState(), 9, 5, 10, pBox);
            BlockState blockstate5 = Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
            BlockState blockstate = Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.SOUTH, Boolean.valueOf(true));
            this.generateBox(pLevel, pBox, 3, 6, 3, 3, 6, 11, blockstate, blockstate, false);
            this.generateBox(pLevel, pBox, 10, 6, 3, 10, 6, 9, blockstate, blockstate, false);
            this.generateBox(pLevel, pBox, 4, 6, 2, 9, 6, 2, blockstate5, blockstate5, false);
            this.generateBox(pLevel, pBox, 4, 6, 12, 7, 6, 12, blockstate5, blockstate5, false);
            this.placeBlock(pLevel, Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true)), 3, 6, 2, pBox);
            this.placeBlock(pLevel, Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true)), 3, 6, 12, pBox);
            this.placeBlock(pLevel, Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.WEST, Boolean.valueOf(true)), 10, 6, 2, pBox);

            for(int i1 = 0; i1 <= 2; ++i1) {
               this.placeBlock(pLevel, Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, Boolean.valueOf(true)).setValue(FenceBlock.WEST, Boolean.valueOf(true)), 8 + i1, 6, 12 - i1, pBox);
               if (i1 != 2) {
                  this.placeBlock(pLevel, Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true)), 8 + i1, 6, 11 - i1, pBox);
               }
            }

            BlockState blockstate6 = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH);
            this.placeBlock(pLevel, blockstate6, 10, 1, 13, pBox);
            this.placeBlock(pLevel, blockstate6, 10, 2, 13, pBox);
            this.placeBlock(pLevel, blockstate6, 10, 3, 13, pBox);
            this.placeBlock(pLevel, blockstate6, 10, 4, 13, pBox);
            this.placeBlock(pLevel, blockstate6, 10, 5, 13, pBox);
            this.placeBlock(pLevel, blockstate6, 10, 6, 13, pBox);
            this.placeBlock(pLevel, blockstate6, 10, 7, 13, pBox);
            int j1 = 7;
            int k1 = 7;
            BlockState blockstate1 = Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, Boolean.valueOf(true));
            this.placeBlock(pLevel, blockstate1, 6, 9, 7, pBox);
            BlockState blockstate2 = Blocks.OAK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, Boolean.valueOf(true));
            this.placeBlock(pLevel, blockstate2, 7, 9, 7, pBox);
            this.placeBlock(pLevel, blockstate1, 6, 8, 7, pBox);
            this.placeBlock(pLevel, blockstate2, 7, 8, 7, pBox);
            BlockState blockstate3 = blockstate.setValue(FenceBlock.WEST, Boolean.valueOf(true)).setValue(FenceBlock.EAST, Boolean.valueOf(true));
            this.placeBlock(pLevel, blockstate3, 6, 7, 7, pBox);
            this.placeBlock(pLevel, blockstate3, 7, 7, 7, pBox);
            this.placeBlock(pLevel, blockstate1, 5, 7, 7, pBox);
            this.placeBlock(pLevel, blockstate2, 8, 7, 7, pBox);
            this.placeBlock(pLevel, blockstate1.setValue(FenceBlock.NORTH, Boolean.valueOf(true)), 6, 7, 6, pBox);
            this.placeBlock(pLevel, blockstate1.setValue(FenceBlock.SOUTH, Boolean.valueOf(true)), 6, 7, 8, pBox);
            this.placeBlock(pLevel, blockstate2.setValue(FenceBlock.NORTH, Boolean.valueOf(true)), 7, 7, 6, pBox);
            this.placeBlock(pLevel, blockstate2.setValue(FenceBlock.SOUTH, Boolean.valueOf(true)), 7, 7, 8, pBox);
            BlockState blockstate4 = Blocks.TORCH.defaultBlockState();
            this.placeBlock(pLevel, blockstate4, 5, 8, 7, pBox);
            this.placeBlock(pLevel, blockstate4, 8, 8, 7, pBox);
            this.placeBlock(pLevel, blockstate4, 6, 8, 6, pBox);
            this.placeBlock(pLevel, blockstate4, 6, 8, 8, pBox);
            this.placeBlock(pLevel, blockstate4, 7, 8, 6, pBox);
            this.placeBlock(pLevel, blockstate4, 7, 8, 8, pBox);
         }

         this.createChest(pLevel, pBox, pRandom, 3, 3, 5, LootTables.STRONGHOLD_LIBRARY);
         if (this.isTall) {
            this.placeBlock(pLevel, CAVE_AIR, 12, 9, 1, pBox);
            this.createChest(pLevel, pBox, pRandom, 12, 8, 1, LootTables.STRONGHOLD_LIBRARY);
         }

         return true;
      }
   }

   static class PieceWeight {
      public final Class<? extends StrongholdPieces.Stronghold> pieceClass;
      public final int weight;
      public int placeCount;
      public final int maxPlaceCount;

      public PieceWeight(Class<? extends StrongholdPieces.Stronghold> pPieceClass, int pWeight, int pMaxPlaceCount) {
         this.pieceClass = pPieceClass;
         this.weight = pWeight;
         this.maxPlaceCount = pMaxPlaceCount;
      }

      public boolean doPlace(int p_75189_1_) {
         return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
      }

      public boolean isValid() {
         return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
      }
   }

   public static class PortalRoom extends StrongholdPieces.Stronghold {
      private boolean hasPlacedSpawner;

      public PortalRoom(int pGenDepth, MutableBoundingBox pBox, Direction pOrientation) {
         super(IStructurePieceType.STRONGHOLD_PORTAL_ROOM, pGenDepth);
         this.setOrientation(pOrientation);
         this.boundingBox = pBox;
      }

      public PortalRoom(TemplateManager p_i50132_1_, CompoundNBT p_i50132_2_) {
         super(IStructurePieceType.STRONGHOLD_PORTAL_ROOM, p_i50132_2_);
         this.hasPlacedSpawner = p_i50132_2_.getBoolean("Mob");
      }

      protected void addAdditionalSaveData(CompoundNBT p_143011_1_) {
         super.addAdditionalSaveData(p_143011_1_);
         p_143011_1_.putBoolean("Mob", this.hasPlacedSpawner);
      }

      public void addChildren(StructurePiece p_74861_1_, List<StructurePiece> p_74861_2_, Random p_74861_3_) {
         if (p_74861_1_ != null) {
            ((StrongholdPieces.Stairs2)p_74861_1_).portalRoomPiece = this;
         }

      }

      public static StrongholdPieces.PortalRoom createPiece(List<StructurePiece> p_175865_0_, int p_175865_1_, int p_175865_2_, int p_175865_3_, Direction p_175865_4_, int p_175865_5_) {
         MutableBoundingBox mutableboundingbox = MutableBoundingBox.orientBox(p_175865_1_, p_175865_2_, p_175865_3_, -4, -1, 0, 11, 8, 16, p_175865_4_);
         return isOkBox(mutableboundingbox) && StructurePiece.findCollisionPiece(p_175865_0_, mutableboundingbox) == null ? new StrongholdPieces.PortalRoom(p_175865_5_, mutableboundingbox, p_175865_4_) : null;
      }

      public boolean postProcess(ISeedReader pLevel, StructureManager pStructureManager, ChunkGenerator pChunkGenerator, Random pRandom, MutableBoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         this.generateBox(pLevel, pBox, 0, 0, 0, 10, 7, 15, false, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor(pLevel, pRandom, pBox, StrongholdPieces.Stronghold.Door.GRATES, 4, 1, 0);
         int i = 6;
         this.generateBox(pLevel, pBox, 1, i, 1, 1, i, 14, false, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 9, i, 1, 9, i, 14, false, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 2, i, 1, 8, i, 2, false, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 2, i, 14, 8, i, 14, false, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 1, 1, 1, 2, 1, 4, false, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 8, 1, 1, 9, 1, 4, false, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 1, 1, 1, 1, 1, 3, Blocks.LAVA.defaultBlockState(), Blocks.LAVA.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 9, 1, 1, 9, 1, 3, Blocks.LAVA.defaultBlockState(), Blocks.LAVA.defaultBlockState(), false);
         this.generateBox(pLevel, pBox, 3, 1, 8, 7, 1, 12, false, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 4, 1, 9, 6, 1, 11, Blocks.LAVA.defaultBlockState(), Blocks.LAVA.defaultBlockState(), false);
         BlockState blockstate = Blocks.IRON_BARS.defaultBlockState().setValue(PaneBlock.NORTH, Boolean.valueOf(true)).setValue(PaneBlock.SOUTH, Boolean.valueOf(true));
         BlockState blockstate1 = Blocks.IRON_BARS.defaultBlockState().setValue(PaneBlock.WEST, Boolean.valueOf(true)).setValue(PaneBlock.EAST, Boolean.valueOf(true));

         for(int j = 3; j < 14; j += 2) {
            this.generateBox(pLevel, pBox, 0, 3, j, 0, 4, j, blockstate, blockstate, false);
            this.generateBox(pLevel, pBox, 10, 3, j, 10, 4, j, blockstate, blockstate, false);
         }

         for(int i1 = 2; i1 < 9; i1 += 2) {
            this.generateBox(pLevel, pBox, i1, 3, 15, i1, 4, 15, blockstate1, blockstate1, false);
         }

         BlockState blockstate5 = Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairsBlock.FACING, Direction.NORTH);
         this.generateBox(pLevel, pBox, 4, 1, 5, 6, 1, 7, false, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 4, 2, 6, 6, 2, 7, false, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 4, 3, 7, 6, 3, 7, false, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);

         for(int k = 4; k <= 6; ++k) {
            this.placeBlock(pLevel, blockstate5, k, 1, 4, pBox);
            this.placeBlock(pLevel, blockstate5, k, 2, 5, pBox);
            this.placeBlock(pLevel, blockstate5, k, 3, 6, pBox);
         }

         BlockState blockstate6 = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.NORTH);
         BlockState blockstate2 = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.SOUTH);
         BlockState blockstate3 = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.EAST);
         BlockState blockstate4 = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.WEST);
         boolean flag = true;
         boolean[] aboolean = new boolean[12];

         for(int l = 0; l < aboolean.length; ++l) {
            aboolean[l] = pRandom.nextFloat() > 0.9F;
            flag &= aboolean[l];
         }

         this.placeBlock(pLevel, blockstate6.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(aboolean[0])), 4, 3, 8, pBox);
         this.placeBlock(pLevel, blockstate6.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(aboolean[1])), 5, 3, 8, pBox);
         this.placeBlock(pLevel, blockstate6.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(aboolean[2])), 6, 3, 8, pBox);
         this.placeBlock(pLevel, blockstate2.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(aboolean[3])), 4, 3, 12, pBox);
         this.placeBlock(pLevel, blockstate2.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(aboolean[4])), 5, 3, 12, pBox);
         this.placeBlock(pLevel, blockstate2.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(aboolean[5])), 6, 3, 12, pBox);
         this.placeBlock(pLevel, blockstate3.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(aboolean[6])), 3, 3, 9, pBox);
         this.placeBlock(pLevel, blockstate3.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(aboolean[7])), 3, 3, 10, pBox);
         this.placeBlock(pLevel, blockstate3.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(aboolean[8])), 3, 3, 11, pBox);
         this.placeBlock(pLevel, blockstate4.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(aboolean[9])), 7, 3, 9, pBox);
         this.placeBlock(pLevel, blockstate4.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(aboolean[10])), 7, 3, 10, pBox);
         this.placeBlock(pLevel, blockstate4.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(aboolean[11])), 7, 3, 11, pBox);
         if (flag) {
            BlockState blockstate7 = Blocks.END_PORTAL.defaultBlockState();
            this.placeBlock(pLevel, blockstate7, 4, 3, 9, pBox);
            this.placeBlock(pLevel, blockstate7, 5, 3, 9, pBox);
            this.placeBlock(pLevel, blockstate7, 6, 3, 9, pBox);
            this.placeBlock(pLevel, blockstate7, 4, 3, 10, pBox);
            this.placeBlock(pLevel, blockstate7, 5, 3, 10, pBox);
            this.placeBlock(pLevel, blockstate7, 6, 3, 10, pBox);
            this.placeBlock(pLevel, blockstate7, 4, 3, 11, pBox);
            this.placeBlock(pLevel, blockstate7, 5, 3, 11, pBox);
            this.placeBlock(pLevel, blockstate7, 6, 3, 11, pBox);
         }

         if (!this.hasPlacedSpawner) {
            i = this.getWorldY(3);
            BlockPos blockpos = new BlockPos(this.getWorldX(5, 6), i, this.getWorldZ(5, 6));
            if (pBox.isInside(blockpos)) {
               this.hasPlacedSpawner = true;
               pLevel.setBlock(blockpos, Blocks.SPAWNER.defaultBlockState(), 2);
               TileEntity tileentity = pLevel.getBlockEntity(blockpos);
               if (tileentity instanceof MobSpawnerTileEntity) {
                  ((MobSpawnerTileEntity)tileentity).getSpawner().setEntityId(EntityType.SILVERFISH);
               }
            }
         }

         return true;
      }
   }

   public static class Prison extends StrongholdPieces.Stronghold {
      public Prison(int pGenDepth, Random pRandom, MutableBoundingBox pBox, Direction pOrientation) {
         super(IStructurePieceType.STRONGHOLD_PRISON_HALL, pGenDepth);
         this.setOrientation(pOrientation);
         this.entryDoor = this.randomSmallDoor(pRandom);
         this.boundingBox = pBox;
      }

      public Prison(TemplateManager p_i50130_1_, CompoundNBT p_i50130_2_) {
         super(IStructurePieceType.STRONGHOLD_PRISON_HALL, p_i50130_2_);
      }

      public void addChildren(StructurePiece p_74861_1_, List<StructurePiece> p_74861_2_, Random p_74861_3_) {
         this.generateSmallDoorChildForward((StrongholdPieces.Stairs2)p_74861_1_, p_74861_2_, p_74861_3_, 1, 1);
      }

      public static StrongholdPieces.Prison createPiece(List<StructurePiece> p_175860_0_, Random p_175860_1_, int p_175860_2_, int p_175860_3_, int p_175860_4_, Direction p_175860_5_, int p_175860_6_) {
         MutableBoundingBox mutableboundingbox = MutableBoundingBox.orientBox(p_175860_2_, p_175860_3_, p_175860_4_, -1, -1, 0, 9, 5, 11, p_175860_5_);
         return isOkBox(mutableboundingbox) && StructurePiece.findCollisionPiece(p_175860_0_, mutableboundingbox) == null ? new StrongholdPieces.Prison(p_175860_6_, p_175860_1_, mutableboundingbox, p_175860_5_) : null;
      }

      public boolean postProcess(ISeedReader pLevel, StructureManager pStructureManager, ChunkGenerator pChunkGenerator, Random pRandom, MutableBoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         this.generateBox(pLevel, pBox, 0, 0, 0, 8, 4, 10, true, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor(pLevel, pRandom, pBox, this.entryDoor, 1, 1, 0);
         this.generateBox(pLevel, pBox, 1, 1, 10, 3, 3, 10, CAVE_AIR, CAVE_AIR, false);
         this.generateBox(pLevel, pBox, 4, 1, 1, 4, 3, 1, false, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 4, 1, 3, 4, 3, 3, false, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 4, 1, 7, 4, 3, 7, false, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateBox(pLevel, pBox, 4, 1, 9, 4, 3, 9, false, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);

         for(int i = 1; i <= 3; ++i) {
            this.placeBlock(pLevel, Blocks.IRON_BARS.defaultBlockState().setValue(PaneBlock.NORTH, Boolean.valueOf(true)).setValue(PaneBlock.SOUTH, Boolean.valueOf(true)), 4, i, 4, pBox);
            this.placeBlock(pLevel, Blocks.IRON_BARS.defaultBlockState().setValue(PaneBlock.NORTH, Boolean.valueOf(true)).setValue(PaneBlock.SOUTH, Boolean.valueOf(true)).setValue(PaneBlock.EAST, Boolean.valueOf(true)), 4, i, 5, pBox);
            this.placeBlock(pLevel, Blocks.IRON_BARS.defaultBlockState().setValue(PaneBlock.NORTH, Boolean.valueOf(true)).setValue(PaneBlock.SOUTH, Boolean.valueOf(true)), 4, i, 6, pBox);
            this.placeBlock(pLevel, Blocks.IRON_BARS.defaultBlockState().setValue(PaneBlock.WEST, Boolean.valueOf(true)).setValue(PaneBlock.EAST, Boolean.valueOf(true)), 5, i, 5, pBox);
            this.placeBlock(pLevel, Blocks.IRON_BARS.defaultBlockState().setValue(PaneBlock.WEST, Boolean.valueOf(true)).setValue(PaneBlock.EAST, Boolean.valueOf(true)), 6, i, 5, pBox);
            this.placeBlock(pLevel, Blocks.IRON_BARS.defaultBlockState().setValue(PaneBlock.WEST, Boolean.valueOf(true)).setValue(PaneBlock.EAST, Boolean.valueOf(true)), 7, i, 5, pBox);
         }

         this.placeBlock(pLevel, Blocks.IRON_BARS.defaultBlockState().setValue(PaneBlock.NORTH, Boolean.valueOf(true)).setValue(PaneBlock.SOUTH, Boolean.valueOf(true)), 4, 3, 2, pBox);
         this.placeBlock(pLevel, Blocks.IRON_BARS.defaultBlockState().setValue(PaneBlock.NORTH, Boolean.valueOf(true)).setValue(PaneBlock.SOUTH, Boolean.valueOf(true)), 4, 3, 8, pBox);
         BlockState blockstate1 = Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.WEST);
         BlockState blockstate = Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.WEST).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
         this.placeBlock(pLevel, blockstate1, 4, 1, 2, pBox);
         this.placeBlock(pLevel, blockstate, 4, 2, 2, pBox);
         this.placeBlock(pLevel, blockstate1, 4, 1, 8, pBox);
         this.placeBlock(pLevel, blockstate, 4, 2, 8, pBox);
         return true;
      }
   }

   public static class RightTurn extends StrongholdPieces.Turn {
      public RightTurn(int pGenDepth, Random pRandom, MutableBoundingBox pBox, Direction pOrientation) {
         super(IStructurePieceType.STRONGHOLD_RIGHT_TURN, pGenDepth);
         this.setOrientation(pOrientation);
         this.entryDoor = this.randomSmallDoor(pRandom);
         this.boundingBox = pBox;
      }

      public RightTurn(TemplateManager p_i50128_1_, CompoundNBT p_i50128_2_) {
         super(IStructurePieceType.STRONGHOLD_RIGHT_TURN, p_i50128_2_);
      }

      public void addChildren(StructurePiece p_74861_1_, List<StructurePiece> p_74861_2_, Random p_74861_3_) {
         Direction direction = this.getOrientation();
         if (direction != Direction.NORTH && direction != Direction.EAST) {
            this.generateSmallDoorChildLeft((StrongholdPieces.Stairs2)p_74861_1_, p_74861_2_, p_74861_3_, 1, 1);
         } else {
            this.generateSmallDoorChildRight((StrongholdPieces.Stairs2)p_74861_1_, p_74861_2_, p_74861_3_, 1, 1);
         }

      }

      public static StrongholdPieces.RightTurn createPiece(List<StructurePiece> p_214824_0_, Random p_214824_1_, int p_214824_2_, int p_214824_3_, int p_214824_4_, Direction p_214824_5_, int p_214824_6_) {
         MutableBoundingBox mutableboundingbox = MutableBoundingBox.orientBox(p_214824_2_, p_214824_3_, p_214824_4_, -1, -1, 0, 5, 5, 5, p_214824_5_);
         return isOkBox(mutableboundingbox) && StructurePiece.findCollisionPiece(p_214824_0_, mutableboundingbox) == null ? new StrongholdPieces.RightTurn(p_214824_6_, p_214824_1_, mutableboundingbox, p_214824_5_) : null;
      }

      public boolean postProcess(ISeedReader pLevel, StructureManager pStructureManager, ChunkGenerator pChunkGenerator, Random pRandom, MutableBoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         this.generateBox(pLevel, pBox, 0, 0, 0, 4, 4, 4, true, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor(pLevel, pRandom, pBox, this.entryDoor, 1, 1, 0);
         Direction direction = this.getOrientation();
         if (direction != Direction.NORTH && direction != Direction.EAST) {
            this.generateBox(pLevel, pBox, 0, 1, 1, 0, 3, 3, CAVE_AIR, CAVE_AIR, false);
         } else {
            this.generateBox(pLevel, pBox, 4, 1, 1, 4, 3, 3, CAVE_AIR, CAVE_AIR, false);
         }

         return true;
      }
   }

   public static class RoomCrossing extends StrongholdPieces.Stronghold {
      protected final int type;

      public RoomCrossing(int pGenDepth, Random pRandom, MutableBoundingBox pBox, Direction pOrientation) {
         super(IStructurePieceType.STRONGHOLD_ROOM_CROSSING, pGenDepth);
         this.setOrientation(pOrientation);
         this.entryDoor = this.randomSmallDoor(pRandom);
         this.boundingBox = pBox;
         this.type = pRandom.nextInt(5);
      }

      public RoomCrossing(TemplateManager p_i50125_1_, CompoundNBT p_i50125_2_) {
         super(IStructurePieceType.STRONGHOLD_ROOM_CROSSING, p_i50125_2_);
         this.type = p_i50125_2_.getInt("Type");
      }

      protected void addAdditionalSaveData(CompoundNBT p_143011_1_) {
         super.addAdditionalSaveData(p_143011_1_);
         p_143011_1_.putInt("Type", this.type);
      }

      public void addChildren(StructurePiece p_74861_1_, List<StructurePiece> p_74861_2_, Random p_74861_3_) {
         this.generateSmallDoorChildForward((StrongholdPieces.Stairs2)p_74861_1_, p_74861_2_, p_74861_3_, 4, 1);
         this.generateSmallDoorChildLeft((StrongholdPieces.Stairs2)p_74861_1_, p_74861_2_, p_74861_3_, 1, 4);
         this.generateSmallDoorChildRight((StrongholdPieces.Stairs2)p_74861_1_, p_74861_2_, p_74861_3_, 1, 4);
      }

      public static StrongholdPieces.RoomCrossing createPiece(List<StructurePiece> p_175859_0_, Random p_175859_1_, int p_175859_2_, int p_175859_3_, int p_175859_4_, Direction p_175859_5_, int p_175859_6_) {
         MutableBoundingBox mutableboundingbox = MutableBoundingBox.orientBox(p_175859_2_, p_175859_3_, p_175859_4_, -4, -1, 0, 11, 7, 11, p_175859_5_);
         return isOkBox(mutableboundingbox) && StructurePiece.findCollisionPiece(p_175859_0_, mutableboundingbox) == null ? new StrongholdPieces.RoomCrossing(p_175859_6_, p_175859_1_, mutableboundingbox, p_175859_5_) : null;
      }

      public boolean postProcess(ISeedReader pLevel, StructureManager pStructureManager, ChunkGenerator pChunkGenerator, Random pRandom, MutableBoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         this.generateBox(pLevel, pBox, 0, 0, 0, 10, 6, 10, true, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor(pLevel, pRandom, pBox, this.entryDoor, 4, 1, 0);
         this.generateBox(pLevel, pBox, 4, 1, 10, 6, 3, 10, CAVE_AIR, CAVE_AIR, false);
         this.generateBox(pLevel, pBox, 0, 1, 4, 0, 3, 6, CAVE_AIR, CAVE_AIR, false);
         this.generateBox(pLevel, pBox, 10, 1, 4, 10, 3, 6, CAVE_AIR, CAVE_AIR, false);
         switch(this.type) {
         case 0:
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 1, 5, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 2, 5, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 3, 5, pBox);
            this.placeBlock(pLevel, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST), 4, 3, 5, pBox);
            this.placeBlock(pLevel, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST), 6, 3, 5, pBox);
            this.placeBlock(pLevel, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH), 5, 3, 4, pBox);
            this.placeBlock(pLevel, Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.NORTH), 5, 3, 6, pBox);
            this.placeBlock(pLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 4, 1, 4, pBox);
            this.placeBlock(pLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 4, 1, 5, pBox);
            this.placeBlock(pLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 4, 1, 6, pBox);
            this.placeBlock(pLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 6, 1, 4, pBox);
            this.placeBlock(pLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 6, 1, 5, pBox);
            this.placeBlock(pLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 6, 1, 6, pBox);
            this.placeBlock(pLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 5, 1, 4, pBox);
            this.placeBlock(pLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 5, 1, 6, pBox);
            break;
         case 1:
            for(int i1 = 0; i1 < 5; ++i1) {
               this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 1, 3 + i1, pBox);
               this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 7, 1, 3 + i1, pBox);
               this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3 + i1, 1, 3, pBox);
               this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3 + i1, 1, 7, pBox);
            }

            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 1, 5, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 2, 5, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 5, 3, 5, pBox);
            this.placeBlock(pLevel, Blocks.WATER.defaultBlockState(), 5, 4, 5, pBox);
            break;
         case 2:
            for(int i = 1; i <= 9; ++i) {
               this.placeBlock(pLevel, Blocks.COBBLESTONE.defaultBlockState(), 1, 3, i, pBox);
               this.placeBlock(pLevel, Blocks.COBBLESTONE.defaultBlockState(), 9, 3, i, pBox);
            }

            for(int j = 1; j <= 9; ++j) {
               this.placeBlock(pLevel, Blocks.COBBLESTONE.defaultBlockState(), j, 3, 1, pBox);
               this.placeBlock(pLevel, Blocks.COBBLESTONE.defaultBlockState(), j, 3, 9, pBox);
            }

            this.placeBlock(pLevel, Blocks.COBBLESTONE.defaultBlockState(), 5, 1, 4, pBox);
            this.placeBlock(pLevel, Blocks.COBBLESTONE.defaultBlockState(), 5, 1, 6, pBox);
            this.placeBlock(pLevel, Blocks.COBBLESTONE.defaultBlockState(), 5, 3, 4, pBox);
            this.placeBlock(pLevel, Blocks.COBBLESTONE.defaultBlockState(), 5, 3, 6, pBox);
            this.placeBlock(pLevel, Blocks.COBBLESTONE.defaultBlockState(), 4, 1, 5, pBox);
            this.placeBlock(pLevel, Blocks.COBBLESTONE.defaultBlockState(), 6, 1, 5, pBox);
            this.placeBlock(pLevel, Blocks.COBBLESTONE.defaultBlockState(), 4, 3, 5, pBox);
            this.placeBlock(pLevel, Blocks.COBBLESTONE.defaultBlockState(), 6, 3, 5, pBox);

            for(int k = 1; k <= 3; ++k) {
               this.placeBlock(pLevel, Blocks.COBBLESTONE.defaultBlockState(), 4, k, 4, pBox);
               this.placeBlock(pLevel, Blocks.COBBLESTONE.defaultBlockState(), 6, k, 4, pBox);
               this.placeBlock(pLevel, Blocks.COBBLESTONE.defaultBlockState(), 4, k, 6, pBox);
               this.placeBlock(pLevel, Blocks.COBBLESTONE.defaultBlockState(), 6, k, 6, pBox);
            }

            this.placeBlock(pLevel, Blocks.TORCH.defaultBlockState(), 5, 3, 5, pBox);

            for(int l = 2; l <= 8; ++l) {
               this.placeBlock(pLevel, Blocks.OAK_PLANKS.defaultBlockState(), 2, 3, l, pBox);
               this.placeBlock(pLevel, Blocks.OAK_PLANKS.defaultBlockState(), 3, 3, l, pBox);
               if (l <= 3 || l >= 7) {
                  this.placeBlock(pLevel, Blocks.OAK_PLANKS.defaultBlockState(), 4, 3, l, pBox);
                  this.placeBlock(pLevel, Blocks.OAK_PLANKS.defaultBlockState(), 5, 3, l, pBox);
                  this.placeBlock(pLevel, Blocks.OAK_PLANKS.defaultBlockState(), 6, 3, l, pBox);
               }

               this.placeBlock(pLevel, Blocks.OAK_PLANKS.defaultBlockState(), 7, 3, l, pBox);
               this.placeBlock(pLevel, Blocks.OAK_PLANKS.defaultBlockState(), 8, 3, l, pBox);
            }

            BlockState blockstate = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.WEST);
            this.placeBlock(pLevel, blockstate, 9, 1, 3, pBox);
            this.placeBlock(pLevel, blockstate, 9, 2, 3, pBox);
            this.placeBlock(pLevel, blockstate, 9, 3, 3, pBox);
            this.createChest(pLevel, pBox, pRandom, 3, 4, 8, LootTables.STRONGHOLD_CROSSING);
         }

         return true;
      }
   }

   public static class Stairs extends StrongholdPieces.Stronghold {
      private final boolean isSource;

      public Stairs(IStructurePieceType p_i50120_1_, int p_i50120_2_, Random p_i50120_3_, int p_i50120_4_, int p_i50120_5_) {
         super(p_i50120_1_, p_i50120_2_);
         this.isSource = true;
         this.setOrientation(Direction.Plane.HORIZONTAL.getRandomDirection(p_i50120_3_));
         this.entryDoor = StrongholdPieces.Stronghold.Door.OPENING;
         if (this.getOrientation().getAxis() == Direction.Axis.Z) {
            this.boundingBox = new MutableBoundingBox(p_i50120_4_, 64, p_i50120_5_, p_i50120_4_ + 5 - 1, 74, p_i50120_5_ + 5 - 1);
         } else {
            this.boundingBox = new MutableBoundingBox(p_i50120_4_, 64, p_i50120_5_, p_i50120_4_ + 5 - 1, 74, p_i50120_5_ + 5 - 1);
         }

      }

      public Stairs(int pGenDepth, Random pRandom, MutableBoundingBox pBox, Direction pOrientation) {
         super(IStructurePieceType.STRONGHOLD_STAIRS_DOWN, pGenDepth);
         this.isSource = false;
         this.setOrientation(pOrientation);
         this.entryDoor = this.randomSmallDoor(pRandom);
         this.boundingBox = pBox;
      }

      public Stairs(IStructurePieceType p_i50121_1_, CompoundNBT p_i50121_2_) {
         super(p_i50121_1_, p_i50121_2_);
         this.isSource = p_i50121_2_.getBoolean("Source");
      }

      public Stairs(TemplateManager p_i50122_1_, CompoundNBT p_i50122_2_) {
         this(IStructurePieceType.STRONGHOLD_STAIRS_DOWN, p_i50122_2_);
      }

      protected void addAdditionalSaveData(CompoundNBT p_143011_1_) {
         super.addAdditionalSaveData(p_143011_1_);
         p_143011_1_.putBoolean("Source", this.isSource);
      }

      public void addChildren(StructurePiece p_74861_1_, List<StructurePiece> p_74861_2_, Random p_74861_3_) {
         if (this.isSource) {
            StrongholdPieces.imposedPiece = StrongholdPieces.Crossing.class;
         }

         this.generateSmallDoorChildForward((StrongholdPieces.Stairs2)p_74861_1_, p_74861_2_, p_74861_3_, 1, 1);
      }

      public static StrongholdPieces.Stairs createPiece(List<StructurePiece> p_175863_0_, Random p_175863_1_, int p_175863_2_, int p_175863_3_, int p_175863_4_, Direction p_175863_5_, int p_175863_6_) {
         MutableBoundingBox mutableboundingbox = MutableBoundingBox.orientBox(p_175863_2_, p_175863_3_, p_175863_4_, -1, -7, 0, 5, 11, 5, p_175863_5_);
         return isOkBox(mutableboundingbox) && StructurePiece.findCollisionPiece(p_175863_0_, mutableboundingbox) == null ? new StrongholdPieces.Stairs(p_175863_6_, p_175863_1_, mutableboundingbox, p_175863_5_) : null;
      }

      public boolean postProcess(ISeedReader pLevel, StructureManager pStructureManager, ChunkGenerator pChunkGenerator, Random pRandom, MutableBoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         this.generateBox(pLevel, pBox, 0, 0, 0, 4, 10, 4, true, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor(pLevel, pRandom, pBox, this.entryDoor, 1, 7, 0);
         this.generateSmallDoor(pLevel, pRandom, pBox, StrongholdPieces.Stronghold.Door.OPENING, 1, 1, 4);
         this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 6, 1, pBox);
         this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 5, 1, pBox);
         this.placeBlock(pLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 6, 1, pBox);
         this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 5, 2, pBox);
         this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 4, 3, pBox);
         this.placeBlock(pLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 5, 3, pBox);
         this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 4, 3, pBox);
         this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 3, 3, pBox);
         this.placeBlock(pLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3, 4, 3, pBox);
         this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 3, 2, pBox);
         this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 2, 1, pBox);
         this.placeBlock(pLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3, 3, 1, pBox);
         this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 2, 1, pBox);
         this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 1, 1, pBox);
         this.placeBlock(pLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 2, 1, pBox);
         this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 1, 2, pBox);
         this.placeBlock(pLevel, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 1, 3, pBox);
         return true;
      }
   }

   public static class Stairs2 extends StrongholdPieces.Stairs {
      public StrongholdPieces.PieceWeight previousPiece;
      @Nullable
      public StrongholdPieces.PortalRoom portalRoomPiece;
      public final List<StructurePiece> pendingChildren = Lists.newArrayList();

      public Stairs2(Random pRandom, int pX, int pZ) {
         super(IStructurePieceType.STRONGHOLD_START, 0, pRandom, pX, pZ);
      }

      public Stairs2(TemplateManager p_i50118_1_, CompoundNBT p_i50118_2_) {
         super(IStructurePieceType.STRONGHOLD_START, p_i50118_2_);
      }
   }

   public static class StairsStraight extends StrongholdPieces.Stronghold {
      public StairsStraight(int pGenDepth, Random pRandom, MutableBoundingBox pBox, Direction pOrientation) {
         super(IStructurePieceType.STRONGHOLD_STRAIGHT_STAIRS_DOWN, pGenDepth);
         this.setOrientation(pOrientation);
         this.entryDoor = this.randomSmallDoor(pRandom);
         this.boundingBox = pBox;
      }

      public StairsStraight(TemplateManager p_i50113_1_, CompoundNBT p_i50113_2_) {
         super(IStructurePieceType.STRONGHOLD_STRAIGHT_STAIRS_DOWN, p_i50113_2_);
      }

      public void addChildren(StructurePiece p_74861_1_, List<StructurePiece> p_74861_2_, Random p_74861_3_) {
         this.generateSmallDoorChildForward((StrongholdPieces.Stairs2)p_74861_1_, p_74861_2_, p_74861_3_, 1, 1);
      }

      public static StrongholdPieces.StairsStraight createPiece(List<StructurePiece> p_175861_0_, Random p_175861_1_, int p_175861_2_, int p_175861_3_, int p_175861_4_, Direction p_175861_5_, int p_175861_6_) {
         MutableBoundingBox mutableboundingbox = MutableBoundingBox.orientBox(p_175861_2_, p_175861_3_, p_175861_4_, -1, -7, 0, 5, 11, 8, p_175861_5_);
         return isOkBox(mutableboundingbox) && StructurePiece.findCollisionPiece(p_175861_0_, mutableboundingbox) == null ? new StrongholdPieces.StairsStraight(p_175861_6_, p_175861_1_, mutableboundingbox, p_175861_5_) : null;
      }

      public boolean postProcess(ISeedReader pLevel, StructureManager pStructureManager, ChunkGenerator pChunkGenerator, Random pRandom, MutableBoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         this.generateBox(pLevel, pBox, 0, 0, 0, 4, 10, 7, true, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor(pLevel, pRandom, pBox, this.entryDoor, 1, 7, 0);
         this.generateSmallDoor(pLevel, pRandom, pBox, StrongholdPieces.Stronghold.Door.OPENING, 1, 1, 7);
         BlockState blockstate = Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairsBlock.FACING, Direction.SOUTH);

         for(int i = 0; i < 6; ++i) {
            this.placeBlock(pLevel, blockstate, 1, 6 - i, 1 + i, pBox);
            this.placeBlock(pLevel, blockstate, 2, 6 - i, 1 + i, pBox);
            this.placeBlock(pLevel, blockstate, 3, 6 - i, 1 + i, pBox);
            if (i < 5) {
               this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 1, 5 - i, 1 + i, pBox);
               this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 2, 5 - i, 1 + i, pBox);
               this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), 3, 5 - i, 1 + i, pBox);
            }
         }

         return true;
      }
   }

   static class Stones extends StructurePiece.BlockSelector {
      private Stones() {
      }

      /**
       * picks Block Ids and Metadata (Silverfish)
       */
      public void next(Random pRandom, int pX, int pY, int pZ, boolean pWall) {
         if (pWall) {
            float f = pRandom.nextFloat();
            if (f < 0.2F) {
               this.next = Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
            } else if (f < 0.5F) {
               this.next = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
            } else if (f < 0.55F) {
               this.next = Blocks.INFESTED_STONE_BRICKS.defaultBlockState();
            } else {
               this.next = Blocks.STONE_BRICKS.defaultBlockState();
            }
         } else {
            this.next = Blocks.CAVE_AIR.defaultBlockState();
         }

      }
   }

   public static class Straight extends StrongholdPieces.Stronghold {
      private final boolean leftChild;
      private final boolean rightChild;

      public Straight(int pGenDepth, Random pRandom, MutableBoundingBox pBox, Direction pOrientation) {
         super(IStructurePieceType.STRONGHOLD_STRAIGHT, pGenDepth);
         this.setOrientation(pOrientation);
         this.entryDoor = this.randomSmallDoor(pRandom);
         this.boundingBox = pBox;
         this.leftChild = pRandom.nextInt(2) == 0;
         this.rightChild = pRandom.nextInt(2) == 0;
      }

      public Straight(TemplateManager p_i50115_1_, CompoundNBT p_i50115_2_) {
         super(IStructurePieceType.STRONGHOLD_STRAIGHT, p_i50115_2_);
         this.leftChild = p_i50115_2_.getBoolean("Left");
         this.rightChild = p_i50115_2_.getBoolean("Right");
      }

      protected void addAdditionalSaveData(CompoundNBT p_143011_1_) {
         super.addAdditionalSaveData(p_143011_1_);
         p_143011_1_.putBoolean("Left", this.leftChild);
         p_143011_1_.putBoolean("Right", this.rightChild);
      }

      public void addChildren(StructurePiece p_74861_1_, List<StructurePiece> p_74861_2_, Random p_74861_3_) {
         this.generateSmallDoorChildForward((StrongholdPieces.Stairs2)p_74861_1_, p_74861_2_, p_74861_3_, 1, 1);
         if (this.leftChild) {
            this.generateSmallDoorChildLeft((StrongholdPieces.Stairs2)p_74861_1_, p_74861_2_, p_74861_3_, 1, 2);
         }

         if (this.rightChild) {
            this.generateSmallDoorChildRight((StrongholdPieces.Stairs2)p_74861_1_, p_74861_2_, p_74861_3_, 1, 2);
         }

      }

      public static StrongholdPieces.Straight createPiece(List<StructurePiece> p_175862_0_, Random p_175862_1_, int p_175862_2_, int p_175862_3_, int p_175862_4_, Direction p_175862_5_, int p_175862_6_) {
         MutableBoundingBox mutableboundingbox = MutableBoundingBox.orientBox(p_175862_2_, p_175862_3_, p_175862_4_, -1, -1, 0, 5, 5, 7, p_175862_5_);
         return isOkBox(mutableboundingbox) && StructurePiece.findCollisionPiece(p_175862_0_, mutableboundingbox) == null ? new StrongholdPieces.Straight(p_175862_6_, p_175862_1_, mutableboundingbox, p_175862_5_) : null;
      }

      public boolean postProcess(ISeedReader pLevel, StructureManager pStructureManager, ChunkGenerator pChunkGenerator, Random pRandom, MutableBoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         this.generateBox(pLevel, pBox, 0, 0, 0, 4, 4, 6, true, pRandom, StrongholdPieces.SMOOTH_STONE_SELECTOR);
         this.generateSmallDoor(pLevel, pRandom, pBox, this.entryDoor, 1, 1, 0);
         this.generateSmallDoor(pLevel, pRandom, pBox, StrongholdPieces.Stronghold.Door.OPENING, 1, 1, 6);
         BlockState blockstate = Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.EAST);
         BlockState blockstate1 = Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.WEST);
         this.maybeGenerateBlock(pLevel, pBox, pRandom, 0.1F, 1, 2, 1, blockstate);
         this.maybeGenerateBlock(pLevel, pBox, pRandom, 0.1F, 3, 2, 1, blockstate1);
         this.maybeGenerateBlock(pLevel, pBox, pRandom, 0.1F, 1, 2, 5, blockstate);
         this.maybeGenerateBlock(pLevel, pBox, pRandom, 0.1F, 3, 2, 5, blockstate1);
         if (this.leftChild) {
            this.generateBox(pLevel, pBox, 0, 1, 2, 0, 3, 4, CAVE_AIR, CAVE_AIR, false);
         }

         if (this.rightChild) {
            this.generateBox(pLevel, pBox, 4, 1, 2, 4, 3, 4, CAVE_AIR, CAVE_AIR, false);
         }

         return true;
      }
   }

   abstract static class Stronghold extends StructurePiece {
      protected StrongholdPieces.Stronghold.Door entryDoor = StrongholdPieces.Stronghold.Door.OPENING;

      protected Stronghold(IStructurePieceType p_i50110_1_, int p_i50110_2_) {
         super(p_i50110_1_, p_i50110_2_);
      }

      public Stronghold(IStructurePieceType p_i50111_1_, CompoundNBT p_i50111_2_) {
         super(p_i50111_1_, p_i50111_2_);
         this.entryDoor = StrongholdPieces.Stronghold.Door.valueOf(p_i50111_2_.getString("EntryDoor"));
      }

      protected void addAdditionalSaveData(CompoundNBT p_143011_1_) {
         p_143011_1_.putString("EntryDoor", this.entryDoor.name());
      }

      protected void generateSmallDoor(ISeedReader pLevel, Random pRandom, MutableBoundingBox pBox, StrongholdPieces.Stronghold.Door pType, int pX, int pY, int pZ) {
         switch(pType) {
         case OPENING:
            this.generateBox(pLevel, pBox, pX, pY, pZ, pX + 3 - 1, pY + 3 - 1, pZ, CAVE_AIR, CAVE_AIR, false);
            break;
         case WOOD_DOOR:
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), pX, pY, pZ, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), pX, pY + 1, pZ, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), pX, pY + 2, pZ, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), pX + 1, pY + 2, pZ, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), pX + 2, pY + 2, pZ, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), pX + 2, pY + 1, pZ, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), pX + 2, pY, pZ, pBox);
            this.placeBlock(pLevel, Blocks.OAK_DOOR.defaultBlockState(), pX + 1, pY, pZ, pBox);
            this.placeBlock(pLevel, Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), pX + 1, pY + 1, pZ, pBox);
            break;
         case GRATES:
            this.placeBlock(pLevel, Blocks.CAVE_AIR.defaultBlockState(), pX + 1, pY, pZ, pBox);
            this.placeBlock(pLevel, Blocks.CAVE_AIR.defaultBlockState(), pX + 1, pY + 1, pZ, pBox);
            this.placeBlock(pLevel, Blocks.IRON_BARS.defaultBlockState().setValue(PaneBlock.WEST, Boolean.valueOf(true)), pX, pY, pZ, pBox);
            this.placeBlock(pLevel, Blocks.IRON_BARS.defaultBlockState().setValue(PaneBlock.WEST, Boolean.valueOf(true)), pX, pY + 1, pZ, pBox);
            this.placeBlock(pLevel, Blocks.IRON_BARS.defaultBlockState().setValue(PaneBlock.EAST, Boolean.valueOf(true)).setValue(PaneBlock.WEST, Boolean.valueOf(true)), pX, pY + 2, pZ, pBox);
            this.placeBlock(pLevel, Blocks.IRON_BARS.defaultBlockState().setValue(PaneBlock.EAST, Boolean.valueOf(true)).setValue(PaneBlock.WEST, Boolean.valueOf(true)), pX + 1, pY + 2, pZ, pBox);
            this.placeBlock(pLevel, Blocks.IRON_BARS.defaultBlockState().setValue(PaneBlock.EAST, Boolean.valueOf(true)).setValue(PaneBlock.WEST, Boolean.valueOf(true)), pX + 2, pY + 2, pZ, pBox);
            this.placeBlock(pLevel, Blocks.IRON_BARS.defaultBlockState().setValue(PaneBlock.EAST, Boolean.valueOf(true)), pX + 2, pY + 1, pZ, pBox);
            this.placeBlock(pLevel, Blocks.IRON_BARS.defaultBlockState().setValue(PaneBlock.EAST, Boolean.valueOf(true)), pX + 2, pY, pZ, pBox);
            break;
         case IRON_DOOR:
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), pX, pY, pZ, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), pX, pY + 1, pZ, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), pX, pY + 2, pZ, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), pX + 1, pY + 2, pZ, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), pX + 2, pY + 2, pZ, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), pX + 2, pY + 1, pZ, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BRICKS.defaultBlockState(), pX + 2, pY, pZ, pBox);
            this.placeBlock(pLevel, Blocks.IRON_DOOR.defaultBlockState(), pX + 1, pY, pZ, pBox);
            this.placeBlock(pLevel, Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), pX + 1, pY + 1, pZ, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BUTTON.defaultBlockState().setValue(AbstractButtonBlock.FACING, Direction.NORTH), pX + 2, pY + 1, pZ + 1, pBox);
            this.placeBlock(pLevel, Blocks.STONE_BUTTON.defaultBlockState().setValue(AbstractButtonBlock.FACING, Direction.SOUTH), pX + 2, pY + 1, pZ - 1, pBox);
         }

      }

      protected StrongholdPieces.Stronghold.Door randomSmallDoor(Random pRandom) {
         int i = pRandom.nextInt(5);
         switch(i) {
         case 0:
         case 1:
         default:
            return StrongholdPieces.Stronghold.Door.OPENING;
         case 2:
            return StrongholdPieces.Stronghold.Door.WOOD_DOOR;
         case 3:
            return StrongholdPieces.Stronghold.Door.GRATES;
         case 4:
            return StrongholdPieces.Stronghold.Door.IRON_DOOR;
         }
      }

      @Nullable
      protected StructurePiece generateSmallDoorChildForward(StrongholdPieces.Stairs2 p_74986_1_, List<StructurePiece> p_74986_2_, Random p_74986_3_, int p_74986_4_, int p_74986_5_) {
         Direction direction = this.getOrientation();
         if (direction != null) {
            switch(direction) {
            case NORTH:
               return StrongholdPieces.generateAndAddPiece(p_74986_1_, p_74986_2_, p_74986_3_, this.boundingBox.x0 + p_74986_4_, this.boundingBox.y0 + p_74986_5_, this.boundingBox.z0 - 1, direction, this.getGenDepth());
            case SOUTH:
               return StrongholdPieces.generateAndAddPiece(p_74986_1_, p_74986_2_, p_74986_3_, this.boundingBox.x0 + p_74986_4_, this.boundingBox.y0 + p_74986_5_, this.boundingBox.z1 + 1, direction, this.getGenDepth());
            case WEST:
               return StrongholdPieces.generateAndAddPiece(p_74986_1_, p_74986_2_, p_74986_3_, this.boundingBox.x0 - 1, this.boundingBox.y0 + p_74986_5_, this.boundingBox.z0 + p_74986_4_, direction, this.getGenDepth());
            case EAST:
               return StrongholdPieces.generateAndAddPiece(p_74986_1_, p_74986_2_, p_74986_3_, this.boundingBox.x1 + 1, this.boundingBox.y0 + p_74986_5_, this.boundingBox.z0 + p_74986_4_, direction, this.getGenDepth());
            }
         }

         return null;
      }

      @Nullable
      protected StructurePiece generateSmallDoorChildLeft(StrongholdPieces.Stairs2 p_74989_1_, List<StructurePiece> p_74989_2_, Random p_74989_3_, int p_74989_4_, int p_74989_5_) {
         Direction direction = this.getOrientation();
         if (direction != null) {
            switch(direction) {
            case NORTH:
               return StrongholdPieces.generateAndAddPiece(p_74989_1_, p_74989_2_, p_74989_3_, this.boundingBox.x0 - 1, this.boundingBox.y0 + p_74989_4_, this.boundingBox.z0 + p_74989_5_, Direction.WEST, this.getGenDepth());
            case SOUTH:
               return StrongholdPieces.generateAndAddPiece(p_74989_1_, p_74989_2_, p_74989_3_, this.boundingBox.x0 - 1, this.boundingBox.y0 + p_74989_4_, this.boundingBox.z0 + p_74989_5_, Direction.WEST, this.getGenDepth());
            case WEST:
               return StrongholdPieces.generateAndAddPiece(p_74989_1_, p_74989_2_, p_74989_3_, this.boundingBox.x0 + p_74989_5_, this.boundingBox.y0 + p_74989_4_, this.boundingBox.z0 - 1, Direction.NORTH, this.getGenDepth());
            case EAST:
               return StrongholdPieces.generateAndAddPiece(p_74989_1_, p_74989_2_, p_74989_3_, this.boundingBox.x0 + p_74989_5_, this.boundingBox.y0 + p_74989_4_, this.boundingBox.z0 - 1, Direction.NORTH, this.getGenDepth());
            }
         }

         return null;
      }

      @Nullable
      protected StructurePiece generateSmallDoorChildRight(StrongholdPieces.Stairs2 p_74987_1_, List<StructurePiece> p_74987_2_, Random p_74987_3_, int p_74987_4_, int p_74987_5_) {
         Direction direction = this.getOrientation();
         if (direction != null) {
            switch(direction) {
            case NORTH:
               return StrongholdPieces.generateAndAddPiece(p_74987_1_, p_74987_2_, p_74987_3_, this.boundingBox.x1 + 1, this.boundingBox.y0 + p_74987_4_, this.boundingBox.z0 + p_74987_5_, Direction.EAST, this.getGenDepth());
            case SOUTH:
               return StrongholdPieces.generateAndAddPiece(p_74987_1_, p_74987_2_, p_74987_3_, this.boundingBox.x1 + 1, this.boundingBox.y0 + p_74987_4_, this.boundingBox.z0 + p_74987_5_, Direction.EAST, this.getGenDepth());
            case WEST:
               return StrongholdPieces.generateAndAddPiece(p_74987_1_, p_74987_2_, p_74987_3_, this.boundingBox.x0 + p_74987_5_, this.boundingBox.y0 + p_74987_4_, this.boundingBox.z1 + 1, Direction.SOUTH, this.getGenDepth());
            case EAST:
               return StrongholdPieces.generateAndAddPiece(p_74987_1_, p_74987_2_, p_74987_3_, this.boundingBox.x0 + p_74987_5_, this.boundingBox.y0 + p_74987_4_, this.boundingBox.z1 + 1, Direction.SOUTH, this.getGenDepth());
            }
         }

         return null;
      }

      /**
       * returns false if the Structure Bounding Box goes below 10
       */
      protected static boolean isOkBox(MutableBoundingBox pBox) {
         return pBox != null && pBox.y0 > 10;
      }

      public static enum Door {
         OPENING,
         WOOD_DOOR,
         GRATES,
         IRON_DOOR;
      }
   }

   public abstract static class Turn extends StrongholdPieces.Stronghold {
      protected Turn(IStructurePieceType p_i50108_1_, int p_i50108_2_) {
         super(p_i50108_1_, p_i50108_2_);
      }

      public Turn(IStructurePieceType p_i50109_1_, CompoundNBT p_i50109_2_) {
         super(p_i50109_1_, p_i50109_2_);
      }
   }
}