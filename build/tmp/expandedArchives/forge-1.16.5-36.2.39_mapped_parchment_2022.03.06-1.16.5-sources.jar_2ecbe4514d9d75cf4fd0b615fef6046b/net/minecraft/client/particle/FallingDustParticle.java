package net.minecraft.client.particle;

import javax.annotation.Nullable;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FallingDustParticle extends SpriteTexturedParticle {
   private final float rotSpeed;
   private final IAnimatedSprite sprites;

   private FallingDustParticle(ClientWorld pLevel, double pX, double pY, double pZ, float pXSpeed, float pYSpeed, float pZSpeed, IAnimatedSprite pSprites) {
      super(pLevel, pX, pY, pZ);
      this.sprites = pSprites;
      this.rCol = pXSpeed;
      this.gCol = pYSpeed;
      this.bCol = pZSpeed;
      float f = 0.9F;
      this.quadSize *= 0.67499995F;
      int i = (int)(32.0D / (Math.random() * 0.8D + 0.2D));
      this.lifetime = (int)Math.max((float)i * 0.9F, 1.0F);
      this.setSpriteFromAge(pSprites);
      this.rotSpeed = ((float)Math.random() - 0.5F) * 0.1F;
      this.roll = (float)Math.random() * ((float)Math.PI * 2F);
   }

   public IParticleRenderType getRenderType() {
      return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
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
         this.setSpriteFromAge(this.sprites);
         this.oRoll = this.roll;
         this.roll += (float)Math.PI * this.rotSpeed * 2.0F;
         if (this.onGround) {
            this.oRoll = this.roll = 0.0F;
         }

         this.move(this.xd, this.yd, this.zd);
         this.yd -= (double)0.003F;
         this.yd = Math.max(this.yd, (double)-0.14F);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class Factory implements IParticleFactory<BlockParticleData> {
      private final IAnimatedSprite sprite;

      public Factory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      @Nullable
      public Particle createParticle(BlockParticleData pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         BlockState blockstate = pType.getState();
         if (!blockstate.isAir() && blockstate.getRenderShape() == BlockRenderType.INVISIBLE) {
            return null;
         } else {
            BlockPos blockpos = new BlockPos(pX, pY, pZ);
            int i = Minecraft.getInstance().getBlockColors().getColor(blockstate, pLevel, blockpos);
            if (blockstate.getBlock() instanceof FallingBlock) {
               i = ((FallingBlock)blockstate.getBlock()).getDustColor(blockstate, pLevel, blockpos);
            }

            float f = (float)(i >> 16 & 255) / 255.0F;
            float f1 = (float)(i >> 8 & 255) / 255.0F;
            float f2 = (float)(i & 255) / 255.0F;
            return new FallingDustParticle(pLevel, pX, pY, pZ, f, f1, f2, this.sprite);
         }
      }
   }
}