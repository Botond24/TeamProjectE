package net.minecraft.block;

import java.util.Random;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public interface IGrowable {
   /**
    * Whether this IGrowable can grow
    */
   boolean isValidBonemealTarget(IBlockReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient);

   boolean isBonemealSuccess(World pLevel, Random pRand, BlockPos pPos, BlockState pState);

   void performBonemeal(ServerWorld pLevel, Random pRand, BlockPos pPos, BlockState pState);
}