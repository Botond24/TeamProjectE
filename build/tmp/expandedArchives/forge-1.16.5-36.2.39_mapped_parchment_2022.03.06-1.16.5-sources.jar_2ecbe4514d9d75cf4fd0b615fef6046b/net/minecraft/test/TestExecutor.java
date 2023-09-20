package net.minecraft.test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestExecutor {
   private static final Logger LOGGER = LogManager.getLogger();
   private final BlockPos firstTestNorthWestCorner;
   private final ServerWorld level;
   private final TestCollection testTicker;
   private final int testsPerRow;
   private final List<TestTracker> allTestInfos = Lists.newArrayList();
   private final Map<TestTracker, BlockPos> northWestCorners = Maps.newHashMap();
   private final List<Pair<TestBatch, Collection<TestTracker>>> batches = Lists.newArrayList();
   private TestResultList currentBatchTracker;
   private int currentBatchIndex = 0;
   private BlockPos.Mutable nextTestNorthWestCorner;

   public TestExecutor(Collection<TestBatch> pTestBatches, BlockPos pPos, Rotation pRotation, ServerWorld pServerLevel, TestCollection pTestTicker, int pTestsPerRow) {
      this.nextTestNorthWestCorner = pPos.mutable();
      this.firstTestNorthWestCorner = pPos;
      this.level = pServerLevel;
      this.testTicker = pTestTicker;
      this.testsPerRow = pTestsPerRow;
      pTestBatches.forEach((p_240539_3_) -> {
         Collection<TestTracker> collection = Lists.newArrayList();

         for(TestFunctionInfo testfunctioninfo : p_240539_3_.getTestFunctions()) {
            TestTracker testtracker = new TestTracker(testfunctioninfo, pRotation, pServerLevel);
            collection.add(testtracker);
            this.allTestInfos.add(testtracker);
         }

         this.batches.add(Pair.of(p_240539_3_, collection));
      });
   }

   public List<TestTracker> getTestInfos() {
      return this.allTestInfos;
   }

   public void start() {
      this.runBatch(0);
   }

   private void runBatch(int pBatchId) {
      this.currentBatchIndex = pBatchId;
      this.currentBatchTracker = new TestResultList();
      if (pBatchId < this.batches.size()) {
         Pair<TestBatch, Collection<TestTracker>> pair = this.batches.get(this.currentBatchIndex);
         TestBatch testbatch = pair.getFirst();
         Collection<TestTracker> collection = pair.getSecond();
         this.createStructuresForBatch(collection);
         testbatch.runBeforeBatchFunction(this.level);
         String s = testbatch.getName();
         LOGGER.info("Running test batch '" + s + "' (" + collection.size() + " tests)...");
         collection.forEach((p_229483_1_) -> {
            this.currentBatchTracker.addTestToTrack(p_229483_1_);
            this.currentBatchTracker.addListener(new ITestCallback() {
               public void testStructureLoaded(TestTracker pTestInfo) {
               }

               public void testFailed(TestTracker pTestInfo) {
                  TestExecutor.this.testCompleted(pTestInfo);
               }
            });
            BlockPos blockpos = this.northWestCorners.get(p_229483_1_);
            TestUtils.runTest(p_229483_1_, blockpos, this.testTicker);
         });
      }
   }

   private void testCompleted(TestTracker p_229479_1_) {
      if (this.currentBatchTracker.isDone()) {
         this.runBatch(this.currentBatchIndex + 1);
      }

   }

   private void createStructuresForBatch(Collection<TestTracker> p_229480_1_) {
      int i = 0;
      AxisAlignedBB axisalignedbb = new AxisAlignedBB(this.nextTestNorthWestCorner);

      for(TestTracker testtracker : p_229480_1_) {
         BlockPos blockpos = new BlockPos(this.nextTestNorthWestCorner);
         StructureBlockTileEntity structureblocktileentity = StructureHelper.spawnStructure(testtracker.getStructureName(), blockpos, testtracker.getRotation(), 2, this.level, true);
         AxisAlignedBB axisalignedbb1 = StructureHelper.getStructureBounds(structureblocktileentity);
         testtracker.setStructureBlockPos(structureblocktileentity.getBlockPos());
         this.northWestCorners.put(testtracker, new BlockPos(this.nextTestNorthWestCorner));
         axisalignedbb = axisalignedbb.minmax(axisalignedbb1);
         this.nextTestNorthWestCorner.move((int)axisalignedbb1.getXsize() + 5, 0, 0);
         if (i++ % this.testsPerRow == this.testsPerRow - 1) {
            this.nextTestNorthWestCorner.move(0, 0, (int)axisalignedbb.getZsize() + 6);
            this.nextTestNorthWestCorner.setX(this.firstTestNorthWestCorner.getX());
            axisalignedbb = new AxisAlignedBB(this.nextTestNorthWestCorner);
         }
      }

   }
}