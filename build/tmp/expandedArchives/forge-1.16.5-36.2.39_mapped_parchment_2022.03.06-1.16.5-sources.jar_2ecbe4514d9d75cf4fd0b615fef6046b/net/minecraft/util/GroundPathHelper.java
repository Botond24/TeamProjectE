package net.minecraft.util;

import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.GroundPathNavigator;

public class GroundPathHelper {
   public static boolean hasGroundPathNavigation(MobEntity pMob) {
      return pMob.getNavigation() instanceof GroundPathNavigator;
   }
}