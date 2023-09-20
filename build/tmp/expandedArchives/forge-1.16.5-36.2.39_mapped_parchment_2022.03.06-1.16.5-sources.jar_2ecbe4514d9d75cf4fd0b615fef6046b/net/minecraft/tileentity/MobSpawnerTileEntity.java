package net.minecraft.tileentity;

import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.util.WeightedSpawnerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;

public class MobSpawnerTileEntity extends TileEntity implements ITickableTileEntity {
   private final AbstractSpawner spawner = new AbstractSpawner() {
      public void broadcastEvent(int pId) {
         MobSpawnerTileEntity.this.level.blockEvent(MobSpawnerTileEntity.this.worldPosition, Blocks.SPAWNER, pId, 0);
      }

      public World getLevel() {
         return MobSpawnerTileEntity.this.level;
      }

      public BlockPos getPos() {
         return MobSpawnerTileEntity.this.worldPosition;
      }

      public void setNextSpawnData(WeightedSpawnerEntity p_184993_1_) {
         super.setNextSpawnData(p_184993_1_);
         if (this.getLevel() != null) {
            BlockState blockstate = this.getLevel().getBlockState(this.getPos());
            this.getLevel().sendBlockUpdated(MobSpawnerTileEntity.this.worldPosition, blockstate, blockstate, 4);
         }

      }
   };

   public MobSpawnerTileEntity() {
      super(TileEntityType.MOB_SPAWNER);
   }

   public void load(BlockState p_230337_1_, CompoundNBT p_230337_2_) {
      super.load(p_230337_1_, p_230337_2_);
      this.spawner.load(p_230337_2_);
   }

   public CompoundNBT save(CompoundNBT pCompound) {
      super.save(pCompound);
      this.spawner.save(pCompound);
      return pCompound;
   }

   public void tick() {
      this.spawner.tick();
   }

   /**
    * Retrieves packet to send to the client whenever this Tile Entity is resynced via World.notifyBlockUpdate. For
    * modded TE's, this packet comes back to you clientside in {@link #onDataPacket}
    */
   @Nullable
   public SUpdateTileEntityPacket getUpdatePacket() {
      return new SUpdateTileEntityPacket(this.worldPosition, 1, this.getUpdateTag());
   }

   /**
    * Get an NBT compound to sync to the client with SPacketChunkData, used for initial loading of the chunk or when
    * many blocks change at once. This compound comes back to you clientside in {@link handleUpdateTag}
    */
   public CompoundNBT getUpdateTag() {
      CompoundNBT compoundnbt = this.save(new CompoundNBT());
      compoundnbt.remove("SpawnPotentials");
      return compoundnbt;
   }

   /**
    * See {@link Block#eventReceived} for more information. This must return true serverside before it is called
    * clientside.
    */
   public boolean triggerEvent(int pId, int pType) {
      return this.spawner.onEventTriggered(pId) ? true : super.triggerEvent(pId, pType);
   }

   /**
    * Checks if players can use this tile entity to access operator (permission level 2) commands either directly or
    * indirectly, such as give or setblock. A similar method exists for entities at {@link
    * net.minecraft.entity.Entity#ignoreItemEntityData()}.<p>For example, {@link
    * net.minecraft.tileentity.TileEntitySign#onlyOpsCanSetNbt() signs} (player right-clicking) and {@link
    * net.minecraft.tileentity.TileEntityCommandBlock#onlyOpsCanSetNbt() command blocks} are considered
    * accessible.</p>@return true if this block entity offers ways for unauthorized players to use restricted commands
    */
   public boolean onlyOpCanSetNbt() {
      return true;
   }

   public AbstractSpawner getSpawner() {
      return this.spawner;
   }
}