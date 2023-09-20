package net.minecraft.block;

import net.minecraft.enchantment.IArmorVanishable;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractSkullBlock extends ContainerBlock implements IArmorVanishable {
   private final SkullBlock.ISkullType type;

   public AbstractSkullBlock(SkullBlock.ISkullType pType, AbstractBlock.Properties pProperties) {
      super(pProperties);
      this.type = pType;
   }

   public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
      return new SkullTileEntity();
   }

   @OnlyIn(Dist.CLIENT)
   public SkullBlock.ISkullType getType() {
      return this.type;
   }

   public boolean isPathfindable(BlockState pState, IBlockReader pLevel, BlockPos pPos, PathType pType) {
      return false;
   }
}