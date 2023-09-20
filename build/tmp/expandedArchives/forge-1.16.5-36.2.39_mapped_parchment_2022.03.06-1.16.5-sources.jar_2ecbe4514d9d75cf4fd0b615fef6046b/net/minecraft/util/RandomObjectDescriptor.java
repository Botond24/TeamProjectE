package net.minecraft.util;

import java.util.Random;
import java.util.UUID;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RandomObjectDescriptor {
   private static final String[] NAMES_FIRST_PART = new String[]{"Slim", "Far", "River", "Silly", "Fat", "Thin", "Fish", "Bat", "Dark", "Oak", "Sly", "Bush", "Zen", "Bark", "Cry", "Slack", "Soup", "Grim", "Hook", "Dirt", "Mud", "Sad", "Hard", "Crook", "Sneak", "Stink", "Weird", "Fire", "Soot", "Soft", "Rough", "Cling", "Scar"};
   private static final String[] NAMES_SECOND_PART = new String[]{"Fox", "Tail", "Jaw", "Whisper", "Twig", "Root", "Finder", "Nose", "Brow", "Blade", "Fry", "Seek", "Wart", "Tooth", "Foot", "Leaf", "Stone", "Fall", "Face", "Tongue", "Voice", "Lip", "Mouth", "Snail", "Toe", "Ear", "Hair", "Beard", "Shirt", "Fist"};

   public static String getEntityName(UUID pUuid) {
      Random random = getRandom(pUuid);
      return getRandomString(random, NAMES_FIRST_PART) + getRandomString(random, NAMES_SECOND_PART);
   }

   private static String getRandomString(Random pRand, String[] pStrings) {
      return Util.getRandom(pStrings, pRand);
   }

   private static Random getRandom(UUID pUuid) {
      return new Random((long)(pUuid.hashCode() >> 2));
   }
}