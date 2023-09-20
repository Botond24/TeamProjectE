package net.minecraft.util.math;

import com.google.common.collect.AbstractIterator;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.concurrent.Immutable;
import net.minecraft.dispenser.IPosition;
import net.minecraft.util.AxisRotation;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Immutable
public class BlockPos extends Vector3i {
   public static final Codec<BlockPos> CODEC = Codec.INT_STREAM.comapFlatMap((p_239586_0_) -> {
      return Util.fixedSize(p_239586_0_, 3).map((p_239587_0_) -> {
         return new BlockPos(p_239587_0_[0], p_239587_0_[1], p_239587_0_[2]);
      });
   }, (p_239582_0_) -> {
      return IntStream.of(p_239582_0_.getX(), p_239582_0_.getY(), p_239582_0_.getZ());
   }).stable();
   private static final Logger LOGGER = LogManager.getLogger();
   /** An immutable BlockPos with zero as all coordinates. */
   public static final BlockPos ZERO = new BlockPos(0, 0, 0);
   private static final int PACKED_X_LENGTH = 1 + MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(30000000));
   private static final int PACKED_Z_LENGTH = PACKED_X_LENGTH;
   private static final int PACKED_Y_LENGTH = 64 - PACKED_X_LENGTH - PACKED_Z_LENGTH;
   private static final long PACKED_X_MASK = (1L << PACKED_X_LENGTH) - 1L;
   private static final long PACKED_Y_MASK = (1L << PACKED_Y_LENGTH) - 1L;
   private static final long PACKED_Z_MASK = (1L << PACKED_Z_LENGTH) - 1L;
   private static final int Z_OFFSET = PACKED_Y_LENGTH;
   private static final int X_OFFSET = PACKED_Y_LENGTH + PACKED_Z_LENGTH;

   /**
    * Constructs a {@code BlockPos} using the provided X, Y, and Z coordinates.
    */
   public BlockPos(int pX, int pY, int pZ) {
      super(pX, pY, pZ);
   }

   /**
    * Constructs a {@code BlockPos} using the provided X, Y, and Z coordinates.
    */
   public BlockPos(double p_i46031_1_, double p_i46031_3_, double p_i46031_5_) {
      super(p_i46031_1_, p_i46031_3_, p_i46031_5_);
   }

   /**
    * Constructs a {@code BlockPos} using the provided X, Y, and Z coordinates.
    */
   public BlockPos(Vector3d pVector) {
      this(pVector.x, pVector.y, pVector.z);
   }

   /**
    * Constructs a {@code BlockPos} using the provided X, Y, and Z coordinates.
    */
   public BlockPos(IPosition pPos) {
      this(pPos.x(), pPos.y(), pPos.z());
   }

   /**
    * Constructs a {@code BlockPos} using the provided X, Y, and Z coordinates.
    */
   public BlockPos(Vector3i pVector) {
      this(pVector.getX(), pVector.getY(), pVector.getZ());
   }

   public static long offset(long pPos, Direction pDirection) {
      return offset(pPos, pDirection.getStepX(), pDirection.getStepY(), pDirection.getStepZ());
   }

   public static long offset(long pPos, int pDx, int pDy, int pDz) {
      return asLong(getX(pPos) + pDx, getY(pPos) + pDy, getZ(pPos) + pDz);
   }

   public static int getX(long pPackedPos) {
      return (int)(pPackedPos << 64 - X_OFFSET - PACKED_X_LENGTH >> 64 - PACKED_X_LENGTH);
   }

   public static int getY(long pPackedPos) {
      return (int)(pPackedPos << 64 - PACKED_Y_LENGTH >> 64 - PACKED_Y_LENGTH);
   }

   public static int getZ(long pPackedPos) {
      return (int)(pPackedPos << 64 - Z_OFFSET - PACKED_Z_LENGTH >> 64 - PACKED_Z_LENGTH);
   }

   public static BlockPos of(long pPackedPos) {
      return new BlockPos(getX(pPackedPos), getY(pPackedPos), getZ(pPackedPos));
   }

   public long asLong() {
      return asLong(this.getX(), this.getY(), this.getZ());
   }

   public static long asLong(int pX, int pY, int pZ) {
      long i = 0L;
      i = i | ((long)pX & PACKED_X_MASK) << X_OFFSET;
      i = i | ((long)pY & PACKED_Y_MASK) << 0;
      return i | ((long)pZ & PACKED_Z_MASK) << Z_OFFSET;
   }

   public static long getFlatIndex(long pPackedPos) {
      return pPackedPos & -16L;
   }

   public BlockPos offset(double p_177963_1_, double p_177963_3_, double p_177963_5_) {
      return p_177963_1_ == 0.0D && p_177963_3_ == 0.0D && p_177963_5_ == 0.0D ? this : new BlockPos((double)this.getX() + p_177963_1_, (double)this.getY() + p_177963_3_, (double)this.getZ() + p_177963_5_);
   }

   public BlockPos offset(int p_177982_1_, int p_177982_2_, int p_177982_3_) {
      return p_177982_1_ == 0 && p_177982_2_ == 0 && p_177982_3_ == 0 ? this : new BlockPos(this.getX() + p_177982_1_, this.getY() + p_177982_2_, this.getZ() + p_177982_3_);
   }

   public BlockPos offset(Vector3i p_177971_1_) {
      return this.offset(p_177971_1_.getX(), p_177971_1_.getY(), p_177971_1_.getZ());
   }

   public BlockPos subtract(Vector3i p_177973_1_) {
      return this.offset(-p_177973_1_.getX(), -p_177973_1_.getY(), -p_177973_1_.getZ());
   }

   /**
    * Offset this vector 1 unit up
    */
   public BlockPos above() {
      return this.relative(Direction.UP);
   }

   /**
    * Offset this vector upwards by the given distance.
    */
   public BlockPos above(int pDistance) {
      return this.relative(Direction.UP, pDistance);
   }

   /**
    * Offset this vector 1 unit down
    */
   public BlockPos below() {
      return this.relative(Direction.DOWN);
   }

   /**
    * Offset this vector downwards by the given distance.
    */
   public BlockPos below(int pDistance) {
      return this.relative(Direction.DOWN, pDistance);
   }

   public BlockPos north() {
      return this.relative(Direction.NORTH);
   }

   public BlockPos north(int p_177964_1_) {
      return this.relative(Direction.NORTH, p_177964_1_);
   }

   public BlockPos south() {
      return this.relative(Direction.SOUTH);
   }

   public BlockPos south(int p_177970_1_) {
      return this.relative(Direction.SOUTH, p_177970_1_);
   }

   public BlockPos west() {
      return this.relative(Direction.WEST);
   }

   public BlockPos west(int p_177985_1_) {
      return this.relative(Direction.WEST, p_177985_1_);
   }

   public BlockPos east() {
      return this.relative(Direction.EAST);
   }

   public BlockPos east(int p_177965_1_) {
      return this.relative(Direction.EAST, p_177965_1_);
   }

   public BlockPos relative(Direction p_177972_1_) {
      return new BlockPos(this.getX() + p_177972_1_.getStepX(), this.getY() + p_177972_1_.getStepY(), this.getZ() + p_177972_1_.getStepZ());
   }

   /**
    * Offsets this Vector by the given distance in the specified direction.
    */
   public BlockPos relative(Direction pDirection, int pDistance) {
      return pDistance == 0 ? this : new BlockPos(this.getX() + pDirection.getStepX() * pDistance, this.getY() + pDirection.getStepY() * pDistance, this.getZ() + pDirection.getStepZ() * pDistance);
   }

   public BlockPos relative(Direction.Axis p_241872_1_, int p_241872_2_) {
      if (p_241872_2_ == 0) {
         return this;
      } else {
         int i = p_241872_1_ == Direction.Axis.X ? p_241872_2_ : 0;
         int j = p_241872_1_ == Direction.Axis.Y ? p_241872_2_ : 0;
         int k = p_241872_1_ == Direction.Axis.Z ? p_241872_2_ : 0;
         return new BlockPos(this.getX() + i, this.getY() + j, this.getZ() + k);
      }
   }

   public BlockPos rotate(Rotation pRotation) {
      switch(pRotation) {
      case NONE:
      default:
         return this;
      case CLOCKWISE_90:
         return new BlockPos(-this.getZ(), this.getY(), this.getX());
      case CLOCKWISE_180:
         return new BlockPos(-this.getX(), this.getY(), -this.getZ());
      case COUNTERCLOCKWISE_90:
         return new BlockPos(this.getZ(), this.getY(), -this.getX());
      }
   }

   /**
    * Calculate the cross product of this and the given Vector
    */
   public BlockPos cross(Vector3i pVector) {
      return new BlockPos(this.getY() * pVector.getZ() - this.getZ() * pVector.getY(), this.getZ() * pVector.getX() - this.getX() * pVector.getZ(), this.getX() * pVector.getY() - this.getY() * pVector.getX());
   }

   /**
    * Returns a version of this BlockPos that is guaranteed to be immutable.
    * 
    * <p>When storing a BlockPos given to you for an extended period of time, make sure you
    * use this in case the value is changed internally.</p>
    */
   public BlockPos immutable() {
      return this;
   }

   public BlockPos.Mutable mutable() {
      return new BlockPos.Mutable(this.getX(), this.getY(), this.getZ());
   }

   public static Iterable<BlockPos> randomBetweenClosed(Random pRandom, int pAmount, int pMinX, int pMinY, int pMinZ, int pMaxX, int pMaxY, int pMaxZ) {
      int i = pMaxX - pMinX + 1;
      int j = pMaxY - pMinY + 1;
      int k = pMaxZ - pMinZ + 1;
      return () -> {
         return new AbstractIterator<BlockPos>() {
            final BlockPos.Mutable nextPos = new BlockPos.Mutable();
            int counter = pAmount;

            protected BlockPos computeNext() {
               if (this.counter <= 0) {
                  return this.endOfData();
               } else {
                  BlockPos blockpos = this.nextPos.set(pMinX + pRandom.nextInt(i), pMinY + pRandom.nextInt(j), pMinZ + pRandom.nextInt(k));
                  --this.counter;
                  return blockpos;
               }
            }
         };
      };
   }

   /**
    * Returns BlockPos#getProximitySortedBoxPositions as an Iterator.
    */
   public static Iterable<BlockPos> withinManhattan(BlockPos pPos, int pXSize, int pYSize, int pZSize) {
      int i = pXSize + pYSize + pZSize;
      int j = pPos.getX();
      int k = pPos.getY();
      int l = pPos.getZ();
      return () -> {
         return new AbstractIterator<BlockPos>() {
            private final BlockPos.Mutable cursor = new BlockPos.Mutable();
            private int currentDepth;
            private int maxX;
            private int maxY;
            private int x;
            private int y;
            private boolean zMirror;

            protected BlockPos computeNext() {
               if (this.zMirror) {
                  this.zMirror = false;
                  this.cursor.setZ(l - (this.cursor.getZ() - l));
                  return this.cursor;
               } else {
                  BlockPos blockpos;
                  for(blockpos = null; blockpos == null; ++this.y) {
                     if (this.y > this.maxY) {
                        ++this.x;
                        if (this.x > this.maxX) {
                           ++this.currentDepth;
                           if (this.currentDepth > i) {
                              return this.endOfData();
                           }

                           this.maxX = Math.min(pXSize, this.currentDepth);
                           this.x = -this.maxX;
                        }

                        this.maxY = Math.min(pYSize, this.currentDepth - Math.abs(this.x));
                        this.y = -this.maxY;
                     }

                     int i1 = this.x;
                     int j1 = this.y;
                     int k1 = this.currentDepth - Math.abs(i1) - Math.abs(j1);
                     if (k1 <= pZSize) {
                        this.zMirror = k1 != 0;
                        blockpos = this.cursor.set(j + i1, k + j1, l + k1);
                     }
                  }

                  return blockpos;
               }
            }
         };
      };
   }

   public static Optional<BlockPos> findClosestMatch(BlockPos pPos, int pWidth, int pHeight, Predicate<BlockPos> pPosFilter) {
      return withinManhattanStream(pPos, pWidth, pHeight, pWidth).filter(pPosFilter).findFirst();
   }

   /**
    * Returns a stream of positions in a box shape, ordered by closest to furthest. Returns by definition the given
    * position as first element in the stream.
    */
   public static Stream<BlockPos> withinManhattanStream(BlockPos pPos, int pXSize, int pYSize, int pZSize) {
      return StreamSupport.stream(withinManhattan(pPos, pXSize, pYSize, pZSize).spliterator(), false);
   }

   public static Iterable<BlockPos> betweenClosed(BlockPos pFirstPos, BlockPos pSecondPos) {
      return betweenClosed(Math.min(pFirstPos.getX(), pSecondPos.getX()), Math.min(pFirstPos.getY(), pSecondPos.getY()), Math.min(pFirstPos.getZ(), pSecondPos.getZ()), Math.max(pFirstPos.getX(), pSecondPos.getX()), Math.max(pFirstPos.getY(), pSecondPos.getY()), Math.max(pFirstPos.getZ(), pSecondPos.getZ()));
   }

   public static Stream<BlockPos> betweenClosedStream(BlockPos pFirstPos, BlockPos pSecondPos) {
      return StreamSupport.stream(betweenClosed(pFirstPos, pSecondPos).spliterator(), false);
   }

   public static Stream<BlockPos> betweenClosedStream(MutableBoundingBox pBox) {
      return betweenClosedStream(Math.min(pBox.x0, pBox.x1), Math.min(pBox.y0, pBox.y1), Math.min(pBox.z0, pBox.z1), Math.max(pBox.x0, pBox.x1), Math.max(pBox.y0, pBox.y1), Math.max(pBox.z0, pBox.z1));
   }

   public static Stream<BlockPos> betweenClosedStream(AxisAlignedBB pAabb) {
      return betweenClosedStream(MathHelper.floor(pAabb.minX), MathHelper.floor(pAabb.minY), MathHelper.floor(pAabb.minZ), MathHelper.floor(pAabb.maxX), MathHelper.floor(pAabb.maxY), MathHelper.floor(pAabb.maxZ));
   }

   public static Stream<BlockPos> betweenClosedStream(int pMinX, int pMinY, int pMinZ, int pMaxX, int pMaxY, int pMaxZ) {
      return StreamSupport.stream(betweenClosed(pMinX, pMinY, pMinZ, pMaxX, pMaxY, pMaxZ).spliterator(), false);
   }

   /**
    * Creates an Iterable that returns all positions in the box specified by the given corners. <strong>Coordinates must
    * be in order</strong>" e.g. x1 <= x2.
    * 
    * This method uses {@link BlockPos.MutableBlockPos MutableBlockPos} instead of regular BlockPos, which grants better
    * performance. However, the resulting BlockPos instances can only be used inside the iteration loop (as otherwise
    * the value will change), unless {@link #toImmutable()} is called. This method is ideal for searching large areas
    * and only storing a few locations.
    * 
    * @see #betweenClosed(BlockPos, BlockPos)
    * @see #betweenClosed(int, int, int, int, int, int)
    */
   public static Iterable<BlockPos> betweenClosed(int pX1, int pY1, int pZ1, int pX2, int pY2, int pZ2) {
      int i = pX2 - pX1 + 1;
      int j = pY2 - pY1 + 1;
      int k = pZ2 - pZ1 + 1;
      int l = i * j * k;
      return () -> {
         return new AbstractIterator<BlockPos>() {
            private final BlockPos.Mutable cursor = new BlockPos.Mutable();
            private int index;

            protected BlockPos computeNext() {
               if (this.index == l) {
                  return this.endOfData();
               } else {
                  int i1 = this.index % i;
                  int j1 = this.index / i;
                  int k1 = j1 % j;
                  int l1 = j1 / j;
                  ++this.index;
                  return this.cursor.set(pX1 + i1, pY1 + k1, pZ1 + l1);
               }
            }
         };
      };
   }

   public static Iterable<BlockPos.Mutable> spiralAround(BlockPos p_243514_0_, int p_243514_1_, Direction p_243514_2_, Direction p_243514_3_) {
      Validate.validState(p_243514_2_.getAxis() != p_243514_3_.getAxis(), "The two directions cannot be on the same axis");
      return () -> {
         return new AbstractIterator<BlockPos.Mutable>() {
            private final Direction[] directions = new Direction[]{p_243514_2_, p_243514_3_, p_243514_2_.getOpposite(), p_243514_3_.getOpposite()};
            private final BlockPos.Mutable cursor = p_243514_0_.mutable().move(p_243514_3_);
            private final int legs = 4 * p_243514_1_;
            private int leg = -1;
            private int legSize;
            private int legIndex;
            private int lastX = this.cursor.getX();
            private int lastY = this.cursor.getY();
            private int lastZ = this.cursor.getZ();

            protected BlockPos.Mutable computeNext() {
               this.cursor.set(this.lastX, this.lastY, this.lastZ).move(this.directions[(this.leg + 4) % 4]);
               this.lastX = this.cursor.getX();
               this.lastY = this.cursor.getY();
               this.lastZ = this.cursor.getZ();
               if (this.legIndex >= this.legSize) {
                  if (this.leg >= this.legs) {
                     return this.endOfData();
                  }

                  ++this.leg;
                  this.legIndex = 0;
                  this.legSize = this.leg / 2 + 1;
               }

               ++this.legIndex;
               return this.cursor;
            }
         };
      };
   }

   public static class Mutable extends BlockPos {
      public Mutable() {
         this(0, 0, 0);
      }

      public Mutable(int p_i46024_1_, int p_i46024_2_, int p_i46024_3_) {
         super(p_i46024_1_, p_i46024_2_, p_i46024_3_);
      }

      public Mutable(double p_i50824_1_, double p_i50824_3_, double p_i50824_5_) {
         this(MathHelper.floor(p_i50824_1_), MathHelper.floor(p_i50824_3_), MathHelper.floor(p_i50824_5_));
      }

      public BlockPos offset(double p_177963_1_, double p_177963_3_, double p_177963_5_) {
         return super.offset(p_177963_1_, p_177963_3_, p_177963_5_).immutable();
      }

      public BlockPos offset(int p_177982_1_, int p_177982_2_, int p_177982_3_) {
         return super.offset(p_177982_1_, p_177982_2_, p_177982_3_).immutable();
      }

      /**
       * Offsets this Vector by the given distance in the specified direction.
       */
      public BlockPos relative(Direction pDirection, int pDistance) {
         return super.relative(pDirection, pDistance).immutable();
      }

      public BlockPos relative(Direction.Axis p_241872_1_, int p_241872_2_) {
         return super.relative(p_241872_1_, p_241872_2_).immutable();
      }

      public BlockPos rotate(Rotation pRotation) {
         return super.rotate(pRotation).immutable();
      }

      /**
       * Sets position
       */
      public BlockPos.Mutable set(int pX, int pY, int pZ) {
         this.setX(pX);
         this.setY(pY);
         this.setZ(pZ);
         return this;
      }

      public BlockPos.Mutable set(double pX, double pY, double pZ) {
         return this.set(MathHelper.floor(pX), MathHelper.floor(pY), MathHelper.floor(pZ));
      }

      public BlockPos.Mutable set(Vector3i pVector) {
         return this.set(pVector.getX(), pVector.getY(), pVector.getZ());
      }

      public BlockPos.Mutable set(long pPackedPos) {
         return this.set(getX(pPackedPos), getY(pPackedPos), getZ(pPackedPos));
      }

      public BlockPos.Mutable set(AxisRotation pCycle, int pX, int pY, int pZ) {
         return this.set(pCycle.cycle(pX, pY, pZ, Direction.Axis.X), pCycle.cycle(pX, pY, pZ, Direction.Axis.Y), pCycle.cycle(pX, pY, pZ, Direction.Axis.Z));
      }

      public BlockPos.Mutable setWithOffset(Vector3i pPos, Direction pDirection) {
         return this.set(pPos.getX() + pDirection.getStepX(), pPos.getY() + pDirection.getStepY(), pPos.getZ() + pDirection.getStepZ());
      }

      public BlockPos.Mutable setWithOffset(Vector3i pVector, int pOffsetX, int pOffsetY, int pOffsetZ) {
         return this.set(pVector.getX() + pOffsetX, pVector.getY() + pOffsetY, pVector.getZ() + pOffsetZ);
      }

      public BlockPos.Mutable move(Direction pDirection) {
         return this.move(pDirection, 1);
      }

      public BlockPos.Mutable move(Direction pDirection, int pN) {
         return this.set(this.getX() + pDirection.getStepX() * pN, this.getY() + pDirection.getStepY() * pN, this.getZ() + pDirection.getStepZ() * pN);
      }

      public BlockPos.Mutable move(int pX, int pY, int pZ) {
         return this.set(this.getX() + pX, this.getY() + pY, this.getZ() + pZ);
      }

      public BlockPos.Mutable move(Vector3i p_243531_1_) {
         return this.set(this.getX() + p_243531_1_.getX(), this.getY() + p_243531_1_.getY(), this.getZ() + p_243531_1_.getZ());
      }

      public BlockPos.Mutable clamp(Direction.Axis pAxis, int pMin, int pMax) {
         switch(pAxis) {
         case X:
            return this.set(MathHelper.clamp(this.getX(), pMin, pMax), this.getY(), this.getZ());
         case Y:
            return this.set(this.getX(), MathHelper.clamp(this.getY(), pMin, pMax), this.getZ());
         case Z:
            return this.set(this.getX(), this.getY(), MathHelper.clamp(this.getZ(), pMin, pMax));
         default:
            throw new IllegalStateException("Unable to clamp axis " + pAxis);
         }
      }

      public void setX(int p_223471_1_) {
         super.setX(p_223471_1_);
      }

      public void setY(int p_185336_1_) {
         super.setY(p_185336_1_);
      }

      public void setZ(int p_223472_1_) {
         super.setZ(p_223472_1_);
      }

      /**
       * Returns a version of this BlockPos that is guaranteed to be immutable.
       * 
       * <p>When storing a BlockPos given to you for an extended period of time, make sure you
       * use this in case the value is changed internally.</p>
       */
      public BlockPos immutable() {
         return new BlockPos(this);
      }
   }
}