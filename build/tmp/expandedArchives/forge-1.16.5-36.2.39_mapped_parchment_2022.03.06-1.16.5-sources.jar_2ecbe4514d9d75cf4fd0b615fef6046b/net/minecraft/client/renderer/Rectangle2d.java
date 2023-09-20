package net.minecraft.client.renderer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Rectangle2d {
   private int xPos;
   private int yPos;
   private int width;
   private int height;

   public Rectangle2d(int pXPos, int pYPos, int pWidth, int pHeight) {
      this.xPos = pXPos;
      this.yPos = pYPos;
      this.width = pWidth;
      this.height = pHeight;
   }

   public int getX() {
      return this.xPos;
   }

   public int getY() {
      return this.yPos;
   }

   public int getWidth() {
      return this.width;
   }

   public int getHeight() {
      return this.height;
   }

   public boolean contains(int pX, int pY) {
      return pX >= this.xPos && pX <= this.xPos + this.width && pY >= this.yPos && pY <= this.yPos + this.height;
   }
}