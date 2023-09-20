package net.minecraft.command.arguments;

import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class MessageArgument implements ArgumentType<MessageArgument.Message> {
   private static final Collection<String> EXAMPLES = Arrays.asList("Hello world!", "foo", "@e", "Hello @p :)");

   public static MessageArgument message() {
      return new MessageArgument();
   }

   public static ITextComponent getMessage(CommandContext<CommandSource> pContext, String pName) throws CommandSyntaxException {
      return pContext.getArgument(pName, MessageArgument.Message.class).toComponent(pContext.getSource(), pContext.getSource().hasPermission(2));
   }

   public MessageArgument.Message parse(StringReader p_parse_1_) throws CommandSyntaxException {
      return MessageArgument.Message.parseText(p_parse_1_, true);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static class Message {
      private final String text;
      private final MessageArgument.Part[] parts;

      public Message(String pText, MessageArgument.Part[] pParts) {
         this.text = pText;
         this.parts = pParts;
      }

      /**
       * Converts this message into a text component, replacing any selectors in the text with the actual evaluated
       * selector.
       */
      public ITextComponent toComponent(CommandSource pSource, boolean pAllowSelectors) throws CommandSyntaxException {
         if (this.parts.length != 0 && pAllowSelectors) {
            IFormattableTextComponent iformattabletextcomponent = new StringTextComponent(this.text.substring(0, this.parts[0].getStart()));
            int i = this.parts[0].getStart();

            for(MessageArgument.Part messageargument$part : this.parts) {
               ITextComponent itextcomponent = messageargument$part.toComponent(pSource);
               if (i < messageargument$part.getStart()) {
                  iformattabletextcomponent.append(this.text.substring(i, messageargument$part.getStart()));
               }

               if (itextcomponent != null) {
                  iformattabletextcomponent.append(itextcomponent);
               }

               i = messageargument$part.getEnd();
            }

            if (i < this.text.length()) {
               iformattabletextcomponent.append(this.text.substring(i, this.text.length()));
            }

            return iformattabletextcomponent;
         } else {
            return new StringTextComponent(this.text);
         }
      }

      /**
       * Parses a message. The algorithm for this is simply to run though and look for selectors, ignoring any invalid
       * selectors in the text (since players may type e.g. "[@]").
       */
      public static MessageArgument.Message parseText(StringReader pReader, boolean pAllowSelectors) throws CommandSyntaxException {
         String s = pReader.getString().substring(pReader.getCursor(), pReader.getTotalLength());
         if (!pAllowSelectors) {
            pReader.setCursor(pReader.getTotalLength());
            return new MessageArgument.Message(s, new MessageArgument.Part[0]);
         } else {
            List<MessageArgument.Part> list = Lists.newArrayList();
            int i = pReader.getCursor();

            while(true) {
               int j;
               EntitySelector entityselector;
               while(true) {
                  if (!pReader.canRead()) {
                     return new MessageArgument.Message(s, list.toArray(new MessageArgument.Part[list.size()]));
                  }

                  if (pReader.peek() == '@') {
                     j = pReader.getCursor();

                     try {
                        EntitySelectorParser entityselectorparser = new EntitySelectorParser(pReader);
                        entityselector = entityselectorparser.parse();
                        break;
                     } catch (CommandSyntaxException commandsyntaxexception) {
                        if (commandsyntaxexception.getType() != EntitySelectorParser.ERROR_MISSING_SELECTOR_TYPE && commandsyntaxexception.getType() != EntitySelectorParser.ERROR_UNKNOWN_SELECTOR_TYPE) {
                           throw commandsyntaxexception;
                        }

                        pReader.setCursor(j + 1);
                     }
                  } else {
                     pReader.skip();
                  }
               }

               list.add(new MessageArgument.Part(j - i, pReader.getCursor() - i, entityselector));
            }
         }
      }
   }

   public static class Part {
      private final int start;
      private final int end;
      private final EntitySelector selector;

      public Part(int pStart, int pEnd, EntitySelector pSelector) {
         this.start = pStart;
         this.end = pEnd;
         this.selector = pSelector;
      }

      public int getStart() {
         return this.start;
      }

      public int getEnd() {
         return this.end;
      }

      /**
       * Runs the selector and returns the component produced by it. This method does not actually appear to ever return
       * null.
       */
      @Nullable
      public ITextComponent toComponent(CommandSource pSource) throws CommandSyntaxException {
         return EntitySelector.joinNames(this.selector.findEntities(pSource));
      }
   }
}