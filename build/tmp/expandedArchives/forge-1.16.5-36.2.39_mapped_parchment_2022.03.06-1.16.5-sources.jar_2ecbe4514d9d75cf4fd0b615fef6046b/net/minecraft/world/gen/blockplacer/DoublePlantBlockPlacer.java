package net.minecraft.world.gen.blockplacer;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class DoublePlantBlockPlacer extends BlockPlacer {
   public static final Codec<DoublePlantBlockPlacer> CODEC;
   public static final DoublePlantBlockPlacer INSTANCE = new DoublePlantBlockPlacer();

   protected BlockPlacerType<?> type() {
      return BlockPlacerType.DOUBLE_PLANT_PLACER;
   }

   public void place(IWorld pLevel, BlockPos pPos, BlockState pState, Random pRandom) {
      ((DoublePlantBlock)pState.getBlock()).placeAt(pLevel, pPos, 2);
   }

   static {
      CODEC = Codec.unit(() -> {
         return INSTANCE;
      });
   }
}