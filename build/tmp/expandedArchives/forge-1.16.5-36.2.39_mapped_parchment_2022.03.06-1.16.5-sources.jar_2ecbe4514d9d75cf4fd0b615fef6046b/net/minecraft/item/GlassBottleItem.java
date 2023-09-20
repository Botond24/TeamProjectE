package net.minecraft.item;

import java.util.List;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DrinkHelper;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class GlassBottleItem extends Item {
   public GlassBottleItem(Item.Properties p_i48523_1_) {
      super(p_i48523_1_);
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pHand) {
      List<AreaEffectCloudEntity> list = pLevel.getEntitiesOfClass(AreaEffectCloudEntity.class, pPlayer.getBoundingBox().inflate(2.0D), (p_210311_0_) -> {
         return p_210311_0_ != null && p_210311_0_.isAlive() && p_210311_0_.getOwner() instanceof EnderDragonEntity;
      });
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (!list.isEmpty()) {
         AreaEffectCloudEntity areaeffectcloudentity = list.get(0);
         areaeffectcloudentity.setRadius(areaeffectcloudentity.getRadius() - 0.5F);
         pLevel.playSound((PlayerEntity)null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundCategory.NEUTRAL, 1.0F, 1.0F);
         return ActionResult.sidedSuccess(this.turnBottleIntoItem(itemstack, pPlayer, new ItemStack(Items.DRAGON_BREATH)), pLevel.isClientSide());
      } else {
         RayTraceResult raytraceresult = getPlayerPOVHitResult(pLevel, pPlayer, RayTraceContext.FluidMode.SOURCE_ONLY);
         if (raytraceresult.getType() == RayTraceResult.Type.MISS) {
            return ActionResult.pass(itemstack);
         } else {
            if (raytraceresult.getType() == RayTraceResult.Type.BLOCK) {
               BlockPos blockpos = ((BlockRayTraceResult)raytraceresult).getBlockPos();
               if (!pLevel.mayInteract(pPlayer, blockpos)) {
                  return ActionResult.pass(itemstack);
               }

               if (pLevel.getFluidState(blockpos).is(FluidTags.WATER)) {
                  pLevel.playSound(pPlayer, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                  return ActionResult.sidedSuccess(this.turnBottleIntoItem(itemstack, pPlayer, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER)), pLevel.isClientSide());
               }
            }

            return ActionResult.pass(itemstack);
         }
      }
   }

   protected ItemStack turnBottleIntoItem(ItemStack pBottleStack, PlayerEntity pPlayer, ItemStack pFilledBottleStack) {
      pPlayer.awardStat(Stats.ITEM_USED.get(this));
      return DrinkHelper.createFilledResult(pBottleStack, pPlayer, pFilledBottleStack);
   }
}