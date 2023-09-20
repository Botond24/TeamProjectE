package net.minecraft.test;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.function.Consumer;
import javax.annotation.Nullable;

public class TestResultList {
   private final Collection<TestTracker> tests = Lists.newArrayList();
   @Nullable
   private Collection<ITestCallback> listeners = Lists.newArrayList();

   public TestResultList() {
   }

   public TestResultList(Collection<TestTracker> pTestInfos) {
      this.tests.addAll(pTestInfos);
   }

   public void addTestToTrack(TestTracker pTestInfo) {
      this.tests.add(pTestInfo);
      this.listeners.forEach(pTestInfo::addListener);
   }

   public void addListener(ITestCallback pTestListener) {
      this.listeners.add(pTestListener);
      this.tests.forEach((p_240559_1_) -> {
         p_240559_1_.addListener(pTestListener);
      });
   }

   public void addFailureListener(final Consumer<TestTracker> p_240556_1_) {
      this.addListener(new ITestCallback() {
         public void testStructureLoaded(TestTracker pTestInfo) {
         }

         public void testFailed(TestTracker pTestInfo) {
            p_240556_1_.accept(pTestInfo);
         }
      });
   }

   public int getFailedRequiredCount() {
      return (int)this.tests.stream().filter(TestTracker::hasFailed).filter(TestTracker::isRequired).count();
   }

   public int getFailedOptionalCount() {
      return (int)this.tests.stream().filter(TestTracker::hasFailed).filter(TestTracker::isOptional).count();
   }

   public int getDoneCount() {
      return (int)this.tests.stream().filter(TestTracker::isDone).count();
   }

   public boolean hasFailedRequired() {
      return this.getFailedRequiredCount() > 0;
   }

   public boolean hasFailedOptional() {
      return this.getFailedOptionalCount() > 0;
   }

   public int getTotalCount() {
      return this.tests.size();
   }

   public boolean isDone() {
      return this.getDoneCount() == this.getTotalCount();
   }

   public String getProgressBar() {
      StringBuffer stringbuffer = new StringBuffer();
      stringbuffer.append('[');
      this.tests.forEach((p_229582_1_) -> {
         if (!p_229582_1_.hasStarted()) {
            stringbuffer.append(' ');
         } else if (p_229582_1_.hasSucceeded()) {
            stringbuffer.append('+');
         } else if (p_229582_1_.hasFailed()) {
            stringbuffer.append((char)(p_229582_1_.isRequired() ? 'X' : 'x'));
         } else {
            stringbuffer.append('_');
         }

      });
      stringbuffer.append(']');
      return stringbuffer.toString();
   }

   public String toString() {
      return this.getProgressBar();
   }
}