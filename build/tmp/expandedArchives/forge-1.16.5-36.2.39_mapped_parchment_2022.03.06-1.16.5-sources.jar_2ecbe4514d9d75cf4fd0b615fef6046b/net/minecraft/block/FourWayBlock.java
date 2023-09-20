package net.minecraft.block;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class FourWayBlock extends Block implements IWaterLoggable {
   public static final BooleanProperty NORTH = SixWayBlock.NORTH;
   public static final BooleanProperty EAST = SixWayBlock.EAST;
   public static final BooleanProperty SOUTH = SixWayBlock.SOUTH;
   public static final BooleanProperty WEST = SixWayBlock.WEST;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   protected static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = SixWayBlock.PROPERTY_BY_DIRECTION.entrySet().stream().filter((p_199775_0_) -> {
      return p_199775_0_.getKey().getAxis().isHorizontal();
   }).collect(Util.toMap());
   protected final VoxelShape[] collisionShapeByIndex;
   protected final VoxelShape[] shapeByIndex;
   private final Object2IntMap<BlockState> stateToIndex = new Object2IntOpenHashMap<>();

   public FourWayBlock(float p_i48420_1_, float p_i48420_2_, float p_i48420_3_, float p_i48420_4_, float p_i48420_5_, AbstractBlock.Properties p_i48420_6_) {
      super(p_i48420_6_);
      this.collisionShapeByIndex = this.makeShapes(p_i48420_1_, p_i48420_2_, p_i48420_5_, 0.0F, p_i48420_5_);
      this.shapeByIndex = this.makeShapes(p_i48420_1_, p_i48420_2_, p_i48420_3_, 0.0F, p_i48420_4_);

      for(BlockState blockstate : this.stateDefinition.getPossibleStates()) {
         this.getAABBIndex(blockstate);
      }

   }

   protected VoxelShape[] makeShapes(float pNodeWidth, float pExtensionWidth, float pNodeHeight, float pExtensionBottom, float pExtensionHeight) {
      float f = 8.0F - pNodeWidth;
      float f1 = 8.0F + pNodeWidth;
      float f2 = 8.0F - pExtensionWidth;
      float f3 = 8.0F + pExtensionWidth;
      VoxelShape voxelshape = Block.box((double)f, 0.0D, (double)f, (double)f1, (double)pNodeHeight, (double)f1);
      VoxelShape voxelshape1 = Block.box((double)f2, (double)pExtensionBottom, 0.0D, (double)f3, (double)pExtensionHeight, (double)f3);
      VoxelShape voxelshape2 = Block.box((double)f2, (double)pExtensionBottom, (double)f2, (double)f3, (double)pExtensionHeight, 16.0D);
      VoxelShape voxelshape3 = Block.box(0.0D, (double)pExtensionBottom, (double)f2, (double)f3, (double)pExtensionHeight, (double)f3);
      VoxelShape voxelshape4 = Block.box((double)f2, (double)pExtensionBottom, (double)f2, 16.0D, (double)pExtensionHeight, (double)f3);
      VoxelShape voxelshape5 = VoxelShapes.or(voxelshape1, voxelshape4);
      VoxelShape voxelshape6 = VoxelShapes.or(voxelshape2, voxelshape3);
      VoxelShape[] avoxelshape = new VoxelShape[]{VoxelShapes.empty(), voxelshape2, voxelshape3, voxelshape6, voxelshape1, VoxelShapes.or(voxelshape2, voxelshape1), VoxelShapes.or(voxelshape3, voxelshape1), VoxelShapes.or(voxelshape6, voxelshape1), voxelshape4, VoxelShapes.or(voxelshape2, voxelshape4), VoxelShapes.or(voxelshape3, voxelshape4), VoxelShapes.or(voxelshape6, voxelshape4), voxelshape5, VoxelShapes.or(voxelshape2, voxelshape5), VoxelShapes.or(voxelshape3, voxelshape5), VoxelShapes.or(voxelshape6, voxelshape5)};

      for(int i = 0; i < 16; ++i) {
         avoxelshape[i] = VoxelShapes.or(voxelshape, avoxelshape[i]);
      }

      return avoxelshape;
   }

   public boolean propagatesSkylightDown(BlockState pState, IBlockReader pReader, BlockPos pPos) {
      return !pState.getValue(WATERLOGGED);
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return this.shapeByIndex[this.getAABBIndex(pState)];
   }

   public VoxelShape getCollisionShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return this.collisionShapeByIndex[this.getAABBIndex(pState)];
   }

   private static int indexFor(Direction pFacing) {
      return 1 << pFacing.get2DDataValue();
   }

   protected int getAABBIndex(BlockState pState) {
      return this.stateToIndex.computeIntIfAbsent(pState, (p_223007_0_) -> {
         int i = 0;
         if (p_223007_0_.getValue(NORTH)) {
            i |= indexFor(Direction.NORTH);
         }

         if (p_223007_0_.getValue(EAST)) {
            i |= indexFor(Direction.EAST);
         }

         if (p_223007_0_.getValue(SOUTH)) {
            i |= indexFor(Direction.SOUTH);
         }

         if (p_223007_0_.getValue(WEST)) {
            i |= indexFor(Direction.WEST);
         }

         return i;
      });
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   public boolean isPathfindable(BlockState pState, IBlockReader pLevel, BlockPos pPos, PathType pType) {
      return false;
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
}