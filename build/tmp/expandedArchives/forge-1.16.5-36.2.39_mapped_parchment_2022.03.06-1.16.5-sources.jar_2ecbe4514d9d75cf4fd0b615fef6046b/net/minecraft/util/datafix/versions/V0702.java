package net.minecraft.util.datafix.versions;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;

public class V0702 extends Schema {
   public V0702(int p_i49585_1_, Schema p_i49585_2_) {
      super(p_i49585_1_, p_i49585_2_);
   }

   protected static void registerMob(Schema pSchema, Map<String, Supplier<TypeTemplate>> pMap, String pName) {
      pSchema.register(pMap, pName, () -> {
         return V0100.equipment(pSchema);
      });
   }

   public Map<String, Supplier<TypeTemplate>> registerEntities(Schema p_registerEntities_1_) {
      Map<String, Supplier<TypeTemplate>> map = super.registerEntities(p_registerEntities_1_);
      registerMob(p_registerEntities_1_, map, "ZombieVillager");
      registerMob(p_registerEntities_1_, map, "Husk");
      return map;
   }
}