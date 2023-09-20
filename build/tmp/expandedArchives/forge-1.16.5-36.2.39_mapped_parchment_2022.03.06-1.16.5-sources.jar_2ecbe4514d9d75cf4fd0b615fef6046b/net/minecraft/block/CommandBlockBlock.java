package net.minecraft.block;

import java.util.Random;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.CommandBlockLogic;
import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandBlockBlock extends ContainerBlock {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final DirectionProperty FACING = DirectionalBlock.FACING;
   public static final BooleanProperty CONDITIONAL = BlockStateProperties.CONDITIONAL;

   public CommandBlockBlock(AbstractBlock.Properties p_i48425_1_) {
      super(p_i48425_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(CONDITIONAL, Boolean.valueOf(false)));
   }

   public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
      CommandBlockTileEntity commandblocktileentity = new CommandBlockTileEntity();
      commandblocktileentity.setAutomatic(this == Blocks.CHAIN_COMMAND_BLOCK);
      return commandblocktileentity;
   }

   public void neighborChanged(BlockState pState, World pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
      if (!pLevel.isClientSide) {
         TileEntity tileentity = pLevel.getBlockEntity(pPos);
         if (tileentity instanceof CommandBlockTileEntity) {
            CommandBlockTileEntity commandblocktileentity = (CommandBlockTileEntity)tileentity;
            boolean flag = pLevel.hasNeighborSignal(pPos);
            boolean flag1 = commandblocktileentity.isPowered();
            commandblocktileentity.setPowered(flag);
            if (!flag1 && !commandblocktileentity.isAutomatic() && commandblocktileentity.getMode() != CommandBlockTileEntity.Mode.SEQUENCE) {
               if (flag) {
                  commandblocktileentity.markConditionMet();
                  pLevel.getBlockTicks().scheduleTick(pPos, this, 1);
               }

            }
         }
      }
   }

   public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      if (tileentity instanceof CommandBlockTileEntity) {
         CommandBlockTileEntity commandblocktileentity = (CommandBlockTileEntity)tileentity;
         CommandBlockLogic commandblocklogic = commandblocktileentity.getCommandBlock();
         boolean flag = !StringUtils.isNullOrEmpty(commandblocklogic.getCommand());
         CommandBlockTileEntity.Mode commandblocktileentity$mode = commandblocktileentity.getMode();
         boolean flag1 = commandblocktileentity.wasConditionMet();
         if (commandblocktileentity$mode == CommandBlockTileEntity.Mode.AUTO) {
            commandblocktileentity.markConditionMet();
            if (flag1) {
               this.execute(pState, pLevel, pPos, commandblocklogic, flag);
            } else if (commandblocktileentity.isConditional()) {
               commandblocklogic.setSuccessCount(0);
            }

            if (commandblocktileentity.isPowered() || commandblocktileentity.isAutomatic()) {
               pLevel.getBlockTicks().scheduleTick(pPos, this, 1);
            }
         } else if (commandblocktileentity$mode == CommandBlockTileEntity.Mode.REDSTONE) {
            if (flag1) {
               this.execute(pState, pLevel, pPos, commandblocklogic, flag);
            } else if (commandblocktileentity.isConditional()) {
               commandblocklogic.setSuccessCount(0);
            }
         }

         pLevel.updateNeighbourForOutputSignal(pPos, this);
      }

   }

   private void execute(BlockState pState, World pLevel, BlockPos pPos, CommandBlockLogic pLogic, boolean pCanTrigger) {
      if (pCanTrigger) {
         pLogic.performCommand(pLevel);
      } else {
         pLogic.setSuccessCount(0);
      }

      executeChain(pLevel, pPos, pState.getValue(FACING));
   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      if (tileentity instanceof CommandBlockTileEntity && pPlayer.canUseGameMasterBlocks()) {
         pPlayer.openCommandBlock((CommandBlockTileEntity)tileentity);
         return ActionResultType.sidedSuccess(pLevel.isClientSide);
      } else {
         return ActionResultType.PASS;
      }
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
      return tileentity instanceof CommandBlockTileEntity ? ((CommandBlockTileEntity)tileentity).getCommandBlock().getSuccessCount() : 0;
   }

   /**
    * Called by ItemBlocks after a block is set in the world, to allow post-place logic
    */
   public void setPlacedBy(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      if (tileentity instanceof CommandBlockTileEntity) {
         CommandBlockTileEntity commandblocktileentity = (CommandBlockTileEntity)tileentity;
         CommandBlockLogic commandblocklogic = commandblocktileentity.getCommandBlock();
         if (pStack.hasCustomHoverName()) {
            commandblocklogic.setName(pStack.getHoverName());
         }

         if (!pLevel.isClientSide) {
            if (pStack.getTagElement("BlockEntityTag") == null) {
               commandblocklogic.setTrackOutput(pLevel.getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK));
               commandblocktileentity.setAutomatic(this == Blocks.CHAIN_COMMAND_BLOCK);
            }

            if (commandblocktileentity.getMode() == CommandBlockTileEntity.Mode.SEQUENCE) {
               boolean flag = pLevel.hasNeighborSignal(pPos);
               commandblocktileentity.setPowered(flag);
            }
         }

      }
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
      pBuilder.add(FACING, CONDITIONAL);
   }

   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      return this.defaultBlockState().setValue(FACING, pContext.getNearestLookingDirection().getOpposite());
   }

   private static void executeChain(World pLevel, BlockPos pPos, Direction pDirection) {
      BlockPos.Mutable blockpos$mutable = pPos.mutable();
      GameRules gamerules = pLevel.getGameRules();

      int i;
      BlockState blockstate;
      for(i = gamerules.getInt(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH); i-- > 0; pDirection = blockstate.getValue(FACING)) {
         blockpos$mutable.move(pDirection);
         blockstate = pLevel.getBlockState(blockpos$mutable);
         Block block = blockstate.getBlock();
         if (!blockstate.is(Blocks.CHAIN_COMMAND_BLOCK)) {
            break;
         }

         TileEntity tileentity = pLevel.getBlockEntity(blockpos$mutable);
         if (!(tileentity instanceof CommandBlockTileEntity)) {
            break;
         }

         CommandBlockTileEntity commandblocktileentity = (CommandBlockTileEntity)tileentity;
         if (commandblocktileentity.getMode() != CommandBlockTileEntity.Mode.SEQUENCE) {
            break;
         }

         if (commandblocktileentity.isPowered() || commandblocktileentity.isAutomatic()) {
            CommandBlockLogic commandblocklogic = commandblocktileentity.getCommandBlock();
            if (commandblocktileentity.markConditionMet()) {
               if (!commandblocklogic.performCommand(pLevel)) {
                  break;
               }

               pLevel.updateNeighbourForOutputSignal(blockpos$mutable, block);
            } else if (commandblocktileentity.isConditional()) {
               commandblocklogic.setSuccessCount(0);
            }
         }
      }

      if (i <= 0) {
         int j = Math.max(gamerules.getInt(GameRules.RULE_MAX_COMMAND_CHAIN_LENGTH), 0);
         LOGGER.warn("Command Block chain tried to execute more than {} steps!", (int)j);
      }

   }
}