package net.minecraft.server.integrated;

import com.mojang.authlib.GameProfile;
import java.net.SocketAddress;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.storage.PlayerData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IntegratedPlayerList extends PlayerList {
   private CompoundNBT playerData;

   public IntegratedPlayerList(IntegratedServer pServer, DynamicRegistries.Impl pRegistryHolder, PlayerData pPlayerIo) {
      super(pServer, pRegistryHolder, pPlayerIo, 8);
      this.setViewDistance(10);
   }

   /**
    * also stores the NBTTags if this is an intergratedPlayerList
    */
   protected void save(ServerPlayerEntity pPlayer) {
      if (pPlayer.getName().getString().equals(this.getServer().getSingleplayerName())) {
         this.playerData = pPlayer.saveWithoutId(new CompoundNBT());
      }

      super.save(pPlayer);
   }

   public ITextComponent canPlayerLogin(SocketAddress pAddress, GameProfile pProfile) {
      return (ITextComponent)(pProfile.getName().equalsIgnoreCase(this.getServer().getSingleplayerName()) && this.getPlayerByName(pProfile.getName()) != null ? new TranslationTextComponent("multiplayer.disconnect.name_taken") : super.canPlayerLogin(pAddress, pProfile));
   }

   public IntegratedServer getServer() {
      return (IntegratedServer)super.getServer();
   }

   /**
    * On integrated servers, returns the host's player data to be written to level.dat.
    */
   public CompoundNBT getSingleplayerData() {
      return this.playerData;
   }
}