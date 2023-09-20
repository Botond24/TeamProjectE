package net.minecraft.entity.item;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnPaintingPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PaintingEntity extends HangingEntity {
   public PaintingType motive;

   public PaintingEntity(EntityType<? extends PaintingEntity> p_i50221_1_, World p_i50221_2_) {
      super(p_i50221_1_, p_i50221_2_);
   }

   public PaintingEntity(World pLevel, BlockPos pPos, Direction pFacingDirection) {
      super(EntityType.PAINTING, pLevel, pPos);
      List<PaintingType> list = Lists.newArrayList();
      int i = 0;

      for(PaintingType paintingtype : Registry.MOTIVE) {
         this.motive = paintingtype;
         this.setDirection(pFacingDirection);
         if (this.survives()) {
            list.add(paintingtype);
            int j = paintingtype.getWidth() * paintingtype.getHeight();
            if (j > i) {
               i = j;
            }
         }
      }

      if (!list.isEmpty()) {
         Iterator<PaintingType> iterator = list.iterator();

         while(iterator.hasNext()) {
            PaintingType paintingtype1 = iterator.next();
            if (paintingtype1.getWidth() * paintingtype1.getHeight() < i) {
               iterator.remove();
            }
         }

         this.motive = list.get(this.random.nextInt(list.size()));
      }

      this.setDirection(pFacingDirection);
   }

   @OnlyIn(Dist.CLIENT)
   public PaintingEntity(World pLevel, BlockPos pPos, Direction pFacingDirection, PaintingType pMotive) {
      this(pLevel, pPos, pFacingDirection);
      this.motive = pMotive;
      this.setDirection(pFacingDirection);
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      pCompound.putString("Motive", Registry.MOTIVE.getKey(this.motive).toString());
      pCompound.putByte("Facing", (byte)this.direction.get2DDataValue());
      super.addAdditionalSaveData(pCompound);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      this.motive = Registry.MOTIVE.get(ResourceLocation.tryParse(pCompound.getString("Motive")));
      this.direction = Direction.from2DDataValue(pCompound.getByte("Facing"));
      super.readAdditionalSaveData(pCompound);
      this.setDirection(this.direction);
   }

   public int getWidth() {
      return this.motive == null ? 1 : this.motive.getWidth();
   }

   public int getHeight() {
      return this.motive == null ? 1 : this.motive.getHeight();
   }

   /**
    * Called when this entity is broken. Entity parameter may be null.
    */
   public void dropItem(@Nullable Entity pBrokenEntity) {
      if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
         this.playSound(SoundEvents.PAINTING_BREAK, 1.0F, 1.0F);
         if (pBrokenEntity instanceof PlayerEntity) {
            PlayerEntity playerentity = (PlayerEntity)pBrokenEntity;
            if (playerentity.abilities.instabuild) {
               return;
            }
         }

         this.spawnAtLocation(Items.PAINTING);
      }
   }

   public void playPlacementSound() {
      this.playSound(SoundEvents.PAINTING_PLACE, 1.0F, 1.0F);
   }

   /**
    * Sets the location and rotation of the entity in the world.
    */
   public void moveTo(double pX, double pY, double pZ, float pYRot, float pXRot) {
      this.setPos(pX, pY, pZ);
   }

   /**
    * Sets a target for the client to interpolate towards over the next few ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void lerpTo(double pX, double pY, double pZ, float pYRot, float pXRot, int pLerpSteps, boolean pTeleport) {
      BlockPos blockpos = this.pos.offset(pX - this.getX(), pY - this.getY(), pZ - this.getZ());
      this.setPos((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ());
   }

   public IPacket<?> getAddEntityPacket() {
      return new SSpawnPaintingPacket(this);
   }
}