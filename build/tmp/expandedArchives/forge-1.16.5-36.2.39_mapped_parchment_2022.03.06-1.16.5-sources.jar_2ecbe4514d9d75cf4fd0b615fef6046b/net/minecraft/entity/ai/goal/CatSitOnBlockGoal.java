package net.minecraft.entity.ai.goal;

import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.state.properties.BedPart;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class CatSitOnBlockGoal extends MoveToBlockGoal {
   private final CatEntity cat;

   public CatSitOnBlockGoal(CatEntity p_i50330_1_, double p_i50330_2_) {
      super(p_i50330_1_, p_i50330_2_, 8);
      this.cat = p_i50330_1_;
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      return this.cat.isTame() && !this.cat.isOrderedToSit() && super.canUse();
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      super.start();
      this.cat.setInSittingPose(false);
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      super.stop();
      this.cat.setInSittingPose(false);
   }

   /**
    * Keep ticking a continuous task that has already been started
    */
   public void tick() {
      super.tick();
      this.cat.setInSittingPose(this.isReachedTarget());
   }

   /**
    * Return true to set given position as destination
    */
   protected boolean isValidTarget(IWorldReader pLevel, BlockPos pPos) {
      if (!pLevel.isEmptyBlock(pPos.above())) {
         return false;
      } else {
         BlockState blockstate = pLevel.getBlockState(pPos);
         if (blockstate.is(Blocks.CHEST)) {
            return ChestTileEntity.getOpenCount(pLevel, pPos) < 1;
         } else {
            return blockstate.is(Blocks.FURNACE) && blockstate.getValue(FurnaceBlock.LIT) ? true : blockstate.is(BlockTags.BEDS, (p_234025_0_) -> {
               return p_234025_0_.<BedPart>getOptionalValue(BedBlock.PART).map((p_234026_0_) -> {
                  return p_234026_0_ != BedPart.HEAD;
               }).orElse(true);
            });
         }
      }
   }
}