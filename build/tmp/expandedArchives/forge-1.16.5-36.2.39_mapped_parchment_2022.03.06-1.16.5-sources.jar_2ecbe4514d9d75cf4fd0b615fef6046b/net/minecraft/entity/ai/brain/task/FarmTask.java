package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.memory.WalkTarget;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPosWrapper;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;

public class FarmTask extends Task<VillagerEntity> {
   @Nullable
   private BlockPos aboveFarmlandPos;
   private long nextOkStartTime;
   private int timeWorkedSoFar;
   private final List<BlockPos> validFarmlandAroundVillager = Lists.newArrayList();

   public FarmTask() {
      super(ImmutableMap.of(MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.VALUE_ABSENT, MemoryModuleType.WALK_TARGET, MemoryModuleStatus.VALUE_ABSENT, MemoryModuleType.SECONDARY_JOB_SITE, MemoryModuleStatus.VALUE_PRESENT));
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, VillagerEntity pOwner) {
      if (!net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(pLevel, pOwner)) {
         return false;
      } else if (pOwner.getVillagerData().getProfession() != VillagerProfession.FARMER) {
         return false;
      } else {
         BlockPos.Mutable blockpos$mutable = pOwner.blockPosition().mutable();
         this.validFarmlandAroundVillager.clear();

         for(int i = -1; i <= 1; ++i) {
            for(int j = -1; j <= 1; ++j) {
               for(int k = -1; k <= 1; ++k) {
                  blockpos$mutable.set(pOwner.getX() + (double)i, pOwner.getY() + (double)j, pOwner.getZ() + (double)k);
                  if (this.validPos(blockpos$mutable, pLevel)) {
                     this.validFarmlandAroundVillager.add(new BlockPos(blockpos$mutable));
                  }
               }
            }
         }

         this.aboveFarmlandPos = this.getValidFarmland(pLevel);
         return this.aboveFarmlandPos != null;
      }
   }

   @Nullable
   private BlockPos getValidFarmland(ServerWorld pServerLevel) {
      return this.validFarmlandAroundVillager.isEmpty() ? null : this.validFarmlandAroundVillager.get(pServerLevel.getRandom().nextInt(this.validFarmlandAroundVillager.size()));
   }

   private boolean validPos(BlockPos pPos, ServerWorld pServerLevel) {
      BlockState blockstate = pServerLevel.getBlockState(pPos);
      Block block = blockstate.getBlock();
      Block block1 = pServerLevel.getBlockState(pPos.below()).getBlock();
      return block instanceof CropsBlock && ((CropsBlock)block).isMaxAge(blockstate) || blockstate.isAir() && block1 instanceof FarmlandBlock;
   }

   protected void start(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      if (pGameTime > this.nextOkStartTime && this.aboveFarmlandPos != null) {
         pEntity.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosWrapper(this.aboveFarmlandPos));
         pEntity.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosWrapper(this.aboveFarmlandPos), 0.5F, 1));
      }

   }

   protected void stop(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      pEntity.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
      pEntity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      this.timeWorkedSoFar = 0;
      this.nextOkStartTime = pGameTime + 40L;
   }

   protected void tick(ServerWorld pLevel, VillagerEntity pOwner, long pGameTime) {
      if (this.aboveFarmlandPos == null || this.aboveFarmlandPos.closerThan(pOwner.position(), 1.0D)) {
         if (this.aboveFarmlandPos != null && pGameTime > this.nextOkStartTime) {
            BlockState blockstate = pLevel.getBlockState(this.aboveFarmlandPos);
            Block block = blockstate.getBlock();
            Block block1 = pLevel.getBlockState(this.aboveFarmlandPos.below()).getBlock();
            if (block instanceof CropsBlock && ((CropsBlock)block).isMaxAge(blockstate)) {
               pLevel.destroyBlock(this.aboveFarmlandPos, true, pOwner);
            }

            if (blockstate.isAir() && block1 instanceof FarmlandBlock && pOwner.hasFarmSeeds()) {
               Inventory inventory = pOwner.getInventory();

               for(int i = 0; i < inventory.getContainerSize(); ++i) {
                  ItemStack itemstack = inventory.getItem(i);
                  boolean flag = false;
                  if (!itemstack.isEmpty()) {
                     if (itemstack.getItem() == Items.WHEAT_SEEDS) {
                        pLevel.setBlock(this.aboveFarmlandPos, Blocks.WHEAT.defaultBlockState(), 3);
                        flag = true;
                     } else if (itemstack.getItem() == Items.POTATO) {
                        pLevel.setBlock(this.aboveFarmlandPos, Blocks.POTATOES.defaultBlockState(), 3);
                        flag = true;
                     } else if (itemstack.getItem() == Items.CARROT) {
                        pLevel.setBlock(this.aboveFarmlandPos, Blocks.CARROTS.defaultBlockState(), 3);
                        flag = true;
                     } else if (itemstack.getItem() == Items.BEETROOT_SEEDS) {
                        pLevel.setBlock(this.aboveFarmlandPos, Blocks.BEETROOTS.defaultBlockState(), 3);
                        flag = true;
                     } else if (itemstack.getItem() instanceof net.minecraftforge.common.IPlantable) {
                        if (((net.minecraftforge.common.IPlantable)itemstack.getItem()).getPlantType(pLevel, aboveFarmlandPos) == net.minecraftforge.common.PlantType.CROP) {
                           pLevel.setBlock(aboveFarmlandPos, ((net.minecraftforge.common.IPlantable)itemstack.getItem()).getPlant(pLevel, aboveFarmlandPos), 3);
                           flag = true;
                        }
                     }
                  }

                  if (flag) {
                     pLevel.playSound((PlayerEntity)null, (double)this.aboveFarmlandPos.getX(), (double)this.aboveFarmlandPos.getY(), (double)this.aboveFarmlandPos.getZ(), SoundEvents.CROP_PLANTED, SoundCategory.BLOCKS, 1.0F, 1.0F);
                     itemstack.shrink(1);
                     if (itemstack.isEmpty()) {
                        inventory.setItem(i, ItemStack.EMPTY);
                     }
                     break;
                  }
               }
            }

            if (block instanceof CropsBlock && !((CropsBlock)block).isMaxAge(blockstate)) {
               this.validFarmlandAroundVillager.remove(this.aboveFarmlandPos);
               this.aboveFarmlandPos = this.getValidFarmland(pLevel);
               if (this.aboveFarmlandPos != null) {
                  this.nextOkStartTime = pGameTime + 20L;
                  pOwner.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosWrapper(this.aboveFarmlandPos), 0.5F, 1));
                  pOwner.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosWrapper(this.aboveFarmlandPos));
               }
            }
         }

         ++this.timeWorkedSoFar;
      }
   }

   protected boolean canStillUse(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      return this.timeWorkedSoFar < 200;
   }
}
