package net.minecraft.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.function.Consumer;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.JSONUtils;

/**
 * Base class for loot pool entry containers that delegate to one or more children.
 * The actual functionality is provided by composing the children into one composed container (see {@link #compose}).
 */
public abstract class ParentedLootEntry extends LootEntry {
   protected final LootEntry[] children;
   private final ILootEntry composedChildren;

   protected ParentedLootEntry(LootEntry[] pChildren, ILootCondition[] pConditions) {
      super(pConditions);
      this.children = pChildren;
      this.composedChildren = this.compose(pChildren);
   }

   public void validate(ValidationTracker pValidationContext) {
      super.validate(pValidationContext);
      if (this.children.length == 0) {
         pValidationContext.reportProblem("Empty children list");
      }

      for(int i = 0; i < this.children.length; ++i) {
         this.children[i].validate(pValidationContext.forChild(".entry[" + i + "]"));
      }

   }

   /**
    * Compose the given children into one container.
    */
   protected abstract ILootEntry compose(ILootEntry[] pEntries);

   public final boolean expand(LootContext p_expand_1_, Consumer<ILootGenerator> p_expand_2_) {
      return !this.canRun(p_expand_1_) ? false : this.composedChildren.expand(p_expand_1_, p_expand_2_);
   }

   public static <T extends ParentedLootEntry> LootEntry.Serializer<T> createSerializer(final ParentedLootEntry.IFactory<T> pFactory) {
      return new LootEntry.Serializer<T>() {
         public void serializeCustom(JsonObject pObject, T pContext, JsonSerializationContext pConditions) {
            pObject.add("children", pConditions.serialize(pContext.children));
         }

         public final T deserializeCustom(JsonObject pObject, JsonDeserializationContext pContext, ILootCondition[] pConditions) {
            LootEntry[] alootentry = JSONUtils.getAsObject(pObject, "children", pContext, LootEntry[].class);
            return pFactory.create(alootentry, pConditions);
         }
      };
   }

   @FunctionalInterface
   public interface IFactory<T extends ParentedLootEntry> {
      T create(LootEntry[] p_create_1_, ILootCondition[] p_create_2_);
   }
}