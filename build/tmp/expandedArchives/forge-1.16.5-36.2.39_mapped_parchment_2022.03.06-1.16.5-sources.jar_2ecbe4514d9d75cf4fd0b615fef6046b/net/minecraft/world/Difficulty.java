package net.minecraft.world;

import java.util.Arrays;
import java.util.Comparator;
import javax.annotation.Nullable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum Difficulty {
   PEACEFUL(0, "peaceful"),
   EASY(1, "easy"),
   NORMAL(2, "normal"),
   HARD(3, "hard");

   private static final Difficulty[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(Difficulty::getId)).toArray((p_199928_0_) -> {
      return new Difficulty[p_199928_0_];
   });
   private final int id;
   private final String key;

   private Difficulty(int pId, String pKey) {
      this.id = pId;
      this.key = pKey;
   }

   public int getId() {
      return this.id;
   }

   public ITextComponent getDisplayName() {
      return new TranslationTextComponent("options.difficulty." + this.key);
   }

   public static Difficulty byId(int pId) {
      return BY_ID[pId % BY_ID.length];
   }

   @Nullable
   public static Difficulty byName(String pName) {
      for(Difficulty difficulty : values()) {
         if (difficulty.key.equals(pName)) {
            return difficulty;
         }
      }

      return null;
   }

   public String getKey() {
      return this.key;
   }

   @OnlyIn(Dist.CLIENT)
   public Difficulty nextById() {
      return BY_ID[(this.id + 1) % BY_ID.length];
   }
}