package net.minecraft.world.gen.feature.structure;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.template.BlockIgnoreStructureProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class IglooPieces {
   private static final ResourceLocation STRUCTURE_LOCATION_IGLOO = new ResourceLocation("igloo/top");
   private static final ResourceLocation STRUCTURE_LOCATION_LADDER = new ResourceLocation("igloo/middle");
   private static final ResourceLocation STRUCTURE_LOCATION_LABORATORY = new ResourceLocation("igloo/bottom");
   private static final Map<ResourceLocation, BlockPos> PIVOTS = ImmutableMap.of(STRUCTURE_LOCATION_IGLOO, new BlockPos(3, 5, 5), STRUCTURE_LOCATION_LADDER, new BlockPos(1, 3, 1), STRUCTURE_LOCATION_LABORATORY, new BlockPos(3, 6, 7));
   private static final Map<ResourceLocation, BlockPos> OFFSETS = ImmutableMap.of(STRUCTURE_LOCATION_IGLOO, BlockPos.ZERO, STRUCTURE_LOCATION_LADDER, new BlockPos(2, -3, 4), STRUCTURE_LOCATION_LABORATORY, new BlockPos(0, -3, -2));

   public static void addPieces(TemplateManager p_236991_0_, BlockPos p_236991_1_, Rotation p_236991_2_, List<StructurePiece> p_236991_3_, Random p_236991_4_) {
      if (p_236991_4_.nextDouble() < 0.5D) {
         int i = p_236991_4_.nextInt(8) + 4;
         p_236991_3_.add(new IglooPieces.Piece(p_236991_0_, STRUCTURE_LOCATION_LABORATORY, p_236991_1_, p_236991_2_, i * 3));

         for(int j = 0; j < i - 1; ++j) {
            p_236991_3_.add(new IglooPieces.Piece(p_236991_0_, STRUCTURE_LOCATION_LADDER, p_236991_1_, p_236991_2_, j * 3));
         }
      }

      p_236991_3_.add(new IglooPieces.Piece(p_236991_0_, STRUCTURE_LOCATION_IGLOO, p_236991_1_, p_236991_2_, 0));
   }

   public static class Piece extends TemplateStructurePiece {
      private final ResourceLocation templateLocation;
      private final Rotation rotation;

      public Piece(TemplateManager pStructureManager, ResourceLocation pLocation, BlockPos pPos, Rotation pRotation, int pDown) {
         super(IStructurePieceType.IGLOO, 0);
         this.templateLocation = pLocation;
         BlockPos blockpos = IglooPieces.OFFSETS.get(pLocation);
         this.templatePosition = pPos.offset(blockpos.getX(), blockpos.getY() - pDown, blockpos.getZ());
         this.rotation = pRotation;
         this.loadTemplate(pStructureManager);
      }

      public Piece(TemplateManager p_i50566_1_, CompoundNBT p_i50566_2_) {
         super(IStructurePieceType.IGLOO, p_i50566_2_);
         this.templateLocation = new ResourceLocation(p_i50566_2_.getString("Template"));
         this.rotation = Rotation.valueOf(p_i50566_2_.getString("Rot"));
         this.loadTemplate(p_i50566_1_);
      }

      private void loadTemplate(TemplateManager p_207614_1_) {
         Template template = p_207614_1_.getOrCreate(this.templateLocation);
         PlacementSettings placementsettings = (new PlacementSettings()).setRotation(this.rotation).setMirror(Mirror.NONE).setRotationPivot(IglooPieces.PIVOTS.get(this.templateLocation)).addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK);
         this.setup(template, this.templatePosition, placementsettings);
      }

      protected void addAdditionalSaveData(CompoundNBT p_143011_1_) {
         super.addAdditionalSaveData(p_143011_1_);
         p_143011_1_.putString("Template", this.templateLocation.toString());
         p_143011_1_.putString("Rot", this.rotation.name());
      }

      protected void handleDataMarker(String pFunction, BlockPos pPos, IServerWorld pLevel, Random pRandom, MutableBoundingBox pSbb) {
         if ("chest".equals(pFunction)) {
            pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 3);
            TileEntity tileentity = pLevel.getBlockEntity(pPos.below());
            if (tileentity instanceof ChestTileEntity) {
               ((ChestTileEntity)tileentity).setLootTable(LootTables.IGLOO_CHEST, pRandom.nextLong());
            }

         }
      }

      public boolean postProcess(ISeedReader pLevel, StructureManager pStructureManager, ChunkGenerator pChunkGenerator, Random pRandom, MutableBoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         PlacementSettings placementsettings = (new PlacementSettings()).setRotation(this.rotation).setMirror(Mirror.NONE).setRotationPivot(IglooPieces.PIVOTS.get(this.templateLocation)).addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK);
         BlockPos blockpos = IglooPieces.OFFSETS.get(this.templateLocation);
         BlockPos blockpos1 = this.templatePosition.offset(Template.calculateRelativePosition(placementsettings, new BlockPos(3 - blockpos.getX(), 0, 0 - blockpos.getZ())));
         int i = pLevel.getHeight(Heightmap.Type.WORLD_SURFACE_WG, blockpos1.getX(), blockpos1.getZ());
         BlockPos blockpos2 = this.templatePosition;
         this.templatePosition = this.templatePosition.offset(0, i - 90 - 1, 0);
         boolean flag = super.postProcess(pLevel, pStructureManager, pChunkGenerator, pRandom, pBox, pChunkPos, pPos);
         if (this.templateLocation.equals(IglooPieces.STRUCTURE_LOCATION_IGLOO)) {
            BlockPos blockpos3 = this.templatePosition.offset(Template.calculateRelativePosition(placementsettings, new BlockPos(3, 0, 5)));
            BlockState blockstate = pLevel.getBlockState(blockpos3.below());
            if (!blockstate.isAir() && !blockstate.is(Blocks.LADDER)) {
               pLevel.setBlock(blockpos3, Blocks.SNOW_BLOCK.defaultBlockState(), 3);
            }
         }

         this.templatePosition = blockpos2;
         return flag;
      }
   }
}