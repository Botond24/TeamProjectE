package net.minecraft.client.resources.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Set;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AnimationMetadataSection {
   public static final AnimationMetadataSectionSerializer SERIALIZER = new AnimationMetadataSectionSerializer();
   public static final AnimationMetadataSection EMPTY = new AnimationMetadataSection(Lists.newArrayList(), -1, -1, 1, false) {
      public Pair<Integer, Integer> getFrameSize(int pWidth, int pHeight) {
         return Pair.of(pWidth, pHeight);
      }
   };
   private final List<AnimationFrame> frames;
   private final int frameWidth;
   private final int frameHeight;
   private final int defaultFrameTime;
   private final boolean interpolatedFrames;

   public AnimationMetadataSection(List<AnimationFrame> pFrames, int pFrameWidth, int pFrameHeight, int pDefaultFrameTime, boolean pInterpolatedFrames) {
      this.frames = pFrames;
      this.frameWidth = pFrameWidth;
      this.frameHeight = pFrameHeight;
      this.defaultFrameTime = pDefaultFrameTime;
      this.interpolatedFrames = pInterpolatedFrames;
   }

   private static boolean isDivisionInteger(int pValMul, int pVal) {
      return pValMul / pVal * pVal == pValMul;
   }

   public Pair<Integer, Integer> getFrameSize(int pWidth, int pHeight) {
      Pair<Integer, Integer> pair = this.calculateFrameSize(pWidth, pHeight);
      int i = pair.getFirst();
      int j = pair.getSecond();
      if (isDivisionInteger(pWidth, i) && isDivisionInteger(pHeight, j)) {
         return pair;
      } else {
         throw new IllegalArgumentException(String.format("Image size %s,%s is not multiply of frame size %s,%s", pWidth, pHeight, i, j));
      }
   }

   private Pair<Integer, Integer> calculateFrameSize(int pDefWidth, int pDefHeight) {
      if (this.frameWidth != -1) {
         return this.frameHeight != -1 ? Pair.of(this.frameWidth, this.frameHeight) : Pair.of(this.frameWidth, pDefHeight);
      } else if (this.frameHeight != -1) {
         return Pair.of(pDefWidth, this.frameHeight);
      } else {
         int i = Math.min(pDefWidth, pDefHeight);
         return Pair.of(i, i);
      }
   }

   public int getFrameHeight(int pDefHeight) {
      return this.frameHeight == -1 ? pDefHeight : this.frameHeight;
   }

   public int getFrameWidth(int pDefWidth) {
      return this.frameWidth == -1 ? pDefWidth : this.frameWidth;
   }

   public int getFrameCount() {
      return this.frames.size();
   }

   public int getDefaultFrameTime() {
      return this.defaultFrameTime;
   }

   public boolean isInterpolatedFrames() {
      return this.interpolatedFrames;
   }

   private AnimationFrame getFrame(int pAnimationIndex) {
      return this.frames.get(pAnimationIndex);
   }

   public int getFrameTime(int pAnimationIndex) {
      AnimationFrame animationframe = this.getFrame(pAnimationIndex);
      return animationframe.isTimeUnknown() ? this.defaultFrameTime : animationframe.getTime();
   }

   public int getFrameIndex(int pAnimationIndex) {
      return this.frames.get(pAnimationIndex).getIndex();
   }

   public Set<Integer> getUniqueFrameIndices() {
      Set<Integer> set = Sets.newHashSet();

      for(AnimationFrame animationframe : this.frames) {
         set.add(animationframe.getIndex());
      }

      return set;
   }
}