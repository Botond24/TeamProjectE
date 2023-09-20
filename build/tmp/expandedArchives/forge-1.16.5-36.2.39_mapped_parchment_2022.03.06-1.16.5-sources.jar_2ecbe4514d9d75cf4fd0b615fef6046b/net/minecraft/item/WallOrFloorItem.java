package net.minecraft.item;

import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.IWorldReader;

public class WallOrFloorItem extends BlockItem {
   protected final Block wallBlock;

   public WallOrFloorItem(Block pStandingBlock, Block pWallBlock, Item.Properties pProperties) {
      super(pStandingBlock, pProperties);
      this.wallBlock = pWallBlock;
   }

   @Nullable
   protected BlockState getPlacementState(BlockItemUseContext pContext) {
      BlockState blockstate = this.wallBlock.getStateForPlacement(pContext);
      BlockState blockstate1 = null;
      IWorldReader iworldreader = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();

      for(Direction direction : pContext.getNearestLookingDirections()) {
         if (direction != Direction.UP) {
            BlockState blockstate2 = direction == Direction.DOWN ? this.getBlock().getStateForPlacement(pContext) : blockstate;
            if (blockstate2 != null && blockstate2.canSurvive(iworldreader, blockpos)) {
               blockstate1 = blockstate2;
               break;
            }
         }
      }

      return blockstate1 != null && iworldreader.isUnobstructed(blockstate1, blockpos, ISelectionContext.empty()) ? blockstate1 : null;
   }

   public void registerBlocks(Map<Block, Item> pBlockToItemMap, Item pItem) {
      super.registerBlocks(pBlockToItemMap, pItem);
      pBlockToItemMap.put(this.wallBlock, pItem);
   }

   public void removeFromBlockToItemMap(Map<Block, Item> blockToItemMap, Item itemIn) {
      super.removeFromBlockToItemMap(blockToItemMap, itemIn);
      blockToItemMap.remove(this.wallBlock);
   }
}
