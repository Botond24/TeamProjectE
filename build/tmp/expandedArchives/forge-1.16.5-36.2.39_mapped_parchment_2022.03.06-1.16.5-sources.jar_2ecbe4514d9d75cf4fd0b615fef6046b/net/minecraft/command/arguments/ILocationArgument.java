package net.minecraft.command.arguments;

import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

public interface ILocationArgument {
   Vector3d getPosition(CommandSource pSource);

   Vector2f getRotation(CommandSource pSource);

   default BlockPos getBlockPos(CommandSource pSource) {
      return new BlockPos(this.getPosition(pSource));
   }

   boolean isXRelative();

   boolean isYRelative();

   boolean isZRelative();
}