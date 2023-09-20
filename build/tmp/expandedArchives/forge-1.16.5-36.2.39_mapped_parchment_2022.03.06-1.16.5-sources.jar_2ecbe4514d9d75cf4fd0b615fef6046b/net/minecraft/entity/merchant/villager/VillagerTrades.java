package net.minecraft.entity.merchant.villager;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.villager.IVillagerDataHolder;
import net.minecraft.entity.villager.VillagerType;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.IDyeableArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.potion.Effect;
import net.minecraft.potion.Effects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionBrewing;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;

public class VillagerTrades {
   public static final Map<VillagerProfession, Int2ObjectMap<VillagerTrades.ITrade[]>> TRADES = Util.make(Maps.newHashMap(), (p_221237_0_) -> {
      p_221237_0_.put(VillagerProfession.FARMER, toIntMap(ImmutableMap.of(1, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.WHEAT, 20, 16, 2), new VillagerTrades.EmeraldForItemsTrade(Items.POTATO, 26, 16, 2), new VillagerTrades.EmeraldForItemsTrade(Items.CARROT, 22, 16, 2), new VillagerTrades.EmeraldForItemsTrade(Items.BEETROOT, 15, 16, 2), new VillagerTrades.ItemsForEmeraldsTrade(Items.BREAD, 1, 6, 16, 1)}, 2, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Blocks.PUMPKIN, 6, 12, 10), new VillagerTrades.ItemsForEmeraldsTrade(Items.PUMPKIN_PIE, 1, 4, 5), new VillagerTrades.ItemsForEmeraldsTrade(Items.APPLE, 1, 4, 16, 5)}, 3, new VillagerTrades.ITrade[]{new VillagerTrades.ItemsForEmeraldsTrade(Items.COOKIE, 3, 18, 10), new VillagerTrades.EmeraldForItemsTrade(Blocks.MELON, 4, 12, 20)}, 4, new VillagerTrades.ITrade[]{new VillagerTrades.ItemsForEmeraldsTrade(Blocks.CAKE, 1, 1, 12, 15), new VillagerTrades.SuspiciousStewForEmeraldTrade(Effects.NIGHT_VISION, 100, 15), new VillagerTrades.SuspiciousStewForEmeraldTrade(Effects.JUMP, 160, 15), new VillagerTrades.SuspiciousStewForEmeraldTrade(Effects.WEAKNESS, 140, 15), new VillagerTrades.SuspiciousStewForEmeraldTrade(Effects.BLINDNESS, 120, 15), new VillagerTrades.SuspiciousStewForEmeraldTrade(Effects.POISON, 280, 15), new VillagerTrades.SuspiciousStewForEmeraldTrade(Effects.SATURATION, 7, 15)}, 5, new VillagerTrades.ITrade[]{new VillagerTrades.ItemsForEmeraldsTrade(Items.GOLDEN_CARROT, 3, 3, 30), new VillagerTrades.ItemsForEmeraldsTrade(Items.GLISTERING_MELON_SLICE, 4, 3, 30)})));
      p_221237_0_.put(VillagerProfession.FISHERMAN, toIntMap(ImmutableMap.of(1, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.STRING, 20, 16, 2), new VillagerTrades.EmeraldForItemsTrade(Items.COAL, 10, 16, 2), new VillagerTrades.ItemsForEmeraldsAndItemsTrade(Items.COD, 6, Items.COOKED_COD, 6, 16, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.COD_BUCKET, 3, 1, 16, 1)}, 2, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.COD, 15, 16, 10), new VillagerTrades.ItemsForEmeraldsAndItemsTrade(Items.SALMON, 6, Items.COOKED_SALMON, 6, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Items.CAMPFIRE, 2, 1, 5)}, 3, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.SALMON, 13, 16, 20), new VillagerTrades.EnchantedItemForEmeraldsTrade(Items.FISHING_ROD, 3, 3, 10, 0.2F)}, 4, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.TROPICAL_FISH, 6, 12, 30)}, 5, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.PUFFERFISH, 4, 12, 30), new VillagerTrades.EmeraldForVillageTypeItemTrade(1, 12, 30, ImmutableMap.<VillagerType, Item>builder().put(VillagerType.PLAINS, Items.OAK_BOAT).put(VillagerType.TAIGA, Items.SPRUCE_BOAT).put(VillagerType.SNOW, Items.SPRUCE_BOAT).put(VillagerType.DESERT, Items.JUNGLE_BOAT).put(VillagerType.JUNGLE, Items.JUNGLE_BOAT).put(VillagerType.SAVANNA, Items.ACACIA_BOAT).put(VillagerType.SWAMP, Items.DARK_OAK_BOAT).build())})));
      p_221237_0_.put(VillagerProfession.SHEPHERD, toIntMap(ImmutableMap.of(1, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Blocks.WHITE_WOOL, 18, 16, 2), new VillagerTrades.EmeraldForItemsTrade(Blocks.BROWN_WOOL, 18, 16, 2), new VillagerTrades.EmeraldForItemsTrade(Blocks.BLACK_WOOL, 18, 16, 2), new VillagerTrades.EmeraldForItemsTrade(Blocks.GRAY_WOOL, 18, 16, 2), new VillagerTrades.ItemsForEmeraldsTrade(Items.SHEARS, 2, 1, 1)}, 2, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.WHITE_DYE, 12, 16, 10), new VillagerTrades.EmeraldForItemsTrade(Items.GRAY_DYE, 12, 16, 10), new VillagerTrades.EmeraldForItemsTrade(Items.BLACK_DYE, 12, 16, 10), new VillagerTrades.EmeraldForItemsTrade(Items.LIGHT_BLUE_DYE, 12, 16, 10), new VillagerTrades.EmeraldForItemsTrade(Items.LIME_DYE, 12, 16, 10), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.WHITE_WOOL, 1, 1, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.ORANGE_WOOL, 1, 1, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.MAGENTA_WOOL, 1, 1, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.LIGHT_BLUE_WOOL, 1, 1, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.YELLOW_WOOL, 1, 1, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.LIME_WOOL, 1, 1, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.PINK_WOOL, 1, 1, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.GRAY_WOOL, 1, 1, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.LIGHT_GRAY_WOOL, 1, 1, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.CYAN_WOOL, 1, 1, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.PURPLE_WOOL, 1, 1, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.BLUE_WOOL, 1, 1, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.BROWN_WOOL, 1, 1, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.GREEN_WOOL, 1, 1, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.RED_WOOL, 1, 1, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.BLACK_WOOL, 1, 1, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.WHITE_CARPET, 1, 4, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.ORANGE_CARPET, 1, 4, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.MAGENTA_CARPET, 1, 4, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.LIGHT_BLUE_CARPET, 1, 4, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.YELLOW_CARPET, 1, 4, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.LIME_CARPET, 1, 4, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.PINK_CARPET, 1, 4, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.GRAY_CARPET, 1, 4, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.LIGHT_GRAY_CARPET, 1, 4, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.CYAN_CARPET, 1, 4, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.PURPLE_CARPET, 1, 4, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.BLUE_CARPET, 1, 4, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.BROWN_CARPET, 1, 4, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.GREEN_CARPET, 1, 4, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.RED_CARPET, 1, 4, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.BLACK_CARPET, 1, 4, 16, 5)}, 3, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.YELLOW_DYE, 12, 16, 20), new VillagerTrades.EmeraldForItemsTrade(Items.LIGHT_GRAY_DYE, 12, 16, 20), new VillagerTrades.EmeraldForItemsTrade(Items.ORANGE_DYE, 12, 16, 20), new VillagerTrades.EmeraldForItemsTrade(Items.RED_DYE, 12, 16, 20), new VillagerTrades.EmeraldForItemsTrade(Items.PINK_DYE, 12, 16, 20), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.WHITE_BED, 3, 1, 12, 10), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.YELLOW_BED, 3, 1, 12, 10), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.RED_BED, 3, 1, 12, 10), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.BLACK_BED, 3, 1, 12, 10), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.BLUE_BED, 3, 1, 12, 10), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.BROWN_BED, 3, 1, 12, 10), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.CYAN_BED, 3, 1, 12, 10), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.GRAY_BED, 3, 1, 12, 10), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.GREEN_BED, 3, 1, 12, 10), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.LIGHT_BLUE_BED, 3, 1, 12, 10), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.LIGHT_GRAY_BED, 3, 1, 12, 10), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.LIME_BED, 3, 1, 12, 10), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.MAGENTA_BED, 3, 1, 12, 10), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.ORANGE_BED, 3, 1, 12, 10), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.PINK_BED, 3, 1, 12, 10), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.PURPLE_BED, 3, 1, 12, 10)}, 4, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.BROWN_DYE, 12, 16, 30), new VillagerTrades.EmeraldForItemsTrade(Items.PURPLE_DYE, 12, 16, 30), new VillagerTrades.EmeraldForItemsTrade(Items.BLUE_DYE, 12, 16, 30), new VillagerTrades.EmeraldForItemsTrade(Items.GREEN_DYE, 12, 16, 30), new VillagerTrades.EmeraldForItemsTrade(Items.MAGENTA_DYE, 12, 16, 30), new VillagerTrades.EmeraldForItemsTrade(Items.CYAN_DYE, 12, 16, 30), new VillagerTrades.ItemsForEmeraldsTrade(Items.WHITE_BANNER, 3, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.BLUE_BANNER, 3, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.LIGHT_BLUE_BANNER, 3, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.RED_BANNER, 3, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.PINK_BANNER, 3, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.GREEN_BANNER, 3, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.LIME_BANNER, 3, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.GRAY_BANNER, 3, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.BLACK_BANNER, 3, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.PURPLE_BANNER, 3, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.MAGENTA_BANNER, 3, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.CYAN_BANNER, 3, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.BROWN_BANNER, 3, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.YELLOW_BANNER, 3, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.ORANGE_BANNER, 3, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.LIGHT_GRAY_BANNER, 3, 1, 12, 15)}, 5, new VillagerTrades.ITrade[]{new VillagerTrades.ItemsForEmeraldsTrade(Items.PAINTING, 2, 3, 30)})));
      p_221237_0_.put(VillagerProfession.FLETCHER, toIntMap(ImmutableMap.of(1, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.STICK, 32, 16, 2), new VillagerTrades.ItemsForEmeraldsTrade(Items.ARROW, 1, 16, 1), new VillagerTrades.ItemsForEmeraldsAndItemsTrade(Blocks.GRAVEL, 10, Items.FLINT, 10, 12, 1)}, 2, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.FLINT, 26, 12, 10), new VillagerTrades.ItemsForEmeraldsTrade(Items.BOW, 2, 1, 5)}, 3, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.STRING, 14, 16, 20), new VillagerTrades.ItemsForEmeraldsTrade(Items.CROSSBOW, 3, 1, 10)}, 4, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.FEATHER, 24, 16, 30), new VillagerTrades.EnchantedItemForEmeraldsTrade(Items.BOW, 2, 3, 15)}, 5, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.TRIPWIRE_HOOK, 8, 12, 30), new VillagerTrades.EnchantedItemForEmeraldsTrade(Items.CROSSBOW, 3, 3, 15), new VillagerTrades.ItemWithPotionForEmeraldsAndItemsTrade(Items.ARROW, 5, Items.TIPPED_ARROW, 5, 2, 12, 30)})));
      p_221237_0_.put(VillagerProfession.LIBRARIAN, toIntMap(ImmutableMap.<Integer, VillagerTrades.ITrade[]>builder().put(1, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.PAPER, 24, 16, 2), new VillagerTrades.EnchantedBookForEmeraldsTrade(1), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.BOOKSHELF, 9, 1, 12, 1)}).put(2, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.BOOK, 4, 12, 10), new VillagerTrades.EnchantedBookForEmeraldsTrade(5), new VillagerTrades.ItemsForEmeraldsTrade(Items.LANTERN, 1, 1, 5)}).put(3, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.INK_SAC, 5, 12, 20), new VillagerTrades.EnchantedBookForEmeraldsTrade(10), new VillagerTrades.ItemsForEmeraldsTrade(Items.GLASS, 1, 4, 10)}).put(4, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.WRITABLE_BOOK, 2, 12, 30), new VillagerTrades.EnchantedBookForEmeraldsTrade(15), new VillagerTrades.ItemsForEmeraldsTrade(Items.CLOCK, 5, 1, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.COMPASS, 4, 1, 15)}).put(5, new VillagerTrades.ITrade[]{new VillagerTrades.ItemsForEmeraldsTrade(Items.NAME_TAG, 20, 1, 30)}).build()));
      p_221237_0_.put(VillagerProfession.CARTOGRAPHER, toIntMap(ImmutableMap.of(1, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.PAPER, 24, 16, 2), new VillagerTrades.ItemsForEmeraldsTrade(Items.MAP, 7, 1, 1)}, 2, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.GLASS_PANE, 11, 16, 10), new VillagerTrades.EmeraldForMapTrade(13, Structure.OCEAN_MONUMENT, MapDecoration.Type.MONUMENT, 12, 5)}, 3, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.COMPASS, 1, 12, 20), new VillagerTrades.EmeraldForMapTrade(14, Structure.WOODLAND_MANSION, MapDecoration.Type.MANSION, 12, 10)}, 4, new VillagerTrades.ITrade[]{new VillagerTrades.ItemsForEmeraldsTrade(Items.ITEM_FRAME, 7, 1, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.WHITE_BANNER, 3, 1, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.BLUE_BANNER, 3, 1, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.LIGHT_BLUE_BANNER, 3, 1, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.RED_BANNER, 3, 1, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.PINK_BANNER, 3, 1, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.GREEN_BANNER, 3, 1, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.LIME_BANNER, 3, 1, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.GRAY_BANNER, 3, 1, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.BLACK_BANNER, 3, 1, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.PURPLE_BANNER, 3, 1, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.MAGENTA_BANNER, 3, 1, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.CYAN_BANNER, 3, 1, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.BROWN_BANNER, 3, 1, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.YELLOW_BANNER, 3, 1, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.ORANGE_BANNER, 3, 1, 15), new VillagerTrades.ItemsForEmeraldsTrade(Items.LIGHT_GRAY_BANNER, 3, 1, 15)}, 5, new VillagerTrades.ITrade[]{new VillagerTrades.ItemsForEmeraldsTrade(Items.GLOBE_BANNER_PATTER, 8, 1, 30)})));
      p_221237_0_.put(VillagerProfession.CLERIC, toIntMap(ImmutableMap.of(1, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.ROTTEN_FLESH, 32, 16, 2), new VillagerTrades.ItemsForEmeraldsTrade(Items.REDSTONE, 1, 2, 1)}, 2, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.GOLD_INGOT, 3, 12, 10), new VillagerTrades.ItemsForEmeraldsTrade(Items.LAPIS_LAZULI, 1, 1, 5)}, 3, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.RABBIT_FOOT, 2, 12, 20), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.GLOWSTONE, 4, 1, 12, 10)}, 4, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.SCUTE, 4, 12, 30), new VillagerTrades.EmeraldForItemsTrade(Items.GLASS_BOTTLE, 9, 12, 30), new VillagerTrades.ItemsForEmeraldsTrade(Items.ENDER_PEARL, 5, 1, 15)}, 5, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.NETHER_WART, 22, 12, 30), new VillagerTrades.ItemsForEmeraldsTrade(Items.EXPERIENCE_BOTTLE, 3, 1, 30)})));
      p_221237_0_.put(VillagerProfession.ARMORER, toIntMap(ImmutableMap.of(1, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.COAL, 15, 16, 2), new VillagerTrades.ItemsForEmeraldsTrade(new ItemStack(Items.IRON_LEGGINGS), 7, 1, 12, 1, 0.2F), new VillagerTrades.ItemsForEmeraldsTrade(new ItemStack(Items.IRON_BOOTS), 4, 1, 12, 1, 0.2F), new VillagerTrades.ItemsForEmeraldsTrade(new ItemStack(Items.IRON_HELMET), 5, 1, 12, 1, 0.2F), new VillagerTrades.ItemsForEmeraldsTrade(new ItemStack(Items.IRON_CHESTPLATE), 9, 1, 12, 1, 0.2F)}, 2, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.IRON_INGOT, 4, 12, 10), new VillagerTrades.ItemsForEmeraldsTrade(new ItemStack(Items.BELL), 36, 1, 12, 5, 0.2F), new VillagerTrades.ItemsForEmeraldsTrade(new ItemStack(Items.CHAINMAIL_BOOTS), 1, 1, 12, 5, 0.2F), new VillagerTrades.ItemsForEmeraldsTrade(new ItemStack(Items.CHAINMAIL_LEGGINGS), 3, 1, 12, 5, 0.2F)}, 3, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.LAVA_BUCKET, 1, 12, 20), new VillagerTrades.EmeraldForItemsTrade(Items.DIAMOND, 1, 12, 20), new VillagerTrades.ItemsForEmeraldsTrade(new ItemStack(Items.CHAINMAIL_HELMET), 1, 1, 12, 10, 0.2F), new VillagerTrades.ItemsForEmeraldsTrade(new ItemStack(Items.CHAINMAIL_CHESTPLATE), 4, 1, 12, 10, 0.2F), new VillagerTrades.ItemsForEmeraldsTrade(new ItemStack(Items.SHIELD), 5, 1, 12, 10, 0.2F)}, 4, new VillagerTrades.ITrade[]{new VillagerTrades.EnchantedItemForEmeraldsTrade(Items.DIAMOND_LEGGINGS, 14, 3, 15, 0.2F), new VillagerTrades.EnchantedItemForEmeraldsTrade(Items.DIAMOND_BOOTS, 8, 3, 15, 0.2F)}, 5, new VillagerTrades.ITrade[]{new VillagerTrades.EnchantedItemForEmeraldsTrade(Items.DIAMOND_HELMET, 8, 3, 30, 0.2F), new VillagerTrades.EnchantedItemForEmeraldsTrade(Items.DIAMOND_CHESTPLATE, 16, 3, 30, 0.2F)})));
      p_221237_0_.put(VillagerProfession.WEAPONSMITH, toIntMap(ImmutableMap.of(1, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.COAL, 15, 16, 2), new VillagerTrades.ItemsForEmeraldsTrade(new ItemStack(Items.IRON_AXE), 3, 1, 12, 1, 0.2F), new VillagerTrades.EnchantedItemForEmeraldsTrade(Items.IRON_SWORD, 2, 3, 1)}, 2, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.IRON_INGOT, 4, 12, 10), new VillagerTrades.ItemsForEmeraldsTrade(new ItemStack(Items.BELL), 36, 1, 12, 5, 0.2F)}, 3, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.FLINT, 24, 12, 20)}, 4, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.DIAMOND, 1, 12, 30), new VillagerTrades.EnchantedItemForEmeraldsTrade(Items.DIAMOND_AXE, 12, 3, 15, 0.2F)}, 5, new VillagerTrades.ITrade[]{new VillagerTrades.EnchantedItemForEmeraldsTrade(Items.DIAMOND_SWORD, 8, 3, 30, 0.2F)})));
      p_221237_0_.put(VillagerProfession.TOOLSMITH, toIntMap(ImmutableMap.of(1, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.COAL, 15, 16, 2), new VillagerTrades.ItemsForEmeraldsTrade(new ItemStack(Items.STONE_AXE), 1, 1, 12, 1, 0.2F), new VillagerTrades.ItemsForEmeraldsTrade(new ItemStack(Items.STONE_SHOVEL), 1, 1, 12, 1, 0.2F), new VillagerTrades.ItemsForEmeraldsTrade(new ItemStack(Items.STONE_PICKAXE), 1, 1, 12, 1, 0.2F), new VillagerTrades.ItemsForEmeraldsTrade(new ItemStack(Items.STONE_HOE), 1, 1, 12, 1, 0.2F)}, 2, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.IRON_INGOT, 4, 12, 10), new VillagerTrades.ItemsForEmeraldsTrade(new ItemStack(Items.BELL), 36, 1, 12, 5, 0.2F)}, 3, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.FLINT, 30, 12, 20), new VillagerTrades.EnchantedItemForEmeraldsTrade(Items.IRON_AXE, 1, 3, 10, 0.2F), new VillagerTrades.EnchantedItemForEmeraldsTrade(Items.IRON_SHOVEL, 2, 3, 10, 0.2F), new VillagerTrades.EnchantedItemForEmeraldsTrade(Items.IRON_PICKAXE, 3, 3, 10, 0.2F), new VillagerTrades.ItemsForEmeraldsTrade(new ItemStack(Items.DIAMOND_HOE), 4, 1, 3, 10, 0.2F)}, 4, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.DIAMOND, 1, 12, 30), new VillagerTrades.EnchantedItemForEmeraldsTrade(Items.DIAMOND_AXE, 12, 3, 15, 0.2F), new VillagerTrades.EnchantedItemForEmeraldsTrade(Items.DIAMOND_SHOVEL, 5, 3, 15, 0.2F)}, 5, new VillagerTrades.ITrade[]{new VillagerTrades.EnchantedItemForEmeraldsTrade(Items.DIAMOND_PICKAXE, 13, 3, 30, 0.2F)})));
      p_221237_0_.put(VillagerProfession.BUTCHER, toIntMap(ImmutableMap.of(1, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.CHICKEN, 14, 16, 2), new VillagerTrades.EmeraldForItemsTrade(Items.PORKCHOP, 7, 16, 2), new VillagerTrades.EmeraldForItemsTrade(Items.RABBIT, 4, 16, 2), new VillagerTrades.ItemsForEmeraldsTrade(Items.RABBIT_STEW, 1, 1, 1)}, 2, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.COAL, 15, 16, 2), new VillagerTrades.ItemsForEmeraldsTrade(Items.COOKED_PORKCHOP, 1, 5, 16, 5), new VillagerTrades.ItemsForEmeraldsTrade(Items.COOKED_CHICKEN, 1, 8, 16, 5)}, 3, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.MUTTON, 7, 16, 20), new VillagerTrades.EmeraldForItemsTrade(Items.BEEF, 10, 16, 20)}, 4, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.DRIED_KELP_BLOCK, 10, 12, 30)}, 5, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.SWEET_BERRIES, 10, 12, 30)})));
      p_221237_0_.put(VillagerProfession.LEATHERWORKER, toIntMap(ImmutableMap.of(1, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.LEATHER, 6, 16, 2), new VillagerTrades.DyedArmorForEmeraldsTrade(Items.LEATHER_LEGGINGS, 3), new VillagerTrades.DyedArmorForEmeraldsTrade(Items.LEATHER_CHESTPLATE, 7)}, 2, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.FLINT, 26, 12, 10), new VillagerTrades.DyedArmorForEmeraldsTrade(Items.LEATHER_HELMET, 5, 12, 5), new VillagerTrades.DyedArmorForEmeraldsTrade(Items.LEATHER_BOOTS, 4, 12, 5)}, 3, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.RABBIT_HIDE, 9, 12, 20), new VillagerTrades.DyedArmorForEmeraldsTrade(Items.LEATHER_CHESTPLATE, 7)}, 4, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.SCUTE, 4, 12, 30), new VillagerTrades.DyedArmorForEmeraldsTrade(Items.LEATHER_HORSE_ARMOR, 6, 12, 15)}, 5, new VillagerTrades.ITrade[]{new VillagerTrades.ItemsForEmeraldsTrade(new ItemStack(Items.SADDLE), 6, 1, 12, 30, 0.2F), new VillagerTrades.DyedArmorForEmeraldsTrade(Items.LEATHER_HELMET, 5, 12, 30)})));
      p_221237_0_.put(VillagerProfession.MASON, toIntMap(ImmutableMap.of(1, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.CLAY_BALL, 10, 16, 2), new VillagerTrades.ItemsForEmeraldsTrade(Items.BRICK, 1, 10, 16, 1)}, 2, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Blocks.STONE, 20, 16, 10), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.CHISELED_STONE_BRICKS, 1, 4, 16, 5)}, 3, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Blocks.GRANITE, 16, 16, 20), new VillagerTrades.EmeraldForItemsTrade(Blocks.ANDESITE, 16, 16, 20), new VillagerTrades.EmeraldForItemsTrade(Blocks.DIORITE, 16, 16, 20), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.POLISHED_ANDESITE, 1, 4, 16, 10), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.POLISHED_DIORITE, 1, 4, 16, 10), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.POLISHED_GRANITE, 1, 4, 16, 10)}, 4, new VillagerTrades.ITrade[]{new VillagerTrades.EmeraldForItemsTrade(Items.QUARTZ, 12, 12, 30), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.ORANGE_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.WHITE_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.BLUE_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.LIGHT_BLUE_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.GRAY_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.LIGHT_GRAY_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.BLACK_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.RED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.PINK_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.MAGENTA_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.LIME_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.GREEN_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.CYAN_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.PURPLE_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.YELLOW_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.BROWN_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.ORANGE_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.WHITE_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.BLUE_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.GRAY_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.BLACK_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.RED_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.PINK_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.MAGENTA_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.LIME_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.GREEN_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.CYAN_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.PURPLE_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.YELLOW_GLAZED_TERRACOTTA, 1, 1, 12, 15), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.BROWN_GLAZED_TERRACOTTA, 1, 1, 12, 15)}, 5, new VillagerTrades.ITrade[]{new VillagerTrades.ItemsForEmeraldsTrade(Blocks.QUARTZ_PILLAR, 1, 1, 12, 30), new VillagerTrades.ItemsForEmeraldsTrade(Blocks.QUARTZ_BLOCK, 1, 1, 12, 30)})));
   });
   public static final Int2ObjectMap<VillagerTrades.ITrade[]> WANDERING_TRADER_TRADES = toIntMap(ImmutableMap.of(1, new VillagerTrades.ITrade[]{new VillagerTrades.ItemsForEmeraldsTrade(Items.SEA_PICKLE, 2, 1, 5, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.SLIME_BALL, 4, 1, 5, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.GLOWSTONE, 2, 1, 5, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.NAUTILUS_SHELL, 5, 1, 5, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.FERN, 1, 1, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.SUGAR_CANE, 1, 1, 8, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.PUMPKIN, 1, 1, 4, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.KELP, 3, 1, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.CACTUS, 3, 1, 8, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.DANDELION, 1, 1, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.POPPY, 1, 1, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.BLUE_ORCHID, 1, 1, 8, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.ALLIUM, 1, 1, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.AZURE_BLUET, 1, 1, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.RED_TULIP, 1, 1, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.ORANGE_TULIP, 1, 1, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.WHITE_TULIP, 1, 1, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.PINK_TULIP, 1, 1, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.OXEYE_DAISY, 1, 1, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.CORNFLOWER, 1, 1, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.LILY_OF_THE_VALLEY, 1, 1, 7, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.WHEAT_SEEDS, 1, 1, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.BEETROOT_SEEDS, 1, 1, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.PUMPKIN_SEEDS, 1, 1, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.MELON_SEEDS, 1, 1, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.ACACIA_SAPLING, 5, 1, 8, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.BIRCH_SAPLING, 5, 1, 8, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.DARK_OAK_SAPLING, 5, 1, 8, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.JUNGLE_SAPLING, 5, 1, 8, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.OAK_SAPLING, 5, 1, 8, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.SPRUCE_SAPLING, 5, 1, 8, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.RED_DYE, 1, 3, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.WHITE_DYE, 1, 3, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.BLUE_DYE, 1, 3, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.PINK_DYE, 1, 3, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.BLACK_DYE, 1, 3, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.GREEN_DYE, 1, 3, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.LIGHT_GRAY_DYE, 1, 3, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.MAGENTA_DYE, 1, 3, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.YELLOW_DYE, 1, 3, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.GRAY_DYE, 1, 3, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.PURPLE_DYE, 1, 3, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.LIGHT_BLUE_DYE, 1, 3, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.LIME_DYE, 1, 3, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.ORANGE_DYE, 1, 3, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.BROWN_DYE, 1, 3, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.CYAN_DYE, 1, 3, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.BRAIN_CORAL_BLOCK, 3, 1, 8, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.BUBBLE_CORAL_BLOCK, 3, 1, 8, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.FIRE_CORAL_BLOCK, 3, 1, 8, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.HORN_CORAL_BLOCK, 3, 1, 8, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.TUBE_CORAL_BLOCK, 3, 1, 8, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.VINE, 1, 1, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.BROWN_MUSHROOM, 1, 1, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.RED_MUSHROOM, 1, 1, 12, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.LILY_PAD, 1, 2, 5, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.SAND, 1, 8, 8, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.RED_SAND, 1, 4, 6, 1)}, 2, new VillagerTrades.ITrade[]{new VillagerTrades.ItemsForEmeraldsTrade(Items.TROPICAL_FISH_BUCKET, 5, 1, 4, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.PUFFERFISH_BUCKET, 5, 1, 4, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.PACKED_ICE, 3, 1, 6, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.BLUE_ICE, 6, 1, 6, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.GUNPOWDER, 1, 1, 8, 1), new VillagerTrades.ItemsForEmeraldsTrade(Items.PODZOL, 3, 3, 6, 1)}));

   private static Int2ObjectMap<VillagerTrades.ITrade[]> toIntMap(ImmutableMap<Integer, VillagerTrades.ITrade[]> pMap) {
      return new Int2ObjectOpenHashMap<>(pMap);
   }

   static class DyedArmorForEmeraldsTrade implements VillagerTrades.ITrade {
      private final Item item;
      private final int value;
      private final int maxUses;
      private final int villagerXp;

      public DyedArmorForEmeraldsTrade(Item pItem, int pValue) {
         this(pItem, pValue, 12, 1);
      }

      public DyedArmorForEmeraldsTrade(Item pItem, int pValue, int pMaxUses, int pVillagerXp) {
         this.item = pItem;
         this.value = pValue;
         this.maxUses = pMaxUses;
         this.villagerXp = pVillagerXp;
      }

      public MerchantOffer getOffer(Entity pTrader, Random pRand) {
         ItemStack itemstack = new ItemStack(Items.EMERALD, this.value);
         ItemStack itemstack1 = new ItemStack(this.item);
         if (this.item instanceof DyeableArmorItem) {
            List<DyeItem> list = Lists.newArrayList();
            list.add(getRandomDye(pRand));
            if (pRand.nextFloat() > 0.7F) {
               list.add(getRandomDye(pRand));
            }

            if (pRand.nextFloat() > 0.8F) {
               list.add(getRandomDye(pRand));
            }

            itemstack1 = IDyeableArmorItem.dyeArmor(itemstack1, list);
         }

         return new MerchantOffer(itemstack, itemstack1, this.maxUses, this.villagerXp, 0.2F);
      }

      private static DyeItem getRandomDye(Random pRandom) {
         return DyeItem.byColor(DyeColor.byId(pRandom.nextInt(16)));
      }
   }

   static class EmeraldForItemsTrade implements VillagerTrades.ITrade {
      private final Item item;
      private final int cost;
      private final int maxUses;
      private final int villagerXp;
      private final float priceMultiplier;

      public EmeraldForItemsTrade(IItemProvider pItem, int pCost, int pMaxUses, int pVillagerXp) {
         this.item = pItem.asItem();
         this.cost = pCost;
         this.maxUses = pMaxUses;
         this.villagerXp = pVillagerXp;
         this.priceMultiplier = 0.05F;
      }

      public MerchantOffer getOffer(Entity pTrader, Random pRand) {
         ItemStack itemstack = new ItemStack(this.item, this.cost);
         return new MerchantOffer(itemstack, new ItemStack(Items.EMERALD), this.maxUses, this.villagerXp, this.priceMultiplier);
      }
   }

   static class EmeraldForMapTrade implements VillagerTrades.ITrade {
      private final int emeraldCost;
      private final Structure<?> destination;
      private final MapDecoration.Type destinationType;
      private final int maxUses;
      private final int villagerXp;

      public EmeraldForMapTrade(int pEmeraldCost, Structure<?> pDestination, MapDecoration.Type pDestinationType, int pMaxUses, int pVillagerXp) {
         this.emeraldCost = pEmeraldCost;
         this.destination = pDestination;
         this.destinationType = pDestinationType;
         this.maxUses = pMaxUses;
         this.villagerXp = pVillagerXp;
      }

      @Nullable
      public MerchantOffer getOffer(Entity pTrader, Random pRand) {
         if (!(pTrader.level instanceof ServerWorld)) {
            return null;
         } else {
            ServerWorld serverworld = (ServerWorld)pTrader.level;
            BlockPos blockpos = serverworld.findNearestMapFeature(this.destination, pTrader.blockPosition(), 100, true);
            if (blockpos != null) {
               ItemStack itemstack = FilledMapItem.create(serverworld, blockpos.getX(), blockpos.getZ(), (byte)2, true, true);
               FilledMapItem.renderBiomePreviewMap(serverworld, itemstack);
               MapData.addTargetDecoration(itemstack, blockpos, "+", this.destinationType);
               itemstack.setHoverName(new TranslationTextComponent("filled_map." + this.destination.getFeatureName().toLowerCase(Locale.ROOT)));
               return new MerchantOffer(new ItemStack(Items.EMERALD, this.emeraldCost), new ItemStack(Items.COMPASS), itemstack, this.maxUses, this.villagerXp, 0.2F);
            } else {
               return null;
            }
         }
      }
   }

   static class EmeraldForVillageTypeItemTrade implements VillagerTrades.ITrade {
      private final Map<VillagerType, Item> trades;
      private final int cost;
      private final int maxUses;
      private final int villagerXp;

      public EmeraldForVillageTypeItemTrade(int pCost, int pMaxUses, int pVillagerXp, Map<VillagerType, Item> pTrades) {
         Registry.VILLAGER_TYPE.stream().filter((p_221188_1_) -> {
            return !pTrades.containsKey(p_221188_1_);
         }).findAny().ifPresent((p_221189_0_) -> {
            throw new IllegalStateException("Missing trade for villager type: " + Registry.VILLAGER_TYPE.getKey(p_221189_0_));
         });
         this.trades = pTrades;
         this.cost = pCost;
         this.maxUses = pMaxUses;
         this.villagerXp = pVillagerXp;
      }

      @Nullable
      public MerchantOffer getOffer(Entity pTrader, Random pRand) {
         if (pTrader instanceof IVillagerDataHolder) {
            ItemStack itemstack = new ItemStack(this.trades.get(((IVillagerDataHolder)pTrader).getVillagerData().getType()), this.cost);
            return new MerchantOffer(itemstack, new ItemStack(Items.EMERALD), this.maxUses, this.villagerXp, 0.05F);
         } else {
            return null;
         }
      }
   }

   static class EnchantedBookForEmeraldsTrade implements VillagerTrades.ITrade {
      private final int villagerXp;

      public EnchantedBookForEmeraldsTrade(int pVillagerXp) {
         this.villagerXp = pVillagerXp;
      }

      public MerchantOffer getOffer(Entity pTrader, Random pRand) {
         List<Enchantment> list = Registry.ENCHANTMENT.stream().filter(Enchantment::isTradeable).collect(Collectors.toList());
         Enchantment enchantment = list.get(pRand.nextInt(list.size()));
         int i = MathHelper.nextInt(pRand, enchantment.getMinLevel(), enchantment.getMaxLevel());
         ItemStack itemstack = EnchantedBookItem.createForEnchantment(new EnchantmentData(enchantment, i));
         int j = 2 + pRand.nextInt(5 + i * 10) + 3 * i;
         if (enchantment.isTreasureOnly()) {
            j *= 2;
         }

         if (j > 64) {
            j = 64;
         }

         return new MerchantOffer(new ItemStack(Items.EMERALD, j), new ItemStack(Items.BOOK), itemstack, 12, this.villagerXp, 0.2F);
      }
   }

   static class EnchantedItemForEmeraldsTrade implements VillagerTrades.ITrade {
      private final ItemStack itemStack;
      private final int baseEmeraldCost;
      private final int maxUses;
      private final int villagerXp;
      private final float priceMultiplier;

      public EnchantedItemForEmeraldsTrade(Item pItem, int pBaseEmeraldCost, int pMaxUses, int pVillagerXp) {
         this(pItem, pBaseEmeraldCost, pMaxUses, pVillagerXp, 0.05F);
      }

      public EnchantedItemForEmeraldsTrade(Item pItem, int pBaseEmeraldCost, int pMaxUses, int pVillagerXp, float pPriceMultiplier) {
         this.itemStack = new ItemStack(pItem);
         this.baseEmeraldCost = pBaseEmeraldCost;
         this.maxUses = pMaxUses;
         this.villagerXp = pVillagerXp;
         this.priceMultiplier = pPriceMultiplier;
      }

      public MerchantOffer getOffer(Entity pTrader, Random pRand) {
         int i = 5 + pRand.nextInt(15);
         ItemStack itemstack = EnchantmentHelper.enchantItem(pRand, new ItemStack(this.itemStack.getItem()), i, false);
         int j = Math.min(this.baseEmeraldCost + i, 64);
         ItemStack itemstack1 = new ItemStack(Items.EMERALD, j);
         return new MerchantOffer(itemstack1, itemstack, this.maxUses, this.villagerXp, this.priceMultiplier);
      }
   }

   public interface ITrade {
      @Nullable
      MerchantOffer getOffer(Entity pTrader, Random pRand);
   }

   static class ItemWithPotionForEmeraldsAndItemsTrade implements VillagerTrades.ITrade {
      /** An ItemStack that can have potion effects written to it. */
      private final ItemStack toItem;
      private final int toCount;
      private final int emeraldCost;
      private final int maxUses;
      private final int villagerXp;
      private final Item fromItem;
      private final int fromCount;
      private final float priceMultiplier;

      public ItemWithPotionForEmeraldsAndItemsTrade(Item pFromItem, int pFromCount, Item pToItem, int pToCount, int pEmeraldCost, int pMaxUses, int pVillagerXp) {
         this.toItem = new ItemStack(pToItem);
         this.emeraldCost = pEmeraldCost;
         this.maxUses = pMaxUses;
         this.villagerXp = pVillagerXp;
         this.fromItem = pFromItem;
         this.fromCount = pFromCount;
         this.toCount = pToCount;
         this.priceMultiplier = 0.05F;
      }

      public MerchantOffer getOffer(Entity pTrader, Random pRand) {
         ItemStack itemstack = new ItemStack(Items.EMERALD, this.emeraldCost);
         List<Potion> list = Registry.POTION.stream().filter((p_221218_0_) -> {
            return !p_221218_0_.getEffects().isEmpty() && PotionBrewing.isBrewablePotion(p_221218_0_);
         }).collect(Collectors.toList());
         Potion potion = list.get(pRand.nextInt(list.size()));
         ItemStack itemstack1 = PotionUtils.setPotion(new ItemStack(this.toItem.getItem(), this.toCount), potion);
         return new MerchantOffer(itemstack, new ItemStack(this.fromItem, this.fromCount), itemstack1, this.maxUses, this.villagerXp, this.priceMultiplier);
      }
   }

   static class ItemsForEmeraldsAndItemsTrade implements VillagerTrades.ITrade {
      private final ItemStack fromItem;
      private final int fromCount;
      private final int emeraldCost;
      private final ItemStack toItem;
      private final int toCount;
      private final int maxUses;
      private final int villagerXp;
      private final float priceMultiplier;

      public ItemsForEmeraldsAndItemsTrade(IItemProvider pFromItem, int pFromCount, Item pToItem, int pToCount, int pMaxUses, int pVillagerXp) {
         this(pFromItem, pFromCount, 1, pToItem, pToCount, pMaxUses, pVillagerXp);
      }

      public ItemsForEmeraldsAndItemsTrade(IItemProvider pFromItem, int pFromCount, int pEmeraldCost, Item pToItem, int pToCount, int pMaxUses, int pVillagerXp) {
         this.fromItem = new ItemStack(pFromItem);
         this.fromCount = pFromCount;
         this.emeraldCost = pEmeraldCost;
         this.toItem = new ItemStack(pToItem);
         this.toCount = pToCount;
         this.maxUses = pMaxUses;
         this.villagerXp = pVillagerXp;
         this.priceMultiplier = 0.05F;
      }

      @Nullable
      public MerchantOffer getOffer(Entity pTrader, Random pRand) {
         return new MerchantOffer(new ItemStack(Items.EMERALD, this.emeraldCost), new ItemStack(this.fromItem.getItem(), this.fromCount), new ItemStack(this.toItem.getItem(), this.toCount), this.maxUses, this.villagerXp, this.priceMultiplier);
      }
   }

   static class ItemsForEmeraldsTrade implements VillagerTrades.ITrade {
      private final ItemStack itemStack;
      private final int emeraldCost;
      private final int numberOfItems;
      private final int maxUses;
      private final int villagerXp;
      private final float priceMultiplier;

      public ItemsForEmeraldsTrade(Block pBlock, int pEmeraldCost, int pNumberOfItems, int pMaxUses, int pVillagerXp) {
         this(new ItemStack(pBlock), pEmeraldCost, pNumberOfItems, pMaxUses, pVillagerXp);
      }

      public ItemsForEmeraldsTrade(Item pItem, int pEmeraldCost, int pNumberOfItems, int pVillagerXp) {
         this(new ItemStack(pItem), pEmeraldCost, pNumberOfItems, 12, pVillagerXp);
      }

      public ItemsForEmeraldsTrade(Item pItem, int pEmeraldCost, int pNumberOfItems, int pMaxUses, int pVillagerXp) {
         this(new ItemStack(pItem), pEmeraldCost, pNumberOfItems, pMaxUses, pVillagerXp);
      }

      public ItemsForEmeraldsTrade(ItemStack pItemStack, int pEmeraldCost, int pNumberOfItems, int pMaxUses, int pVillagerXp) {
         this(pItemStack, pEmeraldCost, pNumberOfItems, pMaxUses, pVillagerXp, 0.05F);
      }

      public ItemsForEmeraldsTrade(ItemStack pItemStack, int pEmeraldCost, int pNumberOfItems, int pMaxUses, int pVillagerXp, float pPriceMultiplier) {
         this.itemStack = pItemStack;
         this.emeraldCost = pEmeraldCost;
         this.numberOfItems = pNumberOfItems;
         this.maxUses = pMaxUses;
         this.villagerXp = pVillagerXp;
         this.priceMultiplier = pPriceMultiplier;
      }

      public MerchantOffer getOffer(Entity pTrader, Random pRand) {
         return new MerchantOffer(new ItemStack(Items.EMERALD, this.emeraldCost), new ItemStack(this.itemStack.getItem(), this.numberOfItems), this.maxUses, this.villagerXp, this.priceMultiplier);
      }
   }

   static class SuspiciousStewForEmeraldTrade implements VillagerTrades.ITrade {
      final Effect effect;
      final int duration;
      final int xp;
      private final float priceMultiplier;

      public SuspiciousStewForEmeraldTrade(Effect pEffect, int pDuration, int pXp) {
         this.effect = pEffect;
         this.duration = pDuration;
         this.xp = pXp;
         this.priceMultiplier = 0.05F;
      }

      @Nullable
      public MerchantOffer getOffer(Entity pTrader, Random pRand) {
         ItemStack itemstack = new ItemStack(Items.SUSPICIOUS_STEW, 1);
         SuspiciousStewItem.saveMobEffect(itemstack, this.effect, this.duration);
         return new MerchantOffer(new ItemStack(Items.EMERALD, 1), itemstack, 12, this.xp, this.priceMultiplier);
      }
   }
}