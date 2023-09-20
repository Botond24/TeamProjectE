package net.minecraft.world.gen.feature.structure;

import java.util.List;
import java.util.Random;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.LockableLootTileEntity;
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
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.template.BlockIgnoreStructureProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class ShipwreckPieces {
   private static final BlockPos PIVOT = new BlockPos(4, 0, 15);
   private static final ResourceLocation[] STRUCTURE_LOCATION_BEACHED = new ResourceLocation[]{new ResourceLocation("shipwreck/with_mast"), new ResourceLocation("shipwreck/sideways_full"), new ResourceLocation("shipwreck/sideways_fronthalf"), new ResourceLocation("shipwreck/sideways_backhalf"), new ResourceLocation("shipwreck/rightsideup_full"), new ResourceLocation("shipwreck/rightsideup_fronthalf"), new ResourceLocation("shipwreck/rightsideup_backhalf"), new ResourceLocation("shipwreck/with_mast_degraded"), new ResourceLocation("shipwreck/rightsideup_full_degraded"), new ResourceLocation("shipwreck/rightsideup_fronthalf_degraded"), new ResourceLocation("shipwreck/rightsideup_backhalf_degraded")};
   private static final ResourceLocation[] STRUCTURE_LOCATION_OCEAN = new ResourceLocation[]{new ResourceLocation("shipwreck/with_mast"), new ResourceLocation("shipwreck/upsidedown_full"), new ResourceLocation("shipwreck/upsidedown_fronthalf"), new ResourceLocation("shipwreck/upsidedown_backhalf"), new ResourceLocation("shipwreck/sideways_full"), new ResourceLocation("shipwreck/sideways_fronthalf"), new ResourceLocation("shipwreck/sideways_backhalf"), new ResourceLocation("shipwreck/rightsideup_full"), new ResourceLocation("shipwreck/rightsideup_fronthalf"), new ResourceLocation("shipwreck/rightsideup_backhalf"), new ResourceLocation("shipwreck/with_mast_degraded"), new ResourceLocation("shipwreck/upsidedown_full_degraded"), new ResourceLocation("shipwreck/upsidedown_fronthalf_degraded"), new ResourceLocation("shipwreck/upsidedown_backhalf_degraded"), new ResourceLocation("shipwreck/sideways_full_degraded"), new ResourceLocation("shipwreck/sideways_fronthalf_degraded"), new ResourceLocation("shipwreck/sideways_backhalf_degraded"), new ResourceLocation("shipwreck/rightsideup_full_degraded"), new ResourceLocation("shipwreck/rightsideup_fronthalf_degraded"), new ResourceLocation("shipwreck/rightsideup_backhalf_degraded")};

   public static void addPieces(TemplateManager p_204760_0_, BlockPos p_204760_1_, Rotation p_204760_2_, List<StructurePiece> p_204760_3_, Random p_204760_4_, ShipwreckConfig p_204760_5_) {
      ResourceLocation resourcelocation = Util.getRandom(p_204760_5_.isBeached ? STRUCTURE_LOCATION_BEACHED : STRUCTURE_LOCATION_OCEAN, p_204760_4_);
      p_204760_3_.add(new ShipwreckPieces.Piece(p_204760_0_, resourcelocation, p_204760_1_, p_204760_2_, p_204760_5_.isBeached));
   }

   public static class Piece extends TemplateStructurePiece {
      private final Rotation rotation;
      private final ResourceLocation templateLocation;
      private final boolean isBeached;

      public Piece(TemplateManager pStructureManager, ResourceLocation pLocation, BlockPos pPos, Rotation pRotation, boolean pIsBeached) {
         super(IStructurePieceType.SHIPWRECK_PIECE, 0);
         this.templatePosition = pPos;
         this.rotation = pRotation;
         this.templateLocation = pLocation;
         this.isBeached = pIsBeached;
         this.loadTemplate(pStructureManager);
      }

      public Piece(TemplateManager p_i50445_1_, CompoundNBT p_i50445_2_) {
         super(IStructurePieceType.SHIPWRECK_PIECE, p_i50445_2_);
         this.templateLocation = new ResourceLocation(p_i50445_2_.getString("Template"));
         this.isBeached = p_i50445_2_.getBoolean("isBeached");
         this.rotation = Rotation.valueOf(p_i50445_2_.getString("Rot"));
         this.loadTemplate(p_i50445_1_);
      }

      protected void addAdditionalSaveData(CompoundNBT p_143011_1_) {
         super.addAdditionalSaveData(p_143011_1_);
         p_143011_1_.putString("Template", this.templateLocation.toString());
         p_143011_1_.putBoolean("isBeached", this.isBeached);
         p_143011_1_.putString("Rot", this.rotation.name());
      }

      private void loadTemplate(TemplateManager p_204754_1_) {
         Template template = p_204754_1_.getOrCreate(this.templateLocation);
         PlacementSettings placementsettings = (new PlacementSettings()).setRotation(this.rotation).setMirror(Mirror.NONE).setRotationPivot(ShipwreckPieces.PIVOT).addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_AND_AIR);
         this.setup(template, this.templatePosition, placementsettings);
      }

      protected void handleDataMarker(String pFunction, BlockPos pPos, IServerWorld pLevel, Random pRandom, MutableBoundingBox pSbb) {
         if ("map_chest".equals(pFunction)) {
            LockableLootTileEntity.setLootTable(pLevel, pRandom, pPos.below(), LootTables.SHIPWRECK_MAP);
         } else if ("treasure_chest".equals(pFunction)) {
            LockableLootTileEntity.setLootTable(pLevel, pRandom, pPos.below(), LootTables.SHIPWRECK_TREASURE);
         } else if ("supply_chest".equals(pFunction)) {
            LockableLootTileEntity.setLootTable(pLevel, pRandom, pPos.below(), LootTables.SHIPWRECK_SUPPLY);
         }

      }

      public boolean postProcess(ISeedReader pLevel, StructureManager pStructureManager, ChunkGenerator pChunkGenerator, Random pRandom, MutableBoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         int i = 256;
         int j = 0;
         BlockPos blockpos = this.template.getSize();
         Heightmap.Type heightmap$type = this.isBeached ? Heightmap.Type.WORLD_SURFACE_WG : Heightmap.Type.OCEAN_FLOOR_WG;
         int k = blockpos.getX() * blockpos.getZ();
         if (k == 0) {
            j = pLevel.getHeight(heightmap$type, this.templatePosition.getX(), this.templatePosition.getZ());
         } else {
            BlockPos blockpos1 = this.templatePosition.offset(blockpos.getX() - 1, 0, blockpos.getZ() - 1);

            for(BlockPos blockpos2 : BlockPos.betweenClosed(this.templatePosition, blockpos1)) {
               int l = pLevel.getHeight(heightmap$type, blockpos2.getX(), blockpos2.getZ());
               j += l;
               i = Math.min(i, l);
            }

            j = j / k;
         }

         int i1 = this.isBeached ? i - blockpos.getY() / 2 - pRandom.nextInt(3) : j;
         this.templatePosition = new BlockPos(this.templatePosition.getX(), i1, this.templatePosition.getZ());
         return super.postProcess(pLevel, pStructureManager, pChunkGenerator, pRandom, pBox, pChunkPos, pPos);
      }
   }
}