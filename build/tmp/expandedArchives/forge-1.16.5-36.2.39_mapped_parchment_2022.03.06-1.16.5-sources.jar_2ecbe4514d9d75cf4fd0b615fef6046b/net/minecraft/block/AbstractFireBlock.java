package net.minecraft.block;

import java.util.Optional;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractFireBlock extends Block {
   private final float fireDamage;
   protected static final VoxelShape DOWN_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);

   public AbstractFireBlock(AbstractBlock.Properties pProperties, float pFireDamage) {
      super(pProperties);
      this.fireDamage = pFireDamage;
   }

   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      return getState(pContext.getLevel(), pContext.getClickedPos());
   }

   public static BlockState getState(IBlockReader pReader, BlockPos pPos) {
      BlockPos blockpos = pPos.below();
      BlockState blockstate = pReader.getBlockState(blockpos);
      return SoulFireBlock.canSurviveOnBlock(blockstate.getBlock()) ? Blocks.SOUL_FIRE.defaultBlockState() : ((FireBlock)Blocks.FIRE).getStateForPlacement(pReader, pPos);
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return DOWN_AABB;
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
    * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
    * of whether the block can receive random update ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
      if (pRand.nextInt(24) == 0) {
         pLevel.playLocalSound((double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, SoundEvents.FIRE_AMBIENT, SoundCategory.BLOCKS, 1.0F + pRand.nextFloat(), pRand.nextFloat() * 0.7F + 0.3F, false);
      }

      BlockPos blockpos = pPos.below();
      BlockState blockstate = pLevel.getBlockState(blockpos);
      if (!this.canBurn(blockstate) && !blockstate.isFaceSturdy(pLevel, blockpos, Direction.UP)) {
         if (this.canBurn(pLevel.getBlockState(pPos.west()))) {
            for(int j = 0; j < 2; ++j) {
               double d3 = (double)pPos.getX() + pRand.nextDouble() * (double)0.1F;
               double d8 = (double)pPos.getY() + pRand.nextDouble();
               double d13 = (double)pPos.getZ() + pRand.nextDouble();
               pLevel.addParticle(ParticleTypes.LARGE_SMOKE, d3, d8, d13, 0.0D, 0.0D, 0.0D);
            }
         }

         if (this.canBurn(pLevel.getBlockState(pPos.east()))) {
            for(int k = 0; k < 2; ++k) {
               double d4 = (double)(pPos.getX() + 1) - pRand.nextDouble() * (double)0.1F;
               double d9 = (double)pPos.getY() + pRand.nextDouble();
               double d14 = (double)pPos.getZ() + pRand.nextDouble();
               pLevel.addParticle(ParticleTypes.LARGE_SMOKE, d4, d9, d14, 0.0D, 0.0D, 0.0D);
            }
         }

         if (this.canBurn(pLevel.getBlockState(pPos.north()))) {
            for(int l = 0; l < 2; ++l) {
               double d5 = (double)pPos.getX() + pRand.nextDouble();
               double d10 = (double)pPos.getY() + pRand.nextDouble();
               double d15 = (double)pPos.getZ() + pRand.nextDouble() * (double)0.1F;
               pLevel.addParticle(ParticleTypes.LARGE_SMOKE, d5, d10, d15, 0.0D, 0.0D, 0.0D);
            }
         }

         if (this.canBurn(pLevel.getBlockState(pPos.south()))) {
            for(int i1 = 0; i1 < 2; ++i1) {
               double d6 = (double)pPos.getX() + pRand.nextDouble();
               double d11 = (double)pPos.getY() + pRand.nextDouble();
               double d16 = (double)(pPos.getZ() + 1) - pRand.nextDouble() * (double)0.1F;
               pLevel.addParticle(ParticleTypes.LARGE_SMOKE, d6, d11, d16, 0.0D, 0.0D, 0.0D);
            }
         }

         if (this.canBurn(pLevel.getBlockState(pPos.above()))) {
            for(int j1 = 0; j1 < 2; ++j1) {
               double d7 = (double)pPos.getX() + pRand.nextDouble();
               double d12 = (double)(pPos.getY() + 1) - pRand.nextDouble() * (double)0.1F;
               double d17 = (double)pPos.getZ() + pRand.nextDouble();
               pLevel.addParticle(ParticleTypes.LARGE_SMOKE, d7, d12, d17, 0.0D, 0.0D, 0.0D);
            }
         }
      } else {
         for(int i = 0; i < 3; ++i) {
            double d0 = (double)pPos.getX() + pRand.nextDouble();
            double d1 = (double)pPos.getY() + pRand.nextDouble() * 0.5D + 0.5D;
            double d2 = (double)pPos.getZ() + pRand.nextDouble();
            pLevel.addParticle(ParticleTypes.LARGE_SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
         }
      }

   }

   protected abstract boolean canBurn(BlockState pState);

   public void entityInside(BlockState pState, World pLevel, BlockPos pPos, Entity pEntity) {
      if (!pEntity.fireImmune()) {
         pEntity.setRemainingFireTicks(pEntity.getRemainingFireTicks() + 1);
         if (pEntity.getRemainingFireTicks() == 0) {
            pEntity.setSecondsOnFire(8);
         }

         pEntity.hurt(DamageSource.IN_FIRE, this.fireDamage);
      }

      super.entityInside(pState, pLevel, pPos, pEntity);
   }

   public void onPlace(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (!pOldState.is(pState.getBlock())) {
         if (inPortalDimension(pLevel)) {
            Optional<PortalSize> optional = PortalSize.findEmptyPortalShape(pLevel, pPos, Direction.Axis.X);
            optional =  net.minecraftforge.event.ForgeEventFactory.onTrySpawnPortal(pLevel, pPos, optional);
            if (optional.isPresent()) {
               optional.get().createPortalBlocks();
               return;
            }
         }

         if (!pState.canSurvive(pLevel, pPos)) {
            pLevel.removeBlock(pPos, false);
         }

      }
   }

   private static boolean inPortalDimension(World pLevel) {
      return pLevel.dimension() == World.OVERWORLD || pLevel.dimension() == World.NETHER;
   }

   /**
    * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually collect
    * this block
    */
   public void playerWillDestroy(World pLevel, BlockPos pPos, BlockState pState, PlayerEntity pPlayer) {
      if (!pLevel.isClientSide()) {
         pLevel.levelEvent((PlayerEntity)null, 1009, pPos, 0);
      }

   }

   public static boolean canBePlacedAt(World pLevel, BlockPos pPos, Direction pDirection) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      if (!blockstate.isAir()) {
         return false;
      } else {
         return getState(pLevel, pPos).canSurvive(pLevel, pPos) || isPortal(pLevel, pPos, pDirection);
      }
   }

   private static boolean isPortal(World pLevel, BlockPos pPos, Direction pDirection) {
      if (!inPortalDimension(pLevel)) {
         return false;
      } else {
         BlockPos.Mutable blockpos$mutable = pPos.mutable();
         boolean flag = false;

         for(Direction direction : Direction.values()) {
            if (pLevel.getBlockState(blockpos$mutable.set(pPos).move(direction)).is(Blocks.OBSIDIAN)) {
               flag = true;
               break;
            }
         }

         if (!flag) {
            return false;
         } else {
            Direction.Axis direction$axis = pDirection.getAxis().isHorizontal() ? pDirection.getCounterClockWise().getAxis() : Direction.Plane.HORIZONTAL.getRandomAxis(pLevel.random);
            return PortalSize.findEmptyPortalShape(pLevel, pPos, direction$axis).isPresent();
         }
      }
   }
}
