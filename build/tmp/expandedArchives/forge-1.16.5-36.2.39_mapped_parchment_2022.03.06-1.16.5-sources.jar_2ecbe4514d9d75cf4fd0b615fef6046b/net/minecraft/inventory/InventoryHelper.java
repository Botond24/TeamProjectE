package net.minecraft.inventory;

import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class InventoryHelper {
   private static final Random RANDOM = new Random();

   public static void dropContents(World pLevel, BlockPos pPos, IInventory pInventory) {
      dropContents(pLevel, (double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ(), pInventory);
   }

   public static void dropContents(World pLevel, Entity pEntityAt, IInventory pInventory) {
      dropContents(pLevel, pEntityAt.getX(), pEntityAt.getY(), pEntityAt.getZ(), pInventory);
   }

   private static void dropContents(World pLevel, double pX, double pY, double pZ, IInventory pInventory) {
      for(int i = 0; i < pInventory.getContainerSize(); ++i) {
         dropItemStack(pLevel, pX, pY, pZ, pInventory.getItem(i));
      }

   }

   public static void dropContents(World pLevel, BlockPos pPos, NonNullList<ItemStack> pStackList) {
      pStackList.forEach((p_219962_2_) -> {
         dropItemStack(pLevel, (double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ(), p_219962_2_);
      });
   }

   public static void dropItemStack(World pLevel, double pX, double pY, double pZ, ItemStack pStack) {
      double d0 = (double)EntityType.ITEM.getWidth();
      double d1 = 1.0D - d0;
      double d2 = d0 / 2.0D;
      double d3 = Math.floor(pX) + RANDOM.nextDouble() * d1 + d2;
      double d4 = Math.floor(pY) + RANDOM.nextDouble() * d1;
      double d5 = Math.floor(pZ) + RANDOM.nextDouble() * d1 + d2;

      while(!pStack.isEmpty()) {
         ItemEntity itementity = new ItemEntity(pLevel, d3, d4, d5, pStack.split(RANDOM.nextInt(21) + 10));
         float f = 0.05F;
         itementity.setDeltaMovement(RANDOM.nextGaussian() * (double)0.05F, RANDOM.nextGaussian() * (double)0.05F + (double)0.2F, RANDOM.nextGaussian() * (double)0.05F);
         pLevel.addFreshEntity(itementity);
      }

   }
}