package net.minecraft.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SkullWallPlayerBlock extends WallSkullBlock {
   public SkullWallPlayerBlock(AbstractBlock.Properties p_i48353_1_) {
      super(SkullBlock.Types.PLAYER, p_i48353_1_);
   }

   /**
    * Called by ItemBlocks after a block is set in the world, to allow post-place logic
    */
   public void setPlacedBy(World pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
      Blocks.PLAYER_HEAD.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
   }

   public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
      return Blocks.PLAYER_HEAD.getDrops(pState, pBuilder);
   }
}