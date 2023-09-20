package net.minecraft.tags;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;

public interface ITag<T> {
   static <T> Codec<ITag<T>> codec(Supplier<ITagCollection<T>> pCollectionSupplier) {
      return ResourceLocation.CODEC.flatXmap((p_232949_1_) -> {
         return Optional.ofNullable(pCollectionSupplier.get().getTag(p_232949_1_)).map(DataResult::success).orElseGet(() -> {
            return DataResult.error("Unknown tag: " + p_232949_1_);
         });
      }, (p_232948_1_) -> {
         return Optional.ofNullable(pCollectionSupplier.get().getId(p_232948_1_)).map(DataResult::success).orElseGet(() -> {
            return DataResult.error("Unknown tag: " + p_232948_1_);
         });
      });
   }

   boolean contains(T pElement);

   List<T> getValues();

   default T getRandomElement(Random pRandom) {
      List<T> list = this.getValues();
      return list.get(pRandom.nextInt(list.size()));
   }

   static <T> ITag<T> fromSet(Set<T> pElements) {
      return Tag.create(pElements);
   }

   public static class Builder {
      private final List<ITag.Proxy> entries = Lists.newArrayList();
      private boolean replace = false;

      public static ITag.Builder tag() {
         return new ITag.Builder();
      }

      public ITag.Builder add(ITag.Proxy pProxyTag) {
         this.entries.add(pProxyTag);
         return this;
      }

      public ITag.Builder add(ITag.ITagEntry pTagEntry, String pIdentifier) {
         return this.add(new ITag.Proxy(pTagEntry, pIdentifier));
      }

      public ITag.Builder addElement(ResourceLocation pRegistryName, String pIdentifier) {
         return this.add(new ITag.ItemEntry(pRegistryName), pIdentifier);
      }

      public ITag.Builder addTag(ResourceLocation pTag, String pIdentifier) {
         return this.add(new ITag.TagEntry(pTag), pIdentifier);
      }

      public ITag.Builder replace(boolean value) {
         this.replace = value;
         return this;
      }

      public ITag.Builder replace() {
         return replace(true);
      }

      public <T> Optional<ITag<T>> build(Function<ResourceLocation, ITag<T>> p_232959_1_, Function<ResourceLocation, T> p_232959_2_) {
         ImmutableSet.Builder<T> builder = ImmutableSet.builder();

         for(ITag.Proxy itag$proxy : this.entries) {
            if (!itag$proxy.getEntry().build(p_232959_1_, p_232959_2_, builder::add)) {
               return Optional.empty();
            }
         }

         return Optional.of(ITag.fromSet(builder.build()));
      }

      public Stream<ITag.Proxy> getEntries() {
         return this.entries.stream();
      }

      public <T> Stream<ITag.Proxy> getUnresolvedEntries(Function<ResourceLocation, ITag<T>> p_232963_1_, Function<ResourceLocation, T> p_232963_2_) {
         return this.getEntries().filter((p_232960_2_) -> {
            return !p_232960_2_.getEntry().build(p_232963_1_, p_232963_2_, (p_232957_0_) -> {
            });
         });
      }

      public ITag.Builder addFromJson(JsonObject pJson, String pIdentifier) {
         JsonArray jsonarray = JSONUtils.getAsJsonArray(pJson, "values");
         List<ITag.ITagEntry> list = Lists.newArrayList();

         for(JsonElement jsonelement : jsonarray) {
            list.add(parseEntry(jsonelement));
         }

         if (JSONUtils.getAsBoolean(pJson, "replace", false)) {
            this.entries.clear();
         }

         net.minecraftforge.common.ForgeHooks.deserializeTagAdditions(list, pJson, entries);
         list.forEach((p_232958_2_) -> {
            this.entries.add(new ITag.Proxy(p_232958_2_, pIdentifier));
         });
         return this;
      }

      private static ITag.ITagEntry parseEntry(JsonElement pJson) {
         String s;
         boolean flag;
         if (pJson.isJsonObject()) {
            JsonObject jsonobject = pJson.getAsJsonObject();
            s = JSONUtils.getAsString(jsonobject, "id");
            flag = JSONUtils.getAsBoolean(jsonobject, "required", true);
         } else {
            s = JSONUtils.convertToString(pJson, "id");
            flag = true;
         }

         if (s.startsWith("#")) {
            ResourceLocation resourcelocation1 = new ResourceLocation(s.substring(1));
            return (ITag.ITagEntry)(flag ? new ITag.TagEntry(resourcelocation1) : new ITag.OptionalTagEntry(resourcelocation1));
         } else {
            ResourceLocation resourcelocation = new ResourceLocation(s);
            return (ITag.ITagEntry)(flag ? new ITag.ItemEntry(resourcelocation) : new ITag.OptionalItemEntry(resourcelocation));
         }
      }

      public JsonObject serializeToJson() {
         JsonObject jsonobject = new JsonObject();
         JsonArray jsonarray = new JsonArray();

         for(ITag.Proxy itag$proxy : this.entries) {
            itag$proxy.getEntry().serializeTo(jsonarray);
         }

         jsonobject.addProperty("replace", replace);
         jsonobject.add("values", jsonarray);
         return jsonobject;
      }
   }

   public interface INamedTag<T> extends ITag<T> {
      ResourceLocation getName();
   }

   public interface ITagEntry {
      <T> boolean build(Function<ResourceLocation, ITag<T>> pResourceTagFunction, Function<ResourceLocation, T> pResourceElementFunction, Consumer<T> pElementConsumer);

      void serializeTo(JsonArray pJsonArray);
   }

   public static class ItemEntry implements ITag.ITagEntry {
      private final ResourceLocation id;

      public ItemEntry(ResourceLocation p_i231435_1_) {
         this.id = p_i231435_1_;
      }

      public <T> boolean build(Function<ResourceLocation, ITag<T>> pResourceTagFunction, Function<ResourceLocation, T> pResourceElementFunction, Consumer<T> pElementConsumer) {
         T t = pResourceElementFunction.apply(this.id);
         if (t == null) {
            return false;
         } else {
            pElementConsumer.accept(t);
            return true;
         }
      }

      public void serializeTo(JsonArray pJsonArray) {
         pJsonArray.add(this.id.toString());
      }

      public String toString() {
         return this.id.toString();
      }
      @Override public boolean equals(Object o) { return o == this || (o instanceof ITag.ItemEntry && java.util.Objects.equals(this.id, ((ITag.ItemEntry) o).id)); }
   }

   public static class OptionalItemEntry implements ITag.ITagEntry {
      private final ResourceLocation id;

      public OptionalItemEntry(ResourceLocation p_i241895_1_) {
         this.id = p_i241895_1_;
      }

      public <T> boolean build(Function<ResourceLocation, ITag<T>> pResourceTagFunction, Function<ResourceLocation, T> pResourceElementFunction, Consumer<T> pElementConsumer) {
         T t = pResourceElementFunction.apply(this.id);
         if (t != null) {
            pElementConsumer.accept(t);
         }

         return true;
      }

      public void serializeTo(JsonArray pJsonArray) {
         JsonObject jsonobject = new JsonObject();
         jsonobject.addProperty("id", this.id.toString());
         jsonobject.addProperty("required", false);
         pJsonArray.add(jsonobject);
      }

      public String toString() {
         return this.id.toString() + "?";
      }
   }

   public static class OptionalTagEntry implements ITag.ITagEntry {
      private final ResourceLocation id;

      public OptionalTagEntry(ResourceLocation p_i241896_1_) {
         this.id = p_i241896_1_;
      }

      public <T> boolean build(Function<ResourceLocation, ITag<T>> pResourceTagFunction, Function<ResourceLocation, T> pResourceElementFunction, Consumer<T> pElementConsumer) {
         ITag<T> itag = pResourceTagFunction.apply(this.id);
         if (itag != null) {
            itag.getValues().forEach(pElementConsumer);
         }

         return true;
      }

      public void serializeTo(JsonArray pJsonArray) {
         JsonObject jsonobject = new JsonObject();
         jsonobject.addProperty("id", "#" + this.id);
         jsonobject.addProperty("required", false);
         pJsonArray.add(jsonobject);
      }

      public String toString() {
         return "#" + this.id + "?";
      }
   }

   public static class Proxy {
      private final ITag.ITagEntry entry;
      private final String source;

      private Proxy(ITag.ITagEntry p_i231433_1_, String p_i231433_2_) {
         this.entry = p_i231433_1_;
         this.source = p_i231433_2_;
      }

      public ITag.ITagEntry getEntry() {
         return this.entry;
      }

      public String toString() {
         return this.entry.toString() + " (from " + this.source + ")";
      }
   }

   public static class TagEntry implements ITag.ITagEntry {
      private final ResourceLocation id;

      public TagEntry(ResourceLocation p_i48228_1_) {
         this.id = p_i48228_1_;
      }

      public <T> boolean build(Function<ResourceLocation, ITag<T>> pResourceTagFunction, Function<ResourceLocation, T> pResourceElementFunction, Consumer<T> pElementConsumer) {
         ITag<T> itag = pResourceTagFunction.apply(this.id);
         if (itag == null) {
            return false;
         } else {
            itag.getValues().forEach(pElementConsumer);
            return true;
         }
      }

      public void serializeTo(JsonArray pJsonArray) {
         pJsonArray.add("#" + this.id);
      }

      public String toString() {
         return "#" + this.id;
      }
      @Override public boolean equals(Object o) { return o == this || (o instanceof ITag.TagEntry && java.util.Objects.equals(this.id, ((ITag.TagEntry) o).id)); }
      public ResourceLocation getId() { return id; }
   }
}
