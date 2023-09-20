package net.minecraft.item;

import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DeadCoralWallFanBlock;
import net.minecraft.block.IGrowable;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BoneMealItem extends Item {
   public BoneMealItem(Item.Properties p_i50055_1_) {
      super(p_i50055_1_);
   }

   /**
    * Called when this item is used when targetting a Block
    */
   public ActionResultType useOn(ItemUseContext pContext) {
      World world = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      BlockPos blockpos1 = blockpos.relative(pContext.getClickedFace());
      if (applyBonemeal(pContext.getItemInHand(), world, blockpos, pContext.getPlayer())) {
         if (!world.isClientSide) {
            world.levelEvent(2005, blockpos, 0);
         }

         return ActionResultType.sidedSuccess(world.isClientSide);
      } else {
         BlockState blockstate = world.getBlockState(blockpos);
         boolean flag = blockstate.isFaceSturdy(world, blockpos, pContext.getClickedFace());
         if (flag && growWaterPlant(pContext.getItemInHand(), world, blockpos1, pContext.getClickedFace())) {
            if (!world.isClientSide) {
               world.levelEvent(2005, blockpos1, 0);
            }

            return ActionResultType.sidedSuccess(world.isClientSide);
         } else {
            return ActionResultType.PASS;
         }
      }
   }

   @Deprecated //Forge: Use Player/Hand version
   public static boolean growCrop(ItemStack pStack, World pLevel, BlockPos pPos) {
      if (pLevel instanceof net.minecraft.world.server.ServerWorld)
         return applyBonemeal(pStack, pLevel, pPos, net.minecraftforge.common.util.FakePlayerFactory.getMinecraft((net.minecraft.world.server.ServerWorld)pLevel));
      return false;
   }

   public static boolean applyBonemeal(ItemStack pStack, World pLevel, BlockPos pPos, net.minecraft.entity.player.PlayerEntity player) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      int hook = net.minecraftforge.event.ForgeEventFactory.onApplyBonemeal(player, pLevel, pPos, blockstate, pStack);
      if (hook != 0) return hook > 0;
      if (blockstate.getBlock() instanceof IGrowable) {
         IGrowable igrowable = (IGrowable)blockstate.getBlock();
         if (igrowable.isValidBonemealTarget(pLevel, pPos, blockstate, pLevel.isClientSide)) {
            if (pLevel instanceof ServerWorld) {
               if (igrowable.isBonemealSuccess(pLevel, pLevel.random, pPos, blockstate)) {
                  igrowable.performBonemeal((ServerWorld)pLevel, pLevel.random, pPos, blockstate);
               }

               pStack.shrink(1);
            }

            return true;
         }
      }

      return false;
   }

   public static boolean growWaterPlant(ItemStack pStack, World pLevel, BlockPos pPos, @Nullable Direction pClickedSide) {
      if (pLevel.getBlockState(pPos).is(Blocks.WATER) && pLevel.getFluidState(pPos).getAmount() == 8) {
         if (!(pLevel instanceof ServerWorld)) {
            return true;
         } else {
            label80:
            for(int i = 0; i < 128; ++i) {
               BlockPos blockpos = pPos;
               BlockState blockstate = Blocks.SEAGRASS.defaultBlockState();

               for(int j = 0; j < i / 16; ++j) {
                  blockpos = blockpos.offset(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1);
                  if (pLevel.getBlockState(blockpos).isCollisionShapeFullBlock(pLevel, blockpos)) {
                     continue label80;
                  }
               }

               Optional<RegistryKey<Biome>> optional = pLevel.getBiomeName(blockpos);
               if (Objects.equals(optional, Optional.of(Biomes.WARM_OCEAN)) || Objects.equals(optional, Optional.of(Biomes.DEEP_WARM_OCEAN))) {
                  if (i == 0 && pClickedSide != null && pClickedSide.getAxis().isHorizontal()) {
                     blockstate = BlockTags.WALL_CORALS.getRandomElement(pLevel.random).defaultBlockState().setValue(DeadCoralWallFanBlock.FACING, pClickedSide);
                  } else if (random.nextInt(4) == 0) {
                     blockstate = BlockTags.UNDERWATER_BONEMEALS.getRandomElement(random).defaultBlockState();
                  }
               }

               if (blockstate.getBlock().is(BlockTags.WALL_CORALS)) {
                  for(int k = 0; !blockstate.canSurvive(pLevel, blockpos) && k < 4; ++k) {
                     blockstate = blockstate.setValue(DeadCoralWallFanBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random));
                  }
               }

               if (blockstate.canSurvive(pLevel, blockpos)) {
                  BlockState blockstate1 = pLevel.getBlockState(blockpos);
                  if (blockstate1.is(Blocks.WATER) && pLevel.getFluidState(blockpos).getAmount() == 8) {
                     pLevel.setBlock(blockpos, blockstate, 3);
                  } else if (blockstate1.is(Blocks.SEAGRASS) && random.nextInt(10) == 0) {
                     ((IGrowable)Blocks.SEAGRASS).performBonemeal((ServerWorld)pLevel, random, blockpos, blockstate1);
                  }
               }
            }

            pStack.shrink(1);
            return true;
         }
      } else {
         return false;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static void addGrowthParticles(IWorld pLevel, BlockPos pPos, int pData) {
      if (pData == 0) {
         pData = 15;
      }

      BlockState blockstate = pLevel.getBlockState(pPos);
      if (!blockstate.isAir(pLevel, pPos)) {
         double d0 = 0.5D;
         double d1;
         if (blockstate.is(Blocks.WATER)) {
            pData *= 3;
            d1 = 1.0D;
            d0 = 3.0D;
         } else if (blockstate.isSolidRender(pLevel, pPos)) {
            pPos = pPos.above();
            pData *= 3;
            d0 = 3.0D;
            d1 = 1.0D;
         } else {
            d1 = blockstate.getShape(pLevel, pPos).max(Direction.Axis.Y);
         }

         pLevel.addParticle(ParticleTypes.HAPPY_VILLAGER, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);

         for(int i = 0; i < pData; ++i) {
            double d2 = random.nextGaussian() * 0.02D;
            double d3 = random.nextGaussian() * 0.02D;
            double d4 = random.nextGaussian() * 0.02D;
            double d5 = 0.5D - d0;
            double d6 = (double)pPos.getX() + d5 + random.nextDouble() * d0 * 2.0D;
            double d7 = (double)pPos.getY() + random.nextDouble() * d1;
            double d8 = (double)pPos.getZ() + d5 + random.nextDouble() * d0 * 2.0D;
            if (!pLevel.getBlockState((new BlockPos(d6, d7, d8)).below()).isAir()) {
               pLevel.addParticle(ParticleTypes.HAPPY_VILLAGER, d6, d7, d8, d2, d3, d4);
            }
         }

      }
   }
}
