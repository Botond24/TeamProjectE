package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class IceBlock extends BreakableBlock {
   public IceBlock(AbstractBlock.Properties p_i48375_1_) {
      super(p_i48375_1_);
   }

   /**
    * Spawns the block's drops in the world. By the time this is called the Block has possibly been set to air via
    * Block.removedByPlayer
    */
   public void playerDestroy(World pLevel, PlayerEntity pPlayer, BlockPos pPos, BlockState pState, @Nullable TileEntity pTe, ItemStack pStack) {
      super.playerDestroy(pLevel, pPlayer, pPos, pState, pTe, pStack);
      if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, pStack) == 0) {
         if (pLevel.dimensionType().ultraWarm()) {
            pLevel.removeBlock(pPos, false);
            return;
         }

         Material material = pLevel.getBlockState(pPos.below()).getMaterial();
         if (material.blocksMotion() || material.isLiquid()) {
            pLevel.setBlockAndUpdate(pPos, Blocks.WATER.defaultBlockState());
         }
      }

   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
      if (pLevel.getBrightness(LightType.BLOCK, pPos) > 11 - pState.getLightBlock(pLevel, pPos)) {
         this.melt(pState, pLevel, pPos);
      }

   }

   protected void melt(BlockState pState, World pLevel, BlockPos pPos) {
      if (pLevel.dimensionType().ultraWarm()) {
         pLevel.removeBlock(pPos, false);
      } else {
         pLevel.setBlockAndUpdate(pPos, Blocks.WATER.defaultBlockState());
         pLevel.neighborChanged(pPos, Blocks.WATER, pPos);
      }
   }

   /**
    * @deprecated call via {@link IBlockState#getMobilityFlag()} whenever possible. Implementing/overriding is fine.
    */
   public PushReaction getPistonPushReaction(BlockState pState) {
      return PushReaction.NORMAL;
   }
}