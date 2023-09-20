package net.minecraft.entity.projectile;

import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LlamaSpitEntity extends ProjectileEntity {
   public LlamaSpitEntity(EntityType<? extends LlamaSpitEntity> p_i50162_1_, World p_i50162_2_) {
      super(p_i50162_1_, p_i50162_2_);
   }

   public LlamaSpitEntity(World pLevel, LlamaEntity pSpitter) {
      this(EntityType.LLAMA_SPIT, pLevel);
      super.setOwner(pSpitter);
      this.setPos(pSpitter.getX() - (double)(pSpitter.getBbWidth() + 1.0F) * 0.5D * (double)MathHelper.sin(pSpitter.yBodyRot * ((float)Math.PI / 180F)), pSpitter.getEyeY() - (double)0.1F, pSpitter.getZ() + (double)(pSpitter.getBbWidth() + 1.0F) * 0.5D * (double)MathHelper.cos(pSpitter.yBodyRot * ((float)Math.PI / 180F)));
   }

   @OnlyIn(Dist.CLIENT)
   public LlamaSpitEntity(World pLevel, double pX, double pY, double pZ, double pSpeedX, double pSpeedY, double pSpeedZ) {
      this(EntityType.LLAMA_SPIT, pLevel);
      this.setPos(pX, pY, pZ);

      for(int i = 0; i < 7; ++i) {
         double d0 = 0.4D + 0.1D * (double)i;
         pLevel.addParticle(ParticleTypes.SPIT, pX, pY, pZ, pSpeedX * d0, pSpeedY, pSpeedZ * d0);
      }

      this.setDeltaMovement(pSpeedX, pSpeedY, pSpeedZ);
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      Vector3d vector3d = this.getDeltaMovement();
      RayTraceResult raytraceresult = ProjectileHelper.getHitResult(this, this::canHitEntity);
      if (raytraceresult != null && raytraceresult.getType() != RayTraceResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
         this.onHit(raytraceresult);
      }

      double d0 = this.getX() + vector3d.x;
      double d1 = this.getY() + vector3d.y;
      double d2 = this.getZ() + vector3d.z;
      this.updateRotation();
      float f = 0.99F;
      float f1 = 0.06F;
      if (this.level.getBlockStates(this.getBoundingBox()).noneMatch(AbstractBlock.AbstractBlockState::isAir)) {
         this.remove();
      } else if (this.isInWaterOrBubble()) {
         this.remove();
      } else {
         this.setDeltaMovement(vector3d.scale((double)0.99F));
         if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, (double)-0.06F, 0.0D));
         }

         this.setPos(d0, d1, d2);
      }
   }

   /**
    * Called when the arrow hits an entity
    */
   protected void onHitEntity(EntityRayTraceResult pResult) {
      super.onHitEntity(pResult);
      Entity entity = this.getOwner();
      if (entity instanceof LivingEntity) {
         pResult.getEntity().hurt(DamageSource.indirectMobAttack(this, (LivingEntity)entity).setProjectile(), 1.0F);
      }

   }

   protected void onHitBlock(BlockRayTraceResult pResult) {
      super.onHitBlock(pResult);
      if (!this.level.isClientSide) {
         this.remove();
      }

   }

   protected void defineSynchedData() {
   }

   public IPacket<?> getAddEntityPacket() {
      return new SSpawnObjectPacket(this);
   }
}
