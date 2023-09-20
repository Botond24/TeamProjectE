package net.minecraft.entity.passive.horse;

import java.util.Arrays;
import java.util.Comparator;

public enum CoatTypes {
   NONE(0),
   WHITE(1),
   WHITE_FIELD(2),
   WHITE_DOTS(3),
   BLACK_DOTS(4);

   private static final CoatTypes[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(CoatTypes::getId)).toArray((p_234249_0_) -> {
      return new CoatTypes[p_234249_0_];
   });
   private final int id;

   private CoatTypes(int pId) {
      this.id = pId;
   }

   public int getId() {
      return this.id;
   }

   public static CoatTypes byId(int pId) {
      return BY_ID[pId % BY_ID.length];
   }
}