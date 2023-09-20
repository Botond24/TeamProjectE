package net.minecraft.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.AngleArgument;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;

public class SetWorldSpawnCommand {
   public static void register(CommandDispatcher<CommandSource> pDispatcher) {
      pDispatcher.register(Commands.literal("setworldspawn").requires((p_198704_0_) -> {
         return p_198704_0_.hasPermission(2);
      }).executes((p_198700_0_) -> {
         return setSpawn(p_198700_0_.getSource(), new BlockPos(p_198700_0_.getSource().getPosition()), 0.0F);
      }).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((p_198703_0_) -> {
         return setSpawn(p_198703_0_.getSource(), BlockPosArgument.getOrLoadBlockPos(p_198703_0_, "pos"), 0.0F);
      }).then(Commands.argument("angle", AngleArgument.angle()).executes((p_244377_0_) -> {
         return setSpawn(p_244377_0_.getSource(), BlockPosArgument.getOrLoadBlockPos(p_244377_0_, "pos"), AngleArgument.getAngle(p_244377_0_, "angle"));
      }))));
   }

   private static int setSpawn(CommandSource pSource, BlockPos pPos, float p_198701_2_) {
      pSource.getLevel().setDefaultSpawnPos(pPos, p_198701_2_);
      pSource.sendSuccess(new TranslationTextComponent("commands.setworldspawn.success", pPos.getX(), pPos.getY(), pPos.getZ(), p_198701_2_), true);
      return 1;
   }
}