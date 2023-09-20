package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class ChorusFlowerBlock extends Block {
   public static final IntegerProperty AGE = BlockStateProperties.AGE_5;
   private final ChorusPlantBlock plant;

   public ChorusFlowerBlock(ChorusPlantBlock pPlant, AbstractBlock.Properties pProperties) {
      super(pProperties);
      this.plant = pPlant;
      this.registerDefaultState(this.stateDefinition.any().setValue(AGE, Integer.valueOf(0)));
   }

   public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
      if (!pState.canSurvive(pLevel, pPos)) {
         pLevel.destroyBlock(pPos, true);
      }

   }

   /**
    * Returns whether or not this block is of a type that needs random ticking. Called for ref-counting purposes by
    * ExtendedBlockStorage in order to broadly cull a chunk from the random chunk update list for efficiency's sake.
    */
   public boolean isRandomlyTicking(BlockState pState) {
      return pState.getValue(AGE) < 5;
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
      BlockPos blockpos = pPos.above();
      if (pLevel.isEmptyBlock(blockpos) && blockpos.getY() < 256) {
         int i = pState.getValue(AGE);
         if (i < 5 && net.minecraftforge.common.ForgeHooks.onCropsGrowPre(pLevel, blockpos, pState, true)) {
            boolean flag = false;
            boolean flag1 = false;
            BlockState blockstate = pLevel.getBlockState(pPos.below());
            Block block = blockstate.getBlock();
            if (block == Blocks.END_STONE) {
               flag = true;
            } else if (block == this.plant) {
               int j = 1;

               for(int k = 0; k < 4; ++k) {
                  Block block1 = pLevel.getBlockState(pPos.below(j + 1)).getBlock();
                  if (block1 != this.plant) {
                     if (block1 == Blocks.END_STONE) {
                        flag1 = true;
                     }
                     break;
                  }

                  ++j;
               }

               if (j < 2 || j <= pRandom.nextInt(flag1 ? 5 : 4)) {
                  flag = true;
               }
            } else if (blockstate.isAir(pLevel, pPos.below())) {
               flag = true;
            }

            if (flag && allNeighborsEmpty(pLevel, blockpos, (Direction)null) && pLevel.isEmptyBlock(pPos.above(2))) {
               pLevel.setBlock(pPos, this.plant.getStateForPlacement(pLevel, pPos), 2);
               this.placeGrownFlower(pLevel, blockpos, i);
            } else if (i < 4) {
               int l = pRandom.nextInt(4);
               if (flag1) {
                  ++l;
               }

               boolean flag2 = false;

               for(int i1 = 0; i1 < l; ++i1) {
                  Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(pRandom);
                  BlockPos blockpos1 = pPos.relative(direction);
                  if (pLevel.isEmptyBlock(blockpos1) && pLevel.isEmptyBlock(blockpos1.below()) && allNeighborsEmpty(pLevel, blockpos1, direction.getOpposite())) {
                     this.placeGrownFlower(pLevel, blockpos1, i + 1);
                     flag2 = true;
                  }
               }

               if (flag2) {
                  pLevel.setBlock(pPos, this.plant.getStateForPlacement(pLevel, pPos), 2);
               } else {
                  this.placeDeadFlower(pLevel, pPos);
               }
            } else {
               this.placeDeadFlower(pLevel, pPos);
            }
            net.minecraftforge.common.ForgeHooks.onCropsGrowPost(pLevel, pPos, pState);
         }
      }
   }

   private void placeGrownFlower(World pLevel, BlockPos pPos, int pAge) {
      pLevel.setBlock(pPos, this.defaultBlockState().setValue(AGE, Integer.valueOf(pAge)), 2);
      pLevel.levelEvent(1033, pPos, 0);
   }

   private void placeDeadFlower(World pLevel, BlockPos pPos) {
      pLevel.setBlock(pPos, this.defaultBlockState().setValue(AGE, Integer.valueOf(5)), 2);
      pLevel.levelEvent(1034, pPos, 0);
   }

   private static boolean allNeighborsEmpty(IWorldReader pLevel, BlockPos pPos, @Nullable Direction pExcludingSide) {
      for(Direction direction : Direction.Plane.HORIZONTAL) {
         if (direction != pExcludingSide && !pLevel.isEmptyBlock(pPos.relative(direction))) {
            return false;
         }
      }

      return true;
   }

   /**
    * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific face passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pFacing != Direction.UP && !pState.canSurvive(pLevel, pCurrentPos)) {
         pLevel.getBlockTicks().scheduleTick(pCurrentPos, this, 1);
      }

      return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public boolean canSurvive(BlockState pState, IWorldReader pLevel, BlockPos pPos) {
      BlockState blockstate = pLevel.getBlockState(pPos.below());
      if (blockstate.getBlock() != this.plant && !blockstate.is(Blocks.END_STONE)) {
         if (!blockstate.isAir(pLevel, pPos.below())) {
            return false;
         } else {
            boolean flag = false;

            for(Direction direction : Direction.Plane.HORIZONTAL) {
               BlockState blockstate1 = pLevel.getBlockState(pPos.relative(direction));
               if (blockstate1.is(this.plant)) {
                  if (flag) {
                     return false;
                  }

                  flag = true;
               } else if (!blockstate1.isAir(pLevel, pPos.relative(direction))) {
                  return false;
               }
            }

            return flag;
         }
      } else {
         return true;
      }
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(AGE);
   }

   public static void generatePlant(IWorld pLevel, BlockPos pPos, Random pRand, int pMaxHorizontalDistance) {
      pLevel.setBlock(pPos, ((ChorusPlantBlock)Blocks.CHORUS_PLANT).getStateForPlacement(pLevel, pPos), 2);
      growTreeRecursive(pLevel, pPos, pRand, pPos, pMaxHorizontalDistance, 0);
   }

   private static void growTreeRecursive(IWorld pLevel, BlockPos pBranchPos, Random pRand, BlockPos pOriginalBranchPos, int pMaxHorizontalDistance, int pIterations) {
      ChorusPlantBlock chorusplantblock = (ChorusPlantBlock)Blocks.CHORUS_PLANT;
      int i = pRand.nextInt(4) + 1;
      if (pIterations == 0) {
         ++i;
      }

      for(int j = 0; j < i; ++j) {
         BlockPos blockpos = pBranchPos.above(j + 1);
         if (!allNeighborsEmpty(pLevel, blockpos, (Direction)null)) {
            return;
         }

         pLevel.setBlock(blockpos, chorusplantblock.getStateForPlacement(pLevel, blockpos), 2);
         pLevel.setBlock(blockpos.below(), chorusplantblock.getStateForPlacement(pLevel, blockpos.below()), 2);
      }

      boolean flag = false;
      if (pIterations < 4) {
         int l = pRand.nextInt(4);
         if (pIterations == 0) {
            ++l;
         }

         for(int k = 0; k < l; ++k) {
            Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(pRand);
            BlockPos blockpos1 = pBranchPos.above(i).relative(direction);
            if (Math.abs(blockpos1.getX() - pOriginalBranchPos.getX()) < pMaxHorizontalDistance && Math.abs(blockpos1.getZ() - pOriginalBranchPos.getZ()) < pMaxHorizontalDistance && pLevel.isEmptyBlock(blockpos1) && pLevel.isEmptyBlock(blockpos1.below()) && allNeighborsEmpty(pLevel, blockpos1, direction.getOpposite())) {
               flag = true;
               pLevel.setBlock(blockpos1, chorusplantblock.getStateForPlacement(pLevel, blockpos1), 2);
               pLevel.setBlock(blockpos1.relative(direction.getOpposite()), chorusplantblock.getStateForPlacement(pLevel, blockpos1.relative(direction.getOpposite())), 2);
               growTreeRecursive(pLevel, blockpos1, pRand, pOriginalBranchPos, pMaxHorizontalDistance, pIterations + 1);
            }
         }
      }

      if (!flag) {
         pLevel.setBlock(pBranchPos.above(i), Blocks.CHORUS_FLOWER.defaultBlockState().setValue(AGE, Integer.valueOf(5)), 2);
      }

   }

   public void onProjectileHit(World pLevel, BlockState pState, BlockRayTraceResult pHit, ProjectileEntity pProjectile) {
      if (pProjectile.getType().is(EntityTypeTags.IMPACT_PROJECTILES)) {
         BlockPos blockpos = pHit.getBlockPos();
         pLevel.destroyBlock(blockpos, true, pProjectile);
      }

   }
}
