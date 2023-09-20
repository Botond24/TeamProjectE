package net.minecraft.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.advancements.criterion.NBTPredicate;
import net.minecraft.command.arguments.NBTPathArgument;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.JSONUtils;

/**
 * LootItemFunction that takes the NBT from  an {@link NbtProvider} and applies a set of copy operations that copy from
 * that source NBT into the stack's NBT.
 */
public class CopyNbt extends LootFunction {
   private final CopyNbt.Source source;
   private final List<CopyNbt.Operation> operations;
   private static final Function<Entity, INBT> ENTITY_GETTER = NBTPredicate::getEntityTagToCompare;
   private static final Function<TileEntity, INBT> BLOCK_ENTITY_GETTER = (p_215882_0_) -> {
      return p_215882_0_.save(new CompoundNBT());
   };

   private CopyNbt(ILootCondition[] p_i51240_1_, CopyNbt.Source p_i51240_2_, List<CopyNbt.Operation> p_i51240_3_) {
      super(p_i51240_1_);
      this.source = p_i51240_2_;
      this.operations = ImmutableList.copyOf(p_i51240_3_);
   }

   public LootFunctionType getType() {
      return LootFunctionManager.COPY_NBT;
   }

   private static NBTPathArgument.NBTPath compileNbtPath(String pNbtPath) {
      try {
         return (new NBTPathArgument()).parse(new StringReader(pNbtPath));
      } catch (CommandSyntaxException commandsyntaxexception) {
         throw new IllegalArgumentException("Failed to parse path " + pNbtPath, commandsyntaxexception);
      }
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootParameter<?>> getReferencedContextParams() {
      return ImmutableSet.of(this.source.param);
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      INBT inbt = this.source.getter.apply(pContext);
      if (inbt != null) {
         this.operations.forEach((p_215885_2_) -> {
            p_215885_2_.apply(pStack::getOrCreateTag, inbt);
         });
      }

      return pStack;
   }

   public static CopyNbt.Builder copyData(CopyNbt.Source p_215881_0_) {
      return new CopyNbt.Builder(p_215881_0_);
   }

   public static enum Action {
      REPLACE("replace") {
         public void merge(INBT pTargetNbt, NBTPathArgument.NBTPath pNbtPath, List<INBT> pSourceNbt) throws CommandSyntaxException {
            pNbtPath.set(pTargetNbt, Iterables.getLast(pSourceNbt)::copy);
         }
      },
      APPEND("append") {
         public void merge(INBT pTargetNbt, NBTPathArgument.NBTPath pNbtPath, List<INBT> pSourceNbt) throws CommandSyntaxException {
            List<INBT> list = pNbtPath.getOrCreate(pTargetNbt, ListNBT::new);
            list.forEach((p_216232_1_) -> {
               if (p_216232_1_ instanceof ListNBT) {
                  pSourceNbt.forEach((p_216231_1_) -> {
                     ((ListNBT)p_216232_1_).add(p_216231_1_.copy());
                  });
               }

            });
         }
      },
      MERGE("merge") {
         public void merge(INBT pTargetNbt, NBTPathArgument.NBTPath pNbtPath, List<INBT> pSourceNbt) throws CommandSyntaxException {
            List<INBT> list = pNbtPath.getOrCreate(pTargetNbt, CompoundNBT::new);
            list.forEach((p_216234_1_) -> {
               if (p_216234_1_ instanceof CompoundNBT) {
                  pSourceNbt.forEach((p_216233_1_) -> {
                     if (p_216233_1_ instanceof CompoundNBT) {
                        ((CompoundNBT)p_216234_1_).merge((CompoundNBT)p_216233_1_);
                     }

                  });
               }

            });
         }
      };

      private final String name;

      public abstract void merge(INBT pTargetNbt, NBTPathArgument.NBTPath pNbtPath, List<INBT> pSourceNbt) throws CommandSyntaxException;

      private Action(String pName) {
         this.name = pName;
      }

      public static CopyNbt.Action getByName(String pName) {
         for(CopyNbt.Action copynbt$action : values()) {
            if (copynbt$action.name.equals(pName)) {
               return copynbt$action;
            }
         }

         throw new IllegalArgumentException("Invalid merge strategy" + pName);
      }
   }

   public static class Builder extends LootFunction.Builder<CopyNbt.Builder> {
      private final CopyNbt.Source source;
      private final List<CopyNbt.Operation> ops = Lists.newArrayList();

      private Builder(CopyNbt.Source p_i50675_1_) {
         this.source = p_i50675_1_;
      }

      public CopyNbt.Builder copy(String pSourcePath, String pTargetPath, CopyNbt.Action pCopyAction) {
         this.ops.add(new CopyNbt.Operation(pSourcePath, pTargetPath, pCopyAction));
         return this;
      }

      public CopyNbt.Builder copy(String pSourcePath, String pTargetPath) {
         return this.copy(pSourcePath, pTargetPath, CopyNbt.Action.REPLACE);
      }

      protected CopyNbt.Builder getThis() {
         return this;
      }

      public ILootFunction build() {
         return new CopyNbt(this.getConditions(), this.source, this.ops);
      }
   }

   static class Operation {
      private final String sourcePathText;
      private final NBTPathArgument.NBTPath sourcePath;
      private final String targetPathText;
      private final NBTPathArgument.NBTPath targetPath;
      private final CopyNbt.Action op;

      private Operation(String pSourcePathText, String pTargetPathText, CopyNbt.Action pMergeStrategy) {
         this.sourcePathText = pSourcePathText;
         this.sourcePath = CopyNbt.compileNbtPath(pSourcePathText);
         this.targetPathText = pTargetPathText;
         this.targetPath = CopyNbt.compileNbtPath(pTargetPathText);
         this.op = pMergeStrategy;
      }

      public void apply(Supplier<INBT> pTargetTag, INBT pSourceTag) {
         try {
            List<INBT> list = this.sourcePath.get(pSourceTag);
            if (!list.isEmpty()) {
               this.op.merge(pTargetTag.get(), this.targetPath, list);
            }
         } catch (CommandSyntaxException commandsyntaxexception) {
         }

      }

      public JsonObject toJson() {
         JsonObject jsonobject = new JsonObject();
         jsonobject.addProperty("source", this.sourcePathText);
         jsonobject.addProperty("target", this.targetPathText);
         jsonobject.addProperty("op", this.op.name);
         return jsonobject;
      }

      public static CopyNbt.Operation fromJson(JsonObject pJson) {
         String s = JSONUtils.getAsString(pJson, "source");
         String s1 = JSONUtils.getAsString(pJson, "target");
         CopyNbt.Action copynbt$action = CopyNbt.Action.getByName(JSONUtils.getAsString(pJson, "op"));
         return new CopyNbt.Operation(s, s1, copynbt$action);
      }
   }

   public static class Serializer extends LootFunction.Serializer<CopyNbt> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, CopyNbt pValue, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pValue, pSerializationContext);
         pJson.addProperty("source", pValue.source.name);
         JsonArray jsonarray = new JsonArray();
         pValue.operations.stream().map(CopyNbt.Operation::toJson).forEach(jsonarray::add);
         pJson.add("ops", jsonarray);
      }

      public CopyNbt deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, ILootCondition[] pConditions) {
         CopyNbt.Source copynbt$source = CopyNbt.Source.getByName(JSONUtils.getAsString(pObject, "source"));
         List<CopyNbt.Operation> list = Lists.newArrayList();

         for(JsonElement jsonelement : JSONUtils.getAsJsonArray(pObject, "ops")) {
            JsonObject jsonobject = JSONUtils.convertToJsonObject(jsonelement, "op");
            list.add(CopyNbt.Operation.fromJson(jsonobject));
         }

         return new CopyNbt(pConditions, copynbt$source, list);
      }
   }

   public static enum Source {
      THIS("this", LootParameters.THIS_ENTITY, CopyNbt.ENTITY_GETTER),
      KILLER("killer", LootParameters.KILLER_ENTITY, CopyNbt.ENTITY_GETTER),
      KILLER_PLAYER("killer_player", LootParameters.LAST_DAMAGE_PLAYER, CopyNbt.ENTITY_GETTER),
      BLOCK_ENTITY("block_entity", LootParameters.BLOCK_ENTITY, CopyNbt.BLOCK_ENTITY_GETTER);

      public final String name;
      public final LootParameter<?> param;
      public final Function<LootContext, INBT> getter;

      private <T> Source(String p_i50672_3_, LootParameter<T> p_i50672_4_, Function<? super T, INBT> p_i50672_5_) {
         this.name = p_i50672_3_;
         this.param = p_i50672_4_;
         this.getter = (p_216222_2_) -> {
            T t = p_216222_2_.getParamOrNull(p_i50672_4_);
            return t != null ? p_i50672_5_.apply(t) : null;
         };
      }

      public static CopyNbt.Source getByName(String p_216223_0_) {
         for(CopyNbt.Source copynbt$source : values()) {
            if (copynbt$source.name.equals(p_216223_0_)) {
               return copynbt$source;
            }
         }

         throw new IllegalArgumentException("Invalid tag source " + p_216223_0_);
      }
   }
}