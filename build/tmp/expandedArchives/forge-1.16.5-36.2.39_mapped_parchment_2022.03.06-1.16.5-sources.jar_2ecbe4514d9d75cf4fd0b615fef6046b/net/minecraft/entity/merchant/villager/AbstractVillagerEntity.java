package net.minecraft.entity.merchant.villager;

import com.google.common.collect.Sets;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.INPC;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractVillagerEntity extends AgeableEntity implements INPC, IMerchant {
   private static final DataParameter<Integer> DATA_UNHAPPY_COUNTER = EntityDataManager.defineId(AbstractVillagerEntity.class, DataSerializers.INT);
   @Nullable
   private PlayerEntity tradingPlayer;
   @Nullable
   protected MerchantOffers offers;
   private final Inventory inventory = new Inventory(8);

   public AbstractVillagerEntity(EntityType<? extends AbstractVillagerEntity> p_i50185_1_, World p_i50185_2_) {
      super(p_i50185_1_, p_i50185_2_);
      this.setPathfindingMalus(PathNodeType.DANGER_FIRE, 16.0F);
      this.setPathfindingMalus(PathNodeType.DAMAGE_FIRE, -1.0F);
   }

   public ILivingEntityData finalizeSpawn(IServerWorld pLevel, DifficultyInstance pDifficulty, SpawnReason pReason, @Nullable ILivingEntityData pSpawnData, @Nullable CompoundNBT pDataTag) {
      if (pSpawnData == null) {
         pSpawnData = new AgeableEntity.AgeableData(false);
      }

      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   public int getUnhappyCounter() {
      return this.entityData.get(DATA_UNHAPPY_COUNTER);
   }

   public void setUnhappyCounter(int pUnhappyCounter) {
      this.entityData.set(DATA_UNHAPPY_COUNTER, pUnhappyCounter);
   }

   public int getVillagerXp() {
      return 0;
   }

   protected float getStandingEyeHeight(Pose pPose, EntitySize pSize) {
      return this.isBaby() ? 0.81F : 1.62F;
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_UNHAPPY_COUNTER, 0);
   }

   public void setTradingPlayer(@Nullable PlayerEntity pTradingPlayer) {
      this.tradingPlayer = pTradingPlayer;
   }

   @Nullable
   public PlayerEntity getTradingPlayer() {
      return this.tradingPlayer;
   }

   public boolean isTrading() {
      return this.tradingPlayer != null;
   }

   public MerchantOffers getOffers() {
      if (this.offers == null) {
         this.offers = new MerchantOffers();
         this.updateTrades();
      }

      return this.offers;
   }

   @OnlyIn(Dist.CLIENT)
   public void overrideOffers(@Nullable MerchantOffers pOffers) {
   }

   public void overrideXp(int pXp) {
   }

   public void notifyTrade(MerchantOffer pOffer) {
      pOffer.increaseUses();
      this.ambientSoundTime = -this.getAmbientSoundInterval();
      this.rewardTradeXp(pOffer);
      if (this.tradingPlayer instanceof ServerPlayerEntity) {
         CriteriaTriggers.TRADE.trigger((ServerPlayerEntity)this.tradingPlayer, this, pOffer.getResult());
      }

   }

   protected abstract void rewardTradeXp(MerchantOffer pOffer);

   public boolean showProgressBar() {
      return true;
   }

   /**
    * Notifies the merchant of a possible merchantrecipe being fulfilled or not. Usually, this is just a sound byte
    * being played depending if the suggested itemstack is not null.
    */
   public void notifyTradeUpdated(ItemStack pStack) {
      if (!this.level.isClientSide && this.ambientSoundTime > -this.getAmbientSoundInterval() + 20) {
         this.ambientSoundTime = -this.getAmbientSoundInterval();
         this.playSound(this.getTradeUpdatedSound(!pStack.isEmpty()), this.getSoundVolume(), this.getVoicePitch());
      }

   }

   public SoundEvent getNotifyTradeSound() {
      return SoundEvents.VILLAGER_YES;
   }

   protected SoundEvent getTradeUpdatedSound(boolean pGetYesSound) {
      return pGetYesSound ? SoundEvents.VILLAGER_YES : SoundEvents.VILLAGER_NO;
   }

   public void playCelebrateSound() {
      this.playSound(SoundEvents.VILLAGER_CELEBRATE, this.getSoundVolume(), this.getVoicePitch());
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      MerchantOffers merchantoffers = this.getOffers();
      if (!merchantoffers.isEmpty()) {
         pCompound.put("Offers", merchantoffers.createTag());
      }

      pCompound.put("Inventory", this.inventory.createTag());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      if (pCompound.contains("Offers", 10)) {
         this.offers = new MerchantOffers(pCompound.getCompound("Offers"));
      }

      this.inventory.fromTag(pCompound.getList("Inventory", 10));
   }

   @Nullable
   public Entity changeDimension(ServerWorld pServer, net.minecraftforge.common.util.ITeleporter teleporter) {
      this.stopTrading();
      return super.changeDimension(pServer, teleporter);
   }

   protected void stopTrading() {
      this.setTradingPlayer((PlayerEntity)null);
   }

   /**
    * Called when the mob's health reaches 0.
    */
   public void die(DamageSource pCause) {
      super.die(pCause);
      this.stopTrading();
   }

   @OnlyIn(Dist.CLIENT)
   protected void addParticlesAroundSelf(IParticleData pParticleOption) {
      for(int i = 0; i < 5; ++i) {
         double d0 = this.random.nextGaussian() * 0.02D;
         double d1 = this.random.nextGaussian() * 0.02D;
         double d2 = this.random.nextGaussian() * 0.02D;
         this.level.addParticle(pParticleOption, this.getRandomX(1.0D), this.getRandomY() + 1.0D, this.getRandomZ(1.0D), d0, d1, d2);
      }

   }

   public boolean canBeLeashed(PlayerEntity pPlayer) {
      return false;
   }

   public Inventory getInventory() {
      return this.inventory;
   }

   public boolean setSlot(int pSlotIndex, ItemStack pStack) {
      if (super.setSlot(pSlotIndex, pStack)) {
         return true;
      } else {
         int i = pSlotIndex - 300;
         if (i >= 0 && i < this.inventory.getContainerSize()) {
            this.inventory.setItem(i, pStack);
            return true;
         } else {
            return false;
         }
      }
   }

   public World getLevel() {
      return this.level;
   }

   protected abstract void updateTrades();

   /**
    * add limites numbers of trades to the given MerchantOffers
    */
   protected void addOffersFromItemListings(MerchantOffers pGivenMerchantOffers, VillagerTrades.ITrade[] pNewTrades, int pMaxNumbers) {
      Set<Integer> set = Sets.newHashSet();
      if (pNewTrades.length > pMaxNumbers) {
         while(set.size() < pMaxNumbers) {
            set.add(this.random.nextInt(pNewTrades.length));
         }
      } else {
         for(int i = 0; i < pNewTrades.length; ++i) {
            set.add(i);
         }
      }

      for(Integer integer : set) {
         VillagerTrades.ITrade villagertrades$itrade = pNewTrades[integer];
         MerchantOffer merchantoffer = villagertrades$itrade.getOffer(this, this.random);
         if (merchantoffer != null) {
            pGivenMerchantOffers.add(merchantoffer);
         }
      }

   }

   @OnlyIn(Dist.CLIENT)
   public Vector3d getRopeHoldPosition(float pPartialTicks) {
      float f = MathHelper.lerp(pPartialTicks, this.yBodyRotO, this.yBodyRot) * ((float)Math.PI / 180F);
      Vector3d vector3d = new Vector3d(0.0D, this.getBoundingBox().getYsize() - 1.0D, 0.2D);
      return this.getPosition(pPartialTicks).add(vector3d.yRot(-f));
   }
}
