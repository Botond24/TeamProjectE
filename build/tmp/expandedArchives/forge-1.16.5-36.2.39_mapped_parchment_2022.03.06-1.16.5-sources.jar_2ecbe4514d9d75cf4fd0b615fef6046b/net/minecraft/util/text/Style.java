package net.minecraft.util.text;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A Style for {@link Component}.
 * Stores color, text formatting (bold, etc.) as well as possible HoverEvent/ClickEvent.
 */
public class Style {
   public static final Style EMPTY = new Style((Color)null, (Boolean)null, (Boolean)null, (Boolean)null, (Boolean)null, (Boolean)null, (ClickEvent)null, (HoverEvent)null, (String)null, (ResourceLocation)null);
   public static final ResourceLocation DEFAULT_FONT = new ResourceLocation("minecraft", "default");
   @Nullable
   private final Color color;
   @Nullable
   private final Boolean bold;
   @Nullable
   private final Boolean italic;
   @Nullable
   private final Boolean underlined;
   @Nullable
   private final Boolean strikethrough;
   @Nullable
   private final Boolean obfuscated;
   @Nullable
   private final ClickEvent clickEvent;
   @Nullable
   private final HoverEvent hoverEvent;
   @Nullable
   private final String insertion;
   @Nullable
   private final ResourceLocation font;

   private Style(@Nullable Color pColor, @Nullable Boolean pBold, @Nullable Boolean pItalic, @Nullable Boolean pUnderlined, @Nullable Boolean pStrikethrough, @Nullable Boolean pObfuscated, @Nullable ClickEvent pClickEvent, @Nullable HoverEvent pHoverEvent, @Nullable String pInsertion, @Nullable ResourceLocation pFont) {
      this.color = pColor;
      this.bold = pBold;
      this.italic = pItalic;
      this.underlined = pUnderlined;
      this.strikethrough = pStrikethrough;
      this.obfuscated = pObfuscated;
      this.clickEvent = pClickEvent;
      this.hoverEvent = pHoverEvent;
      this.insertion = pInsertion;
      this.font = pFont;
   }

   @Nullable
   public Color getColor() {
      return this.color;
   }

   /**
    * Whether or not text of this ChatStyle should be in bold.
    */
   public boolean isBold() {
      return this.bold == Boolean.TRUE;
   }

   /**
    * Whether or not text of this ChatStyle should be italicized.
    */
   public boolean isItalic() {
      return this.italic == Boolean.TRUE;
   }

   /**
    * Whether or not to format text of this ChatStyle using strikethrough.
    */
   public boolean isStrikethrough() {
      return this.strikethrough == Boolean.TRUE;
   }

   /**
    * Whether or not text of this ChatStyle should be underlined.
    */
   public boolean isUnderlined() {
      return this.underlined == Boolean.TRUE;
   }

   /**
    * Whether or not text of this ChatStyle should be obfuscated.
    */
   public boolean isObfuscated() {
      return this.obfuscated == Boolean.TRUE;
   }

   /**
    * Whether or not this style is empty (inherits everything from the parent).
    */
   public boolean isEmpty() {
      return this == EMPTY;
   }

   /**
    * The effective chat click event.
    */
   @Nullable
   public ClickEvent getClickEvent() {
      return this.clickEvent;
   }

   /**
    * The effective chat hover event.
    */
   @Nullable
   public HoverEvent getHoverEvent() {
      return this.hoverEvent;
   }

   /**
    * Get the text to be inserted into Chat when the component is shift-clicked
    */
   @Nullable
   public String getInsertion() {
      return this.insertion;
   }

   /**
    * The font to use for this Style
    */
   public ResourceLocation getFont() {
      return this.font != null ? this.font : DEFAULT_FONT;
   }

   public Style withColor(@Nullable Color pColor) {
      return new Style(pColor, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withColor(@Nullable TextFormatting pFormatting) {
      return this.withColor(pFormatting != null ? Color.fromLegacyFormat(pFormatting) : null);
   }

   public Style withBold(@Nullable Boolean pBold) {
      return new Style(this.color, pBold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withItalic(@Nullable Boolean pItalic) {
      return new Style(this.color, this.bold, pItalic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   @OnlyIn(Dist.CLIENT)
   public Style withUnderlined(@Nullable Boolean pUnderlined) {
      return new Style(this.color, this.bold, this.italic, pUnderlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style setUnderlined(@Nullable Boolean underlined) {
      return new Style(this.color, this.bold, this.italic, underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style setStrikethrough(@Nullable Boolean strikethrough) {
      return new Style(this.color, this.bold, this.italic, this.underlined, strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style setObfuscated(@Nullable Boolean obfuscated) {
      return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withClickEvent(@Nullable ClickEvent pClickEvent) {
      return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, pClickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withHoverEvent(@Nullable HoverEvent pHoverEvent) {
      return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, pHoverEvent, this.insertion, this.font);
   }

   public Style withInsertion(@Nullable String pInsertion) {
      return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, pInsertion, this.font);
   }

   public Style withFont(@Nullable ResourceLocation pFontId) {
      return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, pFontId);
   }

   public Style applyFormat(TextFormatting pFormatting) {
      Color color = this.color;
      Boolean obool = this.bold;
      Boolean obool1 = this.italic;
      Boolean obool2 = this.strikethrough;
      Boolean obool3 = this.underlined;
      Boolean obool4 = this.obfuscated;
      switch(pFormatting) {
      case OBFUSCATED:
         obool4 = true;
         break;
      case BOLD:
         obool = true;
         break;
      case STRIKETHROUGH:
         obool2 = true;
         break;
      case UNDERLINE:
         obool3 = true;
         break;
      case ITALIC:
         obool1 = true;
         break;
      case RESET:
         return EMPTY;
      default:
         color = Color.fromLegacyFormat(pFormatting);
      }

      return new Style(color, obool, obool1, obool3, obool2, obool4, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style applyLegacyFormat(TextFormatting pFormatting) {
      Color color = this.color;
      Boolean obool = this.bold;
      Boolean obool1 = this.italic;
      Boolean obool2 = this.strikethrough;
      Boolean obool3 = this.underlined;
      Boolean obool4 = this.obfuscated;
      switch(pFormatting) {
      case OBFUSCATED:
         obool4 = true;
         break;
      case BOLD:
         obool = true;
         break;
      case STRIKETHROUGH:
         obool2 = true;
         break;
      case UNDERLINE:
         obool3 = true;
         break;
      case ITALIC:
         obool1 = true;
         break;
      case RESET:
         return EMPTY;
      default:
         obool4 = false;
         obool = false;
         obool2 = false;
         obool3 = false;
         obool1 = false;
         color = Color.fromLegacyFormat(pFormatting);
      }

      return new Style(color, obool, obool1, obool3, obool2, obool4, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style applyFormats(TextFormatting... pFormats) {
      Color color = this.color;
      Boolean obool = this.bold;
      Boolean obool1 = this.italic;
      Boolean obool2 = this.strikethrough;
      Boolean obool3 = this.underlined;
      Boolean obool4 = this.obfuscated;

      for(TextFormatting textformatting : pFormats) {
         switch(textformatting) {
         case OBFUSCATED:
            obool4 = true;
            break;
         case BOLD:
            obool = true;
            break;
         case STRIKETHROUGH:
            obool2 = true;
            break;
         case UNDERLINE:
            obool3 = true;
            break;
         case ITALIC:
            obool1 = true;
            break;
         case RESET:
            return EMPTY;
         default:
            color = Color.fromLegacyFormat(textformatting);
         }
      }

      return new Style(color, obool, obool1, obool3, obool2, obool4, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   /**
    * Merges the style with another one. If either styles are empty the other will be returned. If a value already
    * exists on the current style it will not be overriden.
    */
   public Style applyTo(Style pStyle) {
      if (this == EMPTY) {
         return pStyle;
      } else {
         return pStyle == EMPTY ? this : new Style(this.color != null ? this.color : pStyle.color, this.bold != null ? this.bold : pStyle.bold, this.italic != null ? this.italic : pStyle.italic, this.underlined != null ? this.underlined : pStyle.underlined, this.strikethrough != null ? this.strikethrough : pStyle.strikethrough, this.obfuscated != null ? this.obfuscated : pStyle.obfuscated, this.clickEvent != null ? this.clickEvent : pStyle.clickEvent, this.hoverEvent != null ? this.hoverEvent : pStyle.hoverEvent, this.insertion != null ? this.insertion : pStyle.insertion, this.font != null ? this.font : pStyle.font);
      }
   }

   public String toString() {
      return "Style{ color=" + this.color + ", bold=" + this.bold + ", italic=" + this.italic + ", underlined=" + this.underlined + ", strikethrough=" + this.strikethrough + ", obfuscated=" + this.obfuscated + ", clickEvent=" + this.getClickEvent() + ", hoverEvent=" + this.getHoverEvent() + ", insertion=" + this.getInsertion() + ", font=" + this.getFont() + '}';
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (!(p_equals_1_ instanceof Style)) {
         return false;
      } else {
         Style style = (Style)p_equals_1_;
         return this.isBold() == style.isBold() && Objects.equals(this.getColor(), style.getColor()) && this.isItalic() == style.isItalic() && this.isObfuscated() == style.isObfuscated() && this.isStrikethrough() == style.isStrikethrough() && this.isUnderlined() == style.isUnderlined() && Objects.equals(this.getClickEvent(), style.getClickEvent()) && Objects.equals(this.getHoverEvent(), style.getHoverEvent()) && Objects.equals(this.getInsertion(), style.getInsertion()) && Objects.equals(this.getFont(), style.getFont());
      }
   }

   public int hashCode() {
      return Objects.hash(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion);
   }

   public static class Serializer implements JsonDeserializer<Style>, JsonSerializer<Style> {
      @Nullable
      public Style deserialize(JsonElement p_deserialize_1_, Type p_deserialize_2_, JsonDeserializationContext p_deserialize_3_) throws JsonParseException {
         if (p_deserialize_1_.isJsonObject()) {
            JsonObject jsonobject = p_deserialize_1_.getAsJsonObject();
            if (jsonobject == null) {
               return null;
            } else {
               Boolean obool = getOptionalFlag(jsonobject, "bold");
               Boolean obool1 = getOptionalFlag(jsonobject, "italic");
               Boolean obool2 = getOptionalFlag(jsonobject, "underlined");
               Boolean obool3 = getOptionalFlag(jsonobject, "strikethrough");
               Boolean obool4 = getOptionalFlag(jsonobject, "obfuscated");
               Color color = getTextColor(jsonobject);
               String s = getInsertion(jsonobject);
               ClickEvent clickevent = getClickEvent(jsonobject);
               HoverEvent hoverevent = getHoverEvent(jsonobject);
               ResourceLocation resourcelocation = getFont(jsonobject);
               return new Style(color, obool, obool1, obool2, obool3, obool4, clickevent, hoverevent, s, resourcelocation);
            }
         } else {
            return null;
         }
      }

      @Nullable
      private static ResourceLocation getFont(JsonObject pJson) {
         if (pJson.has("font")) {
            String s = JSONUtils.getAsString(pJson, "font");

            try {
               return new ResourceLocation(s);
            } catch (ResourceLocationException resourcelocationexception) {
               throw new JsonSyntaxException("Invalid font name: " + s);
            }
         } else {
            return null;
         }
      }

      @Nullable
      private static HoverEvent getHoverEvent(JsonObject pJson) {
         if (pJson.has("hoverEvent")) {
            JsonObject jsonobject = JSONUtils.getAsJsonObject(pJson, "hoverEvent");
            HoverEvent hoverevent = HoverEvent.deserialize(jsonobject);
            if (hoverevent != null && hoverevent.getAction().isAllowedFromServer()) {
               return hoverevent;
            }
         }

         return null;
      }

      @Nullable
      private static ClickEvent getClickEvent(JsonObject pJson) {
         if (pJson.has("clickEvent")) {
            JsonObject jsonobject = JSONUtils.getAsJsonObject(pJson, "clickEvent");
            String s = JSONUtils.getAsString(jsonobject, "action", (String)null);
            ClickEvent.Action clickevent$action = s == null ? null : ClickEvent.Action.getByName(s);
            String s1 = JSONUtils.getAsString(jsonobject, "value", (String)null);
            if (clickevent$action != null && s1 != null && clickevent$action.isAllowedFromServer()) {
               return new ClickEvent(clickevent$action, s1);
            }
         }

         return null;
      }

      @Nullable
      private static String getInsertion(JsonObject pJson) {
         return JSONUtils.getAsString(pJson, "insertion", (String)null);
      }

      @Nullable
      private static Color getTextColor(JsonObject pJson) {
         if (pJson.has("color")) {
            String s = JSONUtils.getAsString(pJson, "color");
            return Color.parseColor(s);
         } else {
            return null;
         }
      }

      @Nullable
      private static Boolean getOptionalFlag(JsonObject pJson, String pMemberName) {
         return pJson.has(pMemberName) ? pJson.get(pMemberName).getAsBoolean() : null;
      }

      @Nullable
      public JsonElement serialize(Style p_serialize_1_, Type p_serialize_2_, JsonSerializationContext p_serialize_3_) {
         if (p_serialize_1_.isEmpty()) {
            return null;
         } else {
            JsonObject jsonobject = new JsonObject();
            if (p_serialize_1_.bold != null) {
               jsonobject.addProperty("bold", p_serialize_1_.bold);
            }

            if (p_serialize_1_.italic != null) {
               jsonobject.addProperty("italic", p_serialize_1_.italic);
            }

            if (p_serialize_1_.underlined != null) {
               jsonobject.addProperty("underlined", p_serialize_1_.underlined);
            }

            if (p_serialize_1_.strikethrough != null) {
               jsonobject.addProperty("strikethrough", p_serialize_1_.strikethrough);
            }

            if (p_serialize_1_.obfuscated != null) {
               jsonobject.addProperty("obfuscated", p_serialize_1_.obfuscated);
            }

            if (p_serialize_1_.color != null) {
               jsonobject.addProperty("color", p_serialize_1_.color.serialize());
            }

            if (p_serialize_1_.insertion != null) {
               jsonobject.add("insertion", p_serialize_3_.serialize(p_serialize_1_.insertion));
            }

            if (p_serialize_1_.clickEvent != null) {
               JsonObject jsonobject1 = new JsonObject();
               jsonobject1.addProperty("action", p_serialize_1_.clickEvent.getAction().getName());
               jsonobject1.addProperty("value", p_serialize_1_.clickEvent.getValue());
               jsonobject.add("clickEvent", jsonobject1);
            }

            if (p_serialize_1_.hoverEvent != null) {
               jsonobject.add("hoverEvent", p_serialize_1_.hoverEvent.serialize());
            }

            if (p_serialize_1_.font != null) {
               jsonobject.addProperty("font", p_serialize_1_.font.toString());
            }

            return jsonobject;
         }
      }
   }
}
