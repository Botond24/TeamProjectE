package com.button.teamprojectebackport;

import com.google.common.base.Suppliers;
import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.event.PlayerKnowledgeChangeEvent;
import moze_intel.projecte.emc.EMCMappingHandler;
import moze_intel.projecte.emc.nbt.NBTManager;
import moze_intel.projecte.gameObjs.items.Tome;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncChangePKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncEmcPKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncInputsAndLocksPKT;
import moze_intel.projecte.network.packets.to_client.knowledge.KnowledgeSyncPKT;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import javax.annotation.Nullable;
import javax.annotation.Nonnull;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Supplier;

public class TeamKnowledgeProvider implements IKnowledgeProvider {

    private final Supplier<UUID> playerUUID;

    private final ItemStackHandler inputLocks = new ItemStackHandler(9);

    public TeamKnowledgeProvider(@Nonnull ServerPlayerEntity player) {
        this.playerUUID = Suppliers.memoize(() -> TeamProjectEBackport.getPlayerUUID(player));
    }

    public TeamKnowledgeProvider(UUID uuid) {
        this.playerUUID = () -> uuid;
    }

    private void fireChangedEvent() {
        getTeam().getAll()
                .forEach(uuid -> MinecraftForge.EVENT_BUS.post(new PlayerKnowledgeChangeEvent(uuid)));
    }

    private TPTeam getTeam(){
        return TPTeam.getOrCreateTeam(playerUUID.get());
    }

    @Override
    public boolean hasFullKnowledge() {
        return getTeam().hasFullKnowledge();
    }

    @Override
    public void setFullKnowledge(boolean fullKnowledge) {
        boolean changed = hasFullKnowledge() != fullKnowledge;
        getTeam().setFullKnowledge(fullKnowledge);
        if (changed) {
            fireChangedEvent();
            //sync(playerUUID);
        }
    }

    @Override
    public void clearKnowledge() {
        boolean hasKnowledge = hasFullKnowledge() || !getTeam().getKnowledge().isEmpty();
        getTeam().clearKnowledge();
        getTeam().setFullKnowledge(false);
        if (hasKnowledge) {
            //If we previously had any knowledge fire the fact that our knowledge changed
            fireChangedEvent();
            //sync(playerUUID);
        }
    }

    @Nullable
    private ItemInfo getIfPersistent(@Nonnull ItemInfo info) {
        if (!info.hasNBT() || EMCMappingHandler.hasEmcValue(info)) {
            //If we have no NBT or the base mapping has an emc value for our item with the given NBT
            // then we don't have an extended state
            return null;
        }
        ItemInfo cleanedInfo = NBTManager.getPersistentInfo(info);
        if (cleanedInfo.hasNBT() && !EMCMappingHandler.hasEmcValue(cleanedInfo)) {
            //If we still have NBT after unimportant parts being stripped and it doesn't
            // directly have an EMC value, then we it has some persistent information
            return cleanedInfo;
        }
        return null;
    }

    @Override
    public boolean hasKnowledge(@Nonnull ItemInfo info) {
        if (getTeam().hasFullKnowledge()) {
            //If we have all knowledge, check if the item has extra data and
            // may not actually be in our knowledge set but can be added to it
            ItemInfo persistentInfo = getIfPersistent(info);
            return persistentInfo == null || getTeam().getKnowledge().contains(persistentInfo);
        }
        return getTeam().getKnowledge().contains(NBTManager.getPersistentInfo(info));
    }

    @Override
    public boolean addKnowledge(@Nonnull ItemInfo info) {
        if (getTeam().hasFullKnowledge()) {
            ItemInfo persistentInfo = getIfPersistent(info);
            if (persistentInfo == null) {
                //If the item doesn't have extra data, and we have all knowledge, don't actually add any
                return false;
            }
            //If it does have extra data, pretend we don't have full knowledge and try adding it as what we have is persistent.
            // Note: We ignore the tome here being a separate entity because it should not have any persistent info
            return tryAdd(persistentInfo);
        }
        if (info.getItem() instanceof Tome) {
            if (info.hasNBT()) {
                //Make sure we don't have any NBT as it doesn't have any effect for the tome
                info = ItemInfo.fromItem(info.getItem());
            }
            //Note: We don't bother checking if we already somehow know the tome without having full knowledge
            // as we are learning it without any NBT which means that it doesn't have any extra persistent info
            // so can just check if it is already in it by nature of it being a set
            getTeam().addKnowledge(info);
            getTeam().setFullKnowledge(true);
            fireChangedEvent();
            sync(playerUUID.get());
            return true;
        }
        return tryAdd(NBTManager.getPersistentInfo(info));
    }

    private boolean tryAdd(@Nonnull ItemInfo cleanedInfo) {
        if (getTeam().addKnowledge(cleanedInfo)) {
            fireChangedEvent();
            //syncKnowledgeChange(playerUUID, cleanedInfo, true);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeKnowledge(@Nonnull ItemInfo info) {
        if (getTeam().hasFullKnowledge()) {
            if (info.getItem() instanceof Tome) {
                //If we have full knowledge and are trying to remove the tome allow it
                if (info.hasNBT()) {
                    //Make sure we don't have any NBT as it doesn't have any effect for the tome
                    info = ItemInfo.fromItem(info.getItem());
                }
                getTeam().removeKnowledge(info);
                getTeam().setFullKnowledge(false);
                fireChangedEvent();
                //sync(playerUUID);
                return true;
            }
            //Otherwise check if we have any persistent information, and if so try removing that
            // as we may have it known as an "extra" item
            ItemInfo persistentInfo = getIfPersistent(info);
            return persistentInfo != null && tryRemove(persistentInfo);
        }
        return tryRemove(NBTManager.getPersistentInfo(info));
    }

    private boolean tryRemove(@Nonnull ItemInfo cleanedInfo) {
        if (getTeam().getKnowledge().remove(cleanedInfo)) {
            fireChangedEvent();
            //syncKnowledgeChange(playerUUID, cleanedInfo, false);
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public Set<ItemInfo> getKnowledge() {
        if (getTeam().hasFullKnowledge()) {
            Set<ItemInfo> allKnowledge = EMCMappingHandler.getMappedItems();
            //Make sure we include any extra items they have learned such as various enchanted items.
            allKnowledge.addAll(getTeam().getKnowledge());
            return Collections.unmodifiableSet(allKnowledge);
        }
        return Collections.unmodifiableSet(getTeam().getKnowledge());
    }

    @Nonnull
    @Override
    public IItemHandlerModifiable getInputAndLocks() {
        return inputLocks;
    }

    @Override
    public BigInteger getEmc() {
        return getTeam().getEmc();
    }

    @Override
    public void setEmc(BigInteger emc) {
        getTeam().setEmc(emc);
        syncEmc(playerUUID.get());
    }

    @Override
    public void sync(@Nonnull ServerPlayerEntity player) {
        sync(TeamProjectEBackport.getPlayerUUID(player));
    }

    public void sync(UUID uuid) {
        TeamProjectEBackport.getOnlineTeamMembers(uuid).forEach(p -> PacketHandler.sendTo(new KnowledgeSyncPKT(serializeForClient()), p));
    }

    private CompoundNBT serializeForClient(){
        CompoundNBT properties = new CompoundNBT();
        properties.putString("transmutationEmc", getTeam().getEmc().toString());
        ListNBT knowledgeWrite = new ListNBT();
        for (ItemInfo i : getTeam().getKnowledge())
            knowledgeWrite.add(i.write(new CompoundNBT()));

        properties.put("knowledge", knowledgeWrite);
        properties.put("inputlock", this.inputLocks.serializeNBT());
        properties.putBoolean("fullknowledge", getTeam().hasFullKnowledge());
        return properties;
    }


    @Override
    public void syncEmc(@Nonnull ServerPlayerEntity player) {
        syncEmc(TeamProjectEBackport.getPlayerUUID(player));
    }

    public void syncEmc(UUID uuid){
        TeamProjectEBackport.getOnlineTeamMembers(uuid).forEach(p -> PacketHandler.sendTo(new KnowledgeSyncEmcPKT(getEmc()), p));
    }

    @Override
    public void syncKnowledgeChange(@Nonnull ServerPlayerEntity player, ItemInfo change, boolean learned) {
        syncKnowledgeChange(TeamProjectEBackport.getPlayerUUID(player), change, learned);
    }

    public void syncKnowledgeChange(UUID uuid, ItemInfo change, boolean learned){
        TeamProjectEBackport.getOnlineTeamMembers(uuid).forEach(p -> PacketHandler.sendTo(new KnowledgeSyncChangePKT(change, learned), p));
    }

    @Override
    public void syncInputAndLocks(@Nonnull ServerPlayerEntity player, List<Integer> slotsChanged, TargetUpdateType updateTargets) {
        if (!slotsChanged.isEmpty()) {
            int slots = inputLocks.getSlots();
            Map<Integer, ItemStack> stacksToSync = new HashMap<>();
            for (int slot : slotsChanged) {
                if (slot >= 0 && slot < slots) {
                    //Validate the slot is a valid index
                    stacksToSync.put(slot, inputLocks.getStackInSlot(slot));
                }
            }
            if (!stacksToSync.isEmpty()) {
                //Validate it is not empty in case we were fed bad indices
                PacketHandler.sendTo(new KnowledgeSyncInputsAndLocksPKT(stacksToSync, updateTargets), player);
            }
        }
    }

    @Override
    public void receiveInputsAndLocks(Map<Integer, ItemStack> changes) {
        int slots = inputLocks.getSlots();
        for (Map.Entry<Integer, ItemStack> entry : changes.entrySet()) {
            int slot = entry.getKey();
            if (slot >= 0 && slot < slots) {
                //Validate the slot is a valid index
                inputLocks.setStackInSlot(slot, entry.getValue());
            }
        }
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT properties = new CompoundNBT();
        properties.put("inputlock", inputLocks.serializeNBT());
        return properties;
    }

    @Override
    public void deserializeNBT(CompoundNBT properties) {
        for (int i = 0; i < inputLocks.getSlots(); i++) {
            inputLocks.setStackInSlot(i, ItemStack.EMPTY);
        }
        inputLocks.deserializeNBT(properties.getCompound("inputlock"));
    }


}
