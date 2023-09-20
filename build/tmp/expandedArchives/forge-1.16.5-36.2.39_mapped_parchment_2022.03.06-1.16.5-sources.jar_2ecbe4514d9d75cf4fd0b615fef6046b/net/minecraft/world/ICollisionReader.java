package net.minecraft.world;

import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapeSpliterator;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ICollisionReader extends IBlockReader {
   WorldBorder getWorldBorder();

   @Nullable
   IBlockReader getChunkForCollisions(int pChunkX, int pChunkZ);

   default boolean isUnobstructed(@Nullable Entity pEntity, VoxelShape pShape) {
      return true;
   }

   default boolean isUnobstructed(BlockState pState, BlockPos pPos, ISelectionContext pContext) {
      VoxelShape voxelshape = pState.getCollisionShape(this, pPos, pContext);
      return voxelshape.isEmpty() || this.isUnobstructed((Entity)null, voxelshape.move((double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ()));
   }

   default boolean isUnobstructed(Entity pEntity) {
      return this.isUnobstructed(pEntity, VoxelShapes.create(pEntity.getBoundingBox()));
   }

   default boolean noCollision(AxisAlignedBB pCollisionBox) {
      return this.noCollision((Entity)null, pCollisionBox, (p_234866_0_) -> {
         return true;
      });
   }

   default boolean noCollision(Entity pEntity) {
      return this.noCollision(pEntity, pEntity.getBoundingBox(), (p_234864_0_) -> {
         return true;
      });
   }

   default boolean noCollision(Entity pEntity, AxisAlignedBB pCollisionBox) {
      return this.noCollision(pEntity, pCollisionBox, (p_234863_0_) -> {
         return true;
      });
   }

   default boolean noCollision(@Nullable Entity pEntity, AxisAlignedBB pCollisionBox, Predicate<Entity> pEntityPredicate) {
      return this.getCollisions(pEntity, pCollisionBox, pEntityPredicate).allMatch(VoxelShape::isEmpty);
   }

   Stream<VoxelShape> getEntityCollisions(@Nullable Entity pEntity, AxisAlignedBB pArea, Predicate<Entity> pFilter);

   default Stream<VoxelShape> getCollisions(@Nullable Entity pEntity, AxisAlignedBB pCollisionBox, Predicate<Entity> pFilter) {
      return Stream.concat(this.getBlockCollisions(pEntity, pCollisionBox), this.getEntityCollisions(pEntity, pCollisionBox, pFilter));
   }

   default Stream<VoxelShape> getBlockCollisions(@Nullable Entity pEntity, AxisAlignedBB pCollisionBox) {
      return StreamSupport.stream(new VoxelShapeSpliterator(this, pEntity, pCollisionBox), false);
   }

   @OnlyIn(Dist.CLIENT)
   default boolean noBlockCollision(@Nullable Entity p_242405_1_, AxisAlignedBB p_242405_2_, BiPredicate<BlockState, BlockPos> p_242405_3_) {
      return this.getBlockCollisions(p_242405_1_, p_242405_2_, p_242405_3_).allMatch(VoxelShape::isEmpty);
   }

   default Stream<VoxelShape> getBlockCollisions(@Nullable Entity pEntity, AxisAlignedBB pCollisionBox, BiPredicate<BlockState, BlockPos> pFilter) {
      return StreamSupport.stream(new VoxelShapeSpliterator(this, pEntity, pCollisionBox, pFilter), false);
   }
}