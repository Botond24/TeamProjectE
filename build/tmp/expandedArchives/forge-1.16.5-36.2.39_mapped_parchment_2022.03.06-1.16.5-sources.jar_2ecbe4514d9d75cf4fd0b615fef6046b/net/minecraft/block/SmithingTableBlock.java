package net.minecraft.block;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.inventory.container.SmithingTableContainer;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class SmithingTableBlock extends CraftingTableBlock {
   private static final ITextComponent CONTAINER_TITLE = new TranslationTextComponent("container.upgrade");

   public SmithingTableBlock(AbstractBlock.Properties p_i49974_1_) {
      super(p_i49974_1_);
   }

   public INamedContainerProvider getMenuProvider(BlockState pState, World pLevel, BlockPos pPos) {
      return new SimpleNamedContainerProvider((p_235576_2_, p_235576_3_, p_235576_4_) -> {
         return new SmithingTableContainer(p_235576_2_, p_235576_3_, IWorldPosCallable.create(pLevel, pPos));
      }, CONTAINER_TITLE);
   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      if (pLevel.isClientSide) {
         return ActionResultType.SUCCESS;
      } else {
         pPlayer.openMenu(pState.getMenuProvider(pLevel, pPos));
         pPlayer.awardStat(Stats.INTERACT_WITH_SMITHING_TABLE);
         return ActionResultType.CONSUME;
      }
   }
}