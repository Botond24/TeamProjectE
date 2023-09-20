package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.DyeColor;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FireworkParticle {
   @OnlyIn(Dist.CLIENT)
   public static class Overlay extends SpriteTexturedParticle {
      private Overlay(ClientWorld p_i232387_1_, double p_i232387_2_, double p_i232387_4_, double p_i232387_6_) {
         super(p_i232387_1_, p_i232387_2_, p_i232387_4_, p_i232387_6_);
         this.lifetime = 4;
      }

      public IParticleRenderType getRenderType() {
         return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
      }

      public void render(IVertexBuilder pBuffer, ActiveRenderInfo pRenderInfo, float pPartialTicks) {
         this.setAlpha(0.6F - ((float)this.age + pPartialTicks - 1.0F) * 0.25F * 0.5F);
         super.render(pBuffer, pRenderInfo, pPartialTicks);
      }

      public float getQuadSize(float pScaleFactor) {
         return 7.1F * MathHelper.sin(((float)this.age + pScaleFactor - 1.0F) * 0.25F * (float)Math.PI);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class OverlayFactory implements IParticleFactory<BasicParticleType> {
      private final IAnimatedSprite sprite;

      public OverlayFactory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         FireworkParticle.Overlay fireworkparticle$overlay = new FireworkParticle.Overlay(pLevel, pX, pY, pZ);
         fireworkparticle$overlay.pickSprite(this.sprite);
         return fireworkparticle$overlay;
      }
   }

   @OnlyIn(Dist.CLIENT)
   static class Spark extends SimpleAnimatedParticle {
      private boolean trail;
      private boolean flicker;
      private final ParticleManager engine;
      private float fadeR;
      private float fadeG;
      private float fadeB;
      private boolean hasFade;

      private Spark(ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, ParticleManager pEngine, IAnimatedSprite pSprites) {
         super(pLevel, pX, pY, pZ, pSprites, -0.004F);
         this.xd = pXSpeed;
         this.yd = pYSpeed;
         this.zd = pZSpeed;
         this.engine = pEngine;
         this.quadSize *= 0.75F;
         this.lifetime = 48 + this.random.nextInt(12);
         this.setSpriteFromAge(pSprites);
      }

      public void setTrail(boolean pTrail) {
         this.trail = pTrail;
      }

      public void setFlicker(boolean pTwinkle) {
         this.flicker = pTwinkle;
      }

      public void render(IVertexBuilder pBuffer, ActiveRenderInfo pRenderInfo, float pPartialTicks) {
         if (!this.flicker || this.age < this.lifetime / 3 || (this.age + this.lifetime) / 3 % 2 == 0) {
            super.render(pBuffer, pRenderInfo, pPartialTicks);
         }

      }

      public void tick() {
         super.tick();
         if (this.trail && this.age < this.lifetime / 2 && (this.age + this.lifetime) % 2 == 0) {
            FireworkParticle.Spark fireworkparticle$spark = new FireworkParticle.Spark(this.level, this.x, this.y, this.z, 0.0D, 0.0D, 0.0D, this.engine, this.sprites);
            fireworkparticle$spark.setAlpha(0.99F);
            fireworkparticle$spark.setColor(this.rCol, this.gCol, this.bCol);
            fireworkparticle$spark.age = fireworkparticle$spark.lifetime / 2;
            if (this.hasFade) {
               fireworkparticle$spark.hasFade = true;
               fireworkparticle$spark.fadeR = this.fadeR;
               fireworkparticle$spark.fadeG = this.fadeG;
               fireworkparticle$spark.fadeB = this.fadeB;
            }

            fireworkparticle$spark.flicker = this.flicker;
            this.engine.add(fireworkparticle$spark);
         }

      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class SparkFactory implements IParticleFactory<BasicParticleType> {
      private final IAnimatedSprite sprites;

      public SparkFactory(IAnimatedSprite pSprites) {
         this.sprites = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         FireworkParticle.Spark fireworkparticle$spark = new FireworkParticle.Spark(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, Minecraft.getInstance().particleEngine, this.sprites);
         fireworkparticle$spark.setAlpha(0.99F);
         return fireworkparticle$spark;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class Starter extends MetaParticle {
      private int life;
      private final ParticleManager engine;
      private ListNBT explosions;
      private boolean twinkleDelay;

      public Starter(ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, ParticleManager pEngine, @Nullable CompoundNBT pTag) {
         super(pLevel, pX, pY, pZ);
         this.xd = pXSpeed;
         this.yd = pYSpeed;
         this.zd = pZSpeed;
         this.engine = pEngine;
         this.lifetime = 8;
         if (pTag != null) {
            this.explosions = pTag.getList("Explosions", 10);
            if (this.explosions.isEmpty()) {
               this.explosions = null;
            } else {
               this.lifetime = this.explosions.size() * 2 - 1;

               for(int i = 0; i < this.explosions.size(); ++i) {
                  CompoundNBT compoundnbt = this.explosions.getCompound(i);
                  if (compoundnbt.getBoolean("Flicker")) {
                     this.twinkleDelay = true;
                     this.lifetime += 15;
                     break;
                  }
               }
            }
         }

      }

      public void tick() {
         if (this.life == 0 && this.explosions != null) {
            boolean flag = this.isFarAwayFromCamera();
            boolean flag1 = false;
            if (this.explosions.size() >= 3) {
               flag1 = true;
            } else {
               for(int i = 0; i < this.explosions.size(); ++i) {
                  CompoundNBT compoundnbt = this.explosions.getCompound(i);
                  if (FireworkRocketItem.Shape.byId(compoundnbt.getByte("Type")) == FireworkRocketItem.Shape.LARGE_BALL) {
                     flag1 = true;
                     break;
                  }
               }
            }

            SoundEvent soundevent1;
            if (flag1) {
               soundevent1 = flag ? SoundEvents.FIREWORK_ROCKET_LARGE_BLAST_FAR : SoundEvents.FIREWORK_ROCKET_LARGE_BLAST;
            } else {
               soundevent1 = flag ? SoundEvents.FIREWORK_ROCKET_BLAST_FAR : SoundEvents.FIREWORK_ROCKET_BLAST;
            }

            this.level.playLocalSound(this.x, this.y, this.z, soundevent1, SoundCategory.AMBIENT, 20.0F, 0.95F + this.random.nextFloat() * 0.1F, true);
         }

         if (this.life % 2 == 0 && this.explosions != null && this.life / 2 < this.explosions.size()) {
            int k = this.life / 2;
            CompoundNBT compoundnbt1 = this.explosions.getCompound(k);
            FireworkRocketItem.Shape fireworkrocketitem$shape = FireworkRocketItem.Shape.byId(compoundnbt1.getByte("Type"));
            boolean flag4 = compoundnbt1.getBoolean("Trail");
            boolean flag2 = compoundnbt1.getBoolean("Flicker");
            int[] aint = compoundnbt1.getIntArray("Colors");
            int[] aint1 = compoundnbt1.getIntArray("FadeColors");
            if (aint.length == 0) {
               aint = new int[]{DyeColor.BLACK.getFireworkColor()};
            }

            switch(fireworkrocketitem$shape) {
            case SMALL_BALL:
            default:
               this.createParticleBall(0.25D, 2, aint, aint1, flag4, flag2);
               break;
            case LARGE_BALL:
               this.createParticleBall(0.5D, 4, aint, aint1, flag4, flag2);
               break;
            case STAR:
               this.createParticleShape(0.5D, new double[][]{{0.0D, 1.0D}, {0.3455D, 0.309D}, {0.9511D, 0.309D}, {0.3795918367346939D, -0.12653061224489795D}, {0.6122448979591837D, -0.8040816326530612D}, {0.0D, -0.35918367346938773D}}, aint, aint1, flag4, flag2, false);
               break;
            case CREEPER:
               this.createParticleShape(0.5D, new double[][]{{0.0D, 0.2D}, {0.2D, 0.2D}, {0.2D, 0.6D}, {0.6D, 0.6D}, {0.6D, 0.2D}, {0.2D, 0.2D}, {0.2D, 0.0D}, {0.4D, 0.0D}, {0.4D, -0.6D}, {0.2D, -0.6D}, {0.2D, -0.4D}, {0.0D, -0.4D}}, aint, aint1, flag4, flag2, true);
               break;
            case BURST:
               this.createParticleBurst(aint, aint1, flag4, flag2);
            }

            int j = aint[0];
            float f = (float)((j & 16711680) >> 16) / 255.0F;
            float f1 = (float)((j & '\uff00') >> 8) / 255.0F;
            float f2 = (float)((j & 255) >> 0) / 255.0F;
            Particle particle = this.engine.createParticle(ParticleTypes.FLASH, this.x, this.y, this.z, 0.0D, 0.0D, 0.0D);
            particle.setColor(f, f1, f2);
         }

         ++this.life;
         if (this.life > this.lifetime) {
            if (this.twinkleDelay) {
               boolean flag3 = this.isFarAwayFromCamera();
               SoundEvent soundevent = flag3 ? SoundEvents.FIREWORK_ROCKET_TWINKLE_FAR : SoundEvents.FIREWORK_ROCKET_TWINKLE;
               this.level.playLocalSound(this.x, this.y, this.z, soundevent, SoundCategory.AMBIENT, 20.0F, 0.9F + this.random.nextFloat() * 0.15F, true);
            }

            this.remove();
         }

      }

      private boolean isFarAwayFromCamera() {
         Minecraft minecraft = Minecraft.getInstance();
         return minecraft.gameRenderer.getMainCamera().getPosition().distanceToSqr(this.x, this.y, this.z) >= 256.0D;
      }

      /**
       * Creates a single particle.
       */
      private void createParticle(double pX, double pY, double pZ, double pMotionX, double pMotionY, double pMotionZ, int[] pSparkColors, int[] pSparkColorFades, boolean pHasTrail, boolean pHasTwinkle) {
         FireworkParticle.Spark fireworkparticle$spark = (FireworkParticle.Spark)this.engine.createParticle(ParticleTypes.FIREWORK, pX, pY, pZ, pMotionX, pMotionY, pMotionZ);
         fireworkparticle$spark.setTrail(pHasTrail);
         fireworkparticle$spark.setFlicker(pHasTwinkle);
         fireworkparticle$spark.setAlpha(0.99F);
         int i = this.random.nextInt(pSparkColors.length);
         fireworkparticle$spark.setColor(pSparkColors[i]);
         if (pSparkColorFades.length > 0) {
            fireworkparticle$spark.setFadeColor(Util.getRandom(pSparkColorFades, this.random));
         }

      }

      /**
       * Creates a small ball or large ball type explosion effect.
       */
      private void createParticleBall(double pSpeed, int pSize, int[] pColours, int[] pFadeColours, boolean pTrail, boolean pTwinkle) {
         double d0 = this.x;
         double d1 = this.y;
         double d2 = this.z;

         for(int i = -pSize; i <= pSize; ++i) {
            for(int j = -pSize; j <= pSize; ++j) {
               for(int k = -pSize; k <= pSize; ++k) {
                  double d3 = (double)j + (this.random.nextDouble() - this.random.nextDouble()) * 0.5D;
                  double d4 = (double)i + (this.random.nextDouble() - this.random.nextDouble()) * 0.5D;
                  double d5 = (double)k + (this.random.nextDouble() - this.random.nextDouble()) * 0.5D;
                  double d6 = (double)MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5) / pSpeed + this.random.nextGaussian() * 0.05D;
                  this.createParticle(d0, d1, d2, d3 / d6, d4 / d6, d5 / d6, pColours, pFadeColours, pTrail, pTwinkle);
                  if (i != -pSize && i != pSize && j != -pSize && j != pSize) {
                     k += pSize * 2 - 1;
                  }
               }
            }
         }

      }

      /**
       * Creates a creeper-shaped or star-shaped explosion.
       */
      private void createParticleShape(double pSpeed, double[][] pShape, int[] pColours, int[] pFadeColours, boolean pTrail, boolean pTwinkle, boolean pCreeper) {
         double d0 = pShape[0][0];
         double d1 = pShape[0][1];
         this.createParticle(this.x, this.y, this.z, d0 * pSpeed, d1 * pSpeed, 0.0D, pColours, pFadeColours, pTrail, pTwinkle);
         float f = this.random.nextFloat() * (float)Math.PI;
         double d2 = pCreeper ? 0.034D : 0.34D;

         for(int i = 0; i < 3; ++i) {
            double d3 = (double)f + (double)((float)i * (float)Math.PI) * d2;
            double d4 = d0;
            double d5 = d1;

            for(int j = 1; j < pShape.length; ++j) {
               double d6 = pShape[j][0];
               double d7 = pShape[j][1];

               for(double d8 = 0.25D; d8 <= 1.0D; d8 += 0.25D) {
                  double d9 = MathHelper.lerp(d8, d4, d6) * pSpeed;
                  double d10 = MathHelper.lerp(d8, d5, d7) * pSpeed;
                  double d11 = d9 * Math.sin(d3);
                  d9 = d9 * Math.cos(d3);

                  for(double d12 = -1.0D; d12 <= 1.0D; d12 += 2.0D) {
                     this.createParticle(this.x, this.y, this.z, d9 * d12, d10, d11 * d12, pColours, pFadeColours, pTrail, pTwinkle);
                  }
               }

               d4 = d6;
               d5 = d7;
            }
         }

      }

      /**
       * Creates a burst type explosion effect.
       */
      private void createParticleBurst(int[] pColours, int[] pFadeColours, boolean pTrail, boolean pTwinkle) {
         double d0 = this.random.nextGaussian() * 0.05D;
         double d1 = this.random.nextGaussian() * 0.05D;

         for(int i = 0; i < 70; ++i) {
            double d2 = this.xd * 0.5D + this.random.nextGaussian() * 0.15D + d0;
            double d3 = this.zd * 0.5D + this.random.nextGaussian() * 0.15D + d1;
            double d4 = this.yd * 0.5D + this.random.nextDouble() * 0.5D;
            this.createParticle(this.x, this.y, this.z, d2, d4, d3, pColours, pFadeColours, pTrail, pTwinkle);
         }

      }
   }
}