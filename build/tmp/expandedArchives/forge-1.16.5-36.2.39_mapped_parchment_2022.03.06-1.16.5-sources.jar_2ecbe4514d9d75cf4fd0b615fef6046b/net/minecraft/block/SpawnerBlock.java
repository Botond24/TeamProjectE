package net.minecraft.block;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.server.ServerWorld;

public class SpawnerBlock extends ContainerBlock {
   public SpawnerBlock(AbstractBlock.Properties p_i48364_1_) {
      super(p_i48364_1_);
   }

   public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
      return new MobSpawnerTileEntity();
   }

   /**
    * Perform side-effects from block dropping, such as creating silverfish
    */
   public void spawnAfterBreak(BlockState pState, ServerWorld pLevel, BlockPos pPos, ItemStack pStack) {
      super.spawnAfterBreak(pState, pLevel, pPos, pStack);
   }

   @Override
   public int getExpDrop(BlockState state, net.minecraft.world.IWorldReader world, BlockPos pos, int fortune, int silktouch) {
      return 15 + RANDOM.nextInt(15) + RANDOM.nextInt(15);
   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link IBlockState#getRenderType()} whenever possible. Implementing/overriding is fine.
    */
   public BlockRenderType getRenderShape(BlockState pState) {
      return BlockRenderType.MODEL;
   }

   public ItemStack getCloneItemStack(IBlockReader pLevel, BlockPos pPos, BlockState pState) {
      return ItemStack.EMPTY;
   }
}
