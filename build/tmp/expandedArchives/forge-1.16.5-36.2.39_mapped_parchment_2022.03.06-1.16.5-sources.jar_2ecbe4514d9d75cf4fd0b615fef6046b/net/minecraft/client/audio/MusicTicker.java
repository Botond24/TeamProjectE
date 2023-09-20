package net.minecraft.client.audio;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MusicTicker {
   private final Random random = new Random();
   private final Minecraft minecraft;
   @Nullable
   private ISound currentMusic;
   private int nextSongDelay = 100;

   public MusicTicker(Minecraft pMinecraft) {
      this.minecraft = pMinecraft;
   }

   public void tick() {
      BackgroundMusicSelector backgroundmusicselector = this.minecraft.getSituationalMusic();
      if (this.currentMusic != null) {
         if (!backgroundmusicselector.getEvent().getLocation().equals(this.currentMusic.getLocation()) && backgroundmusicselector.replaceCurrentMusic()) {
            this.minecraft.getSoundManager().stop(this.currentMusic);
            this.nextSongDelay = MathHelper.nextInt(this.random, 0, backgroundmusicselector.getMinDelay() / 2);
         }

         if (!this.minecraft.getSoundManager().isActive(this.currentMusic)) {
            this.currentMusic = null;
            this.nextSongDelay = Math.min(this.nextSongDelay, MathHelper.nextInt(this.random, backgroundmusicselector.getMinDelay(), backgroundmusicselector.getMaxDelay()));
         }
      }

      this.nextSongDelay = Math.min(this.nextSongDelay, backgroundmusicselector.getMaxDelay());
      if (this.currentMusic == null && this.nextSongDelay-- <= 0) {
         this.startPlaying(backgroundmusicselector);
      }

   }

   public void startPlaying(BackgroundMusicSelector pSelector) {
      this.currentMusic = SimpleSound.forMusic(pSelector.getEvent());
      if (this.currentMusic.getSound() != SoundHandler.EMPTY_SOUND) {
         this.minecraft.getSoundManager().play(this.currentMusic);
      }

      this.nextSongDelay = Integer.MAX_VALUE;
   }

   public void stopPlaying() {
      if (this.currentMusic != null) {
         this.minecraft.getSoundManager().stop(this.currentMusic);
         this.currentMusic = null;
      }

      this.nextSongDelay += 100;
   }

   public boolean isPlayingMusic(BackgroundMusicSelector pSelector) {
      return this.currentMusic == null ? false : pSelector.getEvent().getLocation().equals(this.currentMusic.getLocation());
   }
}