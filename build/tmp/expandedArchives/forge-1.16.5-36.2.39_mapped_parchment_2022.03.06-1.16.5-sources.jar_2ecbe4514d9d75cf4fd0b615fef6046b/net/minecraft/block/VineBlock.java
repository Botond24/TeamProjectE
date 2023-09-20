package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.server.ServerWorld;

public class VineBlock extends Block implements net.minecraftforge.common.IForgeShearable {
   public static final BooleanProperty UP = SixWayBlock.UP;
   public static final BooleanProperty NORTH = SixWayBlock.NORTH;
   public static final BooleanProperty EAST = SixWayBlock.EAST;
   public static final BooleanProperty SOUTH = SixWayBlock.SOUTH;
   public static final BooleanProperty WEST = SixWayBlock.WEST;
   public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = SixWayBlock.PROPERTY_BY_DIRECTION.entrySet().stream().filter((p_199782_0_) -> {
      return p_199782_0_.getKey() != Direction.DOWN;
   }).collect(Util.toMap());
   private static final VoxelShape UP_AABB = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   private static final VoxelShape WEST_AABB = Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);
   private static final VoxelShape EAST_AABB = Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   private static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
   private static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
   private final Map<BlockState, VoxelShape> shapesCache;

   public VineBlock(AbstractBlock.Properties p_i48303_1_) {
      super(p_i48303_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(UP, Boolean.valueOf(false)).setValue(NORTH, Boolean.valueOf(false)).setValue(EAST, Boolean.valueOf(false)).setValue(SOUTH, Boolean.valueOf(false)).setValue(WEST, Boolean.valueOf(false)));
      this.shapesCache = ImmutableMap.copyOf(this.stateDefinition.getPossibleStates().stream().collect(Collectors.toMap(Function.identity(), VineBlock::calculateShape)));
   }

   private static VoxelShape calculateShape(BlockState p_242685_0_) {
      VoxelShape voxelshape = VoxelShapes.empty();
      if (p_242685_0_.getValue(UP)) {
         voxelshape = UP_AABB;
      }

      if (p_242685_0_.getValue(NORTH)) {
         voxelshape = VoxelShapes.or(voxelshape, NORTH_AABB);
      }

      if (p_242685_0_.getValue(SOUTH)) {
         voxelshape = VoxelShapes.or(voxelshape, SOUTH_AABB);
      }

      if (p_242685_0_.getValue(EAST)) {
         voxelshape = VoxelShapes.or(voxelshape, EAST_AABB);
      }

      if (p_242685_0_.getValue(WEST)) {
         voxelshape = VoxelShapes.or(voxelshape, WEST_AABB);
      }

      return voxelshape;
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return this.shapesCache.get(pState);
   }

   public boolean canSurvive(BlockState pState, IWorldReader pLevel, BlockPos pPos) {
      return this.hasFaces(this.getUpdatedState(pState, pLevel, pPos));
   }

   private boolean hasFaces(BlockState pState) {
      return this.countFaces(pState) > 0;
   }

   private int countFaces(BlockState pState) {
      int i = 0;

      for(BooleanProperty booleanproperty : PROPERTY_BY_DIRECTION.values()) {
         if (pState.getValue(booleanproperty)) {
            ++i;
         }
      }

      return i;
   }

   private boolean canSupportAtFace(IBlockReader pLevel, BlockPos pPos, Direction pDirection) {
      if (pDirection == Direction.DOWN) {
         return false;
      } else {
         BlockPos blockpos = pPos.relative(pDirection);
         if (isAcceptableNeighbour(pLevel, blockpos, pDirection)) {
            return true;
         } else if (pDirection.getAxis() == Direction.Axis.Y) {
            return false;
         } else {
            BooleanProperty booleanproperty = PROPERTY_BY_DIRECTION.get(pDirection);
            BlockState blockstate = pLevel.getBlockState(pPos.above());
            return blockstate.is(this) && blockstate.getValue(booleanproperty);
         }
      }
   }

   public static boolean isAcceptableNeighbour(IBlockReader pBlockReader, BlockPos pLevel, Direction pNeighborPos) {
      BlockState blockstate = pBlockReader.getBlockState(pLevel);
      return Block.isFaceFull(blockstate.getCollisionShape(pBlockReader, pLevel), pNeighborPos.getOpposite());
   }

   private BlockState getUpdatedState(BlockState p_196545_1_, IBlockReader p_196545_2_, BlockPos p_196545_3_) {
      BlockPos blockpos = p_196545_3_.above();
      if (p_196545_1_.getValue(UP)) {
         p_196545_1_ = p_196545_1_.setValue(UP, Boolean.valueOf(isAcceptableNeighbour(p_196545_2_, blockpos, Direction.DOWN)));
      }

      BlockState blockstate = null;

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BooleanProperty booleanproperty = getPropertyForFace(direction);
         if (p_196545_1_.getValue(booleanproperty)) {
            boolean flag = this.canSupportAtFace(p_196545_2_, p_196545_3_, direction);
            if (!flag) {
               if (blockstate == null) {
                  blockstate = p_196545_2_.getBlockState(blockpos);
               }

               flag = blockstate.is(this) && blockstate.getValue(booleanproperty);
            }

            p_196545_1_ = p_196545_1_.setValue(booleanproperty, Boolean.valueOf(flag));
         }
      }

      return p_196545_1_;
   }

   /**
    * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific face passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pFacing == Direction.DOWN) {
         return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
      } else {
         BlockState blockstate = this.getUpdatedState(pState, pLevel, pCurrentPos);
         return !this.hasFaces(blockstate) ? Blocks.AIR.defaultBlockState() : blockstate;
      }
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
      if (pLevel.random.nextInt(4) == 0 && pLevel.isAreaLoaded(pPos, 4)) { // Forge: check area to prevent loading unloaded chunks
         Direction direction = Direction.getRandom(pRandom);
         BlockPos blockpos = pPos.above();
         if (direction.getAxis().isHorizontal() && !pState.getValue(getPropertyForFace(direction))) {
            if (this.canSpread(pLevel, pPos)) {
               BlockPos blockpos4 = pPos.relative(direction);
               BlockState blockstate4 = pLevel.getBlockState(blockpos4);
               if (blockstate4.isAir(pLevel, blockpos4)) {
                  Direction direction3 = direction.getClockWise();
                  Direction direction4 = direction.getCounterClockWise();
                  boolean flag = pState.getValue(getPropertyForFace(direction3));
                  boolean flag1 = pState.getValue(getPropertyForFace(direction4));
                  BlockPos blockpos2 = blockpos4.relative(direction3);
                  BlockPos blockpos3 = blockpos4.relative(direction4);
                  if (flag && isAcceptableNeighbour(pLevel, blockpos2, direction3)) {
                     pLevel.setBlock(blockpos4, this.defaultBlockState().setValue(getPropertyForFace(direction3), Boolean.valueOf(true)), 2);
                  } else if (flag1 && isAcceptableNeighbour(pLevel, blockpos3, direction4)) {
                     pLevel.setBlock(blockpos4, this.defaultBlockState().setValue(getPropertyForFace(direction4), Boolean.valueOf(true)), 2);
                  } else {
                     Direction direction1 = direction.getOpposite();
                     if (flag && pLevel.isEmptyBlock(blockpos2) && isAcceptableNeighbour(pLevel, pPos.relative(direction3), direction1)) {
                        pLevel.setBlock(blockpos2, this.defaultBlockState().setValue(getPropertyForFace(direction1), Boolean.valueOf(true)), 2);
                     } else if (flag1 && pLevel.isEmptyBlock(blockpos3) && isAcceptableNeighbour(pLevel, pPos.relative(direction4), direction1)) {
                        pLevel.setBlock(blockpos3, this.defaultBlockState().setValue(getPropertyForFace(direction1), Boolean.valueOf(true)), 2);
                     } else if ((double)pLevel.random.nextFloat() < 0.05D && isAcceptableNeighbour(pLevel, blockpos4.above(), Direction.UP)) {
                        pLevel.setBlock(blockpos4, this.defaultBlockState().setValue(UP, Boolean.valueOf(true)), 2);
                     }
                  }
               } else if (isAcceptableNeighbour(pLevel, blockpos4, direction)) {
                  pLevel.setBlock(pPos, pState.setValue(getPropertyForFace(direction), Boolean.valueOf(true)), 2);
               }

            }
         } else {
            if (direction == Direction.UP && pPos.getY() < 255) {
               if (this.canSupportAtFace(pLevel, pPos, direction)) {
                  pLevel.setBlock(pPos, pState.setValue(UP, Boolean.valueOf(true)), 2);
                  return;
               }

               if (pLevel.isEmptyBlock(blockpos)) {
                  if (!this.canSpread(pLevel, pPos)) {
                     return;
                  }

                  BlockState blockstate3 = pState;

                  for(Direction direction2 : Direction.Plane.HORIZONTAL) {
                     if (pRandom.nextBoolean() || !isAcceptableNeighbour(pLevel, blockpos.relative(direction2), Direction.UP)) {
                        blockstate3 = blockstate3.setValue(getPropertyForFace(direction2), Boolean.valueOf(false));
                     }
                  }

                  if (this.hasHorizontalConnection(blockstate3)) {
                     pLevel.setBlock(blockpos, blockstate3, 2);
                  }

                  return;
               }
            }

            if (pPos.getY() > 0) {
               BlockPos blockpos1 = pPos.below();
               BlockState blockstate = pLevel.getBlockState(blockpos1);
               boolean isAir = blockstate.isAir(pLevel, blockpos1);
               if (isAir || blockstate.is(this)) {
                  BlockState blockstate1 = isAir ? this.defaultBlockState() : blockstate;
                  BlockState blockstate2 = this.copyRandomFaces(pState, blockstate1, pRandom);
                  if (blockstate1 != blockstate2 && this.hasHorizontalConnection(blockstate2)) {
                     pLevel.setBlock(blockpos1, blockstate2, 2);
                  }
               }
            }

         }
      }
   }

   private BlockState copyRandomFaces(BlockState p_196544_1_, BlockState p_196544_2_, Random pRandom) {
      for(Direction direction : Direction.Plane.HORIZONTAL) {
         if (pRandom.nextBoolean()) {
            BooleanProperty booleanproperty = getPropertyForFace(direction);
            if (p_196544_1_.getValue(booleanproperty)) {
               p_196544_2_ = p_196544_2_.setValue(booleanproperty, Boolean.valueOf(true));
            }
         }
      }

      return p_196544_2_;
   }

   private boolean hasHorizontalConnection(BlockState pState) {
      return pState.getValue(NORTH) || pState.getValue(EAST) || pState.getValue(SOUTH) || pState.getValue(WEST);
   }

   private boolean canSpread(IBlockReader pBlockReader, BlockPos pPos) {
      int i = 4;
      Iterable<BlockPos> iterable = BlockPos.betweenClosed(pPos.getX() - 4, pPos.getY() - 1, pPos.getZ() - 4, pPos.getX() + 4, pPos.getY() + 1, pPos.getZ() + 4);
      int j = 5;

      for(BlockPos blockpos : iterable) {
         if (pBlockReader.getBlockState(blockpos).is(this)) {
            --j;
            if (j <= 0) {
               return false;
            }
         }
      }

      return true;
   }

   public boolean canBeReplaced(BlockState pState, BlockItemUseContext pUseContext) {
      BlockState blockstate = pUseContext.getLevel().getBlockState(pUseContext.getClickedPos());
      if (blockstate.is(this)) {
         return this.countFaces(blockstate) < PROPERTY_BY_DIRECTION.size();
      } else {
         return super.canBeReplaced(pState, pUseContext);
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      BlockState blockstate = pContext.getLevel().getBlockState(pContext.getClickedPos());
      boolean flag = blockstate.is(this);
      BlockState blockstate1 = flag ? blockstate : this.defaultBlockState();

      for(Direction direction : pContext.getNearestLookingDirections()) {
         if (direction != Direction.DOWN) {
            BooleanProperty booleanproperty = getPropertyForFace(direction);
            boolean flag1 = flag && blockstate.getValue(booleanproperty);
            if (!flag1 && this.canSupportAtFace(pContext.getLevel(), pContext.getClickedPos(), direction)) {
               return blockstate1.setValue(booleanproperty, Boolean.valueOf(true));
            }
         }
      }

      return flag ? blockstate1 : null;
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(UP, NORTH, EAST, SOUTH, WEST);
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link IBlockState#withRotation(Rotation)} whenever possible. Implementing/overriding is
    * fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRotation) {
      switch(pRotation) {
      case CLOCKWISE_180:
         return pState.setValue(NORTH, pState.getValue(SOUTH)).setValue(EAST, pState.getValue(WEST)).setValue(SOUTH, pState.getValue(NORTH)).setValue(WEST, pState.getValue(EAST));
      case COUNTERCLOCKWISE_90:
         return pState.setValue(NORTH, pState.getValue(EAST)).setValue(EAST, pState.getValue(SOUTH)).setValue(SOUTH, pState.getValue(WEST)).setValue(WEST, pState.getValue(NORTH));
      case CLOCKWISE_90:
         return pState.setValue(NORTH, pState.getValue(WEST)).setValue(EAST, pState.getValue(NORTH)).setValue(SOUTH, pState.getValue(EAST)).setValue(WEST, pState.getValue(SOUTH));
      default:
         return pState;
      }
   }

   /**
    * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link IBlockState#withMirror(Mirror)} whenever possible. Implementing/overriding is fine.
    */
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      switch(pMirror) {
      case LEFT_RIGHT:
         return pState.setValue(NORTH, pState.getValue(SOUTH)).setValue(SOUTH, pState.getValue(NORTH));
      case FRONT_BACK:
         return pState.setValue(EAST, pState.getValue(WEST)).setValue(WEST, pState.getValue(EAST));
      default:
         return super.mirror(pState, pMirror);
      }
   }

   public static BooleanProperty getPropertyForFace(Direction pFace) {
      return PROPERTY_BY_DIRECTION.get(pFace);
   }
}
