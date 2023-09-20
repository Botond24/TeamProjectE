package net.minecraft.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.INameable;
import net.minecraft.util.JSONUtils;

/**
 * LootItemFunction that sets the stack's name by copying it from somewhere else, such as the killing player.
 */
public class CopyName extends LootFunction {
   private final CopyName.Source source;

   private CopyName(ILootCondition[] pConditions, CopyName.Source pNameSource) {
      super(pConditions);
      this.source = pNameSource;
   }

   public LootFunctionType getType() {
      return LootFunctionManager.COPY_NAME;
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
      Object object = pContext.getParamOrNull(this.source.param);
      if (object instanceof INameable) {
         INameable inameable = (INameable)object;
         if (inameable.hasCustomName()) {
            pStack.setHoverName(inameable.getDisplayName());
         }
      }

      return pStack;
   }

   public static LootFunction.Builder<?> copyName(CopyName.Source pSource) {
      return simpleBuilder((p_215891_1_) -> {
         return new CopyName(p_215891_1_, pSource);
      });
   }

   public static class Serializer extends LootFunction.Serializer<CopyName> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, CopyName pValue, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pValue, pSerializationContext);
         pJson.addProperty("source", pValue.source.name);
      }

      public CopyName deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, ILootCondition[] pConditions) {
         CopyName.Source copyname$source = CopyName.Source.getByName(JSONUtils.getAsString(pObject, "source"));
         return new CopyName(pConditions, copyname$source);
      }
   }

   public static enum Source {
      THIS("this", LootParameters.THIS_ENTITY),
      KILLER("killer", LootParameters.KILLER_ENTITY),
      KILLER_PLAYER("killer_player", LootParameters.LAST_DAMAGE_PLAYER),
      BLOCK_ENTITY("block_entity", LootParameters.BLOCK_ENTITY);

      public final String name;
      public final LootParameter<?> param;

      private Source(String pName, LootParameter<?> pParam) {
         this.name = pName;
         this.param = pParam;
      }

      public static CopyName.Source getByName(String pName) {
         for(CopyName.Source copyname$source : values()) {
            if (copyname$source.name.equals(pName)) {
               return copyname$source;
            }
         }

         throw new IllegalArgumentException("Invalid name source " + pName);
      }
   }
}