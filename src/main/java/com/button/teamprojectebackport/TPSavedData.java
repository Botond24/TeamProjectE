package com.button.teamprojectebackport;


import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TPSavedData extends WorldSavedData {

    private static TPSavedData DATA;

    public static TPSavedData getData(){
        if(DATA == null && ServerLifecycleHooks.getCurrentServer() != null)
            DATA = ServerLifecycleHooks.getCurrentServer().overworld().getDataStorage()
                    .computeIfAbsent(TPSavedData::new, "teamprojectebackport");
        return DATA;
    }

    public static void onServerStopped(){
        DATA = null;
    }

    public Map<UUID, TPTeam> TEAMS = new HashMap<>();
    public final Map<UUID, UUID> PLAYER_TEAM_CACHE = new HashMap<>();

    public void invalidateCache(UUID uuid){
        PLAYER_TEAM_CACHE.remove(uuid);
    }

    public TPSavedData() {
        super("teamprojectebackport");
    }
    public TPSavedData(String name) {
        super(name);
    }

    @Override
    public void load(CompoundNBT p_76184_1_) {}

    public TPSavedData(CompoundNBT tag) {
        super("teamprojectebackport");
        //TeamProjectEBackport.LOGGER.info(tag.toString());
        for (INBT t : tag.getList("teams", NBT.TAG_COMPOUND)) {
            CompoundNBT team = (CompoundNBT) t;
            TEAMS.put(team.getUUID("uuid"), new TPTeam(team.getCompound("team")));
        }
    }

    public static TPSavedData create(){
        return new TPSavedData();
    }

    @Override
    public @Nonnull
    CompoundNBT save(CompoundNBT NBT) {
        ListNBT teams = new ListNBT();
        TEAMS.forEach((uuid, team) -> {
            CompoundNBT t = new CompoundNBT();
            t.putUUID("uuid", uuid);
            t.put("team", team.save());
            teams.add(t);
        });
        NBT.put("teams", teams);
        return NBT;
    }
}
