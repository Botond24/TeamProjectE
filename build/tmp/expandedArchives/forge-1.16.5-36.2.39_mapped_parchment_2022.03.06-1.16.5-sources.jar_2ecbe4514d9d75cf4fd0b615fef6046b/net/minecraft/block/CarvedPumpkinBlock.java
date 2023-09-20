package net.minecraft.block;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.material.Material;
import net.minecraft.block.pattern.BlockMaterialMatcher;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.BlockStateMatcher;
import net.minecraft.enchantment.IArmorVanishable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class CarvedPumpkinBlock extends HorizontalBlock implements IArmorVanishable {
   public static final DirectionProperty FACING = HorizontalBlock.FACING;
   @Nullable
   private BlockPattern snowGolemBase;
   @Nullable
   private BlockPattern snowGolemFull;
   @Nullable
   private BlockPattern ironGolemBase;
   @Nullable
   private BlockPattern ironGolemFull;
   private static final Predicate<BlockState> PUMPKINS_PREDICATE = (p_210301_0_) -> {
      return p_210301_0_ != null && (p_210301_0_.is(Blocks.CARVED_PUMPKIN) || p_210301_0_.is(Blocks.JACK_O_LANTERN));
   };

   public CarvedPumpkinBlock(AbstractBlock.Properties p_i48432_1_) {
      super(p_i48432_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
   }

   public void onPlace(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (!pOldState.is(pState.getBlock())) {
         this.trySpawnGolem(pLevel, pPos);
      }
   }

   public boolean canSpawnGolem(IWorldReader pLevel, BlockPos pPos) {
      return this.getOrCreateSnowGolemBase().find(pLevel, pPos) != null || this.getOrCreateIronGolemBase().find(pLevel, pPos) != null;
   }

   private void trySpawnGolem(World pLevel, BlockPos pPos) {
      BlockPattern.PatternHelper blockpattern$patternhelper = this.getOrCreateSnowGolemFull().find(pLevel, pPos);
      if (blockpattern$patternhelper != null) {
         for(int i = 0; i < this.getOrCreateSnowGolemFull().getHeight(); ++i) {
            CachedBlockInfo cachedblockinfo = blockpattern$patternhelper.getBlock(0, i, 0);
            pLevel.setBlock(cachedblockinfo.getPos(), Blocks.AIR.defaultBlockState(), 2);
            pLevel.levelEvent(2001, cachedblockinfo.getPos(), Block.getId(cachedblockinfo.getState()));
         }

         SnowGolemEntity snowgolementity = EntityType.SNOW_GOLEM.create(pLevel);
         BlockPos blockpos1 = blockpattern$patternhelper.getBlock(0, 2, 0).getPos();
         snowgolementity.moveTo((double)blockpos1.getX() + 0.5D, (double)blockpos1.getY() + 0.05D, (double)blockpos1.getZ() + 0.5D, 0.0F, 0.0F);
         pLevel.addFreshEntity(snowgolementity);

         for(ServerPlayerEntity serverplayerentity : pLevel.getEntitiesOfClass(ServerPlayerEntity.class, snowgolementity.getBoundingBox().inflate(5.0D))) {
            CriteriaTriggers.SUMMONED_ENTITY.trigger(serverplayerentity, snowgolementity);
         }

         for(int l = 0; l < this.getOrCreateSnowGolemFull().getHeight(); ++l) {
            CachedBlockInfo cachedblockinfo3 = blockpattern$patternhelper.getBlock(0, l, 0);
            pLevel.blockUpdated(cachedblockinfo3.getPos(), Blocks.AIR);
         }
      } else {
         blockpattern$patternhelper = this.getOrCreateIronGolemFull().find(pLevel, pPos);
         if (blockpattern$patternhelper != null) {
            for(int j = 0; j < this.getOrCreateIronGolemFull().getWidth(); ++j) {
               for(int k = 0; k < this.getOrCreateIronGolemFull().getHeight(); ++k) {
                  CachedBlockInfo cachedblockinfo2 = blockpattern$patternhelper.getBlock(j, k, 0);
                  pLevel.setBlock(cachedblockinfo2.getPos(), Blocks.AIR.defaultBlockState(), 2);
                  pLevel.levelEvent(2001, cachedblockinfo2.getPos(), Block.getId(cachedblockinfo2.getState()));
               }
            }

            BlockPos blockpos = blockpattern$patternhelper.getBlock(1, 2, 0).getPos();
            IronGolemEntity irongolementity = EntityType.IRON_GOLEM.create(pLevel);
            irongolementity.setPlayerCreated(true);
            irongolementity.moveTo((double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.05D, (double)blockpos.getZ() + 0.5D, 0.0F, 0.0F);
            pLevel.addFreshEntity(irongolementity);

            for(ServerPlayerEntity serverplayerentity1 : pLevel.getEntitiesOfClass(ServerPlayerEntity.class, irongolementity.getBoundingBox().inflate(5.0D))) {
               CriteriaTriggers.SUMMONED_ENTITY.trigger(serverplayerentity1, irongolementity);
            }

            for(int i1 = 0; i1 < this.getOrCreateIronGolemFull().getWidth(); ++i1) {
               for(int j1 = 0; j1 < this.getOrCreateIronGolemFull().getHeight(); ++j1) {
                  CachedBlockInfo cachedblockinfo1 = blockpattern$patternhelper.getBlock(i1, j1, 0);
                  pLevel.blockUpdated(cachedblockinfo1.getPos(), Blocks.AIR);
               }
            }
         }
      }

   }

   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(FACING);
   }

   private BlockPattern getOrCreateSnowGolemBase() {
      if (this.snowGolemBase == null) {
         this.snowGolemBase = BlockPatternBuilder.start().aisle(" ", "#", "#").where('#', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(Blocks.SNOW_BLOCK))).build();
      }

      return this.snowGolemBase;
   }

   private BlockPattern getOrCreateSnowGolemFull() {
      if (this.snowGolemFull == null) {
         this.snowGolemFull = BlockPatternBuilder.start().aisle("^", "#", "#").where('^', CachedBlockInfo.hasState(PUMPKINS_PREDICATE)).where('#', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(Blocks.SNOW_BLOCK))).build();
      }

      return this.snowGolemFull;
   }

   private BlockPattern getOrCreateIronGolemBase() {
      if (this.ironGolemBase == null) {
         this.ironGolemBase = BlockPatternBuilder.start().aisle("~ ~", "###", "~#~").where('#', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(Blocks.IRON_BLOCK))).where('~', CachedBlockInfo.hasState(BlockMaterialMatcher.forMaterial(Material.AIR))).build();
      }

      return this.ironGolemBase;
   }

   private BlockPattern getOrCreateIronGolemFull() {
      if (this.ironGolemFull == null) {
         this.ironGolemFull = BlockPatternBuilder.start().aisle("~^~", "###", "~#~").where('^', CachedBlockInfo.hasState(PUMPKINS_PREDICATE)).where('#', CachedBlockInfo.hasState(BlockStateMatcher.forBlock(Blocks.IRON_BLOCK))).where('~', CachedBlockInfo.hasState(BlockMaterialMatcher.forMaterial(Material.AIR))).build();
      }

      return this.ironGolemFull;
   }
}