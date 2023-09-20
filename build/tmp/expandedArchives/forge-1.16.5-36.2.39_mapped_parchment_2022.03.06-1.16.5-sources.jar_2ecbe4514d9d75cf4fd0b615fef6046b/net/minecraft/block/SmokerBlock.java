package net.minecraft.block;

import java.util.Random;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.SmokerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SmokerBlock extends AbstractFurnaceBlock {
   public SmokerBlock(AbstractBlock.Properties p_i49973_1_) {
      super(p_i49973_1_);
   }

   public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
      return new SmokerTileEntity();
   }

   /**
    * Interface for handling interaction with blocks that impliment AbstractFurnaceBlock. Called in onBlockActivated
    * inside AbstractFurnaceBlock.
    */
   protected void openContainer(World pLevel, BlockPos pPos, PlayerEntity pPlayer) {
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      if (tileentity instanceof SmokerTileEntity) {
         pPlayer.openMenu((INamedContainerProvider)tileentity);
         pPlayer.awardStat(Stats.INTERACT_WITH_SMOKER);
      }

   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
    * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
    * of whether the block can receive random update ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
      if (pState.getValue(LIT)) {
         double d0 = (double)pPos.getX() + 0.5D;
         double d1 = (double)pPos.getY();
         double d2 = (double)pPos.getZ() + 0.5D;
         if (pRand.nextDouble() < 0.1D) {
            pLevel.playLocalSound(d0, d1, d2, SoundEvents.SMOKER_SMOKE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
         }

         pLevel.addParticle(ParticleTypes.SMOKE, d0, d1 + 1.1D, d2, 0.0D, 0.0D, 0.0D);
      }
   }
}