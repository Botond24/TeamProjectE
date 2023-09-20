package net.minecraft.world.gen.feature.structure;

import java.util.List;
import java.util.Random;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.template.BlockIgnoreStructureProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class NetherFossilStructures {
   private static final ResourceLocation[] FOSSILS = new ResourceLocation[]{new ResourceLocation("nether_fossils/fossil_1"), new ResourceLocation("nether_fossils/fossil_2"), new ResourceLocation("nether_fossils/fossil_3"), new ResourceLocation("nether_fossils/fossil_4"), new ResourceLocation("nether_fossils/fossil_5"), new ResourceLocation("nether_fossils/fossil_6"), new ResourceLocation("nether_fossils/fossil_7"), new ResourceLocation("nether_fossils/fossil_8"), new ResourceLocation("nether_fossils/fossil_9"), new ResourceLocation("nether_fossils/fossil_10"), new ResourceLocation("nether_fossils/fossil_11"), new ResourceLocation("nether_fossils/fossil_12"), new ResourceLocation("nether_fossils/fossil_13"), new ResourceLocation("nether_fossils/fossil_14")};

   public static void addPieces(TemplateManager p_236994_0_, List<StructurePiece> p_236994_1_, Random p_236994_2_, BlockPos p_236994_3_) {
      Rotation rotation = Rotation.getRandom(p_236994_2_);
      p_236994_1_.add(new NetherFossilStructures.Piece(p_236994_0_, Util.getRandom(FOSSILS, p_236994_2_), p_236994_3_, rotation));
   }

   public static class Piece extends TemplateStructurePiece {
      private final ResourceLocation templateLocation;
      private final Rotation rotation;

      public Piece(TemplateManager pStructureManager, ResourceLocation pLocation, BlockPos pPos, Rotation pRotation) {
         super(IStructurePieceType.NETHER_FOSSIL, 0);
         this.templateLocation = pLocation;
         this.templatePosition = pPos;
         this.rotation = pRotation;
         this.loadTemplate(pStructureManager);
      }

      public Piece(TemplateManager p_i232107_1_, CompoundNBT p_i232107_2_) {
         super(IStructurePieceType.NETHER_FOSSIL, p_i232107_2_);
         this.templateLocation = new ResourceLocation(p_i232107_2_.getString("Template"));
         this.rotation = Rotation.valueOf(p_i232107_2_.getString("Rot"));
         this.loadTemplate(p_i232107_1_);
      }

      private void loadTemplate(TemplateManager p_236997_1_) {
         Template template = p_236997_1_.getOrCreate(this.templateLocation);
         PlacementSettings placementsettings = (new PlacementSettings()).setRotation(this.rotation).setMirror(Mirror.NONE).addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_AND_AIR);
         this.setup(template, this.templatePosition, placementsettings);
      }

      protected void addAdditionalSaveData(CompoundNBT p_143011_1_) {
         super.addAdditionalSaveData(p_143011_1_);
         p_143011_1_.putString("Template", this.templateLocation.toString());
         p_143011_1_.putString("Rot", this.rotation.name());
      }

      protected void handleDataMarker(String pFunction, BlockPos pPos, IServerWorld pLevel, Random pRandom, MutableBoundingBox pSbb) {
      }

      public boolean postProcess(ISeedReader pLevel, StructureManager pStructureManager, ChunkGenerator pChunkGenerator, Random pRandom, MutableBoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         pBox.expand(this.template.getBoundingBox(this.placeSettings, this.templatePosition));
         return super.postProcess(pLevel, pStructureManager, pChunkGenerator, pRandom, pBox, pChunkPos, pPos);
      }
   }
}