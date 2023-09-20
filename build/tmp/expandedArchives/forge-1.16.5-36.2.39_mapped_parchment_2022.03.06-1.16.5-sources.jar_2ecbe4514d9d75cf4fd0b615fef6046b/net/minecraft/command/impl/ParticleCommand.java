package net.minecraft.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ParticleArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;

public class ParticleCommand {
   private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslationTextComponent("commands.particle.failed"));

   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      pDispatcher.register(Commands.literal("particle").requires((p_198568_0_) -> {
         return p_198568_0_.hasPermission(2);
      }).then(Commands.argument("name", ParticleArgument.particle()).executes((p_198562_0_) -> {
         return sendParticles(p_198562_0_.getSource(), ParticleArgument.getParticle(p_198562_0_, "name"), p_198562_0_.getSource().getPosition(), Vector3d.ZERO, 0.0F, 0, false, p_198562_0_.getSource().getServer().getPlayerList().getPlayers());
      }).then(Commands.argument("pos", Vec3Argument.vec3()).executes((p_201226_0_) -> {
         return sendParticles(p_201226_0_.getSource(), ParticleArgument.getParticle(p_201226_0_, "name"), Vec3Argument.getVec3(p_201226_0_, "pos"), Vector3d.ZERO, 0.0F, 0, false, p_201226_0_.getSource().getServer().getPlayerList().getPlayers());
      }).then(Commands.argument("delta", Vec3Argument.vec3(false)).then(Commands.argument("speed", FloatArgumentType.floatArg(0.0F)).then(Commands.argument("count", IntegerArgumentType.integer(0)).executes((p_198565_0_) -> {
         return sendParticles(p_198565_0_.getSource(), ParticleArgument.getParticle(p_198565_0_, "name"), Vec3Argument.getVec3(p_198565_0_, "pos"), Vec3Argument.getVec3(p_198565_0_, "delta"), FloatArgumentType.getFloat(p_198565_0_, "speed"), IntegerArgumentType.getInteger(p_198565_0_, "count"), false, p_198565_0_.getSource().getServer().getPlayerList().getPlayers());
      }).then(Commands.literal("force").executes((p_198561_0_) -> {
         return sendParticles(p_198561_0_.getSource(), ParticleArgument.getParticle(p_198561_0_, "name"), Vec3Argument.getVec3(p_198561_0_, "pos"), Vec3Argument.getVec3(p_198561_0_, "delta"), FloatArgumentType.getFloat(p_198561_0_, "speed"), IntegerArgumentType.getInteger(p_198561_0_, "count"), true, p_198561_0_.getSource().getServer().getPlayerList().getPlayers());
      }).then(Commands.argument("viewers", EntityArgument.players()).executes((p_198566_0_) -> {
         return sendParticles(p_198566_0_.getSource(), ParticleArgument.getParticle(p_198566_0_, "name"), Vec3Argument.getVec3(p_198566_0_, "pos"), Vec3Argument.getVec3(p_198566_0_, "delta"), FloatArgumentType.getFloat(p_198566_0_, "speed"), IntegerArgumentType.getInteger(p_198566_0_, "count"), true, EntityArgument.getPlayers(p_198566_0_, "viewers"));
      }))).then(Commands.literal("normal").executes((p_198560_0_) -> {
         return sendParticles(p_198560_0_.getSource(), ParticleArgument.getParticle(p_198560_0_, "name"), Vec3Argument.getVec3(p_198560_0_, "pos"), Vec3Argument.getVec3(p_198560_0_, "delta"), FloatArgumentType.getFloat(p_198560_0_, "speed"), IntegerArgumentType.getInteger(p_198560_0_, "count"), false, p_198560_0_.getSource().getServer().getPlayerList().getPlayers());
      }).then(Commands.argument("viewers", EntityArgument.players()).executes((p_198567_0_) -> {
         return sendParticles(p_198567_0_.getSource(), ParticleArgument.getParticle(p_198567_0_, "name"), Vec3Argument.getVec3(p_198567_0_, "pos"), Vec3Argument.getVec3(p_198567_0_, "delta"), FloatArgumentType.getFloat(p_198567_0_, "speed"), IntegerArgumentType.getInteger(p_198567_0_, "count"), false, EntityArgument.getPlayers(p_198567_0_, "viewers"));
      })))))))));
   }

   private static int sendParticles(CommandSource pSource, IParticleData pParticleData, Vector3d pPos, Vector3d pDelta, float pSpeed, int pCount, boolean pForce, Collection<ServerPlayerEntity> pViewers) throws CommandSyntaxException {
      int i = 0;

      for(ServerPlayerEntity serverplayerentity : pViewers) {
         if (pSource.getLevel().sendParticles(serverplayerentity, pParticleData, pForce, pPos.x, pPos.y, pPos.z, pCount, pDelta.x, pDelta.y, pDelta.z, (double)pSpeed)) {
            ++i;
         }
      }

      if (i == 0) {
         throw ERROR_FAILED.create();
      } else {
         pSource.sendSuccess(new TranslationTextComponent("commands.particle.success", Registry.PARTICLE_TYPE.getKey(pParticleData.getType()).toString()), true);
         return i;
      }
   }
}