package net.minecraft.client.gui.spectator.categories;

import com.google.common.base.MoreObjects;
import java.util.List;
import net.minecraft.client.gui.spectator.ISpectatorMenuObject;
import net.minecraft.client.gui.spectator.ISpectatorMenuView;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpectatorDetails {
   private final ISpectatorMenuView category;
   private final List<ISpectatorMenuObject> items;
   private final int selection;

   public SpectatorDetails(ISpectatorMenuView p_i45494_1_, List<ISpectatorMenuObject> p_i45494_2_, int p_i45494_3_) {
      this.category = p_i45494_1_;
      this.items = p_i45494_2_;
      this.selection = p_i45494_3_;
   }

   public ISpectatorMenuObject getItem(int pIndex) {
      return pIndex >= 0 && pIndex < this.items.size() ? MoreObjects.firstNonNull(this.items.get(pIndex), SpectatorMenu.EMPTY_SLOT) : SpectatorMenu.EMPTY_SLOT;
   }

   public int getSelectedSlot() {
      return this.selection;
   }
}