package net.minecraft.client.audio;

import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ISound {
   ResourceLocation getLocation();

   @Nullable
   SoundEventAccessor resolve(SoundHandler pHandler);

   Sound getSound();

   SoundCategory getSource();

   boolean isLooping();

   /**
    * True if the sound is not tied to a particular position in world (e.g. BGM)
    */
   boolean isRelative();

   int getDelay();

   float getVolume();

   float getPitch();

   double getX();

   double getY();

   double getZ();

   ISound.AttenuationType getAttenuation();

   default boolean canStartSilent() {
      return false;
   }

   default boolean canPlaySound() {
      return true;
   }

   @OnlyIn(Dist.CLIENT)
   public static enum AttenuationType {
      NONE,
      LINEAR;
   }

   /*================================ FORGE START ================================================*/

   default java.util.concurrent.CompletableFuture<net.minecraft.client.audio.IAudioStream> getStream(net.minecraft.client.audio.AudioStreamManager soundBuffers, Sound sound, boolean looping) {
      return soundBuffers.getStream(sound.getPath(), looping);
   }
}
