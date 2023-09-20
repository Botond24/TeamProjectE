package net.minecraft.entity.item;

import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneDiodeBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ItemFrameEntity extends HangingEntity {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final DataParameter<ItemStack> DATA_ITEM = EntityDataManager.defineId(ItemFrameEntity.class, DataSerializers.ITEM_STACK);
   private static final DataParameter<Integer> DATA_ROTATION = EntityDataManager.defineId(ItemFrameEntity.class, DataSerializers.INT);
   private float dropChance = 1.0F;
   private boolean fixed;

   public ItemFrameEntity(EntityType<? extends ItemFrameEntity> p_i50224_1_, World p_i50224_2_) {
      super(p_i50224_1_, p_i50224_2_);
   }

   public ItemFrameEntity(World pLevel, BlockPos pPos, Direction pFacingDirection) {
      super(EntityType.ITEM_FRAME, pLevel, pPos);
      this.setDirection(pFacingDirection);
   }

   protected float getEyeHeight(Pose pPose, EntitySize pSize) {
      return 0.0F;
   }

   protected void defineSynchedData() {
      this.getEntityData().define(DATA_ITEM, ItemStack.EMPTY);
      this.getEntityData().define(DATA_ROTATION, 0);
   }

   /**
    * Updates facing and bounding box based on it
    */
   protected void setDirection(Direction pFacingDirection) {
      Validate.notNull(pFacingDirection);
      this.direction = pFacingDirection;
      if (pFacingDirection.getAxis().isHorizontal()) {
         this.xRot = 0.0F;
         this.yRot = (float)(this.direction.get2DDataValue() * 90);
      } else {
         this.xRot = (float)(-90 * pFacingDirection.getAxisDirection().getStep());
         this.yRot = 0.0F;
      }

      this.xRotO = this.xRot;
      this.yRotO = this.yRot;
      this.recalculateBoundingBox();
   }

   /**
    * Updates the entity bounding box based on current facing
    */
   protected void recalculateBoundingBox() {
      if (this.direction != null) {
         double d0 = 0.46875D;
         double d1 = (double)this.pos.getX() + 0.5D - (double)this.direction.getStepX() * 0.46875D;
         double d2 = (double)this.pos.getY() + 0.5D - (double)this.direction.getStepY() * 0.46875D;
         double d3 = (double)this.pos.getZ() + 0.5D - (double)this.direction.getStepZ() * 0.46875D;
         this.setPosRaw(d1, d2, d3);
         double d4 = (double)this.getWidth();
         double d5 = (double)this.getHeight();
         double d6 = (double)this.getWidth();
         Direction.Axis direction$axis = this.direction.getAxis();
         switch(direction$axis) {
         case X:
            d4 = 1.0D;
            break;
         case Y:
            d5 = 1.0D;
            break;
         case Z:
            d6 = 1.0D;
         }

         d4 = d4 / 32.0D;
         d5 = d5 / 32.0D;
         d6 = d6 / 32.0D;
         this.setBoundingBox(new AxisAlignedBB(d1 - d4, d2 - d5, d3 - d6, d1 + d4, d2 + d5, d3 + d6));
      }
   }

   /**
    * checks to make sure painting can be placed there
    */
   public boolean survives() {
      if (this.fixed) {
         return true;
      } else if (!this.level.noCollision(this)) {
         return false;
      } else {
         BlockState blockstate = this.level.getBlockState(this.pos.relative(this.direction.getOpposite()));
         return blockstate.getMaterial().isSolid() || this.direction.getAxis().isHorizontal() && RedstoneDiodeBlock.isDiode(blockstate) ? this.level.getEntities(this, this.getBoundingBox(), HANGING_ENTITY).isEmpty() : false;
      }
   }

   public void move(MoverType pType, Vector3d pPos) {
      if (!this.fixed) {
         super.move(pType, pPos);
      }

   }

   /**
    * Adds to the current velocity of the entity, and sets {@link #isAirBorne} to true.
    */
   public void push(double pX, double pY, double pZ) {
      if (!this.fixed) {
         super.push(pX, pY, pZ);
      }

   }

   public float getPickRadius() {
      return 0.0F;
   }

   /**
    * Called by the /kill command.
    */
   public void kill() {
      this.removeFramedMap(this.getItem());
      super.kill();
   }

   /**
    * Called when the entity is attacked.
    */
   public boolean hurt(DamageSource pSource, float pAmount) {
      if (this.fixed) {
         return pSource != DamageSource.OUT_OF_WORLD && !pSource.isCreativePlayer() ? false : super.hurt(pSource, pAmount);
      } else if (this.isInvulnerableTo(pSource)) {
         return false;
      } else if (!pSource.isExplosion() && !this.getItem().isEmpty()) {
         if (!this.level.isClientSide) {
            this.dropItem(pSource.getEntity(), false);
            this.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 1.0F, 1.0F);
         }

         return true;
      } else {
         return super.hurt(pSource, pAmount);
      }
   }

   public int getWidth() {
      return 12;
   }

   public int getHeight() {
      return 12;
   }

   /**
    * Checks if the entity is in range to render.
    */
   @OnlyIn(Dist.CLIENT)
   public boolean shouldRenderAtSqrDistance(double pDistance) {
      double d0 = 16.0D;
      d0 = d0 * 64.0D * getViewScale();
      return pDistance < d0 * d0;
   }

   /**
    * Called when this entity is broken. Entity parameter may be null.
    */
   public void dropItem(@Nullable Entity pBrokenEntity) {
      this.playSound(SoundEvents.ITEM_FRAME_BREAK, 1.0F, 1.0F);
      this.dropItem(pBrokenEntity, true);
   }

   public void playPlacementSound() {
      this.playSound(SoundEvents.ITEM_FRAME_PLACE, 1.0F, 1.0F);
   }

   private void dropItem(@Nullable Entity pEntity, boolean pDropSelf) {
      if (!this.fixed) {
         ItemStack itemstack = this.getItem();
         this.setItem(ItemStack.EMPTY);
         if (!this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            if (pEntity == null) {
               this.removeFramedMap(itemstack);
            }

         } else {
            if (pEntity instanceof PlayerEntity) {
               PlayerEntity playerentity = (PlayerEntity)pEntity;
               if (playerentity.abilities.instabuild) {
                  this.removeFramedMap(itemstack);
                  return;
               }
            }

            if (pDropSelf) {
               this.spawnAtLocation(Items.ITEM_FRAME);
            }

            if (!itemstack.isEmpty()) {
               itemstack = itemstack.copy();
               this.removeFramedMap(itemstack);
               if (this.random.nextFloat() < this.dropChance) {
                  this.spawnAtLocation(itemstack);
               }
            }

         }
      }
   }

   /**
    * Removes the dot representing this frame's position from the map when the item frame is broken.
    */
   private void removeFramedMap(ItemStack pStack) {
      if (pStack.getItem() == Items.FILLED_MAP) {
         MapData mapdata = FilledMapItem.getOrCreateSavedData(pStack, this.level);
         mapdata.removedFromFrame(this.pos, this.getId());
         mapdata.setDirty(true);
      }

      pStack.setEntityRepresentation((Entity)null);
   }

   public ItemStack getItem() {
      return this.getEntityData().get(DATA_ITEM);
   }

   public void setItem(ItemStack pStack) {
      this.setItem(pStack, true);
   }

   public void setItem(ItemStack pStack, boolean pUpdateNeighbour) {
      if (!pStack.isEmpty()) {
         pStack = pStack.copy();
         pStack.setCount(1);
         pStack.setEntityRepresentation(this);
      }

      this.getEntityData().set(DATA_ITEM, pStack);
      if (!pStack.isEmpty()) {
         this.playSound(SoundEvents.ITEM_FRAME_ADD_ITEM, 1.0F, 1.0F);
      }

      if (pUpdateNeighbour && this.pos != null) {
         this.level.updateNeighbourForOutputSignal(this.pos, Blocks.AIR);
      }

   }

   public boolean setSlot(int pSlotIndex, ItemStack pStack) {
      if (pSlotIndex == 0) {
         this.setItem(pStack);
         return true;
      } else {
         return false;
      }
   }

   public void onSyncedDataUpdated(DataParameter<?> pKey) {
      if (pKey.equals(DATA_ITEM)) {
         ItemStack itemstack = this.getItem();
         if (!itemstack.isEmpty() && itemstack.getFrame() != this) {
            itemstack.setEntityRepresentation(this);
         }
      }

   }

   /**
    * Return the rotation of the item currently on this frame.
    */
   public int getRotation() {
      return this.getEntityData().get(DATA_ROTATION);
   }

   public void setRotation(int pRotation) {
      this.setRotation(pRotation, true);
   }

   private void setRotation(int pRotation, boolean pUpdateNeighbour) {
      this.getEntityData().set(DATA_ROTATION, pRotation % 8);
      if (pUpdateNeighbour && this.pos != null) {
         this.level.updateNeighbourForOutputSignal(this.pos, Blocks.AIR);
      }

   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      if (!this.getItem().isEmpty()) {
         pCompound.put("Item", this.getItem().save(new CompoundNBT()));
         pCompound.putByte("ItemRotation", (byte)this.getRotation());
         pCompound.putFloat("ItemDropChance", this.dropChance);
      }

      pCompound.putByte("Facing", (byte)this.direction.get3DDataValue());
      pCompound.putBoolean("Invisible", this.isInvisible());
      pCompound.putBoolean("Fixed", this.fixed);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      CompoundNBT compoundnbt = pCompound.getCompound("Item");
      if (compoundnbt != null && !compoundnbt.isEmpty()) {
         ItemStack itemstack = ItemStack.of(compoundnbt);
         if (itemstack.isEmpty()) {
            LOGGER.warn("Unable to load item from: {}", (Object)compoundnbt);
         }

         ItemStack itemstack1 = this.getItem();
         if (!itemstack1.isEmpty() && !ItemStack.matches(itemstack, itemstack1)) {
            this.removeFramedMap(itemstack1);
         }

         this.setItem(itemstack, false);
         this.setRotation(pCompound.getByte("ItemRotation"), false);
         if (pCompound.contains("ItemDropChance", 99)) {
            this.dropChance = pCompound.getFloat("ItemDropChance");
         }
      }

      this.setDirection(Direction.from3DDataValue(pCompound.getByte("Facing")));
      this.setInvisible(pCompound.getBoolean("Invisible"));
      this.fixed = pCompound.getBoolean("Fixed");
   }

   public ActionResultType interact(PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      boolean flag = !this.getItem().isEmpty();
      boolean flag1 = !itemstack.isEmpty();
      if (this.fixed) {
         return ActionResultType.PASS;
      } else if (!this.level.isClientSide) {
         if (!flag) {
            if (flag1 && !this.removed) {
               this.setItem(itemstack);
               if (!pPlayer.abilities.instabuild) {
                  itemstack.shrink(1);
               }
            }
         } else {
            this.playSound(SoundEvents.ITEM_FRAME_ROTATE_ITEM, 1.0F, 1.0F);
            this.setRotation(this.getRotation() + 1);
         }

         return ActionResultType.CONSUME;
      } else {
         return !flag && !flag1 ? ActionResultType.PASS : ActionResultType.SUCCESS;
      }
   }

   public int getAnalogOutput() {
      return this.getItem().isEmpty() ? 0 : this.getRotation() % 8 + 1;
   }

   public IPacket<?> getAddEntityPacket() {
      return new SSpawnObjectPacket(this, this.getType(), this.direction.get3DDataValue(), this.getPos());
   }
}