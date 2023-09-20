package net.minecraft.block;

import java.util.Random;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RedstoneOreBlock extends Block {
   public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

   public RedstoneOreBlock(AbstractBlock.Properties p_i48345_1_) {
      super(p_i48345_1_);
      this.registerDefaultState(this.defaultBlockState().setValue(LIT, Boolean.valueOf(false)));
   }

   public void attack(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer) {
      interact(pState, pLevel, pPos);
      super.attack(pState, pLevel, pPos, pPlayer);
   }

   public void stepOn(World p_176199_1_, BlockPos p_176199_2_, Entity p_176199_3_) {
      interact(p_176199_1_.getBlockState(p_176199_2_), p_176199_1_, p_176199_2_);
      super.stepOn(p_176199_1_, p_176199_2_, p_176199_3_);
   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      if (pLevel.isClientSide) {
         spawnParticles(pLevel, pPos);
      } else {
         interact(pState, pLevel, pPos);
      }

      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      return itemstack.getItem() instanceof BlockItem && (new BlockItemUseContext(pPlayer, pHand, itemstack, pHit)).canPlace() ? ActionResultType.PASS : ActionResultType.SUCCESS;
   }

   private static void interact(BlockState pState, World pLevel, BlockPos pPos) {
      spawnParticles(pLevel, pPos);
      if (!pState.getValue(LIT)) {
         pLevel.setBlock(pPos, pState.setValue(LIT, Boolean.valueOf(true)), 3);
      }

   }

   /**
    * Returns whether or not this block is of a type that needs random ticking. Called for ref-counting purposes by
    * ExtendedBlockStorage in order to broadly cull a chunk from the random chunk update list for efficiency's sake.
    */
   public boolean isRandomlyTicking(BlockState pState) {
      return pState.getValue(LIT);
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
      if (pState.getValue(LIT)) {
         pLevel.setBlock(pPos, pState.setValue(LIT, Boolean.valueOf(false)), 3);
      }

   }

   /**
    * Perform side-effects from block dropping, such as creating silverfish
    */
   public void spawnAfterBreak(BlockState pState, ServerWorld pLevel, BlockPos pPos, ItemStack pStack) {
      super.spawnAfterBreak(pState, pLevel, pPos, pStack);
   }

   @Override
   public int getExpDrop(BlockState state, net.minecraft.world.IWorldReader world, BlockPos pos, int fortune, int silktouch) {
      return silktouch == 0 ? 1 + RANDOM.nextInt(5) : 0;
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
    * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
    * of whether the block can receive random update ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
      if (pState.getValue(LIT)) {
         spawnParticles(pLevel, pPos);
      }

   }

   private static void spawnParticles(World pLevel, BlockPos pPos) {
      double d0 = 0.5625D;
      Random random = pLevel.random;

      for(Direction direction : Direction.values()) {
         BlockPos blockpos = pPos.relative(direction);
         if (!pLevel.getBlockState(blockpos).isSolidRender(pLevel, blockpos)) {
            Direction.Axis direction$axis = direction.getAxis();
            double d1 = direction$axis == Direction.Axis.X ? 0.5D + 0.5625D * (double)direction.getStepX() : (double)random.nextFloat();
            double d2 = direction$axis == Direction.Axis.Y ? 0.5D + 0.5625D * (double)direction.getStepY() : (double)random.nextFloat();
            double d3 = direction$axis == Direction.Axis.Z ? 0.5D + 0.5625D * (double)direction.getStepZ() : (double)random.nextFloat();
            pLevel.addParticle(RedstoneParticleData.REDSTONE, (double)pPos.getX() + d1, (double)pPos.getY() + d2, (double)pPos.getZ() + d3, 0.0D, 0.0D, 0.0D);
         }
      }

   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(LIT);
   }
}
