package net.minecraft.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.command.arguments.BlockPredicateArgument;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.enchantment.UnbreakingEnchantment;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.Hand;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ItemStack extends net.minecraftforge.common.capabilities.CapabilityProvider<ItemStack> implements net.minecraftforge.common.extensions.IForgeItemStack {
   public static final Codec<ItemStack> CODEC = RecordCodecBuilder.create((p_234698_0_) -> {
      return p_234698_0_.group(Registry.ITEM.fieldOf("id").forGetter((p_234706_0_) -> {
         return p_234706_0_.item;
      }), Codec.INT.fieldOf("Count").forGetter((p_234705_0_) -> {
         return p_234705_0_.count;
      }), CompoundNBT.CODEC.optionalFieldOf("tag").forGetter((p_234704_0_) -> {
         return Optional.ofNullable(p_234704_0_.tag);
      })).apply(p_234698_0_, ItemStack::new);
   });
   private final net.minecraftforge.registries.IRegistryDelegate<Item> delegate;
   private CompoundNBT capNBT;

   private static final Logger LOGGER = LogManager.getLogger();
   public static final ItemStack EMPTY = new ItemStack((Item)null);
   public static final DecimalFormat ATTRIBUTE_MODIFIER_FORMAT = Util.make(new DecimalFormat("#.##"), (p_234699_0_) -> {
      p_234699_0_.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
   });
   private static final Style LORE_STYLE = Style.EMPTY.withColor(TextFormatting.DARK_PURPLE).withItalic(true);
   private int count;
   private int popTime;
   @Deprecated
   private final Item item;
   private CompoundNBT tag;
   private boolean emptyCacheFlag;
   /** The entity the item is attached to, like an Item Frame. */
   private Entity entityRepresentation;
   private CachedBlockInfo cachedBreakBlock;
   private boolean cachedBreakBlockResult;
   private CachedBlockInfo cachedPlaceBlock;
   private boolean cachedPlaceBlockResult;

   public ItemStack(IItemProvider pItem) {
      this(pItem, 1);
   }

   private ItemStack(IItemProvider p_i231596_1_, int p_i231596_2_, Optional<CompoundNBT> p_i231596_3_) {
      this(p_i231596_1_, p_i231596_2_);
      p_i231596_3_.ifPresent(this::setTag);
   }

   public ItemStack(IItemProvider pItem, int pCount) { this(pItem, pCount, (CompoundNBT) null); }
   public ItemStack(IItemProvider pItem, int pCount, @Nullable CompoundNBT capNBT) {
      super(ItemStack.class, true);
      this.capNBT = capNBT;
      this.item = pItem == null ? null : pItem.asItem();
      this.delegate = pItem == null ? null : pItem.asItem().delegate;
      this.count = pCount;
      this.forgeInit();
      if (this.item != null && this.item.isDamageable(this)) {
         this.setDamageValue(this.getDamageValue());
      }

      this.updateEmptyCacheFlag();
   }

   private void updateEmptyCacheFlag() {
      this.emptyCacheFlag = false;
      this.emptyCacheFlag = this.isEmpty();
   }

   private ItemStack(CompoundNBT pCompoundTag) {
      super(ItemStack.class, true);
      this.capNBT = pCompoundTag.contains("ForgeCaps") ? pCompoundTag.getCompound("ForgeCaps") : null;
      Item rawItem =
      this.item = Registry.ITEM.get(new ResourceLocation(pCompoundTag.getString("id")));
      this.delegate = rawItem.delegate;
      this.count = pCompoundTag.getByte("Count");
      if (pCompoundTag.contains("tag", 10)) {
         this.tag = pCompoundTag.getCompound("tag");
         this.getItem().verifyTagAfterLoad(pCompoundTag);
      }
      this.forgeInit();
      if (this.getItem().isDamageable(this)) {
         this.setDamageValue(this.getDamageValue());
      }

      this.updateEmptyCacheFlag();
   }

   public static ItemStack of(CompoundNBT pCompoundTag) {
      try {
         return new ItemStack(pCompoundTag);
      } catch (RuntimeException runtimeexception) {
         LOGGER.debug("Tried to load invalid item: {}", pCompoundTag, runtimeexception);
         return EMPTY;
      }
   }

   public boolean isEmpty() {
      if (this == EMPTY) {
         return true;
      } else if (this.getItem() != null && this.getItem() != Items.AIR) {
         return this.count <= 0;
      } else {
         return true;
      }
   }

   /**
    * Splits off a stack of the given amount of this stack and reduces this stack by the amount.
    */
   public ItemStack split(int pAmount) {
      int i = Math.min(pAmount, this.count);
      ItemStack itemstack = this.copy();
      itemstack.setCount(i);
      this.shrink(i);
      return itemstack;
   }

   /**
    * Returns the object corresponding to the stack.
    */
   public Item getItem() {
      return this.emptyCacheFlag || this.delegate == null ? Items.AIR : this.delegate.get();
   }

   public ActionResultType useOn(ItemUseContext pContext) {
      if (!pContext.getLevel().isClientSide) return net.minecraftforge.common.ForgeHooks.onPlaceItemIntoWorld(pContext);
      return onItemUse(pContext, (c) -> getItem().useOn(pContext));
   }

   public ActionResultType onItemUseFirst(ItemUseContext pContext) {
      return onItemUse(pContext, (c) -> getItem().onItemUseFirst(this, pContext));
   }

   private ActionResultType onItemUse(ItemUseContext pContext, java.util.function.Function<ItemUseContext, ActionResultType> callback) {
      PlayerEntity playerentity = pContext.getPlayer();
      BlockPos blockpos = pContext.getClickedPos();
      CachedBlockInfo cachedblockinfo = new CachedBlockInfo(pContext.getLevel(), blockpos, false);
      if (playerentity != null && !playerentity.abilities.mayBuild && !this.hasAdventureModePlaceTagForBlock(pContext.getLevel().getTagManager(), cachedblockinfo)) {
         return ActionResultType.PASS;
      } else {
         Item item = this.getItem();
         ActionResultType actionresulttype = callback.apply(pContext);
         if (playerentity != null && actionresulttype.consumesAction()) {
            playerentity.awardStat(Stats.ITEM_USED.get(item));
         }

         return actionresulttype;
      }
   }

   public float getDestroySpeed(BlockState pState) {
      return this.getItem().getDestroySpeed(this, pState);
   }

   /**
    * Called whenr the item stack is equipped and right clicked. Replaces the item stack with the return value.
    */
   public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pUsedHand) {
      return this.getItem().use(pLevel, pPlayer, pUsedHand);
   }

   /**
    * Called when the item in use count reach 0, e.g. item food eaten. Return the new ItemStack. Args : world, entity
    */
   public ItemStack finishUsingItem(World pLevel, LivingEntity pLivingEntity) {
      return this.getItem().finishUsingItem(this, pLevel, pLivingEntity);
   }

   /**
    * Write the stack fields to a NBT object. Return the new NBT object.
    */
   public CompoundNBT save(CompoundNBT pCompoundTag) {
      ResourceLocation resourcelocation = Registry.ITEM.getKey(this.getItem());
      pCompoundTag.putString("id", resourcelocation == null ? "minecraft:air" : resourcelocation.toString());
      pCompoundTag.putByte("Count", (byte)this.count);
      if (this.tag != null) {
         pCompoundTag.put("tag", this.tag.copy());
      }
      CompoundNBT cnbt = this.serializeCaps();
      if (cnbt != null && !cnbt.isEmpty()) {
         pCompoundTag.put("ForgeCaps", cnbt);
      }
      return pCompoundTag;
   }

   /**
    * Returns maximum size of the stack.
    */
   public int getMaxStackSize() {
      return this.getItem().getItemStackLimit(this);
   }

   /**
    * Returns true if the ItemStack can hold 2 or more units of the item.
    */
   public boolean isStackable() {
      return this.getMaxStackSize() > 1 && (!this.isDamageableItem() || !this.isDamaged());
   }

   /**
    * true if this itemStack is damageable
    */
   public boolean isDamageableItem() {
      if (!this.emptyCacheFlag && this.getItem().isDamageable(this)) {
         CompoundNBT compoundnbt = this.getTag();
         return compoundnbt == null || !compoundnbt.getBoolean("Unbreakable");
      } else {
         return false;
      }
   }

   /**
    * returns true when a damageable item is damaged
    */
   public boolean isDamaged() {
      return this.isDamageableItem() && getItem().isDamaged(this);
   }

   public int getDamageValue() {
      return this.getItem().getDamage(this);
   }

   public void setDamageValue(int pDamage) {
      this.getItem().setDamage(this, pDamage);
   }

   /**
    * Returns the max damage an item in the stack can take.
    */
   public int getMaxDamage() {
      return this.getItem().getMaxDamage(this);
   }

   /**
    * Attempts to damage the ItemStack with par1 amount of damage, If the ItemStack has the Unbreaking enchantment there
    * is a chance for each point of damage to be negated. Returns true if it takes more damage than getMaxDamage().
    * Returns false otherwise or if the ItemStack can't be damaged or if all points of damage are negated.
    */
   public boolean hurt(int pAmount, Random pRandom, @Nullable ServerPlayerEntity pUser) {
      if (!this.isDamageableItem()) {
         return false;
      } else {
         if (pAmount > 0) {
            int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, this);
            int j = 0;

            for(int k = 0; i > 0 && k < pAmount; ++k) {
               if (UnbreakingEnchantment.shouldIgnoreDurabilityDrop(this, i, pRandom)) {
                  ++j;
               }
            }

            pAmount -= j;
            if (pAmount <= 0) {
               return false;
            }
         }

         if (pUser != null && pAmount != 0) {
            CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger(pUser, this, this.getDamageValue() + pAmount);
         }

         int l = this.getDamageValue() + pAmount;
         this.setDamageValue(l);
         return l >= this.getMaxDamage();
      }
   }

   public <T extends LivingEntity> void hurtAndBreak(int pAmount, T pEntity, Consumer<T> pOnBroken) {
      if (!pEntity.level.isClientSide && (!(pEntity instanceof PlayerEntity) || !((PlayerEntity)pEntity).abilities.instabuild)) {
         if (this.isDamageableItem()) {
            pAmount = this.getItem().damageItem(this, pAmount, pEntity, pOnBroken);
            if (this.hurt(pAmount, pEntity.getRandom(), pEntity instanceof ServerPlayerEntity ? (ServerPlayerEntity)pEntity : null)) {
               pOnBroken.accept(pEntity);
               Item item = this.getItem();
               this.shrink(1);
               if (pEntity instanceof PlayerEntity) {
                  ((PlayerEntity)pEntity).awardStat(Stats.ITEM_BROKEN.get(item));
               }

               this.setDamageValue(0);
            }

         }
      }
   }

   /**
    * Calls the delegated method to the Item to damage the incoming Entity, and if necessary, triggers a stats increase.
    */
   public void hurtEnemy(LivingEntity pEntity, PlayerEntity pPlayer) {
      Item item = this.getItem();
      if (item.hurtEnemy(this, pEntity, pPlayer)) {
         pPlayer.awardStat(Stats.ITEM_USED.get(item));
      }

   }

   /**
    * Called when a Block is destroyed using this ItemStack
    */
   public void mineBlock(World pLevel, BlockState pState, BlockPos pPos, PlayerEntity pPlayer) {
      Item item = this.getItem();
      if (item.mineBlock(this, pLevel, pState, pPos, pPlayer)) {
         pPlayer.awardStat(Stats.ITEM_USED.get(item));
      }

   }

   /**
    * Check whether the given Block can be harvested using this ItemStack.
    */
   public boolean isCorrectToolForDrops(BlockState pState) {
      return this.getItem().canHarvestBlock(this, pState);
   }

   public ActionResultType interactLivingEntity(PlayerEntity pPlayer, LivingEntity pEntity, Hand pUsedHand) {
      return this.getItem().interactLivingEntity(this, pPlayer, pEntity, pUsedHand);
   }

   /**
    * Returns a new stack with the same properties.
    */
   public ItemStack copy() {
      if (this.isEmpty()) {
         return EMPTY;
      } else {
         ItemStack itemstack = new ItemStack(this.getItem(), this.count, this.serializeCaps());
         itemstack.setPopTime(this.getPopTime());
         if (this.tag != null) {
            itemstack.tag = this.tag.copy();
         }

         return itemstack;
      }
   }

   public static boolean tagMatches(ItemStack pStack, ItemStack pOther) {
      if (pStack.isEmpty() && pOther.isEmpty()) {
         return true;
      } else if (!pStack.isEmpty() && !pOther.isEmpty()) {
         if (pStack.tag == null && pOther.tag != null) {
            return false;
         } else {
            return (pStack.tag == null || pStack.tag.equals(pOther.tag)) && pStack.areCapsCompatible(pOther);
         }
      } else {
         return false;
      }
   }

   /**
    * compares ItemStack argument1 with ItemStack argument2 returns true if both ItemStacks are equal
    */
   public static boolean matches(ItemStack pStack, ItemStack pOther) {
      if (pStack.isEmpty() && pOther.isEmpty()) {
         return true;
      } else {
         return !pStack.isEmpty() && !pOther.isEmpty() ? pStack.matches(pOther) : false;
      }
   }

   /**
    * compares ItemStack argument to the instance ItemStack returns true if both ItemStacks are equal
    */
   private boolean matches(ItemStack pOther) {
      if (this.count != pOther.count) {
         return false;
      } else if (this.getItem() != pOther.getItem()) {
         return false;
      } else if (this.tag == null && pOther.tag != null) {
         return false;
      } else {
         return (this.tag == null || this.tag.equals(pOther.tag)) && this.areCapsCompatible(pOther);
      }
   }

   /**
    * Compares Item and damage value of the two stacks
    */
   public static boolean isSame(ItemStack pStack, ItemStack pOther) {
      if (pStack == pOther) {
         return true;
      } else {
         return !pStack.isEmpty() && !pOther.isEmpty() ? pStack.sameItem(pOther) : false;
      }
   }

   public static boolean isSameIgnoreDurability(ItemStack pStack, ItemStack pOther) {
      if (pStack == pOther) {
         return true;
      } else {
         return !pStack.isEmpty() && !pOther.isEmpty() ? pStack.sameItemStackIgnoreDurability(pOther) : false;
      }
   }

   /**
    * compares ItemStack argument to the instance ItemStack returns true if the Items contained in both ItemStacks are
    * equal
    */
   public boolean sameItem(ItemStack pOther) {
      return !pOther.isEmpty() && this.getItem() == pOther.getItem();
   }

   public boolean sameItemStackIgnoreDurability(ItemStack pStack) {
      if (!this.isDamageableItem()) {
         return this.sameItem(pStack);
      } else {
         return !pStack.isEmpty() && this.getItem() == pStack.getItem();
      }
   }

   public String getDescriptionId() {
      return this.getItem().getDescriptionId(this);
   }

   public String toString() {
      return this.count + " " + this.getItem();
   }

   /**
    * Called each tick as long the ItemStack in on player inventory. Used to progress the pickup animation and update
    * maps.
    */
   public void inventoryTick(World pLevel, Entity pEntity, int pInventorySlot, boolean pIsCurrentItem) {
      if (this.popTime > 0) {
         --this.popTime;
      }

      if (this.getItem() != null) {
         this.getItem().inventoryTick(this, pLevel, pEntity, pInventorySlot, pIsCurrentItem);
      }

   }

   public void onCraftedBy(World pLevel, PlayerEntity pPlayer, int pAmount) {
      pPlayer.awardStat(Stats.ITEM_CRAFTED.get(this.getItem()), pAmount);
      this.getItem().onCraftedBy(this, pLevel, pPlayer);
   }

   public int getUseDuration() {
      return this.getItem().getUseDuration(this);
   }

   public UseAction getUseAnimation() {
      return this.getItem().getUseAnimation(this);
   }

   /**
    * Called when the player releases the use item button.
    */
   public void releaseUsing(World pLevel, LivingEntity pLivingEntity, int pTimeLeft) {
      this.getItem().releaseUsing(this, pLevel, pLivingEntity, pTimeLeft);
   }

   public boolean useOnRelease() {
      return this.getItem().useOnRelease(this);
   }

   /**
    * Returns true if the ItemStack has an NBTTagCompound. Currently used to store enchantments.
    */
   public boolean hasTag() {
      return !this.emptyCacheFlag && this.tag != null && !this.tag.isEmpty();
   }

   @Nullable
   public CompoundNBT getTag() {
      return this.tag;
   }

   public CompoundNBT getOrCreateTag() {
      if (this.tag == null) {
         this.setTag(new CompoundNBT());
      }

      return this.tag;
   }

   public CompoundNBT getOrCreateTagElement(String pKey) {
      if (this.tag != null && this.tag.contains(pKey, 10)) {
         return this.tag.getCompound(pKey);
      } else {
         CompoundNBT compoundnbt = new CompoundNBT();
         this.addTagElement(pKey, compoundnbt);
         return compoundnbt;
      }
   }

   /**
    * Get an NBTTagCompound from this stack's NBT data.
    */
   @Nullable
   public CompoundNBT getTagElement(String pKey) {
      return this.tag != null && this.tag.contains(pKey, 10) ? this.tag.getCompound(pKey) : null;
   }

   public void removeTagKey(String pKey) {
      if (this.tag != null && this.tag.contains(pKey)) {
         this.tag.remove(pKey);
         if (this.tag.isEmpty()) {
            this.tag = null;
         }
      }

   }

   public ListNBT getEnchantmentTags() {
      return this.tag != null ? this.tag.getList("Enchantments", 10) : new ListNBT();
   }

   /**
    * Assigns a NBTTagCompound to the ItemStack, minecraft validates that only non-stackable items can have it.
    */
   public void setTag(@Nullable CompoundNBT p_77982_1_) {
      this.tag = p_77982_1_;
      if (this.getItem().isDamageable(this)) {
         this.setDamageValue(this.getDamageValue());
      }

   }

   public ITextComponent getHoverName() {
      CompoundNBT compoundnbt = this.getTagElement("display");
      if (compoundnbt != null && compoundnbt.contains("Name", 8)) {
         try {
            ITextComponent itextcomponent = ITextComponent.Serializer.fromJson(compoundnbt.getString("Name"));
            if (itextcomponent != null) {
               return itextcomponent;
            }

            compoundnbt.remove("Name");
         } catch (JsonParseException jsonparseexception) {
            compoundnbt.remove("Name");
         }
      }

      return this.getItem().getName(this);
   }

   public ItemStack setHoverName(@Nullable ITextComponent pNameComponent) {
      CompoundNBT compoundnbt = this.getOrCreateTagElement("display");
      if (pNameComponent != null) {
         compoundnbt.putString("Name", ITextComponent.Serializer.toJson(pNameComponent));
      } else {
         compoundnbt.remove("Name");
      }

      return this;
   }

   /**
    * Clear any custom name set for this ItemStack
    */
   public void resetHoverName() {
      CompoundNBT compoundnbt = this.getTagElement("display");
      if (compoundnbt != null) {
         compoundnbt.remove("Name");
         if (compoundnbt.isEmpty()) {
            this.removeTagKey("display");
         }
      }

      if (this.tag != null && this.tag.isEmpty()) {
         this.tag = null;
      }

   }

   /**
    * Returns true if the itemstack has a display name
    */
   public boolean hasCustomHoverName() {
      CompoundNBT compoundnbt = this.getTagElement("display");
      return compoundnbt != null && compoundnbt.contains("Name", 8);
   }

   /**
    * Return a list of strings containing information about the item
    */
   @OnlyIn(Dist.CLIENT)
   public List<ITextComponent> getTooltipLines(@Nullable PlayerEntity pPlayer, ITooltipFlag pIsAdvanced) {
      List<ITextComponent> list = Lists.newArrayList();
      IFormattableTextComponent iformattabletextcomponent = (new StringTextComponent("")).append(this.getHoverName()).withStyle(this.getRarity().color);
      if (this.hasCustomHoverName()) {
         iformattabletextcomponent.withStyle(TextFormatting.ITALIC);
      }

      list.add(iformattabletextcomponent);
      if (!pIsAdvanced.isAdvanced() && !this.hasCustomHoverName() && this.getItem() == Items.FILLED_MAP) {
         list.add((new StringTextComponent("#" + FilledMapItem.getMapId(this))).withStyle(TextFormatting.GRAY));
      }

      int i = this.getHideFlags();
      if (shouldShowInTooltip(i, ItemStack.TooltipDisplayFlags.ADDITIONAL)) {
         this.getItem().appendHoverText(this, pPlayer == null ? null : pPlayer.level, list, pIsAdvanced);
      }

      if (this.hasTag()) {
         if (shouldShowInTooltip(i, ItemStack.TooltipDisplayFlags.ENCHANTMENTS)) {
            appendEnchantmentNames(list, this.getEnchantmentTags());
         }

         if (this.tag.contains("display", 10)) {
            CompoundNBT compoundnbt = this.tag.getCompound("display");
            if (shouldShowInTooltip(i, ItemStack.TooltipDisplayFlags.DYE) && compoundnbt.contains("color", 99)) {
               if (pIsAdvanced.isAdvanced()) {
                  list.add((new TranslationTextComponent("item.color", String.format("#%06X", compoundnbt.getInt("color")))).withStyle(TextFormatting.GRAY));
               } else {
                  list.add((new TranslationTextComponent("item.dyed")).withStyle(new TextFormatting[]{TextFormatting.GRAY, TextFormatting.ITALIC}));
               }
            }

            if (compoundnbt.getTagType("Lore") == 9) {
               ListNBT listnbt = compoundnbt.getList("Lore", 8);

               for(int j = 0; j < listnbt.size(); ++j) {
                  String s = listnbt.getString(j);

                  try {
                     IFormattableTextComponent iformattabletextcomponent1 = ITextComponent.Serializer.fromJson(s);
                     if (iformattabletextcomponent1 != null) {
                        list.add(TextComponentUtils.mergeStyles(iformattabletextcomponent1, LORE_STYLE));
                     }
                  } catch (JsonParseException jsonparseexception) {
                     compoundnbt.remove("Lore");
                  }
               }
            }
         }
      }

      if (shouldShowInTooltip(i, ItemStack.TooltipDisplayFlags.MODIFIERS)) {
         for(EquipmentSlotType equipmentslottype : EquipmentSlotType.values()) {
            Multimap<Attribute, AttributeModifier> multimap = this.getAttributeModifiers(equipmentslottype);
            if (!multimap.isEmpty()) {
               list.add(StringTextComponent.EMPTY);
               list.add((new TranslationTextComponent("item.modifiers." + equipmentslottype.getName())).withStyle(TextFormatting.GRAY));

               for(Entry<Attribute, AttributeModifier> entry : multimap.entries()) {
                  AttributeModifier attributemodifier = entry.getValue();
                  double d0 = attributemodifier.getAmount();
                  boolean flag = false;
                  if (pPlayer != null) {
                     if (attributemodifier.getId() == Item.BASE_ATTACK_DAMAGE_UUID) {
                        d0 = d0 + pPlayer.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                        d0 = d0 + (double)EnchantmentHelper.getDamageBonus(this, CreatureAttribute.UNDEFINED);
                        flag = true;
                     } else if (attributemodifier.getId() == Item.BASE_ATTACK_SPEED_UUID) {
                        d0 += pPlayer.getAttributeBaseValue(Attributes.ATTACK_SPEED);
                        flag = true;
                     }
                  }

                  double d1;
                  if (attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributemodifier.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
                     if (entry.getKey().equals(Attributes.KNOCKBACK_RESISTANCE)) {
                        d1 = d0 * 10.0D;
                     } else {
                        d1 = d0;
                     }
                  } else {
                     d1 = d0 * 100.0D;
                  }

                  if (flag) {
                     list.add((new StringTextComponent(" ")).append(new TranslationTextComponent("attribute.modifier.equals." + attributemodifier.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslationTextComponent(entry.getKey().getDescriptionId()))).withStyle(TextFormatting.DARK_GREEN));
                  } else if (d0 > 0.0D) {
                     list.add((new TranslationTextComponent("attribute.modifier.plus." + attributemodifier.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslationTextComponent(entry.getKey().getDescriptionId()))).withStyle(TextFormatting.BLUE));
                  } else if (d0 < 0.0D) {
                     d1 = d1 * -1.0D;
                     list.add((new TranslationTextComponent("attribute.modifier.take." + attributemodifier.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(d1), new TranslationTextComponent(entry.getKey().getDescriptionId()))).withStyle(TextFormatting.RED));
                  }
               }
            }
         }
      }

      if (this.hasTag()) {
         if (shouldShowInTooltip(i, ItemStack.TooltipDisplayFlags.UNBREAKABLE) && this.tag.getBoolean("Unbreakable")) {
            list.add((new TranslationTextComponent("item.unbreakable")).withStyle(TextFormatting.BLUE));
         }

         if (shouldShowInTooltip(i, ItemStack.TooltipDisplayFlags.CAN_DESTROY) && this.tag.contains("CanDestroy", 9)) {
            ListNBT listnbt1 = this.tag.getList("CanDestroy", 8);
            if (!listnbt1.isEmpty()) {
               list.add(StringTextComponent.EMPTY);
               list.add((new TranslationTextComponent("item.canBreak")).withStyle(TextFormatting.GRAY));

               for(int k = 0; k < listnbt1.size(); ++k) {
                  list.addAll(expandBlockState(listnbt1.getString(k)));
               }
            }
         }

         if (shouldShowInTooltip(i, ItemStack.TooltipDisplayFlags.CAN_PLACE) && this.tag.contains("CanPlaceOn", 9)) {
            ListNBT listnbt2 = this.tag.getList("CanPlaceOn", 8);
            if (!listnbt2.isEmpty()) {
               list.add(StringTextComponent.EMPTY);
               list.add((new TranslationTextComponent("item.canPlace")).withStyle(TextFormatting.GRAY));

               for(int l = 0; l < listnbt2.size(); ++l) {
                  list.addAll(expandBlockState(listnbt2.getString(l)));
               }
            }
         }
      }

      if (pIsAdvanced.isAdvanced()) {
         if (this.isDamaged()) {
            list.add(new TranslationTextComponent("item.durability", this.getMaxDamage() - this.getDamageValue(), this.getMaxDamage()));
         }

         list.add((new StringTextComponent(Registry.ITEM.getKey(this.getItem()).toString())).withStyle(TextFormatting.DARK_GRAY));
         if (this.hasTag()) {
            list.add((new TranslationTextComponent("item.nbt_tags", this.tag.getAllKeys().size())).withStyle(TextFormatting.DARK_GRAY));
         }
      }

      net.minecraftforge.event.ForgeEventFactory.onItemTooltip(this, pPlayer, list, pIsAdvanced);
      return list;
   }

   @OnlyIn(Dist.CLIENT)
   private static boolean shouldShowInTooltip(int pHideFlags, ItemStack.TooltipDisplayFlags pPart) {
      return (pHideFlags & pPart.getMask()) == 0;
   }

   @OnlyIn(Dist.CLIENT)
   private int getHideFlags() {
      return this.hasTag() && this.tag.contains("HideFlags", 99) ? this.tag.getInt("HideFlags") : 0;
   }

   public void hideTooltipPart(ItemStack.TooltipDisplayFlags pPart) {
      CompoundNBT compoundnbt = this.getOrCreateTag();
      compoundnbt.putInt("HideFlags", compoundnbt.getInt("HideFlags") | pPart.getMask());
   }

   @OnlyIn(Dist.CLIENT)
   public static void appendEnchantmentNames(List<ITextComponent> pTooltipComponents, ListNBT pStoredEnchantments) {
      for(int i = 0; i < pStoredEnchantments.size(); ++i) {
         CompoundNBT compoundnbt = pStoredEnchantments.getCompound(i);
         Registry.ENCHANTMENT.getOptional(ResourceLocation.tryParse(compoundnbt.getString("id"))).ifPresent((p_222123_2_) -> {
            pTooltipComponents.add(p_222123_2_.getFullname(compoundnbt.getInt("lvl")));
         });
      }

   }

   @OnlyIn(Dist.CLIENT)
   private static Collection<ITextComponent> expandBlockState(String pStateString) {
      try {
         BlockStateParser blockstateparser = (new BlockStateParser(new StringReader(pStateString), true)).parse(true);
         BlockState blockstate = blockstateparser.getState();
         ResourceLocation resourcelocation = blockstateparser.getTag();
         boolean flag = blockstate != null;
         boolean flag1 = resourcelocation != null;
         if (flag || flag1) {
            if (flag) {
               return Lists.newArrayList(blockstate.getBlock().getName().withStyle(TextFormatting.DARK_GRAY));
            }

            ITag<Block> itag = BlockTags.getAllTags().getTag(resourcelocation);
            if (itag != null) {
               Collection<Block> collection = itag.getValues();
               if (!collection.isEmpty()) {
                  return collection.stream().map(Block::getName).map((p_222119_0_) -> {
                     return p_222119_0_.withStyle(TextFormatting.DARK_GRAY);
                  }).collect(Collectors.toList());
               }
            }
         }
      } catch (CommandSyntaxException commandsyntaxexception) {
      }

      return Lists.newArrayList((new StringTextComponent("missingno")).withStyle(TextFormatting.DARK_GRAY));
   }

   public boolean hasFoil() {
      return this.getItem().isFoil(this);
   }

   public Rarity getRarity() {
      return this.getItem().getRarity(this);
   }

   /**
    * True if it is a tool and has no enchantments to begin with
    */
   public boolean isEnchantable() {
      if (!this.getItem().isEnchantable(this)) {
         return false;
      } else {
         return !this.isEnchanted();
      }
   }

   /**
    * Adds an enchantment with a desired level on the ItemStack.
    */
   public void enchant(Enchantment pEnchantment, int pLevel) {
      this.getOrCreateTag();
      if (!this.tag.contains("Enchantments", 9)) {
         this.tag.put("Enchantments", new ListNBT());
      }

      ListNBT listnbt = this.tag.getList("Enchantments", 10);
      CompoundNBT compoundnbt = new CompoundNBT();
      compoundnbt.putString("id", String.valueOf((Object)Registry.ENCHANTMENT.getKey(pEnchantment)));
      compoundnbt.putShort("lvl", (short)((byte)pLevel));
      listnbt.add(compoundnbt);
   }

   /**
    * True if the item has enchantment data
    */
   public boolean isEnchanted() {
      if (this.tag != null && this.tag.contains("Enchantments", 9)) {
         return !this.tag.getList("Enchantments", 10).isEmpty();
      } else {
         return false;
      }
   }

   public void addTagElement(String pKey, INBT pTag) {
      this.getOrCreateTag().put(pKey, pTag);
   }

   /**
    * Return whether this stack is on an item frame.
    */
   public boolean isFramed() {
      return this.entityRepresentation instanceof ItemFrameEntity;
   }

   public void setEntityRepresentation(@Nullable Entity pEntity) {
      this.entityRepresentation = pEntity;
   }

   /**
    * Return the item frame this stack is on. Returns null if not on an item frame.
    */
   @Nullable
   public ItemFrameEntity getFrame() {
      return this.entityRepresentation instanceof ItemFrameEntity ? (ItemFrameEntity)this.getEntityRepresentation() : null;
   }

   /**
    * For example it'll return a ItemFrameEntity if it is in an itemframe
    */
   @Nullable
   public Entity getEntityRepresentation() {
      return !this.emptyCacheFlag ? this.entityRepresentation : null;
   }

   /**
    * Get this stack's repair cost, or 0 if no repair cost is defined.
    */
   public int getBaseRepairCost() {
      return this.hasTag() && this.tag.contains("RepairCost", 3) ? this.tag.getInt("RepairCost") : 0;
   }

   /**
    * Set this stack's repair cost.
    */
   public void setRepairCost(int pCost) {
      this.getOrCreateTag().putInt("RepairCost", pCost);
   }

   /**
    * Gets the attribute modifiers for this ItemStack.
    * Will check for an NBT tag list containing modifiers for the stack.
    */
   public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlotType pSlot) {
      Multimap<Attribute, AttributeModifier> multimap;
      if (this.hasTag() && this.tag.contains("AttributeModifiers", 9)) {
         multimap = HashMultimap.create();
         ListNBT listnbt = this.tag.getList("AttributeModifiers", 10);

         for(int i = 0; i < listnbt.size(); ++i) {
            CompoundNBT compoundnbt = listnbt.getCompound(i);
            if (!compoundnbt.contains("Slot", 8) || compoundnbt.getString("Slot").equals(pSlot.getName())) {
               Optional<Attribute> optional = Registry.ATTRIBUTE.getOptional(ResourceLocation.tryParse(compoundnbt.getString("AttributeName")));
               if (optional.isPresent()) {
                  AttributeModifier attributemodifier = AttributeModifier.load(compoundnbt);
                  if (attributemodifier != null && attributemodifier.getId().getLeastSignificantBits() != 0L && attributemodifier.getId().getMostSignificantBits() != 0L) {
                     multimap.put(optional.get(), attributemodifier);
                  }
               }
            }
         }
      } else {
         multimap = this.getItem().getAttributeModifiers(pSlot, this);
      }

      multimap = net.minecraftforge.common.ForgeHooks.getAttributeModifiers(this, pSlot, multimap);
      return multimap;
   }

   public void addAttributeModifier(Attribute pAttribute, AttributeModifier pModifier, @Nullable EquipmentSlotType pSlot) {
      this.getOrCreateTag();
      if (!this.tag.contains("AttributeModifiers", 9)) {
         this.tag.put("AttributeModifiers", new ListNBT());
      }

      ListNBT listnbt = this.tag.getList("AttributeModifiers", 10);
      CompoundNBT compoundnbt = pModifier.save();
      compoundnbt.putString("AttributeName", Registry.ATTRIBUTE.getKey(pAttribute).toString());
      if (pSlot != null) {
         compoundnbt.putString("Slot", pSlot.getName());
      }

      listnbt.add(compoundnbt);
   }

   /**
    * Get a ChatComponent for this Item's display name that shows this Item on hover
    */
   public ITextComponent getDisplayName() {
      IFormattableTextComponent iformattabletextcomponent = (new StringTextComponent("")).append(this.getHoverName());
      if (this.hasCustomHoverName()) {
         iformattabletextcomponent.withStyle(TextFormatting.ITALIC);
      }

      IFormattableTextComponent iformattabletextcomponent1 = TextComponentUtils.wrapInSquareBrackets(iformattabletextcomponent);
      if (!this.emptyCacheFlag) {
         iformattabletextcomponent1.withStyle(this.getRarity().color).withStyle((p_234702_1_) -> {
            return p_234702_1_.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemHover(this)));
         });
      }

      return iformattabletextcomponent1;
   }

   private static boolean areSameBlocks(CachedBlockInfo pBlock, @Nullable CachedBlockInfo pOther) {
      if (pOther != null && pBlock.getState() == pOther.getState()) {
         if (pBlock.getEntity() == null && pOther.getEntity() == null) {
            return true;
         } else {
            return pBlock.getEntity() != null && pOther.getEntity() != null ? Objects.equals(pBlock.getEntity().save(new CompoundNBT()), pOther.getEntity().save(new CompoundNBT())) : false;
         }
      } else {
         return false;
      }
   }

   public boolean hasAdventureModeBreakTagForBlock(ITagCollectionSupplier pTagContainer, CachedBlockInfo pBlock) {
      if (areSameBlocks(pBlock, this.cachedBreakBlock)) {
         return this.cachedBreakBlockResult;
      } else {
         this.cachedBreakBlock = pBlock;
         if (this.hasTag() && this.tag.contains("CanDestroy", 9)) {
            ListNBT listnbt = this.tag.getList("CanDestroy", 8);

            for(int i = 0; i < listnbt.size(); ++i) {
               String s = listnbt.getString(i);

               try {
                  Predicate<CachedBlockInfo> predicate = BlockPredicateArgument.blockPredicate().parse(new StringReader(s)).create(pTagContainer);
                  if (predicate.test(pBlock)) {
                     this.cachedBreakBlockResult = true;
                     return true;
                  }
               } catch (CommandSyntaxException commandsyntaxexception) {
               }
            }
         }

         this.cachedBreakBlockResult = false;
         return false;
      }
   }

   public boolean hasAdventureModePlaceTagForBlock(ITagCollectionSupplier pTagContainer, CachedBlockInfo pBlock) {
      if (areSameBlocks(pBlock, this.cachedPlaceBlock)) {
         return this.cachedPlaceBlockResult;
      } else {
         this.cachedPlaceBlock = pBlock;
         if (this.hasTag() && this.tag.contains("CanPlaceOn", 9)) {
            ListNBT listnbt = this.tag.getList("CanPlaceOn", 8);

            for(int i = 0; i < listnbt.size(); ++i) {
               String s = listnbt.getString(i);

               try {
                  Predicate<CachedBlockInfo> predicate = BlockPredicateArgument.blockPredicate().parse(new StringReader(s)).create(pTagContainer);
                  if (predicate.test(pBlock)) {
                     this.cachedPlaceBlockResult = true;
                     return true;
                  }
               } catch (CommandSyntaxException commandsyntaxexception) {
               }
            }
         }

         this.cachedPlaceBlockResult = false;
         return false;
      }
   }

   public int getPopTime() {
      return this.popTime;
   }

   public void setPopTime(int pPopTime) {
      this.popTime = pPopTime;
   }

   public int getCount() {
      return this.emptyCacheFlag ? 0 : this.count;
   }

   public void setCount(int pCount) {
      this.count = pCount;
      this.updateEmptyCacheFlag();
   }

   public void grow(int pIncrement) {
      this.setCount(this.count + pIncrement);
   }

   public void shrink(int pDecrement) {
      this.grow(-pDecrement);
   }

   /**
    * Called as the stack is being used by an entity.
    */
   public void onUseTick(World pLevel, LivingEntity pLivingEntity, int pCount) {
      this.getItem().onUseTick(pLevel, pLivingEntity, this, pCount);
   }

   public boolean isEdible() {
      return this.getItem().isEdible();
   }

   // FORGE START
   public void deserializeNBT(CompoundNBT nbt) {
      final ItemStack itemStack = ItemStack.of(nbt);
      getStack().setTag(itemStack.getTag());
      if (itemStack.capNBT != null) deserializeCaps(itemStack.capNBT);
   }

   /**
    * Set up forge's ItemStack additions.
    */
   private void forgeInit() {
      if (this.delegate != null) {
         this.gatherCapabilities(() -> item.initCapabilities(this, this.capNBT));
         if (this.capNBT != null) deserializeCaps(this.capNBT);
      }
   }

   public SoundEvent getDrinkingSound() {
      return this.getItem().getDrinkingSound();
   }

   public SoundEvent getEatingSound() {
      return this.getItem().getEatingSound();
   }

   public static enum TooltipDisplayFlags {
      ENCHANTMENTS,
      MODIFIERS,
      UNBREAKABLE,
      CAN_DESTROY,
      CAN_PLACE,
      ADDITIONAL,
      DYE;

      private int mask = 1 << this.ordinal();

      public int getMask() {
         return this.mask;
      }
   }
}
