package net.minecraft.block;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ISidedInventoryProvider;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ComposterBlock extends Block implements ISidedInventoryProvider {
   public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_COMPOSTER;
   public static final Object2FloatMap<IItemProvider> COMPOSTABLES = new Object2FloatOpenHashMap<>();
   private static final VoxelShape OUTER_SHAPE = VoxelShapes.block();
   private static final VoxelShape[] SHAPES = Util.make(new VoxelShape[9], (p_220291_0_) -> {
      for(int i = 0; i < 8; ++i) {
         p_220291_0_[i] = VoxelShapes.join(OUTER_SHAPE, Block.box(2.0D, (double)Math.max(2, 1 + i * 2), 2.0D, 14.0D, 16.0D, 14.0D), IBooleanFunction.ONLY_FIRST);
      }

      p_220291_0_[8] = p_220291_0_[7];
   });

   public static void bootStrap() {
      COMPOSTABLES.defaultReturnValue(-1.0F);
      float f = 0.3F;
      float f1 = 0.5F;
      float f2 = 0.65F;
      float f3 = 0.85F;
      float f4 = 1.0F;
      add(0.3F, Items.JUNGLE_LEAVES);
      add(0.3F, Items.OAK_LEAVES);
      add(0.3F, Items.SPRUCE_LEAVES);
      add(0.3F, Items.DARK_OAK_LEAVES);
      add(0.3F, Items.ACACIA_LEAVES);
      add(0.3F, Items.BIRCH_LEAVES);
      add(0.3F, Items.OAK_SAPLING);
      add(0.3F, Items.SPRUCE_SAPLING);
      add(0.3F, Items.BIRCH_SAPLING);
      add(0.3F, Items.JUNGLE_SAPLING);
      add(0.3F, Items.ACACIA_SAPLING);
      add(0.3F, Items.DARK_OAK_SAPLING);
      add(0.3F, Items.BEETROOT_SEEDS);
      add(0.3F, Items.DRIED_KELP);
      add(0.3F, Items.GRASS);
      add(0.3F, Items.KELP);
      add(0.3F, Items.MELON_SEEDS);
      add(0.3F, Items.PUMPKIN_SEEDS);
      add(0.3F, Items.SEAGRASS);
      add(0.3F, Items.SWEET_BERRIES);
      add(0.3F, Items.WHEAT_SEEDS);
      add(0.5F, Items.DRIED_KELP_BLOCK);
      add(0.5F, Items.TALL_GRASS);
      add(0.5F, Items.CACTUS);
      add(0.5F, Items.SUGAR_CANE);
      add(0.5F, Items.VINE);
      add(0.5F, Items.NETHER_SPROUTS);
      add(0.5F, Items.WEEPING_VINES);
      add(0.5F, Items.TWISTING_VINES);
      add(0.5F, Items.MELON_SLICE);
      add(0.65F, Items.SEA_PICKLE);
      add(0.65F, Items.LILY_PAD);
      add(0.65F, Items.PUMPKIN);
      add(0.65F, Items.CARVED_PUMPKIN);
      add(0.65F, Items.MELON);
      add(0.65F, Items.APPLE);
      add(0.65F, Items.BEETROOT);
      add(0.65F, Items.CARROT);
      add(0.65F, Items.COCOA_BEANS);
      add(0.65F, Items.POTATO);
      add(0.65F, Items.WHEAT);
      add(0.65F, Items.BROWN_MUSHROOM);
      add(0.65F, Items.RED_MUSHROOM);
      add(0.65F, Items.MUSHROOM_STEM);
      add(0.65F, Items.CRIMSON_FUNGUS);
      add(0.65F, Items.WARPED_FUNGUS);
      add(0.65F, Items.NETHER_WART);
      add(0.65F, Items.CRIMSON_ROOTS);
      add(0.65F, Items.WARPED_ROOTS);
      add(0.65F, Items.SHROOMLIGHT);
      add(0.65F, Items.DANDELION);
      add(0.65F, Items.POPPY);
      add(0.65F, Items.BLUE_ORCHID);
      add(0.65F, Items.ALLIUM);
      add(0.65F, Items.AZURE_BLUET);
      add(0.65F, Items.RED_TULIP);
      add(0.65F, Items.ORANGE_TULIP);
      add(0.65F, Items.WHITE_TULIP);
      add(0.65F, Items.PINK_TULIP);
      add(0.65F, Items.OXEYE_DAISY);
      add(0.65F, Items.CORNFLOWER);
      add(0.65F, Items.LILY_OF_THE_VALLEY);
      add(0.65F, Items.WITHER_ROSE);
      add(0.65F, Items.FERN);
      add(0.65F, Items.SUNFLOWER);
      add(0.65F, Items.LILAC);
      add(0.65F, Items.ROSE_BUSH);
      add(0.65F, Items.PEONY);
      add(0.65F, Items.LARGE_FERN);
      add(0.85F, Items.HAY_BLOCK);
      add(0.85F, Items.BROWN_MUSHROOM_BLOCK);
      add(0.85F, Items.RED_MUSHROOM_BLOCK);
      add(0.85F, Items.NETHER_WART_BLOCK);
      add(0.85F, Items.WARPED_WART_BLOCK);
      add(0.85F, Items.BREAD);
      add(0.85F, Items.BAKED_POTATO);
      add(0.85F, Items.COOKIE);
      add(1.0F, Items.CAKE);
      add(1.0F, Items.PUMPKIN_PIE);
   }

   private static void add(float pChance, IItemProvider pItem) {
      COMPOSTABLES.put(pItem.asItem(), pChance);
   }

   public ComposterBlock(AbstractBlock.Properties p_i49986_1_) {
      super(p_i49986_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(0)));
   }

   @OnlyIn(Dist.CLIENT)
   public static void handleFill(World pLevel, BlockPos pPos, boolean pSuccess) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      pLevel.playLocalSound((double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ(), pSuccess ? SoundEvents.COMPOSTER_FILL_SUCCESS : SoundEvents.COMPOSTER_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
      double d0 = blockstate.getShape(pLevel, pPos).max(Direction.Axis.Y, 0.5D, 0.5D) + 0.03125D;
      double d1 = (double)0.13125F;
      double d2 = (double)0.7375F;
      Random random = pLevel.getRandom();

      for(int i = 0; i < 10; ++i) {
         double d3 = random.nextGaussian() * 0.02D;
         double d4 = random.nextGaussian() * 0.02D;
         double d5 = random.nextGaussian() * 0.02D;
         pLevel.addParticle(ParticleTypes.COMPOSTER, (double)pPos.getX() + (double)0.13125F + (double)0.7375F * (double)random.nextFloat(), (double)pPos.getY() + d0 + (double)random.nextFloat() * (1.0D - d0), (double)pPos.getZ() + (double)0.13125F + (double)0.7375F * (double)random.nextFloat(), d3, d4, d5);
      }

   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return SHAPES[pState.getValue(LEVEL)];
   }

   public VoxelShape getInteractionShape(BlockState pState, IBlockReader pLevel, BlockPos pPos) {
      return OUTER_SHAPE;
   }

   public VoxelShape getCollisionShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return SHAPES[0];
   }

   public void onPlace(BlockState pState, World pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
      if (pState.getValue(LEVEL) == 7) {
         pLevel.getBlockTicks().scheduleTick(pPos, pState.getBlock(), 20);
      }

   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      int i = pState.getValue(LEVEL);
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      if (i < 8 && COMPOSTABLES.containsKey(itemstack.getItem())) {
         if (i < 7 && !pLevel.isClientSide) {
            BlockState blockstate = addItem(pState, pLevel, pPos, itemstack);
            pLevel.levelEvent(1500, pPos, pState != blockstate ? 1 : 0);
            if (!pPlayer.abilities.instabuild) {
               itemstack.shrink(1);
            }
         }

         return ActionResultType.sidedSuccess(pLevel.isClientSide);
      } else if (i == 8) {
         extractProduce(pState, pLevel, pPos);
         return ActionResultType.sidedSuccess(pLevel.isClientSide);
      } else {
         return ActionResultType.PASS;
      }
   }

   public static BlockState insertItem(BlockState pState, ServerWorld pLevel, ItemStack pStack, BlockPos pPos) {
      int i = pState.getValue(LEVEL);
      if (i < 7 && COMPOSTABLES.containsKey(pStack.getItem())) {
         BlockState blockstate = addItem(pState, pLevel, pPos, pStack);
         pStack.shrink(1);
         return blockstate;
      } else {
         return pState;
      }
   }

   public static BlockState extractProduce(BlockState pState, World pLevel, BlockPos pPos) {
      if (!pLevel.isClientSide) {
         float f = 0.7F;
         double d0 = (double)(pLevel.random.nextFloat() * 0.7F) + (double)0.15F;
         double d1 = (double)(pLevel.random.nextFloat() * 0.7F) + (double)0.060000002F + 0.6D;
         double d2 = (double)(pLevel.random.nextFloat() * 0.7F) + (double)0.15F;
         ItemEntity itementity = new ItemEntity(pLevel, (double)pPos.getX() + d0, (double)pPos.getY() + d1, (double)pPos.getZ() + d2, new ItemStack(Items.BONE_MEAL));
         itementity.setDefaultPickUpDelay();
         pLevel.addFreshEntity(itementity);
      }

      BlockState blockstate = empty(pState, pLevel, pPos);
      pLevel.playSound((PlayerEntity)null, pPos, SoundEvents.COMPOSTER_EMPTY, SoundCategory.BLOCKS, 1.0F, 1.0F);
      return blockstate;
   }

   private static BlockState empty(BlockState pState, IWorld pLevel, BlockPos pPos) {
      BlockState blockstate = pState.setValue(LEVEL, Integer.valueOf(0));
      pLevel.setBlock(pPos, blockstate, 3);
      return blockstate;
   }

   private static BlockState addItem(BlockState pState, IWorld pLevel, BlockPos pPos, ItemStack pStack) {
      int i = pState.getValue(LEVEL);
      float f = COMPOSTABLES.getFloat(pStack.getItem());
      if ((i != 0 || !(f > 0.0F)) && !(pLevel.getRandom().nextDouble() < (double)f)) {
         return pState;
      } else {
         int j = i + 1;
         BlockState blockstate = pState.setValue(LEVEL, Integer.valueOf(j));
         pLevel.setBlock(pPos, blockstate, 3);
         if (j == 7) {
            pLevel.getBlockTicks().scheduleTick(pPos, pState.getBlock(), 20);
         }

         return blockstate;
      }
   }

   public void tick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRand) {
      if (pState.getValue(LEVEL) == 7) {
         pLevel.setBlock(pPos, pState.cycle(LEVEL), 3);
         pLevel.playSound((PlayerEntity)null, pPos, SoundEvents.COMPOSTER_READY, SoundCategory.BLOCKS, 1.0F, 1.0F);
      }

   }

   /**
    * @deprecated call via {@link IBlockState#hasComparatorInputOverride()} whenever possible. Implementing/overriding
    * is fine.
    */
   public boolean hasAnalogOutputSignal(BlockState pState) {
      return true;
   }

   /**
    * @deprecated call via {@link IBlockState#getComparatorInputOverride(World,BlockPos)} whenever possible.
    * Implementing/overriding is fine.
    */
   public int getAnalogOutputSignal(BlockState pBlockState, World pLevel, BlockPos pPos) {
      return pBlockState.getValue(LEVEL);
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(LEVEL);
   }

   public boolean isPathfindable(BlockState pState, IBlockReader pLevel, BlockPos pPos, PathType pType) {
      return false;
   }

   public ISidedInventory getContainer(BlockState pState, IWorld pLevel, BlockPos pPos) {
      int i = pState.getValue(LEVEL);
      if (i == 8) {
         return new ComposterBlock.FullInventory(pState, pLevel, pPos, new ItemStack(Items.BONE_MEAL));
      } else {
         return (ISidedInventory)(i < 7 ? new ComposterBlock.PartialInventory(pState, pLevel, pPos) : new ComposterBlock.EmptyInventory());
      }
   }

   static class EmptyInventory extends Inventory implements ISidedInventory {
      public EmptyInventory() {
         super(0);
      }

      public int[] getSlotsForFace(Direction pSide) {
         return new int[0];
      }

      /**
       * Returns true if automation can insert the given item in the given slot from the given side.
       */
      public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
         return false;
      }

      /**
       * Returns true if automation can extract the given item in the given slot from the given side.
       */
      public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
         return false;
      }
   }

   static class FullInventory extends Inventory implements ISidedInventory {
      private final BlockState state;
      private final IWorld level;
      private final BlockPos pos;
      private boolean changed;

      public FullInventory(BlockState pState, IWorld pLevel, BlockPos pPos, ItemStack p_i50463_4_) {
         super(p_i50463_4_);
         this.state = pState;
         this.level = pLevel;
         this.pos = pPos;
      }

      /**
       * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
       */
      public int getMaxStackSize() {
         return 1;
      }

      public int[] getSlotsForFace(Direction pSide) {
         return pSide == Direction.DOWN ? new int[]{0} : new int[0];
      }

      /**
       * Returns true if automation can insert the given item in the given slot from the given side.
       */
      public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
         return false;
      }

      /**
       * Returns true if automation can extract the given item in the given slot from the given side.
       */
      public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
         return !this.changed && pDirection == Direction.DOWN && pStack.getItem() == Items.BONE_MEAL;
      }

      /**
       * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think
       * it hasn't changed and skip it.
       */
      public void setChanged() {
         ComposterBlock.empty(this.state, this.level, this.pos);
         this.changed = true;
      }
   }

   static class PartialInventory extends Inventory implements ISidedInventory {
      private final BlockState state;
      private final IWorld level;
      private final BlockPos pos;
      private boolean changed;

      public PartialInventory(BlockState pState, IWorld pLevel, BlockPos pPos) {
         super(1);
         this.state = pState;
         this.level = pLevel;
         this.pos = pPos;
      }

      /**
       * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
       */
      public int getMaxStackSize() {
         return 1;
      }

      public int[] getSlotsForFace(Direction pSide) {
         return pSide == Direction.UP ? new int[]{0} : new int[0];
      }

      /**
       * Returns true if automation can insert the given item in the given slot from the given side.
       */
      public boolean canPlaceItemThroughFace(int pIndex, ItemStack pItemStack, @Nullable Direction pDirection) {
         return !this.changed && pDirection == Direction.UP && ComposterBlock.COMPOSTABLES.containsKey(pItemStack.getItem());
      }

      /**
       * Returns true if automation can extract the given item in the given slot from the given side.
       */
      public boolean canTakeItemThroughFace(int pIndex, ItemStack pStack, Direction pDirection) {
         return false;
      }

      /**
       * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think
       * it hasn't changed and skip it.
       */
      public void setChanged() {
         ItemStack itemstack = this.getItem(0);
         if (!itemstack.isEmpty()) {
            this.changed = true;
            BlockState blockstate = ComposterBlock.addItem(this.state, this.level, this.pos, itemstack);
            this.level.levelEvent(1500, this.pos, blockstate != this.state ? 1 : 0);
            this.removeItemNoUpdate(0);
         }

      }
   }
}