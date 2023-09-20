package net.minecraft.world.gen.layer.traits;

import net.minecraft.world.gen.IExtendedNoiseRandom;
import net.minecraft.world.gen.INoiseRandom;
import net.minecraft.world.gen.area.IArea;

public interface IBishopTransformer extends IAreaTransformer1, IDimOffset1Transformer {
   int apply(INoiseRandom pContext, int pX, int pSouthEast, int p_202792_4_, int p_202792_5_, int p_202792_6_);

   default int applyPixel(IExtendedNoiseRandom<?> pContext, IArea pArea, int pX, int pZ) {
      return this.apply(pContext, pArea.get(this.getParentX(pX + 0), this.getParentY(pZ + 2)), pArea.get(this.getParentX(pX + 2), this.getParentY(pZ + 2)), pArea.get(this.getParentX(pX + 2), this.getParentY(pZ + 0)), pArea.get(this.getParentX(pX + 0), this.getParentY(pZ + 0)), pArea.get(this.getParentX(pX + 1), this.getParentY(pZ + 1)));
   }
}