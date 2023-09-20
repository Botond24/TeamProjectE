package net.minecraft.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * LootItemFunction that sets a stack's name.
 * The Component for the name is optionally resolved relative to a given {@link LootContext.EntityTarget} for entity-
 * sensitive component data such as scoreboard scores.
 */
public class SetName extends LootFunction {
   private static final Logger LOGGER = LogManager.getLogger();
   private final ITextComponent name;
   @Nullable
   private final LootContext.EntityTarget resolutionContext;

   private SetName(ILootCondition[] pConditions, @Nullable ITextComponent pName, @Nullable LootContext.EntityTarget pResolutionContext) {
      super(pConditions);
      this.name = pName;
      this.resolutionContext = pResolutionContext;
   }

   public LootFunctionType getType() {
      return LootFunctionManager.SET_NAME;
   }

   /**
    * Get the parameters used by this object.
    */
   public Set<LootParameter<?>> getReferencedContextParams() {
      return this.resolutionContext != null ? ImmutableSet.of(this.resolutionContext.getParam()) : ImmutableSet.of();
   }

   /**
    * Create a UnaryOperator that resolves Components based on the given LootContext and EntityTarget.
    * This will replace for example score components.
    */
   public static UnaryOperator<ITextComponent> createResolver(LootContext pLootContext, @Nullable LootContext.EntityTarget pResolutionContext) {
      if (pResolutionContext != null) {
         Entity entity = pLootContext.getParamOrNull(pResolutionContext.getParam());
         if (entity != null) {
            CommandSource commandsource = entity.createCommandSourceStack().withPermission(2);
            return (p_215937_2_) -> {
               try {
                  return TextComponentUtils.updateForEntity(commandsource, p_215937_2_, entity, 0);
               } catch (CommandSyntaxException commandsyntaxexception) {
                  LOGGER.warn("Failed to resolve text component", (Throwable)commandsyntaxexception);
                  return p_215937_2_;
               }
            };
         }
      }

      return (p_215938_0_) -> {
         return p_215938_0_;
      };
   }

   /**
    * Called to perform the actual action of this function, after conditions have been checked.
    */
   public ItemStack run(ItemStack pStack, LootContext pContext) {
      if (this.name != null) {
         pStack.setHoverName(createResolver(pContext, this.resolutionContext).apply(this.name));
      }

      return pStack;
   }

   public static class Serializer extends LootFunction.Serializer<SetName> {
      /**
       * Serialize the value by putting its data into the JsonObject.
       */
      public void serialize(JsonObject pJson, SetName pValue, JsonSerializationContext pSerializationContext) {
         super.serialize(pJson, pValue, pSerializationContext);
         if (pValue.name != null) {
            pJson.add("name", ITextComponent.Serializer.toJsonTree(pValue.name));
         }

         if (pValue.resolutionContext != null) {
            pJson.add("entity", pSerializationContext.serialize(pValue.resolutionContext));
         }

      }

      public SetName deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, ILootCondition[] pConditions) {
         ITextComponent itextcomponent = ITextComponent.Serializer.fromJson(pObject.get("name"));
         LootContext.EntityTarget lootcontext$entitytarget = JSONUtils.getAsObject(pObject, "entity", (LootContext.EntityTarget)null, pDeserializationContext, LootContext.EntityTarget.class);
         return new SetName(pConditions, itextcomponent, lootcontext$entitytarget);
      }
   }
}