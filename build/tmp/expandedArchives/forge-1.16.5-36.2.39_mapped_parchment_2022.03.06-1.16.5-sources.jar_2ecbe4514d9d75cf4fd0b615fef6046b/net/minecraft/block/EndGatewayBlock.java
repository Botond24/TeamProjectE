package net.minecraft.block;

import java.util.Random;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.EndGatewayTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EndGatewayBlock extends ContainerBlock {
   public EndGatewayBlock(AbstractBlock.Properties p_i48407_1_) {
      super(p_i48407_1_);
   }

   public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
      return new EndGatewayTileEntity();
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
    * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
    * of whether the block can receive random update ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      if (tileentity instanceof EndGatewayTileEntity) {
         int i = ((EndGatewayTileEntity)tileentity).getParticleAmount();

         for(int j = 0; j < i; ++j) {
            double d0 = (double)pPos.getX() + pRand.nextDouble();
            double d1 = (double)pPos.getY() + pRand.nextDouble();
            double d2 = (double)pPos.getZ() + pRand.nextDouble();
            double d3 = (pRand.nextDouble() - 0.5D) * 0.5D;
            double d4 = (pRand.nextDouble() - 0.5D) * 0.5D;
            double d5 = (pRand.nextDouble() - 0.5D) * 0.5D;
            int k = pRand.nextInt(2) * 2 - 1;
            if (pRand.nextBoolean()) {
               d2 = (double)pPos.getZ() + 0.5D + 0.25D * (double)k;
               d5 = (double)(pRand.nextFloat() * 2.0F * (float)k);
            } else {
               d0 = (double)pPos.getX() + 0.5D + 0.25D * (double)k;
               d3 = (double)(pRand.nextFloat() * 2.0F * (float)k);
            }

            pLevel.addParticle(ParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
         }

      }
   }

   public ItemStack getCloneItemStack(IBlockReader pLevel, BlockPos pPos, BlockState pState) {
      return ItemStack.EMPTY;
   }

   public boolean canBeReplaced(BlockState pState, Fluid pFluid) {
      return false;
   }
}