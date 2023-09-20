package net.minecraft.util;

public class ResourceLocationException extends RuntimeException {
   public ResourceLocationException(String pError) {
      super(pError);
   }

   public ResourceLocationException(String pMessage, Throwable pCause) {
      super(pMessage, pCause);
   }
}