package net.minecraft.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ShearsItem extends Item {
   public ShearsItem(Item.Properties p_i48471_1_) {
      super(p_i48471_1_);
   }

   /**
    * Called when a Block is destroyed using this Item. Return true to trigger the "Use Item" statistic.
    */
   public boolean mineBlock(ItemStack pStack, World pLevel, BlockState pState, BlockPos pPos, LivingEntity pEntityLiving) {
      if (!pLevel.isClientSide && !pState.getBlock().is(BlockTags.FIRE)) {
         pStack.hurtAndBreak(1, pEntityLiving, (p_220036_0_) -> {
            p_220036_0_.broadcastBreakEvent(EquipmentSlotType.MAINHAND);
         });
      }

      return !pState.is(BlockTags.LEAVES) && !pState.is(Blocks.COBWEB) && !pState.is(Blocks.GRASS) && !pState.is(Blocks.FERN) && !pState.is(Blocks.DEAD_BUSH) && !pState.is(Blocks.VINE) && !pState.is(Blocks.TRIPWIRE) && !pState.is(BlockTags.WOOL) ? super.mineBlock(pStack, pLevel, pState, pPos, pEntityLiving) : true;
   }

   /**
    * Check whether this Item can harvest the given Block
    */
   public boolean isCorrectToolForDrops(BlockState pBlock) {
      return pBlock.is(Blocks.COBWEB) || pBlock.is(Blocks.REDSTONE_WIRE) || pBlock.is(Blocks.TRIPWIRE);
   }

   public float getDestroySpeed(ItemStack pStack, BlockState pState) {
      if (!pState.is(Blocks.COBWEB) && !pState.is(BlockTags.LEAVES)) {
         return pState.is(BlockTags.WOOL) ? 5.0F : super.getDestroySpeed(pStack, pState);
      } else {
         return 15.0F;
      }
   }

   /**
    * Returns true if the item can be used on the given entity, e.g. shears on sheep.
    */
   @Override
   public net.minecraft.util.ActionResultType interactLivingEntity(ItemStack stack, net.minecraft.entity.player.PlayerEntity playerIn, LivingEntity entity, net.minecraft.util.Hand hand) {
      if (entity.level.isClientSide) return net.minecraft.util.ActionResultType.PASS;
      if (entity instanceof net.minecraftforge.common.IForgeShearable) {
          net.minecraftforge.common.IForgeShearable target = (net.minecraftforge.common.IForgeShearable)entity;
         BlockPos pos = new BlockPos(entity.getX(), entity.getY(), entity.getZ());
         if (target.isShearable(stack, entity.level, pos)) {
            java.util.List<ItemStack> drops = target.onSheared(playerIn, stack, entity.level, pos,
                    net.minecraft.enchantment.EnchantmentHelper.getItemEnchantmentLevel(net.minecraft.enchantment.Enchantments.BLOCK_FORTUNE, stack));
            java.util.Random rand = new java.util.Random();
            drops.forEach(d -> {
               net.minecraft.entity.item.ItemEntity ent = entity.spawnAtLocation(d, 1.0F);
               ent.setDeltaMovement(ent.getDeltaMovement().add((double)((rand.nextFloat() - rand.nextFloat()) * 0.1F), (double)(rand.nextFloat() * 0.05F), (double)((rand.nextFloat() - rand.nextFloat()) * 0.1F)));
            });
            stack.hurtAndBreak(1, entity, e -> e.broadcastBreakEvent(hand));
         }
         return net.minecraft.util.ActionResultType.SUCCESS;
      }
      return net.minecraft.util.ActionResultType.PASS;
   }
}
