package net.minecraft.world.gen.layer.traits;

import net.minecraft.world.gen.IExtendedNoiseRandom;
import net.minecraft.world.gen.INoiseRandom;
import net.minecraft.world.gen.area.IArea;

public interface IC1Transformer extends IAreaTransformer1, IDimOffset1Transformer {
   int apply(INoiseRandom pContext, int pValue);

   default int applyPixel(IExtendedNoiseRandom<?> pContext, IArea pArea, int pX, int pZ) {
      int i = pArea.get(this.getParentX(pX + 1), this.getParentY(pZ + 1));
      return this.apply(pContext, i);
   }
}