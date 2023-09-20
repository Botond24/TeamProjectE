package net.minecraft.server;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.server.ServerBossInfo;

public class CustomServerBossInfo extends ServerBossInfo {
   private final ResourceLocation id;
   private final Set<UUID> players = Sets.newHashSet();
   private int value;
   private int max = 100;

   public CustomServerBossInfo(ResourceLocation p_i48620_1_, ITextComponent p_i48620_2_) {
      super(p_i48620_2_, BossInfo.Color.WHITE, BossInfo.Overlay.PROGRESS);
      this.id = p_i48620_1_;
      this.setPercent(0.0F);
   }

   public ResourceLocation getTextId() {
      return this.id;
   }

   /**
    * Makes the boss visible to the given player.
    */
   public void addPlayer(ServerPlayerEntity pPlayer) {
      super.addPlayer(pPlayer);
      this.players.add(pPlayer.getUUID());
   }

   public void addOfflinePlayer(UUID pPlayer) {
      this.players.add(pPlayer);
   }

   /**
    * Makes the boss non-visible to the given player.
    */
   public void removePlayer(ServerPlayerEntity pPlayer) {
      super.removePlayer(pPlayer);
      this.players.remove(pPlayer.getUUID());
   }

   public void removeAllPlayers() {
      super.removeAllPlayers();
      this.players.clear();
   }

   public int getValue() {
      return this.value;
   }

   public int getMax() {
      return this.max;
   }

   public void setValue(int pValue) {
      this.value = pValue;
      this.setPercent(MathHelper.clamp((float)pValue / (float)this.max, 0.0F, 1.0F));
   }

   public void setMax(int pMax) {
      this.max = pMax;
      this.setPercent(MathHelper.clamp((float)this.value / (float)pMax, 0.0F, 1.0F));
   }

   public final ITextComponent getDisplayName() {
      return TextComponentUtils.wrapInSquareBrackets(this.getName()).withStyle((p_211569_1_) -> {
         return p_211569_1_.withColor(this.getColor().getFormatting()).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent(this.getTextId().toString()))).withInsertion(this.getTextId().toString());
      });
   }

   public boolean setPlayers(Collection<ServerPlayerEntity> pServerPlayerList) {
      Set<UUID> set = Sets.newHashSet();
      Set<ServerPlayerEntity> set1 = Sets.newHashSet();

      for(UUID uuid : this.players) {
         boolean flag = false;

         for(ServerPlayerEntity serverplayerentity : pServerPlayerList) {
            if (serverplayerentity.getUUID().equals(uuid)) {
               flag = true;
               break;
            }
         }

         if (!flag) {
            set.add(uuid);
         }
      }

      for(ServerPlayerEntity serverplayerentity1 : pServerPlayerList) {
         boolean flag1 = false;

         for(UUID uuid2 : this.players) {
            if (serverplayerentity1.getUUID().equals(uuid2)) {
               flag1 = true;
               break;
            }
         }

         if (!flag1) {
            set1.add(serverplayerentity1);
         }
      }

      for(UUID uuid1 : set) {
         for(ServerPlayerEntity serverplayerentity3 : this.getPlayers()) {
            if (serverplayerentity3.getUUID().equals(uuid1)) {
               this.removePlayer(serverplayerentity3);
               break;
            }
         }

         this.players.remove(uuid1);
      }

      for(ServerPlayerEntity serverplayerentity2 : set1) {
         this.addPlayer(serverplayerentity2);
      }

      return !set.isEmpty() || !set1.isEmpty();
   }

   public CompoundNBT save() {
      CompoundNBT compoundnbt = new CompoundNBT();
      compoundnbt.putString("Name", ITextComponent.Serializer.toJson(this.name));
      compoundnbt.putBoolean("Visible", this.isVisible());
      compoundnbt.putInt("Value", this.value);
      compoundnbt.putInt("Max", this.max);
      compoundnbt.putString("Color", this.getColor().getName());
      compoundnbt.putString("Overlay", this.getOverlay().getName());
      compoundnbt.putBoolean("DarkenScreen", this.shouldDarkenScreen());
      compoundnbt.putBoolean("PlayBossMusic", this.shouldPlayBossMusic());
      compoundnbt.putBoolean("CreateWorldFog", this.shouldCreateWorldFog());
      ListNBT listnbt = new ListNBT();

      for(UUID uuid : this.players) {
         listnbt.add(NBTUtil.createUUID(uuid));
      }

      compoundnbt.put("Players", listnbt);
      return compoundnbt;
   }

   public static CustomServerBossInfo load(CompoundNBT pNbt, ResourceLocation pId) {
      CustomServerBossInfo customserverbossinfo = new CustomServerBossInfo(pId, ITextComponent.Serializer.fromJson(pNbt.getString("Name")));
      customserverbossinfo.setVisible(pNbt.getBoolean("Visible"));
      customserverbossinfo.setValue(pNbt.getInt("Value"));
      customserverbossinfo.setMax(pNbt.getInt("Max"));
      customserverbossinfo.setColor(BossInfo.Color.byName(pNbt.getString("Color")));
      customserverbossinfo.setOverlay(BossInfo.Overlay.byName(pNbt.getString("Overlay")));
      customserverbossinfo.setDarkenScreen(pNbt.getBoolean("DarkenScreen"));
      customserverbossinfo.setPlayBossMusic(pNbt.getBoolean("PlayBossMusic"));
      customserverbossinfo.setCreateWorldFog(pNbt.getBoolean("CreateWorldFog"));
      ListNBT listnbt = pNbt.getList("Players", 11);

      for(int i = 0; i < listnbt.size(); ++i) {
         customserverbossinfo.addOfflinePlayer(NBTUtil.loadUUID(listnbt.get(i)));
      }

      return customserverbossinfo;
   }

   public void onPlayerConnect(ServerPlayerEntity pPlayer) {
      if (this.players.contains(pPlayer.getUUID())) {
         this.addPlayer(pPlayer);
      }

   }

   public void onPlayerDisconnect(ServerPlayerEntity pPlayer) {
      super.removePlayer(pPlayer);
   }
}