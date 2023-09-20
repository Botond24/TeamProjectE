package net.minecraft.util.math.shapes;

import net.minecraft.entity.Entity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;

public interface ISelectionContext extends net.minecraftforge.common.extensions.IForgeSelectionContext {
   static ISelectionContext empty() {
      return EntitySelectionContext.EMPTY;
   }

   static ISelectionContext of(Entity pEntity) {
      return new EntitySelectionContext(pEntity);
   }

   boolean isDescending();

   boolean isAbove(VoxelShape pShape, BlockPos pPos, boolean pCanAscend);

   boolean isHoldingItem(Item pItem);

   boolean canStandOnFluid(FluidState pState, FlowingFluid pFlowing);
}
