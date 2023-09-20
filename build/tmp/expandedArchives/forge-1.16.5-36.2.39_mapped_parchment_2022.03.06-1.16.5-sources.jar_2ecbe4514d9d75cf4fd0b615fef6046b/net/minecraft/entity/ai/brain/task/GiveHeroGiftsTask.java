package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.BrainUtil;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class GiveHeroGiftsTask extends Task<VillagerEntity> {
   private static final Map<VillagerProfession, ResourceLocation> gifts = Util.make(Maps.newHashMap(), (p_220395_0_) -> {
      p_220395_0_.put(VillagerProfession.ARMORER, LootTables.ARMORER_GIFT);
      p_220395_0_.put(VillagerProfession.BUTCHER, LootTables.BUTCHER_GIFT);
      p_220395_0_.put(VillagerProfession.CARTOGRAPHER, LootTables.CARTOGRAPHER_GIFT);
      p_220395_0_.put(VillagerProfession.CLERIC, LootTables.CLERIC_GIFT);
      p_220395_0_.put(VillagerProfession.FARMER, LootTables.FARMER_GIFT);
      p_220395_0_.put(VillagerProfession.FISHERMAN, LootTables.FISHERMAN_GIFT);
      p_220395_0_.put(VillagerProfession.FLETCHER, LootTables.FLETCHER_GIFT);
      p_220395_0_.put(VillagerProfession.LEATHERWORKER, LootTables.LEATHERWORKER_GIFT);
      p_220395_0_.put(VillagerProfession.LIBRARIAN, LootTables.LIBRARIAN_GIFT);
      p_220395_0_.put(VillagerProfession.MASON, LootTables.MASON_GIFT);
      p_220395_0_.put(VillagerProfession.SHEPHERD, LootTables.SHEPHERD_GIFT);
      p_220395_0_.put(VillagerProfession.TOOLSMITH, LootTables.TOOLSMITH_GIFT);
      p_220395_0_.put(VillagerProfession.WEAPONSMITH, LootTables.WEAPONSMITH_GIFT);
   });
   private int timeUntilNextGift = 600;
   private boolean giftGivenDuringThisRun;
   private long timeSinceStart;

   public GiveHeroGiftsTask(int p_i50366_1_) {
      super(ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryModuleStatus.REGISTERED, MemoryModuleType.LOOK_TARGET, MemoryModuleStatus.REGISTERED, MemoryModuleType.INTERACTION_TARGET, MemoryModuleStatus.REGISTERED, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleStatus.VALUE_PRESENT), p_i50366_1_);
   }

   protected boolean checkExtraStartConditions(ServerWorld pLevel, VillagerEntity pOwner) {
      if (!this.isHeroVisible(pOwner)) {
         return false;
      } else if (this.timeUntilNextGift > 0) {
         --this.timeUntilNextGift;
         return false;
      } else {
         return true;
      }
   }

   protected void start(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      this.giftGivenDuringThisRun = false;
      this.timeSinceStart = pGameTime;
      PlayerEntity playerentity = this.getNearestTargetableHero(pEntity).get();
      pEntity.getBrain().setMemory(MemoryModuleType.INTERACTION_TARGET, playerentity);
      BrainUtil.lookAtEntity(pEntity, playerentity);
   }

   protected boolean canStillUse(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      return this.isHeroVisible(pEntity) && !this.giftGivenDuringThisRun;
   }

   protected void tick(ServerWorld pLevel, VillagerEntity pOwner, long pGameTime) {
      PlayerEntity playerentity = this.getNearestTargetableHero(pOwner).get();
      BrainUtil.lookAtEntity(pOwner, playerentity);
      if (this.isWithinThrowingDistance(pOwner, playerentity)) {
         if (pGameTime - this.timeSinceStart > 20L) {
            this.throwGift(pOwner, playerentity);
            this.giftGivenDuringThisRun = true;
         }
      } else {
         BrainUtil.setWalkAndLookTargetMemories(pOwner, playerentity, 0.5F, 5);
      }

   }

   protected void stop(ServerWorld pLevel, VillagerEntity pEntity, long pGameTime) {
      this.timeUntilNextGift = calculateTimeUntilNextGift(pLevel);
      pEntity.getBrain().eraseMemory(MemoryModuleType.INTERACTION_TARGET);
      pEntity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
      pEntity.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
   }

   private void throwGift(VillagerEntity pVillager, LivingEntity pHero) {
      for(ItemStack itemstack : this.getItemToThrow(pVillager)) {
         BrainUtil.throwItem(pVillager, itemstack, pHero.position());
      }

   }

   private List<ItemStack> getItemToThrow(VillagerEntity pVillager) {
      if (pVillager.isBaby()) {
         return ImmutableList.of(new ItemStack(Items.POPPY));
      } else {
         VillagerProfession villagerprofession = pVillager.getVillagerData().getProfession();
         if (gifts.containsKey(villagerprofession)) {
            LootTable loottable = pVillager.level.getServer().getLootTables().get(gifts.get(villagerprofession));
            LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerWorld)pVillager.level)).withParameter(LootParameters.ORIGIN, pVillager.position()).withParameter(LootParameters.THIS_ENTITY, pVillager).withRandom(pVillager.getRandom());
            return loottable.getRandomItems(lootcontext$builder.create(LootParameterSets.GIFT));
         } else {
            return ImmutableList.of(new ItemStack(Items.WHEAT_SEEDS));
         }
      }
   }

   private boolean isHeroVisible(VillagerEntity pVillager) {
      return this.getNearestTargetableHero(pVillager).isPresent();
   }

   private Optional<PlayerEntity> getNearestTargetableHero(VillagerEntity pVillager) {
      return pVillager.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).filter(this::isHero);
   }

   private boolean isHero(PlayerEntity p_220402_1_) {
      return p_220402_1_.hasEffect(Effects.HERO_OF_THE_VILLAGE);
   }

   private boolean isWithinThrowingDistance(VillagerEntity pVillager, PlayerEntity pHero) {
      BlockPos blockpos = pHero.blockPosition();
      BlockPos blockpos1 = pVillager.blockPosition();
      return blockpos1.closerThan(blockpos, 5.0D);
   }

   private static int calculateTimeUntilNextGift(ServerWorld pLevel) {
      return 600 + pLevel.random.nextInt(6001);
   }
}