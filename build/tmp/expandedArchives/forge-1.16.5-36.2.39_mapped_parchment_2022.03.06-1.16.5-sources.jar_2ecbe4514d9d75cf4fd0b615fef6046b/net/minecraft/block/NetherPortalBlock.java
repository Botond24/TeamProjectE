package net.minecraft.block;

import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class NetherPortalBlock extends Block {
   public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
   protected static final VoxelShape X_AXIS_AABB = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
   protected static final VoxelShape Z_AXIS_AABB = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);

   public NetherPortalBlock(AbstractBlock.Properties p_i48352_1_) {
      super(p_i48352_1_);
      this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      switch((Direction.Axis)pState.getValue(AXIS)) {
      case Z:
         return Z_AXIS_AABB;
      case X:
      default:
         return X_AXIS_AABB;
      }
   }

   /**
    * Performs a random tick on a block.
    */
   public void randomTick(BlockState pState, ServerWorld pLevel, BlockPos pPos, Random pRandom) {
      if (pLevel.dimensionType().natural() && pLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING) && pRandom.nextInt(2000) < pLevel.getDifficulty().getId()) {
         while(pLevel.getBlockState(pPos).is(this)) {
            pPos = pPos.below();
         }

         if (pLevel.getBlockState(pPos).isValidSpawn(pLevel, pPos, EntityType.ZOMBIFIED_PIGLIN)) {
            Entity entity = EntityType.ZOMBIFIED_PIGLIN.spawn(pLevel, (CompoundNBT)null, (ITextComponent)null, (PlayerEntity)null, pPos.above(), SpawnReason.STRUCTURE, false, false);
            if (entity != null) {
               entity.setPortalCooldown();
            }
         }
      }

   }

   /**
    * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific face passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      Direction.Axis direction$axis = pFacing.getAxis();
      Direction.Axis direction$axis1 = pState.getValue(AXIS);
      boolean flag = direction$axis1 != direction$axis && direction$axis.isHorizontal();
      return !flag && !pFacingState.is(this) && !(new PortalSize(pLevel, pCurrentPos, direction$axis1)).isComplete() ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public void entityInside(BlockState pState, World pLevel, BlockPos pPos, Entity pEntity) {
      if (!pEntity.isPassenger() && !pEntity.isVehicle() && pEntity.canChangeDimensions()) {
         pEntity.handleInsidePortal(pPos);
      }

   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
    * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
    * of whether the block can receive random update ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
      if (pRand.nextInt(100) == 0) {
         pLevel.playLocalSound((double)pPos.getX() + 0.5D, (double)pPos.getY() + 0.5D, (double)pPos.getZ() + 0.5D, SoundEvents.PORTAL_AMBIENT, SoundCategory.BLOCKS, 0.5F, pRand.nextFloat() * 0.4F + 0.8F, false);
      }

      for(int i = 0; i < 4; ++i) {
         double d0 = (double)pPos.getX() + pRand.nextDouble();
         double d1 = (double)pPos.getY() + pRand.nextDouble();
         double d2 = (double)pPos.getZ() + pRand.nextDouble();
         double d3 = ((double)pRand.nextFloat() - 0.5D) * 0.5D;
         double d4 = ((double)pRand.nextFloat() - 0.5D) * 0.5D;
         double d5 = ((double)pRand.nextFloat() - 0.5D) * 0.5D;
         int j = pRand.nextInt(2) * 2 - 1;
         if (!pLevel.getBlockState(pPos.west()).is(this) && !pLevel.getBlockState(pPos.east()).is(this)) {
            d0 = (double)pPos.getX() + 0.5D + 0.25D * (double)j;
            d3 = (double)(pRand.nextFloat() * 2.0F * (float)j);
         } else {
            d2 = (double)pPos.getZ() + 0.5D + 0.25D * (double)j;
            d5 = (double)(pRand.nextFloat() * 2.0F * (float)j);
         }

         pLevel.addParticle(ParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
      }

   }

   public ItemStack getCloneItemStack(IBlockReader pLevel, BlockPos pPos, BlockState pState) {
      return ItemStack.EMPTY;
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link IBlockState#withRotation(Rotation)} whenever possible. Implementing/overriding is
    * fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRotation) {
      switch(pRotation) {
      case COUNTERCLOCKWISE_90:
      case CLOCKWISE_90:
         switch((Direction.Axis)pState.getValue(AXIS)) {
         case Z:
            return pState.setValue(AXIS, Direction.Axis.X);
         case X:
            return pState.setValue(AXIS, Direction.Axis.Z);
         default:
            return pState;
         }
      default:
         return pState;
      }
   }

   protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> pBuilder) {
      pBuilder.add(AXIS);
   }
}