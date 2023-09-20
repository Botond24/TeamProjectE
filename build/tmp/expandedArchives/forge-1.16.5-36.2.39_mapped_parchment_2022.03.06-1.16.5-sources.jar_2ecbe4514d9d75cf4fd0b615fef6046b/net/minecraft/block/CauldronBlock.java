package net.minecraft.block;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BannerItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.IDyeableArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.pathfinding.PathType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class CauldronBlock extends Block {
   public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_CAULDRON;
   private static final VoxelShape INSIDE = box(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D);
   protected static final VoxelShape SHAPE = VoxelShapes.join(VoxelShapes.block(), VoxelShapes.or(box(0.0D, 0.0D, 4.0D, 16.0D, 3.0D, 12.0D), box(4.0D, 0.0D, 0.0D, 12.0D, 3.0D, 16.0D), box(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D), INSIDE), IBooleanFunction.ONLY_FIRST);

   public CauldronBlock(AbstractBlock.Properties p_i48431_1_) {
      super(p_i48431_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(0)));
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return SHAPE;
   }

   public VoxelShape getInteractionShape(BlockState pState, IBlockReader pLevel, BlockPos pPos) {
      return INSIDE;
   }

   public void entityInside(BlockState pState, World pLevel, BlockPos pPos, Entity pEntity) {
      int i = pState.getValue(LEVEL);
      float f = (float)pPos.getY() + (6.0F + (float)(3 * i)) / 16.0F;
      if (!pLevel.isClientSide && pEntity.isOnFire() && i > 0 && pEntity.getY() <= (double)f) {
         pEntity.clearFire();
         this.setWaterLevel(pLevel, pPos, pState, i - 1);
      }

   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (itemstack.isEmpty()) {
         return ActionResultType.PASS;
      } else {
         int i = pState.getValue(LEVEL);
         Item item = itemstack.getItem();
         if (item == Items.WATER_BUCKET) {
            if (i < 3 && !pLevel.isClientSide) {
               if (!pPlayer.abilities.instabuild) {
                  pPlayer.setItemInHand(pHand, new ItemStack(Items.BUCKET));
               }

               pPlayer.awardStat(Stats.FILL_CAULDRON);
               this.setWaterLevel(pLevel, pPos, pState, 3);
               pLevel.playSound((PlayerEntity)null, pPos, SoundEvents.BUCKET_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            return ActionResultType.sidedSuccess(pLevel.isClientSide);
         } else if (item == Items.BUCKET) {
            if (i == 3 && !pLevel.isClientSide) {
               if (!pPlayer.abilities.instabuild) {
                  itemstack.shrink(1);
                  if (itemstack.isEmpty()) {
                     pPlayer.setItemInHand(pHand, new ItemStack(Items.WATER_BUCKET));
                  } else if (!pPlayer.inventory.add(new ItemStack(Items.WATER_BUCKET))) {
                     pPlayer.drop(new ItemStack(Items.WATER_BUCKET), false);
                  }
               }

               pPlayer.awardStat(Stats.USE_CAULDRON);
               this.setWaterLevel(pLevel, pPos, pState, 0);
               pLevel.playSound((PlayerEntity)null, pPos, SoundEvents.BUCKET_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            return ActionResultType.sidedSuccess(pLevel.isClientSide);
         } else if (item == Items.GLASS_BOTTLE) {
            if (i > 0 && !pLevel.isClientSide) {
               if (!pPlayer.abilities.instabuild) {
                  ItemStack itemstack4 = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
                  pPlayer.awardStat(Stats.USE_CAULDRON);
                  itemstack.shrink(1);
                  if (itemstack.isEmpty()) {
                     pPlayer.setItemInHand(pHand, itemstack4);
                  } else if (!pPlayer.inventory.add(itemstack4)) {
                     pPlayer.drop(itemstack4, false);
                  } else if (pPlayer instanceof ServerPlayerEntity) {
                     ((ServerPlayerEntity)pPlayer).refreshContainer(pPlayer.inventoryMenu);
                  }
               }

               pLevel.playSound((PlayerEntity)null, pPos, SoundEvents.BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
               this.setWaterLevel(pLevel, pPos, pState, i - 1);
            }

            return ActionResultType.sidedSuccess(pLevel.isClientSide);
         } else if (item == Items.POTION && PotionUtils.getPotion(itemstack) == Potions.WATER) {
            if (i < 3 && !pLevel.isClientSide) {
               if (!pPlayer.abilities.instabuild) {
                  ItemStack itemstack3 = new ItemStack(Items.GLASS_BOTTLE);
                  pPlayer.awardStat(Stats.USE_CAULDRON);
                  pPlayer.setItemInHand(pHand, itemstack3);
                  if (pPlayer instanceof ServerPlayerEntity) {
                     ((ServerPlayerEntity)pPlayer).refreshContainer(pPlayer.inventoryMenu);
                  }
               }

               pLevel.playSound((PlayerEntity)null, pPos, SoundEvents.BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
               this.setWaterLevel(pLevel, pPos, pState, i + 1);
            }

            return ActionResultType.sidedSuccess(pLevel.isClientSide);
         } else {
            if (i > 0 && item instanceof IDyeableArmorItem) {
               IDyeableArmorItem idyeablearmoritem = (IDyeableArmorItem)item;
               if (idyeablearmoritem.hasCustomColor(itemstack) && !pLevel.isClientSide) {
                  idyeablearmoritem.clearColor(itemstack);
                  this.setWaterLevel(pLevel, pPos, pState, i - 1);
                  pPlayer.awardStat(Stats.CLEAN_ARMOR);
                  return ActionResultType.SUCCESS;
               }
            }

            if (i > 0 && item instanceof BannerItem) {
               if (BannerTileEntity.getPatternCount(itemstack) > 0 && !pLevel.isClientSide) {
                  ItemStack itemstack2 = itemstack.copy();
                  itemstack2.setCount(1);
                  BannerTileEntity.removeLastPattern(itemstack2);
                  pPlayer.awardStat(Stats.CLEAN_BANNER);
                  if (!pPlayer.abilities.instabuild) {
                     itemstack.shrink(1);
                     this.setWaterLevel(pLevel, pPos, pState, i - 1);
                  }

                  if (itemstack.isEmpty()) {
                     pPlayer.setItemInHand(pHand, itemstack2);
                  } else if (!pPlayer.inventory.add(itemstack2)) {
                     pPlayer.drop(itemstack2, false);
                  } else if (pPlayer instanceof ServerPlayerEntity) {
                     ((ServerPlayerEntity)pPlayer).refreshContainer(pPlayer.inventoryMenu);
                  }
               }

               return ActionResultType.sidedSuccess(pLevel.isClientSide);
            } else if (i > 0 && item instanceof BlockItem) {
               Block block = ((BlockItem)item).getBlock();
               if (block instanceof ShulkerBoxBlock && !pLevel.isClientSide()) {
                  ItemStack itemstack1 = new ItemStack(Blocks.SHULKER_BOX, 1);
                  if (itemstack.hasTag()) {
                     itemstack1.setTag(itemstack.getTag().copy());
                  }

                  pPlayer.setItemInHand(pHand, itemstack1);
                  this.setWaterLevel(pLevel, pPos, pState, i - 1);
                  pPlayer.awardStat(Stats.CLEAN_SHULKER_BOX);
                  return ActionResultType.SUCCESS;
               } else {
                  return ActionResultType.CONSUME;
               }
            } else {
               return ActionResultType.PASS;
            }
         }
      }
   }

   public void setWaterLevel(World p_176590_1_, BlockPos p_176590_2_, BlockState p_176590_3_, int p_176590_4_) {
      p_176590_1_.setBlock(p_176590_2_, p_176590_3_.setValue(LEVEL, Integer.valueOf(MathHelper.clamp(p_176590_4_, 0, 3))), 2);
      p_176590_1_.updateNeighbourForOutputSignal(p_176590_2_, this);
   }

   public void handleRain(World p_176224_1_, BlockPos p_176224_2_) {
      if (p_176224_1_.random.nextInt(20) == 1) {
         float f = p_176224_1_.getBiome(p_176224_2_).getTemperature(p_176224_2_);
         if (!(f < 0.15F)) {
            BlockState blockstate = p_176224_1_.getBlockState(p_176224_2_);
            if (blockstate.getValue(LEVEL) < 3) {
               p_176224_1_.setBlock(p_176224_2_, blockstate.cycle(LEVEL), 2);
            }

         }
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
      return pBlockState.getValue(LEVEL);
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(LEVEL);
   }

   public boolean isPathfindable(BlockState pState, IBlockReader pLevel, BlockPos pPos, PathType pType) {
      return false;
   }
}