package net.minecraft.client.entity.player;

import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import java.io.File;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.texture.DownloadingTexture;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractClientPlayerEntity extends PlayerEntity {
   private NetworkPlayerInfo playerInfo;
   public float elytraRotX;
   public float elytraRotY;
   public float elytraRotZ;
   public final ClientWorld clientLevel;

   public AbstractClientPlayerEntity(ClientWorld pClientLevel, GameProfile pGameProfile) {
      super(pClientLevel, pClientLevel.getSharedSpawnPos(), pClientLevel.getSharedSpawnAngle(), pGameProfile);
      this.clientLevel = pClientLevel;
   }

   /**
    * Returns true if the player is in spectator mode.
    */
   public boolean isSpectator() {
      NetworkPlayerInfo networkplayerinfo = Minecraft.getInstance().getConnection().getPlayerInfo(this.getGameProfile().getId());
      return networkplayerinfo != null && networkplayerinfo.getGameMode() == GameType.SPECTATOR;
   }

   public boolean isCreative() {
      NetworkPlayerInfo networkplayerinfo = Minecraft.getInstance().getConnection().getPlayerInfo(this.getGameProfile().getId());
      return networkplayerinfo != null && networkplayerinfo.getGameMode() == GameType.CREATIVE;
   }

   /**
    * Checks if this instance of AbstractClientPlayer has any associated player data.
    */
   public boolean isCapeLoaded() {
      return this.getPlayerInfo() != null;
   }

   @Nullable
   protected NetworkPlayerInfo getPlayerInfo() {
      if (this.playerInfo == null) {
         this.playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(this.getUUID());
      }

      return this.playerInfo;
   }

   /**
    * Returns true if the player has an associated skin.
    */
   public boolean isSkinLoaded() {
      NetworkPlayerInfo networkplayerinfo = this.getPlayerInfo();
      return networkplayerinfo != null && networkplayerinfo.isSkinLoaded();
   }

   /**
    * Returns the ResourceLocation associated with the player's skin
    */
   public ResourceLocation getSkinTextureLocation() {
      NetworkPlayerInfo networkplayerinfo = this.getPlayerInfo();
      return networkplayerinfo == null ? DefaultPlayerSkin.getDefaultSkin(this.getUUID()) : networkplayerinfo.getSkinLocation();
   }

   @Nullable
   public ResourceLocation getCloakTextureLocation() {
      NetworkPlayerInfo networkplayerinfo = this.getPlayerInfo();
      return networkplayerinfo == null ? null : networkplayerinfo.getCapeLocation();
   }

   public boolean isElytraLoaded() {
      return this.getPlayerInfo() != null;
   }

   /**
    * Gets the special Elytra texture for the player.
    */
   @Nullable
   public ResourceLocation getElytraTextureLocation() {
      NetworkPlayerInfo networkplayerinfo = this.getPlayerInfo();
      return networkplayerinfo == null ? null : networkplayerinfo.getElytraLocation();
   }

   public static DownloadingTexture registerSkinTexture(ResourceLocation pLocation, String p_110304_1_) {
      TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
      Texture texture = texturemanager.getTexture(pLocation);
      if (texture == null) {
         texture = new DownloadingTexture((File)null, String.format("http://skins.minecraft.net/MinecraftSkins/%s.png", StringUtils.stripColor(p_110304_1_)), DefaultPlayerSkin.getDefaultSkin(createPlayerUUID(p_110304_1_)), true, (Runnable)null);
         texturemanager.register(pLocation, texture);
      }

      return (DownloadingTexture)texture;
   }

   /**
    * Returns true if the username has an associated skin.
    */
   public static ResourceLocation getSkinLocation(String pUsername) {
      return new ResourceLocation("skins/" + Hashing.sha1().hashUnencodedChars(StringUtils.stripColor(pUsername)));
   }

   public String getModelName() {
      NetworkPlayerInfo networkplayerinfo = this.getPlayerInfo();
      return networkplayerinfo == null ? DefaultPlayerSkin.getSkinModelName(this.getUUID()) : networkplayerinfo.getModelName();
   }

   public float getFieldOfViewModifier() {
      float f = 1.0F;
      if (this.abilities.flying) {
         f *= 1.1F;
      }

      f = (float)((double)f * ((this.getAttributeValue(Attributes.MOVEMENT_SPEED) / (double)this.abilities.getWalkingSpeed() + 1.0D) / 2.0D));
      if (this.abilities.getWalkingSpeed() == 0.0F || Float.isNaN(f) || Float.isInfinite(f)) {
         f = 1.0F;
      }

      if (this.isUsingItem() && this.getUseItem().getItem() == Items.BOW) {
         int i = this.getTicksUsingItem();
         float f1 = (float)i / 20.0F;
         if (f1 > 1.0F) {
            f1 = 1.0F;
         } else {
            f1 = f1 * f1;
         }

         f *= 1.0F - f1 * 0.15F;
      }

      return net.minecraftforge.client.ForgeHooksClient.getOffsetFOV(this, f);
   }
}
