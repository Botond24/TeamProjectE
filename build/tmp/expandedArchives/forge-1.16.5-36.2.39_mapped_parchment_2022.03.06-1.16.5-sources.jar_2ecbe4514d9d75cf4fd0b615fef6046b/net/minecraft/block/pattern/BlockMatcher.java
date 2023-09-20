package net.minecraft.block.pattern;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

public class BlockMatcher implements Predicate<BlockState> {
   private final Block block;

   public BlockMatcher(Block pBlock) {
      this.block = pBlock;
   }

   public static BlockMatcher forBlock(Block pBlock) {
      return new BlockMatcher(pBlock);
   }

   public boolean test(@Nullable BlockState p_test_1_) {
      return p_test_1_ != null && p_test_1_.is(this.block);
   }
}