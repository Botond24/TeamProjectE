package net.minecraft.test;

import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestLogger implements ITestLogger {
   private static final Logger LOGGER = LogManager.getLogger();

   public void onTestFailed(TestTracker pTestInfo) {
      if (pTestInfo.isRequired()) {
         LOGGER.error(pTestInfo.getTestName() + " failed! " + Util.describeError(pTestInfo.getError()));
      } else {
         LOGGER.warn("(optional) " + pTestInfo.getTestName() + " failed. " + Util.describeError(pTestInfo.getError()));
      }

   }
}