package net.minecraft.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

public class ModelsUtil {
   private final Optional<ResourceLocation> model;
   private final Set<StockTextureAliases> requiredSlots;
   private Optional<String> suffix;

   public ModelsUtil(Optional<ResourceLocation> pModel, Optional<String> pSuffix, StockTextureAliases... pRequiredSlots) {
      this.model = pModel;
      this.suffix = pSuffix;
      this.requiredSlots = ImmutableSet.copyOf(pRequiredSlots);
   }

   public ResourceLocation create(Block pModelBlock, ModelTextures pTextureMapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> pModelOutput) {
      return this.create(ModelsResourceUtil.getModelLocation(pModelBlock, this.suffix.orElse("")), pTextureMapping, pModelOutput);
   }

   public ResourceLocation createWithSuffix(Block pModelBlock, String pModelLocationSuffix, ModelTextures pTextureMapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> pModelOutput) {
      return this.create(ModelsResourceUtil.getModelLocation(pModelBlock, pModelLocationSuffix + (String)this.suffix.orElse("")), pTextureMapping, pModelOutput);
   }

   public ResourceLocation createWithOverride(Block pModelBlock, String pModelLocationSuffix, ModelTextures pTextureMapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> pModelOutput) {
      return this.create(ModelsResourceUtil.getModelLocation(pModelBlock, pModelLocationSuffix), pTextureMapping, pModelOutput);
   }

   public ResourceLocation create(ResourceLocation pModelLocation, ModelTextures pTextureMapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> pModelOutput) {
      Map<StockTextureAliases, ResourceLocation> map = this.createMap(pTextureMapping);
      pModelOutput.accept(pModelLocation, () -> {
         JsonObject jsonobject = new JsonObject();
         this.model.ifPresent((p_240231_1_) -> {
            jsonobject.addProperty("parent", p_240231_1_.toString());
         });
         if (!map.isEmpty()) {
            JsonObject jsonobject1 = new JsonObject();
            map.forEach((p_240230_1_, p_240230_2_) -> {
               jsonobject1.addProperty(p_240230_1_.getId(), p_240230_2_.toString());
            });
            jsonobject.add("textures", jsonobject1);
         }

         return jsonobject;
      });
      return pModelLocation;
   }

   private Map<StockTextureAliases, ResourceLocation> createMap(ModelTextures pTextureMapping) {
      return Streams.concat(this.requiredSlots.stream(), pTextureMapping.getForced()).collect(ImmutableMap.toImmutableMap(Function.identity(), pTextureMapping::get));
   }
}