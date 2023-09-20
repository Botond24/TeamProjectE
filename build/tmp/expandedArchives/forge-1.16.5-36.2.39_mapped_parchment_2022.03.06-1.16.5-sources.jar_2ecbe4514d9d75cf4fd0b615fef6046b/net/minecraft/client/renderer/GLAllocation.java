package net.minecraft.client.renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GLAllocation {
   /**
    * Creates and returns a direct byte buffer with the specified capacity. Applies native ordering to speed up access.
    */
   public static synchronized ByteBuffer createByteBuffer(int pCapacity) {
      return ByteBuffer.allocateDirect(pCapacity).order(ByteOrder.nativeOrder());
   }

   /**
    * Creates and returns a direct float buffer with the specified capacity. Applies native ordering to speed up access.
    */
   public static FloatBuffer createFloatBuffer(int pCapacity) {
      return createByteBuffer(pCapacity << 2).asFloatBuffer();
   }
}