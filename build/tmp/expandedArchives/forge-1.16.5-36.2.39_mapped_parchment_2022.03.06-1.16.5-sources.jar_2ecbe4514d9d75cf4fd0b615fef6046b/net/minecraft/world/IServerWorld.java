package net.minecraft.world;

import net.minecraft.entity.Entity;
import net.minecraft.world.server.ServerWorld;

public interface IServerWorld extends IWorld {
   ServerWorld getLevel();

   default void addFreshEntityWithPassengers(Entity pEntity) {
      pEntity.getSelfAndPassengers().forEach(this::addFreshEntity);
   }
}