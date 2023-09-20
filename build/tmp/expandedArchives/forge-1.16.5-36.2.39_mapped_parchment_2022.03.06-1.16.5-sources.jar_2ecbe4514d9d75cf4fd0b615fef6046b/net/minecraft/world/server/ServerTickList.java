package net.minecraft.world.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.ITickList;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.TickPriority;

public class ServerTickList<T> implements ITickList<T> {
   protected final Predicate<T> ignore;
   private final Function<T, ResourceLocation> toId;
   private final Set<NextTickListEntry<T>> tickNextTickSet = Sets.newHashSet();
   private final TreeSet<NextTickListEntry<T>> tickNextTickList = Sets.newTreeSet(NextTickListEntry.createTimeComparator());
   private final ServerWorld level;
   private final Queue<NextTickListEntry<T>> currentlyTicking = Queues.newArrayDeque();
   /** Entries from {@link #pendingTickListEntriesThisTick} that have already run this tick. */
   private final List<NextTickListEntry<T>> alreadyTicked = Lists.newArrayList();
   private final Consumer<NextTickListEntry<T>> ticker;

   public ServerTickList(ServerWorld pLevel, Predicate<T> pIgnore, Function<T, ResourceLocation> pToId, Consumer<NextTickListEntry<T>> pTicker) {
      this.ignore = pIgnore;
      this.toId = pToId;
      this.level = pLevel;
      this.ticker = pTicker;
   }

   public void tick() {
      int i = this.tickNextTickList.size();
      if (i != this.tickNextTickSet.size()) {
         throw new IllegalStateException("TickNextTick list out of synch");
      } else {
         if (i > 65536) {
            i = 65536;
         }

         ServerChunkProvider serverchunkprovider = this.level.getChunkSource();
         Iterator<NextTickListEntry<T>> iterator = this.tickNextTickList.iterator();
         this.level.getProfiler().push("cleaning");

         while(i > 0 && iterator.hasNext()) {
            NextTickListEntry<T> nextticklistentry = iterator.next();
            if (nextticklistentry.triggerTick > this.level.getGameTime()) {
               break;
            }

            if (serverchunkprovider.isTickingChunk(nextticklistentry.pos)) {
               iterator.remove();
               this.tickNextTickSet.remove(nextticklistentry);
               this.currentlyTicking.add(nextticklistentry);
               --i;
            }
         }

         this.level.getProfiler().popPush("ticking");

         NextTickListEntry<T> nextticklistentry1;
         while((nextticklistentry1 = this.currentlyTicking.poll()) != null) {
            if (serverchunkprovider.isTickingChunk(nextticklistentry1.pos)) {
               try {
                  this.alreadyTicked.add(nextticklistentry1);
                  this.ticker.accept(nextticklistentry1);
               } catch (Throwable throwable) {
                  CrashReport crashreport = CrashReport.forThrowable(throwable, "Exception while ticking");
                  CrashReportCategory crashreportcategory = crashreport.addCategory("Block being ticked");
                  CrashReportCategory.populateBlockDetails(crashreportcategory, nextticklistentry1.pos, (BlockState)null);
                  throw new ReportedException(crashreport);
               }
            } else {
               this.scheduleTick(nextticklistentry1.pos, nextticklistentry1.getType(), 0);
            }
         }

         this.level.getProfiler().pop();
         this.alreadyTicked.clear();
         this.currentlyTicking.clear();
      }
   }

   /**
    * Checks if this position/item is scheduled to be updated this tick
    */
   public boolean willTickThisTick(BlockPos pPos, T pObj) {
      return this.currentlyTicking.contains(new NextTickListEntry(pPos, pObj));
   }

   public List<NextTickListEntry<T>> fetchTicksInChunk(ChunkPos pPos, boolean pRemove, boolean pSkipCompleted) {
      int i = (pPos.x << 4) - 2;
      int j = i + 16 + 2;
      int k = (pPos.z << 4) - 2;
      int l = k + 16 + 2;
      return this.fetchTicksInArea(new MutableBoundingBox(i, 0, k, j, 256, l), pRemove, pSkipCompleted);
   }

   public List<NextTickListEntry<T>> fetchTicksInArea(MutableBoundingBox pArea, boolean pRemove, boolean pSkipCompleted) {
      List<NextTickListEntry<T>> list = this.fetchTicksInArea((List<NextTickListEntry<T>>)null, this.tickNextTickList, pArea, pRemove);
      if (pRemove && list != null) {
         this.tickNextTickSet.removeAll(list);
      }

      list = this.fetchTicksInArea(list, this.currentlyTicking, pArea, pRemove);
      if (!pSkipCompleted) {
         list = this.fetchTicksInArea(list, this.alreadyTicked, pArea, pRemove);
      }

      return list == null ? Collections.emptyList() : list;
   }

   @Nullable
   private List<NextTickListEntry<T>> fetchTicksInArea(@Nullable List<NextTickListEntry<T>> pResult, Collection<NextTickListEntry<T>> pEntries, MutableBoundingBox pBb, boolean pRemove) {
      Iterator<NextTickListEntry<T>> iterator = pEntries.iterator();

      while(iterator.hasNext()) {
         NextTickListEntry<T> nextticklistentry = iterator.next();
         BlockPos blockpos = nextticklistentry.pos;
         if (blockpos.getX() >= pBb.x0 && blockpos.getX() < pBb.x1 && blockpos.getZ() >= pBb.z0 && blockpos.getZ() < pBb.z1) {
            if (pRemove) {
               iterator.remove();
            }

            if (pResult == null) {
               pResult = Lists.newArrayList();
            }

            pResult.add(nextticklistentry);
         }
      }

      return pResult;
   }

   public void copy(MutableBoundingBox pArea, BlockPos pOffset) {
      for(NextTickListEntry<T> nextticklistentry : this.fetchTicksInArea(pArea, false, false)) {
         if (pArea.isInside(nextticklistentry.pos)) {
            BlockPos blockpos = nextticklistentry.pos.offset(pOffset);
            T t = nextticklistentry.getType();
            this.addTickData(new NextTickListEntry<>(blockpos, t, nextticklistentry.triggerTick, nextticklistentry.priority));
         }
      }

   }

   public ListNBT save(ChunkPos pPos) {
      List<NextTickListEntry<T>> list = this.fetchTicksInChunk(pPos, false, true);
      return saveTickList(this.toId, list, this.level.getGameTime());
   }

   private static <T> ListNBT saveTickList(Function<T, ResourceLocation> pTargetNameFunction, Iterable<NextTickListEntry<T>> pTickEntries, long pTime) {
      ListNBT listnbt = new ListNBT();

      for(NextTickListEntry<T> nextticklistentry : pTickEntries) {
         CompoundNBT compoundnbt = new CompoundNBT();
         compoundnbt.putString("i", pTargetNameFunction.apply(nextticklistentry.getType()).toString());
         compoundnbt.putInt("x", nextticklistentry.pos.getX());
         compoundnbt.putInt("y", nextticklistentry.pos.getY());
         compoundnbt.putInt("z", nextticklistentry.pos.getZ());
         compoundnbt.putInt("t", (int)(nextticklistentry.triggerTick - pTime));
         compoundnbt.putInt("p", nextticklistentry.priority.getValue());
         listnbt.add(compoundnbt);
      }

      return listnbt;
   }

   public boolean hasScheduledTick(BlockPos pPos, T pItem) {
      return this.tickNextTickSet.contains(new NextTickListEntry(pPos, pItem));
   }

   public void scheduleTick(BlockPos pPos, T pItem, int pScheduledTime, TickPriority pPriority) {
      if (!this.ignore.test(pItem)) {
         this.addTickData(new NextTickListEntry<>(pPos, pItem, (long)pScheduledTime + this.level.getGameTime(), pPriority));
      }

   }

   private void addTickData(NextTickListEntry<T> pEntry) {
      if (!this.tickNextTickSet.contains(pEntry)) {
         this.tickNextTickSet.add(pEntry);
         this.tickNextTickList.add(pEntry);
      }

   }

   public int size() {
      return this.tickNextTickSet.size();
   }
}