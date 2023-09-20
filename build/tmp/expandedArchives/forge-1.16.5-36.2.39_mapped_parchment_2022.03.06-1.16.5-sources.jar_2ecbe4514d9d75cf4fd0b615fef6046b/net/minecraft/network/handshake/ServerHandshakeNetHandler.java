package net.minecraft.network.handshake;

import net.minecraft.network.NetworkManager;
import net.minecraft.network.ProtocolType;
import net.minecraft.network.handshake.client.CHandshakePacket;
import net.minecraft.network.login.ServerLoginNetHandler;
import net.minecraft.network.login.server.SDisconnectLoginPacket;
import net.minecraft.network.status.ServerStatusNetHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ServerHandshakeNetHandler implements IHandshakeNetHandler {
   private static final ITextComponent IGNORE_STATUS_REASON = new StringTextComponent("Ignoring status request");
   private final MinecraftServer server;
   private final NetworkManager connection;

   public ServerHandshakeNetHandler(MinecraftServer p_i45295_1_, NetworkManager p_i45295_2_) {
      this.server = p_i45295_1_;
      this.connection = p_i45295_2_;
   }

   /**
    * There are two recognized intentions for initiating a handshake: logging in and acquiring server status. The
    * NetworkManager's protocol will be reconfigured according to the specified intention, although a login-intention
    * must pass a versioncheck or receive a disconnect otherwise
    */
   public void handleIntention(CHandshakePacket pPacket) {
      if (!net.minecraftforge.fml.server.ServerLifecycleHooks.handleServerLogin(pPacket, this.connection)) return;
      switch(pPacket.getIntention()) {
      case LOGIN:
         this.connection.setProtocol(ProtocolType.LOGIN);
         if (pPacket.getProtocolVersion() != SharedConstants.getCurrentVersion().getProtocolVersion()) {
            ITextComponent itextcomponent;
            if (pPacket.getProtocolVersion() < 754) {
               itextcomponent = new TranslationTextComponent("multiplayer.disconnect.outdated_client", SharedConstants.getCurrentVersion().getName());
            } else {
               itextcomponent = new TranslationTextComponent("multiplayer.disconnect.incompatible", SharedConstants.getCurrentVersion().getName());
            }

            this.connection.send(new SDisconnectLoginPacket(itextcomponent));
            this.connection.disconnect(itextcomponent);
         } else {
            this.connection.setListener(new ServerLoginNetHandler(this.server, this.connection));
         }
         break;
      case STATUS:
         if (this.server.repliesToStatus()) {
            this.connection.setProtocol(ProtocolType.STATUS);
            this.connection.setListener(new ServerStatusNetHandler(this.server, this.connection));
         } else {
            this.connection.disconnect(IGNORE_STATUS_REASON);
         }
         break;
      default:
         throw new UnsupportedOperationException("Invalid intention " + pPacket.getIntention());
      }

   }

   /**
    * Invoked when disconnecting, the parameter is a ChatComponent describing the reason for termination
    */
   public void onDisconnect(ITextComponent pReason) {
   }

   /**
    * Returns this the NetworkManager instance registered with this NetworkHandlerPlayClient
    */
   public NetworkManager getConnection() {
      return this.connection;
   }
}
