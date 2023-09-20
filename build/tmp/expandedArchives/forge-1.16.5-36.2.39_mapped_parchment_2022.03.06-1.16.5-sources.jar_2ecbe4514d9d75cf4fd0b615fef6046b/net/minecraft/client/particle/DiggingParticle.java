package net.minecraft.client.particle;

import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DiggingParticle extends SpriteTexturedParticle {
   private final BlockState blockState;
   private BlockPos pos;
   private final float uo;
   private final float vo;

   public DiggingParticle(ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, BlockState pState) {
      super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
      this.blockState = pState;
      this.setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(pState));
      this.gravity = 1.0F;
      this.rCol = 0.6F;
      this.gCol = 0.6F;
      this.bCol = 0.6F;
      this.quadSize /= 2.0F;
      this.uo = this.random.nextFloat() * 3.0F;
      this.vo = this.random.nextFloat() * 3.0F;
   }

   public IParticleRenderType getRenderType() {
      return IParticleRenderType.TERRAIN_SHEET;
   }

   public DiggingParticle init(BlockPos p_174846_1_) {
      updateSprite(p_174846_1_);
      this.pos = p_174846_1_;
      if (this.blockState.is(Blocks.GRASS_BLOCK)) {
         return this;
      } else {
         this.multiplyColor(p_174846_1_);
         return this;
      }
   }

   public DiggingParticle init() {
      this.pos = new BlockPos(this.x, this.y, this.z);
      if (this.blockState.is(Blocks.GRASS_BLOCK)) {
         return this;
      } else {
         this.multiplyColor(this.pos);
         return this;
      }
   }

   protected void multiplyColor(@Nullable BlockPos p_187154_1_) {
      int i = Minecraft.getInstance().getBlockColors().getColor(this.blockState, this.level, p_187154_1_, 0);
      this.rCol *= (float)(i >> 16 & 255) / 255.0F;
      this.gCol *= (float)(i >> 8 & 255) / 255.0F;
      this.bCol *= (float)(i & 255) / 255.0F;
   }

   protected float getU0() {
      return this.sprite.getU((double)((this.uo + 1.0F) / 4.0F * 16.0F));
   }

   protected float getU1() {
      return this.sprite.getU((double)(this.uo / 4.0F * 16.0F));
   }

   protected float getV0() {
      return this.sprite.getV((double)(this.vo / 4.0F * 16.0F));
   }

   protected float getV1() {
      return this.sprite.getV((double)((this.vo + 1.0F) / 4.0F * 16.0F));
   }

   public int getLightColor(float pPartialTick) {
      int i = super.getLightColor(pPartialTick);
      int j = 0;
      if (this.level.hasChunkAt(this.pos)) {
         j = WorldRenderer.getLightColor(this.level, this.pos);
      }

      return i == 0 ? j : i;
   }

   @OnlyIn(Dist.CLIENT)
   public static class Factory implements IParticleFactory<BlockParticleData> {
      public Particle createParticle(BlockParticleData pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         BlockState blockstate = pType.getState();
         return !blockstate.isAir() && !blockstate.is(Blocks.MOVING_PISTON) ? (new DiggingParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, blockstate)).init().updateSprite(pType.getPos()) : null;
      }
   }

   private Particle updateSprite(BlockPos pos) { //FORGE: we cannot assume that the x y z of the particles match the block pos of the block.
      if (pos != null) // There are cases where we are not able to obtain the correct source pos, and need to fallback to the non-model data version
         this.setSprite(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getTexture(blockState, level, pos));
      return this;
   }
}
