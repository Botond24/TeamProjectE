package net.minecraft.entity.boss.dragon;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.Pose;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;

public class EnderDragonPartEntity extends net.minecraftforge.entity.PartEntity<EnderDragonEntity> {
   public final EnderDragonEntity parentMob;
   public final String name;
   private final EntitySize size;

   public EnderDragonPartEntity(EnderDragonEntity pParentMob, String pName, float pWidth, float pHeight) {
      super(pParentMob);
      this.size = EntitySize.scalable(pWidth, pHeight);
      this.refreshDimensions();
      this.parentMob = pParentMob;
      this.name = pName;
   }

   protected void defineSynchedData() {
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   protected void readAdditionalSaveData(CompoundNBT pCompound) {
   }

   protected void addAdditionalSaveData(CompoundNBT pCompound) {
   }

   /**
    * Returns true if other Entities should be prevented from moving through this Entity.
    */
   public boolean isPickable() {
      return true;
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      return this.isInvulnerableTo(pSource) ? false : this.parentMob.hurt(this, pSource, pAmount);
   }

   /**
    * Returns true if Entity argument is equal to this Entity
    */
   public boolean is(Entity pEntity) {
      return this == pEntity || this.parentMob == pEntity;
   }

   public IPacket<?> getAddEntityPacket() {
      throw new UnsupportedOperationException();
   }

   public EntitySize getDimensions(Pose pPose) {
      return this.size;
   }
}
