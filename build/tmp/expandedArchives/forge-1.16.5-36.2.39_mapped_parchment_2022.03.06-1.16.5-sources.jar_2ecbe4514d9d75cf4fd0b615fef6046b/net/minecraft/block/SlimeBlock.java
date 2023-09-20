package net.minecraft.block;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class SlimeBlock extends BreakableBlock {
   public SlimeBlock(AbstractBlock.Properties p_i48330_1_) {
      super(p_i48330_1_);
   }

   public void fallOn(World p_180658_1_, BlockPos p_180658_2_, Entity p_180658_3_, float p_180658_4_) {
      if (p_180658_3_.isSuppressingBounce()) {
         super.fallOn(p_180658_1_, p_180658_2_, p_180658_3_, p_180658_4_);
      } else {
         p_180658_3_.causeFallDamage(p_180658_4_, 0.0F);
      }

   }

   /**
    * Called when an Entity lands on this Block. This method *must* update motionY because the entity will not do that
    * on its own
    */
   public void updateEntityAfterFallOn(IBlockReader pLevel, Entity pEntity) {
      if (pEntity.isSuppressingBounce()) {
         super.updateEntityAfterFallOn(pLevel, pEntity);
      } else {
         this.bounceUp(pEntity);
      }

   }

   private void bounceUp(Entity pEntity) {
      Vector3d vector3d = pEntity.getDeltaMovement();
      if (vector3d.y < 0.0D) {
         double d0 = pEntity instanceof LivingEntity ? 1.0D : 0.8D;
         pEntity.setDeltaMovement(vector3d.x, -vector3d.y * d0, vector3d.z);
      }

   }

   public void stepOn(World p_176199_1_, BlockPos p_176199_2_, Entity p_176199_3_) {
      double d0 = Math.abs(p_176199_3_.getDeltaMovement().y);
      if (d0 < 0.1D && !p_176199_3_.isSteppingCarefully()) {
         double d1 = 0.4D + d0 * 0.2D;
         p_176199_3_.setDeltaMovement(p_176199_3_.getDeltaMovement().multiply(d1, 1.0D, d1));
      }

      super.stepOn(p_176199_1_, p_176199_2_, p_176199_3_);
   }
}