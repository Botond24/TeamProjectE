package net.minecraft.item;

import javax.annotation.Nullable;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.NonNullList;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class ItemGroup {
   public static ItemGroup[] TABS = new ItemGroup[12];
   public static final ItemGroup TAB_BUILDING_BLOCKS = (new ItemGroup(0, "buildingBlocks") {
      @OnlyIn(Dist.CLIENT)
      public ItemStack makeIcon() {
         return new ItemStack(Blocks.BRICKS);
      }
   }).setRecipeFolderName("building_blocks");
   public static final ItemGroup TAB_DECORATIONS = new ItemGroup(1, "decorations") {
      @OnlyIn(Dist.CLIENT)
      public ItemStack makeIcon() {
         return new ItemStack(Blocks.PEONY);
      }
   };
   public static final ItemGroup TAB_REDSTONE = new ItemGroup(2, "redstone") {
      @OnlyIn(Dist.CLIENT)
      public ItemStack makeIcon() {
         return new ItemStack(Items.REDSTONE);
      }
   };
   public static final ItemGroup TAB_TRANSPORTATION = new ItemGroup(3, "transportation") {
      @OnlyIn(Dist.CLIENT)
      public ItemStack makeIcon() {
         return new ItemStack(Blocks.POWERED_RAIL);
      }
   };
   public static final ItemGroup TAB_MISC = new ItemGroup(6, "misc") {
      @OnlyIn(Dist.CLIENT)
      public ItemStack makeIcon() {
         return new ItemStack(Items.LAVA_BUCKET);
      }
   };
   public static final ItemGroup TAB_SEARCH = (new ItemGroup(5, "search") {
      @OnlyIn(Dist.CLIENT)
      public ItemStack makeIcon() {
         return new ItemStack(Items.COMPASS);
      }
   }).setBackgroundSuffix("item_search.png");
   public static final ItemGroup TAB_FOOD = new ItemGroup(7, "food") {
      @OnlyIn(Dist.CLIENT)
      public ItemStack makeIcon() {
         return new ItemStack(Items.APPLE);
      }
   };
   public static final ItemGroup TAB_TOOLS = (new ItemGroup(8, "tools") {
      @OnlyIn(Dist.CLIENT)
      public ItemStack makeIcon() {
         return new ItemStack(Items.IRON_AXE);
      }
   }).setEnchantmentCategories(new EnchantmentType[]{EnchantmentType.VANISHABLE, EnchantmentType.DIGGER, EnchantmentType.FISHING_ROD, EnchantmentType.BREAKABLE});
   public static final ItemGroup TAB_COMBAT = (new ItemGroup(9, "combat") {
      @OnlyIn(Dist.CLIENT)
      public ItemStack makeIcon() {
         return new ItemStack(Items.GOLDEN_SWORD);
      }
   }).setEnchantmentCategories(new EnchantmentType[]{EnchantmentType.VANISHABLE, EnchantmentType.ARMOR, EnchantmentType.ARMOR_FEET, EnchantmentType.ARMOR_HEAD, EnchantmentType.ARMOR_LEGS, EnchantmentType.ARMOR_CHEST, EnchantmentType.BOW, EnchantmentType.WEAPON, EnchantmentType.WEARABLE, EnchantmentType.BREAKABLE, EnchantmentType.TRIDENT, EnchantmentType.CROSSBOW});
   public static final ItemGroup TAB_BREWING = new ItemGroup(10, "brewing") {
      @OnlyIn(Dist.CLIENT)
      public ItemStack makeIcon() {
         return PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
      }
   };
   public static final ItemGroup TAB_MATERIALS = TAB_MISC;
   public static final ItemGroup TAB_HOTBAR = new ItemGroup(4, "hotbar") {
      @OnlyIn(Dist.CLIENT)
      public ItemStack makeIcon() {
         return new ItemStack(Blocks.BOOKSHELF);
      }

      /**
       * Fills {@code items} with all items that are in this group.
       */
      @OnlyIn(Dist.CLIENT)
      public void fillItemList(NonNullList<ItemStack> pItems) {
         throw new RuntimeException("Implement exception client-side.");
      }

      @OnlyIn(Dist.CLIENT)
      public boolean isAlignedRight() {
         return true;
      }
   };
   public static final ItemGroup TAB_INVENTORY = (new ItemGroup(11, "inventory") {
      @OnlyIn(Dist.CLIENT)
      public ItemStack makeIcon() {
         return new ItemStack(Blocks.CHEST);
      }
   }).setBackgroundSuffix("inventory.png").hideScroll().hideTitle();
   private final int id;
   private final String langId;
   private final ITextComponent displayName;
   private String recipeFolderName;
   @Deprecated
   private String backgroundSuffix = "items.png";
   private net.minecraft.util.ResourceLocation backgroundLocation;
   private boolean canScroll = true;
   private boolean showTitle = true;
   private EnchantmentType[] enchantmentCategories = new EnchantmentType[0];
   private ItemStack iconItemStack;

   public ItemGroup(String label) {
       this(-1, label);
   }

   public ItemGroup(int pId, String pLangId) {
      this.langId = pLangId;
      this.displayName = new TranslationTextComponent("itemGroup." + pLangId);
      this.iconItemStack = ItemStack.EMPTY;
      this.id = addGroupSafe(pId, this);
   }

   @OnlyIn(Dist.CLIENT)
   public int getId() {
      return this.id;
   }

   /**
    * Gets the name that's valid for use in a ResourceLocation's path. This should be set if the tabLabel contains
    * illegal characters.
    */
   public String getRecipeFolderName() {
      return this.recipeFolderName == null ? this.langId : this.recipeFolderName;
   }

   @OnlyIn(Dist.CLIENT)
   public ITextComponent getDisplayName() {
      return this.displayName;
   }

   @OnlyIn(Dist.CLIENT)
   public ItemStack getIconItem() {
      if (this.iconItemStack.isEmpty()) {
         this.iconItemStack = this.makeIcon();
      }

      return this.iconItemStack;
   }

   @OnlyIn(Dist.CLIENT)
   public abstract ItemStack makeIcon();

   /**
    * @deprecated Forge use {@link #getBackgroundImage()} instead
    */
   @OnlyIn(Dist.CLIENT)
   @Deprecated
   public String getBackgroundSuffix() {
      return this.backgroundSuffix;
   }

   /**
    * @deprecated Forge: use {@link #setBackgroundImage(net.minecraft.util.ResourceLocation)} instead
    */
   @Deprecated
   public ItemGroup setBackgroundSuffix(String pBackgroundSuffix) {
      this.backgroundSuffix = pBackgroundSuffix;
      return this;
   }

   public ItemGroup setBackgroundImage(net.minecraft.util.ResourceLocation texture) {
      this.backgroundLocation = texture;
      return this;
   }

   public ItemGroup setRecipeFolderName(String pRecipeFolderName) {
      this.recipeFolderName = pRecipeFolderName;
      return this;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean showTitle() {
      return this.showTitle;
   }

   public ItemGroup hideTitle() {
      this.showTitle = false;
      return this;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean canScroll() {
      return this.canScroll;
   }

   public ItemGroup hideScroll() {
      this.canScroll = false;
      return this;
   }

   /**
    * returns index % 6
    */
   @OnlyIn(Dist.CLIENT)
   public int getColumn() {
      if (id > 11) return ((id - 12) % 10) % 5;
      return this.id % 6;
   }

   /**
    * returns tabIndex < 6
    */
   @OnlyIn(Dist.CLIENT)
   public boolean isTopRow() {
      if (id > 11) return ((id - 12) % 10) < 5;
      return this.id < 6;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isAlignedRight() {
      return this.getColumn() == 5;
   }

   /**
    * Returns the enchantment types relevant to this tab
    */
   public EnchantmentType[] getEnchantmentCategories() {
      return this.enchantmentCategories;
   }

   /**
    * Sets the enchantment types for populating this tab with enchanting books
    */
   public ItemGroup setEnchantmentCategories(EnchantmentType... pEnchantmentCategories) {
      this.enchantmentCategories = pEnchantmentCategories;
      return this;
   }

   public boolean hasEnchantmentCategory(@Nullable EnchantmentType pCategory) {
      if (pCategory != null) {
         for(EnchantmentType enchantmenttype : this.enchantmentCategories) {
            if (enchantmenttype == pCategory) {
               return true;
            }
         }
      }

      return false;
   }

   /**
    * Fills {@code items} with all items that are in this group.
    */
   @OnlyIn(Dist.CLIENT)
   public void fillItemList(NonNullList<ItemStack> pItems) {
      for(Item item : Registry.ITEM) {
         item.fillItemCategory(this, pItems);
      }

   }

   public int getTabPage() {
      return id < 12 ? 0 : ((id - 12) / 10) + 1;
   }

   public boolean hasSearchBar() {
      return id == TAB_SEARCH.id;
   }

   /**
    * Gets the width of the search bar of the creative tab, use this if your
    * creative tab name overflows together with a custom texture.
    *
    * @return The width of the search bar, 89 by default
    */
   public int getSearchbarWidth() {
      return 89;
   }

   @OnlyIn(Dist.CLIENT)
   public net.minecraft.util.ResourceLocation getBackgroundImage() {
      if (backgroundLocation != null) return backgroundLocation; //FORGE: allow custom namespace
      return new net.minecraft.util.ResourceLocation("textures/gui/container/creative_inventory/tab_" + this.getBackgroundSuffix());
   }

   private static final net.minecraft.util.ResourceLocation CREATIVE_INVENTORY_TABS = new net.minecraft.util.ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
   @OnlyIn(Dist.CLIENT)
   public net.minecraft.util.ResourceLocation getTabsImage() {
      return CREATIVE_INVENTORY_TABS;
   }

   public int getLabelColor() {
      return 4210752;
   }

   public int getSlotColor() {
      return -2130706433;
   }

   public static synchronized int getGroupCountSafe() {
      return ItemGroup.TABS.length;
   }

   private static synchronized int addGroupSafe(int index, ItemGroup newGroup) {
      if(index == -1) {
         index = TABS.length;
      }
      if (index >= TABS.length) {
         ItemGroup[] tmp = new ItemGroup[index + 1];
         System.arraycopy(TABS, 0, tmp, 0, TABS.length);
         TABS = tmp;
      }
      TABS[index] = newGroup;
      return index;
   }
}
