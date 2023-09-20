package net.minecraft.data;

import java.nio.file.Path;
import net.minecraft.entity.EntityType;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

public class EntityTypeTagsProvider extends TagsProvider<EntityType<?>> {
   @Deprecated
   public EntityTypeTagsProvider(DataGenerator pGenerator) {
      super(pGenerator, Registry.ENTITY_TYPE);
   }
   public EntityTypeTagsProvider(DataGenerator pGenerator, String modId, @javax.annotation.Nullable net.minecraftforge.common.data.ExistingFileHelper existingFileHelper) {
      super(pGenerator, Registry.ENTITY_TYPE, modId, existingFileHelper);
   }

   protected void addTags() {
      this.tag(EntityTypeTags.SKELETONS).add(EntityType.SKELETON, EntityType.STRAY, EntityType.WITHER_SKELETON);
      this.tag(EntityTypeTags.RAIDERS).add(EntityType.EVOKER, EntityType.PILLAGER, EntityType.RAVAGER, EntityType.VINDICATOR, EntityType.ILLUSIONER, EntityType.WITCH);
      this.tag(EntityTypeTags.BEEHIVE_INHABITORS).add(EntityType.BEE);
      this.tag(EntityTypeTags.ARROWS).add(EntityType.ARROW, EntityType.SPECTRAL_ARROW);
      this.tag(EntityTypeTags.IMPACT_PROJECTILES).addTag(EntityTypeTags.ARROWS).add(EntityType.SNOWBALL, EntityType.FIREBALL, EntityType.SMALL_FIREBALL, EntityType.EGG, EntityType.TRIDENT, EntityType.DRAGON_FIREBALL, EntityType.WITHER_SKULL);
   }

   /**
    * Resolves a Path for the location to save the given tag.
    */
   protected Path getPath(ResourceLocation pId) {
      return this.generator.getOutputFolder().resolve("data/" + pId.getNamespace() + "/tags/entity_types/" + pId.getPath() + ".json");
   }

   /**
    * Gets a name for this provider, to use in logging.
    */
   public String getName() {
      return "Entity Type Tags";
   }
}
