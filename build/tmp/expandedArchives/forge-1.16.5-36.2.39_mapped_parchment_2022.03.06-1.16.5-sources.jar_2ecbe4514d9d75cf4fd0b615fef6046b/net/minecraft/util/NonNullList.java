package net.minecraft.util;

import com.google.common.collect.Lists;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.Validate;

public class NonNullList<E> extends AbstractList<E> {
   private final List<E> list;
   private final E defaultValue;

   public static <E> NonNullList<E> create() {
      return new NonNullList<>();
   }

   /**
    * Creates a new NonNullList with <i>fixed</i> size and default value. The list will be filled with the default
    * value.
    */
   public static <E> NonNullList<E> withSize(int pSize, E pDefaultValue) {
      Validate.notNull(pDefaultValue);
      Object[] aobject = new Object[pSize];
      Arrays.fill(aobject, pDefaultValue);
      return new NonNullList<>(Arrays.asList((E[])aobject), pDefaultValue);
   }

   @SafeVarargs
   public static <E> NonNullList<E> of(E pDefaultValue, E... pElements) {
      return new NonNullList<>(Arrays.asList(pElements), pDefaultValue);
   }

   protected NonNullList() {
      this(Lists.newArrayList(), (E)null);
   }

   protected NonNullList(List<E> pList, @Nullable E pDefaultValue) {
      this.list = pList;
      this.defaultValue = pDefaultValue;
   }

   @Nonnull
   public E get(int p_get_1_) {
      return this.list.get(p_get_1_);
   }

   public E set(int p_set_1_, E p_set_2_) {
      Validate.notNull(p_set_2_);
      return this.list.set(p_set_1_, p_set_2_);
   }

   public void add(int p_add_1_, E p_add_2_) {
      Validate.notNull(p_add_2_);
      this.list.add(p_add_1_, p_add_2_);
   }

   public E remove(int p_remove_1_) {
      return this.list.remove(p_remove_1_);
   }

   public int size() {
      return this.list.size();
   }

   public void clear() {
      if (this.defaultValue == null) {
         super.clear();
      } else {
         for(int i = 0; i < this.size(); ++i) {
            this.set(i, this.defaultValue);
         }
      }

   }
}