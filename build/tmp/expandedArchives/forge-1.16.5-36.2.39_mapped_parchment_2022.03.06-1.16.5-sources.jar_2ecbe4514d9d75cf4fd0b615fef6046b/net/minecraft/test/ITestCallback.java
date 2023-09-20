package net.minecraft.test;

public interface ITestCallback {
   void testStructureLoaded(TestTracker pTestInfo);

   void testFailed(TestTracker pTestInfo);
}