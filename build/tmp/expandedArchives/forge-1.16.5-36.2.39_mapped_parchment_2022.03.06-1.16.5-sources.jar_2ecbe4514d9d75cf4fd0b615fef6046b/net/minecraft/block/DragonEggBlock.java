package net.minecraft.block;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class DragonEggBlock extends FallingBlock {
   protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);

   public DragonEggBlock(AbstractBlock.Properties p_i48411_1_) {
      super(p_i48411_1_);
   }

   public VoxelShape getShape(BlockState pState, IBlockReader pLevel, BlockPos pPos, ISelectionContext pContext) {
      return SHAPE;
   }

   public ActionResultType use(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer, Hand pHand, BlockRayTraceResult pHit) {
      this.teleport(pState, pLevel, pPos);
      return ActionResultType.sidedSuccess(pLevel.isClientSide);
   }

   public void attack(BlockState pState, World pLevel, BlockPos pPos, PlayerEntity pPlayer) {
      this.teleport(pState, pLevel, pPos);
   }

   private void teleport(BlockState pState, World pLevel, BlockPos pPos) {
      for(int i = 0; i < 1000; ++i) {
         BlockPos blockpos = pPos.offset(pLevel.random.nextInt(16) - pLevel.random.nextInt(16), pLevel.random.nextInt(8) - pLevel.random.nextInt(8), pLevel.random.nextInt(16) - pLevel.random.nextInt(16));
         if (pLevel.getBlockState(blockpos).isAir()) {
            if (pLevel.isClientSide) {
               for(int j = 0; j < 128; ++j) {
                  double d0 = pLevel.random.nextDouble();
                  float f = (pLevel.random.nextFloat() - 0.5F) * 0.2F;
                  float f1 = (pLevel.random.nextFloat() - 0.5F) * 0.2F;
                  float f2 = (pLevel.random.nextFloat() - 0.5F) * 0.2F;
                  double d1 = MathHelper.lerp(d0, (double)blockpos.getX(), (double)pPos.getX()) + (pLevel.random.nextDouble() - 0.5D) + 0.5D;
                  double d2 = MathHelper.lerp(d0, (double)blockpos.getY(), (double)pPos.getY()) + pLevel.random.nextDouble() - 0.5D;
                  double d3 = MathHelper.lerp(d0, (double)blockpos.getZ(), (double)pPos.getZ()) + (pLevel.random.nextDouble() - 0.5D) + 0.5D;
                  pLevel.addParticle(ParticleTypes.PORTAL, d1, d2, d3, (double)f, (double)f1, (double)f2);
               }
            } else {
               pLevel.setBlock(blockpos, pState, 2);
               pLevel.removeBlock(pPos, false);
            }

            return;
         }
      }

   }

   protected int getDelayAfterPlace() {
      return 5;
   }

   public boolean isPathfindable(BlockState pState, IBlockReader pLevel, BlockPos pPos, PathType pType) {
      return false;
   }
}