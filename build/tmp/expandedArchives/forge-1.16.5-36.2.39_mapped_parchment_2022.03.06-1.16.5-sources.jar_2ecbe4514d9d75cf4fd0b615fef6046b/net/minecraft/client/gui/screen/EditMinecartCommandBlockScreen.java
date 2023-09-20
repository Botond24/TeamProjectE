package net.minecraft.client.gui.screen;

import net.minecraft.entity.item.minecart.CommandBlockMinecartEntity;
import net.minecraft.network.play.client.CUpdateMinecartCommandBlockPacket;
import net.minecraft.tileentity.CommandBlockLogic;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EditMinecartCommandBlockScreen extends AbstractCommandBlockScreen {
   private final CommandBlockLogic commandBlock;

   public EditMinecartCommandBlockScreen(CommandBlockLogic pCommandBlock) {
      this.commandBlock = pCommandBlock;
   }

   public CommandBlockLogic getCommandBlock() {
      return this.commandBlock;
   }

   int getPreviousY() {
      return 150;
   }

   protected void init() {
      super.init();
      this.trackOutput = this.getCommandBlock().isTrackOutput();
      this.updateCommandOutput();
      this.commandEdit.setValue(this.getCommandBlock().getCommand());
   }

   protected void populateAndSendPacket(CommandBlockLogic pCommandBlock) {
      if (pCommandBlock instanceof CommandBlockMinecartEntity.MinecartCommandLogic) {
         CommandBlockMinecartEntity.MinecartCommandLogic commandblockminecartentity$minecartcommandlogic = (CommandBlockMinecartEntity.MinecartCommandLogic)pCommandBlock;
         this.minecraft.getConnection().send(new CUpdateMinecartCommandBlockPacket(commandblockminecartentity$minecartcommandlogic.getMinecart().getId(), this.commandEdit.getValue(), pCommandBlock.isTrackOutput()));
      }

   }
}