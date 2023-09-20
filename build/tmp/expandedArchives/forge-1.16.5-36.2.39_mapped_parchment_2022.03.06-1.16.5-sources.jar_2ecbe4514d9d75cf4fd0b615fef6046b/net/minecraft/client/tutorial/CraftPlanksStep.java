package net.minecraft.client.tutorial;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.toasts.TutorialToast;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CraftPlanksStep implements ITutorialStep {
   private static final ITextComponent CRAFT_TITLE = new TranslationTextComponent("tutorial.craft_planks.title");
   private static final ITextComponent CRAFT_DESCRIPTION = new TranslationTextComponent("tutorial.craft_planks.description");
   private final Tutorial tutorial;
   private TutorialToast toast;
   private int timeWaiting;

   public CraftPlanksStep(Tutorial pTutorial) {
      this.tutorial = pTutorial;
   }

   public void tick() {
      ++this.timeWaiting;
      if (this.tutorial.getGameMode() != GameType.SURVIVAL) {
         this.tutorial.setStep(TutorialSteps.NONE);
      } else {
         if (this.timeWaiting == 1) {
            ClientPlayerEntity clientplayerentity = this.tutorial.getMinecraft().player;
            if (clientplayerentity != null) {
               if (clientplayerentity.inventory.contains(ItemTags.PLANKS)) {
                  this.tutorial.setStep(TutorialSteps.NONE);
                  return;
               }

               if (hasCraftedPlanksPreviously(clientplayerentity, ItemTags.PLANKS)) {
                  this.tutorial.setStep(TutorialSteps.NONE);
                  return;
               }
            }
         }

         if (this.timeWaiting >= 1200 && this.toast == null) {
            this.toast = new TutorialToast(TutorialToast.Icons.WOODEN_PLANKS, CRAFT_TITLE, CRAFT_DESCRIPTION, false);
            this.tutorial.getMinecraft().getToasts().addToast(this.toast);
         }

      }
   }

   public void clear() {
      if (this.toast != null) {
         this.toast.hide();
         this.toast = null;
      }

   }

   /**
    * Called when the player pick up an ItemStack
    */
   public void onGetItem(ItemStack pStack) {
      Item item = pStack.getItem();
      if (ItemTags.PLANKS.contains(item)) {
         this.tutorial.setStep(TutorialSteps.NONE);
      }

   }

   public static boolean hasCraftedPlanksPreviously(ClientPlayerEntity pPlayer, ITag<Item> pItems) {
      for(Item item : pItems.getValues()) {
         if (pPlayer.getStats().getValue(Stats.ITEM_CRAFTED.get(item)) > 0) {
            return true;
         }
      }

      return false;
   }
}