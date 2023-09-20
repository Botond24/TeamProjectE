package net.minecraft.item;

import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class BlockItemUseContext extends ItemUseContext {
   private final BlockPos relativePos;
   protected boolean replaceClicked = true;

   public BlockItemUseContext(PlayerEntity pPlayer, Hand pHand, ItemStack pItemStack, BlockRayTraceResult pHitResult) {
      this(pPlayer.level, pPlayer, pHand, pItemStack, pHitResult);
   }

   public BlockItemUseContext(ItemUseContext pContext) {
      this(pContext.getLevel(), pContext.getPlayer(), pContext.getHand(), pContext.getItemInHand(), pContext.getHitResult());
   }

   public BlockItemUseContext(World p_i50056_1_, @Nullable PlayerEntity p_i50056_2_, Hand p_i50056_3_, ItemStack p_i50056_4_, BlockRayTraceResult p_i50056_5_) {
      super(p_i50056_1_, p_i50056_2_, p_i50056_3_, p_i50056_4_, p_i50056_5_);
      this.relativePos = p_i50056_5_.getBlockPos().relative(p_i50056_5_.getDirection());
      this.replaceClicked = p_i50056_1_.getBlockState(p_i50056_5_.getBlockPos()).canBeReplaced(this);
   }

   public static BlockItemUseContext at(BlockItemUseContext pContext, BlockPos pPos, Direction pDirection) {
      return new BlockItemUseContext(pContext.getLevel(), pContext.getPlayer(), pContext.getHand(), pContext.getItemInHand(), new BlockRayTraceResult(new Vector3d((double)pPos.getX() + 0.5D + (double)pDirection.getStepX() * 0.5D, (double)pPos.getY() + 0.5D + (double)pDirection.getStepY() * 0.5D, (double)pPos.getZ() + 0.5D + (double)pDirection.getStepZ() * 0.5D), pDirection, pPos, false));
   }

   public BlockPos getClickedPos() {
      return this.replaceClicked ? super.getClickedPos() : this.relativePos;
   }

   public boolean canPlace() {
      return this.replaceClicked || this.getLevel().getBlockState(this.getClickedPos()).canBeReplaced(this);
   }

   public boolean replacingClickedOnBlock() {
      return this.replaceClicked;
   }

   public Direction getNearestLookingDirection() {
      return Direction.orderedByNearest(this.getPlayer())[0];
   }

   public Direction[] getNearestLookingDirections() {
      Direction[] adirection = Direction.orderedByNearest(this.getPlayer());
      if (this.replaceClicked) {
         return adirection;
      } else {
         Direction direction = this.getClickedFace();

         int i;
         for(i = 0; i < adirection.length && adirection[i] != direction.getOpposite(); ++i) {
         }

         if (i > 0) {
            System.arraycopy(adirection, 0, adirection, 1, i);
            adirection[0] = direction.getOpposite();
         }

         return adirection;
      }
   }
}