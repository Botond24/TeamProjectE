package net.minecraft.test;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.state.properties.StructureMode;
import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ServerWorld;
import org.apache.commons.io.IOUtils;

public class StructureHelper {
   public static String testStructuresDir = "gameteststructures";

   public static Rotation getRotationForRotationSteps(int pRotationSteps) {
      switch(pRotationSteps) {
      case 0:
         return Rotation.NONE;
      case 1:
         return Rotation.CLOCKWISE_90;
      case 2:
         return Rotation.CLOCKWISE_180;
      case 3:
         return Rotation.COUNTERCLOCKWISE_90;
      default:
         throw new IllegalArgumentException("rotationSteps must be a value from 0-3. Got value " + pRotationSteps);
      }
   }

   public static AxisAlignedBB getStructureBounds(StructureBlockTileEntity pStructureBlockEntity) {
      BlockPos blockpos = pStructureBlockEntity.getBlockPos();
      BlockPos blockpos1 = blockpos.offset(pStructureBlockEntity.getStructureSize().offset(-1, -1, -1));
      BlockPos blockpos2 = Template.transform(blockpos1, Mirror.NONE, pStructureBlockEntity.getRotation(), blockpos);
      return new AxisAlignedBB(blockpos, blockpos2);
   }

   public static MutableBoundingBox getStructureBoundingBox(StructureBlockTileEntity pStructureBlockEntity) {
      BlockPos blockpos = pStructureBlockEntity.getBlockPos();
      BlockPos blockpos1 = blockpos.offset(pStructureBlockEntity.getStructureSize().offset(-1, -1, -1));
      BlockPos blockpos2 = Template.transform(blockpos1, Mirror.NONE, pStructureBlockEntity.getRotation(), blockpos);
      return new MutableBoundingBox(blockpos, blockpos2);
   }

   public static void addCommandBlockAndButtonToStartTest(BlockPos p_240564_0_, BlockPos p_240564_1_, Rotation pRotation, ServerWorld pServerLevel) {
      BlockPos blockpos = Template.transform(p_240564_0_.offset(p_240564_1_), Mirror.NONE, pRotation, p_240564_0_);
      pServerLevel.setBlockAndUpdate(blockpos, Blocks.COMMAND_BLOCK.defaultBlockState());
      CommandBlockTileEntity commandblocktileentity = (CommandBlockTileEntity)pServerLevel.getBlockEntity(blockpos);
      commandblocktileentity.getCommandBlock().setCommand("test runthis");
      BlockPos blockpos1 = Template.transform(blockpos.offset(0, 0, -1), Mirror.NONE, pRotation, blockpos);
      pServerLevel.setBlockAndUpdate(blockpos1, Blocks.STONE_BUTTON.defaultBlockState().rotate(pRotation));
   }

   public static void createNewEmptyStructureBlock(String p_229603_0_, BlockPos p_229603_1_, BlockPos p_229603_2_, Rotation p_229603_3_, ServerWorld p_229603_4_) {
      MutableBoundingBox mutableboundingbox = getStructureBoundingBox(p_229603_1_, p_229603_2_, p_229603_3_);
      clearSpaceForStructure(mutableboundingbox, p_229603_1_.getY(), p_229603_4_);
      p_229603_4_.setBlockAndUpdate(p_229603_1_, Blocks.STRUCTURE_BLOCK.defaultBlockState());
      StructureBlockTileEntity structureblocktileentity = (StructureBlockTileEntity)p_229603_4_.getBlockEntity(p_229603_1_);
      structureblocktileentity.setIgnoreEntities(false);
      structureblocktileentity.setStructureName(new ResourceLocation(p_229603_0_));
      structureblocktileentity.setStructureSize(p_229603_2_);
      structureblocktileentity.setMode(StructureMode.SAVE);
      structureblocktileentity.setShowBoundingBox(true);
   }

   public static StructureBlockTileEntity spawnStructure(String pStructureName, BlockPos pPos, Rotation pRotation, int p_240565_3_, ServerWorld pServerLevel, boolean p_240565_5_) {
      BlockPos blockpos = getStructureTemplate(pStructureName, pServerLevel).getSize();
      MutableBoundingBox mutableboundingbox = getStructureBoundingBox(pPos, blockpos, pRotation);
      BlockPos blockpos1;
      if (pRotation == Rotation.NONE) {
         blockpos1 = pPos;
      } else if (pRotation == Rotation.CLOCKWISE_90) {
         blockpos1 = pPos.offset(blockpos.getZ() - 1, 0, 0);
      } else if (pRotation == Rotation.CLOCKWISE_180) {
         blockpos1 = pPos.offset(blockpos.getX() - 1, 0, blockpos.getZ() - 1);
      } else {
         if (pRotation != Rotation.COUNTERCLOCKWISE_90) {
            throw new IllegalArgumentException("Invalid rotation: " + pRotation);
         }

         blockpos1 = pPos.offset(0, 0, blockpos.getX() - 1);
      }

      forceLoadChunks(pPos, pServerLevel);
      clearSpaceForStructure(mutableboundingbox, pPos.getY(), pServerLevel);
      StructureBlockTileEntity structureblocktileentity = createStructureBlock(pStructureName, blockpos1, pRotation, pServerLevel, p_240565_5_);
      pServerLevel.getBlockTicks().fetchTicksInArea(mutableboundingbox, true, false);
      pServerLevel.clearBlockEvents(mutableboundingbox);
      return structureblocktileentity;
   }

   private static void forceLoadChunks(BlockPos pPos, ServerWorld pServerLevel) {
      ChunkPos chunkpos = new ChunkPos(pPos);

      for(int i = -1; i < 4; ++i) {
         for(int j = -1; j < 4; ++j) {
            int k = chunkpos.x + i;
            int l = chunkpos.z + j;
            pServerLevel.setChunkForced(k, l, true);
         }
      }

   }

   public static void clearSpaceForStructure(MutableBoundingBox pBoundingBox, int p_229595_1_, ServerWorld pServerLevel) {
      MutableBoundingBox mutableboundingbox = new MutableBoundingBox(pBoundingBox.x0 - 2, pBoundingBox.y0 - 3, pBoundingBox.z0 - 3, pBoundingBox.x1 + 3, pBoundingBox.y1 + 20, pBoundingBox.z1 + 3);
      BlockPos.betweenClosedStream(mutableboundingbox).forEach((p_229592_2_) -> {
         clearBlock(p_229595_1_, p_229592_2_, pServerLevel);
      });
      pServerLevel.getBlockTicks().fetchTicksInArea(mutableboundingbox, true, false);
      pServerLevel.clearBlockEvents(mutableboundingbox);
      AxisAlignedBB axisalignedbb = new AxisAlignedBB((double)mutableboundingbox.x0, (double)mutableboundingbox.y0, (double)mutableboundingbox.z0, (double)mutableboundingbox.x1, (double)mutableboundingbox.y1, (double)mutableboundingbox.z1);
      List<Entity> list = pServerLevel.getEntitiesOfClass(Entity.class, axisalignedbb, (p_229593_0_) -> {
         return !(p_229593_0_ instanceof PlayerEntity);
      });
      list.forEach(Entity::remove);
   }

   public static MutableBoundingBox getStructureBoundingBox(BlockPos p_229598_0_, BlockPos p_229598_1_, Rotation p_229598_2_) {
      BlockPos blockpos = p_229598_0_.offset(p_229598_1_).offset(-1, -1, -1);
      BlockPos blockpos1 = Template.transform(blockpos, Mirror.NONE, p_229598_2_, p_229598_0_);
      MutableBoundingBox mutableboundingbox = MutableBoundingBox.createProper(p_229598_0_.getX(), p_229598_0_.getY(), p_229598_0_.getZ(), blockpos1.getX(), blockpos1.getY(), blockpos1.getZ());
      int i = Math.min(mutableboundingbox.x0, mutableboundingbox.x1);
      int j = Math.min(mutableboundingbox.z0, mutableboundingbox.z1);
      BlockPos blockpos2 = new BlockPos(p_229598_0_.getX() - i, 0, p_229598_0_.getZ() - j);
      mutableboundingbox.move(blockpos2);
      return mutableboundingbox;
   }

   public static Optional<BlockPos> findStructureBlockContainingPos(BlockPos pPos, int p_229596_1_, ServerWorld pServerLevel) {
      return findStructureBlocks(pPos, p_229596_1_, pServerLevel).stream().filter((p_229601_2_) -> {
         return doesStructureContain(p_229601_2_, pPos, pServerLevel);
      }).findFirst();
   }

   @Nullable
   public static BlockPos findNearestStructureBlock(BlockPos pPos, int p_229607_1_, ServerWorld pServerLevel) {
      Comparator<BlockPos> comparator = Comparator.comparingInt((p_229597_1_) -> {
         return p_229597_1_.distManhattan(pPos);
      });
      Collection<BlockPos> collection = findStructureBlocks(pPos, p_229607_1_, pServerLevel);
      Optional<BlockPos> optional = collection.stream().min(comparator);
      return optional.orElse((BlockPos)null);
   }

   public static Collection<BlockPos> findStructureBlocks(BlockPos pPos, int p_229609_1_, ServerWorld pServerLevel) {
      Collection<BlockPos> collection = Lists.newArrayList();
      AxisAlignedBB axisalignedbb = new AxisAlignedBB(pPos);
      axisalignedbb = axisalignedbb.inflate((double)p_229609_1_);

      for(int i = (int)axisalignedbb.minX; i <= (int)axisalignedbb.maxX; ++i) {
         for(int j = (int)axisalignedbb.minY; j <= (int)axisalignedbb.maxY; ++j) {
            for(int k = (int)axisalignedbb.minZ; k <= (int)axisalignedbb.maxZ; ++k) {
               BlockPos blockpos = new BlockPos(i, j, k);
               BlockState blockstate = pServerLevel.getBlockState(blockpos);
               if (blockstate.is(Blocks.STRUCTURE_BLOCK)) {
                  collection.add(blockpos);
               }
            }
         }
      }

      return collection;
   }

   private static Template getStructureTemplate(String pStructureName, ServerWorld pServerLevel) {
      TemplateManager templatemanager = pServerLevel.getStructureManager();
      Template template = templatemanager.get(new ResourceLocation(pStructureName));
      if (template != null) {
         return template;
      } else {
         String s = pStructureName + ".snbt";
         Path path = Paths.get(testStructuresDir, s);
         CompoundNBT compoundnbt = tryLoadStructure(path);
         if (compoundnbt == null) {
            throw new RuntimeException("Could not find structure file " + path + ", and the structure is not available in the world structures either.");
         } else {
            return templatemanager.readStructure(compoundnbt);
         }
      }
   }

   private static StructureBlockTileEntity createStructureBlock(String pStructureName, BlockPos pPos, Rotation pRotation, ServerWorld pServerLevel, boolean p_240566_4_) {
      pServerLevel.setBlockAndUpdate(pPos, Blocks.STRUCTURE_BLOCK.defaultBlockState());
      StructureBlockTileEntity structureblocktileentity = (StructureBlockTileEntity)pServerLevel.getBlockEntity(pPos);
      structureblocktileentity.setMode(StructureMode.LOAD);
      structureblocktileentity.setRotation(pRotation);
      structureblocktileentity.setIgnoreEntities(false);
      structureblocktileentity.setStructureName(new ResourceLocation(pStructureName));
      structureblocktileentity.loadStructure(pServerLevel, p_240566_4_);
      if (structureblocktileentity.getStructureSize() != BlockPos.ZERO) {
         return structureblocktileentity;
      } else {
         Template template = getStructureTemplate(pStructureName, pServerLevel);
         structureblocktileentity.loadStructure(pServerLevel, p_240566_4_, template);
         if (structureblocktileentity.getStructureSize() == BlockPos.ZERO) {
            throw new RuntimeException("Failed to load structure " + pStructureName);
         } else {
            return structureblocktileentity;
         }
      }
   }

   @Nullable
   private static CompoundNBT tryLoadStructure(Path pPathToStructure) {
      try {
         BufferedReader bufferedreader = Files.newBufferedReader(pPathToStructure);
         String s = IOUtils.toString((Reader)bufferedreader);
         return JsonToNBT.parseTag(s);
      } catch (IOException ioexception) {
         return null;
      } catch (CommandSyntaxException commandsyntaxexception) {
         throw new RuntimeException("Error while trying to load structure " + pPathToStructure, commandsyntaxexception);
      }
   }

   private static void clearBlock(int p_229591_0_, BlockPos pPos, ServerWorld pServerLevel) {
      BlockState blockstate = null;
      FlatGenerationSettings flatgenerationsettings = FlatGenerationSettings.getDefault(pServerLevel.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY));
      if (flatgenerationsettings instanceof FlatGenerationSettings) {
         BlockState[] ablockstate = flatgenerationsettings.getLayers();
         if (pPos.getY() < p_229591_0_ && pPos.getY() <= ablockstate.length) {
            blockstate = ablockstate[pPos.getY() - 1];
         }
      } else if (pPos.getY() == p_229591_0_ - 1) {
         blockstate = pServerLevel.getBiome(pPos).getGenerationSettings().getSurfaceBuilderConfig().getTopMaterial();
      } else if (pPos.getY() < p_229591_0_ - 1) {
         blockstate = pServerLevel.getBiome(pPos).getGenerationSettings().getSurfaceBuilderConfig().getUnderMaterial();
      }

      if (blockstate == null) {
         blockstate = Blocks.AIR.defaultBlockState();
      }

      BlockStateInput blockstateinput = new BlockStateInput(blockstate, Collections.emptySet(), (CompoundNBT)null);
      blockstateinput.place(pServerLevel, pPos, 2);
      pServerLevel.blockUpdated(pPos, blockstate.getBlock());
   }

   private static boolean doesStructureContain(BlockPos pStructureBlockPos, BlockPos pPosToTest, ServerWorld pServerLevel) {
      StructureBlockTileEntity structureblocktileentity = (StructureBlockTileEntity)pServerLevel.getBlockEntity(pStructureBlockPos);
      AxisAlignedBB axisalignedbb = getStructureBounds(structureblocktileentity).inflate(1.0D);
      return axisalignedbb.contains(Vector3d.atCenterOf(pPosToTest));
   }
}