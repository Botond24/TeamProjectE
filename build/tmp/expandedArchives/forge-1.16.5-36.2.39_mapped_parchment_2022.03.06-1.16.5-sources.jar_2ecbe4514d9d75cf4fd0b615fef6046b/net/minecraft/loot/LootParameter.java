package net.minecraft.loot;

import net.minecraft.util.ResourceLocation;

/**
 * A parameter of a {@link LootContext}.
 * 
 * @see LootContextParams
 */
public class LootParameter<T> {
   private final ResourceLocation name;

   public LootParameter(ResourceLocation pName) {
      this.name = pName;
   }

   public ResourceLocation getName() {
      return this.name;
   }

   public String toString() {
      return "<parameter " + this.name + ">";
   }
}