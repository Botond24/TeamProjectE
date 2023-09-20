package net.minecraft.client.util;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.util.LazyValue;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.LanguageMap;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharModsCallbackI;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWDropCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;

@OnlyIn(Dist.CLIENT)
public class InputMappings {
   @Nullable
   private static final MethodHandle glfwRawMouseMotionSupported;
   private static final int GLFW_RAW_MOUSE_MOTION;
   public static final InputMappings.Input UNKNOWN;

   public static InputMappings.Input getKey(int pKeyCode, int pScanCode) {
      return pKeyCode == -1 ? InputMappings.Type.SCANCODE.getOrCreate(pScanCode) : InputMappings.Type.KEYSYM.getOrCreate(pKeyCode);
   }

   public static InputMappings.Input getKey(String pName) {
      if (InputMappings.Input.NAME_MAP.containsKey(pName)) {
         return InputMappings.Input.NAME_MAP.get(pName);
      } else {
         for(InputMappings.Type inputmappings$type : InputMappings.Type.values()) {
            if (pName.startsWith(inputmappings$type.defaultPrefix)) {
               String s = pName.substring(inputmappings$type.defaultPrefix.length() + 1);
               return inputmappings$type.getOrCreate(Integer.parseInt(s));
            }
         }

         throw new IllegalArgumentException("Unknown key name: " + pName);
      }
   }

   public static boolean isKeyDown(long pWindow, int pKey) {
      return GLFW.glfwGetKey(pWindow, pKey) == 1;
   }

   public static void setupKeyboardCallbacks(long pWindow, GLFWKeyCallbackI pKeyCallback, GLFWCharModsCallbackI pCharModifierCallback) {
      GLFW.glfwSetKeyCallback(pWindow, pKeyCallback);
      GLFW.glfwSetCharModsCallback(pWindow, pCharModifierCallback);
   }

   public static void setupMouseCallbacks(long pWindow, GLFWCursorPosCallbackI pCursorPositionCallback, GLFWMouseButtonCallbackI pMouseButtonCallback, GLFWScrollCallbackI pScrollCallback, GLFWDropCallbackI pDragAndDropCallback) {
      GLFW.glfwSetCursorPosCallback(pWindow, pCursorPositionCallback);
      GLFW.glfwSetMouseButtonCallback(pWindow, pMouseButtonCallback);
      GLFW.glfwSetScrollCallback(pWindow, pScrollCallback);
      GLFW.glfwSetDropCallback(pWindow, pDragAndDropCallback);
   }

   public static void grabOrReleaseMouse(long pWindow, int pCursorValue, double pXPos, double pYPos) {
      GLFW.glfwSetCursorPos(pWindow, pXPos, pYPos);
      GLFW.glfwSetInputMode(pWindow, 208897, pCursorValue);
   }

   public static boolean isRawMouseInputSupported() {
      try {
         return glfwRawMouseMotionSupported != null && (boolean) glfwRawMouseMotionSupported.invokeExact();
      } catch (Throwable throwable) {
         throw new RuntimeException(throwable);
      }
   }

   public static void updateRawMouseInput(long pWindow, boolean pEnableRawMouseMotion) {
      if (isRawMouseInputSupported()) {
         GLFW.glfwSetInputMode(pWindow, GLFW_RAW_MOUSE_MOTION, pEnableRawMouseMotion ? 1 : 0);
      }

   }

   static {
      Lookup lookup = MethodHandles.lookup();
      MethodType methodtype = MethodType.methodType(Boolean.TYPE);
      MethodHandle methodhandle = null;
      int i = 0;

      try {
         methodhandle = lookup.findStatic(GLFW.class, "glfwRawMouseMotionSupported", methodtype);
         MethodHandle methodhandle1 = lookup.findStaticGetter(GLFW.class, "GLFW_RAW_MOUSE_MOTION", Integer.TYPE);
         i = (int)methodhandle1.invokeExact();
      } catch (NoSuchFieldException | NoSuchMethodException nosuchmethodexception) {
      } catch (Throwable throwable) {
         throw new RuntimeException(throwable);
      }

      glfwRawMouseMotionSupported = methodhandle;
      GLFW_RAW_MOUSE_MOTION = i;
      UNKNOWN = InputMappings.Type.KEYSYM.getOrCreate(-1);
   }

   @OnlyIn(Dist.CLIENT)
   public static final class Input {
      private final String name;
      private final InputMappings.Type type;
      private final int value;
      private final LazyValue<ITextComponent> displayName;
      private static final Map<String, InputMappings.Input> NAME_MAP = Maps.newHashMap();

      private Input(String pName, InputMappings.Type pType, int pValue) {
         this.name = pName;
         this.type = pType;
         this.value = pValue;
         this.displayName = new LazyValue<>(() -> {
            return pType.displayTextSupplier.apply(pValue, pName);
         });
         NAME_MAP.put(pName, this);
      }

      public InputMappings.Type getType() {
         return this.type;
      }

      public int getValue() {
         return this.value;
      }

      public String getName() {
         return this.name;
      }

      public ITextComponent getDisplayName() {
         return this.displayName.get();
      }

      public OptionalInt getNumericKeyValue() {
         if (this.value >= 48 && this.value <= 57) {
            return OptionalInt.of(this.value - 48);
         } else {
            return this.value >= 320 && this.value <= 329 ? OptionalInt.of(this.value - 320) : OptionalInt.empty();
         }
      }

      public boolean equals(Object p_equals_1_) {
         if (this == p_equals_1_) {
            return true;
         } else if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
            InputMappings.Input inputmappings$input = (InputMappings.Input)p_equals_1_;
            return this.value == inputmappings$input.value && this.type == inputmappings$input.type;
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(this.type, this.value);
      }

      public String toString() {
         return this.name;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static enum Type {
      KEYSYM("key.keyboard", (p_237528_0_, p_237528_1_) -> {
         String s = GLFW.glfwGetKeyName(p_237528_0_, -1);
         return (ITextComponent)(s != null ? new StringTextComponent(s) : new TranslationTextComponent(p_237528_1_));
      }),
      SCANCODE("scancode", (p_237527_0_, p_237527_1_) -> {
         String s = GLFW.glfwGetKeyName(-1, p_237527_0_);
         return (ITextComponent)(s != null ? new StringTextComponent(s) : new TranslationTextComponent(p_237527_1_));
      }),
      MOUSE("key.mouse", (p_237524_0_, p_237524_1_) -> {
         return LanguageMap.getInstance().has(p_237524_1_) ? new TranslationTextComponent(p_237524_1_) : new TranslationTextComponent("key.mouse", p_237524_0_ + 1);
      });

      private final Int2ObjectMap<InputMappings.Input> map = new Int2ObjectOpenHashMap<>();
      private final String defaultPrefix;
      private final BiFunction<Integer, String, ITextComponent> displayTextSupplier;

      private static void addKey(InputMappings.Type pType, String pName, int pKeyCode) {
         InputMappings.Input inputmappings$input = new InputMappings.Input(pName, pType, pKeyCode);
         pType.map.put(pKeyCode, inputmappings$input);
      }

      private Type(String pDefaultPrefix, BiFunction<Integer, String, ITextComponent> pDisplayTextSupplier) {
         this.defaultPrefix = pDefaultPrefix;
         this.displayTextSupplier = pDisplayTextSupplier;
      }

      public InputMappings.Input getOrCreate(int pKeyCode) {
         return this.map.computeIfAbsent(pKeyCode, (p_237525_1_) -> {
            int i = p_237525_1_;
            if (this == MOUSE) {
               i = p_237525_1_ + 1;
            }

            String s = this.defaultPrefix + "." + i;
            return new InputMappings.Input(s, this, p_237525_1_);
         });
      }

      static {
         addKey(KEYSYM, "key.keyboard.unknown", -1);
         addKey(MOUSE, "key.mouse.left", 0);
         addKey(MOUSE, "key.mouse.right", 1);
         addKey(MOUSE, "key.mouse.middle", 2);
         addKey(MOUSE, "key.mouse.4", 3);
         addKey(MOUSE, "key.mouse.5", 4);
         addKey(MOUSE, "key.mouse.6", 5);
         addKey(MOUSE, "key.mouse.7", 6);
         addKey(MOUSE, "key.mouse.8", 7);
         addKey(KEYSYM, "key.keyboard.0", 48);
         addKey(KEYSYM, "key.keyboard.1", 49);
         addKey(KEYSYM, "key.keyboard.2", 50);
         addKey(KEYSYM, "key.keyboard.3", 51);
         addKey(KEYSYM, "key.keyboard.4", 52);
         addKey(KEYSYM, "key.keyboard.5", 53);
         addKey(KEYSYM, "key.keyboard.6", 54);
         addKey(KEYSYM, "key.keyboard.7", 55);
         addKey(KEYSYM, "key.keyboard.8", 56);
         addKey(KEYSYM, "key.keyboard.9", 57);
         addKey(KEYSYM, "key.keyboard.a", 65);
         addKey(KEYSYM, "key.keyboard.b", 66);
         addKey(KEYSYM, "key.keyboard.c", 67);
         addKey(KEYSYM, "key.keyboard.d", 68);
         addKey(KEYSYM, "key.keyboard.e", 69);
         addKey(KEYSYM, "key.keyboard.f", 70);
         addKey(KEYSYM, "key.keyboard.g", 71);
         addKey(KEYSYM, "key.keyboard.h", 72);
         addKey(KEYSYM, "key.keyboard.i", 73);
         addKey(KEYSYM, "key.keyboard.j", 74);
         addKey(KEYSYM, "key.keyboard.k", 75);
         addKey(KEYSYM, "key.keyboard.l", 76);
         addKey(KEYSYM, "key.keyboard.m", 77);
         addKey(KEYSYM, "key.keyboard.n", 78);
         addKey(KEYSYM, "key.keyboard.o", 79);
         addKey(KEYSYM, "key.keyboard.p", 80);
         addKey(KEYSYM, "key.keyboard.q", 81);
         addKey(KEYSYM, "key.keyboard.r", 82);
         addKey(KEYSYM, "key.keyboard.s", 83);
         addKey(KEYSYM, "key.keyboard.t", 84);
         addKey(KEYSYM, "key.keyboard.u", 85);
         addKey(KEYSYM, "key.keyboard.v", 86);
         addKey(KEYSYM, "key.keyboard.w", 87);
         addKey(KEYSYM, "key.keyboard.x", 88);
         addKey(KEYSYM, "key.keyboard.y", 89);
         addKey(KEYSYM, "key.keyboard.z", 90);
         addKey(KEYSYM, "key.keyboard.f1", 290);
         addKey(KEYSYM, "key.keyboard.f2", 291);
         addKey(KEYSYM, "key.keyboard.f3", 292);
         addKey(KEYSYM, "key.keyboard.f4", 293);
         addKey(KEYSYM, "key.keyboard.f5", 294);
         addKey(KEYSYM, "key.keyboard.f6", 295);
         addKey(KEYSYM, "key.keyboard.f7", 296);
         addKey(KEYSYM, "key.keyboard.f8", 297);
         addKey(KEYSYM, "key.keyboard.f9", 298);
         addKey(KEYSYM, "key.keyboard.f10", 299);
         addKey(KEYSYM, "key.keyboard.f11", 300);
         addKey(KEYSYM, "key.keyboard.f12", 301);
         addKey(KEYSYM, "key.keyboard.f13", 302);
         addKey(KEYSYM, "key.keyboard.f14", 303);
         addKey(KEYSYM, "key.keyboard.f15", 304);
         addKey(KEYSYM, "key.keyboard.f16", 305);
         addKey(KEYSYM, "key.keyboard.f17", 306);
         addKey(KEYSYM, "key.keyboard.f18", 307);
         addKey(KEYSYM, "key.keyboard.f19", 308);
         addKey(KEYSYM, "key.keyboard.f20", 309);
         addKey(KEYSYM, "key.keyboard.f21", 310);
         addKey(KEYSYM, "key.keyboard.f22", 311);
         addKey(KEYSYM, "key.keyboard.f23", 312);
         addKey(KEYSYM, "key.keyboard.f24", 313);
         addKey(KEYSYM, "key.keyboard.f25", 314);
         addKey(KEYSYM, "key.keyboard.num.lock", 282);
         addKey(KEYSYM, "key.keyboard.keypad.0", 320);
         addKey(KEYSYM, "key.keyboard.keypad.1", 321);
         addKey(KEYSYM, "key.keyboard.keypad.2", 322);
         addKey(KEYSYM, "key.keyboard.keypad.3", 323);
         addKey(KEYSYM, "key.keyboard.keypad.4", 324);
         addKey(KEYSYM, "key.keyboard.keypad.5", 325);
         addKey(KEYSYM, "key.keyboard.keypad.6", 326);
         addKey(KEYSYM, "key.keyboard.keypad.7", 327);
         addKey(KEYSYM, "key.keyboard.keypad.8", 328);
         addKey(KEYSYM, "key.keyboard.keypad.9", 329);
         addKey(KEYSYM, "key.keyboard.keypad.add", 334);
         addKey(KEYSYM, "key.keyboard.keypad.decimal", 330);
         addKey(KEYSYM, "key.keyboard.keypad.enter", 335);
         addKey(KEYSYM, "key.keyboard.keypad.equal", 336);
         addKey(KEYSYM, "key.keyboard.keypad.multiply", 332);
         addKey(KEYSYM, "key.keyboard.keypad.divide", 331);
         addKey(KEYSYM, "key.keyboard.keypad.subtract", 333);
         addKey(KEYSYM, "key.keyboard.down", 264);
         addKey(KEYSYM, "key.keyboard.left", 263);
         addKey(KEYSYM, "key.keyboard.right", 262);
         addKey(KEYSYM, "key.keyboard.up", 265);
         addKey(KEYSYM, "key.keyboard.apostrophe", 39);
         addKey(KEYSYM, "key.keyboard.backslash", 92);
         addKey(KEYSYM, "key.keyboard.comma", 44);
         addKey(KEYSYM, "key.keyboard.equal", 61);
         addKey(KEYSYM, "key.keyboard.grave.accent", 96);
         addKey(KEYSYM, "key.keyboard.left.bracket", 91);
         addKey(KEYSYM, "key.keyboard.minus", 45);
         addKey(KEYSYM, "key.keyboard.period", 46);
         addKey(KEYSYM, "key.keyboard.right.bracket", 93);
         addKey(KEYSYM, "key.keyboard.semicolon", 59);
         addKey(KEYSYM, "key.keyboard.slash", 47);
         addKey(KEYSYM, "key.keyboard.space", 32);
         addKey(KEYSYM, "key.keyboard.tab", 258);
         addKey(KEYSYM, "key.keyboard.left.alt", 342);
         addKey(KEYSYM, "key.keyboard.left.control", 341);
         addKey(KEYSYM, "key.keyboard.left.shift", 340);
         addKey(KEYSYM, "key.keyboard.left.win", 343);
         addKey(KEYSYM, "key.keyboard.right.alt", 346);
         addKey(KEYSYM, "key.keyboard.right.control", 345);
         addKey(KEYSYM, "key.keyboard.right.shift", 344);
         addKey(KEYSYM, "key.keyboard.right.win", 347);
         addKey(KEYSYM, "key.keyboard.enter", 257);
         addKey(KEYSYM, "key.keyboard.escape", 256);
         addKey(KEYSYM, "key.keyboard.backspace", 259);
         addKey(KEYSYM, "key.keyboard.delete", 261);
         addKey(KEYSYM, "key.keyboard.end", 269);
         addKey(KEYSYM, "key.keyboard.home", 268);
         addKey(KEYSYM, "key.keyboard.insert", 260);
         addKey(KEYSYM, "key.keyboard.page.down", 267);
         addKey(KEYSYM, "key.keyboard.page.up", 266);
         addKey(KEYSYM, "key.keyboard.caps.lock", 280);
         addKey(KEYSYM, "key.keyboard.pause", 284);
         addKey(KEYSYM, "key.keyboard.scroll.lock", 281);
         addKey(KEYSYM, "key.keyboard.menu", 348);
         addKey(KEYSYM, "key.keyboard.print.screen", 283);
         addKey(KEYSYM, "key.keyboard.world.1", 161);
         addKey(KEYSYM, "key.keyboard.world.2", 162);
      }
   }
}