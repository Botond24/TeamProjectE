package net.minecraft.tileentity;

import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class TileEntity extends net.minecraftforge.common.capabilities.CapabilityProvider<TileEntity> implements net.minecraftforge.common.extensions.IForgeTileEntity {
   private static final Logger LOGGER = LogManager.getLogger();
   private final TileEntityType<?> type;
   /** the instance of the world the tile entity is in. */
   @Nullable
   protected World level;
   protected BlockPos worldPosition = BlockPos.ZERO;
   protected boolean remove;
   @Nullable
   private BlockState blockState;
   private boolean hasLoggedInvalidStateBefore;
   private CompoundNBT customTileData;

   public TileEntity(TileEntityType<?> p_i48289_1_) {
      super(TileEntity.class);
      this.type = p_i48289_1_;
      this.gatherCapabilities();
   }

   @Nullable
   public World getLevel() {
      return this.level;
   }

   public void setLevelAndPosition(World p_226984_1_, BlockPos p_226984_2_) {
      this.level = p_226984_1_;
      this.worldPosition = p_226984_2_.immutable();
   }

   /**
    * @return whether this BlockEntity's level has been set
    */
   public boolean hasLevel() {
      return this.level != null;
   }

   public void load(BlockState p_230337_1_, CompoundNBT p_230337_2_) {
      this.worldPosition = new BlockPos(p_230337_2_.getInt("x"), p_230337_2_.getInt("y"), p_230337_2_.getInt("z"));
      if (p_230337_2_.contains("ForgeData")) this.customTileData = p_230337_2_.getCompound("ForgeData");
      if (getCapabilities() != null && p_230337_2_.contains("ForgeCaps")) deserializeCaps(p_230337_2_.getCompound("ForgeCaps"));
   }

   public CompoundNBT save(CompoundNBT pCompound) {
      return this.saveMetadata(pCompound);
   }

   private CompoundNBT saveMetadata(CompoundNBT pTag) {
      ResourceLocation resourcelocation = TileEntityType.getKey(this.getType());
      if (resourcelocation == null) {
         throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
      } else {
         pTag.putString("id", resourcelocation.toString());
         pTag.putInt("x", this.worldPosition.getX());
         pTag.putInt("y", this.worldPosition.getY());
         pTag.putInt("z", this.worldPosition.getZ());
         if (this.customTileData != null) pTag.put("ForgeData", this.customTileData);
         if (getCapabilities() != null) pTag.put("ForgeCaps", serializeCaps());
         return pTag;
      }
   }

   @Nullable
   public static TileEntity loadStatic(BlockState p_235657_0_, CompoundNBT p_235657_1_) {
      String s = p_235657_1_.getString("id");
      return Registry.BLOCK_ENTITY_TYPE.getOptional(new ResourceLocation(s)).map((p_213134_1_) -> {
         try {
            return p_213134_1_.create();
         } catch (Throwable throwable) {
            LOGGER.error("Failed to create block entity {}", s, throwable);
            return null;
         }
      }).map((p_235656_3_) -> {
         try {
            p_235656_3_.load(p_235657_0_, p_235657_1_);
            return p_235656_3_;
         } catch (Throwable throwable) {
            LOGGER.error("Failed to load data for block entity {}", s, throwable);
            return null;
         }
      }).orElseGet(() -> {
         LOGGER.warn("Skipping BlockEntity with id {}", (Object)s);
         return null;
      });
   }

   /**
    * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
    * hasn't changed and skip it.
    */
   public void setChanged() {
      if (this.level != null) {
         this.blockState = this.level.getBlockState(this.worldPosition);
         this.level.blockEntityChanged(this.worldPosition, this);
         if (!this.blockState.isAir(this.level, this.worldPosition)) {
            this.level.updateNeighbourForOutputSignal(this.worldPosition, this.blockState.getBlock());
         }
      }

   }

   @OnlyIn(Dist.CLIENT)
   public double getViewDistance() {
      return 64.0D;
   }

   public BlockPos getBlockPos() {
      return this.worldPosition;
   }

   public BlockState getBlockState() {
      if (this.blockState == null) {
         this.blockState = this.level.getBlockState(this.worldPosition);
      }

      return this.blockState;
   }

   /**
    * Retrieves packet to send to the client whenever this Tile Entity is resynced via World.notifyBlockUpdate. For
    * modded TE's, this packet comes back to you clientside in {@link #onDataPacket}
    */
   @Nullable
   public SUpdateTileEntityPacket getUpdatePacket() {
      return null;
   }

   /**
    * Get an NBT compound to sync to the client with SPacketChunkData, used for initial loading of the chunk or when
    * many blocks change at once. This compound comes back to you clientside in {@link handleUpdateTag}
    */
   public CompoundNBT getUpdateTag() {
      return this.saveMetadata(new CompoundNBT());
   }

   public boolean isRemoved() {
      return this.remove;
   }

   /**
    * invalidates a tile entity
    */
   public void setRemoved() {
      this.remove = true;
      this.invalidateCaps();
      requestModelDataUpdate();
   }

   @Override
   public void onChunkUnloaded() {
      this.invalidateCaps();
   }

   /**
    * Marks this {@code BlockEntity} as valid again (no longer removed from the level).
    */
   public void clearRemoved() {
      this.remove = false;
   }

   /**
    * See {@link Block#eventReceived} for more information. This must return true serverside before it is called
    * clientside.
    */
   public boolean triggerEvent(int pId, int pType) {
      return false;
   }

   public void clearCache() {
      this.blockState = null;
   }

   public void fillCrashReportCategory(CrashReportCategory pReportCategory) {
      pReportCategory.setDetail("Name", () -> {
         return Registry.BLOCK_ENTITY_TYPE.getKey(this.getType()) + " // " + this.getClass().getCanonicalName();
      });
      if (this.level != null) {
         CrashReportCategory.populateBlockDetails(pReportCategory, this.worldPosition, this.getBlockState());
         CrashReportCategory.populateBlockDetails(pReportCategory, this.worldPosition, this.level.getBlockState(this.worldPosition));
      }
   }

   public void setPosition(BlockPos p_174878_1_) {
      this.worldPosition = p_174878_1_.immutable();
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
      return false;
   }

   public void rotate(Rotation p_189667_1_) {
   }

   public void mirror(Mirror p_189668_1_) {
   }

   public TileEntityType<?> getType() {
      return this.type;
   }

   @Override
   public CompoundNBT getTileData() {
      if (this.customTileData == null)
         this.customTileData = new CompoundNBT();
      return this.customTileData;
   }

   public void logInvalidState() {
      if (!this.hasLoggedInvalidStateBefore) {
         this.hasLoggedInvalidStateBefore = true;
         LOGGER.warn("Block entity invalid: {} @ {}", () -> {
            return Registry.BLOCK_ENTITY_TYPE.getKey(this.getType());
         }, this::getBlockPos);
      }
   }
}
