package net.minecraft.client.gui.toasts;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TutorialToast implements IToast {
   private final TutorialToast.Icons icon;
   private final ITextComponent title;
   private final ITextComponent message;
   private IToast.Visibility visibility = IToast.Visibility.SHOW;
   private long lastProgressTime;
   private float lastProgress;
   private float progress;
   private final boolean progressable;

   public TutorialToast(TutorialToast.Icons pIcon, ITextComponent pTitle, @Nullable ITextComponent pMessage, boolean pProgressable) {
      this.icon = pIcon;
      this.title = pTitle;
      this.message = pMessage;
      this.progressable = pProgressable;
   }

   public IToast.Visibility render(MatrixStack pPoseStack, ToastGui pToastComponent, long p_230444_3_) {
      pToastComponent.getMinecraft().getTextureManager().bind(TEXTURE);
      RenderSystem.color3f(1.0F, 1.0F, 1.0F);
      pToastComponent.blit(pPoseStack, 0, 0, 0, 96, this.width(), this.height());
      this.icon.render(pPoseStack, pToastComponent, 6, 6);
      if (this.message == null) {
         pToastComponent.getMinecraft().font.draw(pPoseStack, this.title, 30.0F, 12.0F, -11534256);
      } else {
         pToastComponent.getMinecraft().font.draw(pPoseStack, this.title, 30.0F, 7.0F, -11534256);
         pToastComponent.getMinecraft().font.draw(pPoseStack, this.message, 30.0F, 18.0F, -16777216);
      }

      if (this.progressable) {
         AbstractGui.fill(pPoseStack, 3, 28, 157, 29, -1);
         float f = (float)MathHelper.clampedLerp((double)this.lastProgress, (double)this.progress, (double)((float)(p_230444_3_ - this.lastProgressTime) / 100.0F));
         int i;
         if (this.progress >= this.lastProgress) {
            i = -16755456;
         } else {
            i = -11206656;
         }

         AbstractGui.fill(pPoseStack, 3, 28, (int)(3.0F + 154.0F * f), 29, i);
         this.lastProgress = f;
         this.lastProgressTime = p_230444_3_;
      }

      return this.visibility;
   }

   public void hide() {
      this.visibility = IToast.Visibility.HIDE;
   }

   public void updateProgress(float pProgress) {
      this.progress = pProgress;
   }

   @OnlyIn(Dist.CLIENT)
   public static enum Icons {
      MOVEMENT_KEYS(0, 0),
      MOUSE(1, 0),
      TREE(2, 0),
      RECIPE_BOOK(0, 1),
      WOODEN_PLANKS(1, 1),
      SOCIAL_INTERACTIONS(2, 1);

      private final int x;
      private final int y;

      private Icons(int pX, int pY) {
         this.x = pX;
         this.y = pY;
      }

      public void render(MatrixStack pPoseStack, AbstractGui pGuiComponent, int pX, int pY) {
         RenderSystem.enableBlend();
         pGuiComponent.blit(pPoseStack, pX, pY, 176 + this.x * 20, this.y * 20, 20, 20);
         RenderSystem.enableBlend();
      }
   }
}