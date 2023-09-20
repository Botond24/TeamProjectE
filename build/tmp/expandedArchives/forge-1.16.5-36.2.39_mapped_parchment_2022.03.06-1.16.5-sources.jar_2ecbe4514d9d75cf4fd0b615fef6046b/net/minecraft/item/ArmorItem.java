package net.minecraft.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.enchantment.IArmorVanishable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ArmorItem extends Item implements IArmorVanishable {
   private static final UUID[] ARMOR_MODIFIER_UUID_PER_SLOT = new UUID[]{UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"), UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")};
   public static final IDispenseItemBehavior DISPENSE_ITEM_BEHAVIOR = new DefaultDispenseItemBehavior() {
      /**
       * Dispense the specified stack, play the dispense sound and spawn particles.
       */
      protected ItemStack execute(IBlockSource pSource, ItemStack pStack) {
         return ArmorItem.dispenseArmor(pSource, pStack) ? pStack : super.execute(pSource, pStack);
      }
   };
   protected final EquipmentSlotType slot;
   private final int defense;
   private final float toughness;
   protected final float knockbackResistance;
   protected final IArmorMaterial material;
   private final Multimap<Attribute, AttributeModifier> defaultModifiers;

   public static boolean dispenseArmor(IBlockSource pSource, ItemStack pStack) {
      BlockPos blockpos = pSource.getPos().relative(pSource.getBlockState().getValue(DispenserBlock.FACING));
      List<LivingEntity> list = pSource.getLevel().getEntitiesOfClass(LivingEntity.class, new AxisAlignedBB(blockpos), EntityPredicates.NO_SPECTATORS.and(new EntityPredicates.ArmoredMob(pStack)));
      if (list.isEmpty()) {
         return false;
      } else {
         LivingEntity livingentity = list.get(0);
         EquipmentSlotType equipmentslottype = MobEntity.getEquipmentSlotForItem(pStack);
         ItemStack itemstack = pStack.split(1);
         livingentity.setItemSlot(equipmentslottype, itemstack);
         if (livingentity instanceof MobEntity) {
            ((MobEntity)livingentity).setDropChance(equipmentslottype, 2.0F);
            ((MobEntity)livingentity).setPersistenceRequired();
         }

         return true;
      }
   }

   public ArmorItem(IArmorMaterial pMaterial, EquipmentSlotType pSlot, Item.Properties pProperties) {
      super(pProperties.defaultDurability(pMaterial.getDurabilityForSlot(pSlot)));
      this.material = pMaterial;
      this.slot = pSlot;
      this.defense = pMaterial.getDefenseForSlot(pSlot);
      this.toughness = pMaterial.getToughness();
      this.knockbackResistance = pMaterial.getKnockbackResistance();
      DispenserBlock.registerBehavior(this, DISPENSE_ITEM_BEHAVIOR);
      Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
      UUID uuid = ARMOR_MODIFIER_UUID_PER_SLOT[pSlot.getIndex()];
      builder.put(Attributes.ARMOR, new AttributeModifier(uuid, "Armor modifier", (double)this.defense, AttributeModifier.Operation.ADDITION));
      builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(uuid, "Armor toughness", (double)this.toughness, AttributeModifier.Operation.ADDITION));
      if (this.knockbackResistance > 0) {
         builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(uuid, "Armor knockback resistance", (double)this.knockbackResistance, AttributeModifier.Operation.ADDITION));
      }

      this.defaultModifiers = builder.build();
   }

   /**
    * Gets the equipment slot of this armor piece (formerly known as armor type)
    */
   public EquipmentSlotType getSlot() {
      return this.slot;
   }

   /**
    * Return the enchantability factor of the item, most of the time is based on material.
    */
   public int getEnchantmentValue() {
      return this.material.getEnchantmentValue();
   }

   public IArmorMaterial getMaterial() {
      return this.material;
   }

   /**
    * Return whether this item is repairable in an anvil.
    */
   public boolean isValidRepairItem(ItemStack pToRepair, ItemStack pRepair) {
      return this.material.getRepairIngredient().test(pRepair) || super.isValidRepairItem(pToRepair, pRepair);
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      EquipmentSlotType equipmentslottype = MobEntity.getEquipmentSlotForItem(itemstack);
      ItemStack itemstack1 = pPlayer.getItemBySlot(equipmentslottype);
      if (itemstack1.isEmpty()) {
         pPlayer.setItemSlot(equipmentslottype, itemstack.copy());
         itemstack.setCount(0);
         return ActionResult.sidedSuccess(itemstack, pLevel.isClientSide());
      } else {
         return ActionResult.fail(itemstack);
      }
   }

   /**
    * Gets a map of item attribute modifiers, used by ItemSword to increase hit damage.
    */
   public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlotType pEquipmentSlot) {
      return pEquipmentSlot == this.slot ? this.defaultModifiers : super.getDefaultAttributeModifiers(pEquipmentSlot);
   }

   public int getDefense() {
      return this.defense;
   }

   public float getToughness() {
      return this.toughness;
   }
}
