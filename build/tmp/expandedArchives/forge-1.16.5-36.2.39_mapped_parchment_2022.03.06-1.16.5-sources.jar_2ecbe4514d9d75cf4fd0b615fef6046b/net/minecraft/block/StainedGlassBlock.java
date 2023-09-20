package net.minecraft.block;

import net.minecraft.item.DyeColor;

public class StainedGlassBlock extends AbstractGlassBlock implements IBeaconBeamColorProvider {
   private final DyeColor color;

   public StainedGlassBlock(DyeColor pDyeColor, AbstractBlock.Properties pProperties) {
      super(pProperties);
      this.color = pDyeColor;
   }

   public DyeColor getColor() {
      return this.color;
   }
}