package net.minecraft.block;

import net.minecraft.potion.Effect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;

public class FlowerBlock extends BushBlock {
   protected static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 10.0D, 11.0D);
   private final Effect suspiciousStewEffect;
   private final int effectDuration;

   public FlowerBlock(Effect pSuspiciousStewEffect, int pEffectDuration, AbstractBlock.Properties pProperties) {
      super(pProperties);
      this.suspiciousStewEffect = pSuspiciousStewEffect;
      if (pSuspiciousStewEffect.isInstantenous()) {
         this.effectDuration = pEffectDuration;
      } else {
         this.effectDuration = pEffectDuration * 20;
      }

   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      Vector3d vector3d = pState.getOffset(pLevel, pPos);
      return SHAPE.move(vector3d.x, vector3d.y, vector3d.z);
   }

   /**
    * Get the OffsetType for this Block. Determines if the model is rendered slightly offset.
    */
   public AbstractBlock.OffsetType getOffsetType() {
      return AbstractBlock.OffsetType.XZ;
   }

   /**
    * @return the effect that is applied when making suspicious stew with this flower.
    */
   public Effect getSuspiciousStewEffect() {
      return this.suspiciousStewEffect;
   }

   /**
    * @return the duration of the effect granted by a suspicious stew made with this flower.
    */
   public int getEffectDuration() {
      return this.effectDuration;
   }
}