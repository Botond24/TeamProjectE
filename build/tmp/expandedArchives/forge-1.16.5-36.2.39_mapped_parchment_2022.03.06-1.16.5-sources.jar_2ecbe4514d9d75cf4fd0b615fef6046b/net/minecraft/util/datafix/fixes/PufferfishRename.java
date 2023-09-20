package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.schemas.Schema;
import java.util.Map;
import java.util.Objects;

public class PufferfishRename extends TypedEntityRenameHelper {
   public static final Map<String, String> RENAMED_IDS = ImmutableMap.<String, String>builder().put("minecraft:puffer_fish_spawn_egg", "minecraft:pufferfish_spawn_egg").build();

   public PufferfishRename(Schema p_i49658_1_, boolean p_i49658_2_) {
      super("EntityPufferfishRenameFix", p_i49658_1_, p_i49658_2_);
   }

   protected String rename(String pName) {
      return Objects.equals("minecraft:puffer_fish", pName) ? "minecraft:pufferfish" : pName;
   }
}