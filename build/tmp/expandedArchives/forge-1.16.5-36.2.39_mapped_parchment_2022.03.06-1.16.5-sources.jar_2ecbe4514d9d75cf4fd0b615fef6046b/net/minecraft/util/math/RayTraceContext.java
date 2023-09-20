package net.minecraft.util.math;

import java.util.function.Predicate;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;

public class RayTraceContext {
   private final Vector3d from;
   private final Vector3d to;
   private final RayTraceContext.BlockMode block;
   private final RayTraceContext.FluidMode fluid;
   private final ISelectionContext collisionContext;

   public RayTraceContext(Vector3d pFrom, Vector3d pTo, RayTraceContext.BlockMode pBlock, RayTraceContext.FluidMode pFluid, @javax.annotation.Nullable Entity pEntity) {
      this.from = pFrom;
      this.to = pTo;
      this.block = pBlock;
      this.fluid = pFluid;
      this.collisionContext = pEntity == null ? ISelectionContext.empty() : ISelectionContext.of(pEntity);
   }

   public Vector3d getTo() {
      return this.to;
   }

   public Vector3d getFrom() {
      return this.from;
   }

   public VoxelShape getBlockShape(BlockState pBlockState, IBlockReader pLevel, BlockPos pPos) {
      return this.block.get(pBlockState, pLevel, pPos, this.collisionContext);
   }

   public VoxelShape getFluidShape(FluidState pState, IBlockReader pLevel, BlockPos pPos) {
      return this.fluid.canPick(pState) ? pState.getShape(pLevel, pPos) : VoxelShapes.empty();
   }

   public static enum BlockMode implements RayTraceContext.IVoxelProvider {
      COLLIDER(AbstractBlock.AbstractBlockState::getCollisionShape),
      OUTLINE(AbstractBlock.AbstractBlockState::getShape),
      VISUAL(AbstractBlock.AbstractBlockState::getVisualShape);

      private final RayTraceContext.IVoxelProvider shapeGetter;

      private BlockMode(RayTraceContext.IVoxelProvider pShapeGetter) {
         this.shapeGetter = pShapeGetter;
      }

      public VoxelShape get(BlockState p_get_1_, IBlockReader p_get_2_, BlockPos p_get_3_, ISelectionContext p_get_4_) {
         return this.shapeGetter.get(p_get_1_, p_get_2_, p_get_3_, p_get_4_);
      }
   }

   public static enum FluidMode {
      NONE((p_222247_0_) -> {
         return false;
      }),
      SOURCE_ONLY(FluidState::isSource),
      ANY((p_222246_0_) -> {
         return !p_222246_0_.isEmpty();
      });

      private final Predicate<FluidState> canPick;

      private FluidMode(Predicate<FluidState> pCanPick) {
         this.canPick = pCanPick;
      }

      public boolean canPick(FluidState pState) {
         return this.canPick.test(pState);
      }
   }

   public interface IVoxelProvider {
      VoxelShape get(BlockState p_get_1_, IBlockReader p_get_2_, BlockPos p_get_3_, ISelectionContext p_get_4_);
   }
}
