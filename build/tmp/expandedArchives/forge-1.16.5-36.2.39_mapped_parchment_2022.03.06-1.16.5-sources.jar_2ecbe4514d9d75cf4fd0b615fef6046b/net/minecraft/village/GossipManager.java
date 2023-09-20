package net.minecraft.village;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.UUIDCodec;
import net.minecraft.util.Util;

public class GossipManager {
   private final Map<UUID, GossipManager.Gossips> gossips = Maps.newHashMap();

   public void decay() {
      Iterator<GossipManager.Gossips> iterator = this.gossips.values().iterator();

      while(iterator.hasNext()) {
         GossipManager.Gossips gossipmanager$gossips = iterator.next();
         gossipmanager$gossips.decay();
         if (gossipmanager$gossips.isEmpty()) {
            iterator.remove();
         }
      }

   }

   private Stream<GossipManager.GossipEntry> unpack() {
      return this.gossips.entrySet().stream().flatMap((p_220917_0_) -> {
         return p_220917_0_.getValue().unpack(p_220917_0_.getKey());
      });
   }

   private Collection<GossipManager.GossipEntry> selectGossipsForTransfer(Random pRand, int pGossipAmount) {
      List<GossipManager.GossipEntry> list = this.unpack().collect(Collectors.toList());
      if (list.isEmpty()) {
         return Collections.emptyList();
      } else {
         int[] aint = new int[list.size()];
         int i = 0;

         for(int j = 0; j < list.size(); ++j) {
            GossipManager.GossipEntry gossipmanager$gossipentry = list.get(j);
            i += Math.abs(gossipmanager$gossipentry.weightedValue());
            aint[j] = i - 1;
         }

         Set<GossipManager.GossipEntry> set = Sets.newIdentityHashSet();

         for(int i1 = 0; i1 < pGossipAmount; ++i1) {
            int k = pRand.nextInt(i);
            int l = Arrays.binarySearch(aint, k);
            set.add(list.get(l < 0 ? -l - 1 : l));
         }

         return set;
      }
   }

   private GossipManager.Gossips getOrCreate(UUID pIdentifier) {
      return this.gossips.computeIfAbsent(pIdentifier, (p_220922_0_) -> {
         return new GossipManager.Gossips();
      });
   }

   public void transferFrom(GossipManager pGossip, Random pRand, int pGossipAmount) {
      Collection<GossipManager.GossipEntry> collection = pGossip.selectGossipsForTransfer(pRand, pGossipAmount);
      collection.forEach((p_220923_1_) -> {
         int i = p_220923_1_.value - p_220923_1_.type.decayPerTransfer;
         if (i >= 2) {
            this.getOrCreate(p_220923_1_.target).entries.mergeInt(p_220923_1_.type, i, GossipManager::mergeValuesForTransfer);
         }

      });
   }

   public int getReputation(UUID pIdentifier, Predicate<GossipType> pGossip) {
      GossipManager.Gossips gossipmanager$gossips = this.gossips.get(pIdentifier);
      return gossipmanager$gossips != null ? gossipmanager$gossips.weightedValue(pGossip) : 0;
   }

   public void add(UUID pIdentifier, GossipType pGossipType, int pGossipValue) {
      GossipManager.Gossips gossipmanager$gossips = this.getOrCreate(pIdentifier);
      gossipmanager$gossips.entries.mergeInt(pGossipType, pGossipValue, (p_220915_2_, p_220915_3_) -> {
         return this.mergeValuesForAddition(pGossipType, p_220915_2_, p_220915_3_);
      });
      gossipmanager$gossips.makeSureValueIsntTooLowOrTooHigh(pGossipType);
      if (gossipmanager$gossips.isEmpty()) {
         this.gossips.remove(pIdentifier);
      }

   }

   public <T> Dynamic<T> store(DynamicOps<T> pDynamic) {
      return new Dynamic<>(pDynamic, pDynamic.createList(this.unpack().map((p_234059_1_) -> {
         return p_234059_1_.store(pDynamic);
      }).map(Dynamic::getValue)));
   }

   public void update(Dynamic<?> pDynamic) {
      pDynamic.asStream().map(GossipManager.GossipEntry::load).flatMap((p_234056_0_) -> {
         return Util.toStream(p_234056_0_.result());
      }).forEach((p_234055_1_) -> {
         this.getOrCreate(p_234055_1_.target).entries.put(p_234055_1_.type, p_234055_1_.value);
      });
   }

   /**
    * Returns the greater of two int values
    */
   private static int mergeValuesForTransfer(int p_220924_0_, int p_220924_1_) {
      return Math.max(p_220924_0_, p_220924_1_);
   }

   private int mergeValuesForAddition(GossipType pGossipType, int pExisting, int pAdditive) {
      int i = pExisting + pAdditive;
      return i > pGossipType.max ? Math.max(pGossipType.max, pExisting) : i;
   }

   static class GossipEntry {
      public final UUID target;
      public final GossipType type;
      public final int value;

      public GossipEntry(UUID p_i50613_1_, GossipType p_i50613_2_, int p_i50613_3_) {
         this.target = p_i50613_1_;
         this.type = p_i50613_2_;
         this.value = p_i50613_3_;
      }

      public int weightedValue() {
         return this.value * this.type.weight;
      }

      public String toString() {
         return "GossipEntry{target=" + this.target + ", type=" + this.type + ", value=" + this.value + '}';
      }

      public <T> Dynamic<T> store(DynamicOps<T> pDynamic) {
         return new Dynamic<>(pDynamic, pDynamic.createMap(ImmutableMap.of(pDynamic.createString("Target"), UUIDCodec.CODEC.encodeStart(pDynamic, this.target).result().orElseThrow(RuntimeException::new), pDynamic.createString("Type"), pDynamic.createString(this.type.id), pDynamic.createString("Value"), pDynamic.createInt(this.value))));
      }

      public static DataResult<GossipManager.GossipEntry> load(Dynamic<?> pDynamic) {
         return DataResult.unbox(DataResult.instance().group(pDynamic.get("Target").read(UUIDCodec.CODEC), pDynamic.get("Type").asString().map(GossipType::byId), pDynamic.get("Value").asNumber().map(Number::intValue)).apply(DataResult.instance(), GossipManager.GossipEntry::new));
      }
   }

   static class Gossips {
      private final Object2IntMap<GossipType> entries = new Object2IntOpenHashMap<>();

      private Gossips() {
      }

      public int weightedValue(Predicate<GossipType> pGossipType) {
         return this.entries.object2IntEntrySet().stream().filter((p_220898_1_) -> {
            return pGossipType.test(p_220898_1_.getKey());
         }).mapToInt((p_220894_0_) -> {
            return p_220894_0_.getIntValue() * (p_220894_0_.getKey()).weight;
         }).sum();
      }

      public Stream<GossipManager.GossipEntry> unpack(UUID pIdentifier) {
         return this.entries.object2IntEntrySet().stream().map((p_220897_1_) -> {
            return new GossipManager.GossipEntry(pIdentifier, p_220897_1_.getKey(), p_220897_1_.getIntValue());
         });
      }

      public void decay() {
         ObjectIterator<Entry<GossipType>> objectiterator = this.entries.object2IntEntrySet().iterator();

         while(objectiterator.hasNext()) {
            Entry<GossipType> entry = objectiterator.next();
            int i = entry.getIntValue() - (entry.getKey()).decayPerDay;
            if (i < 2) {
               objectiterator.remove();
            } else {
               entry.setValue(i);
            }
         }

      }

      public boolean isEmpty() {
         return this.entries.isEmpty();
      }

      public void makeSureValueIsntTooLowOrTooHigh(GossipType pGossipType) {
         int i = this.entries.getInt(pGossipType);
         if (i > pGossipType.max) {
            this.entries.put(pGossipType, pGossipType.max);
         }

         if (i < 2) {
            this.remove(pGossipType);
         }

      }

      public void remove(GossipType pGossipType) {
         this.entries.removeInt(pGossipType);
      }
   }
}