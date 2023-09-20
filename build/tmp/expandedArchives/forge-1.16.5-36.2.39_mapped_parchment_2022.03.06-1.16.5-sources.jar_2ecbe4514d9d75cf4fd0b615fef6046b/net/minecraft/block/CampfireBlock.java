package net.minecraft.block;

import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CampfireCookingRecipe;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.CampfireTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CampfireBlock extends ContainerBlock implements IWaterLoggable {
   protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 7.0D, 16.0D);
   public static final BooleanProperty LIT = BlockStateProperties.LIT;
   public static final BooleanProperty SIGNAL_FIRE = BlockStateProperties.SIGNAL_FIRE;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   private static final VoxelShape VIRTUAL_FENCE_POST = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 16.0D, 10.0D);
   private final boolean spawnParticles;
   private final int fireDamage;

   public CampfireBlock(boolean pSpawnParticles, int pFireDamage, AbstractBlock.Properties pProperties) {
      super(pProperties);
      this.spawnParticles = pSpawnParticles;
      this.fireDamage = pFireDamage;
      this.registerDefaultState(this.stateDefinition.any().setValue(LIT, Boolean.valueOf(true)).setValue(SIGNAL_FIRE, Boolean.valueOf(false)).setValue(WATERLOGGED, Boolean.valueOf(false)).setValue(FACING, Direction.NORTH));
   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      if (tileentity instanceof CampfireTileEntity) {
         CampfireTileEntity campfiretileentity = (CampfireTileEntity)tileentity;
         ItemStack itemstack = pPlayer.getItemInHand(pHand);
         Optional<CampfireCookingRecipe> optional = campfiretileentity.getCookableRecipe(itemstack);
         if (optional.isPresent()) {
            if (!pLevel.isClientSide && campfiretileentity.placeFood(pPlayer.abilities.instabuild ? itemstack.copy() : itemstack, optional.get().getCookingTime())) {
               pPlayer.awardStat(Stats.INTERACT_WITH_CAMPFIRE);
               return ActionResultType.SUCCESS;
            }

            return ActionResultType.CONSUME;
         }
      }

      return ActionResultType.PASS;
   }

   public void entityInside(BlockState pState, World pLevel, BlockPos pPos, Entity pEntity) {
      if (!pEntity.fireImmune() && pState.getValue(LIT) && pEntity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)pEntity)) {
         pEntity.hurt(DamageSource.IN_FIRE, (float)this.fireDamage);
      }

      super.entityInside(pState, pLevel, pPos, pEntity);
   }

   public void onRemove(BlockState pState, World pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
      if (!pState.is(pNewState.getBlock())) {
         TileEntity tileentity = pLevel.getBlockEntity(pPos);
         if (tileentity instanceof CampfireTileEntity) {
            InventoryHelper.dropContents(pLevel, pPos, ((CampfireTileEntity)tileentity).getItems());
         }

         super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      IWorld iworld = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      boolean flag = iworld.getFluidState(blockpos).getType() == Fluids.WATER;
      return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(flag)).setValue(SIGNAL_FIRE, Boolean.valueOf(this.isSmokeSource(iworld.getBlockState(blockpos.below())))).setValue(LIT, Boolean.valueOf(!flag)).setValue(FACING, pContext.getHorizontalDirection());
   }

   /**
    * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific face passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pState.getValue(WATERLOGGED)) {
         pLevel.getLiquidTicks().scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
      }

      return pFacing == Direction.DOWN ? pState.setValue(SIGNAL_FIRE, Boolean.valueOf(this.isSmokeSource(pFacingState))) : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   /**
    * @return whether the given block state produces the thicker signal fire smoke when put below a campfire.
    */
   private boolean isSmokeSource(BlockState pState) {
      return pState.is(Blocks.HAY_BLOCK);
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return SHAPE;
   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link IBlockState#getRenderType()} whenever possible. Implementing/overriding is fine.
    */
   public BlockRenderType getRenderShape(BlockState pState) {
      return BlockRenderType.MODEL;
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
    * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
    * of whether the block can receive random update ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
      if (pState.getValue(LIT)) {
         if (pRand.nextInt(10) == 0) {
            pLevel.playLocalSound((double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, SoundEvents.CAMPFIRE_CRACKLE, SoundCategory.BLOCKS, 0.5F + pRand.nextFloat(), pRand.nextFloat() * 0.7F + 0.6F, false);
         }

         if (this.spawnParticles && pRand.nextInt(5) == 0) {
            for(int i = 0; i < pRand.nextInt(1) + 1; ++i) {
               pLevel.addParticle(ParticleTypes.LAVA, (double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, (double)(pRand.nextFloat() / 2.0F), 5.0E-5D, (double)(pRand.nextFloat() / 2.0F));
            }
         }

      }
   }

   public static void dowse(IWorld p_235475_0_, BlockPos p_235475_1_, BlockState p_235475_2_) {
      if (p_235475_0_.isClientSide()) {
         for(int i = 0; i < 20; ++i) {
            makeParticles((World)p_235475_0_, p_235475_1_, p_235475_2_.getValue(SIGNAL_FIRE), true);
         }
      }

      TileEntity tileentity = p_235475_0_.getBlockEntity(p_235475_1_);
      if (tileentity instanceof CampfireTileEntity) {
         ((CampfireTileEntity)tileentity).dowse();
      }

   }

   public boolean placeLiquid(IWorld pLevel, BlockPos pPos, BlockState pState, FluidState pFluidState) {
      if (!pState.getValue(BlockStateProperties.WATERLOGGED) && pFluidState.getType() == Fluids.WATER) {
         boolean flag = pState.getValue(LIT);
         if (flag) {
            if (!pLevel.isClientSide()) {
               pLevel.playSound((PlayerEntity)null, pPos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            dowse(pLevel, pPos, pState);
         }

         pLevel.setBlock(pPos, pState.setValue(WATERLOGGED, Boolean.valueOf(true)).setValue(LIT, Boolean.valueOf(false)), 3);
         pLevel.getLiquidTicks().scheduleTick(pPos, pFluidState.getType(), pFluidState.getType().getTickDelay(pLevel));
         return true;
      } else {
         return false;
      }
   }

   public void onProjectileHit(World pLevel, BlockState pState, BlockRayTraceResult pHit, ProjectileEntity pProjectile) {
      if (!pLevel.isClientSide && pProjectile.isOnFire()) {
         Entity entity = pProjectile.getOwner();
         boolean flag = entity == null || entity instanceof PlayerEntity || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(pLevel, entity);
         if (flag && !pState.getValue(LIT) && !pState.getValue(WATERLOGGED)) {
            BlockPos blockpos = pHit.getBlockPos();
            pLevel.setBlock(blockpos, pState.setValue(BlockStateProperties.LIT, Boolean.valueOf(true)), 11);
         }
      }

   }

   public static void makeParticles(World pLevel, BlockPos pPos, boolean pIsSignalFire, boolean pSpawnExtraSmoke) {
      Random random = pLevel.getRandom();
      BasicParticleType basicparticletype = pIsSignalFire ? ParticleTypes.CAMPFIRE_SIGNAL_SMOKE : ParticleTypes.CAMPFIRE_COSY_SMOKE;
      pLevel.addAlwaysVisibleParticle(basicparticletype, true, (double)pPos.getX() + 0.5D + random.nextDouble() / 3.0D * (double)(random.nextBoolean() ? 1 : -1), (double)pPos.getY() + random.nextDouble() + random.nextDouble(), (double)pPos.getZ() + 0.5D + random.nextDouble() / 3.0D * (double)(random.nextBoolean() ? 1 : -1), 0.0D, 0.07D, 0.0D);
      if (pSpawnExtraSmoke) {
         pLevel.addParticle(ParticleTypes.SMOKE, (double)pPos.getX() + 0.25D + random.nextDouble() / 2.0D * (double)(random.nextBoolean() ? 1 : -1), (double)pPos.getY() + 0.4D, (double)pPos.getZ() + 0.25D + random.nextDouble() / 2.0D * (double)(random.nextBoolean() ? 1 : -1), 0.0D, 0.005D, 0.0D);
      }

   }

   public static boolean isSmokeyPos(World pLevel, BlockPos pPos) {
      for(int i = 1; i <= 5; ++i) {
         BlockPos blockpos = pPos.below(i);
         BlockState blockstate = pLevel.getBlockState(blockpos);
         if (isLitCampfire(blockstate)) {
            return true;
         }

         boolean flag = VoxelShapes.joinIsNotEmpty(VIRTUAL_FENCE_POST, blockstate.getCollisionShape(pLevel, blockpos, ISelectionContext.empty()), IBooleanFunction.AND);//Forge fix: MC-201374
         if (flag) {
            BlockState blockstate1 = pLevel.getBlockState(blockpos.below());
            return isLitCampfire(blockstate1);
         }
      }

      return false;
   }

   public static boolean isLitCampfire(BlockState pState) {
      return pState.hasProperty(LIT) && pState.is(BlockTags.CAMPFIRES) && pState.getValue(LIT);
   }

   public FluidState getFluidState(BlockState pState) {
      return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link IBlockState#withRotation(Rotation)} whenever possible. Implementing/overriding is
    * fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRotation) {
      return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
   }

   /**
    * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link IBlockState#withMirror(Mirror)} whenever possible. Implementing/overriding is fine.
    */
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      return pState.rotate(pMirror.getRotation(pState.getValue(FACING)));
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(LIT, SIGNAL_FIRE, WATERLOGGED, FACING);
   }

   public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
      return new CampfireTileEntity();
   }

   public boolean isPathfindable(BlockState pState, IBlockReader pLevel, BlockPos pPos, PathType pType) {
      return false;
   }

   public static boolean canLight(BlockState pState) {
      return pState.is(BlockTags.CAMPFIRES, (p_241469_0_) -> {
         return p_241469_0_.hasProperty(BlockStateProperties.WATERLOGGED) && p_241469_0_.hasProperty(BlockStateProperties.LIT);
      }) && !pState.getValue(BlockStateProperties.WATERLOGGED) && !pState.getValue(BlockStateProperties.LIT);
   }
}
