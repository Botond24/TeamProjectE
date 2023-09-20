package net.minecraft.client.multiplayer;

import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientChunkProvider extends AbstractChunkProvider {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Chunk emptyChunk;
   private final WorldLightManager lightEngine;
   private volatile ClientChunkProvider.ChunkArray storage;
   private final ClientWorld level;

   public ClientChunkProvider(ClientWorld p_i51057_1_, int p_i51057_2_) {
      this.level = p_i51057_1_;
      this.emptyChunk = new EmptyChunk(p_i51057_1_, new ChunkPos(0, 0));
      this.lightEngine = new WorldLightManager(this, true, p_i51057_1_.dimensionType().hasSkyLight());
      this.storage = new ClientChunkProvider.ChunkArray(calculateStorageRange(p_i51057_2_));
   }

   public WorldLightManager getLightEngine() {
      return this.lightEngine;
   }

   private static boolean isValidChunk(@Nullable Chunk pChunk, int pX, int pZ) {
      if (pChunk == null) {
         return false;
      } else {
         ChunkPos chunkpos = pChunk.getPos();
         return chunkpos.x == pX && chunkpos.z == pZ;
      }
   }

   /**
    * Unload chunk from ChunkProviderClient's hashmap. Called in response to a Packet50PreChunk with its mode field set
    * to false
    */
   public void drop(int pX, int pZ) {
      if (this.storage.inRange(pX, pZ)) {
         int i = this.storage.getIndex(pX, pZ);
         Chunk chunk = this.storage.getChunk(i);
         if (isValidChunk(chunk, pX, pZ)) {
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.ChunkEvent.Unload(chunk));
            this.storage.replace(i, chunk, (Chunk)null);
         }

      }
   }

   @Nullable
   public Chunk getChunk(int pChunkX, int pChunkZ, ChunkStatus pRequiredStatus, boolean pLoad) {
      if (this.storage.inRange(pChunkX, pChunkZ)) {
         Chunk chunk = this.storage.getChunk(this.storage.getIndex(pChunkX, pChunkZ));
         if (isValidChunk(chunk, pChunkX, pChunkZ)) {
            return chunk;
         }
      }

      return pLoad ? this.emptyChunk : null;
   }

   public IBlockReader getLevel() {
      return this.level;
   }

   @Nullable
   public Chunk replaceWithPacketData(int p_228313_1_, int p_228313_2_, @Nullable BiomeContainer p_228313_3_, PacketBuffer p_228313_4_, CompoundNBT p_228313_5_, int p_228313_6_, boolean p_228313_7_) {
      if (!this.storage.inRange(p_228313_1_, p_228313_2_)) {
         LOGGER.warn("Ignoring chunk since it's not in the view range: {}, {}", p_228313_1_, p_228313_2_);
         return null;
      } else {
         int i = this.storage.getIndex(p_228313_1_, p_228313_2_);
         Chunk chunk = this.storage.chunks.get(i);
         if (!p_228313_7_ && isValidChunk(chunk, p_228313_1_, p_228313_2_)) {
            chunk.replaceWithPacketData(p_228313_3_, p_228313_4_, p_228313_5_, p_228313_6_);
         } else {
            if (p_228313_3_ == null) {
               LOGGER.warn("Ignoring chunk since we don't have complete data: {}, {}", p_228313_1_, p_228313_2_);
               return null;
            }

            chunk = new Chunk(this.level, new ChunkPos(p_228313_1_, p_228313_2_), p_228313_3_);
            chunk.replaceWithPacketData(p_228313_3_, p_228313_4_, p_228313_5_, p_228313_6_);
            this.storage.replace(i, chunk);
         }

         ChunkSection[] achunksection = chunk.getSections();
         WorldLightManager worldlightmanager = this.getLightEngine();
         worldlightmanager.enableLightSources(new ChunkPos(p_228313_1_, p_228313_2_), true);

         for(int j = 0; j < achunksection.length; ++j) {
            ChunkSection chunksection = achunksection[j];
            worldlightmanager.updateSectionStatus(SectionPos.of(p_228313_1_, j, p_228313_2_), ChunkSection.isEmpty(chunksection));
         }

         this.level.onChunkLoaded(p_228313_1_, p_228313_2_);
         net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.world.ChunkEvent.Load(chunk));
         return chunk;
      }
   }

   public void tick(BooleanSupplier p_217207_1_) {
   }

   public void updateViewCenter(int pX, int pZ) {
      this.storage.viewCenterX = pX;
      this.storage.viewCenterZ = pZ;
   }

   public void updateViewRadius(int pViewDistance) {
      int i = this.storage.chunkRadius;
      int j = calculateStorageRange(pViewDistance);
      if (i != j) {
         ClientChunkProvider.ChunkArray clientchunkprovider$chunkarray = new ClientChunkProvider.ChunkArray(j);
         clientchunkprovider$chunkarray.viewCenterX = this.storage.viewCenterX;
         clientchunkprovider$chunkarray.viewCenterZ = this.storage.viewCenterZ;

         for(int k = 0; k < this.storage.chunks.length(); ++k) {
            Chunk chunk = this.storage.chunks.get(k);
            if (chunk != null) {
               ChunkPos chunkpos = chunk.getPos();
               if (clientchunkprovider$chunkarray.inRange(chunkpos.x, chunkpos.z)) {
                  clientchunkprovider$chunkarray.replace(clientchunkprovider$chunkarray.getIndex(chunkpos.x, chunkpos.z), chunk);
               }
            }
         }

         this.storage = clientchunkprovider$chunkarray;
      }

   }

   private static int calculateStorageRange(int pViewDistance) {
      return Math.max(2, pViewDistance) + 3;
   }

   /**
    * Converts the instance data to a readable string.
    */
   public String gatherStats() {
      return "Client Chunk Cache: " + this.storage.chunks.length() + ", " + this.getLoadedChunksCount();
   }

   public int getLoadedChunksCount() {
      return this.storage.chunkCount;
   }

   public void onLightUpdate(LightType pType, SectionPos pPos) {
      Minecraft.getInstance().levelRenderer.setSectionDirty(pPos.x(), pPos.y(), pPos.z());
   }

   public boolean isTickingChunk(BlockPos p_222866_1_) {
      return this.hasChunk(p_222866_1_.getX() >> 4, p_222866_1_.getZ() >> 4);
   }

   public boolean isEntityTickingChunk(ChunkPos p_222865_1_) {
      return this.hasChunk(p_222865_1_.x, p_222865_1_.z);
   }

   public boolean isEntityTickingChunk(Entity p_217204_1_) {
      return this.hasChunk(MathHelper.floor(p_217204_1_.getX()) >> 4, MathHelper.floor(p_217204_1_.getZ()) >> 4);
   }

   @OnlyIn(Dist.CLIENT)
   final class ChunkArray {
      private final AtomicReferenceArray<Chunk> chunks;
      private final int chunkRadius;
      private final int viewRange;
      private volatile int viewCenterX;
      private volatile int viewCenterZ;
      private int chunkCount;

      private ChunkArray(int p_i50568_2_) {
         this.chunkRadius = p_i50568_2_;
         this.viewRange = p_i50568_2_ * 2 + 1;
         this.chunks = new AtomicReferenceArray<>(this.viewRange * this.viewRange);
      }

      private int getIndex(int pX, int pZ) {
         return Math.floorMod(pZ, this.viewRange) * this.viewRange + Math.floorMod(pX, this.viewRange);
      }

      protected void replace(int pChunkIndex, @Nullable Chunk pChunk) {
         Chunk chunk = this.chunks.getAndSet(pChunkIndex, pChunk);
         if (chunk != null) {
            --this.chunkCount;
            ClientChunkProvider.this.level.unload(chunk);
         }

         if (pChunk != null) {
            ++this.chunkCount;
         }

      }

      protected Chunk replace(int pChunkIndex, Chunk pChunk, @Nullable Chunk pReplaceWith) {
         if (this.chunks.compareAndSet(pChunkIndex, pChunk, pReplaceWith) && pReplaceWith == null) {
            --this.chunkCount;
         }

         ClientChunkProvider.this.level.unload(pChunk);
         return pChunk;
      }

      private boolean inRange(int pX, int pZ) {
         return Math.abs(pX - this.viewCenterX) <= this.chunkRadius && Math.abs(pZ - this.viewCenterZ) <= this.chunkRadius;
      }

      @Nullable
      protected Chunk getChunk(int pChunkIndex) {
         return this.chunks.get(pChunkIndex);
      }
   }
}
