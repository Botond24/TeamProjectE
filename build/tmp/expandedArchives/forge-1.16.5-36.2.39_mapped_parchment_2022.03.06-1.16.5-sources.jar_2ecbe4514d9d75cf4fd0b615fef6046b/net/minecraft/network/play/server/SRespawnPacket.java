package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SRespawnPacket implements IPacket<IClientPlayNetHandler> {
   private DimensionType dimensionType;
   private RegistryKey<World> dimension;
   /** First 8 bytes of the SHA-256 hash of the world's seed */
   private long seed;
   private GameType playerGameType;
   private GameType previousPlayerGameType;
   private boolean isDebug;
   private boolean isFlat;
   private boolean keepAllPlayerData;

   public SRespawnPacket() {
   }

   public SRespawnPacket(DimensionType pDimensionType, RegistryKey<World> pDimension, long pSeed, GameType pPlayerGameType, GameType pPreviousPlayerGameType, boolean pIsDebug, boolean pIsFlat, boolean pKeepAllPlayerData) {
      this.dimensionType = pDimensionType;
      this.dimension = pDimension;
      this.seed = pSeed;
      this.playerGameType = pPlayerGameType;
      this.previousPlayerGameType = pPreviousPlayerGameType;
      this.isDebug = pIsDebug;
      this.isFlat = pIsFlat;
      this.keepAllPlayerData = pKeepAllPlayerData;
   }

   /**
    * Passes this Packet on to the NetHandler for processing.
    */
   public void handle(IClientPlayNetHandler pHandler) {
      pHandler.handleRespawn(this);
   }

   public void read(PacketBuffer p_148837_1_) throws IOException {
      this.dimensionType = p_148837_1_.readWithCodec(DimensionType.CODEC).get();
      this.dimension = RegistryKey.create(Registry.DIMENSION_REGISTRY, p_148837_1_.readResourceLocation());
      this.seed = p_148837_1_.readLong();
      this.playerGameType = GameType.byId(p_148837_1_.readUnsignedByte());
      this.previousPlayerGameType = GameType.byId(p_148837_1_.readUnsignedByte());
      this.isDebug = p_148837_1_.readBoolean();
      this.isFlat = p_148837_1_.readBoolean();
      this.keepAllPlayerData = p_148837_1_.readBoolean();
   }

   /**
    * Writes the raw packet data to the data stream.
    */
   public void write(PacketBuffer pBuffer) throws IOException {
      pBuffer.writeWithCodec(DimensionType.CODEC, () -> {
         return this.dimensionType;
      });
      pBuffer.writeResourceLocation(this.dimension.location());
      pBuffer.writeLong(this.seed);
      pBuffer.writeByte(this.playerGameType.getId());
      pBuffer.writeByte(this.previousPlayerGameType.getId());
      pBuffer.writeBoolean(this.isDebug);
      pBuffer.writeBoolean(this.isFlat);
      pBuffer.writeBoolean(this.keepAllPlayerData);
   }

   @OnlyIn(Dist.CLIENT)
   public DimensionType getDimensionType() {
      return this.dimensionType;
   }

   @OnlyIn(Dist.CLIENT)
   public RegistryKey<World> getDimension() {
      return this.dimension;
   }

   /**
    * get value
    */
   @OnlyIn(Dist.CLIENT)
   public long getSeed() {
      return this.seed;
   }

   @OnlyIn(Dist.CLIENT)
   public GameType getPlayerGameType() {
      return this.playerGameType;
   }

   @OnlyIn(Dist.CLIENT)
   public GameType getPreviousPlayerGameType() {
      return this.previousPlayerGameType;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isDebug() {
      return this.isDebug;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isFlat() {
      return this.isFlat;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean shouldKeepAllPlayerData() {
      return this.keepAllPlayerData;
   }
}