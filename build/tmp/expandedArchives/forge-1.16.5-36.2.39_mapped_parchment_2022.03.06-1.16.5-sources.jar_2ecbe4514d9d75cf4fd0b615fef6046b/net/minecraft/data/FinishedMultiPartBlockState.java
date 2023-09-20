package net.minecraft.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer;

public class FinishedMultiPartBlockState implements IFinishedBlockState {
   private final Block block;
   private final List<FinishedMultiPartBlockState.Part> parts = Lists.newArrayList();

   private FinishedMultiPartBlockState(Block pBlock) {
      this.block = pBlock;
   }

   public Block getBlock() {
      return this.block;
   }

   public static FinishedMultiPartBlockState multiPart(Block pBlock) {
      return new FinishedMultiPartBlockState(pBlock);
   }

   public FinishedMultiPartBlockState with(List<BlockModelDefinition> pVariants) {
      this.parts.add(new FinishedMultiPartBlockState.Part(pVariants));
      return this;
   }

   public FinishedMultiPartBlockState with(BlockModelDefinition pVariant) {
      return this.with(ImmutableList.of(pVariant));
   }

   public FinishedMultiPartBlockState with(IMultiPartPredicateBuilder pCondition, List<BlockModelDefinition> pVariants) {
      this.parts.add(new FinishedMultiPartBlockState.ConditionalPart(pCondition, pVariants));
      return this;
   }

   public FinishedMultiPartBlockState with(IMultiPartPredicateBuilder pCondition, BlockModelDefinition... pVariants) {
      return this.with(pCondition, ImmutableList.copyOf(pVariants));
   }

   public FinishedMultiPartBlockState with(IMultiPartPredicateBuilder pCondition, BlockModelDefinition pVariant) {
      return this.with(pCondition, ImmutableList.of(pVariant));
   }

   public JsonElement get() {
      StateContainer<Block, BlockState> statecontainer = this.block.getStateDefinition();
      this.parts.forEach((p_240107_1_) -> {
         p_240107_1_.validate(statecontainer);
      });
      JsonArray jsonarray = new JsonArray();
      this.parts.stream().map(FinishedMultiPartBlockState.Part::get).forEach(jsonarray::add);
      JsonObject jsonobject = new JsonObject();
      jsonobject.add("multipart", jsonarray);
      return jsonobject;
   }

   static class ConditionalPart extends FinishedMultiPartBlockState.Part {
      private final IMultiPartPredicateBuilder condition;

      private ConditionalPart(IMultiPartPredicateBuilder pCondition, List<BlockModelDefinition> pVariants) {
         super(pVariants);
         this.condition = pCondition;
      }

      public void validate(StateContainer<?, ?> pStateDefinition) {
         this.condition.validate(pStateDefinition);
      }

      public void decorate(JsonObject pJsonObject) {
         pJsonObject.add("when", this.condition.get());
      }
   }

   static class Part implements Supplier<JsonElement> {
      private final List<BlockModelDefinition> variants;

      private Part(List<BlockModelDefinition> pVariants) {
         this.variants = pVariants;
      }

      public void validate(StateContainer<?, ?> pStateDefinition) {
      }

      public void decorate(JsonObject pJsonObject) {
      }

      public JsonElement get() {
         JsonObject jsonobject = new JsonObject();
         this.decorate(jsonobject);
         jsonobject.add("apply", BlockModelDefinition.convertList(this.variants));
         return jsonobject;
      }
   }
}