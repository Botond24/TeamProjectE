package net.minecraft.item;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ShovelItem extends ToolItem {
   private static final Set<Block> DIGGABLES = Sets.newHashSet(Blocks.CLAY, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL, Blocks.FARMLAND, Blocks.GRASS_BLOCK, Blocks.GRAVEL, Blocks.MYCELIUM, Blocks.SAND, Blocks.RED_SAND, Blocks.SNOW_BLOCK, Blocks.SNOW, Blocks.SOUL_SAND, Blocks.GRASS_PATH, Blocks.WHITE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE_POWDER, Blocks.LIME_CONCRETE_POWDER, Blocks.PINK_CONCRETE_POWDER, Blocks.GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.CYAN_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE_POWDER, Blocks.BROWN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE_POWDER, Blocks.RED_CONCRETE_POWDER, Blocks.BLACK_CONCRETE_POWDER, Blocks.SOUL_SOIL);
   /** Map used to lookup shovel right click interactions */
   protected static final Map<Block, BlockState> FLATTENABLES = Maps.newHashMap(ImmutableMap.of(Blocks.GRASS_BLOCK, Blocks.GRASS_PATH.defaultBlockState()));

   public ShovelItem(IItemTier pTier, float pAttackDamageModifier, float pAttackSpeedModifier, Item.Properties pProperties) {
      super(pAttackDamageModifier, pAttackSpeedModifier, pTier, DIGGABLES, pProperties.addToolType(net.minecraftforge.common.ToolType.SHOVEL, pTier.getLevel()));
   }

   /**
    * Check whether this Item can harvest the given Block
    */
   public boolean isCorrectToolForDrops(BlockState pBlock) {
      return pBlock.is(Blocks.SNOW) || pBlock.is(Blocks.SNOW_BLOCK);
   }

   /**
    * Called when this item is used when targetting a Block
    */
   public ActionResultType useOn(ItemUseContext pContext) {
      World world = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      BlockState blockstate = world.getBlockState(blockpos);
      if (pContext.getClickedFace() == Direction.DOWN) {
         return ActionResultType.PASS;
      } else {
         PlayerEntity playerentity = pContext.getPlayer();
         BlockState blockstate1 = blockstate.getToolModifiedState(world, blockpos, playerentity, pContext.getItemInHand(), net.minecraftforge.common.ToolType.SHOVEL);
         BlockState blockstate2 = null;
         if (blockstate1 != null && world.isEmptyBlock(blockpos.above())) {
            world.playSound(playerentity, blockpos, SoundEvents.SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
            blockstate2 = blockstate1;
         } else if (blockstate.getBlock() instanceof CampfireBlock && blockstate.getValue(CampfireBlock.LIT)) {
            if (!world.isClientSide()) {
               world.levelEvent((PlayerEntity)null, 1009, blockpos, 0);
            }

            CampfireBlock.dowse(world, blockpos, blockstate);
            blockstate2 = blockstate.setValue(CampfireBlock.LIT, Boolean.valueOf(false));
         }

         if (blockstate2 != null) {
            if (!world.isClientSide) {
               world.setBlock(blockpos, blockstate2, 11);
               if (playerentity != null) {
                  pContext.getItemInHand().hurtAndBreak(1, playerentity, (p_220041_1_) -> {
                     p_220041_1_.broadcastBreakEvent(pContext.getHand());
                  });
               }
            }

            return ActionResultType.sidedSuccess(world.isClientSide);
         } else {
            return ActionResultType.PASS;
         }
      }
   }

   @javax.annotation.Nullable
   public static BlockState getShovelPathingState(BlockState originalState) {
      return FLATTENABLES.get(originalState.getBlock());
   }
}
