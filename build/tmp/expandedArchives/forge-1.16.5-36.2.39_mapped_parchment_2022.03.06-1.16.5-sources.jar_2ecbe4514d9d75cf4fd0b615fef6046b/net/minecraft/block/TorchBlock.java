package net.minecraft.block;

import java.util.Random;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TorchBlock extends Block {
   protected static final VoxelShape AABB = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 10.0D, 10.0D);
   protected final IParticleData flameParticle;

   public TorchBlock(AbstractBlock.Properties pProperties, IParticleData pFlameParticle) {
      super(pProperties);
      this.flameParticle = pFlameParticle;
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return AABB;
   }

   /**
    * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific face passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, IWorld pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
      return pFacing == Direction.DOWN && !this.canSurvive(pState, pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
   }

   public boolean canSurvive(BlockState pState, IWorldReader pLevel, BlockPos pPos) {
      return canSupportCenter(pLevel, pPos.below(), Direction.UP);
   }

   /**
    * Called periodically clientside on blocks near the player to show effects (like furnace fire particles). Note that
    * this method is unrelated to {@link randomTick} and {@link #needsRandomTick}, and will always be called regardless
    * of whether the block can receive random update ticks
    */
   @OnlyIn(Dist.CLIENT)
   public void animateTick(BlockState pState, World pLevel, BlockPos pPos, Random pRand) {
      double d0 = (double)pPos.getX() + 0.5D;
      double d1 = (double)pPos.getY() + 0.7D;
      double d2 = (double)pPos.getZ() + 0.5D;
      pLevel.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
      pLevel.addParticle(this.flameParticle, d0, d1, d2, 0.0D, 0.0D, 0.0D);
   }
}