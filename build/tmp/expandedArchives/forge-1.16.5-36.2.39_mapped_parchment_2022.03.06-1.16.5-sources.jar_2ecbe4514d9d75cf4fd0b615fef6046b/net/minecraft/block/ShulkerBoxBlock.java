package net.minecraft.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.material.PushReaction;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.piglin.PiglinTasks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.ShulkerBoxTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.ShulkerAABBHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ShulkerBoxBlock extends ContainerBlock {
   public static final EnumProperty<Direction> FACING = DirectionalBlock.FACING;
   public static final ResourceLocation CONTENTS = new ResourceLocation("contents");
   @Nullable
   private final DyeColor color;

   public ShulkerBoxBlock(@Nullable DyeColor pColor, AbstractBlock.Properties pProperties) {
      super(pProperties);
      this.color = pColor;
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
   }

   public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
      return new ShulkerBoxTileEntity(this.color);
   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link IBlockState#getRenderType()} whenever possible. Implementing/overriding is fine.
    */
   public BlockRenderType getRenderShape(BlockState pState) {
      return BlockRenderType.ENTITYBLOCK_ANIMATED;
   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      if (pLevel.isClientSide) {
         return ActionResultType.SUCCESS;
      } else if (pPlayer.isSpectator()) {
         return ActionResultType.CONSUME;
      } else {
         TileEntity tileentity = pLevel.getBlockEntity(pPos);
         if (tileentity instanceof ShulkerBoxTileEntity) {
            ShulkerBoxTileEntity shulkerboxtileentity = (ShulkerBoxTileEntity)tileentity;
            boolean flag;
            if (shulkerboxtileentity.getAnimationStatus() == ShulkerBoxTileEntity.AnimationStatus.CLOSED) {
               Direction direction = pState.getValue(FACING);
               flag = pLevel.noCollision(ShulkerAABBHelper.openBoundingBox(pPos, direction));
            } else {
               flag = true;
            }

            if (flag) {
               pPlayer.openMenu(shulkerboxtileentity);
               pPlayer.awardStat(Stats.OPEN_SHULKER_BOX);
               PiglinTasks.angerNearbyPiglins(pPlayer, true);
            }

            return ActionResultType.CONSUME;
         } else {
            return ActionResultType.PASS;
         }
      }
   }

   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      return this.defaultBlockState().setValue(FACING, pContext.getClickedFace());
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING);
   }

   /**
    * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually collect
    * this block
    */
   public void playerWillDestroy(World pLevel, BlockPos pPos, BlockState pState, PlayerEntity pPlayer) {
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      if (tileentity instanceof ShulkerBoxTileEntity) {
         ShulkerBoxTileEntity shulkerboxtileentity = (ShulkerBoxTileEntity)tileentity;
         if (!pLevel.isClientSide && pPlayer.isCreative() && !shulkerboxtileentity.isEmpty()) {
            ItemStack itemstack = getColoredItemStack(this.getColor());
            CompoundNBT compoundnbt = shulkerboxtileentity.saveToTag(new CompoundNBT());
            if (!compoundnbt.isEmpty()) {
               itemstack.addTagElement("BlockEntityTag", compoundnbt);
            }

            if (shulkerboxtileentity.hasCustomName()) {
               itemstack.setHoverName(shulkerboxtileentity.getCustomName());
            }

            ItemEntity itementity = new ItemEntity(pLevel, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, itemstack);
            itementity.setDefaultPickUpDelay();
            pLevel.addFreshEntity(itementity);
         } else {
            shulkerboxtileentity.unpackLootTable(pPlayer);
         }
      }

      super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
   }

   public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
      TileEntity tileentity = pBuilder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
      if (tileentity instanceof ShulkerBoxTileEntity) {
         ShulkerBoxTileEntity shulkerboxtileentity = (ShulkerBoxTileEntity)tileentity;
         pBuilder = pBuilder.withDynamicDrop(CONTENTS, (p_220168_1_, p_220168_2_) -> {
            for(int i = 0; i < shulkerboxtileentity.getContainerSize(); ++i) {
               p_220168_2_.accept(shulkerboxtileentity.getItem(i));
            }

         });
      }

      return super.getDrops(pState, pBuilder);
   }

   /**
    * Called by ItemBlocks after a block is set in the world, to allow post-place logic
    */
   public void setPlacedBy(World pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
      if (pStack.hasCustomHoverName()) {
         TileEntity tileentity = pLevel.getBlockEntity(pPos);
         if (tileentity instanceof ShulkerBoxTileEntity) {
            ((ShulkerBoxTileEntity)tileentity).setCustomName(pStack.getHoverName());
         }
      }

   }

   public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pState.is(pNewState.getBlock())) {
         TileEntity tileentity = pLevel.getBlockEntity(pPos);
         if (tileentity instanceof ShulkerBoxTileEntity) {
            pLevel.updateNeighbourForOutputSignal(pPos, pState.getBlock());
         }

         super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(ItemStack pStack, @Nullable IBlockReader pLevel, List<ITextComponent> pTooltip, ITooltipFlag pFlag) {
      super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
      CompoundNBT compoundnbt = pStack.getTagElement("BlockEntityTag");
      if (compoundnbt != null) {
         if (compoundnbt.contains("LootTable", 8)) {
            pTooltip.add(new StringTextComponent("???????"));
         }

         if (compoundnbt.contains("Items", 9)) {
            NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
            ItemStackHelper.loadAllItems(compoundnbt, nonnulllist);
            int i = 0;
            int j = 0;

            for(ItemStack itemstack : nonnulllist) {
               if (!itemstack.isEmpty()) {
                  ++j;
                  if (i <= 4) {
                     ++i;
                     IFormattableTextComponent iformattabletextcomponent = itemstack.getHoverName().copy();
                     iformattabletextcomponent.append(" x").append(String.valueOf(itemstack.getCount()));
                     pTooltip.add(iformattabletextcomponent);
                  }
               }
            }

            if (j - i > 0) {
               pTooltip.add((new TranslationTextComponent("container.shulkerBox.more", j - i)).withStyle(TextFormatting.ITALIC));
            }
         }
      }

   }

   /**
    * @deprecated call via {@link IBlockState#getMobilityFlag()} whenever possible. Implementing/overriding is fine.
    */
   public PushReaction getPistonPushReaction(BlockState pState) {
      return PushReaction.DESTROY;
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      return tileentity instanceof ShulkerBoxTileEntity ? VoxelShapes.create(((ShulkerBoxTileEntity)tileentity).getBoundingBox(pState)) : VoxelShapes.block();
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
      return Container.getRedstoneSignalFromContainer((IInventory)pLevel.getBlockEntity(pPos));
   }

   public ItemStack getCloneItemStack(IBlockReader pLevel, BlockPos pPos, BlockState pState) {
      ItemStack itemstack = super.getCloneItemStack(pLevel, pPos, pState);
      ShulkerBoxTileEntity shulkerboxtileentity = (ShulkerBoxTileEntity)pLevel.getBlockEntity(pPos);
      CompoundNBT compoundnbt = shulkerboxtileentity.saveToTag(new CompoundNBT());
      if (!compoundnbt.isEmpty()) {
         itemstack.addTagElement("BlockEntityTag", compoundnbt);
      }

      return itemstack;
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public static DyeColor getColorFromItem(Item pItem) {
      return getColorFromBlock(Block.byItem(pItem));
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public static DyeColor getColorFromBlock(Block pBlock) {
      return pBlock instanceof ShulkerBoxBlock ? ((ShulkerBoxBlock)pBlock).getColor() : null;
   }

   public static Block getBlockByColor(@Nullable DyeColor pColor) {
      if (pColor == null) {
         return Blocks.SHULKER_BOX;
      } else {
         switch(pColor) {
         case WHITE:
            return Blocks.WHITE_SHULKER_BOX;
         case ORANGE:
            return Blocks.ORANGE_SHULKER_BOX;
         case MAGENTA:
            return Blocks.MAGENTA_SHULKER_BOX;
         case LIGHT_BLUE:
            return Blocks.LIGHT_BLUE_SHULKER_BOX;
         case YELLOW:
            return Blocks.YELLOW_SHULKER_BOX;
         case LIME:
            return Blocks.LIME_SHULKER_BOX;
         case PINK:
            return Blocks.PINK_SHULKER_BOX;
         case GRAY:
            return Blocks.GRAY_SHULKER_BOX;
         case LIGHT_GRAY:
            return Blocks.LIGHT_GRAY_SHULKER_BOX;
         case CYAN:
            return Blocks.CYAN_SHULKER_BOX;
         case PURPLE:
         default:
            return Blocks.PURPLE_SHULKER_BOX;
         case BLUE:
            return Blocks.BLUE_SHULKER_BOX;
         case BROWN:
            return Blocks.BROWN_SHULKER_BOX;
         case GREEN:
            return Blocks.GREEN_SHULKER_BOX;
         case RED:
            return Blocks.RED_SHULKER_BOX;
         case BLACK:
            return Blocks.BLACK_SHULKER_BOX;
         }
      }
   }

   @Nullable
   public DyeColor getColor() {
      return this.color;
   }

   public static ItemStack getColoredItemStack(@Nullable DyeColor pColor) {
      return new ItemStack(getBlockByColor(pColor));
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
}