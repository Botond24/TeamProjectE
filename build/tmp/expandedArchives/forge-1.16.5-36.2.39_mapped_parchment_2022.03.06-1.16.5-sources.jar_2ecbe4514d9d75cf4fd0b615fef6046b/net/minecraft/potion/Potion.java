package net.minecraft.potion;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

/**
 * Defines a type of potion in the game. These are used to associate one or more effects with items such as the bottled
 * potion or the tipped arrows.
 */
public class Potion extends net.minecraftforge.registries.ForgeRegistryEntry<Potion> {
   private final net.minecraftforge.common.util.ReverseTagWrapper<Potion> reverseTags = new net.minecraftforge.common.util.ReverseTagWrapper<>(this, () -> net.minecraft.tags.TagCollectionManager.getInstance().getCustomTypeCollection(net.minecraftforge.registries.ForgeRegistries.POTION_TYPES));
   /** The base name for the potion type. */
   private final String name;
   private final ImmutableList<EffectInstance> effects;

   /**
    * Attempts to find a Potion using a name. The name will be parsed as a namespaced identifier which will be used to
    * lookup the potion in the potion registry.
    * @return The potion that was found in the registry.
    * @param pName The name of the potion to search for. This name will be parsed as a namespaced identifier.
    */
   public static Potion byName(String pName) {
      return Registry.POTION.get(ResourceLocation.tryParse(pName));
   }

   public Potion(EffectInstance... pEffects) {
      this((String)null, pEffects);
   }

   public Potion(@Nullable String pName, EffectInstance... pEffects) {
      this.name = pName;
      this.effects = ImmutableList.copyOf(pEffects);
   }

   public java.util.Set<net.minecraft.util.ResourceLocation> getTags() {
      return reverseTags.getTagNames();
   }

   public boolean isIn(net.minecraft.tags.ITag<Potion> tag) {
      return tag.contains(this);
   }

   /**
    * Gets the prefixed potion name. This is often used to create a localization key for items like the tipped arrows or
    * bottled potion.
    * @return The prefixed potion name.
    * @param pPrefix The prefix to add on to the base name.
    */
   public String getName(String pPrefix) {
      return pPrefix + (this.name == null ? Registry.POTION.getKey(this).getPath() : this.name);
   }

   /**
    * Gets the base effects applied by the potion.
    * @return The effects applied by the potion.
    */
   public List<EffectInstance> getEffects() {
      return this.effects;
   }

   /**
    * Checks if the potion contains any instant effects such as instant health or instant damage.
    * @return Whether or not the potion contained an instant effect.
    */
   public boolean hasInstantEffects() {
      if (!this.effects.isEmpty()) {
         for(EffectInstance effectinstance : this.effects) {
            if (effectinstance.getEffect().isInstantenous()) {
               return true;
            }
         }
      }

      return false;
   }
}
