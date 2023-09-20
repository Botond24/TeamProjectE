package net.minecraft.network;

import io.netty.buffer.Unpooled;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.pathfinding.Path;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.raid.Raid;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DebugPacketSender {
   private static final Logger LOGGER = LogManager.getLogger();

   public static void sendGameTestAddMarker(ServerWorld pLevel, BlockPos pPos, String pText, int pColor, int pLifetimeMillis) {
      PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
      packetbuffer.writeBlockPos(pPos);
      packetbuffer.writeInt(pColor);
      packetbuffer.writeUtf(pText);
      packetbuffer.writeInt(pLifetimeMillis);
      sendPacketToAllPlayers(pLevel, packetbuffer, SCustomPayloadPlayPacket.DEBUG_GAME_TEST_ADD_MARKER);
   }

   public static void sendGameTestClearPacket(ServerWorld pLevel) {
      PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
      sendPacketToAllPlayers(pLevel, packetbuffer, SCustomPayloadPlayPacket.DEBUG_GAME_TEST_CLEAR);
   }

   public static void sendPoiPacketsForChunk(ServerWorld pLevel, ChunkPos pChunkPos) {
   }

   public static void sendPoiAddedPacket(ServerWorld pLevel, BlockPos pPos) {
      sendVillageSectionsPacket(pLevel, pPos);
   }

   public static void sendPoiRemovedPacket(ServerWorld pLevel, BlockPos pPos) {
      sendVillageSectionsPacket(pLevel, pPos);
   }

   public static void sendPoiTicketCountPacket(ServerWorld pLevel, BlockPos pPos) {
      sendVillageSectionsPacket(pLevel, pPos);
   }

   private static void sendVillageSectionsPacket(ServerWorld pLevel, BlockPos pPos) {
   }

   public static void sendPathFindingPacket(World pLevel, MobEntity pMob, @Nullable Path pPath, float pMaxDistanceToWaypoint) {
   }

   public static void sendNeighborsUpdatePacket(World pLevel, BlockPos pPos) {
   }

   public static void sendStructurePacket(ISeedReader pLevel, StructureStart<?> pStructureStart) {
   }

   public static void sendGoalSelector(World pLevel, MobEntity pMob, GoalSelector pGoalSelector) {
      if (pLevel instanceof ServerWorld) {
         ;
      }
   }

   public static void sendRaids(ServerWorld pLevel, Collection<Raid> pRaids) {
   }

   public static void sendEntityBrain(LivingEntity pLivingEntity) {
   }

   public static void sendBeeInfo(BeeEntity pBee) {
   }

   public static void sendHiveInfo(BeehiveTileEntity p_229750_0_) {
   }

   private static void sendPacketToAllPlayers(ServerWorld p_229753_0_, PacketBuffer p_229753_1_, ResourceLocation p_229753_2_) {
      IPacket<?> ipacket = new SCustomPayloadPlayPacket(p_229753_2_, p_229753_1_);

      for(PlayerEntity playerentity : p_229753_0_.getLevel().players()) {
         ((ServerPlayerEntity)playerentity).connection.send(ipacket);
      }

   }
}