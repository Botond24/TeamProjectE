package net.minecraft.block.pattern;

import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorldReader;

public class BlockPattern {
   private final Predicate<CachedBlockInfo>[][][] pattern;
   private final int depth;
   private final int height;
   private final int width;

   public BlockPattern(Predicate<CachedBlockInfo>[][][] pPattern) {
      this.pattern = pPattern;
      this.depth = pPattern.length;
      if (this.depth > 0) {
         this.height = pPattern[0].length;
         if (this.height > 0) {
            this.width = pPattern[0][0].length;
         } else {
            this.width = 0;
         }
      } else {
         this.height = 0;
         this.width = 0;
      }

   }

   public int getDepth() {
      return this.depth;
   }

   public int getHeight() {
      return this.height;
   }

   public int getWidth() {
      return this.width;
   }

   /**
    * Checks that the given pattern & rotation is at the block coordinates.
    */
   @Nullable
   private BlockPattern.PatternHelper matches(BlockPos pPos, Direction pFinger, Direction pThumb, LoadingCache<BlockPos, CachedBlockInfo> pCache) {
      for(int i = 0; i < this.width; ++i) {
         for(int j = 0; j < this.height; ++j) {
            for(int k = 0; k < this.depth; ++k) {
               if (!this.pattern[k][j][i].test(pCache.getUnchecked(translateAndRotate(pPos, pFinger, pThumb, i, j, k)))) {
                  return null;
               }
            }
         }
      }

      return new BlockPattern.PatternHelper(pPos, pFinger, pThumb, pCache, this.width, this.height, this.depth);
   }

   /**
    * Calculates whether the given world position matches the pattern. Warning, fairly heavy function.
    * @return a BlockPatternMatch if found, null otherwise.
    */
   @Nullable
   public BlockPattern.PatternHelper find(IWorldReader pLevel, BlockPos pPos) {
      LoadingCache<BlockPos, CachedBlockInfo> loadingcache = createLevelCache(pLevel, false);
      int i = Math.max(Math.max(this.width, this.height), this.depth);

      for(BlockPos blockpos : BlockPos.betweenClosed(pPos, pPos.offset(i - 1, i - 1, i - 1))) {
         for(Direction direction : Direction.values()) {
            for(Direction direction1 : Direction.values()) {
               if (direction1 != direction && direction1 != direction.getOpposite()) {
                  BlockPattern.PatternHelper blockpattern$patternhelper = this.matches(blockpos, direction, direction1, loadingcache);
                  if (blockpattern$patternhelper != null) {
                     return blockpattern$patternhelper;
                  }
               }
            }
         }
      }

      return null;
   }

   public static LoadingCache<BlockPos, CachedBlockInfo> createLevelCache(IWorldReader pLevel, boolean pForceLoad) {
      return CacheBuilder.newBuilder().build(new BlockPattern.CacheLoader(pLevel, pForceLoad));
   }

   /**
    * Offsets the position of pos in the direction of finger and thumb facing by offset amounts, follows the right-hand
    * rule for cross products (finger, thumb, palm)
    * 
    * @return a new BlockPos offset in the facing directions
    */
   protected static BlockPos translateAndRotate(BlockPos pPos, Direction pFinger, Direction pThumb, int pPalmOffset, int pThumbOffset, int pFingerOffset) {
      if (pFinger != pThumb && pFinger != pThumb.getOpposite()) {
         Vector3i vector3i = new Vector3i(pFinger.getStepX(), pFinger.getStepY(), pFinger.getStepZ());
         Vector3i vector3i1 = new Vector3i(pThumb.getStepX(), pThumb.getStepY(), pThumb.getStepZ());
         Vector3i vector3i2 = vector3i.cross(vector3i1);
         return pPos.offset(vector3i1.getX() * -pThumbOffset + vector3i2.getX() * pPalmOffset + vector3i.getX() * pFingerOffset, vector3i1.getY() * -pThumbOffset + vector3i2.getY() * pPalmOffset + vector3i.getY() * pFingerOffset, vector3i1.getZ() * -pThumbOffset + vector3i2.getZ() * pPalmOffset + vector3i.getZ() * pFingerOffset);
      } else {
         throw new IllegalArgumentException("Invalid forwards & up combination");
      }
   }

   static class CacheLoader extends com.google.common.cache.CacheLoader<BlockPos, CachedBlockInfo> {
      private final IWorldReader level;
      private final boolean loadChunks;

      public CacheLoader(IWorldReader pLevel, boolean pLoadChunks) {
         this.level = pLevel;
         this.loadChunks = pLoadChunks;
      }

      public CachedBlockInfo load(BlockPos p_load_1_) throws Exception {
         return new CachedBlockInfo(this.level, p_load_1_, this.loadChunks);
      }
   }

   public static class PatternHelper {
      private final BlockPos frontTopLeft;
      private final Direction forwards;
      private final Direction up;
      private final LoadingCache<BlockPos, CachedBlockInfo> cache;
      private final int width;
      private final int height;
      private final int depth;

      public PatternHelper(BlockPos pFrontTopLeft, Direction pForwards, Direction pUp, LoadingCache<BlockPos, CachedBlockInfo> pCache, int pWidth, int pHeight, int pDepth) {
         this.frontTopLeft = pFrontTopLeft;
         this.forwards = pForwards;
         this.up = pUp;
         this.cache = pCache;
         this.width = pWidth;
         this.height = pHeight;
         this.depth = pDepth;
      }

      /**
       * Return the BlockPos of the Pattern
       */
      public BlockPos getFrontTopLeft() {
         return this.frontTopLeft;
      }

      public Direction getForwards() {
         return this.forwards;
      }

      public Direction getUp() {
         return this.up;
      }

      public CachedBlockInfo getBlock(int pPalmOffset, int pThumbOffset, int pFingerOffset) {
         return this.cache.getUnchecked(BlockPattern.translateAndRotate(this.frontTopLeft, this.getForwards(), this.getUp(), pPalmOffset, pThumbOffset, pFingerOffset));
      }

      public String toString() {
         return MoreObjects.toStringHelper(this).add("up", this.up).add("forwards", this.forwards).add("frontTopLeft", this.frontTopLeft).toString();
      }
   }
}