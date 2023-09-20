package net.minecraft.network;

import net.minecraft.util.concurrent.ThreadTaskExecutor;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PacketThreadUtil {
   private static final Logger LOGGER = LogManager.getLogger();

   /**
    * Ensures that the given packet is handled on the main thread. If the current thread is not the main thread, this
    * method
    * throws {@link RunningOnDifferentThreadException}, which is caught and ignored in the outer call ({@link
    * Connection#channelRead0}). Additionally it then re-schedules the packet to be handled on the main thread,
    * which will then end up back here, but this time on the main thread.
    */
   public static <T extends INetHandler> void ensureRunningOnSameThread(IPacket<T> pPacket, T pProcessor, ServerWorld pLevel) throws ThreadQuickExitException {
      ensureRunningOnSameThread(pPacket, pProcessor, pLevel.getServer());
   }

   /**
    * Ensures that the given packet is handled on the main thread. If the current thread is not the main thread, this
    * method
    * throws {@link RunningOnDifferentThreadException}, which is caught and ignored in the outer call ({@link
    * Connection#channelRead0}). Additionally it then re-schedules the packet to be handled on the main thread,
    * which will then end up back here, but this time on the main thread.
    */
   public static <T extends INetHandler> void ensureRunningOnSameThread(IPacket<T> pPacket, T pProcessor, ThreadTaskExecutor<?> pExecutor) throws ThreadQuickExitException {
      if (!pExecutor.isSameThread()) {
         pExecutor.execute(() -> {
            if (pProcessor.getConnection().isConnected()) {
               pPacket.handle(pProcessor);
            } else {
               LOGGER.debug("Ignoring packet due to disconnection: " + pPacket);
            }

         });
         throw ThreadQuickExitException.RUNNING_ON_DIFFERENT_THREAD;
      }
   }
}