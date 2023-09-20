package net.minecraft.world.gen.feature.structure;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.util.Direction;
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

public class EndCityPieces {
   private static final PlacementSettings OVERWRITE = (new PlacementSettings()).setIgnoreEntities(true).addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK);
   private static final PlacementSettings INSERT = (new PlacementSettings()).setIgnoreEntities(true).addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_AND_AIR);
   private static final EndCityPieces.IGenerator HOUSE_TOWER_GENERATOR = new EndCityPieces.IGenerator() {
      public void init() {
      }

      public boolean generate(TemplateManager pStructureManager, int pCounter, EndCityPieces.CityTemplate pPiece, BlockPos pStartPos, List<StructurePiece> pPieces, Random pRandom) {
         if (pCounter > 8) {
            return false;
         } else {
            Rotation rotation = pPiece.placeSettings.getRotation();
            EndCityPieces.CityTemplate endcitypieces$citytemplate = EndCityPieces.addHelper(pPieces, EndCityPieces.addPiece(pStructureManager, pPiece, pStartPos, "base_floor", rotation, true));
            int i = pRandom.nextInt(3);
            if (i == 0) {
               EndCityPieces.addHelper(pPieces, EndCityPieces.addPiece(pStructureManager, endcitypieces$citytemplate, new BlockPos(-1, 4, -1), "base_roof", rotation, true));
            } else if (i == 1) {
               endcitypieces$citytemplate = EndCityPieces.addHelper(pPieces, EndCityPieces.addPiece(pStructureManager, endcitypieces$citytemplate, new BlockPos(-1, 0, -1), "second_floor_2", rotation, false));
               endcitypieces$citytemplate = EndCityPieces.addHelper(pPieces, EndCityPieces.addPiece(pStructureManager, endcitypieces$citytemplate, new BlockPos(-1, 8, -1), "second_roof", rotation, false));
               EndCityPieces.recursiveChildren(pStructureManager, EndCityPieces.TOWER_GENERATOR, pCounter + 1, endcitypieces$citytemplate, (BlockPos)null, pPieces, pRandom);
            } else if (i == 2) {
               endcitypieces$citytemplate = EndCityPieces.addHelper(pPieces, EndCityPieces.addPiece(pStructureManager, endcitypieces$citytemplate, new BlockPos(-1, 0, -1), "second_floor_2", rotation, false));
               endcitypieces$citytemplate = EndCityPieces.addHelper(pPieces, EndCityPieces.addPiece(pStructureManager, endcitypieces$citytemplate, new BlockPos(-1, 4, -1), "third_floor_2", rotation, false));
               endcitypieces$citytemplate = EndCityPieces.addHelper(pPieces, EndCityPieces.addPiece(pStructureManager, endcitypieces$citytemplate, new BlockPos(-1, 8, -1), "third_roof", rotation, true));
               EndCityPieces.recursiveChildren(pStructureManager, EndCityPieces.TOWER_GENERATOR, pCounter + 1, endcitypieces$citytemplate, (BlockPos)null, pPieces, pRandom);
            }

            return true;
         }
      }
   };
   private static final List<Tuple<Rotation, BlockPos>> TOWER_BRIDGES = Lists.newArrayList(new Tuple<>(Rotation.NONE, new BlockPos(1, -1, 0)), new Tuple<>(Rotation.CLOCKWISE_90, new BlockPos(6, -1, 1)), new Tuple<>(Rotation.COUNTERCLOCKWISE_90, new BlockPos(0, -1, 5)), new Tuple<>(Rotation.CLOCKWISE_180, new BlockPos(5, -1, 6)));
   private static final EndCityPieces.IGenerator TOWER_GENERATOR = new EndCityPieces.IGenerator() {
      public void init() {
      }

      public boolean generate(TemplateManager pStructureManager, int pCounter, EndCityPieces.CityTemplate pPiece, BlockPos pStartPos, List<StructurePiece> pPieces, Random pRandom) {
         Rotation rotation = pPiece.placeSettings.getRotation();
         EndCityPieces.CityTemplate lvt_8_1_ = EndCityPieces.addHelper(pPieces, EndCityPieces.addPiece(pStructureManager, pPiece, new BlockPos(3 + pRandom.nextInt(2), -3, 3 + pRandom.nextInt(2)), "tower_base", rotation, true));
         lvt_8_1_ = EndCityPieces.addHelper(pPieces, EndCityPieces.addPiece(pStructureManager, lvt_8_1_, new BlockPos(0, 7, 0), "tower_piece", rotation, true));
         EndCityPieces.CityTemplate endcitypieces$citytemplate1 = pRandom.nextInt(3) == 0 ? lvt_8_1_ : null;
         int i = 1 + pRandom.nextInt(3);

         for(int j = 0; j < i; ++j) {
            lvt_8_1_ = EndCityPieces.addHelper(pPieces, EndCityPieces.addPiece(pStructureManager, lvt_8_1_, new BlockPos(0, 4, 0), "tower_piece", rotation, true));
            if (j < i - 1 && pRandom.nextBoolean()) {
               endcitypieces$citytemplate1 = lvt_8_1_;
            }
         }

         if (endcitypieces$citytemplate1 != null) {
            for(Tuple<Rotation, BlockPos> tuple : EndCityPieces.TOWER_BRIDGES) {
               if (pRandom.nextBoolean()) {
                  EndCityPieces.CityTemplate endcitypieces$citytemplate2 = EndCityPieces.addHelper(pPieces, EndCityPieces.addPiece(pStructureManager, endcitypieces$citytemplate1, tuple.getB(), "bridge_end", rotation.getRotated(tuple.getA()), true));
                  EndCityPieces.recursiveChildren(pStructureManager, EndCityPieces.TOWER_BRIDGE_GENERATOR, pCounter + 1, endcitypieces$citytemplate2, (BlockPos)null, pPieces, pRandom);
               }
            }

            EndCityPieces.addHelper(pPieces, EndCityPieces.addPiece(pStructureManager, lvt_8_1_, new BlockPos(-1, 4, -1), "tower_top", rotation, true));
         } else {
            if (pCounter != 7) {
               return EndCityPieces.recursiveChildren(pStructureManager, EndCityPieces.FAT_TOWER_GENERATOR, pCounter + 1, lvt_8_1_, (BlockPos)null, pPieces, pRandom);
            }

            EndCityPieces.addHelper(pPieces, EndCityPieces.addPiece(pStructureManager, lvt_8_1_, new BlockPos(-1, 4, -1), "tower_top", rotation, true));
         }

         return true;
      }
   };
   private static final EndCityPieces.IGenerator TOWER_BRIDGE_GENERATOR = new EndCityPieces.IGenerator() {
      public boolean shipCreated;

      public void init() {
         this.shipCreated = false;
      }

      public boolean generate(TemplateManager pStructureManager, int pCounter, EndCityPieces.CityTemplate pPiece, BlockPos pStartPos, List<StructurePiece> pPieces, Random pRandom) {
         Rotation rotation = pPiece.placeSettings.getRotation();
         int i = pRandom.nextInt(4) + 1;
         EndCityPieces.CityTemplate endcitypieces$citytemplate = EndCityPieces.addHelper(pPieces, EndCityPieces.addPiece(pStructureManager, pPiece, new BlockPos(0, 0, -4), "bridge_piece", rotation, true));
         endcitypieces$citytemplate.genDepth = -1;
         int j = 0;

         for(int k = 0; k < i; ++k) {
            if (pRandom.nextBoolean()) {
               endcitypieces$citytemplate = EndCityPieces.addHelper(pPieces, EndCityPieces.addPiece(pStructureManager, endcitypieces$citytemplate, new BlockPos(0, j, -4), "bridge_piece", rotation, true));
               j = 0;
            } else {
               if (pRandom.nextBoolean()) {
                  endcitypieces$citytemplate = EndCityPieces.addHelper(pPieces, EndCityPieces.addPiece(pStructureManager, endcitypieces$citytemplate, new BlockPos(0, j, -4), "bridge_steep_stairs", rotation, true));
               } else {
                  endcitypieces$citytemplate = EndCityPieces.addHelper(pPieces, EndCityPieces.addPiece(pStructureManager, endcitypieces$citytemplate, new BlockPos(0, j, -8), "bridge_gentle_stairs", rotation, true));
               }

               j = 4;
            }
         }

         if (!this.shipCreated && pRandom.nextInt(10 - pCounter) == 0) {
            EndCityPieces.addHelper(pPieces, EndCityPieces.addPiece(pStructureManager, endcitypieces$citytemplate, new BlockPos(-8 + pRandom.nextInt(8), j, -70 + pRandom.nextInt(10)), "ship", rotation, true));
            this.shipCreated = true;
         } else if (!EndCityPieces.recursiveChildren(pStructureManager, EndCityPieces.HOUSE_TOWER_GENERATOR, pCounter + 1, endcitypieces$citytemplate, new BlockPos(-3, j + 1, -11), pPieces, pRandom)) {
            return false;
         }

         endcitypieces$citytemplate = EndCityPieces.addHelper(pPieces, EndCityPieces.addPiece(pStructureManager, endcitypieces$citytemplate, new BlockPos(4, j, 0), "bridge_end", rotation.getRotated(Rotation.CLOCKWISE_180), true));
         endcitypieces$citytemplate.genDepth = -1;
         return true;
      }
   };
   private static final List<Tuple<Rotation, BlockPos>> FAT_TOWER_BRIDGES = Lists.newArrayList(new Tuple<>(Rotation.NONE, new BlockPos(4, -1, 0)), new Tuple<>(Rotation.CLOCKWISE_90, new BlockPos(12, -1, 4)), new Tuple<>(Rotation.COUNTERCLOCKWISE_90, new BlockPos(0, -1, 8)), new Tuple<>(Rotation.CLOCKWISE_180, new BlockPos(8, -1, 12)));
   private static final EndCityPieces.IGenerator FAT_TOWER_GENERATOR = new EndCityPieces.IGenerator() {
      public void init() {
      }

      public boolean generate(TemplateManager pStructureManager, int pCounter, EndCityPieces.CityTemplate pPiece, BlockPos pStartPos, List<StructurePiece> pPieces, Random pRandom) {
         Rotation rotation = pPiece.placeSettings.getRotation();
         EndCityPieces.CityTemplate endcitypieces$citytemplate = EndCityPieces.addHelper(pPieces, EndCityPieces.addPiece(pStructureManager, pPiece, new BlockPos(-3, 4, -3), "fat_tower_base", rotation, true));
         endcitypieces$citytemplate = EndCityPieces.addHelper(pPieces, EndCityPieces.addPiece(pStructureManager, endcitypieces$citytemplate, new BlockPos(0, 4, 0), "fat_tower_middle", rotation, true));

         for(int i = 0; i < 2 && pRandom.nextInt(3) != 0; ++i) {
            endcitypieces$citytemplate = EndCityPieces.addHelper(pPieces, EndCityPieces.addPiece(pStructureManager, endcitypieces$citytemplate, new BlockPos(0, 8, 0), "fat_tower_middle", rotation, true));

            for(Tuple<Rotation, BlockPos> tuple : EndCityPieces.FAT_TOWER_BRIDGES) {
               if (pRandom.nextBoolean()) {
                  EndCityPieces.CityTemplate endcitypieces$citytemplate1 = EndCityPieces.addHelper(pPieces, EndCityPieces.addPiece(pStructureManager, endcitypieces$citytemplate, tuple.getB(), "bridge_end", rotation.getRotated(tuple.getA()), true));
                  EndCityPieces.recursiveChildren(pStructureManager, EndCityPieces.TOWER_BRIDGE_GENERATOR, pCounter + 1, endcitypieces$citytemplate1, (BlockPos)null, pPieces, pRandom);
               }
            }
         }

         EndCityPieces.addHelper(pPieces, EndCityPieces.addPiece(pStructureManager, endcitypieces$citytemplate, new BlockPos(-2, 8, -2), "fat_tower_top", rotation, true));
         return true;
      }
   };

   private static EndCityPieces.CityTemplate addPiece(TemplateManager pStructureManager, EndCityPieces.CityTemplate pPiece, BlockPos pPos, String pName, Rotation pRotation, boolean pOverwrite) {
      EndCityPieces.CityTemplate endcitypieces$citytemplate = new EndCityPieces.CityTemplate(pStructureManager, pName, pPiece.templatePosition, pRotation, pOverwrite);
      BlockPos blockpos = pPiece.template.calculateConnectedPosition(pPiece.placeSettings, pPos, endcitypieces$citytemplate.placeSettings, BlockPos.ZERO);
      endcitypieces$citytemplate.move(blockpos.getX(), blockpos.getY(), blockpos.getZ());
      return endcitypieces$citytemplate;
   }

   public static void startHouseTower(TemplateManager pStructureManager, BlockPos pPos, Rotation pRotation, List<StructurePiece> pPieces, Random pRandom) {
      FAT_TOWER_GENERATOR.init();
      HOUSE_TOWER_GENERATOR.init();
      TOWER_BRIDGE_GENERATOR.init();
      TOWER_GENERATOR.init();
      EndCityPieces.CityTemplate endcitypieces$citytemplate = addHelper(pPieces, new EndCityPieces.CityTemplate(pStructureManager, "base_floor", pPos, pRotation, true));
      endcitypieces$citytemplate = addHelper(pPieces, addPiece(pStructureManager, endcitypieces$citytemplate, new BlockPos(-1, 0, -1), "second_floor_1", pRotation, false));
      endcitypieces$citytemplate = addHelper(pPieces, addPiece(pStructureManager, endcitypieces$citytemplate, new BlockPos(-1, 4, -1), "third_floor_1", pRotation, false));
      endcitypieces$citytemplate = addHelper(pPieces, addPiece(pStructureManager, endcitypieces$citytemplate, new BlockPos(-1, 8, -1), "third_roof", pRotation, true));
      recursiveChildren(pStructureManager, TOWER_GENERATOR, 1, endcitypieces$citytemplate, (BlockPos)null, pPieces, pRandom);
   }

   private static EndCityPieces.CityTemplate addHelper(List<StructurePiece> pPieces, EndCityPieces.CityTemplate pPiece) {
      pPieces.add(pPiece);
      return pPiece;
   }

   private static boolean recursiveChildren(TemplateManager pStructureManager, EndCityPieces.IGenerator pSectionGenerator, int pCounter, EndCityPieces.CityTemplate pPiece, BlockPos pStartPos, List<StructurePiece> pPieces, Random pRandom) {
      if (pCounter > 8) {
         return false;
      } else {
         List<StructurePiece> list = Lists.newArrayList();
         if (pSectionGenerator.generate(pStructureManager, pCounter, pPiece, pStartPos, list, pRandom)) {
            boolean flag = false;
            int i = pRandom.nextInt();

            for(StructurePiece structurepiece : list) {
               structurepiece.genDepth = i;
               StructurePiece structurepiece1 = StructurePiece.findCollisionPiece(pPieces, structurepiece.getBoundingBox());
               if (structurepiece1 != null && structurepiece1.genDepth != pPiece.genDepth) {
                  flag = true;
                  break;
               }
            }

            if (!flag) {
               pPieces.addAll(list);
               return true;
            }
         }

         return false;
      }
   }

   public static class CityTemplate extends TemplateStructurePiece {
      private final String templateName;
      private final Rotation rotation;
      private final boolean overwrite;

      public CityTemplate(TemplateManager pStructureManager, String pName, BlockPos pPos, Rotation pRotation, boolean p_i47214_5_) {
         super(IStructurePieceType.END_CITY_PIECE, 0);
         this.templateName = pName;
         this.templatePosition = pPos;
         this.rotation = pRotation;
         this.overwrite = p_i47214_5_;
         this.loadTemplate(pStructureManager);
      }

      public CityTemplate(TemplateManager p_i50598_1_, CompoundNBT p_i50598_2_) {
         super(IStructurePieceType.END_CITY_PIECE, p_i50598_2_);
         this.templateName = p_i50598_2_.getString("Template");
         this.rotation = Rotation.valueOf(p_i50598_2_.getString("Rot"));
         this.overwrite = p_i50598_2_.getBoolean("OW");
         this.loadTemplate(p_i50598_1_);
      }

      private void loadTemplate(TemplateManager p_191085_1_) {
         Template template = p_191085_1_.getOrCreate(new ResourceLocation("end_city/" + this.templateName));
         PlacementSettings placementsettings = (this.overwrite ? EndCityPieces.OVERWRITE : EndCityPieces.INSERT).copy().setRotation(this.rotation);
         this.setup(template, this.templatePosition, placementsettings);
      }

      protected void addAdditionalSaveData(CompoundNBT p_143011_1_) {
         super.addAdditionalSaveData(p_143011_1_);
         p_143011_1_.putString("Template", this.templateName);
         p_143011_1_.putString("Rot", this.rotation.name());
         p_143011_1_.putBoolean("OW", this.overwrite);
      }

      protected void handleDataMarker(String pFunction, BlockPos pPos, IServerWorld pLevel, Random pRandom, MutableBoundingBox pSbb) {
         if (pFunction.startsWith("Chest")) {
            BlockPos blockpos = pPos.below();
            if (pSbb.isInside(blockpos)) {
               LockableLootTileEntity.setLootTable(pLevel, pRandom, blockpos, LootTables.END_CITY_TREASURE);
            }
         } else if (pFunction.startsWith("Sentry")) {
            ShulkerEntity shulkerentity = EntityType.SHULKER.create(pLevel.getLevel());
            shulkerentity.setPos((double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D);
            shulkerentity.setAttachPosition(pPos);
            pLevel.addFreshEntity(shulkerentity);
         } else if (pFunction.startsWith("Elytra")) {
            ItemFrameEntity itemframeentity = new ItemFrameEntity(pLevel.getLevel(), pPos, this.rotation.rotate(Direction.SOUTH));
            itemframeentity.setItem(new ItemStack(Items.ELYTRA), false);
            pLevel.addFreshEntity(itemframeentity);
         }

      }
   }

   interface IGenerator {
      void init();

      boolean generate(TemplateManager pStructureManager, int pCounter, EndCityPieces.CityTemplate pPiece, BlockPos pStartPos, List<StructurePiece> pPieces, Random pRandom);
   }
}