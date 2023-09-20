package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.RedstoneSide;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RedstoneWireBlock extends Block {
   public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.NORTH_REDSTONE;
   public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.EAST_REDSTONE;
   public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.SOUTH_REDSTONE;
   public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.WEST_REDSTONE;
   public static final IntegerProperty POWER = BlockStateProperties.POWER;
   public static final Map<Direction, EnumProperty<RedstoneSide>> PROPERTY_BY_DIRECTION = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST));
   private static final VoxelShape SHAPE_DOT = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D);
   private static final Map<Direction, VoxelShape> SHAPES_FLOOR = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.box(3.0D, 0.0D, 0.0D, 13.0D, 1.0D, 13.0D), Direction.SOUTH, Block.box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 16.0D), Direction.EAST, Block.box(3.0D, 0.0D, 3.0D, 16.0D, 1.0D, 13.0D), Direction.WEST, Block.box(0.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D)));
   private static final Map<Direction, VoxelShape> SHAPES_UP = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, VoxelShapes.or(SHAPES_FLOOR.get(Direction.NORTH), Block.box(3.0D, 0.0D, 0.0D, 13.0D, 16.0D, 1.0D)), Direction.SOUTH, VoxelShapes.or(SHAPES_FLOOR.get(Direction.SOUTH), Block.box(3.0D, 0.0D, 15.0D, 13.0D, 16.0D, 16.0D)), Direction.EAST, VoxelShapes.or(SHAPES_FLOOR.get(Direction.EAST), Block.box(15.0D, 0.0D, 3.0D, 16.0D, 16.0D, 13.0D)), Direction.WEST, VoxelShapes.or(SHAPES_FLOOR.get(Direction.WEST), Block.box(0.0D, 0.0D, 3.0D, 1.0D, 16.0D, 13.0D))));
   private final Map<BlockState, VoxelShape> SHAPES_CACHE = Maps.newHashMap();
   private static final Vector3f[] COLORS = new Vector3f[16];
   private final BlockState crossState;
   private boolean shouldSignal = true;

   public RedstoneWireBlock(AbstractBlock.Properties p_i48344_1_) {
      super(p_i48344_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, RedstoneSide.NONE).setValue(EAST, RedstoneSide.NONE).setValue(SOUTH, RedstoneSide.NONE).setValue(WEST, RedstoneSide.NONE).setValue(POWER, Integer.valueOf(0)));
      this.crossState = this.defaultBlockState().setValue(NORTH, RedstoneSide.SIDE).setValue(EAST, RedstoneSide.SIDE).setValue(SOUTH, RedstoneSide.SIDE).setValue(WEST, RedstoneSide.SIDE);

      for(BlockState blockstate : this.getStateDefinition().getPossibleStates()) {
         if (blockstate.getValue(POWER) == 0) {
            this.SHAPES_CACHE.put(blockstate, this.calculateShape(blockstate));
         }
      }

   }

   private VoxelShape calculateShape(BlockState pState) {
      VoxelShape voxelshape = SHAPE_DOT;

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         RedstoneSide redstoneside = pState.getValue(PROPERTY_BY_DIRECTION.get(direction));
         if (redstoneside == RedstoneSide.SIDE) {
            voxelshape = VoxelShapes.or(voxelshape, SHAPES_FLOOR.get(direction));
         } else if (redstoneside == RedstoneSide.UP) {
            voxelshape = VoxelShapes.or(voxelshape, SHAPES_UP.get(direction));
         }
      }

      return voxelshape;
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return this.SHAPES_CACHE.get(pState.setValue(POWER, Integer.valueOf(0)));
   }

   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      return this.getConnectionState(pContext.getLevel(), this.crossState, pContext.getClickedPos());
   }

   private BlockState getConnectionState(IBlockReader pLevel, BlockState pState, BlockPos pPos) {
      boolean flag = isDot(pState);
      pState = this.getMissingConnections(pLevel, this.defaultBlockState().setValue(POWER, pState.getValue(POWER)), pPos);
      if (flag && isDot(pState)) {
         return pState;
      } else {
         boolean flag1 = pState.getValue(NORTH).isConnected();
         boolean flag2 = pState.getValue(SOUTH).isConnected();
         boolean flag3 = pState.getValue(EAST).isConnected();
         boolean flag4 = pState.getValue(WEST).isConnected();
         boolean flag5 = !flag1 && !flag2;
         boolean flag6 = !flag3 && !flag4;
         if (!flag4 && flag5) {
            pState = pState.setValue(WEST, RedstoneSide.SIDE);
         }

         if (!flag3 && flag5) {
            pState = pState.setValue(EAST, RedstoneSide.SIDE);
         }

         if (!flag1 && flag6) {
            pState = pState.setValue(NORTH, RedstoneSide.SIDE);
         }

         if (!flag2 && flag6) {
            pState = pState.setValue(SOUTH, RedstoneSide.SIDE);
         }

         return pState;
      }
   }

   private BlockState getMissingConnections(IBlockReader pLevel, BlockState pState, BlockPos pPos) {
      boolean flag = !pLevel.getBlockState(pPos.above()).isRedstoneConductor(pLevel, pPos);

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         if (!pState.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected()) {
            RedstoneSide redstoneside = this.getConnectingSide(pLevel, pPos, direction, flag);
            pState = pState.setValue(PROPERTY_BY_DIRECTION.get(direction), redstoneside);
         }
      }

      return pState;
   }

   /**
    * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific face passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pFacing == Direction.DOWN) {
         return pState;
      } else if (pFacing == Direction.UP) {
         return this.getConnectionState(pLevel, pState, pCurrentPos);
      } else {
         RedstoneSide redstoneside = this.getConnectingSide(pLevel, pCurrentPos, pFacing);
         return redstoneside.isConnected() == pState.getValue(PROPERTY_BY_DIRECTION.get(pFacing)).isConnected() && !isCross(pState) ? pState.setValue(PROPERTY_BY_DIRECTION.get(pFacing), redstoneside) : this.getConnectionState(pLevel, this.crossState.setValue(POWER, pState.getValue(POWER)).setValue(PROPERTY_BY_DIRECTION.get(pFacing), redstoneside), pCurrentPos);
      }
   }

   private static boolean isCross(BlockState pState) {
      return pState.getValue(NORTH).isConnected() && pState.getValue(SOUTH).isConnected() && pState.getValue(EAST).isConnected() && pState.getValue(WEST).isConnected();
   }

   private static boolean isDot(BlockState pState) {
      return !pState.getValue(NORTH).isConnected() && !pState.getValue(SOUTH).isConnected() && !pState.getValue(EAST).isConnected() && !pState.getValue(WEST).isConnected();
   }

   /**
    * performs updates on diagonal neighbors of the target position and passes in the flags. The flags can be referenced
    * from the docs for {@link IWorldWriter#setBlockState(IBlockState, BlockPos, int)}.
    */
   public void updateIndirectNeighbourShapes(BlockState pState, IWorld pLevel, BlockPos pPos, int pFlags, int pRecursionLeft) {
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         RedstoneSide redstoneside = pState.getValue(PROPERTY_BY_DIRECTION.get(direction));
         if (redstoneside != RedstoneSide.NONE && !pLevel.getBlockState(blockpos$mutable.setWithOffset(pPos, direction)).is(this)) {
            blockpos$mutable.move(Direction.DOWN);
            BlockState blockstate = pLevel.getBlockState(blockpos$mutable);
            if (!blockstate.is(Blocks.OBSERVER)) {
               BlockPos blockpos = blockpos$mutable.relative(direction.getOpposite());
               BlockState blockstate1 = blockstate.updateShape(direction.getOpposite(), pLevel.getBlockState(blockpos), pLevel, blockpos$mutable, blockpos);
               updateOrDestroy(blockstate, blockstate1, pLevel, blockpos$mutable, pFlags, pRecursionLeft);
            }

            blockpos$mutable.setWithOffset(pPos, direction).move(Direction.UP);
            BlockState blockstate3 = pLevel.getBlockState(blockpos$mutable);
            if (!blockstate3.is(Blocks.OBSERVER)) {
               BlockPos blockpos1 = blockpos$mutable.relative(direction.getOpposite());
               BlockState blockstate2 = blockstate3.updateShape(direction.getOpposite(), pLevel.getBlockState(blockpos1), pLevel, blockpos$mutable, blockpos1);
               updateOrDestroy(blockstate3, blockstate2, pLevel, blockpos$mutable, pFlags, pRecursionLeft);
            }
         }
      }

   }

   private RedstoneSide getConnectingSide(IBlockReader pLevel, BlockPos pPos, Direction pFace) {
      return this.getConnectingSide(pLevel, pPos, pFace, !pLevel.getBlockState(pPos.above()).isRedstoneConductor(pLevel, pPos));
   }

   private RedstoneSide getConnectingSide(IBlockReader pLevel, BlockPos pPos, Direction pDirection, boolean pNonNormalCubeAbove) {
      BlockPos blockpos = pPos.relative(pDirection);
      BlockState blockstate = pLevel.getBlockState(blockpos);
      if (pNonNormalCubeAbove) {
         boolean flag = this.canSurviveOn(pLevel, blockpos, blockstate);
         if (flag && canConnectTo(pLevel.getBlockState(blockpos.above()), pLevel, blockpos.above(), null) ) {
            if (blockstate.isFaceSturdy(pLevel, blockpos, pDirection.getOpposite())) {
               return RedstoneSide.UP;
            }

            return RedstoneSide.SIDE;
         }
      }

      return !canConnectTo(blockstate, pLevel, blockpos, pDirection) && (blockstate.isRedstoneConductor(pLevel, blockpos) || !canConnectTo(pLevel.getBlockState(blockpos.below()), pLevel, blockpos.below(), null)) ? RedstoneSide.NONE : RedstoneSide.SIDE;
   }

   public boolean canSurvive(BlockState pState, IWorldReader pLevel, BlockPos pPos) {
      BlockPos blockpos = pPos.below();
      BlockState blockstate = pLevel.getBlockState(blockpos);
      return this.canSurviveOn(pLevel, blockpos, blockstate);
   }

   private boolean canSurviveOn(IBlockReader pReader, BlockPos pPos, BlockState pState) {
      return pState.isFaceSturdy(pReader, pPos, Direction.UP) || pState.is(Blocks.HOPPER);
   }

   private void updatePowerStrength(World pLevel, BlockPos pPos, BlockState pState) {
      int i = this.calculateTargetStrength(pLevel, pPos);
      if (pState.getValue(POWER) != i) {
         if (pLevel.getBlockState(pPos) == pState) {
            pLevel.setBlock(pPos, pState.setValue(POWER, Integer.valueOf(i)), 2);
         }

         Set<BlockPos> set = Sets.newHashSet();
         set.add(pPos);

         for(Direction direction : Direction.values()) {
            set.add(pPos.relative(direction));
         }

         for(BlockPos blockpos : set) {
            pLevel.updateNeighborsAt(blockpos, this);
         }
      }

   }

   private int calculateTargetStrength(World pLevel, BlockPos pPos) {
      this.shouldSignal = false;
      int i = pLevel.getBestNeighborSignal(pPos);
      this.shouldSignal = true;
      int j = 0;
      if (i < 15) {
         for(Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockpos = pPos.relative(direction);
            BlockState blockstate = pLevel.getBlockState(blockpos);
            j = Math.max(j, this.getWireSignal(blockstate));
            BlockPos blockpos1 = pPos.above();
            if (blockstate.isRedstoneConductor(pLevel, blockpos) && !pLevel.getBlockState(blockpos1).isRedstoneConductor(pLevel, blockpos1)) {
               j = Math.max(j, this.getWireSignal(pLevel.getBlockState(blockpos.above())));
            } else if (!blockstate.isRedstoneConductor(pLevel, blockpos)) {
               j = Math.max(j, this.getWireSignal(pLevel.getBlockState(blockpos.below())));
            }
         }
      }

      return Math.max(i, j - 1);
   }

   private int getWireSignal(BlockState pState) {
      return pState.is(this) ? pState.getValue(POWER) : 0;
   }

   /**
    * Calls {@link net.minecraft.world.level.Level#updateNeighborsAt} for all neighboring blocks, but only if the given
    * block is a redstone wire.
    */
   private void checkCornerChangeAt(World pLevel, BlockPos pPos) {
      if (pLevel.getBlockState(pPos).is(this)) {
         pLevel.updateNeighborsAt(pPos, this);

         for(Direction direction : Direction.values()) {
            pLevel.updateNeighborsAt(pPos.relative(direction), this);
         }

      }
   }

   public void onPlace(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (!pOldState.is(pState.getBlock()) && !pLevel.isClientSide) {
         this.updatePowerStrength(pLevel, pPos, pState);

         for(Direction direction : Direction.Plane.VERTICAL) {
            pLevel.updateNeighborsAt(pPos.relative(direction), this);
         }

         this.updateNeighborsOfNeighboringWires(pLevel, pPos);
      }
   }

   public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pIsMoving && !pState.is(pNewState.getBlock())) {
         super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
         if (!pLevel.isClientSide) {
            for(Direction direction : Direction.values()) {
               pLevel.updateNeighborsAt(pPos.relative(direction), this);
            }

            this.updatePowerStrength(pLevel, pPos, pState);
            this.updateNeighborsOfNeighboringWires(pLevel, pPos);
         }
      }
   }

   private void updateNeighborsOfNeighboringWires(World pLevel, BlockPos pPos) {
      for(Direction direction : Direction.Plane.HORIZONTAL) {
         this.checkCornerChangeAt(pLevel, pPos.relative(direction));
      }

      for(Direction direction1 : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos = pPos.relative(direction1);
         if (pLevel.getBlockState(blockpos).isRedstoneConductor(pLevel, blockpos)) {
            this.checkCornerChangeAt(pLevel, blockpos.above());
         } else {
            this.checkCornerChangeAt(pLevel, blockpos.below());
         }
      }

   }

   public void neighborChanged(BlockState pState, World pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      if (!pLevel.isClientSide) {
         if (pState.canSurvive(pLevel, pPos)) {
            this.updatePowerStrength(pLevel, pPos, pState);
         } else {
            dropResources(pState, pLevel, pPos);
            pLevel.removeBlock(pPos, false);
         }

      }
   }

   /**
    * @deprecated call via {@link IBlockState#getStrongPower(IBlockAccess,BlockPos,EnumFacing)} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getDirectSignal(BlockState pBlockState, IBlockReader pBlockAccess, BlockPos pPos, Direction pSide) {
      return !this.shouldSignal ? 0 : pBlockState.getSignal(pBlockAccess, pPos, pSide);
   }

   /**
    * @deprecated call via {@link IBlockState#getWeakPower(IBlockAccess,BlockPos,EnumFacing)} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getSignal(BlockState pBlockState, IBlockReader pBlockAccess, BlockPos pPos, Direction pSide) {
      if (this.shouldSignal && pSide != Direction.DOWN) {
         int i = pBlockState.getValue(POWER);
         if (i == 0) {
            return 0;
         } else {
            return pSide != Direction.UP && !this.getConnectionState(pBlockAccess, pBlockState, pPos).getValue(PROPERTY_BY_DIRECTION.get(pSide.getOpposite())).isConnected() ? 0 : i;
         }
      } else {
         return 0;
      }
   }

   protected static boolean canConnectTo(BlockState pState, IBlockReader world, BlockPos pos, @Nullable Direction pDirection) {
      if (pState.is(Blocks.REDSTONE_WIRE)) {
         return true;
      } else if (pState.is(Blocks.REPEATER)) {
         Direction direction = pState.getValue(RepeaterBlock.FACING);
         return direction == pDirection || direction.getOpposite() == pDirection;
      } else if (pState.is(Blocks.OBSERVER)) {
         return pDirection == pState.getValue(ObserverBlock.FACING);
      } else {
         return pState.canConnectRedstone(world, pos, pDirection) && pDirection != null;
      }
   }

   /**
    * Can this block provide power. Only wire currently seems to have this change based on its state.
    * @deprecated call via {@link IBlockState#canProvidePower()} whenever possible. Implementing/overriding is fine.
    */
   public boolean isSignalSource(BlockState pState) {
      return this.shouldSignal;
   }

   @OnlyIn(Dist.CLIENT)
   public static int getColorForPower(int pPower) {
      Vector3f vector3f = COLORS[pPower];
      return MathHelper.color(vector3f.x(), vector3f.y(), vector3f.z());
   }

   @OnlyIn(Dist.CLIENT)
   private void spawnParticlesAlongLine(World p_235549_1_, Random p_235549_2_, BlockPos p_235549_3_, Vector3f p_235549_4_, Direction p_235549_5_, Direction p_235549_6_, float p_235549_7_, float p_235549_8_) {
      float f = p_235549_8_ - p_235549_7_;
      if (!(p_235549_2_.nextFloat() >= 0.2F * f)) {
         float f1 = 0.4375F;
         float f2 = p_235549_7_ + f * p_235549_2_.nextFloat();
         double d0 = 0.5D + (double)(0.4375F * (float)p_235549_5_.getStepX()) + (double)(f2 * (float)p_235549_6_.getStepX());
         double d1 = 0.5D + (double)(0.4375F * (float)p_235549_5_.getStepY()) + (double)(f2 * (float)p_235549_6_.getStepY());
         double d2 = 0.5D + (double)(0.4375F * (float)p_235549_5_.getStepZ()) + (double)(f2 * (float)p_235549_6_.getStepZ());
         p_235549_1_.addParticle(new RedstoneParticleData(p_235549_4_.x(), p_235549_4_.y(), p_235549_4_.z(), 1.0F), (double)p_235549_3_.getX() + d0, (double)p_235549_3_.getY() + d1, (double)p_235549_3_.getZ() + d2, 0.0D, 0.0D, 0.0D);
      }
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
    * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
    * of whether the block can receive random update ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
      int i = pState.getValue(POWER);
      if (i != 0) {
         for(Direction direction : Direction.Plane.HORIZONTAL) {
            RedstoneSide redstoneside = pState.getValue(PROPERTY_BY_DIRECTION.get(direction));
            switch(redstoneside) {
            case UP:
               this.spawnParticlesAlongLine(pLevel, pRand, pPos, COLORS[i], direction, Direction.UP, -0.5F, 0.5F);
            case SIDE:
               this.spawnParticlesAlongLine(pLevel, pRand, pPos, COLORS[i], Direction.DOWN, direction, 0.0F, 0.5F);
               break;
            case NONE:
            default:
               this.spawnParticlesAlongLine(pLevel, pRand, pPos, COLORS[i], Direction.DOWN, direction, 0.0F, 0.3F);
            }
         }

      }
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

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(NORTH, EAST, SOUTH, WEST, POWER);
   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      if (!pPlayer.abilities.mayBuild) {
         return ActionResultType.PASS;
      } else {
         if (isCross(pState) || isDot(pState)) {
            BlockState blockstate = isCross(pState) ? this.defaultBlockState() : this.crossState;
            blockstate = blockstate.setValue(POWER, pState.getValue(POWER));
            blockstate = this.getConnectionState(pLevel, blockstate, pPos);
            if (blockstate != pState) {
               pLevel.setBlock(pPos, blockstate, 3);
               this.updatesOnShapeChange(pLevel, pPos, pState, blockstate);
               return ActionResultType.SUCCESS;
            }
         }

         return ActionResultType.PASS;
      }
   }

   private void updatesOnShapeChange(World pLevel, BlockPos pPos, BlockState pOldState, BlockState pNewState) {
      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos = pPos.relative(direction);
         if (pOldState.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected() != pNewState.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected() && pLevel.getBlockState(blockpos).isRedstoneConductor(pLevel, blockpos)) {
            pLevel.updateNeighborsAtExceptFromFacing(blockpos, pNewState.getBlock(), direction.getOpposite());
         }
      }

   }

   static {
      for(int i = 0; i <= 15; ++i) {
         float f = (float)i / 15.0F;
         float f1 = f * 0.6F + (f > 0.0F ? 0.4F : 0.3F);
         float f2 = MathHelper.clamp(f * f * 0.7F - 0.5F, 0.0F, 1.0F);
         float f3 = MathHelper.clamp(f * f * 0.6F - 0.7F, 0.0F, 1.0F);
         COLORS[i] = new Vector3f(f1, f2, f3);
      }

   }
}
