package net.minecraft.block;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Random;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.Position;
import net.minecraft.dispenser.ProxyBlockSource;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.tileentity.DropperTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class DispenserBlock extends ContainerBlock {
   public static final DirectionProperty FACING = DirectionalBlock.FACING;
   public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
   /** Registry for all dispense behaviors. */
   private static final Map<Item, IDispenseItemBehavior> DISPENSER_REGISTRY = Util.make(new Object2ObjectOpenHashMap<>(), (p_212564_0_) -> {
      p_212564_0_.defaultReturnValue(new DefaultDispenseItemBehavior());
   });

   public static void registerBehavior(IItemProvider pItem, IDispenseItemBehavior pBehavior) {
      DISPENSER_REGISTRY.put(pItem.asItem(), pBehavior);
   }

   public DispenserBlock(AbstractBlock.Properties p_i48414_1_) {
      super(p_i48414_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TRIGGERED, Boolean.valueOf(false)));
   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      if (pLevel.isClientSide) {
         return ActionResultType.SUCCESS;
      } else {
         TileEntity tileentity = pLevel.getBlockEntity(pPos);
         if (tileentity instanceof DispenserTileEntity) {
            pPlayer.openMenu((DispenserTileEntity)tileentity);
            if (tileentity instanceof DropperTileEntity) {
               pPlayer.awardStat(Stats.INSPECT_DROPPER);
            } else {
               pPlayer.awardStat(Stats.INSPECT_DISPENSER);
            }
         }

         return ActionResultType.CONSUME;
      }
   }

   protected void dispenseFrom(ServerWorld pLevel, BlockPos pPos) {
      ProxyBlockSource proxyblocksource = new ProxyBlockSource(pLevel, pPos);
      DispenserTileEntity dispensertileentity = proxyblocksource.getEntity();
      int i = dispensertileentity.getRandomSlot();
      if (i < 0) {
         pLevel.levelEvent(1001, pPos, 0);
      } else {
         ItemStack itemstack = dispensertileentity.getItem(i);
         IDispenseItemBehavior idispenseitembehavior = this.getDispenseMethod(itemstack);
         if (idispenseitembehavior != IDispenseItemBehavior.NOOP) {
            dispensertileentity.setItem(i, idispenseitembehavior.dispense(proxyblocksource, itemstack));
         }

      }
   }

   protected IDispenseItemBehavior getDispenseMethod(ItemStack pStack) {
      return DISPENSER_REGISTRY.get(pStack.getItem());
   }

   public void neighborChanged(BlockState pState, World pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      boolean flag = pLevel.hasNeighborSignal(pPos) || pLevel.hasNeighborSignal(pPos.above());
      boolean flag1 = pState.getValue(TRIGGERED);
      if (flag && !flag1) {
         pLevel.getBlockTicks().scheduleTick(pPos, this, 4);
         pLevel.setBlock(pPos, pState.setValue(TRIGGERED, Boolean.valueOf(true)), 4);
      } else if (!flag && flag1) {
         pLevel.setBlock(pPos, pState.setValue(TRIGGERED, Boolean.valueOf(false)), 4);
      }

   }

   public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
      this.dispenseFrom(pLevel, pPos);
   }

   public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
      return new DispenserTileEntity();
   }

   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      return this.defaultBlockState().setValue(FACING, pContext.getNearestLookingDirection().getOpposite());
   }

   /**
    * Called by ItemBlocks after a block is set in the world, to allow post-place logic
    */
   public void setPlacedBy(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
      if (pStack.hasCustomHoverName()) {
         TileEntity tileentity = pLevel.getBlockEntity(pPos);
         if (tileentity instanceof DispenserTileEntity) {
            ((DispenserTileEntity)tileentity).setCustomName(pStack.getHoverName());
         }
      }

   }

   public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pState.is(pNewState.getBlock())) {
         TileEntity tileentity = pLevel.getBlockEntity(pPos);
         if (tileentity instanceof DispenserTileEntity) {
            InventoryHelper.dropContents(pLevel, pPos, (DispenserTileEntity)tileentity);
            pLevel.updateNeighbourForOutputSignal(pPos, this);
         }

         super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
      }
   }

   /**
    * @return the position where the dispenser at the given position should dispense to.
    */
   public static IPosition getDispensePosition(IBlockSource pBlockSource) {
      Direction direction = pBlockSource.getBlockState().getValue(FACING);
      double d0 = pBlockSource.x() + 0.7D * (double)direction.getStepX();
      double d1 = pBlockSource.y() + 0.7D * (double)direction.getStepY();
      double d2 = pBlockSource.z() + 0.7D * (double)direction.getStepZ();
      return new Position(d0, d1, d2);
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
      return Container.getRedstoneSignalFromBlockEntity(pLevel.getBlockEntity(pPos));
   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link IBlockState#getRenderType()} whenever possible. Implementing/overriding is fine.
    */
   public BlockRenderType getRenderShape(BlockState pState) {
      return BlockRenderType.MODEL;
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

   /**
    * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link IBlockState#withMirror(Mirror)} whenever possible. Implementing/overriding is fine.
    */
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING, TRIGGERED);
   }
}