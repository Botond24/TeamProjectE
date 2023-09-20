package net.minecraft.world.chunk;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.stream.Stream;
import net.minecraft.entity.player.ServerPlayerEntity;

public final class PlayerGenerationTracker {
   private final Object2BooleanMap<ServerPlayerEntity> players = new Object2BooleanOpenHashMap<>();

   public Stream<ServerPlayerEntity> getPlayers(long pChunkPos) {
      return this.players.keySet().stream();
   }

   public void addPlayer(long pChunkPos, ServerPlayerEntity pPlayer, boolean pCanGenerateChunks) {
      this.players.put(pPlayer, pCanGenerateChunks);
   }

   public void removePlayer(long pChunkPos, ServerPlayerEntity pPlayer) {
      this.players.removeBoolean(pPlayer);
   }

   public void ignorePlayer(ServerPlayerEntity pPlayer) {
      this.players.replace(pPlayer, true);
   }

   public void unIgnorePlayer(ServerPlayerEntity pPlayer) {
      this.players.replace(pPlayer, false);
   }

   public boolean ignoredOrUnknown(ServerPlayerEntity pPlayer) {
      return this.players.getOrDefault(pPlayer, true);
   }

   public boolean ignored(ServerPlayerEntity pPlayer) {
      return this.players.getBoolean(pPlayer);
   }

   public void updatePlayer(long pOldChunkPos, long pNewChunkPos, ServerPlayerEntity pPlayer) {
   }
}