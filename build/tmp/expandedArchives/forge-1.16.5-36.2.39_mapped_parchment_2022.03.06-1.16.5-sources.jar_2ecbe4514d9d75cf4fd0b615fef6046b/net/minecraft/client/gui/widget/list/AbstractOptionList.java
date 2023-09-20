package net.minecraft.client.gui.widget.list;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractOptionList<E extends AbstractOptionList.Entry<E>> extends AbstractList<E> {
   public AbstractOptionList(Minecraft p_i51139_1_, int p_i51139_2_, int p_i51139_3_, int p_i51139_4_, int p_i51139_5_, int p_i51139_6_) {
      super(p_i51139_1_, p_i51139_2_, p_i51139_3_, p_i51139_4_, p_i51139_5_, p_i51139_6_);
   }

   public boolean changeFocus(boolean pFocus) {
      boolean flag = super.changeFocus(pFocus);
      if (flag) {
         this.ensureVisible(this.getFocused());
      }

      return flag;
   }

   protected boolean isSelectedItem(int pIndex) {
      return false;
   }

   @OnlyIn(Dist.CLIENT)
   public abstract static class Entry<E extends AbstractOptionList.Entry<E>> extends AbstractList.AbstractListEntry<E> implements INestedGuiEventHandler {
      @Nullable
      private IGuiEventListener focused;
      private boolean dragging;

      public boolean isDragging() {
         return this.dragging;
      }

      public void setDragging(boolean pDragging) {
         this.dragging = pDragging;
      }

      public void setFocused(@Nullable IGuiEventListener pListener) {
         this.focused = pListener;
      }

      @Nullable
      public IGuiEventListener getFocused() {
         return this.focused;
      }
   }
}