package net.minecraft.entity.item.minecart;

import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.HopperContainer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.tileentity.IHopper;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class HopperMinecartEntity extends ContainerMinecartEntity implements IHopper {
   private boolean enabled = true;
   private int cooldownTime = -1;
   private final BlockPos lastPosition = BlockPos.ZERO;

   public HopperMinecartEntity(EntityType<? extends HopperMinecartEntity> p_i50116_1_, World p_i50116_2_) {
      super(p_i50116_1_, p_i50116_2_);
   }

   public HopperMinecartEntity(World pLevel, double pX, double pY, double pZ) {
      super(EntityType.HOPPER_MINECART, pX, pY, pZ, pLevel);
   }

   public AbstractMinecartEntity.Type getMinecartType() {
      return AbstractMinecartEntity.Type.HOPPER;
   }

   public BlockState getDefaultDisplayBlockState() {
      return Blocks.HOPPER.defaultBlockState();
   }

   public int getDefaultDisplayOffset() {
      return 1;
   }

   /**
    * Returns the number of slots in the inventory.
    */
   public int getContainerSize() {
      return 5;
   }

   /**
    * Called every tick the minecart is on an activator rail.
    */
   public void activateMinecart(int pX, int pY, int pZ, boolean pReceivingPower) {
      boolean flag = !pReceivingPower;
      if (flag != this.isEnabled()) {
         this.setEnabled(flag);
      }

   }

   /**
    * Get whether this hopper minecart is being blocked by an activator rail.
    */
   public boolean isEnabled() {
      return this.enabled;
   }

   /**
    * Set whether this hopper minecart is being blocked by an activator rail.
    */
   public void setEnabled(boolean pEnabled) {
      this.enabled = pEnabled;
   }

   public World getLevel() {
      return this.level;
   }

   /**
    * Gets the world X position for this hopper entity.
    */
   public double getLevelX() {
      return this.getX();
   }

   /**
    * Gets the world Y position for this hopper entity.
    */
   public double getLevelY() {
      return this.getY() + 0.5D;
   }

   /**
    * Gets the world Z position for this hopper entity.
    */
   public double getLevelZ() {
      return this.getZ();
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      if (!this.level.isClientSide && this.isAlive() && this.isEnabled()) {
         BlockPos blockpos = this.blockPosition();
         if (blockpos.equals(this.lastPosition)) {
            --this.cooldownTime;
         } else {
            this.setCooldown(0);
         }

         if (!this.isOnCooldown()) {
            this.setCooldown(0);
            if (this.suckInItems()) {
               this.setCooldown(4);
               this.setChanged();
            }
         }
      }

   }

   public boolean suckInItems() {
      if (HopperTileEntity.suckInItems(this)) {
         return true;
      } else {
         List<ItemEntity> list = this.level.getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(0.25D, 0.0D, 0.25D), EntityPredicates.ENTITY_STILL_ALIVE);
         if (!list.isEmpty()) {
            HopperTileEntity.addItem(this, list.get(0));
         }

         return false;
      }
   }

   public void destroy(DamageSource pSource) {
      super.destroy(pSource);
      if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
         this.spawnAtLocation(Blocks.HOPPER);
      }

   }

   protected void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("TransferCooldown", this.cooldownTime);
      pCompound.putBoolean("Enabled", this.enabled);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   protected void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.cooldownTime = pCompound.getInt("TransferCooldown");
      this.enabled = pCompound.contains("Enabled") ? pCompound.getBoolean("Enabled") : true;
   }

   /**
    * Sets the transfer ticker, used to determine the delay between transfers.
    */
   public void setCooldown(int pCooldownTime) {
      this.cooldownTime = pCooldownTime;
   }

   /**
    * Returns whether the hopper cart can currently transfer an item.
    */
   public boolean isOnCooldown() {
      return this.cooldownTime > 0;
   }

   public Container createMenu(int pContainerId, PlayerInventory pPlayerInventory) {
      return new HopperContainer(pContainerId, pPlayerInventory, this);
   }
}