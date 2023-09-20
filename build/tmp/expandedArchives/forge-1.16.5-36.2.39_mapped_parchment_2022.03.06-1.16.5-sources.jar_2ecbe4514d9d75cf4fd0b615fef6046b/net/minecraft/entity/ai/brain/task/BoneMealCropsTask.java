package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPosWrapper;
import net.minecraft.world.server.ServerWorld;

public class BoneMealCropsTask extends Task<VillagerEntity> {
   private long nextWorkCycleTime;
   private long lastBonemealingSession;
   private int timeWorkedSoFar;
   private Optional<BlockPos> cropPos = Optional.empty();

   public BoneMealCropsTask() {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT));
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, VillagerEntity pOwner) {
      if (pOwner.tickCount % 10 == 0 && (this.lastBonemealingSession == 0L || this.lastBonemealingSession + 160L <= (long)pOwner.tickCount)) {
         if (pOwner.getInventory().countItem(Items.BONE_MEAL) <= 0) {
            return false;
         } else {
            this.cropPos = this.pickNextTarget(pLevel, pOwner);
            return this.cropPos.isPresent();
         }
      } else {
         return false;
      }
   }

   protected boolean canStillUse(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      return this.timeWorkedSoFar < 80 && this.cropPos.isPresent();
   }

   private Optional<BlockPos> pickNextTarget(ServerWorld pLevel, VillagerEntity pVillager) {
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
      Optional<BlockPos> optional = Optional.empty();
      int i = 0;

      for(int j = -1; j <= 1; ++j) {
         for(int k = -1; k <= 1; ++k) {
            for(int l = -1; l <= 1; ++l) {
               blockpos$mutable.setWithOffset(pVillager.blockPosition(), j, k, l);
               if (this.validPos(blockpos$mutable, pLevel)) {
                  ++i;
                  if (pLevel.random.nextInt(i) == 0) {
                     optional = Optional.of(blockpos$mutable.immutable());
                  }
               }
            }
         }
      }

      return optional;
   }

   private boolean validPos(BlockPos pPos, ServerWorld pLevel) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      Block block = blockstate.getBlock();
      return block instanceof CropsBlock && !((CropsBlock)block).isMaxAge(blockstate);
   }

   protected void start(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      this.setCurrentCropAsTarget(pEntity);
      pEntity.setItemSlot(EquipmentSlotType.MAINHAND, new ItemStack(Items.BONE_MEAL));
      this.nextWorkCycleTime = pGameTime;
      this.timeWorkedSoFar = 0;
   }

   private void setCurrentCropAsTarget(VillagerEntity pVillager) {
      this.cropPos.ifPresent((p_233995_1_) -> {
         BlockPosWrapper blockposwrapper = new BlockPosWrapper(p_233995_1_);
         pVillager.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, blockposwrapper);
         pVillager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(blockposwrapper, 0.5F, 1));
      });
   }

   protected void stop(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      pEntity.setItemSlot(EquipmentSlotType.MAINHAND, ItemStack.EMPTY);
      this.lastBonemealingSession = (long)pEntity.tickCount;
   }

   protected void tick(ServerWorld pLevel, VillagerEntity pOwner, long pGameTime) {
      BlockPos blockpos = this.cropPos.get();
      if (pGameTime >= this.nextWorkCycleTime && blockpos.closerThan(pOwner.position(), 1.0D)) {
         ItemStack itemstack = ItemStack.EMPTY;
         Inventory inventory = pOwner.getInventory();
         int i = inventory.getContainerSize();

         for(int j = 0; j < i; ++j) {
            ItemStack itemstack1 = inventory.getItem(j);
            if (itemstack1.getItem() == Items.BONE_MEAL) {
               itemstack = itemstack1;
               break;
            }
         }

         if (!itemstack.isEmpty() && BoneMealItem.growCrop(itemstack, pLevel, blockpos)) {
            pLevel.levelEvent(2005, blockpos, 0);
            this.cropPos = this.pickNextTarget(pLevel, pOwner);
            this.setCurrentCropAsTarget(pOwner);
            this.nextWorkCycleTime = pGameTime + 40L;
         }

         ++this.timeWorkedSoFar;
      }
   }
}