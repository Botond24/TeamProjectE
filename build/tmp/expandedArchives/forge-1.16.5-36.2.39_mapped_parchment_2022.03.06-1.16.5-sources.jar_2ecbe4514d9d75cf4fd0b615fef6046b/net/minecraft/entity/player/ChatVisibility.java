package net.minecraft.entity.player;

import java.util.Arrays;
import java.util.Comparator;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum ChatVisibility {
   FULL(0, "options.chat.visibility.full"),
   SYSTEM(1, "options.chat.visibility.system"),
   HIDDEN(2, "options.chat.visibility.hidden");

   private static final ChatVisibility[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(ChatVisibility::getId)).toArray((p_221253_0_) -> {
      return new ChatVisibility[p_221253_0_];
   });
   private final int id;
   private final String key;

   private ChatVisibility(int pId, String pKey) {
      this.id = pId;
      this.key = pKey;
   }

   public int getId() {
      return this.id;
   }

   @OnlyIn(Dist.CLIENT)
   public String getKey() {
      return this.key;
   }

   @OnlyIn(Dist.CLIENT)
   public static ChatVisibility byId(int pId) {
      return BY_ID[MathHelper.positiveModulo(pId, BY_ID.length)];
   }
}