package net.minecraft.client.particle;

import java.util.Random;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpellParticle extends SpriteTexturedParticle {
   private static final Random RANDOM = new Random();
   private final IAnimatedSprite sprites;

   private SpellParticle(ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, IAnimatedSprite pSprites) {
      super(pLevel, pX, pY, pZ, 0.5D - RANDOM.nextDouble(), pYSpeed, 0.5D - RANDOM.nextDouble());
      this.sprites = pSprites;
      this.yd *= (double)0.2F;
      if (pXSpeed == 0.0D && pZSpeed == 0.0D) {
         this.xd *= (double)0.1F;
         this.zd *= (double)0.1F;
      }

      this.quadSize *= 0.75F;
      this.lifetime = (int)(8.0D / (Math.random() * 0.8D + 0.2D));
      this.hasPhysics = false;
      this.setSpriteFromAge(pSprites);
   }

   public IParticleRenderType getRenderType() {
      return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         this.setSpriteFromAge(this.sprites);
         this.yd += 0.004D;
         this.move(this.xd, this.yd, this.zd);
         if (this.y == this.yo) {
            this.xd *= 1.1D;
            this.zd *= 1.1D;
         }

         this.xd *= (double)0.96F;
         this.yd *= (double)0.96F;
         this.zd *= (double)0.96F;
         if (this.onGround) {
            this.xd *= (double)0.7F;
            this.zd *= (double)0.7F;
         }

      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class AmbientMobFactory implements IParticleFactory<BasicParticleType> {
      private final IAnimatedSprite sprite;

      public AmbientMobFactory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         Particle particle = new SpellParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, this.sprite);
         particle.setAlpha(0.15F);
         particle.setColor((float)pXSpeed, (float)pYSpeed, (float)pZSpeed);
         return particle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class Factory implements IParticleFactory<BasicParticleType> {
      private final IAnimatedSprite sprite;

      public Factory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         return new SpellParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, this.sprite);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class InstantFactory implements IParticleFactory<BasicParticleType> {
      private final IAnimatedSprite sprite;

      public InstantFactory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         return new SpellParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, this.sprite);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class MobFactory implements IParticleFactory<BasicParticleType> {
      private final IAnimatedSprite sprite;

      public MobFactory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         Particle particle = new SpellParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, this.sprite);
         particle.setColor((float)pXSpeed, (float)pYSpeed, (float)pZSpeed);
         return particle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class WitchFactory implements IParticleFactory<BasicParticleType> {
      private final IAnimatedSprite sprite;

      public WitchFactory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         SpellParticle spellparticle = new SpellParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, this.sprite);
         float f = pLevel.random.nextFloat() * 0.5F + 0.35F;
         spellparticle.setColor(1.0F * f, 0.0F * f, 1.0F * f);
         return spellparticle;
      }
   }
}