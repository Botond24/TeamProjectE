package net.minecraft.crash;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CrashReportCategory {
   private final CrashReport report;
   private final String title;
   private final List<CrashReportCategory.Entry> entries = Lists.newArrayList();
   private StackTraceElement[] stackTrace = new StackTraceElement[0];

   public CrashReportCategory(CrashReport p_i1353_1_, String p_i1353_2_) {
      this.report = p_i1353_1_;
      this.title = p_i1353_2_;
   }

   @OnlyIn(Dist.CLIENT)
   public static String formatLocation(double p_85074_0_, double p_85074_2_, double p_85074_4_) {
      return String.format(Locale.ROOT, "%.2f,%.2f,%.2f - %s", p_85074_0_, p_85074_2_, p_85074_4_, formatLocation(new BlockPos(p_85074_0_, p_85074_2_, p_85074_4_)));
   }

   public static String formatLocation(BlockPos p_180522_0_) {
      return formatLocation(p_180522_0_.getX(), p_180522_0_.getY(), p_180522_0_.getZ());
   }

   public static String formatLocation(int p_184876_0_, int p_184876_1_, int p_184876_2_) {
      StringBuilder stringbuilder = new StringBuilder();

      try {
         stringbuilder.append(String.format("World: (%d,%d,%d)", p_184876_0_, p_184876_1_, p_184876_2_));
      } catch (Throwable throwable2) {
         stringbuilder.append("(Error finding world loc)");
      }

      stringbuilder.append(", ");

      try {
         int i = p_184876_0_ >> 4;
         int j = p_184876_2_ >> 4;
         int k = p_184876_0_ & 15;
         int l = p_184876_1_ >> 4;
         int i1 = p_184876_2_ & 15;
         int j1 = i << 4;
         int k1 = j << 4;
         int l1 = (i + 1 << 4) - 1;
         int i2 = (j + 1 << 4) - 1;
         stringbuilder.append(String.format("Chunk: (at %d,%d,%d in %d,%d; contains blocks %d,0,%d to %d,255,%d)", k, l, i1, i, j, j1, k1, l1, i2));
      } catch (Throwable throwable1) {
         stringbuilder.append("(Error finding chunk loc)");
      }

      stringbuilder.append(", ");

      try {
         int k2 = p_184876_0_ >> 9;
         int l2 = p_184876_2_ >> 9;
         int i3 = k2 << 5;
         int j3 = l2 << 5;
         int k3 = (k2 + 1 << 5) - 1;
         int l3 = (l2 + 1 << 5) - 1;
         int i4 = k2 << 9;
         int j4 = l2 << 9;
         int k4 = (k2 + 1 << 9) - 1;
         int j2 = (l2 + 1 << 9) - 1;
         stringbuilder.append(String.format("Region: (%d,%d; contains chunks %d,%d to %d,%d, blocks %d,0,%d to %d,255,%d)", k2, l2, i3, j3, k3, l3, i4, j4, k4, j2));
      } catch (Throwable throwable) {
         stringbuilder.append("(Error finding world loc)");
      }

      return stringbuilder.toString();
   }

   /**
    * Adds an additional section to this crash report category, resolved by calling the given callable.
    * 
    * If the given callable throws an exception, a detail containing that exception will be created instead.
    */
   public CrashReportCategory setDetail(String pName, ICrashReportDetail<String> pDetail) {
      try {
         this.setDetail(pName, pDetail.call());
      } catch (Throwable throwable) {
         this.setDetailError(pName, throwable);
      }

      return this;
   }

   /**
    * Adds a Crashreport section with the given name with the given value (convered .toString())
    */
   public CrashReportCategory setDetail(String pSectionName, Object pValue) {
      this.entries.add(new CrashReportCategory.Entry(pSectionName, pValue));
      return this;
   }

   /**
    * Adds a Crashreport section with the given name with the given Throwable
    */
   public void setDetailError(String pSectionName, Throwable pThrowable) {
      this.setDetail(pSectionName, pThrowable);
   }

   /**
    * Resets our stack trace according to the current trace, pruning the deepest 3 entries.  The parameter indicates how
    * many additional deepest entries to prune.  Returns the number of entries in the resulting pruned stack trace.
    */
   public int fillInStackTrace(int pSize) {
      StackTraceElement[] astacktraceelement = Thread.currentThread().getStackTrace();
      if (astacktraceelement.length <= 0) {
         return 0;
      } else {
         int len = astacktraceelement.length - 3 - pSize;
         if (len <= 0) len = astacktraceelement.length;
         this.stackTrace = new StackTraceElement[len];
         System.arraycopy(astacktraceelement, astacktraceelement.length - len, this.stackTrace, 0, this.stackTrace.length);
         return this.stackTrace.length;
      }
   }

   /**
    * Do the deepest two elements of our saved stack trace match the given elements, in order from the deepest?
    */
   public boolean validateStackTrace(StackTraceElement pS1, StackTraceElement pS2) {
      if (this.stackTrace.length != 0 && pS1 != null) {
         StackTraceElement stacktraceelement = this.stackTrace[0];
         if (stacktraceelement.isNativeMethod() == pS1.isNativeMethod() && stacktraceelement.getClassName().equals(pS1.getClassName()) && stacktraceelement.getFileName().equals(pS1.getFileName()) && stacktraceelement.getMethodName().equals(pS1.getMethodName())) {
            if (pS2 != null != this.stackTrace.length > 1) {
               return false;
            } else if (pS2 != null && !this.stackTrace[1].equals(pS2)) {
               return false;
            } else {
               this.stackTrace[0] = pS1;
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   /**
    * Removes the given number entries from the bottom of the stack trace.
    */
   public void trimStacktrace(int pAmount) {
      StackTraceElement[] astacktraceelement = new StackTraceElement[this.stackTrace.length - pAmount];
      System.arraycopy(this.stackTrace, 0, astacktraceelement, 0, astacktraceelement.length);
      this.stackTrace = astacktraceelement;
   }

   public void getDetails(StringBuilder pBuilder) {
      pBuilder.append("-- ").append(this.title).append(" --\n");
      pBuilder.append("Details:");

      for(CrashReportCategory.Entry crashreportcategory$entry : this.entries) {
         pBuilder.append("\n\t");
         pBuilder.append(crashreportcategory$entry.getKey());
         pBuilder.append(": ");
         pBuilder.append(crashreportcategory$entry.getValue());
      }

      if (this.stackTrace != null && this.stackTrace.length > 0) {
         pBuilder.append("\nStacktrace:");
         pBuilder.append(net.minecraftforge.fml.CrashReportExtender.generateEnhancedStackTrace(this.stackTrace));
      }

   }

   public StackTraceElement[] getStacktrace() {
      return this.stackTrace;
   }

   public void applyStackTrace(Throwable t) {
      this.stackTrace = t.getStackTrace();
   }

   public static void populateBlockDetails(CrashReportCategory p_175750_0_, BlockPos p_175750_1_, @Nullable BlockState p_175750_2_) {
      if (p_175750_2_ != null) {
         p_175750_0_.setDetail("Block", p_175750_2_::toString);
      }

      p_175750_0_.setDetail("Block location", () -> {
         return formatLocation(p_175750_1_);
      });
   }

   static class Entry {
      private final String key;
      private final String value;

      public Entry(String pKey, @Nullable Object pValue) {
         this.key = pKey;
         if (pValue == null) {
            this.value = "~~NULL~~";
         } else if (pValue instanceof Throwable) {
            Throwable throwable = (Throwable)pValue;
            this.value = "~~ERROR~~ " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
         } else {
            this.value = pValue.toString();
         }

      }

      public String getKey() {
         return this.key;
      }

      public String getValue() {
         return this.value;
      }
   }
}
