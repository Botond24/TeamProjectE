package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.network.play.client.CTabCompletePacket;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientSuggestionProvider implements ISuggestionProvider {
   private final ClientPlayNetHandler connection;
   private final Minecraft minecraft;
   private int pendingSuggestionsId = -1;
   private CompletableFuture<Suggestions> pendingSuggestionsFuture;

   public ClientSuggestionProvider(ClientPlayNetHandler p_i49558_1_, Minecraft p_i49558_2_) {
      this.connection = p_i49558_1_;
      this.minecraft = p_i49558_2_;
   }

   public Collection<String> getOnlinePlayerNames() {
      List<String> list = Lists.newArrayList();

      for(NetworkPlayerInfo networkplayerinfo : this.connection.getOnlinePlayers()) {
         list.add(networkplayerinfo.getProfile().getName());
      }

      return list;
   }

   public Collection<String> getSelectedEntities() {
      return (Collection<String>)(this.minecraft.hitResult != null && this.minecraft.hitResult.getType() == RayTraceResult.Type.ENTITY ? Collections.singleton(((EntityRayTraceResult)this.minecraft.hitResult).getEntity().getStringUUID()) : Collections.emptyList());
   }

   public Collection<String> getAllTeams() {
      return this.connection.getLevel().getScoreboard().getTeamNames();
   }

   public Collection<ResourceLocation> getAvailableSoundEvents() {
      return this.minecraft.getSoundManager().getAvailableSounds();
   }

   public Stream<ResourceLocation> getRecipeNames() {
      return this.connection.getRecipeManager().getRecipeIds();
   }

   public boolean hasPermission(int pLevel) {
      ClientPlayerEntity clientplayerentity = this.minecraft.player;
      return clientplayerentity != null ? clientplayerentity.hasPermissions(pLevel) : pLevel == 0;
   }

   public CompletableFuture<Suggestions> customSuggestion(CommandContext<ISuggestionProvider> pContext, SuggestionsBuilder pSuggestionsBuilder) {
      if (this.pendingSuggestionsFuture != null) {
         this.pendingSuggestionsFuture.cancel(false);
      }

      this.pendingSuggestionsFuture = new CompletableFuture<>();
      int i = ++this.pendingSuggestionsId;
      this.connection.send(new CTabCompletePacket(i, pContext.getInput()));
      return this.pendingSuggestionsFuture;
   }

   private static String prettyPrint(double pDoubleValue) {
      return String.format(Locale.ROOT, "%.2f", pDoubleValue);
   }

   private static String prettyPrint(int pIntValue) {
      return Integer.toString(pIntValue);
   }

   public Collection<ISuggestionProvider.Coordinates> getRelevantCoordinates() {
      RayTraceResult raytraceresult = this.minecraft.hitResult;
      if (raytraceresult != null && raytraceresult.getType() == RayTraceResult.Type.BLOCK) {
         BlockPos blockpos = ((BlockRayTraceResult)raytraceresult).getBlockPos();
         return Collections.singleton(new ISuggestionProvider.Coordinates(prettyPrint(blockpos.getX()), prettyPrint(blockpos.getY()), prettyPrint(blockpos.getZ())));
      } else {
         return ISuggestionProvider.super.getRelevantCoordinates();
      }
   }

   public Collection<ISuggestionProvider.Coordinates> getAbsoluteCoordinates() {
      RayTraceResult raytraceresult = this.minecraft.hitResult;
      if (raytraceresult != null && raytraceresult.getType() == RayTraceResult.Type.BLOCK) {
         Vector3d vector3d = raytraceresult.getLocation();
         return Collections.singleton(new ISuggestionProvider.Coordinates(prettyPrint(vector3d.x), prettyPrint(vector3d.y), prettyPrint(vector3d.z)));
      } else {
         return ISuggestionProvider.super.getAbsoluteCoordinates();
      }
   }

   public Set<RegistryKey<World>> levels() {
      return this.connection.levels();
   }

   public DynamicRegistries registryAccess() {
      return this.connection.registryAccess();
   }

   public void completeCustomSuggestions(int pTransaction, Suggestions pResult) {
      if (pTransaction == this.pendingSuggestionsId) {
         this.pendingSuggestionsFuture.complete(pResult);
         this.pendingSuggestionsFuture = null;
         this.pendingSuggestionsId = -1;
      }

   }
}