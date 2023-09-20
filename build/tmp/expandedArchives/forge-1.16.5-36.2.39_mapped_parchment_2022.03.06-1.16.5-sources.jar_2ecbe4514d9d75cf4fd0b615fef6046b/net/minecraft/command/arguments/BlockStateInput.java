package net.minecraft.command.arguments;

import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class BlockStateInput implements Predicate<CachedBlockInfo> {
   private final BlockState state;
   private final Set<Property<?>> properties;
   @Nullable
   private final CompoundNBT tag;

   public BlockStateInput(BlockState pState, Set<Property<?>> pProperties, @Nullable CompoundNBT pTag) {
      this.state = pState;
      this.properties = pProperties;
      this.tag = pTag;
   }

   public BlockState getState() {
      return this.state;
   }

   public boolean test(CachedBlockInfo p_test_1_) {
      BlockState blockstate = p_test_1_.getState();
      if (!blockstate.is(this.state.getBlock())) {
         return false;
      } else {
         for(Property<?> property : this.properties) {
            if (blockstate.getValue(property) != this.state.getValue(property)) {
               return false;
            }
         }

         if (this.tag == null) {
            return true;
         } else {
            TileEntity tileentity = p_test_1_.getEntity();
            return tileentity != null && NBTUtil.compareNbt(this.tag, tileentity.save(new CompoundNBT()), true);
         }
      }
   }

   public boolean place(ServerWorld pLevel, BlockPos pPos, int pFlags) {
      BlockState blockstate = Block.updateFromNeighbourShapes(this.state, pLevel, pPos);
      if (blockstate.isAir()) {
         blockstate = this.state;
      }

      if (!pLevel.setBlock(pPos, blockstate, pFlags)) {
         return false;
      } else {
         if (this.tag != null) {
            TileEntity tileentity = pLevel.getBlockEntity(pPos);
            if (tileentity != null) {
               CompoundNBT compoundnbt = this.tag.copy();
               compoundnbt.putInt("x", pPos.getX());
               compoundnbt.putInt("y", pPos.getY());
               compoundnbt.putInt("z", pPos.getZ());
               tileentity.load(blockstate, compoundnbt);
            }
         }

         return true;
      }
   }
}