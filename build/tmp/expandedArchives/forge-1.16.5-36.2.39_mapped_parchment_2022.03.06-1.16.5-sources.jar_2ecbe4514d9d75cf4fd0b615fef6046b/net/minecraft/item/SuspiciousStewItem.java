package net.minecraft.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.world.World;

public class SuspiciousStewItem extends Item {
   public SuspiciousStewItem(Item.Properties p_i50035_1_) {
      super(p_i50035_1_);
   }

   public static void saveMobEffect(ItemStack pBowlStack, Effect pEffect, int pEffectDuration) {
      CompoundNBT compoundnbt = pBowlStack.getOrCreateTag();
      ListNBT listnbt = compoundnbt.getList("Effects", 9);
      CompoundNBT compoundnbt1 = new CompoundNBT();
      compoundnbt1.putByte("EffectId", (byte)Effect.getId(pEffect));
      compoundnbt1.putInt("EffectDuration", pEffectDuration);
      listnbt.add(compoundnbt1);
      compoundnbt.put("Effects", listnbt);
   }

   /**
    * Called when the player finishes using this Item (E.g. finishes eating.). Not called when the player stops using
    * the Item before the action is complete.
    */
   public ItemStack finishUsingItem(ItemStack pStack, World pLevel, LivingEntity pEntityLiving) {
      ItemStack itemstack = super.finishUsingItem(pStack, pLevel, pEntityLiving);
      CompoundNBT compoundnbt = pStack.getTag();
      if (compoundnbt != null && compoundnbt.contains("Effects", 9)) {
         ListNBT listnbt = compoundnbt.getList("Effects", 10);

         for(int i = 0; i < listnbt.size(); ++i) {
            int j = 160;
            CompoundNBT compoundnbt1 = listnbt.getCompound(i);
            if (compoundnbt1.contains("EffectDuration", 3)) {
               j = compoundnbt1.getInt("EffectDuration");
            }

            Effect effect = Effect.byId(compoundnbt1.getByte("EffectId"));
            if (effect != null) {
               pEntityLiving.addEffect(new EffectInstance(effect, j));
            }
         }
      }

      return pEntityLiving instanceof PlayerEntity && ((PlayerEntity)pEntityLiving).abilities.instabuild ? itemstack : new ItemStack(Items.BOWL);
   }
}