package net.minecraft.entity.item;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.EndermiteEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EnderPearlEntity extends ProjectileItemEntity {
   public EnderPearlEntity(EntityType<? extends EnderPearlEntity> p_i50153_1_, World p_i50153_2_) {
      super(p_i50153_1_, p_i50153_2_);
   }

   public EnderPearlEntity(World pLevel, LivingEntity pShooter) {
      super(EntityType.ENDER_PEARL, pShooter, pLevel);
   }

   @OnlyIn(Dist.CLIENT)
   public EnderPearlEntity(World pLevel, double pX, double pY, double pZ) {
      super(EntityType.ENDER_PEARL, pX, pY, pZ, pLevel);
   }

   protected Item getDefaultItem() {
      return Items.ENDER_PEARL;
   }

   /**
    * Called when the arrow hits an entity
    */
   protected void onHitEntity(EntityRayTraceResult pResult) {
      super.onHitEntity(pResult);
      pResult.getEntity().hurt(DamageSource.thrown(this, this.getOwner()), 0.0F);
   }

   /**
    * Called when this EntityFireball hits a block or entity.
    */
   protected void onHit(RayTraceResult pResult) {
      super.onHit(pResult);
      Entity entity = this.getOwner();

      for(int i = 0; i < 32; ++i) {
         this.level.addParticle(ParticleTypes.PORTAL, this.getX(), this.getY() + this.random.nextDouble() * 2.0D, this.getZ(), this.random.nextGaussian(), 0.0D, this.random.nextGaussian());
      }

      if (!this.level.isClientSide && !this.removed) {
         if (entity instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverplayerentity = (ServerPlayerEntity)entity;
            if (serverplayerentity.connection.getConnection().isConnected() && serverplayerentity.level == this.level && !serverplayerentity.isSleeping()) {
               net.minecraftforge.event.entity.living.EntityTeleportEvent.EnderPearl event = net.minecraftforge.event.ForgeEventFactory.onEnderPearlLand(serverplayerentity, this.getX(), this.getY(), this.getZ(), this, 5.0F);
               if (!event.isCanceled()) { // Don't indent to lower patch size
               if (this.random.nextFloat() < 0.05F && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
                  EndermiteEntity endermiteentity = EntityType.ENDERMITE.create(this.level);
                  endermiteentity.setPlayerSpawned(true);
                  endermiteentity.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.yRot, entity.xRot);
                  this.level.addFreshEntity(endermiteentity);
               }

               if (entity.isPassenger()) {
                  entity.stopRiding();
               }

               entity.teleportTo(event.getTargetX(), event.getTargetY(), event.getTargetZ());
               entity.fallDistance = 0.0F;
               entity.hurt(DamageSource.FALL, event.getAttackDamage());
               } //Forge: End
            }
         } else if (entity != null) {
            entity.teleportTo(this.getX(), this.getY(), this.getZ());
            entity.fallDistance = 0.0F;
         }

         this.remove();
      }

   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      Entity entity = this.getOwner();
      if (entity instanceof PlayerEntity && !entity.isAlive()) {
         this.remove();
      } else {
         super.tick();
      }

   }

   @Nullable
   public Entity changeDimension(ServerWorld pServer, net.minecraftforge.common.util.ITeleporter teleporter) {
      Entity entity = this.getOwner();
      if (entity != null && entity.level.dimension() != pServer.dimension()) {
         this.setOwner((Entity)null);
      }

      return super.changeDimension(pServer, teleporter);
   }
}
