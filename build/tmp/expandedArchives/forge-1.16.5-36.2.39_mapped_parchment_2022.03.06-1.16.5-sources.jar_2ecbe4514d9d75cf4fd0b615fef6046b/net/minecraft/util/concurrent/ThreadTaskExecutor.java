package net.minecraft.util.concurrent;

import com.google.common.collect.Queues;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ThreadTaskExecutor<R extends Runnable> implements ITaskExecutor<R>, Executor {
   private final String name;
   private static final Logger LOGGER = LogManager.getLogger();
   private final Queue<R> pendingRunnables = Queues.newConcurrentLinkedQueue();
   private int blockingCount;

   protected ThreadTaskExecutor(String p_i50403_1_) {
      this.name = p_i50403_1_;
   }

   protected abstract R wrapRunnable(Runnable pRunnable);

   protected abstract boolean shouldRun(R pRunnable);

   public boolean isSameThread() {
      return Thread.currentThread() == this.getRunningThread();
   }

   protected abstract Thread getRunningThread();

   protected boolean scheduleExecutables() {
      return !this.isSameThread();
   }

   public int getPendingTasksCount() {
      return this.pendingRunnables.size();
   }

   public String name() {
      return this.name;
   }

   @OnlyIn(Dist.CLIENT)
   public <V> CompletableFuture<V> submit(Supplier<V> pSupplier) {
      return this.scheduleExecutables() ? CompletableFuture.supplyAsync(pSupplier, this) : CompletableFuture.completedFuture(pSupplier.get());
   }

   public CompletableFuture<Void> submitAsync(Runnable pTask) {
      return CompletableFuture.supplyAsync(() -> {
         pTask.run();
         return null;
      }, this);
   }

   public CompletableFuture<Void> submit(Runnable pTask) {
      if (this.scheduleExecutables()) {
         return this.submitAsync(pTask);
      } else {
         pTask.run();
         return CompletableFuture.completedFuture((Void)null);
      }
   }

   public void executeBlocking(Runnable pTask) {
      if (!this.isSameThread()) {
         this.submitAsync(pTask).join();
      } else {
         pTask.run();
      }

   }

   public void tell(R pTask) {
      this.pendingRunnables.add(pTask);
      LockSupport.unpark(this.getRunningThread());
   }

   public void execute(Runnable p_execute_1_) {
      if (this.scheduleExecutables()) {
         this.tell(this.wrapRunnable(p_execute_1_));
      } else {
         p_execute_1_.run();
      }

   }

   @OnlyIn(Dist.CLIENT)
   protected void dropAllTasks() {
      this.pendingRunnables.clear();
   }

   protected void runAllTasks() {
      while(this.pollTask()) {
      }

   }

   protected boolean pollTask() {
      R r = this.pendingRunnables.peek();
      if (r == null) {
         return false;
      } else if (this.blockingCount == 0 && !this.shouldRun(r)) {
         return false;
      } else {
         this.doRunTask(this.pendingRunnables.remove());
         return true;
      }
   }

   /**
    * Drive the executor until the given BooleanSupplier returns true
    */
   public void managedBlock(BooleanSupplier pIsDone) {
      ++this.blockingCount;

      try {
         while(!pIsDone.getAsBoolean()) {
            if (!this.pollTask()) {
               this.waitForTasks();
            }
         }
      } finally {
         --this.blockingCount;
      }

   }

   protected void waitForTasks() {
      Thread.yield();
      LockSupport.parkNanos("waiting for tasks", 100000L);
   }

   protected void doRunTask(R pTask) {
      try {
         pTask.run();
      } catch (Exception exception) {
         LOGGER.fatal("Error executing task on {}", this.name(), exception);
      }

   }
}