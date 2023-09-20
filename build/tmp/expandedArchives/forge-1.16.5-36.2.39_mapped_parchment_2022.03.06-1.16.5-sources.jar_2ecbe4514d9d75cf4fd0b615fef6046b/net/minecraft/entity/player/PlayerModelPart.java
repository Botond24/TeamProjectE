package net.minecraft.entity.player;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum PlayerModelPart {
   CAPE(0, "cape"),
   JACKET(1, "jacket"),
   LEFT_SLEEVE(2, "left_sleeve"),
   RIGHT_SLEEVE(3, "right_sleeve"),
   LEFT_PANTS_LEG(4, "left_pants_leg"),
   RIGHT_PANTS_LEG(5, "right_pants_leg"),
   HAT(6, "hat");

   private final int bit;
   private final int mask;
   private final String id;
   private final ITextComponent name;

   private PlayerModelPart(int pBit, String pId) {
      this.bit = pBit;
      this.mask = 1 << pBit;
      this.id = pId;
      this.name = new TranslationTextComponent("options.modelPart." + pId);
   }

   public int getMask() {
      return this.mask;
   }

   public String getId() {
      return this.id;
   }

   public ITextComponent getName() {
      return this.name;
   }
}