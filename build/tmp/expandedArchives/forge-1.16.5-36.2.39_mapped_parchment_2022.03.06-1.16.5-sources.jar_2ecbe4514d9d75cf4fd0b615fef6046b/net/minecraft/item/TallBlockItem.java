package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public class TallBlockItem extends BlockItem {
   public TallBlockItem(Block p_i48511_1_, Item.Properties p_i48511_2_) {
      super(p_i48511_1_, p_i48511_2_);
   }

   protected boolean placeBlock(BlockItemUseContext pContext, BlockState pState) {
      pContext.getLevel().setBlock(pContext.getClickedPos().above(), Blocks.AIR.defaultBlockState(), 27);
      return super.placeBlock(pContext, pState);
   }
}