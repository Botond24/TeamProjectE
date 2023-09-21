package com.button.teamprojectebackport;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import net.minecraft.world.World;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import moze_intel.projecte.api.ProjectEAPI;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Mod("teamprojectebackport")
public class TeamProjectEBackport {

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public void TeamProjectEBackport() {
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommand(RegisterCommandsEvent event) {
        TPCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStopped(FMLServerStoppedEvent event) {
        TPCommand.INVITATIONS.clear();
        TPSavedData.onServerStopped();
    }

    @SubscribeEvent
    public void onPlayerJoin(EntityJoinWorldEvent event) {
        if (event.getWorld().dimension() == World.OVERWORLD && event.getEntity().getClass().equals(ServerPlayerEntity.class))
            sync((ServerPlayerEntity) event.getEntity());
    }

    public static List<ServerPlayerEntity> getAllOnline(List<UUID> uuids) {
        return uuids.stream()
                .map(uuid -> ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static void sync(ServerPlayerEntity player) {
        player.getCapability(ProjectEAPI.KNOWLEDGE_CAPABILITY).ifPresent(provider -> provider.sync(player));
    }

    public static List<ServerPlayerEntity> getOnlineTeamMembers(UUID uuid) {
        return getOnlineTeamMembers(uuid, true);
    }

    public static List<ServerPlayerEntity> getOnlineTeamMembers(UUID uuid, boolean includeOwner) {
        TPTeam team = TPTeam.getOrCreateTeam(uuid);
        return TeamProjectEBackport.getAllOnline(includeOwner ? team.getAll() : team.getMembers());
    }

    public static UUID getPlayerUUID(PlayerEntity player) {
        UUID uuid = player.getGameProfile().getId();
        if (uuid == null)
            uuid = player.getUUID();
        return uuid;
    }
}
