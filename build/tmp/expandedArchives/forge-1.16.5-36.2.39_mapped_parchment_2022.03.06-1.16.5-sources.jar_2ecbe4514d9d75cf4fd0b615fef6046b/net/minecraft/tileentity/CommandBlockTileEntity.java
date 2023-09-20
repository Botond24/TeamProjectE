package net.minecraft.tileentity;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CommandBlockBlock;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CommandBlockTileEntity extends TileEntity {
   private boolean powered;
   private boolean auto;
   private boolean conditionMet;
   private boolean sendToClient;
   private final CommandBlockLogic commandBlock = new CommandBlockLogic() {
      /**
       * Sets the command.
       */
      public void setCommand(String pCommand) {
         super.setCommand(pCommand);
         CommandBlockTileEntity.this.setChanged();
      }

      public ServerWorld getLevel() {
         return (ServerWorld)CommandBlockTileEntity.this.level;
      }

      public void onUpdated() {
         BlockState blockstate = CommandBlockTileEntity.this.level.getBlockState(CommandBlockTileEntity.this.worldPosition);
         this.getLevel().sendBlockUpdated(CommandBlockTileEntity.this.worldPosition, blockstate, blockstate, 3);
      }

      @OnlyIn(Dist.CLIENT)
      public Vector3d getPosition() {
         return Vector3d.atCenterOf(CommandBlockTileEntity.this.worldPosition);
      }

      public CommandSource createCommandSourceStack() {
         return new CommandSource(this, Vector3d.atCenterOf(CommandBlockTileEntity.this.worldPosition), Vector2f.ZERO, this.getLevel(), 2, this.getName().getString(), this.getName(), this.getLevel().getServer(), (Entity)null);
      }
   };

   public CommandBlockTileEntity() {
      super(TileEntityType.COMMAND_BLOCK);
   }

   public CompoundNBT save(CompoundNBT pCompound) {
      super.save(pCompound);
      this.commandBlock.save(pCompound);
      pCompound.putBoolean("powered", this.isPowered());
      pCompound.putBoolean("conditionMet", this.wasConditionMet());
      pCompound.putBoolean("auto", this.isAutomatic());
      return pCompound;
   }

   public void load(BlockState p_230337_1_, CompoundNBT p_230337_2_) {
      super.load(p_230337_1_, p_230337_2_);
      this.commandBlock.load(p_230337_2_);
      this.powered = p_230337_2_.getBoolean("powered");
      this.conditionMet = p_230337_2_.getBoolean("conditionMet");
      this.setAutomatic(p_230337_2_.getBoolean("auto"));
   }

   /**
    * Retrieves packet to send to the client whenever this Tile Entity is resynced via World.notifyBlockUpdate. For
    * modded TE's, this packet comes back to you clientside in {@link #onDataPacket}
    */
   @Nullable
   public SUpdateTileEntityPacket getUpdatePacket() {
      if (this.isSendToClient()) {
         this.setSendToClient(false);
         CompoundNBT compoundnbt = this.save(new CompoundNBT());
         return new SUpdateTileEntityPacket(this.worldPosition, 2, compoundnbt);
      } else {
         return null;
      }
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

   public CommandBlockLogic getCommandBlock() {
      return this.commandBlock;
   }

   public void setPowered(boolean pPowered) {
      this.powered = pPowered;
   }

   public boolean isPowered() {
      return this.powered;
   }

   public boolean isAutomatic() {
      return this.auto;
   }

   public void setAutomatic(boolean pAuto) {
      boolean flag = this.auto;
      this.auto = pAuto;
      if (!flag && pAuto && !this.powered && this.level != null && this.getMode() != CommandBlockTileEntity.Mode.SEQUENCE) {
         this.scheduleTick();
      }

   }

   public void onModeSwitch() {
      CommandBlockTileEntity.Mode commandblocktileentity$mode = this.getMode();
      if (commandblocktileentity$mode == CommandBlockTileEntity.Mode.AUTO && (this.powered || this.auto) && this.level != null) {
         this.scheduleTick();
      }

   }

   private void scheduleTick() {
      Block block = this.getBlockState().getBlock();
      if (block instanceof CommandBlockBlock) {
         this.markConditionMet();
         this.level.getBlockTicks().scheduleTick(this.worldPosition, block, 1);
      }

   }

   public boolean wasConditionMet() {
      return this.conditionMet;
   }

   public boolean markConditionMet() {
      this.conditionMet = true;
      if (this.isConditional()) {
         BlockPos blockpos = this.worldPosition.relative(this.level.getBlockState(this.worldPosition).getValue(CommandBlockBlock.FACING).getOpposite());
         if (this.level.getBlockState(blockpos).getBlock() instanceof CommandBlockBlock) {
            TileEntity tileentity = this.level.getBlockEntity(blockpos);
            this.conditionMet = tileentity instanceof CommandBlockTileEntity && ((CommandBlockTileEntity)tileentity).getCommandBlock().getSuccessCount() > 0;
         } else {
            this.conditionMet = false;
         }
      }

      return this.conditionMet;
   }

   public boolean isSendToClient() {
      return this.sendToClient;
   }

   public void setSendToClient(boolean pSendToClient) {
      this.sendToClient = pSendToClient;
   }

   public CommandBlockTileEntity.Mode getMode() {
      BlockState blockstate = this.getBlockState();
      if (blockstate.is(Blocks.COMMAND_BLOCK)) {
         return CommandBlockTileEntity.Mode.REDSTONE;
      } else if (blockstate.is(Blocks.REPEATING_COMMAND_BLOCK)) {
         return CommandBlockTileEntity.Mode.AUTO;
      } else {
         return blockstate.is(Blocks.CHAIN_COMMAND_BLOCK) ? CommandBlockTileEntity.Mode.SEQUENCE : CommandBlockTileEntity.Mode.REDSTONE;
      }
   }

   public boolean isConditional() {
      BlockState blockstate = this.level.getBlockState(this.getBlockPos());
      return blockstate.getBlock() instanceof CommandBlockBlock ? blockstate.getValue(CommandBlockBlock.CONDITIONAL) : false;
   }

   /**
    * Marks this {@code BlockEntity} as valid again (no longer removed from the level).
    */
   public void clearRemoved() {
      this.clearCache();
      super.clearRemoved();
   }

   public static enum Mode {
      SEQUENCE,
      AUTO,
      REDSTONE;
   }
}