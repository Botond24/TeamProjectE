package net.minecraft.util;

import com.mojang.blaze3d.systems.RenderSystem;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ScreenShotHelper {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");

   /**
    * Saves a screenshot in the game directory with a time-stamped filename.
    * Returns an ITextComponent indicating the success/failure of the saving.
    */
   public static void grab(File pGameDirectory, int pWidth, int pHeight, Framebuffer pBuffer, Consumer<ITextComponent> pMessageConsumer) {
      grab(pGameDirectory, (String)null, pWidth, pHeight, pBuffer, pMessageConsumer);
   }

   /**
    * Saves a screenshot in the game directory with the given file name (or null to generate a time-stamped name).
    * Returns an ITextComponent indicating the success/failure of the saving.
    */
   public static void grab(File pGameDirectory, @Nullable String pScreenshotName, int pWidth, int pHeight, Framebuffer pBuffer, Consumer<ITextComponent> pMessageConsumer) {
      if (!RenderSystem.isOnRenderThread()) {
         RenderSystem.recordRenderCall(() -> {
            _grab(pGameDirectory, pScreenshotName, pWidth, pHeight, pBuffer, pMessageConsumer);
         });
      } else {
         _grab(pGameDirectory, pScreenshotName, pWidth, pHeight, pBuffer, pMessageConsumer);
      }

   }

   private static void _grab(File pGameDirectory, @Nullable String pScreenshotName, int pWidth, int pHeight, Framebuffer pBuffer, Consumer<ITextComponent> pMessageConsumer) {
      NativeImage nativeimage = takeScreenshot(pWidth, pHeight, pBuffer);
      File file1 = new File(pGameDirectory, "screenshots");
      file1.mkdir();
      File file2;
      if (pScreenshotName == null) {
         file2 = getFile(file1);
      } else {
         file2 = new File(file1, pScreenshotName);
      }

      net.minecraftforge.client.event.ScreenshotEvent event = net.minecraftforge.client.ForgeHooksClient.onScreenshot(nativeimage, file2);
      if (event.isCanceled()) {
         pMessageConsumer.accept(event.getCancelMessage());
         return;
      }
      final File target = event.getScreenshotFile();

      Util.ioPool().execute(() -> {
         try {
            nativeimage.writeToFile(target);
            ITextComponent itextcomponent = (new StringTextComponent(file2.getName())).withStyle(TextFormatting.UNDERLINE).withStyle((p_238335_1_) -> {
               return p_238335_1_.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, target.getAbsolutePath()));
            });
            if (event.getResultMessage() != null)
               pMessageConsumer.accept(event.getResultMessage());
            else
               pMessageConsumer.accept(new TranslationTextComponent("screenshot.success", itextcomponent));
         } catch (Exception exception) {
            LOGGER.warn("Couldn't save screenshot", (Throwable)exception);
            pMessageConsumer.accept(new TranslationTextComponent("screenshot.failure", exception.getMessage()));
         } finally {
            nativeimage.close();
         }

      });
   }

   public static NativeImage takeScreenshot(int pWidth, int pHeight, Framebuffer pFramebuffer) {
      pWidth = pFramebuffer.width;
      pHeight = pFramebuffer.height;
      NativeImage nativeimage = new NativeImage(pWidth, pHeight, false);
      RenderSystem.bindTexture(pFramebuffer.getColorTextureId());
      nativeimage.downloadTexture(0, true);
      nativeimage.flipY();
      return nativeimage;
   }

   /**
    * Creates a unique PNG file in the given directory named by a timestamp.  Handles cases where the timestamp alone is
    * not enough to create a uniquely named file, though it still might suffer from an unlikely race condition where the
    * filename was unique when this method was called, but another process or thread created a file at the same path
    * immediately after this method returned.
    */
   private static File getFile(File pGameDirectory) {
      String s = DATE_FORMAT.format(new Date());
      int i = 1;

      while(true) {
         File file1 = new File(pGameDirectory, s + (i == 1 ? "" : "_" + i) + ".png");
         if (!file1.exists()) {
            return file1;
         }

         ++i;
      }
   }
}
