package net.minecraft.inventory;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;

public class ItemStackHelper {
   public static ItemStack removeItem(List<ItemStack> pStacks, int pIndex, int pAmount) {
      return pIndex >= 0 && pIndex < pStacks.size() && !pStacks.get(pIndex).isEmpty() && pAmount > 0 ? pStacks.get(pIndex).split(pAmount) : ItemStack.EMPTY;
   }

   public static ItemStack takeItem(List<ItemStack> pStacks, int pIndex) {
      return pIndex >= 0 && pIndex < pStacks.size() ? pStacks.set(pIndex, ItemStack.EMPTY) : ItemStack.EMPTY;
   }

   public static CompoundNBT saveAllItems(CompoundNBT pTag, NonNullList<ItemStack> pList) {
      return saveAllItems(pTag, pList, true);
   }

   public static CompoundNBT saveAllItems(CompoundNBT pTag, NonNullList<ItemStack> pList, boolean pSaveEmpty) {
      ListNBT listnbt = new ListNBT();

      for(int i = 0; i < pList.size(); ++i) {
         ItemStack itemstack = pList.get(i);
         if (!itemstack.isEmpty()) {
            CompoundNBT compoundnbt = new CompoundNBT();
            compoundnbt.putByte("Slot", (byte)i);
            itemstack.save(compoundnbt);
            listnbt.add(compoundnbt);
         }
      }

      if (!listnbt.isEmpty() || pSaveEmpty) {
         pTag.put("Items", listnbt);
      }

      return pTag;
   }

   public static void loadAllItems(CompoundNBT pTag, NonNullList<ItemStack> pList) {
      ListNBT listnbt = pTag.getList("Items", 10);

      for(int i = 0; i < listnbt.size(); ++i) {
         CompoundNBT compoundnbt = listnbt.getCompound(i);
         int j = compoundnbt.getByte("Slot") & 255;
         if (j >= 0 && j < pList.size()) {
            pList.set(j, ItemStack.of(compoundnbt));
         }
      }

   }

   /**
    * Clears items from the inventory matching a predicate.
    * @return The amount of items cleared
    * @param pMaxItems The maximum amount of items to be cleared. A negative value means unlimited and 0 means count how
    * many items are found that could be cleared.
    */
   public static int clearOrCountMatchingItems(IInventory pContainer, Predicate<ItemStack> pItemPredicate, int pMaxItems, boolean pSimulate) {
      int i = 0;

      for(int j = 0; j < pContainer.getContainerSize(); ++j) {
         ItemStack itemstack = pContainer.getItem(j);
         int k = clearOrCountMatchingItems(itemstack, pItemPredicate, pMaxItems - i, pSimulate);
         if (k > 0 && !pSimulate && itemstack.isEmpty()) {
            pContainer.setItem(j, ItemStack.EMPTY);
         }

         i += k;
      }

      return i;
   }

   public static int clearOrCountMatchingItems(ItemStack pStack, Predicate<ItemStack> pItemPredicate, int pMaxItems, boolean pSimulate) {
      if (!pStack.isEmpty() && pItemPredicate.test(pStack)) {
         if (pSimulate) {
            return pStack.getCount();
         } else {
            int i = pMaxItems < 0 ? pStack.getCount() : Math.min(pMaxItems, pStack.getCount());
            pStack.shrink(i);
            return i;
         }
      } else {
         return 0;
      }
   }
}