package net.minecraft.inventory.container;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class Container {
   private final NonNullList<ItemStack> lastSlots = NonNullList.create();
   public final List<Slot> slots = Lists.newArrayList();
   private final List<IntReferenceHolder> dataSlots = Lists.newArrayList();
   @Nullable
   private final ContainerType<?> menuType;
   public final int containerId;
   @OnlyIn(Dist.CLIENT)
   private short changeUid;
   private int quickcraftType = -1;
   private int quickcraftStatus;
   private final Set<Slot> quickcraftSlots = Sets.newHashSet();
   private final List<IContainerListener> containerListeners = Lists.newArrayList();
   private final Set<PlayerEntity> unSynchedPlayers = Sets.newHashSet();

   protected Container(@Nullable ContainerType<?> pMenuType, int pContainerId) {
      this.menuType = pMenuType;
      this.containerId = pContainerId;
   }

   protected static boolean stillValid(IWorldPosCallable pLevelPos, PlayerEntity pPlayer, Block pTargetBlock) {
      return pLevelPos.evaluate((p_216960_2_, p_216960_3_) -> {
         return !p_216960_2_.getBlockState(p_216960_3_).is(pTargetBlock) ? false : pPlayer.distanceToSqr((double)p_216960_3_.getX() + 0.5D, (double)p_216960_3_.getY() + 0.5D, (double)p_216960_3_.getZ() + 0.5D) <= 64.0D;
      }, true);
   }

   public ContainerType<?> getType() {
      if (this.menuType == null) {
         throw new UnsupportedOperationException("Unable to construct this menu by type");
      } else {
         return this.menuType;
      }
   }

   protected static void checkContainerSize(IInventory pInventory, int pMinSize) {
      int i = pInventory.getContainerSize();
      if (i < pMinSize) {
         throw new IllegalArgumentException("Container size " + i + " is smaller than expected " + pMinSize);
      }
   }

   protected static void checkContainerDataCount(IIntArray pIntArray, int pMinSize) {
      int i = pIntArray.getCount();
      if (i < pMinSize) {
         throw new IllegalArgumentException("Container data count " + i + " is smaller than expected " + pMinSize);
      }
   }

   /**
    * Adds an item slot to this container
    */
   protected Slot addSlot(Slot pSlot) {
      pSlot.index = this.slots.size();
      this.slots.add(pSlot);
      this.lastSlots.add(ItemStack.EMPTY);
      return pSlot;
   }

   protected IntReferenceHolder addDataSlot(IntReferenceHolder pIntValue) {
      this.dataSlots.add(pIntValue);
      return pIntValue;
   }

   protected void addDataSlots(IIntArray pArray) {
      for(int i = 0; i < pArray.getCount(); ++i) {
         this.addDataSlot(IntReferenceHolder.forContainer(pArray, i));
      }

   }

   public void addSlotListener(IContainerListener pListener) {
      if (!this.containerListeners.contains(pListener)) {
         this.containerListeners.add(pListener);
         pListener.refreshContainer(this, this.getItems());
         this.broadcastChanges();
      }
   }

   /**
    * Remove the given Listener. Method name is for legacy.
    */
   @OnlyIn(Dist.CLIENT)
   public void removeSlotListener(IContainerListener pListener) {
      this.containerListeners.remove(pListener);
   }

   /**
    * returns a list if itemStacks, for each slot.
    */
   public NonNullList<ItemStack> getItems() {
      NonNullList<ItemStack> nonnulllist = NonNullList.create();

      for(int i = 0; i < this.slots.size(); ++i) {
         nonnulllist.add(this.slots.get(i).getItem());
      }

      return nonnulllist;
   }

   /**
    * Looks for changes made in the container, sends them to every listener.
    */
   public void broadcastChanges() {
      for(int i = 0; i < this.slots.size(); ++i) {
         ItemStack itemstack = this.slots.get(i).getItem();
         ItemStack itemstack1 = this.lastSlots.get(i);
         if (!ItemStack.matches(itemstack1, itemstack)) {
            boolean clientStackChanged = !itemstack1.equals(itemstack, true);
            ItemStack itemstack2 = itemstack.copy();
            this.lastSlots.set(i, itemstack2);

            if (clientStackChanged)
            for(IContainerListener icontainerlistener : this.containerListeners) {
               icontainerlistener.slotChanged(this, i, itemstack2);
            }
         }
      }

      for(int j = 0; j < this.dataSlots.size(); ++j) {
         IntReferenceHolder intreferenceholder = this.dataSlots.get(j);
         if (intreferenceholder.checkAndClearUpdateFlag()) {
            for(IContainerListener icontainerlistener1 : this.containerListeners) {
               icontainerlistener1.setContainerData(this, j, intreferenceholder.get());
            }
         }
      }

   }

   /**
    * Handles the given Button-click on the server, currently only used by enchanting. Name is for legacy.
    */
   public boolean clickMenuButton(PlayerEntity pPlayer, int pId) {
      return false;
   }

   public Slot getSlot(int pSlotId) {
      return this.slots.get(pSlotId);
   }

   /**
    * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
    * inventory and the other inventory(s).
    */
   public ItemStack quickMoveStack(PlayerEntity pPlayer, int pIndex) {
      Slot slot = this.slots.get(pIndex);
      return slot != null ? slot.getItem() : ItemStack.EMPTY;
   }

   public ItemStack clicked(int pSlotId, int pDragType, ClickType pClickType, PlayerEntity pPlayer) {
      try {
         return this.doClick(pSlotId, pDragType, pClickType, pPlayer);
      } catch (Exception exception) {
         CrashReport crashreport = CrashReport.forThrowable(exception, "Container click");
         CrashReportCategory crashreportcategory = crashreport.addCategory("Click info");
         crashreportcategory.setDetail("Menu Type", () -> {
            return this.menuType != null ? Registry.MENU.getKey(this.menuType).toString() : "<no type>";
         });
         crashreportcategory.setDetail("Menu Class", () -> {
            return this.getClass().getCanonicalName();
         });
         crashreportcategory.setDetail("Slot Count", this.slots.size());
         crashreportcategory.setDetail("Slot", pSlotId);
         crashreportcategory.setDetail("Button", pDragType);
         crashreportcategory.setDetail("Type", pClickType);
         throw new ReportedException(crashreport);
      }
   }

   private ItemStack doClick(int pSlotId, int pDragType, ClickType pClickType, PlayerEntity pPlayer) {
      ItemStack itemstack = ItemStack.EMPTY;
      PlayerInventory playerinventory = pPlayer.inventory;
      if (pClickType == ClickType.QUICK_CRAFT) {
         int i1 = this.quickcraftStatus;
         this.quickcraftStatus = getQuickcraftHeader(pDragType);
         if ((i1 != 1 || this.quickcraftStatus != 2) && i1 != this.quickcraftStatus) {
            this.resetQuickCraft();
         } else if (playerinventory.getCarried().isEmpty()) {
            this.resetQuickCraft();
         } else if (this.quickcraftStatus == 0) {
            this.quickcraftType = getQuickcraftType(pDragType);
            if (isValidQuickcraftType(this.quickcraftType, pPlayer)) {
               this.quickcraftStatus = 1;
               this.quickcraftSlots.clear();
            } else {
               this.resetQuickCraft();
            }
         } else if (this.quickcraftStatus == 1) {
            Slot slot7 = this.slots.get(pSlotId);
            ItemStack itemstack12 = playerinventory.getCarried();
            if (slot7 != null && canItemQuickReplace(slot7, itemstack12, true) && slot7.mayPlace(itemstack12) && (this.quickcraftType == 2 || itemstack12.getCount() > this.quickcraftSlots.size()) && this.canDragTo(slot7)) {
               this.quickcraftSlots.add(slot7);
            }
         } else if (this.quickcraftStatus == 2) {
            if (!this.quickcraftSlots.isEmpty()) {
               ItemStack itemstack10 = playerinventory.getCarried().copy();
               int k1 = playerinventory.getCarried().getCount();

               for(Slot slot8 : this.quickcraftSlots) {
                  ItemStack itemstack13 = playerinventory.getCarried();
                  if (slot8 != null && canItemQuickReplace(slot8, itemstack13, true) && slot8.mayPlace(itemstack13) && (this.quickcraftType == 2 || itemstack13.getCount() >= this.quickcraftSlots.size()) && this.canDragTo(slot8)) {
                     ItemStack itemstack14 = itemstack10.copy();
                     int j3 = slot8.hasItem() ? slot8.getItem().getCount() : 0;
                     getQuickCraftSlotCount(this.quickcraftSlots, this.quickcraftType, itemstack14, j3);
                     int k3 = Math.min(itemstack14.getMaxStackSize(), slot8.getMaxStackSize(itemstack14));
                     if (itemstack14.getCount() > k3) {
                        itemstack14.setCount(k3);
                     }

                     k1 -= itemstack14.getCount() - j3;
                     slot8.set(itemstack14);
                  }
               }

               itemstack10.setCount(k1);
               playerinventory.setCarried(itemstack10);
            }

            this.resetQuickCraft();
         } else {
            this.resetQuickCraft();
         }
      } else if (this.quickcraftStatus != 0) {
         this.resetQuickCraft();
      } else if ((pClickType == ClickType.PICKUP || pClickType == ClickType.QUICK_MOVE) && (pDragType == 0 || pDragType == 1)) {
         if (pSlotId == -999) {
            if (!playerinventory.getCarried().isEmpty()) {
               if (pDragType == 0) {
                  pPlayer.drop(playerinventory.getCarried(), true);
                  playerinventory.setCarried(ItemStack.EMPTY);
               }

               if (pDragType == 1) {
                  pPlayer.drop(playerinventory.getCarried().split(1), true);
               }
            }
         } else if (pClickType == ClickType.QUICK_MOVE) {
            if (pSlotId < 0) {
               return ItemStack.EMPTY;
            }

            Slot slot5 = this.slots.get(pSlotId);
            if (slot5 == null || !slot5.mayPickup(pPlayer)) {
               return ItemStack.EMPTY;
            }

            for(ItemStack itemstack8 = this.quickMoveStack(pPlayer, pSlotId); !itemstack8.isEmpty() && ItemStack.isSame(slot5.getItem(), itemstack8); itemstack8 = this.quickMoveStack(pPlayer, pSlotId)) {
               itemstack = itemstack8.copy();
            }
         } else {
            if (pSlotId < 0) {
               return ItemStack.EMPTY;
            }

            Slot slot6 = this.slots.get(pSlotId);
            if (slot6 != null) {
               ItemStack itemstack9 = slot6.getItem();
               ItemStack itemstack11 = playerinventory.getCarried();
               if (!itemstack9.isEmpty()) {
                  itemstack = itemstack9.copy();
               }

               if (itemstack9.isEmpty()) {
                  if (!itemstack11.isEmpty() && slot6.mayPlace(itemstack11)) {
                     int j2 = pDragType == 0 ? itemstack11.getCount() : 1;
                     if (j2 > slot6.getMaxStackSize(itemstack11)) {
                        j2 = slot6.getMaxStackSize(itemstack11);
                     }

                     slot6.set(itemstack11.split(j2));
                  }
               } else if (slot6.mayPickup(pPlayer)) {
                  if (itemstack11.isEmpty()) {
                     if (itemstack9.isEmpty()) {
                        slot6.set(ItemStack.EMPTY);
                        playerinventory.setCarried(ItemStack.EMPTY);
                     } else {
                        int k2 = pDragType == 0 ? itemstack9.getCount() : (itemstack9.getCount() + 1) / 2;
                        playerinventory.setCarried(slot6.remove(k2));
                        if (itemstack9.isEmpty()) {
                           slot6.set(ItemStack.EMPTY);
                        }

                        slot6.onTake(pPlayer, playerinventory.getCarried());
                     }
                  } else if (slot6.mayPlace(itemstack11)) {
                     if (consideredTheSameItem(itemstack9, itemstack11)) {
                        int l2 = pDragType == 0 ? itemstack11.getCount() : 1;
                        if (l2 > slot6.getMaxStackSize(itemstack11) - itemstack9.getCount()) {
                           l2 = slot6.getMaxStackSize(itemstack11) - itemstack9.getCount();
                        }

                        if (l2 > itemstack11.getMaxStackSize() - itemstack9.getCount()) {
                           l2 = itemstack11.getMaxStackSize() - itemstack9.getCount();
                        }

                        itemstack11.shrink(l2);
                        itemstack9.grow(l2);
                     } else if (itemstack11.getCount() <= slot6.getMaxStackSize(itemstack11)) {
                        slot6.set(itemstack11);
                        playerinventory.setCarried(itemstack9);
                     }
                  } else if (itemstack11.getMaxStackSize() > 1 && consideredTheSameItem(itemstack9, itemstack11) && !itemstack9.isEmpty()) {
                     int i3 = itemstack9.getCount();
                     if (i3 + itemstack11.getCount() <= itemstack11.getMaxStackSize()) {
                        itemstack11.grow(i3);
                        itemstack9 = slot6.remove(i3);
                        if (itemstack9.isEmpty()) {
                           slot6.set(ItemStack.EMPTY);
                        }

                        slot6.onTake(pPlayer, playerinventory.getCarried());
                     }
                  }
               }

               slot6.setChanged();
            }
         }
      } else if (pClickType == ClickType.SWAP) {
         Slot slot = this.slots.get(pSlotId);
         ItemStack itemstack1 = playerinventory.getItem(pDragType);
         ItemStack itemstack2 = slot.getItem();
         if (!itemstack1.isEmpty() || !itemstack2.isEmpty()) {
            if (itemstack1.isEmpty()) {
               if (slot.mayPickup(pPlayer)) {
                  playerinventory.setItem(pDragType, itemstack2);
                  slot.onSwapCraft(itemstack2.getCount());
                  slot.set(ItemStack.EMPTY);
                  slot.onTake(pPlayer, itemstack2);
               }
            } else if (itemstack2.isEmpty()) {
               if (slot.mayPlace(itemstack1)) {
                  int i = slot.getMaxStackSize(itemstack1);
                  if (itemstack1.getCount() > i) {
                     slot.set(itemstack1.split(i));
                  } else {
                     slot.set(itemstack1);
                     playerinventory.setItem(pDragType, ItemStack.EMPTY);
                  }
               }
            } else if (slot.mayPickup(pPlayer) && slot.mayPlace(itemstack1)) {
               int l1 = slot.getMaxStackSize(itemstack1);
               if (itemstack1.getCount() > l1) {
                  slot.set(itemstack1.split(l1));
                  slot.onTake(pPlayer, itemstack2);
                  if (!playerinventory.add(itemstack2)) {
                     pPlayer.drop(itemstack2, true);
                  }
               } else {
                  slot.set(itemstack1);
                  playerinventory.setItem(pDragType, itemstack2);
                  slot.onTake(pPlayer, itemstack2);
               }
            }
         }
      } else if (pClickType == ClickType.CLONE && pPlayer.abilities.instabuild && playerinventory.getCarried().isEmpty() && pSlotId >= 0) {
         Slot slot4 = this.slots.get(pSlotId);
         if (slot4 != null && slot4.hasItem()) {
            ItemStack itemstack7 = slot4.getItem().copy();
            itemstack7.setCount(itemstack7.getMaxStackSize());
            playerinventory.setCarried(itemstack7);
         }
      } else if (pClickType == ClickType.THROW && playerinventory.getCarried().isEmpty() && pSlotId >= 0) {
         Slot slot3 = this.slots.get(pSlotId);
         if (slot3 != null && slot3.hasItem() && slot3.mayPickup(pPlayer)) {
            ItemStack itemstack6 = slot3.remove(pDragType == 0 ? 1 : slot3.getItem().getCount());
            slot3.onTake(pPlayer, itemstack6);
            pPlayer.drop(itemstack6, true);
         }
      } else if (pClickType == ClickType.PICKUP_ALL && pSlotId >= 0) {
         Slot slot2 = this.slots.get(pSlotId);
         ItemStack itemstack5 = playerinventory.getCarried();
         if (!itemstack5.isEmpty() && (slot2 == null || !slot2.hasItem() || !slot2.mayPickup(pPlayer))) {
            int j1 = pDragType == 0 ? 0 : this.slots.size() - 1;
            int i2 = pDragType == 0 ? 1 : -1;

            for(int j = 0; j < 2; ++j) {
               for(int k = j1; k >= 0 && k < this.slots.size() && itemstack5.getCount() < itemstack5.getMaxStackSize(); k += i2) {
                  Slot slot1 = this.slots.get(k);
                  if (slot1.hasItem() && canItemQuickReplace(slot1, itemstack5, true) && slot1.mayPickup(pPlayer) && this.canTakeItemForPickAll(itemstack5, slot1)) {
                     ItemStack itemstack3 = slot1.getItem();
                     if (j != 0 || itemstack3.getCount() != itemstack3.getMaxStackSize()) {
                        int l = Math.min(itemstack5.getMaxStackSize() - itemstack5.getCount(), itemstack3.getCount());
                        ItemStack itemstack4 = slot1.remove(l);
                        itemstack5.grow(l);
                        if (itemstack4.isEmpty()) {
                           slot1.set(ItemStack.EMPTY);
                        }

                        slot1.onTake(pPlayer, itemstack4);
                     }
                  }
               }
            }
         }

         this.broadcastChanges();
      }

      return itemstack;
   }

   public static boolean consideredTheSameItem(ItemStack pStack1, ItemStack pStack2) {
      return pStack1.getItem() == pStack2.getItem() && ItemStack.tagMatches(pStack1, pStack2);
   }

   /**
    * Called to determine if the current slot is valid for the stack merging (double-click) code. The stack passed in is
    * null for the initial slot that was double-clicked.
    */
   public boolean canTakeItemForPickAll(ItemStack pStack, Slot pSlot) {
      return true;
   }

   /**
    * Called when the container is closed.
    */
   public void removed(PlayerEntity pPlayer) {
      PlayerInventory playerinventory = pPlayer.inventory;
      if (!playerinventory.getCarried().isEmpty()) {
         pPlayer.drop(playerinventory.getCarried(), false);
         playerinventory.setCarried(ItemStack.EMPTY);
      }

   }

   protected void clearContainer(PlayerEntity pPlayer, World pLevel, IInventory pContainer) {
      if (!pPlayer.isAlive() || pPlayer instanceof ServerPlayerEntity && ((ServerPlayerEntity)pPlayer).hasDisconnected()) {
         for(int j = 0; j < pContainer.getContainerSize(); ++j) {
            pPlayer.drop(pContainer.removeItemNoUpdate(j), false);
         }

      } else {
         for(int i = 0; i < pContainer.getContainerSize(); ++i) {
            pPlayer.inventory.placeItemBackInInventory(pLevel, pContainer.removeItemNoUpdate(i));
         }

      }
   }

   /**
    * Callback for when the crafting matrix is changed.
    */
   public void slotsChanged(IInventory pInventory) {
      this.broadcastChanges();
   }

   /**
    * Puts an ItemStack in a slot.
    */
   public void setItem(int pSlotID, ItemStack pStack) {
      this.getSlot(pSlotID).set(pStack);
   }

   @OnlyIn(Dist.CLIENT)
   public void setAll(List<ItemStack> pStacks) {
      for(int i = 0; i < pStacks.size(); ++i) {
         this.getSlot(i).set(pStacks.get(i));
      }

   }

   public void setData(int pId, int pData) {
      this.dataSlots.get(pId).set(pData);
   }

   @OnlyIn(Dist.CLIENT)
   public short backup(PlayerInventory pPlayerInventory) {
      ++this.changeUid;
      return this.changeUid;
   }

   public boolean isSynched(PlayerEntity pPlayer) {
      return !this.unSynchedPlayers.contains(pPlayer);
   }

   public void setSynched(PlayerEntity pPlayer, boolean pIsSynched) {
      if (pIsSynched) {
         this.unSynchedPlayers.remove(pPlayer);
      } else {
         this.unSynchedPlayers.add(pPlayer);
      }

   }

   /**
    * Determines whether supplied player can use this container
    */
   public abstract boolean stillValid(PlayerEntity pPlayer);

   /**
    * Merges provided ItemStack with the first avaliable one in the container/player inventor between minIndex
    * (included) and maxIndex (excluded). Args : stack, minIndex, maxIndex, negativDirection. [!] the Container
    * implementation do not check if the item is valid for the slot
    */
   protected boolean moveItemStackTo(ItemStack pStack, int pStartIndex, int pEndIndex, boolean pReverseDirection) {
      boolean flag = false;
      int i = pStartIndex;
      if (pReverseDirection) {
         i = pEndIndex - 1;
      }

      if (pStack.isStackable()) {
         while(!pStack.isEmpty()) {
            if (pReverseDirection) {
               if (i < pStartIndex) {
                  break;
               }
            } else if (i >= pEndIndex) {
               break;
            }

            Slot slot = this.slots.get(i);
            ItemStack itemstack = slot.getItem();
            if (!itemstack.isEmpty() && consideredTheSameItem(pStack, itemstack)) {
               int j = itemstack.getCount() + pStack.getCount();
               int maxSize = Math.min(slot.getMaxStackSize(), pStack.getMaxStackSize());
               if (j <= maxSize) {
                  pStack.setCount(0);
                  itemstack.setCount(j);
                  slot.setChanged();
                  flag = true;
               } else if (itemstack.getCount() < maxSize) {
                  pStack.shrink(maxSize - itemstack.getCount());
                  itemstack.setCount(maxSize);
                  slot.setChanged();
                  flag = true;
               }
            }

            if (pReverseDirection) {
               --i;
            } else {
               ++i;
            }
         }
      }

      if (!pStack.isEmpty()) {
         if (pReverseDirection) {
            i = pEndIndex - 1;
         } else {
            i = pStartIndex;
         }

         while(true) {
            if (pReverseDirection) {
               if (i < pStartIndex) {
                  break;
               }
            } else if (i >= pEndIndex) {
               break;
            }

            Slot slot1 = this.slots.get(i);
            ItemStack itemstack1 = slot1.getItem();
            if (itemstack1.isEmpty() && slot1.mayPlace(pStack)) {
               if (pStack.getCount() > slot1.getMaxStackSize()) {
                  slot1.set(pStack.split(slot1.getMaxStackSize()));
               } else {
                  slot1.set(pStack.split(pStack.getCount()));
               }

               slot1.setChanged();
               flag = true;
               break;
            }

            if (pReverseDirection) {
               --i;
            } else {
               ++i;
            }
         }
      }

      return flag;
   }

   /**
    * Extracts the drag mode. Args : eventButton. Return (0 : evenly split, 1 : one item by slot, 2 : not used ?)
    */
   public static int getQuickcraftType(int pEventButton) {
      return pEventButton >> 2 & 3;
   }

   /**
    * Args : clickedButton, Returns (0 : start drag, 1 : add slot, 2 : end drag)
    */
   public static int getQuickcraftHeader(int pClickedButton) {
      return pClickedButton & 3;
   }

   @OnlyIn(Dist.CLIENT)
   public static int getQuickcraftMask(int pQuickCraftingHeader, int pQuickCraftingType) {
      return pQuickCraftingHeader & 3 | (pQuickCraftingType & 3) << 2;
   }

   public static boolean isValidQuickcraftType(int pDragMode, PlayerEntity pPlayer) {
      if (pDragMode == 0) {
         return true;
      } else if (pDragMode == 1) {
         return true;
      } else {
         return pDragMode == 2 && pPlayer.abilities.instabuild;
      }
   }

   /**
    * Reset the drag fields
    */
   protected void resetQuickCraft() {
      this.quickcraftStatus = 0;
      this.quickcraftSlots.clear();
   }

   /**
    * Checks if it's possible to add the given itemstack to the given slot.
    */
   public static boolean canItemQuickReplace(@Nullable Slot pSlot, ItemStack pStack, boolean pStackSizeMatters) {
      boolean flag = pSlot == null || !pSlot.hasItem();
      if (!flag && pStack.sameItem(pSlot.getItem()) && ItemStack.tagMatches(pSlot.getItem(), pStack)) {
         return pSlot.getItem().getCount() + (pStackSizeMatters ? 0 : pStack.getCount()) <= pStack.getMaxStackSize();
      } else {
         return flag;
      }
   }

   /**
    * Compute the new stack size, Returns the stack with the new size. Args : dragSlots, dragMode, dragStack,
    * slotStackSize
    */
   public static void getQuickCraftSlotCount(Set<Slot> pDragSlots, int pDragMode, ItemStack pStack, int pSlotStackSize) {
      switch(pDragMode) {
      case 0:
         pStack.setCount(MathHelper.floor((float)pStack.getCount() / (float)pDragSlots.size()));
         break;
      case 1:
         pStack.setCount(1);
         break;
      case 2:
         pStack.setCount(pStack.getMaxStackSize());
      }

      pStack.grow(pSlotStackSize);
   }

   /**
    * Returns true if the player can "drag-spilt" items into this slot. Returns true by default. Called to check if the
    * slot can be added to a list of Slots to split the held ItemStack across.
    */
   public boolean canDragTo(Slot pSlot) {
      return true;
   }

   /**
    * Like the version that takes an inventory. If the given TileEntity is not an Inventory, 0 is returned instead.
    */
   public static int getRedstoneSignalFromBlockEntity(@Nullable TileEntity pTe) {
      return pTe instanceof IInventory ? getRedstoneSignalFromContainer((IInventory)pTe) : 0;
   }

   public static int getRedstoneSignalFromContainer(@Nullable IInventory pInv) {
      if (pInv == null) {
         return 0;
      } else {
         int i = 0;
         float f = 0.0F;

         for(int j = 0; j < pInv.getContainerSize(); ++j) {
            ItemStack itemstack = pInv.getItem(j);
            if (!itemstack.isEmpty()) {
               f += (float)itemstack.getCount() / (float)Math.min(pInv.getMaxStackSize(), itemstack.getMaxStackSize());
               ++i;
            }
         }

         f = f / (float)pInv.getContainerSize();
         return MathHelper.floor(f * 14.0F) + (i > 0 ? 1 : 0);
      }
   }
}
