package net.minecraft.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.command.CommandSource;
import net.minecraft.command.FunctionObject;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public class FunctionArgument implements ArgumentType<FunctionArgument.IResult> {
   private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "#foo");
   private static final DynamicCommandExceptionType ERROR_UNKNOWN_TAG = new DynamicCommandExceptionType((p_208691_0_) -> {
      return new TranslationTextComponent("arguments.function.tag.unknown", p_208691_0_);
   });
   private static final DynamicCommandExceptionType ERROR_UNKNOWN_FUNCTION = new DynamicCommandExceptionType((p_208694_0_) -> {
      return new TranslationTextComponent("arguments.function.unknown", p_208694_0_);
   });

   public static FunctionArgument functions() {
      return new FunctionArgument();
   }

   public FunctionArgument.IResult parse(StringReader p_parse_1_) throws CommandSyntaxException {
      if (p_parse_1_.canRead() && p_parse_1_.peek() == '#') {
         p_parse_1_.skip();
         final ResourceLocation resourcelocation1 = ResourceLocation.read(p_parse_1_);
         return new FunctionArgument.IResult() {
            public Collection<FunctionObject> create(CommandContext<CommandSource> pContext) throws CommandSyntaxException {
               ITag<FunctionObject> itag = FunctionArgument.getFunctionTag(pContext, resourcelocation1);
               return itag.getValues();
            }

            public Pair<ResourceLocation, Either<FunctionObject, ITag<FunctionObject>>> unwrap(CommandContext<CommandSource> pContext) throws CommandSyntaxException {
               return Pair.of(resourcelocation1, Either.right(FunctionArgument.getFunctionTag(pContext, resourcelocation1)));
            }
         };
      } else {
         final ResourceLocation resourcelocation = ResourceLocation.read(p_parse_1_);
         return new FunctionArgument.IResult() {
            public Collection<FunctionObject> create(CommandContext<CommandSource> pContext) throws CommandSyntaxException {
               return Collections.singleton(FunctionArgument.getFunction(pContext, resourcelocation));
            }

            public Pair<ResourceLocation, Either<FunctionObject, ITag<FunctionObject>>> unwrap(CommandContext<CommandSource> pContext) throws CommandSyntaxException {
               return Pair.of(resourcelocation, Either.left(FunctionArgument.getFunction(pContext, resourcelocation)));
            }
         };
      }
   }

   private static FunctionObject getFunction(CommandContext<CommandSource> pContext, ResourceLocation pId) throws CommandSyntaxException {
      return pContext.getSource().getServer().getFunctions().get(pId).orElseThrow(() -> {
         return ERROR_UNKNOWN_FUNCTION.create(pId.toString());
      });
   }

   private static ITag<FunctionObject> getFunctionTag(CommandContext<CommandSource> pContext, ResourceLocation pId) throws CommandSyntaxException {
      ITag<FunctionObject> itag = pContext.getSource().getServer().getFunctions().getTag(pId);
      if (itag == null) {
         throw ERROR_UNKNOWN_TAG.create(pId.toString());
      } else {
         return itag;
      }
   }

   public static Collection<FunctionObject> getFunctions(CommandContext<CommandSource> pContext, String pName) throws CommandSyntaxException {
      return pContext.getArgument(pName, FunctionArgument.IResult.class).create(pContext);
   }

   public static Pair<ResourceLocation, Either<FunctionObject, ITag<FunctionObject>>> getFunctionOrTag(CommandContext<CommandSource> pContext, String pName) throws CommandSyntaxException {
      return pContext.getArgument(pName, FunctionArgument.IResult.class).unwrap(pContext);
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public interface IResult {
      Collection<FunctionObject> create(CommandContext<CommandSource> pContext) throws CommandSyntaxException;

      Pair<ResourceLocation, Either<FunctionObject, ITag<FunctionObject>>> unwrap(CommandContext<CommandSource> pContext) throws CommandSyntaxException;
   }
}