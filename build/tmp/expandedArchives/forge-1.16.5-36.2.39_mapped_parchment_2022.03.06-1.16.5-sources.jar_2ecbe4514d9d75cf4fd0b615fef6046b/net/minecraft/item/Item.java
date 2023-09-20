package net.minecraft.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ITag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Item extends net.minecraftforge.registries.ForgeRegistryEntry<Item> implements IItemProvider, net.minecraftforge.common.extensions.IForgeItem {
   public static final Map<Block, Item> BY_BLOCK = net.minecraftforge.registries.GameData.getBlockItemMap();
   protected static final UUID BASE_ATTACK_DAMAGE_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
   protected static final UUID BASE_ATTACK_SPEED_UUID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
   protected static final Random random = new Random();
   protected final ItemGroup category;
   private final Rarity rarity;
   private final int maxStackSize;
   private final int maxDamage;
   private final boolean isFireResistant;
   private final Item craftingRemainingItem;
   @Nullable
   private String descriptionId;
   @Nullable
   private final Food foodProperties;

   public static int getId(Item pItem) {
      return pItem == null ? 0 : Registry.ITEM.getId(pItem);
   }

   public static Item byId(int pId) {
      return Registry.ITEM.byId(pId);
   }

   @Deprecated
   public static Item byBlock(Block pBlock) {
      return BY_BLOCK.getOrDefault(pBlock, Items.AIR);
   }

   public Item(Item.Properties pProperties) {
      this.category = pProperties.category;
      this.rarity = pProperties.rarity;
      this.craftingRemainingItem = pProperties.craftingRemainingItem;
      this.maxDamage = pProperties.maxDamage;
      this.maxStackSize = pProperties.maxStackSize;
      this.foodProperties = pProperties.foodProperties;
      this.isFireResistant = pProperties.isFireResistant;
      this.canRepair = pProperties.canRepair;
      this.toolClasses.putAll(pProperties.toolClasses);
      Object tmp = pProperties.ister == null ? null : net.minecraftforge.fml.DistExecutor.callWhenOn(Dist.CLIENT, pProperties.ister);
      this.ister = tmp == null ? null : () -> (net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer) tmp;
   }

   /**
    * Called as the item is being used by an entity.
    */
   public void onUseTick(World pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pCount) {
   }

   public boolean verifyTagAfterLoad(CompoundNBT p_179215_1_) {
      return false;
   }

   public boolean canAttackBlock(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer) {
      return true;
   }

   public Item asItem() {
      return this;
   }

   /**
    * Called when this item is used when targetting a Block
    */
   public ActionResultType useOn(ItemUseContext pContext) {
      return ActionResultType.PASS;
   }

   public float getDestroySpeed(ItemStack pStack, BlockState pState) {
      return 1.0F;
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pHand) {
      if (this.isEdible()) {
         ItemStack itemstack = pPlayer.getItemInHand(pHand);
         if (pPlayer.canEat(this.getFoodProperties().canAlwaysEat())) {
            pPlayer.startUsingItem(pHand);
            return ActionResult.consume(itemstack);
         } else {
            return ActionResult.fail(itemstack);
         }
      } else {
         return ActionResult.pass(pPlayer.getItemInHand(pHand));
      }
   }

   /**
    * Called when the player finishes using this Item (E.g. finishes eating.). Not called when the player stops using
    * the Item before the action is complete.
    */
   public ItemStack finishUsingItem(ItemStack pStack, World pLevel, LivingEntity pEntityLiving) {
      return this.isEdible() ? pEntityLiving.eat(pLevel, pStack) : pStack;
   }

   /**
    * Returns the maximum size of the stack for a specific item.
    */
   @Deprecated // Use ItemStack sensitive version.
   public final int getMaxStackSize() {
      return this.maxStackSize;
   }

   /**
    * Returns the maximum damage an item can take.
    */
   @Deprecated // Use ItemStack sensitive version.
   public final int getMaxDamage() {
      return this.maxDamage;
   }

   public boolean canBeDepleted() {
      return this.maxDamage > 0;
   }

   /**
    * Current implementations of this method in child classes do not use the entry argument beside ev. They just raise
    * the damage on the stack.
    */
   public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
      return false;
   }

   /**
    * Called when a Block is destroyed using this Item. Return true to trigger the "Use Item" statistic.
    */
   public boolean mineBlock(ItemStack pStack, World pLevel, BlockState pState, BlockPos pPos, LivingEntity pEntityLiving) {
      return false;
   }

   /**
    * Check whether this Item can harvest the given Block
    */
   public boolean isCorrectToolForDrops(BlockState pBlock) {
      return false;
   }

   /**
    * Returns true if the item can be used on the given entity, e.g. shears on sheep.
    */
   public ActionResultType interactLivingEntity(ItemStack pStack, PlayerEntity pPlayer, LivingEntity pTarget, Hand pHand) {
      return ActionResultType.PASS;
   }

   @OnlyIn(Dist.CLIENT)
   public ITextComponent getDescription() {
      return new TranslationTextComponent(this.getDescriptionId());
   }

   public String toString() {
      return Registry.ITEM.getKey(this).getPath();
   }

   protected String getOrCreateDescriptionId() {
      if (this.descriptionId == null) {
         this.descriptionId = Util.makeDescriptionId("item", Registry.ITEM.getKey(this));
      }

      return this.descriptionId;
   }

   /**
    * Returns the unlocalized name of this item.
    */
   public String getDescriptionId() {
      return this.getOrCreateDescriptionId();
   }

   /**
    * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
    * different names based on their damage or NBT.
    */
   public String getDescriptionId(ItemStack pStack) {
      return this.getDescriptionId();
   }

   /**
    * If this function returns true (or the item is damageable), the ItemStack's NBT tag will be sent to the client.
    */
   public boolean shouldOverrideMultiplayerNbt() {
      return true;
   }

   @Nullable
   @Deprecated // Use ItemStack sensitive version.
   public final Item getCraftingRemainingItem() {
      return this.craftingRemainingItem;
   }

   /**
    * True if this Item has a container item (a.k.a. crafting result)
    */
   @Deprecated // Use ItemStack sensitive version.
   public boolean hasCraftingRemainingItem() {
      return this.craftingRemainingItem != null;
   }

   /**
    * Called each tick as long the item is on a player inventory. Uses by maps to check if is on a player hand and
    * update it's contents.
    */
   public void inventoryTick(ItemStack pStack, World pLevel, Entity pEntity, int pItemSlot, boolean pIsSelected) {
   }

   /**
    * Called when item is crafted/smelted. Used only by maps so far.
    */
   public void onCraftedBy(ItemStack pStack, World pLevel, PlayerEntity pPlayer) {
   }

   /**
    * Returns {@code true} if this is a complex item.
    */
   public boolean isComplex() {
      return false;
   }

   /**
    * returns the action that specifies what animation to play when the items is being used
    */
   public UseAction getUseAnimation(ItemStack pStack) {
      return pStack.getItem().isEdible() ? UseAction.EAT : UseAction.NONE;
   }

   /**
    * How long it takes to use or consume an item
    */
   public int getUseDuration(ItemStack pStack) {
      if (pStack.getItem().isEdible()) {
         return this.getFoodProperties().isFastFood() ? 16 : 32;
      } else {
         return 0;
      }
   }

   /**
    * Called when the player stops using an Item (stops holding the right mouse button).
    */
   public void releaseUsing(ItemStack pStack, World pLevel, LivingEntity pEntityLiving, int pTimeLeft) {
   }

   /**
    * allows items to add custom lines of information to the mouseover description
    */
   @OnlyIn(Dist.CLIENT)
   public void appendHoverText(ItemStack pStack, @Nullable World pLevel, List<ITextComponent> pTooltip, ITooltipFlag pFlag) {
   }

   /**
    * Gets the title name of the book
    */
   public ITextComponent getName(ItemStack pStack) {
      return new TranslationTextComponent(this.getDescriptionId(pStack));
   }

   /**
    * Returns true if this item has an enchantment glint. By default, this returns <code>stack.isItemEnchanted()</code>,
    * but other items can override it (for instance, written books always return true).
    * 
    * Note that if you override this method, you generally want to also call the super version (on {@link Item}) to get
    * the glint for enchanted items. Of course, that is unnecessary if the overwritten version always returns true.
    */
   public boolean isFoil(ItemStack pStack) {
      return pStack.isEnchanted();
   }

   /**
    * Return an item rarity from EnumRarity
    */
   public Rarity getRarity(ItemStack pStack) {
      if (!pStack.isEnchanted()) {
         return this.rarity;
      } else {
         switch(this.rarity) {
         case COMMON:
         case UNCOMMON:
            return Rarity.RARE;
         case RARE:
            return Rarity.EPIC;
         case EPIC:
         default:
            return this.rarity;
         }
      }
   }

   /**
    * Checks isDamagable and if it cannot be stacked
    */
   public boolean isEnchantable(ItemStack pStack) {
      return this.getItemStackLimit(pStack) == 1 && this.isDamageable(pStack);
   }

   protected static BlockRayTraceResult getPlayerPOVHitResult(World pLevel, PlayerEntity pPlayer, RayTraceContext.FluidMode pFluidMode) {
      float f = pPlayer.xRot;
      float f1 = pPlayer.yRot;
      Vector3d vector3d = pPlayer.getEyePosition(1.0F);
      float f2 = MathHelper.cos(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
      float f3 = MathHelper.sin(-f1 * ((float)Math.PI / 180F) - (float)Math.PI);
      float f4 = -MathHelper.cos(-f * ((float)Math.PI / 180F));
      float f5 = MathHelper.sin(-f * ((float)Math.PI / 180F));
      float f6 = f3 * f4;
      float f7 = f2 * f4;
      double d0 = pPlayer.getAttribute(net.minecraftforge.common.ForgeMod.REACH_DISTANCE.get()).getValue();;
      Vector3d vector3d1 = vector3d.add((double)f6 * d0, (double)f5 * d0, (double)f7 * d0);
      return pLevel.clip(new RayTraceContext(vector3d, vector3d1, RayTraceContext.BlockMode.OUTLINE, pFluidMode, pPlayer));
   }

   /**
    * Return the enchantability factor of the item, most of the time is based on material.
    */
   public int getEnchantmentValue() {
      return 0;
   }

   /**
    * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
    */
   public void fillItemCategory(ItemGroup pGroup, NonNullList<ItemStack> pItems) {
      if (this.allowdedIn(pGroup)) {
         pItems.add(new ItemStack(this));
      }

   }

   protected boolean allowdedIn(ItemGroup pCategory) {
      if (getCreativeTabs().stream().anyMatch(tab -> tab == pCategory)) return true;
      ItemGroup itemgroup = this.getItemCategory();
      return itemgroup != null && (pCategory == ItemGroup.TAB_SEARCH || pCategory == itemgroup);
   }

   /**
    * gets the CreativeTab this item is displayed on
    */
   @Nullable
   public final ItemGroup getItemCategory() {
      return this.category;
   }

   /**
    * Return whether this item is repairable in an anvil.
    */
   public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair) {
      return false;
   }

   /**
    * Gets a map of item attribute modifiers, used by ItemSword to increase hit damage.
    */
   @Deprecated // Use ItemStack sensitive version.
   public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlotType pEquipmentSlot) {
      return ImmutableMultimap.of();
   }

   @Nullable
   private final java.util.function.Supplier<net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer> ister;
   private final java.util.Map<net.minecraftforge.common.ToolType, Integer> toolClasses = Maps.newHashMap();
   private final net.minecraftforge.common.util.ReverseTagWrapper<Item> reverseTags = new net.minecraftforge.common.util.ReverseTagWrapper<>(this, net.minecraft.tags.ItemTags::getAllTags);
   protected final boolean canRepair;

   @Override
   public boolean isRepairable(ItemStack stack) {
      return canRepair && isDamageable(stack);
   }

   @Override
   public java.util.Set<net.minecraftforge.common.ToolType> getToolTypes(ItemStack stack) {
      return toolClasses.keySet();
   }

   @Override
   public int getHarvestLevel(ItemStack stack, net.minecraftforge.common.ToolType tool, @Nullable PlayerEntity player, @Nullable BlockState blockState) {
      return toolClasses.getOrDefault(tool, -1);
   }

   @OnlyIn(Dist.CLIENT)
   @Override
   public final net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer getItemStackTileEntityRenderer() {
      net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer renderer = ister != null ? ister.get() : null;
      return renderer != null ? renderer : net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer.instance;
   }

   @Override
   public java.util.Set<net.minecraft.util.ResourceLocation> getTags() {
      return reverseTags.getTagNames();
   }

   /**
    * If this itemstack's item is a crossbow
    */
   public boolean useOnRelease(ItemStack pStack) {
      return pStack.getItem() == Items.CROSSBOW;
   }

   public ItemStack getDefaultInstance() {
      return new ItemStack(this);
   }

   public boolean is(ITag<Item> p_206844_1_) {
      return p_206844_1_.contains(this);
   }

   public boolean isEdible() {
      return this.foodProperties != null;
   }

   @Nullable
   public Food getFoodProperties() {
      return this.foodProperties;
   }

   public SoundEvent getDrinkingSound() {
      return SoundEvents.GENERIC_DRINK;
   }

   public SoundEvent getEatingSound() {
      return SoundEvents.GENERIC_EAT;
   }

   public boolean isFireResistant() {
      return this.isFireResistant;
   }

   public boolean canBeHurtBy(DamageSource pDamageSource) {
      return !this.isFireResistant || !pDamageSource.isFire();
   }

   public static class Properties {
      private int maxStackSize = 64;
      private int maxDamage;
      private Item craftingRemainingItem;
      private ItemGroup category;
      private Rarity rarity = Rarity.COMMON;
      /** Sets food information to this item */
      private Food foodProperties;
      private boolean isFireResistant;
      private boolean canRepair = true;
      private java.util.Map<net.minecraftforge.common.ToolType, Integer> toolClasses = Maps.newHashMap();
      private java.util.function.Supplier<java.util.concurrent.Callable<net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer>> ister;

      public Item.Properties food(Food pFood) {
         this.foodProperties = pFood;
         return this;
      }

      public Item.Properties stacksTo(int pMaxStackSize) {
         if (this.maxDamage > 0) {
            throw new RuntimeException("Unable to have damage AND stack.");
         } else {
            this.maxStackSize = pMaxStackSize;
            return this;
         }
      }

      public Item.Properties defaultDurability(int pMaxDamage) {
         return this.maxDamage == 0 ? this.durability(pMaxDamage) : this;
      }

      public Item.Properties durability(int pMaxDamage) {
         this.maxDamage = pMaxDamage;
         this.maxStackSize = 1;
         return this;
      }

      public Item.Properties craftRemainder(Item pCraftingRemainingItem) {
         this.craftingRemainingItem = pCraftingRemainingItem;
         return this;
      }

      public Item.Properties tab(ItemGroup pCategory) {
         this.category = pCategory;
         return this;
      }

      public Item.Properties rarity(Rarity pRarity) {
         this.rarity = pRarity;
         return this;
      }

      public Item.Properties fireResistant() {
         this.isFireResistant = true;
         return this;
      }

      public Item.Properties setNoRepair() {
         canRepair = false;
         return this;
      }

      public Item.Properties addToolType(net.minecraftforge.common.ToolType type, int level) {
         toolClasses.put(type, level);
         return this;
      }

      public Item.Properties setISTER(java.util.function.Supplier<java.util.concurrent.Callable<net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer>> ister) {
         this.ister = ister;
         return this;
      }
   }
}
