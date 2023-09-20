package net.minecraft.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.Unit;

/**
 * @deprecated Forge: {@link net.minecraftforge.resource.ISelectiveResourceReloadListener}, which selectively allows
 * individual resource types being reloaded should rather be used where possible.
 */
@Deprecated
public interface IResourceManagerReloadListener extends IFutureReloadListener {
   default CompletableFuture<Void> reload(IFutureReloadListener.IStage pStage, IResourceManager pResourceManager, IProfiler pPreparationsProfiler, IProfiler pReloadProfiler, Executor pBackgroundExecutor, Executor pGameExecutor) {
      return pStage.wait(Unit.INSTANCE).thenRunAsync(() -> {
         pReloadProfiler.startTick();
         pReloadProfiler.push("listener");
         this.onResourceManagerReload(pResourceManager);
         pReloadProfiler.pop();
         pReloadProfiler.endTick();
      }, pGameExecutor);
   }

   void onResourceManagerReload(IResourceManager pResourceManager);

   @javax.annotation.Nullable
   default net.minecraftforge.resource.IResourceType getResourceType() {
      return null;
   }
}
