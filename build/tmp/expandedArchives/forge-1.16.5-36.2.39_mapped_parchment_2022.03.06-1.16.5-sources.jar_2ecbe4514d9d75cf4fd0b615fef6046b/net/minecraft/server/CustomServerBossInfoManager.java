package net.minecraft.server;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class CustomServerBossInfoManager {
   private final Map<ResourceLocation, CustomServerBossInfo> events = Maps.newHashMap();

   @Nullable
   public CustomServerBossInfo get(ResourceLocation pId) {
      return this.events.get(pId);
   }

   public CustomServerBossInfo create(ResourceLocation pId, ITextComponent p_201379_2_) {
      CustomServerBossInfo customserverbossinfo = new CustomServerBossInfo(pId, p_201379_2_);
      this.events.put(pId, customserverbossinfo);
      return customserverbossinfo;
   }

   public void remove(CustomServerBossInfo pBossbar) {
      this.events.remove(pBossbar.getTextId());
   }

   public Collection<ResourceLocation> getIds() {
      return this.events.keySet();
   }

   public Collection<CustomServerBossInfo> getEvents() {
      return this.events.values();
   }

   public CompoundNBT save() {
      CompoundNBT compoundnbt = new CompoundNBT();

      for(CustomServerBossInfo customserverbossinfo : this.events.values()) {
         compoundnbt.put(customserverbossinfo.getTextId().toString(), customserverbossinfo.save());
      }

      return compoundnbt;
   }

   public void load(CompoundNBT p_201381_1_) {
      for(String s : p_201381_1_.getAllKeys()) {
         ResourceLocation resourcelocation = new ResourceLocation(s);
         this.events.put(resourcelocation, CustomServerBossInfo.load(p_201381_1_.getCompound(s), resourcelocation));
      }

   }

   public void onPlayerConnect(ServerPlayerEntity pPlayer) {
      for(CustomServerBossInfo customserverbossinfo : this.events.values()) {
         customserverbossinfo.onPlayerConnect(pPlayer);
      }

   }

   public void onPlayerDisconnect(ServerPlayerEntity pPlayer) {
      for(CustomServerBossInfo customserverbossinfo : this.events.values()) {
         customserverbossinfo.onPlayerDisconnect(pPlayer);
      }

   }
}