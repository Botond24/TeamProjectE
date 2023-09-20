package net.minecraft.client.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextProcessing;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BidiReorder {
   private final String plainText;
   private final List<Style> charStyles;
   private final Int2IntFunction reverseCharModifier;

   private BidiReorder(String pPlainText, List<Style> pCharStyles, Int2IntFunction pReverseCharModifier) {
      this.plainText = pPlainText;
      this.charStyles = ImmutableList.copyOf(pCharStyles);
      this.reverseCharModifier = pReverseCharModifier;
   }

   public String getPlainText() {
      return this.plainText;
   }

   public List<IReorderingProcessor> substring(int pFromIndex, int pToIndex, boolean pReversed) {
      if (pToIndex == 0) {
         return ImmutableList.of();
      } else {
         List<IReorderingProcessor> list = Lists.newArrayList();
         Style style = this.charStyles.get(pFromIndex);
         int i = pFromIndex;

         for(int j = 1; j < pToIndex; ++j) {
            int k = pFromIndex + j;
            Style style1 = this.charStyles.get(k);
            if (!style1.equals(style)) {
               String s = this.plainText.substring(i, k);
               list.add(pReversed ? IReorderingProcessor.backward(s, style, this.reverseCharModifier) : IReorderingProcessor.forward(s, style));
               style = style1;
               i = k;
            }
         }

         if (i < pFromIndex + pToIndex) {
            String s1 = this.plainText.substring(i, pFromIndex + pToIndex);
            list.add(pReversed ? IReorderingProcessor.backward(s1, style, this.reverseCharModifier) : IReorderingProcessor.forward(s1, style));
         }

         return pReversed ? Lists.reverse(list) : list;
      }
   }

   public static BidiReorder create(ITextProperties pFormattedText, Int2IntFunction pReverseCharModifier, UnaryOperator<String> pTextTransformer) {
      StringBuilder stringbuilder = new StringBuilder();
      List<Style> list = Lists.newArrayList();
      pFormattedText.visit((p_244289_2_, p_244289_3_) -> {
         TextProcessing.iterateFormatted(p_244289_3_, p_244289_2_, (p_244288_2_, p_244288_3_, p_244288_4_) -> {
            stringbuilder.appendCodePoint(p_244288_4_);
            int i = Character.charCount(p_244288_4_);

            for(int j = 0; j < i; ++j) {
               list.add(p_244288_3_);
            }

            return true;
         });
         return Optional.empty();
      }, Style.EMPTY);
      return new BidiReorder(pTextTransformer.apply(stringbuilder.toString()), list, pReverseCharModifier);
   }
}