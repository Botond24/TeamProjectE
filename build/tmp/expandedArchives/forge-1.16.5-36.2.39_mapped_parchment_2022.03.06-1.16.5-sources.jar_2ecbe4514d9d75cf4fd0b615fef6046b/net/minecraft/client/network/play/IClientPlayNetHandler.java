package net.minecraft.client.network.play;

import net.minecraft.network.INetHandler;
import net.minecraft.network.play.server.SAdvancementInfoPacket;
import net.minecraft.network.play.server.SAnimateBlockBreakPacket;
import net.minecraft.network.play.server.SAnimateHandPacket;
import net.minecraft.network.play.server.SBlockActionPacket;
import net.minecraft.network.play.server.SCameraPacket;
import net.minecraft.network.play.server.SChangeBlockPacket;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.network.play.server.SChunkDataPacket;
import net.minecraft.network.play.server.SCloseWindowPacket;
import net.minecraft.network.play.server.SCollectItemPacket;
import net.minecraft.network.play.server.SCombatPacket;
import net.minecraft.network.play.server.SCommandListPacket;
import net.minecraft.network.play.server.SConfirmTransactionPacket;
import net.minecraft.network.play.server.SCooldownPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.network.play.server.SDestroyEntitiesPacket;
import net.minecraft.network.play.server.SDisconnectPacket;
import net.minecraft.network.play.server.SDisplayObjectivePacket;
import net.minecraft.network.play.server.SEntityEquipmentPacket;
import net.minecraft.network.play.server.SEntityHeadLookPacket;
import net.minecraft.network.play.server.SEntityMetadataPacket;
import net.minecraft.network.play.server.SEntityPacket;
import net.minecraft.network.play.server.SEntityPropertiesPacket;
import net.minecraft.network.play.server.SEntityStatusPacket;
import net.minecraft.network.play.server.SEntityTeleportPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.network.play.server.SExplosionPacket;
import net.minecraft.network.play.server.SHeldItemChangePacket;
import net.minecraft.network.play.server.SJoinGamePacket;
import net.minecraft.network.play.server.SKeepAlivePacket;
import net.minecraft.network.play.server.SMapDataPacket;
import net.minecraft.network.play.server.SMerchantOffersPacket;
import net.minecraft.network.play.server.SMountEntityPacket;
import net.minecraft.network.play.server.SMoveVehiclePacket;
import net.minecraft.network.play.server.SMultiBlockChangePacket;
import net.minecraft.network.play.server.SOpenBookWindowPacket;
import net.minecraft.network.play.server.SOpenHorseWindowPacket;
import net.minecraft.network.play.server.SOpenSignMenuPacket;
import net.minecraft.network.play.server.SOpenWindowPacket;
import net.minecraft.network.play.server.SPlaceGhostRecipePacket;
import net.minecraft.network.play.server.SPlayEntityEffectPacket;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.network.play.server.SPlaySoundEventPacket;
import net.minecraft.network.play.server.SPlaySoundPacket;
import net.minecraft.network.play.server.SPlayerAbilitiesPacket;
import net.minecraft.network.play.server.SPlayerDiggingPacket;
import net.minecraft.network.play.server.SPlayerListHeaderFooterPacket;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.network.play.server.SPlayerLookPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.network.play.server.SQueryNBTResponsePacket;
import net.minecraft.network.play.server.SRecipeBookPacket;
import net.minecraft.network.play.server.SRemoveEntityEffectPacket;
import net.minecraft.network.play.server.SRespawnPacket;
import net.minecraft.network.play.server.SScoreboardObjectivePacket;
import net.minecraft.network.play.server.SSelectAdvancementsTabPacket;
import net.minecraft.network.play.server.SSendResourcePackPacket;
import net.minecraft.network.play.server.SServerDifficultyPacket;
import net.minecraft.network.play.server.SSetExperiencePacket;
import net.minecraft.network.play.server.SSetPassengersPacket;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.network.play.server.SSpawnExperienceOrbPacket;
import net.minecraft.network.play.server.SSpawnMobPacket;
import net.minecraft.network.play.server.SSpawnMovingSoundEffectPacket;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.network.play.server.SSpawnPaintingPacket;
import net.minecraft.network.play.server.SSpawnParticlePacket;
import net.minecraft.network.play.server.SSpawnPlayerPacket;
import net.minecraft.network.play.server.SStatisticsPacket;
import net.minecraft.network.play.server.SStopSoundPacket;
import net.minecraft.network.play.server.STabCompletePacket;
import net.minecraft.network.play.server.STagsListPacket;
import net.minecraft.network.play.server.STeamsPacket;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.network.play.server.SUnloadChunkPacket;
import net.minecraft.network.play.server.SUpdateBossInfoPacket;
import net.minecraft.network.play.server.SUpdateChunkPositionPacket;
import net.minecraft.network.play.server.SUpdateHealthPacket;
import net.minecraft.network.play.server.SUpdateLightPacket;
import net.minecraft.network.play.server.SUpdateRecipesPacket;
import net.minecraft.network.play.server.SUpdateScorePacket;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.network.play.server.SUpdateTimePacket;
import net.minecraft.network.play.server.SUpdateViewDistancePacket;
import net.minecraft.network.play.server.SWindowItemsPacket;
import net.minecraft.network.play.server.SWindowPropertyPacket;
import net.minecraft.network.play.server.SWorldBorderPacket;
import net.minecraft.network.play.server.SWorldSpawnChangedPacket;

/**
 * PacketListener for the client side of the PLAY protocol.
 */
public interface IClientPlayNetHandler extends INetHandler {
   /**
    * Spawns an instance of the objecttype indicated by the packet and sets its position and momentum
    */
   void handleAddEntity(SSpawnObjectPacket pPacket);

   /**
    * Spawns an experience orb and sets its value (amount of XP)
    */
   void handleAddExperienceOrb(SSpawnExperienceOrbPacket pPacket);

   /**
    * Spawns the mob entity at the specified location, with the specified rotation, momentum and type. Updates the
    * entities Datawatchers with the entity metadata specified in the packet
    */
   void handleAddMob(SSpawnMobPacket pPacket);

   /**
    * May create a scoreboard objective, remove an objective from the scoreboard or update an objectives' displayname
    */
   void handleAddObjective(SScoreboardObjectivePacket pPacket);

   /**
    * Handles the spawning of a painting object
    */
   void handleAddPainting(SSpawnPaintingPacket pPacket);

   /**
    * Handles the creation of a nearby player entity, sets the position and held item
    */
   void handleAddPlayer(SSpawnPlayerPacket pPacket);

   /**
    * Renders a specified animation: Waking up a player, a living entity swinging its currently held item, being hurt or
    * receiving a critical hit by normal or magical means
    */
   void handleAnimate(SAnimateHandPacket pPacket);

   /**
    * Updates the players statistics or achievements
    */
   void handleAwardStats(SStatisticsPacket pPacket);

   void handleAddOrRemoveRecipes(SRecipeBookPacket pPacket);

   /**
    * Updates all registered IWorldAccess instances with destroyBlockInWorldPartially
    */
   void handleBlockDestruction(SAnimateBlockBreakPacket pPacket);

   /**
    * Creates a sign in the specified location if it didn't exist and opens the GUI to edit its text
    */
   void handleOpenSignEditor(SOpenSignMenuPacket pPacket);

   /**
    * Updates the NBTTagCompound metadata of instances of the following entitytypes: Mob spawners, command blocks,
    * beacons, skulls, flowerpot
    */
   void handleBlockEntityData(SUpdateTileEntityPacket pPacket);

   /**
    * Triggers Block.onBlockEventReceived, which is implemented in BlockPistonBase for extension/retraction, BlockNote
    * for setting the instrument (including audiovisual feedback) and in BlockContainer to set the number of players
    * accessing a (Ender)Chest
    */
   void handleBlockEvent(SBlockActionPacket pPacket);

   /**
    * Updates the block and metadata and generates a blockupdate (and notify the clients)
    */
   void handleBlockUpdate(SChangeBlockPacket pPacket);

   /**
    * Prints a chatmessage in the chat GUI
    */
   void handleChat(SChatPacket pPacket);

   /**
    * Received from the servers PlayerManager if between 1 and 64 blocks in a chunk are changed. If only one block
    * requires an update, the server sends S23PacketBlockChange and if 64 or more blocks are changed, the server sends
    * S21PacketChunkData
    */
   void handleChunkBlocksUpdate(SMultiBlockChangePacket pPacket);

   /**
    * Updates the worlds MapStorage with the specified MapData for the specified map-identifier and invokes a
    * MapItemRenderer for it
    */
   void handleMapItemData(SMapDataPacket pPacket);

   void handleContainerAck(SConfirmTransactionPacket p_147239_1_);

   /**
    * Resets the ItemStack held in hand and closes the window that is opened
    */
   void handleContainerClose(SCloseWindowPacket pPacket);

   /**
    * Handles the placement of a specified ItemStack in a specified container/inventory slot
    */
   void handleContainerContent(SWindowItemsPacket pPacket);

   void handleHorseScreenOpen(SOpenHorseWindowPacket pPacket);

   /**
    * Sets the progressbar of the opened window to the specified value
    */
   void handleContainerSetData(SWindowPropertyPacket pPacket);

   /**
    * Handles pickin up an ItemStack or dropping one in your inventory or an open (non-creative) container
    */
   void handleContainerSetSlot(SSetSlotPacket pPacket);

   /**
    * Handles packets that have room for a channel specification. Vanilla implemented channels are "MC|TrList" to
    * acquire a MerchantRecipeList trades for a villager merchant, "MC|Brand" which sets the server brand? on the player
    * instance and finally "MC|RPack" which the server uses to communicate the identifier of the default server
    * resourcepack for the client to load.
    */
   void handleCustomPayload(SCustomPayloadPlayPacket pPacket);

   /**
    * Closes the network channel
    */
   void handleDisconnect(SDisconnectPacket pPacket);

   /**
    * Invokes the entities' handleUpdateHealth method which is implemented in LivingBase (hurt/death),
    * MinecartMobSpawner (spawn delay), FireworkRocket & MinecartTNT (explosion), IronGolem (throwing,...), Witch (spawn
    * particles), Zombie (villager transformation), Animal (breeding mode particles), Horse (breeding/smoke particles),
    * Sheep (...), Tameable (...), Villager (particles for breeding mode, angry and happy), Wolf (...)
    */
   void handleEntityEvent(SEntityStatusPacket pPacket);

   void handleEntityLinkPacket(SMountEntityPacket pPacket);

   void handleSetEntityPassengersPacket(SSetPassengersPacket pPacket);

   /**
    * Initiates a new explosion (sound, particles, drop spawn) for the affected blocks indicated by the packet.
    */
   void handleExplosion(SExplosionPacket pPacket);

   void handleGameEvent(SChangeGameStatePacket pPacket);

   void handleKeepAlive(SKeepAlivePacket pPacket);

   /**
    * Updates the specified chunk with the supplied data, marks it for re-rendering and lighting recalculation
    */
   void handleLevelChunk(SChunkDataPacket pPacket);

   void handleForgetLevelChunk(SUnloadChunkPacket pPacket);

   void handleLevelEvent(SPlaySoundEventPacket pPacket);

   /**
    * Registers some server properties (gametype,hardcore-mode,terraintype,difficulty,player limit), creates a new
    * WorldClient and sets the player initial dimension
    */
   void handleLogin(SJoinGamePacket pPacket);

   /**
    * Updates the specified entity's position by the specified relative moment and absolute rotation. Note that
    * subclassing of the packet allows for the specification of a subset of this data (e.g. only rel. position, abs.
    * rotation or both).
    */
   void handleMoveEntity(SEntityPacket pPacket);

   void handleMovePlayer(SPlayerPositionLookPacket pPacket);

   /**
    * Spawns a specified number of particles at the specified location with a randomized displacement according to
    * specified bounds
    */
   void handleParticleEvent(SSpawnParticlePacket pPacket);

   void handlePlayerAbilities(SPlayerAbilitiesPacket pPacket);

   void handlePlayerInfo(SPlayerListItemPacket pPacket);

   void handleRemoveEntity(SDestroyEntitiesPacket p_147238_1_);

   void handleRemoveMobEffect(SRemoveEntityEffectPacket pPacket);

   void handleRespawn(SRespawnPacket pPacket);

   /**
    * Updates the direction in which the specified entity is looking, normally this head rotation is independent of the
    * rotation of the entity itself
    */
   void handleRotateMob(SEntityHeadLookPacket pPacket);

   /**
    * Updates which hotbar slot of the player is currently selected
    */
   void handleSetCarriedItem(SHeldItemChangePacket pPacket);

   /**
    * Removes or sets the ScoreObjective to be displayed at a particular scoreboard position (list, sidebar, below name)
    */
   void handleSetDisplayObjective(SDisplayObjectivePacket pPacket);

   /**
    * Invoked when the server registers new proximate objects in your watchlist or when objects in your watchlist have
    * changed -> Registers any changes locally
    */
   void handleSetEntityData(SEntityMetadataPacket pPacket);

   /**
    * Sets the velocity of the specified entity to the specified value
    */
   void handleSetEntityMotion(SEntityVelocityPacket pPacket);

   void handleSetEquipment(SEntityEquipmentPacket pPacket);

   void handleSetExperience(SSetExperiencePacket pPacket);

   void handleSetHealth(SUpdateHealthPacket pPacket);

   /**
    * Updates a team managed by the scoreboard: Create/Remove the team registration, Register/Remove the player-team-
    * memberships, Set team displayname/prefix/suffix and/or whether friendly fire is enabled
    */
   void handleSetPlayerTeamPacket(STeamsPacket pPacket);

   /**
    * Either updates the score with a specified value or removes the score for an objective
    */
   void handleSetScore(SUpdateScorePacket pPacket);

   void handleSetSpawn(SWorldSpawnChangedPacket pPacket);

   void handleSetTime(SUpdateTimePacket pPacket);

   void handleSoundEvent(SPlaySoundEffectPacket pPacket);

   void handleSoundEntityEvent(SSpawnMovingSoundEffectPacket pPacket);

   void handleCustomSoundEvent(SPlaySoundPacket pPacket);

   void handleTakeItemEntity(SCollectItemPacket pPacket);

   /**
    * Updates an entity's position and rotation as specified by the packet
    */
   void handleTeleportEntity(SEntityTeleportPacket pPacket);

   /**
    * Updates en entity's attributes and their respective modifiers, which are used for speed bonusses (player
    * sprinting, animals fleeing, baby speed), weapon/tool attackDamage, hostiles followRange randomization, zombie
    * maxHealth and knockback resistance as well as reinforcement spawning chance.
    */
   void handleUpdateAttributes(SEntityPropertiesPacket pPacket);

   void handleUpdateMobEffect(SPlayEntityEffectPacket pPacket);

   void handleUpdateTags(STagsListPacket pPacket);

   void handlePlayerCombat(SCombatPacket p_175098_1_);

   void handleChangeDifficulty(SServerDifficultyPacket pPacket);

   void handleSetCamera(SCameraPacket pPacket);

   void handleSetBorder(SWorldBorderPacket p_175093_1_);

   void handleSetTitles(STitlePacket p_175099_1_);

   void handleTabListCustomisation(SPlayerListHeaderFooterPacket pPacket);

   void handleResourcePack(SSendResourcePackPacket pPacket);

   void handleBossUpdate(SUpdateBossInfoPacket pPacket);

   void handleItemCooldown(SCooldownPacket pPacket);

   void handleMoveVehicle(SMoveVehiclePacket pPacket);

   void handleUpdateAdvancementsPacket(SAdvancementInfoPacket pPacket);

   void handleSelectAdvancementsTab(SSelectAdvancementsTabPacket pPacket);

   void handlePlaceRecipe(SPlaceGhostRecipePacket pPacket);

   void handleCommands(SCommandListPacket pPacket);

   void handleStopSoundEvent(SStopSoundPacket pPacket);

   /**
    * This method is only called for manual tab-completion (the {@link
    * net.minecraft.command.arguments.SuggestionProviders#ASK_SERVER minecraft:ask_server} suggestion provider).
    */
   void handleCommandSuggestions(STabCompletePacket pPacket);

   void handleUpdateRecipes(SUpdateRecipesPacket pPacket);

   void handleLookAt(SPlayerLookPacket pPacket);

   void handleTagQueryPacket(SQueryNBTResponsePacket pPacket);

   void handleLightUpdatePacked(SUpdateLightPacket pPacket);

   void handleOpenBook(SOpenBookWindowPacket pPacket);

   void handleOpenScreen(SOpenWindowPacket pPacket);

   void handleMerchantOffers(SMerchantOffersPacket pPacket);

   void handleSetChunkCacheRadius(SUpdateViewDistancePacket pPacket);

   void handleSetChunkCacheCenter(SUpdateChunkPositionPacket pPacket);

   void handleBlockBreakAck(SPlayerDiggingPacket pPacket);
}