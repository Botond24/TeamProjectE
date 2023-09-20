package net.minecraft.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Arrays;
import java.util.List;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootEntry;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.ValidationTracker;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;

/**
 * LootItemFunction that sets the contents of a container such as a chest by setting the {@code BlocKEntityTag} of the
 * stacks.
 * The contents are based on a list of loot pools.
 */
public class SetContents extends LootFunction {
   private final List<LootEntry> entries;

   private SetContents(ILootCondition[] pConditions, List<LootEntry> p_i51226_2_) {
      super(pConditions);
      this.entries = ImmutableList.copyOf(p_i51226_2_);
   }

   public LootFunctionType getType() {
      return LootFunctionManager.SET_CONTENTS;
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      if (pStack.isEmpty()) {
         return pStack;
      } else {
         NonNullList<ItemStack> nonnulllist = NonNullList.create();
         this.entries.forEach((p_215921_2_) -> {
            p_215921_2_.expand(pContext, (p_215922_2_) -> {
               p_215922_2_.createItemStack(LootTable.createStackSplitter(nonnulllist::add), pContext);
            });
         });
         CompoundNBT compoundnbt = new CompoundNBT();
         ItemStackHelper.saveAllItems(compoundnbt, nonnulllist);
         CompoundNBT compoundnbt1 = pStack.getOrCreateTag();
         compoundnbt1.put("BlockEntityTag", compoundnbt.merge(compoundnbt1.getCompound("BlockEntityTag")));
         return pStack;
      }
   }

   /**
    * Validate that this object is used correctly according to the given ValidationContext.
    */
   public void validate(ValidationTracker pContext) {
      super.validate(pContext);

      for(int i = 0; i < this.entries.size(); ++i) {
         this.entries.get(i).validate(pContext.forChild(".entry[" + i + "]"));
      }

   }

   public static SetContents.Builder setContents() {
      return new SetContents.Builder();
   }

   public static class Builder extends LootFunction.Builder<SetContents.Builder> {
      private final List<LootEntry> entries = Lists.newArrayList();

      protected SetContents.Builder getThis() {
         return this;
      }

      public SetContents.Builder withEntry(LootEntry.Builder<?> pLootEntryBuilder) {
         this.entries.add(pLootEntryBuilder.build());
         return this;
      }

      public ILootFunction build() {
         return new SetContents(this.getConditions(), this.entries);
      }
   }

   public static class Serializer extends LootFunction.Serializer<SetContents> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, SetContents pValue, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pValue, pSerializationContext);
         pJson.add("entries", pSerializationContext.serialize(pValue.entries));
      }

      public SetContents deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, ILootCondition[] pConditions) {
         LootEntry[] alootentry = JSONUtils.getAsObject(pObject, "entries", pDeserializationContext, LootEntry[].class);
         return new SetContents(pConditions, Arrays.asList(alootentry));
      }
   }
}