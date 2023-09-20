package net.minecraft.client.particle;

import net.minecraft.client.world.ClientWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SimpleAnimatedParticle extends SpriteTexturedParticle {
   protected final IAnimatedSprite sprites;
   private final float baseGravity;
   private float baseAirFriction = 0.91F;
   private float fadeR;
   private float fadeG;
   private float fadeB;
   private boolean hasFade;

   protected SimpleAnimatedParticle(ClientWorld pLevel, double pX, double pY, double pZ, IAnimatedSprite pSprites, float pGravity) {
      super(pLevel, pX, pY, pZ);
      this.sprites = pSprites;
      this.baseGravity = pGravity;
   }

   public void setColor(int pColor) {
      float f = (float)((pColor & 16711680) >> 16) / 255.0F;
      float f1 = (float)((pColor & '\uff00') >> 8) / 255.0F;
      float f2 = (float)((pColor & 255) >> 0) / 255.0F;
      float f3 = 1.0F;
      this.setColor(f * 1.0F, f1 * 1.0F, f2 * 1.0F);
   }

   /**
    * sets a color for the particle to drift toward (20% closer each tick, never actually getting very close)
    */
   public void setFadeColor(int pRgb) {
      this.fadeR = (float)((pRgb & 16711680) >> 16) / 255.0F;
      this.fadeG = (float)((pRgb & '\uff00') >> 8) / 255.0F;
      this.fadeB = (float)((pRgb & 255) >> 0) / 255.0F;
      this.hasFade = true;
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
         if (this.age > this.lifetime / 2) {
            this.setAlpha(1.0F - ((float)this.age - (float)(this.lifetime / 2)) / (float)this.lifetime);
            if (this.hasFade) {
               this.rCol += (this.fadeR - this.rCol) * 0.2F;
               this.gCol += (this.fadeG - this.gCol) * 0.2F;
               this.bCol += (this.fadeB - this.bCol) * 0.2F;
            }
         }

         this.yd += (double)this.baseGravity;
         this.move(this.xd, this.yd, this.zd);
         this.xd *= (double)this.baseAirFriction;
         this.yd *= (double)this.baseAirFriction;
         this.zd *= (double)this.baseAirFriction;
         if (this.onGround) {
            this.xd *= (double)0.7F;
            this.zd *= (double)0.7F;
         }

      }
   }

   public int getLightColor(float pPartialTick) {
      return 15728880;
   }

   protected void setBaseAirFriction(float p_191238_1_) {
      this.baseAirFriction = p_191238_1_;
   }
}