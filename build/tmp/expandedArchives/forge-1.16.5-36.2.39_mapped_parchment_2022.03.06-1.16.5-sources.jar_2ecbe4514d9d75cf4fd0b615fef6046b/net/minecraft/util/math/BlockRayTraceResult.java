package net.minecraft.util.math;

import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

public class BlockRayTraceResult extends RayTraceResult {
   private final Direction direction;
   private final BlockPos blockPos;
   private final boolean miss;
   private final boolean inside;

   /**
    * Creates a new BlockRayTraceResult marked as a miss.
    */
   public static BlockRayTraceResult miss(Vector3d pLocation, Direction pFace, BlockPos pPos) {
      return new BlockRayTraceResult(true, pLocation, pFace, pPos, false);
   }

   public BlockRayTraceResult(Vector3d pLocation, Direction pDirection, BlockPos pBlockPos, boolean pInside) {
      this(false, pLocation, pDirection, pBlockPos, pInside);
   }

   private BlockRayTraceResult(boolean pMiss, Vector3d pLocation, Direction pDirection, BlockPos pBlockPos, boolean pInside) {
      super(pLocation);
      this.miss = pMiss;
      this.direction = pDirection;
      this.blockPos = pBlockPos;
      this.inside = pInside;
   }

   /**
    * Creates a new BlockRayTraceResult, with the clicked face replaced with the given one
    */
   public BlockRayTraceResult withDirection(Direction pNewFace) {
      return new BlockRayTraceResult(this.miss, this.location, pNewFace, this.blockPos, this.inside);
   }

   public BlockRayTraceResult withPosition(BlockPos pPos) {
      return new BlockRayTraceResult(this.miss, this.location, this.direction, pPos, this.inside);
   }

   public BlockPos getBlockPos() {
      return this.blockPos;
   }

   /**
    * Gets the face of the block that was clicked
    */
   public Direction getDirection() {
      return this.direction;
   }

   public RayTraceResult.Type getType() {
      return this.miss ? RayTraceResult.Type.MISS : RayTraceResult.Type.BLOCK;
   }

   /**
    * True if the player's head is inside of a block (used by scaffolding)
    */
   public boolean isInside() {
      return this.inside;
   }
}