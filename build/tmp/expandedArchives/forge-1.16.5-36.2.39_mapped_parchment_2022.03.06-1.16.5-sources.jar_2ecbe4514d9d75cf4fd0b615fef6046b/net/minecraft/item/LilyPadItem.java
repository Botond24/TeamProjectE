package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.world.World;

public class LilyPadItem extends BlockItem {
   public LilyPadItem(Block p_i48456_1_, Item.Properties p_i48456_2_) {
      super(p_i48456_1_, p_i48456_2_);
   }

   /**
    * Called when this item is used when targetting a Block
    */
   public ActionResultType useOn(ItemUseContext pContext) {
      return ActionResultType.PASS;
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pHand) {
      BlockRayTraceResult blockraytraceresult = getPlayerPOVHitResult(pLevel, pPlayer, RayTraceContext.FluidMode.SOURCE_ONLY);
      BlockRayTraceResult blockraytraceresult1 = blockraytraceresult.withPosition(blockraytraceresult.getBlockPos().above());
      ActionResultType actionresulttype = super.useOn(new ItemUseContext(pPlayer, pHand, blockraytraceresult1));
      return new ActionResult<>(actionresulttype, pPlayer.getItemInHand(pHand));
   }
}