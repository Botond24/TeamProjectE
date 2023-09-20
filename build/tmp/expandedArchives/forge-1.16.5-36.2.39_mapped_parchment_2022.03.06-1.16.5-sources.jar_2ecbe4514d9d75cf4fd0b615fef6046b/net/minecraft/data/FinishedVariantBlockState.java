package net.minecraft.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.state.Property;
import net.minecraft.util.Util;

public class FinishedVariantBlockState implements IFinishedBlockState {
   private final Block block;
   private final List<BlockModelDefinition> baseVariants;
   private final Set<Property<?>> seenProperties = Sets.newHashSet();
   private final List<BlockStateVariantBuilder> declaredPropertySets = Lists.newArrayList();

   private FinishedVariantBlockState(Block pBlock, List<BlockModelDefinition> pBaseVariants) {
      this.block = pBlock;
      this.baseVariants = pBaseVariants;
   }

   public FinishedVariantBlockState with(BlockStateVariantBuilder pPropertyDispatch) {
      pPropertyDispatch.getDefinedProperties().forEach((p_240122_1_) -> {
         if (this.block.getStateDefinition().getProperty(p_240122_1_.getName()) != p_240122_1_) {
            throw new IllegalStateException("Property " + p_240122_1_ + " is not defined for block " + this.block);
         } else if (!this.seenProperties.add(p_240122_1_)) {
            throw new IllegalStateException("Values of property " + p_240122_1_ + " already defined for block " + this.block);
         }
      });
      this.declaredPropertySets.add(pPropertyDispatch);
      return this;
   }

   public JsonElement get() {
      Stream<Pair<VariantPropertyBuilder, List<BlockModelDefinition>>> stream = Stream.of(Pair.of(VariantPropertyBuilder.empty(), this.baseVariants));

      for(BlockStateVariantBuilder blockstatevariantbuilder : this.declaredPropertySets) {
         Map<VariantPropertyBuilder, List<BlockModelDefinition>> map = blockstatevariantbuilder.getEntries();
         stream = stream.flatMap((p_240130_1_) -> {
            return map.entrySet().stream().map((p_240124_1_) -> {
               VariantPropertyBuilder variantpropertybuilder = ((VariantPropertyBuilder)p_240130_1_.getFirst()).extend(p_240124_1_.getKey());
               List<BlockModelDefinition> list = mergeVariants((List)p_240130_1_.getSecond(), p_240124_1_.getValue());
               return Pair.of(variantpropertybuilder, list);
            });
         });
      }

      Map<String, JsonElement> map1 = new TreeMap<>();
      stream.forEach((p_240129_1_) -> {
         JsonElement jsonelement = map1.put(p_240129_1_.getFirst().getKey(), BlockModelDefinition.convertList(p_240129_1_.getSecond()));
      });
      JsonObject jsonobject = new JsonObject();
      jsonobject.add("variants", Util.make(new JsonObject(), (p_240128_1_) -> {
         map1.forEach(p_240128_1_::add);
      }));
      return jsonobject;
   }

   private static List<BlockModelDefinition> mergeVariants(List<BlockModelDefinition> pVariants1, List<BlockModelDefinition> pVariants2) {
      Builder<BlockModelDefinition> builder = ImmutableList.builder();
      pVariants1.forEach((p_240126_2_) -> {
         pVariants2.forEach((p_240123_2_) -> {
            builder.add(BlockModelDefinition.merge(p_240126_2_, p_240123_2_));
         });
      });
      return builder.build();
   }

   public Block getBlock() {
      return this.block;
   }

   public static FinishedVariantBlockState multiVariant(Block pBlock) {
      return new FinishedVariantBlockState(pBlock, ImmutableList.of(BlockModelDefinition.variant()));
   }

   public static FinishedVariantBlockState multiVariant(Block pBlock, BlockModelDefinition pVariant) {
      return new FinishedVariantBlockState(pBlock, ImmutableList.of(pVariant));
   }

   public static FinishedVariantBlockState multiVariant(Block pBlock, BlockModelDefinition... pVariants) {
      return new FinishedVariantBlockState(pBlock, ImmutableList.copyOf(pVariants));
   }
}