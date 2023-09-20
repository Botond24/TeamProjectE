package net.minecraft.entity.item.minecart;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class FurnaceMinecartEntity extends AbstractMinecartEntity {
   private static final DataParameter<Boolean> DATA_ID_FUEL = EntityDataManager.defineId(FurnaceMinecartEntity.class, DataSerializers.BOOLEAN);
   private int fuel;
   public double xPush;
   public double zPush;
   /** The fuel item used to make the minecart move. */
   private static final Ingredient INGREDIENT = Ingredient.of(Items.COAL, Items.CHARCOAL);

   public FurnaceMinecartEntity(EntityType<? extends FurnaceMinecartEntity> p_i50119_1_, World p_i50119_2_) {
      super(p_i50119_1_, p_i50119_2_);
   }

   public FurnaceMinecartEntity(World pLevel, double pX, double pY, double pZ) {
      super(EntityType.FURNACE_MINECART, pLevel, pX, pY, pZ);
   }

   public AbstractMinecartEntity.Type getMinecartType() {
      return AbstractMinecartEntity.Type.FURNACE;
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_ID_FUEL, false);
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      if (!this.level.isClientSide()) {
         if (this.fuel > 0) {
            --this.fuel;
         }

         if (this.fuel <= 0) {
            this.xPush = 0.0D;
            this.zPush = 0.0D;
         }

         this.setHasFuel(this.fuel > 0);
      }

      if (this.hasFuel() && this.random.nextInt(4) == 0) {
         this.level.addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.8D, this.getZ(), 0.0D, 0.0D, 0.0D);
      }

   }

   /**
    * Get's the maximum speed for a minecart
    */
   protected double getMaxSpeed() {
      return 0.2D;
   }

   @Override
   public float getMaxCartSpeedOnRail() {
      return 0.2f;
   }

   public void destroy(DamageSource pSource) {
      super.destroy(pSource);
      if (!pSource.isExplosion() && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
         this.spawnAtLocation(Blocks.FURNACE);
      }

   }

   protected void moveAlongTrack(BlockPos pPos, BlockState pState) {
      double d0 = 1.0E-4D;
      double d1 = 0.001D;
      super.moveAlongTrack(pPos, pState);
      Vector3d vector3d = this.getDeltaMovement();
      double d2 = getHorizontalDistanceSqr(vector3d);
      double d3 = this.xPush * this.xPush + this.zPush * this.zPush;
      if (d3 > 1.0E-4D && d2 > 0.001D) {
         double d4 = (double)MathHelper.sqrt(d2);
         double d5 = (double)MathHelper.sqrt(d3);
         this.xPush = vector3d.x / d4 * d5;
         this.zPush = vector3d.z / d4 * d5;
      }

   }

   protected void applyNaturalSlowdown() {
      double d0 = this.xPush * this.xPush + this.zPush * this.zPush;
      if (d0 > 1.0E-7D) {
         d0 = (double)MathHelper.sqrt(d0);
         this.xPush /= d0;
         this.zPush /= d0;
         this.setDeltaMovement(this.getDeltaMovement().multiply(0.8D, 0.0D, 0.8D).add(this.xPush, 0.0D, this.zPush));
      } else {
         this.setDeltaMovement(this.getDeltaMovement().multiply(0.98D, 0.0D, 0.98D));
      }

      super.applyNaturalSlowdown();
   }

   public ActionResultType interact(PlayerEntity pPlayer, Hand pHand) {
      ActionResultType ret = super.interact(pPlayer, pHand);
      if (ret.consumesAction()) return ret;
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (INGREDIENT.test(itemstack) && this.fuel + 3600 <= 32000) {
         if (!pPlayer.abilities.instabuild) {
            itemstack.shrink(1);
         }

         this.fuel += 3600;
      }

      if (this.fuel > 0) {
         this.xPush = this.getX() - pPlayer.getX();
         this.zPush = this.getZ() - pPlayer.getZ();
      }

      return ActionResultType.sidedSuccess(this.level.isClientSide);
   }

   protected void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putDouble("PushX", this.xPush);
      pCompound.putDouble("PushZ", this.zPush);
      pCompound.putShort("Fuel", (short)this.fuel);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   protected void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.xPush = pCompound.getDouble("PushX");
      this.zPush = pCompound.getDouble("PushZ");
      this.fuel = pCompound.getShort("Fuel");
   }

   protected boolean hasFuel() {
      return this.entityData.get(DATA_ID_FUEL);
   }

   protected void setHasFuel(boolean pHasFuel) {
      this.entityData.set(DATA_ID_FUEL, pHasFuel);
   }

   public BlockState getDefaultDisplayBlockState() {
      return Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, Direction.NORTH).setValue(FurnaceBlock.LIT, Boolean.valueOf(this.hasFuel()));
   }
}
