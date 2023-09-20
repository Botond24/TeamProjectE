package net.minecraft.world.gen.feature.structure;

import java.util.Random;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.Heightmap;

public abstract class ScatteredStructurePiece extends StructurePiece {
   protected final int width;
   protected final int height;
   protected final int depth;
   protected int heightPosition = -1;

   protected ScatteredStructurePiece(IStructurePieceType p_i51344_1_, Random p_i51344_2_, int p_i51344_3_, int p_i51344_4_, int p_i51344_5_, int p_i51344_6_, int p_i51344_7_, int p_i51344_8_) {
      super(p_i51344_1_, 0);
      this.width = p_i51344_6_;
      this.height = p_i51344_7_;
      this.depth = p_i51344_8_;
      this.setOrientation(Direction.Plane.HORIZONTAL.getRandomDirection(p_i51344_2_));
      if (this.getOrientation().getAxis() == Direction.Axis.Z) {
         this.boundingBox = new MutableBoundingBox(p_i51344_3_, p_i51344_4_, p_i51344_5_, p_i51344_3_ + p_i51344_6_ - 1, p_i51344_4_ + p_i51344_7_ - 1, p_i51344_5_ + p_i51344_8_ - 1);
      } else {
         this.boundingBox = new MutableBoundingBox(p_i51344_3_, p_i51344_4_, p_i51344_5_, p_i51344_3_ + p_i51344_8_ - 1, p_i51344_4_ + p_i51344_7_ - 1, p_i51344_5_ + p_i51344_6_ - 1);
      }

   }

   protected ScatteredStructurePiece(IStructurePieceType p_i51345_1_, CompoundNBT p_i51345_2_) {
      super(p_i51345_1_, p_i51345_2_);
      this.width = p_i51345_2_.getInt("Width");
      this.height = p_i51345_2_.getInt("Height");
      this.depth = p_i51345_2_.getInt("Depth");
      this.heightPosition = p_i51345_2_.getInt("HPos");
   }

   protected void addAdditionalSaveData(CompoundNBT p_143011_1_) {
      p_143011_1_.putInt("Width", this.width);
      p_143011_1_.putInt("Height", this.height);
      p_143011_1_.putInt("Depth", this.depth);
      p_143011_1_.putInt("HPos", this.heightPosition);
   }

   protected boolean updateAverageGroundHeight(IWorld pLevel, MutableBoundingBox pBounds, int pHeight) {
      if (this.heightPosition >= 0) {
         return true;
      } else {
         int i = 0;
         int j = 0;
         BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

         for(int k = this.boundingBox.z0; k <= this.boundingBox.z1; ++k) {
            for(int l = this.boundingBox.x0; l <= this.boundingBox.x1; ++l) {
               blockpos$mutable.set(l, 64, k);
               if (pBounds.isInside(blockpos$mutable)) {
                  i += pLevel.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, blockpos$mutable).getY();
                  ++j;
               }
            }
         }

         if (j == 0) {
            return false;
         } else {
            this.heightPosition = i / j;
            this.boundingBox.move(0, this.heightPosition - this.boundingBox.y0 + pHeight, 0);
            return true;
         }
      }
   }
}