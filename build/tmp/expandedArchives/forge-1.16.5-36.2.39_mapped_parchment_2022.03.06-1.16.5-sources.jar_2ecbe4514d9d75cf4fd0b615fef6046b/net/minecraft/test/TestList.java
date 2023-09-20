package net.minecraft.test;

import java.util.Iterator;
import java.util.List;

public class TestList {
   private final TestTracker parent = null;
   private final List<TestTickResult> events = null;
   private long lastTick;

   public void tickAndContinue(long pTick) {
      try {
         this.tick(pTick);
      } catch (Exception exception) {
      }

   }

   public void tickAndFailIfNotComplete(long pTicks) {
      try {
         this.tick(pTicks);
      } catch (Exception exception) {
         this.parent.fail(exception);
      }

   }

   private void tick(long pTick) {
      Iterator<TestTickResult> iterator = this.events.iterator();

      while(iterator.hasNext()) {
         TestTickResult testtickresult = iterator.next();
         testtickresult.assertion.run();
         iterator.remove();
         long i = pTick - this.lastTick;
         long j = this.lastTick;
         this.lastTick = pTick;
         if (testtickresult.expectedDelay != null && testtickresult.expectedDelay != i) {
            this.parent.fail(new TestRuntimeException("Succeeded in invalid tick: expected " + (j + testtickresult.expectedDelay) + ", but current tick is " + pTick));
            break;
         }
      }

   }

   private TestList() {
      throw new RuntimeException("Synthetic constructor added by MCP, do not call");
   }
}