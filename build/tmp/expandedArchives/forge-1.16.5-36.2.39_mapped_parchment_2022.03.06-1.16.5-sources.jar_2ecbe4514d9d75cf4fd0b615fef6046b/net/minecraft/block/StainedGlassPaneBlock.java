package net.minecraft.block;

import net.minecraft.item.DyeColor;

public class StainedGlassPaneBlock extends PaneBlock implements IBeaconBeamColorProvider {
   private final DyeColor color;

   public StainedGlassPaneBlock(DyeColor pColor, AbstractBlock.Properties pProperties) {
      super(pProperties);
      this.color = pColor;
      this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, Boolean.valueOf(false)).setValue(EAST, Boolean.valueOf(false)).setValue(SOUTH, Boolean.valueOf(false)).setValue(WEST, Boolean.valueOf(false)).setValue(WATERLOGGED, Boolean.valueOf(false)));
   }

   public DyeColor getColor() {
      return this.color;
   }
}