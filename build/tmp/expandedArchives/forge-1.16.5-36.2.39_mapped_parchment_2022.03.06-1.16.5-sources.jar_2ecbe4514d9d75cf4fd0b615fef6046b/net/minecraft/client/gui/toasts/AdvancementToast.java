package net.minecraft.client.gui.toasts;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AdvancementToast implements IToast {
   private final Advancement advancement;
   private boolean playedSound;

   public AdvancementToast(Advancement pAdvancement) {
      this.advancement = pAdvancement;
   }

   public IToast.Visibility render(MatrixStack pPoseStack, ToastGui pToastComponent, long p_230444_3_) {
      pToastComponent.getMinecraft().getTextureManager().bind(TEXTURE);
      RenderSystem.color3f(1.0F, 1.0F, 1.0F);
      DisplayInfo displayinfo = this.advancement.getDisplay();
      pToastComponent.blit(pPoseStack, 0, 0, 0, 0, this.width(), this.height());
      if (displayinfo != null) {
         List<IReorderingProcessor> list = pToastComponent.getMinecraft().font.split(displayinfo.getTitle(), 125);
         int i = displayinfo.getFrame() == FrameType.CHALLENGE ? 16746751 : 16776960;
         if (list.size() == 1) {
            pToastComponent.getMinecraft().font.draw(pPoseStack, displayinfo.getFrame().getDisplayName(), 30.0F, 7.0F, i | -16777216);
            pToastComponent.getMinecraft().font.draw(pPoseStack, list.get(0), 30.0F, 18.0F, -1);
         } else {
            int j = 1500;
            float f = 300.0F;
            if (p_230444_3_ < 1500L) {
               int k = MathHelper.floor(MathHelper.clamp((float)(1500L - p_230444_3_) / 300.0F, 0.0F, 1.0F) * 255.0F) << 24 | 67108864;
               pToastComponent.getMinecraft().font.draw(pPoseStack, displayinfo.getFrame().getDisplayName(), 30.0F, 11.0F, i | k);
            } else {
               int i1 = MathHelper.floor(MathHelper.clamp((float)(p_230444_3_ - 1500L) / 300.0F, 0.0F, 1.0F) * 252.0F) << 24 | 67108864;
               int l = this.height() / 2 - list.size() * 9 / 2;

               for(IReorderingProcessor ireorderingprocessor : list) {
                  pToastComponent.getMinecraft().font.draw(pPoseStack, ireorderingprocessor, 30.0F, (float)l, 16777215 | i1);
                  l += 9;
               }
            }
         }

         if (!this.playedSound && p_230444_3_ > 0L) {
            this.playedSound = true;
            if (displayinfo.getFrame() == FrameType.CHALLENGE) {
               pToastComponent.getMinecraft().getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.0F));
            }
         }

         pToastComponent.getMinecraft().getItemRenderer().renderAndDecorateFakeItem(displayinfo.getIcon(), 8, 8);
         return p_230444_3_ >= 5000L ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
      } else {
         return IToast.Visibility.HIDE;
      }
   }
}