package net.minecraft.profiler;

import java.util.function.Supplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IProfiler {
   void startTick();

   void endTick();

   /**
    * Start section
    */
   void push(String pName);

   void push(Supplier<String> pNameSupplier);

   /**
    * End section
    */
   void pop();

   void popPush(String pName);

   @OnlyIn(Dist.CLIENT)
   void popPush(Supplier<String> pNameSupplier);

   void incrementCounter(String pEntryId);

   void incrementCounter(Supplier<String> pEntryIdSupplier);

   static IProfiler tee(final IProfiler p_233513_0_, final IProfiler p_233513_1_) {
      if (p_233513_0_ == EmptyProfiler.INSTANCE) {
         return p_233513_1_;
      } else {
         return p_233513_1_ == EmptyProfiler.INSTANCE ? p_233513_0_ : new IProfiler() {
            public void startTick() {
               p_233513_0_.startTick();
               p_233513_1_.startTick();
            }

            public void endTick() {
               p_233513_0_.endTick();
               p_233513_1_.endTick();
            }

            /**
             * Start section
             */
            public void push(String pName) {
               p_233513_0_.push(pName);
               p_233513_1_.push(pName);
            }

            public void push(Supplier<String> pNameSupplier) {
               p_233513_0_.push(pNameSupplier);
               p_233513_1_.push(pNameSupplier);
            }

            /**
             * End section
             */
            public void pop() {
               p_233513_0_.pop();
               p_233513_1_.pop();
            }

            public void popPush(String pName) {
               p_233513_0_.popPush(pName);
               p_233513_1_.popPush(pName);
            }

            @OnlyIn(Dist.CLIENT)
            public void popPush(Supplier<String> pNameSupplier) {
               p_233513_0_.popPush(pNameSupplier);
               p_233513_1_.popPush(pNameSupplier);
            }

            public void incrementCounter(String pEntryId) {
               p_233513_0_.incrementCounter(pEntryId);
               p_233513_1_.incrementCounter(pEntryId);
            }

            public void incrementCounter(Supplier<String> pEntryIdSupplier) {
               p_233513_0_.incrementCounter(pEntryIdSupplier);
               p_233513_1_.incrementCounter(pEntryIdSupplier);
            }
         };
      }
   }
}