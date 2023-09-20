package net.minecraft.client.audio;

import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.openal.AL10;

@OnlyIn(Dist.CLIENT)
public class Listener {
   private float gain = 1.0F;
   private Vector3d position = Vector3d.ZERO;

   public void setListenerPosition(Vector3d pPosition) {
      this.position = pPosition;
      AL10.alListener3f(4100, (float)pPosition.x, (float)pPosition.y, (float)pPosition.z);
   }

   public Vector3d getListenerPosition() {
      return this.position;
   }

   public void setListenerOrientation(Vector3f pClientViewVector, Vector3f pViewVectorRaised) {
      AL10.alListenerfv(4111, new float[]{pClientViewVector.x(), pClientViewVector.y(), pClientViewVector.z(), pViewVectorRaised.x(), pViewVectorRaised.y(), pViewVectorRaised.z()});
   }

   public void setGain(float pGain) {
      AL10.alListenerf(4106, pGain);
      this.gain = pGain;
   }

   public float getGain() {
      return this.gain;
   }

   public void reset() {
      this.setListenerPosition(Vector3d.ZERO);
      this.setListenerOrientation(Vector3f.ZN, Vector3f.YP);
   }
}