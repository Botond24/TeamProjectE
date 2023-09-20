package net.minecraft.network.play;

import net.minecraft.network.INetHandler;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CClientSettingsPacket;
import net.minecraft.network.play.client.CClientStatusPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CConfirmTeleportPacket;
import net.minecraft.network.play.client.CConfirmTransactionPacket;
import net.minecraft.network.play.client.CCreativeInventoryActionPacket;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.client.CEditBookPacket;
import net.minecraft.network.play.client.CEnchantItemPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CInputPacket;
import net.minecraft.network.play.client.CJigsawBlockGeneratePacket;
import net.minecraft.network.play.client.CKeepAlivePacket;
import net.minecraft.network.play.client.CLockDifficultyPacket;
import net.minecraft.network.play.client.CMarkRecipeSeenPacket;
import net.minecraft.network.play.client.CMoveVehiclePacket;
import net.minecraft.network.play.client.CPickItemPacket;
import net.minecraft.network.play.client.CPlaceRecipePacket;
import net.minecraft.network.play.client.CPlayerAbilitiesPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.client.CQueryEntityNBTPacket;
import net.minecraft.network.play.client.CQueryTileEntityNBTPacket;
import net.minecraft.network.play.client.CRenameItemPacket;
import net.minecraft.network.play.client.CResourcePackStatusPacket;
import net.minecraft.network.play.client.CSeenAdvancementsPacket;
import net.minecraft.network.play.client.CSelectTradePacket;
import net.minecraft.network.play.client.CSetDifficultyPacket;
import net.minecraft.network.play.client.CSpectatePacket;
import net.minecraft.network.play.client.CSteerBoatPacket;
import net.minecraft.network.play.client.CTabCompletePacket;
import net.minecraft.network.play.client.CUpdateBeaconPacket;
import net.minecraft.network.play.client.CUpdateCommandBlockPacket;
import net.minecraft.network.play.client.CUpdateJigsawBlockPacket;
import net.minecraft.network.play.client.CUpdateMinecartCommandBlockPacket;
import net.minecraft.network.play.client.CUpdateRecipeBookStatusPacket;
import net.minecraft.network.play.client.CUpdateSignPacket;
import net.minecraft.network.play.client.CUpdateStructureBlockPacket;
import net.minecraft.network.play.client.CUseEntityPacket;

/**
 * PacketListener for the server side of the PLAY protocol.
 */
public interface IServerPlayNetHandler extends INetHandler {
   void handleAnimate(CAnimateHandPacket pPacket);

   /**
    * Process chat messages (broadcast back to clients) and commands (executes)
    */
   void handleChat(CChatMessagePacket pPacket);

   /**
    * Processes the client status updates: respawn attempt from player, opening statistics or achievements, or acquiring
    * 'open inventory' achievement
    */
   void handleClientCommand(CClientStatusPacket pPacket);

   /**
    * Updates serverside copy of client settings: language, render distance, chat visibility, chat colours, difficulty,
    * and whether to show the cape
    */
   void handleClientInformation(CClientSettingsPacket pPacket);

   void handleContainerAck(CConfirmTransactionPacket p_147339_1_);

   /**
    * Enchants the item identified by the packet given some convoluted conditions (matching window, which
    * should/shouldn't be in use?)
    */
   void handleContainerButtonClick(CEnchantItemPacket pPacket);

   /**
    * Executes a container/inventory slot manipulation as indicated by the packet. Sends the serverside result if they
    * didn't match the indicated result and prevents further manipulation by the player until he confirms that it has
    * the same open container/inventory
    */
   void handleContainerClick(CClickWindowPacket pPacket);

   void handlePlaceRecipe(CPlaceRecipePacket pPacket);

   /**
    * Processes the client closing windows (container)
    */
   void handleContainerClose(CCloseWindowPacket pPacket);

   /**
    * Synchronizes serverside and clientside book contents and signing
    */
   void handleCustomPayload(CCustomPayloadPacket pPacket);

   /**
    * Processes left and right clicks on entities
    */
   void handleInteract(CUseEntityPacket pPacket);

   /**
    * Updates a players' ping statistics
    */
   void handleKeepAlive(CKeepAlivePacket pPacket);

   /**
    * Processes clients perspective on player positioning and/or orientation
    */
   void handleMovePlayer(CPlayerPacket pPacket);

   /**
    * Processes a player starting/stopping flying
    */
   void handlePlayerAbilities(CPlayerAbilitiesPacket pPacket);

   /**
    * Processes the player initiating/stopping digging on a particular spot, as well as a player dropping items
    */
   void handlePlayerAction(CPlayerDiggingPacket pPacket);

   /**
    * Processes a range of action-types: sneaking, sprinting, waking from sleep, opening the inventory or setting jump
    * height of the horse the player is riding
    */
   void handlePlayerCommand(CEntityActionPacket pPacket);

   /**
    * Processes player movement input. Includes walking, strafing, jumping, sneaking" excludes riding and toggling
    * flying/sprinting
    */
   void handlePlayerInput(CInputPacket pPacket);

   /**
    * Updates which quickbar slot is selected
    */
   void handleSetCarriedItem(CHeldItemChangePacket pPacket);

   /**
    * Update the server with an ItemStack in a slot.
    */
   void handleSetCreativeModeSlot(CCreativeInventoryActionPacket pPacket);

   void handleSignUpdate(CUpdateSignPacket pPacket);

   void handleUseItemOn(CPlayerTryUseItemOnBlockPacket pPacket);

   /**
    * Called when a client is using an item while not pointing at a block, but simply using an item
    */
   void handleUseItem(CPlayerTryUseItemPacket pPacket);

   void handleTeleportToEntityPacket(CSpectatePacket pPacket);

   void handleResourcePackResponse(CResourcePackStatusPacket pPacket);

   void handlePaddleBoat(CSteerBoatPacket pPacket);

   void handleMoveVehicle(CMoveVehiclePacket pPacket);

   void handleAcceptTeleportPacket(CConfirmTeleportPacket pPacket);

   void handleRecipeBookSeenRecipePacket(CMarkRecipeSeenPacket pPacket);

   void handleRecipeBookChangeSettingsPacket(CUpdateRecipeBookStatusPacket pPacket);

   void handleSeenAdvancements(CSeenAdvancementsPacket pPacket);

   /**
    * This method is only called for manual tab-completion (the {@link
    * net.minecraft.command.arguments.SuggestionProviders#ASK_SERVER minecraft:ask_server} suggestion provider).
    */
   void handleCustomCommandSuggestions(CTabCompletePacket pPacket);

   void handleSetCommandBlock(CUpdateCommandBlockPacket pPacket);

   void handleSetCommandMinecart(CUpdateMinecartCommandBlockPacket pPacket);

   void handlePickItem(CPickItemPacket pPacket);

   void handleRenameItem(CRenameItemPacket pPacket);

   void handleSetBeaconPacket(CUpdateBeaconPacket pPacket);

   void handleSetStructureBlock(CUpdateStructureBlockPacket pPacket);

   void handleSelectTrade(CSelectTradePacket pPacket);

   void handleEditBook(CEditBookPacket pPacket);

   void handleEntityTagQuery(CQueryEntityNBTPacket pPacket);

   void handleBlockEntityTagQuery(CQueryTileEntityNBTPacket pPacket);

   void handleSetJigsawBlock(CUpdateJigsawBlockPacket pPacket);

   void handleJigsawGenerate(CJigsawBlockGeneratePacket pPacket);

   void handleChangeDifficulty(CSetDifficultyPacket pPacket);

   void handleLockDifficulty(CLockDifficultyPacket pPacket);
}