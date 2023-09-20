package net.minecraft.tileentity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ICommandSource;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class CommandBlockLogic implements ICommandSource {
   /** The formatting for the timestamp on commands run. */
   private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
   private static final ITextComponent DEFAULT_NAME = new StringTextComponent("@");
   private long lastExecution = -1L;
   private boolean updateLastExecution = true;
   /** The number of successful commands run. (used for redstone output) */
   private int successCount;
   private boolean trackOutput = true;
   /** The previously run command. */
   @Nullable
   private ITextComponent lastOutput;
   /** The command stored in the command block. */
   private String command = "";
   /** The custom name of the command block. (defaults to "@") */
   private ITextComponent name = DEFAULT_NAME;

   /**
    * returns the successCount int.
    */
   public int getSuccessCount() {
      return this.successCount;
   }

   public void setSuccessCount(int pSuccessCount) {
      this.successCount = pSuccessCount;
   }

   /**
    * Returns the lastOutput.
    */
   public ITextComponent getLastOutput() {
      return this.lastOutput == null ? StringTextComponent.EMPTY : this.lastOutput;
   }

   public CompoundNBT save(CompoundNBT pCompound) {
      pCompound.putString("Command", this.command);
      pCompound.putInt("SuccessCount", this.successCount);
      pCompound.putString("CustomName", ITextComponent.Serializer.toJson(this.name));
      pCompound.putBoolean("TrackOutput", this.trackOutput);
      if (this.lastOutput != null && this.trackOutput) {
         pCompound.putString("LastOutput", ITextComponent.Serializer.toJson(this.lastOutput));
      }

      pCompound.putBoolean("UpdateLastExecution", this.updateLastExecution);
      if (this.updateLastExecution && this.lastExecution > 0L) {
         pCompound.putLong("LastExecution", this.lastExecution);
      }

      return pCompound;
   }

   /**
    * Reads NBT formatting and stored data into variables.
    */
   public void load(CompoundNBT pNbt) {
      this.command = pNbt.getString("Command");
      this.successCount = pNbt.getInt("SuccessCount");
      if (pNbt.contains("CustomName", 8)) {
         this.setName(ITextComponent.Serializer.fromJson(pNbt.getString("CustomName")));
      }

      if (pNbt.contains("TrackOutput", 1)) {
         this.trackOutput = pNbt.getBoolean("TrackOutput");
      }

      if (pNbt.contains("LastOutput", 8) && this.trackOutput) {
         try {
            this.lastOutput = ITextComponent.Serializer.fromJson(pNbt.getString("LastOutput"));
         } catch (Throwable throwable) {
            this.lastOutput = new StringTextComponent(throwable.getMessage());
         }
      } else {
         this.lastOutput = null;
      }

      if (pNbt.contains("UpdateLastExecution")) {
         this.updateLastExecution = pNbt.getBoolean("UpdateLastExecution");
      }

      if (this.updateLastExecution && pNbt.contains("LastExecution")) {
         this.lastExecution = pNbt.getLong("LastExecution");
      } else {
         this.lastExecution = -1L;
      }

   }

   /**
    * Sets the command.
    */
   public void setCommand(String pCommand) {
      this.command = pCommand;
      this.successCount = 0;
   }

   /**
    * Returns the command of the command block.
    */
   public String getCommand() {
      return this.command;
   }

   public boolean performCommand(World pLevel) {
      if (!pLevel.isClientSide && pLevel.getGameTime() != this.lastExecution) {
         if ("Searge".equalsIgnoreCase(this.command)) {
            this.lastOutput = new StringTextComponent("#itzlipofutzli");
            this.successCount = 1;
            return true;
         } else {
            this.successCount = 0;
            MinecraftServer minecraftserver = this.getLevel().getServer();
            if (minecraftserver.isCommandBlockEnabled() && !StringUtils.isNullOrEmpty(this.command)) {
               try {
                  this.lastOutput = null;
                  CommandSource commandsource = this.createCommandSourceStack().withCallback((p_209527_1_, p_209527_2_, p_209527_3_) -> {
                     if (p_209527_2_) {
                        ++this.successCount;
                     }

                  });
                  minecraftserver.getCommands().performCommand(commandsource, this.command);
               } catch (Throwable throwable) {
                  CrashReport crashreport = CrashReport.forThrowable(throwable, "Executing command block");
                  CrashReportCategory crashreportcategory = crashreport.addCategory("Command to be executed");
                  crashreportcategory.setDetail("Command", this::getCommand);
                  crashreportcategory.setDetail("Name", () -> {
                     return this.getName().getString();
                  });
                  throw new ReportedException(crashreport);
               }
            }

            if (this.updateLastExecution) {
               this.lastExecution = pLevel.getGameTime();
            } else {
               this.lastExecution = -1L;
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public ITextComponent getName() {
      return this.name;
   }

   public void setName(@Nullable ITextComponent pName) {
      if (pName != null) {
         this.name = pName;
      } else {
         this.name = DEFAULT_NAME;
      }

   }

   /**
    * Send a chat message to the CommandSender
    */
   public void sendMessage(ITextComponent pComponent, UUID pSenderUUID) {
      if (this.trackOutput) {
         this.lastOutput = (new StringTextComponent("[" + TIME_FORMAT.format(new Date()) + "] ")).append(pComponent);
         this.onUpdated();
      }

   }

   public abstract ServerWorld getLevel();

   public abstract void onUpdated();

   public void setLastOutput(@Nullable ITextComponent pLastOutputMessage) {
      this.lastOutput = pLastOutputMessage;
   }

   public void setTrackOutput(boolean pShouldTrackOutput) {
      this.trackOutput = pShouldTrackOutput;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isTrackOutput() {
      return this.trackOutput;
   }

   public ActionResultType usedBy(PlayerEntity pPlayer) {
      if (!pPlayer.canUseGameMasterBlocks()) {
         return ActionResultType.PASS;
      } else {
         if (pPlayer.getCommandSenderWorld().isClientSide) {
            pPlayer.openMinecartCommandBlock(this);
         }

         return ActionResultType.sidedSuccess(pPlayer.level.isClientSide);
      }
   }

   @OnlyIn(Dist.CLIENT)
   public abstract Vector3d getPosition();

   public abstract CommandSource createCommandSourceStack();

   public boolean acceptsSuccess() {
      return this.getLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK) && this.trackOutput;
   }

   public boolean acceptsFailure() {
      return this.trackOutput;
   }

   public boolean shouldInformAdmins() {
      return this.getLevel().getGameRules().getBoolean(GameRules.RULE_COMMANDBLOCKOUTPUT);
   }
}