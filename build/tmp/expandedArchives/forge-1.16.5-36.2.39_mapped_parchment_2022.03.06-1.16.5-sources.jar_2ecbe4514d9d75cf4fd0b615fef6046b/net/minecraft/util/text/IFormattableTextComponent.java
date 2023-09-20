package net.minecraft.util.text;

import java.util.function.UnaryOperator;

/**
 * A Component which can have its Style and siblings modified.
 */
public interface IFormattableTextComponent extends ITextComponent {
   /**
    * Sets the style for this component and returns the component itself.
    */
   IFormattableTextComponent setStyle(Style pStyle);

   /**
    * Add the given text to this component's siblings.
    * 
    * Note: If this component turns the text bold, that will apply to all the siblings until a later sibling turns the
    * text something else.
    */
   default IFormattableTextComponent append(String pString) {
      return this.append(new StringTextComponent(pString));
   }

   /**
    * Add the given component to this component's siblings.
    * 
    * Note: If this component turns the text bold, that will apply to all the siblings until a later sibling turns the
    * text something else.
    */
   IFormattableTextComponent append(ITextComponent pSibling);

   default IFormattableTextComponent withStyle(UnaryOperator<Style> pModifyFunc) {
      this.setStyle(pModifyFunc.apply(this.getStyle()));
      return this;
   }

   default IFormattableTextComponent withStyle(Style pStyle) {
      this.setStyle(pStyle.applyTo(this.getStyle()));
      return this;
   }

   default IFormattableTextComponent withStyle(TextFormatting... pFormats) {
      this.setStyle(this.getStyle().applyFormats(pFormats));
      return this;
   }

   default IFormattableTextComponent withStyle(TextFormatting pFormat) {
      this.setStyle(this.getStyle().applyFormat(pFormat));
      return this;
   }
}