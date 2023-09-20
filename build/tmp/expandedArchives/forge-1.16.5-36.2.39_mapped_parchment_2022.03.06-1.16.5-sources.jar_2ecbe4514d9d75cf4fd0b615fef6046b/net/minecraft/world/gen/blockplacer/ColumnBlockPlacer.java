package net.minecraft.world.gen.blockplacer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class ColumnBlockPlacer extends BlockPlacer {
   public static final Codec<ColumnBlockPlacer> CODEC = RecordCodecBuilder.create((p_236441_0_) -> {
      return p_236441_0_.group(Codec.INT.fieldOf("min_size").forGetter((p_236442_0_) -> {
         return p_236442_0_.minSize;
      }), Codec.INT.fieldOf("extra_size").forGetter((p_236440_0_) -> {
         return p_236440_0_.extraSize;
      })).apply(p_236441_0_, ColumnBlockPlacer::new);
   });
   private final int minSize;
   private final int extraSize;

   public ColumnBlockPlacer(int p_i225826_1_, int p_i225826_2_) {
      this.minSize = p_i225826_1_;
      this.extraSize = p_i225826_2_;
   }

   protected BlockPlacerType<?> type() {
      return BlockPlacerType.COLUMN_PLACER;
   }

   public void place(IWorld pLevel, BlockPos pPos, BlockState pState, Random pRandom) {
      BlockPos.Mutable blockpos$mutable = pPos.mutable();
      int i = this.minSize + pRandom.nextInt(pRandom.nextInt(this.extraSize + 1) + 1);

      for(int j = 0; j < i; ++j) {
         pLevel.setBlock(blockpos$mutable, pState, 2);
         blockpos$mutable.move(Direction.UP);
      }

   }
}