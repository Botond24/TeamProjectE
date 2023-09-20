package net.minecraft.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.IVanishable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class TridentItem extends Item implements IVanishable {
   private final Multimap<Attribute, AttributeModifier> defaultModifiers;

   public TridentItem(Item.Properties p_i48788_1_) {
      super(p_i48788_1_);
      Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
      builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", 8.0D, AttributeModifier.Operation.ADDITION));
      builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", (double)-2.9F, AttributeModifier.Operation.ADDITION));
      this.defaultModifiers = builder.build();
   }

   public boolean canAttackBlock(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer) {
      return !pPlayer.isCreative();
   }

   /**
    * returns the action that specifies what animation to play when the items is being used
    */
   public UseAction getUseAnimation(ItemStack pStack) {
      return UseAction.SPEAR;
   }

   /**
    * How long it takes to use or consume an item
    */
   public int getUseDuration(ItemStack pStack) {
      return 72000;
   }

   /**
    * Called when the player stops using an Item (stops holding the right mouse button).
    */
   public void releaseUsing(ItemStack pStack, World pLevel, LivingEntity pEntityLiving, int pTimeLeft) {
      if (pEntityLiving instanceof PlayerEntity) {
         PlayerEntity playerentity = (PlayerEntity)pEntityLiving;
         int i = this.getUseDuration(pStack) - pTimeLeft;
         if (i >= 10) {
            int j = EnchantmentHelper.getRiptide(pStack);
            if (j <= 0 || playerentity.isInWaterOrRain()) {
               if (!pLevel.isClientSide) {
                  pStack.hurtAndBreak(1, playerentity, (p_220047_1_) -> {
                     p_220047_1_.broadcastBreakEvent(pEntityLiving.getUsedItemHand());
                  });
                  if (j == 0) {
                     TridentEntity tridententity = new TridentEntity(pLevel, playerentity, pStack);
                     tridententity.shootFromRotation(playerentity, playerentity.xRot, playerentity.yRot, 0.0F, 2.5F + (float)j * 0.5F, 1.0F);
                     if (playerentity.abilities.instabuild) {
                        tridententity.pickup = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
                     }

                     pLevel.addFreshEntity(tridententity);
                     pLevel.playSound((PlayerEntity)null, tridententity, SoundEvents.TRIDENT_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);
                     if (!playerentity.abilities.instabuild) {
                        playerentity.inventory.removeItem(pStack);
                     }
                  }
               }

               playerentity.awardStat(Stats.ITEM_USED.get(this));
               if (j > 0) {
                  float f7 = playerentity.yRot;
                  float f = playerentity.xRot;
                  float f1 = -MathHelper.sin(f7 * ((float)Math.PI / 180F)) * MathHelper.cos(f * ((float)Math.PI / 180F));
                  float f2 = -MathHelper.sin(f * ((float)Math.PI / 180F));
                  float f3 = MathHelper.cos(f7 * ((float)Math.PI / 180F)) * MathHelper.cos(f * ((float)Math.PI / 180F));
                  float f4 = MathHelper.sqrt(f1 * f1 + f2 * f2 + f3 * f3);
                  float f5 = 3.0F * ((1.0F + (float)j) / 4.0F);
                  f1 = f1 * (f5 / f4);
                  f2 = f2 * (f5 / f4);
                  f3 = f3 * (f5 / f4);
                  playerentity.push((double)f1, (double)f2, (double)f3);
                  playerentity.startAutoSpinAttack(20);
                  if (playerentity.isOnGround()) {
                     float f6 = 1.1999999F;
                     playerentity.move(MoverType.SELF, new Vector3d(0.0D, (double)1.1999999F, 0.0D));
                  }

                  SoundEvent soundevent;
                  if (j >= 3) {
                     soundevent = SoundEvents.TRIDENT_RIPTIDE_3;
                  } else if (j == 2) {
                     soundevent = SoundEvents.TRIDENT_RIPTIDE_2;
                  } else {
                     soundevent = SoundEvents.TRIDENT_RIPTIDE_1;
                  }

                  pLevel.playSound((PlayerEntity)null, playerentity, soundevent, SoundCategory.PLAYERS, 1.0F, 1.0F);
               }

            }
         }
      }
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (itemstack.getDamageValue() >= itemstack.getMaxDamage() - 1) {
         return ActionResult.fail(itemstack);
      } else if (EnchantmentHelper.getRiptide(itemstack) > 0 && !pPlayer.isInWaterOrRain()) {
         return ActionResult.fail(itemstack);
      } else {
         pPlayer.startUsingItem(pHand);
         return ActionResult.consume(itemstack);
      }
   }

   /**
    * Current implementations of this method in child classes do not use the entry argument beside ev. They just raise
    * the damage on the stack.
    */
   public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
      pStack.hurtAndBreak(1, pAttacker, (p_220048_0_) -> {
         p_220048_0_.broadcastBreakEvent(EquipmentSlotType.MAINHAND);
      });
      return true;
   }

   /**
    * Called when a Block is destroyed using this Item. Return true to trigger the "Use Item" statistic.
    */
   public boolean mineBlock(ItemStack pStack, World pLevel, BlockState pState, BlockPos pPos, LivingEntity pEntityLiving) {
      if ((double)pState.getDestroySpeed(pLevel, pPos) != 0.0D) {
         pStack.hurtAndBreak(2, pEntityLiving, (p_220046_0_) -> {
            p_220046_0_.broadcastBreakEvent(EquipmentSlotType.MAINHAND);
         });
      }

      return true;
   }

   /**
    * Gets a map of item attribute modifiers, used by ItemSword to increase hit damage.
    */
   public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlotType pEquipmentSlot) {
      return pEquipmentSlot == EquipmentSlotType.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(pEquipmentSlot);
   }

   /**
    * Return the enchantability factor of the item, most of the time is based on material.
    */
   public int getEnchantmentValue() {
      return 1;
   }
}