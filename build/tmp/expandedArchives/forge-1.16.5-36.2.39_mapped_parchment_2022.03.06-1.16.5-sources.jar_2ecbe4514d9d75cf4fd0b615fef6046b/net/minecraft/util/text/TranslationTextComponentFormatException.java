package net.minecraft.util.text;

public class TranslationTextComponentFormatException extends IllegalArgumentException {
   public TranslationTextComponentFormatException(TranslationTextComponent pComponent, String pError) {
      super(String.format("Error parsing: %s: %s", pComponent, pError));
   }

   public TranslationTextComponentFormatException(TranslationTextComponent pComponent, int pInvalidIndex) {
      super(String.format("Invalid index %d requested for %s", pInvalidIndex, pComponent));
   }

   public TranslationTextComponentFormatException(TranslationTextComponent pComponent, Throwable pCause) {
      super(String.format("Error while parsing: %s", pComponent), pCause);
   }
}