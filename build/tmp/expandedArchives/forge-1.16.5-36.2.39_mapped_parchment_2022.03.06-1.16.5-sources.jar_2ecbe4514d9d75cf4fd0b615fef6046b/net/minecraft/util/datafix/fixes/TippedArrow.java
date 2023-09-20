package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import java.util.Objects;

public class TippedArrow extends TypedEntityRenameHelper {
   public TippedArrow(Schema p_i49650_1_, boolean p_i49650_2_) {
      super("EntityTippedArrowFix", p_i49650_1_, p_i49650_2_);
   }

   protected String rename(String pName) {
      return Objects.equals(pName, "TippedArrow") ? "Arrow" : pName;
   }
}