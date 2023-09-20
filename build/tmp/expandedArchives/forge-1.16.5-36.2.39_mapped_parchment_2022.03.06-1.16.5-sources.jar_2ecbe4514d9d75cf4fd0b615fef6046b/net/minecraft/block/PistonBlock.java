package net.minecraft.block;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.PistonType;
import net.minecraft.tileentity.PistonTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class PistonBlock extends DirectionalBlock {
   public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED;
   protected static final VoxelShape EAST_AABB = Block.box(0.0D, 0.0D, 0.0D, 12.0D, 16.0D, 16.0D);
   protected static final VoxelShape WEST_AABB = Block.box(4.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   protected static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 12.0D);
   protected static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 4.0D, 16.0D, 16.0D, 16.0D);
   protected static final VoxelShape UP_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);
   protected static final VoxelShape DOWN_AABB = Block.box(0.0D, 4.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   /** Whether this is a sticky piston */
   private final boolean isSticky;

   public PistonBlock(boolean pIsSticky, AbstractBlock.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(EXTENDED, Boolean.valueOf(false)));
      this.isSticky = pIsSticky;
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      if (pState.getValue(EXTENDED)) {
         switch((Direction)pState.getValue(FACING)) {
         case DOWN:
            return DOWN_AABB;
         case UP:
         default:
            return UP_AABB;
         case NORTH:
            return NORTH_AABB;
         case SOUTH:
            return SOUTH_AABB;
         case WEST:
            return WEST_AABB;
         case EAST:
            return EAST_AABB;
         }
      } else {
         return VoxelShapes.block();
      }
   }

   /**
    * Called by ItemBlocks after a block is set in the world, to allow post-place logic
    */
   public void setPlacedBy(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
      if (!pLevel.isClientSide) {
         this.checkIfExtend(pLevel, pPos, pState);
      }

   }

   public void neighborChanged(BlockState pState, World pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      if (!pLevel.isClientSide) {
         this.checkIfExtend(pLevel, pPos, pState);
      }

   }

   public void onPlace(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (!pOldState.is(pState.getBlock())) {
         if (!pLevel.isClientSide && pLevel.getBlockEntity(pPos) == null) {
            this.checkIfExtend(pLevel, pPos, pState);
         }

      }
   }

   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      return this.defaultBlockState().setValue(FACING, pContext.getNearestLookingDirection().getOpposite()).setValue(EXTENDED, Boolean.valueOf(false));
   }

   private void checkIfExtend(World pLevel, BlockPos pPos, BlockState pState) {
      Direction direction = pState.getValue(FACING);
      boolean flag = this.getNeighborSignal(pLevel, pPos, direction);
      if (flag && !pState.getValue(EXTENDED)) {
         if ((new PistonBlockStructureHelper(pLevel, pPos, direction, true)).resolve()) {
            pLevel.blockEvent(pPos, this, 0, direction.get3DDataValue());
         }
      } else if (!flag && pState.getValue(EXTENDED)) {
         BlockPos blockpos = pPos.relative(direction, 2);
         BlockState blockstate = pLevel.getBlockState(blockpos);
         int i = 1;
         if (blockstate.is(Blocks.MOVING_PISTON) && blockstate.getValue(FACING) == direction) {
            TileEntity tileentity = pLevel.getBlockEntity(blockpos);
            if (tileentity instanceof PistonTileEntity) {
               PistonTileEntity pistontileentity = (PistonTileEntity)tileentity;
               if (pistontileentity.isExtending() && (pistontileentity.getProgress(0.0F) < 0.5F || pLevel.getGameTime() == pistontileentity.getLastTicked() || ((ServerWorld)pLevel).isHandlingTick())) {
                  i = 2;
               }
            }
         }

         pLevel.blockEvent(pPos, this, i, direction.get3DDataValue());
      }

   }

   private boolean getNeighborSignal(World pLevel, BlockPos pPos, Direction pFacing) {
      for(Direction direction : Direction.values()) {
         if (direction != pFacing && pLevel.hasSignal(pPos.relative(direction), direction)) {
            return true;
         }
      }

      if (pLevel.hasSignal(pPos, Direction.DOWN)) {
         return true;
      } else {
         BlockPos blockpos = pPos.above();

         for(Direction direction1 : Direction.values()) {
            if (direction1 != Direction.DOWN && pLevel.hasSignal(blockpos.relative(direction1), direction1)) {
               return true;
            }
         }

         return false;
      }
   }

   /**
    * Called on server when World#addBlockEvent is called. If server returns true, then also called on the client. On
    * the Server, this may perform additional changes to the world, like pistons replacing the block with an extended
    * base. On the client, the update may involve replacing tile entities or effects such as sounds or particles
    * @deprecated call via {@link IBlockState#onBlockEventReceived(World,BlockPos,int,int)} whenever possible.
    * Implementing/overriding is fine.
    */
   public boolean triggerEvent(BlockState pState, World pLevel, BlockPos pPos, int pId, int pParam) {
      Direction direction = pState.getValue(FACING);
      if (!pLevel.isClientSide) {
         boolean flag = this.getNeighborSignal(pLevel, pPos, direction);
         if (flag && (pId == 1 || pId == 2)) {
            pLevel.setBlock(pPos, pState.setValue(EXTENDED, Boolean.valueOf(true)), 2);
            return false;
         }

         if (!flag && pId == 0) {
            return false;
         }
      }

      if (pId == 0) {
         if (net.minecraftforge.event.ForgeEventFactory.onPistonMovePre(pLevel, pPos, direction, true)) return false;
         if (!this.moveBlocks(pLevel, pPos, direction, true)) {
            return false;
         }

         pLevel.setBlock(pPos, pState.setValue(EXTENDED, Boolean.valueOf(true)), 67);
         pLevel.playSound((PlayerEntity)null, pPos, SoundEvents.PISTON_EXTEND, SoundCategory.BLOCKS, 0.5F, pLevel.random.nextFloat() * 0.25F + 0.6F);
      } else if (pId == 1 || pId == 2) {
         if (net.minecraftforge.event.ForgeEventFactory.onPistonMovePre(pLevel, pPos, direction, false)) return false;
         TileEntity tileentity1 = pLevel.getBlockEntity(pPos.relative(direction));
         if (tileentity1 instanceof PistonTileEntity) {
            ((PistonTileEntity)tileentity1).finalTick();
         }

         BlockState blockstate = Blocks.MOVING_PISTON.defaultBlockState().setValue(MovingPistonBlock.FACING, direction).setValue(MovingPistonBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT);
         pLevel.setBlock(pPos, blockstate, 20);
         pLevel.setBlockEntity(pPos, MovingPistonBlock.newMovingBlockEntity(this.defaultBlockState().setValue(FACING, Direction.from3DDataValue(pParam & 7)), direction, false, true));
         pLevel.blockUpdated(pPos, blockstate.getBlock());
         blockstate.updateNeighbourShapes(pLevel, pPos, 2);
         if (this.isSticky) {
            BlockPos blockpos = pPos.offset(direction.getStepX() * 2, direction.getStepY() * 2, direction.getStepZ() * 2);
            BlockState blockstate1 = pLevel.getBlockState(blockpos);
            boolean flag1 = false;
            if (blockstate1.is(Blocks.MOVING_PISTON)) {
               TileEntity tileentity = pLevel.getBlockEntity(blockpos);
               if (tileentity instanceof PistonTileEntity) {
                  PistonTileEntity pistontileentity = (PistonTileEntity)tileentity;
                  if (pistontileentity.getDirection() == direction && pistontileentity.isExtending()) {
                     pistontileentity.finalTick();
                     flag1 = true;
                  }
               }
            }

            if (!flag1) {
               if (pId != 1 || blockstate1.isAir() || !isPushable(blockstate1, pLevel, blockpos, direction.getOpposite(), false, direction) || blockstate1.getPistonPushReaction() != PushReaction.NORMAL && !blockstate1.is(Blocks.PISTON) && !blockstate1.is(Blocks.STICKY_PISTON)) {
                  pLevel.removeBlock(pPos.relative(direction), false);
               } else {
                  this.moveBlocks(pLevel, pPos, direction, false);
               }
            }
         } else {
            pLevel.removeBlock(pPos.relative(direction), false);
         }

         pLevel.playSound((PlayerEntity)null, pPos, SoundEvents.PISTON_CONTRACT, SoundCategory.BLOCKS, 0.5F, pLevel.random.nextFloat() * 0.15F + 0.6F);
      }

      net.minecraftforge.event.ForgeEventFactory.onPistonMovePost(pLevel, pPos, direction, (pId == 0));
      return true;
   }

   /**
    * Checks if the piston can push the given BlockState.
    */
   public static boolean isPushable(BlockState pBlockState, World pLevel, BlockPos pPos, Direction pFacing, boolean pDestroyBlocks, Direction pDirection) {
      if (pPos.getY() >= 0 && pPos.getY() <= pLevel.getMaxBuildHeight() - 1 && pLevel.getWorldBorder().isWithinBounds(pPos)) {
         if (pBlockState.isAir()) {
            return true;
         } else if (!pBlockState.is(Blocks.OBSIDIAN) && !pBlockState.is(Blocks.CRYING_OBSIDIAN) && !pBlockState.is(Blocks.RESPAWN_ANCHOR)) {
            if (pFacing == Direction.DOWN && pPos.getY() == 0) {
               return false;
            } else if (pFacing == Direction.UP && pPos.getY() == pLevel.getMaxBuildHeight() - 1) {
               return false;
            } else {
               if (!pBlockState.is(Blocks.PISTON) && !pBlockState.is(Blocks.STICKY_PISTON)) {
                  if (pBlockState.getDestroySpeed(pLevel, pPos) == -1.0F) {
                     return false;
                  }

                  switch(pBlockState.getPistonPushReaction()) {
                  case BLOCK:
                     return false;
                  case DESTROY:
                     return pDestroyBlocks;
                  case PUSH_ONLY:
                     return pFacing == pDirection;
                  }
               } else if (pBlockState.getValue(EXTENDED)) {
                  return false;
               }

               return !pBlockState.hasTileEntity();
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private boolean moveBlocks(World pLevel, BlockPos pPos, Direction pDirection, boolean pExtending) {
      BlockPos blockpos = pPos.relative(pDirection);
      if (!pExtending && pLevel.getBlockState(blockpos).is(Blocks.PISTON_HEAD)) {
         pLevel.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 20);
      }

      PistonBlockStructureHelper pistonblockstructurehelper = new PistonBlockStructureHelper(pLevel, pPos, pDirection, pExtending);
      if (!pistonblockstructurehelper.resolve()) {
         return false;
      } else {
         Map<BlockPos, BlockState> map = Maps.newHashMap();
         List<BlockPos> list = pistonblockstructurehelper.getToPush();
         List<BlockState> list1 = Lists.newArrayList();

         for(int i = 0; i < list.size(); ++i) {
            BlockPos blockpos1 = list.get(i);
            BlockState blockstate = pLevel.getBlockState(blockpos1);
            list1.add(blockstate);
            map.put(blockpos1, blockstate);
         }

         List<BlockPos> list2 = pistonblockstructurehelper.getToDestroy();
         BlockState[] ablockstate = new BlockState[list.size() + list2.size()];
         Direction direction = pExtending ? pDirection : pDirection.getOpposite();
         int j = 0;

         for(int k = list2.size() - 1; k >= 0; --k) {
            BlockPos blockpos2 = list2.get(k);
            BlockState blockstate1 = pLevel.getBlockState(blockpos2);
            TileEntity tileentity = blockstate1.hasTileEntity() ? pLevel.getBlockEntity(blockpos2) : null;
            dropResources(blockstate1, pLevel, blockpos2, tileentity);
            pLevel.setBlock(blockpos2, Blocks.AIR.defaultBlockState(), 18);
            ablockstate[j++] = blockstate1;
         }

         for(int l = list.size() - 1; l >= 0; --l) {
            BlockPos blockpos3 = list.get(l);
            BlockState blockstate5 = pLevel.getBlockState(blockpos3);
            blockpos3 = blockpos3.relative(direction);
            map.remove(blockpos3);
            pLevel.setBlock(blockpos3, Blocks.MOVING_PISTON.defaultBlockState().setValue(FACING, pDirection), 68);
            pLevel.setBlockEntity(blockpos3, MovingPistonBlock.newMovingBlockEntity(list1.get(l), pDirection, pExtending, false));
            ablockstate[j++] = blockstate5;
         }

         if (pExtending) {
            PistonType pistontype = this.isSticky ? PistonType.STICKY : PistonType.DEFAULT;
            BlockState blockstate4 = Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.FACING, pDirection).setValue(PistonHeadBlock.TYPE, pistontype);
            BlockState blockstate6 = Blocks.MOVING_PISTON.defaultBlockState().setValue(MovingPistonBlock.FACING, pDirection).setValue(MovingPistonBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT);
            map.remove(blockpos);
            pLevel.setBlock(blockpos, blockstate6, 68);
            pLevel.setBlockEntity(blockpos, MovingPistonBlock.newMovingBlockEntity(blockstate4, pDirection, true, true));
         }

         BlockState blockstate3 = Blocks.AIR.defaultBlockState();

         for(BlockPos blockpos4 : map.keySet()) {
            pLevel.setBlock(blockpos4, blockstate3, 82);
         }

         for(Entry<BlockPos, BlockState> entry : map.entrySet()) {
            BlockPos blockpos5 = entry.getKey();
            BlockState blockstate2 = entry.getValue();
            blockstate2.updateIndirectNeighbourShapes(pLevel, blockpos5, 2);
            blockstate3.updateNeighbourShapes(pLevel, blockpos5, 2);
            blockstate3.updateIndirectNeighbourShapes(pLevel, blockpos5, 2);
         }

         j = 0;

         for(int i1 = list2.size() - 1; i1 >= 0; --i1) {
            BlockState blockstate7 = ablockstate[j++];
            BlockPos blockpos6 = list2.get(i1);
            blockstate7.updateIndirectNeighbourShapes(pLevel, blockpos6, 2);
            pLevel.updateNeighborsAt(blockpos6, blockstate7.getBlock());
         }

         for(int j1 = list.size() - 1; j1 >= 0; --j1) {
            pLevel.updateNeighborsAt(list.get(j1), ablockstate[j++].getBlock());
         }

         if (pExtending) {
            pLevel.updateNeighborsAt(blockpos, Blocks.PISTON_HEAD);
         }

         return true;
      }
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link IBlockState#withRotation(Rotation)} whenever possible. Implementing/overriding is
    * fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRotation) {
      return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
   }

   public BlockState rotate(BlockState state, net.minecraft.world.IWorld world, BlockPos pos, Rotation direction) {
       return state.getValue(EXTENDED) ? state : super.rotate(state, world, pos, direction);
   }

   /**
    * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link IBlockState#withMirror(Mirror)} whenever possible. Implementing/overriding is fine.
    */
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING, EXTENDED);
   }

   public boolean useShapeForLightOcclusion(BlockState pState) {
      return pState.getValue(EXTENDED);
   }

   public boolean isPathfindable(BlockState pState, IBlockReader pLevel, BlockPos pPos, PathType pType) {
      return false;
   }
}
