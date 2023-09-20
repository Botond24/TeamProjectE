package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;

public class ZombieSplit extends EntityRenameHelper {
   public ZombieSplit(Schema p_i49648_1_, boolean p_i49648_2_) {
      super("EntityZombieSplitFix", p_i49648_1_, p_i49648_2_);
   }

   protected Pair<String, Dynamic<?>> getNewNameAndTag(String pName, Dynamic<?> pTag) {
      if (Objects.equals("Zombie", pName)) {
         String s = "Zombie";
         int i = pTag.get("ZombieType").asInt(0);
         switch(i) {
         case 0:
         default:
            break;
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
            s = "ZombieVillager";
            pTag = pTag.set("Profession", pTag.createInt(i - 1));
            break;
         case 6:
            s = "Husk";
         }

         pTag = pTag.remove("ZombieType");
         return Pair.of(s, pTag);
      } else {
         return Pair.of(pName, pTag);
      }
   }
}