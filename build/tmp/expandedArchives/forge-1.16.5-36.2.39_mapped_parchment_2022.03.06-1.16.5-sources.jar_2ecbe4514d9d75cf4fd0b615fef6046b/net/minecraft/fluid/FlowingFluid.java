package net.minecraft.fluid;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.block.material.Material;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public abstract class FlowingFluid extends Fluid {
   public static final BooleanProperty FALLING = BlockStateProperties.FALLING;
   public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_FLOWING;
   private static final ThreadLocal<Object2ByteLinkedOpenHashMap<Block.RenderSideCacheKey>> OCCLUSION_CACHE = ThreadLocal.withInitial(() -> {
      Object2ByteLinkedOpenHashMap<Block.RenderSideCacheKey> object2bytelinkedopenhashmap = new Object2ByteLinkedOpenHashMap<Block.RenderSideCacheKey>(200) {
         protected void rehash(int p_rehash_1_) {
         }
      };
      object2bytelinkedopenhashmap.defaultReturnValue((byte)127);
      return object2bytelinkedopenhashmap;
   });
   private final Map<FluidState, VoxelShape> shapes = Maps.newIdentityHashMap();

   protected void createFluidStateDefinition(StateContainer.Builder<Fluid, FluidState> pBuilder) {
      pBuilder.add(FALLING);
   }

   public Vector3d getFlow(IBlockReader pBlockReader, BlockPos pPos, FluidState pFluidState) {
      double d0 = 0.0D;
      double d1 = 0.0D;
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         blockpos$mutable.setWithOffset(pPos, direction);
         FluidState fluidstate = pBlockReader.getFluidState(blockpos$mutable);
         if (this.affectsFlow(fluidstate)) {
            float f = fluidstate.getOwnHeight();
            float f1 = 0.0F;
            if (f == 0.0F) {
               if (!pBlockReader.getBlockState(blockpos$mutable).getMaterial().blocksMotion()) {
                  BlockPos blockpos = blockpos$mutable.below();
                  FluidState fluidstate1 = pBlockReader.getFluidState(blockpos);
                  if (this.affectsFlow(fluidstate1)) {
                     f = fluidstate1.getOwnHeight();
                     if (f > 0.0F) {
                        f1 = pFluidState.getOwnHeight() - (f - 0.8888889F);
                     }
                  }
               }
            } else if (f > 0.0F) {
               f1 = pFluidState.getOwnHeight() - f;
            }

            if (f1 != 0.0F) {
               d0 += (double)((float)direction.getStepX() * f1);
               d1 += (double)((float)direction.getStepZ() * f1);
            }
         }
      }

      Vector3d vector3d = new Vector3d(d0, 0.0D, d1);
      if (pFluidState.getValue(FALLING)) {
         for(Direction direction1 : Direction.Plane.HORIZONTAL) {
            blockpos$mutable.setWithOffset(pPos, direction1);
            if (this.isSolidFace(pBlockReader, blockpos$mutable, direction1) || this.isSolidFace(pBlockReader, blockpos$mutable.above(), direction1)) {
               vector3d = vector3d.normalize().add(0.0D, -6.0D, 0.0D);
               break;
            }
         }
      }

      return vector3d.normalize();
   }

   private boolean affectsFlow(FluidState pState) {
      return pState.isEmpty() || pState.getType().isSame(this);
   }

   protected boolean isSolidFace(IBlockReader pLevel, BlockPos pNeighborPos, Direction pSide) {
      BlockState blockstate = pLevel.getBlockState(pNeighborPos);
      FluidState fluidstate = pLevel.getFluidState(pNeighborPos);
      if (fluidstate.getType().isSame(this)) {
         return false;
      } else if (pSide == Direction.UP) {
         return true;
      } else {
         return blockstate.getMaterial() == Material.ICE ? false : blockstate.isFaceSturdy(pLevel, pNeighborPos, pSide);
      }
   }

   protected void spread(IWorld pLevel, BlockPos pPos, FluidState pState) {
      if (!pState.isEmpty()) {
         BlockState blockstate = pLevel.getBlockState(pPos);
         BlockPos blockpos = pPos.below();
         BlockState blockstate1 = pLevel.getBlockState(blockpos);
         FluidState fluidstate = this.getNewLiquid(pLevel, blockpos, blockstate1);
         if (this.canSpreadTo(pLevel, pPos, blockstate, Direction.DOWN, blockpos, blockstate1, pLevel.getFluidState(blockpos), fluidstate.getType())) {
            this.spreadTo(pLevel, blockpos, blockstate1, Direction.DOWN, fluidstate);
            if (this.sourceNeighborCount(pLevel, pPos) >= 3) {
               this.spreadToSides(pLevel, pPos, pState, blockstate);
            }
         } else if (pState.isSource() || !this.isWaterHole(pLevel, fluidstate.getType(), pPos, blockstate, blockpos, blockstate1)) {
            this.spreadToSides(pLevel, pPos, pState, blockstate);
         }

      }
   }

   private void spreadToSides(IWorld p_207937_1_, BlockPos p_207937_2_, FluidState p_207937_3_, BlockState p_207937_4_) {
      int i = p_207937_3_.getAmount() - this.getDropOff(p_207937_1_);
      if (p_207937_3_.getValue(FALLING)) {
         i = 7;
      }

      if (i > 0) {
         Map<Direction, FluidState> map = this.getSpread(p_207937_1_, p_207937_2_, p_207937_4_);

         for(Entry<Direction, FluidState> entry : map.entrySet()) {
            Direction direction = entry.getKey();
            FluidState fluidstate = entry.getValue();
            BlockPos blockpos = p_207937_2_.relative(direction);
            BlockState blockstate = p_207937_1_.getBlockState(blockpos);
            if (this.canSpreadTo(p_207937_1_, p_207937_2_, p_207937_4_, direction, blockpos, blockstate, p_207937_1_.getFluidState(blockpos), fluidstate.getType())) {
               this.spreadTo(p_207937_1_, blockpos, blockstate, direction, fluidstate);
            }
         }

      }
   }

   protected FluidState getNewLiquid(IWorldReader pLevel, BlockPos pPos, BlockState pBlockState) {
      int i = 0;
      int j = 0;

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos = pPos.relative(direction);
         BlockState blockstate = pLevel.getBlockState(blockpos);
         FluidState fluidstate = blockstate.getFluidState();
         if (fluidstate.getType().isSame(this) && this.canPassThroughWall(direction, pLevel, pPos, pBlockState, blockpos, blockstate)) {
            if (fluidstate.isSource() && net.minecraftforge.event.ForgeEventFactory.canCreateFluidSource(pLevel, blockpos, blockstate, this.canConvertToSource())) {
               ++j;
            }

            i = Math.max(i, fluidstate.getAmount());
         }
      }

      if (j >= 2) {
         BlockState blockstate1 = pLevel.getBlockState(pPos.below());
         FluidState fluidstate1 = blockstate1.getFluidState();
         if (blockstate1.getMaterial().isSolid() || this.isSourceBlockOfThisType(fluidstate1)) {
            return this.getSource(false);
         }
      }

      BlockPos blockpos1 = pPos.above();
      BlockState blockstate2 = pLevel.getBlockState(blockpos1);
      FluidState fluidstate2 = blockstate2.getFluidState();
      if (!fluidstate2.isEmpty() && fluidstate2.getType().isSame(this) && this.canPassThroughWall(Direction.UP, pLevel, pPos, pBlockState, blockpos1, blockstate2)) {
         return this.getFlowing(8, true);
      } else {
         int k = i - this.getDropOff(pLevel);
         return k <= 0 ? Fluids.EMPTY.defaultFluidState() : this.getFlowing(k, false);
      }
   }

   private boolean canPassThroughWall(Direction p_212751_1_, IBlockReader p_212751_2_, BlockPos p_212751_3_, BlockState p_212751_4_, BlockPos p_212751_5_, BlockState p_212751_6_) {
      Object2ByteLinkedOpenHashMap<Block.RenderSideCacheKey> object2bytelinkedopenhashmap;
      if (!p_212751_4_.getBlock().hasDynamicShape() && !p_212751_6_.getBlock().hasDynamicShape()) {
         object2bytelinkedopenhashmap = OCCLUSION_CACHE.get();
      } else {
         object2bytelinkedopenhashmap = null;
      }

      Block.RenderSideCacheKey block$rendersidecachekey;
      if (object2bytelinkedopenhashmap != null) {
         block$rendersidecachekey = new Block.RenderSideCacheKey(p_212751_4_, p_212751_6_, p_212751_1_);
         byte b0 = object2bytelinkedopenhashmap.getAndMoveToFirst(block$rendersidecachekey);
         if (b0 != 127) {
            return b0 != 0;
         }
      } else {
         block$rendersidecachekey = null;
      }

      VoxelShape voxelshape1 = p_212751_4_.getCollisionShape(p_212751_2_, p_212751_3_);
      VoxelShape voxelshape = p_212751_6_.getCollisionShape(p_212751_2_, p_212751_5_);
      boolean flag = !VoxelShapes.mergedFaceOccludes(voxelshape1, voxelshape, p_212751_1_);
      if (object2bytelinkedopenhashmap != null) {
         if (object2bytelinkedopenhashmap.size() == 200) {
            object2bytelinkedopenhashmap.removeLastByte();
         }

         object2bytelinkedopenhashmap.putAndMoveToFirst(block$rendersidecachekey, (byte)(flag ? 1 : 0));
      }

      return flag;
   }

   public abstract Fluid getFlowing();

   public FluidState getFlowing(int pLevel, boolean pFalling) {
      return this.getFlowing().defaultFluidState().setValue(LEVEL, Integer.valueOf(pLevel)).setValue(FALLING, Boolean.valueOf(pFalling));
   }

   public abstract Fluid getSource();

   public FluidState getSource(boolean pFalling) {
      return this.getSource().defaultFluidState().setValue(FALLING, Boolean.valueOf(pFalling));
   }

   protected abstract boolean canConvertToSource();

   protected void spreadTo(IWorld pLevel, BlockPos pPos, BlockState pBlockState, Direction pDirection, FluidState pFluidState) {
      if (pBlockState.getBlock() instanceof ILiquidContainer) {
         ((ILiquidContainer)pBlockState.getBlock()).placeLiquid(pLevel, pPos, pBlockState, pFluidState);
      } else {
         if (!pBlockState.isAir()) {
            this.beforeDestroyingBlock(pLevel, pPos, pBlockState);
         }

         pLevel.setBlock(pPos, pFluidState.createLegacyBlock(), 3);
      }

   }

   protected abstract void beforeDestroyingBlock(IWorld pLevel, BlockPos pPos, BlockState pState);

   private static short getCacheKey(BlockPos p_212752_0_, BlockPos p_212752_1_) {
      int i = p_212752_1_.getX() - p_212752_0_.getX();
      int j = p_212752_1_.getZ() - p_212752_0_.getZ();
      return (short)((i + 128 & 255) << 8 | j + 128 & 255);
   }

   protected int getSlopeDistance(IWorldReader p_205571_1_, BlockPos p_205571_2_, int p_205571_3_, Direction p_205571_4_, BlockState p_205571_5_, BlockPos p_205571_6_, Short2ObjectMap<Pair<BlockState, FluidState>> p_205571_7_, Short2BooleanMap p_205571_8_) {
      int i = 1000;

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         if (direction != p_205571_4_) {
            BlockPos blockpos = p_205571_2_.relative(direction);
            short short1 = getCacheKey(p_205571_6_, blockpos);
            Pair<BlockState, FluidState> pair = p_205571_7_.computeIfAbsent(short1, (p_212748_2_) -> {
               BlockState blockstate1 = p_205571_1_.getBlockState(blockpos);
               return Pair.of(blockstate1, blockstate1.getFluidState());
            });
            BlockState blockstate = pair.getFirst();
            FluidState fluidstate = pair.getSecond();
            if (this.canPassThrough(p_205571_1_, this.getFlowing(), p_205571_2_, p_205571_5_, direction, blockpos, blockstate, fluidstate)) {
               boolean flag = p_205571_8_.computeIfAbsent(short1, (p_212749_4_) -> {
                  BlockPos blockpos1 = blockpos.below();
                  BlockState blockstate1 = p_205571_1_.getBlockState(blockpos1);
                  return this.isWaterHole(p_205571_1_, this.getFlowing(), blockpos, blockstate, blockpos1, blockstate1);
               });
               if (flag) {
                  return p_205571_3_;
               }

               if (p_205571_3_ < this.getSlopeFindDistance(p_205571_1_)) {
                  int j = this.getSlopeDistance(p_205571_1_, blockpos, p_205571_3_ + 1, direction.getOpposite(), blockstate, p_205571_6_, p_205571_7_, p_205571_8_);
                  if (j < i) {
                     i = j;
                  }
               }
            }
         }
      }

      return i;
   }

   private boolean isWaterHole(IBlockReader p_211759_1_, Fluid p_211759_2_, BlockPos p_211759_3_, BlockState p_211759_4_, BlockPos p_211759_5_, BlockState p_211759_6_) {
      if (!this.canPassThroughWall(Direction.DOWN, p_211759_1_, p_211759_3_, p_211759_4_, p_211759_5_, p_211759_6_)) {
         return false;
      } else {
         return p_211759_6_.getFluidState().getType().isSame(this) ? true : this.canHoldFluid(p_211759_1_, p_211759_5_, p_211759_6_, p_211759_2_);
      }
   }

   private boolean canPassThrough(IBlockReader p_211760_1_, Fluid p_211760_2_, BlockPos p_211760_3_, BlockState p_211760_4_, Direction p_211760_5_, BlockPos p_211760_6_, BlockState p_211760_7_, FluidState p_211760_8_) {
      return !this.isSourceBlockOfThisType(p_211760_8_) && this.canPassThroughWall(p_211760_5_, p_211760_1_, p_211760_3_, p_211760_4_, p_211760_6_, p_211760_7_) && this.canHoldFluid(p_211760_1_, p_211760_6_, p_211760_7_, p_211760_2_);
   }

   private boolean isSourceBlockOfThisType(FluidState pState) {
      return pState.getType().isSame(this) && pState.isSource();
   }

   protected abstract int getSlopeFindDistance(IWorldReader pLevel);

   /**
    * Returns the number of immediately adjacent source blocks of the same fluid that lie on the horizontal plane.
    */
   private int sourceNeighborCount(IWorldReader pLevel, BlockPos pPos) {
      int i = 0;

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos = pPos.relative(direction);
         FluidState fluidstate = pLevel.getFluidState(blockpos);
         if (this.isSourceBlockOfThisType(fluidstate)) {
            ++i;
         }
      }

      return i;
   }

   protected Map<Direction, FluidState> getSpread(IWorldReader p_205572_1_, BlockPos p_205572_2_, BlockState p_205572_3_) {
      int i = 1000;
      Map<Direction, FluidState> map = Maps.newEnumMap(Direction.class);
      Short2ObjectMap<Pair<BlockState, FluidState>> short2objectmap = new Short2ObjectOpenHashMap<>();
      Short2BooleanMap short2booleanmap = new Short2BooleanOpenHashMap();

      for(Direction direction : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos = p_205572_2_.relative(direction);
         short short1 = getCacheKey(p_205572_2_, blockpos);
         Pair<BlockState, FluidState> pair = short2objectmap.computeIfAbsent(short1, (p_212755_2_) -> {
            BlockState blockstate1 = p_205572_1_.getBlockState(blockpos);
            return Pair.of(blockstate1, blockstate1.getFluidState());
         });
         BlockState blockstate = pair.getFirst();
         FluidState fluidstate = pair.getSecond();
         FluidState fluidstate1 = this.getNewLiquid(p_205572_1_, blockpos, blockstate);
         if (this.canPassThrough(p_205572_1_, fluidstate1.getType(), p_205572_2_, p_205572_3_, direction, blockpos, blockstate, fluidstate)) {
            BlockPos blockpos1 = blockpos.below();
            boolean flag = short2booleanmap.computeIfAbsent(short1, (p_212753_5_) -> {
               BlockState blockstate1 = p_205572_1_.getBlockState(blockpos1);
               return this.isWaterHole(p_205572_1_, this.getFlowing(), blockpos, blockstate, blockpos1, blockstate1);
            });
            int j;
            if (flag) {
               j = 0;
            } else {
               j = this.getSlopeDistance(p_205572_1_, blockpos, 1, direction.getOpposite(), blockstate, p_205572_2_, short2objectmap, short2booleanmap);
            }

            if (j < i) {
               map.clear();
            }

            if (j <= i) {
               map.put(direction, fluidstate1);
               i = j;
            }
         }
      }

      return map;
   }

   private boolean canHoldFluid(IBlockReader pLevel, BlockPos pPos, BlockState pState, Fluid pFluid) {
      Block block = pState.getBlock();
      if (block instanceof ILiquidContainer) {
         return ((ILiquidContainer)block).canPlaceLiquid(pLevel, pPos, pState, pFluid);
      } else if (!(block instanceof DoorBlock) && !block.is(BlockTags.SIGNS) && block != Blocks.LADDER && block != Blocks.SUGAR_CANE && block != Blocks.BUBBLE_COLUMN) {
         Material material = pState.getMaterial();
         if (material != Material.PORTAL && material != Material.STRUCTURAL_AIR && material != Material.WATER_PLANT && material != Material.REPLACEABLE_WATER_PLANT) {
            return !material.blocksMotion();
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   protected boolean canSpreadTo(IBlockReader pLevel, BlockPos pFromPos, BlockState pFromBlockState, Direction pDirection, BlockPos pToPos, BlockState pToBlockState, FluidState pToFluidState, Fluid pFluid) {
      return pToFluidState.canBeReplacedWith(pLevel, pToPos, pFluid, pDirection) && this.canPassThroughWall(pDirection, pLevel, pFromPos, pFromBlockState, pToPos, pToBlockState) && this.canHoldFluid(pLevel, pToPos, pToBlockState, pFluid);
   }

   protected abstract int getDropOff(IWorldReader pLevel);

   protected int getSpreadDelay(World p_215667_1_, BlockPos p_215667_2_, FluidState p_215667_3_, FluidState p_215667_4_) {
      return this.getTickDelay(p_215667_1_);
   }

   public void tick(World pLevel, BlockPos pPos, FluidState pState) {
      if (!pState.isSource()) {
         FluidState fluidstate = this.getNewLiquid(pLevel, pPos, pLevel.getBlockState(pPos));
         int i = this.getSpreadDelay(pLevel, pPos, pState, fluidstate);
         if (fluidstate.isEmpty()) {
            pState = fluidstate;
            pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 3);
         } else if (!fluidstate.equals(pState)) {
            pState = fluidstate;
            BlockState blockstate = fluidstate.createLegacyBlock();
            pLevel.setBlock(pPos, blockstate, 2);
            pLevel.getLiquidTicks().scheduleTick(pPos, fluidstate.getType(), i);
            pLevel.updateNeighborsAt(pPos, blockstate.getBlock());
         }
      }

      this.spread(pLevel, pPos, pState);
   }

   protected static int getLegacyLevel(FluidState pState) {
      return pState.isSource() ? 0 : 8 - Math.min(pState.getAmount(), 8) + (pState.getValue(FALLING) ? 8 : 0);
   }

   private static boolean hasSameAbove(FluidState p_215666_0_, IBlockReader p_215666_1_, BlockPos p_215666_2_) {
      return p_215666_0_.getType().isSame(p_215666_1_.getFluidState(p_215666_2_.above()).getType());
   }

   public float getHeight(FluidState p_215662_1_, IBlockReader p_215662_2_, BlockPos p_215662_3_) {
      return hasSameAbove(p_215662_1_, p_215662_2_, p_215662_3_) ? 1.0F : p_215662_1_.getOwnHeight();
   }

   public float getOwnHeight(FluidState p_223407_1_) {
      return (float)p_223407_1_.getAmount() / 9.0F;
   }

   public VoxelShape getShape(FluidState p_215664_1_, IBlockReader p_215664_2_, BlockPos p_215664_3_) {
      return p_215664_1_.getAmount() == 9 && hasSameAbove(p_215664_1_, p_215664_2_, p_215664_3_) ? VoxelShapes.block() : this.shapes.computeIfAbsent(p_215664_1_, (p_215668_2_) -> {
         return VoxelShapes.box(0.0D, 0.0D, 0.0D, 1.0D, (double)p_215668_2_.getHeight(p_215664_2_, p_215664_3_), 1.0D);
      });
   }
}
