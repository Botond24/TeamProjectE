package net.minecraft.item;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.MathHelper;

public class MerchantOffer {
   /** The first input for this offer. */
   private final ItemStack baseCostA;
   /** The second input for this offer. */
   private final ItemStack costB;
   /** The output of this offer. */
   private final ItemStack result;
   private int uses;
   private final int maxUses;
   private boolean rewardExp = true;
   private int specialPriceDiff;
   private int demand;
   private float priceMultiplier;
   private int xp = 1;

   public MerchantOffer(CompoundNBT pCompoundTag) {
      this.baseCostA = ItemStack.of(pCompoundTag.getCompound("buy"));
      this.costB = ItemStack.of(pCompoundTag.getCompound("buyB"));
      this.result = ItemStack.of(pCompoundTag.getCompound("sell"));
      this.uses = pCompoundTag.getInt("uses");
      if (pCompoundTag.contains("maxUses", 99)) {
         this.maxUses = pCompoundTag.getInt("maxUses");
      } else {
         this.maxUses = 4;
      }

      if (pCompoundTag.contains("rewardExp", 1)) {
         this.rewardExp = pCompoundTag.getBoolean("rewardExp");
      }

      if (pCompoundTag.contains("xp", 3)) {
         this.xp = pCompoundTag.getInt("xp");
      }

      if (pCompoundTag.contains("priceMultiplier", 5)) {
         this.priceMultiplier = pCompoundTag.getFloat("priceMultiplier");
      }

      this.specialPriceDiff = pCompoundTag.getInt("specialPrice");
      this.demand = pCompoundTag.getInt("demand");
   }

   public MerchantOffer(ItemStack pBaseCostA, ItemStack pResult, int pMaxUses, int pXp, float pPriceMultiplier) {
      this(pBaseCostA, ItemStack.EMPTY, pResult, pMaxUses, pXp, pPriceMultiplier);
   }

   public MerchantOffer(ItemStack pBaseCostA, ItemStack pCostB, ItemStack pResult, int pMaxUses, int pXp, float pPriceMultiplier) {
      this(pBaseCostA, pCostB, pResult, 0, pMaxUses, pXp, pPriceMultiplier);
   }

   public MerchantOffer(ItemStack pBaseCostA, ItemStack pCostB, ItemStack pResult, int pUses, int pMaxUses, int pXp, float pPriceMultiplier) {
      this(pBaseCostA, pCostB, pResult, pUses, pMaxUses, pXp, pPriceMultiplier, 0);
   }

   public MerchantOffer(ItemStack pBaseCostA, ItemStack pCostB, ItemStack pResult, int pUses, int pMaxUses, int pXp, float pPriceMultiplier, int pDemand) {
      this.baseCostA = pBaseCostA;
      this.costB = pCostB;
      this.result = pResult;
      this.uses = pUses;
      this.maxUses = pMaxUses;
      this.xp = pXp;
      this.priceMultiplier = pPriceMultiplier;
      this.demand = pDemand;
   }

   public ItemStack getBaseCostA() {
      return this.baseCostA;
   }

   public ItemStack getCostA() {
      int i = this.baseCostA.getCount();
      ItemStack itemstack = this.baseCostA.copy();
      int j = Math.max(0, MathHelper.floor((float)(i * this.demand) * this.priceMultiplier));
      itemstack.setCount(MathHelper.clamp(i + j + this.specialPriceDiff, 1, this.baseCostA.getMaxStackSize()));
      return itemstack;
   }

   public ItemStack getCostB() {
      return this.costB;
   }

   public ItemStack getResult() {
      return this.result;
   }

   /**
    * Calculates the demand with following formula: demand = demand + uses - maxUses - uses
    */
   public void updateDemand() {
      this.demand = this.demand + this.uses - (this.maxUses - this.uses);
   }

   public ItemStack assemble() {
      return this.result.copy();
   }

   public int getUses() {
      return this.uses;
   }

   public void resetUses() {
      this.uses = 0;
   }

   public int getMaxUses() {
      return this.maxUses;
   }

   public void increaseUses() {
      ++this.uses;
   }

   public int getDemand() {
      return this.demand;
   }

   public void addToSpecialPriceDiff(int pAdd) {
      this.specialPriceDiff += pAdd;
   }

   public void resetSpecialPriceDiff() {
      this.specialPriceDiff = 0;
   }

   public int getSpecialPriceDiff() {
      return this.specialPriceDiff;
   }

   public void setSpecialPriceDiff(int pPrice) {
      this.specialPriceDiff = pPrice;
   }

   public float getPriceMultiplier() {
      return this.priceMultiplier;
   }

   public int getXp() {
      return this.xp;
   }

   public boolean isOutOfStock() {
      return this.uses >= this.maxUses;
   }

   public void setToOutOfStock() {
      this.uses = this.maxUses;
   }

   public boolean needsRestock() {
      return this.uses > 0;
   }

   public boolean shouldRewardExp() {
      return this.rewardExp;
   }

   public CompoundNBT createTag() {
      CompoundNBT compoundnbt = new CompoundNBT();
      compoundnbt.put("buy", this.baseCostA.save(new CompoundNBT()));
      compoundnbt.put("sell", this.result.save(new CompoundNBT()));
      compoundnbt.put("buyB", this.costB.save(new CompoundNBT()));
      compoundnbt.putInt("uses", this.uses);
      compoundnbt.putInt("maxUses", this.maxUses);
      compoundnbt.putBoolean("rewardExp", this.rewardExp);
      compoundnbt.putInt("xp", this.xp);
      compoundnbt.putFloat("priceMultiplier", this.priceMultiplier);
      compoundnbt.putInt("specialPrice", this.specialPriceDiff);
      compoundnbt.putInt("demand", this.demand);
      return compoundnbt;
   }

   public boolean satisfiedBy(ItemStack pPlayerOfferA, ItemStack pPlayerOfferB) {
      return this.isRequiredItem(pPlayerOfferA, this.getCostA()) && pPlayerOfferA.getCount() >= this.getCostA().getCount() && this.isRequiredItem(pPlayerOfferB, this.costB) && pPlayerOfferB.getCount() >= this.costB.getCount();
   }

   private boolean isRequiredItem(ItemStack pOffer, ItemStack pCost) {
      if (pCost.isEmpty() && pOffer.isEmpty()) {
         return true;
      } else {
         ItemStack itemstack = pOffer.copy();
         if (itemstack.getItem().isDamageable(itemstack)) {
            itemstack.setDamageValue(itemstack.getDamageValue());
         }

         return ItemStack.isSame(itemstack, pCost) && (!pCost.hasTag() || itemstack.hasTag() && NBTUtil.compareNbt(pCost.getTag(), itemstack.getTag(), false));
      }
   }

   public boolean take(ItemStack pPlayerOfferA, ItemStack pPlayerOfferB) {
      if (!this.satisfiedBy(pPlayerOfferA, pPlayerOfferB)) {
         return false;
      } else {
         pPlayerOfferA.shrink(this.getCostA().getCount());
         if (!this.getCostB().isEmpty()) {
            pPlayerOfferB.shrink(this.getCostB().getCount());
         }

         return true;
      }
   }
}
