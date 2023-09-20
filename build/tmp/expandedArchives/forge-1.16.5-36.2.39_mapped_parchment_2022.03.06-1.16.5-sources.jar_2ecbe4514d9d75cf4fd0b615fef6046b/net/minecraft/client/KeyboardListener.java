package net.minecraft.client;

import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.client.gui.screen.ControlsScreen;
import net.minecraft.client.gui.screen.GamemodeSelectionScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WithNarratorSettingsScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.client.util.NativeUtil;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KeyboardListener {
   private final Minecraft minecraft;
   private boolean sendRepeatsToGui;
   private final ClipboardHelper clipboardManager = new ClipboardHelper();
   private long debugCrashKeyTime = -1L;
   private long debugCrashKeyReportedTime = -1L;
   private long debugCrashKeyReportedCount = -1L;
   private boolean handledDebugKey;

   public KeyboardListener(Minecraft p_i47674_1_) {
      this.minecraft = p_i47674_1_;
   }

   private void debugFeedbackTranslated(String pMessage, Object... pArgs) {
      this.minecraft.gui.getChat().addMessage((new StringTextComponent("")).append((new TranslationTextComponent("debug.prefix")).withStyle(new TextFormatting[]{TextFormatting.YELLOW, TextFormatting.BOLD})).append(" ").append(new TranslationTextComponent(pMessage, pArgs)));
   }

   private void debugWarningTranslated(String pMessage, Object... pArgs) {
      this.minecraft.gui.getChat().addMessage((new StringTextComponent("")).append((new TranslationTextComponent("debug.prefix")).withStyle(new TextFormatting[]{TextFormatting.RED, TextFormatting.BOLD})).append(" ").append(new TranslationTextComponent(pMessage, pArgs)));
   }

   private boolean handleDebugKeys(int pKey) {
      if (this.debugCrashKeyTime > 0L && this.debugCrashKeyTime < Util.getMillis() - 100L) {
         return true;
      } else {
         switch(pKey) {
         case 65:
            this.minecraft.levelRenderer.allChanged();
            this.debugFeedbackTranslated("debug.reload_chunks.message");
            return true;
         case 66:
            boolean flag = !this.minecraft.getEntityRenderDispatcher().shouldRenderHitBoxes();
            this.minecraft.getEntityRenderDispatcher().setRenderHitBoxes(flag);
            this.debugFeedbackTranslated(flag ? "debug.show_hitboxes.on" : "debug.show_hitboxes.off");
            return true;
         case 67:
            if (this.minecraft.player.isReducedDebugInfo()) {
               return false;
            } else {
               ClientPlayNetHandler clientplaynethandler = this.minecraft.player.connection;
               if (clientplaynethandler == null) {
                  return false;
               }

               this.debugFeedbackTranslated("debug.copy_location.message");
               this.setClipboard(String.format(Locale.ROOT, "/execute in %s run tp @s %.2f %.2f %.2f %.2f %.2f", this.minecraft.player.level.dimension().location(), this.minecraft.player.getX(), this.minecraft.player.getY(), this.minecraft.player.getZ(), this.minecraft.player.yRot, this.minecraft.player.xRot));
               return true;
            }
         case 68:
            if (this.minecraft.gui != null) {
               this.minecraft.gui.getChat().clearMessages(false);
            }

            return true;
         case 70:
            AbstractOption.RENDER_DISTANCE.set(this.minecraft.options, MathHelper.clamp((double)(this.minecraft.options.renderDistance + (Screen.hasShiftDown() ? -1 : 1)), AbstractOption.RENDER_DISTANCE.getMinValue(), AbstractOption.RENDER_DISTANCE.getMaxValue()));
            this.debugFeedbackTranslated("debug.cycle_renderdistance.message", this.minecraft.options.renderDistance);
            return true;
         case 71:
            boolean flag1 = this.minecraft.debugRenderer.switchRenderChunkborder();
            this.debugFeedbackTranslated(flag1 ? "debug.chunk_boundaries.on" : "debug.chunk_boundaries.off");
            return true;
         case 72:
            this.minecraft.options.advancedItemTooltips = !this.minecraft.options.advancedItemTooltips;
            this.debugFeedbackTranslated(this.minecraft.options.advancedItemTooltips ? "debug.advanced_tooltips.on" : "debug.advanced_tooltips.off");
            this.minecraft.options.save();
            return true;
         case 73:
            if (!this.minecraft.player.isReducedDebugInfo()) {
               this.copyRecreateCommand(this.minecraft.player.hasPermissions(2), !Screen.hasShiftDown());
            }

            return true;
         case 78:
            if (!this.minecraft.player.hasPermissions(2)) {
               this.debugFeedbackTranslated("debug.creative_spectator.error");
            } else if (!this.minecraft.player.isSpectator()) {
               this.minecraft.player.chat("/gamemode spectator");
            } else {
               this.minecraft.player.chat("/gamemode " + this.minecraft.gameMode.getPreviousPlayerMode().getName());
            }

            return true;
         case 80:
            this.minecraft.options.pauseOnLostFocus = !this.minecraft.options.pauseOnLostFocus;
            this.minecraft.options.save();
            this.debugFeedbackTranslated(this.minecraft.options.pauseOnLostFocus ? "debug.pause_focus.on" : "debug.pause_focus.off");
            return true;
         case 81:
            this.debugFeedbackTranslated("debug.help.message");
            NewChatGui newchatgui = this.minecraft.gui.getChat();
            newchatgui.addMessage(new TranslationTextComponent("debug.reload_chunks.help"));
            newchatgui.addMessage(new TranslationTextComponent("debug.show_hitboxes.help"));
            newchatgui.addMessage(new TranslationTextComponent("debug.copy_location.help"));
            newchatgui.addMessage(new TranslationTextComponent("debug.clear_chat.help"));
            newchatgui.addMessage(new TranslationTextComponent("debug.cycle_renderdistance.help"));
            newchatgui.addMessage(new TranslationTextComponent("debug.chunk_boundaries.help"));
            newchatgui.addMessage(new TranslationTextComponent("debug.advanced_tooltips.help"));
            newchatgui.addMessage(new TranslationTextComponent("debug.inspect.help"));
            newchatgui.addMessage(new TranslationTextComponent("debug.creative_spectator.help"));
            newchatgui.addMessage(new TranslationTextComponent("debug.pause_focus.help"));
            newchatgui.addMessage(new TranslationTextComponent("debug.help.help"));
            newchatgui.addMessage(new TranslationTextComponent("debug.reload_resourcepacks.help"));
            newchatgui.addMessage(new TranslationTextComponent("debug.pause.help"));
            newchatgui.addMessage(new TranslationTextComponent("debug.gamemodes.help"));
            return true;
         case 84:
            this.debugFeedbackTranslated("debug.reload_resourcepacks.message");
            this.minecraft.reloadResourcePacks();
            return true;
         case 293:
            if (!this.minecraft.player.hasPermissions(2)) {
               this.debugFeedbackTranslated("debug.gamemodes.error");
            } else {
               this.minecraft.setScreen(new GamemodeSelectionScreen());
            }

            return true;
         default:
            return false;
         }
      }
   }

   private void copyRecreateCommand(boolean pPrivileged, boolean pAskServer) {
      RayTraceResult raytraceresult = this.minecraft.hitResult;
      if (raytraceresult != null) {
         switch(raytraceresult.getType()) {
         case BLOCK:
            BlockPos blockpos = ((BlockRayTraceResult)raytraceresult).getBlockPos();
            BlockState blockstate = this.minecraft.player.level.getBlockState(blockpos);
            if (pPrivileged) {
               if (pAskServer) {
                  this.minecraft.player.connection.getDebugQueryHandler().queryBlockEntityTag(blockpos, (p_211561_3_) -> {
                     this.copyCreateBlockCommand(blockstate, blockpos, p_211561_3_);
                     this.debugFeedbackTranslated("debug.inspect.server.block");
                  });
               } else {
                  TileEntity tileentity = this.minecraft.player.level.getBlockEntity(blockpos);
                  CompoundNBT compoundnbt1 = tileentity != null ? tileentity.save(new CompoundNBT()) : null;
                  this.copyCreateBlockCommand(blockstate, blockpos, compoundnbt1);
                  this.debugFeedbackTranslated("debug.inspect.client.block");
               }
            } else {
               this.copyCreateBlockCommand(blockstate, blockpos, (CompoundNBT)null);
               this.debugFeedbackTranslated("debug.inspect.client.block");
            }
            break;
         case ENTITY:
            Entity entity = ((EntityRayTraceResult)raytraceresult).getEntity();
            ResourceLocation resourcelocation = Registry.ENTITY_TYPE.getKey(entity.getType());
            if (pPrivileged) {
               if (pAskServer) {
                  this.minecraft.player.connection.getDebugQueryHandler().queryEntityTag(entity.getId(), (p_227999_3_) -> {
                     this.copyCreateEntityCommand(resourcelocation, entity.position(), p_227999_3_);
                     this.debugFeedbackTranslated("debug.inspect.server.entity");
                  });
               } else {
                  CompoundNBT compoundnbt = entity.saveWithoutId(new CompoundNBT());
                  this.copyCreateEntityCommand(resourcelocation, entity.position(), compoundnbt);
                  this.debugFeedbackTranslated("debug.inspect.client.entity");
               }
            } else {
               this.copyCreateEntityCommand(resourcelocation, entity.position(), (CompoundNBT)null);
               this.debugFeedbackTranslated("debug.inspect.client.entity");
            }
         }

      }
   }

   private void copyCreateBlockCommand(BlockState pState, BlockPos pPos, @Nullable CompoundNBT pCompound) {
      if (pCompound != null) {
         pCompound.remove("x");
         pCompound.remove("y");
         pCompound.remove("z");
         pCompound.remove("id");
      }

      StringBuilder stringbuilder = new StringBuilder(BlockStateParser.serialize(pState));
      if (pCompound != null) {
         stringbuilder.append((Object)pCompound);
      }

      String s = String.format(Locale.ROOT, "/setblock %d %d %d %s", pPos.getX(), pPos.getY(), pPos.getZ(), stringbuilder);
      this.setClipboard(s);
   }

   private void copyCreateEntityCommand(ResourceLocation pEntityId, Vector3d pPos, @Nullable CompoundNBT pCompound) {
      String s;
      if (pCompound != null) {
         pCompound.remove("UUID");
         pCompound.remove("Pos");
         pCompound.remove("Dimension");
         String s1 = pCompound.getPrettyDisplay().getString();
         s = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f %s", pEntityId.toString(), pPos.x, pPos.y, pPos.z, s1);
      } else {
         s = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f", pEntityId.toString(), pPos.x, pPos.y, pPos.z);
      }

      this.setClipboard(s);
   }

   public void keyPress(long pWindowPointer, int pKey, int pScanCode, int pAction, int pModifiers) {
      if (pWindowPointer == this.minecraft.getWindow().getWindow()) {
         if (this.debugCrashKeyTime > 0L) {
            if (!InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 67) || !InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292)) {
               this.debugCrashKeyTime = -1L;
            }
         } else if (InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 67) && InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292)) {
            this.handledDebugKey = true;
            this.debugCrashKeyTime = Util.getMillis();
            this.debugCrashKeyReportedTime = Util.getMillis();
            this.debugCrashKeyReportedCount = 0L;
         }

         INestedGuiEventHandler inestedguieventhandler = this.minecraft.screen;

         if ((!(this.minecraft.screen instanceof ControlsScreen) || ((ControlsScreen)inestedguieventhandler).lastKeySelection <= Util.getMillis() - 20L)) {
            if (pAction == 1) {
            if (this.minecraft.options.keyFullscreen.matches(pKey, pScanCode)) {
               this.minecraft.getWindow().toggleFullScreen();
               this.minecraft.options.fullscreen = this.minecraft.getWindow().isFullscreen();
               this.minecraft.options.save();
               return;
            }

            if (this.minecraft.options.keyScreenshot.matches(pKey, pScanCode)) {
               if (Screen.hasControlDown()) {
               }

               ScreenShotHelper.grab(this.minecraft.gameDirectory, this.minecraft.getWindow().getWidth(), this.minecraft.getWindow().getHeight(), this.minecraft.getMainRenderTarget(), (p_212449_1_) -> {
                  this.minecraft.execute(() -> {
                     this.minecraft.gui.getChat().addMessage(p_212449_1_);
                  });
               });
               return;
            }
            } else if (pAction == 0 /*GLFW_RELEASE*/ && this.minecraft.screen instanceof ControlsScreen)
               ((ControlsScreen)this.minecraft.screen).selectedKey = null; //Forge: Unset pure modifiers.
         }

         boolean flag = inestedguieventhandler == null || !(inestedguieventhandler.getFocused() instanceof TextFieldWidget) || !((TextFieldWidget)inestedguieventhandler.getFocused()).canConsumeInput();
         if (pAction != 0 && pKey == 66 && Screen.hasControlDown() && flag) {
            AbstractOption.NARRATOR.toggle(this.minecraft.options, 1);
            if (inestedguieventhandler instanceof WithNarratorSettingsScreen) {
               ((WithNarratorSettingsScreen)inestedguieventhandler).updateNarratorButton();
            }
         }

         if (inestedguieventhandler != null) {
            boolean[] aboolean = new boolean[]{false};
            Screen.wrapScreenError(() -> {
               if (pAction != 1 && (pAction != 2 || !this.sendRepeatsToGui)) {
                  if (pAction == 0) {
                     aboolean[0] = net.minecraftforge.client.ForgeHooksClient.onGuiKeyReleasedPre(this.minecraft.screen, pKey, pScanCode, pModifiers);
                     if (!aboolean[0]) aboolean[0] = inestedguieventhandler.keyReleased(pKey, pScanCode, pModifiers);
                     if (!aboolean[0]) aboolean[0] = net.minecraftforge.client.ForgeHooksClient.onGuiKeyReleasedPost(this.minecraft.screen, pKey, pScanCode, pModifiers);
                  }
               } else {
                  aboolean[0] = net.minecraftforge.client.ForgeHooksClient.onGuiKeyPressedPre(this.minecraft.screen, pKey, pScanCode, pModifiers);
                  if (!aboolean[0]) aboolean[0] = inestedguieventhandler.keyPressed(pKey, pScanCode, pModifiers);
                  if (!aboolean[0]) aboolean[0] = net.minecraftforge.client.ForgeHooksClient.onGuiKeyPressedPost(this.minecraft.screen, pKey, pScanCode, pModifiers);
               }

            }, "keyPressed event handler", inestedguieventhandler.getClass().getCanonicalName());
            if (aboolean[0]) {
               return;
            }
         }

         if (this.minecraft.screen == null || this.minecraft.screen.passEvents) {
            InputMappings.Input inputmappings$input = InputMappings.getKey(pKey, pScanCode);
            if (pAction == 0) {
               KeyBinding.set(inputmappings$input, false);
               if (pKey == 292) {
                  if (this.handledDebugKey) {
                     this.handledDebugKey = false;
                  } else {
                     this.minecraft.options.renderDebug = !this.minecraft.options.renderDebug;
                     this.minecraft.options.renderDebugCharts = this.minecraft.options.renderDebug && Screen.hasShiftDown();
                     this.minecraft.options.renderFpsChart = this.minecraft.options.renderDebug && Screen.hasAltDown();
                  }
               }
            } else {
               if (pKey == 293 && this.minecraft.gameRenderer != null) {
                  this.minecraft.gameRenderer.togglePostEffect();
               }

               boolean flag1 = false;
               if (this.minecraft.screen == null) {
                  if (pKey == 256) {
                     boolean flag2 = InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292);
                     this.minecraft.pauseGame(flag2);
                  }

                  flag1 = InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292) && this.handleDebugKeys(pKey);
                  this.handledDebugKey |= flag1;
                  if (pKey == 290) {
                     this.minecraft.options.hideGui = !this.minecraft.options.hideGui;
                  }
               }

               if (flag1) {
                  KeyBinding.set(inputmappings$input, false);
               } else {
                  KeyBinding.set(inputmappings$input, true);
                  KeyBinding.click(inputmappings$input);
               }

               if (this.minecraft.options.renderDebugCharts && pKey >= 48 && pKey <= 57) {
                  this.minecraft.debugFpsMeterKeyPress(pKey - 48);
               }
            }
         }
         net.minecraftforge.client.ForgeHooksClient.fireKeyInput(pKey, pScanCode, pAction, pModifiers);
      }
   }

   private void charTyped(long pWindowPointer, int pCodePoint, int pModifiers) {
      if (pWindowPointer == this.minecraft.getWindow().getWindow()) {
         IGuiEventListener iguieventlistener = this.minecraft.screen;
         if (iguieventlistener != null && this.minecraft.getOverlay() == null) {
            if (Character.charCount(pCodePoint) == 1) {
               Screen.wrapScreenError(() -> {
                  if (net.minecraftforge.client.ForgeHooksClient.onGuiCharTypedPre(this.minecraft.screen, (char)pCodePoint, pModifiers)) return;
                  if (iguieventlistener.charTyped((char)pCodePoint, pModifiers)) return;
                  net.minecraftforge.client.ForgeHooksClient.onGuiCharTypedPost(this.minecraft.screen, (char)pCodePoint, pModifiers);
               }, "charTyped event handler", iguieventlistener.getClass().getCanonicalName());
            } else {
               for(char c0 : Character.toChars(pCodePoint)) {
                  Screen.wrapScreenError(() -> {
                     if (net.minecraftforge.client.ForgeHooksClient.onGuiCharTypedPre(this.minecraft.screen, c0, pModifiers)) return;
                     if (iguieventlistener.charTyped(c0, pModifiers)) return;
                     net.minecraftforge.client.ForgeHooksClient.onGuiCharTypedPost(this.minecraft.screen, c0, pModifiers);
                  }, "charTyped event handler", iguieventlistener.getClass().getCanonicalName());
               }
            }

         }
      }
   }

   public void setSendRepeatsToGui(boolean pRepeatEvents) {
      this.sendRepeatsToGui = pRepeatEvents;
   }

   public void setup(long pWindow) {
      InputMappings.setupKeyboardCallbacks(pWindow, (p_228001_1_, p_228001_3_, p_228001_4_, p_228001_5_, p_228001_6_) -> {
         this.minecraft.execute(() -> {
            this.keyPress(p_228001_1_, p_228001_3_, p_228001_4_, p_228001_5_, p_228001_6_);
         });
      }, (p_228000_1_, p_228000_3_, p_228000_4_) -> {
         this.minecraft.execute(() -> {
            this.charTyped(p_228000_1_, p_228000_3_, p_228000_4_);
         });
      });
   }

   public String getClipboard() {
      return this.clipboardManager.getClipboard(this.minecraft.getWindow().getWindow(), (p_227998_1_, p_227998_2_) -> {
         if (p_227998_1_ != 65545) {
            this.minecraft.getWindow().defaultErrorCallback(p_227998_1_, p_227998_2_);
         }

      });
   }

   public void setClipboard(String pString) {
      this.clipboardManager.setClipboard(this.minecraft.getWindow().getWindow(), pString);
   }

   public void tick() {
      if (this.debugCrashKeyTime > 0L) {
         long i = Util.getMillis();
         long j = 10000L - (i - this.debugCrashKeyTime);
         long k = i - this.debugCrashKeyReportedTime;
         if (j < 0L) {
            if (Screen.hasControlDown()) {
               NativeUtil.youJustLostTheGame();
            }

            throw new ReportedException(new CrashReport("Manually triggered debug crash", new Throwable()));
         }

         if (k >= 1000L) {
            if (this.debugCrashKeyReportedCount == 0L) {
               this.debugFeedbackTranslated("debug.crash.message");
            } else {
               this.debugWarningTranslated("debug.crash.warning", MathHelper.ceil((float)j / 1000.0F));
            }

            this.debugCrashKeyReportedTime = i;
            ++this.debugCrashKeyReportedCount;
         }
      }

   }
}
