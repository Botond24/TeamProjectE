package net.minecraft.util;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;

public class ObjectIntIdentityMap<T> implements IObjectIntIterable<T> {
   protected int nextId;
   protected final IdentityHashMap<T, Integer> tToId;
   protected final List<T> idToT;

   public ObjectIntIdentityMap() {
      this(512);
   }

   public ObjectIntIdentityMap(int pExpectedSize) {
      this.idToT = Lists.newArrayListWithExpectedSize(pExpectedSize);
      this.tToId = new IdentityHashMap<>(pExpectedSize);
   }

   public void addMapping(T pKey, int pValue) {
      this.tToId.put(pKey, pValue);

      while(this.idToT.size() <= pValue) {
         this.idToT.add((T)null);
      }

      this.idToT.set(pValue, pKey);
      if (this.nextId <= pValue) {
         this.nextId = pValue + 1;
      }

   }

   public void add(T pKey) {
      this.addMapping(pKey, this.nextId);
   }

   /**
    * Gets the integer ID we use to identify the given object.
    */
   public int getId(T pValue) {
      Integer integer = this.tToId.get(pValue);
      return integer == null ? -1 : integer;
   }

   @Nullable
   public final T byId(int pValue) {
      return (T)(pValue >= 0 && pValue < this.idToT.size() ? this.idToT.get(pValue) : null);
   }

   public Iterator<T> iterator() {
      return Iterators.filter(this.idToT.iterator(), Predicates.notNull());
   }

   public int size() {
      return this.tToId.size();
   }
}