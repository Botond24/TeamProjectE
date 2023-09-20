package net.minecraft.entity.item.minecart;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.CommandBlockLogic;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CommandBlockMinecartEntity extends AbstractMinecartEntity {
   private static final DataParameter<String> DATA_ID_COMMAND_NAME = EntityDataManager.defineId(CommandBlockMinecartEntity.class, DataSerializers.STRING);
   private static final DataParameter<ITextComponent> DATA_ID_LAST_OUTPUT = EntityDataManager.defineId(CommandBlockMinecartEntity.class, DataSerializers.COMPONENT);
   private final CommandBlockLogic commandBlock = new CommandBlockMinecartEntity.MinecartCommandLogic();
   /** Cooldown before command block logic runs again in ticks */
   private int lastActivated;

   public CommandBlockMinecartEntity(EntityType<? extends CommandBlockMinecartEntity> p_i50123_1_, World p_i50123_2_) {
      super(p_i50123_1_, p_i50123_2_);
   }

   public CommandBlockMinecartEntity(World pLevel, double pX, double pY, double pZ) {
      super(EntityType.COMMAND_BLOCK_MINECART, pLevel, pX, pY, pZ);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.getEntityData().define(DATA_ID_COMMAND_NAME, "");
      this.getEntityData().define(DATA_ID_LAST_OUTPUT, StringTextComponent.EMPTY);
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   protected void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.commandBlock.load(pCompound);
      this.getEntityData().set(DATA_ID_COMMAND_NAME, this.getCommandBlock().getCommand());
      this.getEntityData().set(DATA_ID_LAST_OUTPUT, this.getCommandBlock().getLastOutput());
   }

   protected void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      this.commandBlock.save(pCompound);
   }

   public AbstractMinecartEntity.Type getMinecartType() {
      return AbstractMinecartEntity.Type.COMMAND_BLOCK;
   }

   public BlockState getDefaultDisplayBlockState() {
      return Blocks.COMMAND_BLOCK.defaultBlockState();
   }

   public CommandBlockLogic getCommandBlock() {
      return this.commandBlock;
   }

   /**
    * Called every tick the minecart is on an activator rail.
    */
   public void activateMinecart(int pX, int pY, int pZ, boolean pReceivingPower) {
      if (pReceivingPower && this.tickCount - this.lastActivated >= 4) {
         this.getCommandBlock().performCommand(this.level);
         this.lastActivated = this.tickCount;
      }

   }

   public ActionResultType interact(PlayerEntity pPlayer, Hand pHand) {
      ActionResultType ret = super.interact(pPlayer, pHand);
      if (ret.consumesAction()) return ret;
      return this.commandBlock.usedBy(pPlayer);
   }

   public void onSyncedDataUpdated(DataParameter<?> pKey) {
      super.onSyncedDataUpdated(pKey);
      if (DATA_ID_LAST_OUTPUT.equals(pKey)) {
         try {
            this.commandBlock.setLastOutput(this.getEntityData().get(DATA_ID_LAST_OUTPUT));
         } catch (Throwable throwable) {
         }
      } else if (DATA_ID_COMMAND_NAME.equals(pKey)) {
         this.commandBlock.setCommand(this.getEntityData().get(DATA_ID_COMMAND_NAME));
      }

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

   public class MinecartCommandLogic extends CommandBlockLogic {
      public ServerWorld getLevel() {
         return (ServerWorld)CommandBlockMinecartEntity.this.level;
      }

      public void onUpdated() {
         CommandBlockMinecartEntity.this.getEntityData().set(CommandBlockMinecartEntity.DATA_ID_COMMAND_NAME, this.getCommand());
         CommandBlockMinecartEntity.this.getEntityData().set(CommandBlockMinecartEntity.DATA_ID_LAST_OUTPUT, this.getLastOutput());
      }

      @OnlyIn(Dist.CLIENT)
      public Vector3d getPosition() {
         return CommandBlockMinecartEntity.this.position();
      }

      @OnlyIn(Dist.CLIENT)
      public CommandBlockMinecartEntity getMinecart() {
         return CommandBlockMinecartEntity.this;
      }

      public CommandSource createCommandSourceStack() {
         return new CommandSource(this, CommandBlockMinecartEntity.this.position(), CommandBlockMinecartEntity.this.getRotationVector(), this.getLevel(), 2, this.getName().getString(), CommandBlockMinecartEntity.this.getDisplayName(), this.getLevel().getServer(), CommandBlockMinecartEntity.this);
      }
   }
}
