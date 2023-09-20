package net.minecraft.item;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;

public class DyeItem extends Item {
   private static final Map<DyeColor, DyeItem> ITEM_BY_COLOR = Maps.newEnumMap(DyeColor.class);
   private final DyeColor dyeColor;

   public DyeItem(DyeColor pDyeColor, Item.Properties pProperties) {
      super(pProperties);
      this.dyeColor = pDyeColor;
      ITEM_BY_COLOR.put(pDyeColor, this);
   }

   /**
    * Returns true if the item can be used on the given entity, e.g. shears on sheep.
    */
   public ActionResultType interactLivingEntity(ItemStack pStack, PlayerEntity pPlayer, LivingEntity pTarget, Hand pHand) {
      if (pTarget instanceof SheepEntity) {
         SheepEntity sheepentity = (SheepEntity)pTarget;
         if (sheepentity.isAlive() && !sheepentity.isSheared() && sheepentity.getColor() != this.dyeColor) {
            if (!pPlayer.level.isClientSide) {
               sheepentity.setColor(this.dyeColor);
               pStack.shrink(1);
            }

            return ActionResultType.sidedSuccess(pPlayer.level.isClientSide);
         }
      }

      return ActionResultType.PASS;
   }

   public DyeColor getDyeColor() {
      return this.dyeColor;
   }

   public static DyeItem byColor(DyeColor pColor) {
      return ITEM_BY_COLOR.get(pColor);
   }
}