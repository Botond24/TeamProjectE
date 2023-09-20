package net.minecraft.block;

import java.util.Random;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.stats.Stats;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class TargetBlock extends Block {
   private static final IntegerProperty OUTPUT_POWER = BlockStateProperties.POWER;

   public TargetBlock(AbstractBlock.Properties p_i241188_1_) {
      super(p_i241188_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(OUTPUT_POWER, Integer.valueOf(0)));
   }

   public void onProjectileHit(World pLevel, BlockState pState, BlockRayTraceResult pHit, ProjectileEntity pProjectile) {
      int i = updateRedstoneOutput(pLevel, pState, pHit, pProjectile);
      Entity entity = pProjectile.getOwner();
      if (entity instanceof ServerPlayerEntity) {
         ServerPlayerEntity serverplayerentity = (ServerPlayerEntity)entity;
         serverplayerentity.awardStat(Stats.TARGET_HIT);
         CriteriaTriggers.TARGET_BLOCK_HIT.trigger(serverplayerentity, pProjectile, pHit.getLocation(), i);
      }

   }

   private static int updateRedstoneOutput(IWorld pLevel, BlockState pState, BlockRayTraceResult pHit, Entity pProjectile) {
      int i = getRedstoneStrength(pHit, pHit.getLocation());
      int j = pProjectile instanceof AbstractArrowEntity ? 20 : 8;
      if (!pLevel.getBlockTicks().hasScheduledTick(pHit.getBlockPos(), pState.getBlock())) {
         setOutputPower(pLevel, pState, i, pHit.getBlockPos(), j);
      }

      return i;
   }

   private static int getRedstoneStrength(BlockRayTraceResult pHit, Vector3d pHitLocation) {
      Direction direction = pHit.getDirection();
      double d0 = Math.abs(MathHelper.frac(pHitLocation.x) - 0.5D);
      double d1 = Math.abs(MathHelper.frac(pHitLocation.y) - 0.5D);
      double d2 = Math.abs(MathHelper.frac(pHitLocation.z) - 0.5D);
      Direction.Axis direction$axis = direction.getAxis();
      double d3;
      if (direction$axis == Direction.Axis.Y) {
         d3 = Math.max(d0, d2);
      } else if (direction$axis == Direction.Axis.Z) {
         d3 = Math.max(d0, d1);
      } else {
         d3 = Math.max(d1, d2);
      }

      return Math.max(1, MathHelper.ceil(15.0D * MathHelper.clamp((0.5D - d3) / 0.5D, 0.0D, 1.0D)));
   }

   private static void setOutputPower(IWorld pLevel, BlockState pState, int pPower, BlockPos pPos, int pWaitTime) {
      pLevel.setBlock(pPos, pState.setValue(OUTPUT_POWER, Integer.valueOf(pPower)), 3);
      pLevel.getBlockTicks().scheduleTick(pPos, pState.getBlock(), pWaitTime);
   }

   public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
      if (pState.getValue(OUTPUT_POWER) != 0) {
         pLevel.setBlock(pPos, pState.setValue(OUTPUT_POWER, Integer.valueOf(0)), 3);
      }

   }

   /**
    * @deprecated call via {@link IBlockState#getWeakPower(IBlockAccess,BlockPos,EnumFacing)} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getSignal(BlockState pBlockState, IBlockReader pBlockAccess, BlockPos pPos, Direction pSide) {
      return pBlockState.getValue(OUTPUT_POWER);
   }

   /**
    * Can this block provide power. Only wire currently seems to have this change based on its state.
    * @deprecated call via {@link IBlockState#canProvidePower()} whenever possible. Implementing/overriding is fine.
    */
   public boolean isSignalSource(BlockState pState) {
      return true;
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(OUTPUT_POWER);
   }

   public void onPlace(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (!pLevel.isClientSide() && !pState.is(pOldState.getBlock())) {
         if (pState.getValue(OUTPUT_POWER) > 0 && !pLevel.getBlockTicks().hasScheduledTick(pPos, this)) {
            pLevel.setBlock(pPos, pState.setValue(OUTPUT_POWER, Integer.valueOf(0)), 18);
         }

      }
   }
}