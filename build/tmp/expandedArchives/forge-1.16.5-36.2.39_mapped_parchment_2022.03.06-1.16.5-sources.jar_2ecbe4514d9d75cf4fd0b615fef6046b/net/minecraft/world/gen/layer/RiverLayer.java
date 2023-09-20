package net.minecraft.world.gen.layer;

import net.minecraft.world.gen.INoiseRandom;
import net.minecraft.world.gen.layer.traits.ICastleTransformer;

public enum RiverLayer implements ICastleTransformer {
   INSTANCE;

   public int apply(INoiseRandom pContext, int pNorth, int pWest, int pSouth, int pEast, int pCenter) {
      int i = riverFilter(pCenter);
      return i == riverFilter(pEast) && i == riverFilter(pNorth) && i == riverFilter(pWest) && i == riverFilter(pSouth) ? -1 : 7;
   }

   private static int riverFilter(int p_151630_0_) {
      return p_151630_0_ >= 2 ? 2 + (p_151630_0_ & 1) : p_151630_0_;
   }
}