package net.minecraft.util.text;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.Message;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.util.EnumTypeAdapterFactory;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ITextComponent extends Message, ITextProperties {
   /**
    * Gets the style of this component.
    */
   Style getStyle();

   /**
    * Gets the raw content of this component if possible. For special components (like {@link TranslatableComponent}
    * this usually returns the empty string.
    */
   String getContents();

   default String getString() {
      return ITextProperties.super.getString();
   }

   /**
    * Get the plain text of this FormattedText, without any styling or formatting codes, limited to {@code maxLength}
    * characters.
    */
   default String getString(int pMaxLength) {
      StringBuilder stringbuilder = new StringBuilder();
      this.visit((p_240639_2_) -> {
         int i = pMaxLength - stringbuilder.length();
         if (i <= 0) {
            return STOP_ITERATION;
         } else {
            stringbuilder.append(p_240639_2_.length() <= i ? p_240639_2_ : p_240639_2_.substring(0, i));
            return Optional.empty();
         }
      });
      return stringbuilder.toString();
   }

   /**
    * Gets the sibling components of this one.
    */
   List<ITextComponent> getSiblings();

   /**
    * Creates a copy of this component, losing any style or siblings.
    */
   IFormattableTextComponent plainCopy();

   /**
    * Creates a copy of this component and also copies the style and siblings. Note that the siblings are copied
    * shallowly, meaning the siblings themselves are not copied.
    */
   IFormattableTextComponent copy();

   @OnlyIn(Dist.CLIENT)
   IReorderingProcessor getVisualOrderText();

   @OnlyIn(Dist.CLIENT)
   default <T> Optional<T> visit(ITextProperties.IStyledTextAcceptor<T> pAcceptor, Style pStyle) {
      Style style = this.getStyle().applyTo(pStyle);
      Optional<T> optional = this.visitSelf(pAcceptor, style);
      if (optional.isPresent()) {
         return optional;
      } else {
         for(ITextComponent itextcomponent : this.getSiblings()) {
            Optional<T> optional1 = itextcomponent.visit(pAcceptor, style);
            if (optional1.isPresent()) {
               return optional1;
            }
         }

         return Optional.empty();
      }
   }

   default <T> Optional<T> visit(ITextProperties.ITextAcceptor<T> pAcceptor) {
      Optional<T> optional = this.visitSelf(pAcceptor);
      if (optional.isPresent()) {
         return optional;
      } else {
         for(ITextComponent itextcomponent : this.getSiblings()) {
            Optional<T> optional1 = itextcomponent.visit(pAcceptor);
            if (optional1.isPresent()) {
               return optional1;
            }
         }

         return Optional.empty();
      }
   }

   @OnlyIn(Dist.CLIENT)
   default <T> Optional<T> visitSelf(ITextProperties.IStyledTextAcceptor<T> pConsumer, Style pStyle) {
      return pConsumer.accept(pStyle, this.getContents());
   }

   default <T> Optional<T> visitSelf(ITextProperties.ITextAcceptor<T> pConsumer) {
      return pConsumer.accept(this.getContents());
   }

   @OnlyIn(Dist.CLIENT)
   static ITextComponent nullToEmpty(@Nullable String pText) {
      return (ITextComponent)(pText != null ? new StringTextComponent(pText) : StringTextComponent.EMPTY);
   }

   public static class Serializer implements JsonDeserializer<IFormattableTextComponent>, JsonSerializer<ITextComponent> {
      private static final Gson GSON = Util.make(() -> {
         GsonBuilder gsonbuilder = new GsonBuilder();
         gsonbuilder.disableHtmlEscaping();
         gsonbuilder.registerTypeHierarchyAdapter(ITextComponent.class, new ITextComponent.Serializer());
         gsonbuilder.registerTypeHierarchyAdapter(Style.class, new Style.Serializer());
         gsonbuilder.registerTypeAdapterFactory(new EnumTypeAdapterFactory());
         return gsonbuilder.create();
      });
      private static final Field JSON_READER_POS = Util.make(() -> {
         try {
            new JsonReader(new StringReader(""));
            Field field = JsonReader.class.getDeclaredField("pos");
            field.setAccessible(true);
            return field;
         } catch (NoSuchFieldException nosuchfieldexception) {
            throw new IllegalStateException("Couldn't get field 'pos' for JsonReader", nosuchfieldexception);
         }
      });
      private static final Field JSON_READER_LINESTART = Util.make(() -> {
         try {
            new JsonReader(new StringReader(""));
            Field field = JsonReader.class.getDeclaredField("lineStart");
            field.setAccessible(true);
            return field;
         } catch (NoSuchFieldException nosuchfieldexception) {
            throw new IllegalStateException("Couldn't get field 'lineStart' for JsonReader", nosuchfieldexception);
         }
      });

      public IFormattableTextComponent deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {
         if (p_deserialize_1_.isJsonPrimitive()) {
            return new StringTextComponent(p_deserialize_1_.getAsString());
         } else if (!p_deserialize_1_.isJsonObject()) {
            if (p_deserialize_1_.isJsonArray()) {
               JsonArray jsonarray1 = p_deserialize_1_.getAsJsonArray();
               IFormattableTextComponent iformattabletextcomponent1 = null;

               for(JsonElement jsonelement : jsonarray1) {
                  IFormattableTextComponent iformattabletextcomponent2 = this.deserialize(jsonelement, jsonelement.getClass(), p_deserialize_3_);
                  if (iformattabletextcomponent1 == null) {
                     iformattabletextcomponent1 = iformattabletextcomponent2;
                  } else {
                     iformattabletextcomponent1.append(iformattabletextcomponent2);
                  }
               }

               return iformattabletextcomponent1;
            } else {
               throw new JsonParseException("Don't know how to turn " + p_deserialize_1_ + " into a Component");
            }
         } else {
            JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
            IFormattableTextComponent iformattabletextcomponent;
            if (jsonobject.has("text")) {
               iformattabletextcomponent = new StringTextComponent(JSONUtils.getAsString(jsonobject, "text"));
            } else if (jsonobject.has("translate")) {
               String s = JSONUtils.getAsString(jsonobject, "translate");
               if (jsonobject.has("with")) {
                  JsonArray jsonarray = JSONUtils.getAsJsonArray(jsonobject, "with");
                  Object[] aobject = new Object[jsonarray.size()];

                  for(int i = 0; i < aobject.length; ++i) {
                     aobject[i] = this.deserialize(jsonarray.get(i), p_deserialize_2_, p_deserialize_3_);
                     if (aobject[i] instanceof StringTextComponent) {
                        StringTextComponent stringtextcomponent = (StringTextComponent)aobject[i];
                        if (stringtextcomponent.getStyle().isEmpty() && stringtextcomponent.getSiblings().isEmpty()) {
                           aobject[i] = stringtextcomponent.getText();
                        }
                     }
                  }

                  iformattabletextcomponent = new TranslationTextComponent(s, aobject);
               } else {
                  iformattabletextcomponent = new TranslationTextComponent(s);
               }
            } else if (jsonobject.has("score")) {
               JsonObject jsonobject1 = JSONUtils.getAsJsonObject(jsonobject, "score");
               if (!jsonobject1.has("name") || !jsonobject1.has("objective")) {
                  throw new JsonParseException("A score component needs a least a name and an objective");
               }

               iformattabletextcomponent = new ScoreTextComponent(JSONUtils.getAsString(jsonobject1, "name"), JSONUtils.getAsString(jsonobject1, "objective"));
            } else if (jsonobject.has("selector")) {
               iformattabletextcomponent = new SelectorTextComponent(JSONUtils.getAsString(jsonobject, "selector"));
            } else if (jsonobject.has("keybind")) {
               iformattabletextcomponent = new KeybindTextComponent(JSONUtils.getAsString(jsonobject, "keybind"));
            } else {
               if (!jsonobject.has("nbt")) {
                  throw new JsonParseException("Don't know how to turn " + p_deserialize_1_ + " into a Component");
               }

               String s1 = JSONUtils.getAsString(jsonobject, "nbt");
               boolean flag = JSONUtils.getAsBoolean(jsonobject, "interpret", false);
               if (jsonobject.has("block")) {
                  iformattabletextcomponent = new NBTTextComponent.Block(s1, flag, JSONUtils.getAsString(jsonobject, "block"));
               } else if (jsonobject.has("entity")) {
                  iformattabletextcomponent = new NBTTextComponent.Entity(s1, flag, JSONUtils.getAsString(jsonobject, "entity"));
               } else {
                  if (!jsonobject.has("storage")) {
                     throw new JsonParseException("Don't know how to turn " + p_deserialize_1_ + " into a Component");
                  }

                  iformattabletextcomponent = new NBTTextComponent.Storage(s1, flag, new ResourceLocation(JSONUtils.getAsString(jsonobject, "storage")));
               }
            }

            if (jsonobject.has("extra")) {
               JsonArray jsonarray2 = JSONUtils.getAsJsonArray(jsonobject, "extra");
               if (jsonarray2.size() <= 0) {
                  throw new JsonParseException("Unexpected empty array of components");
               }

               for(int j = 0; j < jsonarray2.size(); ++j) {
                  iformattabletextcomponent.append(this.deserialize(jsonarray2.get(j), p_deserialize_2_, p_deserialize_3_));
               }
            }

            iformattabletextcomponent.setStyle(p_deserialize_3_.deserialize(p_deserialize_1_, Style.class));
            return iformattabletextcomponent;
         }
      }

      private void serializeStyle(Style pStyle, JsonObject pObject, JsonSerializationContext pCtx) {
         JsonElement jsonelement = pCtx.serialize(pStyle);
         if (jsonelement.isJsonObject()) {
            JsonObject jsonobject = (JsonObject)jsonelement;

            for(Entry<String, JsonElement> entry : jsonobject.entrySet()) {
               pObject.add(entry.getKey(), entry.getValue());
            }
         }

      }

      public JsonElement serialize(ITextComponent p_serialize_1_, Type p_serialize_2_, JsonSerializationContext p_serialize_3_) {
         JsonObject jsonobject = new JsonObject();
         if (!p_serialize_1_.getStyle().isEmpty()) {
            this.serializeStyle(p_serialize_1_.getStyle(), jsonobject, p_serialize_3_);
         }

         if (!p_serialize_1_.getSiblings().isEmpty()) {
            JsonArray jsonarray = new JsonArray();

            for(ITextComponent itextcomponent : p_serialize_1_.getSiblings()) {
               jsonarray.add(this.serialize(itextcomponent, itextcomponent.getClass(), p_serialize_3_));
            }

            jsonobject.add("extra", jsonarray);
         }

         if (p_serialize_1_ instanceof StringTextComponent) {
            jsonobject.addProperty("text", ((StringTextComponent)p_serialize_1_).getText());
         } else if (p_serialize_1_ instanceof TranslationTextComponent) {
            TranslationTextComponent translationtextcomponent = (TranslationTextComponent)p_serialize_1_;
            jsonobject.addProperty("translate", translationtextcomponent.getKey());
            if (translationtextcomponent.getArgs() != null && translationtextcomponent.getArgs().length > 0) {
               JsonArray jsonarray1 = new JsonArray();

               for(Object object : translationtextcomponent.getArgs()) {
                  if (object instanceof ITextComponent) {
                     jsonarray1.add(this.serialize((ITextComponent)object, object.getClass(), p_serialize_3_));
                  } else {
                     jsonarray1.add(new JsonPrimitive(String.valueOf(object)));
                  }
               }

               jsonobject.add("with", jsonarray1);
            }
         } else if (p_serialize_1_ instanceof ScoreTextComponent) {
            ScoreTextComponent scoretextcomponent = (ScoreTextComponent)p_serialize_1_;
            JsonObject jsonobject1 = new JsonObject();
            jsonobject1.addProperty("name", scoretextcomponent.getName());
            jsonobject1.addProperty("objective", scoretextcomponent.getObjective());
            jsonobject.add("score", jsonobject1);
         } else if (p_serialize_1_ instanceof SelectorTextComponent) {
            SelectorTextComponent selectortextcomponent = (SelectorTextComponent)p_serialize_1_;
            jsonobject.addProperty("selector", selectortextcomponent.getPattern());
         } else if (p_serialize_1_ instanceof KeybindTextComponent) {
            KeybindTextComponent keybindtextcomponent = (KeybindTextComponent)p_serialize_1_;
            jsonobject.addProperty("keybind", keybindtextcomponent.getName());
         } else {
            if (!(p_serialize_1_ instanceof NBTTextComponent)) {
               throw new IllegalArgumentException("Don't know how to serialize " + p_serialize_1_ + " as a Component");
            }

            NBTTextComponent nbttextcomponent = (NBTTextComponent)p_serialize_1_;
            jsonobject.addProperty("nbt", nbttextcomponent.getNbtPath());
            jsonobject.addProperty("interpret", nbttextcomponent.isInterpreting());
            if (p_serialize_1_ instanceof NBTTextComponent.Block) {
               NBTTextComponent.Block nbttextcomponent$block = (NBTTextComponent.Block)p_serialize_1_;
               jsonobject.addProperty("block", nbttextcomponent$block.getPos());
            } else if (p_serialize_1_ instanceof NBTTextComponent.Entity) {
               NBTTextComponent.Entity nbttextcomponent$entity = (NBTTextComponent.Entity)p_serialize_1_;
               jsonobject.addProperty("entity", nbttextcomponent$entity.getSelector());
            } else {
               if (!(p_serialize_1_ instanceof NBTTextComponent.Storage)) {
                  throw new IllegalArgumentException("Don't know how to serialize " + p_serialize_1_ + " as a Component");
               }

               NBTTextComponent.Storage nbttextcomponent$storage = (NBTTextComponent.Storage)p_serialize_1_;
               jsonobject.addProperty("storage", nbttextcomponent$storage.getId().toString());
            }
         }

         return jsonobject;
      }

      /**
       * Serializes a component into JSON.
       */
      public static String toJson(ITextComponent pComponent) {
         return GSON.toJson(pComponent);
      }

      public static JsonElement toJsonTree(ITextComponent pComponent) {
         return GSON.toJsonTree(pComponent);
      }

      @Nullable
      public static IFormattableTextComponent fromJson(String pJson) {
         return JSONUtils.fromJson(GSON, pJson, IFormattableTextComponent.class, false);
      }

      @Nullable
      public static IFormattableTextComponent fromJson(JsonElement pJson) {
         return GSON.fromJson(pJson, IFormattableTextComponent.class);
      }

      @Nullable
      public static IFormattableTextComponent fromJsonLenient(String p_240644_0_) {
         return JSONUtils.fromJson(GSON, p_240644_0_, IFormattableTextComponent.class, true);
      }

      public static IFormattableTextComponent fromJson(com.mojang.brigadier.StringReader pReader) {
         try {
            JsonReader jsonreader = new JsonReader(new StringReader(pReader.getRemaining()));
            jsonreader.setLenient(false);
            IFormattableTextComponent iformattabletextcomponent = GSON.getAdapter(IFormattableTextComponent.class).read(jsonreader);
            pReader.setCursor(pReader.getCursor() + getPos(jsonreader));
            return iformattabletextcomponent;
         } catch (StackOverflowError | IOException ioexception) {
            throw new JsonParseException(ioexception);
         }
      }

      private static int getPos(JsonReader pReader) {
         try {
            return JSON_READER_POS.getInt(pReader) - JSON_READER_LINESTART.getInt(pReader) + 1;
         } catch (IllegalAccessException illegalaccessexception) {
            throw new IllegalStateException("Couldn't read position of JsonReader", illegalaccessexception);
         }
      }
   }
}