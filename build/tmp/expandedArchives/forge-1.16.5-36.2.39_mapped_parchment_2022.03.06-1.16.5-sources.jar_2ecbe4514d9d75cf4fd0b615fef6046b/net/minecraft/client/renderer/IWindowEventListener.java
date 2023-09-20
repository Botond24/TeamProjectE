package net.minecraft.client.renderer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IWindowEventListener {
   void setWindowActive(boolean pFocused);

   void resizeDisplay();

   void cursorEntered();
}