package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class TurtleEggBlock extends Block {
   private static final VoxelShape ONE_EGG_AABB = Block.box(3.0D, 0.0D, 3.0D, 12.0D, 7.0D, 12.0D);
   private static final VoxelShape MULTIPLE_EGGS_AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 7.0D, 15.0D);
   public static final IntegerProperty HATCH = BlockStateProperties.HATCH;
   public static final IntegerProperty EGGS = BlockStateProperties.EGGS;

   public TurtleEggBlock(AbstractBlock.Properties p_i48778_1_) {
      super(p_i48778_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, Integer.valueOf(0)).setValue(EGGS, Integer.valueOf(1)));
   }

   public void stepOn(World p_176199_1_, BlockPos p_176199_2_, Entity p_176199_3_) {
      this.destroyEgg(p_176199_1_, p_176199_2_, p_176199_3_, 100);
      super.stepOn(p_176199_1_, p_176199_2_, p_176199_3_);
   }

   public void fallOn(World p_180658_1_, BlockPos p_180658_2_, Entity p_180658_3_, float p_180658_4_) {
      if (!(p_180658_3_ instanceof ZombieEntity)) {
         this.destroyEgg(p_180658_1_, p_180658_2_, p_180658_3_, 3);
      }

      super.fallOn(p_180658_1_, p_180658_2_, p_180658_3_, p_180658_4_);
   }

   private void destroyEgg(World p_203167_1_, BlockPos p_203167_2_, Entity p_203167_3_, int p_203167_4_) {
      if (this.canDestroyEgg(p_203167_1_, p_203167_3_)) {
         if (!p_203167_1_.isClientSide && p_203167_1_.random.nextInt(p_203167_4_) == 0) {
            BlockState blockstate = p_203167_1_.getBlockState(p_203167_2_);
            if (blockstate.is(Blocks.TURTLE_EGG)) {
               this.decreaseEggs(p_203167_1_, p_203167_2_, blockstate);
            }
         }

      }
   }

   private void decreaseEggs(World pLevel, BlockPos pPos, BlockState pState) {
      pLevel.playSound((PlayerEntity)null, pPos, SoundEvents.TURTLE_EGG_BREAK, SoundCategory.BLOCKS, 0.7F, 0.9F + pLevel.random.nextFloat() * 0.2F);
      int i = pState.getValue(EGGS);
      if (i <= 1) {
         pLevel.destroyBlock(pPos, false);
      } else {
         pLevel.setBlock(pPos, pState.setValue(EGGS, Integer.valueOf(i - 1)), 2);
         pLevel.levelEvent(2001, pPos, Block.getId(pState));
      }

   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
      if (this.shouldUpdateHatchLevel(pLevel) && onSand(pLevel, pPos)) {
         int i = pState.getValue(HATCH);
         if (i < 2) {
            pLevel.playSound((PlayerEntity)null, pPos, SoundEvents.TURTLE_EGG_CRACK, SoundCategory.BLOCKS, 0.7F, 0.9F + pRandom.nextFloat() * 0.2F);
            pLevel.setBlock(pPos, pState.setValue(HATCH, Integer.valueOf(i + 1)), 2);
         } else {
            pLevel.playSound((PlayerEntity)null, pPos, SoundEvents.TURTLE_EGG_HATCH, SoundCategory.BLOCKS, 0.7F, 0.9F + pRandom.nextFloat() * 0.2F);
            pLevel.removeBlock(pPos, false);

            for(int j = 0; j < pState.getValue(EGGS); ++j) {
               pLevel.levelEvent(2001, pPos, Block.getId(pState));
               TurtleEntity turtleentity = EntityType.TURTLE.create(pLevel);
               turtleentity.setAge(-24000);
               turtleentity.setHomePos(pPos);
               turtleentity.moveTo((double)pPos.getX() + 0.3D + (double)j * 0.2D, (double)pPos.getY(), (double)pPos.getZ() + 0.3D, 0.0F, 0.0F);
               pLevel.addFreshEntity(turtleentity);
            }
         }
      }

   }

   public static boolean onSand(IBlockReader pReader, BlockPos pPos) {
      return isSand(pReader, pPos.below());
   }

   public static boolean isSand(IBlockReader pReader, BlockPos pPos) {
      return pReader.getBlockState(pPos).is(BlockTags.SAND);
   }

   public void onPlace(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (onSand(pLevel, pPos) && !pLevel.isClientSide) {
         pLevel.levelEvent(2005, pPos, 0);
      }

   }

   private boolean shouldUpdateHatchLevel(World pLevel) {
      float f = pLevel.getTimeOfDay(1.0F);
      if ((double)f < 0.69D && (double)f > 0.65D) {
         return true;
      } else {
         return pLevel.random.nextInt(500) == 0;
      }
   }

   /**
    * Spawns the block's drops in the world. By the time this is called the Block has possibly been set to air via
    * Block.removedByPlayer
    */
   public void playerDestroy(World pLevel, PlayerEntity pPlayer, BlockPos pPos, BlockState pState, @Nullable TileEntity pTe, ItemStack pStack) {
      super.playerDestroy(pLevel, pPlayer, pPos, pState, pTe, pStack);
      this.decreaseEggs(pLevel, pPos, pState);
   }

   public boolean canBeReplaced(BlockState pState, BlockItemUseContext pUseContext) {
      return pUseContext.getItemInHand().getItem() == this.asItem() && pState.getValue(EGGS) < 4 ? true : super.canBeReplaced(pState, pUseContext);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      BlockState blockstate = pContext.getLevel().getBlockState(pContext.getClickedPos());
      return blockstate.is(this) ? blockstate.setValue(EGGS, Integer.valueOf(Math.min(4, blockstate.getValue(EGGS) + 1))) : super.getStateForPlacement(pContext);
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return pState.getValue(EGGS) > 1 ? MULTIPLE_EGGS_AABB : ONE_EGG_AABB;
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(HATCH, EGGS);
   }

   private boolean canDestroyEgg(World pLevel, Entity pEntity) {
      if (!(pEntity instanceof TurtleEntity) && !(pEntity instanceof BatEntity)) {
         if (!(pEntity instanceof LivingEntity)) {
            return false;
         } else {
            return pEntity instanceof PlayerEntity || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(pLevel, pEntity);
         }
      } else {
         return false;
      }
   }
}
