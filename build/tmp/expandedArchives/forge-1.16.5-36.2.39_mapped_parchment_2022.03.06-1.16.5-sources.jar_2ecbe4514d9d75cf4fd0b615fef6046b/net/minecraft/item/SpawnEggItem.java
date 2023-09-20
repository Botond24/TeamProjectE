package net.minecraft.item;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.stats.Stats;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.AbstractSpawner;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SpawnEggItem extends Item {
   private static final Map<EntityType<?>, SpawnEggItem> BY_ID = Maps.newIdentityHashMap();
   private final int color1;
   private final int color2;
   private final EntityType<?> defaultType;

   /** @deprecated Forge: Use {@link net.minecraftforge.common.ForgeSpawnEggItem} instead for suppliers */
   @Deprecated
   public SpawnEggItem(EntityType<?> pDefaultType, int pBackgroundColor, int pHighlightColor, Item.Properties pProperties) {
      super(pProperties);
      this.defaultType = pDefaultType;
      this.color1 = pBackgroundColor;
      this.color2 = pHighlightColor;
      if (pDefaultType != null)
      BY_ID.put(pDefaultType, this);
   }

   /**
    * Called when this item is used when targetting a Block
    */
   public ActionResultType useOn(ItemUseContext pContext) {
      World world = pContext.getLevel();
      if (!(world instanceof ServerWorld)) {
         return ActionResultType.SUCCESS;
      } else {
         ItemStack itemstack = pContext.getItemInHand();
         BlockPos blockpos = pContext.getClickedPos();
         Direction direction = pContext.getClickedFace();
         BlockState blockstate = world.getBlockState(blockpos);
         if (blockstate.is(Blocks.SPAWNER)) {
            TileEntity tileentity = world.getBlockEntity(blockpos);
            if (tileentity instanceof MobSpawnerTileEntity) {
               AbstractSpawner abstractspawner = ((MobSpawnerTileEntity)tileentity).getSpawner();
               EntityType<?> entitytype1 = this.getType(itemstack.getTag());
               abstractspawner.setEntityId(entitytype1);
               tileentity.setChanged();
               world.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
               itemstack.shrink(1);
               return ActionResultType.CONSUME;
            }
         }

         BlockPos blockpos1;
         if (blockstate.getCollisionShape(world, blockpos).isEmpty()) {
            blockpos1 = blockpos;
         } else {
            blockpos1 = blockpos.relative(direction);
         }

         EntityType<?> entitytype = this.getType(itemstack.getTag());
         if (entitytype.spawn((ServerWorld)world, itemstack, pContext.getPlayer(), blockpos1, SpawnReason.SPAWN_EGG, true, !Objects.equals(blockpos, blockpos1) && direction == Direction.UP) != null) {
            itemstack.shrink(1);
         }

         return ActionResultType.CONSUME;
      }
   }

   /**
    * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
    * {@link #onItemUse}.
    */
   public ActionResult<ItemStack> use(World pLevel, PlayerEntity pPlayer, Hand pHand) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      RayTraceResult raytraceresult = getPlayerPOVHitResult(pLevel, pPlayer, RayTraceContext.FluidMode.SOURCE_ONLY);
      if (raytraceresult.getType() != RayTraceResult.Type.BLOCK) {
         return ActionResult.pass(itemstack);
      } else if (!(pLevel instanceof ServerWorld)) {
         return ActionResult.success(itemstack);
      } else {
         BlockRayTraceResult blockraytraceresult = (BlockRayTraceResult)raytraceresult;
         BlockPos blockpos = blockraytraceresult.getBlockPos();
         if (!(pLevel.getBlockState(blockpos).getBlock() instanceof FlowingFluidBlock)) {
            return ActionResult.pass(itemstack);
         } else if (pLevel.mayInteract(pPlayer, blockpos) && pPlayer.mayUseItemAt(blockpos, blockraytraceresult.getDirection(), itemstack)) {
            EntityType<?> entitytype = this.getType(itemstack.getTag());
            if (entitytype.spawn((ServerWorld)pLevel, itemstack, pPlayer, blockpos, SpawnReason.SPAWN_EGG, false, false) == null) {
               return ActionResult.pass(itemstack);
            } else {
               if (!pPlayer.abilities.instabuild) {
                  itemstack.shrink(1);
               }

               pPlayer.awardStat(Stats.ITEM_USED.get(this));
               return ActionResult.consume(itemstack);
            }
         } else {
            return ActionResult.fail(itemstack);
         }
      }
   }

   public boolean spawnsEntity(@Nullable CompoundNBT pNbt, EntityType<?> pType) {
      return Objects.equals(this.getType(pNbt), pType);
   }

   @OnlyIn(Dist.CLIENT)
   public int getColor(int pTintIndex) {
      return pTintIndex == 0 ? this.color1 : this.color2;
   }

   /** @deprecated Forge: call {@link net.minecraftforge.common.ForgeSpawnEggItem#fromEntityType(EntityType)} instead */
   @Deprecated
   @Nullable
   public static SpawnEggItem byId(@Nullable EntityType<?> pType) {
      return BY_ID.get(pType);
   }

   public static Iterable<SpawnEggItem> eggs() {
      return Iterables.unmodifiableIterable(BY_ID.values());
   }

   public EntityType<?> getType(@Nullable CompoundNBT pNbt) {
      if (pNbt != null && pNbt.contains("EntityTag", 10)) {
         CompoundNBT compoundnbt = pNbt.getCompound("EntityTag");
         if (compoundnbt.contains("id", 8)) {
            return EntityType.byString(compoundnbt.getString("id")).orElse(this.defaultType);
         }
      }

      return this.defaultType;
   }

   public Optional<MobEntity> spawnOffspringFromSpawnEgg(PlayerEntity pPlayer, MobEntity pMob, EntityType<? extends MobEntity> pEntityType, ServerWorld pServerLevel, Vector3d pPos, ItemStack pStack) {
      if (!this.spawnsEntity(pStack.getTag(), pEntityType)) {
         return Optional.empty();
      } else {
         MobEntity mobentity;
         if (pMob instanceof AgeableEntity) {
            mobentity = ((AgeableEntity)pMob).getBreedOffspring(pServerLevel, (AgeableEntity)pMob);
         } else {
            mobentity = pEntityType.create(pServerLevel);
         }

         if (mobentity == null) {
            return Optional.empty();
         } else {
            mobentity.setBaby(true);
            if (!mobentity.isBaby()) {
               return Optional.empty();
            } else {
               mobentity.moveTo(pPos.x(), pPos.y(), pPos.z(), 0.0F, 0.0F);
               pServerLevel.addFreshEntityWithPassengers(mobentity);
               if (pStack.hasCustomHoverName()) {
                  mobentity.setCustomName(pStack.getHoverName());
               }

               if (!pPlayer.abilities.instabuild) {
                  pStack.shrink(1);
               }

               return Optional.of(mobentity);
            }
         }
      }
   }
}
