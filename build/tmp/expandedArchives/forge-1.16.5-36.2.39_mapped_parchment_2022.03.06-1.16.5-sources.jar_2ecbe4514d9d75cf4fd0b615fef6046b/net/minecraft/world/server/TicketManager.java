package net.minecraft.world.server;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntMaps;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.concurrent.ITaskExecutor;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkDistanceGraph;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ChunkTaskPriorityQueueSorter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class TicketManager {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final int PLAYER_TICKET_LEVEL = 33 + ChunkStatus.getDistance(ChunkStatus.FULL) - 2;
   private final Long2ObjectMap<ObjectSet<ServerPlayerEntity>> playersPerChunk = new Long2ObjectOpenHashMap<>();
   private final Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> tickets = new Long2ObjectOpenHashMap<>();
   private final TicketManager.ChunkTicketTracker ticketTracker = new TicketManager.ChunkTicketTracker();
   private final TicketManager.PlayerChunkTracker naturalSpawnChunkCounter = new TicketManager.PlayerChunkTracker(8);
   private final TicketManager.PlayerTicketTracker playerTicketManager = new TicketManager.PlayerTicketTracker(33);
   private final Set<ChunkHolder> chunksToUpdateFutures = Sets.newHashSet();
   private final ChunkTaskPriorityQueueSorter ticketThrottler;
   private final ITaskExecutor<ChunkTaskPriorityQueueSorter.FunctionEntry<Runnable>> ticketThrottlerInput;
   private final ITaskExecutor<ChunkTaskPriorityQueueSorter.RunnableEntry> ticketThrottlerReleaser;
   private final LongSet ticketsToRelease = new LongOpenHashSet();
   private final Executor mainThreadExecutor;
   private long ticketTickCounter;

   private final Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> forcedTickets = new Long2ObjectOpenHashMap<>();

   protected TicketManager(Executor p_i50707_1_, Executor p_i50707_2_) {
      ITaskExecutor<Runnable> itaskexecutor = ITaskExecutor.of("player ticket throttler", p_i50707_2_::execute);
      ChunkTaskPriorityQueueSorter chunktaskpriorityqueuesorter = new ChunkTaskPriorityQueueSorter(ImmutableList.of(itaskexecutor), p_i50707_1_, 4);
      this.ticketThrottler = chunktaskpriorityqueuesorter;
      this.ticketThrottlerInput = chunktaskpriorityqueuesorter.getProcessor(itaskexecutor, true);
      this.ticketThrottlerReleaser = chunktaskpriorityqueuesorter.getReleaseProcessor(itaskexecutor);
      this.mainThreadExecutor = p_i50707_2_;
   }

   protected void purgeStaleTickets() {
      ++this.ticketTickCounter;
      ObjectIterator<Entry<SortedArraySet<Ticket<?>>>> objectiterator = this.tickets.long2ObjectEntrySet().fastIterator();

      while(objectiterator.hasNext()) {
         Entry<SortedArraySet<Ticket<?>>> entry = objectiterator.next();
         if (entry.getValue().removeIf((p_219370_1_) -> {
            return p_219370_1_.timedOut(this.ticketTickCounter);
         })) {
            this.ticketTracker.update(entry.getLongKey(), getTicketLevelAt(entry.getValue()), false);
         }

         if (entry.getValue().isEmpty()) {
            objectiterator.remove();
         }
      }

   }

   private static int getTicketLevelAt(SortedArraySet<Ticket<?>> p_229844_0_) {
      return !p_229844_0_.isEmpty() ? p_229844_0_.first().getTicketLevel() : ChunkManager.MAX_CHUNK_DISTANCE + 1;
   }

   protected abstract boolean isChunkToRemove(long p_219371_1_);

   @Nullable
   protected abstract ChunkHolder getChunk(long pChunkPos);

   @Nullable
   protected abstract ChunkHolder updateChunkScheduling(long pChunkPos, int pNewLevel, @Nullable ChunkHolder pHolder, int pOldLevel);

   public boolean runAllUpdates(ChunkManager pChunkManager) {
      this.naturalSpawnChunkCounter.runAllUpdates();
      this.playerTicketManager.runAllUpdates();
      int i = Integer.MAX_VALUE - this.ticketTracker.runDistanceUpdates(Integer.MAX_VALUE);
      boolean flag = i != 0;
      if (flag) {
      }

      if (!this.chunksToUpdateFutures.isEmpty()) {
         this.chunksToUpdateFutures.forEach((p_219343_1_) -> {
            p_219343_1_.updateFutures(pChunkManager);
         });
         this.chunksToUpdateFutures.clear();
         return true;
      } else {
         if (!this.ticketsToRelease.isEmpty()) {
            LongIterator longiterator = this.ticketsToRelease.iterator();

            while(longiterator.hasNext()) {
               long j = longiterator.nextLong();
               if (this.getTickets(j).stream().anyMatch((p_219369_0_) -> {
                  return p_219369_0_.getType() == TicketType.PLAYER;
               })) {
                  ChunkHolder chunkholder = pChunkManager.getUpdatingChunkIfPresent(j);
                  if (chunkholder == null) {
                     throw new IllegalStateException();
                  }

                  CompletableFuture<Either<Chunk, ChunkHolder.IChunkLoadingError>> completablefuture = chunkholder.getEntityTickingChunkFuture();
                  completablefuture.thenAccept((p_219363_3_) -> {
                     this.mainThreadExecutor.execute(() -> {
                        this.ticketThrottlerReleaser.tell(ChunkTaskPriorityQueueSorter.release(() -> {
                        }, j, false));
                     });
                  });
               }
            }

            this.ticketsToRelease.clear();
         }

         return flag;
      }
   }

   private void addTicket(long pChunkPos, Ticket<?> pTicket) {
      SortedArraySet<Ticket<?>> sortedarrayset = this.getTickets(pChunkPos);
      int i = getTicketLevelAt(sortedarrayset);
      Ticket<?> ticket = sortedarrayset.addOrGet(pTicket);
      ticket.setCreatedTick(this.ticketTickCounter);
      if (pTicket.getTicketLevel() < i) {
         this.ticketTracker.update(pChunkPos, pTicket.getTicketLevel(), true);
      }

      if (pTicket.isForceTicks()) {
          SortedArraySet<Ticket<?>> tickets = forcedTickets.computeIfAbsent(pChunkPos, e -> SortedArraySet.create(4));
          tickets.addOrGet(ticket);
      }
   }

   private void removeTicket(long pChunkPos, Ticket<?> pTicket) {
      SortedArraySet<Ticket<?>> sortedarrayset = this.getTickets(pChunkPos);
      if (sortedarrayset.remove(pTicket)) {
      }

      if (sortedarrayset.isEmpty()) {
         this.tickets.remove(pChunkPos);
      }

      this.ticketTracker.update(pChunkPos, getTicketLevelAt(sortedarrayset), false);

      if (pTicket.isForceTicks()) {
          SortedArraySet<Ticket<?>> tickets = forcedTickets.get(pChunkPos);
          if (tickets != null) {
              tickets.remove(pTicket);
          }
      }
   }

   public <T> void addTicket(TicketType<T> pType, ChunkPos pPos, int pLevel, T pValue) {
      this.addTicket(pPos.toLong(), new Ticket<>(pType, pLevel, pValue));
   }

   public <T> void removeTicket(TicketType<T> pType, ChunkPos pPos, int pLevel, T pValue) {
      Ticket<T> ticket = new Ticket<>(pType, pLevel, pValue);
      this.removeTicket(pPos.toLong(), ticket);
   }

   public <T> void addRegionTicket(TicketType<T> pType, ChunkPos pPos, int pDistance, T pValue) {
      this.addTicket(pPos.toLong(), new Ticket<>(pType, 33 - pDistance, pValue));
   }

   public <T> void removeRegionTicket(TicketType<T> pType, ChunkPos pPos, int pDistance, T pValue) {
      Ticket<T> ticket = new Ticket<>(pType, 33 - pDistance, pValue);
      this.removeTicket(pPos.toLong(), ticket);
   }

   public <T> void registerTicking(TicketType<T> type, ChunkPos pos, int distance, T value) {
      this.addTicket(pos.toLong(), new Ticket<>(type, 33 - distance, value, true));
   }

   public <T> void releaseTicking(TicketType<T> type, ChunkPos pos, int distance, T value) {
      this.removeTicket(pos.toLong(), new Ticket<>(type, 33 - distance, value, true));
   }

   private SortedArraySet<Ticket<?>> getTickets(long p_229848_1_) {
      return this.tickets.computeIfAbsent(p_229848_1_, (p_229851_0_) -> {
         return SortedArraySet.create(4);
      });
   }

   protected void updateChunkForced(ChunkPos pPos, boolean pAdd) {
      Ticket<ChunkPos> ticket = new Ticket<>(TicketType.FORCED, 31, pPos);
      if (pAdd) {
         this.addTicket(pPos.toLong(), ticket);
      } else {
         this.removeTicket(pPos.toLong(), ticket);
      }

   }

   public void addPlayer(SectionPos pSectionPos, ServerPlayerEntity pPlayer) {
      long i = pSectionPos.chunk().toLong();
      this.playersPerChunk.computeIfAbsent(i, (p_219361_0_) -> {
         return new ObjectOpenHashSet();
      }).add(pPlayer);
      this.naturalSpawnChunkCounter.update(i, 0, true);
      this.playerTicketManager.update(i, 0, true);
   }

   public void removePlayer(SectionPos pSectionPos, ServerPlayerEntity pPlayer) {
      long i = pSectionPos.chunk().toLong();
      ObjectSet<ServerPlayerEntity> objectset = this.playersPerChunk.get(i);
      objectset.remove(pPlayer);
      if (objectset.isEmpty()) {
         this.playersPerChunk.remove(i);
         this.naturalSpawnChunkCounter.update(i, Integer.MAX_VALUE, false);
         this.playerTicketManager.update(i, Integer.MAX_VALUE, false);
      }

   }

   protected String getTicketDebugString(long p_225413_1_) {
      SortedArraySet<Ticket<?>> sortedarrayset = this.tickets.get(p_225413_1_);
      String s;
      if (sortedarrayset != null && !sortedarrayset.isEmpty()) {
         s = sortedarrayset.first().toString();
      } else {
         s = "no_ticket";
      }

      return s;
   }

   protected void updatePlayerTickets(int pViewDistance) {
      this.playerTicketManager.updateViewDistance(pViewDistance);
   }

   /**
    * Returns the number of chunks taken into account when calculating the mob cap
    */
   public int getNaturalSpawnChunkCount() {
      this.naturalSpawnChunkCounter.runAllUpdates();
      return this.naturalSpawnChunkCounter.chunks.size();
   }

   public boolean hasPlayersNearby(long pChunkPos) {
      this.naturalSpawnChunkCounter.runAllUpdates();
      return this.naturalSpawnChunkCounter.chunks.containsKey(pChunkPos);
   }

   public String getDebugStatus() {
      return this.ticketThrottler.getDebugStatus();
   }

   public boolean shouldForceTicks(long chunkPos) {
       SortedArraySet<Ticket<?>> tickets = forcedTickets.get(chunkPos);
       return tickets != null && !tickets.isEmpty();
   }

   class ChunkTicketTracker extends ChunkDistanceGraph {
      public ChunkTicketTracker() {
         super(ChunkManager.MAX_CHUNK_DISTANCE + 2, 16, 256);
      }

      protected int getLevelFromSource(long pPos) {
         SortedArraySet<Ticket<?>> sortedarrayset = TicketManager.this.tickets.get(pPos);
         if (sortedarrayset == null) {
            return Integer.MAX_VALUE;
         } else {
            return sortedarrayset.isEmpty() ? Integer.MAX_VALUE : sortedarrayset.first().getTicketLevel();
         }
      }

      protected int getLevel(long pSectionPos) {
         if (!TicketManager.this.isChunkToRemove(pSectionPos)) {
            ChunkHolder chunkholder = TicketManager.this.getChunk(pSectionPos);
            if (chunkholder != null) {
               return chunkholder.getTicketLevel();
            }
         }

         return ChunkManager.MAX_CHUNK_DISTANCE + 1;
      }

      protected void setLevel(long pSectionPos, int pLevel) {
         ChunkHolder chunkholder = TicketManager.this.getChunk(pSectionPos);
         int i = chunkholder == null ? ChunkManager.MAX_CHUNK_DISTANCE + 1 : chunkholder.getTicketLevel();
         if (i != pLevel) {
            chunkholder = TicketManager.this.updateChunkScheduling(pSectionPos, pLevel, chunkholder, i);
            if (chunkholder != null) {
               TicketManager.this.chunksToUpdateFutures.add(chunkholder);
            }

         }
      }

      public int runDistanceUpdates(int p_215493_1_) {
         return this.runUpdates(p_215493_1_);
      }
   }

   class PlayerChunkTracker extends ChunkDistanceGraph {
      /** Chunks that are at most {@link #range} chunks away from the closest player. */
      protected final Long2ByteMap chunks = new Long2ByteOpenHashMap();
      protected final int maxDistance;

      protected PlayerChunkTracker(int p_i50684_2_) {
         super(p_i50684_2_ + 2, 16, 256);
         this.maxDistance = p_i50684_2_;
         this.chunks.defaultReturnValue((byte)(p_i50684_2_ + 2));
      }

      protected int getLevel(long pSectionPos) {
         return this.chunks.get(pSectionPos);
      }

      protected void setLevel(long pSectionPos, int pLevel) {
         byte b0;
         if (pLevel > this.maxDistance) {
            b0 = this.chunks.remove(pSectionPos);
         } else {
            b0 = this.chunks.put(pSectionPos, (byte)pLevel);
         }

         this.onLevelChange(pSectionPos, b0, pLevel);
      }

      /**
       * Called after {@link PlayerChunkTracker#setLevel(long, int)} puts/removes chunk into/from {@link
       * #chunksInRange}.
       * @param pOldLevel Previous level of the chunk if it was smaller than {@link #range}, {@code range + 2}
       * otherwise.
       */
      protected void onLevelChange(long pChunkPos, int pOldLevel, int pNewLevel) {
      }

      protected int getLevelFromSource(long pPos) {
         return this.havePlayer(pPos) ? 0 : Integer.MAX_VALUE;
      }

      private boolean havePlayer(long pChunkPos) {
         ObjectSet<ServerPlayerEntity> objectset = TicketManager.this.playersPerChunk.get(pChunkPos);
         return objectset != null && !objectset.isEmpty();
      }

      public void runAllUpdates() {
         this.runUpdates(Integer.MAX_VALUE);
      }
   }

   class PlayerTicketTracker extends TicketManager.PlayerChunkTracker {
      private int viewDistance;
      private final Long2IntMap queueLevels = Long2IntMaps.synchronize(new Long2IntOpenHashMap());
      private final LongSet toUpdate = new LongOpenHashSet();

      protected PlayerTicketTracker(int p_i50682_2_) {
         super(p_i50682_2_);
         this.viewDistance = 0;
         this.queueLevels.defaultReturnValue(p_i50682_2_ + 2);
      }

      /**
       * Called after {@link PlayerChunkTracker#setLevel(long, int)} puts/removes chunk into/from {@link
       * #chunksInRange}.
       * @param pOldLevel Previous level of the chunk if it was smaller than {@link #range}, {@code range + 2}
       * otherwise.
       */
      protected void onLevelChange(long pChunkPos, int pOldLevel, int pNewLevel) {
         this.toUpdate.add(pChunkPos);
      }

      public void updateViewDistance(int pViewDistance) {
         for(it.unimi.dsi.fastutil.longs.Long2ByteMap.Entry entry : this.chunks.long2ByteEntrySet()) {
            byte b0 = entry.getByteValue();
            long i = entry.getLongKey();
            this.onLevelChange(i, b0, this.haveTicketFor(b0), b0 <= pViewDistance - 2);
         }

         this.viewDistance = pViewDistance;
      }

      private void onLevelChange(long p_215504_1_, int p_215504_3_, boolean p_215504_4_, boolean p_215504_5_) {
         if (p_215504_4_ != p_215504_5_) {
            Ticket<?> ticket = new Ticket<>(TicketType.PLAYER, TicketManager.PLAYER_TICKET_LEVEL, new ChunkPos(p_215504_1_));
            if (p_215504_5_) {
               TicketManager.this.ticketThrottlerInput.tell(ChunkTaskPriorityQueueSorter.message(() -> {
                  TicketManager.this.mainThreadExecutor.execute(() -> {
                     if (this.haveTicketFor(this.getLevel(p_215504_1_))) {
                        TicketManager.this.addTicket(p_215504_1_, ticket);
                        TicketManager.this.ticketsToRelease.add(p_215504_1_);
                     } else {
                        TicketManager.this.ticketThrottlerReleaser.tell(ChunkTaskPriorityQueueSorter.release(() -> {
                        }, p_215504_1_, false));
                     }

                  });
               }, p_215504_1_, () -> {
                  return p_215504_3_;
               }));
            } else {
               TicketManager.this.ticketThrottlerReleaser.tell(ChunkTaskPriorityQueueSorter.release(() -> {
                  TicketManager.this.mainThreadExecutor.execute(() -> {
                     TicketManager.this.removeTicket(p_215504_1_, ticket);
                  });
               }, p_215504_1_, true));
            }
         }

      }

      public void runAllUpdates() {
         super.runAllUpdates();
         if (!this.toUpdate.isEmpty()) {
            LongIterator longiterator = this.toUpdate.iterator();

            while(longiterator.hasNext()) {
               long i = longiterator.nextLong();
               int j = this.queueLevels.get(i);
               int k = this.getLevel(i);
               if (j != k) {
                  TicketManager.this.ticketThrottler.onLevelChange(new ChunkPos(i), () -> {
                     return this.queueLevels.get(i);
                  }, k, (p_215506_3_) -> {
                     if (p_215506_3_ >= this.queueLevels.defaultReturnValue()) {
                        this.queueLevels.remove(i);
                     } else {
                        this.queueLevels.put(i, p_215506_3_);
                     }

                  });
                  this.onLevelChange(i, k, this.haveTicketFor(j), this.haveTicketFor(k));
               }
            }

            this.toUpdate.clear();
         }

      }

      private boolean haveTicketFor(int p_215505_1_) {
         return p_215505_1_ <= this.viewDistance - 2;
      }
   }
}
