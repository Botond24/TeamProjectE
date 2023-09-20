package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WitherSkeletonWallSkullBlock extends WallSkullBlock {
   public WitherSkeletonWallSkullBlock(AbstractBlock.Properties p_i48292_1_) {
      super(SkullBlock.Types.WITHER_SKELETON, p_i48292_1_);
   }

   /**
    * Called by ItemBlocks after a block is set in the world, to allow post-place logic
    */
   public void setPlacedBy(World pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
      Blocks.WITHER_SKELETON_SKULL.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
   }
}