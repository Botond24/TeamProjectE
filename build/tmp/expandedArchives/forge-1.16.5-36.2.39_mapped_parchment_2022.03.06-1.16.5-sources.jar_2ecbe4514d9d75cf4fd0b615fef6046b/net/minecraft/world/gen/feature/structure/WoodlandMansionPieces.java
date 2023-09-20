package net.minecraft.world.gen.feature.structure;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.AbstractIllagerEntity;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.gen.feature.template.BlockIgnoreStructureProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class WoodlandMansionPieces {
   public static void generateMansion(TemplateManager pStructureManager, BlockPos pPos, Rotation pRotation, List<WoodlandMansionPieces.MansionTemplate> pPieces, Random pRandom) {
      WoodlandMansionPieces.Grid woodlandmansionpieces$grid = new WoodlandMansionPieces.Grid(pRandom);
      WoodlandMansionPieces.Placer woodlandmansionpieces$placer = new WoodlandMansionPieces.Placer(pStructureManager, pRandom);
      woodlandmansionpieces$placer.createMansion(pPos, pRotation, pPieces, woodlandmansionpieces$grid);
   }

   static class FirstFloor extends WoodlandMansionPieces.RoomCollection {
      private FirstFloor() {
      }

      public String get1x1(Random pRandom) {
         return "1x1_a" + (pRandom.nextInt(5) + 1);
      }

      public String get1x1Secret(Random pRandom) {
         return "1x1_as" + (pRandom.nextInt(4) + 1);
      }

      public String get1x2SideEntrance(Random pRandom, boolean pIsStairs) {
         return "1x2_a" + (pRandom.nextInt(9) + 1);
      }

      public String get1x2FrontEntrance(Random pRandom, boolean pIsStairs) {
         return "1x2_b" + (pRandom.nextInt(5) + 1);
      }

      public String get1x2Secret(Random pRandom) {
         return "1x2_s" + (pRandom.nextInt(2) + 1);
      }

      public String get2x2(Random pRandom) {
         return "2x2_a" + (pRandom.nextInt(4) + 1);
      }

      public String get2x2Secret(Random pRandom) {
         return "2x2_s1";
      }
   }

   static class Grid {
      private final Random random;
      private final WoodlandMansionPieces.SimpleGrid baseGrid;
      private final WoodlandMansionPieces.SimpleGrid thirdFloorGrid;
      private final WoodlandMansionPieces.SimpleGrid[] floorRooms;
      private final int entranceX;
      private final int entranceY;

      public Grid(Random pRandom) {
         this.random = pRandom;
         int i = 11;
         this.entranceX = 7;
         this.entranceY = 4;
         this.baseGrid = new WoodlandMansionPieces.SimpleGrid(11, 11, 5);
         this.baseGrid.set(this.entranceX, this.entranceY, this.entranceX + 1, this.entranceY + 1, 3);
         this.baseGrid.set(this.entranceX - 1, this.entranceY, this.entranceX - 1, this.entranceY + 1, 2);
         this.baseGrid.set(this.entranceX + 2, this.entranceY - 2, this.entranceX + 3, this.entranceY + 3, 5);
         this.baseGrid.set(this.entranceX + 1, this.entranceY - 2, this.entranceX + 1, this.entranceY - 1, 1);
         this.baseGrid.set(this.entranceX + 1, this.entranceY + 2, this.entranceX + 1, this.entranceY + 3, 1);
         this.baseGrid.set(this.entranceX - 1, this.entranceY - 1, 1);
         this.baseGrid.set(this.entranceX - 1, this.entranceY + 2, 1);
         this.baseGrid.set(0, 0, 11, 1, 5);
         this.baseGrid.set(0, 9, 11, 11, 5);
         this.recursiveCorridor(this.baseGrid, this.entranceX, this.entranceY - 2, Direction.WEST, 6);
         this.recursiveCorridor(this.baseGrid, this.entranceX, this.entranceY + 3, Direction.WEST, 6);
         this.recursiveCorridor(this.baseGrid, this.entranceX - 2, this.entranceY - 1, Direction.WEST, 3);
         this.recursiveCorridor(this.baseGrid, this.entranceX - 2, this.entranceY + 2, Direction.WEST, 3);

         while(this.cleanEdges(this.baseGrid)) {
         }

         this.floorRooms = new WoodlandMansionPieces.SimpleGrid[3];
         this.floorRooms[0] = new WoodlandMansionPieces.SimpleGrid(11, 11, 5);
         this.floorRooms[1] = new WoodlandMansionPieces.SimpleGrid(11, 11, 5);
         this.floorRooms[2] = new WoodlandMansionPieces.SimpleGrid(11, 11, 5);
         this.identifyRooms(this.baseGrid, this.floorRooms[0]);
         this.identifyRooms(this.baseGrid, this.floorRooms[1]);
         this.floorRooms[0].set(this.entranceX + 1, this.entranceY, this.entranceX + 1, this.entranceY + 1, 8388608);
         this.floorRooms[1].set(this.entranceX + 1, this.entranceY, this.entranceX + 1, this.entranceY + 1, 8388608);
         this.thirdFloorGrid = new WoodlandMansionPieces.SimpleGrid(this.baseGrid.width, this.baseGrid.height, 5);
         this.setupThirdFloor();
         this.identifyRooms(this.thirdFloorGrid, this.floorRooms[2]);
      }

      public static boolean isHouse(WoodlandMansionPieces.SimpleGrid p_191109_0_, int p_191109_1_, int p_191109_2_) {
         int i = p_191109_0_.get(p_191109_1_, p_191109_2_);
         return i == 1 || i == 2 || i == 3 || i == 4;
      }

      public boolean isRoomId(WoodlandMansionPieces.SimpleGrid p_191114_1_, int p_191114_2_, int p_191114_3_, int p_191114_4_, int p_191114_5_) {
         return (this.floorRooms[p_191114_4_].get(p_191114_2_, p_191114_3_) & '\uffff') == p_191114_5_;
      }

      @Nullable
      public Direction get1x2RoomDirection(WoodlandMansionPieces.SimpleGrid p_191113_1_, int p_191113_2_, int p_191113_3_, int p_191113_4_, int p_191113_5_) {
         for(Direction direction : Direction.Plane.HORIZONTAL) {
            if (this.isRoomId(p_191113_1_, p_191113_2_ + direction.getStepX(), p_191113_3_ + direction.getStepZ(), p_191113_4_, p_191113_5_)) {
               return direction;
            }
         }

         return null;
      }

      private void recursiveCorridor(WoodlandMansionPieces.SimpleGrid p_191110_1_, int p_191110_2_, int p_191110_3_, Direction p_191110_4_, int p_191110_5_) {
         if (p_191110_5_ > 0) {
            p_191110_1_.set(p_191110_2_, p_191110_3_, 1);
            p_191110_1_.setif(p_191110_2_ + p_191110_4_.getStepX(), p_191110_3_ + p_191110_4_.getStepZ(), 0, 1);

            for(int i = 0; i < 8; ++i) {
               Direction direction = Direction.from2DDataValue(this.random.nextInt(4));
               if (direction != p_191110_4_.getOpposite() && (direction != Direction.EAST || !this.random.nextBoolean())) {
                  int j = p_191110_2_ + p_191110_4_.getStepX();
                  int k = p_191110_3_ + p_191110_4_.getStepZ();
                  if (p_191110_1_.get(j + direction.getStepX(), k + direction.getStepZ()) == 0 && p_191110_1_.get(j + direction.getStepX() * 2, k + direction.getStepZ() * 2) == 0) {
                     this.recursiveCorridor(p_191110_1_, p_191110_2_ + p_191110_4_.getStepX() + direction.getStepX(), p_191110_3_ + p_191110_4_.getStepZ() + direction.getStepZ(), direction, p_191110_5_ - 1);
                     break;
                  }
               }
            }

            Direction direction1 = p_191110_4_.getClockWise();
            Direction direction2 = p_191110_4_.getCounterClockWise();
            p_191110_1_.setif(p_191110_2_ + direction1.getStepX(), p_191110_3_ + direction1.getStepZ(), 0, 2);
            p_191110_1_.setif(p_191110_2_ + direction2.getStepX(), p_191110_3_ + direction2.getStepZ(), 0, 2);
            p_191110_1_.setif(p_191110_2_ + p_191110_4_.getStepX() + direction1.getStepX(), p_191110_3_ + p_191110_4_.getStepZ() + direction1.getStepZ(), 0, 2);
            p_191110_1_.setif(p_191110_2_ + p_191110_4_.getStepX() + direction2.getStepX(), p_191110_3_ + p_191110_4_.getStepZ() + direction2.getStepZ(), 0, 2);
            p_191110_1_.setif(p_191110_2_ + p_191110_4_.getStepX() * 2, p_191110_3_ + p_191110_4_.getStepZ() * 2, 0, 2);
            p_191110_1_.setif(p_191110_2_ + direction1.getStepX() * 2, p_191110_3_ + direction1.getStepZ() * 2, 0, 2);
            p_191110_1_.setif(p_191110_2_ + direction2.getStepX() * 2, p_191110_3_ + direction2.getStepZ() * 2, 0, 2);
         }
      }

      private boolean cleanEdges(WoodlandMansionPieces.SimpleGrid pGrid) {
         boolean flag = false;

         for(int i = 0; i < pGrid.height; ++i) {
            for(int j = 0; j < pGrid.width; ++j) {
               if (pGrid.get(j, i) == 0) {
                  int k = 0;
                  k = k + (isHouse(pGrid, j + 1, i) ? 1 : 0);
                  k = k + (isHouse(pGrid, j - 1, i) ? 1 : 0);
                  k = k + (isHouse(pGrid, j, i + 1) ? 1 : 0);
                  k = k + (isHouse(pGrid, j, i - 1) ? 1 : 0);
                  if (k >= 3) {
                     pGrid.set(j, i, 2);
                     flag = true;
                  } else if (k == 2) {
                     int l = 0;
                     l = l + (isHouse(pGrid, j + 1, i + 1) ? 1 : 0);
                     l = l + (isHouse(pGrid, j - 1, i + 1) ? 1 : 0);
                     l = l + (isHouse(pGrid, j + 1, i - 1) ? 1 : 0);
                     l = l + (isHouse(pGrid, j - 1, i - 1) ? 1 : 0);
                     if (l <= 1) {
                        pGrid.set(j, i, 2);
                        flag = true;
                     }
                  }
               }
            }
         }

         return flag;
      }

      private void setupThirdFloor() {
         List<Tuple<Integer, Integer>> list = Lists.newArrayList();
         WoodlandMansionPieces.SimpleGrid woodlandmansionpieces$simplegrid = this.floorRooms[1];

         for(int i = 0; i < this.thirdFloorGrid.height; ++i) {
            for(int j = 0; j < this.thirdFloorGrid.width; ++j) {
               int k = woodlandmansionpieces$simplegrid.get(j, i);
               int l = k & 983040;
               if (l == 131072 && (k & 2097152) == 2097152) {
                  list.add(new Tuple<>(j, i));
               }
            }
         }

         if (list.isEmpty()) {
            this.thirdFloorGrid.set(0, 0, this.thirdFloorGrid.width, this.thirdFloorGrid.height, 5);
         } else {
            Tuple<Integer, Integer> tuple = list.get(this.random.nextInt(list.size()));
            int l1 = woodlandmansionpieces$simplegrid.get(tuple.getA(), tuple.getB());
            woodlandmansionpieces$simplegrid.set(tuple.getA(), tuple.getB(), l1 | 4194304);
            Direction direction1 = this.get1x2RoomDirection(this.baseGrid, tuple.getA(), tuple.getB(), 1, l1 & '\uffff');
            int i2 = tuple.getA() + direction1.getStepX();
            int i1 = tuple.getB() + direction1.getStepZ();

            for(int j1 = 0; j1 < this.thirdFloorGrid.height; ++j1) {
               for(int k1 = 0; k1 < this.thirdFloorGrid.width; ++k1) {
                  if (!isHouse(this.baseGrid, k1, j1)) {
                     this.thirdFloorGrid.set(k1, j1, 5);
                  } else if (k1 == tuple.getA() && j1 == tuple.getB()) {
                     this.thirdFloorGrid.set(k1, j1, 3);
                  } else if (k1 == i2 && j1 == i1) {
                     this.thirdFloorGrid.set(k1, j1, 3);
                     this.floorRooms[2].set(k1, j1, 8388608);
                  }
               }
            }

            List<Direction> list1 = Lists.newArrayList();

            for(Direction direction : Direction.Plane.HORIZONTAL) {
               if (this.thirdFloorGrid.get(i2 + direction.getStepX(), i1 + direction.getStepZ()) == 0) {
                  list1.add(direction);
               }
            }

            if (list1.isEmpty()) {
               this.thirdFloorGrid.set(0, 0, this.thirdFloorGrid.width, this.thirdFloorGrid.height, 5);
               woodlandmansionpieces$simplegrid.set(tuple.getA(), tuple.getB(), l1);
            } else {
               Direction direction2 = list1.get(this.random.nextInt(list1.size()));
               this.recursiveCorridor(this.thirdFloorGrid, i2 + direction2.getStepX(), i1 + direction2.getStepZ(), direction2, 4);

               while(this.cleanEdges(this.thirdFloorGrid)) {
               }

            }
         }
      }

      private void identifyRooms(WoodlandMansionPieces.SimpleGrid p_191116_1_, WoodlandMansionPieces.SimpleGrid p_191116_2_) {
         List<Tuple<Integer, Integer>> list = Lists.newArrayList();

         for(int i = 0; i < p_191116_1_.height; ++i) {
            for(int j = 0; j < p_191116_1_.width; ++j) {
               if (p_191116_1_.get(j, i) == 2) {
                  list.add(new Tuple<>(j, i));
               }
            }
         }

         Collections.shuffle(list, this.random);
         int k3 = 10;

         for(Tuple<Integer, Integer> tuple : list) {
            int k = tuple.getA();
            int l = tuple.getB();
            if (p_191116_2_.get(k, l) == 0) {
               int i1 = k;
               int j1 = k;
               int k1 = l;
               int l1 = l;
               int i2 = 65536;
               if (p_191116_2_.get(k + 1, l) == 0 && p_191116_2_.get(k, l + 1) == 0 && p_191116_2_.get(k + 1, l + 1) == 0 && p_191116_1_.get(k + 1, l) == 2 && p_191116_1_.get(k, l + 1) == 2 && p_191116_1_.get(k + 1, l + 1) == 2) {
                  j1 = k + 1;
                  l1 = l + 1;
                  i2 = 262144;
               } else if (p_191116_2_.get(k - 1, l) == 0 && p_191116_2_.get(k, l + 1) == 0 && p_191116_2_.get(k - 1, l + 1) == 0 && p_191116_1_.get(k - 1, l) == 2 && p_191116_1_.get(k, l + 1) == 2 && p_191116_1_.get(k - 1, l + 1) == 2) {
                  i1 = k - 1;
                  l1 = l + 1;
                  i2 = 262144;
               } else if (p_191116_2_.get(k - 1, l) == 0 && p_191116_2_.get(k, l - 1) == 0 && p_191116_2_.get(k - 1, l - 1) == 0 && p_191116_1_.get(k - 1, l) == 2 && p_191116_1_.get(k, l - 1) == 2 && p_191116_1_.get(k - 1, l - 1) == 2) {
                  i1 = k - 1;
                  k1 = l - 1;
                  i2 = 262144;
               } else if (p_191116_2_.get(k + 1, l) == 0 && p_191116_1_.get(k + 1, l) == 2) {
                  j1 = k + 1;
                  i2 = 131072;
               } else if (p_191116_2_.get(k, l + 1) == 0 && p_191116_1_.get(k, l + 1) == 2) {
                  l1 = l + 1;
                  i2 = 131072;
               } else if (p_191116_2_.get(k - 1, l) == 0 && p_191116_1_.get(k - 1, l) == 2) {
                  i1 = k - 1;
                  i2 = 131072;
               } else if (p_191116_2_.get(k, l - 1) == 0 && p_191116_1_.get(k, l - 1) == 2) {
                  k1 = l - 1;
                  i2 = 131072;
               }

               int j2 = this.random.nextBoolean() ? i1 : j1;
               int k2 = this.random.nextBoolean() ? k1 : l1;
               int l2 = 2097152;
               if (!p_191116_1_.edgesTo(j2, k2, 1)) {
                  j2 = j2 == i1 ? j1 : i1;
                  k2 = k2 == k1 ? l1 : k1;
                  if (!p_191116_1_.edgesTo(j2, k2, 1)) {
                     k2 = k2 == k1 ? l1 : k1;
                     if (!p_191116_1_.edgesTo(j2, k2, 1)) {
                        j2 = j2 == i1 ? j1 : i1;
                        k2 = k2 == k1 ? l1 : k1;
                        if (!p_191116_1_.edgesTo(j2, k2, 1)) {
                           l2 = 0;
                           j2 = i1;
                           k2 = k1;
                        }
                     }
                  }
               }

               for(int i3 = k1; i3 <= l1; ++i3) {
                  for(int j3 = i1; j3 <= j1; ++j3) {
                     if (j3 == j2 && i3 == k2) {
                        p_191116_2_.set(j3, i3, 1048576 | l2 | i2 | k3);
                     } else {
                        p_191116_2_.set(j3, i3, i2 | k3);
                     }
                  }
               }

               ++k3;
            }
         }

      }
   }

   public static class MansionTemplate extends TemplateStructurePiece {
      private final String templateName;
      private final Rotation rotation;
      private final Mirror mirror;

      public MansionTemplate(TemplateManager pStructureManager, String pName, BlockPos pPos, Rotation pRotation) {
         this(pStructureManager, pName, pPos, pRotation, Mirror.NONE);
      }

      public MansionTemplate(TemplateManager pStructureManager, String pName, BlockPos pPos, Rotation pRotation, Mirror pMirror) {
         super(IStructurePieceType.WOODLAND_MANSION_PIECE, 0);
         this.templateName = pName;
         this.templatePosition = pPos;
         this.rotation = pRotation;
         this.mirror = pMirror;
         this.loadTemplate(pStructureManager);
      }

      public MansionTemplate(TemplateManager p_i50615_1_, CompoundNBT p_i50615_2_) {
         super(IStructurePieceType.WOODLAND_MANSION_PIECE, p_i50615_2_);
         this.templateName = p_i50615_2_.getString("Template");
         this.rotation = Rotation.valueOf(p_i50615_2_.getString("Rot"));
         this.mirror = Mirror.valueOf(p_i50615_2_.getString("Mi"));
         this.loadTemplate(p_i50615_1_);
      }

      private void loadTemplate(TemplateManager p_191081_1_) {
         Template template = p_191081_1_.getOrCreate(new ResourceLocation("woodland_mansion/" + this.templateName));
         PlacementSettings placementsettings = (new PlacementSettings()).setIgnoreEntities(true).setRotation(this.rotation).setMirror(this.mirror).addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK);
         this.setup(template, this.templatePosition, placementsettings);
      }

      protected void addAdditionalSaveData(CompoundNBT p_143011_1_) {
         super.addAdditionalSaveData(p_143011_1_);
         p_143011_1_.putString("Template", this.templateName);
         p_143011_1_.putString("Rot", this.placeSettings.getRotation().name());
         p_143011_1_.putString("Mi", this.placeSettings.getMirror().name());
      }

      protected void handleDataMarker(String pFunction, BlockPos pPos, IServerWorld pLevel, Random pRandom, MutableBoundingBox pSbb) {
         if (pFunction.startsWith("Chest")) {
            Rotation rotation = this.placeSettings.getRotation();
            BlockState blockstate = Blocks.CHEST.defaultBlockState();
            if ("ChestWest".equals(pFunction)) {
               blockstate = blockstate.setValue(ChestBlock.FACING, rotation.rotate(Direction.WEST));
            } else if ("ChestEast".equals(pFunction)) {
               blockstate = blockstate.setValue(ChestBlock.FACING, rotation.rotate(Direction.EAST));
            } else if ("ChestSouth".equals(pFunction)) {
               blockstate = blockstate.setValue(ChestBlock.FACING, rotation.rotate(Direction.SOUTH));
            } else if ("ChestNorth".equals(pFunction)) {
               blockstate = blockstate.setValue(ChestBlock.FACING, rotation.rotate(Direction.NORTH));
            }

            this.createChest(pLevel, pSbb, pRandom, pPos, LootTables.WOODLAND_MANSION, blockstate);
         } else {
            AbstractIllagerEntity abstractillagerentity;
            switch(pFunction) {
            case "Mage":
               abstractillagerentity = EntityType.EVOKER.create(pLevel.getLevel());
               break;
            case "Warrior":
               abstractillagerentity = EntityType.VINDICATOR.create(pLevel.getLevel());
               break;
            default:
               return;
            }

            abstractillagerentity.setPersistenceRequired();
            abstractillagerentity.moveTo(pPos, 0.0F, 0.0F);
            abstractillagerentity.finalizeSpawn(pLevel, pLevel.getCurrentDifficultyAt(abstractillagerentity.blockPosition()), SpawnReason.STRUCTURE, (ILivingEntityData)null, (CompoundNBT)null);
            pLevel.addFreshEntityWithPassengers(abstractillagerentity);
            pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 2);
         }

      }
   }

   static class PlacementData {
      public Rotation rotation;
      public BlockPos position;
      public String wallType;

      private PlacementData() {
      }
   }

   static class Placer {
      private final TemplateManager structureManager;
      private final Random random;
      private int startX;
      private int startY;

      public Placer(TemplateManager pStructureManager, Random pRandom) {
         this.structureManager = pStructureManager;
         this.random = pRandom;
      }

      public void createMansion(BlockPos pPos, Rotation pRotation, List<WoodlandMansionPieces.MansionTemplate> pPieces, WoodlandMansionPieces.Grid pGrid) {
         WoodlandMansionPieces.PlacementData woodlandmansionpieces$placementdata = new WoodlandMansionPieces.PlacementData();
         woodlandmansionpieces$placementdata.position = pPos;
         woodlandmansionpieces$placementdata.rotation = pRotation;
         woodlandmansionpieces$placementdata.wallType = "wall_flat";
         WoodlandMansionPieces.PlacementData woodlandmansionpieces$placementdata1 = new WoodlandMansionPieces.PlacementData();
         this.entrance(pPieces, woodlandmansionpieces$placementdata);
         woodlandmansionpieces$placementdata1.position = woodlandmansionpieces$placementdata.position.above(8);
         woodlandmansionpieces$placementdata1.rotation = woodlandmansionpieces$placementdata.rotation;
         woodlandmansionpieces$placementdata1.wallType = "wall_window";
         if (!pPieces.isEmpty()) {
         }

         WoodlandMansionPieces.SimpleGrid woodlandmansionpieces$simplegrid = pGrid.baseGrid;
         WoodlandMansionPieces.SimpleGrid woodlandmansionpieces$simplegrid1 = pGrid.thirdFloorGrid;
         this.startX = pGrid.entranceX + 1;
         this.startY = pGrid.entranceY + 1;
         int i = pGrid.entranceX + 1;
         int j = pGrid.entranceY;
         this.traverseOuterWalls(pPieces, woodlandmansionpieces$placementdata, woodlandmansionpieces$simplegrid, Direction.SOUTH, this.startX, this.startY, i, j);
         this.traverseOuterWalls(pPieces, woodlandmansionpieces$placementdata1, woodlandmansionpieces$simplegrid, Direction.SOUTH, this.startX, this.startY, i, j);
         WoodlandMansionPieces.PlacementData woodlandmansionpieces$placementdata2 = new WoodlandMansionPieces.PlacementData();
         woodlandmansionpieces$placementdata2.position = woodlandmansionpieces$placementdata.position.above(19);
         woodlandmansionpieces$placementdata2.rotation = woodlandmansionpieces$placementdata.rotation;
         woodlandmansionpieces$placementdata2.wallType = "wall_window";
         boolean flag = false;

         for(int k = 0; k < woodlandmansionpieces$simplegrid1.height && !flag; ++k) {
            for(int l = woodlandmansionpieces$simplegrid1.width - 1; l >= 0 && !flag; --l) {
               if (WoodlandMansionPieces.Grid.isHouse(woodlandmansionpieces$simplegrid1, l, k)) {
                  woodlandmansionpieces$placementdata2.position = woodlandmansionpieces$placementdata2.position.relative(pRotation.rotate(Direction.SOUTH), 8 + (k - this.startY) * 8);
                  woodlandmansionpieces$placementdata2.position = woodlandmansionpieces$placementdata2.position.relative(pRotation.rotate(Direction.EAST), (l - this.startX) * 8);
                  this.traverseWallPiece(pPieces, woodlandmansionpieces$placementdata2);
                  this.traverseOuterWalls(pPieces, woodlandmansionpieces$placementdata2, woodlandmansionpieces$simplegrid1, Direction.SOUTH, l, k, l, k);
                  flag = true;
               }
            }
         }

         this.createRoof(pPieces, pPos.above(16), pRotation, woodlandmansionpieces$simplegrid, woodlandmansionpieces$simplegrid1);
         this.createRoof(pPieces, pPos.above(27), pRotation, woodlandmansionpieces$simplegrid1, (WoodlandMansionPieces.SimpleGrid)null);
         if (!pPieces.isEmpty()) {
         }

         WoodlandMansionPieces.RoomCollection[] awoodlandmansionpieces$roomcollection = new WoodlandMansionPieces.RoomCollection[]{new WoodlandMansionPieces.FirstFloor(), new WoodlandMansionPieces.SecondFloor(), new WoodlandMansionPieces.ThirdFloor()};

         for(int l2 = 0; l2 < 3; ++l2) {
            BlockPos blockpos = pPos.above(8 * l2 + (l2 == 2 ? 3 : 0));
            WoodlandMansionPieces.SimpleGrid woodlandmansionpieces$simplegrid2 = pGrid.floorRooms[l2];
            WoodlandMansionPieces.SimpleGrid woodlandmansionpieces$simplegrid3 = l2 == 2 ? woodlandmansionpieces$simplegrid1 : woodlandmansionpieces$simplegrid;
            String s = l2 == 0 ? "carpet_south_1" : "carpet_south_2";
            String s1 = l2 == 0 ? "carpet_west_1" : "carpet_west_2";

            for(int i1 = 0; i1 < woodlandmansionpieces$simplegrid3.height; ++i1) {
               for(int j1 = 0; j1 < woodlandmansionpieces$simplegrid3.width; ++j1) {
                  if (woodlandmansionpieces$simplegrid3.get(j1, i1) == 1) {
                     BlockPos blockpos1 = blockpos.relative(pRotation.rotate(Direction.SOUTH), 8 + (i1 - this.startY) * 8);
                     blockpos1 = blockpos1.relative(pRotation.rotate(Direction.EAST), (j1 - this.startX) * 8);
                     pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "corridor_floor", blockpos1, pRotation));
                     if (woodlandmansionpieces$simplegrid3.get(j1, i1 - 1) == 1 || (woodlandmansionpieces$simplegrid2.get(j1, i1 - 1) & 8388608) == 8388608) {
                        pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "carpet_north", blockpos1.relative(pRotation.rotate(Direction.EAST), 1).above(), pRotation));
                     }

                     if (woodlandmansionpieces$simplegrid3.get(j1 + 1, i1) == 1 || (woodlandmansionpieces$simplegrid2.get(j1 + 1, i1) & 8388608) == 8388608) {
                        pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "carpet_east", blockpos1.relative(pRotation.rotate(Direction.SOUTH), 1).relative(pRotation.rotate(Direction.EAST), 5).above(), pRotation));
                     }

                     if (woodlandmansionpieces$simplegrid3.get(j1, i1 + 1) == 1 || (woodlandmansionpieces$simplegrid2.get(j1, i1 + 1) & 8388608) == 8388608) {
                        pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, s, blockpos1.relative(pRotation.rotate(Direction.SOUTH), 5).relative(pRotation.rotate(Direction.WEST), 1), pRotation));
                     }

                     if (woodlandmansionpieces$simplegrid3.get(j1 - 1, i1) == 1 || (woodlandmansionpieces$simplegrid2.get(j1 - 1, i1) & 8388608) == 8388608) {
                        pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, s1, blockpos1.relative(pRotation.rotate(Direction.WEST), 1).relative(pRotation.rotate(Direction.NORTH), 1), pRotation));
                     }
                  }
               }
            }

            String s2 = l2 == 0 ? "indoors_wall_1" : "indoors_wall_2";
            String s3 = l2 == 0 ? "indoors_door_1" : "indoors_door_2";
            List<Direction> list = Lists.newArrayList();

            for(int k1 = 0; k1 < woodlandmansionpieces$simplegrid3.height; ++k1) {
               for(int l1 = 0; l1 < woodlandmansionpieces$simplegrid3.width; ++l1) {
                  boolean flag1 = l2 == 2 && woodlandmansionpieces$simplegrid3.get(l1, k1) == 3;
                  if (woodlandmansionpieces$simplegrid3.get(l1, k1) == 2 || flag1) {
                     int i2 = woodlandmansionpieces$simplegrid2.get(l1, k1);
                     int j2 = i2 & 983040;
                     int k2 = i2 & '\uffff';
                     flag1 = flag1 && (i2 & 8388608) == 8388608;
                     list.clear();
                     if ((i2 & 2097152) == 2097152) {
                        for(Direction direction : Direction.Plane.HORIZONTAL) {
                           if (woodlandmansionpieces$simplegrid3.get(l1 + direction.getStepX(), k1 + direction.getStepZ()) == 1) {
                              list.add(direction);
                           }
                        }
                     }

                     Direction direction1 = null;
                     if (!list.isEmpty()) {
                        direction1 = list.get(this.random.nextInt(list.size()));
                     } else if ((i2 & 1048576) == 1048576) {
                        direction1 = Direction.UP;
                     }

                     BlockPos blockpos3 = blockpos.relative(pRotation.rotate(Direction.SOUTH), 8 + (k1 - this.startY) * 8);
                     blockpos3 = blockpos3.relative(pRotation.rotate(Direction.EAST), -1 + (l1 - this.startX) * 8);
                     if (WoodlandMansionPieces.Grid.isHouse(woodlandmansionpieces$simplegrid3, l1 - 1, k1) && !pGrid.isRoomId(woodlandmansionpieces$simplegrid3, l1 - 1, k1, l2, k2)) {
                        pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, direction1 == Direction.WEST ? s3 : s2, blockpos3, pRotation));
                     }

                     if (woodlandmansionpieces$simplegrid3.get(l1 + 1, k1) == 1 && !flag1) {
                        BlockPos blockpos2 = blockpos3.relative(pRotation.rotate(Direction.EAST), 8);
                        pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, direction1 == Direction.EAST ? s3 : s2, blockpos2, pRotation));
                     }

                     if (WoodlandMansionPieces.Grid.isHouse(woodlandmansionpieces$simplegrid3, l1, k1 + 1) && !pGrid.isRoomId(woodlandmansionpieces$simplegrid3, l1, k1 + 1, l2, k2)) {
                        BlockPos blockpos4 = blockpos3.relative(pRotation.rotate(Direction.SOUTH), 7);
                        blockpos4 = blockpos4.relative(pRotation.rotate(Direction.EAST), 7);
                        pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, direction1 == Direction.SOUTH ? s3 : s2, blockpos4, pRotation.getRotated(Rotation.CLOCKWISE_90)));
                     }

                     if (woodlandmansionpieces$simplegrid3.get(l1, k1 - 1) == 1 && !flag1) {
                        BlockPos blockpos5 = blockpos3.relative(pRotation.rotate(Direction.NORTH), 1);
                        blockpos5 = blockpos5.relative(pRotation.rotate(Direction.EAST), 7);
                        pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, direction1 == Direction.NORTH ? s3 : s2, blockpos5, pRotation.getRotated(Rotation.CLOCKWISE_90)));
                     }

                     if (j2 == 65536) {
                        this.addRoom1x1(pPieces, blockpos3, pRotation, direction1, awoodlandmansionpieces$roomcollection[l2]);
                     } else if (j2 == 131072 && direction1 != null) {
                        Direction direction3 = pGrid.get1x2RoomDirection(woodlandmansionpieces$simplegrid3, l1, k1, l2, k2);
                        boolean flag2 = (i2 & 4194304) == 4194304;
                        this.addRoom1x2(pPieces, blockpos3, pRotation, direction3, direction1, awoodlandmansionpieces$roomcollection[l2], flag2);
                     } else if (j2 == 262144 && direction1 != null && direction1 != Direction.UP) {
                        Direction direction2 = direction1.getClockWise();
                        if (!pGrid.isRoomId(woodlandmansionpieces$simplegrid3, l1 + direction2.getStepX(), k1 + direction2.getStepZ(), l2, k2)) {
                           direction2 = direction2.getOpposite();
                        }

                        this.addRoom2x2(pPieces, blockpos3, pRotation, direction2, direction1, awoodlandmansionpieces$roomcollection[l2]);
                     } else if (j2 == 262144 && direction1 == Direction.UP) {
                        this.addRoom2x2Secret(pPieces, blockpos3, pRotation, awoodlandmansionpieces$roomcollection[l2]);
                     }
                  }
               }
            }
         }

      }

      private void traverseOuterWalls(List<WoodlandMansionPieces.MansionTemplate> pPieces, WoodlandMansionPieces.PlacementData pData, WoodlandMansionPieces.SimpleGrid pGrid, Direction pDirection, int pStartX, int pStartY, int p_191130_7_, int p_191130_8_) {
         int i = pStartX;
         int j = pStartY;
         Direction direction = pDirection;

         do {
            if (!WoodlandMansionPieces.Grid.isHouse(pGrid, i + pDirection.getStepX(), j + pDirection.getStepZ())) {
               this.traverseTurn(pPieces, pData);
               pDirection = pDirection.getClockWise();
               if (i != p_191130_7_ || j != p_191130_8_ || direction != pDirection) {
                  this.traverseWallPiece(pPieces, pData);
               }
            } else if (WoodlandMansionPieces.Grid.isHouse(pGrid, i + pDirection.getStepX(), j + pDirection.getStepZ()) && WoodlandMansionPieces.Grid.isHouse(pGrid, i + pDirection.getStepX() + pDirection.getCounterClockWise().getStepX(), j + pDirection.getStepZ() + pDirection.getCounterClockWise().getStepZ())) {
               this.traverseInnerTurn(pPieces, pData);
               i += pDirection.getStepX();
               j += pDirection.getStepZ();
               pDirection = pDirection.getCounterClockWise();
            } else {
               i += pDirection.getStepX();
               j += pDirection.getStepZ();
               if (i != p_191130_7_ || j != p_191130_8_ || direction != pDirection) {
                  this.traverseWallPiece(pPieces, pData);
               }
            }
         } while(i != p_191130_7_ || j != p_191130_8_ || direction != pDirection);

      }

      private void createRoof(List<WoodlandMansionPieces.MansionTemplate> pPieces, BlockPos pPos, Rotation pRotation, WoodlandMansionPieces.SimpleGrid p_191123_4_, @Nullable WoodlandMansionPieces.SimpleGrid p_191123_5_) {
         for(int i = 0; i < p_191123_4_.height; ++i) {
            for(int j = 0; j < p_191123_4_.width; ++j) {
               BlockPos lvt_8_3_ = pPos.relative(pRotation.rotate(Direction.SOUTH), 8 + (i - this.startY) * 8);
               lvt_8_3_ = lvt_8_3_.relative(pRotation.rotate(Direction.EAST), (j - this.startX) * 8);
               boolean flag = p_191123_5_ != null && WoodlandMansionPieces.Grid.isHouse(p_191123_5_, j, i);
               if (WoodlandMansionPieces.Grid.isHouse(p_191123_4_, j, i) && !flag) {
                  pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "roof", lvt_8_3_.above(3), pRotation));
                  if (!WoodlandMansionPieces.Grid.isHouse(p_191123_4_, j + 1, i)) {
                     BlockPos blockpos1 = lvt_8_3_.relative(pRotation.rotate(Direction.EAST), 6);
                     pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "roof_front", blockpos1, pRotation));
                  }

                  if (!WoodlandMansionPieces.Grid.isHouse(p_191123_4_, j - 1, i)) {
                     BlockPos blockpos5 = lvt_8_3_.relative(pRotation.rotate(Direction.EAST), 0);
                     blockpos5 = blockpos5.relative(pRotation.rotate(Direction.SOUTH), 7);
                     pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "roof_front", blockpos5, pRotation.getRotated(Rotation.CLOCKWISE_180)));
                  }

                  if (!WoodlandMansionPieces.Grid.isHouse(p_191123_4_, j, i - 1)) {
                     BlockPos blockpos6 = lvt_8_3_.relative(pRotation.rotate(Direction.WEST), 1);
                     pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "roof_front", blockpos6, pRotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                  }

                  if (!WoodlandMansionPieces.Grid.isHouse(p_191123_4_, j, i + 1)) {
                     BlockPos blockpos7 = lvt_8_3_.relative(pRotation.rotate(Direction.EAST), 6);
                     blockpos7 = blockpos7.relative(pRotation.rotate(Direction.SOUTH), 6);
                     pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "roof_front", blockpos7, pRotation.getRotated(Rotation.CLOCKWISE_90)));
                  }
               }
            }
         }

         if (p_191123_5_ != null) {
            for(int k = 0; k < p_191123_4_.height; ++k) {
               for(int i1 = 0; i1 < p_191123_4_.width; ++i1) {
                  BlockPos blockpos3 = pPos.relative(pRotation.rotate(Direction.SOUTH), 8 + (k - this.startY) * 8);
                  blockpos3 = blockpos3.relative(pRotation.rotate(Direction.EAST), (i1 - this.startX) * 8);
                  boolean flag1 = WoodlandMansionPieces.Grid.isHouse(p_191123_5_, i1, k);
                  if (WoodlandMansionPieces.Grid.isHouse(p_191123_4_, i1, k) && flag1) {
                     if (!WoodlandMansionPieces.Grid.isHouse(p_191123_4_, i1 + 1, k)) {
                        BlockPos blockpos8 = blockpos3.relative(pRotation.rotate(Direction.EAST), 7);
                        pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "small_wall", blockpos8, pRotation));
                     }

                     if (!WoodlandMansionPieces.Grid.isHouse(p_191123_4_, i1 - 1, k)) {
                        BlockPos blockpos9 = blockpos3.relative(pRotation.rotate(Direction.WEST), 1);
                        blockpos9 = blockpos9.relative(pRotation.rotate(Direction.SOUTH), 6);
                        pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "small_wall", blockpos9, pRotation.getRotated(Rotation.CLOCKWISE_180)));
                     }

                     if (!WoodlandMansionPieces.Grid.isHouse(p_191123_4_, i1, k - 1)) {
                        BlockPos blockpos10 = blockpos3.relative(pRotation.rotate(Direction.WEST), 0);
                        blockpos10 = blockpos10.relative(pRotation.rotate(Direction.NORTH), 1);
                        pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "small_wall", blockpos10, pRotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                     }

                     if (!WoodlandMansionPieces.Grid.isHouse(p_191123_4_, i1, k + 1)) {
                        BlockPos blockpos11 = blockpos3.relative(pRotation.rotate(Direction.EAST), 6);
                        blockpos11 = blockpos11.relative(pRotation.rotate(Direction.SOUTH), 7);
                        pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "small_wall", blockpos11, pRotation.getRotated(Rotation.CLOCKWISE_90)));
                     }

                     if (!WoodlandMansionPieces.Grid.isHouse(p_191123_4_, i1 + 1, k)) {
                        if (!WoodlandMansionPieces.Grid.isHouse(p_191123_4_, i1, k - 1)) {
                           BlockPos blockpos12 = blockpos3.relative(pRotation.rotate(Direction.EAST), 7);
                           blockpos12 = blockpos12.relative(pRotation.rotate(Direction.NORTH), 2);
                           pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "small_wall_corner", blockpos12, pRotation));
                        }

                        if (!WoodlandMansionPieces.Grid.isHouse(p_191123_4_, i1, k + 1)) {
                           BlockPos blockpos13 = blockpos3.relative(pRotation.rotate(Direction.EAST), 8);
                           blockpos13 = blockpos13.relative(pRotation.rotate(Direction.SOUTH), 7);
                           pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "small_wall_corner", blockpos13, pRotation.getRotated(Rotation.CLOCKWISE_90)));
                        }
                     }

                     if (!WoodlandMansionPieces.Grid.isHouse(p_191123_4_, i1 - 1, k)) {
                        if (!WoodlandMansionPieces.Grid.isHouse(p_191123_4_, i1, k - 1)) {
                           BlockPos blockpos14 = blockpos3.relative(pRotation.rotate(Direction.WEST), 2);
                           blockpos14 = blockpos14.relative(pRotation.rotate(Direction.NORTH), 1);
                           pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "small_wall_corner", blockpos14, pRotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                        }

                        if (!WoodlandMansionPieces.Grid.isHouse(p_191123_4_, i1, k + 1)) {
                           BlockPos blockpos15 = blockpos3.relative(pRotation.rotate(Direction.WEST), 1);
                           blockpos15 = blockpos15.relative(pRotation.rotate(Direction.SOUTH), 8);
                           pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "small_wall_corner", blockpos15, pRotation.getRotated(Rotation.CLOCKWISE_180)));
                        }
                     }
                  }
               }
            }
         }

         for(int l = 0; l < p_191123_4_.height; ++l) {
            for(int j1 = 0; j1 < p_191123_4_.width; ++j1) {
               BlockPos blockpos4 = pPos.relative(pRotation.rotate(Direction.SOUTH), 8 + (l - this.startY) * 8);
               blockpos4 = blockpos4.relative(pRotation.rotate(Direction.EAST), (j1 - this.startX) * 8);
               boolean flag2 = p_191123_5_ != null && WoodlandMansionPieces.Grid.isHouse(p_191123_5_, j1, l);
               if (WoodlandMansionPieces.Grid.isHouse(p_191123_4_, j1, l) && !flag2) {
                  if (!WoodlandMansionPieces.Grid.isHouse(p_191123_4_, j1 + 1, l)) {
                     BlockPos blockpos16 = blockpos4.relative(pRotation.rotate(Direction.EAST), 6);
                     if (!WoodlandMansionPieces.Grid.isHouse(p_191123_4_, j1, l + 1)) {
                        BlockPos blockpos2 = blockpos16.relative(pRotation.rotate(Direction.SOUTH), 6);
                        pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "roof_corner", blockpos2, pRotation));
                     } else if (WoodlandMansionPieces.Grid.isHouse(p_191123_4_, j1 + 1, l + 1)) {
                        BlockPos blockpos18 = blockpos16.relative(pRotation.rotate(Direction.SOUTH), 5);
                        pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "roof_inner_corner", blockpos18, pRotation));
                     }

                     if (!WoodlandMansionPieces.Grid.isHouse(p_191123_4_, j1, l - 1)) {
                        pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "roof_corner", blockpos16, pRotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                     } else if (WoodlandMansionPieces.Grid.isHouse(p_191123_4_, j1 + 1, l - 1)) {
                        BlockPos blockpos19 = blockpos4.relative(pRotation.rotate(Direction.EAST), 9);
                        blockpos19 = blockpos19.relative(pRotation.rotate(Direction.NORTH), 2);
                        pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "roof_inner_corner", blockpos19, pRotation.getRotated(Rotation.CLOCKWISE_90)));
                     }
                  }

                  if (!WoodlandMansionPieces.Grid.isHouse(p_191123_4_, j1 - 1, l)) {
                     BlockPos blockpos17 = blockpos4.relative(pRotation.rotate(Direction.EAST), 0);
                     blockpos17 = blockpos17.relative(pRotation.rotate(Direction.SOUTH), 0);
                     if (!WoodlandMansionPieces.Grid.isHouse(p_191123_4_, j1, l + 1)) {
                        BlockPos blockpos20 = blockpos17.relative(pRotation.rotate(Direction.SOUTH), 6);
                        pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "roof_corner", blockpos20, pRotation.getRotated(Rotation.CLOCKWISE_90)));
                     } else if (WoodlandMansionPieces.Grid.isHouse(p_191123_4_, j1 - 1, l + 1)) {
                        BlockPos blockpos21 = blockpos17.relative(pRotation.rotate(Direction.SOUTH), 8);
                        blockpos21 = blockpos21.relative(pRotation.rotate(Direction.WEST), 3);
                        pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "roof_inner_corner", blockpos21, pRotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                     }

                     if (!WoodlandMansionPieces.Grid.isHouse(p_191123_4_, j1, l - 1)) {
                        pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "roof_corner", blockpos17, pRotation.getRotated(Rotation.CLOCKWISE_180)));
                     } else if (WoodlandMansionPieces.Grid.isHouse(p_191123_4_, j1 - 1, l - 1)) {
                        BlockPos blockpos22 = blockpos17.relative(pRotation.rotate(Direction.SOUTH), 1);
                        pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "roof_inner_corner", blockpos22, pRotation.getRotated(Rotation.CLOCKWISE_180)));
                     }
                  }
               }
            }
         }

      }

      private void entrance(List<WoodlandMansionPieces.MansionTemplate> pPieces, WoodlandMansionPieces.PlacementData pData) {
         Direction direction = pData.rotation.rotate(Direction.WEST);
         pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "entrance", pData.position.relative(direction, 9), pData.rotation));
         pData.position = pData.position.relative(pData.rotation.rotate(Direction.SOUTH), 16);
      }

      private void traverseWallPiece(List<WoodlandMansionPieces.MansionTemplate> pPieces, WoodlandMansionPieces.PlacementData pData) {
         pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, pData.wallType, pData.position.relative(pData.rotation.rotate(Direction.EAST), 7), pData.rotation));
         pData.position = pData.position.relative(pData.rotation.rotate(Direction.SOUTH), 8);
      }

      private void traverseTurn(List<WoodlandMansionPieces.MansionTemplate> pPieces, WoodlandMansionPieces.PlacementData pData) {
         pData.position = pData.position.relative(pData.rotation.rotate(Direction.SOUTH), -1);
         pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, "wall_corner", pData.position, pData.rotation));
         pData.position = pData.position.relative(pData.rotation.rotate(Direction.SOUTH), -7);
         pData.position = pData.position.relative(pData.rotation.rotate(Direction.WEST), -6);
         pData.rotation = pData.rotation.getRotated(Rotation.CLOCKWISE_90);
      }

      private void traverseInnerTurn(List<WoodlandMansionPieces.MansionTemplate> pPieces, WoodlandMansionPieces.PlacementData pData) {
         pData.position = pData.position.relative(pData.rotation.rotate(Direction.SOUTH), 6);
         pData.position = pData.position.relative(pData.rotation.rotate(Direction.EAST), 8);
         pData.rotation = pData.rotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
      }

      private void addRoom1x1(List<WoodlandMansionPieces.MansionTemplate> pPieces, BlockPos pPos, Rotation pRotation, Direction pDirection, WoodlandMansionPieces.RoomCollection pFloorRooms) {
         Rotation rotation = Rotation.NONE;
         String s = pFloorRooms.get1x1(this.random);
         if (pDirection != Direction.EAST) {
            if (pDirection == Direction.NORTH) {
               rotation = rotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
            } else if (pDirection == Direction.WEST) {
               rotation = rotation.getRotated(Rotation.CLOCKWISE_180);
            } else if (pDirection == Direction.SOUTH) {
               rotation = rotation.getRotated(Rotation.CLOCKWISE_90);
            } else {
               s = pFloorRooms.get1x1Secret(this.random);
            }
         }

         BlockPos blockpos = Template.getZeroPositionWithTransform(new BlockPos(1, 0, 0), Mirror.NONE, rotation, 7, 7);
         rotation = rotation.getRotated(pRotation);
         blockpos = blockpos.rotate(pRotation);
         BlockPos blockpos1 = pPos.offset(blockpos.getX(), 0, blockpos.getZ());
         pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, s, blockpos1, rotation));
      }

      private void addRoom1x2(List<WoodlandMansionPieces.MansionTemplate> pPieces, BlockPos pPos, Rotation pRotation, Direction p_191132_4_, Direction p_191132_5_, WoodlandMansionPieces.RoomCollection pFloorRooms, boolean p_191132_7_) {
         if (p_191132_5_ == Direction.EAST && p_191132_4_ == Direction.SOUTH) {
            BlockPos blockpos13 = pPos.relative(pRotation.rotate(Direction.EAST), 1);
            pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, pFloorRooms.get1x2SideEntrance(this.random, p_191132_7_), blockpos13, pRotation));
         } else if (p_191132_5_ == Direction.EAST && p_191132_4_ == Direction.NORTH) {
            BlockPos blockpos12 = pPos.relative(pRotation.rotate(Direction.EAST), 1);
            blockpos12 = blockpos12.relative(pRotation.rotate(Direction.SOUTH), 6);
            pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, pFloorRooms.get1x2SideEntrance(this.random, p_191132_7_), blockpos12, pRotation, Mirror.LEFT_RIGHT));
         } else if (p_191132_5_ == Direction.WEST && p_191132_4_ == Direction.NORTH) {
            BlockPos blockpos11 = pPos.relative(pRotation.rotate(Direction.EAST), 7);
            blockpos11 = blockpos11.relative(pRotation.rotate(Direction.SOUTH), 6);
            pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, pFloorRooms.get1x2SideEntrance(this.random, p_191132_7_), blockpos11, pRotation.getRotated(Rotation.CLOCKWISE_180)));
         } else if (p_191132_5_ == Direction.WEST && p_191132_4_ == Direction.SOUTH) {
            BlockPos blockpos10 = pPos.relative(pRotation.rotate(Direction.EAST), 7);
            pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, pFloorRooms.get1x2SideEntrance(this.random, p_191132_7_), blockpos10, pRotation, Mirror.FRONT_BACK));
         } else if (p_191132_5_ == Direction.SOUTH && p_191132_4_ == Direction.EAST) {
            BlockPos blockpos9 = pPos.relative(pRotation.rotate(Direction.EAST), 1);
            pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, pFloorRooms.get1x2SideEntrance(this.random, p_191132_7_), blockpos9, pRotation.getRotated(Rotation.CLOCKWISE_90), Mirror.LEFT_RIGHT));
         } else if (p_191132_5_ == Direction.SOUTH && p_191132_4_ == Direction.WEST) {
            BlockPos blockpos8 = pPos.relative(pRotation.rotate(Direction.EAST), 7);
            pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, pFloorRooms.get1x2SideEntrance(this.random, p_191132_7_), blockpos8, pRotation.getRotated(Rotation.CLOCKWISE_90)));
         } else if (p_191132_5_ == Direction.NORTH && p_191132_4_ == Direction.WEST) {
            BlockPos blockpos7 = pPos.relative(pRotation.rotate(Direction.EAST), 7);
            blockpos7 = blockpos7.relative(pRotation.rotate(Direction.SOUTH), 6);
            pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, pFloorRooms.get1x2SideEntrance(this.random, p_191132_7_), blockpos7, pRotation.getRotated(Rotation.CLOCKWISE_90), Mirror.FRONT_BACK));
         } else if (p_191132_5_ == Direction.NORTH && p_191132_4_ == Direction.EAST) {
            BlockPos blockpos6 = pPos.relative(pRotation.rotate(Direction.EAST), 1);
            blockpos6 = blockpos6.relative(pRotation.rotate(Direction.SOUTH), 6);
            pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, pFloorRooms.get1x2SideEntrance(this.random, p_191132_7_), blockpos6, pRotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
         } else if (p_191132_5_ == Direction.SOUTH && p_191132_4_ == Direction.NORTH) {
            BlockPos blockpos5 = pPos.relative(pRotation.rotate(Direction.EAST), 1);
            blockpos5 = blockpos5.relative(pRotation.rotate(Direction.NORTH), 8);
            pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, pFloorRooms.get1x2FrontEntrance(this.random, p_191132_7_), blockpos5, pRotation));
         } else if (p_191132_5_ == Direction.NORTH && p_191132_4_ == Direction.SOUTH) {
            BlockPos blockpos4 = pPos.relative(pRotation.rotate(Direction.EAST), 7);
            blockpos4 = blockpos4.relative(pRotation.rotate(Direction.SOUTH), 14);
            pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, pFloorRooms.get1x2FrontEntrance(this.random, p_191132_7_), blockpos4, pRotation.getRotated(Rotation.CLOCKWISE_180)));
         } else if (p_191132_5_ == Direction.WEST && p_191132_4_ == Direction.EAST) {
            BlockPos blockpos3 = pPos.relative(pRotation.rotate(Direction.EAST), 15);
            pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, pFloorRooms.get1x2FrontEntrance(this.random, p_191132_7_), blockpos3, pRotation.getRotated(Rotation.CLOCKWISE_90)));
         } else if (p_191132_5_ == Direction.EAST && p_191132_4_ == Direction.WEST) {
            BlockPos blockpos2 = pPos.relative(pRotation.rotate(Direction.WEST), 7);
            blockpos2 = blockpos2.relative(pRotation.rotate(Direction.SOUTH), 6);
            pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, pFloorRooms.get1x2FrontEntrance(this.random, p_191132_7_), blockpos2, pRotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
         } else if (p_191132_5_ == Direction.UP && p_191132_4_ == Direction.EAST) {
            BlockPos blockpos1 = pPos.relative(pRotation.rotate(Direction.EAST), 15);
            pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, pFloorRooms.get1x2Secret(this.random), blockpos1, pRotation.getRotated(Rotation.CLOCKWISE_90)));
         } else if (p_191132_5_ == Direction.UP && p_191132_4_ == Direction.SOUTH) {
            BlockPos blockpos = pPos.relative(pRotation.rotate(Direction.EAST), 1);
            blockpos = blockpos.relative(pRotation.rotate(Direction.NORTH), 0);
            pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, pFloorRooms.get1x2Secret(this.random), blockpos, pRotation));
         }

      }

      private void addRoom2x2(List<WoodlandMansionPieces.MansionTemplate> pPieces, BlockPos pPos, Rotation pRotation, Direction p_191127_4_, Direction p_191127_5_, WoodlandMansionPieces.RoomCollection pFloorRooms) {
         int i = 0;
         int j = 0;
         Rotation rotation = pRotation;
         Mirror mirror = Mirror.NONE;
         if (p_191127_5_ == Direction.EAST && p_191127_4_ == Direction.SOUTH) {
            i = -7;
         } else if (p_191127_5_ == Direction.EAST && p_191127_4_ == Direction.NORTH) {
            i = -7;
            j = 6;
            mirror = Mirror.LEFT_RIGHT;
         } else if (p_191127_5_ == Direction.NORTH && p_191127_4_ == Direction.EAST) {
            i = 1;
            j = 14;
            rotation = pRotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
         } else if (p_191127_5_ == Direction.NORTH && p_191127_4_ == Direction.WEST) {
            i = 7;
            j = 14;
            rotation = pRotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
            mirror = Mirror.LEFT_RIGHT;
         } else if (p_191127_5_ == Direction.SOUTH && p_191127_4_ == Direction.WEST) {
            i = 7;
            j = -8;
            rotation = pRotation.getRotated(Rotation.CLOCKWISE_90);
         } else if (p_191127_5_ == Direction.SOUTH && p_191127_4_ == Direction.EAST) {
            i = 1;
            j = -8;
            rotation = pRotation.getRotated(Rotation.CLOCKWISE_90);
            mirror = Mirror.LEFT_RIGHT;
         } else if (p_191127_5_ == Direction.WEST && p_191127_4_ == Direction.NORTH) {
            i = 15;
            j = 6;
            rotation = pRotation.getRotated(Rotation.CLOCKWISE_180);
         } else if (p_191127_5_ == Direction.WEST && p_191127_4_ == Direction.SOUTH) {
            i = 15;
            mirror = Mirror.FRONT_BACK;
         }

         BlockPos blockpos = pPos.relative(pRotation.rotate(Direction.EAST), i);
         blockpos = blockpos.relative(pRotation.rotate(Direction.SOUTH), j);
         pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, pFloorRooms.get2x2(this.random), blockpos, rotation, mirror));
      }

      private void addRoom2x2Secret(List<WoodlandMansionPieces.MansionTemplate> pPieces, BlockPos pPos, Rotation pRotation, WoodlandMansionPieces.RoomCollection pFloorRooms) {
         BlockPos blockpos = pPos.relative(pRotation.rotate(Direction.EAST), 1);
         pPieces.add(new WoodlandMansionPieces.MansionTemplate(this.structureManager, pFloorRooms.get2x2Secret(this.random), blockpos, pRotation, Mirror.NONE));
      }
   }

   abstract static class RoomCollection {
      private RoomCollection() {
      }

      public abstract String get1x1(Random pRandom);

      public abstract String get1x1Secret(Random pRandom);

      public abstract String get1x2SideEntrance(Random pRandom, boolean pIsStairs);

      public abstract String get1x2FrontEntrance(Random pRandom, boolean pIsStairs);

      public abstract String get1x2Secret(Random pRandom);

      public abstract String get2x2(Random pRandom);

      public abstract String get2x2Secret(Random pRandom);
   }

   static class SecondFloor extends WoodlandMansionPieces.RoomCollection {
      private SecondFloor() {
      }

      public String get1x1(Random pRandom) {
         return "1x1_b" + (pRandom.nextInt(4) + 1);
      }

      public String get1x1Secret(Random pRandom) {
         return "1x1_as" + (pRandom.nextInt(4) + 1);
      }

      public String get1x2SideEntrance(Random pRandom, boolean pIsStairs) {
         return pIsStairs ? "1x2_c_stairs" : "1x2_c" + (pRandom.nextInt(4) + 1);
      }

      public String get1x2FrontEntrance(Random pRandom, boolean pIsStairs) {
         return pIsStairs ? "1x2_d_stairs" : "1x2_d" + (pRandom.nextInt(5) + 1);
      }

      public String get1x2Secret(Random pRandom) {
         return "1x2_se" + (pRandom.nextInt(1) + 1);
      }

      public String get2x2(Random pRandom) {
         return "2x2_b" + (pRandom.nextInt(5) + 1);
      }

      public String get2x2Secret(Random pRandom) {
         return "2x2_s1";
      }
   }

   static class SimpleGrid {
      private final int[][] grid;
      private final int width;
      private final int height;
      private final int valueIfOutside;

      public SimpleGrid(int pWidth, int pHeight, int pValueIfOutside) {
         this.width = pWidth;
         this.height = pHeight;
         this.valueIfOutside = pValueIfOutside;
         this.grid = new int[pWidth][pHeight];
      }

      public void set(int pX, int pY, int p_191144_3_) {
         if (pX >= 0 && pX < this.width && pY >= 0 && pY < this.height) {
            this.grid[pX][pY] = p_191144_3_;
         }

      }

      public void set(int pMinX, int pMinY, int pMaxX, int pMaxY, int p_191142_5_) {
         for(int i = pMinY; i <= pMaxY; ++i) {
            for(int j = pMinX; j <= pMaxX; ++j) {
               this.set(j, i, p_191142_5_);
            }
         }

      }

      public int get(int pX, int pY) {
         return pX >= 0 && pX < this.width && pY >= 0 && pY < this.height ? this.grid[pX][pY] : this.valueIfOutside;
      }

      public void setif(int pX, int pY, int p_197588_3_, int p_197588_4_) {
         if (this.get(pX, pY) == p_197588_3_) {
            this.set(pX, pY, p_197588_4_);
         }

      }

      public boolean edgesTo(int p_191147_1_, int p_191147_2_, int p_191147_3_) {
         return this.get(p_191147_1_ - 1, p_191147_2_) == p_191147_3_ || this.get(p_191147_1_ + 1, p_191147_2_) == p_191147_3_ || this.get(p_191147_1_, p_191147_2_ + 1) == p_191147_3_ || this.get(p_191147_1_, p_191147_2_ - 1) == p_191147_3_;
      }
   }

   static class ThirdFloor extends WoodlandMansionPieces.SecondFloor {
      private ThirdFloor() {
      }
   }
}