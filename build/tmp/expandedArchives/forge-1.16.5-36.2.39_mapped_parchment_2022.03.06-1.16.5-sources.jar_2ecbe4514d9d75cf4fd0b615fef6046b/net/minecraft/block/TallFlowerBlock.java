package net.minecraft.block;

import java.util.Random;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class TallFlowerBlock extends DoublePlantBlock implements IGrowable {
   public TallFlowerBlock(AbstractBlock.Properties p_i48311_1_) {
      super(p_i48311_1_);
   }

   public boolean canBeReplaced(BlockState pState, BlockItemUseContext pUseContext) {
      return false;
   }

   /**
    * Whether this IGrowable can grow
    */
   public boolean isValidBonemealTarget(IBlockReader pLevel, BlockPos pPos, BlockState pState, boolean pIsClient) {
      return true;
   }

   public boolean isBonemealSuccess(World pLevel, Random pRand, BlockPos pPos, BlockState pState) {
      return true;
   }

   public void performBonemeal(ServerWorld pLevel, Random pRand, BlockPos pPos, BlockState pState) {
      popResource(pLevel, pPos, new ItemStack(this));
   }
}