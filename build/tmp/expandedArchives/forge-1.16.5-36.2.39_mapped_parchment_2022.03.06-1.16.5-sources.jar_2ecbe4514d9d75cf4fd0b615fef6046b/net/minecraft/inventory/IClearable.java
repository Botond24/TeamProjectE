package net.minecraft.inventory;

import javax.annotation.Nullable;

public interface IClearable {
   void clearContent();

   static void tryClear(@Nullable Object pObject) {
      if (pObject instanceof IClearable) {
         ((IClearable)pObject).clearContent();
      }

   }
}