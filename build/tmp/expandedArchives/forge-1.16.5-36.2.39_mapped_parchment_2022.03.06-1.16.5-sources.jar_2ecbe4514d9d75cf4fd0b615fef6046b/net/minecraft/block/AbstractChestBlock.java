package net.minecraft.block;

import java.util.function.Supplier;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMerger;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractChestBlock<E extends TileEntity> extends ContainerBlock {
   protected final Supplier<TileEntityType<? extends E>> blockEntityType;

   protected AbstractChestBlock(AbstractBlock.Properties pProperties, Supplier<TileEntityType<? extends E>> pBlockEntityFactory) {
      super(pProperties);
      this.blockEntityType = pBlockEntityFactory;
   }

   @OnlyIn(Dist.CLIENT)
   public abstract TileEntityMerger.ICallbackWrapper<? extends ChestTileEntity> combine(BlockState pState, World pLevel, BlockPos pPos, boolean pOverride);
}