package net.minecraft.entity.merchant;

import net.minecraft.entity.Entity;

public interface IReputationTracking {
   void onReputationEventFrom(IReputationType pType, Entity pTarget);
}