package net.minecraft.resources;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.util.Unit;

public interface IReloadableResourceManager extends IResourceManager, AutoCloseable {
   default CompletableFuture<Unit> reload(Executor pBackgroundExecutor, Executor pGameExecutor, List<IResourcePack> pResourcePacks, CompletableFuture<Unit> pWaitingFor) {
      return this.createFullReload(pBackgroundExecutor, pGameExecutor, pWaitingFor, pResourcePacks).done();
   }

   IAsyncReloader createFullReload(Executor p_219537_1_, Executor p_219537_2_, CompletableFuture<Unit> p_219537_3_, List<IResourcePack> p_219537_4_);

   void registerReloadListener(IFutureReloadListener pListener);

   void close();
}