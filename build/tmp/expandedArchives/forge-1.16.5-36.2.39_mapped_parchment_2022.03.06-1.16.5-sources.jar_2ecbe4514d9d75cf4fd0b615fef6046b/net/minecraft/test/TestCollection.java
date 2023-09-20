package net.minecraft.test;

import com.google.common.collect.Lists;
import java.util.Collection;

public class TestCollection {
   public static final TestCollection singleton = new TestCollection();
   private final Collection<TestTracker> testInfos = Lists.newCopyOnWriteArrayList();

   public void add(TestTracker pTestInfo) {
      this.testInfos.add(pTestInfo);
   }

   public void clear() {
      this.testInfos.clear();
   }

   public void tick() {
      this.testInfos.forEach(TestTracker::tick);
      this.testInfos.removeIf(TestTracker::isDone);
   }
}