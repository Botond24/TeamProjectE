package net.minecraft.network;

import net.minecraft.util.text.ITextComponent;

/**
 * Describes how packets are handled. There are various implementations of this class for each possible protocol (e.g.
 * PLAY, CLIENTBOUND; PLAY, SERVERBOUND; etc.)
 */
public interface INetHandler {
   /**
    * Invoked when disconnecting, the parameter is a ChatComponent describing the reason for termination
    */
   void onDisconnect(ITextComponent pReason);

   /**
    * Returns this the NetworkManager instance registered with this NetworkHandlerPlayClient
    */
   NetworkManager getConnection();
}