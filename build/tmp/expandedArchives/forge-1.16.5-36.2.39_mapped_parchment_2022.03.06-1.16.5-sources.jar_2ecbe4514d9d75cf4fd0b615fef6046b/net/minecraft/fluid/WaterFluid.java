package net.minecraft.fluid;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class WaterFluid extends FlowingFluid {
   public Fluid getFlowing() {
      return Fluids.FLOWING_WATER;
   }

   public Fluid getSource() {
      return Fluids.WATER;
   }

   public Item getBucket() {
      return Items.WATER_BUCKET;
   }

   @OnlyIn(Dist.CLIENT)
   public void animateTick(World pLevel, BlockPos pPos, FluidState pState, Random pRandom) {
      if (!pState.isSource() && !pState.getValue(FALLING)) {
         if (pRandom.nextInt(64) == 0) {
            pLevel.playLocalSound((double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, SoundEvents.WATER_AMBIENT, SoundCategory.BLOCKS, pRandom.nextFloat() * 0.25F + 0.75F, pRandom.nextFloat() + 0.5F, false);
         }
      } else if (pRandom.nextInt(10) == 0) {
         pLevel.addParticle(ParticleTypes.UNDERWATER, (double)pPos.getX() + pRandom.nextDouble(), (double)pPos.getY() + pRandom.nextDouble(), (double)pPos.getZ() + pRandom.nextDouble(), 0.0D, 0.0D, 0.0D);
      }

   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public IParticleData getDripParticle() {
      return ParticleTypes.DRIPPING_WATER;
   }

   protected boolean canConvertToSource() {
      return true;
   }

   protected void beforeDestroyingBlock(IWorld pLevel, BlockPos pPos, BlockState pState) {
      TileEntity tileentity = pState.hasTileEntity() ? pLevel.getBlockEntity(pPos) : null;
      Block.dropResources(pState, pLevel, pPos, tileentity);
   }

   public int getSlopeFindDistance(IWorldReader pLevel) {
      return 4;
   }

   public BlockState createLegacyBlock(FluidState pState) {
      return Blocks.WATER.defaultBlockState().setValue(FlowingFluidBlock.LEVEL, Integer.valueOf(getLegacyLevel(pState)));
   }

   public boolean isSame(Fluid pFluid) {
      return pFluid == Fluids.WATER || pFluid == Fluids.FLOWING_WATER;
   }

   public int getDropOff(IWorldReader pLevel) {
      return 1;
   }

   public int getTickDelay(IWorldReader p_205569_1_) {
      return 5;
   }

   public boolean canBeReplacedWith(FluidState pFluidState, IBlockReader pBlockReader, BlockPos pPos, Fluid pFluid, Direction pDirection) {
      return pDirection == Direction.DOWN && !pFluid.is(FluidTags.WATER);
   }

   protected float getExplosionResistance() {
      return 100.0F;
   }

   public static class Flowing extends WaterFluid {
      protected void createFluidStateDefinition(StateContainer.Builder<Fluid, FluidState> pBuilder) {
         super.createFluidStateDefinition(pBuilder);
         pBuilder.add(LEVEL);
      }

      public int getAmount(FluidState pState) {
         return pState.getValue(LEVEL);
      }

      public boolean isSource(FluidState pState) {
         return false;
      }
   }

   public static class Source extends WaterFluid {
      public int getAmount(FluidState pState) {
         return 8;
      }

      public boolean isSource(FluidState pState) {
         return true;
      }
   }
}
