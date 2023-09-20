package net.minecraft.client.gui.screen;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.GameSettings;
import net.minecraft.client.gui.IBidiTooltip;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.list.OptionsRowList;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SettingsScreen extends Screen {
   protected final Screen lastScreen;
   protected final GameSettings options;

   public SettingsScreen(Screen pLastScreen, GameSettings pOptions, ITextComponent pTitle) {
      super(pTitle);
      this.lastScreen = pLastScreen;
      this.options = pOptions;
   }

   public void removed() {
      this.minecraft.options.save();
   }

   public void onClose() {
      this.minecraft.setScreen(this.lastScreen);
   }

   @Nullable
   public static List<IReorderingProcessor> tooltipAt(OptionsRowList pOptions, int pX, int pY) {
      Optional<Widget> optional = pOptions.getMouseOver((double)pX, (double)pY);
      if (optional.isPresent() && optional.get() instanceof IBidiTooltip) {
         Optional<List<IReorderingProcessor>> optional1 = ((IBidiTooltip)optional.get()).getTooltip();
         return optional1.orElse((List<IReorderingProcessor>)null);
      } else {
         return null;
      }
   }
}