package net.minecraft.dispenser;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class ProxyBlockSource implements IBlockSource {
   private final ServerWorld level;
   private final BlockPos pos;

   public ProxyBlockSource(ServerWorld pLevel, BlockPos pPos) {
      this.level = pLevel;
      this.pos = pPos;
   }

   public ServerWorld getLevel() {
      return this.level;
   }

   public double x() {
      return (double)this.pos.getX() + 0.5D;
   }

   public double y() {
      return (double)this.pos.getY() + 0.5D;
   }

   public double z() {
      return (double)this.pos.getZ() + 0.5D;
   }

   public BlockPos getPos() {
      return this.pos;
   }

   /**
    * Gets the block state of this position and returns it.
    * @return Block state in this position
    */
   public BlockState getBlockState() {
      return this.level.getBlockState(this.pos);
   }

   public <T extends TileEntity> T getEntity() {
      return (T)this.level.getBlockEntity(this.pos);
   }
}