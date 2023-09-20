package net.minecraft.client.tutorial;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.TutorialToast;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovementInput;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.KeybindTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Tutorial {
   private final Minecraft minecraft;
   @Nullable
   private ITutorialStep instance;
   private List<Tutorial.ToastTimeInfo> timedToasts = Lists.newArrayList();

   public Tutorial(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
   }

   public void onInput(MovementInput pInput) {
      if (this.instance != null) {
         this.instance.onInput(pInput);
      }

   }

   public void onMouse(double pVelocityX, double pVelocityY) {
      if (this.instance != null) {
         this.instance.onMouse(pVelocityX, pVelocityY);
      }

   }

   public void onLookAt(@Nullable ClientWorld pLevel, @Nullable RayTraceResult pResult) {
      if (this.instance != null && pResult != null && pLevel != null) {
         this.instance.onLookAt(pLevel, pResult);
      }

   }

   public void onDestroyBlock(ClientWorld pLevel, BlockPos pPos, BlockState pState, float pDiggingStage) {
      if (this.instance != null) {
         this.instance.onDestroyBlock(pLevel, pPos, pState, pDiggingStage);
      }

   }

   /**
    * Called when the player opens his inventory
    */
   public void onOpenInventory() {
      if (this.instance != null) {
         this.instance.onOpenInventory();
      }

   }

   /**
    * Called when the player pick up an ItemStack
    */
   public void onGetItem(ItemStack pStack) {
      if (this.instance != null) {
         this.instance.onGetItem(pStack);
      }

   }

   public void stop() {
      if (this.instance != null) {
         this.instance.clear();
         this.instance = null;
      }
   }

   /**
    * Reloads the tutorial step from the game settings
    */
   public void start() {
      if (this.instance != null) {
         this.stop();
      }

      this.instance = this.minecraft.options.tutorialStep.create(this);
   }

   public void addTimedToast(TutorialToast pToast, int pDurationTicks) {
      this.timedToasts.add(new Tutorial.ToastTimeInfo(pToast, pDurationTicks));
      this.minecraft.getToasts().addToast(pToast);
   }

   public void removeTimedToast(TutorialToast pToast) {
      this.timedToasts.removeIf((p_244699_1_) -> {
         return p_244699_1_.toast == pToast;
      });
      pToast.hide();
   }

   public void tick() {
      this.timedToasts.removeIf((p_244700_0_) -> {
         return p_244700_0_.updateProgress();
      });
      if (this.instance != null) {
         if (this.minecraft.level != null) {
            this.instance.tick();
         } else {
            this.stop();
         }
      } else if (this.minecraft.level != null) {
         this.start();
      }

   }

   /**
    * Sets a new step to the tutorial
    */
   public void setStep(TutorialSteps pStep) {
      this.minecraft.options.tutorialStep = pStep;
      this.minecraft.options.save();
      if (this.instance != null) {
         this.instance.clear();
         this.instance = pStep.create(this);
      }

   }

   public Minecraft getMinecraft() {
      return this.minecraft;
   }

   public GameType getGameMode() {
      return this.minecraft.gameMode == null ? GameType.NOT_SET : this.minecraft.gameMode.getPlayerMode();
   }

   public static ITextComponent key(String pKeybind) {
      return (new KeybindTextComponent("key." + pKeybind)).withStyle(TextFormatting.BOLD);
   }

   @OnlyIn(Dist.CLIENT)
   static final class ToastTimeInfo {
      private final TutorialToast toast;
      private final int durationTicks;
      private int progress;

      private ToastTimeInfo(TutorialToast pToast, int pDurationTicks) {
         this.toast = pToast;
         this.durationTicks = pDurationTicks;
      }

      private boolean updateProgress() {
         this.toast.updateProgress(Math.min((float)(++this.progress) / (float)this.durationTicks, 1.0F));
         if (this.progress > this.durationTicks) {
            this.toast.hide();
            return true;
         } else {
            return false;
         }
      }
   }
}