package net.minecraft.block;

import com.google.common.collect.Lists;
import java.util.Queue;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpongeBlock extends Block {
   public SpongeBlock(AbstractBlock.Properties p_i48325_1_) {
      super(p_i48325_1_);
   }

   public void onPlace(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (!pOldState.is(pState.getBlock())) {
         this.tryAbsorbWater(pLevel, pPos);
      }
   }

   public void neighborChanged(BlockState pState, World pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      this.tryAbsorbWater(pLevel, pPos);
      super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);
   }

   protected void tryAbsorbWater(World pLevel, BlockPos pPos) {
      if (this.removeWaterBreadthFirstSearch(pLevel, pPos)) {
         pLevel.setBlock(pPos, Blocks.WET_SPONGE.defaultBlockState(), 2);
         pLevel.levelEvent(2001, pPos, Block.getId(Blocks.WATER.defaultBlockState()));
      }

   }

   private boolean removeWaterBreadthFirstSearch(World pLevel, BlockPos pPos) {
      Queue<Tuple<BlockPos, Integer>> queue = Lists.newLinkedList();
      queue.add(new Tuple<>(pPos, 0));
      int i = 0;

      while(!queue.isEmpty()) {
         Tuple<BlockPos, Integer> tuple = queue.poll();
         BlockPos blockpos = tuple.getA();
         int j = tuple.getB();

         for(Direction direction : Direction.values()) {
            BlockPos blockpos1 = blockpos.relative(direction);
            BlockState blockstate = pLevel.getBlockState(blockpos1);
            FluidState fluidstate = pLevel.getFluidState(blockpos1);
            Material material = blockstate.getMaterial();
            if (fluidstate.is(FluidTags.WATER)) {
               if (blockstate.getBlock() instanceof IBucketPickupHandler && ((IBucketPickupHandler)blockstate.getBlock()).takeLiquid(pLevel, blockpos1, blockstate) != Fluids.EMPTY) {
                  ++i;
                  if (j < 6) {
                     queue.add(new Tuple<>(blockpos1, j + 1));
                  }
               } else if (blockstate.getBlock() instanceof FlowingFluidBlock) {
                  pLevel.setBlock(blockpos1, Blocks.AIR.defaultBlockState(), 3);
                  ++i;
                  if (j < 6) {
                     queue.add(new Tuple<>(blockpos1, j + 1));
                  }
               } else if (material == Material.WATER_PLANT || material == Material.REPLACEABLE_WATER_PLANT) {
                  TileEntity tileentity = blockstate.hasTileEntity() ? pLevel.getBlockEntity(blockpos1) : null;
                  dropResources(blockstate, pLevel, blockpos1, tileentity);
                  pLevel.setBlock(blockpos1, Blocks.AIR.defaultBlockState(), 3);
                  ++i;
                  if (j < 6) {
                     queue.add(new Tuple<>(blockpos1, j + 1));
                  }
               }
            }
         }

         if (i > 64) {
            break;
         }
      }

      return i > 0;
   }
}
