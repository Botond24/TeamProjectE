package net.minecraft.entity.passive;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerBlock;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IShearable;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SuspiciousStewItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DrinkHelper;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.tuple.Pair;

public class MooshroomEntity extends CowEntity implements IShearable, net.minecraftforge.common.IForgeShearable {
   private static final DataParameter<String> DATA_TYPE = EntityDataManager.defineId(MooshroomEntity.class, DataSerializers.STRING);
   private Effect effect;
   private int effectDuration;
   /** Stores the UUID of the most recent lightning bolt to strike */
   private UUID lastLightningBoltUUID;

   public MooshroomEntity(EntityType<? extends MooshroomEntity> p_i50257_1_, World p_i50257_2_) {
      super(p_i50257_1_, p_i50257_2_);
   }

   public float getWalkTargetValue(BlockPos pPos, IWorldReader pLevel) {
      return pLevel.getBlockState(pPos.below()).is(Blocks.MYCELIUM) ? 10.0F : pLevel.getBrightness(pPos) - 0.5F;
   }

   public static boolean checkMushroomSpawnRules(EntityType<MooshroomEntity> pMushroomCow, IWorld pLevel, SpawnReason pSpawnType, BlockPos pPos, Random pRandom) {
      return pLevel.getBlockState(pPos.below()).is(Blocks.MYCELIUM) && pLevel.getRawBrightness(pPos, 0) > 8;
   }

   public void thunderHit(ServerWorld pLevel, LightningBoltEntity pLightning) {
      UUID uuid = pLightning.getUUID();
      if (!uuid.equals(this.lastLightningBoltUUID)) {
         this.setMushroomType(this.getMushroomType() == MooshroomEntity.Type.RED ? MooshroomEntity.Type.BROWN : MooshroomEntity.Type.RED);
         this.lastLightningBoltUUID = uuid;
         this.playSound(SoundEvents.MOOSHROOM_CONVERT, 2.0F, 1.0F);
      }

   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(DATA_TYPE, MooshroomEntity.Type.RED.type);
   }

   public ActionResultType mobInteract(PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (itemstack.getItem() == Items.BOWL && !this.isBaby()) {
         boolean flag = false;
         ItemStack itemstack1;
         if (this.effect != null) {
            flag = true;
            itemstack1 = new ItemStack(Items.SUSPICIOUS_STEW);
            SuspiciousStewItem.saveMobEffect(itemstack1, this.effect, this.effectDuration);
            this.effect = null;
            this.effectDuration = 0;
         } else {
            itemstack1 = new ItemStack(Items.MUSHROOM_STEW);
         }

         ItemStack itemstack2 = DrinkHelper.createFilledResult(itemstack, pPlayer, itemstack1, false);
         pPlayer.setItemInHand(pHand, itemstack2);
         SoundEvent soundevent;
         if (flag) {
            soundevent = SoundEvents.MOOSHROOM_MILK_SUSPICIOUSLY;
         } else {
            soundevent = SoundEvents.MOOSHROOM_MILK;
         }

         this.playSound(soundevent, 1.0F, 1.0F);
         return ActionResultType.sidedSuccess(this.level.isClientSide);
      } else if (false && itemstack.getItem() == Items.SHEARS && this.readyForShearing()) { //Forge: Moved to onSheared
         this.shear(SoundCategory.PLAYERS);
         if (!this.level.isClientSide) {
            itemstack.hurtAndBreak(1, pPlayer, (p_213442_1_) -> {
               p_213442_1_.broadcastBreakEvent(pHand);
            });
         }

         return ActionResultType.sidedSuccess(this.level.isClientSide);
      } else if (this.getMushroomType() == MooshroomEntity.Type.BROWN && itemstack.getItem().is(ItemTags.SMALL_FLOWERS)) {
         if (this.effect != null) {
            for(int i = 0; i < 2; ++i) {
               this.level.addParticle(ParticleTypes.SMOKE, this.getX() + this.random.nextDouble() / 2.0D, this.getY(0.5D), this.getZ() + this.random.nextDouble() / 2.0D, 0.0D, this.random.nextDouble() / 5.0D, 0.0D);
            }
         } else {
            Optional<Pair<Effect, Integer>> optional = this.getEffectFromItemStack(itemstack);
            if (!optional.isPresent()) {
               return ActionResultType.PASS;
            }

            Pair<Effect, Integer> pair = optional.get();
            if (!pPlayer.abilities.instabuild) {
               itemstack.shrink(1);
            }

            for(int j = 0; j < 4; ++j) {
               this.level.addParticle(ParticleTypes.EFFECT, this.getX() + this.random.nextDouble() / 2.0D, this.getY(0.5D), this.getZ() + this.random.nextDouble() / 2.0D, 0.0D, this.random.nextDouble() / 5.0D, 0.0D);
            }

            this.effect = pair.getLeft();
            this.effectDuration = pair.getRight();
            this.playSound(SoundEvents.MOOSHROOM_EAT, 2.0F, 1.0F);
         }

         return ActionResultType.sidedSuccess(this.level.isClientSide);
      } else {
         return super.mobInteract(pPlayer, pHand);
      }
   }

   public void shear(SoundCategory pCategory) {
      this.level.playSound((PlayerEntity)null, this, SoundEvents.MOOSHROOM_SHEAR, pCategory, 1.0F, 1.0F);
      if (!this.level.isClientSide()) {
         ((ServerWorld)this.level).sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(0.5D), this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
         this.remove();
         CowEntity cowentity = EntityType.COW.create(this.level);
         cowentity.moveTo(this.getX(), this.getY(), this.getZ(), this.yRot, this.xRot);
         cowentity.setHealth(this.getHealth());
         cowentity.yBodyRot = this.yBodyRot;
         if (this.hasCustomName()) {
            cowentity.setCustomName(this.getCustomName());
            cowentity.setCustomNameVisible(this.isCustomNameVisible());
         }

         if (this.isPersistenceRequired()) {
            cowentity.setPersistenceRequired();
         }

         cowentity.setInvulnerable(this.isInvulnerable());
         this.level.addFreshEntity(cowentity);

         for(int i = 0; i < 5; ++i) {
            this.level.addFreshEntity(new ItemEntity(this.level, this.getX(), this.getY(1.0D), this.getZ(), new ItemStack(this.getMushroomType().blockState.getBlock())));
         }
      }

   }

   public boolean readyForShearing() {
      return this.isAlive() && !this.isBaby();
   }

   public void addAdditionalSaveData(CompoundNBT pCompound) {
      super.addAdditionalSaveData(pCompound);
      pCompound.putString("Type", this.getMushroomType().type);
      if (this.effect != null) {
         pCompound.putByte("EffectId", (byte)Effect.getId(this.effect));
         pCompound.putInt("EffectDuration", this.effectDuration);
      }

   }

   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundNBT pCompound) {
      super.readAdditionalSaveData(pCompound);
      this.setMushroomType(MooshroomEntity.Type.byType(pCompound.getString("Type")));
      if (pCompound.contains("EffectId", 1)) {
         this.effect = Effect.byId(pCompound.getByte("EffectId"));
      }

      if (pCompound.contains("EffectDuration", 3)) {
         this.effectDuration = pCompound.getInt("EffectDuration");
      }

   }

   private Optional<Pair<Effect, Integer>> getEffectFromItemStack(ItemStack pStack) {
      Item item = pStack.getItem();
      if (item instanceof BlockItem) {
         Block block = ((BlockItem)item).getBlock();
         if (block instanceof FlowerBlock) {
            FlowerBlock flowerblock = (FlowerBlock)block;
            return Optional.of(Pair.of(flowerblock.getSuspiciousStewEffect(), flowerblock.getEffectDuration()));
         }
      }

      return Optional.empty();
   }

   private void setMushroomType(MooshroomEntity.Type pType) {
      this.entityData.set(DATA_TYPE, pType.type);
   }

   public MooshroomEntity.Type getMushroomType() {
      return MooshroomEntity.Type.byType(this.entityData.get(DATA_TYPE));
   }

   public MooshroomEntity getBreedOffspring(ServerWorld pServerLevel, AgeableEntity pMate) {
      MooshroomEntity mooshroomentity = EntityType.MOOSHROOM.create(pServerLevel);
      mooshroomentity.setMushroomType(this.getOffspringType((MooshroomEntity)pMate));
      return mooshroomentity;
   }

   private MooshroomEntity.Type getOffspringType(MooshroomEntity pMate) {
      MooshroomEntity.Type mooshroomentity$type = this.getMushroomType();
      MooshroomEntity.Type mooshroomentity$type1 = pMate.getMushroomType();
      MooshroomEntity.Type mooshroomentity$type2;
      if (mooshroomentity$type == mooshroomentity$type1 && this.random.nextInt(1024) == 0) {
         mooshroomentity$type2 = mooshroomentity$type == MooshroomEntity.Type.BROWN ? MooshroomEntity.Type.RED : MooshroomEntity.Type.BROWN;
      } else {
         mooshroomentity$type2 = this.random.nextBoolean() ? mooshroomentity$type : mooshroomentity$type1;
      }

      return mooshroomentity$type2;
   }

   @Override
   public boolean isShearable(@javax.annotation.Nonnull ItemStack item, World world, BlockPos pos) {
      return readyForShearing();
   }

   @javax.annotation.Nonnull
   @Override
   public java.util.List<ItemStack> onSheared(@javax.annotation.Nullable PlayerEntity player, @javax.annotation.Nonnull ItemStack item, World world, BlockPos pos, int fortune) {
      world.playSound(null, this, SoundEvents.MOOSHROOM_SHEAR, player == null ? SoundCategory.BLOCKS : SoundCategory.PLAYERS, 1.0F, 1.0F);
      if (!world.isClientSide()) {
         ((ServerWorld)this.level).sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(0.5D), this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
         this.remove();
         CowEntity cowentity = EntityType.COW.create(this.level);
         cowentity.moveTo(this.getX(), this.getY(), this.getZ(), this.yRot, this.xRot);
         cowentity.setHealth(this.getHealth());
         cowentity.yBodyRot = this.yBodyRot;
         if (this.hasCustomName()) {
            cowentity.setCustomName(this.getCustomName());
            cowentity.setCustomNameVisible(this.isCustomNameVisible());
         }

         if (this.isPersistenceRequired()) {
            cowentity.setPersistenceRequired();
         }

         cowentity.setInvulnerable(this.isInvulnerable());
         this.level.addFreshEntity(cowentity);

         java.util.List<ItemStack> items = new java.util.ArrayList<>();
         for (int i = 0; i < 5; ++i) {
            items.add(new ItemStack(this.getMushroomType().blockState.getBlock()));
         }

         return items;
      }
      return java.util.Collections.emptyList();
   }


   public static enum Type {
      RED("red", Blocks.RED_MUSHROOM.defaultBlockState()),
      BROWN("brown", Blocks.BROWN_MUSHROOM.defaultBlockState());

      private final String type;
      private final BlockState blockState;

      private Type(String pType, BlockState pBlockState) {
         this.type = pType;
         this.blockState = pBlockState;
      }

      /**
       * A block state that is rendered on the back of the mooshroom.
       */
      @OnlyIn(Dist.CLIENT)
      public BlockState getBlockState() {
         return this.blockState;
      }

      private static MooshroomEntity.Type byType(String pName) {
         for(MooshroomEntity.Type mooshroomentity$type : values()) {
            if (mooshroomentity$type.type.equals(pName)) {
               return mooshroomentity$type;
            }
         }

         return RED;
      }
   }
}
