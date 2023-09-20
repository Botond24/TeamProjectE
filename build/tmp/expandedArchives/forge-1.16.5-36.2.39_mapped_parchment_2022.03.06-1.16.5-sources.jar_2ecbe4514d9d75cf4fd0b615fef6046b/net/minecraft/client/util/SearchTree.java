package net.minecraft.client.util;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SearchTree<T> extends SearchTreeReloadable<T> {
   protected SuffixArray<T> tree = new SuffixArray<>();
   private final Function<T, Stream<String>> filler;

   public SearchTree(Function<T, Stream<String>> pFiller, Function<T, Stream<ResourceLocation>> pIdGetter) {
      super(pIdGetter);
      this.filler = pFiller;
   }

   /**
    * Recalculates the contents of this search tree, reapplying {@link #nameFunc} and {@link #idFunc}. Should be called
    * whenever resources are reloaded (e.g. language changes).
    */
   public void refresh() {
      this.tree = new SuffixArray<>();
      super.refresh();
      this.tree.generate();
   }

   /**
    * Directly puts the given item into {@link #byId} and {@link #byName}, applying {@link #nameFunc} and {@link
    * idFunc}.
    */
   protected void index(T pElement) {
      super.index(pElement);
      this.filler.apply(pElement).forEach((p_217880_2_) -> {
         this.tree.add(pElement, p_217880_2_.toLowerCase(Locale.ROOT));
      });
   }

   /**
    * Searches this search tree for the given text.
    * <p>
    * If the query does not contain a <code>:</code>, then only {@link #byName} is searched" if it does contain a colon,
    * both {@link #byName} and {@link #byId} are searched and the results are merged using a {@link MergingIterator}.
    * @return A list of all matching items in this search tree.
    */
   public List<T> search(String pSearchText) {
      int i = pSearchText.indexOf(58);
      if (i < 0) {
         return this.tree.search(pSearchText);
      } else {
         List<T> list = this.namespaceTree.search(pSearchText.substring(0, i).trim());
         String s = pSearchText.substring(i + 1).trim();
         List<T> list1 = this.pathTree.search(s);
         List<T> list2 = this.tree.search(s);
         return Lists.newArrayList(new SearchTreeReloadable.JoinedIterator<>(list.iterator(), new SearchTree.MergingIterator<>(list1.iterator(), list2.iterator(), this::comparePosition), this::comparePosition));
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class MergingIterator<T> extends AbstractIterator<T> {
      private final PeekingIterator<T> firstIterator;
      private final PeekingIterator<T> secondIterator;
      private final Comparator<T> orderT;

      public MergingIterator(Iterator<T> p_i49977_1_, Iterator<T> p_i49977_2_, Comparator<T> pOrderT) {
         this.firstIterator = Iterators.peekingIterator(p_i49977_1_);
         this.secondIterator = Iterators.peekingIterator(p_i49977_2_);
         this.orderT = pOrderT;
      }

      protected T computeNext() {
         boolean flag = !this.firstIterator.hasNext();
         boolean flag1 = !this.secondIterator.hasNext();
         if (flag && flag1) {
            return this.endOfData();
         } else if (flag) {
            return this.secondIterator.next();
         } else if (flag1) {
            return this.firstIterator.next();
         } else {
            int i = this.orderT.compare(this.firstIterator.peek(), this.secondIterator.peek());
            if (i == 0) {
               this.secondIterator.next();
            }

            return (T)(i <= 0 ? this.firstIterator.next() : this.secondIterator.next());
         }
      }
   }
}