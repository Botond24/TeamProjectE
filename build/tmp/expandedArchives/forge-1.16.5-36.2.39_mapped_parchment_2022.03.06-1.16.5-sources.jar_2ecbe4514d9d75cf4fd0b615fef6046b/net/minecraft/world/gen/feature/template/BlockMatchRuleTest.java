package net.minecraft.world.gen.feature.template;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.registry.Registry;

public class BlockMatchRuleTest extends RuleTest {
   public static final Codec<BlockMatchRuleTest> CODEC = Registry.BLOCK.fieldOf("block").xmap(BlockMatchRuleTest::new, (p_237076_0_) -> {
      return p_237076_0_.block;
   }).codec();
   private final Block block;

   public BlockMatchRuleTest(Block p_i51334_1_) {
      this.block = p_i51334_1_;
   }

   public boolean test(BlockState pState, Random pRandom) {
      return pState.is(this.block);
   }

   protected IRuleTestType<?> getType() {
      return IRuleTestType.BLOCK_TEST;
   }
}