package net.minecraft.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.FunctionManager;
import net.minecraft.util.ResourceLocation;

public class FunctionObject {
   private final FunctionObject.IEntry[] entries;
   private final ResourceLocation id;

   public FunctionObject(ResourceLocation pId, FunctionObject.IEntry[] pEntries) {
      this.id = pId;
      this.entries = pEntries;
   }

   public ResourceLocation getId() {
      return this.id;
   }

   public FunctionObject.IEntry[] getEntries() {
      return this.entries;
   }

   public static FunctionObject fromLines(ResourceLocation pId, CommandDispatcher<CommandSource> pDispatcher, CommandSource pSource, List<String> pLines) {
      List<FunctionObject.IEntry> list = Lists.newArrayListWithCapacity(pLines.size());

      for(int i = 0; i < pLines.size(); ++i) {
         int j = i + 1;
         String s = pLines.get(i).trim();
         StringReader stringreader = new StringReader(s);
         if (stringreader.canRead() && stringreader.peek() != '#') {
            if (stringreader.peek() == '/') {
               stringreader.skip();
               if (stringreader.peek() == '/') {
                  throw new IllegalArgumentException("Unknown or invalid command '" + s + "' on line " + j + " (if you intended to make a comment, use '#' not '//')");
               }

               String s1 = stringreader.readUnquotedString();
               throw new IllegalArgumentException("Unknown or invalid command '" + s + "' on line " + j + " (did you mean '" + s1 + "'? Do not use a preceding forwards slash.)");
            }

            try {
               ParseResults<CommandSource> parseresults = pDispatcher.parse(stringreader, pSource);
               if (parseresults.getReader().canRead()) {
                  throw Commands.getParseException(parseresults);
               }

               list.add(new FunctionObject.CommandEntry(parseresults));
            } catch (CommandSyntaxException commandsyntaxexception) {
               throw new IllegalArgumentException("Whilst parsing command on line " + j + ": " + commandsyntaxexception.getMessage());
            }
         }
      }

      return new FunctionObject(pId, list.toArray(new FunctionObject.IEntry[0]));
   }

   public static class CacheableFunction {
      public static final FunctionObject.CacheableFunction NONE = new FunctionObject.CacheableFunction((ResourceLocation)null);
      @Nullable
      private final ResourceLocation id;
      private boolean resolved;
      private Optional<FunctionObject> function = Optional.empty();

      public CacheableFunction(@Nullable ResourceLocation pId) {
         this.id = pId;
      }

      public CacheableFunction(FunctionObject pFunction) {
         this.resolved = true;
         this.id = null;
         this.function = Optional.of(pFunction);
      }

      public Optional<FunctionObject> get(FunctionManager pFunctionManager) {
         if (!this.resolved) {
            if (this.id != null) {
               this.function = pFunctionManager.get(this.id);
            }

            this.resolved = true;
         }

         return this.function;
      }

      @Nullable
      public ResourceLocation getId() {
         return this.function.map((p_218040_0_) -> {
            return p_218040_0_.id;
         }).orElse(this.id);
      }
   }

   public static class CommandEntry implements FunctionObject.IEntry {
      private final ParseResults<CommandSource> parse;

      public CommandEntry(ParseResults<CommandSource> pParse) {
         this.parse = pParse;
      }

      public void execute(FunctionManager pFunctionManager, CommandSource p_196998_2_, ArrayDeque<FunctionManager.QueuedCommand> p_196998_3_, int p_196998_4_) throws CommandSyntaxException {
         pFunctionManager.getDispatcher().execute(new ParseResults<>(this.parse.getContext().withSource(p_196998_2_), this.parse.getReader(), this.parse.getExceptions()));
      }

      public String toString() {
         return this.parse.getReader().getString();
      }
   }

   public static class FunctionEntry implements FunctionObject.IEntry {
      private final FunctionObject.CacheableFunction function;

      public FunctionEntry(FunctionObject pFunction) {
         this.function = new FunctionObject.CacheableFunction(pFunction);
      }

      public void execute(FunctionManager pFunctionManager, CommandSource p_196998_2_, ArrayDeque<FunctionManager.QueuedCommand> p_196998_3_, int p_196998_4_) {
         this.function.get(pFunctionManager).ifPresent((p_218041_4_) -> {
            FunctionObject.IEntry[] afunctionobject$ientry = p_218041_4_.getEntries();
            int i = p_196998_4_ - p_196998_3_.size();
            int j = Math.min(afunctionobject$ientry.length, i);

            for(int k = j - 1; k >= 0; --k) {
               p_196998_3_.addFirst(new FunctionManager.QueuedCommand(pFunctionManager, p_196998_2_, afunctionobject$ientry[k]));
            }

         });
      }

      public String toString() {
         return "function " + this.function.getId();
      }
   }

   public interface IEntry {
      void execute(FunctionManager pFunctionManager, CommandSource p_196998_2_, ArrayDeque<FunctionManager.QueuedCommand> p_196998_3_, int p_196998_4_) throws CommandSyntaxException;
   }
}