package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.JukeboxTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class JukeboxBlock extends ContainerBlock {
   public static final BooleanProperty HAS_RECORD = BlockStateProperties.HAS_RECORD;

   public JukeboxBlock(AbstractBlock.Properties p_i48372_1_) {
      super(p_i48372_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(HAS_RECORD, Boolean.valueOf(false)));
   }

   /**
    * Called by ItemBlocks after a block is set in the world, to allow post-place logic
    */
   public void setPlacedBy(World pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
      super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
      CompoundNBT compoundnbt = pStack.getOrCreateTag();
      if (compoundnbt.contains("BlockEntityTag")) {
         CompoundNBT compoundnbt1 = compoundnbt.getCompound("BlockEntityTag");
         if (compoundnbt1.contains("RecordItem")) {
            pLevel.setBlock(pPos, pState.setValue(HAS_RECORD, Boolean.valueOf(true)), 2);
         }
      }

   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      if (pState.getValue(HAS_RECORD)) {
         this.dropRecording(pLevel, pPos);
         pState = pState.setValue(HAS_RECORD, Boolean.valueOf(false));
         pLevel.setBlock(pPos, pState, 2);
         return ActionResultType.sidedSuccess(pLevel.isClientSide);
      } else {
         return ActionResultType.PASS;
      }
   }

   public void setRecord(IWorld pLevel, BlockPos pPos, BlockState pState, ItemStack pRecordStack) {
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      if (tileentity instanceof JukeboxTileEntity) {
         ((JukeboxTileEntity)tileentity).setRecord(pRecordStack.copy());
         pLevel.setBlock(pPos, pState.setValue(HAS_RECORD, Boolean.valueOf(true)), 2);
      }
   }

   private void dropRecording(World pLevel, BlockPos pPos) {
      if (!pLevel.isClientSide) {
         TileEntity tileentity = pLevel.getBlockEntity(pPos);
         if (tileentity instanceof JukeboxTileEntity) {
            JukeboxTileEntity jukeboxtileentity = (JukeboxTileEntity)tileentity;
            ItemStack itemstack = jukeboxtileentity.getRecord();
            if (!itemstack.isEmpty()) {
               pLevel.levelEvent(1010, pPos, 0);
               jukeboxtileentity.clearContent();
               float f = 0.7F;
               double d0 = (double)(pLevel.random.nextFloat() * 0.7F) + (double)0.15F;
               double d1 = (double)(pLevel.random.nextFloat() * 0.7F) + (double)0.060000002F + 0.6D;
               double d2 = (double)(pLevel.random.nextFloat() * 0.7F) + (double)0.15F;
               ItemStack itemstack1 = itemstack.copy();
               ItemEntity itementity = new ItemEntity(pLevel, (double)pPos.getX() + d0, (double)pPos.getY() + d1, (double)pPos.getZ() + d2, itemstack1);
               itementity.setDefaultPickUpDelay();
               pLevel.addFreshEntity(itementity);
            }
         }
      }
   }

   public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pState.is(pNewState.getBlock())) {
         this.dropRecording(pLevel, pPos);
         super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
      }
   }

   public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
      return new JukeboxTileEntity();
   }

   /**
    * @deprecated call via {@link IBlockState#hasComparatorInputOverride()} whenever possible. Implementing/overriding
    * is fine.
    */
   public boolean hasAnalogOutputSignal(BlockState pState) {
      return true;
   }

   /**
    * @deprecated call via {@link IBlockState#getComparatorInputOverride(World,BlockPos)} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getAnalogOutputSignal(BlockState pBlockState, World pLevel, BlockPos pPos) {
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      if (tileentity instanceof JukeboxTileEntity) {
         Item item = ((JukeboxTileEntity)tileentity).getRecord().getItem();
         if (item instanceof MusicDiscItem) {
            return ((MusicDiscItem)item).getAnalogOutput();
         }
      }

      return 0;
   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link IBlockState#getRenderType()} whenever possible. Implementing/overriding is fine.
    */
   public BlockRenderType getRenderShape(BlockState pState) {
      return BlockRenderType.MODEL;
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(HAS_RECORD);
   }
}