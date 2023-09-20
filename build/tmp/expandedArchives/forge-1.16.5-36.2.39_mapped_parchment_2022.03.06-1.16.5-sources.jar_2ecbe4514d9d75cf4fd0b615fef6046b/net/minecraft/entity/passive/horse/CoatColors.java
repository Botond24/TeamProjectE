package net.minecraft.entity.passive.horse;

import java.util.Arrays;
import java.util.Comparator;

public enum CoatColors {
   WHITE(0),
   CREAMY(1),
   CHESTNUT(2),
   BROWN(3),
   BLACK(4),
   GRAY(5),
   DARKBROWN(6);

   private static final CoatColors[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(CoatColors::getId)).toArray((p_234255_0_) -> {
      return new CoatColors[p_234255_0_];
   });
   private final int id;

   private CoatColors(int pId) {
      this.id = pId;
   }

   public int getId() {
      return this.id;
   }

   public static CoatColors byId(int pId) {
      return BY_ID[pId % BY_ID.length];
   }
}