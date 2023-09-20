package net.minecraft.inventory.container;

import java.util.List;
import java.util.Random;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EnchantmentContainer extends Container {
   private final IInventory enchantSlots = new Inventory(2) {
      /**
       * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think
       * it hasn't changed and skip it.
       */
      public void setChanged() {
         super.setChanged();
         EnchantmentContainer.this.slotsChanged(this);
      }
   };
   private final IWorldPosCallable access;
   private final Random random = new Random();
   private final IntReferenceHolder enchantmentSeed = IntReferenceHolder.standalone();
   public final int[] costs = new int[3];
   public final int[] enchantClue = new int[]{-1, -1, -1};
   public final int[] levelClue = new int[]{-1, -1, -1};

   public EnchantmentContainer(int pContainerId, PlayerInventory pPlayerInventory) {
      this(pContainerId, pPlayerInventory, IWorldPosCallable.NULL);
   }

   public EnchantmentContainer(int pContainerId, PlayerInventory pPlayerInventory, IWorldPosCallable pAccess) {
      super(ContainerType.ENCHANTMENT, pContainerId);
      this.access = pAccess;
      this.addSlot(new Slot(this.enchantSlots, 0, 15, 47) {
         /**
          * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
          */
         public boolean mayPlace(ItemStack pStack) {
            return true;
         }

         /**
          * Returns the maximum stack size for a given slot (usually the same as getInventoryStackLimit(), but 1 in the
          * case of armor slots)
          */
         public int getMaxStackSize() {
            return 1;
         }
      });
      this.addSlot(new Slot(this.enchantSlots, 1, 35, 47) {
         /**
          * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
          */
         public boolean mayPlace(ItemStack pStack) {
            return net.minecraftforge.common.Tags.Items.GEMS_LAPIS.contains(pStack.getItem());
         }
      });

      for(int i = 0; i < 3; ++i) {
         for(int j = 0; j < 9; ++j) {
            this.addSlot(new Slot(pPlayerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }

      for(int k = 0; k < 9; ++k) {
         this.addSlot(new Slot(pPlayerInventory, k, 8 + k * 18, 142));
      }

      this.addDataSlot(IntReferenceHolder.shared(this.costs, 0));
      this.addDataSlot(IntReferenceHolder.shared(this.costs, 1));
      this.addDataSlot(IntReferenceHolder.shared(this.costs, 2));
      this.addDataSlot(this.enchantmentSeed).set(pPlayerInventory.player.getEnchantmentSeed());
      this.addDataSlot(IntReferenceHolder.shared(this.enchantClue, 0));
      this.addDataSlot(IntReferenceHolder.shared(this.enchantClue, 1));
      this.addDataSlot(IntReferenceHolder.shared(this.enchantClue, 2));
      this.addDataSlot(IntReferenceHolder.shared(this.levelClue, 0));
      this.addDataSlot(IntReferenceHolder.shared(this.levelClue, 1));
      this.addDataSlot(IntReferenceHolder.shared(this.levelClue, 2));
   }

   private float getPower(net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos) {
      return world.getBlockState(pos).getEnchantPowerBonus(world, pos);
   }

   /**
    * Callback for when the crafting matrix is changed.
    */
   public void slotsChanged(IInventory pInventory) {
      if (pInventory == this.enchantSlots) {
         ItemStack itemstack = pInventory.getItem(0);
         if (!itemstack.isEmpty() && itemstack.isEnchantable()) {
            this.access.execute((p_217002_2_, p_217002_3_) -> {
               int power = 0;

               for(int k = -1; k <= 1; ++k) {
                  for(int l = -1; l <= 1; ++l) {
                     if ((k != 0 || l != 0) && p_217002_2_.isEmptyBlock(p_217002_3_.offset(l, 0, k)) && p_217002_2_.isEmptyBlock(p_217002_3_.offset(l, 1, k))) {
                        power += getPower(p_217002_2_, p_217002_3_.offset(l * 2, 0, k * 2));
                        power += getPower(p_217002_2_, p_217002_3_.offset(l * 2, 1, k * 2));

                        if (l != 0 && k != 0) {
                           power += getPower(p_217002_2_, p_217002_3_.offset(l * 2, 0, k));
                           power += getPower(p_217002_2_, p_217002_3_.offset(l * 2, 1, k));
                           power += getPower(p_217002_2_, p_217002_3_.offset(l, 0, k * 2));
                           power += getPower(p_217002_2_, p_217002_3_.offset(l, 1, k * 2));
                        }
                     }
                  }
               }

               this.random.setSeed((long)this.enchantmentSeed.get());

               for(int i1 = 0; i1 < 3; ++i1) {
                  this.costs[i1] = EnchantmentHelper.getEnchantmentCost(this.random, i1, (int)power, itemstack);
                  this.enchantClue[i1] = -1;
                  this.levelClue[i1] = -1;
                  if (this.costs[i1] < i1 + 1) {
                     this.costs[i1] = 0;
                  }
                  this.costs[i1] = net.minecraftforge.event.ForgeEventFactory.onEnchantmentLevelSet(p_217002_2_, p_217002_3_, i1, (int)power, itemstack, costs[i1]);
               }

               for(int j1 = 0; j1 < 3; ++j1) {
                  if (this.costs[j1] > 0) {
                     List<EnchantmentData> list = this.getEnchantmentList(itemstack, j1, this.costs[j1]);
                     if (list != null && !list.isEmpty()) {
                        EnchantmentData enchantmentdata = list.get(this.random.nextInt(list.size()));
                        this.enchantClue[j1] = Registry.ENCHANTMENT.getId(enchantmentdata.enchantment);
                        this.levelClue[j1] = enchantmentdata.level;
                     }
                  }
               }

               this.broadcastChanges();
            });
         } else {
            for(int i = 0; i < 3; ++i) {
               this.costs[i] = 0;
               this.enchantClue[i] = -1;
               this.levelClue[i] = -1;
            }
         }
      }

   }

   /**
    * Handles the given Button-click on the server, currently only used by enchanting. Name is for legacy.
    */
   public boolean clickMenuButton(PlayerEntity pPlayer, int pId) {
      ItemStack itemstack = this.enchantSlots.getItem(0);
      ItemStack itemstack1 = this.enchantSlots.getItem(1);
      int i = pId + 1;
      if ((itemstack1.isEmpty() || itemstack1.getCount() < i) && !pPlayer.abilities.instabuild) {
         return false;
      } else if (this.costs[pId] <= 0 || itemstack.isEmpty() || (pPlayer.experienceLevel < i || pPlayer.experienceLevel < this.costs[pId]) && !pPlayer.abilities.instabuild) {
         return false;
      } else {
         this.access.execute((p_217003_6_, p_217003_7_) -> {
            ItemStack itemstack2 = itemstack;
            List<EnchantmentData> list = this.getEnchantmentList(itemstack, pId, this.costs[pId]);
            if (!list.isEmpty()) {
               pPlayer.onEnchantmentPerformed(itemstack, i);
               boolean flag = itemstack.getItem() == Items.BOOK;
               if (flag) {
                  itemstack2 = new ItemStack(Items.ENCHANTED_BOOK);
                  CompoundNBT compoundnbt = itemstack.getTag();
                  if (compoundnbt != null) {
                     itemstack2.setTag(compoundnbt.copy());
                  }

                  this.enchantSlots.setItem(0, itemstack2);
               }

               for(int j = 0; j < list.size(); ++j) {
                  EnchantmentData enchantmentdata = list.get(j);
                  if (flag) {
                     EnchantedBookItem.addEnchantment(itemstack2, enchantmentdata);
                  } else {
                     itemstack2.enchant(enchantmentdata.enchantment, enchantmentdata.level);
                  }
               }

               if (!pPlayer.abilities.instabuild) {
                  itemstack1.shrink(i);
                  if (itemstack1.isEmpty()) {
                     this.enchantSlots.setItem(1, ItemStack.EMPTY);
                  }
               }

               pPlayer.awardStat(Stats.ENCHANT_ITEM);
               if (pPlayer instanceof ServerPlayerEntity) {
                  CriteriaTriggers.ENCHANTED_ITEM.trigger((ServerPlayerEntity)pPlayer, itemstack2, i);
               }

               this.enchantSlots.setChanged();
               this.enchantmentSeed.set(pPlayer.getEnchantmentSeed());
               this.slotsChanged(this.enchantSlots);
               p_217003_6_.playSound((PlayerEntity)null, p_217003_7_, SoundEvents.ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, p_217003_6_.random.nextFloat() * 0.1F + 0.9F);
            }

         });
         return true;
      }
   }

   private List<EnchantmentData> getEnchantmentList(ItemStack pStack, int pEnchantSlot, int pLevel) {
      this.random.setSeed((long)(this.enchantmentSeed.get() + pEnchantSlot));
      List<EnchantmentData> list = EnchantmentHelper.selectEnchantment(this.random, pStack, pLevel, false);
      if (pStack.getItem() == Items.BOOK && list.size() > 1) {
         list.remove(this.random.nextInt(list.size()));
      }

      return list;
   }

   @OnlyIn(Dist.CLIENT)
   public int getGoldCount() {
      ItemStack itemstack = this.enchantSlots.getItem(1);
      return itemstack.isEmpty() ? 0 : itemstack.getCount();
   }

   @OnlyIn(Dist.CLIENT)
   public int getEnchantmentSeed() {
      return this.enchantmentSeed.get();
   }

   /**
    * Called when the container is closed.
    */
   public void removed(PlayerEntity pPlayer) {
      super.removed(pPlayer);
      this.access.execute((p_217004_2_, p_217004_3_) -> {
         this.clearContainer(pPlayer, pPlayer.level, this.enchantSlots);
      });
   }

   /**
    * Determines whether supplied player can use this container
    */
   public boolean stillValid(PlayerEntity pPlayer) {
      return stillValid(this.access, pPlayer, Blocks.ENCHANTING_TABLE);
   }

   /**
    * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
    * inventory and the other inventory(s).
    */
   public ItemStack quickMoveStack(PlayerEntity pPlayer, int pIndex) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(pIndex);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if (pIndex == 0) {
            if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
               return ItemStack.EMPTY;
            }
         } else if (pIndex == 1) {
            if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
               return ItemStack.EMPTY;
            }
         } else if (itemstack1.getItem() == Items.LAPIS_LAZULI) {
            if (!this.moveItemStackTo(itemstack1, 1, 2, true)) {
               return ItemStack.EMPTY;
            }
         } else {
            if (this.slots.get(0).hasItem() || !this.slots.get(0).mayPlace(itemstack1)) {
               return ItemStack.EMPTY;
            }

            ItemStack itemstack2 = itemstack1.copy();
            itemstack2.setCount(1);
            itemstack1.shrink(1);
            this.slots.get(0).set(itemstack2);
         }

         if (itemstack1.isEmpty()) {
            slot.set(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }

         if (itemstack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }

         slot.onTake(pPlayer, itemstack1);
      }

      return itemstack;
   }
}
