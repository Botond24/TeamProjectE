package net.minecraft.client.gui;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChatLine<T> {
   private final int addedTime;
   private final T message;
   private final int id;

   public ChatLine(int pAddedTime, T pMessage, int pId) {
      this.message = pMessage;
      this.addedTime = pAddedTime;
      this.id = pId;
   }

   public T getMessage() {
      return this.message;
   }

   public int getAddedTime() {
      return this.addedTime;
   }

   public int getId() {
      return this.id;
   }
}