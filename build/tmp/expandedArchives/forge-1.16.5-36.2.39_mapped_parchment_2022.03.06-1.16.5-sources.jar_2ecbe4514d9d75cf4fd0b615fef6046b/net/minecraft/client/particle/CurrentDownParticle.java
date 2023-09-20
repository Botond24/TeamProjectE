package net.minecraft.client.particle;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CurrentDownParticle extends SpriteTexturedParticle {
   private float angle;

   private CurrentDownParticle(ClientWorld p_i232456_1_, double p_i232456_2_, double p_i232456_4_, double p_i232456_6_) {
      super(p_i232456_1_, p_i232456_2_, p_i232456_4_, p_i232456_6_);
      this.lifetime = (int)(Math.random() * 60.0D) + 30;
      this.hasPhysics = false;
      this.xd = 0.0D;
      this.yd = -0.05D;
      this.zd = 0.0D;
      this.setSize(0.02F, 0.02F);
      this.quadSize *= this.random.nextFloat() * 0.6F + 0.2F;
      this.gravity = 0.002F;
   }

   public IParticleRenderType getRenderType() {
      return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      if (this.age++ >= this.lifetime) {
         this.remove();
      } else {
         float f = 0.6F;
         this.xd += (double)(0.6F * MathHelper.cos(this.angle));
         this.zd += (double)(0.6F * MathHelper.sin(this.angle));
         this.xd *= 0.07D;
         this.zd *= 0.07D;
         this.move(this.xd, this.yd, this.zd);
         if (!this.level.getFluidState(new BlockPos(this.x, this.y, this.z)).is(FluidTags.WATER) || this.onGround) {
            this.remove();
         }

         this.angle = (float)((double)this.angle + 0.08D);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class Factory implements IParticleFactory<BasicParticleType> {
      private final IAnimatedSprite sprite;

      public Factory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         CurrentDownParticle currentdownparticle = new CurrentDownParticle(pLevel, pX, pY, pZ);
         currentdownparticle.pickSprite(this.sprite);
         return currentdownparticle;
      }
   }
}