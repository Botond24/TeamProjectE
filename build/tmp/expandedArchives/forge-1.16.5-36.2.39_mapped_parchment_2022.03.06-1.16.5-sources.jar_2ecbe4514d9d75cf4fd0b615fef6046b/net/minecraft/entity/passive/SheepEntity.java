package net.minecraft.entity.passive;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.IShearable;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.BreedGoal;
import net.minecraft.entity.ai.goal.EatGrassGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.PanicGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SheepEntity extends AnimalEntity implements IShearable, net.minecraftforge.common.IForgeShearable {
   private static final DataParameter<Byte> DATA_WOOL_ID = EntityDataManager.defineId(SheepEntity.class, DataSerializers.BYTE);
   private static final Map<DyeColor, IItemProvider> ITEM_BY_DYE = Util.make(Maps.newEnumMap(DyeColor.class), (p_203402_0_) -> {
      p_203402_0_.put(DyeColor.WHITE, Blocks.WHITE_WOOL);
      p_203402_0_.put(DyeColor.ORANGE, Blocks.ORANGE_WOOL);
      p_203402_0_.put(DyeColor.MAGENTA, Blocks.MAGENTA_WOOL);
      p_203402_0_.put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_WOOL);
      p_203402_0_.put(DyeColor.YELLOW, Blocks.YELLOW_WOOL);
      p_203402_0_.put(DyeColor.LIME, Blocks.LIME_WOOL);
      p_203402_0_.put(DyeColor.PINK, Blocks.PINK_WOOL);
      p_203402_0_.put(DyeColor.GRAY, Blocks.GRAY_WOOL);
      p_203402_0_.put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_WOOL);
      p_203402_0_.put(DyeColor.CYAN, Blocks.CYAN_WOOL);
      p_203402_0_.put(DyeColor.PURPLE, Blocks.PURPLE_WOOL);
      p_203402_0_.put(DyeColor.BLUE, Blocks.BLUE_WOOL);
      p_203402_0_.put(DyeColor.BROWN, Blocks.BROWN_WOOL);
      p_203402_0_.put(DyeColor.GREEN, Blocks.GREEN_WOOL);
      p_203402_0_.put(DyeColor.RED, Blocks.RED_WOOL);
      p_203402_0_.put(DyeColor.BLACK, Blocks.BLACK_WOOL);
   });
   /** Map from EnumDyeColor to RGB values for passage to GlStateManager.color() */
   private static final Map<DyeColor, float[]> COLORARRAY_BY_COLOR = Maps.newEnumMap(Arrays.stream(DyeColor.values()).collect(Collectors.toMap((DyeColor p_200204_0_) -> {
      return p_200204_0_;
   }, SheepEntity::createSheepColor)));
   private int eatAnimationTick;
   private EatGrassGoal eatBlockGoal;

   private static float[] createSheepColor(DyeColor p_192020_0_) {
      if (p_192020_0_ == DyeColor.WHITE) {
         return new float[]{0.9019608F, 0.9019608F, 0.9019608F};
      } else {
         float[] afloat = p_192020_0_.getTextureDiffuseColors();
         float f = 0.75F;
         return new float[]{afloat[0] * 0.75F, afloat[1] * 0.75F, afloat[2] * 0.75F};
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static float[] getColorArray(DyeColor pDyeColor) {
      return COLORARRAY_BY_COLOR.get(pDyeColor);
   }

   public SheepEntity(EntityType<? extends SheepEntity> p_i50245_1_, World p_i50245_2_) {
      super(p_i50245_1_, p_i50245_2_);
   }

   protected void registerGoals() {
      this.eatBlockGoal = new EatGrassGoal(this);
      this.goalSelector.addGoal(0, new SwimGoal(this));
      this.goalSelector.addGoal(1, new PanicGoal(this, 1.25D));
      this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
      this.goalSelector.addGoal(3, new TemptGoal(this, 1.1D, Ingredient.of(Items.WHEAT), false));
      this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1D));
      this.goalSelector.addGoal(5, this.eatBlockGoal);
      this.goalSelector.addGoal(6, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
      this.goalSelector.addGoal(7, new LookAtGoal(this, PlayerEntity.class, 6.0F));
      this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
   }

   protected void customServerAiStep() {
      this.eatAnimationTick = this.eatBlockGoal.getEatAnimationTick();
      super.customServerAiStep();
   }

   /**
    * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
    * react to sunlight and start to burn.
    */
   public void aiStep() {
      if (this.level.isClientSide) {
         this.eatAnimationTick = Math.max(0, this.eatAnimationTick - 1);
      }

      super.aiStep();
   }

   public static AttributeModifierMap.MutableAttribute createAttributes() {
      return MobEntity.createMobAttributes().add(Attributes.MAX_HEALTH, 8.0D).add(Attributes.MOVEMENT_SPEED, (double)0.23F);
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_WOOL_ID, (byte)0);
   }

   public ResourceLocation getDefaultLootTable() {
      if (this.isSheared()) {
         return this.getType().getDefaultLootTable();
      } else {
         switch(this.getColor()) {
         case WHITE:
         default:
            return LootTables.SHEEP_WHITE;
         case ORANGE:
            return LootTables.SHEEP_ORANGE;
         case MAGENTA:
            return LootTables.SHEEP_MAGENTA;
         case LIGHT_BLUE:
            return LootTables.SHEEP_LIGHT_BLUE;
         case YELLOW:
            return LootTables.SHEEP_YELLOW;
         case LIME:
            return LootTables.SHEEP_LIME;
         case PINK:
            return LootTables.SHEEP_PINK;
         case GRAY:
            return LootTables.SHEEP_GRAY;
         case LIGHT_GRAY:
            return LootTables.SHEEP_LIGHT_GRAY;
         case CYAN:
            return LootTables.SHEEP_CYAN;
         case PURPLE:
            return LootTables.SHEEP_PURPLE;
         case BLUE:
            return LootTables.SHEEP_BLUE;
         case BROWN:
            return LootTables.SHEEP_BROWN;
         case GREEN:
            return LootTables.SHEEP_GREEN;
         case RED:
            return LootTables.SHEEP_RED;
         case BLACK:
            return LootTables.SHEEP_BLACK;
         }
      }
   }

   /**
    * Handles an entity event fired from {@link net.minecraft.world.level.Level#broadcastEntityEvent}.
    */
   @OnlyIn(Dist.CLIENT)
   public void handleEntityEvent(byte pId) {
      if (pId == 10) {
         this.eatAnimationTick = 40;
      } else {
         super.handleEntityEvent(pId);
      }

   }

   @OnlyIn(Dist.CLIENT)
   public float getHeadEatPositionScale(float pPartialTick) {
      if (this.eatAnimationTick <= 0) {
         return 0.0F;
      } else if (this.eatAnimationTick >= 4 && this.eatAnimationTick <= 36) {
         return 1.0F;
      } else {
         return this.eatAnimationTick < 4 ? ((float)this.eatAnimationTick - pPartialTick) / 4.0F : -((float)(this.eatAnimationTick - 40) - pPartialTick) / 4.0F;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public float getHeadEatAngleScale(float pPartialTick) {
      if (this.eatAnimationTick > 4 && this.eatAnimationTick <= 36) {
         float f = ((float)(this.eatAnimationTick - 4) - pPartialTick) / 32.0F;
         return ((float)Math.PI / 5F) + 0.21991149F * MathHelper.sin(f * 28.7F);
      } else {
         return this.eatAnimationTick > 0 ? ((float)Math.PI / 5F) : this.xRot * ((float)Math.PI / 180F);
      }
   }

   public ActionResultType mobInteract(PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (false && itemstack.getItem() == Items.SHEARS) { //Forge: Moved to onSheared
         if (!this.level.isClientSide && this.readyForShearing()) {
            this.shear(SoundCategory.PLAYERS);
            itemstack.hurtAndBreak(1, pPlayer, (p_213613_1_) -> {
               p_213613_1_.broadcastBreakEvent(pHand);
            });
            return ActionResultType.SUCCESS;
         } else {
            return ActionResultType.CONSUME;
         }
      } else {
         return super.mobInteract(pPlayer, pHand);
      }
   }

   public void shear(SoundCategory pCategory) {
      this.level.playSound((PlayerEntity)null, this, SoundEvents.SHEEP_SHEAR, pCategory, 1.0F, 1.0F);
      this.setSheared(true);
      int i = 1 + this.random.nextInt(3);

      for(int j = 0; j < i; ++j) {
         ItemEntity itementity = this.spawnAtLocation(ITEM_BY_DYE.get(this.getColor()), 1);
         if (itementity != null) {
            itementity.setDeltaMovement(itementity.getDeltaMovement().add((double)((this.random.nextFloat() - this.random.nextFloat()) * 0.1F), (double)(this.random.nextFloat() * 0.05F), (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.1F)));
         }
      }

   }

   public boolean readyForShearing() {
      return this.isAlive() && !this.isSheared() && !this.isBaby();
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putBoolean("Sheared", this.isSheared());
      pCompound.putByte("Color", (byte)this.getColor().getId());
   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setSheared(pCompound.getBoolean("Sheared"));
      this.setColor(DyeColor.byId(pCompound.getByte("Color")));
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.SHEEP_AMBIENT;
   }

   protected SoundEvent getHurtSound(DamageSource pDamageSource) {
      return SoundEvents.SHEEP_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.SHEEP_DEATH;
   }

   protected void playStepSound(BlockPos pPos, BlockState pBlock) {
      this.playSound(SoundEvents.SHEEP_STEP, 0.15F, 1.0F);
   }

   /**
    * Gets the wool color of this sheep.
    */
   public DyeColor getColor() {
      return DyeColor.byId(this.entityData.get(DATA_WOOL_ID) & 15);
   }

   /**
    * Sets the wool color of this sheep
    */
   public void setColor(DyeColor pDyeColor) {
      byte b0 = this.entityData.get(DATA_WOOL_ID);
      this.entityData.set(DATA_WOOL_ID, (byte)(b0 & 240 | pDyeColor.getId() & 15));
   }

   /**
    * returns true if a sheeps wool has been sheared
    */
   public boolean isSheared() {
      return (this.entityData.get(DATA_WOOL_ID) & 16) != 0;
   }

   /**
    * make a sheep sheared if set to true
    */
   public void setSheared(boolean pSheared) {
      byte b0 = this.entityData.get(DATA_WOOL_ID);
      if (pSheared) {
         this.entityData.set(DATA_WOOL_ID, (byte)(b0 | 16));
      } else {
         this.entityData.set(DATA_WOOL_ID, (byte)(b0 & -17));
      }

   }

   /**
    * Chooses a "vanilla" sheep color based on the provided random.
    */
   public static DyeColor getRandomSheepColor(Random pRandom) {
      int i = pRandom.nextInt(100);
      if (i < 5) {
         return DyeColor.BLACK;
      } else if (i < 10) {
         return DyeColor.GRAY;
      } else if (i < 15) {
         return DyeColor.LIGHT_GRAY;
      } else if (i < 18) {
         return DyeColor.BROWN;
      } else {
         return pRandom.nextInt(500) == 0 ? DyeColor.PINK : DyeColor.WHITE;
      }
   }

   public SheepEntity getBreedOffspring(ServerWorld pServerLevel, AgeableEntity pMate) {
      SheepEntity sheepentity = (SheepEntity)pMate;
      SheepEntity sheepentity1 = EntityType.SHEEP.create(pServerLevel);
      sheepentity1.setColor(this.getOffspringColor(this, sheepentity));
      return sheepentity1;
   }

   /**
    * Applies the benefits of growing back wool and faster growing up to the acting entity. This function is used in the
    * {@code EatBlockGoal}.
    */
   public void ate() {
      this.setSheared(false);
      if (this.isBaby()) {
         this.ageUp(60);
      }

   }

   @Nullable
   public ILivingEntityData finalizeSpawn(IServerWorld pLevel, DifficultyInstance pDifficulty, SpawnReason pReason, @Nullable ILivingEntityData pSpawnData, @Nullable CompoundNBT pDataTag) {
      this.setColor(getRandomSheepColor(pLevel.getRandom()));
      return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
   }

   /**
    * Attempts to mix both parent sheep to come up with a mixed dye color.
    */
   private DyeColor getOffspringColor(AnimalEntity pFather, AnimalEntity pMother) {
      DyeColor dyecolor = ((SheepEntity)pFather).getColor();
      DyeColor dyecolor1 = ((SheepEntity)pMother).getColor();
      CraftingInventory craftinginventory = makeContainer(dyecolor, dyecolor1);
      return this.level.getRecipeManager().getRecipeFor(IRecipeType.CRAFTING, craftinginventory, this.level).map((p_213614_1_) -> {
         return p_213614_1_.assemble(craftinginventory);
      }).map(ItemStack::getItem).filter(DyeItem.class::isInstance).map(DyeItem.class::cast).map(DyeItem::getDyeColor).orElseGet(() -> {
         return this.level.random.nextBoolean() ? dyecolor : dyecolor1;
      });
   }

   private static CraftingInventory makeContainer(DyeColor pFatherColor, DyeColor pMotherColor) {
      CraftingInventory craftinginventory = new CraftingInventory(new Container((ContainerType)null, -1) {
         /**
          * Determines whether supplied player can use this container
          */
         public boolean stillValid(PlayerEntity pPlayer) {
            return false;
         }
      }, 2, 1);
      craftinginventory.setItem(0, new ItemStack(DyeItem.byColor(pFatherColor)));
      craftinginventory.setItem(1, new ItemStack(DyeItem.byColor(pMotherColor)));
      return craftinginventory;
   }

   protected float getStandingEyeHeight(Pose pPose, EntitySize pSize) {
      return 0.95F * pSize.height;
   }

   @Override
   public boolean isShearable(@javax.annotation.Nonnull ItemStack item, World world, BlockPos pos) {
      return readyForShearing();
   }

   @javax.annotation.Nonnull
   @Override
   public java.util.List<ItemStack> onSheared(@Nullable PlayerEntity player, @javax.annotation.Nonnull ItemStack item, World world, BlockPos pos, int fortune) {
      world.playSound(null, this, SoundEvents.SHEEP_SHEAR, player == null ? SoundCategory.BLOCKS : SoundCategory.PLAYERS, 1.0F, 1.0F);
      if (!world.isClientSide) {
         this.setSheared(true);
         int i = 1 + this.random.nextInt(3);

         java.util.List<ItemStack> items = new java.util.ArrayList<>();
         for (int j = 0; j < i; ++j) {
            items.add(new ItemStack(ITEM_BY_DYE.get(this.getColor())));
         }
         return items;
      }
      return java.util.Collections.emptyList();
   }
}
