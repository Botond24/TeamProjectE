package net.minecraft.client.particle;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EnchantmentTableParticle extends SpriteTexturedParticle {
   private final double xStart;
   private final double yStart;
   private final double zStart;

   private EnchantmentTableParticle(ClientWorld p_i232380_1_, double p_i232380_2_, double p_i232380_4_, double p_i232380_6_, double p_i232380_8_, double p_i232380_10_, double p_i232380_12_) {
      super(p_i232380_1_, p_i232380_2_, p_i232380_4_, p_i232380_6_);
      this.xd = p_i232380_8_;
      this.yd = p_i232380_10_;
      this.zd = p_i232380_12_;
      this.xStart = p_i232380_2_;
      this.yStart = p_i232380_4_;
      this.zStart = p_i232380_6_;
      this.xo = p_i232380_2_ + p_i232380_8_;
      this.yo = p_i232380_4_ + p_i232380_10_;
      this.zo = p_i232380_6_ + p_i232380_12_;
      this.x = this.xo;
      this.y = this.yo;
      this.z = this.zo;
      this.quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.2F);
      float f = this.random.nextFloat() * 0.6F + 0.4F;
      this.rCol = 0.9F * f;
      this.gCol = 0.9F * f;
      this.bCol = f;
      this.hasPhysics = false;
      this.lifetime = (int)(Math.random() * 10.0D) + 30;
   }

   public IParticleRenderType getRenderType() {
      return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public void move(double pX, double pY, double pZ) {
      this.setBoundingBox(this.getBoundingBox().move(pX, pY, pZ));
      this.setLocationFromBoundingbox();
   }

   public int getLightColor(float pPartialTick) {
      int i = super.getLightColor(pPartialTick);
      float f = (float)this.age / (float)this.lifetime;
      f = f * f;
      f = f * f;
      int j = i & 255;
      int k = i >> 16 & 255;
      k = k + (int)(f * 15.0F * 16.0F);
      if (k > 240) {
         k = 240;
      }

      return j | k << 16;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         float f = (float)this.age / (float)this.lifetime;
         f = 1.0F - f;
         float f1 = 1.0F - f;
         f1 = f1 * f1;
         f1 = f1 * f1;
         this.x = this.xStart + this.xd * (double)f;
         this.y = this.yStart + this.yd * (double)f - (double)(f1 * 1.2F);
         this.z = this.zStart + this.zd * (double)f;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class EnchantmentTable implements IParticleFactory<BasicParticleType> {
      private final IAnimatedSprite sprite;

      public EnchantmentTable(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         EnchantmentTableParticle enchantmenttableparticle = new EnchantmentTableParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
         enchantmenttableparticle.pickSprite(this.sprite);
         return enchantmenttableparticle;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class NautilusFactory implements IParticleFactory<BasicParticleType> {
      private final IAnimatedSprite sprite;

      public NautilusFactory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         EnchantmentTableParticle enchantmenttableparticle = new EnchantmentTableParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
         enchantmenttableparticle.pickSprite(this.sprite);
         return enchantmenttableparticle;
      }
   }
}