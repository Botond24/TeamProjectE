package net.minecraft.client.particle;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ReversePortalParticle extends PortalParticle {
   private ReversePortalParticle(ClientWorld p_i232418_1_, double p_i232418_2_, double p_i232418_4_, double p_i232418_6_, double p_i232418_8_, double p_i232418_10_, double p_i232418_12_) {
      super(p_i232418_1_, p_i232418_2_, p_i232418_4_, p_i232418_6_, p_i232418_8_, p_i232418_10_, p_i232418_12_);
      this.quadSize = (float)((double)this.quadSize * 1.5D);
      this.lifetime = (int)(Math.random() * 2.0D) + 60;
   }

   public float getQuadSize(float pScaleFactor) {
      float f = 1.0F - ((float)this.age + pScaleFactor) / ((float)this.lifetime * 1.5F);
      return this.quadSize * f;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         float f = (float)this.age / (float)this.lifetime;
         this.x += this.xd * (double)f;
         this.y += this.yd * (double)f;
         this.z += this.zd * (double)f;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class Factory implements IParticleFactory<BasicParticleType> {
      private final IAnimatedSprite sprite;

      public Factory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         ReversePortalParticle reverseportalparticle = new ReversePortalParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
         reverseportalparticle.pickSprite(this.sprite);
         return reverseportalparticle;
      }
   }
}