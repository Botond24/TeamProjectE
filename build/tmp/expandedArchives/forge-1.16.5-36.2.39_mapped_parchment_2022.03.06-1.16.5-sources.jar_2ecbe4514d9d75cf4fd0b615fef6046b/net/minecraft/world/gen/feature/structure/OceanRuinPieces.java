package net.minecraft.world.gen.feature.structure;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.DrownedEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.template.BlockIgnoreStructureProcessor;
import net.minecraft.world.gen.feature.template.IntegrityProcessor;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

public class OceanRuinPieces {
   private static final ResourceLocation[] WARM_RUINS = new ResourceLocation[]{new ResourceLocation("underwater_ruin/warm_1"), new ResourceLocation("underwater_ruin/warm_2"), new ResourceLocation("underwater_ruin/warm_3"), new ResourceLocation("underwater_ruin/warm_4"), new ResourceLocation("underwater_ruin/warm_5"), new ResourceLocation("underwater_ruin/warm_6"), new ResourceLocation("underwater_ruin/warm_7"), new ResourceLocation("underwater_ruin/warm_8")};
   private static final ResourceLocation[] RUINS_BRICK = new ResourceLocation[]{new ResourceLocation("underwater_ruin/brick_1"), new ResourceLocation("underwater_ruin/brick_2"), new ResourceLocation("underwater_ruin/brick_3"), new ResourceLocation("underwater_ruin/brick_4"), new ResourceLocation("underwater_ruin/brick_5"), new ResourceLocation("underwater_ruin/brick_6"), new ResourceLocation("underwater_ruin/brick_7"), new ResourceLocation("underwater_ruin/brick_8")};
   private static final ResourceLocation[] RUINS_CRACKED = new ResourceLocation[]{new ResourceLocation("underwater_ruin/cracked_1"), new ResourceLocation("underwater_ruin/cracked_2"), new ResourceLocation("underwater_ruin/cracked_3"), new ResourceLocation("underwater_ruin/cracked_4"), new ResourceLocation("underwater_ruin/cracked_5"), new ResourceLocation("underwater_ruin/cracked_6"), new ResourceLocation("underwater_ruin/cracked_7"), new ResourceLocation("underwater_ruin/cracked_8")};
   private static final ResourceLocation[] RUINS_MOSSY = new ResourceLocation[]{new ResourceLocation("underwater_ruin/mossy_1"), new ResourceLocation("underwater_ruin/mossy_2"), new ResourceLocation("underwater_ruin/mossy_3"), new ResourceLocation("underwater_ruin/mossy_4"), new ResourceLocation("underwater_ruin/mossy_5"), new ResourceLocation("underwater_ruin/mossy_6"), new ResourceLocation("underwater_ruin/mossy_7"), new ResourceLocation("underwater_ruin/mossy_8")};
   private static final ResourceLocation[] BIG_RUINS_BRICK = new ResourceLocation[]{new ResourceLocation("underwater_ruin/big_brick_1"), new ResourceLocation("underwater_ruin/big_brick_2"), new ResourceLocation("underwater_ruin/big_brick_3"), new ResourceLocation("underwater_ruin/big_brick_8")};
   private static final ResourceLocation[] BIG_RUINS_MOSSY = new ResourceLocation[]{new ResourceLocation("underwater_ruin/big_mossy_1"), new ResourceLocation("underwater_ruin/big_mossy_2"), new ResourceLocation("underwater_ruin/big_mossy_3"), new ResourceLocation("underwater_ruin/big_mossy_8")};
   private static final ResourceLocation[] BIG_RUINS_CRACKED = new ResourceLocation[]{new ResourceLocation("underwater_ruin/big_cracked_1"), new ResourceLocation("underwater_ruin/big_cracked_2"), new ResourceLocation("underwater_ruin/big_cracked_3"), new ResourceLocation("underwater_ruin/big_cracked_8")};
   private static final ResourceLocation[] BIG_WARM_RUINS = new ResourceLocation[]{new ResourceLocation("underwater_ruin/big_warm_4"), new ResourceLocation("underwater_ruin/big_warm_5"), new ResourceLocation("underwater_ruin/big_warm_6"), new ResourceLocation("underwater_ruin/big_warm_7")};

   private static ResourceLocation getSmallWarmRuin(Random pRandom) {
      return Util.getRandom(WARM_RUINS, pRandom);
   }

   private static ResourceLocation getBigWarmRuin(Random pRandom) {
      return Util.getRandom(BIG_WARM_RUINS, pRandom);
   }

   public static void addPieces(TemplateManager p_204041_0_, BlockPos p_204041_1_, Rotation p_204041_2_, List<StructurePiece> p_204041_3_, Random p_204041_4_, OceanRuinConfig p_204041_5_) {
      boolean flag = p_204041_4_.nextFloat() <= p_204041_5_.largeProbability;
      float f = flag ? 0.9F : 0.8F;
      addPiece(p_204041_0_, p_204041_1_, p_204041_2_, p_204041_3_, p_204041_4_, p_204041_5_, flag, f);
      if (flag && p_204041_4_.nextFloat() <= p_204041_5_.clusterProbability) {
         addClusterRuins(p_204041_0_, p_204041_4_, p_204041_2_, p_204041_1_, p_204041_5_, p_204041_3_);
      }

   }

   private static void addClusterRuins(TemplateManager p_204047_0_, Random p_204047_1_, Rotation p_204047_2_, BlockPos p_204047_3_, OceanRuinConfig p_204047_4_, List<StructurePiece> p_204047_5_) {
      int i = p_204047_3_.getX();
      int j = p_204047_3_.getZ();
      BlockPos blockpos = Template.transform(new BlockPos(15, 0, 15), Mirror.NONE, p_204047_2_, BlockPos.ZERO).offset(i, 0, j);
      MutableBoundingBox mutableboundingbox = MutableBoundingBox.createProper(i, 0, j, blockpos.getX(), 0, blockpos.getZ());
      BlockPos blockpos1 = new BlockPos(Math.min(i, blockpos.getX()), 0, Math.min(j, blockpos.getZ()));
      List<BlockPos> list = allPositions(p_204047_1_, blockpos1.getX(), blockpos1.getZ());
      int k = MathHelper.nextInt(p_204047_1_, 4, 8);

      for(int l = 0; l < k; ++l) {
         if (!list.isEmpty()) {
            int i1 = p_204047_1_.nextInt(list.size());
            BlockPos blockpos2 = list.remove(i1);
            int j1 = blockpos2.getX();
            int k1 = blockpos2.getZ();
            Rotation rotation = Rotation.getRandom(p_204047_1_);
            BlockPos blockpos3 = Template.transform(new BlockPos(5, 0, 6), Mirror.NONE, rotation, BlockPos.ZERO).offset(j1, 0, k1);
            MutableBoundingBox mutableboundingbox1 = MutableBoundingBox.createProper(j1, 0, k1, blockpos3.getX(), 0, blockpos3.getZ());
            if (!mutableboundingbox1.intersects(mutableboundingbox)) {
               addPiece(p_204047_0_, blockpos2, rotation, p_204047_5_, p_204047_1_, p_204047_4_, false, 0.8F);
            }
         }
      }

   }

   private static List<BlockPos> allPositions(Random p_204044_0_, int p_204044_1_, int p_204044_2_) {
      List<BlockPos> list = Lists.newArrayList();
      list.add(new BlockPos(p_204044_1_ - 16 + MathHelper.nextInt(p_204044_0_, 1, 8), 90, p_204044_2_ + 16 + MathHelper.nextInt(p_204044_0_, 1, 7)));
      list.add(new BlockPos(p_204044_1_ - 16 + MathHelper.nextInt(p_204044_0_, 1, 8), 90, p_204044_2_ + MathHelper.nextInt(p_204044_0_, 1, 7)));
      list.add(new BlockPos(p_204044_1_ - 16 + MathHelper.nextInt(p_204044_0_, 1, 8), 90, p_204044_2_ - 16 + MathHelper.nextInt(p_204044_0_, 4, 8)));
      list.add(new BlockPos(p_204044_1_ + MathHelper.nextInt(p_204044_0_, 1, 7), 90, p_204044_2_ + 16 + MathHelper.nextInt(p_204044_0_, 1, 7)));
      list.add(new BlockPos(p_204044_1_ + MathHelper.nextInt(p_204044_0_, 1, 7), 90, p_204044_2_ - 16 + MathHelper.nextInt(p_204044_0_, 4, 6)));
      list.add(new BlockPos(p_204044_1_ + 16 + MathHelper.nextInt(p_204044_0_, 1, 7), 90, p_204044_2_ + 16 + MathHelper.nextInt(p_204044_0_, 3, 8)));
      list.add(new BlockPos(p_204044_1_ + 16 + MathHelper.nextInt(p_204044_0_, 1, 7), 90, p_204044_2_ + MathHelper.nextInt(p_204044_0_, 1, 7)));
      list.add(new BlockPos(p_204044_1_ + 16 + MathHelper.nextInt(p_204044_0_, 1, 7), 90, p_204044_2_ - 16 + MathHelper.nextInt(p_204044_0_, 4, 8)));
      return list;
   }

   private static void addPiece(TemplateManager p_204045_0_, BlockPos p_204045_1_, Rotation p_204045_2_, List<StructurePiece> p_204045_3_, Random p_204045_4_, OceanRuinConfig p_204045_5_, boolean p_204045_6_, float p_204045_7_) {
      if (p_204045_5_.biomeTemp == OceanRuinStructure.Type.WARM) {
         ResourceLocation resourcelocation = p_204045_6_ ? getBigWarmRuin(p_204045_4_) : getSmallWarmRuin(p_204045_4_);
         p_204045_3_.add(new OceanRuinPieces.Piece(p_204045_0_, resourcelocation, p_204045_1_, p_204045_2_, p_204045_7_, p_204045_5_.biomeTemp, p_204045_6_));
      } else if (p_204045_5_.biomeTemp == OceanRuinStructure.Type.COLD) {
         ResourceLocation[] aresourcelocation2 = p_204045_6_ ? BIG_RUINS_BRICK : RUINS_BRICK;
         ResourceLocation[] aresourcelocation = p_204045_6_ ? BIG_RUINS_CRACKED : RUINS_CRACKED;
         ResourceLocation[] aresourcelocation1 = p_204045_6_ ? BIG_RUINS_MOSSY : RUINS_MOSSY;
         int i = p_204045_4_.nextInt(aresourcelocation2.length);
         p_204045_3_.add(new OceanRuinPieces.Piece(p_204045_0_, aresourcelocation2[i], p_204045_1_, p_204045_2_, p_204045_7_, p_204045_5_.biomeTemp, p_204045_6_));
         p_204045_3_.add(new OceanRuinPieces.Piece(p_204045_0_, aresourcelocation[i], p_204045_1_, p_204045_2_, 0.7F, p_204045_5_.biomeTemp, p_204045_6_));
         p_204045_3_.add(new OceanRuinPieces.Piece(p_204045_0_, aresourcelocation1[i], p_204045_1_, p_204045_2_, 0.5F, p_204045_5_.biomeTemp, p_204045_6_));
      }

   }

   public static class Piece extends TemplateStructurePiece {
      private final OceanRuinStructure.Type biomeType;
      private final float integrity;
      private final ResourceLocation templateLocation;
      private final Rotation rotation;
      private final boolean isLarge;

      public Piece(TemplateManager pStructureManager, ResourceLocation pLocation, BlockPos pPos, Rotation pRotation, float pIntegrity, OceanRuinStructure.Type pBiomeType, boolean pIsLarge) {
         super(IStructurePieceType.OCEAN_RUIN, 0);
         this.templateLocation = pLocation;
         this.templatePosition = pPos;
         this.rotation = pRotation;
         this.integrity = pIntegrity;
         this.biomeType = pBiomeType;
         this.isLarge = pIsLarge;
         this.loadTemplate(pStructureManager);
      }

      public Piece(TemplateManager p_i50592_1_, CompoundNBT p_i50592_2_) {
         super(IStructurePieceType.OCEAN_RUIN, p_i50592_2_);
         this.templateLocation = new ResourceLocation(p_i50592_2_.getString("Template"));
         this.rotation = Rotation.valueOf(p_i50592_2_.getString("Rot"));
         this.integrity = p_i50592_2_.getFloat("Integrity");
         this.biomeType = OceanRuinStructure.Type.valueOf(p_i50592_2_.getString("BiomeType"));
         this.isLarge = p_i50592_2_.getBoolean("IsLarge");
         this.loadTemplate(p_i50592_1_);
      }

      private void loadTemplate(TemplateManager p_204034_1_) {
         Template template = p_204034_1_.getOrCreate(this.templateLocation);
         PlacementSettings placementsettings = (new PlacementSettings()).setRotation(this.rotation).setMirror(Mirror.NONE).addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_AND_AIR);
         this.setup(template, this.templatePosition, placementsettings);
      }

      protected void addAdditionalSaveData(CompoundNBT p_143011_1_) {
         super.addAdditionalSaveData(p_143011_1_);
         p_143011_1_.putString("Template", this.templateLocation.toString());
         p_143011_1_.putString("Rot", this.rotation.name());
         p_143011_1_.putFloat("Integrity", this.integrity);
         p_143011_1_.putString("BiomeType", this.biomeType.toString());
         p_143011_1_.putBoolean("IsLarge", this.isLarge);
      }

      protected void handleDataMarker(String pFunction, BlockPos pPos, IServerWorld pLevel, Random pRandom, MutableBoundingBox pSbb) {
         if ("chest".equals(pFunction)) {
            pLevel.setBlock(pPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.WATERLOGGED, Boolean.valueOf(pLevel.getFluidState(pPos).is(FluidTags.WATER))), 2);
            TileEntity tileentity = pLevel.getBlockEntity(pPos);
            if (tileentity instanceof ChestTileEntity) {
               ((ChestTileEntity)tileentity).setLootTable(this.isLarge ? LootTables.UNDERWATER_RUIN_BIG : LootTables.UNDERWATER_RUIN_SMALL, pRandom.nextLong());
            }
         } else if ("drowned".equals(pFunction)) {
            DrownedEntity drownedentity = EntityType.DROWNED.create(pLevel.getLevel());
            drownedentity.setPersistenceRequired();
            drownedentity.moveTo(pPos, 0.0F, 0.0F);
            drownedentity.finalizeSpawn(pLevel, pLevel.getCurrentDifficultyAt(pPos), SpawnReason.STRUCTURE, (ILivingEntityData)null, (CompoundNBT)null);
            pLevel.addFreshEntityWithPassengers(drownedentity);
            if (pPos.getY() > pLevel.getSeaLevel()) {
               pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 2);
            } else {
               pLevel.setBlock(pPos, Blocks.WATER.defaultBlockState(), 2);
            }
         }

      }

      public boolean postProcess(ISeedReader pLevel, StructureManager pStructureManager, ChunkGenerator pChunkGenerator, Random pRandom, MutableBoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos) {
         this.placeSettings.clearProcessors().addProcessor(new IntegrityProcessor(this.integrity)).addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_AND_AIR);
         int i = pLevel.getHeight(Heightmap.Type.OCEAN_FLOOR_WG, this.templatePosition.getX(), this.templatePosition.getZ());
         this.templatePosition = new BlockPos(this.templatePosition.getX(), i, this.templatePosition.getZ());
         BlockPos blockpos = Template.transform(new BlockPos(this.template.getSize().getX() - 1, 0, this.template.getSize().getZ() - 1), Mirror.NONE, this.rotation, BlockPos.ZERO).offset(this.templatePosition);
         this.templatePosition = new BlockPos(this.templatePosition.getX(), this.getHeight(this.templatePosition, pLevel, blockpos), this.templatePosition.getZ());
         return super.postProcess(pLevel, pStructureManager, pChunkGenerator, pRandom, pBox, pChunkPos, pPos);
      }

      private int getHeight(BlockPos pTemplatePos, IBlockReader pLevel, BlockPos p_204035_3_) {
         int i = pTemplatePos.getY();
         int j = 512;
         int k = i - 1;
         int l = 0;

         for(BlockPos blockpos : BlockPos.betweenClosed(pTemplatePos, p_204035_3_)) {
            int i1 = blockpos.getX();
            int j1 = blockpos.getZ();
            int k1 = pTemplatePos.getY() - 1;
            BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable(i1, k1, j1);
            BlockState blockstate = pLevel.getBlockState(blockpos$mutable);

            for(FluidState fluidstate = pLevel.getFluidState(blockpos$mutable); (blockstate.isAir() || fluidstate.is(FluidTags.WATER) || blockstate.getBlock().is(BlockTags.ICE)) && k1 > 1; fluidstate = pLevel.getFluidState(blockpos$mutable)) {
               --k1;
               blockpos$mutable.set(i1, k1, j1);
               blockstate = pLevel.getBlockState(blockpos$mutable);
            }

            j = Math.min(j, k1);
            if (k1 < k - 2) {
               ++l;
            }
         }

         int l1 = Math.abs(pTemplatePos.getX() - p_204035_3_.getX());
         if (k - j > 2 && l > l1 - 2) {
            i = j + 1;
         }

         return i;
      }
   }
}