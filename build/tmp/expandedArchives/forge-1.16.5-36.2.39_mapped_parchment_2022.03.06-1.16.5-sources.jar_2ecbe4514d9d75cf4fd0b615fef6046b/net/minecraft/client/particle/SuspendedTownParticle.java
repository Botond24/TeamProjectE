package net.minecraft.client.particle;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SuspendedTownParticle extends SpriteTexturedParticle {
   private SuspendedTownParticle(ClientWorld p_i232444_1_, double p_i232444_2_, double p_i232444_4_, double p_i232444_6_, double p_i232444_8_, double p_i232444_10_, double p_i232444_12_) {
      super(p_i232444_1_, p_i232444_2_, p_i232444_4_, p_i232444_6_, p_i232444_8_, p_i232444_10_, p_i232444_12_);
      float f = this.random.nextFloat() * 0.1F + 0.2F;
      this.rCol = f;
      this.gCol = f;
      this.bCol = f;
      this.setSize(0.02F, 0.02F);
      this.quadSize *= this.random.nextFloat() * 0.6F + 0.5F;
      this.xd *= (double)0.02F;
      this.yd *= (double)0.02F;
      this.zd *= (double)0.02F;
      this.lifetime = (int)(20.0D / (Math.random() * 0.8D + 0.2D));
   }

   public IParticleRenderType getRenderType() {
      return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public void move(double pX, double pY, double pZ) {
      this.setBoundingBox(this.getBoundingBox().move(pX, pY, pZ));
      this.setLocationFromBoundingbox();
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.lifetime-- <= 0) {
         this.remove();
      } else {
         this.move(this.xd, this.yd, this.zd);
         this.xd *= 0.99D;
         this.yd *= 0.99D;
         this.zd *= 0.99D;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class ComposterFactory implements IParticleFactory<BasicParticleType> {
      private final IAnimatedSprite sprite;

      public ComposterFactory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         SuspendedTownParticle suspendedtownparticle = new SuspendedTownParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
         suspendedtownparticle.pickSprite(this.sprite);
         suspendedtownparticle.setColor(1.0F, 1.0F, 1.0F);
         suspendedtownparticle.setLifetime(3 + pLevel.getRandom().nextInt(5));
         return suspendedtownparticle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class DolphinSpeedFactory implements IParticleFactory<BasicParticleType> {
      private final IAnimatedSprite sprite;

      public DolphinSpeedFactory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         SuspendedTownParticle suspendedtownparticle = new SuspendedTownParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
         suspendedtownparticle.setColor(0.3F, 0.5F, 1.0F);
         suspendedtownparticle.pickSprite(this.sprite);
         suspendedtownparticle.setAlpha(1.0F - pLevel.random.nextFloat() * 0.7F);
         suspendedtownparticle.setLifetime(suspendedtownparticle.getLifetime() / 2);
         return suspendedtownparticle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class Factory implements IParticleFactory<BasicParticleType> {
      private final IAnimatedSprite sprite;

      public Factory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         SuspendedTownParticle suspendedtownparticle = new SuspendedTownParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
         suspendedtownparticle.pickSprite(this.sprite);
         return suspendedtownparticle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class HappyVillagerFactory implements IParticleFactory<BasicParticleType> {
      private final IAnimatedSprite sprite;

      public HappyVillagerFactory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         SuspendedTownParticle suspendedtownparticle = new SuspendedTownParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
         suspendedtownparticle.pickSprite(this.sprite);
         suspendedtownparticle.setColor(1.0F, 1.0F, 1.0F);
         return suspendedtownparticle;
      }
   }
}