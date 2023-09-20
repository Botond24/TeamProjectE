package net.minecraft.util.text;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TranslationTextComponent extends TextComponent implements ITargetedTextComponent {
   private static final Object[] NO_ARGS = new Object[0];
   private static final ITextProperties TEXT_PERCENT = ITextProperties.of("%");
   private static final ITextProperties TEXT_NULL = ITextProperties.of("null");
   private final String key;
   private final Object[] args;
   @Nullable
   private LanguageMap decomposedWith;
   /**
    * The discrete elements that make up this component. For example, this would be ["Prefix, ", "FirstArg",
    * "SecondArg", " again ", "SecondArg", " and ", "FirstArg", " lastly ", "ThirdArg", " and also ", "FirstArg", "
    * again!"] for "translation.test.complex" (see en_us.json)
    */
   private final List<ITextProperties> decomposedParts = Lists.newArrayList();
   private static final Pattern FORMAT_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

   public TranslationTextComponent(String pKey) {
      this.key = pKey;
      this.args = NO_ARGS;
   }

   public TranslationTextComponent(String pKey, Object... pArgs) {
      this.key = pKey;
      this.args = pArgs;
   }

   /**
    * Ensures that all of the children are up to date with the most recent translation mapping.
    */
   private void decompose() {
      LanguageMap languagemap = LanguageMap.getInstance();
      if (languagemap != this.decomposedWith) {
         this.decomposedWith = languagemap;
         this.decomposedParts.clear();
         String s = languagemap.getOrDefault(this.key);

         try {
            this.decomposeTemplate(s);
         } catch (TranslationTextComponentFormatException translationtextcomponentformatexception) {
            this.decomposedParts.clear();
            this.decomposedParts.add(ITextProperties.of(s));
         }

      }
   }

   private void decomposeTemplate(String pFormatTemplate) {
      Matcher matcher = FORMAT_PATTERN.matcher(pFormatTemplate);

      try {
         int i = 0;

         int j;
         int l;
         for(j = 0; matcher.find(j); j = l) {
            int k = matcher.start();
            l = matcher.end();
            if (k > j) {
               String s = pFormatTemplate.substring(j, k);
               if (s.indexOf(37) != -1) {
                  throw new IllegalArgumentException();
               }

               this.decomposedParts.add(ITextProperties.of(s));
            }

            String s4 = matcher.group(2);
            String s1 = pFormatTemplate.substring(k, l);
            if ("%".equals(s4) && "%%".equals(s1)) {
               this.decomposedParts.add(TEXT_PERCENT);
            } else {
               if (!"s".equals(s4)) {
                  throw new TranslationTextComponentFormatException(this, "Unsupported format: '" + s1 + "'");
               }

               String s2 = matcher.group(1);
               int i1 = s2 != null ? Integer.parseInt(s2) - 1 : i++;
               if (i1 < this.args.length) {
                  this.decomposedParts.add(this.getArgument(i1));
               }
            }
         }

         if (j == 0) {
            // if we failed to match above, lets try the messageformat handler instead.
            j = net.minecraftforge.fml.TextComponentMessageFormatHandler.handle(this, this.decomposedParts, this.args, pFormatTemplate);
         }
         if (j < pFormatTemplate.length()) {
            String s3 = pFormatTemplate.substring(j);
            if (s3.indexOf(37) != -1) {
               throw new IllegalArgumentException();
            }

            this.decomposedParts.add(ITextProperties.of(s3));
         }

      } catch (IllegalArgumentException illegalargumentexception) {
         throw new TranslationTextComponentFormatException(this, illegalargumentexception);
      }
   }

   private ITextProperties getArgument(int pIndex) {
      if (pIndex >= this.args.length) {
         throw new TranslationTextComponentFormatException(this, pIndex);
      } else {
         Object object = this.args[pIndex];
         if (object instanceof ITextComponent) {
            return (ITextComponent)object;
         } else {
            return object == null ? TEXT_NULL : ITextProperties.of(object.toString());
         }
      }
   }

   /**
    * Creates a copy of this component, losing any style or siblings.
    */
   public TranslationTextComponent plainCopy() {
      return new TranslationTextComponent(this.key, this.args);
   }

   @OnlyIn(Dist.CLIENT)
   public <T> Optional<T> visitSelf(ITextProperties.IStyledTextAcceptor<T> pConsumer, Style pStyle) {
      this.decompose();

      for(ITextProperties itextproperties : this.decomposedParts) {
         Optional<T> optional = itextproperties.visit(pConsumer, pStyle);
         if (optional.isPresent()) {
            return optional;
         }
      }

      return Optional.empty();
   }

   public <T> Optional<T> visitSelf(ITextProperties.ITextAcceptor<T> pConsumer) {
      this.decompose();

      for(ITextProperties itextproperties : this.decomposedParts) {
         Optional<T> optional = itextproperties.visit(pConsumer);
         if (optional.isPresent()) {
            return optional;
         }
      }

      return Optional.empty();
   }

   public IFormattableTextComponent resolve(@Nullable CommandSource pCommandSourceStack, @Nullable Entity pEntity, int pRecursionDepth) throws CommandSyntaxException {
      Object[] aobject = new Object[this.args.length];

      for(int i = 0; i < aobject.length; ++i) {
         Object object = this.args[i];
         if (object instanceof ITextComponent) {
            aobject[i] = TextComponentUtils.updateForEntity(pCommandSourceStack, (ITextComponent)object, pEntity, pRecursionDepth);
         } else {
            aobject[i] = object;
         }
      }

      return new TranslationTextComponent(this.key, aobject);
   }

   public boolean equals(Object p_equals_1_) {
      if (this == p_equals_1_) {
         return true;
      } else if (!(p_equals_1_ instanceof TranslationTextComponent)) {
         return false;
      } else {
         TranslationTextComponent translationtextcomponent = (TranslationTextComponent)p_equals_1_;
         return Arrays.equals(this.args, translationtextcomponent.args) && this.key.equals(translationtextcomponent.key) && super.equals(p_equals_1_);
      }
   }

   public int hashCode() {
      int i = super.hashCode();
      i = 31 * i + this.key.hashCode();
      return 31 * i + Arrays.hashCode(this.args);
   }

   public String toString() {
      return "TranslatableComponent{key='" + this.key + '\'' + ", args=" + Arrays.toString(this.args) + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
   }

   /**
    * Gets the key used to translate this component.
    */
   public String getKey() {
      return this.key;
   }

   /**
    * Gets the object array that is used to translate the key.
    */
   public Object[] getArgs() {
      return this.args;
   }
}
