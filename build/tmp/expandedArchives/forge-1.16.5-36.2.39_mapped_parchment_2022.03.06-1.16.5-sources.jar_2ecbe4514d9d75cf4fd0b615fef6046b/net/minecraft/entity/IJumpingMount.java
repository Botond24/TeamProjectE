package net.minecraft.entity;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface IJumpingMount {
   @OnlyIn(Dist.CLIENT)
   void onPlayerJump(int pJumpPower);

   boolean canJump();

   void handleStartJump(int pJumpPower);

   void handleStopJump();
}