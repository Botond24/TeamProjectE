package net.minecraft.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ILocationArgument;
import net.minecraft.command.arguments.LocationInput;
import net.minecraft.command.arguments.RotationArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;

public class TeleportCommand {
   private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(new TranslationTextComponent("commands.teleport.invalidPosition"));

   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      LiteralCommandNode<CommandSource> literalcommandnode = pDispatcher.register(Commands.literal("teleport").requires((p_198816_0_) -> {
         return p_198816_0_.hasPermission(2);
      }).then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("location", Vec3Argument.vec3()).executes((p_198807_0_) -> {
         return teleportToPos(p_198807_0_.getSource(), EntityArgument.getEntities(p_198807_0_, "targets"), p_198807_0_.getSource().getLevel(), Vec3Argument.getCoordinates(p_198807_0_, "location"), (ILocationArgument)null, (TeleportCommand.Facing)null);
      }).then(Commands.argument("rotation", RotationArgument.rotation()).executes((p_198811_0_) -> {
         return teleportToPos(p_198811_0_.getSource(), EntityArgument.getEntities(p_198811_0_, "targets"), p_198811_0_.getSource().getLevel(), Vec3Argument.getCoordinates(p_198811_0_, "location"), RotationArgument.getRotation(p_198811_0_, "rotation"), (TeleportCommand.Facing)null);
      })).then(Commands.literal("facing").then(Commands.literal("entity").then(Commands.argument("facingEntity", EntityArgument.entity()).executes((p_198806_0_) -> {
         return teleportToPos(p_198806_0_.getSource(), EntityArgument.getEntities(p_198806_0_, "targets"), p_198806_0_.getSource().getLevel(), Vec3Argument.getCoordinates(p_198806_0_, "location"), (ILocationArgument)null, new TeleportCommand.Facing(EntityArgument.getEntity(p_198806_0_, "facingEntity"), EntityAnchorArgument.Type.FEET));
      }).then(Commands.argument("facingAnchor", EntityAnchorArgument.anchor()).executes((p_198812_0_) -> {
         return teleportToPos(p_198812_0_.getSource(), EntityArgument.getEntities(p_198812_0_, "targets"), p_198812_0_.getSource().getLevel(), Vec3Argument.getCoordinates(p_198812_0_, "location"), (ILocationArgument)null, new TeleportCommand.Facing(EntityArgument.getEntity(p_198812_0_, "facingEntity"), EntityAnchorArgument.getAnchor(p_198812_0_, "facingAnchor")));
      })))).then(Commands.argument("facingLocation", Vec3Argument.vec3()).executes((p_198805_0_) -> {
         return teleportToPos(p_198805_0_.getSource(), EntityArgument.getEntities(p_198805_0_, "targets"), p_198805_0_.getSource().getLevel(), Vec3Argument.getCoordinates(p_198805_0_, "location"), (ILocationArgument)null, new TeleportCommand.Facing(Vec3Argument.getVec3(p_198805_0_, "facingLocation")));
      })))).then(Commands.argument("destination", EntityArgument.entity()).executes((p_198814_0_) -> {
         return teleportToEntity(p_198814_0_.getSource(), EntityArgument.getEntities(p_198814_0_, "targets"), EntityArgument.getEntity(p_198814_0_, "destination"));
      }))).then(Commands.argument("location", Vec3Argument.vec3()).executes((p_200560_0_) -> {
         return teleportToPos(p_200560_0_.getSource(), Collections.singleton(p_200560_0_.getSource().getEntityOrException()), p_200560_0_.getSource().getLevel(), Vec3Argument.getCoordinates(p_200560_0_, "location"), LocationInput.current(), (TeleportCommand.Facing)null);
      })).then(Commands.argument("destination", EntityArgument.entity()).executes((p_200562_0_) -> {
         return teleportToEntity(p_200562_0_.getSource(), Collections.singleton(p_200562_0_.getSource().getEntityOrException()), EntityArgument.getEntity(p_200562_0_, "destination"));
      })));
      pDispatcher.register(Commands.literal("tp").requires((p_200556_0_) -> {
         return p_200556_0_.hasPermission(2);
      }).redirect(literalcommandnode));
   }

   private static int teleportToEntity(CommandSource pSource, Collection<? extends Entity> pTargets, Entity pDestination) throws CommandSyntaxException {
      for(Entity entity : pTargets) {
         performTeleport(pSource, entity, (ServerWorld)pDestination.level, pDestination.getX(), pDestination.getY(), pDestination.getZ(), EnumSet.noneOf(SPlayerPositionLookPacket.Flags.class), pDestination.yRot, pDestination.xRot, (TeleportCommand.Facing)null);
      }

      if (pTargets.size() == 1) {
         pSource.sendSuccess(new TranslationTextComponent("commands.teleport.success.entity.single", pTargets.iterator().next().getDisplayName(), pDestination.getDisplayName()), true);
      } else {
         pSource.sendSuccess(new TranslationTextComponent("commands.teleport.success.entity.multiple", pTargets.size(), pDestination.getDisplayName()), true);
      }

      return pTargets.size();
   }

   private static int teleportToPos(CommandSource pSource, Collection<? extends Entity> pTargets, ServerWorld pLevel, ILocationArgument pPosition, @Nullable ILocationArgument pRotation, @Nullable TeleportCommand.Facing pFacing) throws CommandSyntaxException {
      Vector3d vector3d = pPosition.getPosition(pSource);
      Vector2f vector2f = pRotation == null ? null : pRotation.getRotation(pSource);
      Set<SPlayerPositionLookPacket.Flags> set = EnumSet.noneOf(SPlayerPositionLookPacket.Flags.class);
      if (pPosition.isXRelative()) {
         set.add(SPlayerPositionLookPacket.Flags.X);
      }

      if (pPosition.isYRelative()) {
         set.add(SPlayerPositionLookPacket.Flags.Y);
      }

      if (pPosition.isZRelative()) {
         set.add(SPlayerPositionLookPacket.Flags.Z);
      }

      if (pRotation == null) {
         set.add(SPlayerPositionLookPacket.Flags.X_ROT);
         set.add(SPlayerPositionLookPacket.Flags.Y_ROT);
      } else {
         if (pRotation.isXRelative()) {
            set.add(SPlayerPositionLookPacket.Flags.X_ROT);
         }

         if (pRotation.isYRelative()) {
            set.add(SPlayerPositionLookPacket.Flags.Y_ROT);
         }
      }

      for(Entity entity : pTargets) {
         if (pRotation == null) {
            performTeleport(pSource, entity, pLevel, vector3d.x, vector3d.y, vector3d.z, set, entity.yRot, entity.xRot, pFacing);
         } else {
            performTeleport(pSource, entity, pLevel, vector3d.x, vector3d.y, vector3d.z, set, vector2f.y, vector2f.x, pFacing);
         }
      }

      if (pTargets.size() == 1) {
         pSource.sendSuccess(new TranslationTextComponent("commands.teleport.success.location.single", pTargets.iterator().next().getDisplayName(), vector3d.x, vector3d.y, vector3d.z), true);
      } else {
         pSource.sendSuccess(new TranslationTextComponent("commands.teleport.success.location.multiple", pTargets.size(), vector3d.x, vector3d.y, vector3d.z), true);
      }

      return pTargets.size();
   }

   private static void performTeleport(CommandSource pSource, Entity pEntity, ServerWorld pLevel, double pX, double pY, double pZ, Set<SPlayerPositionLookPacket.Flags> pRelativeList, float pYaw, float pPitch, @Nullable TeleportCommand.Facing pFacing) throws CommandSyntaxException {
      net.minecraftforge.event.entity.living.EntityTeleportEvent.TeleportCommand event = net.minecraftforge.event.ForgeEventFactory.onEntityTeleportCommand(pEntity, pX, pY, pZ);
      if (event.isCanceled()) return;
      pX = event.getTargetX(); pY = event.getTargetY(); pZ = event.getTargetZ();
      BlockPos blockpos = new BlockPos(pX, pY, pZ);
      if (!World.isInSpawnableBounds(blockpos)) {
         throw INVALID_POSITION.create();
      } else {
         if (pEntity instanceof ServerPlayerEntity) {
            ChunkPos chunkpos = new ChunkPos(new BlockPos(pX, pY, pZ));
            pLevel.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, chunkpos, 1, pEntity.getId());
            pEntity.stopRiding();
            if (((ServerPlayerEntity)pEntity).isSleeping()) {
               ((ServerPlayerEntity)pEntity).stopSleepInBed(true, true);
            }

            if (pLevel == pEntity.level) {
               ((ServerPlayerEntity)pEntity).connection.teleport(pX, pY, pZ, pYaw, pPitch, pRelativeList);
            } else {
               ((ServerPlayerEntity)pEntity).teleportTo(pLevel, pX, pY, pZ, pYaw, pPitch);
            }

            pEntity.setYHeadRot(pYaw);
         } else {
            float f1 = MathHelper.wrapDegrees(pYaw);
            float f = MathHelper.wrapDegrees(pPitch);
            f = MathHelper.clamp(f, -90.0F, 90.0F);
            if (pLevel == pEntity.level) {
               pEntity.moveTo(pX, pY, pZ, f1, f);
               pEntity.setYHeadRot(f1);
            } else {
               pEntity.unRide();
               Entity entity = pEntity;
               pEntity = pEntity.getType().create(pLevel);
               if (pEntity == null) {
                  return;
               }

               pEntity.restoreFrom(entity);
               pEntity.moveTo(pX, pY, pZ, f1, f);
               pEntity.setYHeadRot(f1);
               pLevel.addFromAnotherDimension(pEntity);
            }
         }

         if (pFacing != null) {
            pFacing.perform(pSource, pEntity);
         }

         if (!(pEntity instanceof LivingEntity) || !((LivingEntity)pEntity).isFallFlying()) {
            pEntity.setDeltaMovement(pEntity.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
            pEntity.setOnGround(true);
         }

         if (pEntity instanceof CreatureEntity) {
            ((CreatureEntity)pEntity).getNavigation().stop();
         }

      }
   }

   static class Facing {
      private final Vector3d position;
      private final Entity entity;
      private final EntityAnchorArgument.Type anchor;

      public Facing(Entity p_i48274_1_, EntityAnchorArgument.Type p_i48274_2_) {
         this.entity = p_i48274_1_;
         this.anchor = p_i48274_2_;
         this.position = p_i48274_2_.apply(p_i48274_1_);
      }

      public Facing(Vector3d p_i48246_1_) {
         this.entity = null;
         this.position = p_i48246_1_;
         this.anchor = null;
      }

      public void perform(CommandSource pSource, Entity pEntity) {
         if (this.entity != null) {
            if (pEntity instanceof ServerPlayerEntity) {
               ((ServerPlayerEntity)pEntity).lookAt(pSource.getAnchor(), this.entity, this.anchor);
            } else {
               pEntity.lookAt(pSource.getAnchor(), this.position);
            }
         } else {
            pEntity.lookAt(pSource.getAnchor(), this.position);
         }

      }
   }
}
