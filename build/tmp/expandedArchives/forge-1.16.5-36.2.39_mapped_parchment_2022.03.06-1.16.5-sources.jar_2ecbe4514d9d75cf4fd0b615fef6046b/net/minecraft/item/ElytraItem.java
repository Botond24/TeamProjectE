package net.minecraft.item;

import net.minecraft.block.DispenserBlock;
import net.minecraft.enchantment.IArmorVanishable;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ElytraItem extends Item implements IArmorVanishable {
   public ElytraItem(Item.Properties p_i48507_1_) {
      super(p_i48507_1_);
      DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
   }

   public static boolean isFlyEnabled(ItemStack pElytraStack) {
      return pElytraStack.getDamageValue() < pElytraStack.getMaxDamage() - 1;
   }

   /**
    * Return whether this item is repairable in an anvil.
    */
   public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair) {
      return pRepair.getItem() == Items.PHANTOM_MEMBRANE;
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      EquipmentSlotType equipmentslottype = MobEntity.getEquipmentSlotForItem(itemstack);
      ItemStack itemstack1 = pPlayer.getItemBySlot(equipmentslottype);
      if (itemstack1.isEmpty()) {
         pPlayer.setItemSlot(equipmentslottype, itemstack.copy());
         itemstack.setCount(0);
         return ActionResult.sidedSuccess(itemstack, pLevel.isClientSide());
      } else {
         return ActionResult.fail(itemstack);
      }
   }

   @Override
   public boolean canElytraFly(ItemStack stack, net.minecraft.entity.LivingEntity entity) {
      return ElytraItem.isFlyEnabled(stack);
   }

   @Override
   public boolean elytraFlightTick(ItemStack stack, net.minecraft.entity.LivingEntity entity, int flightTicks) {
      if (!entity.level.isClientSide && (flightTicks + 1) % 20 == 0) {
         stack.hurtAndBreak(1, entity, e -> e.broadcastBreakEvent(net.minecraft.inventory.EquipmentSlotType.CHEST));
      }
      return true;
   }
}
