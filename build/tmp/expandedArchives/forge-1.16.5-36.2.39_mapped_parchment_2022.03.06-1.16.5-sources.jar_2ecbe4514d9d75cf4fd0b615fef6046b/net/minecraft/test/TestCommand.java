package net.minecraft.test;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.data.NBTToSNBTConverter;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import org.apache.commons.io.IOUtils;

public class TestCommand {
   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      pDispatcher.register(Commands.literal("test").then(Commands.literal("runthis").executes((p_229647_0_) -> {
         return runNearbyTest(p_229647_0_.getSource());
      })).then(Commands.literal("runthese").executes((p_229646_0_) -> {
         return runAllNearbyTests(p_229646_0_.getSource());
      })).then(Commands.literal("runfailed").executes((p_240582_0_) -> {
         return runLastFailedTests(p_240582_0_.getSource(), false, 0, 8);
      }).then(Commands.argument("onlyRequiredTests", BoolArgumentType.bool()).executes((p_240585_0_) -> {
         return runLastFailedTests(p_240585_0_.getSource(), BoolArgumentType.getBool(p_240585_0_, "onlyRequiredTests"), 0, 8);
      }).then(Commands.argument("rotationSteps", IntegerArgumentType.integer()).executes((p_240588_0_) -> {
         return runLastFailedTests(p_240588_0_.getSource(), BoolArgumentType.getBool(p_240588_0_, "onlyRequiredTests"), IntegerArgumentType.getInteger(p_240588_0_, "rotationSteps"), 8);
      }).then(Commands.argument("testsPerRow", IntegerArgumentType.integer()).executes((p_240586_0_) -> {
         return runLastFailedTests(p_240586_0_.getSource(), BoolArgumentType.getBool(p_240586_0_, "onlyRequiredTests"), IntegerArgumentType.getInteger(p_240586_0_, "rotationSteps"), IntegerArgumentType.getInteger(p_240586_0_, "testsPerRow"));
      }))))).then(Commands.literal("run").then(Commands.argument("testName", TestArgArgument.testFunctionArgument()).executes((p_229645_0_) -> {
         return runTest(p_229645_0_.getSource(), TestArgArgument.getTestFunction(p_229645_0_, "testName"), 0);
      }).then(Commands.argument("rotationSteps", IntegerArgumentType.integer()).executes((p_240584_0_) -> {
         return runTest(p_240584_0_.getSource(), TestArgArgument.getTestFunction(p_240584_0_, "testName"), IntegerArgumentType.getInteger(p_240584_0_, "rotationSteps"));
      })))).then(Commands.literal("runall").executes((p_229644_0_) -> {
         return runAllTests(p_229644_0_.getSource(), 0, 8);
      }).then(Commands.argument("testClassName", TestTypeArgument.testClassName()).executes((p_229643_0_) -> {
         return runAllTestsInClass(p_229643_0_.getSource(), TestTypeArgument.getTestClassName(p_229643_0_, "testClassName"), 0, 8);
      }).then(Commands.argument("rotationSteps", IntegerArgumentType.integer()).executes((p_240580_0_) -> {
         return runAllTestsInClass(p_240580_0_.getSource(), TestTypeArgument.getTestClassName(p_240580_0_, "testClassName"), IntegerArgumentType.getInteger(p_240580_0_, "rotationSteps"), 8);
      }).then(Commands.argument("testsPerRow", IntegerArgumentType.integer()).executes((p_240579_0_) -> {
         return runAllTestsInClass(p_240579_0_.getSource(), TestTypeArgument.getTestClassName(p_240579_0_, "testClassName"), IntegerArgumentType.getInteger(p_240579_0_, "rotationSteps"), IntegerArgumentType.getInteger(p_240579_0_, "testsPerRow"));
      })))).then(Commands.argument("rotationSteps", IntegerArgumentType.integer()).executes((p_240569_0_) -> {
         return runAllTests(p_240569_0_.getSource(), IntegerArgumentType.getInteger(p_240569_0_, "rotationSteps"), 8);
      }).then(Commands.argument("testsPerRow", IntegerArgumentType.integer()).executes((p_218527_0_) -> {
         return runAllTests(p_218527_0_.getSource(), IntegerArgumentType.getInteger(p_218527_0_, "rotationSteps"), IntegerArgumentType.getInteger(p_218527_0_, "testsPerRow"));
      })))).then(Commands.literal("export").then(Commands.argument("testName", StringArgumentType.word()).executes((p_229642_0_) -> {
         return exportTestStructure(p_229642_0_.getSource(), StringArgumentType.getString(p_229642_0_, "testName"));
      }))).then(Commands.literal("exportthis").executes((p_240587_0_) -> {
         return exportNearestTestStructure(p_240587_0_.getSource());
      })).then(Commands.literal("import").then(Commands.argument("testName", StringArgumentType.word()).executes((p_229641_0_) -> {
         return importTestStructure(p_229641_0_.getSource(), StringArgumentType.getString(p_229641_0_, "testName"));
      }))).then(Commands.literal("pos").executes((p_229640_0_) -> {
         return showPos(p_229640_0_.getSource(), "pos");
      }).then(Commands.argument("var", StringArgumentType.word()).executes((p_229639_0_) -> {
         return showPos(p_229639_0_.getSource(), StringArgumentType.getString(p_229639_0_, "var"));
      }))).then(Commands.literal("create").then(Commands.argument("testName", StringArgumentType.word()).executes((p_229637_0_) -> {
         return createNewStructure(p_229637_0_.getSource(), StringArgumentType.getString(p_229637_0_, "testName"), 5, 5, 5);
      }).then(Commands.argument("width", IntegerArgumentType.integer()).executes((p_229635_0_) -> {
         return createNewStructure(p_229635_0_.getSource(), StringArgumentType.getString(p_229635_0_, "testName"), IntegerArgumentType.getInteger(p_229635_0_, "width"), IntegerArgumentType.getInteger(p_229635_0_, "width"), IntegerArgumentType.getInteger(p_229635_0_, "width"));
      }).then(Commands.argument("height", IntegerArgumentType.integer()).then(Commands.argument("depth", IntegerArgumentType.integer()).executes((p_229632_0_) -> {
         return createNewStructure(p_229632_0_.getSource(), StringArgumentType.getString(p_229632_0_, "testName"), IntegerArgumentType.getInteger(p_229632_0_, "width"), IntegerArgumentType.getInteger(p_229632_0_, "height"), IntegerArgumentType.getInteger(p_229632_0_, "depth"));
      })))))).then(Commands.literal("clearall").executes((p_229628_0_) -> {
         return clearAllTests(p_229628_0_.getSource(), 200);
      }).then(Commands.argument("radius", IntegerArgumentType.integer()).executes((p_229614_0_) -> {
         return clearAllTests(p_229614_0_.getSource(), IntegerArgumentType.getInteger(p_229614_0_, "radius"));
      }))));
   }

   private static int createNewStructure(CommandSource pSource, String pStructureName, int pX, int pY, int pZ) {
      if (pX <= 48 && pY <= 48 && pZ <= 48) {
         ServerWorld serverworld = pSource.getLevel();
         BlockPos blockpos = new BlockPos(pSource.getPosition());
         BlockPos blockpos1 = new BlockPos(blockpos.getX(), pSource.getLevel().getHeightmapPos(Heightmap.Type.WORLD_SURFACE, blockpos).getY(), blockpos.getZ() + 3);
         StructureHelper.createNewEmptyStructureBlock(pStructureName.toLowerCase(), blockpos1, new BlockPos(pX, pY, pZ), Rotation.NONE, serverworld);

         for(int i = 0; i < pX; ++i) {
            for(int j = 0; j < pZ; ++j) {
               BlockPos blockpos2 = new BlockPos(blockpos1.getX() + i, blockpos1.getY() + 1, blockpos1.getZ() + j);
               Block block = Blocks.POLISHED_ANDESITE;
               BlockStateInput blockstateinput = new BlockStateInput(block.defaultBlockState(), Collections.EMPTY_SET, (CompoundNBT)null);
               blockstateinput.place(serverworld, blockpos2, 2);
            }
         }

         StructureHelper.addCommandBlockAndButtonToStartTest(blockpos1, new BlockPos(1, 0, -1), Rotation.NONE, serverworld);
         return 0;
      } else {
         throw new IllegalArgumentException("The structure must be less than 48 blocks big in each axis");
      }
   }

   private static int showPos(CommandSource pSource, String p_229617_1_) throws CommandSyntaxException {
      BlockRayTraceResult blockraytraceresult = (BlockRayTraceResult)pSource.getPlayerOrException().pick(10.0D, 1.0F, false);
      BlockPos blockpos = blockraytraceresult.getBlockPos();
      ServerWorld serverworld = pSource.getLevel();
      Optional<BlockPos> optional = StructureHelper.findStructureBlockContainingPos(blockpos, 15, serverworld);
      if (!optional.isPresent()) {
         optional = StructureHelper.findStructureBlockContainingPos(blockpos, 200, serverworld);
      }

      if (!optional.isPresent()) {
         pSource.sendFailure(new StringTextComponent("Can't find a structure block that contains the targeted pos " + blockpos));
         return 0;
      } else {
         StructureBlockTileEntity structureblocktileentity = (StructureBlockTileEntity)serverworld.getBlockEntity(optional.get());
         BlockPos blockpos1 = blockpos.subtract(optional.get());
         String s = blockpos1.getX() + ", " + blockpos1.getY() + ", " + blockpos1.getZ();
         String s1 = structureblocktileentity.getStructurePath();
         ITextComponent itextcomponent = (new StringTextComponent(s)).setStyle(Style.EMPTY.withBold(true).withColor(TextFormatting.GREEN).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Click to copy to clipboard"))).withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, "final BlockPos " + p_229617_1_ + " = new BlockPos(" + s + ");")));
         pSource.sendSuccess((new StringTextComponent("Position relative to " + s1 + ": ")).append(itextcomponent), false);
         DebugPacketSender.sendGameTestAddMarker(serverworld, new BlockPos(blockpos), s, -2147418368, 10000);
         return 1;
      }
   }

   private static int runNearbyTest(CommandSource pSource) {
      BlockPos blockpos = new BlockPos(pSource.getPosition());
      ServerWorld serverworld = pSource.getLevel();
      BlockPos blockpos1 = StructureHelper.findNearestStructureBlock(blockpos, 15, serverworld);
      if (blockpos1 == null) {
         say(serverworld, "Couldn't find any structure block within 15 radius", TextFormatting.RED);
         return 0;
      } else {
         TestUtils.clearMarkers(serverworld);
         runTest(serverworld, blockpos1, (TestResultList)null);
         return 1;
      }
   }

   private static int runAllNearbyTests(CommandSource pSource) {
      BlockPos blockpos = new BlockPos(pSource.getPosition());
      ServerWorld serverworld = pSource.getLevel();
      Collection<BlockPos> collection = StructureHelper.findStructureBlocks(blockpos, 200, serverworld);
      if (collection.isEmpty()) {
         say(serverworld, "Couldn't find any structure blocks within 200 block radius", TextFormatting.RED);
         return 1;
      } else {
         TestUtils.clearMarkers(serverworld);
         say(pSource, "Running " + collection.size() + " tests...");
         TestResultList testresultlist = new TestResultList();
         collection.forEach((p_229626_2_) -> {
            runTest(serverworld, p_229626_2_, testresultlist);
         });
         return 1;
      }
   }

   private static void runTest(ServerWorld pServerLevel, BlockPos pPos, @Nullable TestResultList pTracker) {
      StructureBlockTileEntity structureblocktileentity = (StructureBlockTileEntity)pServerLevel.getBlockEntity(pPos);
      String s = structureblocktileentity.getStructurePath();
      TestFunctionInfo testfunctioninfo = TestRegistry.getTestFunction(s);
      TestTracker testtracker = new TestTracker(testfunctioninfo, structureblocktileentity.getRotation(), pServerLevel);
      if (pTracker != null) {
         pTracker.addTestToTrack(testtracker);
         testtracker.addListener(new TestCommand.Callback(pServerLevel, pTracker));
      }

      runTestPreparation(testfunctioninfo, pServerLevel);
      AxisAlignedBB axisalignedbb = StructureHelper.getStructureBounds(structureblocktileentity);
      BlockPos blockpos = new BlockPos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ);
      TestUtils.runTest(testtracker, blockpos, TestCollection.singleton);
   }

   private static void showTestSummaryIfAllDone(ServerWorld pServerLevel, TestResultList pTracker) {
      if (pTracker.isDone()) {
         say(pServerLevel, "GameTest done! " + pTracker.getTotalCount() + " tests were run", TextFormatting.WHITE);
         if (pTracker.hasFailedRequired()) {
            say(pServerLevel, "" + pTracker.getFailedRequiredCount() + " required tests failed :(", TextFormatting.RED);
         } else {
            say(pServerLevel, "All required tests passed :)", TextFormatting.GREEN);
         }

         if (pTracker.hasFailedOptional()) {
            say(pServerLevel, "" + pTracker.getFailedOptionalCount() + " optional tests failed", TextFormatting.GRAY);
         }
      }

   }

   private static int clearAllTests(CommandSource pSource, int pRadius) {
      ServerWorld serverworld = pSource.getLevel();
      TestUtils.clearMarkers(serverworld);
      BlockPos blockpos = new BlockPos(pSource.getPosition().x, (double)pSource.getLevel().getHeightmapPos(Heightmap.Type.WORLD_SURFACE, new BlockPos(pSource.getPosition())).getY(), pSource.getPosition().z);
      TestUtils.clearAllTests(serverworld, blockpos, TestCollection.singleton, MathHelper.clamp(pRadius, 0, 1024));
      return 1;
   }

   private static int runTest(CommandSource pSource, TestFunctionInfo pFunction, int pRotationSteps) {
      ServerWorld serverworld = pSource.getLevel();
      BlockPos blockpos = new BlockPos(pSource.getPosition());
      int i = pSource.getLevel().getHeightmapPos(Heightmap.Type.WORLD_SURFACE, blockpos).getY();
      BlockPos blockpos1 = new BlockPos(blockpos.getX(), i, blockpos.getZ() + 3);
      TestUtils.clearMarkers(serverworld);
      runTestPreparation(pFunction, serverworld);
      Rotation rotation = StructureHelper.getRotationForRotationSteps(pRotationSteps);
      TestTracker testtracker = new TestTracker(pFunction, rotation, serverworld);
      TestUtils.runTest(testtracker, blockpos1, TestCollection.singleton);
      return 1;
   }

   private static void runTestPreparation(TestFunctionInfo pFunction, ServerWorld pServerLevel) {
      Consumer<ServerWorld> consumer = TestRegistry.getBeforeBatchFunction(pFunction.getBatchName());
      if (consumer != null) {
         consumer.accept(pServerLevel);
      }

   }

   private static int runAllTests(CommandSource pSource, int pRotationSteps, int pTestsPerRow) {
      TestUtils.clearMarkers(pSource.getLevel());
      Collection<TestFunctionInfo> collection = TestRegistry.getAllTestFunctions();
      say(pSource, "Running all " + collection.size() + " tests...");
      TestRegistry.forgetFailedTests();
      runTests(pSource, collection, pRotationSteps, pTestsPerRow);
      return 1;
   }

   private static int runAllTestsInClass(CommandSource pSource, String pTestClassName, int pRotationSteps, int pTestsPerRow) {
      Collection<TestFunctionInfo> collection = TestRegistry.getTestFunctionsForClassName(pTestClassName);
      TestUtils.clearMarkers(pSource.getLevel());
      say(pSource, "Running " + collection.size() + " tests from " + pTestClassName + "...");
      TestRegistry.forgetFailedTests();
      runTests(pSource, collection, pRotationSteps, pTestsPerRow);
      return 1;
   }

   private static int runLastFailedTests(CommandSource pSource, boolean pRunOnlyRequired, int pRotationSteps, int pTestsPerRow) {
      Collection<TestFunctionInfo> collection;
      if (pRunOnlyRequired) {
         collection = TestRegistry.getLastFailedTests().stream().filter(TestFunctionInfo::isRequired).collect(Collectors.toList());
      } else {
         collection = TestRegistry.getLastFailedTests();
      }

      if (collection.isEmpty()) {
         say(pSource, "No failed tests to rerun");
         return 0;
      } else {
         TestUtils.clearMarkers(pSource.getLevel());
         say(pSource, "Rerunning " + collection.size() + " failed tests (" + (pRunOnlyRequired ? "only required tests" : "including optional tests") + ")");
         runTests(pSource, collection, pRotationSteps, pTestsPerRow);
         return 1;
      }
   }

   private static void runTests(CommandSource pSource, Collection<TestFunctionInfo> p_229619_1_, int pRotationSteps, int pTestsPerRow) {
      BlockPos blockpos = new BlockPos(pSource.getPosition());
      BlockPos blockpos1 = new BlockPos(blockpos.getX(), pSource.getLevel().getHeightmapPos(Heightmap.Type.WORLD_SURFACE, blockpos).getY(), blockpos.getZ() + 3);
      ServerWorld serverworld = pSource.getLevel();
      Rotation rotation = StructureHelper.getRotationForRotationSteps(pRotationSteps);
      Collection<TestTracker> collection = TestUtils.runTests(p_229619_1_, blockpos1, rotation, serverworld, TestCollection.singleton, pTestsPerRow);
      TestResultList testresultlist = new TestResultList(collection);
      testresultlist.addListener(new TestCommand.Callback(serverworld, testresultlist));
      testresultlist.addFailureListener((p_240576_0_) -> {
         TestRegistry.rememberFailedTest(p_240576_0_.getTestFunction());
      });
   }

   private static void say(CommandSource pSource, String pMessage) {
      pSource.sendSuccess(new StringTextComponent(pMessage), false);
   }

   private static int exportNearestTestStructure(CommandSource pSource) {
      BlockPos blockpos = new BlockPos(pSource.getPosition());
      ServerWorld serverworld = pSource.getLevel();
      BlockPos blockpos1 = StructureHelper.findNearestStructureBlock(blockpos, 15, serverworld);
      if (blockpos1 == null) {
         say(serverworld, "Couldn't find any structure block within 15 radius", TextFormatting.RED);
         return 0;
      } else {
         StructureBlockTileEntity structureblocktileentity = (StructureBlockTileEntity)serverworld.getBlockEntity(blockpos1);
         String s = structureblocktileentity.getStructurePath();
         return exportTestStructure(pSource, s);
      }
   }

   private static int exportTestStructure(CommandSource pSource, String pStructurePath) {
      Path path = Paths.get(StructureHelper.testStructuresDir);
      ResourceLocation resourcelocation = new ResourceLocation("minecraft", pStructurePath);
      Path path1 = pSource.getLevel().getStructureManager().createPathToStructure(resourcelocation, ".nbt");
      Path path2 = NBTToSNBTConverter.convertStructure(path1, pStructurePath, path);
      if (path2 == null) {
         say(pSource, "Failed to export " + path1);
         return 1;
      } else {
         try {
            Files.createDirectories(path2.getParent());
         } catch (IOException ioexception) {
            say(pSource, "Could not create folder " + path2.getParent());
            ioexception.printStackTrace();
            return 1;
         }

         say(pSource, "Exported " + pStructurePath + " to " + path2.toAbsolutePath());
         return 0;
      }
   }

   private static int importTestStructure(CommandSource pSource, String pStructurePath) {
      Path path = Paths.get(StructureHelper.testStructuresDir, pStructurePath + ".snbt");
      ResourceLocation resourcelocation = new ResourceLocation("minecraft", pStructurePath);
      Path path1 = pSource.getLevel().getStructureManager().createPathToStructure(resourcelocation, ".nbt");

      try {
         BufferedReader bufferedreader = Files.newBufferedReader(path);
         String s = IOUtils.toString((Reader)bufferedreader);
         Files.createDirectories(path1.getParent());

         try (OutputStream outputstream = Files.newOutputStream(path1)) {
            CompressedStreamTools.writeCompressed(JsonToNBT.parseTag(s), outputstream);
         }

         say(pSource, "Imported to " + path1.toAbsolutePath());
         return 0;
      } catch (CommandSyntaxException | IOException ioexception) {
         System.err.println("Failed to load structure " + pStructurePath);
         ioexception.printStackTrace();
         return 1;
      }
   }

   private static void say(ServerWorld pServerLevel, String pMessage, TextFormatting pFormatting) {
      pServerLevel.getPlayers((p_229627_0_) -> {
         return true;
      }).forEach((p_229621_2_) -> {
         p_229621_2_.sendMessage(new StringTextComponent(pFormatting + pMessage), Util.NIL_UUID);
      });
   }

   static class Callback implements ITestCallback {
      private final ServerWorld level;
      private final TestResultList tracker;

      public Callback(ServerWorld pServerLevel, TestResultList pTracker) {
         this.level = pServerLevel;
         this.tracker = pTracker;
      }

      public void testStructureLoaded(TestTracker pTestInfo) {
      }

      public void testFailed(TestTracker pTestInfo) {
         TestCommand.showTestSummaryIfAllDone(this.level, this.tracker);
      }
   }
}