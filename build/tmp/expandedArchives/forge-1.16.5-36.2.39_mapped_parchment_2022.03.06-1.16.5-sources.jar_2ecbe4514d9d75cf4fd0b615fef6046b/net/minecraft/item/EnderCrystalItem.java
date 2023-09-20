package net.minecraft.item;

import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.end.DragonFightManager;
import net.minecraft.world.server.ServerWorld;

public class EnderCrystalItem extends Item {
   public EnderCrystalItem(Item.Properties p_i48503_1_) {
      super(p_i48503_1_);
   }

   /**
    * Called when this item is used when targetting a Block
    */
   public ActionResultType useOn(ItemUseContext pContext) {
      World world = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      BlockState blockstate = world.getBlockState(blockpos);
      if (!blockstate.is(Blocks.OBSIDIAN) && !blockstate.is(Blocks.BEDROCK)) {
         return ActionResultType.FAIL;
      } else {
         BlockPos blockpos1 = blockpos.above();
         if (!world.isEmptyBlock(blockpos1)) {
            return ActionResultType.FAIL;
         } else {
            double d0 = (double)blockpos1.getX();
            double d1 = (double)blockpos1.getY();
            double d2 = (double)blockpos1.getZ();
            List<Entity> list = world.getEntities((Entity)null, new AxisAlignedBB(d0, d1, d2, d0 + 1.0D, d1 + 2.0D, d2 + 1.0D));
            if (!list.isEmpty()) {
               return ActionResultType.FAIL;
            } else {
               if (world instanceof ServerWorld) {
                  EnderCrystalEntity endercrystalentity = new EnderCrystalEntity(world, d0 + 0.5D, d1, d2 + 0.5D);
                  endercrystalentity.setShowBottom(false);
                  world.addFreshEntity(endercrystalentity);
                  DragonFightManager dragonfightmanager = ((ServerWorld)world).dragonFight();
                  if (dragonfightmanager != null) {
                     dragonfightmanager.tryRespawn();
                  }
               }

               pContext.getItemInHand().shrink(1);
               return ActionResultType.sidedSuccess(world.isClientSide);
            }
         }
      }
   }

   /**
    * Returns true if this item has an enchantment glint. By default, this returns <code>stack.isItemEnchanted()</code>,
    * but other items can override it (for instance, written books always return true).
    * 
    * Note that if you override this method, you generally want to also call the super version (on {@link Item}) to get
    * the glint for enchanted items. Of course, that is unnecessary if the overwritten version always returns true.
    */
   public boolean isFoil(ItemStack pStack) {
      return true;
   }
}