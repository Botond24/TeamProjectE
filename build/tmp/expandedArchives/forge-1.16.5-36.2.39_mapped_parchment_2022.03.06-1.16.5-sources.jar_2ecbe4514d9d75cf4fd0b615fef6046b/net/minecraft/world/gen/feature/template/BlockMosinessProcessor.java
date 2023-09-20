package net.minecraft.world.gen.feature.template;

import com.mojang.serialization.Codec;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StairsBlock;
import net.minecraft.state.properties.Half;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class BlockMosinessProcessor extends StructureProcessor {
   public static final Codec<BlockMosinessProcessor> CODEC = Codec.FLOAT.fieldOf("mossiness").xmap(BlockMosinessProcessor::new, (p_237064_0_) -> {
      return p_237064_0_.mossiness;
   }).codec();
   private final float mossiness;

   public BlockMosinessProcessor(float p_i232115_1_) {
      this.mossiness = p_i232115_1_;
   }

   @Nullable
   public Template.BlockInfo processBlock(IWorldReader pLevel, BlockPos p_230386_2_, BlockPos p_230386_3_, Template.BlockInfo p_230386_4_, Template.BlockInfo p_230386_5_, PlacementSettings pSettings) {
      Random random = pSettings.getRandom(p_230386_5_.pos);
      BlockState blockstate = p_230386_5_.state;
      BlockPos blockpos = p_230386_5_.pos;
      BlockState blockstate1 = null;
      if (!blockstate.is(Blocks.STONE_BRICKS) && !blockstate.is(Blocks.STONE) && !blockstate.is(Blocks.CHISELED_STONE_BRICKS)) {
         if (blockstate.is(BlockTags.STAIRS)) {
            blockstate1 = this.maybeReplaceStairs(random, p_230386_5_.state);
         } else if (blockstate.is(BlockTags.SLABS)) {
            blockstate1 = this.maybeReplaceSlab(random);
         } else if (blockstate.is(BlockTags.WALLS)) {
            blockstate1 = this.maybeReplaceWall(random);
         } else if (blockstate.is(Blocks.OBSIDIAN)) {
            blockstate1 = this.maybeReplaceObsidian(random);
         }
      } else {
         blockstate1 = this.maybeReplaceFullStoneBlock(random);
      }

      return blockstate1 != null ? new Template.BlockInfo(blockpos, blockstate1, p_230386_5_.nbt) : p_230386_5_;
   }

   @Nullable
   private BlockState maybeReplaceFullStoneBlock(Random pRandom) {
      if (pRandom.nextFloat() >= 0.5F) {
         return null;
      } else {
         BlockState[] ablockstate = new BlockState[]{Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), getRandomFacingStairs(pRandom, Blocks.STONE_BRICK_STAIRS)};
         BlockState[] ablockstate1 = new BlockState[]{Blocks.MOSSY_STONE_BRICKS.defaultBlockState(), getRandomFacingStairs(pRandom, Blocks.MOSSY_STONE_BRICK_STAIRS)};
         return this.getRandomBlock(pRandom, ablockstate, ablockstate1);
      }
   }

   @Nullable
   private BlockState maybeReplaceStairs(Random pRandom, BlockState pState) {
      Direction direction = pState.getValue(StairsBlock.FACING);
      Half half = pState.getValue(StairsBlock.HALF);
      if (pRandom.nextFloat() >= 0.5F) {
         return null;
      } else {
         BlockState[] ablockstate = new BlockState[]{Blocks.STONE_SLAB.defaultBlockState(), Blocks.STONE_BRICK_SLAB.defaultBlockState()};
         BlockState[] ablockstate1 = new BlockState[]{Blocks.MOSSY_STONE_BRICK_STAIRS.defaultBlockState().setValue(StairsBlock.FACING, direction).setValue(StairsBlock.HALF, half), Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState()};
         return this.getRandomBlock(pRandom, ablockstate, ablockstate1);
      }
   }

   @Nullable
   private BlockState maybeReplaceSlab(Random pRandom) {
      return pRandom.nextFloat() < this.mossiness ? Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState() : null;
   }

   @Nullable
   private BlockState maybeReplaceWall(Random pRandom) {
      return pRandom.nextFloat() < this.mossiness ? Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState() : null;
   }

   @Nullable
   private BlockState maybeReplaceObsidian(Random pRandom) {
      return pRandom.nextFloat() < 0.15F ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : null;
   }

   private static BlockState getRandomFacingStairs(Random pRandom, Block pBlock) {
      return pBlock.defaultBlockState().setValue(StairsBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(pRandom)).setValue(StairsBlock.HALF, Half.values()[pRandom.nextInt(Half.values().length)]);
   }

   private BlockState getRandomBlock(Random pRandom, BlockState[] p_237069_2_, BlockState[] p_237069_3_) {
      return pRandom.nextFloat() < this.mossiness ? getRandomBlock(pRandom, p_237069_3_) : getRandomBlock(pRandom, p_237069_2_);
   }

   private static BlockState getRandomBlock(Random pRandom, BlockState[] pPossibleStates) {
      return pPossibleStates[pRandom.nextInt(pPossibleStates.length)];
   }

   protected IStructureProcessorType<?> getType() {
      return IStructureProcessorType.BLOCK_AGE;
   }
}