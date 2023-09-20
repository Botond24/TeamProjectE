package net.minecraft.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.text.ITextComponent;

/**
 * LootItemFunction that sets a stack's lore tag, optionally replacing any previously present lore.
 * The Components for the lore tag are optionally resolved relative to a given {@link LootContext.EntityTarget} for
 * entity-sensitive component data such as scoreboard scores.
 */
public class SetLore extends LootFunction {
   private final boolean replace;
   private final List<ITextComponent> lore;
   @Nullable
   private final LootContext.EntityTarget resolutionContext;

   public SetLore(ILootCondition[] pConditions, boolean pReplace, List<ITextComponent> pLore, @Nullable LootContext.EntityTarget pResolutionContext) {
      super(pConditions);
      this.replace = pReplace;
      this.lore = ImmutableList.copyOf(pLore);
      this.resolutionContext = pResolutionContext;
   }

   public LootFunctionType getType() {
      return LootFunctionManager.SET_LORE;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootParameter<?>> getReferencedContextParams() {
      return this.resolutionContext != null ? ImmutableSet.of(this.resolutionContext.getParam()) : ImmutableSet.of();
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      ListNBT listnbt = this.getLoreTag(pStack, !this.lore.isEmpty());
      if (listnbt != null) {
         if (this.replace) {
            listnbt.clear();
         }

         UnaryOperator<ITextComponent> unaryoperator = SetName.createResolver(pContext, this.resolutionContext);
         this.lore.stream().map(unaryoperator).map(ITextComponent.Serializer::toJson).map(StringNBT::valueOf).forEach(listnbt::add);
      }

      return pStack;
   }

   @Nullable
   private ListNBT getLoreTag(ItemStack pStack, boolean pCreateIfMissing) {
      CompoundNBT compoundnbt;
      if (pStack.hasTag()) {
         compoundnbt = pStack.getTag();
      } else {
         if (!pCreateIfMissing) {
            return null;
         }

         compoundnbt = new CompoundNBT();
         pStack.setTag(compoundnbt);
      }

      CompoundNBT compoundnbt1;
      if (compoundnbt.contains("display", 10)) {
         compoundnbt1 = compoundnbt.getCompound("display");
      } else {
         if (!pCreateIfMissing) {
            return null;
         }

         compoundnbt1 = new CompoundNBT();
         compoundnbt.put("display", compoundnbt1);
      }

      if (compoundnbt1.contains("Lore", 9)) {
         return compoundnbt1.getList("Lore", 8);
      } else if (pCreateIfMissing) {
         ListNBT listnbt = new ListNBT();
         compoundnbt1.put("Lore", listnbt);
         return listnbt;
      } else {
         return null;
      }
   }

   public static class Serializer extends LootFunction.Serializer<SetLore> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, SetLore pValue, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pValue, pSerializationContext);
         pJson.addProperty("replace", pValue.replace);
         JsonArray jsonarray = new JsonArray();

         for(ITextComponent itextcomponent : pValue.lore) {
            jsonarray.add(ITextComponent.Serializer.toJsonTree(itextcomponent));
         }

         pJson.add("lore", jsonarray);
         if (pValue.resolutionContext != null) {
            pJson.add("entity", pSerializationContext.serialize(pValue.resolutionContext));
         }

      }

      public SetLore deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, ILootCondition[] pConditions) {
         boolean flag = JSONUtils.getAsBoolean(pObject, "replace", false);
         List<ITextComponent> list = Streams.stream(JSONUtils.getAsJsonArray(pObject, "lore")).map(ITextComponent.Serializer::fromJson).collect(ImmutableList.toImmutableList());
         LootContext.EntityTarget lootcontext$entitytarget = JSONUtils.getAsObject(pObject, "entity", (LootContext.EntityTarget)null, pDeserializationContext, LootContext.EntityTarget.class);
         return new SetLore(pConditions, flag, list, lootcontext$entitytarget);
      }
   }
}