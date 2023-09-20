package net.minecraft.util.text.event;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HoverEvent {
   private static final Logger LOGGER = LogManager.getLogger();
   private final HoverEvent.Action<?> action;
   private final Object value;

   public <T> HoverEvent(HoverEvent.Action<T> pAction, T pValue) {
      this.action = pAction;
      this.value = pValue;
   }

   /**
    * Gets the action to perform when this event is raised.
    */
   public HoverEvent.Action<?> getAction() {
      return this.action;
   }

   @Nullable
   public <T> T getValue(HoverEvent.Action<T> pActionType) {
      return (T)(this.action == pActionType ? pActionType.cast(this.value) : null);
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
         HoverEvent hoverevent = (HoverEvent)p_equals_1_;
         return this.action == hoverevent.action && Objects.equals(this.value, hoverevent.value);
      } else {
         return false;
      }
   }

   public String toString() {
      return "HoverEvent{action=" + this.action + ", value='" + this.value + '\'' + '}';
   }

   public int hashCode() {
      int i = this.action.hashCode();
      return 31 * i + (this.value != null ? this.value.hashCode() : 0);
   }

   @Nullable
   public static HoverEvent deserialize(JsonObject pJson) {
      String s = JSONUtils.getAsString(pJson, "action", (String)null);
      if (s == null) {
         return null;
      } else {
         HoverEvent.Action<?> action = HoverEvent.Action.getByName(s);
         if (action == null) {
            return null;
         } else {
            JsonElement jsonelement = pJson.get("contents");
            if (jsonelement != null) {
               return action.deserialize(jsonelement);
            } else {
               ITextComponent itextcomponent = ITextComponent.Serializer.fromJson(pJson.get("value"));
               return itextcomponent != null ? action.deserializeFromLegacy(itextcomponent) : null;
            }
         }
      }
   }

   public JsonObject serialize() {
      JsonObject jsonobject = new JsonObject();
      jsonobject.addProperty("action", this.action.getName());
      jsonobject.add("contents", this.action.serializeArg(this.value));
      return jsonobject;
   }

   public static class Action<T> {
      public static final HoverEvent.Action<ITextComponent> SHOW_TEXT = new HoverEvent.Action<>("show_text", true, ITextComponent.Serializer::fromJson, ITextComponent.Serializer::toJsonTree, Function.identity());
      public static final HoverEvent.Action<HoverEvent.ItemHover> SHOW_ITEM = new HoverEvent.Action<>("show_item", true, (p_240673_0_) -> {
         return HoverEvent.ItemHover.create(p_240673_0_);
      }, (p_240676_0_) -> {
         return p_240676_0_.serialize();
      }, (p_240675_0_) -> {
         return HoverEvent.ItemHover.create(p_240675_0_);
      });
      public static final HoverEvent.Action<HoverEvent.EntityHover> SHOW_ENTITY = new HoverEvent.Action<>("show_entity", true, HoverEvent.EntityHover::create, HoverEvent.EntityHover::serialize, HoverEvent.EntityHover::create);
      private static final Map<String, HoverEvent.Action> LOOKUP = Stream.of(SHOW_TEXT, SHOW_ITEM, SHOW_ENTITY).collect(ImmutableMap.toImmutableMap(HoverEvent.Action::getName, (p_240671_0_) -> {
         return p_240671_0_;
      }));
      private final String name;
      private final boolean allowFromServer;
      private final Function<JsonElement, T> argDeserializer;
      private final Function<T, JsonElement> argSerializer;
      private final Function<ITextComponent, T> legacyArgDeserializer;

      public Action(String p_i232565_1_, boolean p_i232565_2_, Function<JsonElement, T> p_i232565_3_, Function<T, JsonElement> p_i232565_4_, Function<ITextComponent, T> p_i232565_5_) {
         this.name = p_i232565_1_;
         this.allowFromServer = p_i232565_2_;
         this.argDeserializer = p_i232565_3_;
         this.argSerializer = p_i232565_4_;
         this.legacyArgDeserializer = p_i232565_5_;
      }

      /**
       * Indicates whether this event can be run from chat text.
       */
      public boolean isAllowedFromServer() {
         return this.allowFromServer;
      }

      /**
       * Gets the canonical name for this action (e.g., "show_achievement")
       */
      public String getName() {
         return this.name;
      }

      /**
       * Gets a value by its canonical name.
       */
      @Nullable
      public static HoverEvent.Action getByName(String pCanonicalName) {
         return LOOKUP.get(pCanonicalName);
      }

      private T cast(Object pParameter) {
         return (T)pParameter;
      }

      @Nullable
      public HoverEvent deserialize(JsonElement pElement) {
         T t = this.argDeserializer.apply(pElement);
         return t == null ? null : new HoverEvent(this, t);
      }

      @Nullable
      public HoverEvent deserializeFromLegacy(ITextComponent pComponent) {
         T t = this.legacyArgDeserializer.apply(pComponent);
         return t == null ? null : new HoverEvent(this, t);
      }

      public JsonElement serializeArg(Object pParameter) {
         return this.argSerializer.apply(this.cast(pParameter));
      }

      public String toString() {
         return "<action " + this.name + ">";
      }
   }

   public static class EntityHover {
      public final EntityType<?> type;
      public final UUID id;
      @Nullable
      public final ITextComponent name;
      @Nullable
      @OnlyIn(Dist.CLIENT)
      private List<ITextComponent> linesCache;

      public EntityHover(EntityType<?> pType, UUID pId, @Nullable ITextComponent pName) {
         this.type = pType;
         this.id = pId;
         this.name = pName;
      }

      @Nullable
      public static HoverEvent.EntityHover create(JsonElement pElement) {
         if (!pElement.isJsonObject()) {
            return null;
         } else {
            JsonObject jsonobject = pElement.getAsJsonObject();
            EntityType<?> entitytype = Registry.ENTITY_TYPE.get(new ResourceLocation(JSONUtils.getAsString(jsonobject, "type")));
            UUID uuid = UUID.fromString(JSONUtils.getAsString(jsonobject, "id"));
            ITextComponent itextcomponent = ITextComponent.Serializer.fromJson(jsonobject.get("name"));
            return new HoverEvent.EntityHover(entitytype, uuid, itextcomponent);
         }
      }

      @Nullable
      public static HoverEvent.EntityHover create(ITextComponent pComponent) {
         try {
            CompoundNBT compoundnbt = JsonToNBT.parseTag(pComponent.getString());
            ITextComponent itextcomponent = ITextComponent.Serializer.fromJson(compoundnbt.getString("name"));
            EntityType<?> entitytype = Registry.ENTITY_TYPE.get(new ResourceLocation(compoundnbt.getString("type")));
            UUID uuid = UUID.fromString(compoundnbt.getString("id"));
            return new HoverEvent.EntityHover(entitytype, uuid, itextcomponent);
         } catch (CommandSyntaxException | JsonSyntaxException jsonsyntaxexception) {
            return null;
         }
      }

      public JsonElement serialize() {
         JsonObject jsonobject = new JsonObject();
         jsonobject.addProperty("type", Registry.ENTITY_TYPE.getKey(this.type).toString());
         jsonobject.addProperty("id", this.id.toString());
         if (this.name != null) {
            jsonobject.add("name", ITextComponent.Serializer.toJsonTree(this.name));
         }

         return jsonobject;
      }

      @OnlyIn(Dist.CLIENT)
      public List<ITextComponent> getTooltipLines() {
         if (this.linesCache == null) {
            this.linesCache = Lists.newArrayList();
            if (this.name != null) {
               this.linesCache.add(this.name);
            }

            this.linesCache.add(new TranslationTextComponent("gui.entity_tooltip.type", this.type.getDescription()));
            this.linesCache.add(new StringTextComponent(this.id.toString()));
         }

         return this.linesCache;
      }

      public boolean equals(Object p_equals_1_) {
         if (this == p_equals_1_) {
            return true;
         } else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
            HoverEvent.EntityHover hoverevent$entityhover = (HoverEvent.EntityHover)p_equals_1_;
            return this.type.equals(hoverevent$entityhover.type) && this.id.equals(hoverevent$entityhover.id) && Objects.equals(this.name, hoverevent$entityhover.name);
         } else {
            return false;
         }
      }

      public int hashCode() {
         int i = this.type.hashCode();
         i = 31 * i + this.id.hashCode();
         return 31 * i + (this.name != null ? this.name.hashCode() : 0);
      }
   }

   public static class ItemHover {
      private final Item item;
      private final int count;
      @Nullable
      private final CompoundNBT tag;
      @Nullable
      @OnlyIn(Dist.CLIENT)
      private ItemStack itemStack;

      ItemHover(Item pItem, int pCount, @Nullable CompoundNBT pTag) {
         this.item = pItem;
         this.count = pCount;
         this.tag = pTag;
      }

      public ItemHover(ItemStack pStack) {
         this(pStack.getItem(), pStack.getCount(), pStack.getTag() != null ? pStack.getTag().copy() : null);
      }

      public boolean equals(Object p_equals_1_) {
         if (this == p_equals_1_) {
            return true;
         } else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
            HoverEvent.ItemHover hoverevent$itemhover = (HoverEvent.ItemHover)p_equals_1_;
            return this.count == hoverevent$itemhover.count && this.item.equals(hoverevent$itemhover.item) && Objects.equals(this.tag, hoverevent$itemhover.tag);
         } else {
            return false;
         }
      }

      public int hashCode() {
         int i = this.item.hashCode();
         i = 31 * i + this.count;
         return 31 * i + (this.tag != null ? this.tag.hashCode() : 0);
      }

      @OnlyIn(Dist.CLIENT)
      public ItemStack getItemStack() {
         if (this.itemStack == null) {
            this.itemStack = new ItemStack(this.item, this.count);
            if (this.tag != null) {
               this.itemStack.setTag(this.tag);
            }
         }

         return this.itemStack;
      }

      private static HoverEvent.ItemHover create(JsonElement pElement) {
         if (pElement.isJsonPrimitive()) {
            return new HoverEvent.ItemHover(Registry.ITEM.get(new ResourceLocation(pElement.getAsString())), 1, (CompoundNBT)null);
         } else {
            JsonObject jsonobject = JSONUtils.convertToJsonObject(pElement, "item");
            Item item = Registry.ITEM.get(new ResourceLocation(JSONUtils.getAsString(jsonobject, "id")));
            int i = JSONUtils.getAsInt(jsonobject, "count", 1);
            if (jsonobject.has("tag")) {
               String s = JSONUtils.getAsString(jsonobject, "tag");

               try {
                  CompoundNBT compoundnbt = JsonToNBT.parseTag(s);
                  return new HoverEvent.ItemHover(item, i, compoundnbt);
               } catch (CommandSyntaxException commandsyntaxexception) {
                  HoverEvent.LOGGER.warn("Failed to parse tag: {}", s, commandsyntaxexception);
               }
            }

            return new HoverEvent.ItemHover(item, i, (CompoundNBT)null);
         }
      }

      @Nullable
      private static HoverEvent.ItemHover create(ITextComponent pComponent) {
         try {
            CompoundNBT compoundnbt = JsonToNBT.parseTag(pComponent.getString());
            return new HoverEvent.ItemHover(ItemStack.of(compoundnbt));
         } catch (CommandSyntaxException commandsyntaxexception) {
            HoverEvent.LOGGER.warn("Failed to parse item tag: {}", pComponent, commandsyntaxexception);
            return null;
         }
      }

      private JsonElement serialize() {
         JsonObject jsonobject = new JsonObject();
         jsonobject.addProperty("id", Registry.ITEM.getKey(this.item).toString());
         if (this.count != 1) {
            jsonobject.addProperty("count", this.count);
         }

         if (this.tag != null) {
            jsonobject.addProperty("tag", this.tag.toString());
         }

         return jsonobject;
      }
   }
}