package net.minecraft.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.item.minecart.TNTMinecartEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BeehiveBlock extends ContainerBlock {
   private static final Direction[] SPAWN_DIRECTIONS = new Direction[]{Direction.WEST, Direction.EAST, Direction.SOUTH};
   public static final DirectionProperty FACING = HorizontalBlock.FACING;
   public static final IntegerProperty HONEY_LEVEL = BlockStateProperties.LEVEL_HONEY;

   public BeehiveBlock(AbstractBlock.Properties p_i225756_1_) {
      super(p_i225756_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(HONEY_LEVEL, Integer.valueOf(0)).setValue(FACING, Direction.NORTH));
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
      return pBlockState.getValue(HONEY_LEVEL);
   }
   // Forge: Fixed MC-227255 Beehives and bee nests do not rotate/mirror correctly in structure blocks
   @Override public BlockState rotate(BlockState blockState, net.minecraft.util.Rotation rotation) { return (BlockState)blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING))); }
   @Override public BlockState mirror(BlockState blockState, net.minecraft.util.Mirror mirror) { return blockState.rotate(mirror.getRotation(blockState.getValue(FACING))); }

   /**
    * Spawns the block's drops in the world. By the time this is called the Block has possibly been set to air via
    * Block.removedByPlayer
    */
   public void playerDestroy(World pLevel, PlayerEntity pPlayer, BlockPos pPos, BlockState pState, @Nullable TileEntity pTe, ItemStack pStack) {
      super.playerDestroy(pLevel, pPlayer, pPos, pState, pTe, pStack);
      if (!pLevel.isClientSide && pTe instanceof BeehiveTileEntity) {
         BeehiveTileEntity beehivetileentity = (BeehiveTileEntity)pTe;
         if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, pStack) == 0) {
            beehivetileentity.emptyAllLivingFromHive(pPlayer, pState, BeehiveTileEntity.State.EMERGENCY);
            pLevel.updateNeighbourForOutputSignal(pPos, this);
            this.angerNearbyBees(pLevel, pPos);
         }

         CriteriaTriggers.BEE_NEST_DESTROYED.trigger((ServerPlayerEntity)pPlayer, pState.getBlock(), pStack, beehivetileentity.getOccupantCount());
      }

   }

   private void angerNearbyBees(World pLevel, BlockPos pPos) {
      List<BeeEntity> list = pLevel.getEntitiesOfClass(BeeEntity.class, (new AxisAlignedBB(pPos)).inflate(8.0D, 6.0D, 8.0D));
      if (!list.isEmpty()) {
         List<PlayerEntity> list1 = pLevel.getEntitiesOfClass(PlayerEntity.class, (new AxisAlignedBB(pPos)).inflate(8.0D, 6.0D, 8.0D));
         if (list1.isEmpty()) return; //Forge: Prevent Error when no players are around.
         int i = list1.size();

         for(BeeEntity beeentity : list) {
            if (beeentity.getTarget() == null) {
               beeentity.setTarget(list1.get(pLevel.random.nextInt(i)));
            }
         }
      }

   }

   public static void dropHoneycomb(World pLevel, BlockPos pPos) {
      popResource(pLevel, pPos, new ItemStack(Items.HONEYCOMB, 3));
   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      ItemStack itemstack = pPlayer.getItemInHand(pHand);
      int i = pState.getValue(HONEY_LEVEL);
      boolean flag = false;
      if (i >= 5) {
         if (itemstack.getItem() == Items.SHEARS) {
            pLevel.playSound(pPlayer, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.BEEHIVE_SHEAR, SoundCategory.NEUTRAL, 1.0F, 1.0F);
            dropHoneycomb(pLevel, pPos);
            itemstack.hurtAndBreak(1, pPlayer, (p_226874_1_) -> {
               p_226874_1_.broadcastBreakEvent(pHand);
            });
            flag = true;
         } else if (itemstack.getItem() == Items.GLASS_BOTTLE) {
            itemstack.shrink(1);
            pLevel.playSound(pPlayer, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
            if (itemstack.isEmpty()) {
               pPlayer.setItemInHand(pHand, new ItemStack(Items.HONEY_BOTTLE));
            } else if (!pPlayer.inventory.add(new ItemStack(Items.HONEY_BOTTLE))) {
               pPlayer.drop(new ItemStack(Items.HONEY_BOTTLE), false);
            }

            flag = true;
         }
      }

      if (flag) {
         if (!CampfireBlock.isSmokeyPos(pLevel, pPos)) {
            if (this.hiveContainsBees(pLevel, pPos)) {
               this.angerNearbyBees(pLevel, pPos);
            }

            this.releaseBeesAndResetHoneyLevel(pLevel, pState, pPos, pPlayer, BeehiveTileEntity.State.EMERGENCY);
         } else {
            this.resetHoneyLevel(pLevel, pState, pPos);
         }

         return ActionResultType.sidedSuccess(pLevel.isClientSide);
      } else {
         return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
      }
   }

   private boolean hiveContainsBees(World pLevel, BlockPos pPos) {
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      if (tileentity instanceof BeehiveTileEntity) {
         BeehiveTileEntity beehivetileentity = (BeehiveTileEntity)tileentity;
         return !beehivetileentity.isEmpty();
      } else {
         return false;
      }
   }

   public void releaseBeesAndResetHoneyLevel(World pLevel, BlockState pState, BlockPos pPos, @Nullable PlayerEntity pPlayer, BeehiveTileEntity.State pBeeReleaseStatus) {
      this.resetHoneyLevel(pLevel, pState, pPos);
      TileEntity tileentity = pLevel.getBlockEntity(pPos);
      if (tileentity instanceof BeehiveTileEntity) {
         BeehiveTileEntity beehivetileentity = (BeehiveTileEntity)tileentity;
         beehivetileentity.emptyAllLivingFromHive(pPlayer, pState, pBeeReleaseStatus);
      }

   }

   public void resetHoneyLevel(World pLevel, BlockState pState, BlockPos pPos) {
      pLevel.setBlock(pPos, pState.setValue(HONEY_LEVEL, Integer.valueOf(0)), 3);
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
    * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
    * of whether the block can receive random update ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
      if (pState.getValue(HONEY_LEVEL) >= 5) {
         for(int i = 0; i < pRand.nextInt(1) + 1; ++i) {
            this.trySpawnDripParticles(pLevel, pPos, pState);
         }
      }

   }

   @OnlyIn(Dist.CLIENT)
   private void trySpawnDripParticles(World pLevel, BlockPos pPos, BlockState pState) {
      if (pState.getFluidState().isEmpty() && !(pLevel.random.nextFloat() < 0.3F)) {
         VoxelShape voxelshape = pState.getCollisionShape(pLevel, pPos);
         double d0 = voxelshape.max(Direction.Axis.Y);
         if (d0 >= 1.0D && !pState.is(BlockTags.IMPERMEABLE)) {
            double d1 = voxelshape.min(Direction.Axis.Y);
            if (d1 > 0.0D) {
               this.spawnParticle(pLevel, pPos, voxelshape, (double)pPos.getY() + d1 - 0.05D);
            } else {
               BlockPos blockpos = pPos.below();
               BlockState blockstate = pLevel.getBlockState(blockpos);
               VoxelShape voxelshape1 = blockstate.getCollisionShape(pLevel, blockpos);
               double d2 = voxelshape1.max(Direction.Axis.Y);
               if ((d2 < 1.0D || !blockstate.isCollisionShapeFullBlock(pLevel, blockpos)) && blockstate.getFluidState().isEmpty()) {
                  this.spawnParticle(pLevel, pPos, voxelshape, (double)pPos.getY() - 0.05D);
               }
            }
         }

      }
   }

   @OnlyIn(Dist.CLIENT)
   private void spawnParticle(World pLevel, BlockPos pPos, VoxelShape pShape, double pY) {
      this.spawnFluidParticle(pLevel, (double)pPos.getX() + pShape.min(Direction.Axis.X), (double)pPos.getX() + pShape.max(Direction.Axis.X), (double)pPos.getZ() + pShape.min(Direction.Axis.Z), (double)pPos.getZ() + pShape.max(Direction.Axis.Z), pY);
   }

   @OnlyIn(Dist.CLIENT)
   private void spawnFluidParticle(World pParticleData, double pX1, double pX2, double pZ1, double pZ2, double pY) {
      pParticleData.addParticle(ParticleTypes.DRIPPING_HONEY, MathHelper.lerp(pParticleData.random.nextDouble(), pX1, pX2), pY, MathHelper.lerp(pParticleData.random.nextDouble(), pZ1, pZ2), 0.0D, 0.0D, 0.0D);
   }

   public BlockState getStateForPlacement(BlockItemUseContext pContext) {
      return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(HONEY_LEVEL, FACING);
   }

   /**
    * The type of render function called. MODEL for mixed tesr and static model, MODELBLOCK_ANIMATED for TESR-only,
    * LIQUID for vanilla liquids, INVISIBLE to skip all rendering
    * @deprecated call via {@link IBlockState#getRenderType()} whenever possible. Implementing/overriding is fine.
    */
   public BlockRenderType getRenderShape(BlockState pState) {
      return BlockRenderType.MODEL;
   }

   @Nullable
   public TileEntity newBlockEntity(IBlockReader p_196283_1_) {
      return new BeehiveTileEntity();
   }

   /**
    * Called before the Block is set to air in the world. Called regardless of if the player's tool can actually collect
    * this block
    */
   public void playerWillDestroy(World pLevel, BlockPos pPos, BlockState pState, PlayerEntity pPlayer) {
      if (!pLevel.isClientSide && pPlayer.isCreative() && pLevel.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
         TileEntity tileentity = pLevel.getBlockEntity(pPos);
         if (tileentity instanceof BeehiveTileEntity) {
            BeehiveTileEntity beehivetileentity = (BeehiveTileEntity)tileentity;
            ItemStack itemstack = new ItemStack(this);
            int i = pState.getValue(HONEY_LEVEL);
            boolean flag = !beehivetileentity.isEmpty();
            if (!flag && i == 0) {
               return;
            }

            if (flag) {
               CompoundNBT compoundnbt = new CompoundNBT();
               compoundnbt.put("Bees", beehivetileentity.writeBees());
               itemstack.addTagElement("BlockEntityTag", compoundnbt);
            }

            CompoundNBT compoundnbt1 = new CompoundNBT();
            compoundnbt1.putInt("honey_level", i);
            itemstack.addTagElement("BlockStateTag", compoundnbt1);
            ItemEntity itementity = new ItemEntity(pLevel, (double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ(), itemstack);
            itementity.setDefaultPickUpDelay();
            pLevel.addFreshEntity(itementity);
         }
      }

      super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
   }

   public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
      Entity entity = pBuilder.getOptionalParameter(LootParameters.THIS_ENTITY);
      if (entity instanceof TNTEntity || entity instanceof CreeperEntity || entity instanceof WitherSkullEntity || entity instanceof WitherEntity || entity instanceof TNTMinecartEntity) {
         TileEntity tileentity = pBuilder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
         if (tileentity instanceof BeehiveTileEntity) {
            BeehiveTileEntity beehivetileentity = (BeehiveTileEntity)tileentity;
            beehivetileentity.emptyAllLivingFromHive((PlayerEntity)null, pState, BeehiveTileEntity.State.EMERGENCY);
         }
      }

      return super.getDrops(pState, pBuilder);
   }

   /**
    * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific face passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      if (pLevel.getBlockState(pFacingPos).getBlock() instanceof FireBlock) {
         TileEntity tileentity = pLevel.getBlockEntity(pCurrentPos);
         if (tileentity instanceof BeehiveTileEntity) {
            BeehiveTileEntity beehivetileentity = (BeehiveTileEntity)tileentity;
            beehivetileentity.emptyAllLivingFromHive((PlayerEntity)null, pState, BeehiveTileEntity.State.EMERGENCY);
         }
      }

      return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public static Direction getRandomOffset(Random pRandom) {
      return Util.getRandom(SPAWN_DIRECTIONS, pRandom);
   }
}
