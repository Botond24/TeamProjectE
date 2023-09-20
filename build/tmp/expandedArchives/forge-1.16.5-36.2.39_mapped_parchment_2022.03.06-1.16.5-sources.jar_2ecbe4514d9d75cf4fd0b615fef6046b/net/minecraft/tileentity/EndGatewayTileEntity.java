package net.minecraft.tileentity;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.feature.EndGatewayConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.Features;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EndGatewayTileEntity extends EndPortalTileEntity implements ITickableTileEntity {
   private static final Logger LOGGER = LogManager.getLogger();
   private long age;
   private int teleportCooldown;
   @Nullable
   private BlockPos exitPortal;
   private boolean exactTeleport;

   public EndGatewayTileEntity() {
      super(TileEntityType.END_GATEWAY);
   }

   public CompoundNBT save(CompoundNBT pCompound) {
      super.save(pCompound);
      pCompound.putLong("Age", this.age);
      if (this.exitPortal != null) {
         pCompound.put("ExitPortal", NBTUtil.writeBlockPos(this.exitPortal));
      }

      if (this.exactTeleport) {
         pCompound.putBoolean("ExactTeleport", this.exactTeleport);
      }

      return pCompound;
   }

   public void load(BlockState p_230337_1_, CompoundNBT p_230337_2_) {
      super.load(p_230337_1_, p_230337_2_);
      this.age = p_230337_2_.getLong("Age");
      if (p_230337_2_.contains("ExitPortal", 10)) {
         this.exitPortal = NBTUtil.readBlockPos(p_230337_2_.getCompound("ExitPortal"));
      }

      this.exactTeleport = p_230337_2_.getBoolean("ExactTeleport");
   }

   @OnlyIn(Dist.CLIENT)
   public double getViewDistance() {
      return 256.0D;
   }

   public void tick() {
      boolean flag = this.isSpawning();
      boolean flag1 = this.isCoolingDown();
      ++this.age;
      if (flag1) {
         --this.teleportCooldown;
      } else if (!this.level.isClientSide) {
         List<Entity> list = this.level.getEntitiesOfClass(Entity.class, new AxisAlignedBB(this.getBlockPos()), EndGatewayTileEntity::canEntityTeleport);
         if (!list.isEmpty()) {
            this.teleportEntity(list.get(this.level.random.nextInt(list.size())));
         }

         if (this.age % 2400L == 0L) {
            this.triggerCooldown();
         }
      }

      if (flag != this.isSpawning() || flag1 != this.isCoolingDown()) {
         this.setChanged();
      }

   }

   public static boolean canEntityTeleport(Entity p_242690_0_) {
      return EntityPredicates.NO_SPECTATORS.test(p_242690_0_) && !p_242690_0_.getRootVehicle().isOnPortalCooldown();
   }

   public boolean isSpawning() {
      return this.age < 200L;
   }

   public boolean isCoolingDown() {
      return this.teleportCooldown > 0;
   }

   @OnlyIn(Dist.CLIENT)
   public float getSpawnPercent(float pPartialTicks) {
      return MathHelper.clamp(((float)this.age + pPartialTicks) / 200.0F, 0.0F, 1.0F);
   }

   @OnlyIn(Dist.CLIENT)
   public float getCooldownPercent(float pPartialTicks) {
      return 1.0F - MathHelper.clamp(((float)this.teleportCooldown - pPartialTicks) / 40.0F, 0.0F, 1.0F);
   }

   /**
    * Retrieves packet to send to the client whenever this Tile Entity is resynced via World.notifyBlockUpdate. For
    * modded TE's, this packet comes back to you clientside in {@link #onDataPacket}
    */
   @Nullable
   public SUpdateTileEntityPacket getUpdatePacket() {
      return new SUpdateTileEntityPacket(this.worldPosition, 8, this.getUpdateTag());
   }

   /**
    * Get an NBT compound to sync to the client with SPacketChunkData, used for initial loading of the chunk or when
    * many blocks change at once. This compound comes back to you clientside in {@link handleUpdateTag}
    */
   public CompoundNBT getUpdateTag() {
      return this.save(new CompoundNBT());
   }

   public void triggerCooldown() {
      if (!this.level.isClientSide) {
         this.teleportCooldown = 40;
         this.level.blockEvent(this.getBlockPos(), this.getBlockState().getBlock(), 1, 0);
         this.setChanged();
      }

   }

   /**
    * See {@link Block#eventReceived} for more information. This must return true serverside before it is called
    * clientside.
    */
   public boolean triggerEvent(int pId, int pType) {
      if (pId == 1) {
         this.teleportCooldown = 40;
         return true;
      } else {
         return super.triggerEvent(pId, pType);
      }
   }

   public void teleportEntity(Entity p_195496_1_) {
      if (this.level instanceof ServerWorld && !this.isCoolingDown()) {
         this.teleportCooldown = 100;
         if (this.exitPortal == null && this.level.dimension() == World.END) {
            this.findExitPortal((ServerWorld)this.level);
         }

         if (this.exitPortal != null) {
            BlockPos blockpos = this.exactTeleport ? this.exitPortal : this.findExitPosition();
            Entity entity;
            if (p_195496_1_ instanceof EnderPearlEntity) {
               Entity entity1 = ((EnderPearlEntity)p_195496_1_).getOwner();
               if (entity1 instanceof ServerPlayerEntity) {
                  CriteriaTriggers.ENTER_BLOCK.trigger((ServerPlayerEntity)entity1, this.level.getBlockState(this.getBlockPos()));
               }

               if (entity1 != null) {
                  entity = entity1;
                  p_195496_1_.remove();
               } else {
                  entity = p_195496_1_;
               }
            } else {
               entity = p_195496_1_.getRootVehicle();
            }

            entity.setPortalCooldown();
            entity.teleportToWithTicket((double)blockpos.getX() + 0.5D, (double)blockpos.getY(), (double)blockpos.getZ() + 0.5D);
         }

         this.triggerCooldown();
      }
   }

   private BlockPos findExitPosition() {
      BlockPos blockpos = findTallestBlock(this.level, this.exitPortal.offset(0, 2, 0), 5, false);
      LOGGER.debug("Best exit position for portal at {} is {}", this.exitPortal, blockpos);
      return blockpos.above();
   }

   private void findExitPortal(ServerWorld p_227015_1_) {
      Vector3d vector3d = (new Vector3d((double)this.getBlockPos().getX(), 0.0D, (double)this.getBlockPos().getZ())).normalize();
      Vector3d vector3d1 = vector3d.scale(1024.0D);

      for(int i = 16; getChunk(p_227015_1_, vector3d1).getHighestSectionPosition() > 0 && i-- > 0; vector3d1 = vector3d1.add(vector3d.scale(-16.0D))) {
         LOGGER.debug("Skipping backwards past nonempty chunk at {}", (Object)vector3d1);
      }

      for(int j = 16; getChunk(p_227015_1_, vector3d1).getHighestSectionPosition() == 0 && j-- > 0; vector3d1 = vector3d1.add(vector3d.scale(16.0D))) {
         LOGGER.debug("Skipping forward past empty chunk at {}", (Object)vector3d1);
      }

      LOGGER.debug("Found chunk at {}", (Object)vector3d1);
      Chunk chunk = getChunk(p_227015_1_, vector3d1);
      this.exitPortal = findValidSpawnInChunk(chunk);
      if (this.exitPortal == null) {
         this.exitPortal = new BlockPos(vector3d1.x + 0.5D, 75.0D, vector3d1.z + 0.5D);
         LOGGER.debug("Failed to find suitable block, settling on {}", (Object)this.exitPortal);
         Features.END_ISLAND.place(p_227015_1_, p_227015_1_.getChunkSource().getGenerator(), new Random(this.exitPortal.asLong()), this.exitPortal);
      } else {
         LOGGER.debug("Found block at {}", (Object)this.exitPortal);
      }

      this.exitPortal = findTallestBlock(p_227015_1_, this.exitPortal, 16, true);
      LOGGER.debug("Creating portal at {}", (Object)this.exitPortal);
      this.exitPortal = this.exitPortal.above(10);
      this.createExitPortal(p_227015_1_, this.exitPortal);
      this.setChanged();
   }

   private static BlockPos findTallestBlock(IBlockReader pLevel, BlockPos pPos, int pRadius, boolean pAllowBedrock) {
      BlockPos blockpos = null;

      for(int i = -pRadius; i <= pRadius; ++i) {
         for(int j = -pRadius; j <= pRadius; ++j) {
            if (i != 0 || j != 0 || pAllowBedrock) {
               for(int k = 255; k > (blockpos == null ? 0 : blockpos.getY()); --k) {
                  BlockPos blockpos1 = new BlockPos(pPos.getX() + i, k, pPos.getZ() + j);
                  BlockState blockstate = pLevel.getBlockState(blockpos1);
                  if (blockstate.isCollisionShapeFullBlock(pLevel, blockpos1) && (pAllowBedrock || !blockstate.is(Blocks.BEDROCK))) {
                     blockpos = blockpos1;
                     break;
                  }
               }
            }
         }
      }

      return blockpos == null ? pPos : blockpos;
   }

   private static Chunk getChunk(World pLevel, Vector3d pVec3) {
      return pLevel.getChunk(MathHelper.floor(pVec3.x / 16.0D), MathHelper.floor(pVec3.z / 16.0D));
   }

   @Nullable
   private static BlockPos findValidSpawnInChunk(Chunk pChunk) {
      ChunkPos chunkpos = pChunk.getPos();
      BlockPos blockpos = new BlockPos(chunkpos.getMinBlockX(), 30, chunkpos.getMinBlockZ());
      int i = pChunk.getHighestSectionPosition() + 16 - 1;
      BlockPos blockpos1 = new BlockPos(chunkpos.getMaxBlockX(), i, chunkpos.getMaxBlockZ());
      BlockPos blockpos2 = null;
      double d0 = 0.0D;

      for(BlockPos blockpos3 : BlockPos.betweenClosed(blockpos, blockpos1)) {
         BlockState blockstate = pChunk.getBlockState(blockpos3);
         BlockPos blockpos4 = blockpos3.above();
         BlockPos blockpos5 = blockpos3.above(2);
         if (blockstate.is(Blocks.END_STONE) && !pChunk.getBlockState(blockpos4).isCollisionShapeFullBlock(pChunk, blockpos4) && !pChunk.getBlockState(blockpos5).isCollisionShapeFullBlock(pChunk, blockpos5)) {
            double d1 = blockpos3.distSqr(0.0D, 0.0D, 0.0D, true);
            if (blockpos2 == null || d1 < d0) {
               blockpos2 = blockpos3;
               d0 = d1;
            }
         }
      }

      return blockpos2;
   }

   private void createExitPortal(ServerWorld p_227016_1_, BlockPos p_227016_2_) {
      Feature.END_GATEWAY.configured(EndGatewayConfig.knownExit(this.getBlockPos(), false)).place(p_227016_1_, p_227016_1_.getChunkSource().getGenerator(), new Random(), p_227016_2_);
   }

   @OnlyIn(Dist.CLIENT)
   public boolean shouldRenderFace(Direction pFace) {
      return Block.shouldRenderFace(this.getBlockState(), this.level, this.getBlockPos(), pFace);
   }

   @OnlyIn(Dist.CLIENT)
   public int getParticleAmount() {
      int i = 0;

      for(Direction direction : Direction.values()) {
         i += this.shouldRenderFace(direction) ? 1 : 0;
      }

      return i;
   }

   public void setExitPosition(BlockPos pExitPortal, boolean p_195489_2_) {
      this.exactTeleport = p_195489_2_;
      this.exitPortal = pExitPortal;
   }
}