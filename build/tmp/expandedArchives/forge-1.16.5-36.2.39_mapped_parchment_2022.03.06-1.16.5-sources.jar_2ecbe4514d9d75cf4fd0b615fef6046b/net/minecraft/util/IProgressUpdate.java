package net.minecraft.util;

import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IProgressUpdate {
   void progressStartNoAbort(ITextComponent pComponent);

   @OnlyIn(Dist.CLIENT)
   void progressStart(ITextComponent pComponent);

   void progressStage(ITextComponent pComponent);

   /**
    * Updates the progress bar on the loading screen to the specified amount.
    */
   void progressStagePercentage(int pProgress);

   @OnlyIn(Dist.CLIENT)
   void stop();
}