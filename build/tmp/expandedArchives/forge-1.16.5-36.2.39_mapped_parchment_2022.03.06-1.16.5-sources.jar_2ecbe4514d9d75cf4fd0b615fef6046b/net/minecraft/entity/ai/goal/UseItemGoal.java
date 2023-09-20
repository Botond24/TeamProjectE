package net.minecraft.entity.ai.goal;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.entity.MobEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;

public class UseItemGoal<T extends MobEntity> extends Goal {
   private final T mob;
   private final ItemStack item;
   private final Predicate<? super T> canUseSelector;
   private final SoundEvent finishUsingSound;

   public UseItemGoal(T p_i50319_1_, ItemStack p_i50319_2_, @Nullable SoundEvent p_i50319_3_, Predicate<? super T> p_i50319_4_) {
      this.mob = p_i50319_1_;
      this.item = p_i50319_2_;
      this.finishUsingSound = p_i50319_3_;
      this.canUseSelector = p_i50319_4_;
   }

   /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
   public boolean canUse() {
      return this.canUseSelector.test(this.mob);
   }

   /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
      return this.mob.isUsingItem();
   }

   /**
    * Execute a one shot task or start executing a continuous task
    */
   public void start() {
      this.mob.setItemSlot(EquipmentSlotType.MAINHAND, this.item.copy());
      this.mob.startUsingItem(Hand.MAIN_HAND);
   }

   /**
    * Reset the task's internal state. Called when this task is interrupted by another one
    */
   public void stop() {
      this.mob.setItemSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
      if (this.finishUsingSound != null) {
         this.mob.playSound(this.finishUsingSound, 1.0F, this.mob.getRandom().nextFloat() * 0.2F + 0.9F);
      }

   }
}