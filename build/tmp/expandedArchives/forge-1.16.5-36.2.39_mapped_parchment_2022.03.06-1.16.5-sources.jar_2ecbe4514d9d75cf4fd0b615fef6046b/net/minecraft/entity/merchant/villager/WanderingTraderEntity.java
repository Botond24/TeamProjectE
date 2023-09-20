package net.minecraft.entity.merchant.villager;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtCustomerGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookAtWithoutMovingGoal;
import net.minecraft.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.entity.ai.goal.PanicGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.entity.ai.goal.UseItemGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.monster.EvokerEntity;
import net.minecraft.entity.monster.IllusionerEntity;
import net.minecraft.entity.monster.PillagerEntity;
import net.minecraft.entity.monster.VexEntity;
import net.minecraft.entity.monster.VindicatorEntity;
import net.minecraft.entity.monster.ZoglinEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class WanderingTraderEntity extends AbstractVillagerEntity {
   @Nullable
   private BlockPos wanderTarget;
   private int despawnDelay;

   public WanderingTraderEntity(EntityType<? extends WanderingTraderEntity> p_i50178_1_, World p_i50178_2_) {
      super(p_i50178_1_, p_i50178_2_);
      this.forcedLoading = true;
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(0, new SwimGoal(this));
      this.goalSelector.addGoal(0, new UseItemGoal<>(this, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.INVISIBILITY), SoundEvents.WANDERING_TRADER_DISAPPEARED, (p_213733_1_) -> {
         return this.level.isNight() && !p_213733_1_.isInvisible();
      }));
      this.goalSelector.addGoal(0, new UseItemGoal<>(this, new ItemStack(Items.MILK_BUCKET), SoundEvents.WANDERING_TRADER_REAPPEARED, (p_213736_1_) -> {
         return this.level.isDay() && p_213736_1_.isInvisible();
      }));
      this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
      this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, ZombieEntity.class, 8.0F, 0.5D, 0.5D));
      this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, EvokerEntity.class, 12.0F, 0.5D, 0.5D));
      this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, VindicatorEntity.class, 8.0F, 0.5D, 0.5D));
      this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, VexEntity.class, 8.0F, 0.5D, 0.5D));
      this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, PillagerEntity.class, 15.0F, 0.5D, 0.5D));
      this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, IllusionerEntity.class, 12.0F, 0.5D, 0.5D));
      this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, ZoglinEntity.class, 10.0F, 0.5D, 0.5D));
      this.goalSelector.addGoal(1, new PanicGoal(this, 0.5D));
      this.goalSelector.addGoal(1, new LookAtCustomerGoal(this));
      this.goalSelector.addGoal(2, new WanderingTraderEntity.MoveToGoal(this, 2.0D, 0.35D));
      this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 0.35D));
      this.goalSelector.addGoal(8, new WaterAvoidingRandomWalkingGoal(this, 0.35D));
      this.goalSelector.addGoal(9, new LookAtWithoutMovingGoal(this, PlayerEntity.class, 3.0F, 1.0F));
      this.goalSelector.addGoal(10, new LookAtGoal(this, MobEntity.class, 8.0F));
   }

   @Nullable
   public AgeableEntity getBreedOffspring(ServerWorld pServerLevel, AgeableEntity pMate) {
      return null;
   }

   public boolean showProgressBar() {
      return false;
   }

   public ActionResultType mobInteract(PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (itemstack.getItem() != Items.VILLAGER_SPAWN_EGG && this.isAlive() && !this.isTrading() && !this.isBaby()) {
         if (pHand == Hand.MAIN_HAND) {
            pPlayer.awardStat(Stats.TALKED_TO_VILLAGER);
         }

         if (this.getOffers().isEmpty()) {
            return ActionResultType.sidedSuccess(this.level.isClientSide);
         } else {
            if (!this.level.isClientSide) {
               this.setTradingPlayer(pPlayer);
               this.openTradingScreen(pPlayer, this.getDisplayName(), 1);
            }

            return ActionResultType.sidedSuccess(this.level.isClientSide);
         }
      } else {
         return super.mobInteract(pPlayer, pHand);
      }
   }

   protected void updateTrades() {
      VillagerTrades.ITrade[] avillagertrades$itrade = VillagerTrades.WANDERING_TRADER_TRADES.get(1);
      VillagerTrades.ITrade[] avillagertrades$itrade1 = VillagerTrades.WANDERING_TRADER_TRADES.get(2);
      if (avillagertrades$itrade != null && avillagertrades$itrade1 != null) {
         MerchantOffers merchantoffers = this.getOffers();
         this.addOffersFromItemListings(merchantoffers, avillagertrades$itrade, 5);
         int i = this.random.nextInt(avillagertrades$itrade1.length);
         VillagerTrades.ITrade villagertrades$itrade = avillagertrades$itrade1[i];
         MerchantOffer merchantoffer = villagertrades$itrade.getOffer(this, this.random);
         if (merchantoffer != null) {
            merchantoffers.add(merchantoffer);
         }

      }
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putInt("DespawnDelay", this.despawnDelay);
      if (this.wanderTarget != null) {
         pCompound.put("WanderTarget", NBTUtil.writeBlockPos(this.wanderTarget));
      }

   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.contains("DespawnDelay", 99)) {
         this.despawnDelay = pCompound.getInt("DespawnDelay");
      }

      if (pCompound.contains("WanderTarget")) {
         this.wanderTarget = NBTUtil.readBlockPos(pCompound.getCompound("WanderTarget"));
      }

      this.setAge(Math.max(0, this.getAge()));
   }

   public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
      return false;
   }

   protected void rewardTradeXp(MerchantOffer pOffer) {
      if (pOffer.shouldRewardExp()) {
         int i = 3 + this.random.nextInt(4);
         this.level.addFreshEntity(new ExperienceOrbEntity(this.level, this.getX(), this.getY() + 0.5D, this.getZ(), i));
      }

   }

   protected SoundEvent getAmbientSound() {
      return this.isTrading() ? SoundEvents.WANDERING_TRADER_TRADE : SoundEvents.WANDERING_TRADER_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.WANDERING_TRADER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.WANDERING_TRADER_DEATH;
   }

   protected SoundEvent getDrinkingSound(ItemStack pStack) {
      Item item = pStack.getItem();
      return item == Items.MILK_BUCKET ? SoundEvents.WANDERING_TRADER_DRINK_MILK : SoundEvents.WANDERING_TRADER_DRINK_POTION;
   }

   protected SoundEvent getTradeUpdatedSound(boolean pGetYesSound) {
      return pGetYesSound ? SoundEvents.WANDERING_TRADER_YES : SoundEvents.WANDERING_TRADER_NO;
   }

   public SoundEvent getNotifyTradeSound() {
      return SoundEvents.WANDERING_TRADER_YES;
   }

   public void setDespawnDelay(int pDespawnDelay) {
      this.despawnDelay = pDespawnDelay;
   }

   public int getDespawnDelay() {
      return this.despawnDelay;
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      super.aiStep();
      if (!this.level.isClientSide) {
         this.maybeDespawn();
      }

   }

   private void maybeDespawn() {
      if (this.despawnDelay > 0 && !this.isTrading() && --this.despawnDelay == 0) {
         this.remove();
      }

   }

   public void setWanderTarget(@Nullable BlockPos pWanderTarget) {
      this.wanderTarget = pWanderTarget;
   }

   @Nullable
   private BlockPos getWanderTarget() {
      return this.wanderTarget;
   }

   class MoveToGoal extends Goal {
      final WanderingTraderEntity trader;
      final double stopDistance;
      final double speedModifier;

      MoveToGoal(WanderingTraderEntity pTrader, double pStopDistance, double pSpeedModifier) {
         this.trader = pTrader;
         this.stopDistance = pStopDistance;
         this.speedModifier = pSpeedModifier;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         this.trader.setWanderTarget((BlockPos)null);
         WanderingTraderEntity.this.navigation.stop();
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         BlockPos blockpos = this.trader.getWanderTarget();
         return blockpos != null && this.isTooFarAway(blockpos, this.stopDistance);
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         BlockPos blockpos = this.trader.getWanderTarget();
         if (blockpos != null && WanderingTraderEntity.this.navigation.isDone()) {
            if (this.isTooFarAway(blockpos, 10.0D)) {
               Vector3d vector3d = (new Vector3d((double)blockpos.getX() - this.trader.getX(), (double)blockpos.getY() - this.trader.getY(), (double)blockpos.getZ() - this.trader.getZ())).normalize();
               Vector3d vector3d1 = vector3d.scale(10.0D).add(this.trader.getX(), this.trader.getY(), this.trader.getZ());
               WanderingTraderEntity.this.navigation.moveTo(vector3d1.x, vector3d1.y, vector3d1.z, this.speedModifier);
            } else {
               WanderingTraderEntity.this.navigation.moveTo((double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), this.speedModifier);
            }
         }

      }

      private boolean isTooFarAway(BlockPos pPos, double pDistance) {
         return !pPos.closerThan(this.trader.position(), pDistance);
      }
   }
}