package net.minecraft.block.pattern;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;

public class BlockStateMatcher implements Predicate<BlockState> {
   public static final Predicate<BlockState> ANY = (p_201026_0_) -> {
      return true;
   };
   private final StateContainer<Block, BlockState> definition;
   private final Map<Property<?>, Predicate<Object>> properties = Maps.newHashMap();

   private BlockStateMatcher(StateContainer<Block, BlockState> pDefinition) {
      this.definition = pDefinition;
   }

   public static BlockStateMatcher forBlock(Block pBlock) {
      return new BlockStateMatcher(pBlock.getStateDefinition());
   }

   public boolean test(@Nullable BlockState p_test_1_) {
      if (p_test_1_ != null && p_test_1_.getBlock().equals(this.definition.getOwner())) {
         if (this.properties.isEmpty()) {
            return true;
         } else {
            for(Entry<Property<?>, Predicate<Object>> entry : this.properties.entrySet()) {
               if (!this.applies(p_test_1_, entry.getKey(), entry.getValue())) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   protected <T extends Comparable<T>> boolean applies(BlockState pState, Property<T> pProperty, Predicate<Object> pValuePredicate) {
      T t = pState.getValue(pProperty);
      return pValuePredicate.test(t);
   }

   public <V extends Comparable<V>> BlockStateMatcher where(Property<V> pProperty, Predicate<Object> pValuePredicate) {
      if (!this.definition.getProperties().contains(pProperty)) {
         throw new IllegalArgumentException(this.definition + " cannot support property " + pProperty);
      } else {
         this.properties.put(pProperty, pValuePredicate);
         return this;
      }
   }
}