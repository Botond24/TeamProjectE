package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.material.Material;
import net.minecraft.block.pattern.BlockMaterialMatcher;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.BlockStateMatcher;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

public class WitherSkeletonSkullBlock extends SkullBlock {
   @Nullable
   private static BlockPattern witherPatternFull;
   @Nullable
   private static BlockPattern witherPatternBase;

   public WitherSkeletonSkullBlock(AbstractBlock.Properties p_i48293_1_) {
      super(SkullBlock.Types.WITHER_SKELETON, p_i48293_1_);
   }

   /**
    * Called by ItemBlocks after a block is set in the world, to allow post-place logic
    */
   public void setPlacedBy(World pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
      super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      if (tileentity instanceof SkullTileEntity) {
         checkSpawn(pLevel, pPos, (SkullTileEntity)tileentity);
      }

   }

   public static void checkSpawn(World pLevel, BlockPos pPos, SkullTileEntity pBlockEntity) {
      if (!pLevel.isClientSide) {
         BlockState blockstate = pBlockEntity.getBlockState();
         boolean flag = blockstate.is(Blocks.WITHER_SKELETON_SKULL) || blockstate.is(Blocks.WITHER_SKELETON_WALL_SKULL);
         if (flag && pPos.getY() >= 0 && pLevel.getDifficulty() != Difficulty.PEACEFUL) {
            BlockPattern blockpattern = getOrCreateWitherFull();
            BlockPattern.PatternHelper blockpattern$patternhelper = blockpattern.find(pLevel, pPos);
            if (blockpattern$patternhelper != null) {
               for(int i = 0; i < blockpattern.getWidth(); ++i) {
                  for(int j = 0; j < blockpattern.getHeight(); ++j) {
                     CachedBlockInfo cachedblockinfo = blockpattern$patternhelper.getBlock(i, j, 0);
                     pLevel.setBlock(cachedblockinfo.getPos(), Blocks.AIR.defaultBlockState(), 2);
                     pLevel.levelEvent(2001, cachedblockinfo.getPos(), Block.getId(cachedblockinfo.getState()));
                  }
               }

               WitherEntity witherentity = EntityType.WITHER.create(pLevel);
               BlockPos blockpos = blockpattern$patternhelper.getBlock(1, 2, 0).getPos();
               witherentity.moveTo((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.55D, (double)blockpos.getZ() + 0.5D, blockpattern$patternhelper.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F, 0.0F);
               witherentity.yBodyRot = blockpattern$patternhelper.getForwards().getAxis() == Direction.Axis.X ? 0.0F : 90.0F;
               witherentity.makeInvulnerable();

               for(ServerPlayerEntity serverplayerentity : pLevel.getEntitiesOfClass(ServerPlayerEntity.class, witherentity.getBoundingBox().inflate(50.0D))) {
                  CriteriaTriggers.SUMMONED_ENTITY.trigger(serverplayerentity, witherentity);
               }

               pLevel.addFreshEntity(witherentity);

               for(int k = 0; k < blockpattern.getWidth(); ++k) {
                  for(int l = 0; l < blockpattern.getHeight(); ++l) {
                     pLevel.blockUpdated(blockpattern$patternhelper.getBlock(k, l, 0).getPos(), Blocks.AIR);
                  }
               }

            }
         }
      }
   }

   public static boolean canSpawnMob(World pLevel, BlockPos pPos, ItemStack pStack) {
      if (pStack.getItem() == Items.WITHER_SKELETON_SKULL && pPos.getY() >= 2 && pLevel.getDifficulty() != Difficulty.PEACEFUL && !pLevel.isClientSide) {
         return getOrCreateWitherBase().find(pLevel, pPos) != null;
      } else {
         return false;
      }
   }

   private static BlockPattern getOrCreateWitherFull() {
      if (witherPatternFull == null) {
         witherPatternFull = BlockPatternBuilder.start().aisle("^^^", "###", "~#~").where('#', (p_235639_0_) -> {
            return p_235639_0_.getState().is(BlockTags.WITHER_SUMMON_BASE_BLOCKS);
         }).where('^', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(Blocks.WITHER_SKELETON_SKULL).or(BlockStateMatcher.forBlock(Blocks.WITHER_SKELETON_WALL_SKULL)))).where('~', CachedBlockInfo.hasState(BlockMaterialMatcher.forMaterial(Material.AIR))).build();
      }

      return witherPatternFull;
   }

   private static BlockPattern getOrCreateWitherBase() {
      if (witherPatternBase == null) {
         witherPatternBase = BlockPatternBuilder.start().aisle("   ", "###", "~#~").where('#', (p_235638_0_) -> {
            return p_235638_0_.getState().is(BlockTags.WITHER_SUMMON_BASE_BLOCKS);
         }).where('~', CachedBlockInfo.hasState(BlockMaterialMatcher.forMaterial(Material.AIR))).build();
      }

      return witherPatternBase;
   }
}