package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public abstract class AbstractBannerBlock extends ContainerBlock {
   private final DyeColor color;

   protected AbstractBannerBlock(DyeColor pColor, AbstractBlock.Properties pProperties) {
      super(pProperties);
      this.color = pColor;
   }

   /**
    * Return true if an entity can be spawned inside the block (used to get the player's bed spawn location)
    */
   public boolean isPossibleToRespawnInThis() {
      return true;
   }

   public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
      return new BannerTileEntity(this.color);
   }

   /**
    * Called by ItemBlocks after a block is set in the world, to allow post-place logic
    */
   public void setPlacedBy(World pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
      if (pStack.hasCustomHoverName()) {
         TileEntity tileentity = pLevel.getBlockEntity(pPos);
         if (tileentity instanceof BannerTileEntity) {
            ((BannerTileEntity)tileentity).setCustomName(pStack.getHoverName());
         }
      }

   }

   public ItemStack getCloneItemStack(IBlockReader pLevel, BlockPos pPos, BlockState pState) {
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      return tileentity instanceof BannerTileEntity ? ((BannerTileEntity)tileentity).getItem(pState) : super.getCloneItemStack(pLevel, pPos, pState);
   }

   public DyeColor getColor() {
      return this.color;
   }
}