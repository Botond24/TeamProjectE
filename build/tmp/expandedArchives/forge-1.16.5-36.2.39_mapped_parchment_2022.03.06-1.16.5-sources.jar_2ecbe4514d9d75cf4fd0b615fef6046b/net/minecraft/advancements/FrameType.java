package net.minecraft.advancements;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum FrameType {
   TASK("task", 0, TextFormatting.GREEN),
   CHALLENGE("challenge", 26, TextFormatting.DARK_PURPLE),
   GOAL("goal", 52, TextFormatting.GREEN);

   private final String name;
   private final int texture;
   private final TextFormatting chatColor;
   private final ITextComponent displayName;

   private FrameType(String pName, int pTexture, TextFormatting pChatColor) {
      this.name = pName;
      this.texture = pTexture;
      this.chatColor = pChatColor;
      this.displayName = new TranslationTextComponent("advancements.toast." + pName);
   }

   public String getName() {
      return this.name;
   }

   @OnlyIn(Dist.CLIENT)
   public int getTexture() {
      return this.texture;
   }

   public static FrameType byName(String pName) {
      for(FrameType frametype : values()) {
         if (frametype.name.equals(pName)) {
            return frametype;
         }
      }

      throw new IllegalArgumentException("Unknown frame type '" + pName + "'");
   }

   public TextFormatting getChatColor() {
      return this.chatColor;
   }

   @OnlyIn(Dist.CLIENT)
   public ITextComponent getDisplayName() {
      return this.displayName;
   }
}