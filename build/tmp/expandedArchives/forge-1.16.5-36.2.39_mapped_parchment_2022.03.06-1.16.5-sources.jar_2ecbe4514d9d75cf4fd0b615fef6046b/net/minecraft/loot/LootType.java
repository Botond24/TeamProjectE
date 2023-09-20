package net.minecraft.loot;

/**
 * Represents the registry entry for a serializer for some type T. For example every type of {@link NumberProvider} has
 * a {@link LootNumberProviderType} (which extends SerializerType) that stores its serializer and is registered to a
 * registry to provide the type name in form of the registry ResourceLocation.
 */
public class LootType<T> {
   private final ILootSerializer<? extends T> serializer;

   public LootType(ILootSerializer<? extends T> pSerializer) {
      this.serializer = pSerializer;
   }

   public ILootSerializer<? extends T> getSerializer() {
      return this.serializer;
   }
}