package net.minecraft.world.gen;

import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public interface IWorldGenerationBaseReader {
   boolean isStateAtPosition(BlockPos pPos, Predicate<BlockState> pState);

   BlockPos getHeightmapPos(Heightmap.Type pHeightmapType, BlockPos pPos);
}