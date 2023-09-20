package net.minecraft.world.gen.layer.traits;

import net.minecraft.world.gen.IExtendedNoiseRandom;
import net.minecraft.world.gen.INoiseRandom;
import net.minecraft.world.gen.area.IArea;

public interface IC0Transformer extends IAreaTransformer1, IDimOffset0Transformer {
   int apply(INoiseRandom pContext, int pValue);

   default int applyPixel(IExtendedNoiseRandom<?> pContext, IArea pArea, int pX, int pZ) {
      return this.apply(pContext, pArea.get(this.getParentX(pX), this.getParentY(pZ)));
   }
}