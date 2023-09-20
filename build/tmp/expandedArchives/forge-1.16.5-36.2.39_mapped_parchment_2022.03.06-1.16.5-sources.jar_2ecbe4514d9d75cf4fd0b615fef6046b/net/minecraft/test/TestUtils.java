package net.minecraft.test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.server.ServerWorld;
import org.apache.commons.lang3.mutable.MutableInt;

public class TestUtils {
   public static ITestLogger TEST_REPORTER = new TestLogger();

   public static void runTest(TestTracker pTestInfo, BlockPos pPos, TestCollection pTestTicker) {
      pTestInfo.startExecution();
      pTestTicker.add(pTestInfo);
      pTestInfo.addListener(new ITestCallback() {
         public void testStructureLoaded(TestTracker pTestInfo) {
            TestUtils.spawnBeacon(pTestInfo, Blocks.LIGHT_GRAY_STAINED_GLASS);
         }

         public void testFailed(TestTracker pTestInfo) {
            TestUtils.spawnBeacon(pTestInfo, pTestInfo.isRequired() ? Blocks.RED_STAINED_GLASS : Blocks.ORANGE_STAINED_GLASS);
            TestUtils.spawnLectern(pTestInfo, Util.describeError(pTestInfo.getError()));
            TestUtils.visualizeFailedTest(pTestInfo);
         }
      });
      pTestInfo.spawnStructure(pPos, 2);
   }

   public static Collection<TestTracker> runTestBatches(Collection<TestBatch> pTestBatches, BlockPos pPos, Rotation pRotation, ServerWorld pServerLevel, TestCollection pTestTicker, int pTestsPerRow) {
      TestExecutor testexecutor = new TestExecutor(pTestBatches, pPos, pRotation, pServerLevel, pTestTicker, pTestsPerRow);
      testexecutor.start();
      return testexecutor.getTestInfos();
   }

   public static Collection<TestTracker> runTests(Collection<TestFunctionInfo> pTestFunctions, BlockPos pPos, Rotation pRotation, ServerWorld pServerLevel, TestCollection pTestTicker, int pTestsPerRow) {
      return runTestBatches(groupTestsIntoBatches(pTestFunctions), pPos, pRotation, pServerLevel, pTestTicker, pTestsPerRow);
   }

   public static Collection<TestBatch> groupTestsIntoBatches(Collection<TestFunctionInfo> pTestFunctions) {
      Map<String, Collection<TestFunctionInfo>> map = Maps.newHashMap();
      pTestFunctions.forEach((p_229551_1_) -> {
         String s = p_229551_1_.getBatchName();
         Collection<TestFunctionInfo> collection = map.computeIfAbsent(s, (p_229543_0_) -> {
            return Lists.newArrayList();
         });
         collection.add(p_229551_1_);
      });
      return map.keySet().stream().flatMap((p_229550_1_) -> {
         Collection<TestFunctionInfo> collection = map.get(p_229550_1_);
         Consumer<ServerWorld> consumer = TestRegistry.getBeforeBatchFunction(p_229550_1_);
         MutableInt mutableint = new MutableInt();
         return Streams.stream(Iterables.partition(collection, 100)).map((p_240551_4_) -> {
            return new TestBatch(p_229550_1_ + ":" + mutableint.incrementAndGet(), collection, consumer);
         });
      }).collect(Collectors.toList());
   }

   private static void visualizeFailedTest(TestTracker p_229563_0_) {
      Throwable throwable = p_229563_0_.getError();
      String s = (p_229563_0_.isRequired() ? "" : "(optional) ") + p_229563_0_.getTestName() + " failed! " + Util.describeError(throwable);
      say(p_229563_0_.getLevel(), p_229563_0_.isRequired() ? TextFormatting.RED : TextFormatting.YELLOW, s);
      if (throwable instanceof TestBlockPosException) {
         TestBlockPosException testblockposexception = (TestBlockPosException)throwable;
         showRedBox(p_229563_0_.getLevel(), testblockposexception.getAbsolutePos(), testblockposexception.getMessageToShowAtBlock());
      }

      TEST_REPORTER.onTestFailed(p_229563_0_);
   }

   private static void spawnBeacon(TestTracker p_229559_0_, Block p_229559_1_) {
      ServerWorld serverworld = p_229559_0_.getLevel();
      BlockPos blockpos = p_229559_0_.getStructureBlockPos();
      BlockPos blockpos1 = new BlockPos(-1, -1, -1);
      BlockPos blockpos2 = Template.transform(blockpos.offset(blockpos1), Mirror.NONE, p_229559_0_.getRotation(), blockpos);
      serverworld.setBlockAndUpdate(blockpos2, Blocks.BEACON.defaultBlockState().rotate(p_229559_0_.getRotation()));
      BlockPos blockpos3 = blockpos2.offset(0, 1, 0);
      serverworld.setBlockAndUpdate(blockpos3, p_229559_1_.defaultBlockState());

      for(int i = -1; i <= 1; ++i) {
         for(int j = -1; j <= 1; ++j) {
            BlockPos blockpos4 = blockpos2.offset(i, -1, j);
            serverworld.setBlockAndUpdate(blockpos4, Blocks.IRON_BLOCK.defaultBlockState());
         }
      }

   }

   private static void spawnLectern(TestTracker p_229560_0_, String p_229560_1_) {
      ServerWorld serverworld = p_229560_0_.getLevel();
      BlockPos blockpos = p_229560_0_.getStructureBlockPos();
      BlockPos blockpos1 = new BlockPos(-1, 1, -1);
      BlockPos blockpos2 = Template.transform(blockpos.offset(blockpos1), Mirror.NONE, p_229560_0_.getRotation(), blockpos);
      serverworld.setBlockAndUpdate(blockpos2, Blocks.LECTERN.defaultBlockState().rotate(p_229560_0_.getRotation()));
      BlockState blockstate = serverworld.getBlockState(blockpos2);
      ItemStack itemstack = createBook(p_229560_0_.getTestName(), p_229560_0_.isRequired(), p_229560_1_);
      LecternBlock.tryPlaceBook(serverworld, blockpos2, blockstate, itemstack);
   }

   private static ItemStack createBook(String p_229546_0_, boolean p_229546_1_, String p_229546_2_) {
      ItemStack itemstack = new ItemStack(Items.WRITABLE_BOOK);
      ListNBT listnbt = new ListNBT();
      StringBuffer stringbuffer = new StringBuffer();
      Arrays.stream(p_229546_0_.split("\\.")).forEach((p_229547_1_) -> {
         stringbuffer.append(p_229547_1_).append('\n');
      });
      if (!p_229546_1_) {
         stringbuffer.append("(optional)\n");
      }

      stringbuffer.append("-------------------\n");
      listnbt.add(StringNBT.valueOf(stringbuffer.toString() + p_229546_2_));
      itemstack.addTagElement("pages", listnbt);
      return itemstack;
   }

   private static void say(ServerWorld p_229556_0_, TextFormatting p_229556_1_, String p_229556_2_) {
      p_229556_0_.getPlayers((p_229557_0_) -> {
         return true;
      }).forEach((p_229544_2_) -> {
         p_229544_2_.sendMessage((new StringTextComponent(p_229556_2_)).withStyle(p_229556_1_), Util.NIL_UUID);
      });
   }

   public static void clearMarkers(ServerWorld pServerLevel) {
      DebugPacketSender.sendGameTestClearPacket(pServerLevel);
   }

   private static void showRedBox(ServerWorld p_229554_0_, BlockPos p_229554_1_, String p_229554_2_) {
      DebugPacketSender.sendGameTestAddMarker(p_229554_0_, p_229554_1_, p_229554_2_, -2130771968, Integer.MAX_VALUE);
   }

   public static void clearAllTests(ServerWorld pServerLevel, BlockPos pPos, TestCollection pTestTicker, int pRadius) {
      pTestTicker.clear();
      BlockPos blockpos = pPos.offset(-pRadius, 0, -pRadius);
      BlockPos blockpos1 = pPos.offset(pRadius, 0, pRadius);
      BlockPos.betweenClosedStream(blockpos, blockpos1).filter((p_229562_1_) -> {
         return pServerLevel.getBlockState(p_229562_1_).is(Blocks.STRUCTURE_BLOCK);
      }).forEach((p_229553_1_) -> {
         StructureBlockTileEntity structureblocktileentity = (StructureBlockTileEntity)pServerLevel.getBlockEntity(p_229553_1_);
         BlockPos blockpos2 = structureblocktileentity.getBlockPos();
         MutableBoundingBox mutableboundingbox = StructureHelper.getStructureBoundingBox(structureblocktileentity);
         StructureHelper.clearSpaceForStructure(mutableboundingbox, blockpos2.getY(), pServerLevel);
      });
   }
}