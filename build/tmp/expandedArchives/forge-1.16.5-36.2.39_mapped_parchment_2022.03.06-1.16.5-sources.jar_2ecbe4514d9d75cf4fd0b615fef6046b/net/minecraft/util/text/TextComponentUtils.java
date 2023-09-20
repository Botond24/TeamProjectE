package net.minecraft.util.text;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.event.HoverEvent;

public class TextComponentUtils {
   /**
    * Merge the component's styles with the given Style.
    */
   public static IFormattableTextComponent mergeStyles(IFormattableTextComponent pComponent, Style pStyle) {
      if (pStyle.isEmpty()) {
         return pComponent;
      } else {
         Style style = pComponent.getStyle();
         if (style.isEmpty()) {
            return pComponent.setStyle(pStyle);
         } else {
            return style.equals(pStyle) ? pComponent : pComponent.setStyle(style.applyTo(pStyle));
         }
      }
   }

   public static IFormattableTextComponent updateForEntity(@Nullable CommandSource pCommandSourceStack, ITextComponent pComponent, @Nullable Entity pEntity, int pRecursionDepth) throws CommandSyntaxException {
      if (pRecursionDepth > 100) {
         return pComponent.copy();
      } else {
         IFormattableTextComponent iformattabletextcomponent = pComponent instanceof ITargetedTextComponent ? ((ITargetedTextComponent)pComponent).resolve(pCommandSourceStack, pEntity, pRecursionDepth + 1) : pComponent.plainCopy();

         for(ITextComponent itextcomponent : pComponent.getSiblings()) {
            iformattabletextcomponent.append(updateForEntity(pCommandSourceStack, itextcomponent, pEntity, pRecursionDepth + 1));
         }

         return iformattabletextcomponent.withStyle(resolveStyle(pCommandSourceStack, pComponent.getStyle(), pEntity, pRecursionDepth));
      }
   }

   private static Style resolveStyle(@Nullable CommandSource pCommandSourceStack, Style pStyle, @Nullable Entity pEntity, int pRecursionDepth) throws CommandSyntaxException {
      HoverEvent hoverevent = pStyle.getHoverEvent();
      if (hoverevent != null) {
         ITextComponent itextcomponent = hoverevent.getValue(HoverEvent.Action.SHOW_TEXT);
         if (itextcomponent != null) {
            HoverEvent hoverevent1 = new HoverEvent(HoverEvent.Action.SHOW_TEXT, updateForEntity(pCommandSourceStack, itextcomponent, pEntity, pRecursionDepth + 1));
            return pStyle.withHoverEvent(hoverevent1);
         }
      }

      return pStyle;
   }

   public static ITextComponent getDisplayName(GameProfile pProfile) {
      if (pProfile.getName() != null) {
         return new StringTextComponent(pProfile.getName());
      } else {
         return pProfile.getId() != null ? new StringTextComponent(pProfile.getId().toString()) : new StringTextComponent("(unknown)");
      }
   }

   public static ITextComponent formatList(Collection<String> pElements) {
      return formatAndSortList(pElements, (p_197681_0_) -> {
         return (new StringTextComponent(p_197681_0_)).withStyle(TextFormatting.GREEN);
      });
   }

   public static <T extends Comparable<T>> ITextComponent formatAndSortList(Collection<T> pElements, Function<T, ITextComponent> pComponentExtractor) {
      if (pElements.isEmpty()) {
         return StringTextComponent.EMPTY;
      } else if (pElements.size() == 1) {
         return pComponentExtractor.apply(pElements.iterator().next());
      } else {
         List<T> list = Lists.newArrayList(pElements);
         list.sort(Comparable::compareTo);
         return formatList(list, pComponentExtractor);
      }
   }

   public static <T> IFormattableTextComponent formatList(Collection<T> p_240649_0_, Function<T, ITextComponent> p_240649_1_) {
      if (p_240649_0_.isEmpty()) {
         return new StringTextComponent("");
      } else if (p_240649_0_.size() == 1) {
         return p_240649_1_.apply(p_240649_0_.iterator().next()).copy();
      } else {
         IFormattableTextComponent iformattabletextcomponent = new StringTextComponent("");
         boolean flag = true;

         for(T t : p_240649_0_) {
            if (!flag) {
               iformattabletextcomponent.append((new StringTextComponent(", ")).withStyle(TextFormatting.GRAY));
            }

            iformattabletextcomponent.append(p_240649_1_.apply(t));
            flag = false;
         }

         return iformattabletextcomponent;
      }
   }

   /**
    * Wraps the text with square brackets.
    */
   public static IFormattableTextComponent wrapInSquareBrackets(ITextComponent pToWrap) {
      return new TranslationTextComponent("chat.square_brackets", pToWrap);
   }

   public static ITextComponent fromMessage(Message pMessage) {
      return (ITextComponent)(pMessage instanceof ITextComponent ? (ITextComponent)pMessage : new StringTextComponent(pMessage.getString()));
   }
}