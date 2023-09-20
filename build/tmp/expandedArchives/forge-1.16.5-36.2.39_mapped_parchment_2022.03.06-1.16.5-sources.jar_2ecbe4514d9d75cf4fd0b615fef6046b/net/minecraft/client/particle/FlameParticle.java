package net.minecraft.client.particle;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FlameParticle extends DeceleratingParticle {
   private FlameParticle(ClientWorld p_i232392_1_, double p_i232392_2_, double p_i232392_4_, double p_i232392_6_, double p_i232392_8_, double p_i232392_10_, double p_i232392_12_) {
      super(p_i232392_1_, p_i232392_2_, p_i232392_4_, p_i232392_6_, p_i232392_8_, p_i232392_10_, p_i232392_12_);
   }

   public IParticleRenderType getRenderType() {
      return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public void move(double pX, double pY, double pZ) {
      this.setBoundingBox(this.getBoundingBox().move(pX, pY, pZ));
      this.setLocationFromBoundingbox();
   }

   public float getQuadSize(float pScaleFactor) {
      float f = ((float)this.age + pScaleFactor) / (float)this.lifetime;
      return this.quadSize * (1.0F - f * f * 0.5F);
   }

   public int getLightColor(float pPartialTick) {
      float f = ((float)this.age + pPartialTick) / (float)this.lifetime;
      f = MathHelper.clamp(f, 0.0F, 1.0F);
      int i = super.getLightColor(pPartialTick);
      int j = i & 255;
      int k = i >> 16 & 255;
      j = j + (int)(f * 15.0F * 16.0F);
      if (j > 240) {
         j = 240;
      }

      return j | k << 16;
   }

   @OnlyIn(Dist.CLIENT)
   public static class Factory implements IParticleFactory<BasicParticleType> {
      private final IAnimatedSprite sprite;

      public Factory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         FlameParticle flameparticle = new FlameParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
         flameparticle.pickSprite(this.sprite);
         return flameparticle;
      }
   }
}