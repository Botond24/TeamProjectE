package net.minecraft.client.particle;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RainParticle extends SpriteTexturedParticle {
   protected RainParticle(ClientWorld p_i232458_1_, double p_i232458_2_, double p_i232458_4_, double p_i232458_6_) {
      super(p_i232458_1_, p_i232458_2_, p_i232458_4_, p_i232458_6_, 0.0D, 0.0D, 0.0D);
      this.xd *= (double)0.3F;
      this.yd = Math.random() * (double)0.2F + (double)0.1F;
      this.zd *= (double)0.3F;
      this.setSize(0.01F, 0.01F);
      this.gravity = 0.06F;
      this.lifetime = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
   }

   public IParticleRenderType getRenderType() {
      return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.lifetime-- <= 0) {
         this.remove();
      } else {
         this.yd -= (double)this.gravity;
         this.move(this.xd, this.yd, this.zd);
         this.xd *= (double)0.98F;
         this.yd *= (double)0.98F;
         this.zd *= (double)0.98F;
         if (this.onGround) {
            if (Math.random() < 0.5D) {
               this.remove();
            }

            this.xd *= (double)0.7F;
            this.zd *= (double)0.7F;
         }

         BlockPos blockpos = new BlockPos(this.x, this.y, this.z);
         double d0 = Math.max(this.level.getBlockState(blockpos).getCollisionShape(this.level, blockpos).max(Direction.Axis.Y, this.x - (double)blockpos.getX(), this.z - (double)blockpos.getZ()), (double)this.level.getFluidState(blockpos).getHeight(this.level, blockpos));
         if (d0 > 0.0D && this.y < (double)blockpos.getY() + d0) {
            this.remove();
         }

      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class Factory implements IParticleFactory<BasicParticleType> {
      private final IAnimatedSprite sprite;

      public Factory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         RainParticle rainparticle = new RainParticle(pLevel, pX, pY, pZ);
         rainparticle.pickSprite(this.sprite);
         return rainparticle;
      }
   }
}