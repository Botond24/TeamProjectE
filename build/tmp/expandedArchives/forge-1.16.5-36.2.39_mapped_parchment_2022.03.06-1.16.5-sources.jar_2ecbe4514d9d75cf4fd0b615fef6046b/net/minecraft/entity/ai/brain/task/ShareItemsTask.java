package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.server.ServerWorld;

public class ShareItemsTask extends Task<VillagerEntity> {
   private Set<Item> trades = ImmutableSet.of();

   public ShareItemsTask() {
      super(ImmutableMap.of(MemoryModuleType.INTERACTION_TARGET, MemoryModuleStatus.VALUE_PRESENT, MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryModuleStatus.VALUE_PRESENT));
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, VillagerEntity pOwner) {
      return BrainUtil.targetIsValid(pOwner.getBrain(), MemoryModuleType.INTERACTION_TARGET, EntityType.VILLAGER);
   }

   protected boolean canStillUse(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      return this.checkExtraStartConditions(pLevel, pEntity);
   }

   protected void start(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      VillagerEntity villagerentity = (VillagerEntity)pEntity.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
      BrainUtil.lockGazeAndWalkToEachOther(pEntity, villagerentity, 0.5F);
      this.trades = figureOutWhatIAmWillingToTrade(pEntity, villagerentity);
   }

   protected void tick(ServerWorld pLevel, VillagerEntity pOwner, long pGameTime) {
      VillagerEntity villagerentity = (VillagerEntity)pOwner.getBrain().getMemory(MemoryModuleType.INTERACTION_TARGET).get();
      if (!(pOwner.distanceToSqr(villagerentity) > 5.0D)) {
         BrainUtil.lockGazeAndWalkToEachOther(pOwner, villagerentity, 0.5F);
         pOwner.gossip(pLevel, villagerentity, pGameTime);
         if (pOwner.hasExcessFood() && (pOwner.getVillagerData().getProfession() == VillagerProfession.FARMER || villagerentity.wantsMoreFood())) {
            throwHalfStack(pOwner, VillagerEntity.FOOD_POINTS.keySet(), villagerentity);
         }

         if (villagerentity.getVillagerData().getProfession() == VillagerProfession.FARMER && pOwner.getInventory().countItem(Items.WHEAT) > Items.WHEAT.getMaxStackSize() / 2) {
            throwHalfStack(pOwner, ImmutableSet.of(Items.WHEAT), villagerentity);
         }

         if (!this.trades.isEmpty() && pOwner.getInventory().hasAnyOf(this.trades)) {
            throwHalfStack(pOwner, this.trades, villagerentity);
         }

      }
   }

   protected void stop(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      pEntity.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
   }

   private static Set<Item> figureOutWhatIAmWillingToTrade(VillagerEntity p_220585_0_, VillagerEntity p_220585_1_) {
      ImmutableSet<Item> immutableset = p_220585_1_.getVillagerData().getProfession().getRequestedItems();
      ImmutableSet<Item> immutableset1 = p_220585_0_.getVillagerData().getProfession().getRequestedItems();
      return immutableset.stream().filter((p_220587_1_) -> {
         return !immutableset1.contains(p_220587_1_);
      }).collect(Collectors.toSet());
   }

   private static void throwHalfStack(VillagerEntity p_220586_0_, Set<Item> p_220586_1_, LivingEntity p_220586_2_) {
      Inventory inventory = p_220586_0_.getInventory();
      ItemStack itemstack = ItemStack.EMPTY;
      int i = 0;

      while(i < inventory.getContainerSize()) {
         ItemStack itemstack1;
         Item item;
         int j;
         label28: {
            itemstack1 = inventory.getItem(i);
            if (!itemstack1.isEmpty()) {
               item = itemstack1.getItem();
               if (p_220586_1_.contains(item)) {
                  if (itemstack1.getCount() > itemstack1.getMaxStackSize() / 2) {
                     j = itemstack1.getCount() / 2;
                     break label28;
                  }

                  if (itemstack1.getCount() > 24) {
                     j = itemstack1.getCount() - 24;
                     break label28;
                  }
               }
            }

            ++i;
            continue;
         }

         itemstack1.shrink(j);
         itemstack = new ItemStack(item, j);
         break;
      }

      if (!itemstack.isEmpty()) {
         BrainUtil.throwItem(p_220586_0_, itemstack, p_220586_2_.position());
      }

   }
}