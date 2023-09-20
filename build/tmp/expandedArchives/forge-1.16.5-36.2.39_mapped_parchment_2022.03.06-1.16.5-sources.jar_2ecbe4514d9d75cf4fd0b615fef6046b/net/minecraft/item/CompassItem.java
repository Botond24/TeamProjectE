package net.minecraft.item;

import java.util.Optional;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.IVanishable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CompassItem extends Item implements IVanishable {
   private static final Logger LOGGER = LogManager.getLogger();

   public CompassItem(Item.Properties p_i48515_1_) {
      super(p_i48515_1_);
   }

   public static boolean isLodestoneCompass(ItemStack pStack) {
      CompoundNBT compoundnbt = pStack.getTag();
      return compoundnbt != null && (compoundnbt.contains("LodestoneDimension") || compoundnbt.contains("LodestonePos"));
   }

   /**
    * Returns true if this item has an enchantment glint. By default, this returns <code>stack.isItemEnchanted()</code>,
    * but other items can override it (for instance, written books always return true).
    * 
    * Note that if you override this method, you generally want to also call the super version (on {@link Item}) to get
    * the glint for enchanted items. Of course, that is unnecessary if the overwritten version always returns true.
    */
   public boolean isFoil(ItemStack pStack) {
      return isLodestoneCompass(pStack) || super.isFoil(pStack);
   }

   public static Optional<RegistryKey<World>> getLodestoneDimension(CompoundNBT pCompoundTag) {
      return World.RESOURCE_KEY_CODEC.parse(NBTDynamicOps.INSTANCE, pCompoundTag.get("LodestoneDimension")).result();
   }

   /**
    * Called each tick as long the item is on a player inventory. Uses by maps to check if is on a player hand and
    * update it's contents.
    */
   public void inventoryTick(ItemStack pStack, World pLevel, Entity pEntity, int pItemSlot, boolean pIsSelected) {
      if (!pLevel.isClientSide) {
         if (isLodestoneCompass(pStack)) {
            CompoundNBT compoundnbt = pStack.getOrCreateTag();
            if (compoundnbt.contains("LodestoneTracked") && !compoundnbt.getBoolean("LodestoneTracked")) {
               return;
            }

            Optional<RegistryKey<World>> optional = getLodestoneDimension(compoundnbt);
            if (optional.isPresent() && optional.get() == pLevel.dimension() && compoundnbt.contains("LodestonePos") && !((ServerWorld)pLevel).getPoiManager().existsAtPosition(PointOfInterestType.LODESTONE, NBTUtil.readBlockPos(compoundnbt.getCompound("LodestonePos")))) {
               compoundnbt.remove("LodestonePos");
            }
         }

      }
   }

   /**
    * Called when this item is used when targetting a Block
    */
   public ActionResultType useOn(ItemUseContext pContext) {
      BlockPos blockpos = pContext.getClickedPos();
      World world = pContext.getLevel();
      if (!world.getBlockState(blockpos).is(Blocks.LODESTONE)) {
         return super.useOn(pContext);
      } else {
         world.playSound((PlayerEntity)null, blockpos, SoundEvents.LODESTONE_COMPASS_LOCK, SoundCategory.PLAYERS, 1.0F, 1.0F);
         PlayerEntity playerentity = pContext.getPlayer();
         ItemStack itemstack = pContext.getItemInHand();
         boolean flag = !playerentity.abilities.instabuild && itemstack.getCount() == 1;
         if (flag) {
            this.addLodestoneTags(world.dimension(), blockpos, itemstack.getOrCreateTag());
         } else {
            ItemStack itemstack1 = new ItemStack(Items.COMPASS, 1);
            CompoundNBT compoundnbt = itemstack.hasTag() ? itemstack.getTag().copy() : new CompoundNBT();
            itemstack1.setTag(compoundnbt);
            if (!playerentity.abilities.instabuild) {
               itemstack.shrink(1);
            }

            this.addLodestoneTags(world.dimension(), blockpos, compoundnbt);
            if (!playerentity.inventory.add(itemstack1)) {
               playerentity.drop(itemstack1, false);
            }
         }

         return ActionResultType.sidedSuccess(world.isClientSide);
      }
   }

   private void addLodestoneTags(RegistryKey<World> pLodestoneDimension, BlockPos pLodestonePos, CompoundNBT pCompoundTag) {
      pCompoundTag.put("LodestonePos", NBTUtil.writeBlockPos(pLodestonePos));
      World.RESOURCE_KEY_CODEC.encodeStart(NBTDynamicOps.INSTANCE, pLodestoneDimension).resultOrPartial(LOGGER::error).ifPresent((p_234668_1_) -> {
         pCompoundTag.put("LodestoneDimension", p_234668_1_);
      });
      pCompoundTag.putBoolean("LodestoneTracked", true);
   }

   /**
    * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
    * different names based on their damage or NBT.
    */
   public String getDescriptionId(ItemStack pStack) {
      return isLodestoneCompass(pStack) ? "item.minecraft.lodestone_compass" : super.getDescriptionId(pStack);
   }
}