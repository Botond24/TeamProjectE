package net.minecraft.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.EyeOfEnderEntity;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;

public class EnderEyeItem extends Item {
   public EnderEyeItem(Item.Properties p_i48502_1_) {
      super(p_i48502_1_);
   }

   /**
    * Called when this item is used when targetting a Block
    */
   public ActionResultType useOn(ItemUseContext pContext) {
      World world = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      BlockState blockstate = world.getBlockState(blockpos);
      if (blockstate.is(Blocks.END_PORTAL_FRAME) && !blockstate.getValue(EndPortalFrameBlock.HAS_EYE)) {
         if (world.isClientSide) {
            return ActionResultType.SUCCESS;
         } else {
            BlockState blockstate1 = blockstate.setValue(EndPortalFrameBlock.HAS_EYE, Boolean.valueOf(true));
            Block.pushEntitiesUp(blockstate, blockstate1, world, blockpos);
            world.setBlock(blockpos, blockstate1, 2);
            world.updateNeighbourForOutputSignal(blockpos, Blocks.END_PORTAL_FRAME);
            pContext.getItemInHand().shrink(1);
            world.levelEvent(1503, blockpos, 0);
            BlockPattern.PatternHelper blockpattern$patternhelper = EndPortalFrameBlock.getOrCreatePortalShape().find(world, blockpos);
            if (blockpattern$patternhelper != null) {
               BlockPos blockpos1 = blockpattern$patternhelper.getFrontTopLeft().offset(-3, 0, -3);

               for(int i = 0; i < 3; ++i) {
                  for(int j = 0; j < 3; ++j) {
                     world.setBlock(blockpos1.offset(i, 0, j), Blocks.END_PORTAL.defaultBlockState(), 2);
                  }
               }

               world.globalLevelEvent(1038, blockpos1.offset(1, 0, 1), 0);
            }

            return ActionResultType.CONSUME;
         }
      } else {
         return ActionResultType.PASS;
      }
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      RayTraceResult raytraceresult = getPlayerPOVHitResult(pLevel, pPlayer, RayTraceContext.FluidMode.NONE);
      if (raytraceresult.getType() == RayTraceResult.Type.BLOCK && pLevel.getBlockState(((BlockRayTraceResult)raytraceresult).getBlockPos()).is(Blocks.END_PORTAL_FRAME)) {
         return ActionResult.pass(itemstack);
      } else {
         pPlayer.startUsingItem(pHand);
         if (pLevel instanceof ServerWorld) {
            BlockPos blockpos = ((ServerWorld)pLevel).getChunkSource().getGenerator().findNearestMapFeature((ServerWorld)pLevel, Structure.STRONGHOLD, pPlayer.blockPosition(), 100, false);
            if (blockpos != null) {
               EyeOfEnderEntity eyeofenderentity = new EyeOfEnderEntity(pLevel, pPlayer.getX(), pPlayer.getY(0.5D), pPlayer.getZ());
               eyeofenderentity.setItem(itemstack);
               eyeofenderentity.signalTo(blockpos);
               pLevel.addFreshEntity(eyeofenderentity);
               if (pPlayer instanceof ServerPlayerEntity) {
                  CriteriaTriggers.USED_ENDER_EYE.trigger((ServerPlayerEntity)pPlayer, blockpos);
               }

               pLevel.playSound((PlayerEntity)null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.ENDER_EYE_LAUNCH, SoundCategory.NEUTRAL, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
               pLevel.levelEvent((PlayerEntity)null, 1003, pPlayer.blockPosition(), 0);
               if (!pPlayer.abilities.instabuild) {
                  itemstack.shrink(1);
               }

               pPlayer.awardStat(Stats.ITEM_USED.get(this));
               pPlayer.swing(pHand, true);
               return ActionResult.success(itemstack);
            }
         }

         return ActionResult.consume(itemstack);
      }
   }
}