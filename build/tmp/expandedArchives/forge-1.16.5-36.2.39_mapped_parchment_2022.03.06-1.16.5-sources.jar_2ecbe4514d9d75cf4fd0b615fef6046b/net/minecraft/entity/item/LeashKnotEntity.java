package net.minecraft.entity.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class LeashKnotEntity extends HangingEntity {
   public LeashKnotEntity(EntityType<? extends LeashKnotEntity> p_i50223_1_, World p_i50223_2_) {
      super(p_i50223_1_, p_i50223_2_);
   }

   public LeashKnotEntity(World pLevel, BlockPos pPos) {
      super(EntityType.LEASH_KNOT, pLevel, pPos);
      this.setPos((double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D);
      float f = 0.125F;
      float f1 = 0.1875F;
      float f2 = 0.25F;
      this.setBoundingBox(new AxisAlignedBB(this.getX() - 0.1875D, this.getY() - 0.25D + 0.125D, this.getZ() - 0.1875D, this.getX() + 0.1875D, this.getY() + 0.25D + 0.125D, this.getZ() + 0.1875D));
      this.forcedLoading = true;
   }

   /**
    * Sets the x,y,z of the entity from the given parameters. Also seems to set up a bounding box.
    */
   public void setPos(double pX, double pY, double pZ) {
      super.setPos((double)MathHelper.floor(pX) + 0.5D, (double)MathHelper.floor(pY) + 0.5D, (double)MathHelper.floor(pZ) + 0.5D);
   }

   /**
    * Updates the entity bounding box based on current facing
    */
   protected void recalculateBoundingBox() {
      this.setPosRaw((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D);
      if (this.isAddedToWorld() && this.level instanceof net.minecraft.world.server.ServerWorld) ((net.minecraft.world.server.ServerWorld)this.level).updateChunkPos(this); // Forge - Process chunk registration after moving.
   }

   /**
    * Updates facing and bounding box based on it
    */
   public void setDirection(Direction pFacingDirection) {
   }

   public int getWidth() {
      return 9;
   }

   public int getHeight() {
      return 9;
   }

   protected float getEyeHeight(Pose pPose, EntitySize pSize) {
      return -0.0625F;
   }

   /**
    * Checks if the entity is in range to render.
    */
   @OnlyIn(Dist.CLIENT)
   public boolean shouldRenderAtSqrDistance(double pDistance) {
      return pDistance < 1024.0D;
   }

   /**
    * Called when this entity is broken. Entity parameter may be null.
    */
   public void dropItem(@Nullable Entity pBrokenEntity) {
      this.playSound(SoundEvents.LEASH_KNOT_BREAK, 1.0F, 1.0F);
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
   }

   public ActionResultType interact(PlayerEntity pPlayer, Hand pHand) {
      if (this.level.isClientSide) {
         return ActionResultType.SUCCESS;
      } else {
         boolean flag = false;
         double d0 = 7.0D;
         List<MobEntity> list = this.level.getEntitiesOfClass(MobEntity.class, new AxisAlignedBB(this.getX() - 7.0D, this.getY() - 7.0D, this.getZ() - 7.0D, this.getX() + 7.0D, this.getY() + 7.0D, this.getZ() + 7.0D));

         for(MobEntity mobentity : list) {
            if (mobentity.getLeashHolder() == pPlayer) {
               mobentity.setLeashedTo(this, true);
               flag = true;
            }
         }

         if (!flag) {
            this.remove();
            if (pPlayer.abilities.instabuild) {
               for(MobEntity mobentity1 : list) {
                  if (mobentity1.isLeashed() && mobentity1.getLeashHolder() == this) {
                     mobentity1.dropLeash(true, false);
                  }
               }
            }
         }

         return ActionResultType.CONSUME;
      }
   }

   /**
    * checks to make sure painting can be placed there
    */
   public boolean survives() {
      return this.level.getBlockState(this.pos).getBlock().is(BlockTags.FENCES);
   }

   public static LeashKnotEntity getOrCreateKnot(World pLevel, BlockPos pPos) {
      int i = pPos.getX();
      int j = pPos.getY();
      int k = pPos.getZ();

      for(LeashKnotEntity leashknotentity : pLevel.getEntitiesOfClass(LeashKnotEntity.class, new AxisAlignedBB((double)i - 1.0D, (double)j - 1.0D, (double)k - 1.0D, (double)i + 1.0D, (double)j + 1.0D, (double)k + 1.0D))) {
         if (leashknotentity.getPos().equals(pPos)) {
            return leashknotentity;
         }
      }

      LeashKnotEntity leashknotentity1 = new LeashKnotEntity(pLevel, pPos);
      pLevel.addFreshEntity(leashknotentity1);
      leashknotentity1.playPlacementSound();
      return leashknotentity1;
   }

   public void playPlacementSound() {
      this.playSound(SoundEvents.LEASH_KNOT_PLACE, 1.0F, 1.0F);
   }

   public IPacket<?> getAddEntityPacket() {
      return new SSpawnObjectPacket(this, this.getType(), 0, this.getPos());
   }

   @OnlyIn(Dist.CLIENT)
   public Vector3d getRopeHoldPosition(float pPartialTicks) {
      return this.getPosition(pPartialTicks).add(0.0D, 0.2D, 0.0D);
   }
}
