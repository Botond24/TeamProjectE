package net.minecraft.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;

public class AngleArgument implements ArgumentType<AngleArgument.Result> {
   private static final Collection<String> EXAMPLES = Arrays.asList("0", "~", "~-5");
   public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType(new TranslationTextComponent("argument.angle.incomplete"));

   public static AngleArgument angle() {
      return new AngleArgument();
   }

   public static float getAngle(CommandContext<CommandSource> pContext, String pName) {
      return pContext.getArgument(pName, AngleArgument.Result.class).getAngle(pContext.getSource());
   }

   public AngleArgument.Result parse(StringReader p_parse_1_) throws CommandSyntaxException {
      if (!p_parse_1_.canRead()) {
         throw ERROR_NOT_COMPLETE.createWithContext(p_parse_1_);
      } else {
         boolean flag = LocationPart.isRelative(p_parse_1_);
         float f = p_parse_1_.canRead() && p_parse_1_.peek() != ' ' ? p_parse_1_.readFloat() : 0.0F;
         return new AngleArgument.Result(f, flag);
      }
   }

   public Collection<String> getExamples() {
      return EXAMPLES;
   }

   public static final class Result {
      private final float angle;
      private final boolean isRelative;

      private Result(float pAngle, boolean pIsRelative) {
         this.angle = pAngle;
         this.isRelative = pIsRelative;
      }

      public float getAngle(CommandSource pSource) {
         return MathHelper.wrapDegrees(this.isRelative ? this.angle + pSource.getRotation().y : this.angle);
      }
   }
}