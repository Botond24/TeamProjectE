package net.minecraft.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import java.util.Optional;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.TransportationHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.Explosion;
import net.minecraft.world.ExplosionContext;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ICollisionReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RespawnAnchorBlock extends Block {
   public static final IntegerProperty CHARGE = BlockStateProperties.RESPAWN_ANCHOR_CHARGES;
   private static final ImmutableList<Vector3i> RESPAWN_HORIZONTAL_OFFSETS = ImmutableList.of(new Vector3i(0, 0, -1), new Vector3i(-1, 0, 0), new Vector3i(0, 0, 1), new Vector3i(1, 0, 0), new Vector3i(-1, 0, -1), new Vector3i(1, 0, -1), new Vector3i(-1, 0, 1), new Vector3i(1, 0, 1));
   private static final ImmutableList<Vector3i> RESPAWN_OFFSETS = (new Builder<Vector3i>()).addAll(RESPAWN_HORIZONTAL_OFFSETS).addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map(Vector3i::below).iterator()).addAll(RESPAWN_HORIZONTAL_OFFSETS.stream().map(Vector3i::above).iterator()).add(new Vector3i(0, 1, 0)).build();

   public RespawnAnchorBlock(AbstractBlock.Properties p_i241185_1_) {
      super(p_i241185_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(CHARGE, Integer.valueOf(0)));
   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (pHand == Hand.MAIN_HAND && !isRespawnFuel(itemstack) && isRespawnFuel(pPlayer.getItemInHand(Hand.OFF_HAND))) {
         return ActionResultType.PASS;
      } else if (isRespawnFuel(itemstack) && canBeCharged(pState)) {
         charge(pLevel, pPos, pState);
         if (!pPlayer.abilities.instabuild) {
            itemstack.shrink(1);
         }

         return ActionResultType.sidedSuccess(pLevel.isClientSide);
      } else if (pState.getValue(CHARGE) == 0) {
         return ActionResultType.PASS;
      } else if (!canSetSpawn(pLevel)) {
         if (!pLevel.isClientSide) {
            this.explode(pState, pLevel, pPos);
         }

         return ActionResultType.sidedSuccess(pLevel.isClientSide);
      } else {
         if (!pLevel.isClientSide) {
            ServerPlayerEntity serverplayerentity = (ServerPlayerEntity)pPlayer;
            if (serverplayerentity.getRespawnDimension() != pLevel.dimension() || !serverplayerentity.getRespawnPosition().equals(pPos)) {
               serverplayerentity.setRespawnPosition(pLevel.dimension(), pPos, 0.0F, false, true);
               pLevel.playSound((PlayerEntity)null, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.BLOCKS, 1.0F, 1.0F);
               return ActionResultType.SUCCESS;
            }
         }

         return ActionResultType.CONSUME;
      }
   }

   private static boolean isRespawnFuel(ItemStack pStack) {
      return pStack.getItem() == Items.GLOWSTONE;
   }

   private static boolean canBeCharged(BlockState pState) {
      return pState.getValue(CHARGE) < 4;
   }

   private static boolean isWaterThatWouldFlow(BlockPos pPos, World pLevel) {
      FluidState fluidstate = pLevel.getFluidState(pPos);
      if (!fluidstate.is(FluidTags.WATER)) {
         return false;
      } else if (fluidstate.isSource()) {
         return true;
      } else {
         float f = (float)fluidstate.getAmount();
         if (f < 2.0F) {
            return false;
         } else {
            FluidState fluidstate1 = pLevel.getFluidState(pPos.below());
            return !fluidstate1.is(FluidTags.WATER);
         }
      }
   }

   private void explode(BlockState pState, World pLevel, final BlockPos pPos2) {
      pLevel.removeBlock(pPos2, false);
      boolean flag = Direction.Plane.HORIZONTAL.stream().map(pPos2::relative).anyMatch((p_235563_1_) -> {
         return isWaterThatWouldFlow(p_235563_1_, pLevel);
      });
      final boolean flag1 = flag || pLevel.getFluidState(pPos2.above()).is(FluidTags.WATER);
      ExplosionContext explosioncontext = new ExplosionContext() {
         public Optional<Float> getBlockExplosionResistance(Explosion pExplosion, IBlockReader pReader, BlockPos pPos, BlockState pState, FluidState pFluid) {
            return pPos.equals(pPos2) && flag1 ? Optional.of(Blocks.WATER.getExplosionResistance()) : super.getBlockExplosionResistance(pExplosion, pReader, pPos, pState, pFluid);
         }
      };
      pLevel.explode((Entity)null, DamageSource.badRespawnPointExplosion(), explosioncontext, (double)pPos2.getX() + 0.5D, (double)pPos2.getY() + 0.5D, (double)pPos2.getZ() + 0.5D, 5.0F, true, Explosion.Mode.DESTROY);
   }

   public static boolean canSetSpawn(World pLevel) {
      return pLevel.dimensionType().respawnAnchorWorks();
   }

   public static void charge(World pLevel, BlockPos pPos, BlockState pState) {
      pLevel.setBlock(pPos, pState.setValue(CHARGE, Integer.valueOf(pState.getValue(CHARGE) + 1)), 3);
      pLevel.playSound((PlayerEntity)null, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1.0F, 1.0F);
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
    * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
    * of whether the block can receive random update ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
      if (pState.getValue(CHARGE) != 0) {
         if (pRand.nextInt(100) == 0) {
            pLevel.playSound((PlayerEntity)null, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, SoundEvents.RESPAWN_ANCHOR_AMBIENT, SoundCategory.BLOCKS, 1.0F, 1.0F);
         }

         double d0 = (double)pPos.getX() + 0.5D + (0.5D - pRand.nextDouble());
         double d1 = (double)pPos.getY() + 1.0D;
         double d2 = (double)pPos.getZ() + 0.5D + (0.5D - pRand.nextDouble());
         double d3 = (double)pRand.nextFloat() * 0.04D;
         pLevel.addParticle(ParticleTypes.REVERSE_PORTAL, d0, d1, d2, 0.0D, d3, 0.0D);
      }
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(CHARGE);
   }

   /**
    * @deprecated call via {@link IBlockState#hasComparatorInputOverride()} whenever possible. Implementing/overriding
    * is fine.
    */
   public boolean hasAnalogOutputSignal(BlockState pState) {
      return true;
   }

   public static int getScaledChargeLevel(BlockState pState, int pScale) {
      return MathHelper.floor((float)(pState.getValue(CHARGE) - 0) / 4.0F * (float)pScale);
   }

   /**
    * @deprecated call via {@link IBlockState#getComparatorInputOverride(World,BlockPos)} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getAnalogOutputSignal(BlockState pBlockState, World pLevel, BlockPos pPos) {
      return getScaledChargeLevel(pBlockState, 15);
   }

   public static Optional<Vector3d> findStandUpPosition(EntityType<?> pEntityType, ICollisionReader pLevel, BlockPos pPos) {
      Optional<Vector3d> optional = findStandUpPosition(pEntityType, pLevel, pPos, true);
      return optional.isPresent() ? optional : findStandUpPosition(pEntityType, pLevel, pPos, false);
   }

   private static Optional<Vector3d> findStandUpPosition(EntityType<?> pEntityType, ICollisionReader pLevel, BlockPos pPos, boolean p_242678_3_) {
      BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

      for(Vector3i vector3i : RESPAWN_OFFSETS) {
         blockpos$mutable.set(pPos).move(vector3i);
         Vector3d vector3d = TransportationHelper.findSafeDismountLocation(pEntityType, pLevel, blockpos$mutable, p_242678_3_);
         if (vector3d != null) {
            return Optional.of(vector3d);
         }
      }

      return Optional.empty();
   }

   public boolean isPathfindable(BlockState pState, IBlockReader pLevel, BlockPos pPos, PathType pType) {
      return false;
   }
}