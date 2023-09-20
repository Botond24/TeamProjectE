package net.minecraft.world.gen.feature.structure;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;

public abstract class StructurePiece {
   protected static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
   protected MutableBoundingBox boundingBox;
   @Nullable
   private Direction orientation;
   private Mirror mirror;
   private Rotation rotation;
   protected int genDepth;
   private final IStructurePieceType type;
   private static final Set<Block> SHAPE_CHECK_BLOCKS = ImmutableSet.<Block>builder().add(Blocks.NETHER_BRICK_FENCE).add(Blocks.TORCH).add(Blocks.WALL_TORCH).add(Blocks.OAK_FENCE).add(Blocks.SPRUCE_FENCE).add(Blocks.DARK_OAK_FENCE).add(Blocks.ACACIA_FENCE).add(Blocks.BIRCH_FENCE).add(Blocks.JUNGLE_FENCE).add(Blocks.LADDER).add(Blocks.IRON_BARS).build();

   protected StructurePiece(IStructurePieceType p_i51342_1_, int p_i51342_2_) {
      this.type = p_i51342_1_;
      this.genDepth = p_i51342_2_;
   }

   public StructurePiece(IStructurePieceType pType, CompoundNBT pTag) {
      this(pType, pTag.getInt("GD"));
      if (pTag.contains("BB")) {
         this.boundingBox = new MutableBoundingBox(pTag.getIntArray("BB"));
      }

      int i = pTag.getInt("O");
      this.setOrientation(i == -1 ? null : Direction.from2DDataValue(i));
   }

   public final CompoundNBT createTag() {
      if (Registry.STRUCTURE_PIECE.getKey(this.getType()) == null) { // FORGE: Friendlier error then the Null String error below.
         throw new RuntimeException("StructurePiece \"" + this.getClass().getName() + "\": \"" + this.getType() + "\" missing ID Mapping, Modder see MapGenStructureIO");
      }
      CompoundNBT compoundnbt = new CompoundNBT();
      compoundnbt.putString("id", Registry.STRUCTURE_PIECE.getKey(this.getType()).toString());
      compoundnbt.put("BB", this.boundingBox.createTag());
      Direction direction = this.getOrientation();
      compoundnbt.putInt("O", direction == null ? -1 : direction.get2DDataValue());
      compoundnbt.putInt("GD", this.genDepth);
      this.addAdditionalSaveData(compoundnbt);
      return compoundnbt;
   }

   protected abstract void addAdditionalSaveData(CompoundNBT p_143011_1_);

   public void addChildren(StructurePiece p_74861_1_, List<StructurePiece> p_74861_2_, Random p_74861_3_) {
   }

   public abstract boolean postProcess(ISeedReader pLevel, StructureManager pStructureManager, ChunkGenerator pChunkGenerator, Random pRandom, MutableBoundingBox pBox, ChunkPos pChunkPos, BlockPos pPos);

   public MutableBoundingBox getBoundingBox() {
      return this.boundingBox;
   }

   /**
    * Returns the component type ID of this component.
    */
   public int getGenDepth() {
      return this.genDepth;
   }

   public boolean isCloseToChunk(ChunkPos pChunkPos, int pDistance) {
      int i = pChunkPos.x << 4;
      int j = pChunkPos.z << 4;
      return this.boundingBox.intersects(i - pDistance, j - pDistance, i + 15 + pDistance, j + 15 + pDistance);
   }

   public static StructurePiece findCollisionPiece(List<StructurePiece> p_74883_0_, MutableBoundingBox p_74883_1_) {
      for(StructurePiece structurepiece : p_74883_0_) {
         if (structurepiece.getBoundingBox() != null && structurepiece.getBoundingBox().intersects(p_74883_1_)) {
            return structurepiece;
         }
      }

      return null;
   }

   protected boolean edgesLiquid(IBlockReader p_74860_1_, MutableBoundingBox p_74860_2_) {
      int i = Math.max(this.boundingBox.x0 - 1, p_74860_2_.x0);
      int j = Math.max(this.boundingBox.y0 - 1, p_74860_2_.y0);
      int k = Math.max(this.boundingBox.z0 - 1, p_74860_2_.z0);
      int l = Math.min(this.boundingBox.x1 + 1, p_74860_2_.x1);
      int i1 = Math.min(this.boundingBox.y1 + 1, p_74860_2_.y1);
      int j1 = Math.min(this.boundingBox.z1 + 1, p_74860_2_.z1);
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

      for(int k1 = i; k1 <= l; ++k1) {
         for(int l1 = k; l1 <= j1; ++l1) {
            if (p_74860_1_.getBlockState(blockpos$mutable.set(k1, j, l1)).getMaterial().isLiquid()) {
               return true;
            }

            if (p_74860_1_.getBlockState(blockpos$mutable.set(k1, i1, l1)).getMaterial().isLiquid()) {
               return true;
            }
         }
      }

      for(int i2 = i; i2 <= l; ++i2) {
         for(int k2 = j; k2 <= i1; ++k2) {
            if (p_74860_1_.getBlockState(blockpos$mutable.set(i2, k2, k)).getMaterial().isLiquid()) {
               return true;
            }

            if (p_74860_1_.getBlockState(blockpos$mutable.set(i2, k2, j1)).getMaterial().isLiquid()) {
               return true;
            }
         }
      }

      for(int j2 = k; j2 <= j1; ++j2) {
         for(int l2 = j; l2 <= i1; ++l2) {
            if (p_74860_1_.getBlockState(blockpos$mutable.set(i, l2, j2)).getMaterial().isLiquid()) {
               return true;
            }

            if (p_74860_1_.getBlockState(blockpos$mutable.set(l, l2, j2)).getMaterial().isLiquid()) {
               return true;
            }
         }
      }

      return false;
   }

   protected int getWorldX(int pX, int pZ) {
      Direction direction = this.getOrientation();
      if (direction == null) {
         return pX;
      } else {
         switch(direction) {
         case NORTH:
         case SOUTH:
            return this.boundingBox.x0 + pX;
         case WEST:
            return this.boundingBox.x1 - pZ;
         case EAST:
            return this.boundingBox.x0 + pZ;
         default:
            return pX;
         }
      }
   }

   protected int getWorldY(int pY) {
      return this.getOrientation() == null ? pY : pY + this.boundingBox.y0;
   }

   protected int getWorldZ(int pX, int pZ) {
      Direction direction = this.getOrientation();
      if (direction == null) {
         return pZ;
      } else {
         switch(direction) {
         case NORTH:
            return this.boundingBox.z1 - pZ;
         case SOUTH:
            return this.boundingBox.z0 + pZ;
         case WEST:
         case EAST:
            return this.boundingBox.z0 + pX;
         default:
            return pZ;
         }
      }
   }

   protected void placeBlock(ISeedReader pLevel, BlockState pBlockstate, int pX, int pY, int pZ, MutableBoundingBox pBoundingbox) {
      BlockPos blockpos = new BlockPos(this.getWorldX(pX, pZ), this.getWorldY(pY), this.getWorldZ(pX, pZ));
      if (pBoundingbox.isInside(blockpos)) {
         if (this.mirror != Mirror.NONE) {
            pBlockstate = pBlockstate.mirror(this.mirror);
         }

         if (this.rotation != Rotation.NONE) {
            pBlockstate = pBlockstate.rotate(this.rotation);
         }

         pLevel.setBlock(blockpos, pBlockstate, 2);
         FluidState fluidstate = pLevel.getFluidState(blockpos);
         if (!fluidstate.isEmpty()) {
            pLevel.getLiquidTicks().scheduleTick(blockpos, fluidstate.getType(), 0);
         }

         if (SHAPE_CHECK_BLOCKS.contains(pBlockstate.getBlock())) {
            pLevel.getChunk(blockpos).markPosForPostprocessing(blockpos);
         }

      }
   }

   protected BlockState getBlock(IBlockReader pLevel, int pX, int pY, int pZ, MutableBoundingBox pBoundingbox) {
      int i = this.getWorldX(pX, pZ);
      int j = this.getWorldY(pY);
      int k = this.getWorldZ(pX, pZ);
      BlockPos blockpos = new BlockPos(i, j, k);
      return !pBoundingbox.isInside(blockpos) ? Blocks.AIR.defaultBlockState() : pLevel.getBlockState(blockpos);
   }

   protected boolean isInterior(IWorldReader pLevel, int pX, int pY, int pZ, MutableBoundingBox pBoundingbox) {
      int i = this.getWorldX(pX, pZ);
      int j = this.getWorldY(pY + 1);
      int k = this.getWorldZ(pX, pZ);
      BlockPos blockpos = new BlockPos(i, j, k);
      if (!pBoundingbox.isInside(blockpos)) {
         return false;
      } else {
         return j < pLevel.getHeight(Heightmap.Type.OCEAN_FLOOR_WG, i, k);
      }
   }

   /**
    * arguments: (World worldObj, StructureBoundingBox structBB, int minX, int minY, int minZ, int maxX, int maxY, int
    * maxZ)
    */
   protected void generateAirBox(ISeedReader pLevel, MutableBoundingBox pStructurebb, int pMinX, int pMinY, int pMinZ, int pMaxX, int pMaxY, int pMaxZ) {
      for(int i = pMinY; i <= pMaxY; ++i) {
         for(int j = pMinX; j <= pMaxX; ++j) {
            for(int k = pMinZ; k <= pMaxZ; ++k) {
               this.placeBlock(pLevel, Blocks.AIR.defaultBlockState(), j, i, k, pStructurebb);
            }
         }
      }

   }

   /**
    * Fill the given area with the selected blocks
    */
   protected void generateBox(ISeedReader pLevel, MutableBoundingBox pBoundingbox, int pXMin, int pYMin, int pZMin, int pXMax, int pYMax, int pZMax, BlockState pBoundaryBlockState, BlockState pInsideBlockState, boolean pExistingOnly) {
      for(int i = pYMin; i <= pYMax; ++i) {
         for(int j = pXMin; j <= pXMax; ++j) {
            for(int k = pZMin; k <= pZMax; ++k) {
               if (!pExistingOnly || !this.getBlock(pLevel, j, i, k, pBoundingbox).isAir()) {
                  if (i != pYMin && i != pYMax && j != pXMin && j != pXMax && k != pZMin && k != pZMax) {
                     this.placeBlock(pLevel, pInsideBlockState, j, i, k, pBoundingbox);
                  } else {
                     this.placeBlock(pLevel, pBoundaryBlockState, j, i, k, pBoundingbox);
                  }
               }
            }
         }
      }

   }

   /**
    * arguments: World worldObj, StructureBoundingBox structBB, int minX, int minY, int minZ, int maxX, int maxY, int
    * maxZ, boolean alwaysreplace, Random rand, StructurePieceBlockSelector blockselector
    */
   protected void generateBox(ISeedReader pLevel, MutableBoundingBox pBoundingbox, int pMinX, int pMinY, int pMinZ, int pMaxX, int pMaxY, int pMaxZ, boolean pAlwaysReplace, Random pRandom, StructurePiece.BlockSelector pBlockselector) {
      for(int i = pMinY; i <= pMaxY; ++i) {
         for(int j = pMinX; j <= pMaxX; ++j) {
            for(int k = pMinZ; k <= pMaxZ; ++k) {
               if (!pAlwaysReplace || !this.getBlock(pLevel, j, i, k, pBoundingbox).isAir()) {
                  pBlockselector.next(pRandom, j, i, k, i == pMinY || i == pMaxY || j == pMinX || j == pMaxX || k == pMinZ || k == pMaxZ);
                  this.placeBlock(pLevel, pBlockselector.getNext(), j, i, k, pBoundingbox);
               }
            }
         }
      }

   }

   protected void generateMaybeBox(ISeedReader pLevel, MutableBoundingBox pSbb, Random pRandom, float pChance, int pX1, int pY1, int pZ1, int pX2, int pY2, int pZ2, BlockState pEdgeState, BlockState pState, boolean pRequireNonAir, boolean pRequiredSkylight) {
      for(int i = pY1; i <= pY2; ++i) {
         for(int j = pX1; j <= pX2; ++j) {
            for(int k = pZ1; k <= pZ2; ++k) {
               if (!(pRandom.nextFloat() > pChance) && (!pRequireNonAir || !this.getBlock(pLevel, j, i, k, pSbb).isAir()) && (!pRequiredSkylight || this.isInterior(pLevel, j, i, k, pSbb))) {
                  if (i != pY1 && i != pY2 && j != pX1 && j != pX2 && k != pZ1 && k != pZ2) {
                     this.placeBlock(pLevel, pState, j, i, k, pSbb);
                  } else {
                     this.placeBlock(pLevel, pEdgeState, j, i, k, pSbb);
                  }
               }
            }
         }
      }

   }

   protected void maybeGenerateBlock(ISeedReader pLevel, MutableBoundingBox pBoundingbox, Random pRandom, float pChance, int pX, int pY, int pZ, BlockState pBlockstate) {
      if (pRandom.nextFloat() < pChance) {
         this.placeBlock(pLevel, pBlockstate, pX, pY, pZ, pBoundingbox);
      }

   }

   protected void generateUpperHalfSphere(ISeedReader pLevel, MutableBoundingBox pBoundingbox, int pMinX, int pMinY, int pMinZ, int pMaxX, int pMaxY, int pMaxZ, BlockState pBlockstate, boolean pExcludeAir) {
      float f = (float)(pMaxX - pMinX + 1);
      float f1 = (float)(pMaxY - pMinY + 1);
      float f2 = (float)(pMaxZ - pMinZ + 1);
      float f3 = (float)pMinX + f / 2.0F;
      float f4 = (float)pMinZ + f2 / 2.0F;

      for(int i = pMinY; i <= pMaxY; ++i) {
         float f5 = (float)(i - pMinY) / f1;

         for(int j = pMinX; j <= pMaxX; ++j) {
            float f6 = ((float)j - f3) / (f * 0.5F);

            for(int k = pMinZ; k <= pMaxZ; ++k) {
               float f7 = ((float)k - f4) / (f2 * 0.5F);
               if (!pExcludeAir || !this.getBlock(pLevel, j, i, k, pBoundingbox).isAir()) {
                  float f8 = f6 * f6 + f5 * f5 + f7 * f7;
                  if (f8 <= 1.05F) {
                     this.placeBlock(pLevel, pBlockstate, j, i, k, pBoundingbox);
                  }
               }
            }
         }
      }

   }

   /**
    * Replaces air and liquid from given position downwards. Stops when hitting anything else than air or liquid
    */
   protected void fillColumnDown(ISeedReader pLevel, BlockState pBlockstate, int pX, int pY, int pZ, MutableBoundingBox pBoundingbox) {
      int i = this.getWorldX(pX, pZ);
      int j = this.getWorldY(pY);
      int k = this.getWorldZ(pX, pZ);
      if (pBoundingbox.isInside(new BlockPos(i, j, k))) {
         while((pLevel.isEmptyBlock(new BlockPos(i, j, k)) || pLevel.getBlockState(new BlockPos(i, j, k)).getMaterial().isLiquid()) && j > 1) {
            pLevel.setBlock(new BlockPos(i, j, k), pBlockstate, 2);
            --j;
         }

      }
   }

   /**
    * Adds chest to the structure and sets its contents
    */
   protected boolean createChest(ISeedReader pLevel, MutableBoundingBox pStructurebb, Random pRandom, int pX, int pY, int pZ, ResourceLocation pLoot) {
      BlockPos blockpos = new BlockPos(this.getWorldX(pX, pZ), this.getWorldY(pY), this.getWorldZ(pX, pZ));
      return this.createChest(pLevel, pStructurebb, pRandom, blockpos, pLoot, (BlockState)null);
   }

   public static BlockState reorient(IBlockReader pLevel, BlockPos pPos, BlockState pBlockState) {
      Direction direction = null;

      for(Direction direction1 : Direction.Plane.HORIZONTAL) {
         BlockPos blockpos = pPos.relative(direction1);
         BlockState blockstate = pLevel.getBlockState(blockpos);
         if (blockstate.is(Blocks.CHEST)) {
            return pBlockState;
         }

         if (blockstate.isSolidRender(pLevel, blockpos)) {
            if (direction != null) {
               direction = null;
               break;
            }

            direction = direction1;
         }
      }

      if (direction != null) {
         return pBlockState.setValue(HorizontalBlock.FACING, direction.getOpposite());
      } else {
         Direction direction2 = pBlockState.getValue(HorizontalBlock.FACING);
         BlockPos blockpos1 = pPos.relative(direction2);
         if (pLevel.getBlockState(blockpos1).isSolidRender(pLevel, blockpos1)) {
            direction2 = direction2.getOpposite();
            blockpos1 = pPos.relative(direction2);
         }

         if (pLevel.getBlockState(blockpos1).isSolidRender(pLevel, blockpos1)) {
            direction2 = direction2.getClockWise();
            blockpos1 = pPos.relative(direction2);
         }

         if (pLevel.getBlockState(blockpos1).isSolidRender(pLevel, blockpos1)) {
            direction2 = direction2.getOpposite();
            pPos.relative(direction2);
         }

         return pBlockState.setValue(HorizontalBlock.FACING, direction2);
      }
   }

   protected boolean createChest(IServerWorld pLevel, MutableBoundingBox pBounds, Random pRandom, BlockPos pPos, ResourceLocation pResourceLocation, @Nullable BlockState pState) {
      if (pBounds.isInside(pPos) && !pLevel.getBlockState(pPos).is(Blocks.CHEST)) {
         if (pState == null) {
            pState = reorient(pLevel, pPos, Blocks.CHEST.defaultBlockState());
         }

         pLevel.setBlock(pPos, pState, 2);
         TileEntity tileentity = pLevel.getBlockEntity(pPos);
         if (tileentity instanceof ChestTileEntity) {
            ((ChestTileEntity)tileentity).setLootTable(pResourceLocation, pRandom.nextLong());
         }

         return true;
      } else {
         return false;
      }
   }

   protected boolean createDispenser(ISeedReader pLevel, MutableBoundingBox pSbb, Random pRandom, int pX, int pY, int pZ, Direction pFacing, ResourceLocation pLootTable) {
      BlockPos blockpos = new BlockPos(this.getWorldX(pX, pZ), this.getWorldY(pY), this.getWorldZ(pX, pZ));
      if (pSbb.isInside(blockpos) && !pLevel.getBlockState(blockpos).is(Blocks.DISPENSER)) {
         this.placeBlock(pLevel, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, pFacing), pX, pY, pZ, pSbb);
         TileEntity tileentity = pLevel.getBlockEntity(blockpos);
         if (tileentity instanceof DispenserTileEntity) {
            ((DispenserTileEntity)tileentity).setLootTable(pLootTable, pRandom.nextLong());
         }

         return true;
      } else {
         return false;
      }
   }

   public void move(int pX, int pY, int pZ) {
      this.boundingBox.move(pX, pY, pZ);
   }

   @Nullable
   public Direction getOrientation() {
      return this.orientation;
   }

   public void setOrientation(@Nullable Direction pFacing) {
      this.orientation = pFacing;
      if (pFacing == null) {
         this.rotation = Rotation.NONE;
         this.mirror = Mirror.NONE;
      } else {
         switch(pFacing) {
         case SOUTH:
            this.mirror = Mirror.LEFT_RIGHT;
            this.rotation = Rotation.NONE;
            break;
         case WEST:
            this.mirror = Mirror.LEFT_RIGHT;
            this.rotation = Rotation.CLOCKWISE_90;
            break;
         case EAST:
            this.mirror = Mirror.NONE;
            this.rotation = Rotation.CLOCKWISE_90;
            break;
         default:
            this.mirror = Mirror.NONE;
            this.rotation = Rotation.NONE;
         }
      }

   }

   public Rotation getRotation() {
      return this.rotation;
   }

   public IStructurePieceType getType() {
      return this.type;
   }

   public abstract static class BlockSelector {
      protected BlockState next = Blocks.AIR.defaultBlockState();

      protected BlockSelector() {
      }

      /**
       * picks Block Ids and Metadata (Silverfish)
       */
      public abstract void next(Random pRandom, int pX, int pY, int pZ, boolean pWall);

      public BlockState getNext() {
         return this.next;
      }
   }
}
