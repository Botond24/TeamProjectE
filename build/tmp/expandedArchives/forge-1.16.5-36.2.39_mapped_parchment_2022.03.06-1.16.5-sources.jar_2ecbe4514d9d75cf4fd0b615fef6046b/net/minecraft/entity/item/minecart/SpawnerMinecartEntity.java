package net.minecraft.entity.item.minecart;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SpawnerMinecartEntity extends AbstractMinecartEntity {
   private final AbstractSpawner spawner = new AbstractSpawner() {
      public void broadcastEvent(int pId) {
         SpawnerMinecartEntity.this.level.broadcastEntityEvent(SpawnerMinecartEntity.this, (byte)pId);
      }

      public World getLevel() {
         return SpawnerMinecartEntity.this.level;
      }

      public BlockPos getPos() {
         return SpawnerMinecartEntity.this.blockPosition();
      }

      @Override
      @javax.annotation.Nullable
      public net.minecraft.entity.Entity getSpawnerEntity() {
         return SpawnerMinecartEntity.this;
      }
   };

   public SpawnerMinecartEntity(EntityType<? extends SpawnerMinecartEntity> p_i50114_1_, World p_i50114_2_) {
      super(p_i50114_1_, p_i50114_2_);
   }

   public SpawnerMinecartEntity(World pLevel, double pX, double pY, double pZ) {
      super(EntityType.SPAWNER_MINECART, pLevel, pX, pY, pZ);
   }

   public AbstractMinecartEntity.Type getMinecartType() {
      return AbstractMinecartEntity.Type.SPAWNER;
   }

   public BlockState getDefaultDisplayBlockState() {
      return Blocks.SPAWNER.defaultBlockState();
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   protected void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.spawner.load(pCompound);
   }

   protected void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      this.spawner.save(pCompound);
   }

   /**
    * Handles an entity event fired from {@link net.minecraft.world.level.Level#broadcastEntityEvent}.
    */
   @OnlyIn(Dist.CLIENT)
   public void handleEntityEvent(byte pId) {
      this.spawner.onEventTriggered(pId);
   }

   /**
    * Called to update the entity's position/logic.
    */
   public void tick() {
      super.tick();
      this.spawner.tick();
   }

   /**
    * Checks if players can use this entity to access operator (permission level 2) commands either directly or
    * indirectly, such as give or setblock. A similar method exists for entities at {@link
    * net.minecraft.tileentity.TileEntity#onlyOpsCanSetNbt()}.<p>For example, {@link
    * net.minecraft.entity.item.EntityMinecartCommandBlock#ignoreItemEntityData() command block minecarts} and {@link
    * net.minecraft.entity.item.EntityMinecartMobSpawner#ignoreItemEntityData() mob spawner minecarts} (spawning command
    * block minecarts or drops) are considered accessible.</p>@return true if this entity offers ways for unauthorized
    * players to use restricted commands
    */
   public boolean onlyOpCanSetNbt() {
      return true;
   }
}
