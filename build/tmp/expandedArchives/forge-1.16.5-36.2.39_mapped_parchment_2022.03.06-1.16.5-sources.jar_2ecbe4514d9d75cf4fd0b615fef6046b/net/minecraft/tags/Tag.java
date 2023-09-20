package net.minecraft.tags;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;

public class Tag<T> implements ITag<T> {
   private final ImmutableList<T> valuesList;
   private final Set<T> values;
   @VisibleForTesting
   protected final Class<?> closestCommonSuperType;

   protected Tag(Set<T> p_i241226_1_, Class<?> p_i241226_2_) {
      this.closestCommonSuperType = p_i241226_2_;
      this.values = p_i241226_1_;
      this.valuesList = ImmutableList.copyOf(p_i241226_1_);
   }

   public static <T> Tag<T> empty() {
      return new Tag<>(ImmutableSet.of(), Void.class);
   }

   public static <T> Tag<T> create(Set<T> pContents) {
      return new Tag<>(pContents, findCommonSuperClass(pContents));
   }

   public boolean contains(T pElement) {
      return this.closestCommonSuperType.isInstance(pElement) && this.values.contains(pElement);
   }

   public List<T> getValues() {
      return this.valuesList;
   }

   private static <T> Class<?> findCommonSuperClass(Set<T> pContents) {
      if (pContents.isEmpty()) {
         return Void.class;
      } else {
         Class<?> oclass = null;

         for(T t : pContents) {
            if (oclass == null) {
               oclass = t.getClass();
            } else {
               oclass = findClosestAncestor(oclass, t.getClass());
            }
         }

         return oclass;
      }
   }

   private static Class<?> findClosestAncestor(Class<?> pInput, Class<?> pComparison) {
      while(!pInput.isAssignableFrom(pComparison)) {
         pInput = pInput.getSuperclass();
      }

      return pInput;
   }
}