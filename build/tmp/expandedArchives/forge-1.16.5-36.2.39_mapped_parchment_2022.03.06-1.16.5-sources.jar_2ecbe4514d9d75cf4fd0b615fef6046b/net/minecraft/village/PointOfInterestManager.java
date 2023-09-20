package net.minecraft.village;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.util.SectionDistanceGraph;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.DefaultTypeReferences;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.storage.RegionSectionCache;

public class PointOfInterestManager extends RegionSectionCache<PointOfInterestData> {
   private final PointOfInterestManager.DistanceGraph distanceTracker;
   private final LongSet loadedChunks = new LongOpenHashSet();

   public PointOfInterestManager(File p_i231554_1_, DataFixer p_i231554_2_, boolean p_i231554_3_) {
      super(p_i231554_1_, PointOfInterestData::codec, PointOfInterestData::new, p_i231554_2_, DefaultTypeReferences.POI_CHUNK, p_i231554_3_);
      this.distanceTracker = new PointOfInterestManager.DistanceGraph();
   }

   public void add(BlockPos pPos, PointOfInterestType pPoiType) {
      this.getOrCreate(SectionPos.of(pPos).asLong()).add(pPos, pPoiType);
   }

   public void remove(BlockPos pPos) {
      this.getOrCreate(SectionPos.of(pPos).asLong()).remove(pPos);
   }

   public long getCountInRange(Predicate<PointOfInterestType> p_219145_1_, BlockPos pPos, int pDistance, PointOfInterestManager.Status pStatus) {
      return this.getInRange(p_219145_1_, pPos, pDistance, pStatus).count();
   }

   public boolean existsAtPosition(PointOfInterestType pType, BlockPos pPos) {
      Optional<PointOfInterestType> optional = this.getOrCreate(SectionPos.of(pPos).asLong()).getType(pPos);
      return optional.isPresent() && optional.get().equals(pType);
   }

   public Stream<PointOfInterest> getInSquare(Predicate<PointOfInterestType> pTypePredicate, BlockPos pPos, int pDistance, PointOfInterestManager.Status pStatus) {
      int i = Math.floorDiv(pDistance, 16) + 1;
      return ChunkPos.rangeClosed(new ChunkPos(pPos), i).flatMap((p_226350_3_) -> {
         return this.getInChunk(pTypePredicate, p_226350_3_, pStatus);
      }).filter((p_242322_2_) -> {
         BlockPos blockpos = p_242322_2_.getPos();
         return Math.abs(blockpos.getX() - pPos.getX()) <= pDistance && Math.abs(blockpos.getZ() - pPos.getZ()) <= pDistance;
      });
   }

   public Stream<PointOfInterest> getInRange(Predicate<PointOfInterestType> p_219146_1_, BlockPos p_219146_2_, int p_219146_3_, PointOfInterestManager.Status p_219146_4_) {
      int i = p_219146_3_ * p_219146_3_;
      return this.getInSquare(p_219146_1_, p_219146_2_, p_219146_3_, p_219146_4_).filter((p_226349_2_) -> {
         return p_226349_2_.getPos().distSqr(p_219146_2_) <= (double)i;
      });
   }

   public Stream<PointOfInterest> getInChunk(Predicate<PointOfInterestType> p_219137_1_, ChunkPos pPosChunk, PointOfInterestManager.Status pStatus) {
      return IntStream.range(0, 16).boxed().map((p_219149_2_) -> {
         return this.getOrLoad(SectionPos.of(pPosChunk, p_219149_2_).asLong());
      }).filter(Optional::isPresent).flatMap((p_241393_2_) -> {
         return p_241393_2_.get().getRecords(p_219137_1_, pStatus);
      });
   }

   public Stream<BlockPos> findAll(Predicate<PointOfInterestType> pTypePredicate, Predicate<BlockPos> pPosPredicate, BlockPos pPos, int pDistance, PointOfInterestManager.Status pStatus) {
      return this.getInRange(pTypePredicate, pPos, pDistance, pStatus).map(PointOfInterest::getPos).filter(pPosPredicate);
   }

   public Stream<BlockPos> findAllClosestFirst(Predicate<PointOfInterestType> p_242324_1_, Predicate<BlockPos> p_242324_2_, BlockPos p_242324_3_, int p_242324_4_, PointOfInterestManager.Status p_242324_5_) {
      return this.findAll(p_242324_1_, p_242324_2_, p_242324_3_, p_242324_4_, p_242324_5_).sorted(Comparator.comparingDouble((p_242323_1_) -> {
         return p_242323_1_.distSqr(p_242324_3_);
      }));
   }

   public Optional<BlockPos> find(Predicate<PointOfInterestType> pTypePredicate, Predicate<BlockPos> pPosPredicate, BlockPos pPos, int pDistance, PointOfInterestManager.Status pStatus) {
      return this.findAll(pTypePredicate, pPosPredicate, pPos, pDistance, pStatus).findFirst();
   }

   public Optional<BlockPos> findClosest(Predicate<PointOfInterestType> p_234148_1_, BlockPos p_234148_2_, int p_234148_3_, PointOfInterestManager.Status p_234148_4_) {
      return this.getInRange(p_234148_1_, p_234148_2_, p_234148_3_, p_234148_4_).map(PointOfInterest::getPos).min(Comparator.comparingDouble((p_219160_1_) -> {
         return p_219160_1_.distSqr(p_234148_2_);
      }));
   }

   public Optional<BlockPos> take(Predicate<PointOfInterestType> pTypePredicate, Predicate<BlockPos> pPosPredicate, BlockPos pPos, int pDistance) {
      return this.getInRange(pTypePredicate, pPos, pDistance, PointOfInterestManager.Status.HAS_SPACE).filter((p_219129_1_) -> {
         return pPosPredicate.test(p_219129_1_.getPos());
      }).findFirst().map((p_219152_0_) -> {
         p_219152_0_.acquireTicket();
         return p_219152_0_.getPos();
      });
   }

   public Optional<BlockPos> getRandom(Predicate<PointOfInterestType> pTypePredicate, Predicate<BlockPos> pPosPredicate, PointOfInterestManager.Status pStatus, BlockPos pPos, int pDistance, Random pRand) {
      List<PointOfInterest> list = this.getInRange(pTypePredicate, pPos, pDistance, pStatus).collect(Collectors.toList());
      Collections.shuffle(list, pRand);
      return list.stream().filter((p_234143_1_) -> {
         return pPosPredicate.test(p_234143_1_.getPos());
      }).findFirst().map(PointOfInterest::getPos);
   }

   public boolean release(BlockPos pPos) {
      return this.getOrCreate(SectionPos.of(pPos).asLong()).release(pPos);
   }

   public boolean exists(BlockPos pPos, Predicate<PointOfInterestType> p_219138_2_) {
      return this.getOrLoad(SectionPos.of(pPos).asLong()).map((p_234141_2_) -> {
         return p_234141_2_.exists(pPos, p_219138_2_);
      }).orElse(false);
   }

   public Optional<PointOfInterestType> getType(BlockPos pPos) {
      PointOfInterestData pointofinterestdata = this.getOrCreate(SectionPos.of(pPos).asLong());
      return pointofinterestdata.getType(pPos);
   }

   public int sectionsToVillage(SectionPos pSectionPos) {
      this.distanceTracker.runAllUpdates();
      return this.distanceTracker.getLevel(pSectionPos.asLong());
   }

   private boolean isVillageCenter(long p_219154_1_) {
      Optional<PointOfInterestData> optional = this.get(p_219154_1_);
      return optional == null ? false : optional.map((p_234134_0_) -> {
         return p_234134_0_.getRecords(PointOfInterestType.ALL, PointOfInterestManager.Status.IS_OCCUPIED).count() > 0L;
      }).orElse(false);
   }

   public void tick(BooleanSupplier p_219115_1_) {
      super.tick(p_219115_1_);
      this.distanceTracker.runAllUpdates();
   }

   protected void setDirty(long pSectionPos) {
      super.setDirty(pSectionPos);
      this.distanceTracker.update(pSectionPos, this.distanceTracker.getLevelFromSource(pSectionPos), false);
   }

   protected void onSectionLoad(long p_219111_1_) {
      this.distanceTracker.update(p_219111_1_, this.distanceTracker.getLevelFromSource(p_219111_1_), false);
   }

   public void checkConsistencyWithBlocks(ChunkPos pPos, ChunkSection pSection) {
      SectionPos sectionpos = SectionPos.of(pPos, pSection.bottomBlockY() >> 4);
      Util.ifElse(this.getOrLoad(sectionpos.asLong()), (p_234138_3_) -> {
         p_234138_3_.refresh((p_234145_3_) -> {
            if (mayHavePoi(pSection)) {
               this.updateFromSection(pSection, sectionpos, p_234145_3_);
            }

         });
      }, () -> {
         if (mayHavePoi(pSection)) {
            PointOfInterestData pointofinterestdata = this.getOrCreate(sectionpos.asLong());
            this.updateFromSection(pSection, sectionpos, pointofinterestdata::add);
         }

      });
   }

   private static boolean mayHavePoi(ChunkSection pSection) {
      return pSection.maybeHas(PointOfInterestType.ALL_STATES::contains);
   }

   private void updateFromSection(ChunkSection pSection, SectionPos pSectionPos, BiConsumer<BlockPos, PointOfInterestType> pPosToTypeConsumer) {
      pSectionPos.blocksInside().forEach((p_234139_2_) -> {
         BlockState blockstate = pSection.getBlockState(SectionPos.sectionRelative(p_234139_2_.getX()), SectionPos.sectionRelative(p_234139_2_.getY()), SectionPos.sectionRelative(p_234139_2_.getZ()));
         PointOfInterestType.forState(blockstate).ifPresent((p_234142_2_) -> {
            pPosToTypeConsumer.accept(p_234139_2_, p_234142_2_);
         });
      });
   }

   public void ensureLoadedAndValid(IWorldReader pLevelReader, BlockPos pPos, int pCoordinateOffset) {
      SectionPos.aroundChunk(new ChunkPos(pPos), Math.floorDiv(pCoordinateOffset, 16)).map((p_234147_1_) -> {
         return Pair.of(p_234147_1_, this.getOrLoad(p_234147_1_.asLong()));
      }).filter((p_234146_0_) -> {
         return !p_234146_0_.getSecond().map(PointOfInterestData::isValid).orElse(false);
      }).map((p_234140_0_) -> {
         return p_234140_0_.getFirst().chunk();
      }).filter((p_234144_1_) -> {
         return this.loadedChunks.add(p_234144_1_.toLong());
      }).forEach((p_234136_1_) -> {
         pLevelReader.getChunk(p_234136_1_.x, p_234136_1_.z, ChunkStatus.EMPTY);
      });
   }

   final class DistanceGraph extends SectionDistanceGraph {
      private final Long2ByteMap levels = new Long2ByteOpenHashMap();

      protected DistanceGraph() {
         super(7, 16, 256);
         this.levels.defaultReturnValue((byte)7);
      }

      protected int getLevelFromSource(long pPos) {
         return PointOfInterestManager.this.isVillageCenter(pPos) ? 0 : 7;
      }

      protected int getLevel(long pSectionPos) {
         return this.levels.get(pSectionPos);
      }

      protected void setLevel(long pSectionPos, int pLevel) {
         if (pLevel > 6) {
            this.levels.remove(pSectionPos);
         } else {
            this.levels.put(pSectionPos, (byte)pLevel);
         }

      }

      public void runAllUpdates() {
         super.runUpdates(Integer.MAX_VALUE);
      }
   }

   public static enum Status {
      HAS_SPACE(PointOfInterest::hasSpace),
      IS_OCCUPIED(PointOfInterest::isOccupied),
      ANY((p_221036_0_) -> {
         return true;
      });

      private final Predicate<? super PointOfInterest> test;

      private Status(Predicate<? super PointOfInterest> p_i50192_3_) {
         this.test = p_i50192_3_;
      }

      public Predicate<? super PointOfInterest> getTest() {
         return this.test;
      }
   }
}