package net.minecraft.network.play.server;

import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Set;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SJoinGamePacket implements IPacket<IClientPlayNetHandler> {
   private int playerId;
   /** First 8 bytes of the SHA-256 hash of the world's seed */
   private long seed;
   private boolean hardcore;
   private GameType gameType;
   private GameType previousGameType;
   private Set<RegistryKey<World>> levels;
   private DynamicRegistries.Impl registryHolder;
   private DimensionType dimensionType;
   private RegistryKey<World> dimension;
   private int maxPlayers;
   private int chunkRadius;
   private boolean reducedDebugInfo;
   /** Set to false when the doImmediateRespawn gamerule is true */
   private boolean showDeathScreen;
   private boolean isDebug;
   private boolean isFlat;

   public SJoinGamePacket() {
   }

   public SJoinGamePacket(int pPlayerId, GameType pGameType, GameType pPreviousGameType, long pSeed, boolean pHardcore, Set<RegistryKey<World>> pLevels, DynamicRegistries.Impl pRegistryHolder, DimensionType pDimensionType, RegistryKey<World> pDimension, int pMaxPlayers, int pChunkRadius, boolean pReducedDebugInfo, boolean pShowDeathScreen, boolean pIsDebug, boolean pIsFlat) {
      this.playerId = pPlayerId;
      this.levels = pLevels;
      this.registryHolder = pRegistryHolder;
      this.dimensionType = pDimensionType;
      this.dimension = pDimension;
      this.seed = pSeed;
      this.gameType = pGameType;
      this.previousGameType = pPreviousGameType;
      this.maxPlayers = pMaxPlayers;
      this.hardcore = pHardcore;
      this.chunkRadius = pChunkRadius;
      this.reducedDebugInfo = pReducedDebugInfo;
      this.showDeathScreen = pShowDeathScreen;
      this.isDebug = pIsDebug;
      this.isFlat = pIsFlat;
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.playerId = p_148837_1_.readInt();
      this.hardcore = p_148837_1_.readBoolean();
      this.gameType = GameType.byId(p_148837_1_.readByte());
      this.previousGameType = GameType.byId(p_148837_1_.readByte());
      int i = p_148837_1_.readVarInt();
      this.levels = Sets.newHashSet();

      for(int j = 0; j < i; ++j) {
         this.levels.add(RegistryKey.create(Registry.DIMENSION_REGISTRY, p_148837_1_.readResourceLocation()));
      }

      this.registryHolder = p_148837_1_.readWithCodec(DynamicRegistries.Impl.NETWORK_CODEC);
      this.dimensionType = p_148837_1_.readWithCodec(DimensionType.CODEC).get();
      this.dimension = RegistryKey.create(Registry.DIMENSION_REGISTRY, p_148837_1_.readResourceLocation());
      this.seed = p_148837_1_.readLong();
      this.maxPlayers = p_148837_1_.readVarInt();
      this.chunkRadius = p_148837_1_.readVarInt();
      this.reducedDebugInfo = p_148837_1_.readBoolean();
      this.showDeathScreen = p_148837_1_.readBoolean();
      this.isDebug = p_148837_1_.readBoolean();
      this.isFlat = p_148837_1_.readBoolean();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeInt(this.playerId);
      pBuffer.writeBoolean(this.hardcore);
      pBuffer.writeByte(this.gameType.getId());
      pBuffer.writeByte(this.previousGameType.getId());
      pBuffer.writeVarInt(this.levels.size());

      for(RegistryKey<World> registrykey : this.levels) {
         pBuffer.writeResourceLocation(registrykey.location());
      }

      pBuffer.writeWithCodec(DynamicRegistries.Impl.NETWORK_CODEC, this.registryHolder);
      pBuffer.writeWithCodec(DimensionType.CODEC, () -> {
         return this.dimensionType;
      });
      pBuffer.writeResourceLocation(this.dimension.location());
      pBuffer.writeLong(this.seed);
      pBuffer.writeVarInt(this.maxPlayers);
      pBuffer.writeVarInt(this.chunkRadius);
      pBuffer.writeBoolean(this.reducedDebugInfo);
      pBuffer.writeBoolean(this.showDeathScreen);
      pBuffer.writeBoolean(this.isDebug);
      pBuffer.writeBoolean(this.isFlat);
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleLogin(this);
   }

   @OnlyIn(Dist.CLIENT)
   public int getPlayerId() {
      return this.playerId;
   }

   /**
    * get value
    */
   @OnlyIn(Dist.CLIENT)
   public long getSeed() {
      return this.seed;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isHardcore() {
      return this.hardcore;
   }

   @OnlyIn(Dist.CLIENT)
   public GameType getGameType() {
      return this.gameType;
   }

   @OnlyIn(Dist.CLIENT)
   public GameType getPreviousGameType() {
      return this.previousGameType;
   }

   @OnlyIn(Dist.CLIENT)
   public Set<RegistryKey<World>> levels() {
      return this.levels;
   }

   @OnlyIn(Dist.CLIENT)
   public DynamicRegistries registryAccess() {
      return this.registryHolder;
   }

   @OnlyIn(Dist.CLIENT)
   public DimensionType getDimensionType() {
      return this.dimensionType;
   }

   @OnlyIn(Dist.CLIENT)
   public RegistryKey<World> getDimension() {
      return this.dimension;
   }

   @OnlyIn(Dist.CLIENT)
   public int getChunkRadius() {
      return this.chunkRadius;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isReducedDebugInfo() {
      return this.reducedDebugInfo;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean shouldShowDeathScreen() {
      return this.showDeathScreen;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isDebug() {
      return this.isDebug;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isFlat() {
      return this.isFlat;
   }
}