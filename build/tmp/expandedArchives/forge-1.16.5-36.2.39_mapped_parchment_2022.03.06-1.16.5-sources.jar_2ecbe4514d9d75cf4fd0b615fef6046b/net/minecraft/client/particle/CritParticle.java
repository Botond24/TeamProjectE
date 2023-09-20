package net.minecraft.client.particle;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CritParticle extends SpriteTexturedParticle {
   private CritParticle(ClientWorld p_i232357_1_, double p_i232357_2_, double p_i232357_4_, double p_i232357_6_, double p_i232357_8_, double p_i232357_10_, double p_i232357_12_) {
      super(p_i232357_1_, p_i232357_2_, p_i232357_4_, p_i232357_6_, 0.0D, 0.0D, 0.0D);
      this.xd *= (double)0.1F;
      this.yd *= (double)0.1F;
      this.zd *= (double)0.1F;
      this.xd += p_i232357_8_ * 0.4D;
      this.yd += p_i232357_10_ * 0.4D;
      this.zd += p_i232357_12_ * 0.4D;
      float f = (float)(Math.random() * (double)0.3F + (double)0.6F);
      this.rCol = f;
      this.gCol = f;
      this.bCol = f;
      this.quadSize *= 0.75F;
      this.lifetime = Math.max((int)(6.0D / (Math.random() * 0.8D + 0.6D)), 1);
      this.hasPhysics = false;
      this.tick();
   }

   public float getQuadSize(float pScaleFactor) {
      return this.quadSize * MathHelper.clamp(((float)this.age + pScaleFactor) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         this.move(this.xd, this.yd, this.zd);
         this.gCol = (float)((double)this.gCol * 0.96D);
         this.bCol = (float)((double)this.bCol * 0.9D);
         this.xd *= (double)0.7F;
         this.yd *= (double)0.7F;
         this.zd *= (double)0.7F;
         this.yd -= (double)0.02F;
         if (this.onGround) {
            this.xd *= (double)0.7F;
            this.zd *= (double)0.7F;
         }

      }
   }

   public IParticleRenderType getRenderType() {
      return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   @OnlyIn(Dist.CLIENT)
   public static class DamageIndicatorFactory implements IParticleFactory<BasicParticleType> {
      private final IAnimatedSprite sprite;

      public DamageIndicatorFactory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         CritParticle critparticle = new CritParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed + 1.0D, pZSpeed);
         critparticle.setLifetime(20);
         critparticle.pickSprite(this.sprite);
         return critparticle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class Factory implements IParticleFactory<BasicParticleType> {
      private final IAnimatedSprite sprite;

      public Factory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         CritParticle critparticle = new CritParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
         critparticle.pickSprite(this.sprite);
         return critparticle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class MagicFactory implements IParticleFactory<BasicParticleType> {
      private final IAnimatedSprite sprite;

      public MagicFactory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         CritParticle critparticle = new CritParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
         critparticle.rCol *= 0.3F;
         critparticle.gCol *= 0.8F;
         critparticle.pickSprite(this.sprite);
         return critparticle;
      }
   }
}