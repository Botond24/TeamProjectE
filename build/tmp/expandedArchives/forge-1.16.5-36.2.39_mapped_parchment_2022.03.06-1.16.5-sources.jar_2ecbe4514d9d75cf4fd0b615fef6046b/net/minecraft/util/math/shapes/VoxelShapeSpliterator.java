package net.minecraft.util.math.shapes;

import java.util.Objects;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.CubeCoordinateIterator;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ICollisionReader;
import net.minecraft.world.border.WorldBorder;

public class VoxelShapeSpliterator extends AbstractSpliterator<VoxelShape> {
   @Nullable
   private final Entity source;
   private final AxisAlignedBB box;
   private final ISelectionContext context;
   private final CubeCoordinateIterator cursor;
   private final BlockPos.Mutable pos;
   private final VoxelShape entityShape;
   private final ICollisionReader collisionGetter;
   private boolean needsBorderCheck;
   private final BiPredicate<BlockState, BlockPos> predicate;

   public VoxelShapeSpliterator(ICollisionReader pGetter, @Nullable Entity pEntity, AxisAlignedBB pCollisionBox) {
      this(pGetter, pEntity, pCollisionBox, (p_241459_0_, p_241459_1_) -> {
         return true;
      });
   }

   public VoxelShapeSpliterator(ICollisionReader pCollisionGetter, @Nullable Entity pSource, AxisAlignedBB pBox, BiPredicate<BlockState, BlockPos> pPredicate) {
      super(Long.MAX_VALUE, 1280);
      this.context = pSource == null ? ISelectionContext.empty() : ISelectionContext.of(pSource);
      this.pos = new BlockPos.Mutable();
      this.entityShape = VoxelShapes.create(pBox);
      this.collisionGetter = pCollisionGetter;
      this.needsBorderCheck = pSource != null;
      this.source = pSource;
      this.box = pBox;
      this.predicate = pPredicate;
      int i = MathHelper.floor(pBox.minX - 1.0E-7D) - 1;
      int j = MathHelper.floor(pBox.maxX + 1.0E-7D) + 1;
      int k = MathHelper.floor(pBox.minY - 1.0E-7D) - 1;
      int l = MathHelper.floor(pBox.maxY + 1.0E-7D) + 1;
      int i1 = MathHelper.floor(pBox.minZ - 1.0E-7D) - 1;
      int j1 = MathHelper.floor(pBox.maxZ + 1.0E-7D) + 1;
      this.cursor = new CubeCoordinateIterator(i, k, i1, j, l, j1);
   }

   public boolean tryAdvance(Consumer<? super VoxelShape> p_tryAdvance_1_) {
      return this.needsBorderCheck && this.worldBorderCheck(p_tryAdvance_1_) || this.collisionCheck(p_tryAdvance_1_);
   }

   boolean collisionCheck(Consumer<? super VoxelShape> pConsumer) {
      while(true) {
         if (this.cursor.advance()) {
            int i = this.cursor.nextX();
            int j = this.cursor.nextY();
            int k = this.cursor.nextZ();
            int l = this.cursor.getNextType();
            if (l == 3) {
               continue;
            }

            IBlockReader iblockreader = this.getChunk(i, k);
            if (iblockreader == null) {
               continue;
            }

            this.pos.set(i, j, k);
            BlockState blockstate = iblockreader.getBlockState(this.pos);
            if (!this.predicate.test(blockstate, this.pos) || l == 1 && !blockstate.hasLargeCollisionShape() || l == 2 && !blockstate.is(Blocks.MOVING_PISTON)) {
               continue;
            }

            VoxelShape voxelshape = blockstate.getCollisionShape(this.collisionGetter, this.pos, this.context);
            if (voxelshape == VoxelShapes.block()) {
               if (!this.box.intersects((double)i, (double)j, (double)k, (double)i + 1.0D, (double)j + 1.0D, (double)k + 1.0D)) {
                  continue;
               }

               pConsumer.accept(voxelshape.move((double)i, (double)j, (double)k));
               return true;
            }

            VoxelShape voxelshape1 = voxelshape.move((double)i, (double)j, (double)k);
            if (!VoxelShapes.joinIsNotEmpty(voxelshape1, this.entityShape, IBooleanFunction.AND)) {
               continue;
            }

            pConsumer.accept(voxelshape1);
            return true;
         }

         return false;
      }
   }

   @Nullable
   private IBlockReader getChunk(int pX, int pZ) {
      int i = pX >> 4;
      int j = pZ >> 4;
      return this.collisionGetter.getChunkForCollisions(i, j);
   }

   boolean worldBorderCheck(Consumer<? super VoxelShape> pConsumer) {
      Objects.requireNonNull(this.source);
      this.needsBorderCheck = false;
      WorldBorder worldborder = this.collisionGetter.getWorldBorder();
      AxisAlignedBB axisalignedbb = this.source.getBoundingBox();
      if (!isBoxFullyWithinWorldBorder(worldborder, axisalignedbb)) {
         VoxelShape voxelshape = worldborder.getCollisionShape();
         if (!isOutsideBorder(voxelshape, axisalignedbb) && isCloseToBorder(voxelshape, axisalignedbb)) {
            pConsumer.accept(voxelshape);
            return true;
         }
      }

      return false;
   }

   private static boolean isCloseToBorder(VoxelShape pShape, AxisAlignedBB pCollisionBox) {
      return VoxelShapes.joinIsNotEmpty(pShape, VoxelShapes.create(pCollisionBox.inflate(1.0E-7D)), IBooleanFunction.AND);
   }

   private static boolean isOutsideBorder(VoxelShape pShape, AxisAlignedBB pCollisionBox) {
      return VoxelShapes.joinIsNotEmpty(pShape, VoxelShapes.create(pCollisionBox.deflate(1.0E-7D)), IBooleanFunction.AND);
   }

   public static boolean isBoxFullyWithinWorldBorder(WorldBorder pBorder, AxisAlignedBB pCollisionBox) {
      double d0 = (double)MathHelper.floor(pBorder.getMinX());
      double d1 = (double)MathHelper.floor(pBorder.getMinZ());
      double d2 = (double)MathHelper.ceil(pBorder.getMaxX());
      double d3 = (double)MathHelper.ceil(pBorder.getMaxZ());
      return pCollisionBox.minX > d0 && pCollisionBox.minX < d2 && pCollisionBox.minZ > d1 && pCollisionBox.minZ < d3 && pCollisionBox.maxX > d0 && pCollisionBox.maxX < d2 && pCollisionBox.maxZ > d1 && pCollisionBox.maxZ < d3;
   }
}