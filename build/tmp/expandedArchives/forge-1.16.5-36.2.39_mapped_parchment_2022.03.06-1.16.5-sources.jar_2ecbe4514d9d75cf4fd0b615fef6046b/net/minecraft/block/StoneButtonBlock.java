package net.minecraft.block;

import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;

public class StoneButtonBlock extends AbstractButtonBlock {
   public StoneButtonBlock(AbstractBlock.Properties p_i48315_1_) {
      super(false, p_i48315_1_);
   }

   protected SoundEvent getSound(boolean pIsOn) {
      return pIsOn ? SoundEvents.STONE_BUTTON_CLICK_ON : SoundEvents.STONE_BUTTON_CLICK_OFF;
   }
}