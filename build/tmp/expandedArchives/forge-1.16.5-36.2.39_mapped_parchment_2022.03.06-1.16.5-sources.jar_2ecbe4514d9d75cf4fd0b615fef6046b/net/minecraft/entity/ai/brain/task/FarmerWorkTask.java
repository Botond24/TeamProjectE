package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComposterBlock;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.server.ServerWorld;

public class FarmerWorkTask extends SpawnGolemTask {
   private static final List<Item> COMPOSTABLE_ITEMS = ImmutableList.of(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS);

   protected void useWorkstation(ServerWorld pLevel, VillagerEntity pVillager) {
      Optional<GlobalPos> optional = pVillager.getBrain().getMemory(MemoryModuleType.JOB_SITE);
      if (optional.isPresent()) {
         GlobalPos globalpos = optional.get();
         BlockState blockstate = pLevel.getBlockState(globalpos.pos());
         if (blockstate.is(Blocks.COMPOSTER)) {
            this.makeBread(pVillager);
            this.compostItems(pLevel, pVillager, globalpos, blockstate);
         }

      }
   }

   private void compostItems(ServerWorld pLevel, VillagerEntity pVillager, GlobalPos p_234016_3_, BlockState pState) {
      BlockPos blockpos = p_234016_3_.pos();
      if (pState.getValue(ComposterBlock.LEVEL) == 8) {
         pState = ComposterBlock.extractProduce(pState, pLevel, blockpos);
      }

      int i = 20;
      int j = 10;
      int[] aint = new int[COMPOSTABLE_ITEMS.size()];
      Inventory inventory = pVillager.getInventory();
      int k = inventory.getContainerSize();
      BlockState blockstate = pState;

      for(int l = k - 1; l >= 0 && i > 0; --l) {
         ItemStack itemstack = inventory.getItem(l);
         int i1 = COMPOSTABLE_ITEMS.indexOf(itemstack.getItem());
         if (i1 != -1) {
            int j1 = itemstack.getCount();
            int k1 = aint[i1] + j1;
            aint[i1] = k1;
            int l1 = Math.min(Math.min(k1 - 10, i), j1);
            if (l1 > 0) {
               i -= l1;

               for(int i2 = 0; i2 < l1; ++i2) {
                  blockstate = ComposterBlock.insertItem(blockstate, pLevel, itemstack, blockpos);
                  if (blockstate.getValue(ComposterBlock.LEVEL) == 7) {
                     this.spawnComposterFillEffects(pLevel, pState, blockpos, blockstate);
                     return;
                  }
               }
            }
         }
      }

      this.spawnComposterFillEffects(pLevel, pState, blockpos, blockstate);
   }

   private void spawnComposterFillEffects(ServerWorld p_242308_1_, BlockState p_242308_2_, BlockPos p_242308_3_, BlockState p_242308_4_) {
      p_242308_1_.levelEvent(1500, p_242308_3_, p_242308_4_ != p_242308_2_ ? 1 : 0);
   }

   private void makeBread(VillagerEntity pVillager) {
      Inventory inventory = pVillager.getInventory();
      if (inventory.countItem(Items.BREAD) <= 36) {
         int i = inventory.countItem(Items.WHEAT);
         int j = 3;
         int k = 3;
         int l = Math.min(3, i / 3);
         if (l != 0) {
            int i1 = l * 3;
            inventory.removeItemType(Items.WHEAT, i1);
            ItemStack itemstack = inventory.addItem(new ItemStack(Items.BREAD, l));
            if (!itemstack.isEmpty()) {
               pVillager.spawnAtLocation(itemstack, 0.5F);
            }

         }
      }
   }
}