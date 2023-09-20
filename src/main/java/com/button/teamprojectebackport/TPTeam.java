package com.button.teamprojectebackport;

import com.google.common.collect.Lists;
import moze_intel.projecte.api.ItemInfo;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraft.entity.player.PlayerEntity;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class TPTeam {


    private final UUID teamUUID;
    private UUID owner;
    private final List<UUID> members;

    private final Set<ItemInfo> knowledge;
    private BigInteger emc;
    private boolean fullKnowledge;

    public TPTeam(UUID teamUUID, UUID owner){
        this.teamUUID = teamUUID;
        this.owner = owner;
        this.members = new ArrayList<>();
        this.knowledge = new HashSet<>();
        this.emc = BigInteger.ZERO;
        this.fullKnowledge = false;
    }

    public TPTeam(UUID owner){
        this(UUID.randomUUID(), owner);
    }

    public TPTeam(CompoundNBT tag){
        this(tag.getUUID("uuid"), tag.getUUID("owner"));
        this.members.addAll(tag.getList("members", NBT.TAG_COMPOUND).stream().map(t -> ((CompoundNBT) t).getUUID("uuid")).collect(Collectors.toList()));

        this.knowledge.addAll(tag.getList("knowledge", NBT.TAG_COMPOUND).stream().map(t -> ItemInfo.read(((CompoundNBT)t))).filter(Objects::nonNull).collect(Collectors.toList()));
        this.emc = new BigInteger(tag.getString("emc"));
        this.fullKnowledge = tag.getBoolean("fullKnowledge");
    }

    public UUID getUUID() {
        return teamUUID;
    }

    public UUID getOwner(){
        return owner;
    }

    public void addMemberWithKnowledge(TPTeam originalTeam, PlayerEntity player){
        markDirty();
        addMember(TeamProjectEBackport.getPlayerUUID(player));
        if(originalTeam.getOwner().equals(TeamProjectEBackport.getPlayerUUID(player))){
            setEmc(getEmc().add(originalTeam.getEmc()));
            originalTeam.setEmc(BigInteger.ZERO);
            if(originalTeam.hasFullKnowledge()) {
                setFullKnowledge(true);
                originalTeam.setFullKnowledge(false);
            }
            knowledge.addAll(originalTeam.getKnowledge());
            originalTeam.clearKnowledge();
        }
    }

    public void addMember(UUID uuid){
        markDirty();
        TPSavedData.getData().invalidateCache(uuid);
        members.add(uuid);
    }

    public void removeMember(UUID uuid){
        markDirty();
        TPSavedData.getData().invalidateCache(uuid);
        if(owner.equals(uuid)){
            if(members.isEmpty()){
                TPSavedData.getData().TEAMS.remove(teamUUID);
                return;
            }
            UUID newOwner = members.get(ThreadLocalRandom.current().nextInt(members.size()));
            owner = newOwner;
            members.remove(newOwner);
        }
        else
            members.remove(uuid);
    }

    public void transferOwner(UUID newOwner){
        if(owner.equals(newOwner) || !members.contains(newOwner))
            return;
        members.add(owner);
        owner = newOwner;
    }

    public List<UUID> getMembers(){
        return new ArrayList<UUID>(members);
    }

    public List<UUID> getAll(){
        return Lists.asList(owner, (UUID[]) members.toArray());
    }


    public boolean addKnowledge(ItemInfo info){
        markDirty();
        return knowledge.add(info);
    }

    public void removeKnowledge(ItemInfo info){
        markDirty();
        knowledge.remove(info);
    }

    public void clearKnowledge(){
        markDirty();
        knowledge.clear();
    }


    public Set<ItemInfo> getKnowledge() {
        return new HashSet<>(knowledge);
    }

    public void setEmc(BigInteger emc) {
        markDirty();
        this.emc = emc;
    }

    public BigInteger getEmc() {
        return emc;
    }

    public void setFullKnowledge(boolean fullKnowledge) {
        markDirty();
        this.fullKnowledge = fullKnowledge;
    }

    public boolean hasFullKnowledge() {
        return fullKnowledge;
    }

    public void markDirty(){
        TPSavedData.getData().setDirty();
    }

    public CompoundNBT save(){
        CompoundNBT tag = new CompoundNBT();
        tag.putUUID("uuid", teamUUID);
        tag.putUUID("owner", owner);
        ListNBT list = new ListNBT();
        for (UUID member : members) {
            CompoundNBT t = new CompoundNBT();
            t.putUUID("uuid", member);
            list.add(t);
        }
        tag.put("members", list);

        ListNBT itemInfos = new ListNBT();
        for (ItemInfo info : knowledge)
            itemInfos.add(info.write(new CompoundNBT()));
        tag.put("knowledge", itemInfos);
        tag.putString("emc", emc.toString());
        tag.putBoolean("fullKnowledge", fullKnowledge);

        return tag;
    }


    public static TPTeam getOrCreateTeam(UUID uuid){
        TPTeam team = getTeamByMember(uuid);
        if(team == null)
            team = createTeam(uuid);
        return team;
    }

    public static TPTeam createTeam(UUID uuid){
        TPTeam team = new TPTeam(uuid);
        TPSavedData.getData().TEAMS.put(team.getUUID(), team);
        TPSavedData.getData().setDirty();
        return team;
    }

    public static TPTeam getTeam(UUID uuid) {
        return TPSavedData.getData().TEAMS.get(uuid);
    }

    public static boolean isInTeam(UUID uuid){
        return getTeamByMember(uuid) != null;
    }

    public static TPTeam getTeamByMember(UUID uuid){
        UUID teamUUID = TPSavedData.getData().PLAYER_TEAM_CACHE.get(uuid);

        if(teamUUID == null)
            for (Map.Entry<UUID, TPTeam> entry : TPSavedData.getData().TEAMS.entrySet()) {
                if(entry.getValue().getAll().contains(uuid)) {
                    teamUUID = entry.getKey();
                    TPSavedData.getData().PLAYER_TEAM_CACHE.put(uuid, teamUUID);
                    break;
                }
            }

        if(teamUUID != null)
            return TPSavedData.getData().TEAMS.get(teamUUID);
        return null;
    }
}